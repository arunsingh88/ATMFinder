package com.thinktanki.atmfinder.util;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.thinktanki.atmfinder.BuildConfig;
import com.thinktanki.atmfinder.R;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by aruns512 on 12/12/2016.
 */
public class DataProvider {
    private String response;
    private HttpURLConnection urlConnection;
    private Context context;
    private String TAG = DataProvider.class.getSimpleName();

    public DataProvider(Context context)
    {
        this.context=context;
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

    public String ATMData(String latitude, String longitude, String radius, String type) {
        String atmList = null;
        final String GOOGLE_PLACE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
        final String RADIUS = "radius";
        final String API_KEY = "key";
        final String CURRENT_LOCATION = "location";
        final String TYPE = "type";
        try {
            Uri builtUri = Uri.parse(GOOGLE_PLACE_URL).buildUpon()
                    .appendQueryParameter(CURRENT_LOCATION, latitude + "," + longitude)
                    .appendQueryParameter(RADIUS, radius)
                    .appendQueryParameter(TYPE, type)
                    .appendQueryParameter(API_KEY,context.getResources().getString(R.string.GOOGLE_PLACE_API_KEY) )
                    .build();

            URL url = new URL(builtUri.toString());
            Log.v(TAG + "PLACE_API_URL", url.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            // Starts the query
            urlConnection.connect();
            int response = urlConnection.getResponseCode();
            Log.d(TAG + "ATMData", "The response is: " + response);
            InputStream in = urlConnection.getInputStream();
            // Convert the InputStream into a string
            atmList = readATMData(in);
            return atmList;
        } catch (Exception e) {
            Log.e(TAG + "PLACE_API_CALL", e.getMessage());
        } finally {
            urlConnection.disconnect();
        }

        return atmList;
    }

    public Float distanceInKm(String lat_origin, String lng_origin, String lat_dest, String lng_dest) {
        Location origin = new Location("ORIGIN");
        origin.setLatitude(Double.parseDouble(lat_origin));
        origin.setLongitude(Double.parseDouble(lng_origin));

        Location destination = new Location("DESTINATION");
        destination.setLatitude(Double.parseDouble(lat_dest));
        destination.setLongitude(Double.parseDouble(lng_dest));

        Float distanceInKm = origin.distanceTo(destination) / 1000;
        distanceInKm = Float.parseFloat(String.format("%.2f", distanceInKm));
        return distanceInKm;
    }

    public String mapRouteData(String lat_origin, String lng_origin, String lat_dest, String lng_dest) {
        final String GOOGLE_DISTANCE_URL = "http://maps.google.com/maps/api/directions/json?";
        final String ORIGIN = "origin";
        final String DESTINATION = "destination";
        final String SENSOR = "sensor";
        final String UNITS = "units";

        try {
            Uri builtUri = Uri.parse(GOOGLE_DISTANCE_URL).buildUpon()
                    .appendQueryParameter(ORIGIN, lat_origin + "," + lng_origin)
                    .appendQueryParameter(DESTINATION, lat_dest + "," + lng_dest)
                    .appendQueryParameter(SENSOR, "false")
                    .appendQueryParameter(UNITS, "metric")
                    .build();

            URL url = new URL(builtUri.toString());
            Log.v(TAG + "DIRECTION URL", url.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            // Starts the query
            urlConnection.connect();
            int responseCode = urlConnection.getResponseCode();
            Log.d(TAG + "DIRECTION RESPONSE", "The response is: " + responseCode);
            InputStream is = urlConnection.getInputStream();
            response = readATMData(is);
        } catch (Exception e) {
            Log.e(TAG + "DIRECTION_EXCEPTION", e.getMessage());
        }
        return response;
    }
}
