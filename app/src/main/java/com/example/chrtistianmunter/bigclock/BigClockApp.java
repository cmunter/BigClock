package com.example.chrtistianmunter.bigclock;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;

/**
 * Created by chrtistianmunter on 8/2/17.
 */

public class BigClockApp extends Application {

    @Override public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
    }

}
