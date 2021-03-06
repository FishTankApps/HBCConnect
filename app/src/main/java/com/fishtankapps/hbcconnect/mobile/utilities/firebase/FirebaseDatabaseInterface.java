package com.fishtankapps.hbcconnect.mobile.utilities.firebase;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class FirebaseDatabaseInterface {

	private final FirebaseDatabase database;

	private static FirebaseDatabaseInterface firebaseDatabaseInterface = null;

	public static FirebaseDatabaseInterface getFirebaseDatabaseInterface(){
		if(firebaseDatabaseInterface == null)
			firebaseDatabaseInterface = new FirebaseDatabaseInterface();

		return firebaseDatabaseInterface;
	}


	private FirebaseDatabaseInterface() {
		database = FirebaseDatabase.getInstance();
	}

	public void getValue(String key, OnValueRetrievedListener listener) {
		database.getReference(key).addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(@NotNull DataSnapshot snapshot) {
					Log.v("DatabaseInterface", "getValue(): DataChanged");
					listener.valueRetrieved(snapshot.getValue());
				}

				public void onCancelled(@NotNull DatabaseError arg0) {
					Log.e("DatabaseInterface", "getValue(): Canceled: " + arg0.toException());
					listener.valueRetrieved(null);
				}
			});
	}


	public void setValue(String key, Object value) {
		database.getReference(key).setValue(value);
	}

	public void goOffline(){
		database.goOffline();
		Log.i("Database", "Going Offline...");
	}
	public void goOnline(){
		database.goOnline();
		Log.i("Database", "Going Online...");
	}

	public interface OnValueRetrievedListener {
		void valueRetrieved(Object value);
	}
}
