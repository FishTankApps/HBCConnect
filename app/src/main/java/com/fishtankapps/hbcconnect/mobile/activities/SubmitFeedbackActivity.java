package com.fishtankapps.hbcconnect.mobile.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.fishtankapps.hbcconnect.R;
import com.fishtankapps.hbcconnect.mobile.utilities.email.SendMail;

import java.util.Objects;

public class SubmitFeedbackActivity extends AppCompatActivity {

    private EditText focusEditText, feedbackEditText;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.submit_feedback_layout);

        findViewById(R.id.submitFeedback).setOnClickListener(l-> submitFeedback());

        focusEditText = findViewById(R.id.focusEditText);
        feedbackEditText = findViewById(R.id.feedbackEditText);


        if(getIntent().getExtras() != null){
            focusEditText.setText(getIntent().getExtras().getString(getString(R.string.feedback_focus)));
            feedbackEditText.setText(getIntent().getExtras().getString(getString(R.string.feedback_template)));
        }

        try {
            setSupportActionBar(findViewById(R.id.submitFeedbackToolbar));
            Objects.requireNonNull(getSupportActionBar()).setTitle("Summit Feedback");
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);

        } catch (Exception e){
            Log.w("Submit Feedback", "Error setting up SupportActionBar.");
        }
    }

    private void submitFeedback(){
        String focus = focusEditText.getEditableText().toString();
        String feedback = feedbackEditText.getEditableText().toString();

        String message = "<div dir=3D\"ltr\"><font size=3D\"4\"><b>New HBC Connect Feedback:</b></fon" +
                "t><div><font size=3D\"4\"><b><br></b></font><div><b>Focus: </b>" + focus +
                "</div><div><b>Feedback:</b> " +
                feedback;

        SendMail sendMail = new SendMail(() -> this, "New Feedback! #" + (long) (Math.random() * 1_000_000_000), message, "Feedback", getResources().getString(R.string.fishtankapps_email));
        sendMail.execute();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_the_left, R.anim.exit_to_the_right);
    }
}
