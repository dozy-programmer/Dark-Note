package com.akapps.dailynote.classes.helpers;

import android.content.Context;

import com.akapps.dailynote.R;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

public class RealmDatabase {

    public static Realm setUpDatabase(Context context){
        int currentVersion = Integer.parseInt(context.getString(R.string.schema));

        Realm.init(context);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .schemaVersion(currentVersion) // Must be bumped when the schema changes
                .migration(new MyMigration()) // Migration to run instead of throwing an exception
                .build();
        Realm.setDefaultConfiguration(config);
        return Realm.getDefaultInstance();
    }

    // Example migration adding a new class
    public static class MyMigration implements RealmMigration {
        public int hashCode() {
            return MyMigration.class.hashCode();
        }

        public boolean equals(Object object) {
            if(object == null) {
                return false;
            }
            return object instanceof MyMigration;
        }

        @Override
        public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
            RealmSchema schema = realm.getSchema();

            if(oldVersion == 0){
                schema.get("User")
                        .addField("titleLines", int.class)
                        .transform(obj -> obj.setInt("titleLines", 3))
                        .addField("contentLines", int.class)
                        .transform(obj -> obj.setInt("contentLines", 3))
                        .addField("openFoldersOnStart", boolean.class)
                        .removeField("lastUpdated")
                        .removeField("backUpOnLaunch")
                        .removeField("backUpLocation")
                        .removeField("backupReminder")
                        .removeField("backReminderOccurrence")
                        .removeField("startingDate")
                        .removeField("categories")
                        .removeField("liveNoteAutoComplete");
            }
        }
    }
}
