package com.akapps.dailynote.activity;

import static android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.akapps.dailynote.R;
import com.akapps.dailynote.adapter.IconMenuAdapter;
import com.akapps.dailynote.classes.data.SubCheckListItem;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.AlertReceiver;
import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmDatabase;
import com.akapps.dailynote.classes.helpers.RealmHelper;
import com.akapps.dailynote.classes.other.AppWidget;
import com.akapps.dailynote.classes.other.ChecklistItemSheet;
import com.akapps.dailynote.classes.other.ColorSheet;
import com.akapps.dailynote.classes.other.FilterChecklistSheet;
import com.akapps.dailynote.classes.other.IconPowerMenuItem;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.Photo;
import com.akapps.dailynote.classes.helpers.RepeatListener;
import com.akapps.dailynote.classes.other.InfoSheet;
import com.akapps.dailynote.classes.other.LockSheet;
import com.akapps.dailynote.classes.other.NoteInfoSheet;
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
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import jp.wasabeef.richeditor.RichEditor;
import www.sanju.motiontoast.MotionToast;

public class NoteEdit extends FragmentActivity implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener{

    // layout
    public EditText title;
    public TextView date;
    private RichEditor note;
    private EditText noteSearching;
    private CardView closeNote;
    public CardView photosNote;
    private ImageView pinNoteIcon;
    private CardView pinNoteButton;
    public CardView saveNote;
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
    private FloatingActionButton sort;
    private FloatingActionButton info;
    // search
    private EditText searchEditText;
    private ImageView searchClose;
    private ImageView search;
    private CardView searchLayout;

    // on-device database
    public Realm realm;
    private RealmResults<Note> allNotes;
    private RealmResults<CheckListItem> checkListItems;

    // activity data
    private Context context;
    public Note currentNote;
    public RealmResults<Photo> allNotePhotos;
    private int noteId;
    private boolean isNewNote;
    private String oldTitle;
    private String oldNote;
    private boolean currentPin;
    private boolean dismissDialog;
    private boolean isShowingPhotos;
    private String currentDateTimeSelected;
    private Calendar dateSelected;
    private int countPicsNotFound;
    private boolean noteEdited;
    private boolean isSearchingNotes;
    private int currentWordIndex;
    private String target;
    private ArrayList wordOccurences = new ArrayList();
    private boolean isChangingTextSize;
    private boolean isChanged;
    private boolean isWidget;
    private Handler handler;
    private ActivityResultLauncher<Intent> launcher;
    public boolean sortEnable;
    private boolean isLightMode;
    // dialog
    // dialog
    private AlertDialog colorPickerView;
    private CustomPowerMenu noteMenu;

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

    // empty list layout
    private ScrollView empty_Layout;
    private TextView empty_title;
    private TextView subtitle;
    private TextView subSubTitle;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        context = this;
        noteId = getIntent().getIntExtra("id", -1);
        isCheckList = getIntent().getBooleanExtra("isChecklist", false);
        isWidget = getIntent().getBooleanExtra("isWidget", false);

        if(noteId<-1)
            noteId *=-1;
        else
            overridePendingTransition(R.anim.left_in, R.anim.stay);

        // initializes database and retrieves all notes
        try {
            realm = Realm.getDefaultInstance();
        }
        catch (Exception e){
            realm = RealmDatabase.setUpDatabase(context);
        }
        allNotes = realm.where(Note.class).findAll();

        // if orientation changes, then position is updated
        if (savedInstanceState != null)
            noteId = savedInstanceState.getInt("id");

        initializeLayout(savedInstanceState);

        user = realm.where(User.class).findFirst();
        assert user != null;
        if (user.isModeSettings()) {
            getWindow().setStatusBarColor(context.getColor(R.color.light_mode));
            isLightMode = true;
            scrollView.setBackgroundColor(context.getColor(R.color.light_mode));
            note.setBackgroundColor(context.getColor(R.color.light_mode));
            searchEditText.setTextColor(context.getColor(R.color.light_gray));
            if(noteId != -1 && !isNewNote)
                if (currentNote.getTextColor() <= 0)
                    note.setEditorFontColor(context.getColor(R.color.gray));
            date.setTextColor(context.getColor(R.color.light_gray));
        }
        else {
            scrollView.setBackgroundColor(context.getColor(R.color.gray));
            searchEditText.setTextColor(context.getColor(R.color.light_gray));

            if(noteId != -1 && !isNewNote)
                if (currentNote.getTextColor() <= 0)
                    note.setEditorFontColor(context.getColor(R.color.ultra_white));
        }
    }

    // when orientation changes, then note data is saved
    @Override
    public void onSaveInstanceState(@NotNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("title", title.getText().toString());
        try {
            savedInstanceState.putString("note", note.getHtml());
        }
        catch (Exception e){}
        savedInstanceState.putBoolean("pin", currentPin);
        savedInstanceState.putBoolean("newNote", isNewNote);
        savedInstanceState.putInt("id", noteId);
        savedInstanceState.putBoolean("photos", isShowingPhotos);
        savedInstanceState.putBoolean("search", isSearchingNotes);
    }

    @Override
    public void onBackPressed() {
        if(isSearchingNotes) {
            hideSearchBar();
            note.clearFocus();
            title.clearFocus();
        }
        else if(isChangingTextSize)
            isChangingTextSize = false;
        else {
            if(noteChanged() && !isNewNote)
                Helper.showMessage(this, "Edited", "Note has been edited", MotionToast.TOAST_SUCCESS);

            finish();
            overridePendingTransition(R.anim.stay, R.anim.right_out);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (isWidget && null != currentNote && currentNote.getWidgetId() > 0) {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                AppWidget.updateAppWidget(context, appWidgetManager, currentNote.getNoteId(), currentNote.getWidgetId());
                appWidgetManager.notifyAppWidgetViewDataChanged(currentNote.getWidgetId(), R.id.preview_checklist);
            }
        }catch (Exception e){}
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(realm!=null)
            realm.close();

        if(handler!=null)
            handler.removeCallbacksAndMessages(null);

        // if color picker dialog is open, close it so that memory isn't leaked
        if (colorPickerView != null)
            colorPickerView.cancel();
        if (noteMenu != null)
            noteMenu.dismiss();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // close keyboard if open
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if(currentNote!=null)
            category.setText(currentNote.getCategory());
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
        saveNote = findViewById(R.id.save);
        noteColor = findViewById(R.id.noteColor);
        expandMenu = findViewById(R.id.menu);
        photosScrollView = findViewById(R.id.note_photos);
        remindNote = findViewById(R.id.reminderLayout);
        remindNoteDate = findViewById(R.id.reminderDate);
        textSizeLayout = findViewById(R.id.text_size_layout);
        increaseTextSize = findViewById(R.id.increase_textsize);
        increaseTextSize = findViewById(R.id.increase_textsize);
        decreaseTextSize = findViewById(R.id.decrease_textsize);
        closeTextLayout = findViewById(R.id.close_Layout);
        addCheckListItem = findViewById(R.id.add_checklist_item);
        checkListRecyclerview = findViewById(R.id.checklist);
        empty_Layout = findViewById(R.id.empty_Layout);
        empty_title = findViewById(R.id.empty_title);
        sort = findViewById(R.id.sort);
        info = findViewById(R.id.info);
        subtitle = findViewById(R.id.empty_subtitle);
        subSubTitle = findViewById(R.id.empty_sub_subtitle);
        category = findViewById(R.id.category);
        folderText = findViewById(R.id.folderWord);
        formatMenu = findViewById(R.id.styleFormat);
        scrollView = findViewById(R.id.scroll);

        // search
        searchEditText =findViewById(R.id.search_text);
        searchClose = findViewById(R.id.search_close);
        search = findViewById(R.id.search);
        searchLayout = findViewById(R.id.search_padding);

        note.setEditorFontSize(20);
        note.setPlaceholder("Type something...");
        note.setEditorFontColor(context.getColor(R.color.ultra_white));
        note.setPadding(5, 10, 0, 100);
        note.setBackgroundColor(context.getColor(R.color.gray));
        note.focusEditor();

        photosScrollView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // if it's not a new note and note position is not -1 (which means it is a new note)
        // then data and layout info is updated
        if ((noteId != -1 && !isNewNote)) {
            //try {
                photosNote.setVisibility(View.VISIBLE);
                pinNoteIcon.setVisibility(View.VISIBLE);
                pinNoteButton.setVisibility(View.VISIBLE);
                noteColor.setVisibility(View.VISIBLE);
                expandMenu.setVisibility(View.VISIBLE);
                // current note
                currentNote = realm.where(Note.class).equalTo("noteId", noteId).findFirst();
                if(null != currentNote.getChecklist())
                    checkListItems = currentNote.getChecklist().sort("positionInList");
                allNotePhotos = realm.where(Photo.class).equalTo("noteId", noteId).findAll();
                populatePhotos();
                oldTitle = currentNote.getTitle();
                oldNote = currentNote.getNote();
                title.setText(currentNote.getTitle());
                note.setHtml(currentNote.getNote());
                String textSize = Helper.getPreference(context, "size");
                if(textSize==null)
                    textSize = "20";
                note.setEditorFontSize(Integer.parseInt(textSize));
                note.setEditorFontColor(currentNote.getTextColor());
                title.setTextColor(currentNote.getTitleColor());
                category.setText(currentNote.getCategory());
                category.setVisibility(View.VISIBLE);
                folderText.setVisibility(View.VISIBLE);
                category.setTextColor(context.getColor(R.color.orange));
                searchLayout.setVisibility(View.VISIBLE);
                saveNote.setVisibility(View.GONE);

                initializeEditor();
                updateColors();

                if (currentNote.isCheckList()) {
                    sortChecklist();
                    showCheckListLayout(true);
                    searchLayout.setVisibility(View.GONE);
                    formatMenu.setVisibility(View.GONE);
                }
                else {
                    formatMenu.setVisibility(View.VISIBLE);
                    sort.setVisibility(View.GONE);
                    info.setVisibility(View.GONE);
                }

                if(currentNote.isChecked())
                    title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                // sets background of color icon to whatever the current note color is
                if (!isNewNote)
                    noteColor.setCardBackgroundColor(currentNote.getBackgroundColor());

                if (savedInstanceState != null)
                    isShowingPhotos = savedInstanceState.getBoolean("photos");

                if(currentNote.getNote().length()==0 && !isShowingPhotos) {
                    note.requestFocus();
                    title.clearFocus();
                }

                if(currentNote.isCheckList()) {
                    title.clearFocus();
                    checkListRecyclerview.requestFocus();
                }

                updateDateEdited();
                if (!currentNote.getReminderDateTime().isEmpty()) {
                    updateReminderLayout(View.VISIBLE);
                    Date reminderDate = null;
                    try {
                        reminderDate = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").parse(currentNote.getReminderDateTime());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Date now = new Date();
                    if (now.after(reminderDate)) {
                        updateReminderDate("");
                        updateReminderLayout(View.GONE);
                        Helper.showMessage(NoteEdit.this, "Reminder Deleted", "Reminder has passed " +
                        "so it was deleted", MotionToast.TOAST_SUCCESS);
                    }
                }
                if (currentNote.isPin())
                    pinNoteIcon.setImageDrawable(getDrawable(R.drawable.pin_filled_icon));
//            }catch (Exception e){
//                Helper.showMessage(NoteEdit.this, "Reminder Deleted", "Reminder has passed " +
//                        "so it was deleted", MotionToast.TOAST_SUCCESS);
//            }
        }
        else {
            isNewNote = true;
            photosNote.setVisibility(View.GONE);
            pinNoteIcon.setVisibility(View.GONE);
            pinNoteButton.setVisibility(View.GONE);
            noteColor.setVisibility(View.GONE);
            expandMenu.setVisibility(View.GONE);
            sort.setVisibility(View.GONE);
            info.setVisibility(View.GONE);
            if(isCheckList)
                showCheckListLayout(false);
            title.requestFocus();
        }


        // if orientation changes, then it updates note data
        if (savedInstanceState != null) {
            title.setText(savedInstanceState.getString("title"));
            note.setHtml(savedInstanceState.getString("note"));
            currentPin = savedInstanceState.getBoolean("pin");
            isNewNote = savedInstanceState.getBoolean("newNote");
            isShowingPhotos = savedInstanceState.getBoolean("photos");
            isSearchingNotes = savedInstanceState.getBoolean("search");

            if(isSearchingNotes)
                showSearchBar();

            if (currentPin)
                pinNoteIcon.setImageDrawable(getDrawable(R.drawable.pin_filled_icon));

            if(isShowingPhotos) {
                note.clearFocus();
                showPhotos(View.VISIBLE);
                addCheckListItem.setVisibility(View.VISIBLE);
                if(!currentNote.isCheckList())
                    addCheckListItem.setImageDrawable(getDrawable(R.drawable.camera_icon));
            }

            if(isSearchingNotes){
                addCheckListItem.setVisibility(View.GONE);
                photosScrollView.setVisibility(View.GONE);
            }
        }

        search.setOnClickListener(v -> {
            if(note.getHtml().length()>0) {
                textSizeLayout.setVisibility(View.VISIBLE);
                showSearchBar();
            }
            else
                Helper.showMessage(this, "Empty", "Searching for something " +
                        "that does not exist is impossible", MotionToast.TOAST_WARNING);
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentWordIndex = 0;
                if(isSearchingNotes) {
                    textSizeLayout.setVisibility(View.VISIBLE);
                    if(!s.toString().isEmpty() && s.toString().length() > 1)
                        findText(s.toString().toLowerCase());
                    else {
                        wordOccurences = new ArrayList();
                        noteSearching.setText(Html.fromHtml(currentNote.getNote(), Html.FROM_HTML_MODE_COMPACT).toString());
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        searchClose.setOnClickListener(v -> {
            if(isSearchingNotes)
                hideSearchBar();
        });

        sort.setOnClickListener(v -> {
            if(currentNote.getChecklist().size() > 0) {
                FilterChecklistSheet filter = new FilterChecklistSheet(realm, currentNote);
                filter.show(this.getSupportFragmentManager(), filter.getTag());
            }
        });

        info.setOnClickListener(view -> {
            NoteInfoSheet noteInfoSheet = new NoteInfoSheet(currentNote, allNotePhotos, false);
            noteInfoSheet.show(getSupportFragmentManager(), noteInfoSheet.getTag());
        });

        category.setOnClickListener(v -> {
            realm.beginTransaction();
            allNotes.setBoolean("isSelected", false);
            currentNote.setSelected(true);
            realm.commitTransaction();
            Intent category = new Intent(NoteEdit.this, CategoryScreen.class);
            category.putExtra("editing_reg_note", true);
            startActivity(category);
        });

        title.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(currentNote!=null && !currentNote.getTitle().equals(s.toString())){
                        realm.beginTransaction();
                        currentNote.setTitle(s.toString());
                        currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
                        currentNote.setDateEditedMilli(Helper.dateToCalender(currentNote.getDateEdited()).getTimeInMillis());
                        realm.commitTransaction();
                        updateDateEdited();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) { }
            });

        if(currentNote!=null) {
            note.setOnTextChangeListener(text -> {
                oldTitle = currentNote.getTitle();
                oldNote = note.getHtml().toString();
                if (text.length() == 0 || currentNote.getNote().equals(text)) {
                    if (text.length() == 0)
                        saveChanges(text, 0);
                }
                else {
                    noteEdited = true;
                    if (currentNote != null && !currentNote.getNote().equals(text)) {
                        saveChanges(text, 1);
                    }
                }
            });
        }

        closeTextLayout.setOnClickListener(v -> {
            if(!isSearchingNotes) {
                hideButtons();
                if (currentNote.isCheckList())
                    addCheckListItem.setVisibility(View.VISIBLE);
            }
            else{
                hideSearchBar();
                isChangingTextSize = false;
                noteSearching.clearFocus();
                title.clearFocus();
            }
        });

        increaseTextSize.setOnTouchListener(new RepeatListener(500, 100, v -> {
            if(isSearchingNotes){
                if(currentWordIndex != -1) {
                    int nextIndex;
                    if (currentWordIndex == 0 && wordOccurences.size() != 0) {
                        noteSearching.requestFocus();
                        nextIndex = Integer.parseInt(wordOccurences.get(wordOccurences.size() - 1).toString());
                        noteSearching.setSelection(nextIndex + target.length());
                        currentWordIndex = wordOccurences.size() - 1;
                    } else if (currentWordIndex > 0 && wordOccurences.size() != 0) {
                        noteSearching.requestFocus();
                        nextIndex = Integer.parseInt(wordOccurences.get(currentWordIndex - 1).toString());
                        noteSearching.setSelection(nextIndex + target.length());
                        currentWordIndex -= 1;
                    }
                }
            }
            else if(isChangingTextSize)
                changeTextSize(true, currentNote.isCheckList());
        }));

        decreaseTextSize.setOnTouchListener(new RepeatListener(500, 100, v -> {
            if(isSearchingNotes){
                int nextIndex;
                if(currentWordIndex != -1) {
                    if (currentWordIndex == 0 && wordOccurences.size() != 0) {
                        noteSearching.requestFocus();
                        nextIndex = Integer.parseInt(wordOccurences.get(currentWordIndex).toString());
                        noteSearching.setSelection(nextIndex + target.length());
                        currentWordIndex = 1;
                    } else if (currentWordIndex == wordOccurences.size() - 1 && wordOccurences.size() != 0) {
                        noteSearching.requestFocus();
                        nextIndex = Integer.parseInt(wordOccurences.get(currentWordIndex).toString());
                        noteSearching.setSelection(nextIndex + target.length());
                        currentWordIndex = 0;
                    } else if (currentWordIndex <= wordOccurences.size() - 1) {
                        noteSearching.requestFocus();
                        nextIndex = Integer.parseInt(wordOccurences.get(currentWordIndex).toString());
                        noteSearching.setSelection(nextIndex + target.length());
                        currentWordIndex += 1;
                    }
                }
            }
            else if(isChangingTextSize)
                changeTextSize(false, currentNote.isCheckList());
        }));

        remindNote.setOnClickListener(v -> {
            cancelAlarm(currentNote.getNoteId());
            remindNote.setVisibility(View.GONE);
            showMessage("Reminder", "Reminder was deleted", false);
        });

        closeNote.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.stay, R.anim.right_out);
        });

        addCheckListItem.setOnClickListener(v -> {
            if (currentNote.isCheckList() && !isShowingPhotos)
                openNewItemDialog();
            else
                showCameraDialog();
        });

        photosNote.setOnClickListener(v -> {
            if (isShowingPhotos) {
                showPhotos(View.GONE);
                if(!isCheckList)
                    addCheckListItem.setVisibility(View.GONE);
                addCheckListItem.setImageDrawable(getDrawable(R.drawable.add_icon));
                if(isChangingTextSize)
                    textSizeLayout.setVisibility(View.VISIBLE);
            }
            else {
                if (allNotePhotos.size() == 0) {
                    Helper.showMessage(NoteEdit.this, "Photos Empty",
                            "Add photos to note", MotionToast.TOAST_WARNING);
                }
                photosAllExist();
                showPhotos(View.VISIBLE);
                addCheckListItem.setVisibility(View.VISIBLE);
                addCheckListItem.setImageDrawable(getDrawable(R.drawable.camera_icon));
                if(isChangingTextSize)
                    textSizeLayout.setVisibility(View.GONE);
            }
        });

        expandMenu.setOnClickListener(v ->
                openMenuDialog());

        noteColor.setOnClickListener(v -> {
            ColorSheet colorSheet = new ColorSheet(isLightMode);
            colorSheet.show(getSupportFragmentManager(), colorSheet.getTag());
        });

        pinNoteButton.setOnClickListener(v -> {
            // if editing a note, then it updates the status and pin image
            if (!isNewNote) {
                boolean pin = currentNote.isPin();

                if (pin) {
                    realm.beginTransaction();
                    currentNote.setPin(false);
                    currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
                    currentNote.setDateEditedMilli(Helper.dateToCalender(currentNote.getDateEdited()).getTimeInMillis());
                    realm.commitTransaction();
                    updateDateEdited();
                    pinNoteIcon.setImageDrawable(getDrawable(R.drawable.pin_icon));
                } else {
                    realm.beginTransaction();
                    currentNote.setPin(true);
                    currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
                    currentNote.setDateEditedMilli(Helper.dateToCalender(currentNote.getDateEdited()).getTimeInMillis());
                    realm.commitTransaction();
                    updateDateEdited();
                    pinNoteIcon.setImageDrawable(getDrawable(R.drawable.pin_filled_icon));
                }
            } else
                updatePin();

        });

        saveNote.setOnClickListener(v -> {
            // if a new note, then it adds it to database
            if (checkInput() && isNewNote) {
                addNote();
                finish();
                Intent refresh = new Intent(this, getClass());
                refresh.putExtra("id", currentNote.getNoteId()*-1);
                startActivity(refresh);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                checkListRecyclerview.requestFocus();
            }
        });
    }

    private void showSearchBar(){
        isSearchingNotes = true;
        closeNote.setVisibility(View.GONE);
        expandMenu.setVisibility(View.GONE);
        noteColor.setVisibility(View.GONE);
        photosNote.setVisibility(View.GONE);
        pinNoteButton.setVisibility(View.GONE);
        search.setVisibility(View.GONE);

        searchLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        if(isLightMode)
            searchLayout.setCardBackgroundColor(context.getColor(R.color.light_mode));
        else {
            searchEditText.setTextColor(context.getColor(R.color.ultra_white));
            searchLayout.setCardBackgroundColor(context.getColor(R.color.gray));
        }

        searchEditText.setVisibility(View.VISIBLE);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(0, 0, 100, 0);
        searchEditText.setLayoutParams(params);
        searchLayout.setPadding(100, 100, 100, 100);
        searchClose.setVisibility(View.VISIBLE);
        searchEditText.requestFocusFromTouch();

        if (searchEditText.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
        }

        if(isShowingPhotos){
           addCheckListItem.setVisibility(View.GONE);
           photosScrollView.setVisibility(View.GONE);
        }

        String textSize = Helper.getPreference(context, "size");
        if(textSize==null)
            textSize = "20";
        note.setVisibility(View.GONE);
        noteSearching.setTextSize(TypedValue.COMPLEX_UNIT_SP, Integer.parseInt(textSize));
        noteSearching.setVisibility(View.VISIBLE);
        noteSearching.setText(Html.fromHtml(note.getHtml(), Html.FROM_HTML_MODE_COMPACT).toString());

        showButtons();
    }

    private void hideSearchBar(){
        noteSearching.setText("");
        searchEditText.setText("");
        isSearchingNotes = false;
        closeNote.setVisibility(View.VISIBLE);
        expandMenu.setVisibility(View.VISIBLE);
        noteColor.setVisibility(View.VISIBLE);
        photosNote.setVisibility(View.VISIBLE);
        pinNoteButton.setVisibility(View.VISIBLE);
        search.setVisibility(View.VISIBLE);

        searchLayout.setCardBackgroundColor(context.getColor(R.color.light_gray));

        ViewGroup.MarginLayoutParams vlp = (ViewGroup.MarginLayoutParams) photosNote.getLayoutParams();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        searchLayout.setLayoutParams(params);
        params.setMargins(0, 0, vlp.rightMargin + 30, 0);
        searchLayout.setLayoutParams(params);
        searchEditText.setVisibility(View.GONE);
        searchEditText.setVisibility(View.GONE);
        searchClose.setVisibility(View.GONE);
        searchEditText.clearFocus();

        InputMethodManager inputManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);

        note.setHtml(currentNote.getNote());

        if(isShowingPhotos){
            addCheckListItem.setVisibility(View.VISIBLE);
            photosScrollView.setVisibility(View.VISIBLE);
        }
        note.setVisibility(View.VISIBLE);
        noteSearching.setVisibility(View.GONE);

        hideButtons();
    }

    private void showButtons(){
        new Handler().postDelayed(() -> {
            ObjectAnimator refreshAnimation = ObjectAnimator.ofFloat(textSizeLayout, "translationY",-1 * (textSizeLayout.getY() / 2));
            refreshAnimation.setDuration(500);
            refreshAnimation.start();
        }, 100);
    }

    private void hideButtons(){
        ObjectAnimator refreshAnimation = ObjectAnimator.ofFloat(textSizeLayout, "translationY", 0f);
        refreshAnimation.setDuration(500);
        refreshAnimation.start();
        new Handler().postDelayed(() -> {
            textSizeLayout.setVisibility(View.GONE);
        }, 500);
    }

    public void sortChecklist(){
        int currentSort = currentNote.getSort();

        RealmResults<CheckListItem> results = currentNote.getChecklist()
                .sort("positionInList", Sort.ASCENDING);

        if (currentSort == 1)
            results = results.sort("text", Sort.ASCENDING);
        else if (currentSort == 2)
            results = results.sort("text", Sort.DESCENDING);
        else if (currentSort == 4)
            results = results.sort("lastCheckedDate", Sort.ASCENDING);
        else if (currentSort == 3)
            results = results.sort("lastCheckedDate", Sort.DESCENDING);
        else if (currentSort == 5 || currentSort == 6)
            results = results.sort("positionInList");
        else {
            sortEnable = true;
            results = results.sort("positionInList");
        }

        populateChecklist(results);
    }

    public void updateColors(){
        noteColor.setCardBackgroundColor(currentNote.getBackgroundColor());
        category.setTextColor(currentNote.getBackgroundColor());
        title.setTextColor(currentNote.getTitleColor());
        note.setEditorFontColor(currentNote.getTextColor());
        if(checklistAdapter!=null && currentNote.isCheckList())
            checklistAdapter.notifyDataSetChanged();

        if(!Helper.isColorDark(currentNote.getBackgroundColor()))
            palleteIconColor.setColorFilter(context.getColor(R.color.black));
        else
            palleteIconColor.setColorFilter(context.getColor(R.color.white));
    }

    private void findText(String target){
        if(!currentNote.isCheckList()) {

            String originalString = Html.fromHtml(note.getHtml(),
                    Html.FROM_HTML_MODE_COMPACT).toString()
                    .replaceAll("<.*?>", "")
                    .replaceAll("\n", "<br>");


            if (originalString.toLowerCase().contains(target.toLowerCase())) {
                this.target = target;
                // Replace the specified text/word with formatted text/word
                String modifiedString =  originalString.replaceAll("(?i)" + target,
                        "<font color='#ff8000'>" + "$0" + "</font>");
                // Update the edit text
                noteSearching.setText(Html.fromHtml(modifiedString, Html.FROM_HTML_MODE_COMPACT));
                findAllTextIndexes(target);
                decreaseTextSize.setAlpha(new Float(1.0));
                increaseTextSize.setAlpha(new Float(1.0));
            }
            else {
                currentWordIndex = -1;
                noteSearching.setText(Html.fromHtml(currentNote.getNote(), Html.FROM_HTML_MODE_COMPACT));
                decreaseTextSize.setAlpha(new Float(0.5));
                increaseTextSize.setAlpha(new Float(0.5));
            }
        }
    }

    private void findAllTextIndexes(String target){
        wordOccurences = new ArrayList();
        target = target.toLowerCase();
        String text = Html.fromHtml(currentNote.getNote().toLowerCase(),Html.FROM_HTML_MODE_COMPACT).toString();

        int index = 0;
        int matchLength = target.length();
        while (index >= 0) {
            if(index==0)
                index = text.indexOf(target, index);
            else
                index = text.indexOf(target, index + matchLength);
            if(index!=-1)
                wordOccurences.add(index);
            if(index==0)
                index++;
        }
    }

    private void saveChanges(String text, int size){
        if(size ==0 ){
            text = "";
        }
        realm.beginTransaction();
        currentNote.setNote(text);
        currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
        currentNote.setDateEditedMilli(Helper.dateToCalender(currentNote.getDateEdited()).getTimeInMillis());
        realm.commitTransaction();
        updateSaveDateEdited();
        updateDateEdited();
    }

    private void showCheckListLayout(boolean status){
        note.setVisibility(View.GONE);
        //photosNote.setVisibility(View.GONE);
        addCheckListItem.setVisibility(View.GONE);

        if(status) {
            addCheckListItem.setVisibility(View.VISIBLE);
            checkListRecyclerview.setVisibility(View.VISIBLE);
            isListEmpty(checkListItems.size());
        }
    }

    private void changeTextSize(boolean increaseText, boolean isCheckList){
        String textSize = Helper.getPreference(context, "size");
        if(textSize==null)
            textSize = "20";
        int textSizeNumber = Integer.parseInt(textSize);
        int currentTextSize;
        if(isCheckList){
            currentTextSize = textSizeNumber;
            if(increaseText && currentTextSize+3<50)
                currentTextSize += 1;
            else if(!increaseText && currentTextSize-3>10)
                currentTextSize -= 1;
        }
        else{
            currentTextSize = textSizeNumber;
            if(increaseText && currentTextSize+3<100)
                currentTextSize += 2;
            else if(!increaseText && currentTextSize-3>10)
                currentTextSize -=2;
            note.setEditorFontSize(currentTextSize);
        }

        Helper.savePreference(context, String.valueOf(currentTextSize), "size");

        if(isCheckList)
            checklistAdapter.notifyDataSetChanged();
    }

    // adds note
    private void addNote() {
        // saves inputted note to database
        Note newNote = null;
        if (isCheckList)
            newNote = new Note(title.getText().toString(), "");
        else {
            try {
                newNote = new Note(title.getText().toString(), note.getHtml());
            }
            catch (Exception e) {
                newNote = new Note(title.getText().toString(), "");
            }
        }

        if(realm.where(Note.class).equalTo("noteId", newNote.getNoteId()).findAll().size()!=0){
            addNote();
        }
        else {
            newNote.setPin(currentPin);
            newNote.setIsCheckList(isCheckList);
            // add a random color to the note
            int[] randomColor = context.getResources().getIntArray(R.array.randomColor);
            int randomInt = (int) (Math.random() * (randomColor.length));
            newNote.setBackgroundColor(randomColor[randomInt]);
            newNote.setTitleColor(randomColor[randomInt]);
            newNote.setTextColor(getColor(R.color.ultra_white));
            // ensures that no note exists with the same id
            // insert data to database
            realm.beginTransaction();
            realm.insert(newNote);
            realm.commitTransaction();
            // update status of note
            isNewNote = false;
            currentNote = newNote;
            oldTitle = currentNote.getTitle();
            oldNote = currentNote.getNote();
            noteId = currentNote.getNoteId();
            // show user a message and hide the keyboard
            if (!isCheckList)
                Helper.showMessage(this, "Added", "Note is added", MotionToast.TOAST_SUCCESS);
            else
                Helper.showMessage(this, "Added", "Checklist is added", MotionToast.TOAST_SUCCESS);
            allNotePhotos = realm.where(Photo.class).equalTo("noteId", noteId).findAll();
            // updates note to be editable
            initializeLayout(null);
        }
    }

    // adds note
    public CheckListItem addCheckList(String itemText) {
        int initialPosition = -1;

        if(currentNote.getSort() == 6)
            initialPosition = currentNote.getChecklist().min("positionInList").intValue() - 1;
        else
            initialPosition = currentNote.getChecklist().size();

        Random rand = new Random();

        // insert data to database
        realm.beginTransaction();
        currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
        CheckListItem currentItem = new CheckListItem(itemText, false, currentNote.getNoteId(), initialPosition, rand.nextInt(100000) + 1, new SimpleDateFormat("E, MMM dd").format(Calendar.getInstance().getTime()));
        currentNote.getChecklist().add(currentItem);
        currentNote.setDateEditedMilli(Helper.dateToCalender(currentNote.getDateEdited()).getTimeInMillis());
        currentNote.setChecked(false);
        realm.commitTransaction();
        updateDateEdited();
        isListEmpty(currentNote.getChecklist().size());

        checklistAdapter.notifyDataSetChanged();

        if(title.getText().length()==0)
            title.requestFocus();
        else
            title.clearFocus();
        realm.beginTransaction();
        currentNote.setChecked(false);
        realm.commitTransaction();

        if(currentNote.isChecked())
            ((NoteEdit)context).title.setPaintFlags(((NoteEdit) context).title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        else
            ((NoteEdit)context).title.setPaintFlags(0);

        checkListRecyclerview.smoothScrollToPosition(initialPosition < 0 ? 0 : initialPosition);
        return currentItem;
    }

    public void addSubCheckList(CheckListItem checkListItem, String itemText) {
        // current Head of sub-list
        int initialPosition = checkListItem.getSubChecklist().size();
        // insert data to database
        realm.beginTransaction();
        checkListItem.getSubChecklist().add(new SubCheckListItem(itemText, false, checkListItem.getSubListId(), initialPosition, new SimpleDateFormat("E, MMM dd").format(Calendar.getInstance().getTime())));
        currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
        currentNote.setDateEditedMilli(Helper.dateToCalender(currentNote.getDateEdited()).getTimeInMillis());
        currentNote.setEnableSublist(true);
        realm.commitTransaction();
        updateDateEdited();

        checklistAdapter.notifyDataSetChanged();

        if(title.getText().length() == 0)
            title.requestFocus();
        else
            title.clearFocus();

        try {
            if (!currentNote.isEnableSublist() && checkListItem.getSubChecklist().size() > 0) {
                realm.beginTransaction();
                currentNote.setEnableSublist(true);
                realm.commitTransaction();
            }
        }catch (Exception e){}
    }

    // makes sure all the photos exist and have not been deleted from the device
    // if it has, it deletes it from the note and shows a message for the user
    private void photosAllExist() {
        int before = countPicsNotFound;

        for (int i = 0; i < allNotePhotos.size(); i++) {
            File file = new File(allNotePhotos.get(i).getPhotoLocation());
            if (!file.exists()) {
                countPicsNotFound++;
                realm.beginTransaction();
                allNotePhotos.get(i).deleteFromRealm();
                realm.commitTransaction();
            }
        }

        if(before == countPicsNotFound && before!=0){
            Helper.showMessage(this, countPicsNotFound + " not found", "Picture(s) corrupted " +
                    "or has been deleted from photos", MotionToast.TOAST_ERROR);
            countPicsNotFound = 0;
        }
        else if(countPicsNotFound!=currentNote.getPhotos().size() && countPicsNotFound!=0)
            photosAllExist();
    }

    private void openMenuDialog() {
        String archivedStatus = "Archive";
        String lockStatus = "Lock";
        if (currentNote.isArchived())
            archivedStatus += "d";
        if(currentNote.getPinNumber()!=0)
            lockStatus +="ed";


        noteMenu = new CustomPowerMenu.Builder<>(context, new IconMenuAdapter(false))
                .addItem(new IconPowerMenuItem(getDrawable(R.drawable.archive_icon), archivedStatus))
                .addItem(new IconPowerMenuItem(getDrawable(R.drawable.send_icon), "Send"))
                .addItem(new IconPowerMenuItem(getDrawable(R.drawable.reminder_icon), "Reminder"))
                .addItem(new IconPowerMenuItem(getDrawable(R.drawable.format_size_icon), "Text Size"))
                .addItem(new IconPowerMenuItem(getDrawable(R.drawable.lock_icon), lockStatus))
                .addItem(new IconPowerMenuItem(getDrawable(R.drawable.delete_icon), "Delete"))
                .setBackgroundColor(getColor(R.color.light_gray))
                .setOnMenuItemClickListener(onIconMenuItemClickListener)
                .setAnimation(MenuAnimation.SHOW_UP_CENTER)
                .setMenuRadius(15f)
                .setMenuShadow(10f)
                .build();

        if(currentNote.isCheckList()){
            noteMenu.addItem(3, new IconPowerMenuItem(getDrawable(R.drawable.check_icon), "Select All"));
            noteMenu.addItem(4, new IconPowerMenuItem(getDrawable(R.drawable.box_icon), "Deselect All"));
            noteMenu.addItem(5, new IconPowerMenuItem(getDrawable(R.drawable.delete_all_icon), "Delete All"));
            if( realm.where(User.class).findFirst().isEnableSublists()) {
                String sublistStatus = "";
                if(currentNote.isEnableSublist())
                    sublistStatus = sublistStatus + "Disable Sublist";
                else
                    sublistStatus = sublistStatus + "Enable Sublist";
                noteMenu.addItem(6, new IconPowerMenuItem(getDrawable(R.drawable.sublist_icon), sublistStatus));
            }
        }
        else
            noteMenu.addItem(5, new IconPowerMenuItem(getDrawable(R.drawable.info_icon), "Info"));


        noteMenu.showAsDropDown(expandMenu);
    }

    private final OnMenuItemClickListener<IconPowerMenuItem> onIconMenuItemClickListener = new OnMenuItemClickListener<IconPowerMenuItem>() {
        @Override
        public void onItemClick(int position, IconPowerMenuItem item) {
            if (position == 0) {
                updateArchivedStatus();
            } else if (item.getTitle().equals("Send")) {
                if (!isNewNote) {
                    sendProject();
                }
            } else if (item.getTitle().equals("Reminder")) {
                if(!currentNote.getReminderDateTime().isEmpty()){
                    Helper.showMessage(NoteEdit.this, "Alarm Exists",
                            "Delete Reminder to add another one", MotionToast.TOAST_ERROR);
                }
                else
                    showDatePickerDialog();
            }
            else if (item.getTitle().equals("Delete")) {
                dismissDialog = true;
                openDialog();
            }
            else if(item.getTitle().equals("Lock")){
                LockSheet lockSheet = new LockSheet();
                lockSheet.show(getSupportFragmentManager(), lockSheet.getTag());
            }
            else if(item.getTitle().equals("Locked")){
                unLockNote();
            }
            else if(item.getTitle().equals("Select All")){
                if(checkListItems.size()!=0) {
                    RealmHelper.selectAllChecklists(currentNote, true);
                    checkNote(true);
                    checklistAdapter.notifyDataSetChanged();
                    Helper.showMessage(NoteEdit.this, "Success", "All items " +
                            "have been selected", MotionToast.TOAST_SUCCESS);
                }
            }
            else if(item.getTitle().equals("Deselect All")){
                if(checkListItems.size()!=0) {
                    RealmHelper.selectAllChecklists(currentNote, false);
                    checkNote(false);
                    checklistAdapter.notifyDataSetChanged();
                    Helper.showMessage(NoteEdit.this, "Success", "All items " +
                            "have been unselected", MotionToast.TOAST_SUCCESS);
                }
            }
            else if(item.getTitle().equals("Delete All")){
                InfoSheet info = new InfoSheet(3, true);
                info.show(getSupportFragmentManager(), info.getTag());
            }
            else if(item.getTitle().contains("Text")){
                isChangingTextSize = true;
                textSizeLayout.setVisibility(View.VISIBLE);
                addCheckListItem.setVisibility(View.GONE);
                decreaseTextSize.setAlpha(new Float(1.0));
                increaseTextSize.setAlpha(new Float(1.0));
            }
            else if(item.getTitle().equals("Info")){
                NoteInfoSheet noteInfoSheet = new NoteInfoSheet(currentNote, allNotePhotos, false);
                noteInfoSheet.show(getSupportFragmentManager(), noteInfoSheet.getTag());
            }
            else if(position == 6)
                updateSublistEnabledStatus();

            noteMenu.dismiss();
        }
    };

    public void deleteChecklist(){
        RealmHelper.deleteChecklist(currentNote);
        checklistAdapter.notifyDataSetChanged();
        Helper.showMessage(NoteEdit.this, "Success", "All items deleted", MotionToast.TOAST_SUCCESS);
        isListEmpty(0, true);
        updateSaveDateEdited();
    }

    private void checkNote(boolean status){
        realm.beginTransaction();
        currentNote.setChecked(status);
        currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
        currentNote.setDateEditedMilli(Helper.dateToCalender(currentNote.getDateEdited()).getTimeInMillis());
        realm.commitTransaction();
        if(currentNote.isChecked())
            title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        else
            title.setPaintFlags(0);
        updateDateEdited();
    }

    public void lockNote(int pin, String securityWord, boolean fingerprint){
        realm.beginTransaction();
        currentNote.setPinNumber(pin);
        currentNote.setSecurityWord(securityWord);
        currentNote.setFingerprint(fingerprint);
        currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
        currentNote.setDateEditedMilli(Helper.dateToCalender(currentNote.getDateEdited()).getTimeInMillis());
        realm.commitTransaction();
        updateDateEdited();
        Helper.showMessage(this, "Note Locked", "Note has been " +
                "locked" , MotionToast.TOAST_SUCCESS);

        if(currentNote.getReminderDateTime().length() > 1){
            InfoSheet info = new InfoSheet(-1);
            info.show(getSupportFragmentManager(), info.getTag());
        }
    }

    public void unLockNote(){
        realm.beginTransaction();
        currentNote.setPinNumber(0);
        currentNote.setSecurityWord("");
        currentNote.setFingerprint(false);
        currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
        currentNote.setDateEditedMilli(Helper.dateToCalender(currentNote.getDateEdited()).getTimeInMillis());
        realm.commitTransaction();
        updateDateEdited();
        Helper.showMessage(this, "Note un-Lock", "Note has been " +
                "un-locked" , MotionToast.TOAST_SUCCESS);
    }

    // updated pinned status
    private void updatePin() {
        if (currentPin)
            pinNoteIcon.setImageDrawable(getDrawable(R.drawable.pin_icon));
        else
            pinNoteIcon.setImageDrawable(getDrawable(R.drawable.pin_filled_icon));
        currentPin = !currentPin;
    }

    private void updateReminderLayout(int visibility){
        remindNote.setVisibility(visibility);
        remindNoteDate.setVisibility(visibility);
        if(!currentNote.getReminderDateTime().isEmpty()) {
            int formatDate24Hours = Integer.parseInt(currentNote.getReminderDateTime().split(" ")[1].split(":")[0]);
            int formatDate12Hours = formatDate24Hours % 12;
            String formatted = currentNote.getReminderDateTime().split(" ")[0] + " " + formatDate12Hours +
                    ":" + currentNote.getReminderDateTime().split(" ")[1].split(":")[1] +
                    ":" + currentNote.getReminderDateTime().split(" ")[1].split(":")[2] + " " + ((formatDate24Hours > 12) ? "PM" : "AM");
            remindNoteDate.setText(formatted);
        }
        else
            remindNoteDate.setVisibility(View.GONE);
    }

    private void showPhotos(int visibility) {
        if (visibility == View.VISIBLE) {
            isShowingPhotos = true;
            photosNote.setCardBackgroundColor(getColor(R.color.blue));
        } else {
            isShowingPhotos = false;
            photosNote.setCardBackgroundColor(getColor(R.color.light_gray));
        }

        photosScrollView.setVisibility(visibility);
        photosScrollView.requestFocus();
    }

    private void populatePhotos() {
        photosScrollView.setVisibility(View.GONE);
        scrollAdapter = new photos_recyclerview(allNotePhotos, NoteEdit.this, context, true);
        photosScrollView.setAdapter(scrollAdapter);
    }

    private void populateChecklist(RealmResults<CheckListItem> currentList) {
        int span = 1;
        if(Helper.isTablet(context))
            span = 2;
        GridLayoutManager layout = new GridLayoutManager(context, span);
        checkListRecyclerview.setLayoutManager(layout);
        checklistAdapter = new checklist_recyclerview(realm.where(User.class).findFirst(),
                currentList, currentNote, realm, this);
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
        timer.setThemeDark(true);
        timer.setAccentColor(getColor(R.color.light_gray_2));
        timer.setOkColor(getColor(R.color.blue));
        timer.setCancelColor(getColor(R.color.light_gray_2));
        timer.show(getSupportFragmentManager(), "Datepickerdialog");
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        // Get Current Time
        dateSelected.set(Calendar.HOUR_OF_DAY, hourOfDay);
        dateSelected.set(Calendar.MINUTE, minute);
        dateSelected.set(Calendar.SECOND, 0);
        currentDateTimeSelected += hourOfDay+ ":" + ((minute<10)? ("0" + minute): minute) + ":00";
        startAlarm(dateSelected);
    }

    public void showDatePickerDialog() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                this,
                now.get(Calendar.YEAR), // Initial year selection
                now.get(Calendar.MONTH), // Initial month selection
                now.get(Calendar.DAY_OF_MONTH) // Inital day selection
        );
        datePickerDialog.setThemeDark(true);
        datePickerDialog.setAccentColor(getColor(R.color.light_gray_2));
        datePickerDialog.setOkColor(getColor(R.color.blue));
        datePickerDialog.setCancelColor(getColor(R.color.light_gray_2));
        datePickerDialog.show(getSupportFragmentManager(), "Datepickerdialog");
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int month, int day) {
        dateSelected = Calendar.getInstance();
        dateSelected.set(Calendar.YEAR, year);
        dateSelected.set(Calendar.MONTH, ++month);
        dateSelected.set(Calendar.DAY_OF_MONTH, day);
        currentDateTimeSelected = month + "-" + day + "-" + year + " ";
        timeDialog();
    }

    private void startAlarm(Calendar c) {
        int month = c.get(Calendar.MONTH)-1;
        c.set(Calendar.MONTH, month);

        if (c.after(Calendar.getInstance())) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, AlertReceiver.class);
            intent.putExtra("id", currentNote.getNoteId());
            intent.putExtra("title", currentNote.getTitle().replace("\n", " "));
            intent.putExtra("pin", currentNote.getPinNumber());
            intent.putExtra("securityWord", currentNote.getSecurityWord());
            intent.putExtra("fingerprint", currentNote.isFingerprint());
            intent.putExtra("checklist", currentNote.isCheckList());
            updateReminderDate(currentDateTimeSelected);
            updateReminderLayout(View.VISIBLE);
            Helper.showMessage(this, "Reminder set", "Will Remind you " +
                    "in " + Helper.getTimeDifference(c, true), MotionToast.TOAST_SUCCESS);
            PendingIntent pendingIntent;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if(alarmManager.canScheduleExactAlarms()){
                }
                else {
                    Helper.showMessage(this, "Alarm Not Set", "Please Enable " +
                            "Alarms & Reminders ", MotionToast.TOAST_ERROR);
                    Intent intent2 = new Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(intent2);
                }
                pendingIntent = PendingIntent.getBroadcast(this, noteId, intent,
                        PendingIntent.FLAG_IMMUTABLE);
            }
            else
                pendingIntent = PendingIntent.getBroadcast(this, noteId, intent,
                        PendingIntent.FLAG_ONE_SHOT);

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
        }
        else
            showMessage("Reminder not set", "Reminder cannot be in the past", true);
    }

    private void updateReminderDate(String date){
        realm.beginTransaction();
        currentNote.setReminderDateTime(date);
        currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
        currentNote.setDateEditedMilli(Helper.dateToCalender(currentNote.getDateEdited()).getTimeInMillis());
        realm.commitTransaction();
        updateDateEdited();
    }

    private void cancelAlarm(int noteId) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        updateReminderDate("");
        PendingIntent pendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
            pendingIntent = PendingIntent.getBroadcast(this, noteId, intent,
                    PendingIntent.FLAG_IMMUTABLE);
        else
            pendingIntent = PendingIntent.getBroadcast(this, noteId, intent,
                    PendingIntent.FLAG_ONE_SHOT);
        alarmManager.cancel(pendingIntent);
    }

    private void updateArchivedStatus() {
        boolean isNoteArchived = currentNote.isArchived();

        if (isNoteArchived) {
            realm.beginTransaction();
            currentNote.setArchived(false);
            currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
            currentNote.setDateEditedMilli(Helper.dateToCalender(currentNote.getDateEdited()).getTimeInMillis());
            realm.commitTransaction();
            updateDateEdited();
            Helper.showMessage(this, "Archived Status", "Note has been " +
                    "un-archived", MotionToast.TOAST_SUCCESS);
        } else {
            realm.beginTransaction();
            currentNote.setArchived(true);
            currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
            currentNote.setDateEditedMilli(Helper.dateToCalender(currentNote.getDateEdited()).getTimeInMillis());
            realm.commitTransaction();
            updateDateEdited();
            Helper.showMessage(this, "Archived Status", "Note has been " +
                    "archived and put in the archived folder", MotionToast.TOAST_SUCCESS);
        }
    }

    private void updateSublistEnabledStatus() {
        boolean isSublistEnabled = currentNote.isEnableSublist();

        if (isSublistEnabled) {
            realm.beginTransaction();
            currentNote.setEnableSublist(false);
            currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
            currentNote.setDateEditedMilli(Helper.dateToCalender(currentNote.getDateEdited()).getTimeInMillis());
            realm.commitTransaction();
            updateDateEdited();
            Helper.showMessage(this, "Sublist Status", "It has has been " +
                    "disabled", MotionToast.TOAST_SUCCESS);
        } else {
            realm.beginTransaction();
            currentNote.setEnableSublist(true);
            currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
            currentNote.setDateEditedMilli(Helper.dateToCalender(currentNote.getDateEdited()).getTimeInMillis());
            realm.commitTransaction();
            updateDateEdited();
            Helper.showMessage(this, "Sublist Status", "It has been " +
                    "enabled", MotionToast.TOAST_SUCCESS);
        }
        checklistAdapter.notifyDataSetChanged();
    }

    // determines if there were changes to the note
    private boolean noteChanged() {
        return !title.getText().toString().equals(oldTitle) || !note.getHtml().toString().equals(oldNote);
    }

    // makes sure title is not empty
    private boolean checkInput() {
        if (title.getText().toString().isEmpty()) {
            Helper.showMessage(this, "fill in empty fields", "Title is empty", MotionToast.TOAST_ERROR);
            return false;
        }
        return true;
    }

    private void sendProject() {
        final Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("text/plain");

        // attaching to intent to send folders
        ArrayList<Uri> uris = new ArrayList<>();


        // only attaches to email if there are project photos
        for (int i = 0; i < allNotePhotos.size(); i++) {
            File file = new File(allNotePhotos.get(i).getPhotoLocation());
            if(file.exists()) {
                uris.add(FileProvider.getUriForFile(
                        this,
                        "com.akapps.dailynote.fileprovider",
                        file));
            }
        }

        String note = "Note: " + "\n" + Html.fromHtml(currentNote.getNote(),
                Html.FROM_HTML_MODE_COMPACT).toString().replaceAll("(\\s{2,})", " ");
        if (currentNote.getNote().isEmpty() && !currentNote.isCheckList())
            note += "--note is empty--";
        else if(currentNote.isCheckList())
            note = "";

        StringBuilder checklist = new StringBuilder("Checklist: \n");
        if(checkListItems!=null && checkListItems.size()!=0) {
            for (int i = 0; i < checkListItems.size(); i++) {
                // adds checklist text and it's current status (checked or unchecked)
                checklist.append(checkListItems.get(i).getText())
                        .append("  " + (checkListItems.get(i).isChecked() ? "✔" : "✖")).append("\n");
                if(checkListItems.get(i).getSubChecklist().size() > 0) {
                    // if checklist item has subchecklists, then print them out and their status
                    for (int j = 0; j < checkListItems.get(i).getSubChecklist().size(); j++) {
                        String currentSubList = checkListItems.get(i).getSubChecklist().get(j).getText();
                        boolean isSublistChecked = checkListItems.get(i).getSubChecklist().get(j).isChecked();
                        checklist.append("     " + currentSubList).append("  " + (isSublistChecked ? "✔" : "✖")).append("\n");
                    }
                }
                checklist.append("\n");
            }
        }
        else if(currentNote.isCheckList())
            checklist.append("--checklist is empty--");
        else
            checklist = new StringBuilder("");

        // creates a formatted body of project data
        String emailBody = "Title: " + currentNote.getTitle() + "\n\n"
                + "Date Created: " + currentNote.getDateCreated().replace("\n", " ") + "\n\n"
                + "Date Edited: " + currentNote.getDateEdited().replace("\n", " ") + "\n\n"
                + note
                + checklist;

        // adds email subject and email body to intent
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Sending Note: " + currentNote.getTitle());

        emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);

        if(uris.size() > 0)
            emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

        startActivity(Intent.createChooser(emailIntent, "Share Note"));
    }

    private void showCameraDialog(){
        ImagePicker.with(this)
                .maxResultSize(814, 814)
                .compress(1024)
                .saveDir(getExternalFilesDir("/Documents"))
                .start(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 0) {
            Uri uri = data.getData();

            Photo currentPhoto;
            currentPhoto = new Photo(noteId, uri.getPath());
            realm.beginTransaction();
            realm.insert(currentPhoto);
            currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
            currentNote.setDateEditedMilli(Helper.dateToCalender(currentNote.getDateEdited()).getTimeInMillis());
            realm.commitTransaction();
            updateDateEdited();

            scrollAdapter.notifyDataSetChanged();
            showPhotos(View.VISIBLE);
        }
        else if (resultCode== ImagePicker.RESULT_ERROR) {}
    }

    private void openNewItemDialog(){
        ChecklistItemSheet checklistItemSheet = new ChecklistItemSheet();
        checklistItemSheet.show(getSupportFragmentManager(), checklistItemSheet.getTag());
    }

    private void openDialog() {
        InfoSheet info = new InfoSheet(currentNote.isTrash() ? -3 : 3, false);
        info.show(getSupportFragmentManager(), info.getTag());
    }

    public void deleteNote(){
        if(handler!=null)
            handler.removeCallbacksAndMessages(null);
        if(currentNote.isTrash()){
            String noteTitle = currentNote.getTitle();
            RealmHelper.deleteNote(currentNote.getNoteId());
            Helper.showMessage(NoteEdit.this, "Deleted", noteTitle, MotionToast.TOAST_SUCCESS);
        }
        else {
            realm.beginTransaction();
            currentNote.setTrash(true);
            realm.commitTransaction();
            Helper.showMessage(NoteEdit.this, "Sent to trash", currentNote.getTitle(), MotionToast.TOAST_SUCCESS);
        }
        finish();
    }

    public void deleteSubLists(RealmList<CheckListItem> list){
        for (int i=0; i<list.size(); i++){
            realm.beginTransaction();
            list.get(i).getSubChecklist().deleteAllFromRealm();
            realm.commitTransaction();
        }
    }

    public void isListEmpty(int size){
        Helper.isListEmpty(context, size, empty_Layout, empty_title, subtitle, subSubTitle, false, true, false);
    }

    public void isListEmpty(int size, boolean isChecklistAdded){
        Helper.isListEmpty(context, size, empty_Layout, empty_title, subtitle, subSubTitle, false, true, isChecklistAdded);
    }

    public void updateDateEdited(){
        if(currentNote.isCheckList())
            isListEmpty(currentNote.getChecklist().size());
        handler = new Handler();

        date.setVisibility(View.VISIBLE);

        handler.postDelayed(new Runnable() {
            public void run() {
                if (!realm.isClosed()) {
                    if (Helper.getTimeDifference(Helper.dateToCalender(currentNote.getDateEdited().replace("\n", " ")), false).length() > 0) {
                        date.setText(Html.fromHtml("<font color='#FFFFFF'>Last Edit:</font> " + currentNote.getDateEdited().replace("\n", " ") +
                                "<br>" + Helper.getTimeDifference(Helper.dateToCalender(currentNote.getDateEdited().replace("\n", " ")), false) + " ago", Html.FROM_HTML_MODE_COMPACT));
                    } else {
                        date.setText(currentNote.getDateEdited().replace("\n", " ") + "\n  ");
                    }
                    handler.postDelayed(this, 1000);
                }
            }
        }, 0);
    }

    private void updateSaveDateEdited(){
        realm.beginTransaction();
        currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
        currentNote.setDateEditedMilli(Helper.dateToCalender(currentNote.getDateEdited()).getTimeInMillis());
        realm.commitTransaction();
        updateDateEdited();
    }

    private void showMessage(String title, String message, boolean error){
        if(error) {
            Helper.showMessage(NoteEdit.this, title,
                    message, MotionToast.TOAST_ERROR);
        }
        else{
            Helper.showMessage(NoteEdit.this, title,
                    message, MotionToast.TOAST_SUCCESS);
        }
    }

    private void initializeEditor(){
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
                note.setTextColor(context.getColor(R.color.ultra_white));
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

    public void removeFormatting(){
        updateSaveDateEdited();
        String removedFormat = Html.fromHtml(currentNote.getNote(), Html.FROM_HTML_MODE_COMPACT).toString();
        realm.beginTransaction();
        currentNote.setNote(removedFormat);
        realm.commitTransaction();
        note.setHtml(removedFormat);
        note.focusEditor();
        Helper.showMessage(this, "Removed", "Formatting has been removed",
                MotionToast.TOAST_SUCCESS);
    }
}