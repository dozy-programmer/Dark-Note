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
import androidx.recyclerview.widget.RecyclerView;
import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.SubCheckListItem;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.other.ChecklistItemSheet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import io.realm.Realm;
import io.realm.RealmResults;

public class sub_checklist_recyclerview extends RecyclerView.Adapter<sub_checklist_recyclerview.MyViewHolder>{

    // project data
    private final RealmResults<SubCheckListItem> checkList;
    private final Note currentNote;
    private Context context;
    private FragmentActivity activity;
    private final Realm realm;
    private String parentNode;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView checklistText;
        private final ImageView selectedIcon;
        private final LinearLayout checkItem;
        private final LinearLayout edit;

        public MyViewHolder(View v) {
            super(v);
            checklistText = v.findViewById(R.id.note_Textview);
            selectedIcon = v.findViewById(R.id.check_status);
            checkItem = v.findViewById(R.id.checkItem);
            edit = v.findViewById(R.id.edit);
        }
    }

    public sub_checklist_recyclerview(String parentNode, RealmResults<SubCheckListItem> checkList, Note currentNote, Realm realm, FragmentActivity activity) {
        this.parentNode = parentNode;
        this.checkList = checkList;
        this.currentNote = currentNote;
        this.realm = realm;
        this.activity = activity;
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
            holder.checklistText.setTextColor(context.getColor(R.color.light_gray_2));
            holder.selectedIcon.setImageDrawable(context.getDrawable(R.drawable.checked_icon));
        }
        else {
            holder.checklistText.setPaintFlags(0);
            holder.selectedIcon.setImageDrawable(context.getDrawable(R.drawable.unchecked_icon));
            holder.checklistText.setTextColor(currentNote.getTextColor());
        }

        // if checklist item is clicked, then it updates the status of the item
        holder.checkItem.setOnClickListener(v -> {
            saveSelected(checkListItem, !isSelected);
            notifyItemChanged(position);
            Log.d("Here", "Sublist id " + checkListItem.getId());
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
    private void saveSelected(SubCheckListItem checkListItem, boolean status){
        // save status to database
        realm.beginTransaction();
        checkListItem.setChecked(status);
        currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
        realm.commitTransaction();
        ((NoteEdit)context).updateDateEdited();
    }

    // opens dialog that allows user to edit or delete checklist item
    private void openEditDialog(SubCheckListItem checkListItem, int position){
        ChecklistItemSheet checklistItemSheet = new ChecklistItemSheet(checkListItem, position, this);
        checklistItemSheet.show(activity.getSupportFragmentManager(), checklistItemSheet.getTag());
    }
}
