package com.fishtankapps.hbcconnect.ui.livestream;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.fishtankapps.hbcconnect.R;
import com.fishtankapps.hbcconnect.activity.HBCConnectActivity;
import com.fishtankapps.hbcconnect.activity.WatchFacebookVideoActivity;
import com.fishtankapps.hbcconnect.dataStorage.LivestreamData;
import com.fishtankapps.hbcconnect.utilities.Constants;
import com.fishtankapps.hbcconnect.utilities.Utilities;

import java.util.ArrayList;

public class LiveStreamSelector extends Fragment {

    public static final String LIVE_VIDEO_ID = "LIVE";

    private static final String ALL_LIVESTREAMS = "All Livestreams";

    private LinearLayout livestreamButtonLayout;
    private Spinner livestreamTypeSpinner;
    private SearchView livestreamSearcher;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.livestream_selector_layout, container, false);

        Button watchLiveLivestream = root.findViewById(R.id.watchLiveLivestream);
        watchLiveLivestream.setOnClickListener(l -> {
            Intent watchVideoIntent = new Intent(HBCConnectActivity.hbcConnectActivity, WatchFacebookVideoActivity.class);
            watchVideoIntent.putExtra(Constants.LIVESTREAM_ID, LIVE_VIDEO_ID);
            startActivity(watchVideoIntent);
        });

        root.findViewById(R.id.spacer) .setBackgroundColor(Utilities.getColor(root.getContext(), R.attr.colorSecondary));
        root.findViewById(R.id.spacer2).setBackgroundColor(Utilities.getColor(root.getContext(), R.attr.colorSecondary));

        livestreamButtonLayout = root.findViewById(R.id.livestreamLayout);
        livestreamSearcher = root.findViewById(R.id.livestreamSearchBar);
        livestreamSearcher.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            public boolean onQueryTextChange(String s) {
                refreshLiveStreamVideos();
                return false;
            }
        });

        setUpSpinner(root);
        setUpSwipeToRefresh(root);
        refreshLiveStreamVideos();

        return root;
    }

    private volatile boolean notRefreshedYet = false;
    private void setUpSwipeToRefresh(View root){
        SwipeRefreshLayout swipeRefreshLayout = root.findViewById(R.id.livestreamSwipeRefresh);

        swipeRefreshLayout.setOnRefreshListener(() -> {

            if(HBCConnectActivity.hbcConnectServer ==  null || !HBCConnectActivity.hbcConnectServer.isConnected()){
                Toast.makeText(HBCConnectActivity.hbcConnectActivity, "No Connection to Server", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
                return;
            }

            notRefreshedYet = true;
            new Thread(() -> {
                HBCConnectActivity.dataFile.syncDataWithServer(HBCConnectActivity.hbcConnectServer);
                notRefreshedYet = false;
            }).start();

            while (notRefreshedYet)
                try{Thread.sleep(100);}catch(Exception ignored){}

            Log.d("LiveStreamSelector", "setUpSwipeToRefresh: Done Refreshing!");
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void setUpSpinner(View root){
        livestreamTypeSpinner = root.findViewById(R.id.livestreamTypeSpinner);

        ArrayList<String> livestreamTypes = new ArrayList<>();
        livestreamTypes.add(ALL_LIVESTREAMS);
        for(LivestreamData data : HBCConnectActivity.dataFile.getPreviousLiveStreams())
            if(!livestreamTypes.contains(data.getLivestreamClassification()))
                livestreamTypes.add(data.getLivestreamClassification());


        ArrayAdapter<String> adapter = new ArrayAdapter<>(root.getContext(), R.layout.spinner_item_look, livestreamTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        livestreamTypeSpinner.setAdapter(adapter);

        livestreamTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                refreshLiveStreamVideos();
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                refreshLiveStreamVideos();
            }
        });
    }

    private boolean isCloseEnoughToSearchText(LivestreamData livestreamData){
        String searchBarText = livestreamSearcher.getQuery().toString();
        String livestreamDataString = livestreamData.toString();


        if(searchBarText.equals(""))
            return true;

        else return searchBarText.toUpperCase().contains(livestreamDataString.toUpperCase()) ||
                livestreamDataString.toUpperCase().contains(searchBarText.toUpperCase());
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private void refreshLiveStreamVideos(){

        while(livestreamButtonLayout.getChildCount() > 0)
            livestreamButtonLayout.removeViewAt(0);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        params.setMargins(10, 30, 10, 30);

        for(LivestreamData livestreamData : HBCConnectActivity.dataFile.getPreviousLiveStreams()){

            if(!livestreamTypeSpinner.getSelectedItem().toString().equals(ALL_LIVESTREAMS) &&
                    !livestreamTypeSpinner.getSelectedItem().toString().equals(livestreamData.getLivestreamClassification()))
                continue;

            if(!isCloseEnoughToSearchText(livestreamData))
                continue;

            Button livestreamButton = new Button(livestreamButtonLayout.getContext());

            livestreamButton.setText(livestreamData.getLivestreamName() + "\n- " + livestreamData.getLivestreamClassification());
            livestreamButton.setAllCaps(false);

            livestreamButton.setTextSize(22);
            livestreamButton.setTextAlignment(Button.TEXT_ALIGNMENT_TEXT_START);

            livestreamButton.setPadding(20, 35, 20, 35);
            livestreamButton.setLayoutParams(params);

            int color = Utilities.changeColorBrightness(Utilities.getColor(livestreamButtonLayout.getContext(), android.R.attr.colorBackground),
                    (Utilities.isInDarkMode(livestreamButtonLayout.getContext()) ? 1.75f : 0.975f));

            livestreamButton.setBackgroundColor(color);

            livestreamButton.setOnClickListener((button) -> {
                if(!Utilities.isInternetAvailable()){
                    Toast.makeText(HBCConnectActivity.hbcConnectActivity, "You need an Internet connection to watch livestreams. Sorry. :-(", Toast.LENGTH_LONG).show();
                    return;
                }

                Intent watchVideoIntent = new Intent(HBCConnectActivity.hbcConnectActivity, WatchFacebookVideoActivity.class);
                watchVideoIntent.putExtra(Constants.LIVESTREAM_ID, livestreamData.getLivestreamID());
                startActivity(watchVideoIntent);
            });

            livestreamButtonLayout.addView(livestreamButton);
        }
    }
    
}