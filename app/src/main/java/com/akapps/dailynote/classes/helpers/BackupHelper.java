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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

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
        try {
            return zipAppFiles(getAllAppFiles(includeImages, includeAudio));
        } catch (IOException e) {
            Log.e("BackupHelper", "Error creating zip file", e);
            return null;
        }
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
                    .isNotNull("photoLocation")
                    .and()
                    .isNotEmpty("photoLocation")
                    .findAll(), checklistPhotos);
        }
        if (includeAudio) {
            allRecordings = getAllRecordingsPaths(RealmSingleton.get(activity).where(CheckListItem.class)
                    .isNotNull("audioPath")
                    .and()
                    .isNotEmpty("audioPath")
                    .findAll());
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

    private String zipAppFiles(ArrayList<String> files) throws IOException {
        File backupZipFile = FileHelper.createBackupZipFolder(context);
        Uri backupUri = Uri.fromFile(backupZipFile);

        FileHelper.zip(context, files, backupUri);

        File realmFile = new File(context.getFilesDir(), AppConstants.REALM_EXPORT_FILE_NAME);
        File extractedRealmFile = new File(context.getCacheDir(), "extracted.realm");

        try {
            FileHelper.extractFileFromZip(backupZipFile.getAbsolutePath(), AppConstants.REALM_EXPORT_FILE_NAME, extractedRealmFile);
            if (realmFile.length() != extractedRealmFile.length()) {
                throw new IOException("Realm file size mismatch after backup.");
            }
        } finally {
            extractedRealmFile.delete();
        }

        return backupZipFile.getAbsolutePath();
    }

    public void upLoadToFirebaseStorage() {
        AtomicReference<User> currentUser = new AtomicReference<>(RealmHelper.getUser(context, "backupHelper"));
        String backupPath = backUpZip(true, true);
        if (backupPath == null) {
            Helper.showMessage(activity, "Upload Failed", "Could not create backup file.", MotionToast.TOAST_ERROR);
            return;
        }
        File backupFile = new File(backupPath);
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
                if (fileSizeNumber > 30) {
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
                    if (progressDialog != null) progressDialog.dismiss();
                }).addOnFailureListener(exception -> {
                    if (progressDialog != null) progressDialog.dismiss();
                    Helper.showMessage(activity, "Error", "Restoring Error from database, please clear app storage & try again", MotionToast.TOAST_ERROR);
                });
    }

    private void importDatabase(Uri uri) {
        if (performZipRestore(uri)) {
            if (activity instanceof SettingsScreen) {
                ((SettingsScreen) activity).close();
            }
        }
    }

    private boolean performZipRestore(Uri uri) {
        try {
            File tempDir = FileHelper.getTemporaryBackupDirectory(context);
            FileHelper.newOrDelete(tempDir);
            FileHelper.existsOrCreate(tempDir);

            backupRealm.restore(uri, AppConstants.BACKUP_ZIP_FILE_NAME);

            if (!RealmHelper.deleteRealmDatabase(activity)) {
                showBackupFailStatus(false);
                return false;
            }
            File externalFilesDir = context.getExternalFilesDir(null);
            if (externalFilesDir != null) {
                FilesKt.deleteRecursively(externalFilesDir);
            }

            FileHelper.moveDirectory(tempDir, context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS));

            ArrayList<String> images = getImagesPath();
            ArrayList<String> recordings = getRecordingsPath();
            backupRealm.restoreRealmFileIntoDatabase(getBackupPath(), AppConstants.REALM_EXPORT_FILE_NAME);

            Realm realm = RealmSingleton.get(context);
            updateAlarms(activity, realm.where(Note.class)
                    .equalTo("archived", false)
                    .equalTo("trash", false).findAll());
            updateImages(context, images);
            updateRecordings(context, recordings);
            resetWidgets(context);

            Helper.showMessage(activity, "Restored", "Notes have been restored", MotionToast.TOAST_SUCCESS);

            Helper.deleteZipFile(context);
            return true;
        } catch (IOException e) {
            Log.e("BackupHelper", "Error during zip restore", e);
            Helper.showMessage(activity, "Error Restoring", e.getMessage(), MotionToast.TOAST_ERROR);
            return false;
        }
    }

    public void restoreBackupFromFile(Intent data) {
        if (data != null) {
            Uri uri = data.getData();
            String fileName = getFileName(uri);

            if (fileName == null) {
                Helper.showMessage(activity, "Restoring Error", "File not received due to error", MotionToast.TOAST_ERROR);
                return;
            }

            if (fileName.endsWith(".realm")) {
                Log.d("Here", "restoring realm backup");
                restoreBackupFromRealmFile(uri);
            } else if (fileName.contains(".zip")) {
                Log.d("Here", "restoring zip backup");
                if (performZipRestore(uri)) {
                    if (activity instanceof SettingsScreen) {
                        ((SettingsScreen) activity).close();
                    }
                }
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

            Realm realm = RealmSingleton.get(context);
            updateAlarms(activity, realm.where(Note.class)
                    .equalTo("archived", false)
                    .equalTo("trash", false).findAll());

            ((SettingsScreen) activity).close();
        } catch (Exception e) {
            Helper.showMessage(activity, "Restore Error", "" +
                    "Issue Restoring, close app and try again...", MotionToast.TOAST_ERROR);
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
        if (files != null) {
            for (File file : files) {
                if (file.getName().contains(".mp4") || file.getName().contains(".mp3"))
                    recordings.add(file.getPath());
            }
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
        if (files != null) {
            for (File file : files) {
                if (file.getName().contains(".png"))
                    images.add(file.getPath());
                else if (file.getName().contains(".realm"))
                    backupPath = file.getPath();
            }
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
        try {
            Intent emailIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            emailIntent.addCategory(Intent.CATEGORY_OPENABLE);
            emailIntent.setType("application/zip");
            emailIntent.putExtra(Intent.EXTRA_TITLE, AppConstants.getMonthDay() + "_dark_note_backup");

            Intent shareIntent = Intent.createChooser(emailIntent, "Share Dark Note Backup File");
            activity.startActivityForResult(shareIntent, 2);
        } catch (Exception e){
            Helper.showMessage(activity, "Sharing Error", "Size too large v3, email developer", MotionToast.TOAST_ERROR);        }
    }

    private void shareFileAndroid10Under(File backup) {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("*/*");

            Uri fileBackingUp = FileProvider.getUriForFile(
                    context,
                    "com.akapps.dailynote.fileprovider",
                    backup);
            emailIntent.putExtra(Intent.EXTRA_STREAM, fileBackingUp);

            Intent shareIntent = Intent.createChooser(emailIntent, "Share Dark Note Backup File");
            activity.startActivity(shareIntent);
        } catch (Exception e) {
            Helper.showMessage(activity, "Sharing Error", "Size too large v4, email developer", MotionToast.TOAST_ERROR);
        }
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

