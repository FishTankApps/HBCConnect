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

        setUpServer(false);
        setUpServerPinger();

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

    private boolean firstTry = true;
    private void setUpServer(boolean previouslyFailed){
        if(hbcConnectServer != null && hbcConnectServer.isConnected())
            return;

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("Firebase", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();


                        Log.d("Firebase", token);
                        Toast.makeText(HBCConnectActivity.this, token, Toast.LENGTH_SHORT).show();
                    }
                });

        if(!Utilities.isInternetAvailable()){
            Toast.makeText(this, "Unable to connect to server - No internet connection", Toast.LENGTH_LONG).show();
            return;
        }

        new Thread(()->{
            boolean successful;
            int numberOfAttempts = 0;

            do{
                hbcConnectServer = new HBCConnectServer();
                successful = hbcConnectServer.connect();

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                numberOfAttempts++;

                if(numberOfAttempts > 5){
                    break;
                }
            }while(!successful && !firstTry);

            firstTry = false;

            if(!successful){
                Snackbar snackbar = Snackbar.make(findViewById(R.id.nav_view), "Unable to Connect to server", Snackbar.LENGTH_LONG);

                snackbar.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE);
                snackbar.setAction("Retry", l->setUpServer(true));

                snackbar.show();
            } else {
                if (previouslyFailed) {
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.nav_view), "Connected to Server!", Snackbar.LENGTH_LONG);

                    snackbar.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE);
                    snackbar.show();
                }

                readInitialServerData();
            }

        }).start();
    }

    private void setUpServerPinger(){
        Intent intent = new Intent(this, ServerNotificationPinger.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);

        AlarmManager am = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,  SystemClock.elapsedRealtime() + 1000, Constants.SERVER_PING_MILLIS, sender);// 10min interval
    }

    private void readInitialServerData(){
        dataFile.syncDataWithServer(hbcConnectServer);
    }
}