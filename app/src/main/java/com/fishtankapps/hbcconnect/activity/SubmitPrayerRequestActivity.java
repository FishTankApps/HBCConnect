package com.fishtankapps.hbcconnect.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fishtankapps.hbcconnect.R;
import com.fishtankapps.hbcconnect.dataStorage.SubmittedPrayerRequestCard;
import com.fishtankapps.hbcconnect.utilities.email.SendMail;

import java.util.Objects;

public class SubmitPrayerRequestActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.submit_prayer_request_screen_layout);

        findViewById(R.id.submitPrayerRequest).setOnClickListener(l->submitCountMeIn());

        try {
            setSupportActionBar(findViewById(R.id.prayerRequestToolbar));
            Objects.requireNonNull(getSupportActionBar()).setTitle("Prayer Request");
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);

        } catch (Exception e){
            Log.w("Prayer Request", "Error setting up SupportActionBar.");
        }
    }

    private void submitCountMeIn(){
        SubmittedPrayerRequestCard submittedCountMeInCard = new SubmittedPrayerRequestCard(((EditText) findViewById(R.id.nameEditText)).getText().toString(),
                ((EditText) findViewById(R.id.phoneEditText)).getText().toString(),
                ((EditText) findViewById(R.id.prayerRequestEditText)).getText().toString());

        if(submittedCountMeInCard.isInputValid()){
            SendMail sendMail = new SendMail(this, "New Virtual Count Me In Card! #" + (long) (Math.random() * 1_000_000_000), submittedCountMeInCard.getHTMLEmail());
            sendMail.execute();

            HBCConnectActivity.dataFile.addSubmittedCountMeInCards(submittedCountMeInCard);
        } else
            Toast.makeText(this, "Please make sure you've filled in all the required info.", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
