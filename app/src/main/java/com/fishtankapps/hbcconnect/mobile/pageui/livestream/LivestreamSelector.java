package com.fishtankapps.hbcconnect.mobile.pageui.livestream;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.fishtankapps.hbcconnect.R;
import com.fishtankapps.hbcconnect.mobile.activities.LivestreamViewerActivity;
import com.fishtankapps.hbcconnect.mobile.storage.DataFile;
import com.fishtankapps.hbcconnect.mobile.storage.LivestreamData;
import com.fishtankapps.hbcconnect.mobile.utilities.Utilities;
import com.fishtankapps.hbcconnect.mobile.utilities.firebase.FirebaseDatabaseInterface;

import java.util.ArrayList;
import java.util.Arrays;

public class LivestreamSelector extends Fragment {

    public static final String LIVE_VIDEO_ID = "LIVE";

    private static final String ALL_LIVESTREAMS = "All Livestreams";

    private LinearLayout livestreamButtonLayout;
    private Spinner livestreamTypeSpinner;
    private SearchView livestreamSearcher;

    private ArrayList<WeightedLivestreamData> livestreamDataList;

    private boolean loadingMoreLivestreams = false;
    private Thread loadMoreThread;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.livestream_selector_layout, container, false);

        livestreamDataList = new ArrayList<>();

        Button watchLiveLivestream = root.findViewById(R.id.watchLiveLivestream);
        watchLiveLivestream.setOnClickListener(l -> {
                if(Utilities.isInternetUnavailable()){
                    Toast.makeText(root.getContext(), "You need an Internet connection to watch livestreams. Sorry. :-(", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseDatabaseInterface.getFirebaseDatabaseInterface().getValue("livestreams/live_livestream", (value) -> {


                    Intent watchVideoIntent = new Intent(requireActivity(), LivestreamViewerActivity.class);
                    watchVideoIntent.putExtra(getString(R.string.livestream_id), value.toString());
                    watchVideoIntent.putExtra(getString(R.string.livestream_name), "Live Livestream");
                    startActivity(watchVideoIntent);
                    requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                });
            });

        livestreamButtonLayout = root.findViewById(R.id.livestreamLayout);
        livestreamSearcher = root.findViewById(R.id.livestreamSearchBar);
        livestreamSearcher.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            public boolean onQueryTextChange(String s) {
                refreshLivestreamButtons();
                return false;
            }
        });

        livestreamTypeSpinner = root.findViewById(R.id.livestreamTypeSpinner);

        refreshUpSpinner();
        setUpSwipeToRefresh(root);


        loadMoreThread = new Thread(() -> {
            try{
                loadMoreLivestreams();

                ProgressBar loadingBar = root.findViewById(R.id.livestreamLoadingWheel);
                NestedScrollView scrollView = root.findViewById(R.id.livestreamScrollView);

                 while (true){
                     Thread.sleep(250);

                     Rect scrollBounds = new Rect();
                     scrollView.getHitRect(scrollBounds);

                     if (loadingBar.getLocalVisibleRect(scrollBounds) && loadingBar.isShown() && !loadingMoreLivestreams) {
                         loadMoreLivestreams();

                     }
                 }
            } catch (InterruptedException ignore){}

            Log.d("LivestreamLoader", "Thread Stopped!");
        });
        loadMoreThread.start();


        return root;
    }

    public void onDestroy(){
        loadMoreThread.interrupt();
        super.onDestroy();
    }

    private void setUpSwipeToRefresh(View root){
        SwipeRefreshLayout swipeRefreshLayout = root.findViewById(R.id.livestreamSwipeRefresh);

        swipeRefreshLayout.setOnRefreshListener(() -> {

            if(Utilities.isInternetUnavailable()){
                Toast.makeText(getContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
                return;
            }

            Log.d("LiveStreamSelector", "setUpSwipeToRefresh: Started Refreshing...");
            DataFile.getDataFile(getContext()).checkForNewLivestreams(getContext(), (newLivestreams) -> {
                for(LivestreamData livestream : newLivestreams)
                    livestreamDataList.add(new WeightedLivestreamData(livestream, getLivestreamSearchWeight(livestream)));

                requireActivity().runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);

                    refreshLivestreamButtons();
                    loadingMoreLivestreams = false;
                });
            });
        });
    }

    private void refreshUpSpinner(){
        ArrayList<String> livestreamTypes = new ArrayList<>();
        livestreamTypes.add(ALL_LIVESTREAMS);
        for(LivestreamData data : DataFile.getDataFile(getContext()).getPreviousLiveStreams())
            if(!livestreamTypes.contains(data.getLivestreamTag()))
                livestreamTypes.add(data.getLivestreamTag());


        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item_look, livestreamTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        livestreamTypeSpinner.setAdapter(adapter);

        livestreamTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                refreshLivestreamButtons();
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                refreshLivestreamButtons();
            }
        });
    }

    private int getLivestreamSearchWeight(LivestreamData livestreamData){
        if(livestreamSearcher.getQuery().toString().equals(""))
            return 1;

        String[] keywords = livestreamSearcher.getQuery().toString().split(" ");

        Log.v("LivestreamSelector", "Search Key Words: " + Arrays.toString(keywords) + " length=" + keywords.length);
        int weight = -1;

        for(String word : keywords){
            if(livestreamData.getLivestreamName().toUpperCase().contains(word.toUpperCase()) ||
            word.toUpperCase().contains(livestreamData.getLivestreamName().toUpperCase()))
                weight++;
        }

        return weight;
    }


    /**
     * Ask DataFile for 10 livestreams, first from the DataFile, then the Remote Database
     */
    private void loadMoreLivestreams() {
        loadingMoreLivestreams = true;

        int numberOfNewLivestreams = requireContext().getResources().getInteger(R.integer.load_more_livestreams_count);
        Log.d("LivestreamSelector", "Loading " + numberOfNewLivestreams + "More Livestreams...");

        long lowestID;

        if(livestreamDataList.size() == 0)
            lowestID = -1;
        else
            lowestID = livestreamDataList.get(0).getLivestreamData().getLivestreamDataId();

        DataFile.getDataFile(getContext()).getMoreLivestreams(getContext(), lowestID, (livestreamData) -> {
                    for(LivestreamData livestream : livestreamData)
                        livestreamDataList.add(new WeightedLivestreamData(livestream, getLivestreamSearchWeight(livestream)));

                    requireActivity().runOnUiThread(() -> {
                        refreshLivestreamButtons();
                        loadingMoreLivestreams = false;
                    });

                });
    }


    /**
     *  Use the livestreamDataList to generate new buttons
     */
    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private void refreshLivestreamButtons(){

        Utilities.quickSort(livestreamDataList);

        ArrayList<WeightedLivestreamData> newWeightLivestreams = new ArrayList<>();

        for(WeightedLivestreamData oldWeightedLivestreamData : livestreamDataList)
            newWeightLivestreams.add(new WeightedLivestreamData(oldWeightedLivestreamData.getLivestreamData(), getLivestreamSearchWeight(oldWeightedLivestreamData.getLivestreamData())));

        livestreamDataList.clear();
        livestreamDataList.addAll(newWeightLivestreams);

        Utilities.quickSort(livestreamDataList);

        while(livestreamButtonLayout.getChildCount() > 1)
            livestreamButtonLayout.removeViewAt(0);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        params.setMargins(10, 30, 10, 30);

        for(WeightedLivestreamData weightedLivestreamData : livestreamDataList){
            LivestreamData livestreamData = weightedLivestreamData.getLivestreamData();

            if(!livestreamTypeSpinner.getSelectedItem().toString().equals(ALL_LIVESTREAMS) &&
                    !livestreamTypeSpinner.getSelectedItem().toString().equals(livestreamData.getLivestreamTag()))
                continue;

            LivestreamView livestreamView = new LivestreamView(requireActivity(), livestreamData);
            livestreamView.setOnClickListener((l) -> {
                    if(Utilities.isInternetUnavailable()){
                        Toast.makeText(getContext(), "You need an Internet connection to watch livestreams. Sorry. :-(", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Intent watchVideoIntent = new Intent(getContext(), LivestreamViewerActivity.class);
                    watchVideoIntent.putExtra(getString(R.string.livestream_id), livestreamData.getLivestreamVideoID());
                    watchVideoIntent.putExtra(getString(R.string.livestream_name), livestreamData.getLivestreamName());
                    startActivity(watchVideoIntent);
                    requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                });

            livestreamButtonLayout.addView(livestreamView, 0);
        }
    }
}