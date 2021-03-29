package com.fishtankapps.hbcconnect.activity;

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
import com.fishtankapps.hbcconnect.utilities.firebase.FirebaseDatabaseInterface;
import com.google.android.material.navigation.NavigationView;
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
    }

    public void initNavigation(){
        setContentView(R.layout.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(this::onThreeButtonsMenuItemClicked);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_virtual_card, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    private void initFirebase(){
        FirebaseMessaging.getInstance().subscribeToTopic("debug").addOnCompleteListener(task ->
                Log.i("HBC Connect", "Subscription to topic \"debug\" successful: " + task.isSuccessful()));

        FirebaseMessaging.getInstance().subscribeToTopic("live.livestream").addOnCompleteListener(task ->
                Log.i("HBC Connect", "Subscription to topic \"live.livestream\" successful: " + task.isSuccessful()));

        databaseInterface = new FirebaseDatabaseInterface();

        dataFile.syncWithDatabase(null);
    }

    @Override
    public void onPause(){
        super.onPause();

        DataFile.saveDataFile(dataFile, this);
        databaseInterface.goOffline();
    }

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


    private boolean onThreeButtonsMenuItemClicked(MenuItem menuItem){

        return true;
    }
}