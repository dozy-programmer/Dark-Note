package com.akapps.dailynote.classes.helpers;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

import io.realm.Realm;

/**
 * Implement this in the future for certain crashes maybe so we can save the state of the app.
 */
public class AppAnalytics {
    private static FirebaseAnalytics mFirebaseAnalytics;

    private AppAnalytics() {
        // Private constructor to prevent instantiation
    }

    public static FirebaseAnalytics get(Context context) {
        if (mFirebaseAnalytics == null) {
            // initialize analytics
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        }
        return mFirebaseAnalytics;
    }

    public static void logNewUser(Context context, int uniqueId){
        if(mFirebaseAnalytics == null) get(context);
        mFirebaseAnalytics.setUserId(String.valueOf(uniqueId));
    }

    public static void logEvent(Context context, String itemId, String locationInApp, String event){
        if(mFirebaseAnalytics == null) get(context);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, locationInApp);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, event);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

}
