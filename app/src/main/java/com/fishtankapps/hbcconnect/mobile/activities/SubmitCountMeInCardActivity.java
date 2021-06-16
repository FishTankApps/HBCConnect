package com.fishtankapps.hbcconnect.mobile.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fishtankapps.hbcconnect.R;
import com.fishtankapps.hbcconnect.mobile.storage.DataFile;
import com.fishtankapps.hbcconnect.mobile.storage.SubmittedCountMeInCard;
import com.fishtankapps.hbcconnect.mobile.utilities.email.SendMail;

import java.util.Objects;

public class SubmitCountMeInCardActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.submit_count_me_in_screen_layout);

        findViewById(R.id.submitCountMeIn).setOnClickListener(l->submitCountMeIn());

        try {
            setSupportActionBar(findViewById(R.id.countMeInToolbar));
            Objects.requireNonNull(getSupportActionBar()).setTitle("Count Me In");
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);

        } catch (Exception e){
            Log.w("Count Me In", "Error setting up SupportActionBar.");
        }


    }

    private void submitCountMeIn(){
        SubmittedCountMeInCard submittedCountMeInCard = new SubmittedCountMeInCard(((EditText) findViewById(R.id.activityNameEditText)).getText().toString(),
                ((EditText) findViewById(R.id.nameEditText)).getText().toString(),
                ((EditText) findViewById(R.id.phoneEditText)).getText().toString(),
                ((EditText) findViewById(R.id.numberOfAdultsEditText)).getText().toString(),
                ((EditText) findViewById(R.id.numberOfChildrenEditText)).getText().toString(),
                ((EditText) findViewById(R.id.prayerRequestEditText)).getText().toString());

        if(submittedCountMeInCard.isInputValid()){
            SendMail sendMail = new SendMail(() -> this, "New Virtual Count Me In Card! #" + (long) (Math.random() * 1_000_000_000), submittedCountMeInCard.getHTMLEmail(), "Count Me In", getResources().getString(R.string.hbc_admin_email));
            sendMail.execute();

            DataFile.getDataFile(getApplicationContext()).addSubmittedCountMeInCards(submittedCountMeInCard);
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
