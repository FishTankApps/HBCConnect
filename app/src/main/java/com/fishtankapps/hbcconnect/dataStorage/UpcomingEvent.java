package com.fishtankapps.hbcconnect.dataStorage;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class UpcomingEvent implements Serializable {
    private static final long serialVersionUID = 3802383018368142123L;

    private final String name, tags, description, timeframe;
    private final long id, date;

    public UpcomingEvent(String name, String tags, String description, String timeframe, long id, long date) {
        this.name = name;
        this.tags = tags;
        this.description = description;
        this.timeframe = timeframe;
        this.id = id;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public String getTags() {
        return tags;
    }

    public String getDescription() {
        return description;
    }

    public String getTimeframe() {
        return timeframe;
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

    @NotNull
    @Override
    public String toString(){
        return "UpcomingEvent: ID:" + id + ", Name: " + name + ", Date: " + formatDate() + ", Tags: " + tags + ", Description: " + description;
    }
}
