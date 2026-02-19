package com.akapps.dailynote.classes.helpers;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.data.Backup;
import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.data.Folder;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.Photo;
import com.akapps.dailynote.classes.data.SubCheckListItem;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.other.LockFolderSheet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import www.sanju.motiontoast.MotionToast;

public class RealmHelper {

    public static Realm getRealm(Context context) {
        return RealmSingleton.get(context);
    }

    public static void deleteNote(Context context, int noteID) {
        // get a note
        Note currentNote;
        getRealm(context).beginTransaction();
        currentNote = getRealm(context).where(Note.class).equalTo("noteId", noteID).findFirst();
        getRealm(context).commitTransaction();

        // delete checklist, check list item photos, and sublists
        if (currentNote.isCheckList()) {
            deleteChecklist(currentNote, context);
        }

        // deletes photos if they exist
        deleteNotePhotos(currentNote, context);

        if (!currentNote.getReminderDateTime().isEmpty())
            Helper.cancelNotification(context, currentNote.getNoteId());

        // deletes note
        if (currentNote.getWidgetId() > 0) {
            getRealm(context).beginTransaction();
            currentNote.setPinNumber(0);
            currentNote.setIsCheckList(false);
            currentNote.setTitle("Delete Me");
            currentNote.setNote("* Note has been deleted, delete this widget *");
            getRealm(context).commitTransaction();
            Helper.updateWidget(currentNote, context, getRealm(context));
        }
        deleteNoteFromDatabase(context, currentNote);
        new Handler().postDelayed(() -> Helper.updateWidget(currentNote, context, getRealm(context)), 3000);
    }

    public static void deleteNoteFromDatabase(Context context, Note currentNote) {
        getRealm(context).beginTransaction();
        currentNote.deleteFromRealm();
        getRealm(context).commitTransaction();
    }


    public static void deleteNotePhotos(Note currentNote, Context context) {
        getRealm(context).beginTransaction();
        RealmResults<Photo> allNotePhotos = getRealm(context).where(Photo.class).equalTo("noteId", currentNote.getNoteId()).findAll();
        for (Photo currentPhoto : allNotePhotos) {
            deleteImage(currentPhoto.getPhotoLocation());
        }
        allNotePhotos.deleteAllFromRealm();
        getRealm(context).commitTransaction();
    }

    public static void deleteChecklist(Note currentNote, Context context) {
        // delete each checklist item's associated data
        // like image, recording, and sublist (if they exist)
        for (CheckListItem checkListItem : currentNote.getChecklist()) {
            deleteChecklistItem(checkListItem, context, true);
        }

        // delete all checklist items
        getRealm(context).beginTransaction();
        currentNote.getChecklist().deleteAllFromRealm();
        getRealm(context).commitTransaction();
    }

    public static void deleteChecklistItems(Note currentNote, Context context, boolean checkedStatus, boolean deleteOnlyContents) {
        // delete each checklist item and all its associated data
        // like image, recording, and sublist (if they exist)
        RealmResults<CheckListItem> checkListItems = RealmSingleton.get(context).where(CheckListItem.class)
                .equalTo("id", currentNote.getNoteId())
                .equalTo("checked", checkedStatus)
                .findAll();
        for (CheckListItem checkListItem : checkListItems) {
            if (checkListItem.isChecked() == checkedStatus)
                deleteChecklistItem(checkListItem, context, deleteOnlyContents);
        }
    }

    public static void deleteRecording(CheckListItem item, Context context) {
        getRealm(context).beginTransaction();
        Helper.deleteFile(item.getAudioPath());
        item.setAudioPath("");
        item.setAudioDuration(0);
        getRealm(context).commitTransaction();
    }

    public static void deleteChecklistItem(CheckListItem item, Context context, boolean deleteOnlyContents) {
        // delete recording if it exists
        if (item.getAudioPath() != null && !item.getAudioPath().isEmpty())
            deleteRecording(item, context);
        // delete sublist if it exits
        if (null != item.getSubChecklist()) {
            if (item.getSubChecklist().size() > 0)
                deleteSublist(item.getSubChecklist(), context);
        }

        getRealm(context).beginTransaction();
        // delete image if it exists
        if (item.getItemImage() != null && !item.getItemImage().isEmpty())
            deleteImage(item.getItemImage());
        if (!deleteOnlyContents) {
            // delete item
            item.deleteFromRealm();
        }
        getRealm(context).commitTransaction();
    }

    public static void deleteImage(String photoPath) {
        File photo = new File(photoPath);
        if (photo.exists())
            photo.delete();
    }

    public static void deleteSublist(RealmList<SubCheckListItem> sublist, Context context) {
        if (sublist.size() > 0) {
            getRealm(context).beginTransaction();
            sublist.deleteAllFromRealm();
            getRealm(context).commitTransaction();
        }
    }

    public static void deleteSublistItem(SubCheckListItem sublistItem, Context context) {
        getRealm(context).beginTransaction();
        try {
            sublistItem.deleteFromRealm();
        } catch (Exception ignore) {}
        getRealm(context).commitTransaction();
    }

    public static void selectAllChecklists(Note currentNote, Context context, boolean status) {
        getRealm(context).beginTransaction();
        for (CheckListItem item : currentNote.getChecklist()) {
            item.setChecked(status);
            if (null != item.getSubChecklist()) {
                if (item.getSubChecklist().size() > 0) {
                    for (SubCheckListItem subItem : item.getSubChecklist())
                        subItem.setChecked(status);
                }
            }
        }
        getRealm(context).commitTransaction();
    }

    // verify that all notes formatted date strings match their millisecond date parameter
    // issue when sorting notes, some millisecond date parameters do not match their date
    public static void verifyDateWithMilli(Context context) {
        getRealm(context).beginTransaction();
        RealmResults<Note> allNotes = getRealm(context).where(Note.class).findAll();
        for (Note currentNote : allNotes) {
            try {
                long lastDateEditedNoteInMilli = Helper.dateToCalender(currentNote.getDateEdited()).getTimeInMillis();
                if (lastDateEditedNoteInMilli != currentNote.getDateEditedMilli()) {
                    currentNote.setDateEditedMilli(lastDateEditedNoteInMilli);
                }
            } catch (Exception e) {
            }
        }
        getRealm(context).commitTransaction();
    }

    public static Note getNote(Context context, int noteId) {
        return getRealm(context).where(Note.class).equalTo("noteId", noteId).findFirst();
    }

    public static int getTextColorBasedOnTheme(Context context, int noteId) {
        Note currentNote = getNote(context, noteId);
        boolean isLightMode = isLightMode(context);
        int primaryTextColor = UiHelper.getColorFromTheme(context, R.attr.primaryTextColor);

        if (currentNote == null) return primaryTextColor;

        if (isLightMode) {
            if (currentNote.getLightTextColor() == 0 || currentNote.getLightTextColor() == -1)
                return primaryTextColor;

            if (currentNote.getLightTextColor() == context.getResources().getColor(R.color.white))
                return primaryTextColor;
        } else {
            if (currentNote.getTextColor() == 0 || currentNote.getTextColor() == -1)
                return primaryTextColor;

            if (currentNote.getTextColor() == context.getResources().getColor(R.color.black))
                return primaryTextColor;
        }

        return isLightMode ? currentNote.getLightTextColor() : currentNote.getTextColor();
    }

    public static void setTextColorBasedOnTheme(Context context, int noteId, int newColor) {
        Note currentNote = getNote(context, noteId);
        boolean isLightMode = isLightMode(context);
        if (isLightMode)
            currentNote.setLightTextColor(newColor);
        else
            currentNote.setTextColor(newColor);
    }

    public static boolean isLightMode(Context context) {
        return getUser(context, "in realm helper").getScreenMode() == User.Mode.Light;
    }

    public static int getNotePin(Context context, int noteId) {
        Note note = getCurrentNote(context, noteId);
        if (note == null) return 0;
        return note.getPinNumber();
    }

    public static User getUser(Context context, String location) {
        User user = getRealm(context).where(User.class).findFirst();
        if (user == null && !getRealm(context).isInTransaction()) return addUser(context);
        return user;
    }

    public static RealmResults<Folder> getAllFolders(Context context) {
        return RealmSingleton.get(context).where(Folder.class).sort("positionInList").findAll();
    }

    public static int getNoteSorting(Context context, int noteId) {
        return getCurrentNote(context, noteId).getSort();
    }

    public static void updateAudioExtensions(Context context) {
        getRealm(context).beginTransaction();
        List<CheckListItem> audioPaths = getRealm(context).copyToRealm(getRealm(context).where(CheckListItem.class)
                .isNotEmpty("audioPath").findAll());
        getRealm(context).commitTransaction();

        for (CheckListItem item : audioPaths) {
            if (item.getAudioPath() != null && !item.getAudioPath().isEmpty()) {
                if (item.getAudioPath().contains(".mp4")) {
                    File originalFile = new File(item.getAudioPath());
                    String newFilename = originalFile.getName().replaceFirst(".mp4", ".mp3"); // Replace the existing extension with ".pdf"
                    File newFile = new File(originalFile.getParent(), newFilename);

                    boolean renamed = originalFile.renameTo(newFile);

                    if (renamed) {
                        Log.d("Here", "Audio path: " + item.getAudioPath() + " was successfully updated");
                        updateAudioPath(context, item, newFilename);
                    } else
                        Log.d("Here", "Audio path: " + item.getAudioPath() + " [ERROR]");
                } else {
                    //Log.d("Here", "Audio path: " + item.getAudioPath() + " is already MP3");
                }
            }
        }

    }

    public static void updateAudioPath(Context context, CheckListItem item, String newPath) {
        getRealm(context).beginTransaction();
        item.setAudioPath(newPath);
        getRealm(context).commitTransaction();
    }

    public static void updateChecklistOrdering(Context context, List<CheckListItem> newChecklist, int noteId) {
        for (CheckListItem checkListItem : newChecklist) {
            CheckListItem item = getCurrentNote(context, noteId).getChecklist().where()
                    .equalTo("id", checkListItem.getId())
                    .equalTo("subListId", checkListItem.getSubListId())
                    .equalTo("text", checkListItem.getText())
                    .findFirst();
            getRealm(context).beginTransaction();
            updateCheckListPosition(item, checkListItem.getPositionInList());
            getRealm(context).commitTransaction();
        }

        RealmResults<CheckListItem> oldChecklist = getCurrentNote(context, noteId).getChecklist().sort("positionInList");
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (CheckListItem checkListItem : oldChecklist) {
            builder
                    .append("(" + checkListItem.getId() + ") ")
                    .append(checkListItem.getText())
                    .append(" @ ")
                    .append(checkListItem.getPositionInList())
                    .append(", ");
        }
        builder.append("]");
        Log.d("Here", builder.toString());
    }

    public static void updateFolderOrdering(Context context, List<Folder> newFolder) {
        for (Folder folder : newFolder) {
            Folder item = getRealm(context).where(Folder.class)
                    .equalTo("id", folder.getId())
                    .equalTo("name", folder.getName())
                    .findFirst();
            getRealm(context).beginTransaction();
            updateFolderPosition(context, item, folder.getPositionInList());
            getRealm(context).commitTransaction();
        }

        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (Folder folder : getRealm(context).where(Folder.class).sort("positionInList").findAll()) {
            builder
                    .append("(" + folder.getId() + ") ")
                    .append(folder.getName())
                    .append(" @ ")
                    .append(folder.getPositionInList())
                    .append(", ");
        }
        builder.append("]");
        Log.d("Here", builder.toString());
    }

    public static void updateCheckListPosition(CheckListItem item, int newPosition) {
        try {
            item.setPositionInList(newPosition);
        } catch (Exception ignored) {}
    }

    public static void updateFolderPosition(Context context, Folder item, int newPosition) {
        try {
            item.setPositionInList(newPosition);
        } catch (Exception e) {
            getRealm(context).cancelTransaction();
        }
    }

    public static Note getCurrentNote(Context context, int noteId) {
        return getRealm(context).where(Note.class).equalTo("noteId", noteId).findFirst();
    }

    public static Folder getCurrentFolder(Context context, int folderId) {
        return getRealm(context).where(Folder.class).equalTo("id", folderId).findFirst();
    }

    public static int getFolderSize(Context context, int folderId) {
        return RealmSingleton.get(context).where(Note.class)
                .equalTo("category", getCurrentFolder(context, folderId).getName())
                .findAll().size();
    }

    public static void lockNote(Context context, int noteId, int pin, String securityWord, boolean isFingerprintAdded) {
        Note currentNote = getCurrentNote(context, noteId);
        getRealm(context).beginTransaction();
        currentNote.setPinNumber(pin);
        currentNote.setSecurityWord(securityWord);
        currentNote.setFingerprint(isFingerprintAdded);
        getRealm(context).commitTransaction();
    }

    // unlock the notes
    public static void unlockNotesInsideFolder(Context context, int folderId) {
        Folder currentFolder = getCurrentFolder(context, folderId);
        if (!currentFolder.isValid()) return;
        RealmResults<Note> notesInsideFolder = RealmSingleton.get(context).where(Note.class)
                .equalTo("category", currentFolder.getName())
                .findAll();

        if (notesInsideFolder.size() > 0) {
            getRealm(context).beginTransaction();
            notesInsideFolder.setInt("pinNumber", 0);
            notesInsideFolder.setString("securityWord", "");
            notesInsideFolder.setBoolean("fingerprint", false);
            getRealm(context).commitTransaction();
        }
    }

    public static void lockNotesInsideFolder(FragmentActivity activity, Context context, int folderId) {
        RealmResults<Note> notesInsideFolder = RealmSingleton.get(context).where(Note.class)
                .equalTo("category", getCurrentFolder(context, folderId).getName())
                .equalTo("pinNumber", 0)
                .findAll();

        RealmResults<Note> lockedNotesInsideFolder = RealmSingleton.get(context).where(Note.class)
                .equalTo("category", getCurrentFolder(context, folderId).getName())
                .greaterThan("pinNumber", 0)
                .findAll();

        if (lockedNotesInsideFolder.size() > 0) {
            LockFolderSheet lockFolderSheet = new LockFolderSheet(folderId, notesInsideFolder, lockedNotesInsideFolder, null);
            lockFolderSheet.show(activity.getSupportFragmentManager(), lockFolderSheet.getTag());
        } else {
            if (notesInsideFolder.size() > 0) {
                getRealm(context).beginTransaction();
                notesInsideFolder.setInt("pinNumber", getCurrentFolder(context, folderId).getPin());
                notesInsideFolder.setString("securityWord", getCurrentFolder(context, folderId).getSecurityWord());
                notesInsideFolder.setBoolean("fingerprint", getCurrentFolder(context, folderId).isFingerprintAdded());
                getRealm(context).commitTransaction();
            }
        }
    }

    public static boolean isNoteWidget(Context context, int noteId) {
        if (getCurrentNote(context, noteId) == null) return false;
        return getCurrentNote(context, noteId).getWidgetId() > 0;
    }

    public static int getNoteIdUsingTitle(Context context, String target) {
        if (target == null || target.isEmpty()) return 0;
        Note queryNotes = getRealm(context).where(Note.class)
                .contains("title", target)
                .findFirst();
        return queryNotes == null ? 0 : queryNotes.getNoteId();
    }

    public static String getTitleUsingId(Context context, int id) {
        Note queryNotes = getRealm(context).where(Note.class)
                .equalTo("noteId", id)
                .findFirst();
        return queryNotes == null ? "" : queryNotes.getTitle();
    }

    public static Backup getBackup(Context context, String backupFilename) {
        return RealmHelper.getRealm(context).where(Backup.class).equalTo("fileName", backupFilename).findFirst();
    }

    public static void setUserTextSize(Context context, int newSize) {
        getRealm(context).beginTransaction();
        getUser(context, "in space").setTextSize(newSize);
        getRealm(context).commitTransaction();
    }

    public static int getUserTextSize(Context context) {
        int defaultTextSize = 20;
        if (getUser(context, "in space") == null) return defaultTextSize;
        int userTextSize = getUser(context, "in space").getTextSize();
        return userTextSize == 0 ? defaultTextSize : userTextSize;
    }

    public static boolean isRealmInstancesClosed() {
        if (RealmSingleton.getOnlyRealm() == null || RealmSingleton.getOnlyRealm().isClosed())
            return true;
        else
            RealmSingleton.getOnlyRealm().close();
        return Realm.getLocalInstanceCount(RealmSingleton.getOnlyRealm().getConfiguration()) == 0;
    }

    public static boolean deleteRealmDatabase(Activity activity) {
        if (!RealmHelper.isRealmInstancesClosed()) {
            Helper.showMessage(activity, "Restore Error", "" +
                    "Issue deleting database...try again", MotionToast.TOAST_ERROR);
            return false;
        }
        return deleteDatabase();
    }

    private static boolean deleteDatabase() {
        if (RealmSingleton.getOnlyRealm() != null) {
            return Realm.deleteRealm(RealmSingleton.getOnlyRealm().getConfiguration());
        }
        return false;
    }

    private static User addUser(Context context) {
        int uniqueId = UniqueIDGenerator.generateUniqueID();
        User user = new User(uniqueId);
        Realm realm = getRealm(context);
        realm.beginTransaction();
        realm.insert(user);
        realm.commitTransaction();
        return getRealm(context).where(User.class).findFirst();
    }

    public static ArrayList<String> getAllImagePaths(Context context) {
        RealmResults<Photo> allNotePhotos = getRealm(context).where(Photo.class).findAll();
        RealmResults<Note> allMarkdownPhotos = getRealm(context)
                .where(Note.class)
                .contains("note", "<img src=")
                .findAll();
        ArrayList<String> allPhotoPaths = new ArrayList<>();
        // images in note images and checklist images
        for (Photo image : allNotePhotos) allPhotoPaths.add(image.getPhotoLocation());
        // images in just note in markdown
        for (Note note : allMarkdownPhotos) {
            for (String fileName : Helper.extractFileNames(note.getNote())) {
                allPhotoPaths.add(fileName);
            }
        }
        return allPhotoPaths;
    }

    public static ArrayList<String> getAllAudioPaths(Context context) {
        RealmResults<CheckListItem> allChecklistItemsAudioPaths = getRealm(context).where(CheckListItem.class)
                .isNotNull("audioPath")
                .isNotEmpty("audioPath").findAll();
        ArrayList<String> allAudioPaths = new ArrayList<>();
        for (CheckListItem checkListItem : allChecklistItemsAudioPaths)
            allAudioPaths.add(checkListItem.getAudioPath());
        return allAudioPaths;
    }

    public static RealmResults<Note> getDefaultNotesSorted(Context context){
        String dateType = Helper.getPreference(context, "_dateType");
        boolean oldestToNewest = Helper.getBooleanPreference(context, "_oldestToNewest");
        boolean newestToOldest = Helper.getBooleanPreference(context, "_newestToOldest");

        boolean aToZ = Helper.getBooleanPreference(context, "_aToZ");
        boolean zToA = Helper.getBooleanPreference(context, "_zToA");

        RealmResults<Note> notes = null;

        if (dateType != null || aToZ || zToA) {
            if (oldestToNewest) {
                notes = getRealm(context).where(Note.class)
                        .equalTo("archived", false)
                        .equalTo("trash", false)
                        .sort(dateType, Sort.ASCENDING).findAll();

                if (getUser(context, "RealmHelper").isShowFolderNotes())
                    notes = notes.where().equalTo("category", "none").findAll();
            } else if (newestToOldest) {
                notes = getRealm(context).where(Note.class)
                        .equalTo("archived", false)
                        .equalTo("trash", false)
                        .sort(dateType, Sort.DESCENDING).findAll();

                if (getUser(context, "RealmHelper").isShowFolderNotes())
                    notes = notes.where().equalTo("category", "none").findAll();
            } else if (aToZ) {
                notes = getRealm(context).where(Note.class)
                        .equalTo("archived", false)
                        .equalTo("trash", false)
                        .sort("title").findAll();

                if (getUser(context, "RealmHelper").isShowFolderNotes())
                    notes = notes.where().equalTo("category", "none").findAll();
            } else if (zToA) {
                notes = getRealm(context).where(Note.class)
                        .equalTo("archived", false)
                        .equalTo("trash", false)
                        .sort("title", Sort.DESCENDING).findAll();

                if (getUser(context, "RealmHelper").isShowFolderNotes())
                    notes = notes.where().equalTo("category", "none").findAll();
            }
            notes = notes.where().sort("pin", Sort.DESCENDING).findAll();
        }
        return notes;
    }

    public static RealmResults<Note> sortNotes(Context context, RealmResults<Note> oldNotes){
        String dateType = Helper.getPreference(context, "_dateType");
        boolean oldestToNewest = Helper.getBooleanPreference(context, "_oldestToNewest");
        boolean newestToOldest = Helper.getBooleanPreference(context, "_newestToOldest");

        boolean aToZ = Helper.getBooleanPreference(context, "_aToZ");
        boolean zToA = Helper.getBooleanPreference(context, "_zToA");

        RealmResults<Note> notes = null;

        if (dateType != null || aToZ || zToA) {
            if (oldestToNewest) {
                notes = oldNotes.where()
                        .sort(dateType, Sort.ASCENDING).findAll();
            } else if (newestToOldest) {
                notes = oldNotes.where()
                        .sort(dateType, Sort.DESCENDING).findAll();
            } else if (aToZ) {
                notes = oldNotes.where()
                        .sort("title").findAll();
            } else if (zToA) {
                notes = oldNotes.where()
                        .sort("title", Sort.DESCENDING).findAll();
            }
            notes = notes.where().sort("pin", Sort.DESCENDING).findAll();
        }
        return (notes == null) ? oldNotes : notes;
    }
}
