package com.fishtankapps.hbcconnect.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.fishtankapps.hbcconnect.R;
import com.fishtankapps.hbcconnect.dataStorage.DataFile;
import com.fishtankapps.hbcconnect.server.HBCConnectServer;
import com.fishtankapps.hbcconnect.utilities.Constants;
import com.fishtankapps.hbcconnect.utilities.Utilities;
import com.fishtankapps.hbcconnect.utilities.notification.ServerNotificationPinger;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;

public class HBCConnectActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    public static DataFile dataFile;
    public static HBCConnectActivity hbcConnectActivity;

    public static HBCConnectServer hbcConnectServer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataFile = DataFile.openDataFile(this);

        hbcConnectActivity = this;

        setContentView(R.layout.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(this::onThreeButtonsMenuItemClicked);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_count_me_in, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public void onStop(){
        super.onStop();
        DataFile.saveDataFile(dataFile, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(hbcConnectServer != null && hbcConnectServer.isConnected())
            hbcConnectServer.disconnect();
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

    private void readInitialServerData(){
        dataFile.syncDataWithServer(hbcConnectServer);
    }
}