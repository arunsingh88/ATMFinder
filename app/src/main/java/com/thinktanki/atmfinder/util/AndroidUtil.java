package com.thinktanki.atmfinder.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.view.ViewGroup.LayoutParams;

import com.thinktanki.atmfinder.R;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aruns512 on 19/12/2016.
 */

public class AndroidUtil {
    private Activity context;
    private PopupWindow popupWindow;
    private Intent shareApp;
    private SharedPreferences sharedPreferences;
    private String response;
    private String TAG = DataProvider.class.getSimpleName();

    public AndroidUtil(Activity context) {
        this.context = context;
    }

    public void rateApp() {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + context.getPackageName())));
        } catch (android.content.ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName())));
        }
    }

    public void shareApp() {
        String SHAREAPP = "https://play.google.com/store/apps/details?id=" + context.getPackageName();
        shareApp = new Intent();
        shareApp.setAction(Intent.ACTION_SEND);
        shareApp.putExtra(Intent.EXTRA_TEXT, SHAREAPP);
        shareApp.setType("text/plain");
        // Verify that the intent will resolve to an activity
        if (shareApp.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(shareApp);
        }
    }

    /*Description about app in popup window*/
    public void aboutApp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.about_app_title))
                .setMessage(Html.fromHtml(context.getResources().getString(R.string.about_app_desc)))
                .setNegativeButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void changeRadius() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final String[] RADIUS = {sharedPreferences.getString("RADIUS", "1000")};
        final String[] range = {RADIUS[0]};

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_range, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(layout)
                .setTitle(context.getResources().getString(R.string.range))
                .setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editor.putString("RADIUS", range[0]);
                        editor.commit();
                    }
                })
                .setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        /*Initilize the view for Dialog Box*/
        final TextView tv = (TextView) layout.findViewById(R.id.txtViewRadius);
        tv.setText("ATMs/Bank within the range of " + Integer.parseInt(RADIUS[0]) / 1000 + " kms.");

        SeekBar sb = (SeekBar) layout.findViewById(R.id.seekBarRadius);
        sb.setMax(10);
        sb.setProgress(Integer.parseInt(RADIUS[0]) / 1000);
        sb.getProgressDrawable().setColorFilter(context.getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            sb.getThumb().setColorFilter(context.getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_IN);
        }


        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressBar = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressBar = progress;
                tv.setText("ATMs/Bank within the range of " + progressBar + " kms.");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                range[0] = String.valueOf(progressBar * 1000);
            }
        });
    }

    public void updateCurrentLocation() {
        TrackGPS gps = new TrackGPS(context);
        if (gps.canGetLocation()) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("LATITUDE", String.valueOf(gps.getLatitude()));
            editor.putString("LONGITUDE", String.valueOf(gps.getLongitude()));
            editor.commit();
        } else {
            gps.showSettingsAlert();
        }
    }

    /*Change Status Bar Color*/
    public void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= 21) {
            context.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = context.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public String readATMData(InputStream stream) {

        try {
            final int bufferSize = 1024;
            final char[] buffer = new char[bufferSize];
            final StringBuilder out = new StringBuilder();
            Reader in = new InputStreamReader(stream, "UTF-8");
            for (; ; ) {
                int rsz = in.read(buffer, 0, buffer.length);
                if (rsz < 0)
                    break;
                out.append(buffer, 0, rsz);
            }

            response = out.toString();
        } catch (Exception e) {
            Log.e("HELPER_CLASS", e.getMessage());
        }
        return response;
    }

    /*Function to check Internet Connectivity*/
    public boolean checkNetworkStatus() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }
}
