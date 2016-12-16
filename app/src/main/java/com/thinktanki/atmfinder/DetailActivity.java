package com.thinktanki.atmfinder;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private String TAG = DetailActivity.class.getSimpleName();
    private MapView detailMapview;
    private TextView atmName, atmAddress, atmDistance;
    private String currentLat, currentLng, destLat, destLng;
    private GoogleMap mMap;
    private Context context;
    private DataProvider dataProvider = new DataProvider();
    private String distance;
    private Bundle bundle;
    private List<List<HashMap<String, String>>> routes = null;
    private LatLng currentPosition, destPosition;
    private String nameATM, addressATM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        context = getApplicationContext();

        bundle = getIntent().getExtras();
        if (bundle != null) {
            nameATM = bundle.getString("ATM_NAME");
            addressATM = bundle.getString("ATM_ADDRESS");
            destLat = bundle.getString("LATITUDE");
            destLng = bundle.getString("LONGITUDE");
        }

        /*Initilize View*/
        detailMapview = (MapView) findViewById(R.id.detailMapView);
        atmName = (TextView) findViewById(R.id.atmName);
        atmAddress = (TextView) findViewById(R.id.atmAddress);
        atmDistance = (TextView) findViewById(R.id.atmDistance);

        detailMapview.onCreate(savedInstanceState);
        detailMapview.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        currentLat = sharedPreferences.getString("LATITUDE", null);
        currentLng = sharedPreferences.getString("LONGITUDE", null);
        try {
            MapsInitializer.initialize(context);
        } catch (Exception e) {
            Log.e(TAG + "MAP_INITILIZE", e.getMessage());
        }

        detailMapview.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setMapToolbarEnabled(true);
                destLat = "12.845872";
                destLng = "77.661911";
                new ATMRoute().execute(currentLat, currentLng, destLat, destLng);
            }
        });
    }

    private class ATMRoute extends AsyncTask<String, Void, String> {
        ProgressDialog pd = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(DetailActivity.this);
            pd.setMessage("Fetching ATM Details..");
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String lat_origin = params[0];
            String lng_origin = params[1];
            String lat_dest = params[2];
            String lng_dest = params[3];
            String mapRoute = dataProvider.mapRouteData(lat_origin, lng_origin, lat_dest, lng_dest);

            return mapRoute;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray jsonArray = jsonObject.getJSONArray("routes");

                distance = jsonArray.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getString("text");
                Log.v(TAG + " DISTANCE", distance);

                DirectionJSONParser parser = new DirectionJSONParser();
                // Starts parsing data
                routes = parser.parse(jsonObject);
            } catch (JSONException e) {
                Log.e(TAG + " ROUTE_PARSER", e.getMessage());
            }
            drawRouteonMap(routes);

            currentPosition = new LatLng(Double.parseDouble(currentLat), Double.parseDouble(currentLng));
            destPosition = new LatLng(Double.parseDouble(destLat), Double.parseDouble(destLng));
            CameraPosition cameraPosition = new CameraPosition.Builder().target(currentPosition).zoom(15).build();

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            mMap.addMarker(new MarkerOptions().position(currentPosition).title("ME").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            mMap.addMarker(new MarkerOptions().position(destPosition).title(nameATM).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).snippet(addressATM));

            atmName.setText(nameATM);
            atmAddress.setText(addressATM);
            atmDistance.setText(distance);

            pd.dismiss();
        }
    }

    private void drawRouteonMap(List<List<HashMap<String, String>>> routes) {
        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = null;
        // Traversing through all the routes
        for (int i = 0; i < routes.size(); i++) {
            points = new ArrayList<LatLng>();
            lineOptions = new PolylineOptions();
            // Fetching i-th route
            List<HashMap<String, String>> path = routes.get(i);
            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }
            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            lineOptions.width(5);
            lineOptions.color(Color.BLUE);
        }
        mMap.addPolyline(lineOptions);
    }
}
