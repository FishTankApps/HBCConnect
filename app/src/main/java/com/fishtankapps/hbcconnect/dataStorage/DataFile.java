package com.fishtankapps.hbcconnect.dataStorage;

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
import java.util.List;
import java.util.Objects;

public class DataFile implements Serializable {

    private static final long serialVersionUID = -8054366027640391094L;

    private static final int CURRENT_DATA_FILE_VERSION = 7;

    private final ArrayList<LivestreamData> previousLiveStreams;
    private final ArrayList<SubmittedCountMeInCard> submittedCountMeInCards;
    private final ArrayList<SubmittedPrayerRequestCard> submittedPrayerRequestCards;

    private final int dataFileVersion;


    DataFile() {
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

    public void syncWithDatabase(@Nullable OnSyncCompleteListener listener) {
        Log.d("DataFile", "Sync: Syncing Data...");
        syncPreviousLivestreams(listener);
    }

    private void syncPreviousLivestreams(@Nullable OnSyncCompleteListener listener){
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

    public interface OnSyncCompleteListener{
        void doneSyncingLivestream();
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
