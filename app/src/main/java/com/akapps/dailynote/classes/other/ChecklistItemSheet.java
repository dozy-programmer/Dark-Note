package com.akapps.dailynote.classes.other;

import android.content.Context;
import android.os.Bundle;
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

    // adding
    public ChecklistItemSheet(){
        isAdding = true;
    }

    // editing
    public ChecklistItemSheet(CheckListItem checkListItem, int position){
        isAdding = false;
        this.currentItem = checkListItem;
        this.position = position;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_checklist_item, container, false);

        if(savedInstanceState!=null) {
            isAdding = savedInstanceState.getBoolean("add");
            position = savedInstanceState.getInt("position");
        }

        realm = ((NoteEdit)getActivity()).realm;
        adapter = ((NoteEdit)getActivity()).checklistAdapter;
        currentNote = ((NoteEdit)getActivity()).currentNote;

        view.setBackgroundColor(getContext().getColor(R.color.gray));

        ImageView closeFilter = view.findViewById(R.id.close_filter);
        TextView resetFilter = view.findViewById(R.id.reset_filter);
        MaterialButton confirmFilter = view.findViewById(R.id.confirm_filter);
        TextView next = view.findViewById(R.id.next_confirm);
        ImageView delete = view.findViewById(R.id.delete);

        TextInputLayout itemNameLayout = view.findViewById(R.id.item_name_layout);
        itemName = view.findViewById(R.id.item_name);
        TextView title = view.findViewById(R.id.title);

        itemName.requestFocusFromTouch();

        if(isAdding){
            title.setText("Adding");
            delete.setVisibility(View.GONE);
        }
        else{
            try {
                title.setText("Editing");
                itemName.setText(currentItem.getText());
                itemName.setSelection(itemName.getText().toString().length());
                delete.setVisibility(View.VISIBLE);
                next.setVisibility(View.GONE);
            }
            catch (Exception e){
                this.dismiss();
            }
        }

        closeFilter.setOnClickListener(v -> {
            this.dismiss();
        });

        resetFilter.setOnClickListener(v -> {
            if(itemName.getText().length()>0)
                itemName.getText().clear();
        });

        delete.setOnClickListener(v-> {
            if(!isAdding) {
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
                ChecklistItemSheet checklistItemSheet = new ChecklistItemSheet();
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
    private void deleteItem(CheckListItem checkListItem){
        // save status to database
        realm.beginTransaction();
        checkListItem.deleteFromRealm();
        currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
        realm.commitTransaction();
        ((NoteEdit)getActivity()).updateDateEdited();
        adapter.notifyItemRemoved(position);
    }


    private boolean confirmEntry(TextInputEditText itemName, TextInputLayout itemNameLayout){

        if(!itemName.getText().toString().isEmpty()){
            if(isAdding) {
                String text = itemName.getText().toString();
                String[] items = text.replaceAll(" +, +", ",,").split(",,");
                for (String item : items)
                    ((NoteEdit) getActivity()).addCheckList(item.trim().replaceAll(" +", " "));
                adapter.notifyItemInserted(adapter.getItemCount()+1);
            }
            else {
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