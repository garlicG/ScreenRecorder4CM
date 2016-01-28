package com.garlicg.screenrecordct;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if(BuildConfig.USE_CRASHLYTICS){
            Fabric.with(this , new Crashlytics());
        }
    }
}
