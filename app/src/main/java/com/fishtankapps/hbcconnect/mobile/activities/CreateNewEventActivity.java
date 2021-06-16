package com.fishtankapps.hbcconnect.mobile.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.fishtankapps.hbcconnect.R;
import com.fishtankapps.hbcconnect.mobile.storage.DataFile;
import com.fishtankapps.hbcconnect.mobile.storage.UpcomingEvent;
import com.fishtankapps.hbcconnect.mobile.utilities.Utilities;
import com.fishtankapps.hbcconnect.mobile.utilities.firebase.FirebaseDatabaseInterface;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreateNewEventActivity extends AppCompatActivity {

    private int month, day, year;
    private int startTimeHour, startTimeMinute, endTimeHour, endTimeMinute;
    private long date = -1;

    private String startTime, endTime;

    private ArrayList<String> checkedTags;

    //TODO: Add Tags, Add Image

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkedTags = new ArrayList<>();

        Calendar currentDate = Calendar.getInstance();
        year = currentDate.get(Calendar.YEAR);
        month = currentDate.get(Calendar.MONTH);
        day = currentDate.get(Calendar.DAY_OF_MONTH);

        startTimeHour = currentDate.get(Calendar.HOUR_OF_DAY);
        startTimeMinute = currentDate.get(Calendar.MINUTE);

        endTimeHour = currentDate.get(Calendar.HOUR_OF_DAY);
        endTimeMinute = currentDate.get(Calendar.MINUTE);


        setContentView(R.layout.create_event_layout);

        EditText dateEditText = findViewById(R.id.eventDate);
        dateEditText.setOnClickListener(this::setDate);

        EditText startTimeEditText = findViewById(R.id.startTimeEditText);
        startTimeEditText.setOnClickListener(this::setStartTime);

        EditText endTimeEditText = findViewById(R.id.endTimeEditText);
        endTimeEditText.setOnClickListener(this::setEndTime);

        EditText imageURLEditText = findViewById(R.id.imageURLEditText);
        imageURLEditText.addTextChangedListener(new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateImagePreview();
            }
        });

        try {
            setSupportActionBar(findViewById(R.id.countMeInToolbar));
            Objects.requireNonNull(getSupportActionBar()).setTitle("Create New Event");
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);

        } catch (Exception e){
            Log.w("Count Me In", "Error setting up SupportActionBar.");
        }

        initTagCheckBoxes();

        findViewById(R.id.submitNewEvent).setOnClickListener(this::createNewEvent);

    }

    private void updateImagePreview() {
        new Thread(() -> {
            String url = ((EditText) findViewById(R.id.imageURLEditText)).getText().toString();

            Log.d("URL", url);

            ImageView imageView = findViewById(R.id.imagePreview);

            Bitmap bitmap = Utilities.getImageFromURL(url);

            if(bitmap != null)
                runOnUiThread(() -> imageView.setImageBitmap(bitmap));
            else
                runOnUiThread(() -> imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_no_image_selected, getTheme())));

        }).start();
    }

    private void initTagCheckBoxes() {
        ArrayList<String> tags = DataFile.getDataFile(getApplicationContext()).getTags();
        for(UpcomingEvent event : DataFile.getDataFile(getApplicationContext()).getUpcomingEvents())
            for(String tag : event.getTags()) {
                if(!tags.contains(tag))
                    tags.add(tag);
            }

        Utilities.quickSort(tags);

        LinearLayout eventTagRightLayout = findViewById(R.id.eventTagRightLayout);
        LinearLayout eventTagLeftLayout  = findViewById(R.id.eventTagLeftLayout);

        eventTagRightLayout.removeAllViews();
        eventTagLeftLayout.removeAllViews();

        LinearLayout.LayoutParams checkboxParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        int checkboxCount = 0;

        for(String tag : tags) {

            CheckBox checkBox = new CheckBox(this);
            checkBox.setTextSize(16);
            checkBox.setText(tag);
            checkBox.setLayoutParams(checkboxParams);
            checkBox.setChecked(false);
            checkBox.setOnCheckedChangeListener((checkbox, state) -> {
                checkedTags.remove(tag);

                if(state)
                    checkedTags.add(tag);
            });

            if(checkboxCount <= tags.size() / 2)
                eventTagLeftLayout.addView(checkBox);
            else
                eventTagRightLayout.addView(checkBox);

            checkboxCount++;
        }
    }

    @SuppressLint("SetTextI18n")
    private void setDate(View editText) {
        DatePickerDialog mDatePicker;

        mDatePicker = new DatePickerDialog(this, (view, year, month, day) -> {
            month = month + 1;

            this.day = day;
            this.month = month;
            this.year = year;
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, day);
            date = selectedDate.getTimeInMillis();

            ((EditText) editText).setText("" + month + "/" + day + "/" + year);
        }, year, month, day);

        mDatePicker.show();
    }

    private void setStartTime(View editText) {

        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(this, (timePicker, selectedHour, selectedMinute) -> {
            startTimeHour = selectedHour;
            startTimeMinute = selectedMinute;

            startTime = ((startTimeHour == 0) ? "12" : ((startTimeHour - 1) % 12) + 1) + ":" + ((startTimeMinute < 9) ? "0" : "") + startTimeMinute +
                    ((startTimeHour < 12) ? "am" : "pm");

            ((EditText) editText).setText(startTime);
        }, startTimeHour, startTimeMinute, false);

        mTimePicker.show();
    }

    private void setEndTime(View editText) {

        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(this, (timePicker, selectedHour, selectedMinute) -> {
            endTimeHour = selectedHour;
            endTimeMinute = selectedMinute;

            endTime = ((endTimeHour == 0) ? "12" : ((endTimeHour - 1) % 12) + 1) + ":" + ((endTimeMinute < 9) ? "0" : "") + endTimeMinute +
                    ((endTimeHour < 12) ? "am" : "pm");

            ((EditText) editText).setText(endTime);
        }, endTimeHour, endTimeMinute, false);

        mTimePicker.show();
    }


    private void createNewEvent(View view) {
        if(((EditText) findViewById(R.id.eventNameEditText)).getText().toString().equals("") ||
                ((EditText) findViewById(R.id.eventDescriptionEditText)).getText().toString().equals("") ||
                date == -1 || startTime == null || endTime == null || checkedTags.size() == 0) {
            Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = ProgressDialog.show(this,"Creating New Event","Please wait...",false,false);

        FirebaseDatabaseInterface.getFirebaseDatabaseInterface().goOnline();
        Utilities.createImageLink(((EditText) findViewById(R.id.imageURLEditText)).getText().toString(), (imageID) -> {

            Log.d("CreateNewEvent", "Creating map...");
            Map<String, String> eventMap = new HashMap<>();

            eventMap.put("name",        ((EditText) findViewById(R.id.eventNameEditText)).getText().toString());
            eventMap.put("description", ((EditText) findViewById(R.id.eventDescriptionEditText)).getText().toString());
            eventMap.put("date",        date + "");
            eventMap.put("timeframe",  startTime + " - " + endTime);

            if(imageID != -1)
                eventMap.put("background_image",  imageID + "");

            StringBuilder tags = new StringBuilder();

            for(String tag : checkedTags)
                tags.append(tag).append(";");

            Log.d("CreateNewEvent", "Tags: " + tags.toString());

            eventMap.put("tags",  tags.toString());

            Log.d("CreateNewEvent", "Map = " + eventMap);

            Log.d("CreateNewEvent", "Getting ID...");

            FirebaseDatabaseInterface.getFirebaseDatabaseInterface().getValue("upcoming_events/highest_event_id", (rawID) -> {
                long id = (long) rawID + 1;

                FirebaseDatabaseInterface.getFirebaseDatabaseInterface().setValue("upcoming_events/" + id, eventMap);
                FirebaseDatabaseInterface.getFirebaseDatabaseInterface().setValue("upcoming_events/highest_event_id", id);

                progressDialog.dismiss();
                finish();
            });
        });
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
