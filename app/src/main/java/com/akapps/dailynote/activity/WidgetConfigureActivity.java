package com.akapps.dailynote.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.akapps.dailynote.classes.other.AppWidget;
import java.util.ArrayList;

public class WidgetConfigureActivity extends Activity {

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    //private EditText mAppWidgetText;
    private static final String PREFS_NAME = "AppWidget";
    private static final String PREF_PREFIX_KEY = "appwidget";

    private ArrayList<Note> allNotes;
    private boolean isLightMode;

    public WidgetConfigureActivity() { super(); }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);
        isLightMode = UiHelper.getLightThemePreference(this);

        setContentView(R.layout.activity_widget_configure);
        // Set layout size of activity
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        //mAppWidgetText = (EditText) findViewById(R.id.appwidget_text);
        ListView listView = (ListView) findViewById(R.id.list);
        LinearLayout background = (LinearLayout) findViewById(R.id.background);
        TextView title = findViewById(R.id.title);

        background.setBackground(getDrawable(isLightMode ? R.drawable.round_corner_light : R.drawable.round_corner));
        listView.setBackgroundColor(getColor(isLightMode ? R.color.white_100 : R.color.gray));
        title.setTextColor(getColor(isLightMode ? R.color.black : R.color.white));

        new Handler(Looper.getMainLooper()).post(() -> {
            ArrayList<String> allArraylistNotes = new ArrayList<>();
            allNotes = AppData.getAllNotes(WidgetConfigureActivity.this);

            for(Note currentNote: allNotes)
                allArraylistNotes.add(currentNote.getTitle());

            if(allArraylistNotes.size() == 0)
                allArraylistNotes.add("No notes or checklists found\n\nPress Here to Close");

            initializeLayout(listView, allArraylistNotes);
        });
    }

    private void initializeLayout(ListView listView, ArrayList allArraylistNotes){
        SimpleArrayAdapter adapter = new SimpleArrayAdapter(this,
                R.layout.recyclerview_configure_widget, allArraylistNotes);

        listView.setAdapter(adapter);

        // ListView Item Click Listener
        listView.setOnItemClickListener((parent, view, position, id) -> {
            // Take ListView clicked item value
            String  widgetText = (String) listView.getItemAtPosition(position);

            if(widgetText.equals("No notes or checklists found\n\nPress Here to Close"))
                finish();
            else
                createWidget(getApplicationContext(), widgetText);
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

    private void createWidget(Context context, String widgetText) {
        // Store the string locally
        saveTitlePref(context, mAppWidgetId, widgetText);

        // It is the responsibility of the configuration activity to update the app widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        AppWidget.updateAppWidget(context, appWidgetManager, -1, mAppWidgetId);

        // Make sure we pass back the original appWidgetId
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveTitlePref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    public static String loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (titleValue != null) {
            return titleValue;
        } else {
            return "null";
        }
    }

    public static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
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