package com.akapps.dailynote.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.fragment.app.FragmentActivity;
import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.fragments.notes;

public class Homepage extends FragmentActivity{

    private int isAppStartedMain = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage_screen);

        int isAppStart = getIntent().getIntExtra("app_started", 0);

        if (savedInstanceState != null)
            isAppStartedMain = savedInstanceState.getInt("app_started");
        else
            isAppStartedMain = 0;

        // means that we are going from settings screen to Homepage, therefore Category screen
        // should not be shown automatically due to user settings
        if(isAppStart == 2)
            Helper.saveBooleanPreference(this, true, "app_started");
        else if(isAppStartedMain != 0)
            Helper.saveBooleanPreference(this, true, "app_started");
        else
            Helper.saveBooleanPreference(this, false, "app_started");

        openFragment();
    }

    @Override
    public void onSaveInstanceState (Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("app_started", ++isAppStartedMain);
    }

    private void openFragment(){
        getSupportFragmentManager().beginTransaction().add(android.R.id.content, new notes()).commit();
    }
}
