package com.akapps.dailynote.classes.helpers;

public class AppConstants {

    public static String REALM_EXPORT_FILE_NAME = "default.realm";
    public static String BACKUP_ZIP_FILE_NAME = "backup.zip";
    public static String ZIP_EXTENSION = "zip";

    public static String UNUSED_FILES_MESSAGE = "unused_files_key";

    public enum LockType {
        LOCK_NOTE,
        LOCK_APP,
        LOCK_FOLDER
    }
}
