package com.fishtankapps.hbcconnect.mobile.storage;

import android.graphics.Bitmap;
import android.util.Log;

import com.fishtankapps.hbcconnect.mobile.utilities.Utilities;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;

public class LivestreamData implements Serializable, Comparable<LivestreamData> {

	private static final long serialVersionUID = 932244568973618159L;
	private final String livestreamName;
	private final String livestreamTag;
	private final String livestreamVideoID;
	private final String backgroundImageID;
	private transient Bitmap backgroundImage;
	private final long livestreamDataId;

	private transient boolean isLoadingBitMap = false;
	
	public LivestreamData(String livestreamName, String livestreamTag, String livestreamVideoID, String backgroundImageID, long livestreamDataId) {
		this.livestreamName = livestreamName;
		this.livestreamTag = livestreamTag;
		this.livestreamVideoID = livestreamVideoID;
		this.livestreamDataId = livestreamDataId;
		this.backgroundImageID = backgroundImageID;

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

	public String getLivestreamName() {
		return livestreamName;
	}

	public String getLivestreamTag() {
		return livestreamTag;
	}

	public String getLivestreamVideoID() {
		return livestreamVideoID;
	}

	public long getLivestreamDataId(){
		return livestreamDataId;
	}

	public Bitmap getBackgroundImage() {
		return backgroundImage;
	}

	public boolean isLoadingBitMap() {
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
	public String toString() {
		return livestreamName + ";" + livestreamTag + ";" + livestreamVideoID;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		LivestreamData that = (LivestreamData) o;

		return Objects.equals(livestreamName, that.livestreamName) &&
				Objects.equals(livestreamTag, that.livestreamTag) &&
				Objects.equals(livestreamVideoID, that.livestreamVideoID) &&
				Objects.equals(livestreamDataId, that.livestreamDataId);
	}

	@Override
	public int compareTo(LivestreamData o) {
		return (int) (getLivestreamDataId() - o.getLivestreamDataId());
	}
}
