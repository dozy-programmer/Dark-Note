package com.akapps.dailynote.classes.other;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.SubCheckListItem;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.jetbrains.annotations.NotNull;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import io.realm.Realm;

public class ChecklistItemSheet extends RoundedBottomSheetDialogFragment{

    private CheckListItem currentItem;
    private Note currentNote;
    private boolean isAdding;
    private RecyclerView.Adapter adapter;
    private int position;

    private Realm realm;

    private TextInputEditText itemName;

    private SubCheckListItem currentSubItem;
    private boolean isSubChecklist;
    private String parentNode;

    // adding
    public ChecklistItemSheet(){
        isAdding = true;
        isSubChecklist = false;
    }

    public ChecklistItemSheet(String parentNode, boolean isSubChecklist, RecyclerView.Adapter adapter, int position){
        this.parentNode = parentNode;
        isAdding = true;
        this.isSubChecklist = isSubChecklist;
        this.adapter = adapter;
        this.position = position;
    }

    // editing note
    public ChecklistItemSheet(CheckListItem checkListItem, int position, RecyclerView.Adapter adapter){
        isAdding = false;
        isSubChecklist = false;
        this.currentItem = checkListItem;
        this.position = position;
        this.adapter = adapter;
    }

    // editing sub-note
    public ChecklistItemSheet(SubCheckListItem checkListItem, int position, RecyclerView.Adapter adapter){
        isAdding = false;
        isSubChecklist = true;
        this.currentSubItem = checkListItem;
        this.position = position;
        this.adapter = adapter;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_checklist_item, container, false);

        if(savedInstanceState != null) {
            isAdding = savedInstanceState.getBoolean("add");
            position = savedInstanceState.getInt("position");
        }

        realm = ((NoteEdit) getActivity()).realm;
        currentNote = ((NoteEdit)getActivity()).currentNote;

        view.setBackgroundColor(getContext().getColor(R.color.gray));

        MaterialButton confirmFilter = view.findViewById(R.id.confirm_filter);
        MaterialButton next = view.findViewById(R.id.next_confirm);
        ImageView delete = view.findViewById(R.id.delete);
        TextView info = view.findViewById(R.id.checklist_info);

        TextInputLayout itemNameLayout = view.findViewById(R.id.item_name_layout);
        itemName = view.findViewById(R.id.item_name);
        TextView title = view.findViewById(R.id.title);

        itemName.requestFocusFromTouch();

        if(isAdding){
            if(isSubChecklist)
                title.setText("Adding Sub-Item to\n" + parentNode);
            else
                title.setText("Adding");
            delete.setVisibility(View.GONE);
        }
        else{
            info.setVisibility(View.GONE);
            try {
                if(isSubChecklist){
                    title.setText("Editing Sub-Item");
                    itemName.setText(currentSubItem.getText());
                }
                else {
                    title.setText("Editing");
                    itemName.setText(currentItem.getText());
                }
                itemName.setSelection(itemName.getText().toString().length());
                delete.setVisibility(View.VISIBLE);
                next.setVisibility(View.GONE);
            }
            catch (Exception e){
                this.dismiss();
            }
        }

        delete.setOnClickListener(v-> {
            if(!isAdding) {
                if(isSubChecklist)
                    deleteItem(currentSubItem);
                else
                    deleteItem(currentItem);
                this.dismiss();
            }
        });

        confirmFilter.setOnClickListener(v -> {
            if(confirmEntry(itemName, itemNameLayout))
                this.dismiss();
        });

        next.setOnClickListener(v -> {
            if(confirmEntry(itemName, itemNameLayout)){
                this.dismiss();
                ChecklistItemSheet checklistItemSheet;
                if(isSubChecklist)
                    checklistItemSheet = new ChecklistItemSheet(parentNode, true, adapter, position);
                else
                    checklistItemSheet = new ChecklistItemSheet();
                checklistItemSheet.show(getActivity().getSupportFragmentManager(), checklistItemSheet.getTag());
            }
        });

        return view;
    }

    // updates select status of note in database
    private void updateItem(CheckListItem checkListItem, String text){
        // save status to database
        realm.beginTransaction();
        checkListItem.setText(text);
        currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
        realm.commitTransaction();
        ((NoteEdit)getActivity()).updateDateEdited();
        adapter.notifyItemChanged(position);
    }

    // updates select status of note in database
    private void updateItem(SubCheckListItem checkListItem, String text){
        // save status to database
        realm.beginTransaction();
        checkListItem.setText(text);
        currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
        realm.commitTransaction();
        ((NoteEdit)getActivity()).updateDateEdited();
        adapter.notifyItemChanged(position);
    }

    // updates select status of note in database
    private void deleteItem(CheckListItem checkListItem){
        // save status to database
        realm.beginTransaction();
        checkListItem.getSubChecklist().deleteAllFromRealm();
        checkListItem.deleteFromRealm();
        currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
        realm.commitTransaction();
        ((NoteEdit)getActivity()).updateDateEdited();
        adapter.notifyDataSetChanged();
    }

    // updates select status of sub-note in database
    private void deleteItem(SubCheckListItem checkListItem){
        // save status to database
        realm.beginTransaction();
        checkListItem.deleteFromRealm();
        currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
        realm.commitTransaction();
        ((NoteEdit)getActivity()).updateDateEdited();
        adapter.notifyDataSetChanged();
    }


    private boolean confirmEntry(TextInputEditText itemName, TextInputLayout itemNameLayout){
        if(!itemName.getText().toString().isEmpty()){
            if(isAdding) {
                String text = itemName.getText().toString();
                String[] items = text.replaceAll(" +, +", ",,").split(",,");
                if(isSubChecklist){
                    for (String item : items) {
                        Log.d("Here", "adding " + item);
                        ((NoteEdit) getActivity()).addSubCheckList(item.trim().replaceAll(" +", " "), position);
                    }
                }
                else {
                    for (String item : items)
                        ((NoteEdit) getActivity()).addCheckList(item.trim().replaceAll(" +", " "));
                }
            }
            else {
                if(isSubChecklist)
                    updateItem(currentSubItem, itemName.getText().toString());
                else
                    updateItem(currentItem, itemName.getText().toString());
            }
            return true;
        }
        else
            itemNameLayout.setError("Required");

        return false;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("add", isAdding);
        outState.putInt("position", position);
        itemName.clearFocus();
    }

    @Override
    public int getTheme() {
        return R.style.BaseBottomSheetDialog;
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