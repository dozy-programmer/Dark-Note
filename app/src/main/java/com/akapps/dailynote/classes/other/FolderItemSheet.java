package com.akapps.dailynote.classes.other;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.CategoryScreen;
import com.akapps.dailynote.classes.data.Folder;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.helpers.AppConstants;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmHelper;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import www.sanju.motiontoast.MotionToast;

public class FolderItemSheet extends RoundedBottomSheetDialogFragment {

    private int folderId;
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
    public FolderItemSheet(int folderId, RecyclerView.Adapter adapter, int position) {
        isAdding = false;
        this.folderId = folderId;
        this.allSelectedNotes = RealmSingleton.getInstance(getContext()).where(Note.class).equalTo("isSelected", true).findAll();
        this.adapter = adapter;
        this.position = position;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_folder, container, false);

        if (savedInstanceState != null) {
            isAdding = savedInstanceState.getBoolean("add");
            position = savedInstanceState.getInt("pos");
        }

        if (isAdding) {
            adapter = ((CategoryScreen) getActivity()).categoriesAdapter;
            allCategories = getRealm().where(Folder.class).sort("positionInList").findAll();
        }

        folderColor = view.findViewById(R.id.folder_color);
        MaterialButton confirmFilter = view.findViewById(R.id.confirm_filter);
        MaterialButton next = view.findViewById(R.id.next_confirm);
        MaterialButton delete = view.findViewById(R.id.delete);
        MaterialButton lockFolder = view.findViewById(R.id.lock);

        TextInputLayout itemNameLayout = view.findViewById(R.id.item_name_layout);
        itemName = view.findViewById(R.id.item_name);
        TextView title = view.findViewById(R.id.title);

        itemName.requestFocusFromTouch();

        AtomicReference<Folder> currentFolder = new AtomicReference<>(RealmHelper.getCurrentFolder(getContext(), folderId));

        if (isAdding) {
            title.setText("Adding");
            delete.setVisibility(View.GONE);
        } else {
            title.setText("Editing");
            try {
                itemName.setText(currentFolder.get().getName());
                folderColor.setCardBackgroundColor(currentFolder.get().getColor() == 0 ? getContext().getColor(R.color.azure) : currentFolder.get().getColor());
                itemName.setSelection(itemName.getText().toString().length());
                if (currentFolder.get().getPin() > 0)
                    lockFolder.setIcon(getActivity().getDrawable(R.drawable.lock_icon));
                delete.setVisibility(View.VISIBLE);
                next.setVisibility(View.GONE);
            } catch (Exception e) {
                this.dismiss();
            }
        }

        folderColor.setOnClickListener(v -> editColorDialog(currentFolder.get()));

        delete.setOnClickListener(v -> {
            if (!isAdding) {
                deleteCategory(folderId);
                dismiss();
            }
        });

        lockFolder.setOnClickListener(view1 -> {
            currentFolder.set(RealmHelper.getCurrentFolder(getContext(), folderId));
            if (isAdding) {
                if (AppData.pin > 0) {
                    AppData.updateLockData(0, "", false);
                    lockFolder.setIcon(getActivity().getDrawable(R.drawable.unlock_icon));
                    Helper.showMessage(getActivity(), "Folder Unlocked", "Folder has been unlocked", MotionToast.TOAST_SUCCESS);
                } else {
                    LockSheet lockSheet = new LockSheet(AppConstants.LockType.LOCK_FOLDER, lockFolder);
                    lockSheet.show(getActivity().getSupportFragmentManager(), lockSheet.getTag());
                }
            } else {
                if (currentFolder.get().getPin() > 0) {
                    getRealm().beginTransaction();
                    currentFolder.get().setPin(0);
                    currentFolder.get().setSecurityWord("");
                    currentFolder.get().setFingerprintAdded(false);
                    getRealm().commitTransaction();
                    lockFolder.setIcon(getActivity().getDrawable(R.drawable.unlock_icon));
                    adapter.notifyDataSetChanged();
                    RealmHelper.lockNotesInsideFolder(getContext(), folderId, false);
                    Helper.showMessage(getActivity(), "Unlocked", "Folder & notes inside have been unlocked", MotionToast.TOAST_SUCCESS);

                } else {
                    LockSheet lockSheet = new LockSheet(AppConstants.LockType.LOCK_FOLDER, folderId, lockFolder);
                    lockSheet.show(getActivity().getSupportFragmentManager(), lockSheet.getTag());
                    this.dismiss();
                }
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
            initialColor = getContext().getColor(R.color.orange);
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
                        getRealm().beginTransaction();
                        current.setColor(selectedColor);
                        getRealm().commitTransaction();
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
        // update database
        if (!current.getName().equals(newName)) {
            allSelectedNotes = getRealm().where(Note.class).equalTo("category", current.getName()).findAll();
            getRealm().beginTransaction();
            allSelectedNotes.setString("category", newName);
            current.setName(newName);
            getRealm().commitTransaction();
            adapter.notifyItemChanged(position);
        }
        this.dismiss();
    }

    private void deleteCategory(int folderId) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Folder folder = RealmHelper.getCurrentFolder(getContext(), folderId);
        allSelectedNotes = getRealm().where(Note.class).equalTo("category", folder.getName()).findAll();
        // update database
        RealmHelper.lockNotesInsideFolder(getContext(), folderId, false);
        getRealm().beginTransaction();
        allSelectedNotes.setString("category", "none");
        folder.deleteFromRealm();
        getRealm().commitTransaction();
        adapter.notifyDataSetChanged();
    }

    // adds note
    private void addCategory(String itemText, int color) {
        // insert data to database
        RealmResults<Folder> results = getRealm().where(Folder.class)
                .equalTo("name", itemText, Case.INSENSITIVE).findAll();
        Folder newItem = new Folder(itemText, allCategories.size());
        if (AppData.pin > 0) {
            newItem.setPin(AppData.pin);
            newItem.setSecurityWord(AppData.securityWord);
            newItem.setFingerprintAdded(AppData.isFingerprintAdded);
        }
        newItem.setColor(color);
        if (results.size() == 0) {
            getRealm().beginTransaction();
            getRealm().insert(newItem);
            getRealm().commitTransaction();
            adapter.notifyDataSetChanged();
            this.dismiss();
        } else
            Helper.showMessage(getActivity(), "Duplicate", "A category with that name exists",
                    MotionToast.TOAST_ERROR);
    }

    private boolean confirmEntry(TextInputEditText itemName, TextInputLayout itemNameLayout) {
        Folder folder = RealmHelper.getCurrentFolder(getContext(), folderId);
        if (!itemName.getText().toString().isEmpty()) {
            if (isAdding) {
                addCategory(itemName.getText().toString(), color);
            } else
                editCategory(folder, itemName.getText().toString());
            return true;
        } else
            itemNameLayout.setError("Required");

        return false;
    }

    private Realm getRealm() {
        return RealmSingleton.getInstance(getContext());
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
        return UiHelper.getBottomSheetTheme(getContext());
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        UiHelper.setBottomSheetBehavior(view, dialog);
    }

}