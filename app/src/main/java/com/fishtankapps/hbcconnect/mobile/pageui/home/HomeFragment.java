package com.fishtankapps.hbcconnect.mobile.pageui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.fishtankapps.hbcconnect.R;
import com.fishtankapps.hbcconnect.mobile.activities.CreateNewEventActivity;
import com.fishtankapps.hbcconnect.mobile.activities.SubmitFeedbackActivity;
import com.fishtankapps.hbcconnect.mobile.storage.DataFile;
import com.fishtankapps.hbcconnect.mobile.storage.UpcomingEvent;
import com.fishtankapps.hbcconnect.mobile.utilities.Utilities;

import java.util.Arrays;

public class HomeFragment extends Fragment {

    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.home_screen_layout, container, false);

        if(DataFile.getSharedPreferenceBooleanValue(getString(R.string.admin_permission), requireContext()))
            (root.findViewById(R.id.addEventButton)).setVisibility(View.VISIBLE);
        else
            (root.findViewById(R.id.addEventButton)).setVisibility(View.INVISIBLE);

        root.findViewById(R.id.addEventButton).setOnClickListener(this::addNewEvent);

        updateUpcomingEventsList();
        setUpSwipeToRefresh();
        return root;
    }

    private void addNewEvent(View view) {
        Intent intent = new Intent(getContext(), CreateNewEventActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUpcomingEventsButtons();

        if(DataFile.getSharedPreferenceBooleanValue(getString(R.string.admin_permission), requireContext()))
            (root.findViewById(R.id.addEventButton)).setVisibility(View.VISIBLE);
        else
            (root.findViewById(R.id.addEventButton)).setVisibility(View.INVISIBLE);
    }

    private void setUpSwipeToRefresh(){
        SwipeRefreshLayout swipeRefreshLayout = root.findViewById(R.id.homeScreenSwipeRefresh);

        swipeRefreshLayout.setOnRefreshListener(() -> {

            if(Utilities.isInternetUnavailable()){
                Toast.makeText(getContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
                return;
            }

            Log.d("HomeScreen", "setUpSwipeToRefresh: Started Refreshing...");
            DataFile.getDataFile(getContext()).syncUpcomingEvents(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    updateUpcomingEventsButtons();
                    Log.d("HomeScreen", "setUpSwipeToRefresh: Done Refreshing!");
                });
        });
    }


    public void updateUpcomingEventsList(){
        DataFile.getDataFile(getContext()).syncUpcomingEvents(this::updateUpcomingEventsButtons);
    }

    public void updateUpcomingEventsButtons(){
        LinearLayout upcomingEventsLinearLayout = root.findViewById(R.id.upcomingEventsLinearLayout);
        upcomingEventsLinearLayout.removeAllViews();

        for(UpcomingEvent upcomingEvent : DataFile.getDataFile(getContext()).getUpcomingEvents()) {
            UpcomingEventView upcomingEventView = new UpcomingEventView(requireActivity(), upcomingEvent, (menuItem) -> menuButtonClick(menuItem, upcomingEvent), this);

            if(upcomingEventView.shouldBeVisible())
                upcomingEventsLinearLayout.addView(upcomingEventView);
        }
    }

    private boolean menuButtonClick(MenuItem menuItem, UpcomingEvent upcomingEvent){
        Log.d("HomeFragment", "MenuItem Clicked: " + menuItem.getTitle() + ", Event: " + upcomingEvent);

        if(menuItem.getItemId() == R.id.dismis_event) {
            DataFile.getDataFile(getContext()).addDismissedEvent(upcomingEvent);
            DataFile.getDataFile(getContext()).cleanEventList();

            updateUpcomingEventsButtons();
        } else if (menuItem.getItemId() == R.id.suggest_event_edit){
            String feedbackTemp = "Event Name: " + upcomingEvent.getName() +
                    "\nEvent Date: " + upcomingEvent.formatDate() +
                    "\nEvent Time: " + upcomingEvent.getTimeframe() +
                    "\nEvent Tags: " + Arrays.toString(upcomingEvent.getTags()) +
                    "\n\nEvent Description:\n" + upcomingEvent.getDescription();


            Intent feedbackIntent = new Intent(root.getContext(), SubmitFeedbackActivity.class);
            feedbackIntent.putExtra(getString(R.string.feedback_focus), "Event #" + upcomingEvent.getId() + ": \"" + upcomingEvent.getName() + "\"");
            feedbackIntent.putExtra(getString(R.string.feedback_template), feedbackTemp);
            startActivity(feedbackIntent);

            requireActivity().overridePendingTransition(R.anim.enter_from_the_right, R.anim.exit_to_the_left);
        }

        return true;
    }
}