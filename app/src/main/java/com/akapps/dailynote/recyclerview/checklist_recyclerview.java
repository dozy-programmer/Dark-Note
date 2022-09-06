package com.akapps.dailynote.recyclerview;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
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
import com.akapps.dailynote.classes.data.SubCheckListItem;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.other.ChecklistItemSheet;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.stfalcon.imageviewer.StfalconImageViewer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class checklist_recyclerview extends RecyclerView.Adapter<checklist_recyclerview.MyViewHolder>{

    // project data
    private final RealmResults<CheckListItem> checkList;
    private final Note currentNote;
    private Context context;
    private FragmentActivity activity;
    private final Realm realm;
    private User user;

    private GridLayoutManager layout;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView checklistText;
        private final ImageView selectedIcon;
        private final LinearLayout checkItem;
        private final LinearLayout edit;
        private final RecyclerView subChecklist;
        private final FloatingActionButton addSubChecklist;
        private final MaterialCardView itemImageLayout;
        private final ImageView itemImage;
        private final TextView dateCreated;

        public MyViewHolder(View v) {
            super(v);
            checklistText = v.findViewById(R.id.note_Textview);
            selectedIcon = v.findViewById(R.id.check_status);
            checkItem = v.findViewById(R.id.checkItem);
            edit = v.findViewById(R.id.edit);
            subChecklist = v.findViewById(R.id.subchecklist);
            addSubChecklist = v.findViewById(R.id.add_subchecklist);
            subChecklist.setLayoutManager(new GridLayoutManager(v.getContext(), 1));
            itemImageLayout = v.findViewById(R.id.item_image_layout);
            itemImage = v.findViewById(R.id.item_image);
            dateCreated = v.findViewById(R.id.date_created);
        }
    }

    public checklist_recyclerview(User user, RealmResults<CheckListItem> checkList, Note currentNote, Realm realm, FragmentActivity activity) {
        this.user = user;
        this.checkList = checkList;
        this.currentNote = currentNote;
        this.realm = realm;
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
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        CheckListItem checkListItem = checkList.get(position);

        // search for duplicate sublist id
        int duplicateSize = realm.where(CheckListItem.class)
                .equalTo("subListId", checkListItem.getSubListId()).findAll().size();
        if(duplicateSize > 1){
            Random rand = new Random();
            realm.beginTransaction();
            checkListItem.setSubListId(rand.nextInt(100000) + 1);
            realm.commitTransaction();
            Log.d("Here", "Changed duplicate at position " + position);
        }

        holder.subChecklist.setAdapter(null);

        RecyclerView.Adapter subChecklistAdapter = null;
        if(user.isProUser() && user.isEnableSublists() && currentNote.isEnableSublist()) {
            holder.addSubChecklist.setVisibility(View.VISIBLE);
            if (null == checkListItem.getSubChecklist()) {
                realm.beginTransaction();
                checkListItem.setSubChecklist(new RealmList<>());
                realm.commitTransaction();
                holder.subChecklist.setVisibility(View.GONE);
            }
            else {
                if(checkListItem.getSubChecklist().size() != 0) {
                    holder.subChecklist.setVisibility(View.VISIBLE);
                    subChecklistAdapter = new sub_checklist_recyclerview(checkListItem.getText(), realm.where(SubCheckListItem.class)
                            .equalTo("id", checkListItem.getSubListId())
                            .sort("positionInList").findAll(), currentNote, realm, activity);
                    holder.subChecklist.setAdapter(subChecklistAdapter);
                    subChecklistAdapter.notifyItemChanged(position);
                }
            }
        }
        else{
            holder.subChecklist.setVisibility(View.GONE);
            holder.addSubChecklist.setVisibility(View.GONE);
        }


        // checks to see if there is a reminder and makes sure it has not passed
        if (!currentNote.getReminderDateTime().isEmpty()) {
            Date reminderDate = null;
            try {
                reminderDate = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").parse(currentNote.getReminderDateTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date now = new Date();
            if (now.after(reminderDate)) {
                realm.beginTransaction();
                currentNote.setReminderDateTime("");
                realm.commitTransaction();
            }
        }

        // retrieves checkList text and select status of checkListItem
        String checkListText = checkListItem.getText();
        boolean isSelected = checkListItem.isChecked();

        // populates note into the recyclerview
        holder.checklistText.setText(checkListText);

        if(checkListItem.getDateCreated() != null) {
            if (!checkListItem.getDateCreated().isEmpty()) {
                holder.dateCreated.setText(checkListItem.getDateCreated());
                holder.dateCreated.setVisibility(View.VISIBLE);
            }
            else
                holder.dateCreated.setVisibility(View.GONE);
        }
        else
            holder.dateCreated.setVisibility(View.GONE);

        String textSize = Helper.getPreference(context, "size");
        if(textSize==null)
            textSize = "20";
        holder.checklistText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Integer.parseInt(textSize));

        // if note is selected, then it shows a strike through the text, changes the icon
        // to be filled and changes text color to gray
        if(isSelected) {
            holder.checklistText.setPaintFlags(holder.checklistText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.checklistText.setTextColor(context.getColor(R.color.gray));
            holder.selectedIcon.setImageDrawable(context.getDrawable(R.drawable.checked_icon));
        }
        else {
            holder.checklistText.setPaintFlags(0);
            holder.selectedIcon.setImageDrawable(context.getDrawable(R.drawable.unchecked_icon));
            holder.checklistText.setTextColor(currentNote.getTextColor());
        }

        // show if checklist item has an image
        if(user.isProUser() && checkListItem.getItemImage()!=null && !checkListItem.getItemImage().isEmpty()) {
            holder.itemImageLayout.setVisibility(View.VISIBLE);
            Glide.with(context).load(checkListItem.getItemImage()).into(holder.itemImage);
        }
        else
            holder.itemImageLayout.setVisibility(View.GONE);

        RecyclerView.Adapter finalSubChecklistAdapter = subChecklistAdapter;
        holder.addSubChecklist.setOnClickListener(view -> {
            ChecklistItemSheet checklistItemSheet = new ChecklistItemSheet(checkListItem, checkListText, true, finalSubChecklistAdapter);
            checklistItemSheet.show(activity.getSupportFragmentManager(), checklistItemSheet.getTag());
        });

        // if checklist item is clicked, then it updates the status of the item
        holder.checkItem.setOnClickListener(v -> {
            saveSelected(checkListItem, !isSelected);
            isAllItemsSelected();
            if(currentNote.getSort() == 3 || currentNote.getSort() == 4)
                notifyDataSetChanged();
            else
                notifyItemChanged(position);
        });

        holder.edit.setOnClickListener(v -> {
            openEditDialog(checkListItem, position);
        });

        holder.itemImageLayout.setOnClickListener(view -> {
            ArrayList<String> images = new ArrayList<>();
            images.add(checkListItem.getItemImage());
            new StfalconImageViewer.Builder<>(context, images, (imageView, image) ->
                    Glide.with(context)
                            .load(image)
                            .into(imageView))
                    .withBackgroundColor(context.getColor(R.color.gray))
                    .allowZooming(true)
                    .allowSwipeToDismiss(true)
                    .withHiddenStatusBar(false)
                    .withStartPosition(0)
                    .withTransitionFrom(holder.itemImage)
                    .show();
        });

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

    // updates select status of note in database
    private void saveSelected(CheckListItem checkListItem, boolean status){
        // save status to database
        realm.beginTransaction();
        checkListItem.setChecked(status);
        if(status)
            checkListItem.setLastCheckedDate(Helper.dateToCalender(Helper.getCurrentDate()).getTimeInMillis());
        else
            checkListItem.setLastCheckedDate(0);
        currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
        realm.commitTransaction();
        ((NoteEdit)context).updateDateEdited();
    }

    // opens dialog that allows user to edit or delete checklist item
    private void openEditDialog(CheckListItem checkListItem, int position){
        ChecklistItemSheet checklistItemSheet = new ChecklistItemSheet(checkListItem, position, user.isProUser(), this);
        checklistItemSheet.show(activity.getSupportFragmentManager(), checklistItemSheet.getTag());
    }

    // determines if all items are select and if they are, checklist is set to check or "finished"
    private void isAllItemsSelected(){
        RealmResults<CheckListItem> select = checkList.where()
                .equalTo("checked", true)
                .findAll();

        boolean isAllChecked = select.size() == checkList.size();
        if(select.size()==0 && checkList.size()==0)
            isAllChecked = false;

        if(currentNote.isChecked() != isAllChecked) {
            realm.beginTransaction();
            currentNote.setChecked(isAllChecked);
            realm.commitTransaction();

            if(currentNote.isChecked())
                ((NoteEdit)context).title.setPaintFlags(((NoteEdit)context).title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            else
                ((NoteEdit)context).title.setPaintFlags(0);
        }
    }
}
