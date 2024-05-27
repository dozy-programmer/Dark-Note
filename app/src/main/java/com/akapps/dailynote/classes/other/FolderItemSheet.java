package com.akapps.dailynote.classes.other;

import static android.app.Activity.RESULT_OK;
import static com.akapps.dailynote.classes.helpers.RealmHelper.getCurrentFolder;
import static com.akapps.dailynote.classes.helpers.RealmHelper.getFolderSize;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.CategoryScreen;
import com.akapps.dailynote.activity.NoteLockScreen;
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
    private boolean isFolderUnlocked;
    AtomicReference<Folder> currentFolder;
    private boolean isEditing;

    // firebase
    private RealmResults<Note> allSelectedNotes;
    private RealmResults<Folder> allCategories;

    private MaterialCardView folderColor;
    private MaterialButton next;
    private MaterialButton lockFolder;

    private ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    isFolderUnlocked = true;
                    onLockClick();
                } else {
                    isFolderUnlocked = false;
                    Helper.showMessage(getActivity(), "Folder not Unlocked", "Unlock folder to open", MotionToast.TOAST_WARNING);
                }
            }
    );

    private ActivityResultLauncher<Intent> deleteCategoryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (!isAdding) {
                        deleteCategory(folderId);
                        dismiss();
                    }
                } else {
                    Helper.showMessage(getActivity(), "Folder not Unlocked", "Unlock folder to delete", MotionToast.TOAST_WARNING);
                }
            }
    );

    // adding
    public FolderItemSheet(boolean isEditing) {
        isAdding = true;
        this.isEditing = isEditing;
    }

    // layout
    private TextInputEditText itemName;

    // editing
    public FolderItemSheet(int folderId, RecyclerView.Adapter adapter, int position, boolean isEditing) {
        isAdding = false;
        this.folderId = folderId;
        this.allSelectedNotes = RealmSingleton.getInstance(getContext()).where(Note.class).equalTo("isSelected", true).findAll();
        this.adapter = adapter;
        this.position = position;
        this.isEditing = isEditing;
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
        next = view.findViewById(R.id.next_confirm);
        MaterialButton delete = view.findViewById(R.id.delete);
        lockFolder = view.findViewById(R.id.lock);

        TextInputLayout itemNameLayout = view.findViewById(R.id.item_name_layout);
        itemName = view.findViewById(R.id.item_name);
        TextView title = view.findViewById(R.id.title);

        itemName.requestFocusFromTouch();

        currentFolder = new AtomicReference<>(getCurrentFolder(getContext(), folderId));

        if (isAdding) {
            title.setText("Adding");
            delete.setVisibility(View.GONE);
        } else {
            title.setText("Editing");
            try {
                itemName.setText(currentFolder.get().getName());
                folderColor.setCardBackgroundColor(currentFolder.get().getColor() == 0 ? getContext().getResources().getColor(R.color.azure) : currentFolder.get().getColor());
                itemName.setSelection(itemName.getText().toString().length());
                if (currentFolder.get().getPin() > 0) {
                    next.setIcon(getActivity().getDrawable(R.drawable.lock_icon));
                } else
                    next.setIcon(getActivity().getDrawable(R.drawable.unlock_icon));
                delete.setVisibility(View.VISIBLE);
                lockFolder.setVisibility(View.GONE);
            } catch (Exception e) {
                this.dismiss();
            }
        }

        folderColor.setOnClickListener(v -> editColorDialog(currentFolder.get()));

        delete.setOnClickListener(v -> {
            if (!isAdding) {
                if (getFolderSize(getContext(), folderId) > 0 && getCurrentFolder(getContext(), folderId).getPin() > 0) {
                    Intent lockScreen = new Intent(getActivity(), NoteLockScreen.class);
                    lockScreen.putExtra("id", -12);
                    lockScreen.putExtra("title", "Unlock Folder");
                    lockScreen.putExtra("pin", currentFolder.get().getPin());
                    lockScreen.putExtra("securityWord", currentFolder.get().getSecurityWord());
                    lockScreen.putExtra("fingerprint", currentFolder.get().isFingerprintAdded());
                    lockScreen.putExtra("isFolderLocked", true);
                    deleteCategoryLauncher.launch(lockScreen);
                } else {
                    deleteCategory(folderId);
                    dismiss();
                }
            }
        });

        lockFolder.setOnClickListener(view1 -> onLockClick());

        confirmFilter.setOnClickListener(v -> {
            confirmEntry(itemName, itemNameLayout);
        });

        next.setOnClickListener(v -> {
            if (!isAdding)
                onLockClick();
            else if (confirmEntry(itemName, itemNameLayout)) {
                FolderItemSheet checklistItemSheet = new FolderItemSheet(isEditing);
                checklistItemSheet.show(getActivity().getSupportFragmentManager(), checklistItemSheet.getTag());
            }
        });

        return view;
    }

    private void onLockClick() {
        currentFolder.set(getCurrentFolder(getContext(), folderId));
        if (isAdding) {
            if (AppData.pin > 0) {
                AppData.updateLockData(0, "", false);
                lockFolder.setIcon(getActivity().getDrawable(R.drawable.lock_icon));
                Helper.showMessage(getActivity(), "Folder Unlocked", "Folder has been unlocked", MotionToast.TOAST_SUCCESS);
            } else {
                LockSheet lockSheet = new LockSheet(AppConstants.LockType.LOCK_FOLDER, lockFolder);
                lockSheet.show(getActivity().getSupportFragmentManager(), lockSheet.getTag());
            }
        } else {
            if (currentFolder.get().getPin() > 0) {
                if (isFolderUnlocked) {
                    getRealm().beginTransaction();
                    currentFolder.get().setPin(0);
                    currentFolder.get().setSecurityWord("");
                    currentFolder.get().setFingerprintAdded(false);
                    getRealm().commitTransaction();
                    adapter.notifyDataSetChanged();
                    RealmHelper.unlockNotesInsideFolder(getContext(), folderId);
                    next.setIcon(getActivity().getDrawable(R.drawable.unlock_icon));
                    Helper.showMessage(getActivity(), "Unlocked", "Folder & notes inside have been unlocked", MotionToast.TOAST_SUCCESS);
                } else {
                    Intent lockScreen = new Intent(getActivity(), NoteLockScreen.class);
                    lockScreen.putExtra("id", -12);
                    lockScreen.putExtra("title", "Unlock Folder");
                    lockScreen.putExtra("pin", currentFolder.get().getPin());
                    lockScreen.putExtra("securityWord", currentFolder.get().getSecurityWord());
                    lockScreen.putExtra("fingerprint", currentFolder.get().isFingerprintAdded());
                    lockScreen.putExtra("isFolderLocked", true);
                    launcher.launch(lockScreen);
                }
            } else {
                RealmResults<Note> lockedNotesInsideFolder = RealmSingleton.getInstance(getContext()).where(Note.class)
                        .equalTo("category", getCurrentFolder(getContext(), folderId).getName())
                        .greaterThan("pinNumber", 0)
                        .findAll();

                if (lockedNotesInsideFolder.size() > 0) {
                    StringBuilder lockedNotes = new StringBuilder();
                    lockedNotes.append("\n\nThe following notes need to be unlocked:");
                    for (Note lockedNote : lockedNotesInsideFolder) {
                        lockedNotes.append("\n" + (lockedNote.getTitle().isEmpty() ? "~ No Title ~" : lockedNote.getTitle()));
                    }
                    GenericInfoSheet infoSheet = new GenericInfoSheet("Lock Folder", "To" +
                            " lock a folder, all notes inside of it need to be unlocked for security purposes." + lockedNotes);
                    infoSheet.show(getActivity().getSupportFragmentManager(), infoSheet.getTag());
                } else {
                    LockSheet lockSheet = new LockSheet(AppConstants.LockType.LOCK_FOLDER, folderId, lockFolder, isEditing);
                    lockSheet.show(getActivity().getSupportFragmentManager(), lockSheet.getTag());
                    this.dismiss();
                }
            }
        }
    }

    private void editColorDialog(Folder current) {
        int initialColor;
        if (current == null || current.getColor() == 0)
            initialColor = getContext().getResources().getColor(R.color.orange);
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
        Folder folder = getCurrentFolder(getContext(), folderId);
        allSelectedNotes = getRealm().where(Note.class).equalTo("category", folder.getName()).findAll();
        // update database
        RealmHelper.unlockNotesInsideFolder(getContext(), folderId);
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
        Folder folder = getCurrentFolder(getContext(), folderId);
        if (!itemName.getText().toString().isEmpty()) {
            if (isAdding)
                addCategory(itemName.getText().toString(), color);
            else
                editCategory(folder, itemName.getText().toString());
            return true;
        } else
            itemNameLayout.setError("Required");

        return false;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        AppData.updateLockData(0, "", false);
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