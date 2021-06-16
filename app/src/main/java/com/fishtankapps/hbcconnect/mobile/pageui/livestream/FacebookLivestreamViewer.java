package com.fishtankapps.hbcconnect.mobile.pageui.livestream;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.fishtankapps.hbcconnect.BuildConfig;
import com.fishtankapps.hbcconnect.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class FacebookLivestreamViewer extends WebView {

    public FacebookLivestreamViewer(Context context) {
        super(context);
    }

    public FacebookLivestreamViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void initialize(String videoUrl){

        if(!videoUrl.equals(LivestreamSelector.LIVE_VIDEO_ID))
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

        if(!videoUrl.equals(LivestreamSelector.LIVE_VIDEO_ID))
            this.loadDataWithBaseURL("http://facebook.com", getVideoHTML(videoUrl), "text/html", "utf-8", null);
        else
            loadData(getLivestreamHTML(), "text/html", "UTF-8");

        this.setLongClickable(true);
        this.setOnLongClickListener(v -> true);

        if (BuildConfig.DEBUG) {
            setWebContentsDebuggingEnabled(true);
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
                        .replace("{app_id}", "")
                        .replace("{video_url}", videoUrl)
                        .replace("{auto_play}", "false")
                        .replace("{show_text}", "false")
                        .replace("{show_captions}", "false");
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return "";
    }

    private String getLivestreamHTML(){
        return "<iframe src=\"https://www.facebook.com/plugins/video.php?href=https%3A%2F%2Fwww.facebook.com%2FHillcrestBaptistChurch.CarlisleOH%2Fvideos%2F701011743797242%2F&width=1280\" width=\"1280\" height=\"720\" style=\"border:none;overflow:hidden\" scrolling=\"no\" frameborder=\"0\" allowfullscreen=\"false\" allow=\"autoplay; clipboard-write; encrypted-media; picture-in-picture; web-share\" allowFullScreen=\"false\"></iframe>";
    }

    public void setAutoPlayerHeight(Context context, boolean landscape) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((AppCompatActivity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        if(landscape) {
            this.getLayoutParams().width = (int) (displayMetrics.heightPixels * Double.parseDouble(context.getString(R.string.livestream_video_ratio)));
            this.getLayoutParams().height = (displayMetrics.heightPixels);
        } else {
            this.getLayoutParams().height = (int) (displayMetrics.widthPixels / Double.parseDouble(context.getString(R.string.livestream_video_ratio)));
            this.getLayoutParams().width = (displayMetrics.widthPixels);
        }
    }
}