package com.fishtankapps.hbcconnect.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.fishtankapps.hbcconnect.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_screen_layout);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
