package com.fishtankapps.hbcconnect.activity;

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
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.fishtankapps.hbcconnect.R;
import com.fishtankapps.hbcconnect.dataStorage.DataFile;
import com.fishtankapps.hbcconnect.utilities.Constants;
import com.fishtankapps.hbcconnect.utilities.firebase.FirebaseDatabaseInterface;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;

public class HBCConnectActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    public static DataFile dataFile;
    public static HBCConnectActivity hbcConnectActivity;

    public static FirebaseDatabaseInterface databaseInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataFile = DataFile.openDataFile(this);

        hbcConnectActivity = this;

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

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    private void initFirebase(){
        databaseInterface = new FirebaseDatabaseInterface();
        dataFile.syncWithDatabase(null);
    }

    private void doInitialSetup(){
        if(DataFile.getSharedPreferenceBooleanValue(Constants.NOT_FIRST_TIME, this)) {
            Log.v("HBCConnectActivity", "doInitialSetup: not first time, skipping set up");
            return;
        }

        Log.d("HBCConnectActivity", "doInitialSetup: First Time!");
        DataFile.setSharedPreferenceBooleanValue(Constants.NOT_FIRST_TIME, true,this);

        // Set up Notification Preferences:
        DataFile.setSharedPreferenceBooleanValue(Constants.ALERTS_NOTIFICATION, true, this);
        DataFile.setSharedPreferenceBooleanValue(Constants.LIVESTREAM_NOTIFICATION, true, this);

        // Set up Firebase Listeners:
        FirebaseMessaging.getInstance().subscribeToTopic("live.livestream").addOnCompleteListener(task ->
                Log.i("HBC Connect", "Subscription to topic \"live.livestream\" successful: " + task.isSuccessful()));
        FirebaseMessaging.getInstance().subscribeToTopic("emergency.alerts").addOnCompleteListener(task ->
                Log.i("HBC Connect", "Subscription to topic \"emergency.alerts\" successful: " + task.isSuccessful()));

        Snackbar snackbar = Snackbar.make(findViewById(R.id.drawer_layout), "Welcome to the HBC Connect app! Would you like a tour?", Snackbar.LENGTH_INDEFINITE);

        snackbar.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE);
        snackbar.setAction("Sure!", l-> giveTour());

        new Thread(()->{
            try{Thread.sleep(5_000);}catch (Exception ignore){};
            snackbar.dismiss();
        }).start();

        snackbar.show();
    }

    private void giveTour(){

    }

    private boolean onThreeButtonsMenuItemClicked(MenuItem menuItem){

        if(menuItem.getTitle().equals("Settings")){
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            overridePendingTransition(R.anim.enter_from_the_right, R.anim.exit_to_the_left);
        }

        return true;
    }


    @Override
    public void onPause(){
        super.onPause();

        DataFile.saveDataFile(dataFile, this);
        databaseInterface.goOffline();
    }

    @Override
    public void onResume(){
        super.onResume();
        databaseInterface.goOnline();
    }

    @Override
    public void onStop(){
        super.onStop();

        DataFile.saveDataFile(dataFile, this);
        databaseInterface.goOffline();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.three_buttons_menu, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}