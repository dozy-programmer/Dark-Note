package com.akapps.dailynote.classes.helpers;

import android.content.Context;
import android.util.Log;

import com.akapps.dailynote.classes.data.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmMigrationNeededException;

public class RealmSingleton {

    private static final String TAG = "RealmSingleton";

    private static Realm realmInstance;
    private static boolean closeRealm = true;
    private static User cachedUser;

    private RealmSingleton() {
    }

    // Main accessor (auto re-init if closed)
    public static Realm get(Context context) {
        if (realmInstance == null || realmInstance.isClosed()) {
            realmInstance = initializeRealm(context);
        }
        return realmInstance;
    }

    // Direct accessor (NO auto re-init)
    public static Realm getOnlyRealm() {
        return realmInstance;
    }

    private static Realm initializeRealm(Context context) {
        try {
            RealmConfiguration config = Realm.getDefaultConfiguration();
            return (config != null)
                    ? Realm.getInstance(config)
                    : RealmDatabase.setUpDatabase(context);

        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("less than last set version")) {
                Log.w(TAG, "Schema mismatch detected — attempting recovery", e);
                return recoverDatabase(context, e, 0);
            }
            throw e;

        } catch (RealmMigrationNeededException e) {
            Log.w(TAG, "Migration required — attempting recovery", e);
            return recoverDatabase(context, e, 0);

        } catch (Exception e) {
            Log.e(TAG, "Fatal Realm init error", e);
            throw new RuntimeException("Database failed to open. Possibly corrupted.", e);
        }
    }

    private static Realm recoverDatabase(Context context, Exception error, Integer attempts) {
        try {
            long storedVersion = getSavedSchemaVersion(context);

            RealmConfiguration config = Realm.getDefaultConfiguration();
            long configVersion = (config != null) ? config.getSchemaVersion() : 0;

            long extractedVersion = extractHighestVersion(error.getMessage());
            long newVersion = Math.max(Math.max(extractedVersion, storedVersion), configVersion) + 1;

            Helper.savePreference(context, String.valueOf(newVersion), AppConstants.SCHEMA_VERSION);

            Realm.setDefaultConfiguration(
                    new RealmConfiguration.Builder()
                            .schemaVersion(newVersion)
                            .build()
            );

            Log.i(TAG, "Realm recovered — schema updated to version: " + newVersion);
            return RealmDatabase.setUpDatabase(context);

        } catch (Exception e) {
            if (attempts == 0) {
                recoverDatabase(context, error, 1);
            } else {
                Log.e(TAG, "Recovery failed", e);
                throw new RuntimeException("Could not recover Realm DB.", e);
            }
        }
    }

    private static long extractHighestVersion(String msg) {
        if (msg == null) return 0;

        long highest = 0;

        Matcher matcher = Pattern.compile("\\d+").matcher(msg);
        while (matcher.find()) {
            long num = Long.parseLong(matcher.group());
            if (num > highest) highest = num;
        }

        return highest;
    }

    private static long getSavedSchemaVersion(Context context) {
        RealmConfiguration config = Realm.getDefaultConfiguration();
        if (config != null) return config.getSchemaVersion();

        return Helper.getIntPreference(context, AppConstants.SCHEMA_VERSION);
    }

    // --------------------------
    // USER DATA CACHE
    // --------------------------

    public static User getUser(Context context) {
        if (cachedUser == null || !cachedUser.isValid()) {
            cachedUser = RealmHelper.getUser(context, "");
        }
        return cachedUser;
    }

    public static void updateUserCache(User updatedUser) {
        cachedUser = updatedUser;
    }

    // --------------------------
    // REALM LIFECYCLE
    // --------------------------

    public static void setCloseRealm(boolean shouldClose) {
        closeRealm = shouldClose;
    }

    public static void closeRealmInstance(String tag) {
        if (realmInstance != null && !realmInstance.isClosed() && closeRealm) {
            realmInstance.close();
            Log.d(TAG, "Realm CLOSED @" + tag);
        }
        realmInstance = null;
        closeRealm = true;
    }
}

