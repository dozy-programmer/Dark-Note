package com.akapps.dailynote.classes.helpers;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import com.akapps.dailynote.activity.SettingsScreen;
import com.akapps.dailynote.classes.data.Backup;
import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.Photo;
import com.akapps.dailynote.classes.data.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import kotlin.io.FilesKt;
import www.sanju.motiontoast.MotionToast;

public class BackupHelper {

    private final Activity activity;
    private final Context context;
    private Dialog progressDialog;
    private final BackupRealm backupRealm;

    // account authentication
    private final FirebaseAuth mAuth;

    private String backupPath;

    public BackupHelper(Activity activity, Context context, FirebaseAuth mAuth) {
        this.activity = activity;
        this.context = context;
        this.mAuth = mAuth;
        backupRealm = new BackupRealm(context);
    }

    public String backUpZip(boolean includeImages, boolean includeAudio) {
        return zipAppFiles(getAllAppFiles(includeImages, includeAudio));
    }

    public boolean showBackupFailStatus(boolean isSuccessful) {
        if (!isSuccessful) Helper.showMessage(activity, "Backup Error",
                "Cannot restore your notes", MotionToast.TOAST_ERROR);
        return isSuccessful;
    }

    public ArrayList<String> getAllAppFiles(boolean includeImages, boolean includeAudio) {
        RealmResults<Note> checklistPhotos = RealmSingleton.get(activity).where(Note.class).findAll();
        ArrayList<String> allFiles = new ArrayList<>();
        ArrayList<String> allPhotos = new ArrayList<>();
        ArrayList<String> allRecordings = new ArrayList<>();
        if (includeImages) {
            allPhotos = getAllPhotoPaths(RealmSingleton.get(activity).where(Photo.class)
                    .not().isNull("photoLocation").or().equalTo("photoLocation", "")
                    .findAll(), checklistPhotos);
        }
        if (includeAudio) {
            allRecordings = getAllRecordingsPaths(RealmSingleton.get(activity).where(CheckListItem.class).not()
                    .isNull("audioPath").or().equalTo("audioPath", "").findAll());
        }

        // duplicate database realm to backup
        File exportedFilePath = backupRealm.createCopyOfRealmDatabase();
        // add realm path (this contains all the note data)
        allFiles.add(exportedFilePath.getAbsolutePath());
        // add all photos paths
        if (includeImages && !allPhotos.isEmpty()) allFiles.addAll(allPhotos);
        // add all recording paths
        if (includeAudio && !allRecordings.isEmpty()) allFiles.addAll(allRecordings);
        return allFiles;
    }

    private ArrayList<String> getAllPhotoPaths(RealmResults<Photo> allNotePhotos, RealmResults<Note> allNotes) {
        ArrayList<String> allPhotos = new ArrayList<>();
        for (int i = 0; i < allNotePhotos.size(); i++)
            allPhotos.add(allNotePhotos.get(i).getPhotoLocation());

        for (int i = 0; i < allNotes.size(); i++) {
            RealmList<CheckListItem> currentNoteChecklist = allNotes.get(i).getChecklist();
            if (currentNoteChecklist.size() > 0) {
                for (int j = 0; j < currentNoteChecklist.size(); j++) {
                    CheckListItem currentChecklistItem = currentNoteChecklist.get(j);
                    if (currentChecklistItem.getItemImage() != null && !currentChecklistItem.getItemImage().isEmpty())
                        allPhotos.add(currentChecklistItem.getItemImage());
                }
            }
        }

        return allPhotos;
    }

    private ArrayList<String> getAllRecordingsPaths(RealmResults<CheckListItem> allChecklistRecordings) {
        ArrayList<String> allRecordings = new ArrayList<>();
        for (int i = 0; i < allChecklistRecordings.size(); i++)
            allRecordings.add(allChecklistRecordings.get(i).getAudioPath());

        return allRecordings;
    }

    // places all the photos in a zip file and returns a string of the file path
    private String zipAppFiles(ArrayList<String> files) {
        File backupZipFolder = FileHelper.createBackupZipFolder(context);
        int BUFFER = 1024;
        try {
            BufferedInputStream origin;
            FileOutputStream dest = new FileOutputStream(backupZipFolder);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte[] data = new byte[BUFFER];
            for (int i = 0; i < files.size(); i++) {
                File newFile = new File(files.get(i));
                if (newFile.exists()) {
                    FileInputStream fi = new FileInputStream(files.get(i));
                    origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry = new ZipEntry(files.get(i).substring(files.get(i).lastIndexOf("/") + 1));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                }
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return backupZipFolder.getAbsolutePath();
    }

    public void upLoadToFirebaseStorage() {
        AtomicReference<User> currentUser = new AtomicReference<>(RealmHelper.getUser(context, "backupHelper"));
        File backupFile = new File(backUpZip(true, true));
        Uri file = Uri.fromFile(backupFile);
        // file info
        String fileSize = Helper.getFormattedFileSize(context, backupFile.length());

        if (!fileSize.toLowerCase().contains("b") && !fileSize.toLowerCase().contains("kb")
                && !fileSize.toLowerCase().contains("mb")) {
            Helper.showMessage(activity, "Upload Failed", "File size is too big, backup locally",
                    MotionToast.TOAST_ERROR);
            return;
        }

        if (fileSize.toLowerCase().contains("mb")) {
            try {
                double fileSizeNumber = Double.parseDouble(fileSize.toLowerCase().replace("mb", "").trim());
                if (fileSizeNumber > 100.0) {
                    Helper.showMessage(activity, "Upload Failed", "File size is too big, backup locally",
                            MotionToast.TOAST_ERROR);
                    return;
                }
            } catch (Exception e) {
            }
        }

        progressDialog = Helper.showLoading("Uploading...\n" + fileSize, progressDialog, context, true);

        String currentDate = Helper.getBackupDate(fileSize);
        String fileName = currentDate + "_" + AppConstants.BACKUP_ZIP_FILE_NAME;

        // check if file exists
        if (RealmSingleton.get(activity).where(Backup.class).equalTo("fileName", fileName).count() == 1) {
            Helper.showLoading("", progressDialog, context, false);
            Helper.showMessage(activity, "Upload Failed", "File name exists, " +
                    "please wait a minute and try again", MotionToast.TOAST_ERROR);
        } else {
            String userEmail = mAuth.getCurrentUser().getEmail();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child("users").child(userEmail).child(fileName);
            UploadTask uploadTask = storageRef.putFile(file);

            uploadTask.addOnProgressListener(snapshot -> {
                String bytesTransferredFormatted = Helper.getFormattedFileSize(context, snapshot.getBytesTransferred());
                progressDialog = Helper.showLoading("Uploading...\n" + bytesTransferredFormatted + " / " + fileSize,
                        progressDialog, context, true);
            }).addOnFailureListener(exception -> {
                progressDialog.cancel();
                Helper.showMessage(activity, "Upload error",
                        "Error Uploading data, try again",
                        MotionToast.TOAST_ERROR);
                Helper.restart(activity);
            }).addOnSuccessListener(taskSnapshot -> {
                currentUser.set(RealmHelper.getUser(context, "backupHelper"));
                String bytesTransferredFormatted = Helper.getFormattedFileSize(context, taskSnapshot.getBytesTransferred());
                if (bytesTransferredFormatted.equals(fileSize)) {
                    RealmSingleton.get(activity).beginTransaction();
                    RealmSingleton.get(activity).insert(new Backup(currentUser.get().getUserId(), fileName, new Date(), 0));
                    currentUser.get().setLastUpload(Helper.getCurrentDate());
                    RealmSingleton.get(activity).commitTransaction();
                    progressDialog.cancel();
                    Helper.showMessage(activity, "Upload Success", "Data Uploaded", MotionToast.TOAST_SUCCESS);
                } else {
                    // deleting file since it was missing files
                    storageRef.delete()
                            .addOnSuccessListener(aVoid -> {
                            })
                            .addOnFailureListener(exception -> {
                            });
                    progressDialog.cancel();
                    Helper.showMessage(activity, "Upload Error", "Files lost in transfer, please upload again!", MotionToast.TOAST_ERROR);
                }
            });
        }
    }

    public void restoreFromFirebaseStorage(String fileName, String fileSize) {
        progressDialog = Helper.showLoading("Syncing...", progressDialog, context, true);
        String userEmail = mAuth.getCurrentUser().getEmail();
        Log.d("Here", "filename " + fileName + ", userEmail " + userEmail);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference()
                .child("users/" + userEmail + "/" + fileName);

        File storageDir = new File(context.getExternalFilesDir(null) + "/Dark Note");

        if (!storageDir.exists())
            storageDir.mkdirs();

        File localFile = new File(storageDir, AppConstants.BACKUP_ZIP_FILE_NAME);
        storageRef.getFile(localFile)
                .addOnProgressListener(snapshot -> {
                    String bytesTransferredFormatted = Helper.getFormattedFileSize(context, snapshot.getBytesTransferred());
                    progressDialog = Helper.showLoading("Syncing...\n" + bytesTransferredFormatted + " / " + fileSize, progressDialog, context, true);
                }).addOnSuccessListener(taskSnapshot -> {
                    importDatabase(Uri.fromFile(localFile));
                }).addOnFailureListener(exception -> {
                    Helper.showLoading("", progressDialog, context, false);
                    Helper.showMessage(activity, "Error", "Restoring Error from database, please clear app storage & try again", MotionToast.TOAST_ERROR);
                });
    }

    private void importDatabase(Uri uri) {
        try {
            // delete realm before restoring
            if (!RealmHelper.deleteRealmDatabase(activity)) return;

            // make a copy of the backup zip file selected by user and unzip it
            boolean isSuccessful = backupRealm.restore(uri, AppConstants.BACKUP_ZIP_FILE_NAME);
            if (!showBackupFailStatus(isSuccessful)) return;

            Helper.showLoading("", progressDialog, context, false);

            ArrayList<String> images = getImagesPath();
            ArrayList<String> recordings = getRecordingsPath();
            isSuccessful = backupRealm.restoreRealmFileIntoDatabase(getBackupPath(), AppConstants.REALM_EXPORT_FILE_NAME);
            if (!showBackupFailStatus(isSuccessful)) return;

            Helper.showMessage(activity, "Restored", "Notes have been restored", MotionToast.TOAST_SUCCESS);

            // update image paths from restored database so it knows where the images are
            Realm realm = RealmSingleton.getInstance(context);
            updateAlarms(activity, realm.where(Note.class)
                    .equalTo("archived", false)
                    .equalTo("trash", false).findAll());
            updateImages(context, images);
            updateRecordings(context, recordings);
            resetWidgets(context);

            // delete all zip files
            Helper.deleteZipFile(context);
            ((SettingsScreen) activity).close();
        } catch (Exception e) {
            Helper.showLoading("", progressDialog, context, false);
            Helper.showMessage(activity, "Error", "Restoring Error to device, try again", MotionToast.TOAST_ERROR);
        }
    }

    public void restoreBackupFromFile(Intent data) {
        if (data != null) {
            Uri uri = data.getData();
            String fileName = getFileName(uri);

            if (fileName.endsWith(".realm")) {
                Log.d("Here", "restoring realm backup");
                restoreBackupFromRealmFile(uri);
            } else if (fileName.contains(".zip")) {
                Log.d("Here", "restoring zip backup");
//                try {
                // delete realm before restoring
                if (!RealmHelper.deleteRealmDatabase(activity)) return;

                // delete all files first
                FilesKt.deleteRecursively(new File(context.getExternalFilesDir(null) + ""));

                // make a copy of the backup zip file selected by user and unzip it
                backupRealm.restore(uri, AppConstants.BACKUP_ZIP_FILE_NAME);

                ArrayList<String> images = getImagesPath();
                ArrayList<String> recordings = getRecordingsPath();
                backupRealm.restoreRealmFileIntoDatabase(getBackupPath(), AppConstants.REALM_EXPORT_FILE_NAME);

                // update image paths from restored database so it knows where the images are
                Realm realm = RealmSingleton.getInstance(context);
                updateAlarms(activity, realm.where(Note.class)
                        .equalTo("archived", false)
                        .equalTo("trash", false).findAll());
                updateImages(context, images);
                updateRecordings(context, recordings);
                resetWidgets(context);

                Helper.showMessage(activity, "Restored", "Notes have been restored", MotionToast.TOAST_SUCCESS);

                // delete all zip files
                Helper.deleteZipFile(context);

                ((SettingsScreen) activity).close();
//                } catch (Exception e) {
//                    Helper.showMessage(activity, "Error Restoring", "An issue occurred, Try again!", MotionToast.TOAST_ERROR);
//                }
            } else {
                Helper.showMessage(activity, "Error\uD83D\uDE14", "Dark Note Backup Files end in '.zip' or '.realm'. Try again!", MotionToast.TOAST_ERROR);
            }
        }
    }

    public void restoreBackupFromRealmFile(Uri uri) {
        try {
            // delete realm
            boolean isSuccessful = RealmHelper.deleteRealmDatabase(activity);
            if (!showBackupFailStatus(isSuccessful)) return;

            backupRealm.restore(uri, AppConstants.REALM_EXPORT_FILE_NAME);
            Helper.showMessage(activity, "Restored", "Notes have been restored", MotionToast.TOAST_SUCCESS);

            Realm realm = RealmSingleton.getInstance(context);
            updateAlarms(activity, realm.where(Note.class)
                    .equalTo("archived", false)
                    .equalTo("trash", false).findAll());

            ((SettingsScreen) activity).close();
        } catch (Exception e) {
            Helper.showMessage(activity, "Restore Error", "" +
                    "Issue Restoring, try again...", MotionToast.TOAST_ERROR);
        }
    }

    public ArrayList<String> getRecordingsPath() {
        return findRecordingPaths();
    }

    private ArrayList<String> findRecordingPaths() {
        ArrayList<String> recordings = new ArrayList<>();
        String path = activity.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "";
        File directory = new File(path);
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.getName().contains(".mp4") || file.getName().contains(".mp3"))
                recordings.add(file.getPath());
        }
        return recordings;
    }

    public ArrayList<String> getImagesPath() {
        return findImageAndBackupPaths();
    }

    private ArrayList<String> findImageAndBackupPaths() {
        ArrayList<String> images = new ArrayList<>();
        String path = activity.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "";
        File directory = new File(path);
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.getName().contains(".png"))
                images.add(file.getPath());
            else if (file.getName().contains(".realm"))
                backupPath = file.getPath();
        }
        return images;
    }

    private void updateAlarms(Activity activity, RealmResults<Note> allNotes) {
        for (int i = 0; i < allNotes.size(); i++) {
            Note currentNote = allNotes.get(i);
            if (null != currentNote.getReminderDateTime() && !currentNote.getReminderDateTime().isEmpty())
                Helper.startAlarm(activity, currentNote.getNoteId(), RealmSingleton.get(activity));
        }
    }

    private void resetWidgets(Context context) {
        RealmSingleton.get(context).beginTransaction();
        RealmSingleton.get(context).where(Note.class).not().equalTo("widgetId", 0).findAll().setInt("widgetId", 0);
        RealmSingleton.get(context).commitTransaction();
    }

    private void updateImages(Context context, ArrayList<String> imagePaths) {
        if (imagePaths.size() != 0) {
            for (int i = 0; i < imagePaths.size(); i++) {
                String imagePath = imagePaths.get(i).substring(imagePaths.get(i).lastIndexOf("/") + 1);
                if (imagePaths.get(i).contains("~")) {
                    CheckListItem currentChecklistPhoto = RealmSingleton.get(context).where(CheckListItem.class).contains("itemImage", imagePath).findFirst();
                    if (currentChecklistPhoto != null) {
                        RealmSingleton.get(context).beginTransaction();
                        currentChecklistPhoto.setItemImage(imagePaths.get(i));
                        RealmSingleton.get(context).commitTransaction();
                    }
                } else {
                    Photo currentPhoto = RealmSingleton.get(context).where(Photo.class).contains("photoLocation", imagePath).findFirst();
                    if (currentPhoto != null) {
                        RealmSingleton.get(context).beginTransaction();
                        currentPhoto.setPhotoLocation(imagePaths.get(i));
                        RealmSingleton.get(context).commitTransaction();
                    }
                }
            }
        }
    }

    private void updateRecordings(Context context, ArrayList<String> recordingsPath) {
        if (recordingsPath.size() != 0) {
            for (int i = 0; i < recordingsPath.size(); i++) {
                String fullRecordingPath = recordingsPath.get(i);
                String recordingPath = fullRecordingPath.substring(fullRecordingPath.lastIndexOf("/") + 1);
                CheckListItem checkListItem = RealmSingleton.get(context).where(CheckListItem.class).contains("audioPath", recordingPath).findFirst();
                if (checkListItem != null) {
                    RealmSingleton.get(context).beginTransaction();
                    checkListItem.setAudioPath(fullRecordingPath);
                    RealmSingleton.get(context).commitTransaction();
                }
            }
        }
    }

    public void shareFile(File backup) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            shareZipFileAndroid10Plus();
        else
            shareFileAndroid10Under(backup);
    }

    private void shareZipFileAndroid10Plus() {
        Intent emailIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        emailIntent.addCategory(Intent.CATEGORY_OPENABLE);
        emailIntent.setType("application/zip");
        emailIntent.putExtra(Intent.EXTRA_TITLE, "dark_note_backup");

        Intent shareIntent = Intent.createChooser(emailIntent, "Share Dark Note Backup File");
        activity.startActivityForResult(shareIntent, 2);
    }

    private void shareFileAndroid10Under(File backup) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("*/*");

        Uri fileBackingUp = FileProvider.getUriForFile(
                context,
                "com.akapps.dailynote.fileprovider",
                backup);
        emailIntent.putExtra(Intent.EXTRA_STREAM, fileBackingUp);

        Intent shareIntent = Intent.createChooser(emailIntent, "Share Dark Note Backup File");
        activity.startActivity(shareIntent);
    }

    private String getFileName(Uri uri) {
        String fileName = null;
        DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
        if (documentFile != null) {
            fileName = documentFile.getName();
        } else {
            fileName = "";
        }
        return fileName;
    }

    private String getBackupPath() {
        return backupPath;
    }

}
