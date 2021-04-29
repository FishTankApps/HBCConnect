package com.fishtankapps.hbcconnect.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fishtankapps.hbcconnect.R;
import com.fishtankapps.hbcconnect.ui.livestream.FacebookLivestreamViewer;
import com.fishtankapps.hbcconnect.utilities.Constants;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;

public class LivestreamViewerActivity extends AppCompatActivity {

    private static final boolean WAIT_FOR_VIDEO_TO_LOAD = false;

    private LinearLayout videoPlayerLinearLayout;
    private FacebookLivestreamViewer player;
    private Toolbar toolbar;

    private Thread logReaderThread;

    private boolean videoLoaded = false;
    private boolean displayInLandscape = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.livestream_viewer_layout);

        String livestreamID = getIntent().getStringExtra(Constants.LIVESTREAM_ID);

        Log.i("FacebookVideoWatcher", "Livestream ID: " + livestreamID);

        player = findViewById(R.id.facebookVideoPlayer);
        player.setAutoPlayerHeight(this, false);
        player.initialize(livestreamID);

        videoPlayerLinearLayout = findViewById(R.id.videoPlayerLinearLayout);
        videoPlayerLinearLayout.setAlpha(0);

        try {
            toolbar = findViewById(R.id.livestreamVeiwerToolbar);
            setSupportActionBar(toolbar);
            Objects.requireNonNull(getSupportActionBar()).setTitle(getIntent().getStringExtra(Constants.LIVESTREAM_NAME));
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);

        } catch (Exception e){
            Log.w("Count Me In", "Error setting up SupportActionBar.");
        }

        enterFullScreenMode();

        if(WAIT_FOR_VIDEO_TO_LOAD){
            logReaderThread = new Thread(() -> {
                Process process = null;
                try {
                    Runtime.getRuntime().exec("logcat -b all -c").waitFor();

                    String[] command = new String[] { "logcat", "-v", "threadtime" };

                    process = Runtime.getRuntime().exec(command);

                    BufferedReader bufferedReader =
                            new BufferedReader(new InputStreamReader(process.getInputStream()));

                    String line;
                    long startTime = System.currentTimeMillis();
                    while ((line = bufferedReader.readLine()) != null){
                        if(line.contains("OMX"))
                            break;

                        if(System.currentTimeMillis() - startTime > 15_000)
                            break;
                        Thread.sleep(50);
                    }

                    process.destroy();
                } catch (InterruptedException ie) {
                    Log.i("LogLooker", "Thread killed!");
                } catch (Exception ex){
                    Log.e("LogLooker", "start failed", ex);
                }

                if(process != null)
                    process.destroy();

                Log.d("LogLooker", "Livestream Done Loading!");
                this.runOnUiThread(this::onLivestreamDoneLoading);
                videoLoaded = true;
            });

            logReaderThread.start();
        } else
            onLivestreamDoneLoading();

    }

    private void onLivestreamDoneLoading(){
        videoPlayerLinearLayout.setAlpha(1);
        ((LinearLayout) findViewById(R.id.livestreamVeiwerLayout)).removeView(findViewById(R.id.loadingBar));
        player.setAutoPlayerHeight(this, displayInLandscape);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if(logReaderThread != null && logReaderThread.isAlive())
            logReaderThread.interrupt();

        exitFullScreenMode();
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        displayInLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;

        if(videoLoaded || !WAIT_FOR_VIDEO_TO_LOAD)
            player.setAutoPlayerHeight(this, displayInLandscape);

        if(displayInLandscape)
            ((LinearLayout) findViewById(R.id.livestreamVeiwerLayout)).removeView(toolbar);
        else
            ((LinearLayout) findViewById(R.id.livestreamVeiwerLayout)).addView(toolbar, 0);
    }

    private void exitFullScreenMode() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public void enterFullScreenMode(){
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
