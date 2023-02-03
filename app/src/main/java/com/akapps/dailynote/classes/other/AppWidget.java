package com.akapps.dailynote.classes.other;

import android.app.PendingIntent;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.text.Html;
import android.widget.RemoteViews;
import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.activity.NoteLockScreen;
import com.akapps.dailynote.activity.WidgetConfigureActivity;
import com.akapps.dailynote.activity.WidgetListView;
import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.SubCheckListItem;
import com.akapps.dailynote.classes.helpers.AppData;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AppWidget extends AppWidgetProvider {

    private static Note currentNote;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        // initialize database and get data

        for (int appWidgetId : appWidgetIds)
            updateAppWidget(context, appWidgetManager, -1, appWidgetId);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            WidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {}

    @Override
    public void onDisabled(Context context) {}

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int noteId, int appWidgetId) {
        CharSequence widgetText = WidgetConfigureActivity.loadTitlePref(context, appWidgetId);

        if(widgetText.equals("null"))
            return;

        ArrayList<Note> allNotes = AppData.getAllNotes(context);

        currentNote = getCurrentNote(allNotes, (String) widgetText, noteId);

        if(currentNote != null) {
            boolean isAllChecklistDone = isAllChecklistChecked(currentNote);
            AppData.updateNoteWidget(context, currentNote.getNoteId(), appWidgetId);
            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
            views.setTextViewText(R.id.appwidget_text, noteId != -1 ? currentNote.getTitle() : (String) widgetText);
            views.setInt(R.id.widget_background, "setBackgroundColor", currentNote.getBackgroundColor());
            if (isAllChecklistDone)
                views.setInt(R.id.appwidget_text, "setPaintFlags", Paint.STRIKE_THRU_TEXT_FLAG);
            else
                views.setInt(R.id.appwidget_text, "setPaintFlags", 0);
            views.setTextColor(R.id.appwidget_text, currentNote.getTextColor());

            ArrayList<String> list = new ArrayList<>();
            if (currentNote.isCheckList()) {
                for (CheckListItem currentChecklist : currentNote.getChecklist()) {
                    if (currentChecklist.getSubChecklist().size() == 0)
                        list.add(currentChecklist.getText());
                    else {
                        StringBuilder subList = new StringBuilder();
                        for (SubCheckListItem subCheckListItem : currentChecklist.getSubChecklist())
                            subList.append("-> " + subCheckListItem.getText() + "\n");
                        list.add(currentChecklist.getText() + "\n" + subList);
                    }
                }
            } else {
                String preview = Html.fromHtml(currentNote.getNote(), Html.FROM_HTML_MODE_COMPACT).toString();
                preview = preview.replaceAll("(\\s{2,})", " ");
                list.add(preview);
            }

            Intent intent = new Intent(context, WidgetListView.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.putStringArrayListExtra("list", list);
            intent.setData((Uri.fromParts("content", String.valueOf(appWidgetId), null)));
            intent.putExtra("id", currentNote.getNoteId());
            views.setRemoteAdapter(R.id.preview_checklist, intent);

            if (currentNote.getPinNumber() == 0) {
                intent = new Intent(context, NoteEdit.class);
                intent.putExtra("id", currentNote.getNoteId());
                intent.putExtra("isChecklist", currentNote.isCheckList());
            } else {
                intent = new Intent(context, NoteLockScreen.class);
                intent.putExtra("id", currentNote.getNoteId());
                intent.putExtra("title", currentNote.getTitle().replace("\n", " "));
                intent.putExtra("pin", currentNote.getPinNumber());
                intent.putExtra("securityWord", currentNote.getSecurityWord());
                intent.putExtra("fingerprint", currentNote.isFingerprint());
            }
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            // if title or checklist is clicked, then open note
            views.setOnClickPendingIntent(R.id.widget_background, pendingIntent);
            views.setPendingIntentTemplate(R.id.preview_checklist, pendingIntent);

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private static Note getCurrentNote(ArrayList<Note> allNotes, String noteTitle, int noteId){
        List<Note> results;

        if(noteId != -1)
            results = allNotes.stream()
                    .filter(item -> item.getNoteId() == noteId)
                    .collect(Collectors.toList());
        else
            results = allNotes.stream()
                    .filter(item -> item.getTitle().equals(noteTitle))
                    .collect(Collectors.toList());

        if(results.size() == 0)
            return null;

        return results.get(0);
    }
    private static boolean isAllChecklistChecked(Note currentNote){
        if(currentNote.isCheckList()){
            List<CheckListItem> results = currentNote.getChecklist().stream()
                    .filter(item -> item.isChecked() == true)
                    .collect(Collectors.toList());

            if(results.size() == currentNote.getChecklist().size() && results.size()!=0)
                return true;
        }
        return false;
    }
}