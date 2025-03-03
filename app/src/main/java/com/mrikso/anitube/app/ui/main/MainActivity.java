package com.mrikso.anitube.app.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.DynamicColors;
import com.mrikso.anitube.app.App;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.databinding.ActivityMainBinding;
import com.mrikso.anitube.app.utils.ParserUtils;
import com.mrikso.anitube.app.utils.PreferenceUtils;
import com.mrikso.anitube.app.utils.PreferencesHelper;
import com.mrikso.anitube.app.viewmodel.SharedViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private NavController navController;
    // private MenuItem selectedItem;
    private SharedViewModel viewModel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate and get instance of binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        // set content view to binding's root
        setContentView(binding.getRoot());

        initViewModel();
        initObservers();
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        // navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(binding.bttomNav, navController);

        navController.addOnDestinationChangedListener((nav, destination, bundle) -> {
            if (destination.getId() == R.id.nav_screenshots || destination.getId() == R.id.nav_settings) {
                hideBottomNavigation();
            } else {
                showBottomNavigation();
            }
        });

        binding.bttomNav.setOnItemSelectedListener(item -> {
            //  selectedItem = item;
            int selectedItemId = item.getItemId();
            handleBottomNavItemSelections(binding.bttomNav, selectedItemId);
            //                    if (selectedItemId == R.id.nav_home) {
            //                        navController.navigate(R.id.nav_home);
            //                    } else if (selectedItemId == R.id.nav_anime_list) {
            //                        navController.navigate(R.id.nav_anime_list);
            //                    } else if (selectedItemId == R.id.nav_search) {
            //                        navController.navigate(R.id.nav_anime_list);
            //                    } else if (selectedItemId == R.id.nav_profile) {
            //                        navController.navigate(R.id.nav_profile);
            //                    }
            //  if (selectedItemId != binding.bttomNav.getSelectedItemId()) {

            NavigationUI.onNavDestinationSelected(item, navController);
            //   }
            return true;
        });

        handleIntent(getIntent());
    }

    private void handleBottomNavItemSelections(BottomNavigationView bn, int itemId) {
        bn.getMenu().findItem(itemId).setChecked(true);
    }

    private void showBottomNavigation() {
        binding.bttomNav.setVisibility(View.VISIBLE);
    }

    private void hideBottomNavigation() {
        binding.bttomNav.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        navController = null;
        binding = null;
        viewModel = null;
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(SharedViewModel.class);
    }

    private void initObservers() {
        viewModel.hikkaLogin().observe(this, result -> {
            if (result) {
                Toast.makeText(this, getString(R.string.hikka_login_succes), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // NavigationUI.onNavDestinationSelected(selectedItem, navController);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    void handleIntent(Intent intent) {
        if (intent != null && intent.getData() != null) {
            String uriLink = intent.getData().toString();
            // hikka login case
            if (uriLink.contains("anitube://auth?reference=")) {
                String reference = intent.getData().getQueryParameter("reference");
                viewModel.hikkaLogin(reference);
            }
            // anitube anime link
            else if (ParserUtils.isAnimeLink(uriLink)) {
                Bundle bundle = new Bundle();
                bundle.putString("url", uriLink);
                navController.navigate(R.id.nav_details_anime_info, bundle);
            } else {
                Toast.makeText(this, "This url not supported", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
}
