package com.akapps.dailynote.classes.data;

import io.realm.RealmObject;

public class Photo extends RealmObject {

    private int noteId;
    private String photoLocation;

    public Photo(){}

    public Photo(int noteId, String photoLocation) {
        this.noteId = noteId;
        this.photoLocation = photoLocation;
    }

    public int getNoteId() {
        return noteId;
    }

    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }

    public String getPhotoLocation() {
        return photoLocation;
    }
}
