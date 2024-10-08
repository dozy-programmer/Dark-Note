package com.akapps.dailynote.activity;

import static com.akapps.dailynote.classes.helpers.RealmHelper.getCurrentNote;
import static com.akapps.dailynote.classes.helpers.UiHelper.getThemeStyle;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akapps.dailynote.R;
import com.akapps.dailynote.adapter.IconMenuAdapter;
import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.Photo;
import com.akapps.dailynote.classes.data.Place;
import com.akapps.dailynote.classes.data.SubCheckListItem;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.AlertReceiver;
import com.akapps.dailynote.classes.helpers.AppConstants;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.MediaHelper;
import com.akapps.dailynote.classes.helpers.RealmHelper;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.akapps.dailynote.classes.other.BudgetSheet;
import com.akapps.dailynote.classes.other.CheckListDeleteSheet;
import com.akapps.dailynote.classes.other.CheckListHideSheet;
import com.akapps.dailynote.classes.other.ChecklistItemSheet;
import com.akapps.dailynote.classes.other.ColorSheet;
import com.akapps.dailynote.classes.other.ExportNotesSheet;
import com.akapps.dailynote.classes.other.FilterChecklistSheet;
import com.akapps.dailynote.classes.other.GenericInfoSheet;
import com.akapps.dailynote.classes.other.IconPowerMenuItem;
import com.akapps.dailynote.classes.other.InfoSheet;
import com.akapps.dailynote.classes.other.LockSheet;
import com.akapps.dailynote.classes.other.MediaSelectionSheet;
import com.akapps.dailynote.classes.other.NoteInfoSheet;
import com.akapps.dailynote.classes.other.RecordAudioSheet;
import com.akapps.dailynote.classes.other.insertsheet.InsertImageSheet;
import com.akapps.dailynote.classes.other.insertsheet.InsertLinkSheet;
import com.akapps.dailynote.classes.other.insertsheet.InsertYoutubeVideoSheet;
import com.akapps.dailynote.classes.other.insertsheet.MediaTypeSheet;
import com.akapps.dailynote.recyclerview.checklist_recyclerview;
import com.akapps.dailynote.recyclerview.photos_recyclerview;
import com.bumptech.glide.Glide;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.material.button.MaterialButton;
import com.skydoves.powermenu.CustomPowerMenu;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.stfalcon.imageviewer.StfalconImageViewer;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import io.realm.Realm;
import io.realm.RealmResults;
import jp.wasabeef.richeditor.RichEditor;
import www.sanju.motiontoast.MotionToast;

public class NoteEdit extends FragmentActivity implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    // layout
    public EditText title;
    public TextView date;
    public RichEditor note;
    private CardView closeNote;
    public CardView photosNote;
    private ImageView pinNoteIcon;
    private CardView pinNoteButton;
    private CardView noteColor;
    private CardView expandMenu;
    private LinearLayout formatMenu;
    private RecyclerView photosScrollView;
    public RecyclerView.Adapter scrollAdapter;
    private LinearLayout remindNote;
    private TextView remindNoteDate;
    private ImageView palleteIconColor;
    private TextView category;
    private TextView folderText;
    private ConstraintLayout scrollView;
    private FloatingActionButton budget;
    private HorizontalScrollView editorScroll;
    // search
    private EditText searchEditText;
    private ImageView searchClose;
    private ImageView search;
    private CardView searchLayout;

    // on-device database
    private RealmResults<CheckListItem> checkListItems;

    // activity data
    private Context context;
    public int noteId;
    private boolean isNewNote;
    private boolean isNewNoteCopy;
    private String oldTitle;
    private String oldNote;
    private boolean currentPin;
    private boolean dismissDialog;
    private boolean isShowingImagePreviewDialog = false;
    private boolean isAppPaused;
    private boolean isShowingPhotos;
    private boolean isEditLockedMode = true;
    private String currentDateTimeSelected;
    private Calendar dateSelected;
    private int countPicsNotFound;
    private boolean isSearchingNotes;
    private int currentWordIndex;
    private String target;
    private ArrayList wordOccurences = new ArrayList<String>();
    private boolean isChangingTextSize;
    private boolean isChanged;
    private boolean isWidget;
    private Handler handler;
    private boolean dismissNotification;
    private boolean isReverseScroll = false;
    // dialog
    private AlertDialog colorPickerView;
    private CustomPowerMenu noteMenu;
    private boolean hideRichTextStatus;
    private String titleFromOtherApp;
    private String noteFromOtherApp;
    private ArrayList<String> importedImages;

    // Change Text Size Layout
    private LinearLayout textSizeLayout;
    private com.google.android.material.floatingactionbutton.FloatingActionButton increaseTextSize;
    private com.google.android.material.floatingactionbutton.FloatingActionButton decreaseTextSize;
    private com.google.android.material.floatingactionbutton.FloatingActionButton closeTextLayout;

    // checklist data
    private boolean isCheckList;
    private RecyclerView checkListRecyclerview;
    public RecyclerView.Adapter checklistAdapter;
    private com.google.android.material.floatingactionbutton.FloatingActionButton addCheckListItem;
    private com.google.android.material.floatingactionbutton.FloatingActionButton addAudioItem;
    private MaterialButton reorderButton;

    // drag and drop
    private List<CheckListItem> checkListItemsUnmanaged;
    private ItemTouchHelper.Callback callback;
    private ItemTouchHelper helper;

    // empty list layout
    private ScrollView empty_Layout;
    private TextView empty_title;
    private TextView subtitle;
    private TextView subSubTitle;

    private final String ACTION_ADD_CHECKLIST = "android.intent.action.CREATE_SHORTCUT";

    private ActivityResultLauncher<Intent> startAlarmPermission;
    private ActivityResultLauncher<Intent> categoryLauncher;

    public boolean showLockScreen = true;

    private boolean boldSelected = false;
    private boolean italicsSelected = false;
    private boolean crossedSelected = false;
    private boolean underlineSelected = false;

    private String tempCameraPhotoPath;

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicture(),
                    result -> {
                        if (tempCameraPhotoPath != null) {
                            Photo currentPhoto = new Photo(noteId, tempCameraPhotoPath);
                            getRealm().beginTransaction();
                            getRealm().insert(currentPhoto);
                            getRealm().commitTransaction();
                            updateSaveDateEdited();
                            scrollAdapter.notifyDataSetChanged();
                            showPhotos(View.VISIBLE);
                            tempCameraPhotoPath = null;
                        }
                    });

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(10), uris -> {
                if (!uris.isEmpty()) {
                    for (Uri image : uris) {
                        File newFile = Helper.createFile(this, "image", ".png");
                        String filePath = Helper.createFile(context, image, newFile).getAbsolutePath();
                        Photo currentPhoto = new Photo(noteId, filePath);
                        getRealm().beginTransaction();
                        getRealm().insert(currentPhoto);
                        getRealm().commitTransaction();
                    }
                    updateSaveDateEdited();
                    scrollAdapter.notifyDataSetChanged();
                    showPhotos(View.VISIBLE);
                } else {
                    Log.d("Here", "No media selected");
                }
            });

    private final ActivityResultLauncher<String[]> mediaPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            isGranted -> {
                if (isGranted.containsValue(false)) {
                    GenericInfoSheet infoSheet = new GenericInfoSheet("Media Permission", "This " +
                            "permission is required so that you can select images from your phone.\n\n" +
                            "To enable, go to Dark Note Settings -> App Settings -> Permissions\n\nor Click Proceed", "Proceed", 1);
                    infoSheet.show(getSupportFragmentManager(), infoSheet.getTag());
                } else {
                    MediaHelper.openMedia(pickMultipleMedia);
                }
            });

    private final ActivityResultLauncher<String[]> cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            isGranted -> {
                if (isGranted.containsValue(false)) {
                    GenericInfoSheet infoSheet = new GenericInfoSheet("Camera Permission", "This " +
                            "permission is required so that you can use the camera.\n\n" +
                            "To enable, go to Dark Note Settings -> App Settings -> Permissions\n\nor Click Proceed", "Proceed", 1);
                    infoSheet.show(getSupportFragmentManager(), infoSheet.getTag());
                } else {
                    tempCameraPhotoPath = MediaHelper.openCamera(this, context, takePictureLauncher);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(getThemeStyle(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        AppData.getAppData();

        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        context = this;
        noteId = getIntent().getIntExtra("id", -1);

        if (noteId > 0 && getRealm().where(Note.class).equalTo("noteId", noteId).count() == 0) {
            RealmSingleton.setCloseRealm(false);
            // this catches a widget whose associated note has been deleted
            Toast.makeText(this, "Note has been deleted, please delete widget!", Toast.LENGTH_LONG).show();
            RealmSingleton.closeRealmInstance("NoteEdit onDestroy - Deleted Note is being accessed via widget");
            finish();
            return;
        }

        noteFromOtherApp = getIntent().getStringExtra("otherAppNote");
        titleFromOtherApp = getIntent().getStringExtra("otherAppTitle");
        importedImages = getIntent().getStringArrayListExtra("images");
        dismissNotification = getIntent().getBooleanExtra("dismissNotification", false);
        if (ACTION_ADD_CHECKLIST.equals(getIntent().getAction()))
            isCheckList = true;
        else
            isCheckList = getIntent().getBooleanExtra("isChecklist", false);

        if (noteId < -1)
            noteId *= -1;
        else if (noteId == -1) {
            isNewNote = true;
            isNewNoteCopy = true;
        } else if (!AppData.isDisableAnimation)
            overridePendingTransition(R.anim.left_in, R.anim.stay);

        // if orientation changes, then position is updated
        if (savedInstanceState != null)
            noteId = savedInstanceState.getInt("id");

        initializeLayout(savedInstanceState);
        changeTextSize(true, getCurrentNote(context, noteId).isCheckList());
        changeTextSize(false, getCurrentNote(context, noteId).isCheckList());
        UiHelper.setStatusBarColor(this);

        scrollView.setBackgroundColor(UiHelper.getColorFromTheme(this, R.attr.primaryBackgroundColor));
        note.setBackgroundColor(UiHelper.getColorFromTheme(this, R.attr.primaryBackgroundColor));
        searchEditText.setTextColor(UiHelper.getColorFromTheme(this, R.attr.primaryTextColor));
        budget.setColorNormal(UiHelper.getColorFromTheme(this, R.attr.secondaryBackgroundColor));

        hideRichTextStatus = getUser().isHideRichTextEditor();
        if (getUser().isHideRichTextEditor())
            formatMenu.setVisibility(View.GONE);
        if (!getUser().isShowAudioButton() && getCurrentNote(context, noteId).isCheckList())
            addAudioItem.setVisibility(View.VISIBLE);
        if (!getUser().isHideBudget() && getCurrentNote(context, noteId).isCheckList())
            budget.setVisibility(View.VISIBLE);

        updateOtherColors();

        TextView text = (TextView) findViewById(R.id.text_direction);
        if (getCurrentNote(context, noteId).getNoteTextDirection() == null || getCurrentNote(context, noteId).getNoteTextDirection().isEmpty()) {
            getRealm().beginTransaction();
            getCurrentNote(context, noteId).setNoteTextDirection("ltr");
            getRealm().commitTransaction();
            text.setText("RTL");
            Log.d("Here", "I am null, setting to ltr");
            note.setDefaultDirection();
        } else if (getCurrentNote(context, noteId).getNoteTextDirection().equals("rtl")) {
            text.setText("LTR");
            note.setRtlDirection();
            Log.d("Here", "I am set to rtl");
        } else if (getCurrentNote(context, noteId).getNoteTextDirection().equals("ltr")) {
            text.setText("RTL");
            note.setDefaultDirection();
            Log.d("Here", "I am set to ltr");
        }

        if (RealmHelper.isNoteWidget(context, noteId))
            Helper.updateWidget(getCurrentNote(context, noteId), context, getRealm());

        if (dismissNotification)
            Helper.cancelNotification(context, noteId);

        startAlarmPermission = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), (ActivityResult result) -> {
                    if (result.getResultCode() == RESULT_OK) {
                        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (dateSelected != null && alarmManager.canScheduleExactAlarms())
                                startAlarm(dateSelected);
                            else
                                Helper.showMessage(this, "Alarm not set", "Alarm permission needs to be enabled",
                                        MotionToast.TOAST_ERROR);
                        }
                    }
                });

        categoryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d("Here", "launcher");
                    showLockScreen = false;
                }
        );

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getCurrentNote(context, noteId) == null) {
                    finish("note is null");
                    if (!AppData.isDisableAnimation)
                        overridePendingTransition(R.anim.stay, R.anim.right_out);
                } else if (getCurrentNote(context, noteId).getTitle().isEmpty() && getCurrentNote(context, noteId).getNote().isEmpty() &&
                        getCurrentNote(context, noteId).getChecklist().size() == 0)
                    closeAndDeleteNote();
                else if (isSearchingNotes) {
                    hideSearchBar();
                    note.clearFocus();
                    title.clearFocus();
                } else if (isChangingTextSize) {
                    isChangingTextSize = false;
                    hideButtons();
                    if (getCurrentNote(context, noteId).isCheckList())
                        showChecklistButtons();
                    else {
                        if (getUser().isEnableEditableNoteButton()) {
                            addCheckListItem.setVisibility(View.VISIBLE);
                            addCheckListItem.setImageDrawable(getDrawable(
                                    isEditLockedMode ? R.drawable.edit_icon : R.drawable.do_not_edit_icon));
                        }
                    }
                } else {
                    if (noteChanged() && !isNewNote)
                        Helper.showMessage(NoteEdit.this, "Edited", "Note has been edited", MotionToast.TOAST_SUCCESS);

                    finish("on-back press");
                    if (!AppData.isDisableAnimation)
                        overridePendingTransition(R.anim.stay, R.anim.right_out);
                }
            }
        });
    }

    private void swap(int initialFrom, int initialTo) {
        int oldInitialPosition = checkListItemsUnmanaged.get(initialFrom).getPositionInList();
        int oldInitialToPosition = checkListItemsUnmanaged.get(initialTo).getPositionInList();
        if (initialFrom < 0 || initialTo < 0) return;
        checkListItemsUnmanaged.get(initialFrom).setPositionInList(oldInitialToPosition);
        checkListItemsUnmanaged.get(initialTo).setPositionInList(oldInitialPosition);
        Collections.swap(checkListItemsUnmanaged, initialFrom, initialTo);
    }

    // when orientation changes, then note data is saved
    @Override
    public void onSaveInstanceState(@NotNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("title", title.getText().toString());
        try {
            savedInstanceState.putString("note", note.getHtml());
        } catch (Exception e) {
        }
        savedInstanceState.putBoolean("pin", currentPin);
        savedInstanceState.putBoolean("newNote", isNewNote);
        savedInstanceState.putInt("id", noteId);
        savedInstanceState.putBoolean("photos", isShowingPhotos);
        savedInstanceState.putBoolean("search", isSearchingNotes);
        savedInstanceState.putBoolean("do_not_edit", isEditLockedMode);
    }

    @Override
    protected void onPause() {
        isAppPaused = true;
        if (RealmHelper.isNoteWidget(context, noteId))
            Helper.updateWidget(getCurrentNote(context, noteId), context, getRealm());
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        RealmSingleton.closeRealmInstance("NoteEdit onDestroy");

        if (handler != null)
            handler.removeCallbacksAndMessages(null);

        // if color picker dialog is open, close it so that memory isn't leaked
        if (colorPickerView != null)
            colorPickerView.cancel();
        if (noteMenu != null)
            noteMenu.dismiss();

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Here", "on resume");

        if (RealmHelper.getNotePin(context, noteId) != 0 && isAppPaused && showLockScreen) {
            RealmSingleton.setCloseRealm(false);
            Intent lockScreen = new Intent(this, NoteLockScreen.class);
            lockScreen.putExtra("id", getCurrentNote(context, noteId).getNoteId());
            lockScreen.putExtra("title", getCurrentNote(context, noteId).getTitle().replace("\n", " "));
            lockScreen.putExtra("pin", getCurrentNote(context, noteId).getPinNumber());
            lockScreen.putExtra("securityWord", getCurrentNote(context, noteId).getSecurityWord());
            lockScreen.putExtra("fingerprint", getCurrentNote(context, noteId).isFingerprint());
            startActivity(lockScreen);
            if (!AppData.isDisableAnimation)
                overridePendingTransition(R.anim.stay, R.anim.right_out);
            finish();
        } else if (!getRealm().isClosed() && getCurrentNote(context, noteId) != null) {
            category.setText(getCategoryName());
        }
        isAppPaused = false;
        showLockScreen = true;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeLayout(Bundle savedInstanceState) {
        // layout is initialized
        title = findViewById(R.id.title);
        date = findViewById(R.id.date);
        note = findViewById(R.id.note);
        closeNote = findViewById(R.id.close_note);
        photosNote = findViewById(R.id.camera);
        pinNoteButton = findViewById(R.id.pinButton);
        pinNoteIcon = findViewById(R.id.pinIcon);
        palleteIconColor = findViewById(R.id.pallete_icon);
        noteColor = findViewById(R.id.noteColor);
        expandMenu = findViewById(R.id.menu);
        photosScrollView = findViewById(R.id.note_photos);
        remindNote = findViewById(R.id.reminderLayout);
        remindNoteDate = findViewById(R.id.reminderDate);
        textSizeLayout = findViewById(R.id.text_size_layout);
        increaseTextSize = findViewById(R.id.increase_textsize);
        decreaseTextSize = findViewById(R.id.decrease_textsize);
        closeTextLayout = findViewById(R.id.close_Layout);
        addCheckListItem = findViewById(R.id.add_checklist_item);
        addAudioItem = findViewById(R.id.add_audio_note);
        reorderButton = findViewById(R.id.confirm_reorder);
        checkListRecyclerview = findViewById(R.id.checklist);
        empty_Layout = findViewById(R.id.empty_Layout);
        empty_title = findViewById(R.id.empty_title);
        budget = findViewById(R.id.budget);
        subtitle = findViewById(R.id.empty_subtitle);
        subSubTitle = findViewById(R.id.empty_sub_subtitle);
        category = findViewById(R.id.category);
        folderText = findViewById(R.id.folderWord);
        formatMenu = findViewById(R.id.styleFormat);
        scrollView = findViewById(R.id.scroll);
        editorScroll = findViewById(R.id.horizontalScrollView);

        // search
        searchEditText = findViewById(R.id.search_text);
        searchClose = findViewById(R.id.search_close);
        search = findViewById(R.id.search);
        searchLayout = findViewById(R.id.search_padding);

        note.setEditorFontSize(20);
        note.setPlaceholder("Type something...");
        note.setEditorFontColor(UiHelper.getColorFromTheme(this, R.attr.primaryTextColor));
        note.setPadding(5, 10, 0, 100);
        note.getSettings().setAllowFileAccess(true);
        note.setBackgroundColor(UiHelper.getColorFromTheme(this, R.attr.primaryBackgroundColor));
        note.focusEditor();

        photosScrollView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // if it's a new note, create it
        if (isNewNote || noteId == -1)
            addNote();

        photosNote.setVisibility(View.VISIBLE);
        pinNoteIcon.setVisibility(View.VISIBLE);
        pinNoteButton.setVisibility(View.VISIBLE);
        noteColor.setVisibility(View.VISIBLE);
        expandMenu.setVisibility(View.VISIBLE);

        // current note
        if (null != getCurrentNote(context, noteId) && null != getCurrentNote(context, noteId).getChecklist()) {
            checkListItems = getCurrentNote(context, noteId).getChecklist().sort("positionInList");
        }
        populatePhotos();
        oldTitle = getCurrentNote(context, noteId).getTitle();
        oldNote = getCurrentNote(context, noteId).getNote();
        title.setText(getCurrentNote(context, noteId).getTitle());
        note.setHtml(getCurrentNote(context, noteId).getNote());
        //Log.d("Here", "note -> " + note.getHtml());
        note.setEditorFontSize(RealmHelper.getUser(context, "checklist_recyclerview").getTextSize());
        note.setEditorFontColor(RealmHelper.getTextColorBasedOnTheme(context, noteId));
        title.setTextColor(getCurrentNote(context, noteId).getTitleColor());
        category.setText(getCategoryName());
        category.setVisibility(View.VISIBLE);
        folderText.setVisibility(View.VISIBLE);
        searchLayout.setVisibility(View.VISIBLE);

        initializeEditor();
        updateColors();

        if (getCurrentNote(context, noteId) == null) {
            finish("checklist is null");
            return;
        }

        if (getCurrentNote(context, noteId).isChecked())
            title.setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        if (getCurrentNote(context, noteId).isPin())
            pinNoteIcon.setImageDrawable(getDrawable(R.drawable.pin_filled_icon));

        if (getCurrentNote(context, noteId).isCheckList()) {
            sortChecklist("");
            showCheckListLayout(true);
            formatMenu.setVisibility(View.GONE);
            checkListRecyclerview.requestFocus();
        } else {
            formatMenu.setVisibility(View.VISIBLE);

            if (getUser().isEnableEditableNoteButton() && !isNewNoteCopy) {
                note.setInputEnabled(!isEditLockedMode);
                addCheckListItem.setVisibility(View.VISIBLE);
                addCheckListItem.setImageDrawable(getDrawable(
                        isEditLockedMode ? R.drawable.edit_icon : R.drawable.do_not_edit_icon));
            }

            if (isNewNote)
                title.requestFocus();
            else
                note.focusEditor();
        }

        // sets background of color icon to whatever the current note color is
        if (!isNewNote)
            noteColor.setCardBackgroundColor(getCurrentNote(context, noteId).getBackgroundColor());

        updateDateEdited();
        if (!getCurrentNote(context, noteId).getReminderDateTime().isEmpty()) {
            updateReminderLayout(View.VISIBLE, 0);
            Date reminderDate = null;
            try {
                reminderDate = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").parse(getCurrentNote(context, noteId).getReminderDateTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date now = new Date();
            if (now.after(reminderDate)) {
                remindNoteDate.setTextColor(UiHelper.getColorFromTheme(this, R.attr.tertiaryButtonColor));
                Helper.showMessage(NoteEdit.this, "Reminder Passed", "Please delete reminder to " +
                        "get rid of this message!", MotionToast.TOAST_WARNING);
            }
        }

        // if orientation changes, then it updates note data
        if (savedInstanceState != null) {
            title.setText(savedInstanceState.getString("title"));
            note.setHtml(savedInstanceState.getString("note"));
            currentPin = savedInstanceState.getBoolean("pin");
            isNewNote = savedInstanceState.getBoolean("newNote");
            isShowingPhotos = savedInstanceState.getBoolean("photos");
            isSearchingNotes = savedInstanceState.getBoolean("search");
            isEditLockedMode = savedInstanceState.getBoolean("do_not_edit");

            isNewNoteCopy = isNewNote;

            if (isSearchingNotes)
                showSearchBar();

            if (currentPin)
                pinNoteIcon.setImageDrawable(getDrawable(R.drawable.pin_filled_icon));

            if (isShowingPhotos) {
                note.clearFocus();
                showPhotos(View.VISIBLE);
                addCheckListItem.setVisibility(View.VISIBLE);
                if (!getCurrentNote(context, noteId).isCheckList())
                    addCheckListItem.setImageDrawable(getDrawable(R.drawable.media_selector_icon));
            }

            if (isSearchingNotes) {
                addCheckListItem.setVisibility(View.GONE);
                photosScrollView.setVisibility(View.GONE);
            }

            if (getUser().isEnableEditableNoteButton() && !isNewNoteCopy) {
                addCheckListItem.setVisibility(View.VISIBLE);
                addCheckListItem.setImageDrawable(getDrawable(
                        isEditLockedMode ? R.drawable.edit_icon : R.drawable.do_not_edit_icon));
            }
        }

        search.setOnClickListener(v -> {
            if (getCurrentNote(context, noteId).isCheckList()) {
                textSizeLayout.setVisibility(View.VISIBLE);
                showSearchBar();
            } else if (note.getHtml().length() > 0) {
                textSizeLayout.setVisibility(View.VISIBLE);
                showSearchBar();
            } else
                Helper.showMessage(this, "Empty", "Searching for something " +
                        "that does not exist is impossible", MotionToast.TOAST_WARNING);
        });

        scrollView.setOnClickListener(view -> {
            if (getCurrentNote(context, noteId) != null && !getCurrentNote(context, noteId).isCheckList()) {
                if (!getUser().isEnableEditableNoteButton() || !isEditLockedMode) {
                    note.focusEditor();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(note, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (getCurrentNote(context, noteId).isCheckList()) {
                    if (s == null) return;
                    AppData.resetWordFoundPositions();
                    RealmResults<CheckListItem> results = Helper.sortChecklist(context, noteId, getRealm());
                    boolean updateRecyclerview = false;
                    for (int i = 0; i < results.size(); i++) {
                        CheckListItem item = results.get(i);
                        if (item.getText().toLowerCase().contains(s.toString().toLowerCase())) {
                            AppData.addWordFoundPositions(i);
                            updateRecyclerview = true;
                        }
                    }
                    if (updateRecyclerview) sortChecklist(s.toString());
                } else {
                    currentWordIndex = -1;
                    if (isSearchingNotes) {
                        textSizeLayout.setVisibility(View.VISIBLE);
                        if (!s.toString().isEmpty())
                            findText(s.toString().toLowerCase());
                        else {
                            wordOccurences = new ArrayList();
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        searchClose.setOnClickListener(v -> {
            if (isSearchingNotes)
                hideSearchBar();
        });

        budget.setOnClickListener(v -> {
            BudgetSheet budget = new BudgetSheet(noteId, getUser().getBudgetCharacter(), getUser().getExpenseCharacter());
            budget.show(this.getSupportFragmentManager(), budget.getTag());
        });

        category.setOnClickListener(v -> {
            getRealm().beginTransaction();
            getRealm().where(Note.class).findAll().setBoolean("isSelected", false);
            getCurrentNote(context, noteId).setSelected(true);
            getRealm().commitTransaction();
            Intent category = new Intent(NoteEdit.this, CategoryScreen.class);
            category.putExtra("editing_reg_note", true);
            categoryLauncher.launch(category);
            if (!AppData.isDisableAnimation)
                overridePendingTransition(R.anim.show_from_bottom, R.anim.stay);
        });

        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (getCurrentNote(context, noteId) != null && !getCurrentNote(context, noteId).getTitle().equals(s.toString())) {
                    getRealm().beginTransaction();
                    getCurrentNote(context, noteId).setTitle(s.toString());
                    getRealm().commitTransaction();
                    updateSaveDateEdited();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        if (getCurrentNote(context, noteId) != null) {
            note.setOnTextChangeListener(text -> {
                oldTitle = getCurrentNote(context, noteId).getTitle();
                oldNote = getCurrentNote(context, noteId).getNote();
                if (text.length() == 0 || getCurrentNote(context, noteId).getNote().equals(text)) {
                    if (text.length() == 0)
                        saveChanges(text, 0);
                } else {
                    if (getCurrentNote(context, noteId) != null && !getCurrentNote(context, noteId).getNote().equals(text)) {
                        saveChanges(text, 1);
                    }
                }
                if (isSearchingNotes) {
                    textSizeLayout.setVisibility(View.VISIBLE);
                    if (currentWordIndex > 0)
                        currentWordIndex = -1;
                    wordOccurences = new ArrayList();
                    String s = searchEditText.getText().toString();
                    if (!s.isEmpty() && s.length() > 1)
                        findText(s.toString().toLowerCase());
                }
            });
            note.setOnDecorationChangeListener((text, types) -> {
                findViewById(R.id.action_bold).setBackgroundColor(context.getResources().getColor(R.color.transparent));
                findViewById(R.id.action_underline).setBackgroundColor(context.getResources().getColor(R.color.transparent));
                findViewById(R.id.action_italic).setBackgroundColor(context.getResources().getColor(R.color.transparent));
                findViewById(R.id.action_strikethrough).setBackgroundColor(context.getResources().getColor(R.color.transparent));
                int selected = UiHelper.getColorFromTheme(context, R.attr.tertiaryBackgroundColor);
                for (RichEditor.Type type : types) {
                    Log.d("Here", "onStateChangeListener() called with: text = [" + text + "], types = [" + types + "]");
                    switch (type) {
                        case BOLD:
                            findViewById(R.id.action_bold).setBackgroundColor(selected);
                            break;
                        case UNDERLINE:
                            findViewById(R.id.action_underline).setBackgroundColor(selected);
                            break;
                        case ITALIC:
                            findViewById(R.id.action_italic).setBackgroundColor(selected);
                            break;
                        case STRIKETHROUGH:
                            findViewById(R.id.action_strikethrough).setBackgroundColor(selected);
                            break;
                    }
                }
            });
            note.setOnImageClickListener(new RichEditor.ImageClickListener() {
                @Override
                public void onImageClick(String imageSrc) {
                    if (!isShowingImagePreviewDialog) {
                        isShowingImagePreviewDialog = true;
                        Log.d("Here", "NoteEdit: recieved -> " + imageSrc);
                        ArrayList<String> images = new ArrayList<>();
                        images.add(imageSrc);
                        Handler mainHandler = new Handler(Looper.getMainLooper());
                        Runnable myRunnable = () ->
                                new StfalconImageViewer.Builder<>(context, images, (imageView, image) -> Glide.with(context).load(image).into(imageView))
                                        .withBackgroundColor(context.getResources().getColor(R.color.gray))
                                        .allowZooming(true)
                                        .allowSwipeToDismiss(true)
                                        .withHiddenStatusBar(false)
                                        .withStartPosition(0)
                                        .withDismissListener(() -> isShowingImagePreviewDialog = false)
                                        .show();
                        mainHandler.post(myRunnable);
                    }
                    Helper.toggleKeyboard(context, note, false);
                }

                @Override
                public void onImageClick(String imageSrc, String html, int width, int height) {
                    Helper.toggleKeyboard(context, note, false);
                    if ((imageSrc.contains("embed") && imageSrc.contains("you")) || imageSrc.contains("youtube")) {
                        InsertYoutubeVideoSheet insertYoutubeVideoSheet = new InsertYoutubeVideoSheet(imageSrc, html, width, height);
                        insertYoutubeVideoSheet.show(getSupportFragmentManager(), insertYoutubeVideoSheet.getTag());
                        Log.d("Here", "youtube");
                    } else if (imageSrc.contains("www") || imageSrc.contains("http")) {
                        InsertLinkSheet insertLinkSheet = new InsertLinkSheet(imageSrc, html, width, height);
                        insertLinkSheet.show(getSupportFragmentManager(), insertLinkSheet.getTag());
                        Log.d("Here", "online image");
                    } else {
                        InsertImageSheet insertImageSheet = new InsertImageSheet(imageSrc, html, width, height);
                        insertImageSheet.show(getSupportFragmentManager(), insertImageSheet.getTag());
                        Log.d("Here", "local image");
                    }
                }
            });
        }

        closeTextLayout.setOnClickListener(v -> {
            isChangingTextSize = false;
            if (!isSearchingNotes) {
                hideButtons();
                if (getCurrentNote(context, noteId).isCheckList())
                    showChecklistButtons();
                else {
                    if (getUser().isEnableEditableNoteButton()) {
                        addCheckListItem.setVisibility(View.VISIBLE);
                        addCheckListItem.setImageDrawable(getDrawable(
                                isEditLockedMode ? R.drawable.edit_icon : R.drawable.do_not_edit_icon));
                    }
                }
            } else {
                hideSearchBar();
                title.clearFocus();
            }
        });

        increaseTextSize.setOnClickListener(view -> {
            Helper.toggleKeyboard(context, note, false);
            if (isChangingTextSize)
                changeTextSize(true, getCurrentNote(context, noteId).isCheckList());
            else if (getCurrentNote(context, noteId).isCheckList()) {
                if (AppData.getWordFoundPositions().size() == 0) return;
                int currentIndex = AppData.getIndexPosition(true);
                checkListRecyclerview.smoothScrollToPosition(currentIndex);
            } else if (isSearchingNotes) {
                if (wordOccurences.size() != 0) {
                    int nextIndex;
                    currentWordIndex = (currentWordIndex <= 0) ? wordOccurences.size() - 1
                            : currentWordIndex - 1;
                    nextIndex = Integer.parseInt(wordOccurences.get(currentWordIndex).toString());
                    note.requestFocus();
                    isReverseScroll = (currentWordIndex <= 0) ? false : true;
                    note.setSelection(nextIndex);
                    Helper.toggleKeyboard(context, note, false);
                }
            }
        });

        reorderButton.setOnClickListener(view -> {
            RealmHelper.updateChecklistOrdering(context, checkListItemsUnmanaged, noteId);
            disableSorting();
        });

        decreaseTextSize.setOnClickListener(view -> {
            Helper.toggleKeyboard(context, note, false);
            if (isChangingTextSize)
                changeTextSize(false, getCurrentNote(context, noteId).isCheckList());
            else if (getCurrentNote(context, noteId).isCheckList()) {
                if (AppData.getWordFoundPositions().size() == 0) return;
                int currentIndex = AppData.getIndexPosition(false);
                checkListRecyclerview.smoothScrollToPosition(currentIndex);
            } else if (isSearchingNotes) {
                int nextIndex;
                if (wordOccurences.size() != 0) {
                    currentWordIndex = (currentWordIndex == -1 || currentWordIndex == wordOccurences.size() - 1) ? 0 :
                            currentWordIndex + 1;
                    nextIndex = Integer.parseInt(wordOccurences.get(currentWordIndex).toString());
                    note.requestFocus();
                    isReverseScroll = false;
                    note.setSelection(nextIndex);
                    Helper.toggleKeyboard(context, note, false);
                }
            }
        });

        //note.setOnClickListener(view -> {});

        remindNote.setOnClickListener(v -> {
            cancelAlarm(getCurrentNote(context, noteId).getNoteId());
            remindNote.setVisibility(View.GONE);
            showMessage("Reminder", "Reminder was deleted", false);
        });

        closeNote.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        addCheckListItem.setOnClickListener(v -> {
            if (getCurrentNote(context, noteId).isCheckList() && !isShowingPhotos)
                openNewItemDialog();
            else if (isShowingPhotos) {
                MediaSelectionSheet mediaSelectionSheet = new MediaSelectionSheet(0);
                mediaSelectionSheet.show(getSupportFragmentManager(), mediaSelectionSheet.getTag());
            } else {
                note.setInputEnabled(isEditLockedMode);
                isEditLockedMode = !isEditLockedMode;
                addCheckListItem.setImageDrawable(getDrawable(
                        isEditLockedMode ? R.drawable.edit_icon : R.drawable.do_not_edit_icon));
            }
        });

        addCheckListItem.setOnLongClickListener(view -> {
            if (Helper.deviceMicExists(this)) {
                if (isMicrophonePermissionEnabled())
                    checkMicrophonePermission();
                else {
                    InfoSheet permissionInfo = new InfoSheet("Microphone Permission Required", Manifest.permission.RECORD_AUDIO);
                    permissionInfo.show(getSupportFragmentManager(), permissionInfo.getTag());
                }
            } else
                Helper.showMessage(NoteEdit.this, "Mic Error",
                        "No mic detected on device", MotionToast.TOAST_ERROR);
            return true;
        });

        addAudioItem.setOnClickListener(view -> {
            if (Helper.deviceMicExists(this)) {
                if (isMicrophonePermissionEnabled())
                    checkMicrophonePermission();
                else {
                    InfoSheet permissionInfo = new InfoSheet("Microphone Permission Required", Manifest.permission.RECORD_AUDIO);
                    permissionInfo.show(getSupportFragmentManager(), permissionInfo.getTag());
                }
            } else
                Helper.showMessage(NoteEdit.this, "Mic Error",
                        "No mic detected on device", MotionToast.TOAST_ERROR);
        });

        photosNote.setOnClickListener(v -> {
            if (isShowingPhotos) {
                showPhotos(View.GONE);
                if (!getCurrentNote(context, noteId).isCheckList())
                    addCheckListItem.setVisibility(View.GONE);
                addCheckListItem.setImageDrawable(getDrawable(R.drawable.add_icon));
                if (isChangingTextSize)
                    textSizeLayout.setVisibility(View.VISIBLE);
            } else {
                if (getRealm().where(Photo.class).equalTo("noteId", noteId).findAll().size() == 0) {
                    Helper.showMessage(NoteEdit.this, "Photos Empty",
                            "Add photos to note", MotionToast.TOAST_WARNING);
                }
                photosAllExist();
                showPhotos(View.VISIBLE);
                addCheckListItem.setVisibility(View.VISIBLE);
                addCheckListItem.setImageDrawable(getDrawable(R.drawable.media_selector_icon));
                if (isChangingTextSize)
                    textSizeLayout.setVisibility(View.GONE);
            }
        });

        expandMenu.setOnClickListener(v ->
                openMenuDialog());

        noteColor.setOnClickListener(v -> {
            ColorSheet colorSheet = new ColorSheet(noteId);
            colorSheet.show(getSupportFragmentManager(), colorSheet.getTag());
        });

        pinNoteButton.setOnClickListener(v -> {
            // update the status and pin image
            boolean pin = getCurrentNote(context, noteId).isPin();

            getRealm().beginTransaction();
            getCurrentNote(context, noteId).setPin(!pin);
            getRealm().commitTransaction();
            updateSaveDateEdited();

            if (pin)
                pinNoteIcon.setImageDrawable(getDrawable(R.drawable.pin_icon));
            else
                pinNoteIcon.setImageDrawable(getDrawable(R.drawable.pin_filled_icon));

        });
    }

    private void closeAndDeleteNote() {
        if (getCurrentNote(context, noteId) != null && getCurrentNote(context, noteId).getTitle().isEmpty() && getCurrentNote(context, noteId).getNote().isEmpty()
                && getCurrentNote(context, noteId).getChecklist().isEmpty()) {
            if (!getUser().isEnableEmptyNote() && getPhotos() != null && getPhotos().isEmpty()) {
                RealmHelper.deleteNote(context, getCurrentNote(context, noteId).getNoteId());
                Helper.showMessage(this, "Deleted", "Note has been deleted, change in settings", MotionToast.TOAST_WARNING);
            }
        }
        finish("Closing and maybe deleting note");
        if (!AppData.isDisableAnimation)
            overridePendingTransition(R.anim.stay, R.anim.right_out);
    }

    private void showSearchBar() {
        isSearchingNotes = true;
        closeNote.setVisibility(View.GONE);
        expandMenu.setVisibility(View.GONE);
        noteColor.setVisibility(View.GONE);
        photosNote.setVisibility(View.GONE);
        pinNoteButton.setVisibility(View.GONE);
        search.setVisibility(View.GONE);
        searchClose.setVisibility(View.VISIBLE);
        searchEditText.setVisibility(View.VISIBLE);

        searchLayout.setCardBackgroundColor(UiHelper.getColorFromTheme(this, R.attr.primaryBackgroundColor));
        searchLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        searchLayout.setPadding(100, 100, 8, 100);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(24, 0, 16, 0);
        searchEditText.setLayoutParams(params);
        searchEditText.requestFocusFromTouch();

        if (searchEditText.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
        }

        if (getCurrentNote(context, noteId).isCheckList()) {
            addCheckListItem.setVisibility(View.GONE);
            addAudioItem.setVisibility(View.GONE);
            if (isShowingPhotos)
                photosScrollView.setVisibility(View.GONE);
        }

        showButtons();
    }

    private void hideSearchBar() {
        isSearchingNotes = false;
        closeNote.setVisibility(View.VISIBLE);
        expandMenu.setVisibility(View.VISIBLE);
        noteColor.setVisibility(View.VISIBLE);
        photosNote.setVisibility(View.VISIBLE);
        pinNoteButton.setVisibility(View.VISIBLE);
        search.setVisibility(View.VISIBLE);
        searchEditText.setVisibility(View.GONE);
        searchEditText.setVisibility(View.GONE);
        searchClose.setVisibility(View.GONE);
        searchEditText.clearFocus();

        ViewGroup.MarginLayoutParams vlp = (ViewGroup.MarginLayoutParams) photosNote.getLayoutParams();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, vlp.rightMargin, 0);
        searchLayout.setLayoutParams(params);
        searchLayout.setCardBackgroundColor(UiHelper.getColorFromTheme(this, R.attr.secondaryBackgroundColor));

        InputMethodManager inputManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);

        searchEditText.setText("");
        if (getCurrentNote(context, noteId).isCheckList()) {
            note.setVisibility(View.GONE);
            sortChecklist("");
        } else {
            note.setHtml(getCurrentNote(context, noteId).getNote());
            note.setVisibility(View.VISIBLE);
        }

        hideButtons();
        if (getCurrentNote(context, noteId).isCheckList())
            showChecklistButtons();
        else {
            if (getUser().isEnableEditableNoteButton()) {
                addCheckListItem.setVisibility(View.VISIBLE);
                addCheckListItem.setImageDrawable(getDrawable(
                        isEditLockedMode ? R.drawable.edit_icon : R.drawable.do_not_edit_icon));
            }
            note.focusEditor();
            currentWordIndex = -1;
            wordOccurences = new ArrayList();
        }
    }

    private void showButtons() {
        textSizeLayout.setVisibility(View.VISIBLE);
    }

    private void hideButtons() {
        textSizeLayout.setVisibility(View.GONE);
    }

    private void showChecklistButtons() {
        if (getCurrentNote(context, noteId).isCheckList()) {
            new Handler().postDelayed(() -> {
                addCheckListItem.setVisibility(View.VISIBLE);
            }, 500);
            if (isShowingPhotos) {
                new Handler().postDelayed(() -> {
                    photosScrollView.setVisibility(View.VISIBLE);
                }, 500);
            }
            if (!getUser().isShowAudioButton()) {
                new Handler().postDelayed(() -> {
                    addAudioItem.setVisibility(View.VISIBLE);
                }, 500);
            }
        }
    }

    public void sortChecklist(String word) {
        RealmResults<CheckListItem> results = Helper.sortChecklist(context, noteId, getRealm());
        results = filterChecklistVisibility(results);
        populateChecklist(results, word);
        title.clearFocus();
    }

    /**
     * unchecked [ ] and checked [ ] 3 (this is not an option)
     * unchecked [ ] and checked [x] 2
     * unchecked [x] and checked [ ] 1
     * unchecked [x] and checked [x] 0 (default)
     */
    public RealmResults<CheckListItem> filterChecklistVisibility(RealmResults<CheckListItem> results) {
        int visibility = getCurrentNote(context, noteId).getVisibilityStatus();
        if (visibility == 1) {
            return results.where().equalTo("checked", false).findAll();
        } else if (visibility == 2) {
            return results.where().equalTo("checked", true).findAll();
        }
        return results;
    }

    public boolean isUsingPreviewBackground() {
        return getUser().isUsePreviewColorAsBackground() && getCurrentNote(context, noteId).isUsePreviewAsNoteBackground();
    }

    public void updateOtherColors() {
        if (isUsingPreviewBackground()) {
            int previewBackgroundColor = getCurrentNote(context, noteId).getBackgroundColor();
            note.setBackgroundColor(previewBackgroundColor);
            scrollView.setBackgroundColor(previewBackgroundColor);
            if (getCurrentNote(context, noteId).getLastEditFolderTextColor() != 0) {
                folderText.setTextColor(getCurrentNote(context, noteId).getLastEditFolderTextColor());
                category.setTextColor(getCurrentNote(context, noteId).getLastEditFolderTextColor());
                date.setTextColor(getCurrentNote(context, noteId).getLastEditFolderTextColor());
            }
            UiHelper.setStatusBarColor(this, previewBackgroundColor);
            if (getCurrentNote(context, noteId).getEditorIconTransparency() != 0) {
                double dimValue = getCurrentNote(context, noteId).getEditorIconTransparency();
                dimNote((float) dimValue);
            } else {
                getRealm().beginTransaction();
                getCurrentNote(context, noteId).setEditorIconTransparency(0.75);
                getRealm().commitTransaction();
                dimNote(0.75f);
            }
        } else {
            folderText.setTextColor(UiHelper.getColorFromTheme(this, R.attr.primaryTextColor));
            date.setTextColor(UiHelper.getColorFromTheme(this, R.attr.primaryTextColor));
            category.setTextColor(UiHelper.getColorFromTheme(this, R.attr.primaryTextColor));
            note.setBackgroundColor(UiHelper.getColorFromTheme(this, R.attr.primaryBackgroundColor));
            scrollView.setBackgroundColor(UiHelper.getColorFromTheme(this, R.attr.primaryBackgroundColor));
            UiHelper.setStatusBarColor(this);
            dimNote(1f);
        }
    }

    public void updateColors() {
        noteColor.setCardBackgroundColor(getCurrentNote(context, noteId).getBackgroundColor());
        if (!getUser().isUsePreviewColorAsBackground())
            category.setTextColor(getCurrentNote(context, noteId).getBackgroundColor());
        else
            category.setTextColor(getCurrentNote(context, noteId).getLastEditFolderTextColor());
        title.setTextColor(getCurrentNote(context, noteId).getTitleColor());
        note.setEditorFontColor(RealmHelper.getTextColorBasedOnTheme(context, noteId));
        if (checklistAdapter != null && getCurrentNote(context, noteId).isCheckList())
            checklistAdapter.notifyDataSetChanged();

        if (!Helper.isColorDark(getCurrentNote(context, noteId).getBackgroundColor()))
            palleteIconColor.setColorFilter(context.getResources().getColor(R.color.black));
        else
            palleteIconColor.setColorFilter(context.getResources().getColor(R.color.white));
    }

    private void dimNote(float value) {
        closeNote.setAlpha(value);
        expandMenu.setAlpha(value);
        noteColor.setAlpha(value);
        photosNote.setAlpha(value);
        pinNoteButton.setAlpha(value);
        searchLayout.setAlpha(value);
        editorScroll.setAlpha(value);
    }

    private void findText(String target) {
        if (!getCurrentNote(context, noteId).isCheckList()) {

            String originalString = note.getHtml();
            if (originalString.toLowerCase().contains(target.toLowerCase())) {
                this.target = target;
                findAllTextIndexes(target);
                decreaseTextSize.setAlpha(new Float(1.0));
                increaseTextSize.setAlpha(new Float(1.0));
            } else {
                currentWordIndex = -1;
                decreaseTextSize.setAlpha(new Float(0.5));
                increaseTextSize.setAlpha(new Float(0.5));
            }
        }
    }

    private void findAllTextIndexes(String target) {
        wordOccurences = new ArrayList<String>();
        target = target.toLowerCase();
        String text = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            text = Html.fromHtml(getCurrentNote(context, noteId).getNote().toLowerCase(), Html.FROM_HTML_MODE_COMPACT).toString();
        } else {
            text = Html.fromHtml(getCurrentNote(context, noteId).getNote().toLowerCase()).toString();
        }
        int index = 0;
        while (index >= 0) {
            index = text.indexOf(target, index);
            if (index != -1) {
                int numBreaks = text.substring(0, index).split("\\n").length - 1;
                Log.d("Here", "before -> " + index + ", after -> " + (index - numBreaks) + "  -- num <br> -> " + numBreaks);
                wordOccurences.add(index - numBreaks);
                index++;
            }
        }
        String word = "";
        for (Object found : wordOccurences) {
            word += found + ",";
        }
        //Log.d("Here", " -> " + word);
    }

    private void saveChanges(String text, int size) {
        if (size == 0) {
            text = "";
        }
        getRealm().beginTransaction();
        getCurrentNote(context, noteId).setNote(text);
        getRealm().commitTransaction();
        updateSaveDateEdited();
    }

    private void showCheckListLayout(boolean status) {
        note.setVisibility(View.GONE);
        addCheckListItem.setVisibility(View.GONE);

        if (status) {
            addCheckListItem.setVisibility(View.VISIBLE);
            checkListRecyclerview.setVisibility(View.VISIBLE);
            isListEmpty(checkListItems.size());
        }
    }

    private void changeTextSize(boolean increaseText, boolean isCheckList) {
        int textSizeNumber = RealmHelper.getUserTextSize(context);
        int currentTextSize;
        if (isCheckList) {
            currentTextSize = textSizeNumber;
            if (increaseText && currentTextSize + 3 < 50)
                currentTextSize += 1;
            else if (!increaseText && currentTextSize - 3 > 5)
                currentTextSize -= 1;
        } else {
            currentTextSize = textSizeNumber;
            if (increaseText && currentTextSize + 3 < 50)
                currentTextSize += 1;
            else if (!increaseText && currentTextSize - 3 > 5)
                currentTextSize -= 1;
            note.setEditorFontSize(currentTextSize);
        }

        RealmHelper.setUserTextSize(context, currentTextSize);

        if (isCheckList)
            checklistAdapter.notifyDataSetChanged();
    }

    // adds note
    private void addNote() {
        String[] currentItems = new String[0];
        if (noteFromOtherApp != null && !noteFromOtherApp.isEmpty()) {
            // [V] for ColorNote, idk they changed it to V instead of x
            noteFromOtherApp = noteFromOtherApp.replaceAll("\\[V\\]", "[x]");
            noteFromOtherApp = noteFromOtherApp.replaceAll("○", "[]");
            noteFromOtherApp = noteFromOtherApp.replaceAll("●", "[x]");
            noteFromOtherApp = noteFromOtherApp.replaceAll("\n", "<br>");
            if (noteFromOtherApp.contains("[ ]") || noteFromOtherApp.contains("[x]")) {
                isCheckList = true;
                noteFromOtherApp = noteFromOtherApp.replaceAll("\\[x\\]", "n~~n~~~");
                noteFromOtherApp = noteFromOtherApp.replaceAll("\\[ \\]", "c--c");
                currentItems = noteFromOtherApp.split("n~~n|c--c");
            }
        }

        // saves inputted note to database
        Note newNote = null;
        if (isCheckList) {
            if (titleFromOtherApp != null && !titleFromOtherApp.isEmpty())
                newNote = new Note(titleFromOtherApp, "", getUser().isEnableSublists());
            else
                newNote = new Note(title.getText().toString(), "", getUser().isEnableSublists());
        } else {
            try {
                if ((noteFromOtherApp != null && !noteFromOtherApp.isEmpty()) ||
                        (titleFromOtherApp != null && !titleFromOtherApp.isEmpty()))
                    newNote = new Note(titleFromOtherApp, noteFromOtherApp, getUser().isEnableSublists());
                else
                    newNote = new Note(title.getText().toString(), note.getHtml(), getUser().isEnableSublists());
            } catch (Exception e) {
                if ((noteFromOtherApp != null && !noteFromOtherApp.isEmpty()) ||
                        (titleFromOtherApp != null && !titleFromOtherApp.isEmpty()))
                    newNote = new Note(titleFromOtherApp, noteFromOtherApp, getUser().isEnableSublists());
                else
                    newNote = new Note(title.getText().toString(), "", getUser().isEnableSublists());
            }
        }

        if (getRealm().where(Note.class).equalTo("noteId", newNote.getNoteId()).findAll().size() != 0) {
            // note id for new note already exists, try to add again with a NEW unique note id
            addNote();
        } else {
            newNote.setPin(currentPin);
            newNote.setIsCheckList(isCheckList);
            newNote.setUsePreviewAsNoteBackground(getUser().isUsePreviewColorAsBackground());
            // add a random color to the note
            int[] randomColor = context.getResources().getIntArray(R.array.randomColor);
            int randomInt = (int) (Math.random() * (randomColor.length));
            newNote.setBackgroundColor(randomColor[randomInt]);
            newNote.setTitleColor(UiHelper.getColorFromTheme(context, R.attr.primaryTextColor));
            newNote.setTextColor(UiHelper.getColorFromTheme(context, R.attr.primaryTextColor));
            newNote.setLastEditFolderTextColor(UiHelper.getColorFromTheme(context, R.attr.primaryTextColor));
            // insert data to database
            getRealm().beginTransaction();
            getRealm().insert(newNote);
            getRealm().commitTransaction();
            // update status of note
            noteId = newNote.getNoteId();
            oldTitle = getCurrentNote(context, noteId).getTitle();
            oldNote = getCurrentNote(context, noteId).getNote();
            isNewNote = false;

            if (importedImages != null && importedImages.size() > 0) {
                for (String image : importedImages) {
                    getRealm().beginTransaction();
                    getRealm().insert(new Photo(noteId, image));
                    getRealm().commitTransaction();
                }
            }

            if (currentItems.length > 0) {
                for (String currentItem : currentItems) {
                    boolean isChecked = false;
                    if (currentItem.contains("~~~")) {
                        currentItem = currentItem.replace("~~~", "").trim();
                        isChecked = true;
                    }

                    if (!currentItem.isEmpty())
                        onlyAddChecklist(currentItem, isChecked);
                }
            }
        }
    }

    // adds note
    public CheckListItem addCheckList(String itemText, Place place, String redirectToOtherNote) {
        int initialPosition = getCurrentNote(context, noteId).getChecklist().size();

        if (getCurrentNote(context, noteId).getChecklist().size() != 0) {
            if (RealmHelper.getNoteSorting(context, noteId) == 6)
                initialPosition = getCurrentNote(context, noteId).getChecklist().min("positionInList").intValue() - 1;
            else if (RealmHelper.getNoteSorting(context, noteId) == 5)
                initialPosition = getCurrentNote(context, noteId).getChecklist().max("positionInList").intValue() + 1;
        }

        Random rand = new Random();

        int redirectId = RealmHelper.getNoteIdUsingTitle(context, redirectToOtherNote);
        long lastCheckedDate = Helper.dateToCalender(Helper.getCurrentDate()).getTimeInMillis();
        // insert data to database
        getRealm().beginTransaction();
        CheckListItem currentItem = new CheckListItem(itemText.trim(), false, getCurrentNote(context, noteId).getNoteId(),
                initialPosition, rand.nextInt(100000) + 1, new SimpleDateFormat("E, MMM dd")
                .format(Calendar.getInstance().getTime()), place, redirectId);
        getCurrentNote(context, noteId).getChecklist().add(currentItem);
        if (RealmHelper.getNoteSorting(context, noteId) == 8)
            getRealm().where(CheckListItem.class).equalTo("id", noteId).findAll().setLong("lastCheckedDate", lastCheckedDate);
        getCurrentNote(context, noteId).setChecked(false);
        getRealm().commitTransaction();
        updateSaveDateEdited();
        isListEmpty(getCurrentNote(context, noteId).getChecklist().size());

        sortChecklist("");

        if (title.getText().length() == 0)
            title.requestFocus();
        else
            title.clearFocus();

        if (getCurrentNote(context, noteId).isChecked())
            ((NoteEdit) context).title.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.SUBPIXEL_TEXT_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        else
            ((NoteEdit) context).title.setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);

        checkListRecyclerview.smoothScrollToPosition(initialPosition < 0 ? 0 : initialPosition);
        return currentItem;
    }

    public void addCheckList(int subListId, String path, int duration) {
        CheckListItem newItem = getRealm().where(CheckListItem.class).equalTo("subListId", subListId).findFirst();
        getRealm().beginTransaction();
        newItem.setAudioPath(path);
        newItem.setAudioDuration(duration);
        getRealm().commitTransaction();
    }

    public void onlyAddChecklist(String itemText, boolean isChecked) {
        int initialPosition = -1;

        if (getCurrentNote(context, noteId).getChecklist().size() != 0) {
            if (RealmHelper.getNoteSorting(context, noteId) == 6)
                initialPosition = getCurrentNote(context, noteId).getChecklist().min("positionInList").intValue() - 1;
            else if (RealmHelper.getNoteSorting(context, noteId) == 5)
                initialPosition = getCurrentNote(context, noteId).getChecklist().max("positionInList").intValue() + 1;
        } else
            initialPosition = getCurrentNote(context, noteId).getChecklist().size();

        Random rand = new Random();

        // insert data to database
        getRealm().beginTransaction();
        CheckListItem currentItem = new CheckListItem(itemText.trim(), isChecked, getCurrentNote(context, noteId).getNoteId(),
                initialPosition, rand.nextInt(100000) + 1, new SimpleDateFormat("E, MMM dd")
                .format(Calendar.getInstance().getTime()), new Place("", "", "", 0, 0), 0);
        getCurrentNote(context, noteId).getChecklist().add(currentItem);
        getCurrentNote(context, noteId).setChecked(false);
        getRealm().commitTransaction();
        updateSaveDateEdited();
        isListEmpty(getCurrentNote(context, noteId).getChecklist().size());
    }

    public void addSubCheckList(CheckListItem checkListItem, String itemText) {
        // current Head of sub-list
        int initialPosition = checkListItem.getSubChecklist().size();
        // insert data to database
        getRealm().beginTransaction();
        checkListItem.getSubChecklist().add(new SubCheckListItem(itemText.trim(), false, checkListItem.getSubListId(), initialPosition, new SimpleDateFormat("E, MMM dd").format(Calendar.getInstance().getTime())));
        getCurrentNote(context, noteId).setEnableSublist(true);
        checkListItem.setChecked(false);
        getRealm().commitTransaction();
        updateSaveDateEdited();

        checklistAdapter.notifyDataSetChanged();

        if (title.getText().length() == 0)
            title.requestFocus();
        else
            title.clearFocus();

        try {
            if (!getCurrentNote(context, noteId).isEnableSublist() && checkListItem.getSubChecklist().size() > 0) {
                getRealm().beginTransaction();
                getCurrentNote(context, noteId).setEnableSublist(true);
                getRealm().commitTransaction();
            }
        } catch (Exception e) {
        }
    }

    // makes sure all the photos exist and have not been deleted from the device
    // if it has, it deletes it from the note and shows a message for the user
    private void photosAllExist() {
        RealmResults<Photo> allNotePhotos = getPhotos();
        int before = countPicsNotFound;

        if (getCurrentNote(context, noteId) == null || getCurrentNote(context, noteId).getPhotos() == null)
            return;

        for (int i = 0; i < allNotePhotos.size(); i++) {
            File file = new File(allNotePhotos.get(i).getPhotoLocation());
            if (!file.exists()) {
                countPicsNotFound++;
                getRealm().beginTransaction();
                allNotePhotos.get(i).deleteFromRealm();
                getRealm().commitTransaction();
            }
        }

        if (before == countPicsNotFound && before != 0) {
            Helper.showMessage(this, countPicsNotFound + " not found", "Picture(s) corrupted " +
                    "or has been deleted from photos", MotionToast.TOAST_ERROR);
            countPicsNotFound = 0;
        } else if (countPicsNotFound != getCurrentNote(context, noteId).getPhotos().size() && countPicsNotFound != 0)
            photosAllExist();
    }

    private void openMenuDialog() {
        String archivedStatus = "Archive";
        String lockStatus = "Lock";
        if (getCurrentNote(context, noteId).isArchived())
            archivedStatus += "d";
        if (getCurrentNote(context, noteId).getPinNumber() != 0)
            lockStatus += "ed";

        IconPowerMenuItem restoreFromTrash = new IconPowerMenuItem(getDrawable(R.drawable.restore_icon), "Undo Delete");
        IconPowerMenuItem reminderItem = new IconPowerMenuItem(getDrawable(R.drawable.reminder_icon), "Reminder");
        noteMenu = new CustomPowerMenu.Builder<>(context, new IconMenuAdapter(false))
                .addItem(new IconPowerMenuItem(getDrawable(R.drawable.archive_icon), archivedStatus))
                .addItem(reminderItem)
                .addItem(new IconPowerMenuItem(getDrawable(R.drawable.format_size_icon), "Text Size"))
                .addItem(new IconPowerMenuItem(getDrawable(R.drawable.export_icon), "Export"))
                .addItem(new IconPowerMenuItem(getDrawable(R.drawable.lock_icon), lockStatus))
                .addItem(restoreFromTrash)
                .addItem(new IconPowerMenuItem(getDrawable(R.drawable.delete_icon), "Delete"))
                .addItem(new IconPowerMenuItem(getDrawable(R.drawable.info_icon), "Info"))
                .setBackgroundColor(UiHelper.getColorFromTheme(this, R.attr.secondaryBackgroundColor))
                .setOnMenuItemClickListener(onIconMenuItemClickListener)
                .setAnimation(MenuAnimation.SHOW_UP_CENTER)
                .setMenuRadius(15f)
                .setMenuShadow(10f)
                .build();

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            noteMenu.removeItem(reminderItem);

        if (getCurrentNote(context, noteId).isCheckList()) {
            noteMenu.addItem(2, new IconPowerMenuItem(getDrawable(R.drawable.check_icon), "Select All"));
            noteMenu.addItem(3, new IconPowerMenuItem(getDrawable(R.drawable.box_icon), "Deselect All"));
            noteMenu.addItem(4, new IconPowerMenuItem(getDrawable(R.drawable.delete_all_icon), "Delete Items"));
            noteMenu.addItem(5, new IconPowerMenuItem(getDrawable(R.drawable.hide_icon), "Hide Items"));
            noteMenu.addItem(7, new IconPowerMenuItem(getDrawable(R.drawable.filter_icon), "Sort"));
            if (getUser().isEnableSublists()) {
                String sublistStatus = "";
                if (getCurrentNote(context, noteId).isEnableSublist()) {
                    sublistStatus = sublistStatus + "Sub-List";
                    noteMenu.addItem(6, new IconPowerMenuItem(getDrawable(R.drawable.visible_false_icon), sublistStatus));
                } else {
                    sublistStatus = sublistStatus + "Sub-List";
                    noteMenu.addItem(6, new IconPowerMenuItem(getDrawable(R.drawable.visible_icon), sublistStatus));
                }
            }
        } else {
            if (getUser().isHideRichTextEditor()) {
                String formatStatus = hideRichTextStatus ? "Show Formatter" : "Hide Formatter";
                IconPowerMenuItem formatIcon = new IconPowerMenuItem(getDrawable(R.drawable.text_format_icon), formatStatus);
                noteMenu.addItem(3, formatIcon);
            }
        }

        if (!getCurrentNote(context, noteId).isTrash())
            noteMenu.removeItem(restoreFromTrash);

        noteMenu.showAsDropDown(expandMenu);
    }

    private final OnMenuItemClickListener<IconPowerMenuItem> onIconMenuItemClickListener = new OnMenuItemClickListener<IconPowerMenuItem>() {
        @Override
        public void onItemClick(int position, IconPowerMenuItem item) {
            if (item.getTitle().contains("Archive")) {
                updateArchivedStatus();
            } else if (item.getTitle().equals("Export")) {
                ExportNotesSheet exportNotesSheet = new ExportNotesSheet(noteId,
                        true, getCurrentNote(context, noteId).isCheckList() ?
                        Helper.getNoteString(context, noteId, getRealm()) : getCurrentNote(context, noteId).getNote());
                exportNotesSheet.show(getSupportFragmentManager(), exportNotesSheet.getTag());
            } else if (item.getTitle().equals("Reminder")) {
                if (!getCurrentNote(context, noteId).getReminderDateTime().isEmpty()) {
                    Helper.showMessage(NoteEdit.this, "Alarm Exists",
                            "Delete Reminder to add another one", MotionToast.TOAST_ERROR);
                } else {
                    if (isNotificationPermissionEnabled())
                        checkNotificationPermission();
                    else {
                        InfoSheet permissionInfo = new InfoSheet("Notification Permission Required", Manifest.permission.POST_NOTIFICATIONS);
                        permissionInfo.show(getSupportFragmentManager(), permissionInfo.getTag());
                    }
                }
            } else if (item.getTitle().equals("Delete")) {
                dismissDialog = true;
                openDialog();
            } else if (item.getTitle().equals("Sort")) {
                FilterChecklistSheet filter = new FilterChecklistSheet(noteId);
                filter.show(getSupportFragmentManager(), filter.getTag());
            } else if (item.getTitle().equals("Lock")) {
                LockSheet lockSheet = new LockSheet(AppConstants.LockType.LOCK_NOTE, null);
                lockSheet.show(getSupportFragmentManager(), lockSheet.getTag());
            } else if (item.getTitle().equals("Locked")) {
                unLockNote();
            } else if (item.getTitle().contains("Formatter")) {
                hideRichTextStatus = !hideRichTextStatus;

                if (hideRichTextStatus)
                    formatMenu.setVisibility(View.GONE);
                else
                    formatMenu.setVisibility(View.VISIBLE);
            } else if (item.getTitle().equals("Select All")) {
                if (getCurrentNote(context, noteId).getChecklist().sort("positionInList").size() != 0) {
                    RealmHelper.selectAllChecklists(getCurrentNote(context, noteId), context, true);
                    checkNote(true);
                    checklistAdapter.notifyDataSetChanged();
                    Helper.showMessage(NoteEdit.this, "Success", "All items " +
                            "have been selected", MotionToast.TOAST_SUCCESS);
                }
            } else if (item.getTitle().equals("Deselect All")) {
                if (getCurrentNote(context, noteId).getChecklist().sort("positionInList").size() != 0) {
                    RealmHelper.selectAllChecklists(getCurrentNote(context, noteId), context, false);
                    checkNote(false);
                    checklistAdapter.notifyDataSetChanged();
                    Helper.showMessage(NoteEdit.this, "Success", "All items " +
                            "have been unselected", MotionToast.TOAST_SUCCESS);
                }
            } else if (item.getTitle().equals("Delete Items")) {
                showChecklistItemsDeleteBottomSheet();
            } else if (item.getTitle().equals("Hide Items")) {
                showChecklistItemsHideBottomSheet();
            } else if (item.getTitle().contains("Text")) {
                isChangingTextSize = true;
                textSizeLayout.setVisibility(View.VISIBLE);
                addAudioItem.setVisibility(View.GONE);
                addCheckListItem.setVisibility(View.GONE);
                decreaseTextSize.setAlpha(new Float(1.0));
                increaseTextSize.setAlpha(new Float(1.0));
            } else if (item.getTitle().equals("Info")) {
                NoteInfoSheet noteInfoSheet = new NoteInfoSheet(getCurrentNote(context, noteId).getNoteId(), false);
                noteInfoSheet.show(getSupportFragmentManager(), noteInfoSheet.getTag());
            } else if (item.getTitle().contains("Sub-List"))
                updateSublistEnabledStatus();
            else if (item.getTitle().equals("Undo Delete")) {
                getRealm().beginTransaction();
                getCurrentNote(context, noteId).setTrash(false);
                getRealm().commitTransaction();
                Helper.showMessage(NoteEdit.this, "Note Restored",
                        "Note restored from trash", MotionToast.TOAST_SUCCESS);
            }

            noteMenu.dismiss();
        }
    };

    public boolean isNotificationPermissionEnabled() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true;
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }

    public void checkNotificationPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 2);
        } else
            showDatePickerDialog();
    }

    public boolean isMicrophonePermissionEnabled() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    public void checkMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 3);
            }
        } else
            openRecordingSheet();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                showDatePickerDialog();
            else
                Helper.showMessage(this, "Reminder Permission", "Accept permission " +
                        "to send yourself a reminder", MotionToast.TOAST_ERROR);
        } else if (requestCode == 3) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                openRecordingSheet();
            else
                Helper.showMessage(this, "Microphone Permission", "Accept permission " +
                        "to record voice notes", MotionToast.TOAST_ERROR);
        }
    }

    public void openRecordingSheet() {
        RecordAudioSheet recordAudioSheet = new RecordAudioSheet();
        recordAudioSheet.show(getSupportFragmentManager(), recordAudioSheet.getTag());
    }

    public void deleteChecklist(String selectedToDelete) {
        switch (selectedToDelete) {
            case "all":
                Log.d("Here", "Selected Button -> " + selectedToDelete);
                RealmHelper.deleteChecklist(getCurrentNote(context, noteId), context);
                checkNote(false);
                Helper.showMessage(NoteEdit.this, "Success", "All items deleted", MotionToast.TOAST_SUCCESS);
                break;
            case "checked":
                Log.d("Here", "Selected Button -> " + selectedToDelete);
                RealmHelper.deleteChecklistItems(getCurrentNote(context, noteId), context, true, false);
                Helper.showMessage(NoteEdit.this, "Success", "Checked items deleted", MotionToast.TOAST_SUCCESS);
                break;
            case "un-checked":
                Log.d("Here", "Selected Button -> " + selectedToDelete);
                RealmHelper.deleteChecklistItems(getCurrentNote(context, noteId), context, false, false);
                Helper.showMessage(NoteEdit.this, "Success", "Un-Checked items deleted", MotionToast.TOAST_SUCCESS);
                break;
            default:
                return;
        }
        checklistAdapter.notifyDataSetChanged();
        isListEmpty(getCurrentNote(context, noteId).getChecklist().size(), true);
    }

    public void enableSorting() {
        callback = new ItemTouchHelper.Callback() {

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
            }

            @Override
            public boolean isLongPressDragEnabled() {
                checkListItemsUnmanaged = getRealm().copyFromRealm(checkListItems);
                return super.isLongPressDragEnabled();
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAbsoluteAdapterPosition();
                int toPosition = target.getAbsoluteAdapterPosition();
                // Determine direction and calculate number of items to shift
                int shiftDirection = Integer.compare(toPosition, fromPosition);

                // Handle shifting to lower position (3 to 0):
                if (shiftDirection < 0) {
                    // Shift items to the right starting from the new position
                    for (int i = fromPosition; i > toPosition; i--) {
                        swap(i, i - 1);
                    }
                }
                // Handle shifting to higher position (0 to 3):
                else {
                    // Shift items to the left starting from the original position
                    for (int i = fromPosition; i < toPosition; i++) {
                        swap(i, i + 1);
                    }
                }

                checklistAdapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
//                    StringBuilder builder = new StringBuilder();
//                    builder.append("[");
//                    for (CheckListItem checkListItem : checkListItemsUnmanaged) {
//                        builder
//                                .append("(" + checkListItem.getId() + ") ")
//                                .append(checkListItem.getText())
//                                .append(" @ ")
//                                .append(checkListItem.getPositionInList())
//                                .append(", ");
//                    }
//                    builder.append("]");
//                    Log.d("Here", builder.toString());
                RealmHelper.updateChecklistOrdering(context, checkListItemsUnmanaged, noteId);
                new Handler().post(() -> {
                    while (true) {
                        try {
                            checklistAdapter.notifyDataSetChanged();
                            Log.d("Here", "Updated Checklist");
                            break;
                        } catch (Exception e) {
                            try {
                                Log.d("Here", "Sleeping for 1/10th of a second...");
                                Thread.sleep(100);
                            } catch (InterruptedException ex) {
                                Log.d("Here", "Error even sleeping...");
                                break;
                            }
                        }
                    }
                });
                Log.d("Here", "Continuing");
            }
        };

        helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(checkListRecyclerview);
    }

    private void disableSorting() {
        try {
            helper.attachToRecyclerView(null);
        } catch (Exception e) {
        }
    }

    private void checkNote(boolean status) {
        getRealm().beginTransaction();
        getCurrentNote(context, noteId).setChecked(status);
        getRealm().commitTransaction();
        if (getCurrentNote(context, noteId).isChecked())
            title.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.SUBPIXEL_TEXT_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        else
            title.setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        updateSaveDateEdited();
    }

    public void lockNote(int pin, String securityWord, boolean fingerprint) {
        RealmHelper.lockNote(context, noteId, pin, securityWord, fingerprint);
        updateSaveDateEdited();
        Helper.showMessage(this, "Note Locked", "Note has been " +
                "locked", MotionToast.TOAST_SUCCESS);

        if (getCurrentNote(context, noteId).getReminderDateTime().length() > 1) {
            InfoSheet info = new InfoSheet(-1);
            info.show(getSupportFragmentManager(), info.getTag());
        }
    }

    public void unLockNote() {
        getRealm().beginTransaction();
        getCurrentNote(context, noteId).setPinNumber(0);
        getCurrentNote(context, noteId).setSecurityWord("");
        getCurrentNote(context, noteId).setFingerprint(false);
        getRealm().commitTransaction();
        updateSaveDateEdited();
        Helper.showMessage(this, "Note un-Lock", "Note has been " +
                "un-locked", MotionToast.TOAST_SUCCESS);
    }

    private void updateReminderLayout(int visibility, int color) {
        remindNote.setVisibility(visibility);
        remindNoteDate.setVisibility(visibility);
        if (!getCurrentNote(context, noteId).getReminderDateTime().isEmpty()) {
            int formatDate24Hours = Integer.parseInt(getCurrentNote(context, noteId).getReminderDateTime().split(" ")[1].split(":")[0]);
            int formatDate12Hours = formatDate24Hours % 12;
            String formatted = getCurrentNote(context, noteId).getReminderDateTime().split(" ")[0] + " " + formatDate12Hours +
                    ":" + getCurrentNote(context, noteId).getReminderDateTime().split(" ")[1].split(":")[1] +
                    ":" + getCurrentNote(context, noteId).getReminderDateTime().split(" ")[1].split(":")[2] + " " + ((formatDate24Hours > 12) ? "PM" : "AM");
            remindNoteDate.setText(formatted);
            if (color != 0) remindNoteDate.setTextColor(color);
        } else
            remindNoteDate.setVisibility(View.GONE);
    }

    private void showPhotos(int visibility) {
        if (visibility == View.VISIBLE) {
            isShowingPhotos = true;
            photosNote.setCardBackgroundColor(UiHelper.getColorFromTheme(this, R.attr.primaryButtonColor));
        } else {
            isShowingPhotos = false;
            photosNote.setCardBackgroundColor(UiHelper.getColorFromTheme(this, R.attr.secondaryBackgroundColor));
        }

        photosScrollView.setVisibility(visibility);
        photosScrollView.requestFocus();
    }

    private void populatePhotos() {
        photosScrollView.setVisibility(View.GONE);
        scrollAdapter = new photos_recyclerview(getPhotos(), NoteEdit.this, context, true);
        photosScrollView.setAdapter(scrollAdapter);
    }

    private void populateChecklist(RealmResults<CheckListItem> currentList, String word) {
        int span = 1;
        if (Helper.isTablet(context)) span = 2;
        checkListRecyclerview.setLayoutManager(new GridLayoutManager(context, span));
        checkListRecyclerview.setHasFixedSize(true);
        if (Helper.isDragDropEnabled(context, noteId))
            enableSorting();
        else {
            disableSorting();
        }
        checkListRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (title.hasFocus()) title.clearFocus();
            }
        });
        checklistAdapter = new checklist_recyclerview(currentList, noteId, this, word);
        checkListRecyclerview.setAdapter(checklistAdapter);
    }

    private void timeDialog() {
        Calendar now = Calendar.getInstance();
        TimePickerDialog timer = TimePickerDialog.newInstance(
                this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(context)
        );
        timer.setThemeDark(RealmHelper.getUser(context, "in edit").getScreenMode() != User.Mode.Light);
        timer.setAccentColor(UiHelper.getColorFromTheme(this, R.attr.quaternaryBackgroundColor));
        timer.setOkColor(UiHelper.getColorFromTheme(this, R.attr.secondaryTextColor));
        timer.setCancelColor(UiHelper.getColorFromTheme(this, R.attr.primaryTextColor));
        timer.show(getSupportFragmentManager(), "TimePickerDialog");
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        // Get Current Time
        dateSelected.set(Calendar.HOUR_OF_DAY, hourOfDay);
        dateSelected.set(Calendar.MINUTE, minute);
        dateSelected.set(Calendar.SECOND, 0);
        currentDateTimeSelected += hourOfDay + ":" + ((minute < 10) ? ("0" + minute) : minute) + ":00";
        startAlarm(dateSelected);
    }

    public void showDatePickerDialog() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Calendar now = Calendar.getInstance();
            DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                    this,
                    now.get(Calendar.YEAR), // Initial year selection
                    now.get(Calendar.MONTH), // Initial month selection
                    now.get(Calendar.DAY_OF_MONTH) // Inital day selection
            );
            datePickerDialog.setThemeDark(RealmHelper.getUser(context, "in edit").getScreenMode() != User.Mode.Light);
            datePickerDialog.setAccentColor(UiHelper.getColorFromTheme(this, R.attr.quaternaryBackgroundColor));
            datePickerDialog.setOkColor(UiHelper.getColorFromTheme(this, R.attr.secondaryTextColor));
            datePickerDialog.setCancelColor(UiHelper.getColorFromTheme(this, R.attr.primaryTextColor));
            datePickerDialog.show(getSupportFragmentManager(), "DatePickerDialog");
        } else
            Helper.showMessage(this, "Reminder Issue", "Reminders do not work on Android 7 devices", MotionToast.TOAST_ERROR);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int month, int day) {
        dateSelected = Calendar.getInstance();
        dateSelected.set(Calendar.YEAR, year);
        dateSelected.set(Calendar.MONTH, ++month);
        dateSelected.set(Calendar.DAY_OF_MONTH, day);
        currentDateTimeSelected = month + "-" + day + "-" + year + " ";
        dateSelected.set(Calendar.MONTH, --month);
        timeDialog();
    }

    private void startAlarm(Calendar c) {
        if (c.after(Calendar.getInstance())) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, AlertReceiver.class);
            intent.putExtra("id", getCurrentNote(context, noteId).getNoteId());
            intent.putExtra("title", getCurrentNote(context, noteId).getTitle().replace("\n", " "));
            intent.putExtra("pin", getCurrentNote(context, noteId).getPinNumber());
            intent.putExtra("securityWord", getCurrentNote(context, noteId).getSecurityWord());
            intent.putExtra("fingerprint", getCurrentNote(context, noteId).isFingerprint());
            updateReminderDate(currentDateTimeSelected);
            updateReminderLayout(View.VISIBLE, UiHelper.getColorFromTheme(this, R.attr.secondaryTextColor));
            Helper.showMessage(this, "Reminder set", "Will Remind you " +
                    "in " + Helper.getTimeDifference(c, true), MotionToast.TOAST_SUCCESS);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, noteId, intent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Helper.showMessage(this, "Alarm Not Set", "Enable " +
                            "Alarms & try again", MotionToast.TOAST_ERROR);
                    startAlarmPermission.launch(new Intent(
                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                            Uri.parse("package:" + getPackageName())
                    ));
                    return;
                }
            }

            AlarmManager.AlarmClockInfo clock = new AlarmManager.AlarmClockInfo(c.getTimeInMillis(), pendingIntent);
            alarmManager.setAlarmClock(clock, pendingIntent);
        } else
            showMessage("Reminder not set", "Reminder cannot be in the past", true);
    }

    private void updateReminderDate(String date) {
        getRealm().beginTransaction();
        getCurrentNote(context, noteId).setReminderDateTime(date);
        getRealm().commitTransaction();
        updateSaveDateEdited();
    }

    private void cancelAlarm(int noteId) {
        updateReminderDate("");
        Helper.cancelNotification(context, noteId);
    }

    private void updateArchivedStatus() {
        boolean isNoteArchived = getCurrentNote(context, noteId).isArchived();

        getRealm().beginTransaction();
        getCurrentNote(context, noteId).setArchived(!isNoteArchived);
        getRealm().commitTransaction();
        updateSaveDateEdited();

        if (isNoteArchived) {
            Helper.showMessage(this, "Archived Status", "Note has been " +
                    "un-archived", MotionToast.TOAST_SUCCESS);
        } else {
            Helper.showMessage(this, "Archived Status", "Note has been " +
                    "archived and put in the archived folder", MotionToast.TOAST_SUCCESS);
        }
    }

    private void updateSublistEnabledStatus() {
        boolean isSublistEnabled = getCurrentNote(context, noteId).isEnableSublist();

        getRealm().beginTransaction();
        getCurrentNote(context, noteId).setEnableSublist(!isSublistEnabled);
        getRealm().commitTransaction();
        Helper.showMessage(this, "Sublist Status", "It has been " +
                (isSublistEnabled ? "disabled" : "enabled"), MotionToast.TOAST_SUCCESS);
        updateSaveDateEdited();
        checklistAdapter.notifyDataSetChanged();
    }

    // determines if there were changes to the note
    private boolean noteChanged() {
        boolean isTitleChanged = !title.getText().toString().equals(oldTitle);
        boolean isNoteChanged = !note.getHtml().equals(oldNote);
        return isTitleChanged || isNoteChanged;
    }

    public void openMediaSelected(int selection) {
        // open gallery
        if (selection == 0) {
            if (MediaHelper.hasMediaPermissions(context)) {
                MediaHelper.openMedia(pickMultipleMedia);
            } else {
                mediaPermissionLauncher.launch(MediaHelper.getMediaPermissions());
            }
        }
        // open camera
        else if (selection == 1) {
            if (MediaHelper.hasPermission(context, Manifest.permission.CAMERA)) {
                tempCameraPhotoPath = MediaHelper.openCamera(this, context, takePictureLauncher);
            } else {
                cameraPermissionLauncher.launch(MediaHelper.getCameraPermission());
            }
        }
    }

    private void openNewItemDialog() {
        ChecklistItemSheet checklistItemSheet = new ChecklistItemSheet();
        checklistItemSheet.show(getSupportFragmentManager(), checklistItemSheet.getTag());
    }

    private void openDialog() {
        InfoSheet info = new InfoSheet(getCurrentNote(context, noteId).isTrash() ? -3 : 3);
        info.show(getSupportFragmentManager(), info.getTag());
    }

    private String getCategoryName() {
        if (getCurrentNote(context, noteId).getCategory().equals("none") && getCurrentNote(context, noteId).isTrash())
            return "Trash";
        else if (getCurrentNote(context, noteId).isTrash())
            return getCurrentNote(context, noteId).getCategory() + " [Now in Trash]";
        else
            return getCurrentNote(context, noteId).getCategory();
    }

    public void deleteNote(boolean deleteNote) {
        if (handler != null)
            handler.removeCallbacksAndMessages(null);

        if (getCurrentNote(context, noteId).isTrash() || deleteNote) {
            String noteTitle = getCurrentNote(context, noteId).getTitle();
            RealmHelper.deleteNote(context, getCurrentNote(context, noteId).getNoteId());
            Helper.showMessage(NoteEdit.this, "Deleted", noteTitle, MotionToast.TOAST_SUCCESS);
        } else {
            getRealm().beginTransaction();
            getCurrentNote(context, noteId).setTrash(true);
            getRealm().commitTransaction();
            Helper.showMessage(NoteEdit.this, "Sent to trash", getCurrentNote(context, noteId).getTitle(), MotionToast.TOAST_SUCCESS);
        }
        finish("Deleting note");
    }

    public void finish(String message) {
        RealmSingleton.setCloseRealm(false);
        Log.d("Here", "keep realm open in NoteEdit " + message);
        finish();
    }

    public void isListEmpty(int size) {
        Helper.isListEmpty(context, size, empty_Layout, empty_title, subtitle, subSubTitle,
                false, true, false, empty_Layout.getRootView().findViewById(R.id.empty_view),
                empty_Layout.findViewById(R.id.empty_view_no_animation));
    }

    public void isListEmpty(int size, boolean isChecklistAdded) {
        Helper.isListEmpty(context, size, empty_Layout, empty_title, subtitle, subSubTitle,
                false, true, isChecklistAdded, empty_Layout.getRootView().findViewById(R.id.empty_view),
                empty_Layout.findViewById(R.id.empty_view_no_animation));
    }

    public void updateDateEdited() {
        if (isNewNoteCopy) {
            date.setVisibility(View.GONE);
            return;
        }

        if (getCurrentNote(context, noteId).isCheckList())
            isListEmpty(getCurrentNote(context, noteId).getChecklist().size());
        handler = new Handler();

        date.setVisibility(View.VISIBLE);

        if (!RealmHelper.getUser(context, "in space").isDisableLastEditInfo()) {
            handler.postDelayed(new Runnable() {
                public void run() {
                    if (!getRealm().isClosed()) {
                        try {
                            if (Helper.getTimeDifference(Helper.dateToCalender(getCurrentNote(context, noteId).getDateEdited().replace("\n", " ")), false).length() > 0) {
                                if (getUser().isTwentyFourHourFormat()) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        date.setText(Html.fromHtml("Last Edit: " + Helper.convertToTwentyFourHour(getCurrentNote(context, noteId).getDateEdited()).replace("\n", " ") +
                                                "<br>" + Helper.getTimeDifference(Helper.dateToCalender(getCurrentNote(context, noteId).getDateEdited().replace("\n", " ")), false) + " ago", Html.FROM_HTML_MODE_COMPACT));
                                    } else {
                                        date.setText(Html.fromHtml("Last Edit: " + Helper.convertToTwentyFourHour(getCurrentNote(context, noteId).getDateEdited()).replace("\n", " ") +
                                                "<br>" + Helper.getTimeDifference(Helper.dateToCalender(getCurrentNote(context, noteId).getDateEdited().replace("\n", " ")), false) + " ago"));
                                    }
                                } else {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        date.setText(Html.fromHtml("Last Edit: " + getCurrentNote(context, noteId).getDateEdited().replace("\n", " ") +
                                                "<br>" + Helper.getTimeDifference(Helper.dateToCalender(getCurrentNote(context, noteId).getDateEdited().replace("\n", " ")), false) + " ago", Html.FROM_HTML_MODE_COMPACT));
                                    } else {
                                        date.setText(Html.fromHtml("Last Edit: " + getCurrentNote(context, noteId).getDateEdited().replace("\n", " ") +
                                                "<br>" + Helper.getTimeDifference(Helper.dateToCalender(getCurrentNote(context, noteId).getDateEdited().replace("\n", " ")), false) + " ago"));
                                    }
                                }
                            } else {
                                date.setText("Last Edit: " + getCurrentNote(context, noteId).getDateEdited().replace("\n", " ") + "\n");
                            }
                            handler.postDelayed(this, 1000);
                        } catch (Exception e) {
                        }
                    }
                }
            }, 0);
        } else {
            LinearLayout categoryLayout = findViewById(R.id.category_layout);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) categoryLayout.getLayoutParams();
            params.topMargin = -10;
            categoryLayout.setLayoutParams(params);
            date.setVisibility(View.GONE);
        }

    }

    private Realm getRealm() {
        return RealmSingleton.get(NoteEdit.this);
    }


    public RealmResults<Photo> getPhotos() {
        return getRealm().where(Photo.class).equalTo("noteId", noteId).findAll();
    }

    public void updateSaveDateEdited() {
        getRealm().beginTransaction();
        getCurrentNote(context, noteId).setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
        getCurrentNote(context, noteId).setDateEditedMilli(Helper.dateToCalender(getCurrentNote(context, noteId).getDateEdited()).getTimeInMillis());
        getRealm().commitTransaction();
        updateDateEdited();
    }

    private void showMessage(String title, String message, boolean error) {
        if (error) {
            Helper.showMessage(NoteEdit.this, title,
                    message, MotionToast.TOAST_ERROR);
        } else {
            Helper.showMessage(NoteEdit.this, title,
                    message, MotionToast.TOAST_SUCCESS);
        }
    }

    private void isToolbarItemSelected(View view) {
//        // TODO - get this to function and users knows which item is being applied
//        try {
//            int selected = UiHelper.getColorFromTheme(context, R.attr.tertiaryBackgroundColor);
//            ColorDrawable background = (ColorDrawable) view.getBackground();
//            int backgroundColor = background.getColor();
//            if (backgroundColor == selected) {
//                view.setBackgroundColor(UiHelper.getColorFromTheme(context, R.attr.secondaryBackgroundColor));
//            } else {
//                view.setBackgroundColor(selected);
//            }
//        } catch (Exception ignored) {
//        }
    }

    private void initializeEditor() {
        findViewById(R.id.action_undo).setOnClickListener(v -> {
            note.undo();
        });

        findViewById(R.id.action_redo).setOnClickListener(v -> {
            note.redo();
        });

        findViewById(R.id.action_bold).setOnClickListener(v -> {
            updateSaveDateEdited();
            note.setBold();
            isToolbarItemSelected(v);
        });

        findViewById(R.id.action_italic).setOnClickListener(v -> {
            updateSaveDateEdited();
            note.setItalic();
            isToolbarItemSelected(v);
        });

        findViewById(R.id.action_strikethrough).setOnClickListener(v -> {
            updateSaveDateEdited();
            note.setStrikeThrough();
            isToolbarItemSelected(v);
        });

        findViewById(R.id.action_underline).setOnClickListener(v -> {
            updateSaveDateEdited();
            note.setUnderline();
            isToolbarItemSelected(v);
        });

        findViewById(R.id.action_highlight).setOnClickListener(v -> {
            updateSaveDateEdited();
            note.setTextColor(getColor(R.color.black));
            note.setTextBackgroundColor(getColor(R.color.highlight_yellow));
        });

        findViewById(R.id.action_insert_image).setOnClickListener(v -> {
            MediaTypeSheet mediaTypeSheet = new MediaTypeSheet();
            mediaTypeSheet.show(getSupportFragmentManager(), mediaTypeSheet.getTag());
        });

        findViewById(R.id.action_youtube).setOnClickListener(v -> {
            InsertYoutubeVideoSheet insertYoutubeVideoSheet = new InsertYoutubeVideoSheet();
            insertYoutubeVideoSheet.show(getSupportFragmentManager(), insertYoutubeVideoSheet.getTag());
        });

        findViewById(R.id.action_indent).setOnClickListener(v -> {
            updateSaveDateEdited();
            note.setIndent();
        });

        findViewById(R.id.action_outdent).setOnClickListener(v -> {
            updateSaveDateEdited();
            note.setOutdent();
        });

        findViewById(R.id.action_align_left).setOnClickListener(v -> {
            updateSaveDateEdited();
            note.setAlignLeft();
        });

        findViewById(R.id.action_align_center).setOnClickListener(v -> {
            updateSaveDateEdited();
            note.setAlignCenter();
        });

        findViewById(R.id.action_align_right).setOnClickListener(v -> {
            updateSaveDateEdited();
            note.setAlignRight();
        });

        findViewById(R.id.text_direction).setOnClickListener(v -> {
            TextView text = (TextView) findViewById(R.id.text_direction);
            if (getCurrentNote(context, noteId).getNoteTextDirection() == null || getCurrentNote(context, noteId).getNoteTextDirection().isEmpty()) {
                getRealm().beginTransaction();
                getCurrentNote(context, noteId).setNoteTextDirection("ltr");
                getRealm().commitTransaction();
                Log.d("Here", "I am null, setting to ltr");
            } else if (getCurrentNote(context, noteId).getNoteTextDirection().equals("rtl")) {
                getRealm().beginTransaction();
                getCurrentNote(context, noteId).setNoteTextDirection("ltr");
                getRealm().commitTransaction();
                text.setText("RTL");
                note.setDefaultDirection();
                Log.d("Here", "I am being changed to ltr");
            } else if (getCurrentNote(context, noteId).getNoteTextDirection().equals("ltr")) {
                getRealm().beginTransaction();
                getCurrentNote(context, noteId).setNoteTextDirection("rtl");
                getRealm().commitTransaction();
                text.setText("LTR");
                note.setRtlDirection();
                Log.d("Here", "I am being changed to trl");
            }
        });

        findViewById(R.id.action_insert_bullets).setOnClickListener(v -> {
            updateSaveDateEdited();
            note.setBullets();
        });

        findViewById(R.id.action_insert_numbers).setOnClickListener(v -> {
            updateSaveDateEdited();
            note.setNumbers();
        });

        findViewById(R.id.action_txt_color).setOnClickListener(v -> {
            // opens dialog to choose a color
            updateSaveDateEdited();
            if (isChanged)
                note.setTextColor(UiHelper.getColorFromTheme(this, R.attr.primaryTextColor));
            else {
                colorPickerView = ColorPickerDialogBuilder
                        .with(context, R.style.ColorPickerDialogTheme)
                        .setTitle("Select Text Color")
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(10)
                        .setPositiveButton("SELECT", (dialog, selectedColor, allColors) -> {
                            note.setTextColor(selectedColor);
                            note.focusEditor();
                        })
                        .setNegativeButton("CLOSE", (dialog, which) ->
                                dialog.dismiss())
                        .build();
                colorPickerView.show();
            }
            isChanged = !isChanged;
        });

        findViewById(R.id.action_format_clear).setOnClickListener(v -> {
            note.removeFormat();
//            InfoSheet info = new InfoSheet(9);
//            info.show(getSupportFragmentManager(), info.getTag());
        });

        findViewById(R.id.action_text_size).setOnClickListener(v -> {
            isChangingTextSize = true;
            textSizeLayout.setVisibility(View.VISIBLE);
            addCheckListItem.setVisibility(View.GONE);
        });

    }

    public void removeFormatting() {
//        updateSaveDateEdited();
//        String removedFormat = "";
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            removedFormat = Html.fromHtml(getCurrentNote(context, noteId).getNote().replaceAll("<br>", "\n"), Html.FROM_HTML_MODE_COMPACT).toString();
//        } else {
//            removedFormat = Html.fromHtml(getCurrentNote(context, noteId).getNote().replaceAll("<br>", "\n")).toString();
//        }
//        removedFormat = removedFormat.replaceAll("\n", "<br>");
//        getRealm().beginTransaction();
//        getCurrentNote(context, noteId).setNote(removedFormat);
//        getRealm().commitTransaction();
//        note.setHtml(removedFormat);
//        note.focusEditor();
//        Helper.showMessage(this, "Removed", "Formatting has been removed",
//                MotionToast.TOAST_SUCCESS);
    }

    private User getUser() {
        return RealmHelper.getUser(context, "in noteEdit");
    }

    public User getUser(Context context) {
        return RealmHelper.getUser(context, "in noteEdit from sheet");
    }

    private void showChecklistItemsDeleteBottomSheet() {
        CheckListDeleteSheet checkListDeleteSheet = new CheckListDeleteSheet();
        checkListDeleteSheet.show(getSupportFragmentManager(), checkListDeleteSheet.getTag());
    }

    private void showChecklistItemsHideBottomSheet() {
        CheckListHideSheet checkListHideSheet = new CheckListHideSheet(getCurrentNote(context, noteId).getVisibilityStatus(), noteId);
        checkListHideSheet.show(getSupportFragmentManager(), checkListHideSheet.getTag());
    }

}