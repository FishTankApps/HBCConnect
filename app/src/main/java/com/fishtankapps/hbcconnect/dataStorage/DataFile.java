package com.fishtankapps.hbcconnect.dataStorage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.fishtankapps.hbcconnect.server.HBCConnectServer;
import com.fishtankapps.hbcconnect.utilities.Constants;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class DataFile implements Serializable {

    private static final long serialVersionUID = -8054366027640391094L;

    private static final int CURRENT_DATA_FILE_VERSION = 4;

    private ArrayList<LivestreamData> previousLiveStreams;
    private ArrayList<SubmittedCountMeInCard> submittedCountMeInCards;
    private final int dataFileVersion;


    DataFile() {
        previousLiveStreams = new ArrayList<>();
        submittedCountMeInCards = new ArrayList<>();

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


    public void syncDataWithServer(HBCConnectServer server) {
        if(server == null)
            return;

        server.sendObject("request.livestreams");

        Object rawMessage = server.waitForMessage(5000);
        if(rawMessage != null){
            String message = (String) rawMessage;
            Log.d("Livestream Data", "Message: " + message);

            String[] livestreamDataPieces = message.split("\\|");

            for(String livestreamDataPiece : livestreamDataPieces){
                Log.d("Livestream Data", "LivestreamDataPiece: " + livestreamDataPiece);

                LivestreamData data = LivestreamData.getLivestreamDataFromMessage(livestreamDataPiece);
                if(!this.previousLiveStreams.contains(data))
                    this.previousLiveStreams.add(data);
            }
        }
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

            Log.i("DataFile", "DataFile Location: " + dataFileFile.getAbsolutePath());

            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(dataFileFile));

            DataFile dataFile = (DataFile) objectInputStream.readObject();

            if(dataFile.dataFileVersion != CURRENT_DATA_FILE_VERSION)
                throw new Exception("Old DataFile!");

            if(dataFile.previousLiveStreams == null)
                dataFile.previousLiveStreams = new ArrayList<>();

            if(dataFile.submittedCountMeInCards == null)
                dataFile.submittedCountMeInCards = new ArrayList<>();

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
