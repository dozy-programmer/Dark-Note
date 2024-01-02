package com.akapps.dailynote.classes.helpers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.Photo;
import com.akapps.dailynote.classes.data.SubCheckListItem;
import com.akapps.dailynote.classes.data.User;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;

public class RealmHelper {

    public static Realm getRealm(Context context){
        return RealmSingleton.getInstance(context);
    }

    public static void deleteNote(Context context, int noteID){
        // get a note
        Note currentNote;
        getRealm(context).beginTransaction();
        currentNote = getRealm(context).where(Note.class).equalTo("noteId", noteID).findFirst();
        getRealm(context).commitTransaction();

        // delete checklist, check list item photos, and sublists
        if(currentNote.isCheckList())
            deleteChecklist(currentNote, context);

        // deletes photos if they exist
        deleteNotePhotos(currentNote, context);

        if(!currentNote.getReminderDateTime().isEmpty())
            Helper.cancelNotification(context, currentNote.getNoteId());

        // deletes note
        getRealm(context).beginTransaction();
        if(currentNote.getWidgetId() > 0) {
            currentNote.setPinNumber(0);
            currentNote.setTitle("Delete Me");
            currentNote.setNote("* Note has been deleted, delete this widget *");
            Helper.updateWidget(currentNote, context, RealmSingleton.getInstance(context));
        }
        getRealm(context).commitTransaction();
        deleteNoteFromDatabase(context, currentNote);
    }

    public static void deleteNoteFromDatabase(Context context, Note currentNote){
        getRealm(context).beginTransaction();
        currentNote.deleteFromRealm();
        getRealm(context).commitTransaction();
    }


    public static void deleteNotePhotos(Note currentNote, Context context){
        getRealm(context).beginTransaction();
        RealmResults<Photo> allNotePhotos = getRealm(context).where(Photo.class).equalTo("noteId", currentNote.getNoteId()).findAll();
        for(Photo currentPhoto: allNotePhotos) {
            deleteImage(currentPhoto.getPhotoLocation());
        }
        allNotePhotos.deleteAllFromRealm();
        getRealm(context).commitTransaction();
    }

    public static void deleteChecklist(Note currentNote, Context context){
        // delete each checklist item and all its associated data
        // like image, recording, and sublist (if they exist)
        for (CheckListItem checkListItem : currentNote.getChecklist())
            deleteChecklistItem(checkListItem, context, true);

        // make sure all checklist items are deleted
        getRealm(context).beginTransaction();
        currentNote.getChecklist().deleteAllFromRealm();
        getRealm(context).commitTransaction();
    }

    public static void deleteRecording(CheckListItem item, Context context){
        getRealm(context).beginTransaction();
        Helper.deleteFile(item.getAudioPath());
        item.setAudioPath("");
        item.setAudioDuration(0);
        getRealm(context).commitTransaction();
    }

    public static void deleteChecklistItem(CheckListItem item, Context context, boolean deleteOnlyContents){
        // delete recording if it exists
        if(item.getAudioPath() != null && !item.getAudioPath().isEmpty())
            deleteRecording(item, context);
        // delete sublist if it exits
        if(null != item.getSubChecklist()) {
            if(item.getSubChecklist().size() > 0)
                deleteSublist(item.getSubChecklist(), context);
        }

        getRealm(context).beginTransaction();
        // delete image if it exists
        if(item.getItemImage() != null && !item.getItemImage().isEmpty())
            deleteImage(item.getItemImage());
        if(!deleteOnlyContents) {
            // delete item
            item.deleteFromRealm();
        }
        getRealm(context).commitTransaction();
    }

    public static void deleteImage(String photoPath){
        File photo = new File(photoPath);
        if (photo.exists())
            photo.delete();
    }

    public static void deleteSublist(RealmList<SubCheckListItem> sublist, Context context){
        if(sublist.size() > 0) {
            getRealm(context).beginTransaction();
            sublist.deleteAllFromRealm();
            getRealm(context).commitTransaction();
        }
    }

    public static void deleteSublistItem(SubCheckListItem sublistItem, Context context){
        getRealm(context).beginTransaction();
        sublistItem.deleteFromRealm();
        getRealm(context).commitTransaction();
    }

    public static void selectAllChecklists(Note currentNote, Context context, boolean status){
        getRealm(context).beginTransaction();
        for(CheckListItem item: currentNote.getChecklist()) {
            item.setChecked(status);
            if(null != item.getSubChecklist()) {
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
    public static void verifyDateWithMilli(Context context){
        getRealm(context).beginTransaction();
        RealmResults<Note> allNotes = getRealm(context).where(Note.class).findAll();
        for (Note currentNote : allNotes) {
            try {
                long lastDateEditedNoteInMilli = Helper.dateToCalender(currentNote.getDateEdited()).getTimeInMillis();
                if (lastDateEditedNoteInMilli != currentNote.getDateEditedMilli()) {
                    currentNote.setDateEditedMilli(lastDateEditedNoteInMilli);
                }
            } catch (Exception e){}
        }
        getRealm(context).commitTransaction();
    }

    public static Note getNote(Context context, int noteId){
        return getRealm(context).where(Note.class).equalTo("noteId", noteId).findFirst();
    }

    public static int getTextColorBasedOnTheme(Context context, int noteId){
        Note currentNote = getNote(context, noteId);
        boolean isLightMode = isLightMode(context);
        int primaryTextColor = UiHelper.getColorFromTheme(context, R.attr.primaryTextColor);

        if(currentNote == null) return primaryTextColor;

        if(isLightMode) {
            if(currentNote.getLightTextColor() == 0 || currentNote.getLightTextColor() == -1) return primaryTextColor;

            if(currentNote.getLightTextColor() == context.getColor(R.color.white)) return primaryTextColor;
        }
        else {
            if (currentNote.getTextColor() == 0 || currentNote.getTextColor() == -1) return primaryTextColor;

            if(currentNote.getTextColor() == context.getColor(R.color.black)) return primaryTextColor;
        }

        return isLightMode ? currentNote.getLightTextColor() : currentNote.getTextColor();
    }

    public static void setTextColorBasedOnTheme(Context context, int noteId, int newColor){
        Note currentNote = getNote(context, noteId);
        boolean isLightMode = isLightMode(context);
        if(isLightMode)
            currentNote.setLightTextColor(newColor);
        else
            currentNote.setTextColor(newColor);
    }

    public static boolean isLightMode(Context context){
        return getUser(context, "in realm helper").getScreenMode() == User.Mode.Light;
    }

    public static int getNotePin(Context context, int noteId){
        Note note = getCurrentNote(context, noteId);
        if(note == null) return 0;
        return note.getPinNumber();
    }

    public static User getUser(Context context, String location){
        User user = getRealm(context).where(User.class).findFirst();
        if(user == null) return addUser(context);
        return user;
    }

    public static Note getCurrentNote(Context context, int noteId) {
        return getRealm(context).where(Note.class).equalTo("noteId", noteId).findFirst();
    }

    public static boolean isNoteWidget(Context context, int noteId){
        if(getCurrentNote(context, noteId) == null) return false;
        return getCurrentNote(context, noteId).getWidgetId() > 0;
    }

    public static int getNoteIdUsingTitle(Context context, String target){
        if(target == null || target.isEmpty()) return 0;
        Note queryNotes = getRealm(context).where(Note.class)
                .contains("title", target)
                .findFirst();
        return queryNotes == null ? 0 : queryNotes.getNoteId();
    }

    public static String getTitleUsingId(Context context, int id){
        Note queryNotes = getRealm(context).where(Note.class)
                .equalTo("noteId", id)
                .findFirst();
        return queryNotes == null ? "" : queryNotes.getTitle();
    }

    private static User addUser(Context context){
        int uniqueId = UniqueIDGenerator.generateUniqueID();
        User user = new User(uniqueId);
        Realm realm = getRealm(context);
        realm.beginTransaction();
        realm.insert(user);
        realm.commitTransaction();
        AppAnalytics.logNewUser(context, uniqueId);
        return getRealm(context).where(User.class).findFirst();
    }

}
