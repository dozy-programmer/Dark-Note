package com.akapps.dailynote.classes.helpers;

import android.content.Context;
import android.util.Log;

import com.akapps.dailynote.R;

import java.util.Date;

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
                    .schemaVersion(currentVersion)
                    .migration(new MyMigration())
                    .compactOnLaunch()
                    .allowWritesOnUiThread(true)
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

            // added User fields
            if(!schema.get("User").hasField("lastUpload"))
                schema.get("User").addField("lastUpload", String.class);
            if(!schema.get("User").hasField("email"))
                schema.get("User").addField("email", String.class);
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

            // added note fields
            if(!schema.get("Note").hasField("dateCreatedMilli"))
                schema.get("Note").addField("dateCreatedMilli", long.class);
            if(!schema.get("Note").hasField("dateEditedMilli"))
                schema.get("Note").addField("dateEditedMilli", long.class);
            if(!schema.get("Note").hasField("sort"))
                schema.get("Note").addField("sort", int.class);
            if(!schema.get("Note").hasField("widgetId"))
                schema.get("Note").addField("widgetId", int.class);

            // added checklist item fields
            if(!schema.get("CheckListItem").hasField("lastCheckedDate"))
                schema.get("CheckListItem").addField("lastCheckedDate", long.class);
            if(!schema.get("CheckListItem").hasField("subChecklist"))
                schema.get("CheckListItem").addRealmListField("subChecklist", schema.get("SubCheckListItem"));
            if(!schema.get("CheckListItem").hasField("subListId"))
                schema.get("CheckListItem").addField("subListId", int.class);
            if(!schema.get("CheckListItem").hasField("itemImage"))
                schema.get("CheckListItem").addField("itemImage", String.class);
            if(!schema.get("CheckListItem").hasField("dateCreated"))
                schema.get("CheckListItem").addField("dateCreated", String.class);

            // added sub-lists class
            if(!schema.contains("SubCheckListItem"))
                schema.create("SubCheckListItem")
                        .addField("id", int.class)
                        .addField("text", String.class)
                        .addField("checked", boolean.class)
                        .addField("positionInList", int.class);

            if(!schema.get("SubCheckListItem").hasField("dateCreated"))
                schema.get("SubCheckListItem").addField("dateCreated", String.class);

            // added backup class
            if(!schema.contains("Backup"))
                schema.create("Backup")
                        .addField("userId", int.class)
                        .addField("fileSize", int.class)
                        .addField("fileName", String.class)
                        .addField("upLoadTime", String.class)
                        .removeField("upLoadTime")
                        .addField("upLoadTime", Date.class);

            if(!schema.get("Backup").hasField("upLoadTime"))
                schema.get("Backup").removeField("upLoadTime").addField("upLoadTime", Date.class);
        }
    }
}
