package com.async.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.async.app.model.User;
import com.google.gson.Gson;

public class SessionManager {
    private static final String PREF_NAME = "async_pref";
    private static final String KEY_USER = "logged_in_user";
    
    private static SessionManager instance;
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private final Gson gson;

    private SessionManager(Context context) {
        pref = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
        gson = new Gson();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    public void saveUser(User user) {
        String json = gson.toJson(user);
        editor.putString(KEY_USER, json);
        editor.apply();
    }

    public void saveToken(String token) {
        editor.putString("access_token", token);
        editor.apply();
    }

    public String getToken() {
        return pref.getString("access_token", null);
    }

    public User getUser() {
        String json = pref.getString(KEY_USER, null);
        if (json == null) {
            return null;
        }
        return gson.fromJson(json, User.class);
    }

    public void logout() {
        editor.remove(KEY_USER);
        editor.remove("access_token");
        editor.apply();
    }

    public boolean isLoggedIn() {
        return getUser() != null;
    }

    public void setDarkModeEnabled(boolean enabled) {
        editor.putBoolean("dark_mode_enabled", enabled);
        editor.apply();
    }

    public boolean isDarkModeEnabled() {
        return pref.getBoolean("dark_mode_enabled", false);
    }

    public void applyTheme() {
        boolean isDark = isDarkModeEnabled();
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
            isDark ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES 
                   : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}
