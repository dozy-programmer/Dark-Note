package com.akapps.dailynote.classes.helpers;

import android.content.Context;
import android.util.Log;

import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.data.User;

import io.realm.Realm;
import io.realm.exceptions.RealmMigrationNeededException;

public class RealmSingleton {
    private static Realm realmInstance;
    private static boolean closeRealm;
    private static User user;

    private RealmSingleton() {
        // Private constructor to prevent instantiation
    }

    public static Realm getOnlyRealm() {
        return realmInstance;
    }

    public static Realm get(Context context) {
        return getInstance(context);
    }

    public static Realm getInstance(Context context) {
        if (realmInstance == null || realmInstance.isClosed()) {
            // initialize database and get data
            try {
                realmInstance = Realm.getDefaultInstance();
            } catch (Exception e) {
                try {
                    realmInstance = RealmDatabase.setUpDatabase(context);
                } catch (Exception exception){
                    int adjustableVersion = Helper.getIntPreference(context, AppConstants.SCHEMA_VERSION);
                    int currentVersion = Integer.parseInt(context.getString(R.string.schema));
                    int finalVersion = Math.max(adjustableVersion, currentVersion);
                    Helper.savePreference(context, String.valueOf(finalVersion + 1), AppConstants.SCHEMA_VERSION);
                    realmInstance = getInstance(context);
                }
            }
        }
        return realmInstance;
    }

    public static User getUser(Context context) {
        if (user == null || !user.isValid()) RealmHelper.getUser(context, "");
        return user;
    }

    public static void updateUser(User updatedUser) {
        user = updatedUser;
    }

    public static void setCloseRealm(boolean newValue) {
        closeRealm = newValue;
    }

    public static void closeRealmInstance(String location) {
        if (realmInstance == null || realmInstance.isClosed()) {
        } else if (closeRealm) {
            realmInstance.close();
            realmInstance = null;
        }
        if (closeRealm)
            Log.d("Here", "realm instance CLOSED at " + location);

        setCloseRealm(true);
    }

}
