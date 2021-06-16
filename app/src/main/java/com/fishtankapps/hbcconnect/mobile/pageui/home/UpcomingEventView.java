package com.fishtankapps.hbcconnect.mobile.pageui.home;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.fishtankapps.hbcconnect.R;
import com.fishtankapps.hbcconnect.mobile.storage.DataFile;
import com.fishtankapps.hbcconnect.mobile.storage.UpcomingEvent;
import com.fishtankapps.hbcconnect.mobile.utilities.Utilities;

public class UpcomingEventView extends CardView {

    private boolean shouldBeVisible = false;

    public UpcomingEventView(Context context) {
        super(context);
    }

    public UpcomingEventView(Activity activity, UpcomingEvent upcomingEvent, OnMenuButtonClickedListener listener, HomeFragment homeFragment) {
        super(activity);

        int pxOf5dp = (int) (5 * getContext().getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);

        LinearLayout.LayoutParams matchParentParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);

        LinearLayout.LayoutParams fillWidthParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);

        LinearLayout.LayoutParams wrapContentParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);

        LinearLayout.LayoutParams tagLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);

        tagLayoutParams.setMargins(15, 0, 15, 0);
        cardParams.setMargins(pxOf5dp,pxOf5dp,pxOf5dp,pxOf5dp);

        setLayoutParams(cardParams);
        setContentPadding(pxOf5dp, pxOf5dp, pxOf5dp, pxOf5dp);
        setCardElevation(pxOf5dp);
        setRadius(pxOf5dp);

        Toolbar toolbar = new Toolbar(activity);
        toolbar.setTitle(upcomingEvent.getName());
        toolbar.inflateMenu(R.menu.upcoming_event_toolbar_menu);
        toolbar.setOnMenuItemClickListener(listener::menuButtonClicked);

        TextView eventDate = new TextView(activity);
        eventDate.setText(upcomingEvent.formatDate());
        eventDate.setTextSize(12f);
        eventDate.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        eventDate.setLayoutParams(wrapContentParams);

        TextView eventTime = new TextView(activity);
        eventTime.setText(upcomingEvent.getTimeframe());
        eventTime.setTextSize(12f);
        eventTime.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        eventTime.setLayoutParams(wrapContentParams);

        TextView eventDescription = new TextView(activity);
        eventDescription.setText(upcomingEvent.getDescription());
        eventDescription.setLayoutParams(fillWidthParams);
        eventDescription.setPadding(0,0,0,50);

        LinearLayout horizontalLinearLayout = new LinearLayout(activity);
        horizontalLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        horizontalLinearLayout.setLayoutParams(tagLayoutParams);

        TextView tagLabel = new TextView(activity);
        tagLabel.setText(R.string.tags);
        horizontalLinearLayout.addView(tagLabel);

        LinearLayout verticalLinearLayout = new LinearLayout(activity);
        verticalLinearLayout.setOrientation(LinearLayout.VERTICAL);
        verticalLinearLayout.setLayoutParams(matchParentParams);

        verticalLinearLayout.addView(toolbar);
        verticalLinearLayout.addView(eventDate);
        verticalLinearLayout.addView(eventTime);
        verticalLinearLayout.addView(eventDescription);
        verticalLinearLayout.addView(horizontalLinearLayout);

        addView(verticalLinearLayout);

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for(String tag : upcomingEvent.getTags()) {
            String key = tag.toLowerCase().replace(' ', '.');
            if (!DataFile.getSharedPreferenceBooleanValue(activity.getString(R.string.tag_start)+ key, true, getContext()))
                continue;

            shouldBeVisible = true;

            Button tagView = (Button) inflater.inflate(R.layout.tag_button_layout, horizontalLinearLayout, false);
            tagView.setText(tag);
            tagView.setBackgroundColor(Utilities.getColor(activity, R.attr.colorOnPrimary));
            tagView.setOnClickListener((l) -> {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Would you like to ignore the \"" + tag + "\" tag? ");
                builder.setPositiveButton("Yes", (dialog, which) -> {

                    DataFile.setSharedPreferenceBooleanValue(activity.getString(R.string.tag_start) + key, false, getContext());
                    homeFragment.updateUpcomingEventsButtons();
                });
                builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());
                builder.show();
            });

            horizontalLinearLayout.addView(tagView);
        }

        EventBackgroundDrawable eventBackgroundDrawable = new EventBackgroundDrawable(upcomingEvent);
        verticalLinearLayout.setBackground(eventBackgroundDrawable);

        if(upcomingEvent.isLoadingBitMap()){
            new Thread(() -> {
                try {
                    do {
                        Thread.sleep(100);
                    } while (upcomingEvent.getBackgroundImage() == null);

                    activity.runOnUiThread(verticalLinearLayout::invalidate);
                } catch (InterruptedException ignore) {}
            }).start();
        }

    }

    public boolean shouldBeVisible() {
        return shouldBeVisible;
    }

    public interface OnMenuButtonClickedListener {
        boolean menuButtonClicked(MenuItem menuItem);
    }
}
