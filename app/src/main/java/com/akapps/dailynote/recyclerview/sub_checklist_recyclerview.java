package com.akapps.dailynote.recyclerview;

import android.content.Context;
import android.graphics.Paint;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.SubCheckListItem;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmHelper;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
import com.akapps.dailynote.classes.other.ChecklistItemSheet;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.regex.Pattern;

import io.realm.RealmResults;

public class sub_checklist_recyclerview extends RecyclerView.Adapter<sub_checklist_recyclerview.MyViewHolder> {

    // project data
    private final RealmResults<SubCheckListItem> checkList;
    private final CheckListItem parentSublistListItem;
    private final Note currentNote;
    private Context context;
    private final FragmentActivity activity;
    private final int parentPosition;
    private final String searchingForWord;
    public RecyclerView.Adapter parentChecklistAdapter;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView checklistText;
        private final MaterialCheckBox selectedIcon;
        private final LinearLayout checkItem;
        private final LinearLayout edit;
        private final MaterialCardView background;

        public MyViewHolder(View v) {
            super(v);
            checklistText = v.findViewById(R.id.note_Textview);
            selectedIcon = v.findViewById(R.id.check_status);
            checkItem = v.findViewById(R.id.checkItem);
            edit = v.findViewById(R.id.edit);
            background = v.findViewById(R.id.background);
        }
    }

    public sub_checklist_recyclerview(RecyclerView.Adapter checklistAdapter, CheckListItem parentSublistListItem, RealmResults<SubCheckListItem> checkList, Note currentNote,
                                      FragmentActivity activity, int parentPosition, String searchingForWord) {
        this.parentChecklistAdapter = checklistAdapter;
        this.parentSublistListItem = parentSublistListItem;
        this.checkList = checkList;
        this.currentNote = currentNote;
        this.activity = activity;
        this.parentPosition = parentPosition;
        this.searchingForWord = searchingForWord;
    }

    @Override
    public sub_checklist_recyclerview.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_subchecklist_layout, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        context = parent.getContext();
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        SubCheckListItem checkListItem = checkList.get(position);

        if(checkListItem == null || !checkListItem.isValid()){
            notifyDataSetChanged();
        }

        // retrieves checkList text and select status of checkListItem
        String checkListText = checkListItem.getText();
        boolean isSelected = checkListItem.isChecked();

        // populates note into the recyclerview
        holder.checklistText.setText(checkListText);
        holder.checklistText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, RealmHelper.getUserTextSize(context));

        if (!searchingForWord.isEmpty() && checkListText.toLowerCase().contains(searchingForWord.toLowerCase())) {
            String regex = "(" + searchingForWord + ")";
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

            String modifiedString = pattern.matcher(holder.checklistText.getText()).replaceAll("<font color='#8CA9CF'><b>$1</b></font>");
            holder.checklistText.setText(Html.fromHtml(modifiedString, Html.FROM_HTML_MODE_COMPACT));
            holder.background.setStrokeColor(context.getColor(R.color.azure));
            AppData.addWordFoundPositions(parentPosition);
        }

        // if note is selected, then it shows a strike through the text, changes the icon
        // to be filled and changes text color to gray
        if (isSelected) {
            holder.checklistText.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.SUBPIXEL_TEXT_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            holder.checklistText.setTextColor(Helper.darkenColor(RealmHelper.getTextColorBasedOnTheme(context, currentNote.getNoteId()), 100));
            if (RealmHelper.getUser(context, "sub_checklist_recyclerview").isShowChecklistCheckbox()) {
                holder.selectedIcon.setBackground(context.getDrawable(R.drawable.icon_checkbox_checked));
            } else {
                holder.selectedIcon.setBackground(context.getDrawable(R.drawable.checked_icon));
            }
        } else {
            holder.checklistText.setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            holder.checklistText.setTextColor(Helper.darkenColor(RealmHelper.getTextColorBasedOnTheme(context, currentNote.getNoteId()), 200));
            if (RealmHelper.getUser(context, "sub_checklist_recyclerview").isShowChecklistCheckbox()) {
                holder.selectedIcon.setBackground(context.getDrawable(R.drawable.icon_checkbox_unchecked));
            } else {
                holder.selectedIcon.setBackground(context.getDrawable(R.drawable.unchecked_icon));
            }
        }

        // if checklist item is clicked, then it updates the status of the item
        holder.checkItem.setOnClickListener(v -> {
            saveSelected(checkListItem, !isSelected);
            notifyItemChanged(position);
        });
        // if checklist item is clicked, then it updates the status of the item
        // this is added to support clickable links
        holder.checklistText.setOnClickListener(v -> {
            saveSelected(checkListItem, !isSelected);
            notifyItemChanged(position);
        });

        holder.checkItem.setOnLongClickListener(view -> {
            if (parentSublistListItem.getSubChecklist().size() != 0) {
                updateSublistView(parentSublistListItem);
                parentChecklistAdapter.notifyItemChanged(parentPosition);
            }
            return true;
        });
        holder.checklistText.setOnLongClickListener(view -> {
            if (parentSublistListItem.getSubChecklist().size() != 0) {
                updateSublistView(parentSublistListItem);
                parentChecklistAdapter.notifyItemChanged(parentPosition);
            }
            return true;
        });

        holder.edit.setOnClickListener(v -> {
            openEditDialog(parentSublistListItem, checkListItem, position);
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

    // updates select status of note in database
    private void saveSelected(SubCheckListItem checkListItem, boolean status) {
        // save status to database
        RealmSingleton.getInstance(context).beginTransaction();
        checkListItem.setChecked(status);
        RealmSingleton.getInstance(context).commitTransaction();
        ((NoteEdit) context).updateSaveDateEdited();
    }

    private void updateSublistView(CheckListItem checkListItem) {
        RealmSingleton.getInstance(context).beginTransaction();
        checkListItem.setSublistExpanded(true);
        RealmSingleton.getInstance(context).commitTransaction();
    }

    // opens dialog that allows user to edit or delete checklist item
    private void openEditDialog(CheckListItem parentSubItem, SubCheckListItem checkListItem, int position) {
        ChecklistItemSheet checklistItemSheet = new ChecklistItemSheet(parentSubItem, checkListItem, position, this);
        checklistItemSheet.show(activity.getSupportFragmentManager(), checklistItemSheet.getTag());
    }
}
