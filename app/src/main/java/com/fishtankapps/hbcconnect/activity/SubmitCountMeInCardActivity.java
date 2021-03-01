package com.fishtankapps.hbcconnect.activity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fishtankapps.hbcconnect.R;
import com.fishtankapps.hbcconnect.dataStorage.SubmittedCountMeInCard;
import com.fishtankapps.hbcconnect.utilities.email.SendMail;

public class SubmitCountMeInCardActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.submit_count_me_in_screen_layout);

        findViewById(R.id.submitCountMeIn).setOnClickListener(l->submitCountMeIn());
    }

    private void submitCountMeIn(){
        SubmittedCountMeInCard submittedCountMeInCard = new SubmittedCountMeInCard(((EditText) findViewById(R.id.activityNameEditText)).getText().toString(),
                ((EditText) findViewById(R.id.nameEditText)).getText().toString(),
                ((EditText) findViewById(R.id.phoneEditText)).getText().toString(),
                ((EditText) findViewById(R.id.numberOfAdultsEditText)).getText().toString(),
                ((EditText) findViewById(R.id.numberOfChildrenEditText)).getText().toString(),
                ((EditText) findViewById(R.id.commentsEditText)).getText().toString());

        if(submittedCountMeInCard.isInputValid()){
            SendMail sendMail = new SendMail(this, "New Virtual Count Me In Card! #" + (long) (Math.random() * 1_000_000_000), submittedCountMeInCard.getHTMLEmail());
            sendMail.execute();

            HBCConnectActivity.dataFile.addSubmittedCountMeInCards(submittedCountMeInCard);
        } else
            Toast.makeText(this, "Please make sure you've filled in all the required info.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
