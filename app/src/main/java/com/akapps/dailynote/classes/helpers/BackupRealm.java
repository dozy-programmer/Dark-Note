package com.akapps.dailynote.classes.helpers;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

    public static Map<String, String> readBackupFile(Context context, Uri backupUri) {
        Map<String, String> gsonStrings = new HashMap<>();
        ArrayList<String> realmModels = BackupRealmHelper.getAllRealmModels();

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
                            currentGson.setLength(0);
                        } else if (line.startsWith(BackupRealmHelper.getSection(realmModels.get(realmModelPosition), false))) {
                            gsonStrings.put(realmModels.get(realmModelPosition), currentGson.toString());
                            Log.d("Here", "Finished " + realmModels.get(realmModelPosition));
                            realmModelPosition++;
                            currentGson.setLength(0);
                        } else {
                            currentGson.append(line);
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
