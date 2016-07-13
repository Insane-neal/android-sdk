package io.relayr.android;

import android.content.Context;

import dagger.ObjectGraph;
import io.relayr.android.storage.DataStorage;
import io.relayr.java.RelayrJavaApp;
import retrofit.RestAdapter;

public class RelayrApp {

    private static Context sApplicationContext;
    private static RelayrApp sRelayrApp;
    private static ObjectGraph sObjectGraph;

    private RelayrApp(boolean mockMode) {
        sRelayrApp = this;
        buildObjectGraphAndInject(mockMode);
    }

    /**
     * Condition (sRelayrApp == null || mockMode) is used when Relayr app is already initialized
     * but you need to recreate it with another set of Dagger modules (e.g. while testing)
     * @param mockMode true for debug mode and tests
     */
    private static void init(boolean mockMode) {
        reset();
        if (sRelayrApp == null || mockMode) {
            synchronized (new Object()) {
                if (sRelayrApp == null || mockMode) new RelayrApp(mockMode);
            }
        }
    }

    /**
     * Condition (sApp == null || mockMode) is used when Relayr app is already initialized
     * but you need to recreate it with another set of Dagger modules (e.g. while testing)
     * @param mockMode        true for debug mode and tests
     * @param production      if true production API is used, if false it uses development environment
     * @param cacheModels     true to load and cache all the device models
     * @param level           defines log level for all API calls - {@link RestAdapter.LogLevel#NONE}
     *                        by default for production, {@link RestAdapter.LogLevel#BASIC} for development
     * @param userAgent       agent identificator
     * @param mainApi         - relayr's main api url (default {@link RelayrJavaApp#API_DEFAULT_PROD})
     * @param mqttApi         - relayr's mqtt api url (default {@link RelayrJavaApp#API_DEFAULT_MQTT_PROD})
     * @param historyApi      - relayr's history api url (default {@link RelayrJavaApp#API_DEFAULT_HISTORY_PROD})
     * @param notificationApi - relayr's history api url (default {@link RelayrJavaApp#API_DEFAULT_HISTORY_PROD})
     */
    public static void init(Context context, boolean mockMode, boolean production, boolean cacheModels,
                            RestAdapter.LogLevel level, String userAgent,
                            String mainApi, String mqttApi, String historyApi, String notificationApi) {
        sApplicationContext = context.getApplicationContext();
        if (mainApi == null)
            RelayrJavaApp.init(DataStorage.getUserToken(), mockMode, production, cacheModels, level, userAgent);
        else
            RelayrJavaApp.init(DataStorage.getUserToken(), mockMode, production, cacheModels, level, userAgent, mainApi, mqttApi, historyApi, notificationApi);
        init(mockMode);
    }

    private static void buildObjectGraphAndInject(boolean mockMode) {
        sObjectGraph = mockMode ? ObjectGraph.create(DebugModules.list(sApplicationContext)) :
                ObjectGraph.create(Modules.list(sApplicationContext));
        sObjectGraph.injectStatics();
        sObjectGraph.inject(sRelayrApp);
    }

    public static void inject(Object o) {
        sObjectGraph.inject(o);
    }

    public static Context get() {
        return sApplicationContext;
    }

    private static void reset() {
        sRelayrApp = null;
        sObjectGraph = null;
    }
}
