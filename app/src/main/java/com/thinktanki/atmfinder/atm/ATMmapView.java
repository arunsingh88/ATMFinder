package com.thinktanki.atmfinder.atm;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.thinktanki.atmfinder.DataProvider;
import com.thinktanki.atmfinder.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ATMmapView extends Fragment {
    private final String TAG = ATMmapView.class.getSimpleName();
    private String RADIUS = "2000";
    private int noOfATM;
    private String atmName;
    private String atmAddress;
    private MapView mapView;
    private GoogleMap mMap;
    private List<ATM> atmList;
    private ATM atmObj;
    private String lat_dest, lng_dest, latitude, longitude;
    private LatLng marker;
    private SeekBar seekBar;
    private TextView radiusOfArea;
    private DataProvider dataProvider = new DataProvider();

    public ATMmapView() {
        atmList = new ArrayList<ATM>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_atmmap_view, container, false);
        mapView = (MapView) rootView.findViewById(R.id.mapView);
        seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        seekBar.setProgress(2);
        radiusOfArea = (TextView) rootView.findViewById(R.id.textView);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChanged = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radiusOfArea.setText("ATMs within range of " + progress + " kms.");
                progressChanged = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                String radius = String.valueOf(progressChanged * 1000);
                new ATMData().execute(latitude, longitude, radius);
                //radiusOfArea.setText(seekBar);

            }
        });
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        latitude = sharedPreferences.getString("LATITUDE", null);
        longitude = sharedPreferences.getString("LONGITUDE", null);
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            Log.e(TAG + "MAP_INITILIZE", e.getMessage());
        }

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;

                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setMapToolbarEnabled(true);

                new ATMData().execute(latitude, longitude, RADIUS);
            }
        });

        return rootView;

    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    private class ATMData extends AsyncTask<String, Void, String> {
        ProgressDialog pd = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(getActivity());
            pd.setMessage("Fetching ATM Details..");
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String latitude = params[0];
            String longitude = params[1];
            String radius = params[2];

            /*Fetching ATM List from Google API Server*/
            String atmData = dataProvider.ATMData(latitude, longitude, radius);
            return atmData;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pd.dismiss();
            addToATMList(result);

            Log.v(TAG + "ATMLIST", atmList.toString());
            int noOfATM = atmList.size();
            mMap.clear();
            LatLng currentPos = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));

            CircleOptions circleOptions = new CircleOptions().center(currentPos).radius(1000).strokeWidth(2)
                    .strokeColor(Color.BLUE);
            mMap.addCircle(circleOptions);
            CameraPosition cameraPosition = new CameraPosition.Builder().target(currentPos).zoom(14).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            mMap.addMarker(new MarkerOptions().position(currentPos).title("ME").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));

            List<Marker> markers = new ArrayList<Marker>();
            for (int i = 0; i < noOfATM; i++) {
                marker = new LatLng(atmList.get(i).getLatitude(), atmList.get(i).getLongitude());

                String title = atmList.get(i).getAtmName();
                String address = atmList.get(i).getAtmAddress();
                mMap.addMarker(new MarkerOptions().position(marker).title(title).snippet(address));

            }

           /* LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : markers) {
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();
            int padding = 0; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.moveCamera(cu);*/

        }
    }

    private void addToATMList(String result) {

        if (atmList.size() > 0) {
            atmList.clear();
        }
        if (result != null) {
            try {
                JSONObject jsonObj = new JSONObject(result);
                JSONArray jsonArray = jsonObj.getJSONArray("results");
                noOfATM = jsonArray.length();

                /*Adding ATM details in List*/
                for (int i = 0; i < noOfATM; i++) {
                    atmName = jsonArray.getJSONObject(i).getString("name");
                    atmAddress = jsonArray.getJSONObject(i).getString("vicinity");
                    lat_dest = jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lat");
                    lng_dest = jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lng");
                    atmObj = new ATM();
                    atmObj.setAtmName(atmName);
                    atmObj.setAtmAddress(atmAddress);
                    atmObj.setLatitude(Double.parseDouble(lat_dest));
                    atmObj.setLongitude(Double.parseDouble(lng_dest));

                    if (!atmList.contains(atmObj))
                        atmList.add(atmObj);
                }
                Log.v(TAG + "MAPVIEW_ARRAY", atmList.toString());

            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

}
