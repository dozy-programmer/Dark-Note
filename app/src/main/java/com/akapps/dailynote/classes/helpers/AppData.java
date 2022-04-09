package com.akapps.dailynote.classes.helpers;

import com.akapps.dailynote.classes.data.User;

public class AppData{
    private static AppData appData;
    public boolean isLightMode;
    public static boolean isAppFirstStarted;

    private AppData() { }

    public static AppData getAppData() {
        //instantiate a new CustomerLab if we didn't instantiate one yet
        if (appData == null) {
            isAppFirstStarted = true;
            appData = new AppData();
        }
        return appData;
    }

    public void setLightMode(boolean isLightMode){
        this.isLightMode = isLightMode;
    }
}