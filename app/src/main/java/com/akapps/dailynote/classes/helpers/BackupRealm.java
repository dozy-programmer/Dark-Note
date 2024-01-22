package com.akapps.dailynote.classes.helpers;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import io.realm.Realm;

public class BackupRealm {
    private final Context context;

    public BackupRealm(Context context) {
        this.context = context;
    }

    private Realm getRealm() {
        return RealmSingleton.getInstance(context);
    }

    public File createCopyOfRealmDatabase() {
        File storageDir = FileHelper.getInternalBackupDirectory(context);
        File exportRealmFile = new File(storageDir, AppConstants.REALM_EXPORT_FILE_NAME);
        FileHelper.newOrDelete(exportRealmFile);

        // copy current realm to backup file
        getRealm().writeCopyTo(exportRealmFile);
        return exportRealmFile;
    }

    public boolean restore(Uri uri, String exportFileName) {
        boolean unZip = exportFileName.equals(AppConstants.BACKUP_ZIP_FILE_NAME) ||
                exportFileName.endsWith(AppConstants.ZIP_EXTENSION);
        File storageDir = FileHelper.getBackupDirectory(context, unZip);
        FileHelper.existsOrCreate(storageDir);
        File exportRealmFile = new File(storageDir, exportFileName);
        FileHelper.newOrDelete(exportRealmFile);

        FileHelper.writeFileTo(context, uri, exportRealmFile);
        String restoredRealmFilePath = exportRealmFile.getAbsolutePath();

        // unzip file if it is a directory
        if (unZip) {
            try {
                FileHelper.unzip(restoredRealmFilePath, storageDir.getPath());
            } catch (Exception e) {
                return false;
            }
        }
        // restore realm database from received file
        return restoreRealmFileIntoDatabase(restoredRealmFilePath, exportFileName);
    }

    public boolean restoreRealmFileIntoDatabase(String oldFilePath, String outFileName) {
        try {
            File file = new File(context.getFilesDir(), outFileName);
            FileOutputStream outputStream = new FileOutputStream(file);
            FileInputStream inputStream = new FileInputStream(oldFilePath);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, bytesRead);
            }
            outputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}