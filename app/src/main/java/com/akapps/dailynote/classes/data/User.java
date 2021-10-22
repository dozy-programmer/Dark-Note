package com.akapps.dailynote.classes.data;

import io.realm.RealmObject;

public class User extends RealmObject {

    private int userId;
    private boolean proUser;

    // backup data
    private int textSize;
    private String liveNoteAutoComplete;
    private String categories;

    private String layoutSelected;
    private boolean showPreview;
    private String lastUpdated;
    private boolean backUpOnLaunch;
    private String backUpLocation;

    private boolean backupReminder;
    private int backReminderOccurrence;
    private String startingDate;

    public User() {}

    public User(int userId) {
        this.userId = userId;
        proUser = backUpOnLaunch =  backupReminder = false;
        textSize = 0;
        liveNoteAutoComplete = categories = startingDate = "";
        layoutSelected = "stag";
        showPreview = true;
        lastUpdated = backUpLocation = "";
        backReminderOccurrence = 1;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public boolean isProUser() {
        return proUser;
    }

    public void setProUser(boolean proUser) {
        this.proUser = proUser;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public String getLiveNoteAutoComplete() {
        return liveNoteAutoComplete;
    }

    public void setLiveNoteAutoComplete(String liveNoteAutoComplete) {
        this.liveNoteAutoComplete = liveNoteAutoComplete;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public String getLayoutSelected() {
        return layoutSelected;
    }

    public void setLayoutSelected(String layoutSelected) {
        this.layoutSelected = layoutSelected;
    }

    public boolean isShowPreview() {
        return showPreview;
    }

    public void setShowPreview(boolean showPreview) {
        this.showPreview = showPreview;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isBackUpOnLaunch() {
        return backUpOnLaunch;
    }

    public void setBackUpOnLaunch(boolean backUpOnLaunch) {
        this.backUpOnLaunch = backUpOnLaunch;
    }

    public String getBackUpLocation() {
        return backUpLocation;
    }

    public void setBackUpLocation(String backUpLocation) {
        this.backUpLocation = backUpLocation;
    }

    public boolean isBackupReminder() {
        return backupReminder;
    }

    public void setBackupReminder(boolean backupReminder) {
        this.backupReminder = backupReminder;
    }

    public int getBackReminderOccurrence() {
        return backReminderOccurrence;
    }

    public void setBackReminderOccurrence(int backReminderOccurrence) {
        this.backReminderOccurrence = backReminderOccurrence;
    }

    public String getStartingDate() {
        return startingDate;
    }

    public void setStartingDate(String startingDate) {
        this.startingDate = startingDate;
    }
}
