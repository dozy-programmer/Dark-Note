package com.akapps.dailynote.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.data.Folder;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmDatabase;
import com.akapps.dailynote.classes.other.FolderItemSheet;
import com.akapps.dailynote.classes.other.InfoSheet;
import com.akapps.dailynote.recyclerview.categories_recyclerview;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import io.realm.Realm;
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
    private RecyclerView customCategories;
    public RecyclerView.Adapter categoriesAdapter;
    private FloatingActionButton addCategory;
    private LinearLayout selections;

    // dialog
    private ImageView info;
    private ImageView edit;

    // on-device database
    public Realm realm;
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

    // category empty view
    private TextView showEmptyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_screen);
        overridePendingTransition(R.anim.show_from_bottom, R.anim.stay);

        context = this;

        // if orientation changes, retrieve these values
        if (savedInstanceState != null) {
            editingRegularNote = savedInstanceState.getBoolean("editing_reg_note");
            multiSelect = savedInstanceState.getBoolean("multi_select");
        } else {
            editingRegularNote = getIntent().getBooleanExtra("editing_reg_note", false);
            multiSelect = getIntent().getBooleanExtra("multi_select", false);
        }

        // initialize database and get data
        try {
            realm = Realm.getDefaultInstance();
        } catch (Exception e) {
            realm = RealmDatabase.setUpDatabase(context);
        }

        allCategories = realm.where(Folder.class).sort("positionInList").findAll();

        allNotes = realm.where(Note.class).findAll();
        archivedAllNotes = realm.where(Note.class)
                .equalTo("trash", false)
                .equalTo("archived", true)
                .findAll();
        pinnedAllNotes = realm.where(Note.class)
                .equalTo("trash", false)
                .equalTo("archived", false)
                .equalTo("pin", true)
                .findAll();
        allSelectedNotes = allNotes.where().equalTo("isSelected", true).findAll();
        isNotesSelected = allSelectedNotes.size() > 0 ? true : false;
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

        User user = realm.where(User.class).findFirst();
        assert user != null;
        if (user.isModeSettings()) {
            ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0).setBackgroundColor(context.getColor(R.color.light_mode));
            getWindow().setStatusBarColor(context.getColor(R.color.light_mode));
        }
        else
            ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0).setBackgroundColor(context.getColor(R.color.gray));
    }

    @Override
    public void onBackPressed() {
        // if any note is selected, un-select all of them
        Intent home = new Intent();
        setResult(0, home);
        finish();
        overridePendingTransition(R.anim.stay, R.anim.hide_to_bottom);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("editing_reg_note", editingRegularNote);
        savedInstanceState.putBoolean("multi_select", multiSelect);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(realm!=null)
            realm.close();
    }

    @SuppressLint("SetTextI18n")
    private void initializeLayout(){
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
        info = findViewById(R.id.info);
        edit = findViewById(R.id.edit);
        showEmptyMessage = findViewById(R.id.empty_category);
        selections = findViewById(R.id.top_layout);

        // toolbar
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        archived.setText(archived.getText() + " (" + archivedAllNotes.size() + ")");
        pinned.setText(pinned.getText() + " [" + pinnedAllNotes.size() + "]");
        trash.setText(trash.getText() + " (" + trashAllNotes.size() + ")");
        locked.setText(locked.getText() + " [" + lockedAllNotes.size() + "]");
        reminder.setText(reminder.getText() + " [" + reminderAllNotes.size() + "]");

        if(editingRegularNote){
            allSelectedNotes = realm.where(Note.class).equalTo("isSelected", true).findAll();
            title.setText("Current:\n" + allSelectedNotes.get(0).getCategory());
            if(allSelectedNotes.get(0).getCategory().equals("none"))
                unselectCategories.setVisibility(View.GONE);

            showAllNotes.setVisibility(View.GONE);
            noCategory.setVisibility(View.GONE);
            trash.setVisibility(View.GONE);
            archived.setVisibility(View.GONE);
            pinned.setVisibility(View.GONE);
            locked.setVisibility(View.GONE);
            reminder.setVisibility(View.GONE);
        }
        else{
            int allNotesSize = 0;
            int noCategoryNotesSize = 0;
            int allSelected= 0;

            allNotesSize += allNotes.size() ;
            noCategoryNotesSize += uncategorizedNotes.size();
            allSelected += allSelectedNotes.size();

            showAllNotes.setText("All Notes (" + allNotesSize+ ")");

            noCategory.setText("Uncategorized notes (" + noCategoryNotesSize + ")");

            if(allSelected == 0)
                unselectCategories.setVisibility(View.GONE);
            else
                title.setText(allSelected + " Selected");

            // make padding shorter since there isn't a folder opened
            title.setPadding(0,20,0,0);

            if(multiSelect){
                showAllNotes.setVisibility(View.VISIBLE);
                noCategory.setVisibility(View.VISIBLE);
                trash.setVisibility(View.GONE);
                archived.setVisibility(View.VISIBLE);
                showAllNotes.setText("Archive");
                noCategory.setText("Un-Archive");
                showAllNotes.setTextColor(getColor(R.color.darker_blue));
                noCategory.setTextColor(getColor(R.color.chetwode_blue));
                unpinned.setTextColor(getColor(R.color.chardonnay));
                unpinned.setVisibility(View.VISIBLE);
                archived.setTextColor(getColor(R.color.orange));
                archived.setText("Pin");
                archived.setCompoundDrawables(null, null, null, null);

                findViewById(R.id.pinned_layout).setVisibility(View.GONE);

                reminder.setVisibility(View.INVISIBLE);
                locked.setVisibility(View.GONE);
            }
        }

        // recyclerview
        customCategories.setHasFixedSize(true);
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.ACTION_STATE_DRAG| ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END, 0) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder dragged, @NonNull RecyclerView.ViewHolder target) {
                int started = dragged.getAbsoluteAdapterPosition();
                int ended = target.getAbsoluteAdapterPosition();

                Folder item = allCategories.get(started);
                Folder item2 = allCategories.get(ended);
                realm.beginTransaction();
                if(Math.abs(ended-started)>1){
                    if(started<ended) {
                        Folder item3 = allCategories.get(started+1);
                        int middlePosition = item3.getPositionInList();
                        item.setPositionInList(ended);
                        item3.setPositionInList(started);
                        item2.setPositionInList(middlePosition);
                    }
                    else{
                        Folder item3 = allCategories.get(started-1);
                        int middlePosition = item3.getPositionInList();
                        item.setPositionInList(ended);
                        item3.setPositionInList(started);
                        item2.setPositionInList(middlePosition);
                    }
                }
                else {
                    item.setPositionInList(ended);
                    item2.setPositionInList(started);
                }
                realm.commitTransaction();
                categoriesAdapter.notifyItemMoved(started, ended);
                return false;
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                if(!recyclerView.isComputingLayout()) {
                    categoriesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { }

        });

        helper.attachToRecyclerView(customCategories);
        if(Helper.isTablet(context))
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

        info.setOnClickListener(v -> showInfoDialog());

        edit.setOnClickListener(v-> {
            isEditing = !isEditing;
            if(isEditing) {
                titleBefore = title.getText().toString();
                title.setText("Editing");
                Helper.showMessage(this, "Editing", "Click on item to edit", MotionToast.TOAST_SUCCESS);
                edit.setImageDrawable(getDrawable(R.drawable.cancel));
                info.setVisibility(View.GONE);
                selections.setVisibility(View.GONE);
            }
            else{
                title.setText(titleBefore);
                edit.setImageDrawable(getDrawable(R.drawable.edit_icon));
                info.setVisibility(View.VISIBLE);
                selections.setVisibility(View.VISIBLE);
            }
        });

        addCategory.setOnClickListener(v -> openNewItemDialog());

        showAllNotes.setOnClickListener(v -> {
            if(multiSelect) {
                realm.beginTransaction();
                allSelectedNotes.setBoolean("archived", true);
                realm.commitTransaction();
                closeActivity(-8);
            }
            else if(!isNotesSelected)
                closeActivity(-2);
            else
                showErrorMessage();
        });

        noCategory.setOnClickListener(v -> {
            if(multiSelect) {
                realm.beginTransaction();
                allSelectedNotes.setBoolean("archived", false);
                realm.commitTransaction();
                closeActivity(-9);
            }
            else if(!isNotesSelected)
                closeActivity(-3);
            else
                showErrorMessage();
        });

        trash.setOnClickListener(v -> {
            if(!isNotesSelected && trashAllNotes.size() > 0)
                closeActivity(-5);
            else if(trashAllNotes.size() == 0)
                showEmptyMessage();
            else
                showErrorMessage();
        });

        archived.setOnClickListener(v -> {
            if(multiSelect) {
                realm.beginTransaction();
                allSelectedNotes.setBoolean("pin", true);
                realm.commitTransaction();
                closeActivity(-13);
            }
            if(!isNotesSelected && archivedAllNotes.size() > 0)
                closeActivity(-10);
            else if(archivedAllNotes.size() == 0)
                showEmptyMessage();
            else
                showErrorMessage();
        });

        pinned.setOnClickListener(v -> {
            if(multiSelect) {
                realm.beginTransaction();
                allSelectedNotes.setBoolean("pin", false);
                realm.commitTransaction();
                closeActivity(-12);
            }
            if(!isNotesSelected && pinnedAllNotes.size() > 0)
                closeActivity(-11);
            else if(pinnedAllNotes.size() == 0)
                showEmptyMessage();
            else
                showErrorMessage();
        });

        unpinned.setOnClickListener(v -> {
            realm.beginTransaction();
            allSelectedNotes.setBoolean("pin", false);
            realm.commitTransaction();
            closeActivity(-12);
        });

        locked.setOnClickListener(v -> {
            if(!isNotesSelected && lockedAllNotes.size() > 0)
                closeActivity(-14);
            else if(lockedAllNotes.size() == 0)
                showEmptyMessage();
            else
                showErrorMessage();
        });

        reminder.setOnClickListener(v -> {
            if(!isNotesSelected && reminderAllNotes.size() > 0)
                closeActivity(-15);
            else if(reminderAllNotes.size() == 0)
                showEmptyMessage();
            else
                showErrorMessage();
        });

        unselectCategories.setOnClickListener(v -> {
            realm.beginTransaction();
            allSelectedNotes.setString("category", "none");
            realm.commitTransaction();
            unSelectAllNotes();
            closeActivity(-4);
        });

        close.setOnClickListener(v -> {
            if(editingRegularNote)
                unSelectAllNotes();
            closeActivity(0);
        });
    }

    private void populateCategories() {
        categoriesAdapter = new categories_recyclerview(allCategories, realm, CategoryScreen.this, context);
        customCategories.setAdapter(categoriesAdapter);
    }

    private void showErrorMessage(){
        Helper.showMessage(this, "Select Folder", "You need to select a folder" +
                        " to put all the notes you selected", MotionToast.TOAST_ERROR);
    }

    private void showEmptyMessage(){
        Helper.showMessage(this, "Empty", "Cannot open empty folder", MotionToast.TOAST_ERROR);
    }

    private void checkEmpty() {
        showEmptyMessage.setVisibility(categoriesAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void unSelectAllNotes(){
        realm.beginTransaction();
        allSelectedNotes.setBoolean("isSelected", false);
        realm.commitTransaction();
    }

    private void closeActivity(int resultCode){
        Intent home = new Intent();
        setResult(resultCode, home);
        finish();
        overridePendingTransition(R.anim.stay, R.anim.hide_to_bottom);
    }

    private void showInfoDialog(){
        InfoSheet info = new InfoSheet(0);
        info.show(getSupportFragmentManager(), info.getTag());
    }

    private void openNewItemDialog(){
        FolderItemSheet folderItemSheet = new FolderItemSheet();
        folderItemSheet.show(getSupportFragmentManager(), folderItemSheet.getTag());
    }

}