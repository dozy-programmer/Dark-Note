package com.akapps.dailynote.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.akapps.dailynote.classes.other.AppWidget;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;

public class WidgetConfigureActivity extends Activity {

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private static final String PREFS_NAME = "AppWidget";
    private static final String PREF_PREFIX_KEY = "appwidget";

    private ArrayList<Note> allNotes;
    private HashMap<String, Integer> allHashMapNotes;
    private ArrayList<String> allArrayListNotes;
    private boolean isLightMode;

    // layout
    private TextInputLayout searchNoteLayout;
    private TextInputEditText searchNoteEditText;
    private SimpleArrayAdapter adapter;
    private ListView listView;

    public WidgetConfigureActivity() {
        super();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setResult(RESULT_CANCELED);
        isLightMode = UiHelper.getLightThemePreference(this);

        setContentView(R.layout.activity_widget_configure);
        // Set layout size of activity
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        new Handler(Looper.getMainLooper()).post(() -> {
            allHashMapNotes = new HashMap<>();
            allArrayListNotes = new ArrayList<>();
            allNotes = AppData.getInstance().getAllNotes(WidgetConfigureActivity.this, false);

            for (Note currentNote : allNotes)
                allHashMapNotes.put(currentNote.getTitle(), currentNote.getNoteId());

            if (allHashMapNotes.isEmpty())
                allHashMapNotes.put("No notes or checklists found, search something else...", -1);

            initializeLayout();
        });

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
    }

    private void initializeLayout() {
        searchNoteLayout = (TextInputLayout) findViewById(R.id.search_edittext_layout);
        searchNoteEditText = (TextInputEditText) findViewById(R.id.search_edittext);
        listView = (ListView) findViewById(R.id.list);
        LinearLayout background = (LinearLayout) findViewById(R.id.background);
        TextView title = findViewById(R.id.title);

        background.setBackground(getDrawable(isLightMode ? R.drawable.round_corner_light : R.drawable.round_corner));
        searchNoteLayout.setBoxBackgroundColor(getColor(isLightMode ? R.color.white_100 : R.color.gray));
        searchNoteLayout.setHintTextColor(ColorStateList.valueOf(getColor(isLightMode ? R.color.black : R.color.white)));
        searchNoteEditText.setBackgroundColor(getColor(isLightMode ? R.color.white_100 : R.color.gray));
        searchNoteEditText.setTextColor(getColor(isLightMode ? R.color.black : R.color.white));
        listView.setBackgroundColor(getColor(isLightMode ? R.color.white_100 : R.color.gray));
        listView.setDivider(new ColorDrawable(getColor(isLightMode ? R.color.gray_300 : R.color.light_gray_300)));
        listView.setDividerHeight(1);
        title.setTextColor(getColor(isLightMode ? R.color.black : R.color.white));

        adapter = new SimpleArrayAdapter(this, R.layout.recyclerview_configure_widget, updateArrayListNotes());
        listView.setAdapter(adapter);

        searchNoteEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (allNotes.isEmpty()) return;

                allHashMapNotes.clear();
                allArrayListNotes.clear();
                for (Note currentNote : allNotes)
                    if (currentNote.getTitle().toLowerCase().contains(s.toString().toLowerCase()))
                        allHashMapNotes.put(currentNote.getTitle(), currentNote.getNoteId());

                if (allHashMapNotes.size() == 0)
                    allHashMapNotes.put("No notes or checklists found, search something else...", -1);

                updateArrayListNotes();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String widgetText = (String) listView.getItemAtPosition(position);

            if (widgetText.equals("No notes or checklists found, search something else...")) {
            } else
                createWidget(getApplicationContext(), widgetText, allHashMapNotes.get(widgetText));
        });
    }

    private void createWidget(Context context, String widgetText, int noteId) {
        // It is the responsibility of the configuration activity to update the app widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        AppData.getInstance().updateNoteWidget(context, noteId, mAppWidgetId);
        AppWidget.updateAppWidget(context, appWidgetManager, noteId, mAppWidgetId);

        // Make sure we pass back the original appWidgetId
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    public ArrayList<String> updateArrayListNotes() {
        allArrayListNotes.clear();
        allArrayListNotes.addAll(allHashMapNotes.keySet());
        return allArrayListNotes;
    }

    class SimpleArrayAdapter extends ArrayAdapter<String> {
        public SimpleArrayAdapter(Context context, int resource, ArrayList<String> notes) {
            super(context, resource, notes);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView textView = (TextView) view.findViewById(R.id.note_title);
            boolean isLightMode = UiHelper.getLightThemePreference(view.getContext());
            textView.setTextColor(getColor(isLightMode ? R.color.black : R.color.white));
            return view;
        }
    }
}