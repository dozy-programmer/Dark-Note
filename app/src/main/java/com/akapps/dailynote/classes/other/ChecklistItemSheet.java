package com.akapps.dailynote.classes.other;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.adapter.IconMenuAdapter;
import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.SubCheckListItem;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmHelper;
import com.bumptech.glide.Glide;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.skydoves.powermenu.CustomPowerMenu;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import io.realm.Realm;
import www.sanju.motiontoast.MotionToast;

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
    private CheckListItem checkListItem;
    private CustomPowerMenu noteMenu;
    private ImageView dropDownMenu;

    private MaterialCardView itemImageLayout;
    private ImageView itemImage;
    private TextView photo_info;
    private TextView dateCreated;

    // adding
    public ChecklistItemSheet(){
        isAdding = true;
        isSubChecklist = false;
    }

    public ChecklistItemSheet(CheckListItem checkListItem, String parentNode, boolean isSubChecklist, RecyclerView.Adapter adapter){
        this.checkListItem = checkListItem;
        this.parentNode = parentNode;
        isAdding = true;
        this.isSubChecklist = isSubChecklist;
        this.adapter = adapter;
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
    public ChecklistItemSheet(SubCheckListItem checkListItem, int position,RecyclerView.Adapter adapter){
        isAdding = false;
        isSubChecklist = true;
        this.currentSubItem = checkListItem;
        this.position = position;
        this.adapter = adapter;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_checklist_item, container, false);

        if(savedInstanceState != null)
            this.dismiss();

        currentNote = ((NoteEdit)getActivity()).currentNote;
        realm = ((NoteEdit)getActivity()).realm;

        MaterialButton confirmFilter = view.findViewById(R.id.confirm_filter);
        MaterialButton next = view.findViewById(R.id.next_confirm);
        ImageView delete = view.findViewById(R.id.delete);
        dropDownMenu = view.findViewById(R.id.dropdown_menu);
        TextView info = view.findViewById(R.id.checklist_info);
        dateCreated = view.findViewById(R.id.date_created);
        photo_info = view.findViewById(R.id.photo_info);

        TextInputLayout itemNameLayout = view.findViewById(R.id.item_name_layout);
        itemName = view.findViewById(R.id.item_name);
        TextView title = view.findViewById(R.id.title);

        itemImageLayout = view.findViewById(R.id.item_image_layout);
        itemImage = view.findViewById(R.id.item_image);

        itemName.requestFocusFromTouch();

        if (AppData.getAppData().isDarkerMode) {
            itemNameLayout.setBoxBackgroundColor(getContext().getColor(R.color.darker_mode));
            itemNameLayout.setHintTextColor(ColorStateList.valueOf(getContext().getColor(R.color.ultra_white)));
            itemName.setHintTextColor(ColorStateList.valueOf(getContext().getColor(R.color.ultra_white)));
            itemNameLayout.setDefaultHintTextColor(ColorStateList.valueOf(getContext().getColor(R.color.ultra_white)));
            itemName.setTextColor(getContext().getColor(R.color.ultra_white));
            view.setBackgroundColor(getContext().getColor(R.color.darker_mode));
        }
        else
            view.setBackgroundColor(getContext().getColor(R.color.gray));

        if(isSubChecklist || isAdding)
            itemImageLayout.setVisibility(View.GONE);

        if(isAdding){
            info.setVisibility(View.VISIBLE);
            dropDownMenu.setVisibility(View.GONE);
            if(isSubChecklist) {
                title.setText("Adding Sub-Item to\n" + parentNode);
                info.setText(info.getText());
            }
            else {
                title.setText("Adding");
                if(currentNote.isEnableSublist())
                    info.setText(info.getText() + "\nAdd sublist item(s) using \"--\" (2 dashes) after item\n");
            }
            delete.setVisibility(View.GONE);
        }
        else{
            info.setVisibility(View.GONE);
            try {
                if(isSubChecklist){
                    title.setText("Editing Sub-Item");
                    itemName.setText(currentSubItem.getText());

                    if(currentSubItem.getDateCreated() != null)
                        if (!currentSubItem.getDateCreated().isEmpty()) {
                            dateCreated.setText("Created: " + currentSubItem.getDateCreated());
                            dateCreated.setVisibility(View.VISIBLE);
                            dateCreated.setGravity(Gravity.RIGHT);
                        }
                }
                else {
                    title.setText("Editing");
                    itemName.setText(currentItem.getText());

                    if(currentItem.getItemImage()!=null && !currentItem.getItemImage().isEmpty()) {
                        Glide.with(getContext()).load(currentItem.getItemImage()).into(itemImage);
                        photo_info.setVisibility(View.VISIBLE);
                        dateCreated.setGravity(Gravity.LEFT);
                    }

                    if(currentItem.getDateCreated() != null)
                        if (!currentItem.getDateCreated().isEmpty()) {
                            dateCreated.setText("Created: " + currentItem.getDateCreated());
                            dateCreated.setVisibility(View.VISIBLE);
                        }
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
            Helper.showMessage(getActivity(), "How to delete",
                    "Long click delete button", MotionToast.TOAST_WARNING);
        });

        delete.setOnLongClickListener(view13 -> {
            if(!isAdding) {
                if(isSubChecklist)
                    deleteItem(currentSubItem);
                else
                    deleteItem(currentItem);
                dismiss();
            }
            return false;
        });

        dropDownMenu.setOnClickListener(view1 -> openMenuDialog());

        confirmFilter.setOnClickListener(v -> {
            if(confirmEntry(itemName, itemNameLayout))
                this.dismiss();
        });

        next.setOnClickListener(v -> {
            if(confirmEntry(itemName, itemNameLayout)){
                this.dismiss();
                ChecklistItemSheet checklistItemSheet;
                if(isSubChecklist)
                    checklistItemSheet = new ChecklistItemSheet(checkListItem, parentNode, true, adapter);
                else
                    checklistItemSheet = new ChecklistItemSheet();
                checklistItemSheet.show(getActivity().getSupportFragmentManager(), checklistItemSheet.getTag());
            }
        });

        itemImageLayout.setOnClickListener(view12 -> {
            showCameraDialog();
        });

        itemImageLayout.setOnLongClickListener(view14 -> {
            realm.beginTransaction();
            currentItem.setItemImage("");
            realm.commitTransaction();

            Glide.with(getContext()).load(getContext().getDrawable(R.drawable.icon_image)).into(itemImage);
            photo_info.setVisibility(View.INVISIBLE);
            adapter.notifyItemChanged(position);
            return true;
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
        RealmHelper.deleteChecklistItem(checkListItem);
        realm.beginTransaction();
        currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
        realm.commitTransaction();
        ((NoteEdit)getActivity()).updateDateEdited();
        adapter.notifyDataSetChanged();
    }

    // updates select status of sub-note in database
    private void deleteItem(SubCheckListItem checkListItem){
        // save status to database
        RealmHelper.deleteSublistItem(checkListItem);
        realm.beginTransaction();
        //checkListItem.deleteFromRealm();
        currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
        realm.commitTransaction();
        ((NoteEdit)getActivity()).updateDateEdited();
        adapter.notifyDataSetChanged();
    }


    private boolean confirmEntry(TextInputEditText itemName, TextInputLayout itemNameLayout){
        if(!itemName.getText().toString().isEmpty()){
            if(isAdding) {
                String text = itemName.getText().toString().trim().replaceAll("<br>", "\n").replaceAll(" +", " ");
                String[] items = text.replaceAll("\n+", "\n").replaceAll(" +, +", ",,").split(",,");
                if(isSubChecklist)
                    for (String subItem : items)
                        ((NoteEdit) getActivity()).addSubCheckList(checkListItem, subItem);
                else
                    for (String item : items) {
                        currentItem = ((NoteEdit) getActivity()).addCheckList(item.split("--")[0]);
                        if(item.contains("--")) {
                            String[] currentSublistItems = item.split("--");
                            for (int i = 1 ; i < currentSublistItems.length; i++) {
                                String currentSublistItem = currentSublistItems[i];
                                if (!currentSublistItem.equals(currentItem.getText()))
                                    ((NoteEdit) getActivity()).addSubCheckList(realm.where(CheckListItem.class).equalTo("subListId", currentItem.getSubListId()).findFirst(), currentSublistItem);
                            }
                        }
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

    private void showCameraDialog(){
        ImagePicker.with(this)
                .maxResultSize(814, 814)
                .compress(1024)
                .saveDir(getActivity().getExternalFilesDir("/Documents"))
                .start(1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            Uri uri = data.getData();

            File file = new File(uri.getPath());
            String fileName = uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1);
            File newFile = new File(getActivity().getExternalFilesDir("/Documents"), "~" + fileName);
            file.renameTo(newFile);

            if(currentItem.getItemImage()!=null && !currentItem.getItemImage().isEmpty()){
                File fdelete = new File(currentItem.getItemImage());
                if (fdelete.exists())
                    fdelete.delete();
            }

            realm.beginTransaction();
            currentItem.setItemImage(newFile.getPath());
            realm.commitTransaction();

            Glide.with(getContext()).load(currentItem.getItemImage()).into(itemImage);
            photo_info.setVisibility(View.VISIBLE);
            dateCreated.setGravity(Gravity.LEFT);
            adapter.notifyItemChanged(position);
        }
        else if (resultCode== ImagePicker.RESULT_ERROR) {}
    }

    private void openMenuDialog() {
        noteMenu = new CustomPowerMenu.Builder<>(getContext(), new IconMenuAdapter(false))
                .addItem(new IconPowerMenuItem(getContext().getDrawable(R.drawable.copy_icon), "Copy"))
                .addItem(new IconPowerMenuItem(getContext().getDrawable(R.drawable.send_icon), "Send"))
                .setBackgroundColor(getContext().getColor(R.color.light_gray))
                .setOnMenuItemClickListener(onIconMenuItemClickListener)
                .setAnimation(MenuAnimation.SHOW_UP_CENTER)
                .setMenuRadius(15f)
                .setMenuShadow(10f)
                .build();
        noteMenu.showAsDropDown(dropDownMenu);
    }

    private final OnMenuItemClickListener<IconPowerMenuItem> onIconMenuItemClickListener = new OnMenuItemClickListener<IconPowerMenuItem>() {
        @Override
        public void onItemClick(int position, IconPowerMenuItem item) {
            if (position == 0) {
                // copy text
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip;
                if(!isSubChecklist)
                    clip = ClipData.newPlainText("Code", currentItem.getText());
                else
                    clip = ClipData.newPlainText("Code", currentSubItem.getText());
                clipboard.setPrimaryClip(clip);
                Helper.showMessage(getActivity(), "Success!",
                        "Copied successfully", MotionToast.TOAST_SUCCESS);
            }
            else if(position == 1){
                Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                emailIntent.setType("*/*");

                ArrayList<Uri> uris = new ArrayList<>();

                if(!isSubChecklist) {
                    File file = new File(currentItem.getItemImage());
                    if (file.exists()) {
                        uris.add(FileProvider.getUriForFile(
                                getContext(),
                                "com.akapps.dailynote.fileprovider",
                                file));
                        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                    }
                    emailIntent.putExtra(Intent.EXTRA_TEXT, currentItem.getText());
                }
                else
                    emailIntent.putExtra(Intent.EXTRA_TEXT, currentSubItem.getText());

                // adds email subject and email body to intent
                getContext().startActivity(Intent.createChooser(emailIntent, "Share Checklist Item"));
            }
            noteMenu.dismiss();
        }
    };

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("add", isAdding);
        outState.putInt("position", position);
        itemName.clearFocus();
    }

    @Override
    public int getTheme() {
        if(AppData.getAppData().isDarkerMode)
            return R.style.BaseBottomSheetDialogLight;
        else
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