package com.akapps.dailynote.recyclerview;

import android.content.Context;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.SubCheckListItem;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmHelper;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
import com.akapps.dailynote.classes.other.ChecklistItemSheet;
import com.akapps.dailynote.classes.other.PlayAudioSheet;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.stfalcon.imageviewer.StfalconImageViewer;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import io.realm.RealmList;
import io.realm.RealmResults;

public class checklist_recyclerview extends RecyclerView.Adapter<checklist_recyclerview.MyViewHolder> {

    // project data
    private final RealmResults<CheckListItem> checkList;
    private final Note currentNote;
    private Context context;
    private final FragmentActivity activity;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView checklistText;
        private final TextView placeAttached;
        private final MaterialCheckBox selectedIcon;
        private final ImageView deleteIcon;
        private final LinearLayout checkItem;
        private final LinearLayout edit;
        private final LinearLayout locationLayout;
        private final RecyclerView subChecklist;
        private final FloatingActionButton addSubChecklist;
        private final FloatingActionButton audio;
        private final MaterialCardView itemImageLayout;
        private final ImageView itemImage;
        private final MaterialCardView background;

        public MyViewHolder(View v) {
            super(v);
            checklistText = v.findViewById(R.id.note_Textview);
            placeAttached = v.findViewById(R.id.place_info);
            locationLayout = v.findViewById(R.id.location_layout);
            selectedIcon = v.findViewById(R.id.check_status);
            checkItem = v.findViewById(R.id.checkItem);
            edit = v.findViewById(R.id.edit);
            subChecklist = v.findViewById(R.id.subchecklist);
            addSubChecklist = v.findViewById(R.id.add_subchecklist);
            audio = v.findViewById(R.id.audio);
            subChecklist.setLayoutManager(new GridLayoutManager(v.getContext(), 1));
            itemImageLayout = v.findViewById(R.id.item_image_layout);
            itemImage = v.findViewById(R.id.item_image);
            deleteIcon = v.findViewById(R.id.delete_checklist_item);
            background = v.findViewById(R.id.background);
        }
    }

    public checklist_recyclerview(RealmResults<CheckListItem> checkList, Note currentNote, FragmentActivity activity) {
        this.checkList = checkList;
        this.currentNote = currentNote;
        this.activity = activity;
    }

    @Override
    public checklist_recyclerview.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_checklist_layout, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        context = parent.getContext();
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        CheckListItem checkListItem = checkList.get(position);

        // search for duplicate sublist id
        int duplicateSize = RealmSingleton.getInstance(context).where(CheckListItem.class)
                .equalTo("subListId", checkListItem.getSubListId()).findAll().size();
        if (duplicateSize > 1) {
            Random rand = new Random();
            RealmSingleton.getInstance(context).beginTransaction();
            checkListItem.setSubListId(rand.nextInt(100000) + 1);
            RealmSingleton.getInstance(context).commitTransaction();
        }

        if (checkListItem.getPlace() != null && !checkListItem.getPlace().getPlaceName().isEmpty()) {
            holder.locationLayout.setVisibility(View.VISIBLE);
            holder.placeAttached.setText(checkListItem.getPlace().getPlaceName());
        } else
            holder.locationLayout.setVisibility(View.GONE);

        boolean recordingExists = false;
        if (checkListItem.getAudioPath() != null)
            recordingExists = new File(checkListItem.getAudioPath()).length() > 0;

        if (null != checkListItem.getAudioPath() && !checkListItem.getAudioPath().isEmpty() && recordingExists) {
            holder.audio.setVisibility(View.VISIBLE);
            holder.selectedIcon.setVisibility(View.GONE);
        } else {
            holder.audio.setVisibility(View.GONE);
            holder.selectedIcon.setVisibility(View.VISIBLE);

            if (null != checkListItem.getAudioPath() && !checkListItem.getAudioPath().isEmpty()) {
                RealmSingleton.getInstance(context).beginTransaction();
                checkListItem.setAudioPath("");
                checkListItem.setAudioDuration(0);
                RealmSingleton.getInstance(context).commitTransaction();
            }
        }

        if (RealmHelper.getUser(context, "checklist_recyclerview").isEnableDeleteIcon())
            holder.deleteIcon.setVisibility(View.VISIBLE);
        else
            holder.deleteIcon.setVisibility(View.GONE);

        if (RealmHelper.getUser(context, "checklist_recyclerview").getScreenMode() == User.Mode.Dark) {
            holder.background.setCardBackgroundColor(activity.getColor(R.color.darker_mode));
            holder.background.setStrokeColor(activity.getColor(R.color.light_gray));
            holder.background.setStrokeWidth(5);
        } else if (RealmHelper.getUser(context, "checklist_recyclerview").getScreenMode() == User.Mode.Light) {
        }

        holder.subChecklist.setAdapter(null);
        RecyclerView.Adapter subChecklistAdapter = null;
        if (RealmHelper.getUser(context, "checklist_recyclerview").isEnableSublists() && currentNote.isEnableSublist()) {
            holder.addSubChecklist.setVisibility(View.VISIBLE);
            if (null == checkListItem.getSubChecklist()) {
                RealmSingleton.getInstance(context).beginTransaction();
                checkListItem.setSubChecklist(new RealmList<>());
                RealmSingleton.getInstance(context).commitTransaction();
                holder.subChecklist.setVisibility(View.GONE);
            } else {
                if (checkListItem.getSubChecklist().size() != 0) {
                    holder.subChecklist.setVisibility(View.VISIBLE);
                    subChecklistAdapter = new sub_checklist_recyclerview(RealmSingleton.getInstance(context).where(SubCheckListItem.class)
                            .equalTo("id", checkListItem.getSubListId())
                            .sort("positionInList").findAll(), currentNote, activity);
                    holder.subChecklist.setAdapter(subChecklistAdapter);
                    subChecklistAdapter.notifyItemChanged(position);
                }
            }
        } else {
            holder.subChecklist.setVisibility(View.GONE);
            holder.addSubChecklist.setVisibility(View.GONE);
        }

        // retrieves checkList text and select status of checkListItem
        String checkListText = checkListItem.getText();
        boolean isSelected = checkListItem.isChecked();

        // populates note into the recyclerview
        holder.checklistText.setText(recordingExists && checkListItem.getText().isEmpty() ? "[Audio]" : checkListText);

        String textSize = Helper.getPreference(context, "size");
        if (textSize == null)
            textSize = "20";
        holder.checklistText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Integer.parseInt(textSize));

        // if note is selected, then it shows a strike through the text, changes the icon
        // to be filled and changes text color to gray
        if (isSelected) {
            holder.checklistText.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.SUBPIXEL_TEXT_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            holder.checklistText.setTextColor(Helper.darkenColor(currentNote.getTextColor(), 100));
            if (RealmHelper.getUser(context, "checklist_recyclerview").isShowChecklistCheckbox()) {
                holder.selectedIcon.setBackground(context.getDrawable(R.drawable.icon_checkbox_checked));
            } else {
                holder.selectedIcon.setBackground(context.getDrawable(R.drawable.checked_icon));
            }
        } else {
            holder.checklistText.setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            holder.checklistText.setTextColor(currentNote.getTextColor());
            if (RealmHelper.getUser(context, "checklist_recyclerview").isShowChecklistCheckbox()) {
                holder.selectedIcon.setBackground(context.getDrawable(R.drawable.icon_checkbox_unchecked));
            } else {
                holder.selectedIcon.setBackground(context.getDrawable(R.drawable.unchecked_icon));
            }
        }

        // show if checklist item has an image
        if (checkListItem.getItemImage() != null && !checkListItem.getItemImage().isEmpty()) {
            holder.itemImageLayout.setVisibility(View.VISIBLE);
            Glide.with(context).load(checkListItem.getItemImage()).into(holder.itemImage);
        } else
            holder.itemImageLayout.setVisibility(View.GONE);

        holder.audio.setOnClickListener(view -> {
            if (holder.audio.getVisibility() == View.VISIBLE) {
                PlayAudioSheet playAudioSheet = new PlayAudioSheet(checkListItem);
                playAudioSheet.show(activity.getSupportFragmentManager(), playAudioSheet.getTag());
            }
        });

        RecyclerView.Adapter finalSubChecklistAdapter = subChecklistAdapter;
        holder.addSubChecklist.setOnClickListener(view -> {
            ChecklistItemSheet checklistItemSheet = new ChecklistItemSheet(checkListItem, checkListText, true, finalSubChecklistAdapter);
            checklistItemSheet.show(activity.getSupportFragmentManager(), checklistItemSheet.getTag());
        });

        // if checklist item is clicked, then it updates the status of the item
        holder.checkItem.setOnClickListener(v -> {
            updateChecklistStatus(checkListItem, !isSelected, position);
        });

        // if checklist item is clicked, then it updates the status of the item
        // this is added to support clickable links
        holder.checklistText.setOnClickListener(v -> {
            updateChecklistStatus(checkListItem, isSelected, position);
        });

        holder.edit.setOnClickListener(v -> {
            openEditDialog(checkListItem, position);
        });

        holder.selectedIcon.setOnClickListener(view -> updateChecklistStatus(checkListItem, isSelected, position));

        holder.itemImageLayout.setOnClickListener(view -> {
            ArrayList<String> images = new ArrayList<>();
            images.add(checkListItem.getItemImage());
            new StfalconImageViewer.Builder<>(context, images, (imageView, image) ->
                    Glide.with(context).load(image).into(imageView))
                    .withBackgroundColor(context.getColor(R.color.gray))
                    .allowZooming(true)
                    .allowSwipeToDismiss(true)
                    .withHiddenStatusBar(false)
                    .withStartPosition(0)
                    .withTransitionFrom(holder.itemImage)
                    .show();
        });

        holder.deleteIcon.setOnClickListener(view -> {
            RealmHelper.deleteChecklistItem(checkListItem, context, false);
            RealmSingleton.getInstance(context).beginTransaction();
            currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
            RealmSingleton.getInstance(context).commitTransaction();
            ((NoteEdit) activity).updateDateEdited();
            notifyDataSetChanged();
        });

        holder.locationLayout.setOnClickListener(view -> Helper.openMapView(activity, checkListItem.getPlace()));
    }

    @Override
    public int getItemCount() {
        return checkList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private void updateChecklistStatus(CheckListItem checkListItem, boolean isSelected, int position) {
        saveSelected(checkListItem, !isSelected);
        isAllItemsSelected();
        notifyDataSetChanged();
    }

    // updates select status of note in database
    private void saveSelected(CheckListItem checkListItem, boolean status) {
        // save status to database
        RealmSingleton.getInstance(context).beginTransaction();
        checkListItem.setChecked(status);

        if (null != checkListItem.getSubChecklist()) {
            if (checkListItem.getSubChecklist().size() > 0) {
                RealmResults<SubCheckListItem> subCheckListItems = RealmSingleton.getInstance(context).where(SubCheckListItem.class).equalTo("id", checkListItem.getSubListId()).findAll();
                subCheckListItems.setBoolean("checked", status);
            }
        }

        if (status)
            checkListItem.setLastCheckedDate(Helper.dateToCalender(Helper.getCurrentDate()).getTimeInMillis());
        else
            checkListItem.setLastCheckedDate(0);
        RealmSingleton.getInstance(context).commitTransaction();

        ((NoteEdit) context).updateSaveDateEdited();
    }

    // opens dialog that allows user to edit or delete checklist item
    private void openEditDialog(CheckListItem checkListItem, int position) {
        ChecklistItemSheet checklistItemSheet = new ChecklistItemSheet(checkListItem, position, this);
        checklistItemSheet.show(activity.getSupportFragmentManager(), checklistItemSheet.getTag());
    }

    // determines if all items are select and if they are, checklist is set to check or "finished"
    private void isAllItemsSelected() {
        RealmResults<CheckListItem> select = checkList.where()
                .equalTo("checked", true)
                .findAll();

        boolean isAllChecked = select.size() == checkList.size();
        if (select.size() == 0 && checkList.size() == 0)
            isAllChecked = false;

        if (currentNote.isChecked() != isAllChecked) {
            RealmSingleton.getInstance(context).beginTransaction();
            currentNote.setChecked(isAllChecked);
            RealmSingleton.getInstance(context).commitTransaction();

            if (currentNote.isChecked())
                ((NoteEdit) context).title.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.SUBPIXEL_TEXT_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            else
                ((NoteEdit) context).title.setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        }
    }
}
