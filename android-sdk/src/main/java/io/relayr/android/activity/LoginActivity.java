package io.relayr.android.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import javax.inject.Inject;

import io.relayr.android.R;
import io.relayr.android.RelayrApp;
import io.relayr.android.RelayrSdk;
import io.relayr.android.storage.DataStorage;
import io.relayr.android.storage.RelayrProperties;
import io.relayr.java.RelayrJavaApp;
import io.relayr.java.api.OauthApi;
import io.relayr.java.api.UserApi;
import io.relayr.java.helper.observer.SimpleObserver;
import io.relayr.java.model.OauthToken;
import io.relayr.java.model.User;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class LoginActivity extends Activity {

    private static final String TAG = "LoginActivity";

    @Inject OauthApi mOauthApi;
    @Inject UserApi mUserApi;

    private volatile boolean isObtainingAccessToken;
    private WebView mWebView;
    private View mLoadingView;
    private TextView mInfoText;
    private View mInfoView;

    public static void startActivity(Activity currentActivity) {
        Intent loginActivity = new Intent(currentActivity, LoginActivity.class);
        loginActivity.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        currentActivity.startActivity(loginActivity);
    }

    @SuppressLint("setJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RelayrApp.inject(this);
        View view = View.inflate(this, R.layout.login_layout, null);

        mWebView = (WebView) view.findViewById(R.id.web_view);
        mLoadingView = view.findViewById(R.id.loading_spinner);
        mInfoText = (TextView) view.findViewById(R.id.info_text);
        mInfoView = view.findViewById(R.id.info_view);

        setContentView(view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkConditions();
    }

    @Override public void onBackPressed() {
        Subscriber<? super User> subscriber = RelayrSdk.getLoginSubscriber();
        if (subscriber != null) subscriber.onCompleted();

        super.onBackPressed();
        finish();
    }

    /* Called from xml */
    public void onRetryClick(View view) {
        checkConditions();
    }

    /* Called from xml */
    public void onCancelClick(View view) {
        finish();
    }

    private void checkConditions() {
        showView(mLoadingView);

        if (!RelayrSdk.isPermissionGrantedToAccessInternet()) {
            showWarning(String.format(getString(R.string.permission_error), RelayrSdk.PERMISSION_INTERNET));
            return;
        }

        if (!RelayrSdk.isPermissionGrantedToAccessTheNetwork()) {
            showWarning(String.format(getString(R.string.permission_error), RelayrSdk.PERMISSION_NETWORK));
            return;
        }

        if (!RelayrSdk.isConnectedToInternet()) {
            showWarning(getString(R.string.network_error));
            return;
        }

        RelayrSdk.isPlatformReachable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override public void error(Throwable e) {
                        showWarning(getString(R.string.platform_error));
                    }

                    @Override public void success(Boolean status) {
                        if (status) showLogInScreen();
                        else showWarning(getString(R.string.platform_error));
                    }
                });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void showLogInScreen() {
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setVerticalScrollBarEnabled(false);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        showView(mWebView);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.d(TAG, "WebView opening: " + url);

                final String code = getCode(url);

                if (code != null && !isObtainingAccessToken) {
                    Log.d(TAG, "onPageStarted code: " + code);

                    isObtainingAccessToken = true;
                    mLoadingView.setVisibility(View.VISIBLE);
                    mWebView.setVisibility(View.GONE);
                    mOauthApi
                            .authoriseUser(
                                    code,
                                    RelayrProperties.get().appId,
                                    RelayrProperties.get().clientSecret,
                                    "authorization_code",
                                    RelayrProperties.get().redirectUri,
                                    "")
                            .flatMap(new Func1<OauthToken, Observable<User>>() {
                                @Override
                                public Observable<User> call(OauthToken token) {
                                    DataStorage.saveUserToken(token.type + " " + token.token);
                                    return mUserApi.getUserInfo();
                                }
                            })
                            .map(new Func1<User, User>() {
                                @Override
                                public User call(User user) {
                                    DataStorage.saveUserId(user.getId());
                                    return user;
                                }
                            })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SimpleObserver<User>() {
                                @Override
                                public void error(Throwable e) {
                                    Subscriber<? super User> subscriber = RelayrSdk.getLoginSubscriber();
                                    if (subscriber != null) subscriber.onError(e);
                                    finish();
                                }

                                @Override
                                public void success(User user) {
                                    Subscriber<? super User> subscriber = RelayrSdk.getLoginSubscriber();
                                    if (subscriber != null) subscriber.onNext(user);
                                    finish();
                                }
                            });
                }
            }
        });

        mWebView.loadUrl(getLoginUrl());
    }

    private void showWarning(String warning) {
        Log.e(TAG, warning);
        showView(mInfoView);
        mInfoText.setText(warning);
    }

    private void showView(View view) {
        mLoadingView.setVisibility(View.GONE);
        mWebView.setVisibility(View.GONE);
        mInfoView.setVisibility(View.GONE);

        view.setVisibility(View.VISIBLE);
    }

    private String getLoginUrl() {
        Uri.Builder uriBuilder = Uri.parse(RelayrJavaApp.getMainApiPoint()).buildUpon();
        uriBuilder.path("/oauth2/auth");

        uriBuilder.appendQueryParameter("client_id", RelayrProperties.get().appId);
        uriBuilder.appendQueryParameter("redirect_uri", RelayrProperties.get().redirectUri);
        uriBuilder.appendQueryParameter("response_type", "code");
        uriBuilder.appendQueryParameter("scope", "access-own-user-info configure-devices");

        return uriBuilder.build().toString();
    }

    static String getCode(String url) {
        String codeParam = "?code=";
        if (url.contains(RelayrProperties.get().redirectUri) && url.contains(codeParam)) {
            int tokenPosition = url.indexOf(codeParam);
            String code = url.substring(tokenPosition + codeParam.length());
            if (code.contains("&")) code = code.substring(0, code.indexOf("&"));
            Log.d(TAG, "Access code: " + code);
            return code;
        } else {
            return null;
        }
    }
}
