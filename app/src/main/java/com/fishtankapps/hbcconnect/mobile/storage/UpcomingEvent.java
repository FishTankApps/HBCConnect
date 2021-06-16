package com.fishtankapps.hbcconnect.mobile.storage;

import android.graphics.Bitmap;
import android.util.Log;

import com.fishtankapps.hbcconnect.mobile.utilities.Utilities;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

public class UpcomingEvent implements Serializable, Comparable<UpcomingEvent> {
    private static final long serialVersionUID = 3802383018368142123L;

    private transient Bitmap backgroundImage = null;

    private final String backgroundImageID;

    private final String name, description, timeframe;
    private final String[] tags;
    private final long id, date;

    private transient boolean isLoadingBitMap = false;

    public UpcomingEvent(String name, String tags, String description, String timeframe, String backgroundImageID, long id, long date) {
        this.name = name;
        this.tags = tags.split(";");
        this.description = description;
        this.timeframe = timeframe;
        this.id = id;
        this.date = date;
        this.backgroundImageID = backgroundImageID;

        new Thread(() -> {
            try {
                if(backgroundImageID != null) {
                    isLoadingBitMap = true;
                    Utilities.getBackgroundImage(backgroundImageID, bitmap -> {backgroundImage = bitmap; isLoadingBitMap = false;});
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }).start();

        for(int index = 0; index < this.tags.length; index++)
            this.tags[index] = this.tags[index].replace('_', ' ');
    }

    public String getName() {
        return name;
    }

    public String[] getTags() {
        return tags;
    }

    public String getDescription() {
        return description;
    }

    public String getTimeframe() {
        return timeframe;
    }

    public Bitmap getBackgroundImage() {
        return backgroundImage;
    }

    public long getId() {
        return id;
    }

    public long getDate() {
        return date;
    }

    public String formatDate(){
        Calendar date = new GregorianCalendar();
        date.setTimeInMillis(getDate());

        return date.get(Calendar.MONTH) + "/" + date.get(Calendar.DAY_OF_MONTH) + "/" + date.get(Calendar.YEAR);
    }


    public boolean isLoadingBitMap(){
        return isLoadingBitMap;
    }


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        new Thread(() -> {
            try {
                if(backgroundImageID != null) {
                    isLoadingBitMap = true;
                    Utilities.getBackgroundImage(backgroundImageID, bitmap -> {backgroundImage = bitmap; isLoadingBitMap = false;});
                }
            } catch (Exception e){
                Log.w("Upcoming Event", "Error Reading Image! URL: " + backgroundImageID);
                e.printStackTrace();
            }
        }).start();
    }

    @NotNull
    @Override
    public String toString(){
        return "UpcomingEvent: ID:" + id + ", Name: " + name;
    }

    @Override
    public int compareTo(UpcomingEvent o) {
        return (int) (getDate() - o.getDate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpcomingEvent that = (UpcomingEvent) o;
        return id == that.id &&
                date == that.date &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(timeframe, that.timeframe) &&
                Arrays.equals(tags, that.tags);
    }
}
