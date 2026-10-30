package com.example.personalfinanceexpensetrackerandroidapp.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class SecurityUtils {
    private static final String PREF_NAME = "moneyflow_security_prefs";
    private static final String KEY_PIN = "pin_code";
    private static final String KEY_APP_LOCK = "app_lock_enabled";
    private static final String KEY_BIOMETRIC = "biometric_enabled";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_INSTALL_TIMESTAMP = "install_timestamp";
    private static final String KEY_IS_REGISTERED = "is_registered";
    private static final String KEY_USER_NAME = "user_name";

    private static boolean sIsUnlockedInSession = false;

    public static long getInstallTimestamp(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long installTime = prefs.getLong(KEY_INSTALL_TIMESTAMP, 0L);
        if (installTime == 0L) {
            installTime = System.currentTimeMillis();
            prefs.edit().putLong(KEY_INSTALL_TIMESTAMP, installTime).apply();
        }
        return installTime;
    }

    public static boolean isUserRegistered(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_REGISTERED, false);
    }

    public static void setUserRegistered(Context context, boolean registered) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_IS_REGISTERED, registered).apply();
    }

    public static String getUserName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_NAME, "Alex Morgan");
    }

    public static void setUserName(Context context, String name) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_USER_NAME, name).apply();
    }

    public static boolean isAppLockEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_APP_LOCK, false);
    }

    public static void setAppLockEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_APP_LOCK, enabled).apply();
    }

    public static boolean isBiometricEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_BIOMETRIC, false);
    }

    public static void setBiometricEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_BIOMETRIC, enabled).apply();
    }

    public static String getSavedPin(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_PIN, "");
    }

    public static void savePin(Context context, String pin) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_PIN, pin).apply();
    }

    public static int getThemeMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public static void setThemeMode(Context context, int mode) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public static boolean isUnlockedInSession() {
        return sIsUnlockedInSession;
    }

    public static void setUnlockedInSession(boolean unlocked) {
        sIsUnlockedInSession = unlocked;
    }
}
