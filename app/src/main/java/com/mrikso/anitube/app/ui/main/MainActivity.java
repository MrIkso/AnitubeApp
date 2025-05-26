package com.mrikso.anitube.app.ui.main;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.MaterialColors;
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

    private @ColorInt int defaultStatusBarColor; // Зберігаємо стандартний колір

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate and get instance of binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        // set content view to binding's root
        setContentView(binding.getRoot());

        saveDefaultStatusBarSettings();

        initViewModel();
        initObservers();
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        // navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(binding.bttomNav, navController);

        navController.addOnDestinationChangedListener((nav, destination, bundle) -> {
            updateStatusBarAppearance(destination);
            if (destination.getId() == R.id.nav_screenshots || destination.getId() == R.id.nav_settings) {
                hideBottomNavigation();
            } else {
                showBottomNavigation();
            }
        });

        // Додаємо обробку відступів для BottomNavigationView
        applyInsetsToBottomNavigationView();

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

    // Метод для збереження стандартних налаштувань
    private void saveDefaultStatusBarSettings() {
        // --- НАЛАШТОВУЄМО EDGE-TO-EDGE ОДИН РАЗ ---
        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        // -----------------------------------------

        // Зберігаємо стандартний колір статус-бару з теми
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.statusBarColor, typedValue, true);
        defaultStatusBarColor = typedValue.data;
    }

    // Метод, що викликається при зміні екрану
    private void updateStatusBarAppearance(NavDestination destination) {
        Window window = getWindow();
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(window, binding.getRoot());

        if (destination.getId() == R.id.nav_details_anime_info) {
            // Екран деталей: Прозорий фон, світлі іконки (для темного постера)
            window.setStatusBarColor(Color.TRANSPARENT);
            if (insetsController != null) {
                insetsController.setAppearanceLightStatusBars(false);
            }
        } else {
            // Інші екрани: Стандартний колір з теми, стандартний вигляд іконок
            window.setStatusBarColor(defaultStatusBarColor);
            if (insetsController != null) {
                // Отримуємо стандартний вигляд іконок з теми
                boolean defaultLightStatusBar;
                TypedValue lightValue = new TypedValue();
                getTheme().resolveAttribute(android.R.attr.windowLightStatusBar, lightValue, true);
                defaultLightStatusBar = lightValue.data != 0;
                insetsController.setAppearanceLightStatusBars(defaultLightStatusBar);
            }
        }
    }

    private void applyInsetsToBottomNavigationView() {
        final int initialPaddingLeft = binding.bttomNav.getPaddingLeft();
        final int initialPaddingTop = binding.bttomNav.getPaddingTop();
        final int initialPaddingRight = binding.bttomNav.getPaddingRight();
        final int initialPaddingBottom = binding.bttomNav.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(binding.bttomNav, (v, windowInsets) -> {
            Insets navBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
            // Додаємо нижній відступ до початкового
            v.setPadding(
                    initialPaddingLeft,
                    initialPaddingTop,
                    initialPaddingRight,
                    initialPaddingBottom + navBars.bottom
            );
            // Повертаємо інсети, не поглинаючи їх
            return windowInsets;
        });
        // Запит на застосування інсетів після встановлення слухача
        ViewCompat.requestApplyInsets(binding.bttomNav);
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
