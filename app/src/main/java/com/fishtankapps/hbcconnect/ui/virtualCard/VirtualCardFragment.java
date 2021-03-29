package com.fishtankapps.hbcconnect.ui.virtualCard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.fishtankapps.hbcconnect.R;
import com.fishtankapps.hbcconnect.activity.HBCConnectActivity;
import com.fishtankapps.hbcconnect.activity.SubmitCountMeInCardActivity;
import com.fishtankapps.hbcconnect.activity.SubmitPrayerRequestActivity;
import com.fishtankapps.hbcconnect.dataStorage.SubmittedCountMeInCard;
import com.fishtankapps.hbcconnect.dataStorage.SubmittedPrayerRequestCard;
import com.fishtankapps.hbcconnect.utilities.Utilities;
import com.google.android.material.tabs.TabLayout;

public class VirtualCardFragment extends Fragment {

    private View root;
    private TextView noSubmissionsTextView;
    private TabLayout tabLayout;
    private LinearLayout selectedCardTypeLinearLayout;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.virtual_card_screen_layout, container, false);

        noSubmissionsTextView = root.findViewById(R.id.noSummitionsTextView);


        Button submitNewCountMeIn = root.findViewById(R.id.submitNewCountMeIn);
        submitNewCountMeIn.setOnClickListener(l-> {
            if(Utilities.isInternetUnavailable()) {
                Toast.makeText(root.getContext(), "Internet is required to submit a virtual card.", Toast.LENGTH_LONG).show();
                return;
            }

            Intent submitCountMeInActivity = new Intent(HBCConnectActivity.hbcConnectActivity, SubmitCountMeInCardActivity.class);
            startActivity(submitCountMeInActivity);
        });

        Button submitNewPrayerRequest = root.findViewById(R.id.submitNewPrayerRequest);
        submitNewPrayerRequest.setOnClickListener(l-> {
            if(Utilities.isInternetUnavailable()) {
                Toast.makeText(root.getContext(), "Internet is required to submit a virtual card.", Toast.LENGTH_LONG).show();
                return;
            }

            Intent submitCountMeInActivity = new Intent(HBCConnectActivity.hbcConnectActivity, SubmitPrayerRequestActivity.class);
            startActivity(submitCountMeInActivity);
        });


        selectedCardTypeLinearLayout = root.findViewById(R.id.selectedCardTypeLinearLayout);
        selectedCardTypeLinearLayout.removeView(submitNewPrayerRequest);


        tabLayout = root.findViewById(R.id.cardTypeSelector);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                Log.d("V-Card", "tabLayout.onTabSelected, Position: " + position);

                Animation fadeOut = new AlphaAnimation(1, 0);
                fadeOut.setInterpolator(new DecelerateInterpolator());
                fadeOut.setDuration(250);

                fadeOut.setAnimationListener(new Animation.AnimationListener() {

                    public void onAnimationEnd(Animation animation) {

                        if(position == 0){
                            selectedCardTypeLinearLayout.removeView(submitNewPrayerRequest);
                            selectedCardTypeLinearLayout.addView(submitNewCountMeIn, 0);
                        } else {
                            selectedCardTypeLinearLayout.addView(submitNewPrayerRequest, 0);
                            selectedCardTypeLinearLayout.removeView(submitNewCountMeIn);
                        }

                        Animation fadeIn = new AlphaAnimation(0, 1);
                        fadeIn.setInterpolator(new AccelerateInterpolator());
                        fadeIn.setDuration(250);

                        selectedCardTypeLinearLayout.setAnimation(fadeIn);
                    }
                    public void onAnimationStart(Animation animation) {}
                    public void onAnimationRepeat(Animation animation) {}
                });
                selectedCardTypeLinearLayout.setAnimation(fadeOut);

                refreshPreviousSubmissions();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });




        refreshPreviousSubmissions();
        return root;
    }

    public void refreshPreviousSubmissions() {
        LinearLayout layout = root.findViewById(R.id.selectedCardTypeLinearLayout);

        while(layout.getChildCount() > 2)
            layout.removeViewAt(2);

        if(tabLayout.getSelectedTabPosition() == 0){
            if(HBCConnectActivity.dataFile.getSubmittedCountMeInCards().size() == 0)
                layout.addView(noSubmissionsTextView);

            for(SubmittedCountMeInCard countMeInCard : HBCConnectActivity.dataFile.getSubmittedCountMeInCards())
                layout.addView(countMeInCard.getTextViewDisplay(root.getContext(), this));

        } else {
            if(HBCConnectActivity.dataFile.getSubmittedPrayerRequestCards().size() == 0)
                layout.addView(noSubmissionsTextView);

            for(SubmittedPrayerRequestCard prayerRequestCard : HBCConnectActivity.dataFile.getSubmittedPrayerRequestCards())
                layout.addView(prayerRequestCard.getTextViewDisplay(root.getContext(), this));
        }


    }

    public void onResume() {
        super.onResume();
        refreshPreviousSubmissions();
    }
}