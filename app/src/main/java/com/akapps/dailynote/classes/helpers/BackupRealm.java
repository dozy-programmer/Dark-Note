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
        return RealmSingleton.get(context);
    }

    public File createCopyOfRealmDatabase() {
        File storageDir = FileHelper.getInternalBackupDirectory(context);
        File exportRealmFile = new File(storageDir, AppConstants.REALM_EXPORT_FILE_NAME);
        FileHelper.newOrDelete(exportRealmFile);

        // copy current realm to backup file
        getRealm().writeCopyTo(exportRealmFile);
        return exportRealmFile;
    }

    public void restore(Uri uri, String exportFileName) throws IOException {
        boolean unZip = exportFileName.equals(AppConstants.BACKUP_ZIP_FILE_NAME) ||
                exportFileName.endsWith(AppConstants.ZIP_EXTENSION);
        File storageDir = FileHelper.getTemporaryBackupDirectory(context);
        FileHelper.existsOrCreate(storageDir);
        File exportRealmFile = new File(storageDir, exportFileName);
        FileHelper.newOrDelete(exportRealmFile);

        FileHelper.writeFileTo(context, uri, exportRealmFile);
        String restoredRealmFilePath = exportRealmFile.getAbsolutePath();

        // unzip file if it is a directory
        if (unZip) {
            FileHelper.unzip(restoredRealmFilePath, storageDir.getPath());
        }
        // restore realm database from received file
        restoreRealmFileIntoDatabase(restoredRealmFilePath, exportFileName);
    }

    public void restoreRealmFileIntoDatabase(String oldFilePath, String outFileName) throws IOException {
        File file = new File(context.getFilesDir(), outFileName);
        try (FileOutputStream outputStream = new FileOutputStream(file);
             FileInputStream inputStream = new FileInputStream(oldFilePath)) {
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, bytesRead);
            }
        }
    }

}

