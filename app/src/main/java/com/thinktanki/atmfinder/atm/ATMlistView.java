package com.thinktanki.atmfinder.atm;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.Toast;

import com.thinktanki.atmfinder.util.DataProvider;
import com.thinktanki.atmfinder.R;
import com.thinktanki.atmfinder.listview.ATMAdapter;
import com.thinktanki.atmfinder.listview.RecyclerViewDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ATMlistView extends Fragment implements SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener, SharedPreferences.OnSharedPreferenceChangeListener
{
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
    private String RADIUS;
    private SharedPreferences sharedPreferences;
    private ATM atmObj;
    private SearchView searchView;
    private SwipeRefreshLayout swipeRefreshLayout;

    public ATMlistView() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_atmlist_view, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        atmList = new ArrayList<ATM>();
        atmAdapter = new ATMAdapter(atmList, getActivity());

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new RecyclerViewDecoration(getActivity(), LinearLayoutManager.VERTICAL));

        recyclerView.setAdapter(atmAdapter);
        atmAdapter.notifyDataSetChanged();
        prepareATMList();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.listview_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_search:
                Toast.makeText(getActivity(), "search click", Toast.LENGTH_SHORT).show();
                searchView.setOnQueryTextListener(this);
                return true;

            case R.id.action_sort:
                //do something
                Toast.makeText(getActivity(), "clcik", Toast.LENGTH_LONG).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        //sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    private void prepareATMList() {
        /*Getting the current location*/
        latitude = sharedPreferences.getString("LATITUDE", null);
        longitude = sharedPreferences.getString("LONGITUDE", null);
        RADIUS=sharedPreferences.getString("RADIUS","1000");

        Log.v(TAG + " Current Location:", latitude + longitude);

        new ATMData().execute(latitude, longitude, RADIUS);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Toast.makeText(getActivity(), "onQueryTextSubmit", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Toast.makeText(getActivity(), "onQueryTextChange", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onRefresh() {
        prepareATMList();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
       /* if(key.equalsIgnoreCase("RADIUS"))
        {
            prepareATMList();
            //Toast.makeText(getActivity(),"New Radius: "+sharedPreferences.getString(key,"hll"),Toast.LENGTH_LONG).show();
        }*/

        prepareATMList();

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
            // stopping swipe refresh
            swipeRefreshLayout.setRefreshing(false);
            // avLoadingIndicatorView.hide();
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
                    atmObj.setLatitude(Double.parseDouble(lat_dest));
                    atmObj.setLongitude(Double.parseDouble(lng_dest));

                    distanceInKms = dataProvider.distanceInKm(latitude, longitude, lat_dest, lng_dest);
                    atmObj.setDistance(distanceInKms);

                    /*Adding ATM Object to ATM list*/
                    if (!atmList.contains(atmObj))
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
