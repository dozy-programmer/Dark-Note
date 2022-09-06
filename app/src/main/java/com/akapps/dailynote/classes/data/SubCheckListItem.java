package com.akapps.dailynote.classes.data;

import io.realm.RealmObject;

public class SubCheckListItem extends RealmObject {

    private int id;
    private String text;
    private boolean checked;
    private int positionInList;
    private String dateCreated;

    public SubCheckListItem(){}

    public SubCheckListItem(String text, boolean checked, int id, int positionInList, String dateCreated) {
        this.text = text;
        this.checked = checked;
        this.id = id;
        this.positionInList = positionInList;
        this.dateCreated = dateCreated;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPositionInList() {
        return positionInList;
    }

    public void setPositionInList(int positionInList) {
        this.positionInList = positionInList;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }
}
