package com.mrikso.anitube.app.ui.preferences;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.DynamicColors;
import com.mrikso.anitube.app.App;
import com.mrikso.anitube.app.BuildConfig;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.ui.MainActivity;
import com.mrikso.anitube.app.utils.PreferencesHelper;

import dagger.hilt.android.AndroidEntryPoint;

import static com.mrikso.anitube.app.utils.PreferenceKeys.*;

@AndroidEntryPoint
public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
    private PreferencesHelper prefHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefHelper = App.getApplication().getPreferenceHelper();
        initPreferences();
    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }

    private void initPreferences() {
        SwitchPreferenceCompat dynamicColors = findPreference(PREF_KEY_DYNAMIC_COLORS);
        bindOnPreferenceChangeListener(dynamicColors);
        // Hide theme section in versions that don't support dynamic colors.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            dynamicColors.setVisible(false);
        }

        ListPreference themeMode = findPreference(PREF_KEY_THEME);
        bindOnPreferenceChangeListener(themeMode);

        Preference about = findPreference("pref_key_version");
        about.setSummary(getString(R.string.version_summary, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
    }

    private void bindOnPreferenceChangeListener(Preference preference) {
        preference.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        switch (key) {
            case PREF_KEY_DYNAMIC_COLORS:
                boolean enable = (Boolean) newValue;
                prefHelper.setDynamicColorsEnabled(enable);
                if (enable) {
                    DynamicColors.applyToActivitiesIfAvailable(App.getApplication());
                }

                requireActivity().recreate();
                break;

            case PREF_KEY_THEME:
                prefHelper.setThemeMode(newValue.toString());
                prefHelper.applyThemeMode(newValue.toString());
                break;
        }
        return true;
    }

    private void restartMainActivity() {
        Intent intent = new Intent(requireActivity().getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        MaterialToolbar toolbar = view.findViewById(R.id.settings_toolbar);
        toolbar.setNavigationOnClickListener(
                v -> Navigation.findNavController(requireView()).popBackStack());
    }
}
