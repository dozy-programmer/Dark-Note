package com.akapps.dailynote.classes.data;

import com.akapps.dailynote.classes.helpers.Helper;

import io.realm.RealmList;
import io.realm.RealmObject;

public class CheckListItem extends RealmObject {

    private int id;
    private String text;
    private boolean checked;
    private int positionInList;
    private long lastCheckedDate;
    private int subListId;
    private RealmList<SubCheckListItem> subChecklist;
    private String itemImage;

    public CheckListItem(){}

    public CheckListItem(String text, boolean checked, int id, int positionInList, int subListId) {
        this.text = text;
        this.checked = checked;
        this.id = id;
        this.positionInList = positionInList;
        lastCheckedDate = Helper.dateToCalender(Helper.getCurrentDate()).getTimeInMillis();
        subChecklist = new RealmList<>();
        this.subListId = subListId;
        itemImage = "";
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

    public RealmList<SubCheckListItem> getSubChecklist() {
        return subChecklist;
    }

    public void setSubChecklist(RealmList<SubCheckListItem> subChecklist) {
        this.subChecklist = subChecklist;
    }

    public int getSubListId() {
        return subListId;
    }

    public void setSubListId(int subListId) {
        this.subListId = subListId;
    }

    public String getItemImage() {
        return itemImage;
    }

    public void setItemImage(String itemImage) {
        this.itemImage = itemImage;
    }
}
