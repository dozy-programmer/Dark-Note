package com.akapps.dailynote.classes.other;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.CategoryScreen;
import com.akapps.dailynote.classes.data.Folder;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
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

public class FolderItemSheet extends RoundedBottomSheetDialogFragment {

    private Realm realm;
    private Folder currentItem;
    private boolean isAdding;
    private RecyclerView.Adapter adapter;
    private int position;
    private int color;

    // firebase
    private RealmResults<Note> allSelectedNotes;
    private RealmResults<Folder> allCategories;

    private MaterialCardView folderColor;

    // adding
    public FolderItemSheet() {
        isAdding = true;
    }

    // layout
    private TextInputEditText itemName;

    // editing
    public FolderItemSheet(Folder checkListItem, RecyclerView.Adapter adapter, int position) {
        isAdding = false;
        this.currentItem = checkListItem;
        this.allSelectedNotes = RealmSingleton.getInstance(getContext()).where(Note.class).equalTo("isSelected", true).findAll();
        this.adapter = adapter;
        this.position = position;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_folder_item, container, false);

        if (savedInstanceState != null) {
            isAdding = savedInstanceState.getBoolean("add");
            position = savedInstanceState.getInt("pos");
        }

        realm = RealmSingleton.getInstance(getContext());

        if (isAdding) {
            adapter = ((CategoryScreen) getActivity()).categoriesAdapter;
            allCategories = realm.where(Folder.class).sort("positionInList").findAll();
        }

        folderColor = view.findViewById(R.id.folder_color);
        MaterialButton confirmFilter = view.findViewById(R.id.confirm_filter);
        MaterialButton next = view.findViewById(R.id.next_confirm);
        MaterialButton delete = view.findViewById(R.id.delete);

        TextInputLayout itemNameLayout = view.findViewById(R.id.item_name_layout);
        itemName = view.findViewById(R.id.item_name);
        TextView title = view.findViewById(R.id.title);

        itemName.requestFocusFromTouch();

        if (AppData.getAppData().isDarkerMode) {
            itemNameLayout.setBoxBackgroundColor(getContext().getColor(R.color.darker_mode));
            itemNameLayout.setHintTextColor(ColorStateList.valueOf(getContext().getColor(R.color.ultra_white)));
            itemName.setTextColor(getContext().getColor(R.color.ultra_white));
            view.setBackgroundColor(getContext().getColor(R.color.darker_mode));
        } else {
            view.setBackgroundColor(getContext().getColor(R.color.gray));
            delete.setBackgroundColor(getContext().getColor(R.color.light_gray));
        }

        if (isAdding) {
            title.setText("Adding");
            delete.setVisibility(View.GONE);
            folderColor.setCardBackgroundColor(getContext().getColor(R.color.orange));
        } else {
            title.setText("Editing");
            try {
                itemName.setText(currentItem.getName());
                folderColor.setCardBackgroundColor(currentItem.getColor() == 0 ? getContext().getColor(R.color.orange) : currentItem.getColor());
                itemName.setSelection(itemName.getText().toString().length());
                delete.setVisibility(View.VISIBLE);
                next.setVisibility(View.GONE);
            } catch (Exception e) {
                this.dismiss();
            }
        }

        folderColor.setOnClickListener(v -> editColorDialog(currentItem));

        delete.setOnClickListener(v -> {
            if (!isAdding) {
                deleteCategory(currentItem);
                this.dismiss();
            }
        });

        confirmFilter.setOnClickListener(v -> {
            confirmEntry(itemName, itemNameLayout);
        });

        next.setOnClickListener(v -> {
            if (confirmEntry(itemName, itemNameLayout)) {
                FolderItemSheet checklistItemSheet = new FolderItemSheet();
                checklistItemSheet.show(getActivity().getSupportFragmentManager(), checklistItemSheet.getTag());
            }
        });

        return view;
    }

    private void editColorDialog(Folder current) {
        int initialColor;
        if (current == null || current.getColor() == 0)
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
                    if (isAdding) {
                        color = selectedColor;
                        folderColor.setCardBackgroundColor(color);
                    } else {
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

    private void editCategory(Folder current, String newName) {
        allSelectedNotes = realm.where(Note.class).equalTo("category", current.getName()).findAll();
        // update database
        realm.beginTransaction();
        allSelectedNotes.setString("category", newName);
        current.setName(newName);
        realm.commitTransaction();
        adapter.notifyItemChanged(position);
        this.dismiss();
    }

    private void deleteCategory(Folder current) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        allSelectedNotes = realm.where(Note.class).equalTo("category", current.getName()).findAll();
        // update database
        realm.beginTransaction();
        allSelectedNotes.setString("category", "none");
        current.deleteFromRealm();
        realm.commitTransaction();
        adapter.notifyDataSetChanged();
    }

    // adds note
    private void addCategory(String itemText, int color) {
        // insert data to database
        RealmResults<Folder> results = realm.where(Folder.class)
                .equalTo("name", itemText, Case.INSENSITIVE).findAll();
        Folder newItem = new Folder(itemText, allCategories.size());
        newItem.setColor(color);
        if (results.size() == 0) {
            realm.beginTransaction();
            realm.insert(newItem);
            realm.commitTransaction();
            adapter.notifyDataSetChanged();
            this.dismiss();
        } else
            Helper.showMessage(getActivity(), "Duplicate", "A category with that name exists",
                    MotionToast.TOAST_ERROR);
    }

    private boolean confirmEntry(TextInputEditText itemName, TextInputLayout itemNameLayout) {

        if (!itemName.getText().toString().isEmpty()) {
            if (isAdding)
                addCategory(itemName.getText().toString(), color);
            else
                editCategory(currentItem, itemName.getText().toString());
            return true;
        } else
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
    public void onPause() {
        super.onPause();
        Helper.updateKeyboardStatus(getActivity());
        Helper.toggleKeyboard(getContext(), itemName, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        itemName.requestFocus();
        if (AppData.isKeyboardOpen) {
            Helper.toggleKeyboard(getContext(), itemName, true);
            AppData.isKeyboardOpen = false;
        }
    }

    @Override
    public int getTheme() {
        if (AppData.getAppData().isDarkerMode)
            return R.style.BaseBottomSheetDialogLight;
        else
            return R.style.BaseBottomSheetDialog;
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.getViewTreeObserver()
                .addOnGlobalLayoutListener(() -> {
                    BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
                    if (dialog != null) {
                        FrameLayout bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
                        if (bottomSheet != null) {
                            BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
                            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                    }
                });
    }

}