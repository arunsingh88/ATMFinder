package com.thinktanki.atmfinder.atm;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.thinktanki.atmfinder.R;
import com.thinktanki.atmfinder.util.DataProvider;
import com.thinktanki.atmfinder.util.DirectionJSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private String TAG = DetailActivity.class.getSimpleName();
    private MapView detailMapview;
    private String type;
    private TextView atmAddress, atmDistance;
    private AdView adView;
    private AdRequest adRequest;
    private String currentLat, currentLng, destLat, destLng;
    private GoogleMap mMap;
    private Context context;
    private DataProvider dataProvider;
    private String distance;
    private Bundle bundle;
    private List<List<HashMap<String, String>>> routes = null;
    private LatLng currentPosition, destPosition;
    private String nameATM, addressATM;
    private final Handler refreshHandler = new Handler();
    private final Runnable refreshRunnable = new RefreshRunnable();
    private final int REFRESH_RATE_IN_SECONDS = 1;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context = getApplicationContext();

        bundle = getIntent().getExtras();
        if (bundle != null) {
            type = bundle.getString("TYPE");
            nameATM = bundle.getString("ATM_NAME");
            addressATM = bundle.getString("ATM_ADDRESS");
            Double doubleLat = bundle.getDouble("LATITUDE");
            Double doubleLng = bundle.getDouble("LONGITUDE");
            destLat = doubleLat.toString();
            destLng = doubleLng.toString();
            distance = bundle.getString("DISTANCE");
        }

        dataProvider = new DataProvider(this);

        setTitle(nameATM);
        /*Initilize View*/
        detailMapview = (MapView) findViewById(R.id.detailMapView);
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
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        String baseUri = "http://maps.google.com/maps?saddr=%s,%s&daddr=%s,%s";
                        String uri = String.format(baseUri, currentLat, currentLng, marker.getPosition().latitude, marker.getPosition().longitude);
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setPackage("com.google.android.apps.maps");
                        if (!currentLat.equalsIgnoreCase(String.valueOf(marker.getPosition().latitude)))
                            startActivity(intent);
                        return false;
                    }
                });

                currentPosition = new LatLng(Double.parseDouble(currentLat), Double.parseDouble(currentLng));
                destPosition = new LatLng(Double.parseDouble(destLat), Double.parseDouble(destLng));

                List<Marker> marker = new ArrayList<Marker>();
                mMap.addMarker(new MarkerOptions().position(currentPosition).title(getResources().getString(R.string.marker_you)).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_me))).showInfoWindow();

                marker.add(mMap.addMarker(new MarkerOptions().position(currentPosition).title(getResources().getString(R.string.marker_you)).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_me))));
                if(type.equalsIgnoreCase("atm")) {
                    marker.add(mMap.addMarker(new MarkerOptions().position(destPosition).title(nameATM).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_atm_marker)).snippet(addressATM)));
                }
                else
                {
                    marker.add(mMap.addMarker(new MarkerOptions().position(destPosition).title(nameATM).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bank_marker)).snippet(addressATM)));

                }


                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Marker markers : marker) {
                    builder.include(markers.getPosition());
                }


                LatLngBounds bounds = builder.build();
                int width = getResources().getDisplayMetrics().widthPixels;
                int height = getResources().getDisplayMetrics().heightPixels;
                int padding = (int) (width * 0.30);

                LatLng currentPos = new LatLng(Double.parseDouble(currentLat), Double.parseDouble(currentLng));
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
                CircleOptions circleOptions = new CircleOptions().center(currentPos).radius(Double.parseDouble(distance)).strokeWidth(3)
                        .strokeColor(Color.BLUE);
                mMap.addCircle(circleOptions);
                mMap.moveCamera(cameraUpdate);

                drawRoute(currentPosition,destPosition);

                //atmName.setText(nameATM);
                atmAddress.setText(addressATM);

                if (type.equalsIgnoreCase("atm")) {
                    atmDistance.setText("This ATM is " + distance + " km from you.");
                }
                else if (type.equalsIgnoreCase("bank"))
                {
                    atmDistance.setText("This BANK is " + distance + " km from you.");
                }
                else
                {
                    atmDistance.setText("This BANK/ATM is " + distance + " km from you.");
                }
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                    Log.d("TAG", "The interstitial loaded.");
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                }

                //new ATMRoute().execute(currentLat, currentLng, destLat, destLng);
            }
        });

        /*Initilialize Advertisement*/
        adView = (AdView) findViewById(R.id.adViewActivity);
        adView.setVisibility(View.GONE);

        // Initialize the Mobile Ads SDK.
        //MobileAds.initialize(this, getResources().getString(R.string.atm_detail_page_footer));
        adRequest = new AdRequest.Builder().build();

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.fullpage_ad));
        mInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice("110A4DDA99F50844536842DEDAF9DB32").build());


        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                //adView.setVisibility(View.GONE);
                refreshHandler.removeCallbacks(refreshRunnable);
                refreshHandler.postDelayed(refreshRunnable, REFRESH_RATE_IN_SECONDS * 1000);
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adView.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class ATMRoute extends AsyncTask<String, Void, String> {
        ProgressDialog pd = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(DetailActivity.this);
            pd.setMessage(getResources().getString(R.string.atm_loader_message));
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

            List<Marker> marker = new ArrayList<Marker>();
            mMap.addMarker(new MarkerOptions().position(currentPosition).title(getResources().getString(R.string.marker_you)).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_me))).showInfoWindow();

            marker.add(mMap.addMarker(new MarkerOptions().position(currentPosition).title(getResources().getString(R.string.marker_you)).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_me))));
            marker.add(mMap.addMarker(new MarkerOptions().position(destPosition).title(nameATM).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_atm_marker)).snippet(addressATM)));


            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker markers : marker) {
                builder.include(markers.getPosition());
            }
            LatLngBounds bounds = builder.build();
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.30);

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
            mMap.moveCamera(cameraUpdate);
            //atmName.setText(nameATM);
            atmAddress.setText(addressATM);
            atmDistance.setText("This ATM is " + distance + " from you.");

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
            lineOptions.width(7);
            lineOptions.color(getResources().getColor(R.color.colorPrimaryDark));
        }
        mMap.addPolyline(lineOptions);
    }

    private class RefreshRunnable implements Runnable {
        @Override
        public void run() {
            adView.loadAd(adRequest);
        }
    }

    private void drawRoute(LatLng currentPosition,LatLng destPosition){
        ArrayList<LatLng> points = new ArrayList<LatLng>();
        PolylineOptions lineOptions = new PolylineOptions();
        points.add(currentPosition);
        points.add(destPosition);

        // Adding all the points in the route to LineOptions
        lineOptions.addAll(points);
        lineOptions.width(7);
        lineOptions.color(getResources().getColor(R.color.colorPrimaryDark));
        mMap.addPolyline(lineOptions);

    }
}

