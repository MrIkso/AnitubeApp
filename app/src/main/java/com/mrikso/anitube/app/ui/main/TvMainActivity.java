package com.mrikso.anitube.app.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class TvMainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Запускаємо звичайний MainActivity
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}