package com.akapps.dailynote.classes.other;

import static com.akapps.dailynote.classes.helpers.RealmHelper.getCurrentNote;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.activity.NoteLockScreen;
import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.Photo;
import com.akapps.dailynote.classes.data.SubCheckListItem;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmHelper;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.akapps.dailynote.recyclerview.photos_recyclerview;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import org.jetbrains.annotations.NotNull;

import io.realm.Realm;
import io.realm.RealmResults;
import www.sanju.motiontoast.MotionToast;

public class NoteInfoSheet extends RoundedBottomSheetDialogFragment {

    private Note currentNote;
    private int noteId;
    boolean showOpenButton;
    String noteTitle;

    public NoteInfoSheet() {
    }

    public NoteInfoSheet(int noteId, boolean showOpenButton) {
        this.noteId = noteId;
        this.showOpenButton = showOpenButton;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_note_info, container, false);

        RealmResults<Photo> allPhotos = getRealm().where(Photo.class).equalTo("noteId", noteId).findAll();
        currentNote = getCurrentNote(getContext(), noteId);

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
        ImageView copyIcon = view.findViewById(R.id.copy_icon);
        MaterialButton open = view.findViewById(R.id.open);
        RecyclerView photosScrollView = view.findViewById(R.id.note_photos);

        if (!showOpenButton)
            open.setVisibility(View.GONE);

        photosScrollView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        RecyclerView.Adapter scrollAdapter = new photos_recyclerview(allPhotos, getActivity(), getContext(), false);
        photosScrollView.setAdapter(scrollAdapter);

        if (allPhotos.size() == 0 || currentNote.getPinNumber() > 0)
            photosScrollView.setVisibility(View.GONE);

        open.setOnClickListener(view1 -> {
            openNoteActivity(currentNote);
        });

        try {
            if (currentNote.getPinNumber() == 0) {
                lockIcon.setVisibility(View.GONE);
            }
            else{
                copyIcon.setVisibility(View.GONE);
            }

            if (!currentNote.isPin())
                pinIcon.setVisibility(View.GONE);

            if (!currentNote.isTrash())
                trashIcon.setVisibility(View.GONE);

            if (!currentNote.getReminderDateTime().isEmpty() && !currentNote.isTrash()) {
                trashIcon.setVisibility(View.VISIBLE);
                trashIcon.setImageDrawable(getContext().getDrawable(R.drawable.reminder_icon));
            }

            if (!currentNote.isArchived())
                archiveIcon.setVisibility(View.GONE);

            if (currentNote.getTitle().isEmpty())
                noteTitle = "~ No Title ~";
            else
                noteTitle = currentNote.getTitle();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                noteName.setText(Html.fromHtml(noteName.getText() + "<br>" +
                                "<font color='#7A9CC7'>" + noteTitle + "</font>",
                        Html.FROM_HTML_MODE_COMPACT));
                dateCreated.setText(Html.fromHtml(dateCreated.getText() + "<br>" +
                                "<font color='#7A9CC7'>" + (getUser().isTwentyFourHourFormat() ?
                                Helper.convertToTwentyFourHour(currentNote.getDateCreated()).replace("\n", " ")
                                : currentNote.getDateCreated().replace("\n", " ")) + "</font>",
                        Html.FROM_HTML_MODE_COMPACT));

                dateEdited.setText(Html.fromHtml(dateEdited.getText() + "<br>" +
                                "<font color='#7A9CC7'>" + (getUser().isTwentyFourHourFormat() ?
                                Helper.convertToTwentyFourHour(currentNote.getDateEdited()).replace("\n", " ")
                                : currentNote.getDateEdited().replace("\n", " ")) + "</font>",
                        Html.FROM_HTML_MODE_COMPACT));

                folderName.setText(Html.fromHtml(folderName.getText() + "<br>" +
                                "<font color='#7A9CC7'>" + currentNote.getCategory() + "</font>",
                        Html.FROM_HTML_MODE_COMPACT));

                numPhotos.setText(Html.fromHtml(numPhotos.getText() + "<br>" +
                                "<font color='#7A9CC7'>" + allPhotos.size() + "</font>",
                        Html.FROM_HTML_MODE_COMPACT));
            } else {
                noteName.setText(Html.fromHtml(noteName.getText() + "<br>" +
                        "<font color='#7A9CC7'>" + noteTitle + "</font>"));
                dateCreated.setText(Html.fromHtml(dateCreated.getText() + "<br>" +
                        "<font color='#7A9CC7'>" + (getUser().isTwentyFourHourFormat() ?
                        Helper.convertToTwentyFourHour(currentNote.getDateCreated()).replace("\n", " ")
                        : currentNote.getDateCreated().replace("\n", " ")) + "</font>"));

                dateEdited.setText(Html.fromHtml(dateEdited.getText() + "<br>" +
                        "<font color='#7A9CC7'>" + (getUser().isTwentyFourHourFormat() ?
                        Helper.convertToTwentyFourHour(currentNote.getDateEdited()).replace("\n", " ")
                        : currentNote.getDateEdited().replace("\n", " ")) + "</font>"));

                folderName.setText(Html.fromHtml(folderName.getText() + "<br>" +
                        "<font color='#7A9CC7'>" + currentNote.getCategory() + "</font>"));

                numPhotos.setText(Html.fromHtml(numPhotos.getText() + "<br>" +
                        "<font color='#7A9CC7'>" + allPhotos.size() + "</font>"));
            }

            String getNoteString = sanitizeWord(currentNote.getNote());

            String getChecklistString = "";
            int checklistSize = 0;
            int subChecklistSize = 0;
            if (currentNote.isCheckList()) {
                checklistSize = currentNote.getChecklist().size();
                for (int i = 0; i < checklistSize; i++) {
                    getChecklistString += sanitizeWord(currentNote.getChecklist().get(i).getText()) + " ";
                    int currentSublistSize = currentNote.getChecklist().get(i).getSubChecklist().size();
                    subChecklistSize += currentSublistSize;
                    for (int j = 0; j < currentSublistSize; j++)
                        getChecklistString += sanitizeWord(currentNote.getChecklist().get(i).getSubChecklist().get(j).getText()) + " ";
                }
            }

            int noteSize = 0;
            if (getNoteString.length() == 0)
                noteSize = 0;
            else
                noteSize = getNoteString.split(" ").length;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                numWords.setText(Html.fromHtml(numWords.getText() + "<br>" +
                                "<font color='#7A9CC7'>" +
                                (currentNote.isCheckList() ? checklistSize + " list items<br>" +
                                        subChecklistSize + " sub-list items<br>" +
                                        getChecklistString.split(" ").length :
                                        noteSize) + " words" + "</font>",
                        Html.FROM_HTML_MODE_COMPACT));

                numChars.setText(Html.fromHtml(numChars.getText() + "<br>" +
                                "<font color='#7A9CC7'>" + (currentNote.isCheckList() ?
                                getChecklistString.length() :
                                getNoteString.length()) + " characters" + "</font>",
                        Html.FROM_HTML_MODE_COMPACT));
            } else {
                numWords.setText(Html.fromHtml(numWords.getText() + "<br>" +
                        "<font color='#7A9CC7'>" +
                        (currentNote.isCheckList() ? checklistSize + " list items<br>" +
                                subChecklistSize + " sub-list items<br>" +
                                getChecklistString.split(" ").length :
                                noteSize) + " words" + "</font>"));

                numChars.setText(Html.fromHtml(numChars.getText() + "<br>" +
                        "<font color='#7A9CC7'>" + (currentNote.isCheckList() ?
                        getChecklistString.length() :
                        getNoteString.length()) + " characters" + "</font>"));
            }
        } catch (Exception e) {
            this.dismiss();
        }

        copyIcon.setOnClickListener(view14 -> {
            // copy text
            if (currentNote.isCheckList())
                copyToClipboard(copyChecklist(currentNote));
            else
                copyToClipboard(copyWord(currentNote.getNote()));
        });

        return view;
    }

    private void copyToClipboard(String wordToCopy) {
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("text", wordToCopy);
        clipboard.setPrimaryClip(clip);
        Helper.showMessage(getActivity(), "Success!",
                "Copied successfully", MotionToast.TOAST_SUCCESS);
    }

    private String sanitizeWord(String word) {
        return word.replaceAll("<.*?>", " ").replaceAll("&nbsp;", " ")
                .replaceAll("\\s+", " ").trim();
    }

    private String copyChecklist(Note currentNote) {
        String checklistString = "";
        String checklistSeparator = getUser().getItemsSeparator().equals("newline") ? "\n" : getUser().getItemsSeparator();
        String sublistSeparator = getUser().getSublistSeparator().equals("space") ? "\n " : getUser().getSublistSeparator();

        for (CheckListItem checkListItem : currentNote.getChecklist()) {
            checklistString += checkListItem.getText();
            for (SubCheckListItem subCheckListItem : checkListItem.getSubChecklist()) {
                checklistString += sublistSeparator + subCheckListItem.getText();
            }
            checklistString += checklistSeparator;
        }
        return checklistString;
    }

    private String copyWord(String word) {
        return word.replaceAll("<br>", "\n").replaceAll("<.*?>", " ").replaceAll("&nbsp;", " ");
    }

    private void openNoteActivity(Note currentNote) {
        if (currentNote.getPinNumber() == 0) {
            Intent note = new Intent(getActivity(), NoteEdit.class);
            note.putExtra("id", currentNote.getNoteId());
            note.putExtra("isChecklist", currentNote.isCheckList());
            getActivity().startActivity(note);
            dismiss();
        } else {
            Intent lockScreen = new Intent(getActivity(), NoteLockScreen.class);
            lockScreen.putExtra("id", currentNote.getNoteId());
            lockScreen.putExtra("title", currentNote.getTitle().replace("\n", " "));
            lockScreen.putExtra("pin", currentNote.getPinNumber());
            lockScreen.putExtra("securityWord", currentNote.getSecurityWord());
            lockScreen.putExtra("fingerprint", currentNote.isFingerprint());
            getActivity().startActivity(lockScreen);
            dismiss();
        }
    }

    private Realm getRealm() {
        return RealmSingleton.getInstance(getContext());
    }

    private User getUser() {
        return RealmHelper.getUser(getContext(), "noteInfoSheet");
    }

    @Override
    public int getTheme() {
        return UiHelper.getBottomSheetTheme(getContext());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.dismiss();
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        UiHelper.setBottomSheetBehavior(view, dialog);
    }

}