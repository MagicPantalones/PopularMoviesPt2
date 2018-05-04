package io.magics.popularmovies;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

public class DebugAppClass extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)){
            return;
        }
        LeakCanary.install(this);
    }
}
