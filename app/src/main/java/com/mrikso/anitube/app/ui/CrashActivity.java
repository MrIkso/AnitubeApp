package com.mrikso.anitube.app.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.DeviceUtils;
import com.mrikso.anitube.app.BuildConfig;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.databinding.ActivityCrashBinding;

import java.util.Objects;

public class CrashActivity extends AppCompatActivity {

    private ActivityCrashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCrashBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        setSupportActionBar(binding.topAppBar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.app_crashed);

        var error = new StringBuilder();

        error.append("Manufacturer: ").append(DeviceUtils.getManufacturer()).append("\n");
        error.append("Device: ").append(DeviceUtils.getModel()).append("\n");
        error.append(getIntent().getStringExtra("Software"));
        error.append(String.format("App version: %s (%s)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
        error.append("\n\n");
        error.append(getIntent().getStringExtra("Error"));
        error.append("\n\n");
        error.append(getIntent().getStringExtra("Date"));

        binding.result.setText(error.toString());

        binding.fab.setOnClickListener(v -> {
            ClipboardUtils.copyText(binding.result.getText());
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        var close = menu.add(getString(R.string.close));
        close.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        close.setIcon(AppCompatResources.getDrawable(this, R.drawable.ic_close));
        close.setContentDescription(getString(R.string.close_app));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (Objects.equals(item.getTitle(), getString(R.string.close))) {
            finishAffinity();
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
