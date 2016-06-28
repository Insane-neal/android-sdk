package io.relayr.android.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.relayr.android.RelayrApp;
import io.relayr.android.RelayrSdk;
import io.relayr.java.api.CloudApi;
import io.relayr.java.model.Status;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

@Singleton
public class ReachabilityUtils {

    private static final String TAG = "ReachabilityUtils";

    private CloudApi mApi;
    private Map<String, Boolean> sPermissions;

    @Inject ReachabilityUtils(CloudApi api) {
        mApi = api;
        sPermissions = new HashMap<>();
    }

    public Observable<Boolean> isPlatformReachable() {
        if (!isConnectedToInternet()) return emptyResult();
        else return platformAvailable();
    }

    public boolean isConnectedToInternet() {
        if (!isPermissionGranted(RelayrSdk.PERMISSION_NETWORK)) return false;

        ConnectivityManager manager = (ConnectivityManager) RelayrApp.get().getSystemService(Context
                .CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    public boolean isPermissionGranted(String permission) {
        if (permission == null || permission.isEmpty()) return false;

        if (sPermissions.get(permission) != null) return sPermissions.get(permission);

        Context appContext = RelayrApp.get().getApplicationContext();
        try {
            PackageInfo info = appContext.getPackageManager()
                    .getPackageInfo(appContext.getPackageName(), PackageManager.GET_PERMISSIONS);
            if (info.requestedPermissions != null) {
                for (String p : info.requestedPermissions) {
                    if (p.equals(permission)) {
                        sPermissions.put(permission, true);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.w(TAG, "Permission " + permission + " doesn't exist in AndroidManifest file.");

        return false;
    }

    public Observable<Boolean> isPlatformAvailable() {
        if (!isPermissionGranted(RelayrSdk.PERMISSION_INTERNET)) return emptyResult();
        else return platformAvailable();

    }

    //FIXME
    private Observable<Boolean> platformAvailable() {
        return Observable.just(true);
        //        return mApi.getServerStatus()
        //                .map(new Func1<Status, Boolean>() {
        //                    @Override
        //                    public Boolean call(Status status) {
        //                        return status != null && status.getDatabase() != null && status.getDatabase().equals("ok");
        //                    }
        //                });
    }

    private Observable<Boolean> emptyResult() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> sub) {
                sub.onNext(false);
            }
        });
    }
}
