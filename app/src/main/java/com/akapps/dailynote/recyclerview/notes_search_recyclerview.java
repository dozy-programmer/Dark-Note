package com.akapps.dailynote.recyclerview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.SubExpense;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class notes_search_recyclerview extends RecyclerView.Adapter<notes_search_recyclerview.MyViewHolder>{

    // project data
    private ArrayList<Note> allNotes;
    private MaterialButton selectedNote;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView noteTitle;
        private final LinearLayout background;
        private final View view;

        public MyViewHolder(View v) {
            super(v);
            noteTitle = v.findViewById(R.id.note_title);
            background = v.findViewById(R.id.background);
            view = v;
        }
    }

    public notes_search_recyclerview(ArrayList<Note> allNotes, MaterialButton selectedNote) {
        this.allNotes = allNotes;
        this.selectedNote = selectedNote;
    }

    @Override
    public notes_search_recyclerview.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_note_search_layout, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Note currentNote = allNotes.get(position);
        holder.noteTitle.setText(currentNote.getTitle());

        holder.view.setOnClickListener(view -> selectedNote.setText(currentNote.getTitle()));
    }

    @Override
    public int getItemCount() {
        return allNotes.size();
    }

}
