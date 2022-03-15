package com.akapps.dailynote.classes.helpers;

import com.akapps.dailynote.classes.data.User;

public class AppData{
    private static AppData appData;
    public boolean isLightMode;
    public User user;

    private AppData() { }

    public static AppData getAppData() {
        //instantiate a new CustomerLab if we didn't instantiate one yet
        if (appData == null) {
            appData = new AppData();
        }
        return appData;
    }

    public void setUser(User user){
        this.user = user;
    }

    public void setLightMode(boolean isLightMode){
        this.isLightMode = isLightMode;
    }
}