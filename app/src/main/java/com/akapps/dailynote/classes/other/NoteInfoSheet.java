package com.akapps.dailynote.classes.other;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.activity.NoteLockScreen;
import com.akapps.dailynote.activity.SettingsScreen;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.Photo;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.fragments.notes;
import com.akapps.dailynote.recyclerview.photos_recyclerview;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import org.jetbrains.annotations.NotNull;

import io.realm.RealmResults;
import www.sanju.motiontoast.MotionToast;

public class NoteInfoSheet extends RoundedBottomSheetDialogFragment{

    private Note currentNote;
    private Fragment fragment;
    private RealmResults<Photo> allPhotos;

    int deleteCounter;

    public NoteInfoSheet(){}

    public NoteInfoSheet(Note currentNote, Fragment fragment, RealmResults<Photo> allPhotos){
        this.currentNote = currentNote;
        this.fragment = fragment;
        this.allPhotos = allPhotos;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_note_info, container, false);

        if (AppData.getAppData().isLightTheme) {
            view.setBackgroundColor(getContext().getColor(R.color.light_mode));
        }
        else
            view.setBackgroundColor(getContext().getColor(R.color.gray));

        TextView noteName = view.findViewById(R.id.note_name);
        TextView dateCreated = view.findViewById(R.id.date_created);
        TextView dateEdited = view.findViewById(R.id.date_edited);
        TextView folderName = view.findViewById(R.id.folder_name);
        TextView numPhotos = view.findViewById(R.id.num_photos);
        TextView numWords = view.findViewById(R.id.num_words);
        TextView numChars = view.findViewById(R.id.num_chars);
        ImageView lockIcon = view.findViewById(R.id.lock_icon);
        ImageView pinIcon = view.findViewById(R.id.pin_icon);
        ImageView trashIcon = view.findViewById(R.id.trash_icon);
        ImageView archiveIcon = view.findViewById(R.id.archive_icon);
        MaterialButton delete = view.findViewById(R.id.delete);
        MaterialButton copy = view.findViewById(R.id.copy);
        MaterialButton open = view.findViewById(R.id.open);
        RecyclerView photosScrollView = view.findViewById(R.id.note_photos);

        photosScrollView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        RecyclerView.Adapter scrollAdapter = new photos_recyclerview(allPhotos, getActivity(), getContext(), false);
        photosScrollView.setAdapter(scrollAdapter);

        if(allPhotos.size() == 0 || currentNote.getPinNumber() > 0)
            photosScrollView.setVisibility(View.GONE);

        open.setOnClickListener(view1 -> openNoteActivity(currentNote));

        delete.setOnClickListener(view12 -> {
            if(deleteCounter == 1) {
                ((notes) fragment).deleteNote(currentNote);
                NoteInfoSheet.this.dismiss();
            }
            else{
                deleteCounter++;
                Helper.showMessage(getActivity(), "Confirm", "Press delete again " +
                        " to confirm delete action", MotionToast.TOAST_ERROR);
            }
        });

        try {
            if(currentNote.getPinNumber() == 0)
                lockIcon.setVisibility(View.GONE);

            if(!currentNote.isPin())
                pinIcon.setVisibility(View.GONE);

            if(!currentNote.isTrash())
                trashIcon.setVisibility(View.GONE);

            if(!currentNote.isArchived())
                archiveIcon.setVisibility(View.GONE);

            noteName.setText(Html.fromHtml(noteName.getText() + "<br>" +
                            "<font color='#e65c00'>" + currentNote.getTitle() + "</font>",
                    Html.FROM_HTML_MODE_COMPACT));

            dateCreated.setText(Html.fromHtml(dateCreated.getText() + "<br>" +
                            "<font color='#e65c00'>" + currentNote.getDateCreated().replace("\n", " ") + "</font>",
                    Html.FROM_HTML_MODE_COMPACT));

            dateEdited.setText(Html.fromHtml(dateEdited.getText() + "<br>" +
                            "<font color='#e65c00'>" + currentNote.getDateEdited().replace("\n", " ") + "</font>",
                    Html.FROM_HTML_MODE_COMPACT));

            folderName.setText(Html.fromHtml(folderName.getText() + "<br>" +
                            "<font color='#e65c00'>" + currentNote.getCategory() + "</font>",
                    Html.FROM_HTML_MODE_COMPACT));

            numPhotos.setText(Html.fromHtml(numPhotos.getText() + "<br>" +
                            "<font color='#e65c00'>" + allPhotos.size() + "</font>",
                    Html.FROM_HTML_MODE_COMPACT));

            String getNoteString = sanitizeWord(currentNote.getNote());

            String getChecklistString = "";
            int checklistSize = 0;
            int subChecklistSize = 0;
            if(currentNote.isCheckList()){
                checklistSize =  currentNote.getChecklist().size();
                for(int i=0 ;i < checklistSize; i++){
                    getChecklistString += sanitizeWord(currentNote.getChecklist().get(i).getText()) + " ";
                    int currentSublistSize = currentNote.getChecklist().get(i).getSubChecklist().size();
                    subChecklistSize += currentSublistSize;
                    for(int j=0; j< currentSublistSize; j++)
                        getChecklistString += sanitizeWord(currentNote.getChecklist().get(i).getSubChecklist().get(j).getText()) + " ";
                }
            }

            int noteSize = 0;
            if(getNoteString.length() == 0)
                noteSize = 0;
            else
                noteSize = getNoteString.split(" ").length;

            numWords.setText(Html.fromHtml(numWords.getText() + "<br>" +
                            "<font color='#e65c00'>" +
                            (currentNote.isCheckList() ? checklistSize + " list items<br>" +
                                    subChecklistSize + " sub-list items<br>" +
                                    getChecklistString.split(" ").length :
                                    noteSize) + " words" + "</font>",
                    Html.FROM_HTML_MODE_COMPACT));

            numChars.setText(Html.fromHtml(numChars.getText() + "<br>" +
                            "<font color='#e65c00'>" + (currentNote.isCheckList() ?
                            getChecklistString.length():
                            getNoteString.length()) + " characters" + "</font>",
                    Html.FROM_HTML_MODE_COMPACT));
        }catch (Exception e){
            this.dismiss();
        }

        return view;
    }

    private String sanitizeWord(String word){
        return word.replaceAll("<.*?>", " ").replaceAll("&nbsp;", " ")
                .replaceAll("\\s+", " ").trim();
    }

    private void openNoteActivity(Note currentNote){
        if(currentNote.getPinNumber()==0){
            Intent note = new Intent(getActivity(), NoteEdit.class);
            note.putExtra("id", currentNote.getNoteId());
            note.putExtra("isChecklist", currentNote.isCheckList());
            getActivity().startActivity(note);
        }
        else {
            Intent lockScreen = new Intent(getActivity(), NoteLockScreen.class);
            lockScreen.putExtra("id", currentNote.getNoteId());
            lockScreen.putExtra("title", currentNote.getTitle().replace("\n", " "));
            lockScreen.putExtra("pin", currentNote.getPinNumber());
            lockScreen.putExtra("securityWord", currentNote.getSecurityWord());
            lockScreen.putExtra("fingerprint", currentNote.isFingerprint());
            getActivity().startActivity(lockScreen);
        }
    }

    @Override
    public int getTheme() {
        if(AppData.getAppData().isLightTheme)
            return R.style.BaseBottomSheetDialogLight;
        else
            return R.style.BaseBottomSheetDialog;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.dismiss();
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.getViewTreeObserver()
                .addOnGlobalLayoutListener(() -> {
                    BottomSheetDialog dialog =(BottomSheetDialog) getDialog ();
                    if (dialog != null) {
                        FrameLayout bottomSheet = dialog.findViewById (R.id.design_bottom_sheet);
                        if (bottomSheet != null) {
                            BottomSheetBehavior behavior = BottomSheetBehavior.from (bottomSheet);
                            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                    }
                });
    }

}