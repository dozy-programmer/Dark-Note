package com.akapps.dailynote.classes.helpers;

import android.util.Log;

import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.Photo;
import com.akapps.dailynote.classes.data.SubCheckListItem;
import java.io.File;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class RealmHelper {

    public static void deleteNote(int noteID){
        // get a note
        Note currentNote;
        try(Realm realm = Realm.getDefaultInstance()) {
            currentNote = realm.where(Note.class).equalTo("noteId", noteID).findFirst();
        }

        // delete checklist, check list item photos, and sublists
        if(currentNote.isCheckList())
            deleteChecklist(currentNote);

        // deletes photos if they exist
        deleteNotePhotos(currentNote);

        // deletes note
        try(Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(tRealm -> {
                currentNote.deleteFromRealm();
            });
        }
    }

    public static void deleteNotePhotos(Note currentNote){
        try(Realm realm = Realm.getDefaultInstance()) {
            RealmResults<Photo> allNotePhotos = realm.where(Photo.class).equalTo("noteId", currentNote.getNoteId()).findAll();
            for(Photo currentPhoto: allNotePhotos) {
                deleteImage(currentPhoto.getPhotoLocation());
            }
            realm.executeTransaction(tRealm -> {
                allNotePhotos.deleteAllFromRealm();
            });
        }
    }

    public static void deleteChecklist(Note currentNote){
        // delete each checklist item and all its associated data
        // like image, recording, and sublist (if they exist)
        for (CheckListItem checkListItem : currentNote.getChecklist())
            deleteChecklistItem(checkListItem, true);

        // make sure all checklist items are deleted
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(tRealm -> {
                currentNote.getChecklist().deleteAllFromRealm();
            });
        }
    }

    public static void deleteRecording(CheckListItem item){
        try(Realm realm = Realm.getDefaultInstance()){
            realm.executeTransaction(tRealm -> {
                Helper.deleteFile(item.getAudioPath());
                item.setAudioPath("");
                item.setAudioDuration(0);
            });
        }
    }

    public static void deleteChecklistItem(CheckListItem item, boolean deleteOnlyContents){
        // delete recording if it exists
        if(item.getAudioPath() != null && !item.getAudioPath().isEmpty())
            deleteRecording(item);
        // delete sublist if it exits
        if(null != item.getSubChecklist()) {
            if(item.getSubChecklist().size() > 0)
                deleteSublist(item.getSubChecklist());
        }

        try(Realm realm = Realm.getDefaultInstance()){
                realm.executeTransaction(tRealm -> {
                    // delete image if it exists
                    if(item.getItemImage() != null && !item.getItemImage().isEmpty())
                        deleteImage(item.getItemImage());
                    if(!deleteOnlyContents) {
                        // delete item
                        item.deleteFromRealm();
                    }
                });
        }
    }

    public static void deleteImage(String photoPath){
        File photo = new File(photoPath);
        if (photo.exists())
            photo.delete();
    }

    public static void deleteSublist(RealmList<SubCheckListItem> sublist){
        if(sublist.size() > 0) {
            try (Realm realm = Realm.getDefaultInstance()) {
                realm.executeTransaction(tRealm -> {
                    sublist.deleteAllFromRealm();
                });
            }
        }
    }

    public static void deleteSublistItem(SubCheckListItem sublistItem){
        try(Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(tRealm -> {
                sublistItem.deleteFromRealm();
            });
        }
    }

    public static void selectAllChecklists(Note currentNote, boolean status){
        try(Realm realm = Realm.getDefaultInstance()) {
            for(CheckListItem item: currentNote.getChecklist()) {
                realm.executeTransaction(tRealm -> {
                    item.setChecked(status);
                });
                if(null != item.getSubChecklist()) {
                    if (item.getSubChecklist().size() > 0)
                        for (SubCheckListItem subItem : item.getSubChecklist())
                            realm.executeTransaction(tRealm -> {
                                subItem.setChecked(status);
                            });
                }
            }
        }
    }

    // verify that all notes formatted date strings match their millisecond date parameter
    // issue when sorting notes, some millisecond date parameters do not match their date
    public static void verifyDateWithMilli(){
        try(Realm realm = Realm.getDefaultInstance()) {
            RealmResults<Note> allNotes = realm.where(Note.class).findAll();
            for (Note currentNote : allNotes) {
                    try {
                        long lastDateEditedNoteInMilli = Helper.dateToCalender(currentNote.getDateEdited()).getTimeInMillis();
                        if (lastDateEditedNoteInMilli != currentNote.getDateEditedMilli()) {
                            realm.executeTransaction(tRealm -> {
                                currentNote.setDateEditedMilli(lastDateEditedNoteInMilli);
                            });
                        }
                    } catch (Exception e){
                        continue;
                    }
            }
        }
    }
}
