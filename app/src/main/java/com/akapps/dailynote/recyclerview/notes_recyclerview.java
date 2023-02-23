package com.akapps.dailynote.recyclerview;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.activity.NoteLockScreen;
import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.data.Folder;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.Photo;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.other.NoteInfoSheet;
import com.akapps.dailynote.fragments.notes;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.stfalcon.imageviewer.StfalconImageViewer;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import www.sanju.motiontoast.MotionToast;

public class notes_recyclerview extends RecyclerView.Adapter<notes_recyclerview.MyViewHolder>{

    // project data
    private final RealmResults<Note> allNotes;
    private String noteText;
    private final Activity activity;
    private final Fragment noteFragment;
    private boolean showPreview;
    private boolean showPreviewNotesInfo;

    // database
    private final Realm realm;

    // multi select
    private boolean enableSelectMultiple;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final ImageView note_info;
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
        private final LinearLayout preview_1_layout;
        private final LinearLayout preview_2_layout;
        private final LinearLayout preview_3_layout;

        public MyViewHolder(View v) {
            super(v);
            note_title = v.findViewById(R.id.note_title);
            note_info = v.findViewById(R.id.note_info);
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
            preview_1_layout = v.findViewById(R.id.preview_1_layout);
            preview_2_layout = v.findViewById(R.id.preview_2_layout);
            preview_3_layout = v.findViewById(R.id.preview_3_layout);
            view = v;
        }
    }

    public notes_recyclerview(RealmResults<Note> allNotes, Realm realm, Activity activity, Fragment fragment,
                              boolean showPreview, boolean showPreviewNotesInfo) {
        this.allNotes = allNotes;
        this.realm = realm;
        this.activity = activity;
        this.noteFragment = fragment;
        this.showPreview = showPreview;
        this.showPreviewNotesInfo = showPreviewNotesInfo;
    }

    @Override
    public notes_recyclerview.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_note_layout, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        // retrieves current note object
        Note currentNote = allNotes.get(position);

        // retrieves all photos that belong to note
        RealmResults<Photo> allPhotos = realm.where(Photo.class)
                .equalTo("noteId", currentNote.getNoteId()).findAll();

        // retrieves note text, the lock status of note and reminder status
        noteText = currentNote.getNote() == null ? "": currentNote.getNote();
        boolean isNoteLocked = currentNote.getPinNumber() > 0;
        boolean hasReminder = currentNote.getReminderDateTime().length() > 0;

        // populates note data into the recyclerview
        holder.note_title.setText(currentNote.getTitle().replaceAll("\n"," "));
        holder.note_edited.setText(currentNote.getDateEdited());
        holder.note_background.setCardBackgroundColor(currentNote.getBackgroundColor());

        if(Helper.isColorDark(currentNote.getBackgroundColor())) {
            holder.note_title.setTextColor(activity.getColor(R.color.white));
            holder.note_edited.setTextColor(activity.getColor(R.color.white));
            holder.note_preview.setTextColor(activity.getColor(R.color.white));
            holder.preview_photo_message.setTextColor(activity.getColor(R.color.white));
        }
        else{
            holder.note_title.setTextColor(activity.getColor(R.color.black));
            holder.note_edited.setTextColor(activity.getColor(R.color.gray));
            holder.note_preview.setTextColor(activity.getColor(R.color.gray));
            holder.preview_photo_message.setTextColor(activity.getColor(R.color.main));
        }

        if(((notes) noteFragment).user.isModeSettings())
            holder.note_info.setBackgroundColor(activity.getColor(R.color.black));
        else
            holder.note_info.setBackgroundColor(activity.getColor(R.color.not_too_dark_gray));

        // changes the number of lines title and preview occupy depending on user setting
        int titleLines = ((notes) noteFragment).user.getTitleLines();
        int contentLines = ((notes) noteFragment).user.getContentLines();
        holder.note_title.setMaxLines(titleLines);
        holder.note_preview.setMaxLines(contentLines);

        if(showPreviewNotesInfo)
            holder.note_info.setVisibility(View.VISIBLE);
        else
            holder.note_info.setVisibility(View.GONE);

        // format note to remove all new line characters and any spaces more than a length of 1
        String preview = Html.fromHtml(currentNote.getNote(), Html.FROM_HTML_MODE_COMPACT).toString();
        preview = preview.replaceAll("(\\s{2,})", " ");
        holder.note_preview.setText(preview);

        // if note has a category, then it shows it
        if(currentNote.getCategory().equals("none"))
            holder.category_background.setVisibility(View.GONE);
        else {
            Folder folderColor = realm.where(Folder.class)
                    .equalTo("name", currentNote.getCategory())
                    .findFirst();

            holder.category_background.setVisibility(View.VISIBLE);
            holder.category.setText(currentNote.getCategory());
            holder.category.setTextColor(folderColor.getColor() == 0 ?
                    activity.getColor(R.color.orange) : folderColor.getColor());
            holder.category_background.setStrokeColor(folderColor.getColor() == 0 ?
                    activity.getColor(R.color.orange) : folderColor.getColor());
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
            RealmResults<CheckListItem> checklist = currentNote.getChecklist()
                    .sort("positionInList", Sort.ASCENDING);
            if (!isNoteLocked) {
                holder.preview_photo_message.setVisibility(View.VISIBLE);
                holder.preview_photo_message.setText(checklist.size()+ " items");
            }
            for (int i = 0; i < checklist.size(); i++) {
                checkListString.append("• ").append(checklist.get(i).getText()).append("\n");
            }

            realm.beginTransaction();
            currentNote.setChecklistConvertedToString(checkListString.toString());
            realm.commitTransaction();
            holder.note_preview.setText(checkListString.toString());
            holder.note_preview.setTextSize(13);
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
        if(hasReminder) {
            holder.reminder_icon.setVisibility(View.VISIBLE);
            Date reminderDate = null;
            try {
                reminderDate = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").parse(currentNote.getReminderDateTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date now = new Date();
            if (now.after(reminderDate))
                holder.reminder_icon.setColorFilter(activity.getColor(R.color.red));
            else
                holder.reminder_icon.setColorFilter(activity.getColor(R.color.ocean_green));
        }
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

        if(currentNote.getTitle().isEmpty())
            holder.note_title.setVisibility(View.GONE);
        else
            holder.note_title.setVisibility(View.VISIBLE);

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
            holder.note_preview.setTextSize(13);
        }

        if(showPreview && !isNoteLocked){
            holder.preview_photo_message.setVisibility(View.VISIBLE);
            holder.note_preview.setVisibility(View.VISIBLE);
            holder.preview_photo_message.setText(currentNote.getChecklist().size()+ " items");
        }
        else {
            holder.note_preview.setVisibility(View.GONE);
            holder.preview_photo_message.setVisibility(View.GONE);
        }

        int preview_1_position = 0;
        int preview_2_position = 0;
        int preview_3_position = 0;

        // if note has photos, it displays them under note preview (up to 3)
        // if there are more, it shows a text underneath them with
        // the number of photos that are left
        if(allPhotos.size()>0 & !isNoteLocked && showPreview){
            if(allPhotos.size()==1) {
                holder.preview_1.setVisibility(View.GONE);
                holder.preview_3.setVisibility(View.GONE);
                holder.preview_2.setVisibility(View.VISIBLE);
                preview_2_position = 0;
                if(new File(allPhotos.get(0).getPhotoLocation()).exists()) {
                    Glide.with(activity).load(allPhotos.get(0).getPhotoLocation())
                            .centerCrop()
                            .placeholder(activity.getDrawable(R.drawable.error_icon))
                            .into(holder.preview_2);
                }
            }
            else if(allPhotos.size()==2) {
                holder.preview_1.setVisibility(View.VISIBLE);
                holder.preview_2.setVisibility(View.GONE);
                holder.preview_3.setVisibility(View.VISIBLE);
                preview_1_position = 0;
                preview_3_position = 1;

                if(new File(allPhotos.get(0).getPhotoLocation()).exists()) {
                    Glide.with(activity).load(allPhotos.get(0).getPhotoLocation())
                            .centerCrop()
                            .placeholder(activity.getDrawable(R.drawable.error_icon))
                            .into(holder.preview_1);
                }

                if(new File(allPhotos.get(1).getPhotoLocation()).exists()) {
                    Glide.with(activity).load(allPhotos.get(1).getPhotoLocation())
                            .centerCrop()
                            .placeholder(activity.getDrawable(R.drawable.error_icon))
                            .into(holder.preview_3);
                }
            }
            else{
                holder.preview_1.setVisibility(View.VISIBLE);
                holder.preview_2.setVisibility(View.VISIBLE);
                holder.preview_3.setVisibility(View.VISIBLE);

                preview_1_position = 0;
                preview_2_position = 1;
                preview_3_position = 2;

                if(new File(allPhotos.get(0).getPhotoLocation()).exists()) {
                    Glide.with(activity).load(allPhotos.get(0).getPhotoLocation())
                            .centerCrop()
                            .placeholder(activity.getDrawable(R.drawable.error_icon))
                            .into(holder.preview_1);
                }

                if(new File(allPhotos.get(1).getPhotoLocation()).exists()) {
                    Glide.with(activity).load(allPhotos.get(1).getPhotoLocation())
                            .centerCrop()
                            .placeholder(activity.getDrawable(R.drawable.error_icon))
                            .into(holder.preview_2);
                }

                if(new File(allPhotos.get(2).getPhotoLocation()).exists()) {
                    Glide.with(activity).load(allPhotos.get(2).getPhotoLocation())
                            .centerCrop()
                            .placeholder(activity.getDrawable(R.drawable.error_icon))
                            .into(holder.preview_3);
                }
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

        int finalPreview_1_position = preview_1_position;
        holder.preview_1_layout.setOnClickListener(view -> showPhotos(finalPreview_1_position, allPhotos, holder.preview_1));
        int finalPreview_2_position = preview_2_position;
        holder.preview_2_layout.setOnClickListener(view -> showPhotos(finalPreview_2_position, allPhotos, holder.preview_2));
        int finalPreview_3_position = preview_3_position;
        holder.preview_3_layout.setOnClickListener(view -> showPhotos(finalPreview_3_position, allPhotos, holder.preview_3));

        holder.preview_1_layout.setOnLongClickListener(view -> {
            // prevent opening of images when multi-selecting
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
            return false;
        });

        holder.preview_2_layout.setOnLongClickListener(view -> {
            // prevent opening of images when multi-selecting
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
            return false;
        });

        holder.preview_3_layout.setOnLongClickListener(view -> {
            // prevent opening of images when multi-selecting
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
            return false;
        });

        // if user is selecting multiple notes, it updates status of select in note,
        // changes outline color, and updates number of selected notes
        // if not, it opens note
        holder.view.setOnClickListener(v -> {
            enableSelectMultiple = ((notes) noteFragment).enableSelectMultiple;
            if(!enableSelectMultiple) {
                openNoteActivity(currentNote);
            }
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
            enableSelectMultiple = ((notes) noteFragment).enableSelectMultiple;
            // prevent opening of images when multi-selecting
            if(!enableSelectMultiple) {
                ((notes) noteFragment).unSelectAllNotes();
                enableSelectMultiple = true;
                saveSelected(currentNote, true);
                holder.note_background.setStrokeColor(activity.getColor(R.color.red));
                holder.note_background.setStrokeWidth(10);
                ((notes) noteFragment).deleteMultipleNotesLayout();
                ((notes) noteFragment).numberSelected(1, 0, -1);
            }
            return true;
        });

        holder.note_info.setOnClickListener(view -> {
            NoteInfoSheet noteInfoSheet = new NoteInfoSheet(((notes) noteFragment).user, currentNote, allPhotos, true);
            noteInfoSheet.show(noteFragment.getParentFragmentManager(), noteInfoSheet.getTag());
        });
    }

    private void showPhotos(int position, RealmResults<Photo> allPhotos, ImageView currentImage){
        if(!enableSelectMultiple) {
            ArrayList<String> images = new ArrayList<>();
            for (int i = 0; i < allPhotos.size(); i++) {
                if (!allPhotos.get(i).getPhotoLocation().isEmpty())
                    images.add(allPhotos.get(i).getPhotoLocation());
            }

            new StfalconImageViewer.Builder<>(noteFragment.getContext(), images, (imageView, image) ->
                    Glide.with(noteFragment.getContext()).load(image).into(imageView))
                    .withBackgroundColor(noteFragment.getContext().getColor(R.color.gray))
                    .allowZooming(true)
                    .allowSwipeToDismiss(true)
                    .withHiddenStatusBar(false)
                    .withStartPosition(position)
                    .withTransitionFrom(currentImage)
                    .show();
        }
    }

    @Override
    public int getItemCount() {
        return allNotes.size();
    }

    // Checks to see if note is locked, if it is then user is sent to lock screen activity
    // where they need to enter a pin. If not locked, it opens note
    private void openNoteActivity(Note currentNote){
        if(currentNote.getPinNumber() == 0){
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
