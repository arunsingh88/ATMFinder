package com.thinktanki.atmfinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.thinktanki.atmfinder.atm.ATMListInterface;
import com.thinktanki.atmfinder.atm.ATMlistView;
import com.thinktanki.atmfinder.util.AndroidUtil;
import com.thinktanki.atmfinder.util.FragmentViewPager;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ATMListInterface, NavigationView.OnNavigationItemSelectedListener {

    private String TAG = MainActivity.class.getSimpleName();
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private AdView adView;
    private final Handler refreshHandler = new Handler();
    private final Runnable refreshRunnable = new RefreshRunnable();
    private AdRequest adRequest;
    private int REFRESH_RATE_IN_SECONDS = 5;
    private final int MAP_VIEW = 1;
    private final int LIST_VIEW = 0;
    private AndroidUtil androidUtil;
    private final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private SharedPreferences sharedPreferences;
    private FragmentViewPager adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        toolbar.setTitle(getResources().getString(R.string.app_name));
        adView = (AdView) findViewById(R.id.adViewActivity);
        adView.setVisibility(View.GONE);
        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, getResources().getString(R.string.admob_app_id));
        adRequest = new AdRequest.Builder()/*.addTestDevice("196FCE962C3DC7551A19FD25FC8543D0")*/.build();

        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                adView.setVisibility(View.GONE);
                refreshHandler.removeCallbacks(refreshRunnable);
                refreshHandler.postDelayed(refreshRunnable, REFRESH_RATE_IN_SECONDS * 1000);
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adView.setVisibility(View.VISIBLE);
            }
        });

        androidUtil = new AndroidUtil(MainActivity.this);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupViewPager(ViewPager viewPager) {
        ArrayList<String> tabs = new ArrayList<>();
        tabs.add(getResources().getString(R.string.list_view));
        tabs.add(getResources().getString(R.string.map_view));
        adapter = new FragmentViewPager(getSupportFragmentManager(), tabs);
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.map_view) {
            viewPager.setCurrentItem(MAP_VIEW, true);
        } else if (id == R.id.list_view) {
            viewPager.setCurrentItem(LIST_VIEW, true);
        } else if (id == R.id.search_location) {
            androidUtil.searchByLocation();

        } else if (id == R.id.search_pincode) {
            androidUtil.searchByLocation();
        } else if (id == R.id.near_me) {
            androidUtil.updateCurrentLocation();
        } else if (id == R.id.nav_sort) {
            Fragment fragment = adapter.getFragment(LIST_VIEW);
            ((ATMlistView) fragment).createDialogBox();

        } else if (id == R.id.nav_radius) {
            androidUtil.changeRadius();

        } else if (id == R.id.nav_aboutApp) {
            LinearLayout layout = (LinearLayout) MainActivity.this.findViewById(R.id.main_activity);
            androidUtil.aboutApp(layout);

        } else if (id == R.id.nav_share) {
            androidUtil.shareApp();

        } else if (id == R.id.nav_rateApp) {
            androidUtil.rateApp();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void showDialog() {

    }

    private class RefreshRunnable implements Runnable {
        @Override
        public void run() {
            adView.loadAd(adRequest);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);

                LatLng placeLatLng = place.getLatLng();
                Double lat = placeLatLng.latitude;
                Double lng = placeLatLng.longitude;
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("LATITUDE", lat.toString());
                editor.putString("LONGITUDE", lng.toString());
                editor.commit();

                Log.i(TAG, "Place: " + place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
}
