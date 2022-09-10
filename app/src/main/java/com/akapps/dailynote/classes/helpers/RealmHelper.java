package com.akapps.dailynote.classes.helpers;

import android.content.Context;
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

        Log.d("Here", "Note title is " + currentNote.getTitle());

        // delete checklist, check list item photos, and sublists
        deleteChecklist(currentNote);

        // deletes photos if they exist
        deleteNotePhotos(currentNote);

        // deletes note
        try(Realm realm = Realm.getDefaultInstance()) {
            Log.d("Here", "Note is deleted!");
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
                Log.d("Here", "< -- Photo deleted -- >");
            }
            realm.executeTransaction(tRealm -> {
                Log.d("Here", "< -- Note photos deleted -- >");
                allNotePhotos.deleteAllFromRealm();
            });
        }
    }

    public static void deleteChecklist(Note currentNote){
        for(CheckListItem checkListItem: currentNote.getChecklist()){
            if(null != checkListItem.getItemImage())
                if(!checkListItem.getItemImage().isEmpty())
                    deleteImage(checkListItem.getItemImage());

            deleteSublist(checkListItem.getSubChecklist());
        }

        try(Realm realm = Realm.getDefaultInstance()){
            realm.executeTransaction(tRealm -> {
                Log.d("Here", "< -- Checklist deleted -- >");
                currentNote.getChecklist().deleteAllFromRealm();
            });
        }
    }

    public static void deleteChecklistItem(CheckListItem item){
        if(null != item.getSubChecklist()) {
            if(item.getSubChecklist().size() > 0)
                deleteSublist(item.getSubChecklist());
            Log.d("Here", "Checklist --> " + item.getText());
        }

        try(Realm realm = Realm.getDefaultInstance()){
                realm.executeTransaction(tRealm -> {
                    if(!item.getItemImage().isEmpty())
                        deleteImage(item.getItemImage());
                    item.deleteFromRealm();
                });
        }
    }

    public static void deleteImage(String photoPath){
        File photo = new File(photoPath);
        if (photo.exists()) {
            photo.delete();
            Log.d("Here", "Photo --> " + photoPath);
        }
    }

    public static void deleteSublist(RealmList<SubCheckListItem> sublist){
        if(sublist.size() > 0) {
            try (Realm realm = Realm.getDefaultInstance()) {
                realm.executeTransaction(tRealm -> {
                    Log.d("Here", "Sublists - checklist --> " + " ~" + sublist.size() + " deleted");
                    sublist.deleteAllFromRealm();
                });
            }
        }
    }

    public static void deleteSublistItem(SubCheckListItem sublistItem){
        try(Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(tRealm -> {
                Log.d("Here", "Sublist -> " + sublistItem.getText());
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
                if(null != item.getSubChecklist())
                    if(item.getSubChecklist().size() > 0)
                        for(SubCheckListItem subItem: item.getSubChecklist())
                            realm.executeTransaction(tRealm -> {
                                subItem.setChecked(status);
                            });
            }
        }
    }
}
