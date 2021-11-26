package com.akapps.dailynote.classes.data;

import com.akapps.dailynote.classes.helpers.Helper;

import io.realm.RealmObject;

public class CheckListItem extends RealmObject {

    private int id;
    private String text;
    private boolean checked;
    private int positionInList;

    private long lastCheckedDate;

    public CheckListItem(){}

    public CheckListItem(String text, boolean checked, int id, int positionInList) {
        this.text = text;
        this.checked = checked;
        this.id = id;
        this.positionInList = positionInList;
        lastCheckedDate = Helper.dateToCalender(Helper.getCurrentDate()).getTimeInMillis();
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

    public long getLastCheckedDate() {
        return lastCheckedDate;
    }

    public void setLastCheckedDate(long lastCheckedDate) {
        this.lastCheckedDate = lastCheckedDate;
    }
}
