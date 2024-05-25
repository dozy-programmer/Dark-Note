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
    private String dateCreated;
    private RealmList<SubCheckListItem> subChecklist;
    private boolean isSublistExpanded;
    private String itemImage;

    // audio recording data
    private String audioPath;
    private int audioDuration;

    private Place place;

    private int redirectToOtherNote;

    private int visibility;

    public CheckListItem(){}

    public CheckListItem(String text, boolean checked, int id, int positionInList, int subListId,
                         String dateCreated, Place place, int redirectToOtherNote) {
        this.text = text;
        this.checked = checked;
        this.id = id;
        this.positionInList = positionInList;
        this.lastCheckedDate = Helper.dateToCalender(Helper.getCurrentDate()).getTimeInMillis();
        this.subListId = subListId;
        this.itemImage = audioPath = "";
        this.dateCreated = dateCreated;
        this.subChecklist = new RealmList<>();
        audioDuration = 0;
        this.place = place;
        this.redirectToOtherNote = redirectToOtherNote;
        visibility = 0;
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

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public int getAudioDuration() {
        return audioDuration;
    }

    public void setAudioDuration(int audioDuration) {
        this.audioDuration = audioDuration;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public boolean isSublistExpanded() {
        return isSublistExpanded;
    }

    public void setSublistExpanded(boolean sublistExpanded) {
        isSublistExpanded = sublistExpanded;
    }

    public int getRedirectToOtherNote() {
        return redirectToOtherNote;
    }

    public void setRedirectToOtherNote(int redirectToOtherNote) {
        this.redirectToOtherNote = redirectToOtherNote;
    }

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }
}
