package com.fishtankapps.hbcconnect.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.fishtankapps.hbcconnect.R;
import com.fishtankapps.hbcconnect.dataStorage.DataFile;
import com.fishtankapps.hbcconnect.utilities.Constants;
import com.fishtankapps.hbcconnect.utilities.Utilities;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_screen_layout);

        initToolbar();
        initSwitches();

        findViewById(R.id.settingsDividerLine).setBackgroundColor(Utilities.getColor(this, R.attr.colorSecondary));
    }

    private void initToolbar(){
        try {
            Toolbar toolbar = findViewById(R.id.settingsToolbar);
            setSupportActionBar(toolbar);
            Objects.requireNonNull(getSupportActionBar()).setTitle("Settings");
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);

        } catch (Exception e){
            Log.w("Settings", "Error setting up SupportActionBar.");
        }
    }
    private void initSwitches(){
        SwitchCompat livestreamNotificationSwitch = findViewById(R.id.livestreamsSwitch);
        livestreamNotificationSwitch.setChecked(DataFile.getSharedPreferenceBooleanValue(Constants.LIVESTREAM_NOTIFICATION, this));
        livestreamNotificationSwitch.setOnCheckedChangeListener((button, state) ->
                DataFile.setSharedPreferenceBooleanValue(Constants.LIVESTREAM_NOTIFICATION, state, this));

        SwitchCompat alertsNotificationSwitch = findViewById(R.id.alertsSwitch);
        alertsNotificationSwitch.setChecked(DataFile.getSharedPreferenceBooleanValue(Constants.ALERTS_NOTIFICATION, this));
        alertsNotificationSwitch.setOnCheckedChangeListener((button, state) ->
                DataFile.setSharedPreferenceBooleanValue(Constants.ALERTS_NOTIFICATION, state, this));
    }

    public void updateNotificationSubscriptions(){
        // EMERGENCY ALERTS:
        if(DataFile.getSharedPreferenceBooleanValue(Constants.ALERTS_NOTIFICATION, this))
            FirebaseMessaging.getInstance().subscribeToTopic("emergency.alerts").addOnCompleteListener(task ->
                    Log.i("HBC Connect", "Subscription to topic \"emergency.alerts\" successful: " + task.isSuccessful()));
        else
            FirebaseMessaging.getInstance().unsubscribeFromTopic("emergency.alerts").addOnCompleteListener(task ->
                    Log.i("HBC Connect", "Unsubscription to topic \"emergency.alerts\" successful: " + task.isSuccessful()));


        // LIVESTREAMS:
        if(DataFile.getSharedPreferenceBooleanValue(Constants.LIVESTREAM_NOTIFICATION, this))
            FirebaseMessaging.getInstance().subscribeToTopic("live.livestream").addOnCompleteListener(task ->
                    Log.i("HBC Connect", "Subscription to topic \"live.livestream\" successful: " + task.isSuccessful()));
        else
            FirebaseMessaging.getInstance().unsubscribeFromTopic("live.livestream").addOnCompleteListener(task ->
                    Log.i("HBC Connect", "Unsubscription to topic \"live.livestream\" successful: " + task.isSuccessful()));
    }

    @Override
    public void onBackPressed() {
        updateNotificationSubscriptions();

        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_the_left, R.anim.exit_to_the_right);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
