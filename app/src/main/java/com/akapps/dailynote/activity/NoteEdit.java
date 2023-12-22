package com.akapps.dailynote.activity;

import static android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM;
import static com.akapps.dailynote.classes.helpers.RealmHelper.getCurrentNote;
import static com.akapps.dailynote.classes.helpers.UiHelper.getThemeStyle;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
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
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmHelper;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
import com.akapps.dailynote.classes.helpers.RepeatListener;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.akapps.dailynote.classes.other.BudgetSheet;
import com.akapps.dailynote.classes.other.ChecklistItemSheet;
import com.akapps.dailynote.classes.other.ColorSheet;
import com.akapps.dailynote.classes.other.ExportNotesSheet;
import com.akapps.dailynote.classes.other.FilterChecklistSheet;
import com.akapps.dailynote.classes.other.IconPowerMenuItem;
import com.akapps.dailynote.classes.other.InfoSheet;
import com.akapps.dailynote.classes.other.LockSheet;
import com.akapps.dailynote.classes.other.NoteInfoSheet;
import com.akapps.dailynote.classes.other.RecordAudioSheet;
import com.akapps.dailynote.recyclerview.checklist_recyclerview;
import com.akapps.dailynote.recyclerview.photos_recyclerview;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.github.clans.fab.FloatingActionButton;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.skydoves.powermenu.CustomPowerMenu;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private RichEditor note;
    private EditText noteSearching;
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
    private boolean isAppPaused;
    private boolean isShowingPhotos;
    private boolean isEditLockedMode = true;
    private String currentDateTimeSelected;
    private Calendar dateSelected;
    private int countPicsNotFound;
    private boolean isSearchingNotes;
    private int currentWordIndex;
    private String target;
    private ArrayList wordOccurences = new ArrayList();
    private boolean isChangingTextSize;
    private boolean isChanged;
    private boolean isWidget;
    private Handler handler;
    private boolean dismissNotification;
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

    // empty list layout
    private ScrollView empty_Layout;
    private TextView empty_title;
    private TextView subtitle;
    private TextView subSubTitle;

    public User user;

    private final String ACTION_ADD_CHECKLIST = "android.intent.action.CREATE_SHORTCUT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(getThemeStyle(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        context = this;
        noteId = getIntent().getIntExtra("id", -1);

        if (noteId > 0 && getRealm().where(Note.class).equalTo("noteId", noteId).count() == 0) {
            RealmSingleton.setCloseRealm(false);
            // this catches a widget whose associated note has been deleted
            Toast.makeText(this, "Note has been deleted, please delete widget!", Toast.LENGTH_LONG).show();
            RealmSingleton.closeRealmInstance("NoteEdit onDestroy - Deleted Note is being accessed via widget");
            finish();
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

        user = RealmHelper.getUser(context, "in space");
        initializeLayout(savedInstanceState);
        UiHelper.setStatusBarColor(this);

        scrollView.setBackgroundColor(UiHelper.getColorFromTheme(this, R.attr.primaryBackgroundColor));
        note.setBackgroundColor(UiHelper.getColorFromTheme(this, R.attr.primaryBackgroundColor));
        searchEditText.setTextColor(UiHelper.getColorFromTheme(this, R.attr.primaryTextColor));
        budget.setColorNormal(UiHelper.getColorFromTheme(this, R.attr.secondaryBackgroundColor));

        hideRichTextStatus = user.isHideRichTextEditor();
        if (user.isHideRichTextEditor())
            formatMenu.setVisibility(View.GONE);
        if (!user.isShowAudioButton() && getCurrentNote(context, noteId).isCheckList())
            addAudioItem.setVisibility(View.VISIBLE);
        if (!user.isHideBudget() && getCurrentNote(context, noteId).isCheckList())
            budget.setVisibility(View.VISIBLE);

        if (RealmHelper.isNoteWidget(context, noteId))
            Helper.updateWidget(getCurrentNote(context, noteId), context, getRealm());

        if (dismissNotification)
            Helper.cancelNotification(context, noteId);
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
    public void onBackPressed() {
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
        } else if (isChangingTextSize)
            isChangingTextSize = false;
        else {
            if (noteChanged() && !isNewNote)
                Helper.showMessage(this, "Edited", "Note has been edited", MotionToast.TOAST_SUCCESS);

            finish("on-back press");
            if (!AppData.isDisableAnimation)
                overridePendingTransition(R.anim.stay, R.anim.right_out);
        }
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

        if (RealmHelper.getNotePin(context, noteId) != 0 && isAppPaused) {
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
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeLayout(Bundle savedInstanceState) {
        // layout is initialized
        title = findViewById(R.id.title);
        date = findViewById(R.id.date);
        note = findViewById(R.id.note);
        noteSearching = findViewById(R.id.searchEdittext);
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

        // search
        searchEditText = findViewById(R.id.search_text);
        searchClose = findViewById(R.id.search_close);
        search = findViewById(R.id.search);
        searchLayout = findViewById(R.id.search_padding);

        note.setEditorFontSize(20);
        note.setPlaceholder("Type something...");
        note.setEditorFontColor(UiHelper.getColorFromTheme(this, R.attr.primaryTextColor));
        note.setPadding(5, 10, 0, 100);
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
        Realm realm = getRealm();

        if (null != getCurrentNote(context, noteId).getChecklist())
            checkListItems = getCurrentNote(context, noteId).getChecklist().sort("positionInList");
        populatePhotos();
        oldTitle = getCurrentNote(context, noteId).getTitle();
        oldNote = getCurrentNote(context, noteId).getNote();
        title.setText(getCurrentNote(context, noteId).getTitle());
        note.setHtml(getCurrentNote(context, noteId).getNote());
        String textSize = Helper.getPreference(context, "size");
        if (textSize == null)
            textSize = "20";
        note.setEditorFontSize(Integer.parseInt(textSize));
        note.setEditorFontColor(getCurrentNote(context, noteId).getTextColor() == 0 ? R.attr.primaryTextColor : getCurrentNote(context, noteId).getTextColor());
        title.setTextColor(getCurrentNote(context, noteId).getTitleColor());
        category.setText(getCategoryName());
        category.setVisibility(View.VISIBLE);
        folderText.setVisibility(View.VISIBLE);
        searchLayout.setVisibility(View.VISIBLE);

        initializeEditor();
        updateColors();

        if (getCurrentNote(context, noteId).isCheckList()) {
            sortChecklist();
            showCheckListLayout(true);
            searchLayout.setVisibility(View.GONE);
            formatMenu.setVisibility(View.GONE);
            checkListRecyclerview.requestFocus();
        } else {
            formatMenu.setVisibility(View.VISIBLE);

            // TO DO, see if do not edit note toggle is enabled and then run the following
            if (user.isEnableEditableNoteButton() && !isNewNoteCopy) {
                note.setInputEnabled(!isEditLockedMode);
                addCheckListItem.setVisibility(View.VISIBLE);
                addCheckListItem.setImageDrawable(getDrawable(
                        isEditLockedMode ? R.drawable.edit_filled_icon : R.drawable.do_not_edit_icon));
            }

            if (isNewNote)
                title.requestFocus();
            else
                note.focusEditor();
        }

        if (getCurrentNote(context, noteId).isChecked())
            title.setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);

        // sets background of color icon to whatever the current note color is
        if (!isNewNote)
            noteColor.setCardBackgroundColor(getCurrentNote(context, noteId).getBackgroundColor());

        updateDateEdited();
        if (!getCurrentNote(context, noteId).getReminderDateTime().isEmpty()) {
            updateReminderLayout(View.VISIBLE);
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
        if (getCurrentNote(context, noteId).isPin())
            pinNoteIcon.setImageDrawable(getDrawable(R.drawable.pin_filled_icon));


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
                    addCheckListItem.setImageDrawable(getDrawable(R.drawable.camera_icon));
            }

            if (isSearchingNotes) {
                addCheckListItem.setVisibility(View.GONE);
                photosScrollView.setVisibility(View.GONE);
            }

            // TO DO, see if do not edit note toggle is enabled and then run the following
            if (user.isEnableEditableNoteButton() && !isNewNoteCopy) {
                addCheckListItem.setVisibility(View.VISIBLE);
                addCheckListItem.setImageDrawable(getDrawable(
                        isEditLockedMode ? R.drawable.edit_filled_icon : R.drawable.do_not_edit_icon));
            }
        }

        search.setOnClickListener(v -> {
            if (note.getHtml().length() > 0) {
                textSizeLayout.setVisibility(View.VISIBLE);
                showSearchBar();
            } else
                Helper.showMessage(this, "Empty", "Searching for something " +
                        "that does not exist is impossible", MotionToast.TOAST_WARNING);
        });

        scrollView.setOnClickListener(view -> {
            if (getCurrentNote(context, noteId) != null && !getCurrentNote(context, noteId).isCheckList()) {
                if (!user.isEnableEditableNoteButton() || !isEditLockedMode) {
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
                currentWordIndex = 0;
                if (isSearchingNotes) {
                    textSizeLayout.setVisibility(View.VISIBLE);
                    if (!s.toString().isEmpty() && s.toString().length() > 1)
                        findText(s.toString().toLowerCase());
                    else {
                        wordOccurences = new ArrayList();
                        noteSearching.setText(Html.fromHtml(getCurrentNote(context, noteId).getNote(), Html.FROM_HTML_MODE_COMPACT));
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
            BudgetSheet budget = new BudgetSheet(getCurrentNote(context, noteId).getChecklist(), user.getBudgetCharacter(), user.getExpenseCharacter());
            budget.show(this.getSupportFragmentManager(), budget.getTag());
        });

        category.setOnClickListener(v -> {
            getRealm().beginTransaction();
            getRealm().where(Note.class).findAll().setBoolean("isSelected", false);
            getCurrentNote(context, noteId).setSelected(true);
            getRealm().commitTransaction();
            Intent category = new Intent(NoteEdit.this, CategoryScreen.class);
            category.putExtra("editing_reg_note", true);
            startActivity(category);
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
            });
        }

        closeTextLayout.setOnClickListener(v -> {
            isChangingTextSize = false;
            if (!isSearchingNotes) {
                hideButtons();
                if (getCurrentNote(context, noteId).isCheckList()) {
                    addCheckListItem.setVisibility(View.VISIBLE);
                    if (!RealmHelper.getUser(context, "in space").isShowAudioButton())
                        addAudioItem.setVisibility(View.VISIBLE);
                } else {
                    // TO DO, see if do not edit note toggle is enabled and then run the following
                    if (user.isEnableEditableNoteButton()) {
                        addCheckListItem.setVisibility(View.VISIBLE);
                        addCheckListItem.setImageDrawable(getDrawable(
                                isEditLockedMode ? R.drawable.edit_filled_icon : R.drawable.do_not_edit_icon));
                    }
                }
            } else {
                hideSearchBar();
                noteSearching.clearFocus();
                title.clearFocus();
            }
        });

        increaseTextSize.setOnTouchListener(new RepeatListener(500, 100, v -> {
            if (isSearchingNotes) {
                if (currentWordIndex != -1 && wordOccurences.size() != 0) {
                    int nextIndex;
                    noteSearching.requestFocus();
                    currentWordIndex = currentWordIndex == 0 ? wordOccurences.size() - 1
                            : currentWordIndex - 1;
                    nextIndex = Integer.parseInt(wordOccurences.get(currentWordIndex).toString());
                    noteSearching.setSelection(nextIndex);
                }
            } else if (isChangingTextSize)
                changeTextSize(true, getCurrentNote(context, noteId).isCheckList());
        }));

        decreaseTextSize.setOnTouchListener(new RepeatListener(500, 100, v -> {
            if (isSearchingNotes) {
                int nextIndex;
                if (currentWordIndex != -1 && wordOccurences.size() != 0) {
                    noteSearching.requestFocus();
                    currentWordIndex = currentWordIndex == wordOccurences.size() - 1 ? 0 :
                            currentWordIndex + 1;
                    nextIndex = Integer.parseInt(wordOccurences.get(currentWordIndex).toString());
                    noteSearching.setSelection(nextIndex);
                }
            } else if (isChangingTextSize)
                changeTextSize(false, getCurrentNote(context, noteId).isCheckList());
        }));

        note.setOnClickListener(view -> {
        });

        remindNote.setOnClickListener(v -> {
            cancelAlarm(getCurrentNote(context, noteId).getNoteId());
            remindNote.setVisibility(View.GONE);
            showMessage("Reminder", "Reminder was deleted", false);
        });

        closeNote.setOnClickListener(v -> {
            onBackPressed();
        });

        addCheckListItem.setOnClickListener(v -> {
            if (getCurrentNote(context, noteId).isCheckList() && !isShowingPhotos)
                openNewItemDialog();
            else if (isShowingPhotos) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                    InfoSheet permissionInfo = new InfoSheet("Camera Permission Required", Manifest.permission.CAMERA);
                    permissionInfo.show(getSupportFragmentManager(), permissionInfo.getTag());
                } else
                    showCameraDialog();
            } else {
                note.setInputEnabled(isEditLockedMode);
                isEditLockedMode = !isEditLockedMode;
                // TO DO, see if do not edit note toggle is enabled and then run the following
                addCheckListItem.setImageDrawable(getDrawable(
                        isEditLockedMode ? R.drawable.edit_filled_icon : R.drawable.do_not_edit_icon));
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
            return false;
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
                addCheckListItem.setImageDrawable(getDrawable(R.drawable.camera_icon));
                if (isChangingTextSize)
                    textSizeLayout.setVisibility(View.GONE);
            }
        });

        expandMenu.setOnClickListener(v ->
                openMenuDialog());

        noteColor.setOnClickListener(v -> {
            ColorSheet colorSheet = new ColorSheet();
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
                && getCurrentNote(context, noteId).getChecklist().size() == 0) {
            if (!user.isEnableEmptyNote()) {
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
        params.setMargins(0, 0, 8, 0);
        searchEditText.setLayoutParams(params);
        searchEditText.requestFocusFromTouch();

        if (searchEditText.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
        }

        if (isShowingPhotos) {
            addCheckListItem.setVisibility(View.GONE);
            photosScrollView.setVisibility(View.GONE);
        }

        String textSize = Helper.getPreference(context, "size");
        if (textSize == null)
            textSize = "20";

        note.setVisibility(View.GONE);
        noteSearching.setTextSize(TypedValue.COMPLEX_UNIT_SP, Integer.parseInt(textSize));
        noteSearching.setVisibility(View.VISIBLE);
        noteSearching.setText(Html.fromHtml(note.getHtml(), Html.FROM_HTML_MODE_COMPACT));

        showButtons();
    }

    private void hideSearchBar() {
        isSearchingNotes = false;
        noteSearching.setText("");
        searchEditText.setText("");
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
        note.setHtml(getCurrentNote(context, noteId).getNote());

        if (isShowingPhotos) {
            addCheckListItem.setVisibility(View.VISIBLE);
            photosScrollView.setVisibility(View.VISIBLE);
        }

        note.setVisibility(View.VISIBLE);
        noteSearching.setVisibility(View.GONE);

        hideButtons();
    }

    private void showButtons() {
        new Handler().postDelayed(() -> {
            ObjectAnimator refreshAnimation = ObjectAnimator.ofFloat(textSizeLayout, "translationY", -1 * (textSizeLayout.getY() / 2));
            refreshAnimation.setDuration(500);
            refreshAnimation.start();
        }, 100);
    }

    private void hideButtons() {
        ObjectAnimator refreshAnimation = ObjectAnimator.ofFloat(textSizeLayout, "translationY", 0f);
        refreshAnimation.setDuration(500);
        refreshAnimation.start();
        new Handler().postDelayed(() -> {
            textSizeLayout.setVisibility(View.GONE);
        }, 500);
    }

    public void sortChecklist() {
        RealmResults<CheckListItem> results = Helper.sortChecklist(getCurrentNote(context, noteId), getRealm());
        populateChecklist(results);
        title.clearFocus();
    }

    public void updateColors() {
        noteColor.setCardBackgroundColor(getCurrentNote(context, noteId).getBackgroundColor());
        category.setTextColor(getCurrentNote(context, noteId).getBackgroundColor());
        title.setTextColor(getCurrentNote(context, noteId).getTitleColor());
        note.setEditorFontColor(getCurrentNote(context, noteId).getTextColor() == -1 ? R.attr.primaryTextColor : getCurrentNote(context, noteId).getTextColor());
        if (checklistAdapter != null && getCurrentNote(context, noteId).isCheckList())
            checklistAdapter.notifyDataSetChanged();

        if (!Helper.isColorDark(getCurrentNote(context, noteId).getBackgroundColor()))
            palleteIconColor.setColorFilter(context.getColor(R.color.black));
        else
            palleteIconColor.setColorFilter(context.getColor(R.color.white));
    }

    private void findText(String target) {
        if (!getCurrentNote(context, noteId).isCheckList()) {

            String originalString = note.getHtml();

            if (originalString.toLowerCase().contains(target.toLowerCase())) {
                this.target = target;
                // Replace the specified text/word with formatted text/word
                String modifiedString = originalString.replaceAll("(?i)" + target,
                        "<font color='#ff8000'>" + "$0" + "</font>");
                // Update the edit text
                noteSearching.setText(Html.fromHtml(modifiedString, Html.FROM_HTML_MODE_COMPACT));
                findAllTextIndexes(target);
                decreaseTextSize.setAlpha(new Float(1.0));
                increaseTextSize.setAlpha(new Float(1.0));
            } else {
                currentWordIndex = -1;
                noteSearching.setText(Html.fromHtml(getCurrentNote(context, noteId).getNote(), Html.FROM_HTML_MODE_COMPACT));
                decreaseTextSize.setAlpha(new Float(0.5));
                increaseTextSize.setAlpha(new Float(0.5));
            }
        }
    }

    private void findAllTextIndexes(String target) {
        wordOccurences = new ArrayList<String>();
        target = target.toLowerCase();
        String text = Html.fromHtml(getCurrentNote(context, noteId).getNote().toLowerCase(), Html.FROM_HTML_MODE_COMPACT).toString();

        int index = 0;
        while (index >= 0) {
            index = text.indexOf(target, index);
            if (index != -1)
                wordOccurences.add(index++);
        }
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
        String textSize = Helper.getPreference(context, "size");
        if (textSize == null)
            textSize = "20";
        int textSizeNumber = Integer.parseInt(textSize);
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

        Helper.savePreference(context, String.valueOf(currentTextSize), "size");

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
                newNote = new Note(titleFromOtherApp, "", user.isEnableSublists());
            else
                newNote = new Note(title.getText().toString(), "", user.isEnableSublists());
        } else {
            try {
                if ((noteFromOtherApp != null && !noteFromOtherApp.isEmpty()) ||
                        (titleFromOtherApp != null && !titleFromOtherApp.isEmpty()))
                    newNote = new Note(titleFromOtherApp, noteFromOtherApp, user.isEnableSublists());
                else
                    newNote = new Note(title.getText().toString(), note.getHtml(), user.isEnableSublists());
            } catch (Exception e) {
                if ((noteFromOtherApp != null && !noteFromOtherApp.isEmpty()) ||
                        (titleFromOtherApp != null && !titleFromOtherApp.isEmpty()))
                    newNote = new Note(titleFromOtherApp, noteFromOtherApp, user.isEnableSublists());
                else
                    newNote = new Note(title.getText().toString(), "", user.isEnableSublists());
            }
        }

        if (getRealm().where(Note.class).equalTo("noteId", newNote.getNoteId()).findAll().size() != 0) {
            // note id for new note already exists, try to add again with a NEW unique note id
            addNote();
        } else {
            newNote.setPin(currentPin);
            newNote.setIsCheckList(isCheckList);
            // add a random color to the note
            int[] randomColor = context.getResources().getIntArray(R.array.randomColor);
            int randomInt = (int) (Math.random() * (randomColor.length));
            newNote.setBackgroundColor(randomColor[randomInt]);
            newNote.setTitleColor(randomColor[randomInt]);
            newNote.setTextColor(0);
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
    public CheckListItem addCheckList(String itemText, Place place) {
        int initialPosition = -1;

        if (getCurrentNote(context, noteId).getChecklist().size() != 0) {
            if (getCurrentNote(context, noteId).getSort() == 6)
                initialPosition = getCurrentNote(context, noteId).getChecklist().min("positionInList").intValue() - 1;
            else if (getCurrentNote(context, noteId).getSort() == 5)
                initialPosition = getCurrentNote(context, noteId).getChecklist().max("positionInList").intValue() + 1;
        } else
            initialPosition = getCurrentNote(context, noteId).getChecklist().size();

        Random rand = new Random();

        // insert data to database
        getRealm().beginTransaction();
        CheckListItem currentItem = new CheckListItem(itemText.trim(), false, getCurrentNote(context, noteId).getNoteId(),
                initialPosition, rand.nextInt(100000) + 1, new SimpleDateFormat("E, MMM dd")
                .format(Calendar.getInstance().getTime()), place);
        getCurrentNote(context, noteId).getChecklist().add(currentItem);
        getCurrentNote(context, noteId).setChecked(false);
        getRealm().commitTransaction();
        updateSaveDateEdited();
        isListEmpty(getCurrentNote(context, noteId).getChecklist().size());

        checklistAdapter.notifyDataSetChanged();

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

    public CheckListItem addCheckList(int subListId, String path, int duration) {
        CheckListItem newItem = getRealm().where(CheckListItem.class).equalTo("subListId", subListId).findFirst();
        getRealm().beginTransaction();
        newItem.setAudioPath(path);
        newItem.setAudioDuration(duration);
        getRealm().commitTransaction();
        return newItem;
    }

    public void onlyAddChecklist(String itemText, boolean isChecked) {
        int initialPosition = -1;

        if (getCurrentNote(context, noteId).getChecklist().size() != 0) {
            if (getCurrentNote(context, noteId).getSort() == 6)
                initialPosition = getCurrentNote(context, noteId).getChecklist().min("positionInList").intValue() - 1;
            else if (getCurrentNote(context, noteId).getSort() == 5)
                initialPosition = getCurrentNote(context, noteId).getChecklist().max("positionInList").intValue() + 1;
        } else
            initialPosition = getCurrentNote(context, noteId).getChecklist().size();

        Random rand = new Random();

        // insert data to database
        getRealm().beginTransaction();
        CheckListItem currentItem = new CheckListItem(itemText.trim(), isChecked, getCurrentNote(context, noteId).getNoteId(),
                initialPosition, rand.nextInt(100000) + 1, new SimpleDateFormat("E, MMM dd")
                .format(Calendar.getInstance().getTime()), new Place("", "", "", 0, 0));
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

        int backgroundColor = R.color.gray_100;
        if (user.getScreenMode() == User.Mode.Dark)
            backgroundColor = R.color.black;
        else if (user.getScreenMode() == User.Mode.Light) {

        }

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
            noteMenu.addItem(4, new IconPowerMenuItem(getDrawable(R.drawable.delete_all_icon), "Delete All"));
            noteMenu.addItem(7, new IconPowerMenuItem(getDrawable(R.drawable.filter_icon), "Sort"));
            if (user.isEnableSublists()) {
                String sublistStatus = "";
                if (getCurrentNote(context, noteId).isEnableSublist()) {
                    sublistStatus = sublistStatus + "Sub-List";
                    noteMenu.addItem(5, new IconPowerMenuItem(getDrawable(R.drawable.not_visible_icon), sublistStatus));
                } else {
                    sublistStatus = sublistStatus + "Sub-List";
                    noteMenu.addItem(5, new IconPowerMenuItem(getDrawable(R.drawable.visible_icon), sublistStatus));
                }
            }
        } else {
            if (user.isHideRichTextEditor()) {
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
                        Helper.getNoteString(getCurrentNote(context, noteId), getRealm()) : getCurrentNote(context, noteId).getNote());
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
                FilterChecklistSheet filter = new FilterChecklistSheet(getCurrentNote(context, noteId));
                filter.show(getSupportFragmentManager(), filter.getTag());
            } else if (item.getTitle().equals("Lock")) {
                LockSheet lockSheet = new LockSheet(false);
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
            } else if (item.getTitle().equals("Delete All")) {
                InfoSheet info = new InfoSheet(3, true);
                info.show(getSupportFragmentManager(), info.getTag());
            } else if (item.getTitle().contains("Text")) {
                isChangingTextSize = true;
                textSizeLayout.setVisibility(View.VISIBLE);
                addAudioItem.setVisibility(View.GONE);
                addCheckListItem.setVisibility(View.GONE);
                decreaseTextSize.setAlpha(new Float(1.0));
                increaseTextSize.setAlpha(new Float(1.0));
            } else if (item.getTitle().equals("Info")) {
                NoteInfoSheet noteInfoSheet = new NoteInfoSheet(getCurrentNote(context, noteId), false);
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
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU;
    }

    public void checkNotificationPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED && Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 2);
        } else
            showDatePickerDialog();
    }

    public boolean isMicrophonePermissionEnabled() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    public void checkMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 3);
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

    public void deleteChecklist() {
        RealmHelper.deleteChecklist(getCurrentNote(context, noteId), context);
        checkNote(false);
        checklistAdapter.notifyDataSetChanged();
        Helper.showMessage(NoteEdit.this, "Success", "All items deleted", MotionToast.TOAST_SUCCESS);
        isListEmpty(0, true);
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
        getRealm().beginTransaction();
        getCurrentNote(context, noteId).setPinNumber(pin);
        getCurrentNote(context, noteId).setSecurityWord(securityWord);
        getCurrentNote(context, noteId).setFingerprint(fingerprint);
        getRealm().commitTransaction();
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

    private void updateReminderLayout(int visibility) {
        remindNote.setVisibility(visibility);
        remindNoteDate.setVisibility(visibility);
        if (!getCurrentNote(context, noteId).getReminderDateTime().isEmpty()) {
            int formatDate24Hours = Integer.parseInt(getCurrentNote(context, noteId).getReminderDateTime().split(" ")[1].split(":")[0]);
            int formatDate12Hours = formatDate24Hours % 12;
            String formatted = getCurrentNote(context, noteId).getReminderDateTime().split(" ")[0] + " " + formatDate12Hours +
                    ":" + getCurrentNote(context, noteId).getReminderDateTime().split(" ")[1].split(":")[1] +
                    ":" + getCurrentNote(context, noteId).getReminderDateTime().split(" ")[1].split(":")[2] + " " + ((formatDate24Hours > 12) ? "PM" : "AM");
            remindNoteDate.setText(formatted);
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

    private void populateChecklist(RealmResults<CheckListItem> currentList) {
        int span = 1;
        if (Helper.isTablet(context))
            span = 2;
        GridLayoutManager layout = new GridLayoutManager(context, span);
        checkListRecyclerview.setLayoutManager(layout);
        checklistAdapter = new checklist_recyclerview(currentList, getCurrentNote(context, noteId), this);
        checklistAdapter.setHasStableIds(true);
        checkListRecyclerview.setAdapter(checklistAdapter);
    }

    private void timeDialog() {
        Calendar now = Calendar.getInstance();
        TimePickerDialog timer = TimePickerDialog.newInstance(
                this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                false
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
            updateReminderLayout(View.VISIBLE);
            Helper.showMessage(this, "Reminder set", "Will Remind you " +
                    "in " + Helper.getTimeDifference(c, true), MotionToast.TOAST_SUCCESS);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, noteId, intent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                } else {
                    Helper.showMessage(this, "Alarm Not Set", "Please Enable " +
                            "Alarms & Reminders ", MotionToast.TOAST_ERROR);
                    Intent intent2 = new Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(intent2);
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

        if (isSublistEnabled) {
            getRealm().beginTransaction();
            getCurrentNote(context, noteId).setEnableSublist(false);
            getRealm().commitTransaction();
            Helper.showMessage(this, "Sublist Status", "It has has been " +
                    "disabled", MotionToast.TOAST_SUCCESS);
        } else {
            getRealm().beginTransaction();
            getCurrentNote(context, noteId).setEnableSublist(true);
            getRealm().commitTransaction();
            Helper.showMessage(this, "Sublist Status", "It has been " +
                    "enabled", MotionToast.TOAST_SUCCESS);
        }
        updateSaveDateEdited();
        checklistAdapter.notifyDataSetChanged();
    }

    // determines if there were changes to the note
    private boolean noteChanged() {
        boolean isTitleChanged = !title.getText().toString().equals(oldTitle);
        boolean isNoteChanged = !note.getHtml().equals(oldNote);
        return isTitleChanged || isNoteChanged;
    }

    // makes sure title is not empty
    private boolean checkInput() {
        if (title.getText().toString().isEmpty()) {
            Helper.showMessage(this, "fill in empty fields", "Title is empty", MotionToast.TOAST_ERROR);
            return false;
        }
        return true;
    }

    public void showCameraDialog() {
        ImagePicker.with(this)
                .maxResultSize(704, 704)
                .compress(1024)
                .saveDir(getExternalFilesDir("/Documents"))
                .start(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 0) {
            Uri uri = data.getData();

            Photo currentPhoto = new Photo(noteId, uri.getPath());
            getRealm().beginTransaction();
            getRealm().insert(currentPhoto);
            getRealm().commitTransaction();
            updateSaveDateEdited();
            scrollAdapter.notifyDataSetChanged();
            showPhotos(View.VISIBLE);
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
        }
    }

    private void openNewItemDialog() {
        ChecklistItemSheet checklistItemSheet = new ChecklistItemSheet();
        checklistItemSheet.show(getSupportFragmentManager(), checklistItemSheet.getTag());
    }

    private void openDialog() {
        InfoSheet info = new InfoSheet(getCurrentNote(context, noteId).isTrash() ? -3 : 3, false);
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

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && !RealmHelper.getUser(context, "in space").isDisableLastEditInfo()) {
            handler.postDelayed(new Runnable() {
                public void run() {
                    if (!getRealm().isClosed()) {
                        try {
                            if (Helper.getTimeDifference(Helper.dateToCalender(getCurrentNote(context, noteId).getDateEdited().replace("\n", " ")), false).length() > 0) {
                                if (user.isTwentyFourHourFormat()) {
                                    date.setText(Html.fromHtml("Last Edit: " + Helper.convertToTwentyFourHour(getCurrentNote(context, noteId).getDateEdited()).replace("\n", " ") +
                                            "<br>" + Helper.getTimeDifference(Helper.dateToCalender(getCurrentNote(context, noteId).getDateEdited().replace("\n", " ")), false) + " ago", Html.FROM_HTML_MODE_COMPACT));
                                } else {
                                    date.setText(Html.fromHtml("Last Edit: " + getCurrentNote(context, noteId).getDateEdited().replace("\n", " ") +
                                            "<br>" + Helper.getTimeDifference(Helper.dateToCalender(getCurrentNote(context, noteId).getDateEdited().replace("\n", " ")), false) + " ago", Html.FROM_HTML_MODE_COMPACT));
                                }
                            } else {
                                date.setText(getCurrentNote(context, noteId).getDateEdited().replace("\n", " ") + "\n  ");
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
        });

        findViewById(R.id.action_italic).setOnClickListener(v -> {
            updateSaveDateEdited();
            note.setItalic();
        });

        findViewById(R.id.action_strikethrough).setOnClickListener(v -> {
            updateSaveDateEdited();
            note.setStrikeThrough();
        });

        findViewById(R.id.action_underline).setOnClickListener(v -> {
            updateSaveDateEdited();
            note.setUnderline();
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
            InfoSheet info = new InfoSheet(9, false);
            info.show(getSupportFragmentManager(), info.getTag());
        });

        findViewById(R.id.action_text_size).setOnClickListener(v -> {
            isChangingTextSize = true;
            textSizeLayout.setVisibility(View.VISIBLE);
            addCheckListItem.setVisibility(View.GONE);
        });

    }

    public void removeFormatting() {
        updateSaveDateEdited();
        String removedFormat = Html.fromHtml(getCurrentNote(context, noteId).getNote(), Html.FROM_HTML_MODE_COMPACT).toString();
        getRealm().beginTransaction();
        getCurrentNote(context, noteId).setNote(removedFormat);
        getRealm().commitTransaction();
        note.setHtml(removedFormat);
        note.focusEditor();
        Helper.showMessage(this, "Removed", "Formatting has been removed",
                MotionToast.TOAST_SUCCESS);
    }
}