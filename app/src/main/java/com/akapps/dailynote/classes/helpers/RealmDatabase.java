package com.akapps.dailynote.classes.helpers;

import android.content.Context;
import android.util.Log;

import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.data.CheckListItem;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

public class RealmDatabase {

    public static Realm setUpDatabase(Context context){
        int currentVersion = Integer.parseInt(context.getString(R.string.schema));

        Realm.init(context);

        RealmConfiguration config = new RealmConfiguration.Builder()
                    .schemaVersion(currentVersion) // Must be bumped when the schema changes
                    .migration(new MyMigration())
                    .compactOnLaunch()
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

                if(!schema.get("User").hasField("lastUpload"))
                    schema.get("User").addField("lastUpload", String.class);

                if(!schema.get("User").hasField("email"))
                    schema.get("User").addField("email", String.class);

                if(!schema.get("Note").hasField("dateCreatedMilli"))
                    schema.get("Note").addField("dateCreatedMilli", long.class);

                if(!schema.get("Note").hasField("dateEditedMilli"))
                    schema.get("Note").addField("dateEditedMilli", long.class);

                if(!schema.get("Note").hasField("sort"))
                    schema.get("Note").addField("sort", int.class);

                if(!schema.get("CheckListItem").hasField("lastCheckedDate"))
                    schema.get("CheckListItem").addField("lastCheckedDate", long.class);

                if(!schema.contains("SubCheckListItem")) {
                    schema.create("SubCheckListItem")
                            .addField("id", int.class)
                            .addField("text", String.class)
                            .addField("checked", boolean.class)
                            .addField("positionInList", int.class);
                }

                if(!schema.get("CheckListItem").hasField("subChecklist"))
                    schema.get("CheckListItem").addRealmListField("subChecklist", schema.get("SubCheckListItem"));

                if(!schema.get("CheckListItem").hasField("subListId"))
                    schema.get("CheckListItem").addField("subListId", int.class);

                if(!schema.get("User").hasField("showFolderNotes"))
                    schema.get("User").addField("showFolderNotes", boolean.class);

                if(!schema.get("User").hasField("showPreviewNoteInfo"))
                    schema.get("User").addField("showPreviewNoteInfo", boolean.class);

                if(!schema.get("User").hasField("modeSettings"))
                    schema.get("User").addField("modeSettings", boolean.class);

                if(!schema.get("User").hasField("backupReminderOccurrence"))
                    schema.get("User").addField("backupReminderOccurrence", int.class);

                if(!schema.get("Note").hasField("enableSublist"))
                    schema.get("Note").addField("enableSublist", boolean.class);

                if(!schema.get("User").hasField("enableSublists"))
                    schema.get("User").addField("enableSublists", boolean.class);

                if(!schema.get("User").hasField("backupReminderDate"))
                    schema.get("User").addField("backupReminderDate", String.class);

                schema.create("Backup")
                        .addField("userId", int.class)
                        .addField("fileSize", int.class)
                        .addField("fileName", String.class)
                        .addField("upLoadTime", String.class);
            }
            else {
                if(!schema.get("User").hasField("email"))
                    schema.get("User").addField("email", String.class);

                if(!schema.get("User").hasField("lastUpload"))
                    schema.get("User").addField("lastUpload", String.class);

                if(!schema.get("Note").hasField("dateCreatedMilli"))
                    schema.get("Note").addField("dateCreatedMilli", long.class);

                if(!schema.get("Note").hasField("dateEditedMilli"))
                    schema.get("Note").addField("dateEditedMilli", long.class);

                if(!schema.get("Note").hasField("sort"))
                    schema.get("Note").addField("sort", int.class);

                if(!schema.get("CheckListItem").hasField("lastCheckedDate"))
                    schema.get("CheckListItem").addField("lastCheckedDate", long.class);

                if(!schema.contains("SubCheckListItem")) {
                    schema.create("SubCheckListItem")
                            .addField("id", int.class)
                            .addField("text", String.class)
                            .addField("checked", boolean.class)
                            .addField("positionInList", int.class);
                }

                if(!schema.get("CheckListItem").hasField("subChecklist"))
                    schema.get("CheckListItem").addRealmListField("subChecklist", schema.get("SubCheckListItem"));

                if(!schema.get("CheckListItem").hasField("subListId"))
                    schema.get("CheckListItem").addField("subListId", int.class);

                if(!schema.get("User").hasField("showFolderNotes"))
                    schema.get("User").addField("showFolderNotes", boolean.class);

                if(!schema.get("User").hasField("showPreviewNoteInfo"))
                    schema.get("User").addField("showPreviewNoteInfo", boolean.class);

                if(!schema.get("User").hasField("modeSettings"))
                    schema.get("User").addField("modeSettings", boolean.class);

                if(!schema.get("User").hasField("backupReminderOccurrence"))
                    schema.get("User").addField("backupReminderOccurrence", int.class);

                if(!schema.get("Note").hasField("enableSublist"))
                    schema.get("Note").addField("enableSublist", boolean.class);

                if(!schema.get("User").hasField("enableSublists"))
                    schema.get("User").addField("enableSublists", boolean.class);

                if(!schema.get("User").hasField("backupReminderDate"))
                    schema.get("User").addField("backupReminderDate", String.class);

                schema.create("Backup")
                        .addField("userId", int.class)
                        .addField("fileSize", int.class)
                        .addField("fileName", String.class)
                        .addField("upLoadTime", String.class);
            }

        }
    }
}
