package com.thinktanki.atmfinder;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.thinktanki.atmfinder.util.TrackGPS;

/**
 * Created by aruns512 on 12/12/2016.
 */
public class ATMApplication extends Application {
    private SharedPreferences sharedPreferences;
    private Double lat, lng;
    private TrackGPS gps;

    @Override
    public void onCreate() {
        super.onCreate();

        gps = new TrackGPS(getApplicationContext());
        if (gps.canGetLocation()) {
            lng = gps.getLongitude();
            lat = gps.getLatitude();
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("LATITUDE", lat.toString());
            editor.putString("LONGITUDE", lng.toString());
            editor.putString("RADIUS", "1000");
            editor.commit();
        } else {
            gps.showSettingsAlert();
        }
    }

}
