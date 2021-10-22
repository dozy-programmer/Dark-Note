package com.akapps.dailynote.classes.other;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.CategoryScreen;
import com.akapps.dailynote.classes.data.Folder;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.helpers.Helper;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.jetbrains.annotations.NotNull;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import www.sanju.motiontoast.MotionToast;

public class FolderItemSheet extends RoundedBottomSheetDialogFragment{

    private Realm realm;
    private Folder currentItem;
    private  boolean isAdding;
    private RecyclerView.Adapter adapter;
    private int position;
    private int color;

    // firebase
    private RealmResults<Note> allSelectedNotes;
    private RealmResults<Folder> allCategories;

    private MaterialCardView folderColor;

    // adding
    public FolderItemSheet(){
        isAdding = true;
    }

    // editing
    public FolderItemSheet(Realm realm, Folder checkListItem, RecyclerView.Adapter adapter, int position){
        isAdding = false;
        this.realm = realm;
        this.currentItem = checkListItem;
        this.allSelectedNotes = realm.where(Note.class).equalTo("isSelected", true).findAll();
        this.adapter = adapter;
        this.position = position;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_folder_item, container, false);

        view.setBackgroundColor(getContext().getColor(R.color.gray));

        if(savedInstanceState!=null) {
            isAdding = savedInstanceState.getBoolean("add");
            position = savedInstanceState.getInt("pos");
        }

        if(isAdding) {
            realm = ((CategoryScreen) getActivity()).realm;
            adapter = ((CategoryScreen) getActivity()).categoriesAdapter;
            allCategories = realm.where(Folder.class).sort("positionInList").findAll();
        }

        ImageView closeFilter = view.findViewById(R.id.close_filter);
        folderColor = view.findViewById(R.id.folder_color);
        TextView resetFilter = view.findViewById(R.id.reset_filter);
        MaterialButton confirmFilter = view.findViewById(R.id.confirm_filter);
        MaterialButton next = view.findViewById(R.id.next_confirm);
        ImageView delete = view.findViewById(R.id.delete);

        TextInputLayout itemNameLayout = view.findViewById(R.id.item_name_layout);
        TextInputEditText itemName = view.findViewById(R.id.item_name);
        TextView title = view.findViewById(R.id.title);

        if(isAdding){
            title.setText("Adding");
            delete.setVisibility(View.GONE);
            itemName.requestFocus();
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
        else{
            title.setText("Editing");
            try{
                itemName.setText(currentItem.getName());
                folderColor.setCardBackgroundColor(currentItem.getColor()==0 ? getContext().getColor(R.color.gray) : currentItem.getColor());
                itemName.setSelection(itemName.getText().toString().length());
                delete.setVisibility(View.VISIBLE);
                next.setVisibility(View.GONE);
            }catch (Exception e){
                this.dismiss();
            }
        }

        folderColor.setOnClickListener(v -> editColorDialog(currentItem));

        closeFilter.setOnClickListener(v -> {
            this.dismiss();
        });

        resetFilter.setOnClickListener(v -> {
            if(itemName.getText().length()>0)
                itemName.getText().clear();
        });

        delete.setOnClickListener(v-> {
            if(!isAdding) {
                deleteCategory(currentItem);
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
                FolderItemSheet checklistItemSheet = new FolderItemSheet();
                checklistItemSheet.show(getActivity().getSupportFragmentManager(), checklistItemSheet.getTag());
            }
        });

        return view;
    }

    private void editColorDialog(Folder current){
        int initialColor;
        if(current==null || current.getColor()==0)
            initialColor = getContext().getColor(R.color.orange_red);
        else
            initialColor = current.getColor();

        // opens dialog to choose a color
        AlertDialog colorPickerView = ColorPickerDialogBuilder
                .with(getContext(), R.style.ColorPickerDialogTheme)
                .setTitle("Select Folder Color")
                .initialColor(initialColor)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(10)
                .setPositiveButton("SELECT", (dialog, selectedColor, allColors) -> {
                    if(isAdding) {
                        color = selectedColor;
                        folderColor.setCardBackgroundColor(color);
                    }
                    else {
                        realm.beginTransaction();
                        current.setColor(selectedColor);
                        realm.commitTransaction();
                        adapter.notifyItemChanged(position);
                        folderColor.setCardBackgroundColor(selectedColor);
                    }
                })
                .setNegativeButton("CLOSE", (dialog, which) ->
                        dialog.dismiss())
                .build();
        colorPickerView.show();
    }

    private void editCategory(Folder current, String newName){
        allSelectedNotes = realm.where(Note.class).equalTo("category", current.getName()).findAll();
        // update database
        realm.beginTransaction();
        allSelectedNotes.setString("category", newName);
        current.setName(newName);
        realm.commitTransaction();
        adapter.notifyItemChanged(position);
    }

    private void deleteCategory(Folder current){
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        allSelectedNotes = realm.where(Note.class).equalTo("category", current.getName()).findAll();
        // update database
        realm.beginTransaction();
        allSelectedNotes.setString("category", "none");
        current.deleteFromRealm();
        realm.commitTransaction();
        adapter.notifyItemRemoved(position);
    }

    // adds note
    private void addCategory(String itemText, int color) {
        // insert data to database
        RealmResults<Folder> results = realm.where(Folder.class)
                .contains("name", itemText, Case.INSENSITIVE).findAll();
        Folder newItem = new Folder(itemText, allCategories.size());
        newItem.setColor(color);
        if(results.size() == 0) {
            realm.beginTransaction();
            realm.insert(newItem);
            realm.commitTransaction();
            adapter.notifyItemInserted(allCategories.size()-1);
        }
        else
            Helper.showMessage(getActivity(), "Duplicate", "A category with that name exists",
                    MotionToast.TOAST_ERROR);
    }

    private boolean confirmEntry(TextInputEditText itemName, TextInputLayout itemNameLayout){

        if(!itemName.getText().toString().isEmpty()){
            if(isAdding)
                addCategory(itemName.getText().toString(), color);
            else
                editCategory(currentItem, itemName.getText().toString());
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
        outState.putInt("pos", position);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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