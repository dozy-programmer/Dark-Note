package com.akapps.dailynote.classes.data;

import io.realm.RealmObject;

public class User extends RealmObject {

    private int userId;
    private boolean proUser;

    private String layoutSelected;

    // backup data
    private int textSize;

    // adding
    private int titleLines;
    private int contentLines;
    private boolean openFoldersOnStart;

    private boolean showPreview;

    private String email;

    private String lastUpload;

    public User() {}

    public User(int userId) {
        this.userId = userId;
        proUser = openFoldersOnStart = false;
        textSize = 0;
        layoutSelected = "stag";
        showPreview = true;
        titleLines = contentLines = 3;
        email = "";
        lastUpload = "";
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

    public int getTitleLines() {
        return titleLines;
    }

    public void setTitleLines(int titleLines) {
        this.titleLines = titleLines;
    }

    public int getContentLines() {
        return contentLines;
    }

    public void setContentLines(int contentLines) {
        this.contentLines = contentLines;
    }

    public boolean isOpenFoldersOnStart() {
        return openFoldersOnStart;
    }

    public void setOpenFoldersOnStart(boolean openFoldersOnStart) {
        this.openFoldersOnStart = openFoldersOnStart;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastUpload() {
        return lastUpload;
    }

    public void setLastUpload(String lastUpload) {
        this.lastUpload = lastUpload;
    }
}
