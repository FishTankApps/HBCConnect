package com.fishtankapps.hbcconnect.ui.countMeIn;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.fishtankapps.hbcconnect.R;
import com.fishtankapps.hbcconnect.activity.HBCConnectActivity;
import com.fishtankapps.hbcconnect.activity.SubmitCountMeInCardActivity;
import com.fishtankapps.hbcconnect.dataStorage.SubmittedCountMeInCard;
import com.fishtankapps.hbcconnect.utilities.Utilities;

public class CountMeInFragment extends Fragment {

    private View root;
    private TextView noSubmissionsTextView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.count_me_in_screen_layout, container, false);

        noSubmissionsTextView = root.findViewById(R.id.noSummitionsTextView);

        Button submitNewCountMeIn = root.findViewById(R.id.submitNewCountMeIn);
        submitNewCountMeIn.setOnClickListener(l-> {
            if(!Utilities.isInternetAvailable()) {
                Toast.makeText(root.getContext(), "Internet is required to submit a virtual card.", Toast.LENGTH_LONG).show();
                return;
            }

            Intent submitCountMeInActivity = new Intent(HBCConnectActivity.hbcConnectActivity, SubmitCountMeInCardActivity.class);
            startActivity(submitCountMeInActivity);
        });

        refreshPreviousCountMeInCards();
        return root;
    }

    public void refreshPreviousCountMeInCards() {
        LinearLayout layout = root.findViewById(R.id.countMeInLinearLayout);

        while(layout.getChildCount() > 2)
            layout.removeViewAt(2);

        if(HBCConnectActivity.dataFile.getSubmittedCountMeInCards().size() == 0)
            layout.addView(noSubmissionsTextView);

        for(SubmittedCountMeInCard countMeInCard : HBCConnectActivity.dataFile.getSubmittedCountMeInCards()){
            layout.addView(countMeInCard.getTextViewDisplay(root.getContext(), this));
        }
    }

    public void onResume() {
        super.onResume();
        refreshPreviousCountMeInCards();
    }
}