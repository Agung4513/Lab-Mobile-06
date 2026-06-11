package com.example.habittracker;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.habittracker.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize darkness based on user preferences before drawing views
        android.content.SharedPreferences prefs = getSharedPreferences("app_profile_prefs", MODE_PRIVATE);
        if (prefs.contains("pref_dark_mode")) {
            boolean isDark = prefs.getBoolean("pref_dark_mode", false);
            int targetMode = isDark ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
            if (androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode() != targetMode) {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(targetMode);
            }
        }
        
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setTheme(R.style.Theme_HabitTracker); // Explicit style theme initialization
        setContentView(binding.getRoot());

        setUpNavigationController();
        handleExploreRedirect(getIntent());
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleExploreRedirect(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleExploreRedirect(getIntent());
    }

    private void handleExploreRedirect(android.content.Intent intent) {
        if (intent != null && intent.getBooleanExtra("navigate_to_explore", false)) {
            intent.removeExtra("navigate_to_explore");
            switchToExplore();
        }
    }

    private void switchToExplore() {
        if (binding != null && binding.navView != null) {
            binding.navView.setSelectedItemId(R.id.navigation_explore);
        }
    }

    private void setUpNavigationController() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.navView, navController);
        }
    }
}
