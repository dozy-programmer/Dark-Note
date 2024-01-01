package com.akapps.dailynote.activity;

import static com.akapps.dailynote.classes.helpers.UiHelper.getColorFromTheme;
import static com.akapps.dailynote.classes.helpers.UiHelper.getThemeStyle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.data.Folder;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.Photo;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmHelper;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.akapps.dailynote.classes.other.FolderItemSheet;
import com.akapps.dailynote.classes.other.InfoSheet;
import com.akapps.dailynote.recyclerview.categories_recyclerview;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import io.realm.RealmResults;
import www.sanju.motiontoast.MotionToast;

public class CategoryScreen extends AppCompatActivity {

    // activity
    private Context context;

    // Toolbar
    private Toolbar toolbar;
    private ImageView close;
    private TextView title;

    // layout
    private TextView showAllNotes;
    private TextView noCategory;
    private TextView unselectCategories;
    private TextView trash;
    private TextView archived;
    private TextView unpinned;
    private MaterialButton pinned;
    private MaterialButton locked;
    private MaterialButton reminder;
    private MaterialButton photos;
    private RecyclerView customCategories;
    public RecyclerView.Adapter categoriesAdapter;
    private FloatingActionButton addCategory;
    private LinearLayout selections;

    // dialog
    private ImageView info;
    private ImageView edit;

    // on-device database
    private RealmResults<Note> allNotes;
    private RealmResults<Folder> allCategories;
    private RealmResults<Note> allSelectedNotes;
    private RealmResults<Note> uncategorizedNotes;
    private RealmResults<Note> archivedAllNotes;
    private RealmResults<Note> pinnedAllNotes;
    private RealmResults<Note> trashAllNotes;
    private RealmResults<Note> lockedAllNotes;
    private RealmResults<Note> reminderAllNotes;

    // activity data
    private boolean editingRegularNote;
    private boolean multiSelect;
    private boolean isNotesSelected;
    public boolean isEditing;
    private String titleBefore;
    private User.Mode screenMode;

    // category empty view
    private TextView showEmptyMessage;
    private LottieAnimationView emptyAnimation;
    private ImageView emptyNoAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(getThemeStyle(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_screen);

        context = this;
        screenMode = RealmHelper.getUser(context, "in space").getScreenMode();

        // if orientation changes, retrieve these values
        if (savedInstanceState != null) {
            editingRegularNote = savedInstanceState.getBoolean("editing_reg_note");
            multiSelect = savedInstanceState.getBoolean("multi_select");
        } else {
            editingRegularNote = getIntent().getBooleanExtra("editing_reg_note", false);
            multiSelect = getIntent().getBooleanExtra("multi_select", false);
        }

        allCategories = RealmSingleton.getInstance(context).where(Folder.class).sort("positionInList").findAll();

        allNotes = RealmSingleton.getInstance(context).where(Note.class).findAll();
        archivedAllNotes = RealmSingleton.getInstance(context).where(Note.class)
                .equalTo("trash", false)
                .equalTo("archived", true)
                .findAll();
        pinnedAllNotes = RealmSingleton.getInstance(context).where(Note.class)
                .equalTo("pin", true)
                .findAll();
        allSelectedNotes = allNotes.where().equalTo("isSelected", true).findAll();
        isNotesSelected = allSelectedNotes.size() > 0;
        uncategorizedNotes = allNotes.where()
                .equalTo("archived", false)
                .equalTo("pin", false)
                .equalTo("trash", false)
                .equalTo("category", "none").findAll();
        trashAllNotes = allNotes.where()
                .equalTo("trash", true).findAll();
        lockedAllNotes = allNotes.where()
                .greaterThan("pinNumber", 0).findAll();
        reminderAllNotes = allNotes.where()
                .isNotEmpty("reminderDateTime").findAll();

        initializeLayout();
        UiHelper.setStatusBarColor(this);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (editingRegularNote)
                    unSelectAllNotes();
                closeActivity(0);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("editing_reg_note", editingRegularNote);
        savedInstanceState.putBoolean("multi_select", multiSelect);
    }

    @Override
    protected void onDestroy() {
        RealmSingleton.closeRealmInstance("CategoryScreen onDestroy");
        super.onDestroy();
    }

    @SuppressLint("SetTextI18n")
    private void initializeLayout() {
        toolbar = findViewById(R.id.toolbar);
        title = findViewById(R.id.title);
        close = findViewById(R.id.close_activity);
        showAllNotes = findViewById(R.id.all_notes);
        noCategory = findViewById(R.id.no_category);
        addCategory = findViewById(R.id.add_category);
        customCategories = findViewById(R.id.custom_categories);
        unselectCategories = findViewById(R.id.unselect_categories);
        trash = findViewById(R.id.trash);
        archived = findViewById(R.id.archived);
        pinned = findViewById(R.id.pinned);
        unpinned = findViewById(R.id.un_pinned);
        locked = findViewById(R.id.locked);
        reminder = findViewById(R.id.reminder);
        photos = findViewById(R.id.photos);
        info = findViewById(R.id.info);
        edit = findViewById(R.id.edit);
        showEmptyMessage = findViewById(R.id.empty_category);
        emptyAnimation = findViewById(R.id.empty_category_animation);
        emptyNoAnimation = findViewById(R.id.empty_category_no_animation);
        selections = findViewById(R.id.top_layout);

        // toolbar
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        // text
        archived.setText(archived.getText());
        trash.setText(trash.getText());
        // buttons
        locked.setText(locked.getText());
        reminder.setText(reminder.getText());
        photos.setText(photos.getText());
        pinned.setText(pinned.getText());

        if (editingRegularNote) {
            allSelectedNotes = RealmSingleton.getInstance(context).where(Note.class).equalTo("isSelected", true).findAll();
            title.setText("Current:\n" + allSelectedNotes.get(0).getCategory());
            if (allSelectedNotes.get(0).getCategory().equals("none"))
                unselectCategories.setVisibility(View.GONE);

            showAllNotes.setVisibility(View.GONE);
            noCategory.setVisibility(View.GONE);
            trash.setVisibility(View.GONE);
            archived.setVisibility(View.GONE);
            pinned.setVisibility(View.GONE);
            locked.setVisibility(View.GONE);
            reminder.setVisibility(View.GONE);
            photos.setVisibility(View.GONE);
        } else {
            int allNotesSize = 0;
            int noCategoryNotesSize = 0;
            int allSelected = 0;

            allNotesSize += allNotes.size();
            noCategoryNotesSize += uncategorizedNotes.size();
            allSelected += allSelectedNotes.size();

            showAllNotes.setText("All Notes");

            noCategory.setText("Uncategorized notes");

            if (allSelected == 0)
                unselectCategories.setVisibility(View.GONE);
            else
                title.setText(allSelected + " Selected");

            // make padding shorter since there isn't a folder opened
            title.setPadding(0, 20, 0, 0);

            if (multiSelect) {
                int textColor = UiHelper.getColorFromTheme(this, R.attr.primaryTextColor);
                showAllNotes.setVisibility(View.VISIBLE);
                noCategory.setVisibility(View.VISIBLE);
                trash.setVisibility(View.GONE);
                archived.setVisibility(View.VISIBLE);
                unpinned.setVisibility(View.VISIBLE);
                showAllNotes.setText("Archive");
                noCategory.setText("Un-Archive");
                archived.setText("Pin");
                archived.setTextColor(textColor);
                showAllNotes.setTextColor(textColor);
                noCategory.setTextColor(textColor);
                unpinned.setTextColor(textColor);

                archived.setCompoundDrawables(null, null, null, null);

                findViewById(R.id.pinned_layout).setVisibility(View.GONE);
                findViewById(R.id.locked_layout).setVisibility(View.GONE);
            } else {
                int backgroundColor = getColorFromTheme(this, R.attr.primaryStrokeColor);
                int noBackgroundColor = getColorFromTheme(this, R.attr.secondaryBackgroundColor);
                int textColor = getColorFromTheme(this, R.attr.primaryTextColor);
                Helper.addNotificationNumber(this, noCategory, noCategoryNotesSize, 0,
                        true, backgroundColor, textColor);
                Helper.addNotificationNumber(this, showAllNotes, allNotesSize, 0,
                        true, backgroundColor, textColor);
                Helper.addNotificationNumber(this, archived, archivedAllNotes.size(), 0,
                        true, backgroundColor, textColor);
                Helper.addNotificationNumber(this, trash, trashAllNotes.size(), 0,
                        true, backgroundColor, textColor);
                // buttons
                Helper.addNotificationNumber(this, locked, lockedAllNotes.size(), 75,
                        true, noBackgroundColor, textColor);
                Helper.addNotificationNumber(this, reminder, reminderAllNotes.size(), 75,
                        true, noBackgroundColor, textColor);
                Helper.addNotificationNumber(this, pinned, pinnedAllNotes.size(), 75,
                        true, noBackgroundColor, textColor);
                Helper.addNotificationNumber(this, photos, getNumberOfNotesWithPhotos(allNotes), 75,
                        true, noBackgroundColor, textColor);
            }
        }

        // recyclerview
        customCategories.setHasFixedSize(true);
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.ACTION_STATE_DRAG | ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END, 0) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder dragged, @NonNull RecyclerView.ViewHolder target) {
                int started = dragged.getAbsoluteAdapterPosition();
                int ended = target.getAbsoluteAdapterPosition();

                Folder item = allCategories.get(started);
                Folder item2 = allCategories.get(ended);
                RealmSingleton.getInstance(context).beginTransaction();
                if (Math.abs(ended - started) > 1) {
                    if (started < ended) {
                        Folder item3 = allCategories.get(started + 1);
                        int middlePosition = item3.getPositionInList();
                        item.setPositionInList(ended);
                        item3.setPositionInList(started);
                        item2.setPositionInList(middlePosition);
                    } else {
                        Folder item3 = allCategories.get(started - 1);
                        int middlePosition = item3.getPositionInList();
                        item.setPositionInList(ended);
                        item3.setPositionInList(started);
                        item2.setPositionInList(middlePosition);
                    }
                } else {
                    item.setPositionInList(ended);
                    item2.setPositionInList(started);
                }
                RealmSingleton.getInstance(context).commitTransaction();
                categoriesAdapter.notifyItemMoved(started, ended);
                return false;
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                if (!recyclerView.isComputingLayout()) {
                    categoriesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }

        });

        helper.attachToRecyclerView(customCategories);
        if (Helper.isTablet(context))
            customCategories.setLayoutManager(new GridLayoutManager(context, 3));
        else
            customCategories.setLayoutManager(new GridLayoutManager(context, 2));
        populateCategories();
        checkEmpty();

        categoriesAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onChanged() {
                super.onChanged();
                checkEmpty();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                checkEmpty();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                checkEmpty();
            }
        });

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isEditing)
                    showInfoDialog();
                else {
                    isEditing = false;
                    title.setText(titleBefore);
                    info.setImageDrawable(getDrawable(R.drawable.info_icon));
                    edit.setVisibility(View.VISIBLE);
                    selections.setVisibility(View.VISIBLE);
                }
            }
        });

        edit.setOnClickListener(v -> {
            isEditing = true;
            selections.setVisibility(View.GONE);
            edit.setVisibility(View.GONE);
            titleBefore = title.getText().toString();
            title.setText("Editing");
            Helper.showMessage(this, "Editing", "Click on item to edit", MotionToast.TOAST_SUCCESS);
            info.setImageDrawable(getDrawable(R.drawable.cancel));
        });

        addCategory.setOnClickListener(v -> openNewItemDialog());

        showAllNotes.setOnClickListener(v -> {
            if (multiSelect) {
                RealmSingleton.getInstance(context).beginTransaction();
                allSelectedNotes.setBoolean("archived", true);
                RealmSingleton.getInstance(context).commitTransaction();
                closeActivity(-8);
            } else if (!isNotesSelected)
                closeActivity(-2);
            else
                showErrorMessage();
        });

        noCategory.setOnClickListener(v -> {
            if (multiSelect) {
                RealmSingleton.getInstance(context).beginTransaction();
                allSelectedNotes.setBoolean("archived", false);
                RealmSingleton.getInstance(context).commitTransaction();
                closeActivity(-9);
            } else if (!isNotesSelected)
                closeActivity(-3);
            else
                showErrorMessage();
        });

        trash.setOnClickListener(v -> {
            if (!isNotesSelected && trashAllNotes.size() > 0)
                closeActivity(-5);
            else if (trashAllNotes.size() == 0)
                showEmptyMessage();
            else
                showErrorMessage();
        });

        archived.setOnClickListener(v -> {
            if (multiSelect) {
                RealmSingleton.getInstance(context).beginTransaction();
                allSelectedNotes.setBoolean("pin", true);
                RealmSingleton.getInstance(context).commitTransaction();
                closeActivity(-13);
            }
            if (!isNotesSelected && archivedAllNotes.size() > 0)
                closeActivity(-10);
            else if (archivedAllNotes.size() == 0)
                showEmptyMessage();
            else
                showErrorMessage();
        });

        pinned.setOnClickListener(v -> {
            if (multiSelect) {
                RealmSingleton.getInstance(context).beginTransaction();
                allSelectedNotes.setBoolean("pin", false);
                RealmSingleton.getInstance(context).commitTransaction();
                closeActivity(-12);
            }
            if (!isNotesSelected && pinnedAllNotes.size() > 0)
                closeActivity(-11);
            else if (pinnedAllNotes.size() == 0)
                showEmptyMessage();
            else
                showErrorMessage();
        });

        unpinned.setOnClickListener(v -> {
            RealmSingleton.getInstance(context).beginTransaction();
            allSelectedNotes.setBoolean("pin", false);
            RealmSingleton.getInstance(context).commitTransaction();
            closeActivity(-12);
        });

        locked.setOnClickListener(v -> {
            if (!isNotesSelected && lockedAllNotes.size() > 0)
                closeActivity(-14);
            else if (lockedAllNotes.size() == 0)
                showEmptyMessage();
            else
                showErrorMessage();
        });

        reminder.setOnClickListener(v -> {
            if (!isNotesSelected && reminderAllNotes.size() > 0)
                closeActivity(-15);
            else if (reminderAllNotes.size() == 0)
                showEmptyMessage();
            else
                showErrorMessage();
        });

        photos.setOnClickListener(v -> {
            int notesWithPhotos = getNumberOfNotesWithPhotos(allNotes);
            if (!isNotesSelected && notesWithPhotos > 0)
                closeActivity(-16);
            else if (notesWithPhotos == 0)
                showEmptyMessage();
            else
                showErrorMessage();
        });

        unselectCategories.setOnClickListener(v -> {
            RealmSingleton.getInstance(context).beginTransaction();
            allSelectedNotes.setString("category", "none");
            RealmSingleton.getInstance(context).commitTransaction();
            unSelectAllNotes();
            closeActivity(-4);
        });

        close.setOnClickListener(v -> {
            if (editingRegularNote)
                unSelectAllNotes();
            closeActivity(0);
        });
    }

    private void populateCategories() {
        categoriesAdapter = new categories_recyclerview(allCategories, CategoryScreen.this, context);
        customCategories.setAdapter(categoriesAdapter);
    }

    private void showErrorMessage() {
        Helper.showMessage(this, "Select Folder", "You need to select a folder" +
                " to put all the notes you selected", MotionToast.TOAST_ERROR);
    }

    private void showEmptyMessage() {
        Helper.showMessage(this, "Empty", "Cannot open empty folder", MotionToast.TOAST_ERROR);
    }

    private void checkEmpty() {
        showEmptyMessage.setVisibility(categoriesAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        if(RealmHelper.getUser(context, "in space").isDisableAnimation())
            emptyNoAnimation.setVisibility(categoriesAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        else
            emptyAnimation.setVisibility(categoriesAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void unSelectAllNotes() {
        RealmSingleton.getInstance(context).beginTransaction();
        allSelectedNotes.setBoolean("isSelected", false);
        RealmSingleton.getInstance(context).commitTransaction();
    }

    private void closeActivity(int resultCode) {
        RealmSingleton.setCloseRealm(false);
        Log.d("Here", "Keep realm open in CategoryScreen");
        Intent home = new Intent();
        setResult(resultCode, home);
        finish();
        if (!AppData.isDisableAnimation) overridePendingTransition(R.anim.stay, R.anim.hide_to_bottom);
    }

    private void showInfoDialog() {
        InfoSheet info = new InfoSheet(0);
        info.show(getSupportFragmentManager(), info.getTag());
    }

    private void openNewItemDialog() {
        FolderItemSheet folderItemSheet = new FolderItemSheet();
        folderItemSheet.show(getSupportFragmentManager(), folderItemSheet.getTag());
    }

    private int getNumberOfNotesWithPhotos(RealmResults<Note> allNotes){
        int notesWithPhotos = 0;
        for(Note currentNote : allNotes){
            if(RealmSingleton.getInstance(context).where(Photo.class)
                    .equalTo("noteId", currentNote.getNoteId()).findAll().size() > 0)
                notesWithPhotos++;
        }
        return notesWithPhotos;
    }

}