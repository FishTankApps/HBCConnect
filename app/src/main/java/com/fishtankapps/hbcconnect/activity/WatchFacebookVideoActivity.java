package com.fishtankapps.hbcconnect.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.fishtankapps.hbcconnect.R;
import com.fishtankapps.hbcconnect.ui.livestream.FacebookPlayer;
import com.fishtankapps.hbcconnect.ui.livestream.LiveStreamSelector;
import com.fishtankapps.hbcconnect.utilities.Constants;

import org.jetbrains.annotations.NotNull;

public class WatchFacebookVideoActivity extends AppCompatActivity {

    private FacebookPlayer player;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.facebook_video_veiwer_layout);

        Bundle extras = getIntent().getExtras();
        String livestreamID = extras.getString(Constants.LIVESTREAM_ID);


        player = findViewById(R.id.facebookVideoPlayer);
        player.setAutoPlayerHeight(this, false);
        player.initialize(livestreamID);

        enterFullScreenMode();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        player.setAutoPlayerHeight(this, newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE);
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
