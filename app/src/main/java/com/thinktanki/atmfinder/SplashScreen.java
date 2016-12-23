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
        androidUtil.changeStatusBarColor();

        setContentView(R.layout.activity_splash_screen);

        //Checking for permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startApp();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startApp();

                } else {
                    finish();
                    Toast.makeText(SplashScreen.this, "Please provide the GPS permission to this app", Toast.LENGTH_LONG).show();

                }
                return;
            }
        }
    }

    /*Start the Application*/
    private void startApp() {
        if (!androidUtil.checkNetworkStatus()) {
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.internet_dialog_msg))
                    .setTitle(getResources().getString(R.string.internet_dialog_title))
                    .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .create()
                    .show();

        } else {

            trackGPS = new TrackGPS(this);
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
