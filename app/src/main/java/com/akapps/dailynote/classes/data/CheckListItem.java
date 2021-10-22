package com.akapps.dailynote.classes.data;

import io.realm.RealmObject;

public class CheckListItem extends RealmObject {

    private int id;
    private String text;
    private boolean checked;
    private int positionInList;

    public CheckListItem(){}

    public CheckListItem(String text, boolean checked, int id, int positionInList) {
        this.text = text;
        this.checked = checked;
        this.id = id;
        this.positionInList = positionInList;
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
}
