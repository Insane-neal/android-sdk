package io.relayr;

import android.content.Context;

import io.relayr.activity.UiModule;
import io.relayr.api.mock.DebugApiModule;
import io.relayr.ble.DebugBleModule;
import io.relayr.util.DebugUtilModule;
import io.relayr.websocket.DebugWebSocketModule;

final class DebugModules {
    static Object[] list(Context app) {
        return new Object[] {
                new RelayrModule(),
                new DebugApiModule(app),
                new DebugWebSocketModule(),
                new DebugBleModule(app),
                new DebugUtilModule(),
                new UiModule()
        };
    }

    private DebugModules() {
        // No instances.
    }
}

