package com.mrikso.anitube.app.ui.preferences;

import static com.mrikso.anitube.app.utils.PreferenceKeys.PREF_KEY_DYNAMIC_COLORS;
import static com.mrikso.anitube.app.utils.PreferenceKeys.PREF_KEY_THEME;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.google.android.material.color.HarmonizedColors;
import com.google.android.material.color.HarmonizedColorsOptions;
import com.mrikso.anitube.app.App;
import com.mrikso.anitube.app.BuildConfig;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.ui.main.MainActivity;
import com.mrikso.anitube.app.utils.DialogUtils;
import com.mrikso.anitube.app.utils.PreferencesHelper;

import dagger.hilt.android.AndroidEntryPoint;

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
        if (!DynamicColors.isDynamicColorAvailable()) {
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
                    requireActivity().recreate();
                } else {
                    DialogUtils.showConfirmation(requireContext(), R.string.pref_restart_dialog_title,
                            R.string.pref_restart_dialog_description, () -> {
                                restartMainActivity();
                            }
                    );
                }

                break;

            case PREF_KEY_THEME:
                prefHelper.setThemeMode(newValue.toString());
                prefHelper.applyThemeMode(newValue.toString());
                break;
        }
        return true;
    }

    private void restartMainActivity() {
        Intent intent = getActivity().getIntent();
        getActivity().finish();
        startActivity(intent);

        //Runtime.getRuntime().exit(0);
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        MaterialToolbar toolbar = view.findViewById(R.id.settings_toolbar);
        toolbar.setNavigationOnClickListener(
                v -> Navigation.findNavController(requireView()).popBackStack());
    }
}
