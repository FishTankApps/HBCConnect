package com.fishtankapps.hbcconnect.mobile.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.fishtankapps.hbcconnect.R;
import com.fishtankapps.hbcconnect.mobile.utilities.Utilities;
import com.fishtankapps.hbcconnect.mobile.utilities.firebase.FirebaseDatabaseInterface;
import com.google.firebase.auth.FirebaseAuth;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class DataFile implements Serializable {

    private static final long serialVersionUID = -8054366027640391094L;

    private static final int CURRENT_DATA_FILE_VERSION = 23;
    private static final int EXPIRED_EVENT_TIME = 86_400_000; // = 1 Day in mls

    private final ArrayList<SubmittedPrayerRequestCard> submittedPrayerRequestCards;
    private final ArrayList<SubmittedCountMeInCard> submittedCountMeInCards;
    private final ArrayList<LivestreamData> previousLiveStreams;
    private final ArrayList<UpcomingEvent> dismissedEvents;
    private final ArrayList<UpcomingEvent> upcomingEvents;
    private final ArrayList<String> tags;

    private final File filesDir;
    private int dataFileVersion;



    private static DataFile dataFile;
    public static DataFile getDataFile(Context context) {
        if(dataFile == null)
            dataFile = openDataFile(context);

        return dataFile;
    }



    DataFile(Context applicationContext) {
        tags = new ArrayList<>();
        upcomingEvents = new ArrayList<>();
        dismissedEvents = new ArrayList<>();
        previousLiveStreams = new ArrayList<>();
        submittedCountMeInCards = new ArrayList<>();
        submittedPrayerRequestCards = new ArrayList<>();

        dataFileVersion = CURRENT_DATA_FILE_VERSION;

        filesDir = applicationContext.getFilesDir();
    }



    //-------{ GETTERS AND SETTERS }----------------------------------------------------------------
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

    public ArrayList<String> getTags(){
        return tags;
    }



    //---------{ SYNC HELPER METHODS }--------------------------------------------------------------
    private long getIdOfMostRecentEvent(){
        long id = -1;
        for(UpcomingEvent event : upcomingEvents)
            if(event.getId() > id)
                id = event.getId();

            return id;
    }
    public void addDismissedEvent(UpcomingEvent upcomingEvent){
        dismissedEvents.add(upcomingEvent);
    }
    private boolean isEventOnDismissedEventList(long eventId) {
        for(UpcomingEvent dismissedEvent : dismissedEvents)
            if(eventId == dismissedEvent.getId())
                return true;

        return false;
    }
    public void cleanEventList() {
        long currentTime = System.currentTimeMillis();
        Log.d("DataFile", "Cleaning Event List - Current Time: " + currentTime);
        Log.d("DataFile", "Cleaning Event List - Dismissed Event List: " + dismissedEvents.toString());
        // Clean Dismissed Event List:
        for(int index = 0; index < dismissedEvents.size(); index++) {
            UpcomingEvent event = dismissedEvents.get(index);

            if(currentTime - event.getDate() > EXPIRED_EVENT_TIME) {
                Log.d("DataFile", "Cleaning Event List - Dismissed List: \"" + event.getName() + "\" removed (old)");
                dismissedEvents.remove(index);
                index--;
            }
        }

        // Clean Upcoming Event List:
        for(int index = 0; index < upcomingEvents.size(); index++) {
            UpcomingEvent event = upcomingEvents.get(index);

            if(currentTime - event.getDate() > EXPIRED_EVENT_TIME) {
                Log.d("DataFile", "Cleaning Event List - Upcoming List: \"" + event.getName() + "\" removed (old)");
                upcomingEvents.remove(index);
                index--;
            } else if (isEventOnDismissedEventList(event.getId())) {
                Log.d("DataFile", "Cleaning Event List - Upcoming List: \"" + event.getName() + "\" removed (dismissed)");
                upcomingEvents.remove(index);
                index--;
            }
        }

        Log.d("DataFile", "Cleaning Event List - Event Lists Cleaned!");
    }



    //---------{ SYNC WITH FIREBASE DATABASE }------------------------------------------------------
    public void syncUpcomingEvents(@Nullable OnEventSyncCompleteListener listener){
        Log.d("DataFile", "Sync: Syncing - Upcoming Events Started...");
        Log.d("DataFile", "Sync: Syncing - Upcoming Events Highest ID = " + getIdOfMostRecentEvent());
        upcomingEvents.clear();
        retrieveNextUpcomingEvent(0, listener);
    }
    private void retrieveNextUpcomingEvent(long eventID, @Nullable OnEventSyncCompleteListener listener){
        Log.d("DataFile", "Sync: Syncing - Retrieving UpcomingEvent (ID = " + eventID + ")...");
        FirebaseDatabaseInterface.getFirebaseDatabaseInterface().getValue("upcoming_events/" + eventID, (rawEvent) -> {
            if(rawEvent == null) {
                Log.d("DataFile", "Sync: Syncing - Retrieved All UpcomingEvents");
                cleanEventList();
                Utilities.quickSort(upcomingEvents);

                saveDataFile();

                if(listener != null)
                    listener.eventsSynced();

                return;
            }

            @SuppressWarnings("unchecked")
            HashMap<String, Object> eventMap = (HashMap<String, Object>) rawEvent;
            Log.d("DataFile", "Sync: Syncing - Retrieved Event Map (ID = " + eventID + "): " + eventMap);


            long date =          Long.parseLong(Objects.requireNonNull(eventMap.get("date")).toString());
            String description = (String) eventMap.get("description");
            String name =        (String) eventMap.get("name");
            String tags =        (String) eventMap.get("tags");
            String timeframe =   (String) eventMap.get("timeframe");
            String backgroundImageURL =   (String) eventMap.get("background_image");

            assert tags != null;
            UpcomingEvent event = new UpcomingEvent(name, tags, description, timeframe, backgroundImageURL, eventID, date);
            Log.d("DataFile", "Sync: Syncing - Retrieved Event, UpcomingEvent Value: " + event);

            if(!upcomingEvents.contains(event)) {
                upcomingEvents.add(event);
            }

            retrieveNextUpcomingEvent(eventID + 1, listener);
        });
    }

    public void syncMiscellaneousData() {
        Log.d("DataFile", "Sync: Syncing Miscellaneous Data - Getting Tags String...");
        FirebaseDatabaseInterface.getFirebaseDatabaseInterface().getValue("tags", (rawTags) -> {
            String tagsString = rawTags.toString();
            Log.d("DataFile", "Sync: Syncing Miscellaneous Data - Raw Tags String: \"" + tagsString + '"');

            String[] tagsArray = tagsString.split(";");
            Log.d("DataFile", "Sync: Syncing Miscellaneous Data - Split Tags String: " + Arrays.toString(tagsArray));

            tags.clear();
            for(String tag : tagsArray)
                tags.add(tag.replace('_', ' '));

            Log.d("DataFile", "Sync: Syncing Miscellaneous Data - Tags ArrayList: " + tags);
        });
    }

    public void checkForNewLivestreams(Context context, OnLivestreamsReceivedListener listener) {
        ArrayList<LivestreamData> newLivestreams = new ArrayList<>();

        long highestID = -1;
        for(LivestreamData livestreamData : previousLiveStreams)
            if(livestreamData.getLivestreamDataId() > highestID)
                highestID = livestreamData.getLivestreamDataId();

        if(highestID == -1) {
            getMoreLivestreams(context, -1, listener);
        } else {
            Log.d("DataFile", "Check for New Livestreams - Highest ID = " + highestID);
            retrieveNextLivestream(highestID + 1, newLivestreams, listener);
        }
    }
    private void retrieveNextLivestream(long livestreamID, ArrayList<LivestreamData> newLivestreams, @Nullable OnLivestreamsReceivedListener listener){
        Log.d("DataFile", "Check for New Livestreams - Retrieving Livestream (ID = " + livestreamID + ")...");

        FirebaseDatabaseInterface.getFirebaseDatabaseInterface().getValue("livestreams/livestream_list/" + livestreamID, (rawLivestreamData) -> {
            if(rawLivestreamData == null) {
                Log.d("DataFile", "Check for New Livestreams - Done!");
                Utilities.quickSort(previousLiveStreams);
                saveDataFile();

                if(listener != null)
                    listener.livestreamsReceived(newLivestreams);

                return;
            }

            @SuppressWarnings("unchecked")
            HashMap<String, Object> livestreamDataMap = (HashMap<String, Object>) rawLivestreamData;
            Log.d("DataFile", "Check for New Livestreams - Retrieved Livestream Map (ID = " + livestreamID + "): " + livestreamDataMap);

            String videoId = (String) livestreamDataMap.get("livestream_id");
            String name = (String) livestreamDataMap.get("livestream_name");
            String tag = (String) livestreamDataMap.get("livestream_tag");
            String imageURL = (String) livestreamDataMap.get("background_image");

            LivestreamData newLivestream = new LivestreamData(name, tag, videoId, imageURL, livestreamID);
            Log.d("DataFile", "Check for New Livestreams - Parsed LivestreamData, LivestreamData Value (ID = " + livestreamID + "): " + newLivestream);

            Log.d("DataFile", "Check for New Livestreams - Adding Livestream From Database to List...");
            previousLiveStreams.add(newLivestream);
            newLivestreams.add(newLivestream);

            retrieveNextLivestream(livestreamID + 1, newLivestreams, listener);
        });
    }

    public void getMoreLivestreams(Context context, long lowestID, @NotNull OnLivestreamsReceivedListener listener) {
        ArrayList<LivestreamData> livestreams = new ArrayList<>();
        getMoreLivestreams(context, lowestID, listener, livestreams, lowestID);
    }
    private void getMoreLivestreams(Context context, long lowestID, @NotNull OnLivestreamsReceivedListener listener, ArrayList<LivestreamData> livestreamData, long lowestRetrievedID) {

        if(lowestID - lowestRetrievedID > context.getResources().getInteger(R.integer.load_more_livestreams_count)) {
            Log.d("DataFile", context.getResources().getInteger(R.integer.load_more_livestreams_count) + " Livestreams have been gathered. Calling listener...");
            previousLiveStreams.addAll(livestreamData);
            saveDataFile();

            listener.livestreamsReceived(livestreamData);
            return;
        }

        Log.d("DataFile", "Getting More Livestreams - Lowest ID: " + lowestID + ", Lowest Received: " + lowestRetrievedID);

        if(lowestID == -1) {
            Log.d("DataFile", "Getting More Livestreams - Lowest ID = -1, Getting Highest ID...");
            FirebaseDatabaseInterface.getFirebaseDatabaseInterface().getValue("livestreams/highest_livestream_id", (rawID) -> {
                long highestID = (long) rawID;
                Log.d("DataFile", "Getting More Livestreams - Highest ID: " + highestID);
                getMoreLivestreams(context, highestID + 1, listener, livestreamData, highestID + 1);
            });

        } else {
            LivestreamData livestream = null;
            for(LivestreamData livestreamData1 : previousLiveStreams)
                if(livestreamData1.getLivestreamDataId() == lowestRetrievedID - 1)
                    livestream = livestreamData1;

            Log.d("DataFile", "Getting More Livestreams - Checking DataFile for ID = " + (lowestRetrievedID - 1) + ": " + (livestream != null));

            if(livestream == null) {
                Log.d("DataFile", "Getting More Livestreams - Retrieving Livestream From Database (ID = " + (lowestRetrievedID - 1) + ")...");
                FirebaseDatabaseInterface.getFirebaseDatabaseInterface().getValue("livestreams/livestream_list/" + (lowestRetrievedID - 1), (rawLivestreamData) -> {
                    if(rawLivestreamData == null) {
                        getMoreLivestreams(context, lowestID, listener, livestreamData, Long.MIN_VALUE);
                        return;
                    }

                    @SuppressWarnings("unchecked")
                    HashMap<String, Object> livestreamDataMap = (HashMap<String, Object>) rawLivestreamData;
                    Log.d("DataFile", "Getting More Livestreams - Retrieved Livestream Map (ID = " + (lowestRetrievedID - 1) + "): " + livestreamDataMap);

                    String videoId = (String) livestreamDataMap.get("livestream_id");
                    String name = (String) livestreamDataMap.get("livestream_name");
                    String tag = (String) livestreamDataMap.get("livestream_tag");
                    String imageURL = (String) livestreamDataMap.get("background_image");

                    LivestreamData newLivestream = new LivestreamData(name, tag, videoId, imageURL, (lowestRetrievedID - 1));
                    Log.d("DataFile", "Getting More Livestreams - Parsed LivestreamData, LivestreamData Value (ID = " + (lowestRetrievedID - 1) + "): " + livestreamData);

                    Log.d("DataFile", "Getting More Livestreams - Adding Livestream From Database to List...");
                    livestreamData.add(newLivestream);

                    getMoreLivestreams(context, lowestID, listener, livestreamData, lowestRetrievedID - 1);
                });

            } else {
                Log.d("DataFile", "Getting More Livestreams - Adding Livestream From DataFile to List...");

                livestreamData.add(livestream);
                getMoreLivestreams(context, lowestID, listener, livestreamData, lowestRetrievedID - 1);
            }
        }
    }

    public interface OnEventSyncCompleteListener {
        void eventsSynced();
    }
    public interface OnLivestreamsReceivedListener{
        void livestreamsReceived(ArrayList<LivestreamData> livestreamData);
    }




    //---------{ STORAGE }--------------------------------------------------------------------------
    private static DataFile openDataFile(@NotNull Context applicationContext){
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

            DataFile dataFile = new DataFile(applicationContext);
            saveDataFile(dataFile, applicationContext);

            return dataFile;
        }
    }
    private static void saveDataFile(DataFile dataFile, @NotNull Context applicationContext){
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
    public void saveDataFile() {
        try{
            File dataFileFile = new File(filesDir, "DataFile.dat");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(dataFileFile));

            objectOutputStream.writeObject(this);

            objectOutputStream.close();

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void clearData(Context context) {
        Log.d("DataFile", "Clearing Data - Clearing Lists");
        upcomingEvents.clear();
        dismissedEvents.clear();
        previousLiveStreams.clear();
        submittedCountMeInCards.clear();
        submittedPrayerRequestCards.clear();


        Log.d("DataFile", "Clearing Data - Clearing Shared Preferences");
        dataFileVersion = CURRENT_DATA_FILE_VERSION;
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();

        FirebaseAuth.getInstance().signOut();

        File directory = context.getDir("image_cache", Context.MODE_PRIVATE);
        for(File imageFile : Objects.requireNonNull(directory.listFiles()))
            Log.d("DataFile", "Clearing Data - Deleting Image File " + imageFile.getName() + ", Result: " + imageFile.delete());

        Log.d("DataFile", "Clearing Data - Deleting Image Dir, Result: " + directory.delete());


        Log.d("DataFile", "Clearing Data - Saving Cleared DataFile");
        saveDataFile(this, context);


        Log.d("DataFile", "Clearing Data - Complete");
        Toast.makeText(context, "App Reset", Toast.LENGTH_SHORT).show();
    }



    //----------{ SHARED PREFERENCE TOOLS }---------------------------------------------------------
    public static int getSharedPreferenceIntValue(String key, @NotNull Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
        return sharedPref.getInt(key, 0);
    }
    public static void setSharedPreferenceIntValue(String key, int value, @NotNull Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static boolean getSharedPreferenceBooleanValue(String key, @NotNull Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
        return sharedPref.getBoolean(key, false);
    }
    public static boolean getSharedPreferenceBooleanValue(String key, boolean defValue, @NotNull Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
        return sharedPref.getBoolean(key, defValue);
    }
    public static void setSharedPreferenceBooleanValue(String key, boolean value, @NotNull Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
}
