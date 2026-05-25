package com.async.app;

import android.app.Application;
import com.async.app.util.SessionManager;

public class AsyncApplication extends Application {
    private static AsyncApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        SessionManager.getInstance(this).applyTheme();
    }

    public static AsyncApplication getInstance() {
        return instance;
    }
}
