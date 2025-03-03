package com.mrikso.anitube.app.ui.preferences;

import static com.mrikso.anitube.app.utils.PreferenceKeys.PREF_KEY_DYNAMIC_COLORS;
import static com.mrikso.anitube.app.utils.PreferenceKeys.PREF_KEY_THEME;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
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
import com.mrikso.anitube.app.network.ApiClient;
import com.mrikso.anitube.app.ui.base.BasePreferenceFragment;
import com.mrikso.anitube.app.ui.detail.DetailsAnimeFragmemtViewModel;
import com.mrikso.anitube.app.ui.main.MainActivity;
import com.mrikso.anitube.app.utils.DialogUtils;
import com.mrikso.anitube.app.utils.IntentUtils;
import com.mrikso.anitube.app.utils.PreferenceKeys;
import com.mrikso.anitube.app.utils.PreferenceUtils;
import com.mrikso.anitube.app.utils.PreferencesHelper;
import com.mrikso.anitube.app.viewmodel.SharedViewModel;

import org.apache.commons.lang3.StringUtils;

import dagger.hilt.android.AndroidEntryPoint;
import okhttp3.HttpUrl;

@AndroidEntryPoint
public class SettingsFragment extends BasePreferenceFragment implements Preference.OnPreferenceChangeListener {
    private PreferencesHelper prefHelper;
    private SharedViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        initObservers();
    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, String rootKey) {
        prefHelper = PreferencesHelper.getInstance();
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        initPreferences();
    }

    private void initPreferences() {
        // ui prefs
        SwitchPreferenceCompat dynamicColors = findPreference(PREF_KEY_DYNAMIC_COLORS);
        bindOnPreferenceChangeListener(dynamicColors);
        // Hide theme section in versions that don't support dynamic colors.
        if (!DynamicColors.isDynamicColorAvailable()) {
            dynamicColors.setVisible(false);
        }

        ListPreference themeMode = findPreference(PREF_KEY_THEME);
        bindOnPreferenceChangeListener(themeMode);


        //player prefs
        ListPreference playerDoubleTapSeek = findPreference(PreferenceKeys.PREF_PLAYER_DOUBLE_TAP_SEEK);
        /*int sec = prefHelper.getPlayerDoubleTapSeek();
        if(sec == -1){
            playerDoubleTapSeek.setSummary(R.string.disabled);
        }
        else {
            playerDoubleTapSeek.setSummary(getResources().getQuantityString(com.mrikso.player.R.plurals.quick_seek_x_second, sec, sec));
        }*/
        bindOnPreferenceChangeListener(playerDoubleTapSeek);

        bindOnPreferenceChangeListener(PreferenceKeys.PREF_PLAYER_SWIPE_CONTROLS);
        bindOnPreferenceChangeListener(PreferenceKeys.PREF_PLAYER_AUTOPLAY_NEXT_EPISODE);

        // Open website Preference
        Preference hikkaLogin = findPreference(PreferenceKeys.PREF_KEY_HIKKA_LOGIN);

        if (prefHelper.isLogginedToHikka()) {
            PreferenceUtils.setSummary(hikkaLogin, getString(R.string.hikka_loggined));
        }

        PreferenceUtils.setOnPreferenceClickListener(hikkaLogin, preference -> {
            if (prefHelper.isLogginedToHikka()) {
                prefHelper.setHikkaToken("");
                PreferenceUtils.setSummary(hikkaLogin, getString(R.string.hikka_login));
            } else {
                final String url = buildHikkaOauthUrl();
                IntentUtils.openInBrowser(requireContext(), url);
            }
            return true;
        });

        bindOnPreferenceChangeListener(PreferenceKeys.PREF_KEY_LOAD_ADDITIONAL_ANIME_INFO);

        // about prefs
        Preference about = findPreference("pref_key_version");
        PreferenceUtils.setSummary(about, getString(R.string.version_summary, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
    }

    private void bindOnPreferenceChangeListener(String key) {
        Preference preference = findPreference(key);
        if(preference!=null) {
            preference.setOnPreferenceChangeListener(this);
        }
        else throw new IllegalArgumentException(String.format("preference key %s not found", key));
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

            /*case PreferenceKeys.PREF_PLAYER_DOUBLE_TAP_SEEK:
                String sec = (String) newValue;
                if(sec.equals("off")){
                    preference.setSummary(R.string.disabled);
                }
                else {
                    preference.setSummary(getResources().getQuantityString(com.mrikso.player.R.plurals.quick_seek_x_second, Integer.parseInt(sec), sec));
                }
                PreferenceUtils.setPrefString(requireContext(), key, "10");
                break;*/
            case PREF_KEY_THEME:
                PreferenceUtils.setPrefString(requireContext(), key, newValue.toString());
                prefHelper.applyThemeMode(newValue.toString());
                break;
        }
        return true;
    }

    private void initObservers() {
        viewModel.hikkaLogin().observe(this, result -> {
            if (result) {
                Preference hikkaLogin = findPreference(PreferenceKeys.PREF_KEY_HIKKA_LOGIN);
                PreferenceUtils.setSummary(hikkaLogin, getString(R.string.hikka_loggined));
            }
        });
    }

    private String buildHikkaOauthUrl() {
        HttpUrl url = HttpUrl.parse(String.format("%s/oauth", ApiClient.HIKKA_URL)).newBuilder()
                .addQueryParameter("reference", BuildConfig.CLIENT_ID)
                .addQueryParameter("scope", "update:watchlist,read:watchlist")  // OkHttp автоматично кодує символи
                .build();

        return url.toString();
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
