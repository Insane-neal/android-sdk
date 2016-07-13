package io.relayr.android.storage;

import android.content.Context;
import android.content.SharedPreferences;

import io.relayr.android.RelayrApp;
import io.relayr.java.RelayrJavaApp;
import io.relayr.java.model.OauthToken;

public class DataStorage {

    private static final String STORAGE_FILE = "relayr_sdk_storage";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_TOKEN = "user_token";
    private static final SharedPreferences STORAGE =
            RelayrApp.get().getSharedPreferences(STORAGE_FILE, Context.MODE_PRIVATE);

    public static void saveUserId(String userId) {
        STORAGE.edit().putString(KEY_USER_ID, userId).apply();
    }

    public static boolean isUserLoggedIn() {
        return STORAGE.contains(KEY_USER_TOKEN) && STORAGE.contains(KEY_USER_ID);
    }

    public static String getUserId() {
        return STORAGE.getString(KEY_USER_ID, "");
    }

    public static String getUserToken() {
        return STORAGE.getString(KEY_USER_TOKEN, "");
    }

    public static void logOut() {
        STORAGE.edit().clear().apply();
    }

    public static void saveUserToken(OauthToken token) {
        saveUserToken(token.type + " " + token.token);
    }

    private static void saveUserToken(String userToken) {
        RelayrJavaApp.setToken(userToken);
        STORAGE.edit().putString(KEY_USER_TOKEN, userToken).apply();
    }
}
