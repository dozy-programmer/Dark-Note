package com.akapps.dailynote.classes.helpers;

import android.content.Context;

import com.akapps.dailynote.R;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

public class RealmDatabase {

    public static Realm setUpDatabase(Context context) {
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
            if (object == null) {
                return false;
            }
            return object instanceof MyMigration;
        }

        @Override
        public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
            RealmSchema schema = realm.getSchema();

            if (oldVersion == 0) {
                schema.get("User")
                        .addField("titleLines", int.class)
                        .addField("contentLines", int.class)
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
            if (!schema.get("User").hasField("lastUpload"))
                schema.get("User").addField("lastUpload", String.class);
            if (!schema.get("User").hasField("email"))
                schema.get("User").addField("email", String.class);
            if (!schema.get("User").hasField("showFolderNotes"))
                schema.get("User").addField("showFolderNotes", boolean.class);
            if (!schema.get("User").hasField("showPreviewNoteInfo"))
                schema.get("User").addField("showPreviewNoteInfo", boolean.class);
            if (!schema.get("User").hasField("modeSettings"))
                schema.get("User").addField("modeSettings", boolean.class);
            if (!schema.get("User").hasField("backupReminderOccurrence"))
                schema.get("User").addField("backupReminderOccurrence", int.class);
            if (!schema.get("Note").hasField("enableSublist"))
                schema.get("Note").addField("enableSublist", boolean.class);
            if (!schema.get("User").hasField("enableSublists"))
                schema.get("User").addField("enableSublists", boolean.class);
            if (!schema.get("User").hasField("backupReminderDate"))
                schema.get("User").addField("backupReminderDate", String.class);
            if (!schema.get("User").hasField("ultimateUser"))
                schema.get("User").addField("ultimateUser", boolean.class);

            // added note fields
            if (!schema.get("Note").hasField("dateCreatedMilli"))
                schema.get("Note").addField("dateCreatedMilli", long.class);
            if (!schema.get("Note").hasField("dateEditedMilli"))
                schema.get("Note").addField("dateEditedMilli", long.class);
            if (!schema.get("Note").hasField("sort"))
                schema.get("Note").addField("sort", int.class);
            if (!schema.get("Note").hasField("widgetId"))
                schema.get("Note").addField("widgetId", int.class);

            // added checklist item fields
            if (!schema.get("CheckListItem").hasField("lastCheckedDate"))
                schema.get("CheckListItem").addField("lastCheckedDate", long.class);
            if (!schema.get("CheckListItem").hasField("subChecklist"))
                schema.get("CheckListItem").addRealmListField("subChecklist", schema.get("SubCheckListItem"));
            if (!schema.get("CheckListItem").hasField("subListId"))
                schema.get("CheckListItem").addField("subListId", int.class);
            if (!schema.get("CheckListItem").hasField("itemImage"))
                schema.get("CheckListItem").addField("itemImage", String.class);
            if (!schema.get("CheckListItem").hasField("dateCreated"))
                schema.get("CheckListItem").addField("dateCreated", String.class);

            // added sub-lists class
            if (!schema.contains("SubCheckListItem"))
                schema.create("SubCheckListItem")
                        .addField("id", int.class)
                        .addField("text", String.class)
                        .addField("checked", boolean.class)
                        .addField("positionInList", int.class);

            if (!schema.get("SubCheckListItem").hasField("dateCreated"))
                schema.get("SubCheckListItem").addField("dateCreated", String.class);

            // added backup class
            if (!schema.contains("Backup"))
                schema.create("Backup")
                        .addField("userId", int.class)
                        .addField("fileSize", int.class)
                        .addField("fileName", String.class);

            if (!schema.get("User").hasField("increaseFabSize"))
                schema.get("User").addField("increaseFabSize", boolean.class);

            if (!schema.get("User").hasField("enableEmptyNote"))
                schema.get("User").addField("enableEmptyNote", boolean.class);
            if (!schema.get("User").hasField("itemsSeparator"))
                schema.get("User").addField("itemsSeparator", String.class);
            if (!schema.get("User").hasField("sublistSeparator"))
                schema.get("User").addField("sublistSeparator", String.class);
            if (!schema.get("User").hasField("budgetCharacter"))
                schema.get("User").addField("budgetCharacter", String.class);
            if (!schema.get("User").hasField("expenseCharacter"))
                schema.get("User").addField("expenseCharacter", String.class);
            if (!schema.get("User").hasField("enableDeleteIcon"))
                schema.get("User").addField("enableDeleteIcon", boolean.class);

            if (!schema.get("User").hasField("securityWord"))
                schema.get("User").addField("securityWord", String.class);
            if (!schema.get("User").hasField("fingerprint"))
                schema.get("User").addField("fingerprint", boolean.class);
            if (!schema.get("User").hasField("pinNumber"))
                schema.get("User").addField("pinNumber", int.class);
            if (!schema.get("User").hasField("hideRichTextEditor"))
                schema.get("User").addField("hideRichTextEditor", boolean.class);

            if (!schema.get("CheckListItem").hasField("audioDuration"))
                schema.get("CheckListItem").addField("audioDuration", int.class);
            if (!schema.get("CheckListItem").hasField("audioPath"))
                schema.get("CheckListItem").addField("audioPath", String.class);

            if (!schema.get("User").hasField("showAudioButton"))
                schema.get("User").addField("showAudioButton", boolean.class);

            // added place class
            if (!schema.contains("Place"))
                schema.create("Place")
                        .addField("placeName", String.class)
                        .addField("addressString", String.class)
                        .addField("placeId", String.class);

            // add place to checklist item
            if (!schema.get("CheckListItem").hasField("place"))
                schema.get("CheckListItem").addRealmObjectField("place", schema.get("Place"));

            if (!schema.get("Place").hasField("latitude"))
                schema.get("Place").addField("latitude", double.class);

            if (!schema.get("Place").hasField("longitude"))
                schema.get("Place").addField("longitude", double.class);

            if (!schema.get("User").hasField("hideBudget"))
                schema.get("User").addField("hideBudget", boolean.class);

            if (!schema.get("User").hasField("twentyFourHourFormat"))
                schema.get("User").addField("twentyFourHourFormat", boolean.class);

            if (!schema.get("User").hasField("enableEditableNoteButton"))
                schema.get("User").addField("enableEditableNoteButton", boolean.class);

            if (!schema.get("User").hasField("disableAnimation"))
                schema.get("User").addField("disableAnimation", boolean.class);

            if (!schema.get("User").hasField("showChecklistCheckbox"))
                schema.get("User").addField("showChecklistCheckbox", boolean.class);

            if (!schema.get("User").hasField("screenMode"))
                schema.get("User").addField("screenMode", int.class);

            if (!schema.get("User").hasField("disableLastEditInfo"))
                schema.get("User").addField("disableLastEditInfo", boolean.class);

            if (!schema.get("Note").hasField("lightTextColor"))
                schema.get("Note").addField("lightTextColor", int.class);
        }
    }
}
