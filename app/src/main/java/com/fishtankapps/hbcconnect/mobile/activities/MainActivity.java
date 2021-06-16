package com.fishtankapps.hbcconnect.mobile.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.fishtankapps.hbcconnect.R;
import com.fishtankapps.hbcconnect.mobile.storage.DataFile;
import com.fishtankapps.hbcconnect.mobile.utilities.Utilities;
import com.fishtankapps.hbcconnect.mobile.utilities.firebase.FirebaseDatabaseInterface;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    private static MainActivity activity;
    public static Context getContext(){
        return activity;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;

        initNavigation();
        initFirebase();

        doInitialSetup();
    }

    private void initNavigation(){
        setContentView(R.layout.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(this::onThreeButtonsMenuItemClicked);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_virtual_card, R.id.nav_slideshow)
                .setOpenableLayout(drawerLayout)
                .build();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_container);

        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    private void initFirebase(){
        DataFile.getDataFile(this).syncMiscellaneousData();
    }

    private void doInitialSetup(){
        if(DataFile.getSharedPreferenceBooleanValue(getString(R.string.not_first_time_opened), this)) {
            Log.v("HBCConnectActivity", "doInitialSetup: not first time, skipping set up");
            return;
        }

        Log.d("HBCConnectActivity", "doInitialSetup: First Time!");
        DataFile.setSharedPreferenceBooleanValue(getString(R.string.not_first_time_opened), true,this);

        // Set up Notification Preferences:
        DataFile.setSharedPreferenceBooleanValue(getString(R.string.alerts_notification), true, this);
        DataFile.setSharedPreferenceBooleanValue(getString(R.string.livestream_notification), true, this);

        // Set up Firebase Listeners:
        FirebaseMessaging.getInstance().subscribeToTopic("live.livestream").addOnCompleteListener(task ->
                Log.i("HBC Connect", "Subscription to topic \"live.livestream\" successful: " + task.isSuccessful()));
        FirebaseMessaging.getInstance().subscribeToTopic("emergency.alerts").addOnCompleteListener(task ->
                Log.i("HBC Connect", "Subscription to topic \"emergency.alerts\" successful: " + task.isSuccessful()));

        Snackbar snackbar = Snackbar.make(findViewById(R.id.drawer_layout), "Welcome to the HBC Connect app! Would you like a tour?", Snackbar.LENGTH_INDEFINITE);

        snackbar.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE);
        snackbar.setAction("Sure!", l -> giveTour());

        new Thread(()->{
            try{Thread.sleep(5_000);}catch (Exception ignore){}
            snackbar.dismiss();
        }).start();

        snackbar.show();
    }

    private void giveTour(){
        throw new RuntimeException("TEST");
    }

    private boolean onThreeButtonsMenuItemClicked(MenuItem menuItem){

        if(menuItem.getItemId() == R.id.open_settings){
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            overridePendingTransition(R.anim.enter_from_the_right, R.anim.exit_to_the_left);
        } else if(menuItem.getItemId() == R.id.send_feedback) {
            Intent feedbackIntent = new Intent(this, SubmitFeedbackActivity.class);
            startActivity(feedbackIntent);
            overridePendingTransition(R.anim.enter_from_the_right, R.anim.exit_to_the_left);
        }

        return true;
    }


    @Override
    public void onPause(){
        super.onPause();

        DataFile.getDataFile(this).saveDataFile();
        FirebaseDatabaseInterface.getFirebaseDatabaseInterface().goOffline();
    }

    @Override
    public void onResume(){
        super.onResume();
        FirebaseDatabaseInterface.getFirebaseDatabaseInterface().goOnline();
    }

    @Override
    public void onStop(){
        super.onStop();

        DataFile.getDataFile(this).saveDataFile();
        FirebaseDatabaseInterface.getFirebaseDatabaseInterface().goOffline();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_container);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}