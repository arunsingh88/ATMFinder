package com.thinktanki.atmfinder.atm;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thinktanki.atmfinder.DataProvider;
import com.thinktanki.atmfinder.R;
import com.thinktanki.atmfinder.listview.ATMAdapter;
import com.thinktanki.atmfinder.listview.RecyclerViewDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ATMlistView extends Fragment {
    private final String TAG = ATMlistView.class.getSimpleName();
    private List<ATM> atmList;
    private RecyclerView recyclerView;
    private ATMAdapter atmAdapter;
    private DataProvider dataProvider = new DataProvider();
    private String latitude, lat_dest;
    private String longitude, lng_dest;
    private String atmName, atmAddress;
    private Float distanceInKms;
    private int noOfATMs;
    private String RADIUS = "2000";
    private SharedPreferences sharedPreferences;
    private ATM atmObj;

    public ATMlistView() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_atmlist_view, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        atmList = new ArrayList<ATM>();
        atmAdapter = new ATMAdapter(atmList,getActivity());

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new RecyclerViewDecoration(getActivity(), LinearLayoutManager.VERTICAL));

        recyclerView.setAdapter(atmAdapter);
        prepareATMList();
        return rootView;
    }

    private void prepareATMList() {
        /*Getting the current location*/
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        latitude = sharedPreferences.getString("LATITUDE", null);
        longitude = sharedPreferences.getString("LONGITUDE", null);

        Log.v(TAG + " Current Location:", latitude + longitude);

        new ATMData().execute(latitude, longitude, RADIUS);
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
            String lat = params[0];
            String lng = params[1];
            String radius = params[2];
            return new DataProvider().ATMData(lat, lng, radius);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pd.dismiss();
            addToATMList(result);
        }
    }

    private void addToATMList(String result) {
        if (result != null) {
            try {
                JSONObject jsonObj = new JSONObject(result);
                JSONArray jsonArray = jsonObj.getJSONArray("results");
                noOfATMs = jsonArray.length();

                for (int i = 0; i < noOfATMs; i++) {

                    atmName = jsonArray.getJSONObject(i).getString("name");
                    atmAddress = jsonArray.getJSONObject(i).getString("vicinity");
                    lat_dest = jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lat");
                    lng_dest = jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lng");

                    /*Adding ATM details to ATM Object*/
                    atmObj = new ATM();
                    atmObj.setAtmName(atmName);
                    atmObj.setAtmAddress(atmAddress);
                    distanceInKms = dataProvider.distanceInKm(latitude, longitude, lat_dest, lng_dest);
                    atmObj.setDistance(distanceInKms);

                    /*Adding ATM Object to ATM list*/
                    atmList.add(atmObj);
                }
                atmAdapter.notifyDataSetChanged();
                Log.v(TAG + " MAP_ARRAY", atmList.toString());

            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
