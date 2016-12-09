package io.relayr.android;

import android.app.Activity;
import android.content.Context;

import javax.inject.Inject;

import io.relayr.android.activity.LoginActivity;
import io.relayr.android.api.Utils;
import io.relayr.android.ble.BleUtils;
import io.relayr.android.ble.RelayrBleSdk;
import io.relayr.android.log.Logger;
import io.relayr.android.storage.DataStorage;
import io.relayr.android.util.ReachabilityUtils;
import io.relayr.java.RelayrJavaSdk;
import io.relayr.java.model.Device;
import io.relayr.java.model.Transmitter;
import io.relayr.java.model.User;
import io.relayr.java.model.account.Account;
import io.relayr.java.model.groups.Group;
import retrofit.RestAdapter.LogLevel;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * The RelayrSdk Class serves as the access point to all endpoints in the Android SDK.
 * It includes basic calls such as user login validation and can also call the handlers of the
 * other classes- {@link io.relayr.java.api.RelayrApi}, {@link RelayrBleSdk} and
 * {@link io.relayr.java.websocket.WebSocketClient}.
 * -----------------------------------------------------------------------------------------------
 * For easy start, after logging in, obtain {@link User} by calling {@link #getUser()}.
 * This is main object in Relayr SDK that can be used to fetch users {@link Device},
 * {@link Transmitter}, {@link Group} and {@link Account} objects.
 * Every mentioned object has it's own interaction methods so direct usage of APIs is not necessary.
 * However it's still possible to obtain any of the desired API handlers through appropriate method:
 * <ul>
 * <li>{@link #getUserApi()}</li>
 * <li>{@link #getRelayrApi()}</li>
 * <li>{@link #getDeviceApi()}</li>
 * <li>{@link #getGroupsApi()}</li>
 * <li>{@link #getAccountsApi()}</li>
 * <li>{@link #getPublishersApi()}</li>
 * <li>{@link #getProjectsApi()}</li>
 * <li>{@link #getRuleTemplateApi()}</li>
 * <li>{@link #getHistoryApi()} or using {@link io.relayr.java.helper.HistoryHelper}</li>
 * <li>{@link #getDeviceModelsApi()} or using cache {@link #getDeviceModelsCache()} if
 * {@link io.relayr.android.RelayrSdk.Builder#cacheModels(boolean)} is set as true </li>
 * </ul>
 * -----------------------------------------------------------------------------------------------
 * For other details check JavaDoc and other documentation:
 * Java-SDK: @see <a href="https://developer.relayr.io/documents/Java/Introduction"></a> and
 * @see <a href="http://relayr.github.io/java-sdk/"></a>
 * Android SDK: @see <a href="https://developer.relayr.io/documents/Android/Reference"></a> and
 * @see <a href="https://developer.relayr.io/rendered-doc/javadoc/index.html"></a>
 */
public class RelayrSdk extends RelayrJavaSdk {

    public static final String PERMISSION_INTERNET = "android.permission.INTERNET";
    public static final String PERMISSION_NETWORK = "android.permission.ACCESS_NETWORK_STATE";
    public static final String PERMISSION_BLUETOOTH = "android.permission.BLUETOOTH";
    public static final String PERMISSION_BLUETOOTH_ADMIN = "android.permission.BLUETOOTH_ADMIN";

    @Inject static Logger loggerUtils;
    @Inject static ReachabilityUtils reachabilityUtils;
    @Inject static BleUtils bleUtils;
    @Inject static RelayrBleSdk relayrBleSdk;

    private static Subscriber<? super User> loginSubscriber;

    /**
     * Initializes the SDK. Should be built when the {@link android.app.Application} is
     * created.
     */
    public static class Builder {

        private final Context mContext;

        private boolean cacheModels = false;
        private boolean mockMode = false;
        private boolean production = true;

        private LogLevel level;
        private String userAgent = Utils.getUserAgent();
        private String mainApi;
        private String mqttApi;

        public Builder(Context context) {
            if (context == null) throw new NullPointerException("Context can not be NULL");
            mContext = context;
        }

        /**
         * Initializes the SDK in Mock Mode.
         * In this mode, mock reading values are generated.
         * Used for testing purposes, without the need of a WunderBar or an internet connection.
         * @param inMockMode - default FALSE.
         * @return {@link io.relayr.android.RelayrSdk.Builder}
         */
        public Builder inMockMode(boolean inMockMode) {
            mockMode = inMockMode;
            return this;
        }

        /**
         * Initializes the SDK in production or development mode.
         * Use each environment with corresponding API key.
         * @param production - Default TRUE (uses production).
         * @return {@link io.relayr.android.RelayrSdk.Builder}
         */
        public Builder useProduction(boolean production) {
            this.production = production;
            return this;
        }

        /**
         * @param cache - If true it will cache all device models in the background (default FALSE)
         * @return {@link io.relayr.android.RelayrSdk.Builder}
         */
        public Builder cacheModels(boolean cache) {
            this.cacheModels = cache;
            return this;
        }

        /**
         * Sets Log level for all API calls.
         * Defaults: in production {@link retrofit.RestAdapter.LogLevel#NONE}
         * in development {@link retrofit.RestAdapter.LogLevel#BASIC}
         * @param level - {@link retrofit.RestAdapter.LogLevel}
         * @return {@link io.relayr.android.RelayrSdk.Builder}
         */
        public Builder setLogLevel(LogLevel level) {
            if (level == null) throw new NullPointerException("Log level can not be NULL");
            this.level = level;
            return this;
        }

        /** Define specific user agent for your app */
        public Builder setUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        /** Set specific API urls. If not used, default relayr APIs are used. */
        public Builder setApiUrls(String mainApi, String mqttApi) {
            if (mainApi == null || mqttApi == null) throw new NullPointerException("Api endpoint can not be NULL!");
            this.mainApi = mainApi;
            this.mqttApi = mqttApi;
            return this;
        }

        public void build() {
            RelayrApp.init(mContext, mockMode, production, cacheModels, level, userAgent, mainApi, mqttApi);
        }
    }

    /**
     * Returns the version of the SDK
     * @return the version String
     */
    public static String getVersion() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * Launches the login activity. Enables the user to log in to the relayr platform.
     * Returns
     * <ul>
     * <li>onNext() with user object if login is successful</li>
     * <li>onError() if there was an error</li>
     * <li>onCompleted() if user stopped the login </li>
     * </ul>
     * @param activity - activity context
     * @return {@link Observable<User>} calls onNext() for success, onError() for error and onCompleted() for cancellation
     */
    public static Observable<User> logIn(Activity activity) {
        return logIn(activity, false);
    }

    /**
     * Launches the login activity. Enables the user to log in to the relayr platform.
     * Returns
     * <ul>
     * <li>onNext() with user object if login is successful</li>
     * <li>onError() if there was an error</li>
     * <li>onCompleted() if user stopped the login </li>
     * </ul>
     * @param activity      - activity context
     * @param finishOnError - activity will call finish() and return error that caused it (user can then show it's own warning)
     * @return {@link Observable<User>} calls onNext() for success, onError() for error and onCompleted() for cancellation
     */
    public static Observable<User> logIn(Activity activity, boolean finishOnError) {
        if (activity == null) throw new NullPointerException("Activity can not be NULL!");

        Observable.OnSubscribe<User> onSubscribe = new Observable.OnSubscribe<User>() {
            @Override
            public void call(Subscriber<? super User> subscriber) {
                loginSubscriber = subscriber;
            }
        };
        LoginActivity.startActivity(activity, finishOnError);
        return Observable
                .create(onSubscribe)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Checks whether or not a user is logged in to the relayr platform.
     * @return true if the user is logged in, false otherwise.
     */
    public static boolean isUserLoggedIn() {
        return DataStorage.isUserLoggedIn();
    }

    /** Logs the user out of the relayr platform. */
    public static void logOut() {
        DataStorage.logOut();
    }

    /**
     * Logs an event in the relayr platform. In debug mode, the event will be logged in the console
     * instead. In production mode messages will be saved locally and logged to platform
     * in bulks after every 5 logged messages. Connection availability and platform reachability
     * are checked automatically when using this method.
     * @param message - string message to log
     * @return whether the logging event was performed
     */
    public static boolean logMessage(String message) {
        return loggerUtils.logMessage(message);
    }

    /**
     * Sends bulk of all previously logged messages to relayr platform. Connection availability
     * and platform reachability are checked automatically when using this method.
     * @return whether the messages were flushed
     */
    public static boolean flushLoggedMessages() {
        return loggerUtils.flushLoggedMessages();
    }

    /**
     * Checks if android.permission.INTERNET permission is granted by application.
     * @return true if granted, false otherwise
     */
    public static boolean isPermissionGrantedToAccessInternet() {
        return reachabilityUtils.isPermissionGranted(PERMISSION_INTERNET);
    }

    /**
     * Checks if android.permission.ACCESS_NETWORK_STATE permission is granted by application.
     * @return true if granted, false otherwise
     */
    public static boolean isPermissionGrantedToAccessTheNetwork() {
        return reachabilityUtils.isPermissionGranted(PERMISSION_NETWORK);
    }

    /**
     * Checks if android.permission.BLUETOOTH permission is granted by application.
     * @return true if granted, false otherwise
     */
    public static boolean isPermissionGrantedBluetooth() {
        return reachabilityUtils.isPermissionGranted(PERMISSION_BLUETOOTH);
    }

    /**
     * Checks if android.permission.BLUETOOTH_ADMIN permission is granted by application.
     * @return true if granted, false otherwise
     */
    public static boolean isPermissionGrantedBluetoothAdmin() {
        return reachabilityUtils.isPermissionGranted(PERMISSION_BLUETOOTH_ADMIN);
    }

    /**
     * Checks connection to the Internet.
     * @return true if connected, false otherwise
     */
    public static boolean isConnectedToInternet() {
        return reachabilityUtils.isConnectedToInternet();
    }

    /**
     * Checks relayr platform reachability.
     * @return true if platform is reachable, false otherwise
     */
    public static Observable<Boolean> isPlatformReachable() {
        return reachabilityUtils.isPlatformReachable();
    }

    /**
     * Provides the relayr sdk with a BLE implementation or an empty implementation, in case
     * bluetooth is not available on the device.
     * An empty implementation is one in which the methods do not function
     * This call should be preceded by {@link RelayrSdk#isBleSupported}
     * to check whether BLE is supported
     * and by {@link RelayrSdk#isBleAvailable} to check whether BLE is activated
     * @return the handler of the Relayr BLE SDK
     */
    public static RelayrBleSdk getRelayrBleSdk() {
        return relayrBleSdk;
    }

    /**
     * Checks whether or not Bluetooth is supported.
     * Should be called before the RelayrBleSdk handler {@link #getRelayrBleSdk}
     * @return true if Bluetooth is supported, false otherwise.
     */
    public static boolean isBleSupported() {
        return bleUtils.isBleSupported();
    }

    /**
     * Checks whether Bluetooth is turned on or not.
     * @return true if Bluetooth is turned on, false otherwise.
     * Should be called before calling the RelayrBleSdk handler {@link #getRelayrBleSdk}.
     * The user can be prompted to activate their Bluetooth using
     * {@link #promptUserToActivateBluetooth}
     */
    public static boolean isBleAvailable() {
        return bleUtils.isBleAvailable();
    }

    /**
     * Prompts the user to activate Bluetooth.
     * The method will not perform any action in case Bluetooth is not supported,
     * i.e. if {@link #isBleSupported()} returns true.
     * @param activity an instance of {@link android.app.Activity}
     */
    public static void promptUserToActivateBluetooth(Activity activity) {
        if (isBleSupported()) bleUtils.promptUserToActivateBluetooth(activity);
    }

    /**
     * Listener indicating a 'login' event
     * @return the listener or null if doesn't exist
     */
    public static Subscriber<? super User> getLoginSubscriber() {
        return loginSubscriber;
    }
}
