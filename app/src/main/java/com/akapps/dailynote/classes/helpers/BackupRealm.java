package com.akapps.dailynote.classes.helpers;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import io.realm.RealmResults;

public class BackupRealm {

    public static String convertRealmToJson(Context context, RealmResults modelList) {
        Gson gson = new Gson();
        String convertedGson = gson.toJson(RealmHelper.getRealm(context).copyFromRealm(modelList));
        if (convertedGson == null || convertedGson.isEmpty() || convertedGson.replaceAll(" ", "").equals("[]"))
            return "";
        return convertedGson;
    }

    public static boolean convertGsonToRealm(Context context, Map<String, String> backups) {
        Class realmClass = null;
        for (String key : backups.keySet()) {
            String value = backups.get(key);
            Log.d("Here", key + ": " + value);
            realmClass = BackupRealmHelper.getClassName(key);

            RealmSingleton.getInstance(context).beginTransaction();
            try {
                if (realmClass == null) {
                    Log.d("Here", "error converting class string to class");
                    return false;
                }
                RealmSingleton.getInstance(context).createAllFromJson(realmClass, value);
                RealmSingleton.getInstance(context).commitTransaction();
            } catch (Exception e) {
                RealmSingleton.getInstance(context).cancelTransaction();
                Log.d("Here", "error converting gson to class " + realmClass);
                return false;
            }
        }
        Log.d("Here", "---------------------- DONE CONVERTING ----------------------");
        return true;
    }

    public static boolean create(Context context, Uri uri) {
        try {
            ParcelFileDescriptor document = context.getContentResolver().openFileDescriptor(uri, "w");
            FileOutputStream fileOutputStream = new FileOutputStream(document.getFileDescriptor());
            StringBuilder line = new StringBuilder();
            Log.d("Here", "realm models -> " + BackupRealmHelper.getAllRealmModels());
            for (String dataClass : BackupRealmHelper.getAllRealmModels()) {
                String data = convertRealmToJson(context, BackupRealmHelper.getAllClassData(context, dataClass));
                if (data.isEmpty()) continue;
                Log.d("Here", dataClass + ": " + data);
                line.append(BackupRealmHelper.createSectionHeader(dataClass, true))
                        .append(data)
                        .append(BackupRealmHelper.createSectionHeader(dataClass, false));
                fileOutputStream.write(line.toString().getBytes());
                line.setLength(0);
            }
            // Let the document provider know you're done by closing the stream.
            fileOutputStream.close();
            document.close();
            Log.d("Here", "---------------------- DONE SAVING ----------------------");
        } catch (FileNotFoundException e) {
            Log.d("Here", "file not found");
            return false;
        } catch (IOException e) {
            Log.d("Here", "io exception");
            return false;
        }
        return true;
    }

    public static boolean zipAppFiles(Context context, ArrayList<String> files, Uri uri) {
        int BUFFER = 1024;

        try {
            ParcelFileDescriptor document = context.getContentResolver().openFileDescriptor(uri, "w");
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(document.getFileDescriptor())));

            byte[] data = new byte[BUFFER];
            for (String filePath : files) {
                File newFile = new File(filePath);
                if (newFile.exists()) {
                    FileInputStream fi = new FileInputStream(filePath);
                    ZipEntry entry = new ZipEntry(filePath.substring(filePath.lastIndexOf("/") + 1));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = fi.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    fi.close();
                    out.closeEntry();
                }
            }

            out.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static boolean unzipAppFiles(Context context, Uri uri) {
        int BUFFER = 1024;

        File storageDir;
        storageDir = new File(context.getApplicationContext()
                .getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "");

        if (!storageDir.exists()) storageDir.mkdirs();

        try {
            ParcelFileDescriptor document = context.getContentResolver().openFileDescriptor(uri, "r");
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(document.getFileDescriptor())));

            ZipEntry entry;
            byte[] data = new byte[BUFFER];

            while ((entry = zis.getNextEntry()) != null) {
                String filePath = storageDir + File.separator + entry.getName();
                File file = new File(filePath);

                // Ensure the parent directory exists
                File parentDir = new File(file.getParent());
                if (!parentDir.exists()) {
                    parentDir.mkdirs();
                }

                FileOutputStream fos = new FileOutputStream(file);
                int count;
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    fos.write(data, 0, count);
                }
                fos.close();
                zis.closeEntry();
            }

            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static Map<String, String> readBackupFile(Context context, Uri backupUri) {
        Map<String, String> gsonStrings = new HashMap<>();
        ArrayList<String> realmModels = BackupRealmHelper.getAllRealmModels();
        boolean shouldReadLine = false;

        if (backupUri != null) {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(backupUri);

                if (inputStream != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder currentGson = new StringBuilder();
                    String line;
                    int realmModelPosition = 0;

                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith(BackupRealmHelper.getSection(realmModels.get(realmModelPosition), true))) {
                            Log.d("Here", "Reading " + realmModels.get(realmModelPosition));
                            shouldReadLine = true;
                            currentGson.setLength(0);
                        } else if (line.startsWith(BackupRealmHelper.getSection(realmModels.get(realmModelPosition), false))) {
                            gsonStrings.put(realmModels.get(realmModelPosition), currentGson.toString());
                            Log.d("Here", "Finished " + realmModels.get(realmModelPosition));
                            realmModelPosition++;
                            shouldReadLine = false;
                            currentGson.setLength(0);
                        } else {
                            if (shouldReadLine) currentGson.append(line);
                        }
                    }

                    inputStream.close();
                } else {
                    Log.d("Here", "input  stream null");
                    return null;
                }
            } catch (IOException e) {
                Log.d("Here", "io exception");
                return null;
            }
        }
        return gsonStrings;
    }
}
