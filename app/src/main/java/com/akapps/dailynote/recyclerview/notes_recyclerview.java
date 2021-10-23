package com.akapps.dailynote.recyclerview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.activity.NoteLockScreen;
import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.Photo;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.fragments.notes;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import www.sanju.motiontoast.MotionToast;

public class notes_recyclerview extends RecyclerView.Adapter<notes_recyclerview.MyViewHolder>{

    // project data
    private final RealmResults<Note> allNotes;
    private RealmResults<Photo> allPhotos;
    private String noteText;
    private final Activity activity;
    private final Fragment noteFragment;
    private boolean showPreview;
    private final String TITLE_KEY = "title_lines";
    private final String PREVIEW_KEY = "preview_lines";

    // database
    private final Realm realm;

    // multi select
    private boolean enableSelectMultiple;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView note_title;
        private final TextView note_edited;
        private final TextView note_preview;
        private final TextView preview_photo_message;
        private final TextView category;
        private final View view;
        private final MaterialCardView note_background;
        private final MaterialCardView category_background;
        private final ImageView preview_1;
        private final ImageView preview_2;
        private final ImageView preview_3;
        private final ImageView lock_icon;
        private final ImageView reminder_icon;
        private final ImageView pin_icon;
        private final ImageView checklist_icon;
        private final ImageView archived_icon;

        public MyViewHolder(View v) {
            super(v);
            note_title = v.findViewById(R.id.note_title);
            note_edited = v.findViewById(R.id.note_edited);
            note_background = v.findViewById(R.id.note_background);
            note_preview = v.findViewById(R.id.note_preview);
            preview_1 = v.findViewById(R.id.preview_1);
            preview_2 = v.findViewById(R.id.preview_2);
            preview_3 = v.findViewById(R.id.preview_3);
            preview_photo_message = v.findViewById(R.id.preview_photos_number);
            lock_icon = v.findViewById(R.id.lock_icon);
            reminder_icon = v.findViewById(R.id.reminder_icon);
            pin_icon = v.findViewById(R.id.pin_icon);
            checklist_icon = v.findViewById(R.id.checklist_icon);
            archived_icon = v.findViewById(R.id.archived_icon);
            category = v.findViewById(R.id.category);
            category_background = v.findViewById(R.id.category_background);
            view = v;
        }
    }

    public notes_recyclerview(RealmResults<Note> allNotes, Realm realm, Activity activity, Fragment fragment, boolean showPreview) {
        this.allNotes = allNotes;
        this.realm = realm;
        this.activity = activity;
        this.noteFragment = fragment;
        this.showPreview = showPreview;
    }

    @Override
    public notes_recyclerview.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_note_layout, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        // retrieves current note object
        Note currentNote = allNotes.get(position);

        // retrieves all photos that belong to note
        allPhotos = realm.where(Photo.class).equalTo("noteId", currentNote.getNoteId()).findAll();

        // retrieves note text, the lock status of note and reminder status
        noteText = currentNote.getNote() == null ? "": currentNote.getNote();
        boolean isNoteLocked = currentNote.getPinNumber() > 0;
        boolean hasReminder = currentNote.getReminderDateTime().length() > 0;

        // populates note data into the recyclerview
        holder.note_title.setText(currentNote.getTitle().replaceAll("\n"," "));
        holder.note_edited.setText(currentNote.getDateEdited());
        holder.note_background.setCardBackgroundColor(currentNote.getBackgroundColor());

        // changes the number of lines title and preview occupy depending on user setting
        String title_lines = Helper.getPreference(noteFragment.getContext(), "title_lines");
        holder.note_title.setMaxLines(title_lines == null || title_lines.equals("") ? 3 : Integer.parseInt(title_lines));
        String preview_lines = Helper.getPreference(noteFragment.getContext(), "preview_lines");
        holder.note_preview.setMaxLines(preview_lines == null  || preview_lines.equals("") ? 3 : Integer.parseInt(preview_lines));

        // format note to remove all new line characters and any spaces more than a length of 1
        String preview = Html.fromHtml(currentNote.getNote(), Html.FROM_HTML_MODE_COMPACT).toString();
        preview = preview.replaceAll("(\\s{2,})", " ");
        holder.note_preview.setText(preview);

        // if note has a category, then it shows it
        if(currentNote.getCategory().equals("none"))
            holder.category.setVisibility(View.GONE);
        else {
            holder.category.setVisibility(View.VISIBLE);
            holder.category.setText(currentNote.getCategory());
            holder.category_background.setCardBackgroundColor(currentNote.getBackgroundColor());
        }

        // if selecting multiple notes, it changes the color of the note outline
        // based on if it is selected or not
        if(enableSelectMultiple) {
            if (currentNote.isSelected())
                holder.note_background.setStrokeColor(activity.getColor(R.color.red));
            else
                holder.note_background.setStrokeColor(currentNote.getBackgroundColor());
            holder.note_background.setStrokeWidth(10);
        }

        // if a note has a checklist, show an icon so user can differentiate between them
        if(currentNote.isCheckList()) {
            holder.checklist_icon.setVisibility(View.VISIBLE);
            holder.checklist_icon.setImageDrawable(activity.getDrawable(R.drawable.checklist_icon));
            StringBuilder checkListString = new StringBuilder();
            RealmResults<CheckListItem> checklist = realm.where(CheckListItem.class)
                    .equalTo("id", currentNote.getNoteId())
                    .sort("positionInList", Sort.ASCENDING)
                    .findAll();
            for (int i = 0; i < currentNote.getChecklist().size(); i++) {
                checkListString.append("• ").append(checklist.get(i).getText()).append("\n");
                if (i == 3) {
                    if (!isNoteLocked) {
                        holder.preview_photo_message.setVisibility(View.VISIBLE);
                        holder.preview_photo_message.setText("..." + (checklist.size() - 3) + " more");
                    }
                }
            }
            if(checklist.size()<=3)
                holder.preview_photo_message.setVisibility(View.GONE);

            realm.beginTransaction();
            currentNote.setChecklistConvertedToString(checkListString.toString());
            realm.commitTransaction();
            holder.note_preview.setText(checkListString.toString());
            holder.note_preview.setTextSize(15);
            holder.note_preview.setGravity(Gravity.LEFT);
        }
        else
            holder.checklist_icon.setVisibility(View.GONE);

        if(currentNote.isArchived()) {
            if(!currentNote.isCheckList()){
                holder.checklist_icon.setVisibility(View.VISIBLE);
                holder.checklist_icon.setImageDrawable(activity.getDrawable(R.drawable.archive_icon));
                holder.archived_icon.setVisibility(View.GONE);
            }
            else {
                holder.archived_icon.setVisibility(View.VISIBLE);
                holder.archived_icon.setImageDrawable(activity.getDrawable(R.drawable.archive_icon));
            }
        }
        else
            holder.archived_icon.setVisibility(View.GONE);

        // if note is locked, then the note will display a lock icon
        // and user can't see a preview of the note text
        if(isNoteLocked) {
            holder.note_preview.setVisibility(View.GONE);
            holder.lock_icon.setVisibility(View.VISIBLE);
        }
        else{
            holder.note_preview.setVisibility(View.VISIBLE);
            holder.lock_icon.setVisibility(View.GONE);
        }

        // if note is pinned, it shows a pin icon
        if (currentNote.isPin())
            holder.pin_icon.setVisibility(View.VISIBLE);
        else
            holder.pin_icon.setVisibility(View.GONE);

        // if a note has a reminder, then a clock icon is showed
        if(hasReminder)
            holder.reminder_icon.setVisibility(View.VISIBLE);
        else
            holder.reminder_icon.setVisibility(View.GONE);

        // if note is checked, it sets the note title and note preview text to
        // has a strike through them
        if(currentNote.isChecked()) {
            holder.note_title.setPaintFlags(holder.note_title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.note_preview.setPaintFlags(holder.note_title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else {
            holder.note_title.setPaintFlags(0);
            holder.note_preview.setPaintFlags(0);
        }

        // if preview is empty and it is note locked, then the note will show a sad emoji instead
        if(noteText.isEmpty() && !isNoteLocked) {
            if(!currentNote.isCheckList()){
                holder.note_preview.setText("\uD83E\uDD7A");
                holder.note_preview.setGravity(Gravity.CENTER);
                holder.note_preview.setTextSize(30);
            }
        }
        else {
            holder.note_preview.setGravity(Gravity.LEFT);
            holder.note_preview.setTextSize(15);
        }

        // changes the number of lines title and preview occupy depending on user setting
        int previewLines = null == Helper.getPreference(activity, PREVIEW_KEY)? 3
                : Integer.parseInt(Helper.getPreference(activity, PREVIEW_KEY));
        holder.note_title.setMaxLines(null == Helper.getPreference(activity, TITLE_KEY) ? 3
                : Integer.parseInt(Helper.getPreference(activity, TITLE_KEY)));
        holder.note_preview.setMaxLines(previewLines);

        if(showPreview && !isNoteLocked){
            holder.preview_photo_message.setVisibility(View.VISIBLE);
            holder.note_preview.setVisibility(View.VISIBLE);
            if((currentNote.getChecklist().size()-previewLines)>0)
                holder.preview_photo_message.setText("..." + (currentNote.getChecklist().size()-previewLines) + " more");
            else
                holder.preview_photo_message.setText("");
        }
        else {
            holder.note_preview.setVisibility(View.GONE);
            holder.preview_photo_message.setVisibility(View.GONE);
        }

        // if note has photos, it displays them under note preview (up to 3)
        // if there are more, it shows a text underneath them with
        // the number of photos that are left
        if(allPhotos.size()>0 & !isNoteLocked && showPreview){
            if(allPhotos.size()==1) {
                holder.preview_1.setVisibility(View.GONE);
                holder.preview_3.setVisibility(View.GONE);
                holder.preview_2.setVisibility(View.VISIBLE);
                Glide.with(activity).load(allPhotos.get(0).getPhotoLocation())
                        .placeholder(activity.getDrawable(R.drawable.error_icon))
                        .into(holder.preview_2);
            }
            else if(allPhotos.size()==2) {
                holder.preview_1.setVisibility(View.VISIBLE);
                holder.preview_2.setVisibility(View.GONE);
                holder.preview_3.setVisibility(View.VISIBLE);
                Glide.with(activity).load(allPhotos.get(0).getPhotoLocation())
                        .placeholder(activity.getDrawable(R.drawable.error_icon))
                        .into(holder.preview_1);

                Glide.with(activity).load(allPhotos.get(1).getPhotoLocation())
                        .placeholder(activity.getDrawable(R.drawable.error_icon))
                        .into(holder.preview_3);
            }
            else{
                holder.preview_1.setVisibility(View.VISIBLE);
                holder.preview_2.setVisibility(View.VISIBLE);
                holder.preview_3.setVisibility(View.VISIBLE);

                Glide.with(activity).load(allPhotos.get(0).getPhotoLocation())
                        .placeholder(activity.getDrawable(R.drawable.error_icon))
                        .into(holder.preview_1);

                Glide.with(activity).load(allPhotos.get(1).getPhotoLocation())
                        .placeholder(activity.getDrawable(R.drawable.error_icon))
                        .into(holder.preview_2);

                Glide.with(activity).load(allPhotos.get(2).getPhotoLocation())
                        .placeholder(activity.getDrawable(R.drawable.error_icon))
                        .into(holder.preview_3);
            }

            if(allPhotos.size()>3) {
                holder.preview_photo_message.setVisibility(View.VISIBLE);
                holder.preview_photo_message.setText("..." + (allPhotos.size() - 3) + " more");
            }
            else
                holder.preview_photo_message.setVisibility(View.GONE);
        }
        else {
            holder.preview_1.setVisibility(View.GONE);
            holder.preview_2.setVisibility(View.GONE);
            holder.preview_3.setVisibility(View.GONE);
            if(!currentNote.isCheckList() || isNoteLocked)
                holder.preview_photo_message.setVisibility(View.GONE);
        }

        // if user is selecting multiple notes, it updates status of select in note,
        // changes outline color, and updates number of selected notes
        // if not, it opens note
        holder.view.setOnClickListener(v -> {
            enableSelectMultiple = ((notes) noteFragment).enableSelectMultiple;
            if(!enableSelectMultiple)
                openNoteActivity(currentNote);
            else{
                if(currentNote.isSelected()) {
                    saveSelected(currentNote, false);
                    holder.note_background.setStrokeColor(currentNote.getBackgroundColor());
                    holder.note_background.setStrokeWidth(10);
                    ((notes) noteFragment).numberSelected(0, 1, -1);
                }
                else{
                    saveSelected(currentNote, true);
                    holder.note_background.setStrokeColor(activity.getColor(R.color.red));
                    holder.note_background.setStrokeWidth(10);
                    ((notes) noteFragment).numberSelected(1, 0, -1);
                }
            }
        });

        // if note is long clicked, then the ability to select multiple notes is enabled
        holder.view.setOnLongClickListener(v -> {
            if(!enableSelectMultiple) {
                ((notes) noteFragment).unSelectAllNotes();
                enableSelectMultiple = true;
                saveSelected(currentNote, true);
                holder.note_background.setStrokeColor(activity.getColor(R.color.red));
                holder.note_background.setStrokeWidth(10);
                ((notes) noteFragment).deleteMultipleNotesLayout();
                ((notes) noteFragment).numberSelected(1, 0, -1);
            }
            else
                Helper.showMessage(activity, "Action not supported", "Can't multi-select" +
                        "notes, checklists, AND shareable checklists", MotionToast.TOAST_ERROR);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return allNotes.size();
    }

    // Checks to see if note is locked, if it is then user is sent to lock screen activity
    // where they need to enter a pin. If not locked, it opens note
    private void openNoteActivity(Note currentNote){
        if(currentNote.getPinNumber()==0){
            Intent note = new Intent(activity, NoteEdit.class);
            note.putExtra("id", currentNote.getNoteId());
            note.putExtra("isChecklist", currentNote.isCheckList());
            activity.startActivity(note);
        }
        else {
            Intent lockScreen = new Intent(activity, NoteLockScreen.class);
            lockScreen.putExtra("id", currentNote.getNoteId());
            lockScreen.putExtra("title", currentNote.getTitle().replace("\n", " "));
            lockScreen.putExtra("pin", currentNote.getPinNumber());
            lockScreen.putExtra("securityWord", currentNote.getSecurityWord());
            lockScreen.putExtra("fingerprint", currentNote.isFingerprint());
            activity.startActivity(lockScreen);
        }
    }

    // updates select status of note in database
    private void saveSelected(Note currentNote, boolean status){
        // save status to database
        realm.beginTransaction();
        currentNote.setSelected(status);
        realm.commitTransaction();
    }
}
