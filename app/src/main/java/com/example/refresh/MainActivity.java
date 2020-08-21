package com.example.refresh;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.refresh.util.fragmentCallbackListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements fragmentCallbackListener, SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
        }

        String menuFragment = getIntent().getStringExtra("menuFragment");
        if (menuFragment != null && menuFragment.equals("RecipeFragment")) {
            bottomNav.setSelectedItemId(R.id.nav_recipe);
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = new HomeFragment();

                switch (item.getItemId()) {
                    case R.id.nav_recipe:
                        Bundle bundle = new Bundle();
                        bundle.putString("loadLocation", "main");
                        selectedFragment = new RecipeFragment();
                        selectedFragment.setArguments(bundle);
                        break;
                    case R.id.nav_shop:
                        selectedFragment = new ShoppingFragment();
                        break;
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        selectedFragment).commit();

                return true;
            };

    @Override
    public void onCallback(String fragmentName, @Nullable Bundle param) {
        Fragment frag;
        switch (fragmentName) {
            case "shop":
                frag = new ShoppingFragment();
                break;
            case "recipe":
                frag = new RecipeFragment();
                frag.setArguments(param);
                break;
            default:
                frag = new HomeFragment();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, frag).commit();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals("reminder") && !sharedPreferences.getBoolean("reminder", false)) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
