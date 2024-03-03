package com.akapps.dailynote.classes.other;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akapps.dailynote.BuildConfig;
import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.adapter.IconMenuAdapter;
import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.Place;
import com.akapps.dailynote.classes.data.SubCheckListItem;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmHelper;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.akapps.dailynote.recyclerview.notes_search_recyclerview;
import com.bumptech.glide.Glide;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nguyenhoanglam.imagepicker.helper.Constants;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.model.ImagePickerConfig;
import com.nguyenhoanglam.imagepicker.model.StatusBarContent;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePickerLauncher;
import com.skydoves.powermenu.CustomPowerMenu;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import io.realm.Realm;
import www.sanju.motiontoast.MotionToast;

public class ChecklistItemSheet extends RoundedBottomSheetDialogFragment {

    private CheckListItem currentItem;
    private Note currentNote;
    private final boolean isAdding;
    private RecyclerView.Adapter adapter;
    private RecyclerView allNotesRecyclerview;
    private RecyclerView.Adapter notesSearchAdapter;
    private ArrayList<Note> allNotes;
    private int position;

    private User user;
    private Place selectedPlace;
    private boolean isTextPastedDetected;
    private boolean isTextPasted;

    private TextInputEditText itemName;

    private SubCheckListItem currentSubItem;
    private CheckListItem parentCurrentSubItem;
    private final boolean isSubChecklist;
    private String parentNode;
    private CheckListItem checkListItem;
    private CustomPowerMenu noteMenu;
    private ImageView dropDownMenu;

    private MaterialCardView itemImageLayout;
    private ImageView itemImage;
    private TextView photo_info;
    private TextView dateCreated;

    private LinearLayout locationLayout;
    private TextView placeLocation;

    private BottomSheetDialog dialog;

    private MaterialButton redirectToNote;
    private String noteSelectedTitle;
    private String wordAfterAt;

    // adding
    public ChecklistItemSheet() {
        isAdding = true;
        isSubChecklist = false;
    }

    public ChecklistItemSheet(CheckListItem checkListItem, String parentNode, boolean isSubChecklist, RecyclerView.Adapter adapter) {
        this.checkListItem = checkListItem;
        this.parentNode = parentNode;
        isAdding = true;
        this.isSubChecklist = isSubChecklist;
        this.adapter = adapter;
    }

    // editing note
    public ChecklistItemSheet(CheckListItem checkListItem, int position, RecyclerView.Adapter adapter) {
        isAdding = false;
        isSubChecklist = false;
        this.currentItem = checkListItem;
        this.position = position;
        this.adapter = adapter;
    }

    // editing sub-note
    public ChecklistItemSheet(CheckListItem parentCurrentSubItem, SubCheckListItem checkListItem, int position, RecyclerView.Adapter adapter) {
        isAdding = false;
        isSubChecklist = true;
        this.parentCurrentSubItem = parentCurrentSubItem;
        this.currentSubItem = checkListItem;
        this.position = position;
        this.adapter = adapter;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_checklist_item, container, false);

        if (savedInstanceState != null)
            this.dismiss();

        int noteId = ((NoteEdit) getActivity()).noteId;
        currentNote = RealmHelper.getCurrentNote(getContext(), noteId);
        user = RealmHelper.getUser(getContext(), "bottom sheet");

        // Initialize the SDK
        if (!Places.isInitialized())
            Places.initialize(getContext(), BuildConfig.MAPS_API_KEY);

        MaterialButton confirmFilter = view.findViewById(R.id.confirm_filter);
        MaterialButton addLocation = view.findViewById(R.id.add_location);
        ImageView delete = view.findViewById(R.id.delete);
        dropDownMenu = view.findViewById(R.id.dropdown_menu);
        TextView info = view.findViewById(R.id.checklist_info);
        dateCreated = view.findViewById(R.id.date_created);
        photo_info = view.findViewById(R.id.photo_info);
        locationLayout = view.findViewById(R.id.location_layout);
        placeLocation = view.findViewById(R.id.place_info);
        ImageView editLocation = view.findViewById(R.id.edit_location);
        ImageView deleteLocation = view.findViewById(R.id.delete_location);
        redirectToNote = view.findViewById(R.id.redirect_to_note);

        TextInputLayout itemNameLayout = view.findViewById(R.id.item_name_layout);
        itemName = view.findViewById(R.id.item_name);
        TextView title = view.findViewById(R.id.title);

        itemImageLayout = view.findViewById(R.id.item_image_layout);
        itemImage = view.findViewById(R.id.item_image);

        // initialize recyclerview
        allNotesRecyclerview = view.findViewById(R.id.all_notes);

        allNotes = AppData.getAllNotes(getActivity());
        allNotesRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        populateSearchAdapter(allNotes);
        allNotesRecyclerview.setVisibility(View.GONE);
        redirectToNote.setVisibility(View.GONE);

        itemName.requestFocusFromTouch();
        itemName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int current, int before, int count) {
                if (!isTextPastedDetected) {
                    if (count - before > 20) {
                        info.setText(info.getText().toString().split("\n")[0] + "\n\nPaste detected, if you want this to " +
                                "only be one item, [Click Here].\n");
                        isTextPastedDetected = true;
                    }
                }
                int atIndex = s.toString().lastIndexOf("@");
                if (isSubChecklist) return;
                if (atIndex != -1 && atIndex < s.length() - 1) { // Ensure "@" exists and isn't the last character
                    redirectToNote.setVisibility(View.GONE);
                    wordAfterAt = s.toString().substring(atIndex + 1); // Extract word
                    // Do something with the wordAfterAt
                    Log.d("Here", "Word after @: " + wordAfterAt);
                    if (wordAfterAt.equals(" ")) {
                        allNotesRecyclerview.setVisibility(View.GONE);
                        return;
                    }
                    ArrayList<Note> filteredNotes = new ArrayList<>(allNotes.stream()
                            .filter(note -> note.getTitle().toLowerCase().contains(wordAfterAt.toLowerCase()))
                            .collect(Collectors.toList()));
                    populateSearchAdapter(filteredNotes);
                    if (allNotesRecyclerview.getVisibility() == View.GONE)
                        allNotesRecyclerview.setVisibility(View.VISIBLE);
                } else if (atIndex != -1 && atIndex == s.length() - 1) {
                    populateSearchAdapter(allNotes);
                    if (allNotesRecyclerview.getVisibility() == View.GONE)
                        allNotesRecyclerview.setVisibility(View.VISIBLE);
                } else {
                    allNotesRecyclerview.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        if (!isSubChecklist) {
            redirectToNote.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (wordAfterAt == null) wordAfterAt = "";
                    noteSelectedTitle = redirectToNote.getText().toString();
                    itemName.setText(itemName.getText().toString().replace("@" + wordAfterAt, ""));
                    redirectToNote.setVisibility(View.VISIBLE);
                    Helper.toggleKeyboard(getContext(), itemName, false);
                    itemName.setSelection(itemName.getText().length());
                    allNotesRecyclerview.setVisibility(View.GONE);
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
        }

        if (isSubChecklist || isAdding)
            itemImageLayout.setVisibility(View.GONE);

        String multipleItemsMessage = "";
        String newlineSeparatorMessage = "Add Multiple items using newline\n\n";
        String commaSeparatorMessage = "Add Multiple items using \",,\" (2 commas)\n\n";
        String spaceSeparatorMessage = "Add Sublist Items: Start with a space after newline\n";
        String newLineDashSeparatorMessage = "Add Sublist Items: On the same line, add \"--\" (2 dashes)\n";
        String commaDashSeparatorMessage = "Add Sublist Items: After item add \"--\" (2 dashes)\n";

        if (user.getItemsSeparator().equals("newline"))
            multipleItemsMessage = newlineSeparatorMessage;
        else
            multipleItemsMessage = commaSeparatorMessage;

        if (!isSubChecklist && currentNote.isEnableSublist()) {
            if (user.getSublistSeparator().equals("space"))
                multipleItemsMessage += spaceSeparatorMessage;
            else {
                if (!user.getItemsSeparator().equals("newline"))
                    multipleItemsMessage += commaDashSeparatorMessage;
                else
                    multipleItemsMessage += newLineDashSeparatorMessage;
            }
        }

        if (isAdding) {
            info.setVisibility(View.VISIBLE);
            dropDownMenu.setVisibility(View.GONE);
            if (isSubChecklist) {
                title.setText("Adding Sub-Item to\n" + parentNode);
                addLocation.setVisibility(View.GONE);
            } else
                title.setText("Adding");
            info.setText(multipleItemsMessage);
            delete.setVisibility(View.GONE);
        } else {
            info.setVisibility(View.GONE);
            dropDownMenu.setVisibility(View.VISIBLE);
            try {
                if (isSubChecklist) {
                    title.setText("Editing Sub-Item");
                    itemName.setText(currentSubItem.getText());

                    if (currentSubItem.getDateCreated() != null)
                        if (!currentSubItem.getDateCreated().isEmpty()) {
                            dateCreated.setText("Created: " + currentSubItem.getDateCreated());
                            dateCreated.setVisibility(View.VISIBLE);
                            dateCreated.setGravity(Gravity.RIGHT);
                        }
                } else {
                    title.setText("Editing");
                    itemName.setText(currentItem.getText());

                    if (currentItem.getRedirectToOtherNote() != 0)
                        redirectToNote.setText(RealmHelper.getTitleUsingId(getContext(), currentItem.getRedirectToOtherNote()));

                    if (currentItem.getItemImage() != null && !currentItem.getItemImage().isEmpty()) {
                        Glide.with(getContext()).load(currentItem.getItemImage()).into(itemImage);
                        photo_info.setVisibility(View.VISIBLE);
                        dateCreated.setGravity(Gravity.LEFT);
                    }

                    if (currentItem.getPlace() != null && !currentItem.getPlace().getPlaceName().isEmpty()) {
                        locationLayout.setVisibility(View.VISIBLE);
                        placeLocation.setText(currentItem.getPlace().getPlaceName());
                    }

                    if (currentItem.getDateCreated() != null)
                        if (!currentItem.getDateCreated().isEmpty()) {
                            dateCreated.setText("Created: " + currentItem.getDateCreated());
                            dateCreated.setVisibility(View.VISIBLE);
                        }
                }
                itemName.setSelection(itemName.getText().toString().length());
                delete.setVisibility(View.VISIBLE);
                addLocation.setVisibility(View.GONE);
            } catch (Exception e) {
                this.dismiss();
            }
        }

        delete.setOnClickListener(v -> {
            Helper.showMessage(getActivity(), "How to delete",
                    "Long click delete button", MotionToast.TOAST_WARNING);
        });

        delete.setOnLongClickListener(view13 -> {
            if (!isAdding) {
                if (isSubChecklist)
                    deleteItem(currentSubItem);
                else
                    deleteItem(currentItem);
                dismiss();
            }
            return true;
        });

        dropDownMenu.setOnClickListener(view1 -> openMenuDialog());

        confirmFilter.setOnClickListener(v -> {
            if (confirmEntry(itemName, itemNameLayout))
                this.dismiss();
        });

        addLocation.setOnClickListener(v -> {
            startLocationSearch();
        });

        editLocation.setOnClickListener(view15 -> startLocationSearch());

        deleteLocation.setOnClickListener(view15 -> deleteLocation());

        itemImageLayout.setOnClickListener(view12 -> {
            showCameraDialog();
        });

        itemImageLayout.setOnLongClickListener(view14 -> {
            getRealm().beginTransaction();
            currentItem.setItemImage("");
            getRealm().commitTransaction();

            Glide.with(getContext()).load(getContext().getDrawable(R.drawable.icon_image)).into(itemImage);
            photo_info.setVisibility(View.GONE);
            adapter.notifyItemChanged(position);
            return true;
        });

        redirectToNote.setOnClickListener(view17 -> {
            InfoSheet info1 = new InfoSheet(14, redirectToNote);
            info1.show(getActivity().getSupportFragmentManager(), info1.getTag());
        });

        info.setOnClickListener(view16 -> {
            if (isTextPastedDetected) {
                isTextPasted = true;
                info.setText(info.getText().toString().split("\n")[0] + "\n");
            }
        });

        return view;
    }

    private void populateSearchAdapter(ArrayList<Note> notes) {
        notesSearchAdapter = new notes_search_recyclerview(notes, redirectToNote);
        allNotesRecyclerview.setAdapter(notesSearchAdapter);
    }

    // updates select status of note in database
    private void updateItem(CheckListItem checkListItem, String text) {
        // save status to database
        getRealm().beginTransaction();
        checkListItem.setText(text);
        currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
        checkListItem.setRedirectToOtherNote(RealmHelper.getNoteIdUsingTitle(getContext(), noteSelectedTitle));
        Log.d("Here", "note title -> " + noteSelectedTitle);
        getRealm().commitTransaction();
        ((NoteEdit) getActivity()).updateDateEdited();
        adapter.notifyItemChanged(position);
    }

    // updates place of checklist item in database
    private void updateItem(CheckListItem checkListItem, Place place) {
        // save status to database
        getRealm().beginTransaction();
        Place newPlace = getRealm().copyToRealm(place);
        checkListItem.setPlace(newPlace);
        currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
        getRealm().commitTransaction();
        ((NoteEdit) getActivity()).updateDateEdited();
        adapter.notifyItemChanged(position);
    }

    // updates select status of note in database
    private void updateItem(SubCheckListItem checkListItem, String text) {
        // save status to database
        getRealm().beginTransaction();
        checkListItem.setText(text);
        currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
        getRealm().commitTransaction();
        ((NoteEdit) getActivity()).updateDateEdited();
        adapter.notifyItemChanged(position);
        ((NoteEdit) getActivity()).checklistAdapter.notifyDataSetChanged();
    }

    // updates select status of note in database
    private void deleteItem(CheckListItem checkListItem) {
        RealmHelper.deleteChecklistItem(checkListItem, getContext(), false);
        getRealm().beginTransaction();
        currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
        getRealm().commitTransaction();
        ((NoteEdit) getActivity()).updateDateEdited();
        adapter.notifyDataSetChanged();
        ((NoteEdit) getActivity()).isListEmpty(currentNote.getChecklist().size(), true);
    }

    // updates select status of sub-note in database
    private void deleteItem(SubCheckListItem checkListItem) {
        // save status to database
        RealmHelper.deleteSublistItem(checkListItem, getContext());
        getRealm().beginTransaction();
        currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
        getRealm().commitTransaction();
        ((NoteEdit) getActivity()).updateDateEdited();
        adapter.notifyDataSetChanged();
    }

    private boolean confirmEntry(TextInputEditText itemName, TextInputLayout itemNameLayout) {
        String checklistItemsSeparator = user.getItemsSeparator();
        if (checklistItemsSeparator.equals("newline"))
            checklistItemsSeparator = "\n";
        String sublistItemsSeparator = user.getSublistSeparator();
        if (sublistItemsSeparator.equals("space"))
            sublistItemsSeparator = "\n";

        if (!itemName.getText().toString().isEmpty()) {
            if (isAdding && !isTextPasted) {
                String text = itemName.getText().toString().trim().replaceAll("<br>", "\n").replaceAll(" +", " ");
                String[] items = text.replaceAll("\n+", "\n").replaceAll(" +, +", ",,").split(checklistItemsSeparator);
                if (isSubChecklist)
                    for (String subItem : items)
                        ((NoteEdit) getActivity()).addSubCheckList(checkListItem, subItem);
                else {
                    for (String item : items) {
                        if (!item.startsWith(" ") && checklistItemsSeparator.equals("\n")) {
                            currentItem = ((NoteEdit) getActivity()).addCheckList(item.split(sublistItemsSeparator)[0], selectedPlace, noteSelectedTitle);
                            selectedPlace = new Place();
                            if (sublistItemsSeparator.equals("--")) {
                                String[] currentSublistItems = item.split(sublistItemsSeparator);
                                for (int i = 1; i < currentSublistItems.length; i++) {
                                    String currentSublistItem = currentSublistItems[i];
                                    if (!currentSublistItem.equals(currentItem.getText()))
                                        ((NoteEdit) getActivity()).addSubCheckList(getRealm().where(CheckListItem.class).equalTo("subListId", currentItem.getSubListId()).findFirst(), currentSublistItem);
                                }
                            }
                        } else if (item.startsWith(" ") && sublistItemsSeparator.equals("\n")) {
                            if (currentItem == null)
                                currentItem = ((NoteEdit) getActivity()).addCheckList("", new Place(), noteSelectedTitle);
                            ((NoteEdit) getActivity()).addSubCheckList(getRealm().where(CheckListItem.class).equalTo("subListId", currentItem.getSubListId()).findFirst(), item);
                        } else {
                            currentItem = ((NoteEdit) getActivity()).addCheckList(item.split(sublistItemsSeparator)[0], selectedPlace, noteSelectedTitle);
                            selectedPlace = new Place();
                            if (item.contains(sublistItemsSeparator)) {
                                String[] currentSublistItems = item.split(sublistItemsSeparator);
                                for (int i = 1; i < currentSublistItems.length; i++) {
                                    String currentSublistItem = currentSublistItems[i];
                                    if (!currentSublistItem.equals(currentItem.getText()))
                                        ((NoteEdit) getActivity()).addSubCheckList(getRealm().where(CheckListItem.class).equalTo("subListId", currentItem.getSubListId()).findFirst(), currentSublistItem);
                                }
                            }
                        }
                    }
                }
            } else if (isAdding) {
                if (isSubChecklist)
                    ((NoteEdit) getActivity()).addSubCheckList(checkListItem, itemName.getText().toString());
                else
                    ((NoteEdit) getActivity()).addCheckList(itemName.getText().toString(), selectedPlace, noteSelectedTitle);
            } else {
                if (isSubChecklist)
                    updateItem(currentSubItem, itemName.getText().toString());
                else
                    updateItem(currentItem, itemName.getText().toString());
            }
            return true;
        } else
            itemNameLayout.setError("Required");

        return false;
    }

    private void showCameraDialog() {
        ImagePickerConfig config = new ImagePickerConfig();
        config.setShowCamera(true);
        config.setSingleSelectMode(true);
        config.setCustomColor(UiHelper.getImagePickerTheme(getActivity()));
        config.setStatusBarContentMode(UiHelper.getLightThemePreference(getContext()) ? StatusBarContent.DARK : StatusBarContent.LIGHT);
        Intent intent = ImagePickerLauncher.Companion.createIntent(getContext(), config);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1) {
            Uri uri = data.getData();
            File createImageFile = null;
            String filePath = "";
            ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.EXTRA_IMAGES);
            if (!images.isEmpty()) {
                createImageFile = Helper.createFile(getActivity(), "image", ".png");
                filePath = Helper.createFile(getContext(), images.get(0).getUri(), createImageFile).getAbsolutePath();
            } else return;

            if (currentItem.getItemImage() != null && !currentItem.getItemImage().isEmpty()) {
                File fdelete = new File(currentItem.getItemImage());
                if (fdelete.exists()) fdelete.delete();
            }

            getRealm().beginTransaction();
            currentItem.setItemImage(filePath);
            getRealm().commitTransaction();

            Glide.with(getContext()).load(currentItem.getItemImage()).into(itemImage);
            photo_info.setVisibility(View.VISIBLE);
            dateCreated.setGravity(Gravity.LEFT);
            adapter.notifyItemChanged(position);
        } else if (requestCode == 5) {
            if (resultCode == RESULT_OK) {
                com.google.android.libraries.places.api.model.Place place = Autocomplete.getPlaceFromIntent(data);
                selectedPlace = new Place(place.getName(), place.getAddress(), place.getId(),
                        place.getLatLng().latitude, place.getLatLng().longitude);
                if (!isAdding)
                    updateItem(currentItem, selectedPlace);

                locationLayout.setVisibility(View.VISIBLE);
                placeLocation.setText(selectedPlace.getPlaceName());
            }
        }
    }

    private void openMenuDialog() {
        noteMenu = new CustomPowerMenu.Builder<>(getContext(), new IconMenuAdapter(false))
                .addItem(new IconPowerMenuItem(getContext().getDrawable(R.drawable.copy_icon), "Copy Text"))
                .addItem(new IconPowerMenuItem(getContext().getDrawable(R.drawable.send_icon), "Send"))
                .setBackgroundColor(UiHelper.getColorFromTheme(getActivity(), R.attr.secondaryBackgroundColor))
                .setOnMenuItemClickListener(onIconMenuItemClickListener)
                .setAnimation(MenuAnimation.DROP_DOWN)
                .setMenuRadius(15f)
                .setMenuShadow(10f)
                .build();

        if (currentItem != null) {
            if (currentItem.getPlace() == null || currentItem.getPlace().getPlaceName().isEmpty())
                noteMenu.addItem(0, new IconPowerMenuItem(getContext().getDrawable(R.drawable.add_location_icon), "Location"));
        }

        noteMenu.showAsAnchorLeftTop(dropDownMenu);
    }

    private final OnMenuItemClickListener<IconPowerMenuItem> onIconMenuItemClickListener = new OnMenuItemClickListener<IconPowerMenuItem>() {
        @Override
        public void onItemClick(int position, IconPowerMenuItem item) {
            if (item.getTitle().equals("Location"))
                startLocationSearch();
            else if (item.getTitle().equals("Copy Text")) {
                // copy text
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip;
                if (!isSubChecklist)
                    clip = ClipData.newPlainText("Code", currentItem.getText());
                else
                    clip = ClipData.newPlainText("Code", currentSubItem.getText());
                clipboard.setPrimaryClip(clip);
                Helper.showMessage(getActivity(), "Success!",
                        "Copied successfully", MotionToast.TOAST_SUCCESS);
            } else if (item.getTitle().equals("Send")) {
                try {
                    Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                    emailIntent.setType("*/*");

                    ArrayList<Uri> uris = new ArrayList<>();

                    if (!isSubChecklist) {
                        File file = new File(currentItem.getItemImage());
                        if (file.exists()) {
                            uris.add(FileProvider.getUriForFile(
                                    getContext(),
                                    "com.akapps.dailynote.fileprovider",
                                    file));
                            emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                        }
                        if (currentItem.getAudioPath() != null && !currentItem.getAudioPath().isEmpty()) {
                            file = new File(currentItem.getAudioPath());
                            if (file.exists()) {
                                uris.add(FileProvider.getUriForFile(
                                        getContext(),
                                        "com.akapps.dailynote.fileprovider",
                                        file));
                            }
                        }
                        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                        emailIntent.putExtra(Intent.EXTRA_TEXT, currentItem.getText());
                    } else
                        emailIntent.putExtra(Intent.EXTRA_TEXT, currentSubItem.getText());

                    // adds email subject and email body to intent
                    getContext().startActivity(Intent.createChooser(emailIntent, "Share Checklist Item"));
                } catch (Exception e) {
                    Helper.showMessage(getActivity(), "Sharing Error", "Size too large v20, email developer", MotionToast.TOAST_ERROR);
                }
            }
            noteMenu.dismiss();
        }
    };

    private void startLocationSearch() {
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        List<com.google.android.libraries.places.api.model.Place.Field> fields =
                Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ID,
                        com.google.android.libraries.places.api.model.Place.Field.NAME,
                        com.google.android.libraries.places.api.model.Place.Field.LAT_LNG);

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(getContext());
        startActivityForResult(intent, 5);
    }

    private void deleteLocation() {
        if (isAdding)
            selectedPlace = new Place();
        else {
            getRealm().beginTransaction();
            currentItem.setPlace(getRealm().copyToRealm(new Place()));
            getRealm().commitTransaction();
            adapter.notifyItemChanged(position);
        }
        locationLayout.setVisibility(View.GONE);
    }

    private Realm getRealm() {
        return RealmSingleton.getInstance(getContext());
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
        return UiHelper.getBottomSheetTheme(getContext());
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
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dialog = (BottomSheetDialog) getDialog();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(true);
        UiHelper.setBottomSheetBehavior(view, dialog);
    }

}