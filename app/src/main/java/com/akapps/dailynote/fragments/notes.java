package com.akapps.dailynote.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.CategoryScreen;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.activity.SettingsScreen;
import com.akapps.dailynote.classes.data.Folder;
import com.akapps.dailynote.classes.helpers.RealmBackupRestore;
import com.akapps.dailynote.classes.other.FilterSheet;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.recyclerview.notes_recyclerview;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import www.sanju.motiontoast.MotionToast;

public class notes extends Fragment{

    // layout
    private View view;
    private Context context;
    private TextView fragmentTitle;
    private TextView sortedBy;
    private CardView searchLayout;
    private CardView filterNotes;
    private CardView settings;
    private CardView restoreNotes;
    private CardView categoryNotes;
    private SearchView searchEditText;
    private ImageView searchClose;
    private ImageView search;
    private ImageView filterIcon;
    private RecyclerView recyclerViewNotes;
    private RecyclerView.Adapter adapterNotes;
    private FloatingActionButton addRegularNotes;
    private FloatingActionButton addChecklistNotes;
    private FloatingActionButton addRegularMenu;
    private LinearLayout menu;

    // on-device database
    private Realm realm;
    private RealmResults<Note> allNotes;
    private User user;

    // activity data
    private boolean isSearchingNotes;
    private String searchingString;
    private boolean deletingMultipleNotes;
    private boolean isAllSelected;
    private boolean isTrashSelected;
    public boolean enableSelectMultiple;

    // dialog
    private boolean isNotesFiltered;

    // filter
    private FilterSheet customSheet;

    // empty list layout
    private ScrollView empty_Layout;
    private TextView title;
    private TextView subtitle;
    private TextView subSubTitle;

    public notes() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getContext();

        // initialize database and get data
        try {
            realm = Realm.getDefaultInstance();
        }
        catch (Exception e){
            Realm.init(context);
            realm = Realm.getDefaultInstance();
        }

        allNotes = realm.where(Note.class)
                .equalTo("archived", false)
                .equalTo("trash", false)
                .sort("dateEdited", Sort.DESCENDING).findAll();

        if (realm.where(User.class).findAll().size() == 0)
            addUser();
        else
            user = realm.where(User.class).findFirst();
        unSelectAllNotes();

        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(isSearchingNotes)
                    refreshFragment(false);
                else
                    getActivity().finish();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_notes, container, false);

        // if orientation changes, then isSearchingNotes is set to the value it was beforehand
        if (savedInstanceState != null) {
            isSearchingNotes = savedInstanceState.getBoolean("searching");
            searchingString = savedInstanceState.getString("searchingString");
        }

        // shows all realm notes (offline) aka notes and checklists
        initializeUi();
        initializeLayout();
        showData();

        if(isSearchingNotes) {
            showSearchBar();
            searchNotesAndUpdate(searchingString);
        }
        else
            isListEmpty(allNotes.size(), false);

        return view;
    }

    // when orientation changes, then search bar status is saved
    @Override
    public void onSaveInstanceState (Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("searching", isSearchingNotes);
        savedInstanceState.putString("searchingString", searchingString);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(user!=null)
            savePreferences();

        if(realm.isClosed()) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> refreshFragment(true), 800);
        }
        else{
            adapterNotes.notifyDataSetChanged();
            // if list is empty, then it shows an empty layout
            isListEmpty(adapterNotes.getItemCount(), isNotesFiltered && adapterNotes.getItemCount() == 0);
            if(menu.getVisibility() == View.VISIBLE)
                menu.animate().alpha(0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        menu.setVisibility(View.GONE);
                    }});
        }
        Helper.deleteCache(context);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(realm!=null)
            realm.close();
    }

    private void setRecyclerviewLayout(){
        int span = 2;
        if(Helper.isTablet(context))
            span = 3;

        if(user.getLayoutSelected().equals("stag")) {
            StaggeredGridLayoutManager layout = new StaggeredGridLayoutManager(span, LinearLayoutManager.VERTICAL);
            layout.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
            recyclerViewNotes.setLayoutManager(layout);
        }
        else if(user.getLayoutSelected().equals("grid")){
            GridLayoutManager layout = new GridLayoutManager(context, span);
            recyclerViewNotes.setLayoutManager(layout);
        }
        else if(user.getLayoutSelected().equals("row")){
            LinearLayoutManager layout = new LinearLayoutManager(context);
            recyclerViewNotes.setLayoutManager(layout);
        }
    }

    private void initializeUi(){
        fragmentTitle = view.findViewById(R.id.fragment_title);
        sortedBy = view.findViewById(R.id.sorted_by);
        searchLayout = view.findViewById(R.id.search_padding);
        filterNotes = view.findViewById(R.id.filter);
        restoreNotes = view.findViewById(R.id.restore);
        settings = view.findViewById(R.id.settings_toolbar);
        categoryNotes = view.findViewById(R.id.category);
        searchEditText = view.findViewById(R.id.search_text);
        searchClose = view.findViewById(R.id.search_close);
        search = view.findViewById(R.id.search);
        recyclerViewNotes = view.findViewById(R.id.notes_recyclerview);
        empty_Layout = view.findViewById(R.id.empty_Layout);
        title = view.findViewById(R.id.empty_title);
        subtitle = view.findViewById(R.id.empty_subtitle);
        subSubTitle = view.findViewById(R.id.empty_sub_subtitle);
        filterIcon = view.findViewById(R.id.filter_icon);
        addRegularNotes = view.findViewById(R.id.add_note_button);
        addChecklistNotes = view.findViewById(R.id.add_checklist_button);
        addRegularMenu = view.findViewById(R.id.regular_notes_layout);
        menu = view.findViewById(R.id.menu);
        addRegularMenu.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.darker_blue)));
    }

    private void showData(){
        populateAdapter(allNotes);
        isListEmpty(allNotes.size(), false);
        getSortDataAndSort();
    }

    private void initializeLayout(){
        setRecyclerviewLayout();

        searchClose.setOnClickListener(v -> {
            if(isSearchingNotes){
                hideSearchBar();
                closeMultipleNotesLayout();
                showData();
            }
        });

        settings.setOnClickListener(v -> openSettings());

        addRegularMenu.setOnClickListener(v -> {
            if(isNotesFiltered || isTrashSelected || enableSelectMultiple) {
                unSelectAllNotes();
                enableSelectMultiple = false;
                settings.setVisibility(View.VISIBLE);
                closeMultipleNotesLayout();
                showData();
            }
            else{
                if(menu.getVisibility() == View.VISIBLE){
                    menu.animate().alpha(0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    menu.setVisibility(View.GONE);
                                }});
                }
                else {
                    menu.animate().alpha(1f).setDuration(500).setListener(null);
                    menu.setVisibility(View.VISIBLE);
                }
            }
        });

        filterNotes.setOnClickListener(v -> {
            if(deletingMultipleNotes)
                selectAllNotes();
            else if((realm.where(Note.class).findAll().size()!=0)) {
                showFilterMenu();
            }
            else
                showMessage("Empty", "There are no notes \uD83D\uDE10", true);
        });

        categoryNotes.setOnClickListener(v -> {
           if(realm.where(Note.class).findAll().size()!=0) {
                if(isNotesFiltered)
                    showMessage("Failed", "Close filter to open categories", true);
                else {
                    Intent category = new Intent(getActivity(), CategoryScreen.class);
                    startActivityForResult(category, 5);
                }
            }
            else
                showMessage("Empty", "There are no notes \uD83D\uDE10", true);
        });

        search.setOnClickListener(v -> {
            if(deletingMultipleNotes){
                isAllSelected = false;
                deleteMultipleNotes();
            }
            else if(realm.where(Note.class).findAll().size()!=0)
                showSearchBar();
            else
                showMessage("Not Searching...", "Can't looking for something that does not exist", true);
        });

        restoreNotes.setOnClickListener(v -> restoreMultipleNotes());

        addRegularNotes.setOnClickListener(v -> {
            Intent note = new Intent(getActivity(), NoteEdit.class);
            startActivity(note);
        });

        addChecklistNotes.setOnClickListener(v -> {
            Intent checklist = new Intent(getActivity(), NoteEdit.class);
            checklist.putExtra("isChecklist", true);
            startActivity(checklist);
        });

        searchEditText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(isSearchingNotes) {
                    searchNotesAndUpdate(s);
                    searchingString = s;
                }
                return false;
            }
        });
    }

    private void openSettings(){
        Intent settings = new Intent(context, SettingsScreen.class);
        settings.putExtra("size", allNotes.size());
        settings.putExtra("user", String.valueOf(user.getUserId()));
        startActivity(settings);
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.show_from_bottom, R.anim.stay);
        realm.close();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 5) {
            int id = data.getIntExtra("category", -1);
            boolean viewCategoryNotes = data.getBooleanExtra("viewing", false);

            if (id != -1) {
                String currentCategory = realm.where(Folder.class).equalTo("id", id)
                        .findFirst().getName();
                RealmResults<Note> category = realm.where(Note.class)
                        .equalTo("category", currentCategory).findAll();
                filteringByCategory(category, true);
                if (!viewCategoryNotes) {
                    Helper.showMessage(getActivity(), "Added", "Note(s) have been added to " +
                            "category", MotionToast.TOAST_SUCCESS);
                    refreshFragment(false);
                }
            }
        }
        else if (resultCode == -4 || resultCode == -2) {
            if(resultCode == -2){
                filteringByCategory(realm.where(Note.class).findAll(), true);
                restoreNotes.setVisibility(View.GONE);
            }
            else {
                closeMultipleNotesLayout();
                Helper.showMessage(getActivity(), "UnSelected", "All Selected note(s) have " +
                        "been unselected from category", MotionToast.TOAST_SUCCESS);
            }
        }
        else if (resultCode == -3) {
            restoreNotes.setVisibility(View.GONE);
            RealmResults<Note> category = realm.where(Note.class)
                    .equalTo("trash", false)
                    .equalTo("archived", false)
                    .equalTo("category", "none").findAll();
            filteringByCategory(category, true);
        }
        else if(resultCode == -5){
            RealmResults<Note> queryDeletedNotes = realm.where(Note.class).equalTo("trash", true).findAll();
            isListEmpty(queryDeletedNotes.size(), false);
            populateAdapter(queryDeletedNotes);
            if (queryDeletedNotes.size() == 0)
                isListEmpty(queryDeletedNotes.size(), true);
            isTrashSelected = true;
            restoreNotes.setVisibility(View.VISIBLE);
            settings.setVisibility(View.GONE);
            unSelectAllNotes();
            isNotesFiltered = true;
            closeFilter();
        }
        else if(resultCode == -10){
            RealmResults<Note> queryArchivedNotes =
                    realm.where(Note.class).equalTo("archived", true)
                    .equalTo("trash", false).findAll();

            filteringAllNotesRealm(queryArchivedNotes, true);
        }
    }

    private void showFilterMenu(){
        customSheet = new FilterSheet(this);
        customSheet.show(getParentFragmentManager(), customSheet.getTag());
    }

    public void filterAndSortNotes(boolean note, boolean checklist,
                                   String kind, String dateType, boolean oldestToNewest,
                                   boolean newestToOldest, boolean aToZ, boolean zToA){
        isNotesFiltered = true;

        RealmResults<Note> result = null;
        if(note || checklist){
            if(note && checklist)
                result = realm.where(Note.class)
                        .equalTo("archived", false)
                        .equalTo("trash", false)
                        .findAll();
            else if(checklist)
                result = realm.where(Note.class)
                        .equalTo("archived", false)
                        .equalTo("trash", false)
                        .equalTo("isCheckList", true).findAll();
            else if(note)
                result = realm.where(Note.class)
                        .equalTo("archived", false)
                        .equalTo("trash", false)
                        .equalTo("isCheckList", false).findAll();


            if(kind!=null && !kind.equals("null")) {
                result = result.where().equalTo(kind, true).findAll();
            }

            if(dateType!=null && !dateType.equals("null")) {
                if(oldestToNewest) {
                    result = result.where().sort(dateType).findAll();
                }
                else if(newestToOldest){
                    result = result.where().sort(dateType , Sort.DESCENDING).findAll();
                }
            }

            if(aToZ)
                result = result.where().sort("title").findAll();
            if(zToA)
                result = result.where().sort("title", Sort.DESCENDING).findAll();

            if(result!=null) {
                getSortDataAndSort();
                filteringAllNotesRealm(result, false);
            }
        }
    }

    private void filteringAllNotesRealm(RealmResults<Note> query, boolean isCategory){
        isNotesFiltered = true;
        restoreNotes.setVisibility(View.GONE);
        isListEmpty(query.size(), false);
        populateAdapter(query);
        if(query.size()==0)
            isListEmpty(query.size(), true);
        if(!isCategory) {
            filterNotes.setCardBackgroundColor(context.getColor(R.color.darker_blue));
            closeMenu();
            addRegularMenu.setImageDrawable(context.getDrawable(R.drawable.close_icon));
            addRegularMenu.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.red)));
        }
        else
            closeFilter();
    }

    private void addUser(){
        int generateId = (int)(Math.random() * 10000000 + 1);
        user = new User(generateId);
        realm.beginTransaction();
        realm.insert(user);
        realm.commitTransaction();
    }

    private void filteringByCategory(RealmResults<Note> query, boolean isCategory){
        isNotesFiltered = true;
        isListEmpty(query.size(), false);
        populateAdapter(query);
        if(query.size()==0)
            isListEmpty(query.size(), true);
        if(!isCategory) {
            filterNotes.setCardBackgroundColor(context.getColor(R.color.darker_blue));
            addRegularMenu.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.red)));
            closeMenu();
            addRegularMenu.setImageDrawable(context.getDrawable(R.drawable.close_icon));
        }
        else
            closeFilter();
    }

    public void getSortDataAndSort(){
        String notes = Helper.getPreference(getContext(), "notes_sort");

        String dateType = Helper.getPreference(getContext(), notes + "_dateType");
        boolean oldestToNewest = Helper.getBooleanPreference(getContext(), notes + "_oldestToNewest");
        boolean newestToOldest = Helper.getBooleanPreference(getContext(), notes + "_newestToOldest");

        boolean aToZ = Helper.getBooleanPreference(getContext(), notes + "_aToZ");
        boolean zToA = Helper.getBooleanPreference(getContext(), notes + "_zToA");

        sortedBy.setVisibility(View.GONE);

        if(notes!=null && !notes.isEmpty()) {
            sortedBy.setVisibility(View.VISIBLE);
            if (notes.equals("notes") && dateType!=null) {
                if (oldestToNewest) {
                    allNotes =  realm.where(Note.class)
                            .equalTo("archived", false)
                            .equalTo("trash", false)
                            .sort(dateType).findAll();
                    if(dateType.equals("dateEdited"))
                        sortedBy.setText("Sorted by: Date Edited");
                    else
                        sortedBy.setText("Sorted by: Date Created");
                }
                else if (newestToOldest) {
                    allNotes =  realm.where(Note.class)
                            .equalTo("archived", false)
                            .equalTo("trash", false)
                            .sort(dateType, Sort.DESCENDING).findAll();
                    if(dateType.equals("dateEdited"))
                        sortedBy.setText("Sorted by: Date Edited");
                    else
                        sortedBy.setText("Sorted by: Date Created");
                }
            }
            else if (notes.equals("notes")) {
                if (aToZ) {
                    allNotes =  realm.where(Note.class)
                            .equalTo("archived", false)
                            .equalTo("trash", false)
                            .sort("title").findAll();
                    sortedBy.setText("Sorted by: Alphabetical");
                }
                else if (zToA) {
                    allNotes =  realm.where(Note.class)
                            .equalTo("archived", false)
                            .equalTo("trash", false)
                            .sort("title", Sort.DESCENDING).findAll();
                    sortedBy.setText("Sorted by: Alphabetical");
                }
            }
            populateAdapter(allNotes);
            isListEmpty(allNotes.size(), false);
        }
    }

    // populates the recyclerview
    private void populateAdapter(RealmResults<Note> allNotes) {
        adapterNotes = new notes_recyclerview(allNotes, realm, getActivity(), notes.this, user.isShowPreview());
        recyclerViewNotes.setAdapter(adapterNotes);
    }

    private void closeFilter(){
        isNotesFiltered = true;
        categoryNotes.setCardBackgroundColor(context.getColor(R.color.darker_blue));
        addRegularMenu.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.red)));
        closeMenu();
        addRegularMenu.setImageDrawable(context.getDrawable(R.drawable.close_icon));
    }

    private void closeMenu(){
        if (menu.getVisibility() == View.VISIBLE)
            menu.animate().alpha(0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    menu.setVisibility(View.GONE);
                }});
    }

    private void searchNotesAndUpdate(String target){
        RealmResults<Note> queryNotes = realm.where(Note.class)
                .contains("note", target, Case.INSENSITIVE).or()
                .contains("title", target, Case.INSENSITIVE).or()
                .contains("checklistConvertedToString", target, Case.INSENSITIVE)
                .findAll();

        isListEmpty(queryNotes.size(), queryNotes.size() == 0);
        populateAdapter(queryNotes);
    }

    private void showSearchBar(){
        isSearchingNotes = true;
        fragmentTitle.setVisibility(View.GONE);
        filterNotes.setVisibility(View.GONE);
        searchLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        searchLayout.setCardBackgroundColor(context.getColor(R.color.gray));
        searchEditText.setVisibility(View.VISIBLE);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(50, 0, 50, 0);
        searchEditText.setLayoutParams(params);
        searchLayout.setPadding(100, 100, 100, 100);
        searchClose.setVisibility(View.VISIBLE);
        search.setVisibility(View.GONE);
        categoryNotes.setVisibility(View.GONE);
        restoreNotes.setVisibility(View.GONE);
        searchEditText.setQueryHint("Searching...");
        searchEditText.setQuery("", false);
        searchEditText.setIconified(true);
        searchEditText.setIconified(false);
    }

    private void hideSearchBar(){
        searchEditText.setQuery("", false);
        search.setVisibility(View.VISIBLE);

        fragmentTitle.setVisibility(View.VISIBLE);
        filterNotes.setVisibility(View.VISIBLE);
        searchLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        searchLayout.setCardBackgroundColor(context.getColor(R.color.gray));
        searchEditText.setVisibility(View.GONE);
        searchClose.setVisibility(View.GONE);
        if(isTrashSelected)
            restoreNotes.setVisibility(View.VISIBLE);
        search.setVisibility(View.VISIBLE);
        categoryNotes.setVisibility(View.VISIBLE);
        searchEditText.clearFocus();

        ViewGroup.MarginLayoutParams vlp = (ViewGroup.MarginLayoutParams) filterNotes.getLayoutParams();

        LinearLayout.LayoutParams params = (new LinearLayout.LayoutParams(filterNotes.getWidth(), filterNotes.getHeight()));
        params.setMargins(0, 0, vlp.rightMargin, 0);
        searchLayout.setLayoutParams(params);
        searchLayout.setCardBackgroundColor(context.getColor(R.color.light_gray));
        searchLayout.setPadding(filterNotes.getPaddingLeft(), filterNotes.getPaddingTop(), filterNotes.getPaddingRight(), filterNotes.getPaddingBottom());
        searchEditText.setVisibility(View.GONE);
        searchEditText.setVisibility(View.GONE);
        searchClose.setVisibility(View.GONE);
        searchEditText.clearFocus();
    }

    public void deleteMultipleNotesLayout(){
        enableSelectMultiple = true;
        addRegularMenu.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.red)));
        addRegularMenu.setImageDrawable(context.getDrawable(R.drawable.close_icon));
        search.setImageDrawable(context.getDrawable(R.drawable.delete_icon));
        filterIcon.setImageDrawable(context.getDrawable(R.drawable.select_all_icon));
        deletingMultipleNotes = true;
        menu.animate().alpha(0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                menu.setVisibility(View.GONE);
            }});
    }

    public void closeMultipleNotesLayout(){
        unSelectAllNotes();
        clearVariables();

        restoreNotes.setVisibility(View.GONE);
        fragmentTitle.setText("Dark Note");
        fragmentTitle.setTextSize(30);
        addRegularMenu.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.darker_blue)));
        addRegularMenu.setImageDrawable(context.getDrawable(R.drawable.add_icon));
        search.setImageDrawable(context.getDrawable(R.drawable.search_icon));
        filterNotes.setCardBackgroundColor(context.getColor(R.color.light_gray));
        filterIcon.setImageDrawable(context.getDrawable(R.drawable.filter_icon));
        categoryNotes.setCardBackgroundColor(context.getColor(R.color.light_gray));
        settings.setVisibility(View.VISIBLE);
    }

    public void deleteMultipleNotes(){
        RealmResults<Note> selectedNotes = realm.where(Note.class).equalTo("isSelected", true).findAll();
        RealmResults<Note> lockedNotes = realm.where(Note.class).equalTo("isSelected", true)
                .greaterThan("pinNumber", 0)
                .findAll();

        if(lockedNotes.size()>0){
            Helper.showMessage(getActivity(), "Locked Noted", "Locked notes " +
                    "cannot be deleted", MotionToast.TOAST_ERROR);
        }
        else {
            if (selectedNotes.size() != 0) {
                int number = selectedNotes.size();
                if (isTrashSelected) {
                    realm.beginTransaction();
                    selectedNotes.deleteAllFromRealm();
                    realm.commitTransaction();
                    isListEmpty(allNotes.size(), false);
                    numberSelected(0, 0, 0);
                    Helper.showMessage(getActivity(), "Deleted", number + " selected " +
                            "have been deleted", MotionToast.TOAST_SUCCESS);
                    closeMultipleNotesLayout();
                    showData();
                }
                else {
                    realm.beginTransaction();
                    selectedNotes.setBoolean("trash", true);
                    realm.commitTransaction();
                    isListEmpty(allNotes.size(), false);
                    Helper.showMessage(getActivity(), "Sent to trash", number + " selected " +
                            "have been sent to trash", MotionToast.TOAST_SUCCESS);
                    numberSelected(0, 0, 0);
                }
            }
            else
                Helper.showMessage(getActivity(), "Not Deleted", "Nothing was selected " +
                        "and thus not deleted", MotionToast.TOAST_ERROR);
            adapterNotes.notifyDataSetChanged();
        }
    }

    public void restoreMultipleNotes(){
        RealmResults<Note> selectedNotes = realm.where(Note.class).equalTo("isSelected", true)
                .equalTo("trash", true).findAll();

        if(selectedNotes.size()!=0) {
            int number = selectedNotes.size();
            realm.beginTransaction();
            selectedNotes.setBoolean("trash", false);
            realm.commitTransaction();
            adapterNotes.notifyDataSetChanged();
            Helper.showMessage(getActivity(), "Restored", number + " selected " +
                    "have been restored", MotionToast.TOAST_SUCCESS);
            refreshFragment(false);
        }
        else
            Helper.showMessage(getActivity(), "Not Restored", "Nothing was selected " +
                    "and thus not restored", MotionToast.TOAST_ERROR);
    }

    public void numberSelected(int add, int subtract, int number){
        int currentlySelected = 1;
        if(number==-1) {
            try {
                currentlySelected = Integer.parseInt(fragmentTitle.getText().toString()
                        .replaceAll("[^0-9]", "")) + add - subtract;
            } catch (Exception e) { }
        }
        else
            currentlySelected = number;

        fragmentTitle.setText(currentlySelected + " Selected");
        fragmentTitle.setTextSize(24);
    }

    public void unSelectAllNotes(){
        RealmResults<Note> realmResults = realm.where(Note.class).equalTo("isSelected", true).findAll();
        if(realmResults.size()!=0) {
            realm.beginTransaction();
            realmResults.setBoolean("isSelected", false);
            realm.commitTransaction();
        }
    }

    private void selectAllNotes(){
        isAllSelected = !isAllSelected;
        if(isTrashSelected){
            RealmResults<Note> realmResults = realm.where(Note.class).equalTo("trash", true).findAll();
            if (realmResults.size() != 0) {
                realm.beginTransaction();
                realmResults.setBoolean("isSelected", isAllSelected);
                realm.commitTransaction();
            }
        }
        else {
            if (allNotes.size() != 0) {
                realm.beginTransaction();
                allNotes.setBoolean("isSelected", isAllSelected);
                realm.commitTransaction();
            }
        }
        numberSelected(0, 0, isAllSelected ? adapterNotes.getItemCount() : 0);
        adapterNotes.notifyDataSetChanged();
    }

    private void refreshFragment(boolean refresh){
        if(refresh) {
            getActivity().finish();
            Intent refreshActivity = new Intent(getActivity(), getActivity().getClass());
            startActivity(refreshActivity);
            getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        else {
            getActivity().getSupportFragmentManager().beginTransaction().detach(this).attach(this).commit();
            clearVariables();
        }
    }

    private void clearVariables(){
        isSearchingNotes = false;
        searchEditText.setQuery("", false);
        isNotesFiltered = false;
        deletingMultipleNotes = false;
        isTrashSelected = false;
        searchingString = "";
        isAllSelected = false;
    }

    private void isListEmpty(int size, boolean isResult){
        Helper.isListEmpty(context, size, empty_Layout, title, subtitle, subSubTitle, isResult, false, false);
    }

    private void savePreferences(){
        // text size saved by user
        int savedSize = user.getTextSize();
        // text size set by user
        String textSize = Helper.getPreference(context, "size");
        int currentSize = Integer.parseInt(textSize==null ? "0" : textSize);

        // if device was backed up, then restore text size
        if(savedSize>0 && currentSize == 0) {
            Helper.savePreference(context, String.valueOf(savedSize), "size");
        }
        // if text size save b
        else if(savedSize != currentSize){
            realm.beginTransaction();
            user.setTextSize(currentSize);
            realm.commitTransaction();
        }

        String savedCategories = PreferenceManager.getDefaultSharedPreferences(context).getString("categories", null);
        String savedAutocomplete = PreferenceManager.getDefaultSharedPreferences(context).getString("autocomplete", null);

        if(savedCategories!=null && savedCategories.length()>0 && !savedCategories.equals(user.getCategories())){
            realm.beginTransaction();
            user.setCategories(savedCategories);
            realm.commitTransaction();
        }
        else if(user.getCategories()!=null && user.getCategories().length()>0)
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString("categories", user.getCategories()).apply();

        if(savedAutocomplete!=null && savedAutocomplete.length()>0 && !savedAutocomplete.equals(user.getLiveNoteAutoComplete())){
            realm.beginTransaction();
            user.setLiveNoteAutoComplete(savedAutocomplete);
            realm.commitTransaction();
        }
        else if(user.getLiveNoteAutoComplete()!=null && user.getLiveNoteAutoComplete().length()>0)
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString("autocomplete", user.getLiveNoteAutoComplete()).apply();

        if(user.isBackUpOnLaunch() && user.isProUser()){
            RealmBackupRestore backup = new RealmBackupRestore(getActivity());
            backup.update(getActivity(), context);
            realm.beginTransaction();
            user.setBackUpLocation(backup.saveBackUp());
            user.setLastUpdated(new SimpleDateFormat("E, MMM dd, yyyy hh:mm:ss aa").format(Calendar.getInstance().getTime()));
            realm.commitTransaction();
        }
    }

    private void showMessage(String title, String message, boolean error){
        if(error)
            Helper.showMessage(getActivity(), title, message, MotionToast.TOAST_ERROR);
        else
            Helper.showMessage(getActivity(), title, message, MotionToast.TOAST_SUCCESS);
    }
}