package com.akapps.dailynote.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
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
        return new WidgetListViewFactory(getApplicationContext(), AppData.getNoteChecklist(noteId, getApplicationContext()));
    }

    class WidgetListViewFactory implements RemoteViewsService.RemoteViewsFactory {
        private final Context context;
        private List<String> checklist;

        public WidgetListViewFactory(Context context, ArrayList<String> checklist) {
            this.context = context;
            this.checklist = checklist;
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
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
            remoteView.setTextViewText(R.id.checklist_text, preview);

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
            return checklist.get(position).getBytes().length;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
