package com.akapps.dailynote.classes.helpers;

import android.content.Context;
import android.util.Log;

import io.realm.Realm;

public class RealmSingleton {
    private static Realm realmInstance;
    private static boolean closeRealm;

    private RealmSingleton() {
        // Private constructor to prevent instantiation
    }

    public static Realm getInstance(Context context) {
        if (realmInstance == null || realmInstance.isClosed()) {
            // initialize database and get data
            try {
                realmInstance = Realm.getDefaultInstance();
            } catch (Exception e) {
                realmInstance = RealmDatabase.setUpDatabase(context);
            }
        }
        return realmInstance;
    }

    public static void setCloseRealm(boolean newValue){
        closeRealm = newValue;
    }

    public static void closeRealmInstance(String location) {
        if (realmInstance != null && closeRealm) {
            realmInstance.close();
            realmInstance = null;
            Log.d("Here", "realm instance closed at " + location);
        }

        setCloseRealm(true);
    }
}
