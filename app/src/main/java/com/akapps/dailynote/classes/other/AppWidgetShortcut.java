package com.akapps.dailynote.classes.other;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.classes.helpers.UiHelper;

public class AppWidgetShortcut extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            boolean isLightTheme = UiHelper.getLightThemePreference(context);
            // Get the layout for the widget and attach an on-click listener to the buttons
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_shortcut);
            views.setOnClickPendingIntent(R.id.add_checklist_shortcut, createPendingIntent(context, true));
            views.setOnClickPendingIntent(R.id.add_note_shortcut, createPendingIntent(context, false));
            views.setInt(R.id.widget_background, "setBackgroundColor", context.getColor(isLightTheme ? R.color.white_100 : R.color.gray));
            views.setInt(R.id.divider, "setBackgroundColor", context.getColor(isLightTheme ? R.color.light_gray_300 : R.color.gray_100));

            // Tell the AppWidgetManager to perform an update on the current app widget.
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private PendingIntent createPendingIntent(Context context, boolean isChecklist){
        // Create an Intent to launch NoteEdit
        Intent intent = new Intent(context, NoteEdit.class);
        if(isChecklist) {
            intent.putExtra("isChecklist", true);
            intent.setAction(Intent.ACTION_CREATE_SHORTCUT);
        }
        PendingIntent checklistPendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        return checklistPendingIntent;
    }
}