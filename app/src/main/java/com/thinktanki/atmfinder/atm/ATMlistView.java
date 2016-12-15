package com.thinktanki.atmfinder.atm;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.thinktanki.atmfinder.Helper;
import com.thinktanki.atmfinder.R;
import com.thinktanki.atmfinder.listview.ATMAdapter;
import com.thinktanki.atmfinder.listview.RecyclerViewDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class ATMlistView extends Fragment implements SearchView.OnQueryTextListener {
    private List<ATM> atmList;
    private RecyclerView recyclerView;
    private ATMAdapter atmAdapter;
    private Helper helper = new Helper();
    private String latitude, lat_dest;
    private String longitude, lng_dest;
    private String distance;
    private SharedPreferences sharedPreferences;
    private ATM atmObj;
    private EditText editText;


    public ATMlistView() {
    }

    public static ATMlistView newInstance(String param1, String param2) {
        ATMlistView fragment = new ATMlistView();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_atmlist_view, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        atmList = new ArrayList<ATM>();
        atmAdapter = new ATMAdapter(atmList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new RecyclerViewDecoration(getActivity(), LinearLayoutManager.VERTICAL));

        recyclerView.setAdapter(atmAdapter);

        prepareATMList();
        return rootView;
    }

    private void prepareATMList() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        latitude = sharedPreferences.getString("LATITUDE", null);
        longitude = sharedPreferences.getString("LONGITUDE", null);


        new ATMData().execute(latitude, longitude);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        final List<ATM> filteredModelList = filter(atmList, query);

        atmAdapter.setFilter(filteredModelList);
        return false;
    }

    public void Filter() {
        String query = editText.getText().toString();
        final List<ATM> filteredModelList = filter(atmList, query);

        atmAdapter.setFilter(filteredModelList);
    }

    private List<ATM> filter(List<ATM> models, String query) {
        query = query.toLowerCase();
        final List<ATM> filteredModelList = new ArrayList<>();
        for (ATM model : models) {
            final String text = model.getAtmName();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.main_activity_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);

        MenuItemCompat.setOnActionExpandListener(item,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        atmAdapter.setFilter(atmList);
                        return true; // Return true to collapse action view
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true; // Return true to expand action view
                    }
                });
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

            HttpURLConnection urlConnection = null;
            String latitude = params[0];
            String longitude = params[1];
            try {
                URL url = new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude + "," + longitude + "&radius=2000&type=atm&key=AIzaSyCiHkJCZ9O-IZ_6JJwHcAnXelf3mx4Y1_I");
                Log.v("URL", url.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);
                // Starts the query
                urlConnection.connect();
                int response = urlConnection.getResponseCode();
                Log.d("RESPONSE", "The response is: " + response);
                InputStream in = urlConnection.getInputStream();
                // Convert the InputStream into a string
                String contentAsString = helper.readATMData(in);
                return contentAsString;
            } catch (Exception e) {
                Log.e("MAP", e.getMessage());
            } finally {
                urlConnection.disconnect();
            }
            return null;
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
                int atmSize = jsonArray.length();
                String atmName = "ATM";
                String atmAddress = "";

                for (int i = 0; i < atmSize; i++) {
                    atmName = jsonArray.getJSONObject(i).getString("name");
                    atmAddress = jsonArray.getJSONObject(i).getString("vicinity");
                    lat_dest = jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lat");
                    lng_dest = jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lng");
                    atmObj = new ATM();
                    atmObj.setAtmName(atmName);
                    atmObj.setAtmAddress(atmAddress);
                    atmObj.setDistance("2.1km");
                    atmList.add(atmObj);
                }
                Log.v("MAP_ARRAY", atmList.toString());

            } catch (JSONException e) {
                Log.e("JSON_EXCEPTION", e.getMessage());
            } catch (Exception e) {
                Log.e("EXCEPTION", e.getMessage());
            }
        }
        atmAdapter.notifyDataSetChanged();
    }


    public String getDistance(double lat1, double lon1, double lat2, double lon2) {
        String result_in_kms = "";
        String url = "http://maps.google.com/maps/api/directions/json?origin=" + lat1 + "," + lon1 + "&destination=" + lat2 + "," + lon2 + "&sensor=false&units=metric";
        String tag[] = {"text"};
        HttpURLConnection urlConnection = null;
        try {
            URL url_distance = new URL(url);

            Log.v("URL", url.toString());
            urlConnection = (HttpURLConnection) url_distance.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            // Starts the query
            urlConnection.connect();
            int response = urlConnection.getResponseCode();
            Log.d("RESPONSE", "The response is: " + response);
            InputStream is = urlConnection.getInputStream();
            String result = helper.readATMData(is);

            JSONObject jsonObj = new JSONObject(result);
            JSONArray jsonArray = jsonObj.getJSONArray("routes");
            result_in_kms = jsonArray.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getString("text");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result_in_kms;
    }

}
