package com.fishtankapps.hbcconnect.dataStorage;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

public class LivestreamData implements Serializable {

	private static final long serialVersionUID = 932244568973618159L;
	private final String livestreamName;
	private final String livestreamClassification;
	private final String livestreamID;
	
	public LivestreamData(String livestreamName, String livestreamClassification, String livestreamID) {
		this.livestreamName = livestreamName;
		this.livestreamClassification = livestreamClassification;
		this.livestreamID = livestreamID;
	}

	public String getLivestreamName() {
		return livestreamName;
	}

	public String getLivestreamClassification() {
		return livestreamClassification;
	}

	public String getLivestreamID() {
		return livestreamID;
	}

	@NotNull
	public String toString() {
		return livestreamName + ";" + livestreamClassification + ";" + livestreamID;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		LivestreamData that = (LivestreamData) o;

		return Objects.equals(livestreamName, that.livestreamName) &&
				Objects.equals(livestreamClassification, that.livestreamClassification) &&
				Objects.equals(livestreamID, that.livestreamID);
	}

	public static LivestreamData getLivestreamDataFromMessage(String message){
		String[] split = message.split(";");

		return new LivestreamData(split[0], split[1], split[2]);

	}

}
