package com.akapps.dailynote.activity;

import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.fragments.notes;

public class Homepage extends FragmentActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage_screen);

        if(savedInstanceState == null)
            openFragment();
    }

    private void openFragment(){
        getSupportFragmentManager().beginTransaction().add(android.R.id.content, new notes()).commit();
    }
}
