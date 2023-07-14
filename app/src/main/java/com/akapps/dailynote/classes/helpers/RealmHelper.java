package com.akapps.dailynote.classes.helpers;

import android.content.Context;

import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.Photo;
import com.akapps.dailynote.classes.data.SubCheckListItem;
import java.io.File;
import io.realm.Realm;
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

    public static void updateNoteReminder(Context context, int noteID){
        getRealm(context).beginTransaction();
        Note currentNote = getRealm(context).where(Note.class).equalTo("noteId", noteID).findFirst();
        if(currentNote != null && currentNote.getReminderDateTime() != null)
            currentNote.setReminderDateTime("");
        getRealm(context).commitTransaction();
    }
}
