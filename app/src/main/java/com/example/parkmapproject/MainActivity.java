package com.example.parkmapproject;

import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.parkmapproject.main_ui.dashboard.DashboardFragment;
import com.example.parkmapproject.main_ui.maps.MapsFragment;
import com.example.parkmapproject.main_ui.me.MeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    final Fragment mapsFragment = new MapsFragment();
    final Fragment dashboardFragment = new DashboardFragment();
    final Fragment meFragment = new MeFragment();
    final FragmentManager fm = getSupportFragmentManager();
    Fragment activeFragment = mapsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_main);

        // Set listener for navigation items
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_maps:
                    fm.beginTransaction().hide(activeFragment).show(mapsFragment).commit();
                    activeFragment = mapsFragment;
                    return true;
                case R.id.navigation_dashboard:
                    fm.beginTransaction().hide(activeFragment).show(dashboardFragment).commit();
                    activeFragment = dashboardFragment;
                    return true;
                case R.id.navigation_me:
                    fm.beginTransaction().hide(activeFragment).show(meFragment).commit();
                    activeFragment = meFragment;
                    return true;
            }
            return false;
        });

        // Add all fragments but show only maps fragment
        fm.beginTransaction().add(R.id.nav_host_fragment, mapsFragment, "1").commit();
        fm.beginTransaction().add(R.id.nav_host_fragment, dashboardFragment, "2").hide(dashboardFragment).commit();
        fm.beginTransaction().add(R.id.nav_host_fragment, meFragment, "3").hide(meFragment).commit();
    }

}
