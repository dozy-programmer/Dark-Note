package com.akapps.dailynote.classes.helpers;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;

import io.realm.RealmResults;

public class BackupRealmHelper {

    private final static String CLASS_PREFIX = "com.akapps.dailynote.classes.data.";
    private final static String fileSectionHeader = "---%s %s---";
    private final static String fileSectionTop = "START";
    private final static String fileSectionEnd = "END";

    public enum DataType {
        USER("User"),
        NOTE("Note"),
        FOLDER("Folder"),
        PHOTO("Photo");

        private final String name;

        DataType(String name) {
            this.name = name;
        }
    }

    public static ArrayList<String> getAllRealmModels() {
        return new ArrayList<>(Arrays.asList(
                DataType.USER.name,
                DataType.NOTE.name,
                DataType.FOLDER.name,
                DataType.PHOTO.name
        )
        );
    }

    public static Class getClassName(String className) {
        try {
            return Class.forName(CLASS_PREFIX + className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static RealmResults getAllClassData(Context context, String className) {
        return RealmHelper.getRealm(context).where(getClassName(className)).findAll();
    }

    public static String createSectionHeader(String className, boolean isAtTop) {
        return (isAtTop ? "" : "\n") + String.format(fileSectionHeader, isAtTop ? fileSectionTop : fileSectionEnd, className) + "\n";
    }

    public static String getSection(String className, boolean isAtTop) {
        return createSectionHeader(className, isAtTop).replaceAll("\n", "");
    }

}
