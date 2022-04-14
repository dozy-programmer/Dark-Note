package com.akapps.dailynote.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.text.Html;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.helpers.AppData;
import java.util.ArrayList;
import java.util.List;

public class WidgetListView extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int noteId = intent.getIntExtra("id", 0);
        return new WidgetListViewFactory(getApplicationContext(), noteId, AppData.getNoteChecklist(noteId, getApplicationContext()));
    }

    class WidgetListViewFactory implements RemoteViewsService.RemoteViewsFactory {
        private final Context context;
        private List<String> checklist;
        private int noteId;

        public WidgetListViewFactory(Context context, int noteId, ArrayList<String> checklist) {
            this.context = context;
            this.noteId = noteId;
            this.checklist = checklist;
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            checklist = AppData.getNoteChecklist(noteId, getApplicationContext());
        }

        @Override
        public void onDestroy() {
            checklist.clear();
        }

        @Override
        public int getCount() {
            return checklist.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            String currentItem = checklist.get(position);
            RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.recyclerview_widget);

            String preview = Html.fromHtml(currentItem, Html.FROM_HTML_MODE_COMPACT).toString();
            preview = preview.replaceAll("(\\s{2,})", " ");

            if(currentItem.contains("~~"))
                remoteView.setInt(R.id.checklist_text, "setPaintFlags", Paint.STRIKE_THRU_TEXT_FLAG);
            else
                remoteView.setInt(R.id.checklist_text, "setPaintFlags", 0);

            String checklistItemText = preview.replace("~~", "");
            remoteView.setTextViewText(R.id.checklist_text, checklistItemText);

            return remoteView;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
