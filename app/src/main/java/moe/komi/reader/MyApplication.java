package moe.komi.reader;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import io.fabric.sdk.android.Fabric;

public class MyApplication extends Application {

    Tracker tracker;

    synchronized Tracker getTracker() {
        if (tracker == null) {
            tracker = GoogleAnalytics.getInstance(this).newTracker(R.xml.global_tracker);
        }

        return tracker;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        getTracker();
    }
}
