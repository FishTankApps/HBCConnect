package com.fishtankapps.hbcconnect.mobile.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.fishtankapps.hbcconnect.R;
import com.fishtankapps.hbcconnect.mobile.storage.DataFile;
import com.fishtankapps.hbcconnect.mobile.storage.UpcomingEvent;
import com.fishtankapps.hbcconnect.mobile.utilities.Utilities;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_screen_layout);

        init();
    }

    private void init(){
        initToolbar();
        initNotificationSettings();
        initEventTagSettings();
        initOtherSettings();
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
    private void initNotificationSettings(){
        SwitchCompat livestreamNotificationSwitch = findViewById(R.id.livestreamsSwitch);
        livestreamNotificationSwitch.setChecked(DataFile.getSharedPreferenceBooleanValue(getString(R.string.livestream_notification), this));
        livestreamNotificationSwitch.setOnCheckedChangeListener((button, state) ->
                DataFile.setSharedPreferenceBooleanValue(getString(R.string.livestream_notification), state, this));

        SwitchCompat alertsNotificationSwitch = findViewById(R.id.alertsSwitch);
        alertsNotificationSwitch.setChecked(DataFile.getSharedPreferenceBooleanValue(getString(R.string.alerts_notification), this));
        alertsNotificationSwitch.setOnCheckedChangeListener((button, state) ->
                DataFile.setSharedPreferenceBooleanValue(getString(R.string.alerts_notification), state, this));
    }
    private void initEventTagSettings() {
        ArrayList<String> tags = DataFile.getDataFile(getApplicationContext()).getTags();
        for(UpcomingEvent event : DataFile.getDataFile(getApplicationContext()).getUpcomingEvents())
            for(String tag : event.getTags()) {
                if(!tags.contains(tag))
                    tags.add(tag);
                }

        Utilities.quickSort(tags);

        LinearLayout eventTagRightLayout = findViewById(R.id.eventTagRightLayout);
        LinearLayout eventTagLeftLayout  = findViewById(R.id.eventTagLeftLayout);

        eventTagRightLayout.removeAllViews();
        eventTagLeftLayout.removeAllViews();

        LinearLayout.LayoutParams checkboxParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        int checkboxCount = 0;
        boolean isAdmin = DataFile.getSharedPreferenceBooleanValue(getString(R.string.admin_permission), this);

        for(String tag : tags) {

            if(!isAdmin && (tag.equals("Admin") || tag.equals("Developer")))
                continue;

            String key = tag.toLowerCase().replace(' ', '.');

            CheckBox checkBox = new CheckBox(this);
            checkBox.setTextSize(16);
            checkBox.setText(tag);
            checkBox.setLayoutParams(checkboxParams);
            checkBox.setChecked(DataFile.getSharedPreferenceBooleanValue(getString(R.string.tag_start) + key, true, getApplicationContext()));
            checkBox.setOnCheckedChangeListener(this::onCheckboxCheckChange);

            if(checkboxCount <= tags.size() / 2)
                eventTagLeftLayout.addView(checkBox);
            else
                eventTagRightLayout.addView(checkBox);

            checkboxCount++;
        }
    }
    private void initOtherSettings(){
        findViewById(R.id.resetApp).setOnClickListener((l)-> DataFile.getDataFile(getApplicationContext()).clearData(this));

        Button requestAdmin = findViewById(R.id.requestAdmin);

        if(DataFile.getSharedPreferenceBooleanValue(getString(R.string.admin_permission), this))
            requestAdmin.setText(R.string.already_admin);
        else {
            requestAdmin.setOnClickListener((l) -> {
                if(DataFile.getSharedPreferenceBooleanValue(getString(R.string.admin_permission), this))
                    return;

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Enter Admin Password:");

                EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("OK", (dialog, which) -> {
                    Log.d("AdminRequest", "Getting Password...");

                    FirebaseAuth.getInstance().signInWithEmailAndPassword(getString(R.string.fishtankapps_email), input.getText().toString())
                            .addOnCompleteListener(this, task -> {
                                if (task.isSuccessful()) {
                                    Log.d("AdminLogin", "signIn:success");
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    Log.d("AdminLogin", "User: " + user);

                                    DataFile.setSharedPreferenceBooleanValue(getString(R.string.admin_permission), true, this);
                                    init();

                                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_LONG).show();


                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("AdminLogin", "signInWithEmail:failure");
                                    Toast.makeText(this, "Password is Incorrect", Toast.LENGTH_LONG).show();
                                }
                            });

                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

                builder.show();
            });
        }


    }

    public void onCheckboxCheckChange(CompoundButton checkBox, boolean state) {
        Log.v("TagCheckbox", "CheckBox Text: " + checkBox.getText());
        String key = checkBox.getText().toString().toLowerCase().replace(' ', '.');
        Log.v("TagCheckbox", "CheckBox Key: " + key);

        DataFile.setSharedPreferenceBooleanValue(getString(R.string.tag_start) + key, state, getApplicationContext());
    }

    public void updateNotificationSubscriptions(){
        // EMERGENCY ALERTS:
        if(DataFile.getSharedPreferenceBooleanValue(getString(R.string.alerts_notification), this))
            FirebaseMessaging.getInstance().subscribeToTopic("emergency.alerts").addOnCompleteListener(task ->
                    Log.i("HBC Connect", "Subscription to topic \"emergency.alerts\" successful: " + task.isSuccessful()));
        else
            FirebaseMessaging.getInstance().unsubscribeFromTopic("emergency.alerts").addOnCompleteListener(task ->
                    Log.i("HBC Connect", "Unsubscription to topic \"emergency.alerts\" successful: " + task.isSuccessful()));


        // LIVESTREAMS:
        if(DataFile.getSharedPreferenceBooleanValue(getString(R.string.livestream_notification), this))
            FirebaseMessaging.getInstance().subscribeToTopic("live.livestream").addOnCompleteListener(task ->
                    Log.i("HBC Connect", "Subscription to topic \"live.livestream\" successful: " + task.isSuccessful()));
        else
            FirebaseMessaging.getInstance().unsubscribeFromTopic("live.livestream").addOnCompleteListener(task ->
                    Log.i("HBC Connect", "Unsubscription to topic \"live.livestream\" successful: " + task.isSuccessful()));


        for(String tag : DataFile.getDataFile(getApplicationContext()).getTags()) {
            String key = tag.toLowerCase().replace('_', '.');
            if(DataFile.getSharedPreferenceBooleanValue(getString(R.string.tag_start) + key, getApplicationContext()))
                FirebaseMessaging.getInstance().subscribeToTopic(key).addOnCompleteListener(task ->
                        Log.i("HBC Connect", "Subscription to topic \"" + key + "\" successful: " + task.isSuccessful()));
            else
                FirebaseMessaging.getInstance().unsubscribeFromTopic(key).addOnCompleteListener(task ->
                        Log.i("HBC Connect", "Unsubscription to topic \"" + key + "\" successful: " + task.isSuccessful()));
        }
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
