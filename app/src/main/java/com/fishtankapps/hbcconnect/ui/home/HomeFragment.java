package com.fishtankapps.hbcconnect.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.fishtankapps.hbcconnect.R;
import com.fishtankapps.hbcconnect.activity.HBCConnectActivity;
import com.fishtankapps.hbcconnect.dataStorage.DataFile;
import com.fishtankapps.hbcconnect.dataStorage.UpcomingEvent;
import com.fishtankapps.hbcconnect.utilities.Utilities;
import com.fishtankapps.hbcconnect.utilities.firebase.FirebaseDatabaseInterface;

import java.util.Objects;

public class HomeFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.home_screen_layout, container, false);
        root.findViewById(R.id.homeDividerLine).setBackgroundColor(Utilities.getColor(root.getContext(), R.attr.colorSecondary));

        updateUpcomingEventsList(root);
        setUpSwipeToRefresh(root);
        return root;
    }

    private void setUpSwipeToRefresh(View root){
        SwipeRefreshLayout swipeRefreshLayout = root.findViewById(R.id.homeScreenSwipeRefresh);

        swipeRefreshLayout.setOnRefreshListener(() -> {

            if(Utilities.isInternetUnavailable()){
                Toast.makeText(HBCConnectActivity.hbcConnectActivity, "No Internet Connection", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
                return;
            }

            Log.d("HomeScreen", "setUpSwipeToRefresh: Started Refreshing...");
            HBCConnectActivity.dataFile.syncUpcomingEvents(new DataFile.OnSyncCompleteListener() {
                public void doneSyncingLivestream() {}
                public void doneSyncingUpcomingEvents() {
                    swipeRefreshLayout.setRefreshing(false);
                    updateUpcomingEventsButtons(root);
                    Log.d("HomeScreen", "setUpSwipeToRefresh: Done Refreshing!");
                }
            });
        });
    }


    public void updateUpcomingEventsList(View root){
        HBCConnectActivity.dataFile.syncUpcomingEvents(new DataFile.OnSyncCompleteListener() {
            public void doneSyncingLivestream() {}
            public void doneSyncingUpcomingEvents() {
                updateUpcomingEventsButtons(root);
            }
        });
    }

    private void updateUpcomingEventsButtons(View root){
        Log.e("HomeScreen", "updateUpcomingEventsButtons: HERE");

        LinearLayout upcomingEventsLinearLayout = root.findViewById(R.id.upcomingEventsLinearLayout);
        upcomingEventsLinearLayout.removeAllViews();

        int pxOf5dp = (int) (5 * requireContext().getResources().getDisplayMetrics().density);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(pxOf5dp,pxOf5dp,pxOf5dp,pxOf5dp);


        /*
         *    .-----------------------.
         *    | Event Title      Date |  Vertical Linear Layout
         *    | Description ........  |   '-> Horizontal Linear Layout
         *    | ..................... |   '   '-> Event Title TextView
         *    |               Dismiss |   '   '-> Spacer
         *    '-----------------------'   '   '-> Date TextView
         *                                '-> Description TextView
         *   Hold Click: Suggest Edit     '-> Horizontal Linear Layout
         *                                    '-> Spacer
         *                                    '-> Dismiss Button
         */

        for(UpcomingEvent upcomingEvent : HBCConnectActivity.dataFile.getUpcomingEvents()) {
            CardView card = new CardView(root.getContext());
            card.setLayoutParams(cardParams);
            card.setContentPadding(pxOf5dp,pxOf5dp,pxOf5dp,pxOf5dp);
            card.setCardElevation(pxOf5dp);
            card.setRadius(pxOf5dp);

            Button button = new Button(root.getContext());
            button.setText(upcomingEvent.toString());

            card.addView(button);

            upcomingEventsLinearLayout.addView(card);
        }
    }
}