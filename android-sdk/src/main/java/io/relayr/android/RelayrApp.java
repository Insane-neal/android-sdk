package io.relayr.android;

import android.content.Context;

import dagger.ObjectGraph;
import io.relayr.java.RelayrJavaApp;
import retrofit.RestAdapter;

public class RelayrApp {

    private static Context sApplicationContext;
    private static RelayrApp sRelayrApp;
    private static ObjectGraph sObjectGraph;

    private RelayrApp(Context context, boolean mockMode) {
        sRelayrApp = this;
        sApplicationContext = context.getApplicationContext();
        buildObjectGraphAndInject(mockMode);
    }

    /**
     * Condition (sRelayrApp == null || mockMode) is used when Relayr app is already initialized
     * but you need to recreate it with another set of Dagger modules (e.g. while testing)
     * @param context  app context
     * @param mockMode true for debug mode and tests
     */
    private static void init(Context context, boolean mockMode) {
        reset();
        if (sRelayrApp == null || mockMode) {
            synchronized (new Object()) {
                if (sRelayrApp == null || mockMode) new RelayrApp(context, mockMode);
            }
        }
    }

    /**
     * Condition (sApp == null || mockMode) is used when Relayr app is already initialized
     * but you need to recreate it with another set of Dagger modules (e.g. while testing)
     * @param mock       true for debug mode and tests
     * @param production if true production API is used, if false it uses development environment
     * @param level      defines log level for all API calls -
     *                   {@link RestAdapter.LogLevel#NONE} by default for production -
     *                   {@link RestAdapter.LogLevel#BASIC} for development
     */
    public static void init(Context context, boolean mock, boolean production, boolean cacheModels, RestAdapter.LogLevel level) {
        RelayrJavaApp.setLogLevel(level);
        RelayrJavaApp.setProduction(production);
        RelayrJavaApp.setModelsCache(cacheModels);
        init(context, mock);
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
        sApplicationContext = null;
        sRelayrApp = null;
        sObjectGraph = null;
    }

}
