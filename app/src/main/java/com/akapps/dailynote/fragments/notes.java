package com.akapps.dailynote.fragments;

import static com.akapps.dailynote.classes.helpers.UiHelper.getColorFromTheme;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.airbnb.lottie.LottieAnimationView;
import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.CategoryScreen;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.activity.SettingsScreen;
import com.akapps.dailynote.classes.data.Folder;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.Photo;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmHelper;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.akapps.dailynote.classes.other.ExportNotesSheet;
import com.akapps.dailynote.classes.other.FilterSheet;
import com.akapps.dailynote.classes.other.InfoSheet;
import com.akapps.dailynote.recyclerview.notes_recyclerview;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.util.ArrayList;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import www.sanju.motiontoast.MotionToast;

public class notes extends Fragment {

    // layout
    private View view;
    private Context context;
    private TextView fragmentTitle;
    private TextView sortedBy;
    private MaterialCardView searchLayout;
    private MaterialCardView filterNotes;
    private MaterialCardView settings;
    private MaterialCardView restoreNotes;
    private MaterialCardView categoryNotes;
    private SearchView searchEditText;
    private ImageView search;
    private ImageView categoryIcon;
    private ImageView filterIcon;
    private ImageView settingsIcon;
    private RecyclerView recyclerViewNotes;
    private RecyclerView.Adapter adapterNotes;
    private FloatingActionButton addNote;
    private FloatingActionButton addCheckList;
    private FloatingActionMenu addMenu;
    private FloatingActionButton addNoteTwo;
    private FloatingActionButton addCheckListTwo;
    private FloatingActionMenu addMenuLarge;

    // on-device database
    private RealmResults<Note> allNotes;
    private RealmResults<Note> filteredNotes;

    // activity data
    private boolean isSearchingNotes;
    private boolean deletingMultipleNotes;
    private boolean isAllSelected;
    private boolean isTrashSelected;
    public boolean enableSelectMultiple;
    private int numMultiSelect = -1;
    private int lightColor;

    // dialog
    private boolean isNotesFiltered;

    // filter
    private FilterSheet customSheet;

    // empty list layout
    private ScrollView empty_Layout;
    private TextView title;
    private TextView subtitle;
    private TextView subSubTitle;

    public notes() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();

        Thread thread = new Thread() {
            @Override
            public void run() {
                Intent intent = getActivity().getIntent();
                String action = intent.getAction();
                String type = intent.getType();

                if ((Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action))
                        && type != null) {
                    String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                    String sharedTitle = intent.getStringExtra(Intent.EXTRA_SUBJECT);
                    Intent note = new Intent(getActivity(), NoteEdit.class);
                    if (sharedText != null)
                        note.putExtra("otherAppNote", sharedText);
                    else
                        note.putExtra("otherAppNote", "");
                    if (sharedTitle != null)
                        note.putExtra("otherAppTitle", sharedTitle);
                    else
                        note.putExtra("otherAppTitle", "");

                    Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    ArrayList<String> images = new ArrayList<>();
                    if (imageUri != null) {
                        File newFile = Helper.createFile(getActivity(), "image", ".png");
                        String filePath = Helper.createFile(context, imageUri, newFile).getAbsolutePath();
                        images.add(filePath);
                    }

                    ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                    if (imageUris != null) {
                        for (Uri image : imageUris) {
                            File newFile = Helper.createFile(getActivity(), "image", ".png");
                            String filePath = Helper.createFile(context, image, newFile).getAbsolutePath();
                            images.add(filePath);
                        }
                    }

                    if (images.size() > 0)
                        note.putStringArrayListExtra("images", images);

                    getActivity().startActivity(note);
                }

                Helper.deleteCache(context);
                Helper.deleteUnneededFiles(getActivity());
            }
        };
        thread.start();

        // before getting all notes, make sure all their date and millisecond parameters match
        RealmHelper.verifyDateWithMilli(context);
        updateDateEditedMilli();
        unSelectAllNotes();

        allNotes = getAllNotes();

        if (getUser().isShowFolderNotes())
            allNotes = allNotes.where().equalTo("category", "none").findAll();
        allNotes = allNotes.where().sort("pin", Sort.DESCENDING).findAll();

        getActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isSearchingNotes) {
                    closeMultipleNotesLayout();
                    showData();
                    isListEmpty(getAllNotes().size(), false);
                } else {
                    getActivity().moveTaskToBack(true);
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_notes, container, false);

        UiHelper.setStatusBarColor(getActivity());
        // shows all realm notes (offline) aka notes and checklists
        initializeUi();
        initializeLayout();
        showData();

        if (getUser().isOpenFoldersOnStart() && AppData.isAppFirstStarted) {
            AppData.isAppFirstStarted = false;
            Helper.saveBooleanPreference(context, true, "app_started");
            Intent category = new Intent(getActivity(), CategoryScreen.class);
            Helper.setOrientation(getActivity(), context);
            startActivityForResult(category, 5);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Helper.unSetOrientation(getActivity(), context);
        if (isSearchingNotes) return;

        if (getRealm().isClosed())
            new Handler(Looper.getMainLooper()).postDelayed(() -> refreshFragment(true), 800);
        else {
            adapterNotes.notifyDataSetChanged();
            try {
                // if list is empty, then it shows an empty layout
                isListEmpty(adapterNotes.getItemCount(), isNotesFiltered && adapterNotes.getItemCount() == 0);
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> refreshFragment(true), 800);
            }

            if (RealmSingleton.getInstance(context).where(Note.class).findAll().size() == 0)
                Helper.deleteAppFiles(context);
        }
    }

    @Override
    public void onDestroy() {
        RealmSingleton.closeRealmInstance("fragment notes onDestroy");

        if (customSheet != null)
            customSheet.dismiss();

        Helper.deleteCache(context);
        super.onDestroy();
    }

    private void setRecyclerviewLayout() {
        int span = 2;
        if (Helper.isTablet(context))
            span = 3;

        if (getUser().getLayoutSelected().equals("stag")) {
            StaggeredGridLayoutManager layout = new StaggeredGridLayoutManager(span, LinearLayoutManager.VERTICAL);
            layout.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
            recyclerViewNotes.setLayoutManager(layout);
        } else if (getUser().getLayoutSelected().equals("grid")) {
            GridLayoutManager layout = new GridLayoutManager(context, span);
            recyclerViewNotes.setLayoutManager(layout);
        } else if (getUser().getLayoutSelected().equals("row")) {
            LinearLayoutManager layout = new LinearLayoutManager(context);
            recyclerViewNotes.setLayoutManager(layout);
        }
    }

    private void initializeUi() {
        fragmentTitle = view.findViewById(R.id.fragment_title);
        sortedBy = view.findViewById(R.id.sorted_by);
        searchLayout = view.findViewById(R.id.search_padding);
        filterNotes = view.findViewById(R.id.filter);
        restoreNotes = view.findViewById(R.id.restore);
        settings = view.findViewById(R.id.settings_toolbar);
        categoryNotes = view.findViewById(R.id.category);
        searchEditText = view.findViewById(R.id.search_text);
        search = view.findViewById(R.id.search);
        recyclerViewNotes = view.findViewById(R.id.notes_recyclerview);
        empty_Layout = view.findViewById(R.id.empty_Layout);
        title = view.findViewById(R.id.empty_title);
        subtitle = view.findViewById(R.id.empty_subtitle);
        subSubTitle = view.findViewById(R.id.empty_sub_subtitle);
        filterIcon = view.findViewById(R.id.filter_icon);
        categoryIcon = view.findViewById(R.id.category_icon);
        settingsIcon = view.findViewById(R.id.settings_icon);
        addMenu = view.findViewById(R.id.menu);
        addNote = view.findViewById(R.id.add_note);
        addCheckList = view.findViewById(R.id.add_checklist);
        addMenuLarge = view.findViewById(R.id.menu_2);
        addNoteTwo = view.findViewById(R.id.add_note_2);
        addCheckListTwo = view.findViewById(R.id.add_checklist_2);
    }

    private void showData() {
        getSortDataAndSort();
    }

    private void initializeLayout() {
        setRecyclerviewLayout();

        if (getUser().isIncreaseFabSize()) {
            addMenuLarge.setVisibility(View.VISIBLE);
            addMenu.setVisibility(View.GONE);
        } else {
            addMenuLarge.setVisibility(View.GONE);
            addMenu.setVisibility(View.VISIBLE);
        }

        searchEditText.setIconifiedByDefault(false);
        int searchPlateId = searchEditText.getContext().getResources()
                .getIdentifier("android:id/search_plate", null, null);
        View searchPlateView = searchEditText.findViewById(searchPlateId);
        if (searchPlateView != null) {
            searchPlateView.setBackgroundColor(getColorFromTheme(getActivity(), R.attr.primaryBackgroundColor));
            int id = searchEditText.getContext().getResources()
                    .getIdentifier("android:id/search_src_text", null, null);
            TextView textView = searchEditText.findViewById(id);
            textView.setTextColor(getColorFromTheme(getActivity(), R.attr.primaryBackgroundColor));
            ((EditText) searchEditText.findViewById(id)).setHintTextColor(getColorFromTheme(getActivity(), R.attr.primaryTextColor));
            ((EditText) searchEditText.findViewById(id)).setTextColor(getColorFromTheme(getActivity(), R.attr.primaryTextColor));
        }

        settings.setOnClickListener(v -> {
            if (enableSelectMultiple) {
                int numSelectedNotes = Integer.parseInt(fragmentTitle.getText().toString().replace(" Selected", ""));
                ExportNotesSheet exportNotesSheet = new ExportNotesSheet(numSelectedNotes, this, false);
                exportNotesSheet.show(getActivity().getSupportFragmentManager(), exportNotesSheet.getTag());
            } else
                openSettings();
        });

        addMenuLarge.setOnMenuButtonClickListener(v -> {
            if (isSearchingNotes) {
                closeMultipleNotesLayout();
                isListEmpty(0, false);
                showData();
            } else if (isNotesFiltered || isTrashSelected || enableSelectMultiple)
                clearMultipleSelect();
            else {
                if (addMenuLarge.isOpened())
                    addMenuLarge.close(true);
                else
                    addMenuLarge.open(true);
            }
        });

        addMenu.setOnMenuButtonClickListener(v -> {
            if (isSearchingNotes) {
                closeMultipleNotesLayout();
                isListEmpty(0, false);
                showData();
            } else if (isNotesFiltered || isTrashSelected || enableSelectMultiple)
                clearMultipleSelect();
            else {
                if (addMenu.isOpened())
                    addMenu.close(true);
                else
                    addMenu.open(true);
            }
        });

        filterNotes.setOnClickListener(v -> {
            if (deletingMultipleNotes)
                selectAllNotes();
            else
                showFilterMenu();
        });

        categoryNotes.setOnClickListener(v -> {
            Intent category = new Intent(getActivity(), CategoryScreen.class);
            Helper.setOrientation(getActivity(), context);
            if (enableSelectMultiple)
                category.putExtra("multi_select", true);
            startActivityForResult(category, 5);
            if (!AppData.isDisableAnimation)
                getActivity().overridePendingTransition(R.anim.show_from_bottom, R.anim.stay);
        });

        search.setOnClickListener(v -> {
            addMenu.close(true);
            addMenuLarge.close(true);
            if (deletingMultipleNotes) {
                isAllSelected = false;
                InfoSheet info = new InfoSheet(3, true, notes.this, isTrashSelected);
                info.show(getActivity().getSupportFragmentManager(), info.getTag());
            } else {
                showSearchBar();
                isListEmpty(0, true);
                populateAdapter(getRealm().where(Note.class).equalTo("title", "~~test~~").findAll());
            }

        });

        restoreNotes.setOnClickListener(v -> restoreMultipleNotes());

        addNote.setOnClickListener(v -> {
            Intent note = new Intent(getActivity(), NoteEdit.class);
            startActivity(note);
            addMenu.close(true);
        });

        addCheckList.setOnClickListener(v -> {
            Intent checklist = new Intent(getActivity(), NoteEdit.class);
            checklist.putExtra("isChecklist", true);
            startActivity(checklist);
            addMenu.close(true);
        });

        addNoteTwo.setOnClickListener(v -> {
            Intent note = new Intent(getActivity(), NoteEdit.class);
            startActivity(note);
            addMenuLarge.close(true);
        });

        addCheckListTwo.setOnClickListener(v -> {
            Intent checklist = new Intent(getActivity(), NoteEdit.class);
            checklist.putExtra("isChecklist", true);
            startActivity(checklist);
            addMenuLarge.close(true);
        });

        searchEditText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (isSearchingNotes) {
                    if (s.length() == 0) {
                        RealmResults<Note> showEmptyLayout = getRealm().where(Note.class).equalTo("pinNumber", -1).findAll();
                        isListEmpty(0, true);
                        populateAdapter(showEmptyLayout);
                    } else
                        searchNotesAndUpdate(s);
                }
                return false;
            }
        });
    }

    private void updateDateEditedMilli() {
        RealmResults<Note> resultsCreated = getRealm().where(Note.class)
                .equalTo("dateCreatedMilli", 0)
                .findAll();

        RealmResults<Note> resultsEdited = getRealm().where(Note.class)
                .equalTo("dateEditedMilli", 0)
                .findAll();

        if (resultsCreated.size() != 0) {
            for (int i = 0; i < resultsCreated.size(); i++) {
                getRealm().beginTransaction();
                Note currentNote = resultsCreated.get(i);
                currentNote.setDateCreatedMilli(Helper.dateToCalender(currentNote.getDateCreated()).getTimeInMillis());
                getRealm().commitTransaction();
            }
        }

        if (resultsEdited.size() != 0) {
            for (int i = 0; i < resultsEdited.size(); i++) {
                getRealm().beginTransaction();
                Note currentNote = resultsEdited.get(i);
                currentNote.setDateEditedMilli(Helper.dateToCalender(currentNote.getDateEdited()).getTimeInMillis());
                getRealm().commitTransaction();
            }
        }

        if (resultsCreated.size() > 0 || resultsEdited.size() > 0)
            updateDateEditedMilli();
    }

    public void clearMultipleSelect() {
        numMultiSelect = -1;
        enableSelectMultiple = isNotesFiltered = false;
        closeMultipleNotesLayout();
        showData();
    }

    private void openSettings() {
        savePreferences();
        int size = getRealm().where(Note.class).findAll().size();
        String userId = String.valueOf(getUser().getUserId());
        Intent settings = new Intent(context, SettingsScreen.class);
        settings.putExtra("size", size);
        //RealmSingleton.setCloseRealm(false);
        startActivity(settings);
        //getActivity().finish();
        getActivity().overridePendingTransition(R.anim.show_from_bottom, R.anim.stay);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 5) {
            int id = data.getIntExtra("category", -1);
            boolean viewCategoryNotes = data.getBooleanExtra("viewing", false);

            if (id != -1) {
                String currentCategory = getRealm().where(Folder.class).equalTo("id", id)
                        .findFirst().getName();
                RealmResults<Note> category = getRealm().where(Note.class)
                        .equalTo("trash", false)
                        .equalTo("archived", false)
                        .equalTo("category", currentCategory).findAll();
                filteringByCategory(category, true);
                if (!viewCategoryNotes) {
                    Helper.showMessage(getActivity(), "Added", "Note(s) have been added to " +
                            "category", MotionToast.TOAST_SUCCESS);
                    clearMultipleSelect();
                }
            }
        } else if (resultCode == -4 || resultCode == -2) {
            if (resultCode == -2) {
                filteringByCategory(getRealm().where(Note.class).findAll(), true);
                restoreNotes.setVisibility(View.GONE);
            } else {
                closeMultipleNotesLayout();
                Helper.showMessage(getActivity(), "UnSelected", "All Selected note(s) have " +
                        "been unselected from category", MotionToast.TOAST_SUCCESS);
                clearMultipleSelect();
            }
        } else if (resultCode == -3) {
            restoreNotes.setVisibility(View.GONE);
            RealmResults<Note> category = getRealm().where(Note.class)
                    .equalTo("trash", false)
                    .equalTo("archived", false)
                    .equalTo("category", "none").findAll();
            filteringByCategory(category, true);
        } else if (resultCode == -5) {
            RealmResults<Note> queryDeletedNotes = getRealm().where(Note.class).equalTo("trash", true).findAll();
            isListEmpty(queryDeletedNotes.size(), false);
            populateAdapter(queryDeletedNotes);
            if (queryDeletedNotes.size() == 0)
                isListEmpty(queryDeletedNotes.size(), true);
            isTrashSelected = true;
            restoreNotes.setVisibility(View.VISIBLE);
            settings.setVisibility(View.GONE);
            filterNotes.setVisibility(View.GONE);
            searchLayout.setVisibility(View.GONE);
            unSelectAllNotes();
            isNotesFiltered = true;
            closeFilter();
        } else if (resultCode == -10 || resultCode == -9 || resultCode == -8) {
            RealmResults<Note> queryArchivedNotes =
                    getRealm().where(Note.class)
                            .equalTo("archived", true)
                            .equalTo("trash", false).findAll();

            if (resultCode == -8)
                Helper.showMessage(getActivity(), "Archived", "All Selected note(s) have " +
                        "been archived", MotionToast.TOAST_SUCCESS);
            else if (resultCode == -9)
                Helper.showMessage(getActivity(), "Un-Archived", "All Selected note(s) have " +
                        "been un-archived", MotionToast.TOAST_SUCCESS);

            if (enableSelectMultiple) {
                closeMultipleNotesLayout();
                showData();
            } else
                filteringAllNotesRealm(queryArchivedNotes, true);
        } else if (resultCode == -11 || resultCode == -12 || resultCode == -13) {
            RealmResults<Note> queryPinnedNotes =
                    getRealm().where(Note.class)
                            .equalTo("pin", true).findAll();

            if (resultCode == -13)
                Helper.showMessage(getActivity(), "Pinned", "All Selected note(s) have " +
                        "been pinned", MotionToast.TOAST_SUCCESS);
            else if (resultCode == -12)
                Helper.showMessage(getActivity(), "Un-Pinned", "All Selected note(s) have " +
                        "been un-pinned", MotionToast.TOAST_SUCCESS);

            if (enableSelectMultiple) {
                closeMultipleNotesLayout();
                showData();
            } else
                filteringAllNotesRealm(queryPinnedNotes, true);
        } else if (resultCode == -14) {
            RealmResults<Note> queryLockedNotes =
                    getRealm().where(Note.class)
                            .greaterThan("pinNumber", 0).findAll();

            filteringAllNotesRealm(queryLockedNotes, true);
        } else if (resultCode == -15) {
            RealmResults<Note> queryRemindNotes =
                    getRealm().where(Note.class)
                            .isNotEmpty("reminderDateTime").findAll();

            filteringAllNotesRealm(queryRemindNotes, true);
        } else if (resultCode == -16) {
            RealmResults<Photo> allNotePhotos = getRealm().where(Photo.class).distinct("noteId").findAll();

            Integer[] allNotePhotosId = new Integer[allNotePhotos.size()];

            // Iterate over RealmResults and populate the array
            for (int i = 0; i < allNotePhotos.size(); i++) {
                allNotePhotosId[i] = allNotePhotos.get(i).getNoteId();
            }

            RealmResults<Note> queryNotesWithPhotos =
                    getRealm().where(Note.class)
                            .in("noteId", allNotePhotosId).findAll();

            filteringAllNotesRealm(queryNotesWithPhotos, true);
        }
    }

    private void showFilterMenu() {
        customSheet = new FilterSheet(this);
        customSheet.show(getParentFragmentManager(), customSheet.getTag());
    }

    public void filterAndSortNotes(String dateType, boolean oldestToNewest,
                                   boolean newestToOldest, boolean aToZ, boolean zToA) {
        isNotesFiltered = true;

        RealmResults<Note> result = getRealm().where(Note.class)
                .equalTo("archived", false)
                .equalTo("trash", false)
                .findAll();

        if (dateType != null && !dateType.equals("null")) {
            if (newestToOldest)
                result = result.where().sort(dateType, Sort.DESCENDING).findAll();
            else if (oldestToNewest)
                result = result.where().sort(dateType, Sort.ASCENDING).findAll();
        }

        if (aToZ)
            result = result.where().sort("title").findAll();
        if (zToA)
            result = result.where().sort("title", Sort.DESCENDING).findAll();

        if (result != null)
            filteringAllNotesRealm(result, false);
    }

    private void filteringAllNotesRealm(RealmResults<Note> query, boolean isCategory) {
        isNotesFiltered = true;
        restoreNotes.setVisibility(View.GONE);
        isListEmpty(query.size(), false);
        populateAdapter(query);
        if (query.size() == 0)
            isListEmpty(query.size(), true);
        if (!isCategory)
            setCategoryIconColors();
        else
            closeFilter();
    }

    private void filteringByCategory(RealmResults<Note> query, boolean isCategory) {
        isNotesFiltered = true;
        isListEmpty(query.size(), false);
        populateAdapter(query);
        if (query.size() == 0)
            isListEmpty(query.size(), true);
        if (!isCategory)
            setCategoryIconColors();
        else
            closeFilter();
    }

    public void setCategoryIconColors(){
        filterNotes.setCardBackgroundColor(getColorFromTheme(getActivity(), R.attr.primaryButtonColor));
        addMenu.setMenuButtonColorNormal(getColorFromTheme(getActivity(), R.attr.tertiaryButtonColor));
        addMenu.getMenuIconView().setImageDrawable(context.getDrawable(R.drawable.close_icon));
        addMenuLarge.setMenuButtonColorNormal(getColorFromTheme(getActivity(), R.attr.tertiaryButtonColor));
        addMenuLarge.getMenuIconView().setImageDrawable(context.getDrawable(R.drawable.close_icon));
    }

    public void showDefaultSort() {
        sortedBy.setVisibility(View.GONE);
        allNotes = getAllNotes();
        if (getUser().isShowFolderNotes())
            allNotes = allNotes.where().equalTo("category", "none").findAll();
        allNotes = allNotes.where().sort("pin", Sort.DESCENDING).findAll();
        populateAdapter(allNotes);
        isListEmpty(allNotes.size(), false);
    }

    public void getSortDataAndSort() {
        String dateType = Helper.getPreference(getContext(), "_dateType");
        boolean oldestToNewest = Helper.getBooleanPreference(getContext(), "_oldestToNewest");
        boolean newestToOldest = Helper.getBooleanPreference(getContext(), "_newestToOldest");

        boolean aToZ = Helper.getBooleanPreference(getContext(), "_aToZ");
        boolean zToA = Helper.getBooleanPreference(getContext(), "_zToA");

        sortedBy.setVisibility(View.GONE);

        if (dateType != null || aToZ || zToA) {
            if (oldestToNewest) {
                allNotes = getRealm().where(Note.class)
                        .equalTo("archived", false)
                        .equalTo("trash", false)
                        .sort(dateType, Sort.ASCENDING).findAll();

                if (getUser().isShowFolderNotes())
                    allNotes = allNotes.where().equalTo("category", "none").findAll();
                allNotes = allNotes.where().sort("pin", Sort.DESCENDING).findAll();
            } else if (newestToOldest) {
                allNotes = getRealm().where(Note.class)
                        .equalTo("archived", false)
                        .equalTo("trash", false)
                        .sort(dateType, Sort.DESCENDING).findAll();

                if (getUser().isShowFolderNotes())
                    allNotes = allNotes.where().equalTo("category", "none").findAll();
                allNotes = allNotes.where().sort("pin", Sort.DESCENDING).findAll();
            } else if (aToZ) {
                allNotes = getRealm().where(Note.class)
                        .equalTo("archived", false)
                        .equalTo("trash", false)
                        .sort("title").findAll();

                if (getUser().isShowFolderNotes())
                    allNotes = allNotes.where().equalTo("category", "none").findAll();
                allNotes = allNotes.where().sort("pin", Sort.DESCENDING).findAll();
            } else if (zToA) {
                allNotes = getRealm().where(Note.class)
                        .equalTo("archived", false)
                        .equalTo("trash", false)
                        .sort("title", Sort.DESCENDING).findAll();

                if (getUser().isShowFolderNotes())
                    allNotes = allNotes.where().equalTo("category", "none").findAll();
                allNotes = allNotes.where().sort("pin", Sort.DESCENDING).findAll();
            }
            populateAdapter(allNotes);
            isListEmpty(allNotes.size(), false);
        } else
            showDefaultSort();
    }

    // populates the recyclerview
    private void populateAdapter(RealmResults<Note> allNotes) {
        filteredNotes = allNotes;
        adapterNotes = new notes_recyclerview(isNotesFiltered ? filteredNotes : allNotes, context, getActivity(),
                notes.this, getUser().isShowPreview(), getUser().isShowPreviewNoteInfo());
        recyclerViewNotes.setAdapter(adapterNotes);
    }

    private void closeFilter() {
        isNotesFiltered = true;
        categoryNotes.setCardBackgroundColor(getColorFromTheme(getActivity(), R.attr.primaryButtonColor));
        categoryIcon.setImageDrawable(context.getDrawable(R.drawable.folder_open_icon));
        addMenu.setMenuButtonColorNormal(getColorFromTheme(getActivity(), R.attr.tertiaryButtonColor));
        addMenu.getMenuIconView().setImageDrawable(context.getDrawable(R.drawable.close_icon));
        addMenuLarge.setMenuButtonColorNormal(getColorFromTheme(getActivity(), R.attr.tertiaryButtonColor));
        addMenuLarge.getMenuIconView().setImageDrawable(context.getDrawable(R.drawable.close_icon));
    }

    private void searchNotesAndUpdate(String target) {
        RealmResults<Note> queryNotes = getRealm().where(Note.class)
                .contains("note", target, Case.INSENSITIVE).or()
                .contains("title", target, Case.INSENSITIVE).or()
                .contains("checklistConvertedToString", target, Case.INSENSITIVE)
                .findAll();

        isListEmpty(queryNotes.size(), queryNotes.size() == 0);
        populateAdapter(queryNotes);
    }

    private void showSearchBar() {
        isSearchingNotes = true;
        fragmentTitle.setVisibility(View.GONE);
        filterNotes.setVisibility(View.GONE);
        search.setVisibility(View.GONE);
        categoryNotes.setVisibility(View.GONE);
        restoreNotes.setVisibility(View.GONE);
        searchLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        searchLayout.setCardBackgroundColor(getColorFromTheme(getActivity(), R.attr.primaryBackgroundColor));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(0, 0, 0, 0);
        searchEditText.setLayoutParams(params);
        searchLayout.setPadding(0, 100, 0, 100);
        searchEditText.setVisibility(View.VISIBLE);
        searchEditText.setQueryHint("Searching...");
        searchEditText.setQuery("", false);
        searchEditText.setIconified(true);
        searchEditText.setIconified(false);
        searchEditText.setBackgroundColor(getColorFromTheme(getActivity(), R.attr.primaryBackgroundColor));

        addMenu.setMenuButtonColorNormal(getColorFromTheme(getActivity(), R.attr.tertiaryButtonColor));
        addMenu.getMenuIconView().setImageDrawable(context.getDrawable(R.drawable.back_icon));
        addMenuLarge.setMenuButtonColorNormal(getColorFromTheme(getActivity(), R.attr.tertiaryButtonColor));
        addMenuLarge.getMenuIconView().setImageDrawable(context.getDrawable(R.drawable.back_icon));
        ((LottieAnimationView) view.findViewById(R.id.empty_view)).pauseAnimation();
    }

    private void hideSearchBar() {
        fragmentTitle.setText("Dark Note");
        fragmentTitle.setTextSize(28);
        searchEditText.setQuery("", false);
        searchEditText.setVisibility(View.GONE);
        fragmentTitle.setVisibility(View.VISIBLE);
        settings.setVisibility(View.VISIBLE);
        settingsIcon.setImageDrawable(context.getDrawable(R.drawable.settings_icon));
        restoreNotes.setVisibility(View.GONE);
        if (isTrashSelected) restoreNotes.setVisibility(View.VISIBLE);
        restoreNotes.setCardBackgroundColor(getColorFromTheme(getActivity(), R.attr.secondaryBackgroundColor));
        search.setVisibility(View.VISIBLE);
        searchLayout.setVisibility(View.VISIBLE);
        search.setImageDrawable(context.getDrawable(R.drawable.search_icon));
        filterNotes.setVisibility(View.VISIBLE);
        filterIcon.setImageDrawable(context.getDrawable(R.drawable.filter_icon));
        filterNotes.setCardBackgroundColor(getColorFromTheme(getActivity(), R.attr.secondaryBackgroundColor));
        categoryNotes.setVisibility(View.VISIBLE);
        categoryIcon.setImageDrawable(context.getDrawable(R.drawable.folder_icon));
        categoryNotes.setCardBackgroundColor(getColorFromTheme(getActivity(), R.attr.secondaryBackgroundColor));

        LinearLayout.LayoutParams params = (new LinearLayout.LayoutParams(filterNotes.getWidth(), filterNotes.getHeight()));
        params.setMargins(0, 0, 24, 0);
        searchLayout.setLayoutParams(params);
        searchLayout.setCardBackgroundColor(getColorFromTheme(getActivity(), R.attr.secondaryBackgroundColor));
        searchLayout.setPadding(filterNotes.getPaddingLeft(), filterNotes.getPaddingTop(), filterNotes.getPaddingRight(), filterNotes.getPaddingBottom());
        searchEditText.clearFocus();

        addMenu.setMenuButtonColorNormal(getColorFromTheme(getActivity(), R.attr.primaryButtonColor));
        addMenu.getMenuIconView().setImageDrawable(context.getDrawable(R.drawable.add_icon));
        addMenuLarge.setMenuButtonColorNormal(getColorFromTheme(getActivity(), R.attr.primaryButtonColor));
        addMenuLarge.getMenuIconView().setImageDrawable(context.getDrawable(R.drawable.add_icon));
    }

    public void deleteMultipleNotesLayout() {
        enableSelectMultiple = true;
        addMenu.setMenuButtonColorNormal(getColorFromTheme(getActivity(), R.attr.tertiaryButtonColor));
        addMenu.getMenuIconView().setImageDrawable(context.getDrawable(R.drawable.close_icon));
        addMenuLarge.setMenuButtonColorNormal(getColorFromTheme(getActivity(), R.attr.tertiaryButtonColor));
        addMenuLarge.getMenuIconView().setImageDrawable(context.getDrawable(R.drawable.close_icon));

        search.setImageDrawable(context.getDrawable(R.drawable.delete_icon));
        filterIcon.setImageDrawable(context.getDrawable(R.drawable.select_all_icon));
        settingsIcon.setImageDrawable(context.getDrawable(R.drawable.export_icon));
        settingsIcon.setColorFilter(getColorFromTheme(getActivity(), R.attr.primaryIconTintColor));
        settings.setVisibility(View.VISIBLE);
        filterNotes.setVisibility(View.VISIBLE);
        deletingMultipleNotes = true;
    }

    public void closeMultipleNotesLayout() {
        enableSelectMultiple = false;
        deletingMultipleNotes = false;
        unSelectAllNotes();
        clearVariables();
        hideSearchBar();
    }

    public void deleteMultipleNotes(boolean deleteNotes) {
        RealmResults<Note> selectedNotes = getRealm().where(Note.class).equalTo("isSelected", true).findAll();
        RealmResults<Note> lockedNotes = getRealm().where(Note.class).equalTo("isSelected", true)
                .greaterThan("pinNumber", 0)
                .findAll();

        if (lockedNotes.size() > 0) {
            Helper.showMessage(getActivity(), "Locked Notes", "Locked notes " +
                    "cannot be deleted", MotionToast.TOAST_ERROR);
        } else {
            if (selectedNotes.size() != 0) {
                int number = selectedNotes.size();
                if (isTrashSelected || deleteNotes) {
                    for (Note deleteCurrentNote : selectedNotes)
                        RealmHelper.deleteNote(getContext(), deleteCurrentNote.getNoteId());
                    isListEmpty(getAllNotes().size(), false);
                    numberSelected(0, 0, 0);
                    Helper.showMessage(getActivity(), "Deleted", number + " selected " +
                            "have been deleted", MotionToast.TOAST_SUCCESS);
                    closeMultipleNotesLayout();
                    showData();

                    if (getAllNotes().size() == 0)
                        Helper.deleteAppFiles(context);
                } else {
                    getRealm().beginTransaction();
                    selectedNotes.setBoolean("trash", true);
                    getRealm().commitTransaction();
                    isListEmpty(getAllNotes().size(), false);
                    Helper.showMessage(getActivity(), "Sent to trash", number + " selected " +
                            "have been sent to trash", MotionToast.TOAST_SUCCESS);
                    numberSelected(0, 0, 0);
                    clearMultipleSelect();
                }
            } else
                Helper.showMessage(getActivity(), "Not Deleted", "Nothing was selected " +
                        "and thus not deleted", MotionToast.TOAST_ERROR);
            adapterNotes.notifyDataSetChanged();
        }
    }

    public void restoreMultipleNotes() {
        RealmResults<Note> selectedNotes = getRealm().where(Note.class).equalTo("isSelected", true)
                .equalTo("trash", true).findAll();

        if (selectedNotes.size() != 0) {
            int number = selectedNotes.size();
            getRealm().beginTransaction();
            selectedNotes.setBoolean("trash", false);
            getRealm().commitTransaction();
            adapterNotes.notifyDataSetChanged();
            Helper.showMessage(getActivity(), "Restored", number + " selected " +
                    "have been restored", MotionToast.TOAST_SUCCESS);
            clearMultipleSelect();
        } else
            Helper.showMessage(getActivity(), "Not Restored", "Nothing was selected " +
                    "and thus not restored", MotionToast.TOAST_ERROR);
    }

    public void numberSelected(int add, int subtract, int number) {
        int currentlySelected = 1;
        if (number == -1) {
            try {
                currentlySelected = Integer.parseInt(fragmentTitle.getText().toString()
                        .replaceAll("[^0-9]", "")) + add - subtract;
            } catch (Exception e) {
            }
        } else
            currentlySelected = number;

        numMultiSelect = currentlySelected;
        fragmentTitle.setText(currentlySelected + " Selected");
        fragmentTitle.setTextSize(24);
    }

    public void unSelectAllNotes() {
        if (getRealm().isClosed()) {
            refreshFragment(true);
            return;
        }
        RealmResults<Note> realmResults = getRealm().where(Note.class).equalTo("isSelected", true).findAll();
        if (realmResults.size() != 0) {
            getRealm().beginTransaction();
            realmResults.setBoolean("isSelected", false);
            getRealm().commitTransaction();
        }
    }

    private void selectAllNotes() {
        isAllSelected = !isAllSelected;
        if (isTrashSelected) {
            RealmResults<Note> realmResults = getRealm().where(Note.class).equalTo("trash", true).findAll();
            if (realmResults.size() != 0) {
                getRealm().beginTransaction();
                realmResults.setBoolean("isSelected", isAllSelected);
                getRealm().commitTransaction();
            }
        } else {
            if (filteredNotes.size() != 0) {
                getRealm().beginTransaction();
                filteredNotes.setBoolean("isSelected", isAllSelected);
                getRealm().commitTransaction();
            }
        }
        numberSelected(0, 0, isAllSelected ? adapterNotes.getItemCount() : 0);
        adapterNotes.notifyDataSetChanged();
    }

    private void refreshFragment(boolean refresh) {
        if (refresh) {
            Helper.restart(getActivity());
        } else {
            getActivity().getSupportFragmentManager().beginTransaction().detach(this).attach(this).commit();
            clearVariables();
        }
    }

    private void clearVariables() {
        isSearchingNotes = false;
        searchEditText.setQuery("", false);
        isNotesFiltered = false;
        deletingMultipleNotes = false;
        isTrashSelected = false;
        isAllSelected = false;
    }

    private Realm getRealm() {
        return RealmSingleton.get(getActivity());
    }

    private User getUser() {
        return RealmHelper.getUser(context, "notes fragment");
    }

    private RealmResults<Note> getAllNotes() {
        return getRealm().where(Note.class)
                .equalTo("archived", false)
                .equalTo("trash", false)
                .sort("dateEditedMilli", Sort.DESCENDING).findAll();
    }

    private void isListEmpty(int size, boolean isResult) {
        Helper.isListEmpty(context, size, empty_Layout, title, subtitle, subSubTitle,
                isResult, false, false, view.findViewById(R.id.empty_view),
                view.findViewById(R.id.empty_view_no_animation));
    }

    private void savePreferences() {
        // text size saved by getUser()
        int savedSize = getUser().getTextSize();
        // text size set by getUser()
        String textSize = Helper.getPreference(context, "size");
        int currentSize = Integer.parseInt(textSize == null ? "0" : textSize);

        // if device was backed up, then restore text size
        if (savedSize > 0 && currentSize == 0) {
            Helper.savePreference(context, String.valueOf(savedSize), "size");
        }
        // if text size save
        else if (savedSize != currentSize) {
            getRealm().beginTransaction();
            getUser().setTextSize(currentSize);
            getRealm().commitTransaction();
        }
    }
}