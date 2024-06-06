package com.akapps.dailynote.classes.helpers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AppConstants {

    public static String REALM_EXPORT_FILE_NAME = "default.realm";
    public static String BACKUP_ZIP_FILE_NAME = "backup.zip";
    public static String ZIP_EXTENSION = "zip";

    public static String SCHEMA_VERSION = "schema_version";
    public static String USER_ID = "user_id";

    public static String WHATS_NEW_18_1 = "update_18_1";

    public static String getMonthDay() {
        DateFormat dateFormat = new SimpleDateFormat("MMM_dd"); // Use "MMM dd" format
        Date date = new Date();
        return dateFormat.format(date).toLowerCase();
    }

    public enum LockType {
        LOCK_NOTE,
        LOCK_APP,
        LOCK_FOLDER
    }

}
