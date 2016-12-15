package com.thinktanki.atmfinder;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

/**
 * Created by aruns512 on 12/12/2016.
 */
public class ATMApplication extends Application {

    private LocationManager locManager;
    private boolean gps_enabled;
    private Location location;
    private SharedPreferences sharedPreferences;
    private Double lat, lng;

    @Override
    public void onCreate() {
        super.onCreate();
        locManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        gps_enabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (gps_enabled) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                lat = location.getLatitude();
                lng = location.getLongitude();
                Log.v("CURRENT LOCATION:", "latitude" + lat.toString() + " : longitude" + lng.toString());
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("LATITUDE", lat.toString());
                editor.putString("LONGITUDE", lng.toString());
                editor.commit();
            }
        }

    }

}
