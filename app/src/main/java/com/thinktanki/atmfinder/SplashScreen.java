package com.thinktanki.atmfinder;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.thinktanki.atmfinder.util.AndroidUtil;
import com.thinktanki.atmfinder.util.TrackGPS;

import java.util.HashMap;
import java.util.Map;

public class SplashScreen extends AppCompatActivity {

    private AndroidUtil androidUtil;
    private TrackGPS trackGPS;
    final private int PERMISSION_REQUEST = 12;
    private String TAG = SplashScreen.class.getSimpleName();
    private SharedPreferences sharedPreferences;
    final private int WAIT_IN_MILLISECOND = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidUtil = new AndroidUtil(this);
        trackGPS = new TrackGPS(this);
        androidUtil.changeStatusBarColor();

        setContentView(R.layout.activity_splash_screen);

        //Checking for permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startApp();
        } else {
            androidUtil.checkAndRequestPermissions(PERMISSION_REQUEST);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d(TAG, "Permission callback called-------");
        switch (requestCode) {
            case PERMISSION_REQUEST: {

                Map<String, Integer> perms = new HashMap<>();
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "Precise location services permission granted");
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again ");
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                            showDialogOK("Access to GPS required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    androidUtil.checkAndRequestPermissions(PERMISSION_REQUEST);
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    finish();
                                                    Toast.makeText(SplashScreen.this, "Please provide the GPS permission to this app", Toast.LENGTH_LONG).show();
                                                    break;
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_SHORT)
                                    .show();
                            finish();
                        }
                    }
                }
            }
        }
    }

    /*Dialog for Asking permission*/
    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    /*Start the Application*/
    private void startApp() {
        if (!androidUtil.checkNetworkStatus()) {
            new AlertDialog.Builder(this)
                    .setMessage("Internet is not available.\nCheck your internet connectivity and try again")
                    .setTitle("No Internet")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .create()
                    .show();

        } else {
            if (trackGPS.canGetLocation()) {
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("LATITUDE", String.valueOf(trackGPS.getLatitude()));
                editor.putString("LONGITUDE", String.valueOf(trackGPS.getLongitude()));
                editor.putString("RADIUS", "1000");
                editor.commit();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                        startActivity(intent);
                        SplashScreen.this.finish();
                    }
                }, WAIT_IN_MILLISECOND);
            } else {
                trackGPS.showSettingsAlert();
            }
        }
    }
}
