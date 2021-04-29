package com.fishtankapps.hbcconnect.dataStorage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.fishtankapps.hbcconnect.activity.HBCConnectActivity;
import com.fishtankapps.hbcconnect.utilities.Constants;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class DataFile implements Serializable {

    private static final long serialVersionUID = -8054366027640391094L;

    private static final int CURRENT_DATA_FILE_VERSION = 9;

    private final ArrayList<UpcomingEvent> upcomingEvents;
    private final ArrayList<LivestreamData> previousLiveStreams;
    private final ArrayList<SubmittedCountMeInCard> submittedCountMeInCards;
    private final ArrayList<SubmittedPrayerRequestCard> submittedPrayerRequestCards;

    private final int dataFileVersion;


    DataFile() {
        upcomingEvents = new ArrayList<>();
        previousLiveStreams = new ArrayList<>();
        submittedCountMeInCards = new ArrayList<>();
        submittedPrayerRequestCards = new ArrayList<>();

        dataFileVersion = CURRENT_DATA_FILE_VERSION;
    }

    public ArrayList<LivestreamData> getPreviousLiveStreams() {
        return previousLiveStreams;
    }

    public ArrayList<SubmittedCountMeInCard> getSubmittedCountMeInCards() {
        return submittedCountMeInCards;
    }
    public void addSubmittedCountMeInCards(SubmittedCountMeInCard submittedCountMeInCard) {
        submittedCountMeInCards.add(submittedCountMeInCard);
    }

    public ArrayList<SubmittedPrayerRequestCard> getSubmittedPrayerRequestCards() {
        return submittedPrayerRequestCards;
    }
    public void addSubmittedCountMeInCards(SubmittedPrayerRequestCard submittedPrayerRequestCard) {
        submittedPrayerRequestCards.add(submittedPrayerRequestCard);
    }

    public ArrayList<UpcomingEvent> getUpcomingEvents(){
        return upcomingEvents;
    }
    public void addUpcomingEvents(UpcomingEvent upcomingEvent){
        upcomingEvents.add(upcomingEvent);
    }
    public long getIdOfMostRecentEvent(){
        long id = -1;
        for(UpcomingEvent event : upcomingEvents)
            if(event.getId() > id)
                id = event.getId();

            return id;
    }

    public void syncWithDatabase(@Nullable OnSyncCompleteListener listener) {
        Log.d("DataFile", "Sync: Syncing Data...");
        syncPreviousLivestreams(listener);
        syncUpcomingEvents(listener);
    }
    public void syncPreviousLivestreams(@Nullable OnSyncCompleteListener listener){
        Log.d("DataFile", "Sync: Syncing - Previous Livestreams Started...");
        HBCConnectActivity.databaseInterface.getValue("livestreams/livestream_list", (value) -> {

            Log.d("DataFile", "Sync: Syncing - Previous Livestreams Value Received");

            if(value == null) {
                Log.d("DataFile", "Sync: Syncing - Previous Livestreams Value Null!");
                if(listener != null)
                    listener.doneSyncingLivestream();
                return;
            }

            List<?> pastLivestreams;
            if (value instanceof Collection) {
                pastLivestreams = new ArrayList<>((Collection<?>) value);
            } else {
                Log.e("DataFile", "Sync: Error - livestreams/livestream_list is not a list!");
                return;
            }

            Log.d("DataFile", "Sync: Syncing - Previous Livestreams Adding Data");
            previousLiveStreams.clear();
            for(Object livestreamDataListObject : pastLivestreams){

                List<Object> livestreamDataList;
                if (livestreamDataListObject instanceof Collection) {
                    livestreamDataList = new ArrayList<>((Collection<?>) livestreamDataListObject);

                    LivestreamData livestreamData = new LivestreamData(livestreamDataList.get(1).toString(),
                            livestreamDataList.get(2).toString(), livestreamDataList.get(0).toString());

                    previousLiveStreams.add(livestreamData);
                } else {
                    Log.e("DataFile", "Sync: Error - livestreams/livestream_list/object is not a List!");
                    return;
                }
            }

            Log.d("DataFile", "Sync: Syncing - Previous Livestreams Done!");
            if(listener != null)
                listener.doneSyncingLivestream();
        });
    }
    public void syncUpcomingEvents(@Nullable OnSyncCompleteListener listener){
        HBCConnectActivity.databaseInterface.getValue("events/upcoming_events/highest_id", (value) -> {
            long highestID = (long) value;
            long highestSyncedID = getIdOfMostRecentEvent();
            Log.d("DataFile", "Sync: Syncing - Upcoming Events Highest ID: " + value + ", Personal High: " + highestSyncedID);

            if(highestSyncedID >= highestID && listener != null)
                listener.doneSyncingUpcomingEvents();

            while(highestSyncedID < highestID){
                Log.d("DataFile", "Sync: Syncing - Retrieving Event with ID of " + (highestSyncedID + 1));

                HBCConnectActivity.databaseInterface.getValue("events/upcoming_events/" + (highestSyncedID + 1), (rawEvent, bundle) -> {
                    try {
                        long id = (long) bundle[0];
                        Log.d("DataFile", "Sync: Syncing - Retrieved Event #" + id + " , Raw Value: " + rawEvent);
                        HashMap<String, Object> upcomingEvent = (HashMap<String, Object>) rawEvent;
                        long date = (long) upcomingEvent.get("date");
                        String description = (String) upcomingEvent.get("description");
                        String name = (String) upcomingEvent.get("name");
                        String tags = (String) upcomingEvent.get("tags");
                        String timeframe = (String) upcomingEvent.get("timeframe");

                        UpcomingEvent event = new UpcomingEvent(name, tags, description, timeframe, id, date);
                        Log.d("DataFile", "Sync: Syncing - Retrieved Event, UpcomingEvent Value: " + event);
                        addUpcomingEvents(event);
                        Log.d("DataFile", "Sync: Syncing - Event #" + id + " Added!");

                        if(listener != null)
                            listener.doneSyncingUpcomingEvents();

                    } catch (NullPointerException e){
                        e.printStackTrace();
                    }
                }, new Object[]{highestSyncedID + 1});
                highestSyncedID++;
            }
        });
    }

    public interface OnSyncCompleteListener{
        void doneSyncingLivestream();
        void doneSyncingUpcomingEvents();
    }


    public static String getSharedPreferenceStringValue(String key, @NotNull Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        return sharedPref.getString(key, null);
    }
    public static void setSharedPreferenceStringValue(String key, String value, @NotNull Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static int getSharedPreferenceIntValue(String key, @NotNull Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        return sharedPref.getInt(key, 0);
    }
    public static void setSharedPreferenceIntValue(String key, int value, @NotNull Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static boolean getSharedPreferenceBooleanValue(String key, @NotNull Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(key, false);
    }
    public static void setSharedPreferenceBooleanValue(String key, boolean value, @NotNull Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static DataFile openDataFile(@NotNull Context applicationContext){
        try{
            File dataFileFile = new File(applicationContext.getFilesDir(), "DataFile.dat");

            Log.v("DataFile", "DataFile Location: " + dataFileFile.getAbsolutePath());

            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(dataFileFile));

            DataFile dataFile = (DataFile) objectInputStream.readObject();

            if(dataFile.dataFileVersion != CURRENT_DATA_FILE_VERSION)
                throw new Exception("Old DataFile!");

            objectInputStream.close();

            return dataFile;
        } catch (Exception e){

            if(!Objects.equals(e.getMessage(), "Old DataFile!"))
                e.printStackTrace();
            else
                Log.w("DataFile", "Out of date DataFile, Creating new one...");

            DataFile dataFile = new DataFile();
            saveDataFile(dataFile, applicationContext);

            return dataFile;
        }
    }
    public static void saveDataFile(DataFile dataFile, @NotNull Context applicationContext){
        try{
            File dataFileFile = new File(applicationContext.getFilesDir(), "DataFile.dat");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(dataFileFile));

            objectOutputStream.writeObject(dataFile);

            objectOutputStream.close();

        } catch (IOException e){
            e.printStackTrace();
            Toast.makeText(applicationContext, "Error:\n" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
