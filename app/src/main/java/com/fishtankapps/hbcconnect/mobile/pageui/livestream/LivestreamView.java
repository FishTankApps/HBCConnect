package com.fishtankapps.hbcconnect.mobile.pageui.livestream;

import android.app.Activity;
import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.fishtankapps.hbcconnect.mobile.storage.LivestreamData;

public class LivestreamView extends CardView {

    public LivestreamView(Context context) {
        super(context);
    }

    public LivestreamView(Activity activity, LivestreamData livestreamData) {
        super(activity);

        setClickable(true);
        setFocusable(true);

        int pxOf5dp = (int) (5 * getContext().getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);

        LinearLayout.LayoutParams matchParentParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);

        LinearLayout.LayoutParams fillWidthParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);

        cardParams.setMargins(pxOf5dp,pxOf5dp,pxOf5dp,pxOf5dp);

        setLayoutParams(cardParams);
        setContentPadding(pxOf5dp, pxOf5dp, pxOf5dp, pxOf5dp);
        setCardElevation(pxOf5dp);
        setRadius(pxOf5dp);


        TextView livestreamNameTextView = new TextView(activity);
        livestreamNameTextView.setText(livestreamData.getLivestreamName());
        livestreamNameTextView.setTextSize(20f);
        livestreamNameTextView.setLayoutParams(fillWidthParams);

        TextView livestreamTagTextView = new TextView(activity);
        livestreamTagTextView.setText(livestreamData.getLivestreamTag());
        livestreamTagTextView.setTextSize(18f);
        livestreamTagTextView.setLayoutParams(fillWidthParams);
        livestreamTagTextView.setPadding(0,0,0,50);

        LinearLayout verticalLinearLayout = new LinearLayout(activity);
        verticalLinearLayout.setOrientation(LinearLayout.VERTICAL);
        verticalLinearLayout.setLayoutParams(matchParentParams);

        verticalLinearLayout.addView(livestreamNameTextView);
        verticalLinearLayout.addView(livestreamTagTextView);

        addView(verticalLinearLayout);

        LivestreamBackgroundDrawable livestreamBackgroundDrawable = new LivestreamBackgroundDrawable(livestreamData);
        verticalLinearLayout.setBackground(livestreamBackgroundDrawable);

        if(livestreamData.isLoadingBitMap()){
            new Thread(() -> {
                try {
                    do {
                        Thread.sleep(100);
                    } while (livestreamData.getBackgroundImage() == null);

                    activity.runOnUiThread(verticalLinearLayout::invalidate);
                } catch (InterruptedException ignore) {}
            }).start();
        }
    }
}
