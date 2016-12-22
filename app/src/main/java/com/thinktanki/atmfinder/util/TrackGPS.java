package com.thinktanki.atmfinder.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;

import com.thinktanki.atmfinder.R;

import static android.content.Context.LOCATION_SERVICE;


public class TrackGPS {
    private final Context mContext;
    private final String TAG = TrackGPS.class.getSimpleName();
    boolean checkGPS = false;
    boolean checkNetwork = false;
    boolean canGetLocation = false;

    Location loc;
    double latitude;
    double longitude;
    protected LocationManager locationManager;

    public TrackGPS(Context mContext) {
        this.mContext = mContext;
        getLocation();
    }

    private Location getLocation() {
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            checkGPS = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            checkNetwork = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!checkGPS && !checkNetwork) {
                Log.i(TAG, "No Service Provider Available");
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (checkNetwork) {
                    //Toast.makeText(mContext, "Network", Toast.LENGTH_SHORT).show();

                    try {
                        Log.d("Network", "Network");
                        if (locationManager != null) {
                            loc = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        }

                        if (loc != null) {
                            latitude = loc.getLatitude();
                            longitude = loc.getLongitude();
                        }
                    } catch (SecurityException e) {

                    }
                }
            }
            // if GPS Enabled get lat/long using GPS Services
            if (checkGPS) {
                if (loc == null) {
                    try {
                        Log.d(TAG, "GPS Enabled");
                        if (locationManager != null) {
                            loc = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (loc != null) {
                                latitude = loc.getLatitude();
                                longitude = loc.getLongitude();
                            }
                        }
                    } catch (SecurityException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return loc;
    }

    public double getLongitude() {
        if (loc != null) {
            longitude = loc.getLongitude();
        }
        return longitude;
    }

    public double getLatitude() {
        if (loc != null) {
            latitude = loc.getLatitude();
        }
        return latitude;
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle(mContext.getResources().getString(R.string.gps_not_enabled));
        alertDialog.setMessage(mContext.getResources().getString(R.string.gps_turnon_msg));
        alertDialog.setPositiveButton(mContext.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        alertDialog.setNegativeButton(mContext.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

}
