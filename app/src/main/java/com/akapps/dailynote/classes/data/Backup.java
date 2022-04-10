package com.akapps.dailynote.classes.data;

import io.realm.RealmObject;

public class Backup extends RealmObject {
    private int userId;
    private String fileName;
    private String upLoadTime;
    private int fileSize;

    public Backup(){}

    public Backup(int userId, String fileName, String upLoadTime, int fileSize) {
        this.userId = userId;
        this.fileName = fileName;
        this.upLoadTime = upLoadTime;
        this.fileSize = fileSize;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUpLoadTime() {
        return upLoadTime;
    }

    public void setUpLoadTime(String upLoadTime) {
        this.upLoadTime = upLoadTime;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }
}
