package com.fishtankapps.hbcconnect.ui.livestream;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.fishtankapps.hbcconnect.R;
import com.fishtankapps.hbcconnect.activity.HBCConnectActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Created by zeta on 9/13/16.
 */

public class FacebookVideoPreviewButton extends WebView {

    public FacebookVideoPreviewButton(Context context) {
        super(context);
    }

    public FacebookVideoPreviewButton(Context context, AttributeSet a) {
        super(context, a);
    }


    @SuppressLint("SetJavaScriptEnabled")
    public void initialize(String videoUrl){
        videoUrl = "http://www.facebook.com/photo.php?v=" + videoUrl;

        WebSettings set = this.getSettings();
        set.setJavaScriptEnabled(true);
        set.setUseWideViewPort(true);
        set.setLoadWithOverviewMode(true);
        set.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        set.setCacheMode(WebSettings.LOAD_NO_CACHE);
        set.setAllowContentAccess(true);
        set.setAllowFileAccess(true);
        set.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36 Edge/12.0");

        this.setLayerType(View.LAYER_TYPE_NONE, null);
        this.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

        this.loadDataWithBaseURL("http://facebook.com", getVideoHTML(videoUrl), "text/html", "utf-8", null);

        this.setWebChromeClient(new MyChromeClient());

        setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));

        new Thread(()->{
            try{Thread.sleep(3000);}catch (Exception ignore){}
            HBCConnectActivity.hbcConnectActivity.runOnUiThread(this::setAutoPlayerHeight);
        }).start();
    }

    private static final class MyChromeClient extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            return super.onConsoleMessage(consoleMessage);
        }
    }

    private String getVideoHTML(String videoUrl){
        try {
            InputStream in = getResources().openRawResource(R.raw.players);
            if (in != null) {
                InputStreamReader stream = new InputStreamReader(in, StandardCharsets.UTF_8);
                BufferedReader buffer = new BufferedReader(stream);
                String read;
                StringBuilder sb = new StringBuilder();

                while ((read = buffer.readLine()) != null) {
                    sb.append(read).append("\n");
                }

                in.close();

                return sb.toString()
                        .replace("{app_id}", "test")
                        .replace("{video_url}", videoUrl)
                        .replace("{auto_play}", "false")
                        .replace("{show_text}", "true")
                        .replace("{show_captions}", "true");
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return "";
    }

    private void setAutoPlayerHeight() {
        setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (this.getWidth() * 0.5625) + 5));
    }
}