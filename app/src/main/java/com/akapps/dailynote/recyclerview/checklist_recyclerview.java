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
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.other.ChecklistItemSheet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import io.realm.Realm;
import io.realm.RealmResults;

public class checklist_recyclerview extends RecyclerView.Adapter<checklist_recyclerview.MyViewHolder>{

    // project data
    private final RealmResults<CheckListItem> checkList;
    private final Note currentNote;
    private Context context;
    private FragmentActivity activity;
    private final Realm realm;

    private RecyclerView.Adapter subChecklistAdapter;
    private GridLayoutManager layout;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView checklistText;
        private final ImageView selectedIcon;
        private final LinearLayout checkItem;
        private final LinearLayout edit;
        private final RecyclerView subChecklist;
        private final FloatingActionButton addSubChecklist;

        public MyViewHolder(View v) {
            super(v);
            checklistText = v.findViewById(R.id.note_Textview);
            selectedIcon = v.findViewById(R.id.check_status);
            checkItem = v.findViewById(R.id.checkItem);
            edit = v.findViewById(R.id.edit);
            subChecklist = v.findViewById(R.id.subchecklist);
            addSubChecklist = v.findViewById(R.id.add_subchecklist);
            subChecklist.setLayoutManager(new GridLayoutManager(v.getContext(), 1));
        }
    }

    public checklist_recyclerview(RealmResults<CheckListItem> checkList, Note currentNote, Realm realm, FragmentActivity activity) {
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

        isAllItemsSelected();

        // delete line 84+85 and uncomment to restore sub checklists
        holder.subChecklist.setVisibility(View.GONE);
        holder.addSubChecklist.setVisibility(View.GONE);
        /* comment start
        if(null == checkListItem.getSubChecklist() || checkListItem.getSubChecklist().size() == 0)
            holder.subChecklist.setVisibility(View.GONE);
        else {
            holder.subChecklist.setVisibility(View.VISIBLE);
            subChecklistAdapter = new sub_checklist_recyclerview(realm.where(SubCheckListItem.class)
                            .equalTo("id", currentNote.getChecklist().get(position).getSubListId())
                            .sort("positionInList").findAll(), currentNote, realm, activity);
            holder.subChecklist.setAdapter(subChecklistAdapter);
        }
         comment end */

        // retrieves checkList text and select status of checkListItem
        String checkListText = checkListItem.getText();
        boolean isSelected = checkListItem.isChecked();

        // populates note into the recyclerview
        holder.checklistText.setText(checkListText);
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

        holder.addSubChecklist.setOnClickListener(view -> {
            ChecklistItemSheet checklistItemSheet = new ChecklistItemSheet(true, subChecklistAdapter, position);
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

    }

    @Override
    public int getItemCount() {
        return checkList.size();
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
        ChecklistItemSheet checklistItemSheet = new ChecklistItemSheet(checkListItem, position, this);
        checklistItemSheet.show(activity.getSupportFragmentManager(), checklistItemSheet.getTag());
    }

    // determines if all items are select and if they are, checklist is set to check or "finished"
    private void isAllItemsSelected(){
        RealmResults<CheckListItem> select = realm.where(CheckListItem.class)
                .equalTo("checked", true)
                .equalTo("id", currentNote.getNoteId())
                .findAll();

        boolean isAllChecked = select.size() == checkList.size();
        if(select.size()==0 && checkList.size()==0)
            isAllChecked = false;

        if(currentNote.isChecked()!=isAllChecked) {
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
