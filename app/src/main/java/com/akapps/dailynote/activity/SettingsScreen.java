package com.akapps.dailynote.activity;

import static com.akapps.dailynote.classes.helpers.UiHelper.getColorFromTheme;
import static com.akapps.dailynote.classes.helpers.UiHelper.getThemeStyle;
import static com.akapps.dailynote.classes.helpers.UiHelper.saveLightThemePreference;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.airbnb.lottie.LottieAnimationView;
import com.akapps.dailynote.R;
import com.akapps.dailynote.adapter.IconMenuAdapter;
import com.akapps.dailynote.classes.data.Backup;
import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.Photo;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmBackupRestore;
import com.akapps.dailynote.classes.helpers.RealmDatabase;
import com.akapps.dailynote.classes.helpers.RealmHelper;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.akapps.dailynote.classes.other.AccountSheet;
import com.akapps.dailynote.classes.other.CreditsSheet;
import com.akapps.dailynote.classes.other.IconPowerMenuItem;
import com.akapps.dailynote.classes.other.InfoSheet;
import com.akapps.dailynote.classes.other.LockSheet;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.skydoves.powermenu.CustomPowerMenu;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;
import kotlin.io.FilesKt;
import www.sanju.motiontoast.MotionToast;

public class SettingsScreen extends AppCompatActivity {

    // activity
    private Context context;
    private int all_Notes;
    private boolean tryAgain;
    private int upgradeToProCounter;

    private boolean isEditingChecklistSep;
    private boolean isEditingSublistSep;
    private boolean isEditingBudgetSymbol;
    private boolean isEditingExpenseSymbol;

    // account authentication
    private FirebaseAuth mAuth;

    // Toolbar
    private Toolbar toolbar;
    private ImageView close;
    private ImageView lockApp;

    // layout
    private LinearLayout backup;
    private LinearLayout restoreBackup;
    private LinearLayout backupBeta;
    private LinearLayout restoreBackupBeta;
    private LinearLayout appSettings;
    private LinearLayout syncLayout;
    private TextView titleLines;
    private TextView previewLines;
    private TextView checklistSeparator;
    private TextView sublistSeparator;
    private TextView budgetSymbol;
    private TextView expenseSymbol;
    private LinearLayout titleLayout;
    private LinearLayout previewLayout;
    private LinearLayout checklistSeparatorLayout;
    private LinearLayout sublistSeparatorLayout;
    private LinearLayout budgetSymbolLayout;
    private LinearLayout expenseSymbolLayout;
    private TextView accountText;
    private CustomPowerMenu linesMenu;
    private boolean isTitleSelected;
    private SwitchCompat showPreview;
    private SwitchCompat showPreviewNoteInfo;
    private SwitchCompat openFoldersOnStart;
    private SwitchCompat showFolderNotes;
    private MaterialButtonToggleGroup themeToggle;
    private SwitchCompat sublistMode;
    private SwitchCompat emptyNoteMode;
    private SwitchCompat fabButtonSizeMode;
    private SwitchCompat showScreenAnimation;
    private SwitchCompat showDeleteIcon;
    private SwitchCompat hideRichTextEditor;
    private SwitchCompat showAudioButton;
    private SwitchCompat hideBudgetButton;
    private SwitchCompat twentyFourHourFormatButton;
    private SwitchCompat editableNoteButton;
    private SwitchCompat enableSqaureStyleForChecklists;
    private SwitchCompat hideLastEditInfo;
    private TextView about;
    private MaterialButton signUp;
    private MaterialButton logIn;
    private MaterialButton sync;
    private MaterialButton upload;
    private MaterialButton darkMode;
    private MaterialButton grayMode;
    private MaterialButton lightMode;
    private TextView accountInfo;
    private TextView lastUploadDate;
    private Dialog progressDialog;
    private ImageView spaceOne;
    private ImageView spaceTwo;
    private MaterialCardView themeToggleLayout;
    private MaterialCardView grid;
    private MaterialCardView row;
    private MaterialCardView staggered;
    private MaterialCardView buyMeCoffeeLayout;
    private MaterialCardView accountLayout;
    private MaterialCardView backupRestoreLayout;
    private MaterialCardView notePreviewSettingsLayout;
    private MaterialCardView listTypeLayout;
    private MaterialCardView folderSettingsLayout;
    private MaterialCardView noteSettingLayout;
    private MaterialCardView checklistSettingLayout;
    private MaterialCardView appSettingsLayout;
    private MaterialCardView fabSizeLayout;
    private MaterialCardView animationLayout;
    private MaterialCardView contact;
    private MaterialCardView reddit;
    private MaterialCardView review;
    private MaterialCardView aboutInfoLayout;
    private LottieAnimationView coffeeAnimation;

    // variables
    private boolean betaBackup = false;
    private boolean betaRestore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(getThemeStyle(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_screen);

        context = this;
        mAuth = FirebaseAuth.getInstance();
        all_Notes = getIntent().getIntExtra("size", 0);
        boolean backingUp = getIntent().getBooleanExtra("backup", false);

        populateUserSettings();

        if (backingUp)
            showBackupRestoreInfo(6);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        RealmSingleton.closeRealmInstance("SettingsScreen onDestroy");
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        close();
    }

    private void initializeLayout() {
        toolbar = findViewById(R.id.toolbar);
        close = findViewById(R.id.close_activity);
        lockApp = findViewById(R.id.lock_app);
        backup = findViewById(R.id.backup);
        restoreBackup = findViewById(R.id.restore_backup);
        appSettings = findViewById(R.id.app_settings);
        contact = findViewById(R.id.contact);
        reddit = findViewById(R.id.reddit);
        review = findViewById(R.id.review);
        about = findViewById(R.id.about);
        titleLines = findViewById(R.id.title_lines);
        previewLines = findViewById(R.id.preview_lines);
        checklistSeparator = findViewById(R.id.item_separator);
        sublistSeparator = findViewById(R.id.sublist_sep);
        budgetSymbol = findViewById(R.id.budget_char);
        expenseSymbol = findViewById(R.id.expense_char);
        titleLayout = findViewById(R.id.title_layout);
        previewLayout = findViewById(R.id.preview_layout);
        checklistSeparatorLayout = findViewById(R.id.checklist_item_sep_layout);
        sublistSeparatorLayout = findViewById(R.id.sublist_item_sep_layout);
        budgetSymbolLayout = findViewById(R.id.budget_char_layout);
        expenseSymbolLayout = findViewById(R.id.expense_char_layout);
        showPreview = findViewById(R.id.show_preview_switch);
        showPreviewNoteInfo = findViewById(R.id.show_info_switch);
        openFoldersOnStart = findViewById(R.id.open_folder_switch);
        showFolderNotes = findViewById(R.id.show_folder_switch);
        themeToggle = findViewById(R.id.theme_mode_toggle_group);
        themeToggleLayout = findViewById(R.id.theme_mode_toggle_group_layout);
        darkMode = findViewById(R.id.dark_mode);
        grayMode = findViewById(R.id.gray_mode);
        lightMode = findViewById(R.id.light_mode);
        sublistMode = findViewById(R.id.sublists_switch);
        emptyNoteMode = findViewById(R.id.empty_note_switch);
        fabButtonSizeMode = findViewById(R.id.fab_switch);
        showScreenAnimation = findViewById(R.id.animation_switch);
        showDeleteIcon = findViewById(R.id.add_delete_icon_switch);
        hideRichTextEditor = findViewById(R.id.rich_text_switch);
        showAudioButton = findViewById(R.id.audio_button_switch);
        hideBudgetButton = findViewById(R.id.hide_budget_switch);
        twentyFourHourFormatButton = findViewById(R.id.twenty_hour_format_switch);
        editableNoteButton = findViewById(R.id.editable_note_switch);
        enableSqaureStyleForChecklists = findViewById(R.id.checkbox_style_switch);
        hideLastEditInfo = findViewById(R.id.last_edit_info_switch);
        grid = findViewById(R.id.grid);
        row = findViewById(R.id.row);
        staggered = findViewById(R.id.staggered);
        backupBeta = findViewById(R.id.backup_beta);
        restoreBackupBeta = findViewById(R.id.restore_beta_backup);
        syncLayout = findViewById(R.id.logged_in_layout);
        signUp = findViewById(R.id.sign_up);
        logIn = findViewById(R.id.log_in);
        sync = findViewById(R.id.sync);
        upload = findViewById(R.id.upload);
        accountInfo = findViewById(R.id.account_name);
        lastUploadDate = findViewById(R.id.last_upload);
        spaceOne = findViewById(R.id.space_one);
        spaceTwo = findViewById(R.id.space_two);
        accountLayout = findViewById(R.id.account_layout);
        accountText = findViewById(R.id.account_settings);
        buyMeCoffeeLayout = findViewById(R.id.buy_me_coffee_layout);
        accountLayout = findViewById(R.id.account_layout);
        backupRestoreLayout = findViewById(R.id.back_up_layout);
        notePreviewSettingsLayout = findViewById(R.id.note_preview_settings_layout);
        listTypeLayout = findViewById(R.id.view_layout);
        folderSettingsLayout = findViewById(R.id.folder_settings_layout);
        noteSettingLayout = findViewById(R.id.checklist_setting_layout);
        checklistSettingLayout = findViewById(R.id.note_setting_layout);
        appSettingsLayout = findViewById(R.id.materialCardView2);
        fabSizeLayout = findViewById(R.id.fab_setting);
        animationLayout = findViewById(R.id.animation_setting);
        aboutInfoLayout = findViewById(R.id.about_info);
        coffeeAnimation = findViewById(R.id.coffee_moving);

        if (!Helper.isTablet(context)) {
            MaterialCardView coffee = findViewById(R.id.coffee_button);
            TextView coffeeText = findViewById(R.id.support_me_message);
            coffeeText.setOnClickListener(view -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.buymeacoffee.com/akapps"));
                startActivity(browserIntent);
            });
            coffee.setOnClickListener(view -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.buymeacoffee.com/akapps"));
                startActivity(browserIntent);
            });
        }

        if(AppData.isDisableAnimation) {
            coffeeAnimation.pauseAnimation();
            ((LottieAnimationView)findViewById(R.id.version_icon)).pauseAnimation();
        }
        else
            Helper.moveAnimation(findViewById(R.id.version_icon), 300f);

        User currentUser = getUser();
        if (null == currentUser.getEmail()) {
            RealmSingleton.get(this).beginTransaction();
            currentUser.setEmail("");
            RealmSingleton.get(this).commitTransaction();
        }

        if (currentUser.isUltimateUser()) {
            accountLayout.setVisibility(View.VISIBLE);
            accountText.setVisibility(View.VISIBLE);
            if (mAuth.getCurrentUser() != null) {
                if (mAuth.getCurrentUser().isEmailVerified()) {
                    signUp.setVisibility(View.GONE);
                    logIn.setText("Log Out");
                    syncLayout.setVisibility(View.VISIBLE);
                    accountInfo.setVisibility(View.VISIBLE);
                    accountInfo.setText(mAuth.getCurrentUser().getEmail());
                    spaceOne.setVisibility(View.VISIBLE);
                    spaceTwo.setVisibility(View.VISIBLE);

                    if (null != currentUser.getLastUpload() && !currentUser.getLastUpload().isEmpty()) {
                        lastUploadDate.setVisibility(View.VISIBLE);
                        lastUploadDate.setText("Last Upload : " + currentUser.getLastUpload().replaceAll("\n", " "));
                    }
                }
            }
        } else {
            accountLayout.setVisibility(View.GONE);
            accountText.setVisibility(View.GONE);
        }

        String titleLinesNumber = String.valueOf(currentUser.getTitleLines());
        String previewLinesNumber = String.valueOf(currentUser.getContentLines());
        String checklistSeparatorText = currentUser.getItemsSeparator();
        String sublistSeparatorText = currentUser.getSublistSeparator();
        String budgetSymbolText = currentUser.getBudgetCharacter();
        String expenseSymbolText = currentUser.getExpenseCharacter();

        // sets the current select title lines and preview lines
        // by default it is 3
        titleLines.setText(titleLinesNumber);
        previewLines.setText(previewLinesNumber);
        checklistSeparator.setText(checklistSeparatorText);
        sublistSeparator.setText(sublistSeparatorText);
        budgetSymbol.setText(budgetSymbolText);
        expenseSymbol.setText(expenseSymbolText);

        // toolbar
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }

    private void initializeButtonListeners(){
        signUp.setOnClickListener(view -> {
            if (getUser().isUltimateUser()) {
                if (mAuth.getCurrentUser() == null) {
                    AccountSheet accountLoginSheet = new AccountSheet(mAuth, true);
                    accountLoginSheet.show(getSupportFragmentManager(), accountLoginSheet.getTag());
                }
            }
        });

        logIn.setOnClickListener(view -> {
            if (mAuth.getCurrentUser() != null) {
                showBackupRestoreInfo(8);
            } else {
                AccountSheet accountLoginSheet = new AccountSheet(mAuth, false);
                accountLoginSheet.show(getSupportFragmentManager(), accountLoginSheet.getTag());
            }
        });

        sync.setOnClickListener(view -> {
            if (mAuth.getCurrentUser() != null && getUser().isUltimateUser())
                showBackupRestoreInfo(7);
        });

        upload.setOnClickListener(view -> {
            if (mAuth.getCurrentUser() != null && getUser().isUltimateUser())
                showBackupRestoreInfo(6);
        });

        backup.setOnClickListener(v -> {
            betaBackup = false;
            showBackupRestoreInfo(1);
        });

        backupBeta.setOnClickListener(view -> {
            betaBackup = true;
            showBackupRestoreInfo(2);
        });

        restoreBackup.setOnClickListener(v -> {
            betaRestore = false;
            openFile();
        });

        restoreBackupBeta.setOnClickListener(view -> {
            betaRestore = true;
            openFile();
        });

        titleLayout.setOnClickListener(v -> {
            isTitleSelected = true;
            showLineNumberMenu(titleLines, null);
        });

        previewLayout.setOnClickListener(v -> {
            isTitleSelected = false;
            showLineNumberMenu(previewLines, null);
        });

        checklistSeparatorLayout.setOnClickListener(v -> {
            List<IconPowerMenuItem> options = new ArrayList<>();
            options.add(new IconPowerMenuItem(null, ",,"));
            options.add(new IconPowerMenuItem(null, "newline"));
            isEditingChecklistSep = true;
            expandListMenu(options, checklistSeparator);
        });

        sublistSeparatorLayout.setOnClickListener(v -> {
            List<IconPowerMenuItem> options = new ArrayList<>();
            options.add(new IconPowerMenuItem(null, "--"));
            options.add(new IconPowerMenuItem(null, "space"));
            isEditingSublistSep = true;
            expandListMenu(options, sublistSeparator);
        });

        budgetSymbolLayout.setOnClickListener(v -> {
            List<IconPowerMenuItem> options = new ArrayList<>();
            options.add(new IconPowerMenuItem(null, "+$"));
            options.add(new IconPowerMenuItem(null, "+₹"));
            options.add(new IconPowerMenuItem(null, "+£"));
            options.add(new IconPowerMenuItem(null, "+€"));
            options.add(new IconPowerMenuItem(null, "+¥"));
            isEditingBudgetSymbol = true;
            expandListMenu(options, budgetSymbol);
        });

        expenseSymbolLayout.setOnClickListener(v -> {
            List<IconPowerMenuItem> options = new ArrayList<>();
            options.add(new IconPowerMenuItem(null, "$"));
            options.add(new IconPowerMenuItem(null, "₹"));
            options.add(new IconPowerMenuItem(null, "£"));
            options.add(new IconPowerMenuItem(null, "€"));
            options.add(new IconPowerMenuItem(null, "¥"));
            isEditingExpenseSymbol = true;
            expandListMenu(options, expenseSymbol);
        });

        appSettings.setOnClickListener(v -> openAppInSettings());

        row.setOnClickListener(v -> {
            RealmSingleton.get(this).beginTransaction();
            getUser().setLayoutSelected("row");
            RealmSingleton.get(this).commitTransaction();
            toggleLayoutSelected();
        });

        grid.setOnClickListener(v -> {
            RealmSingleton.get(this).beginTransaction();
            getUser().setLayoutSelected("grid");
            RealmSingleton.get(this).commitTransaction();
            toggleLayoutSelected();
        });

        staggered.setOnClickListener(v -> {
            if (!getUser().getLayoutSelected().equals("stag")) {
                RealmSingleton.get(this).beginTransaction();
                getUser().setLayoutSelected("stag");
                RealmSingleton.get(this).commitTransaction();
                toggleLayoutSelected();
            }
        });

        themeToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            User.Mode currentMode = User.Mode.Dark;
            if (isChecked) {
                if(checkedId == R.id.gray_mode)
                    currentMode = User.Mode.Gray;
                else if(checkedId == R.id.light_mode)
                    currentMode = User.Mode.Light;
                RealmSingleton.get(SettingsScreen.this).beginTransaction();
                getUser().setScreenMode(currentMode.getValue());
                RealmSingleton.get(SettingsScreen.this).commitTransaction();
                saveLightThemePreference(context, currentMode);
                Helper.updateAllWidgetTypes(context);
                Helper.restart(this);
            }
        });

        contact.setOnClickListener(v -> contactMe());

        reddit.setOnClickListener(v -> openReddit());

        review.setOnClickListener(v -> openAppInPlayStore());

        close.setOnClickListener(v -> close());

        lockApp.setOnClickListener(view -> {
            if (getUser().getPinNumber() == 0) {
                LockSheet lockSheet = new LockSheet(true);
                lockSheet.show(getSupportFragmentManager(), lockSheet.getTag());
            } else
                unLockNote();
        });

        showPreview.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RealmSingleton.get(this).beginTransaction();
            getUser().setShowPreview(isChecked);
            RealmSingleton.get(this).commitTransaction();
        });

        showPreviewNoteInfo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RealmSingleton.get(this).beginTransaction();
            getUser().setShowPreviewNoteInfo(isChecked);
            RealmSingleton.get(this).commitTransaction();
        });

        openFoldersOnStart.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppData.isAppFirstStarted = false;
            RealmSingleton.get(this).beginTransaction();
            getUser().setOpenFoldersOnStart(isChecked);
            RealmSingleton.get(this).commitTransaction();
        });

        showFolderNotes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RealmSingleton.get(this).beginTransaction();
            getUser().setShowFolderNotes(isChecked);
            RealmSingleton.get(this).commitTransaction();
        });

        hideRichTextEditor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RealmSingleton.get(this).beginTransaction();
            getUser().setHideRichTextEditor(isChecked);
            RealmSingleton.get(this).commitTransaction();
        });

        showAudioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RealmSingleton.get(this).beginTransaction();
            getUser().setShowAudioButton(isChecked);
            RealmSingleton.get(this).commitTransaction();
        });

        hideBudgetButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RealmSingleton.get(this).beginTransaction();
            getUser().setHideBudget(isChecked);
            RealmSingleton.get(this).commitTransaction();
        });

        twentyFourHourFormatButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RealmSingleton.get(this).beginTransaction();
            getUser().setTwentyFourHourFormat(isChecked);
            RealmSingleton.get(this).commitTransaction();
        });

        editableNoteButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RealmSingleton.get(this).beginTransaction();
            getUser().setEnableEditableNoteButton(isChecked);
            RealmSingleton.get(this).commitTransaction();
        });

        enableSqaureStyleForChecklists.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RealmSingleton.get(this).beginTransaction();
            getUser().setShowChecklistCheckbox(isChecked);
            RealmSingleton.get(this).commitTransaction();
        });

        hideLastEditInfo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RealmSingleton.get(this).beginTransaction();
            getUser().setDisableLastEditInfo(isChecked);
            RealmSingleton.get(this).commitTransaction();
        });

        sublistMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RealmSingleton.get(this).beginTransaction();
            getUser().setEnableSublists(isChecked);
            RealmSingleton.get(this).where(Note.class).findAll().setBoolean("enableSublist", isChecked);
            RealmSingleton.get(this).commitTransaction();
        });

        emptyNoteMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RealmSingleton.get(this).beginTransaction();
            getUser().setEnableEmptyNote(isChecked);
            RealmSingleton.get(this).commitTransaction();
        });

        fabButtonSizeMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RealmSingleton.get(this).beginTransaction();
            getUser().setIncreaseFabSize(isChecked);
            RealmSingleton.get(this).commitTransaction();
        });

        showScreenAnimation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RealmSingleton.get(this).beginTransaction();
            getUser().setDisableAnimation(isChecked);
            RealmSingleton.get(this).commitTransaction();
            AppData.isDisableAnimation = isChecked;
            Helper.restart(this);
        });

        showDeleteIcon.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RealmSingleton.get(this).beginTransaction();
            getUser().setEnableDeleteIcon(isChecked);
            RealmSingleton.get(this).commitTransaction();
        });

        about.setOnClickListener(v -> {
            upgradeToProCounter++;
            if (upgradeToProCounter == 1) {
                CreditsSheet creditSheet = new CreditsSheet();
                creditSheet.show(getSupportFragmentManager(), creditSheet.getTag());
            }
        });

        about.setOnLongClickListener(v -> {
            if (upgradeToProCounter == 12)
                upgradeToPro();
            return false;
        });
    }

    // hidden feature
    private void upgradeToPro() {
        User currentUser = getUser();
        RealmSingleton.get(this).beginTransaction();
        currentUser.setUltimateUser(!currentUser.isUltimateUser());
        RealmSingleton.get(this).commitTransaction();

        if (currentUser.isUltimateUser())
            Helper.showMessage(SettingsScreen.this, "Upgrade Successful", "" +
                    "Thank you and Enjoy!\uD83D\uDE04", MotionToast.TOAST_SUCCESS);
        else
            Helper.showMessage(SettingsScreen.this, "Downgrade Successful", "" +
                    "Enjoy!\uD83D\uDE04", MotionToast.TOAST_SUCCESS);

        Helper.restart(this);
    }

    private void populateUserSettings() {
        initializeLayout();
        initializeSettings();
        initializeButtonListeners();
    }

    private void updateTheme() {
        UiHelper.setStatusBarColor(this);
        User.Mode currentTheme = getUser().getScreenMode();
        if (currentTheme == User.Mode.Dark) {
            toggleCurrentTheme(currentTheme, darkMode.getId());
        } else if (currentTheme == User.Mode.Gray) {
            toggleCurrentTheme(currentTheme, grayMode.getId());
        } else if (currentTheme == User.Mode.Light) {
            toggleCurrentTheme(currentTheme, lightMode.getId());
        }
        toggleLayoutSelected();
    }

    private void toggleLayoutSelected() {
        String selectedLayout = getUser().getLayoutSelected();
        int unSelectedBackgroundColor = getColorFromTheme(this, R.attr.secondaryBackgroundColor);
        int selectedBackgroundColor = getColorFromTheme(this, R.attr.primarySelectionColor);
        row.setCardBackgroundColor(unSelectedBackgroundColor);
        grid.setCardBackgroundColor(unSelectedBackgroundColor);
        staggered.setCardBackgroundColor(unSelectedBackgroundColor);
        if (selectedLayout.equals("row"))
            row.setCardBackgroundColor(selectedBackgroundColor);
        else if (selectedLayout.equals("grid"))
            grid.setCardBackgroundColor(selectedBackgroundColor);
        else
            staggered.setCardBackgroundColor(selectedBackgroundColor);
    }

    private void toggleCurrentTheme(User.Mode currentTheme, int toToggle){
        themeToggle.check(toToggle);
        if (currentTheme == User.Mode.Dark) {
            darkMode.setIconTint(ColorStateList.valueOf(getColorFromTheme(this, R.attr.primarySelectedIconColor)));
            grayMode.setIconTint(ColorStateList.valueOf(getColorFromTheme(this, R.attr.primaryUnSelectedIconColor)));
            lightMode.setIconTint(ColorStateList.valueOf(getColorFromTheme(this, R.attr.primaryUnSelectedIconColor)));
        } else if (currentTheme == User.Mode.Gray) {
            grayMode.setIconTint(ColorStateList.valueOf(getColorFromTheme(this, R.attr.primarySelectedIconColor)));
            darkMode.setIconTint(ColorStateList.valueOf(getColorFromTheme(this, R.attr.primaryUnSelectedIconColor)));
            lightMode.setIconTint(ColorStateList.valueOf(getColorFromTheme(this, R.attr.primaryUnSelectedIconColor)));
        } else if (currentTheme == User.Mode.Light) {
            lightMode.setIconTint(ColorStateList.valueOf(getColorFromTheme(this, R.attr.primarySelectedIconColor)));
            grayMode.setIconTint(ColorStateList.valueOf(getColorFromTheme(this, R.attr.primaryUnSelectedIconColor)));
            darkMode.setIconTint(ColorStateList.valueOf(getColorFromTheme(this, R.attr.primaryUnSelectedIconColor)));
        }
    }

    private void initializeSettings() {
        User currentUser = getUser();
        showPreview.setChecked(currentUser.isShowPreview());
        showPreviewNoteInfo.setChecked(currentUser.isShowPreviewNoteInfo());
        openFoldersOnStart.setChecked(currentUser.isOpenFoldersOnStart());
        showFolderNotes.setChecked(currentUser.isShowFolderNotes());
        int getSelectTheme = R.id.dark_mode;
        if(currentUser.getScreenMode() == User.Mode.Gray)
            getSelectTheme = R.id.gray_mode;
        else if(currentUser.getScreenMode() == User.Mode.Light)
            getSelectTheme = R.id.light_mode;
        themeToggle.check(getSelectTheme);
        sublistMode.setChecked(currentUser.isEnableSublists());
        emptyNoteMode.setChecked(currentUser.isEnableEmptyNote());
        fabButtonSizeMode.setChecked(currentUser.isIncreaseFabSize());
        showScreenAnimation.setChecked(currentUser.isDisableAnimation());
        showDeleteIcon.setChecked(currentUser.isEnableDeleteIcon());
        hideRichTextEditor.setChecked(currentUser.isHideRichTextEditor());
        showAudioButton.setChecked(currentUser.isShowAudioButton());
        hideBudgetButton.setChecked(currentUser.isHideBudget());
        twentyFourHourFormatButton.setChecked(currentUser.isTwentyFourHourFormat());
        editableNoteButton.setChecked(currentUser.isEnableEditableNoteButton());
        enableSqaureStyleForChecklists.setChecked(currentUser.isShowChecklistCheckbox());
        hideLastEditInfo.setChecked(currentUser.isDisableLastEditInfo());
        if (currentUser.getPinNumber() > 0)
            lockApp.setImageDrawable(getDrawable(R.drawable.lock_icon));
        else
            lockApp.setImageDrawable(getDrawable(R.drawable.unlock_icon));
        updateTheme();
    }

    public boolean isBackupPermissionEnabled(){
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT != Build.VERSION_CODES.TIRAMISU;
    }

    public void openBackUpRestoreDialog() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED && Build.VERSION.SDK_INT != Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
        } else
            openBackup();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                openBackup();
            else
                Helper.showMessage(this, "Accept Permission", "You need " +
                        "to accept permissions to backup", MotionToast.TOAST_ERROR);
        }
    }

    public void lockNote(int pin, String securityWord, boolean fingerprint) {
        RealmSingleton.get(this).beginTransaction();
        getUser().setPinNumber(pin);
        getUser().setSecurityWord(securityWord);
        getUser().setFingerprint(fingerprint);
        RealmSingleton.get(this).commitTransaction();
        Helper.showMessage(this, "App Locked", "App has been " +
                "locked", MotionToast.TOAST_SUCCESS);
        lockApp.setImageDrawable(getDrawable(R.drawable.lock_icon));
        lockApp.setColorFilter(UiHelper.getColorFromTheme(this, R.attr.primaryIconTintColor));
    }

    public void unLockNote() {
        RealmSingleton.get(this).beginTransaction();
        getUser().setPinNumber(0);
        getUser().setSecurityWord("");
        getUser().setFingerprint(false);
        RealmSingleton.get(this).commitTransaction();
        Helper.showMessage(this, "App un-Locked", "App has been " +
                "un-locked", MotionToast.TOAST_SUCCESS);
        lockApp.setImageDrawable(getDrawable(R.drawable.unlock_icon));
        lockApp.setColorFilter(UiHelper.getColorFromTheme(this, R.attr.primaryIconTintColor));
    }

    private void openBackup() {
        if (betaBackup)
            backUpDataAndImages();
        else
            backUpData();
    }

    private void backUpData() {
        if (all_Notes != 0) {
            RealmSingleton.get(this).close();
            RealmBackupRestore realmBackupRestore = new RealmBackupRestore(this);
            realmBackupRestore.update(this, context);
            File exportedFilePath = realmBackupRestore.backup_Share();
            shareFile(exportedFilePath);
        } else
            Helper.showMessage(this, "Backup Failed", "\uD83D\uDE10No " +
                    "data to backup\uD83D\uDE10", MotionToast.TOAST_ERROR);
    }

    private void backUpDataAndImages() {
        if (all_Notes != 0)
            shareFile(new File(backUpZip()));
        else
            Helper.showMessage(this, "Backup Failed", "\uD83D\uDE10No " +
                    "data to backup\uD83D\uDE10", MotionToast.TOAST_ERROR);
    }

    private String backUpZip() {
        RealmResults<Note> checklistPhotos = RealmSingleton.get(this).where(Note.class).findAll();
        ArrayList<String> allFiles = new ArrayList<>();
        ArrayList<String> allPhotos = getAllFilePaths(RealmSingleton.get(this).where(Photo.class)
                .not().isNull("photoLocation").or().equalTo("photoLocation", "")
                .findAll(), checklistPhotos);
        ArrayList<String> allRecordings = getAllRecordingsPaths(RealmSingleton.get(this).where(CheckListItem.class).not()
                .isNull("audioPath").or().equalTo("audioPath", "").findAll());
        RealmSingleton.get(this).close();
        RealmBackupRestore realmBackupRestore = new RealmBackupRestore(this);
        realmBackupRestore.update(this, context);
        File exportedFilePath = realmBackupRestore.backup_Share();
        // add all photos paths
        allFiles.addAll(allPhotos);
        // add realm path (this contains all the note data)
        allFiles.add(exportedFilePath.getAbsolutePath());
        // add all recording paths
        allFiles.addAll(allRecordings);
        return zipPhotos(allFiles);
    }

    private ArrayList<String> getAllFilePaths(RealmResults<Photo> allNotePhotos, RealmResults<Note> allNotes) {
        ArrayList<String> allPhotos = new ArrayList<>();
        for (int i = 0; i < allNotePhotos.size(); i++)
            allPhotos.add(allNotePhotos.get(i).getPhotoLocation());

        for (int i = 0; i < allNotes.size(); i++) {
            RealmList<CheckListItem> currentNoteChecklist = allNotes.get(i).getChecklist();
            if (currentNoteChecklist.size() > 0) {
                for (int j = 0; j < currentNoteChecklist.size(); j++) {
                    CheckListItem currentChecklistItem = currentNoteChecklist.get(j);
                    if (currentChecklistItem.getItemImage() != null && !currentChecklistItem.getItemImage().isEmpty())
                        allPhotos.add(currentChecklistItem.getItemImage());
                }
            }
        }

        return allPhotos;
    }

    private ArrayList<String> getAllRecordingsPaths(RealmResults<CheckListItem> allChecklistRecordings) {
        ArrayList<String> allRecordings = new ArrayList<>();
        for (int i = 0; i < allChecklistRecordings.size(); i++)
            allRecordings.add(allChecklistRecordings.get(i).getAudioPath());

        return allRecordings;
    }

    // creates a zip folder
    public String createZipFolder() {
        File storageDir = new File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        return storageDir.getAbsolutePath();
    }

    // places all the photos in a zip file and returns a string of the file path
    public String zipPhotos(ArrayList<String> files) {
        String zipPath = createZipFolder() + ".zip";
        File zipFile = new File(zipPath);

        int BUFFER = 1024;
        try {
            BufferedInputStream origin;
            FileOutputStream dest = new FileOutputStream(zipFile);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            byte[] data = new byte[BUFFER];
            for (int i = 0; i < files.size(); i++) {
                File newFile = new File(files.get(i));
                if (newFile.exists()) {
                    String canonicalPath = newFile.getCanonicalPath();
                    if (!canonicalPath.startsWith(zipPath.replace(".zip", ""))) {
                        throw new SecurityException("Zipping Error");
                    } else {
                        FileInputStream fi = new FileInputStream(files.get(i));
                        origin = new BufferedInputStream(fi, BUFFER);
                        ZipEntry entry = new ZipEntry(files.get(i).substring(files.get(i).lastIndexOf("/") + 1));
                        out.putNextEntry(entry);
                        int count;
                        while ((count = origin.read(data, 0, BUFFER)) != -1) {
                            out.write(data, 0, count);
                        }
                        origin.close();
                    }
                }
            }

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return zipFile.getAbsolutePath();
    }

    public void upLoadData() {
        AtomicReference<User> currentUser = new AtomicReference<>(getUser());
        File backupFile = new File(backUpZip());
        Uri file = Uri.fromFile(backupFile);
        // file info
        String fileSize = Helper.getFormattedFileSize(context, backupFile.length());

        if (!fileSize.toLowerCase().contains("b") && !fileSize.toLowerCase().contains("kb")
                && !fileSize.toLowerCase().contains("mb")) {
            Helper.showMessage(this, "Upload Failed", "File size is too big, backup locally",
                    MotionToast.TOAST_ERROR);
            return;
        }

        if (fileSize.toLowerCase().contains("mb")) {
            try {
                double fileSizeNumber = Double.parseDouble(fileSize.toLowerCase().replace("mb", "").trim());
                if (fileSizeNumber > 100.0) {
                    Helper.showMessage(this, "Upload Failed", "File size is too big, backup locally",
                            MotionToast.TOAST_ERROR);
                    return;
                }
            } catch (Exception e) {
            }
        }

        progressDialog = Helper.showLoading("Uploading...\n" + fileSize,
                progressDialog, context, true);

        String currentDate = Helper.getBackupDate(fileSize);
        String fileName = currentDate + "_backup.zip";

        // check if file exists
        if (RealmSingleton.get(this).where(Backup.class).equalTo("fileName", fileName).count() == 1) {
            Helper.showLoading("", progressDialog, context, false);
            Helper.showMessage(this, "Upload Failed", "File name exists, " +
                    "please wait a minute and try again", MotionToast.TOAST_ERROR);
        } else {
            String userEmail = mAuth.getCurrentUser().getEmail();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child("users")
                    .child(userEmail)
                    .child(fileName);
            UploadTask uploadTask = storageRef.putFile(file);

            uploadTask.addOnProgressListener(snapshot -> {
                String bytesTransferredFormatted = Helper.getFormattedFileSize(context, snapshot.getBytesTransferred());
                progressDialog = Helper.showLoading("Uploading...\n" + bytesTransferredFormatted + " / " + fileSize,
                        progressDialog, context, true);
            }).addOnFailureListener(exception -> {
                progressDialog.cancel();
                Helper.showMessage(this, "Upload error",
                        "Error Uploading data, try again",
                        MotionToast.TOAST_ERROR);
                Helper.restart(this);
            }).addOnSuccessListener(taskSnapshot -> {
                currentUser.set(getUser());
                String bytesTransferredFormatted = Helper.getFormattedFileSize(context, taskSnapshot.getBytesTransferred());
                if (bytesTransferredFormatted.equals(fileSize)) {
                    RealmSingleton.get(this).beginTransaction();
                    RealmSingleton.get(this).insert(new Backup(currentUser.get().getUserId(), fileName, new Date(), 0));
                    currentUser.get().setLastUpload(Helper.getCurrentDate());
                    RealmSingleton.get(this).commitTransaction();
                    lastUploadDate.setVisibility(View.VISIBLE);
                    lastUploadDate.setText("Last Upload : " + currentUser.get().getLastUpload().replaceAll("\n", " "));
                    progressDialog.cancel();
                    Helper.showMessage(SettingsScreen.this, "Upload Success",
                            "Data Uploaded",
                            MotionToast.TOAST_SUCCESS);
                } else {
                    // deleting file since it was missing files
                    storageRef.delete().addOnSuccessListener(aVoid -> {
                            })
                            .addOnFailureListener(exception -> {
                            });
                    progressDialog.cancel();
                    Helper.showMessage(SettingsScreen.this, "Upload Error",
                            "Files lost in transfer, please upload again!",
                            MotionToast.TOAST_ERROR);
                }
            });
        }
    }

    private void showBackupRestoreInfo(int selection) {
        InfoSheet info = new InfoSheet(selection);
        info.show(getSupportFragmentManager(), info.getTag());
    }

    private void openFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, 1);
    }

    private void shareFile(File backup) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("*/*");

        Uri fileBackingUp = FileProvider.getUriForFile(
                context,
                "com.akapps.dailynote.fileprovider",
                backup);
        emailIntent.putExtra(Intent.EXTRA_STREAM, fileBackingUp);

        startActivity(emailIntent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (betaRestore)
                restoreBackupBeta(data);
            else
                restoreBackup(data);
        }
    }

    public void restoreFromDatabase(String fileName, String fileSize) {
        progressDialog = Helper.showLoading("Syncing...", progressDialog, context, true);
        String userEmail = mAuth.getCurrentUser().getEmail();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference()
                .child("users/" + userEmail + "/" + fileName);

        File storageDir = new File(context
                .getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/Dark Note");

        if (!storageDir.exists())
            storageDir.mkdirs();

        File localFile = new File(storageDir, "backup.zip");
        storageRef.getFile(localFile)
                .addOnProgressListener(snapshot -> {
                    String bytesTransferredFormatted = Helper.getFormattedFileSize(context, snapshot.getBytesTransferred());
                    progressDialog = Helper.showLoading("Syncing...\n" + bytesTransferredFormatted + " / " + fileSize,
                            progressDialog, context, true);
                }).addOnSuccessListener(taskSnapshot -> {
                    restoreFromDatabase(Uri.fromFile(localFile));
                }).addOnFailureListener(exception -> {
                    Helper.showLoading("", progressDialog, context, false);
                    Helper.showMessage(SettingsScreen.this, "Error", "" +
                            "Restoring Error from database, please clear app storage & try again", MotionToast.TOAST_ERROR);
                });
    }

    private void restoreFromDatabase(Uri uri) {
        RealmBackupRestore realmBackupRestore = new RealmBackupRestore(this);
        try {
            // close realm before restoring
            RealmConfiguration configuration = RealmSingleton.get(this).getConfiguration();
            RealmSingleton.get(this).close();
            Realm.deleteRealm(configuration);
            // initialize backup object
            realmBackupRestore = new RealmBackupRestore(this);
            // make a copy of the backup zip file selected by user and unzip it
            realmBackupRestore.restore(uri, "backup.zip", true);

            Helper.showLoading("", progressDialog, context, false);

            ArrayList<String> images = realmBackupRestore.getImagesPath();
            ArrayList<String> recordings = realmBackupRestore.getRecordingsPath();
            realmBackupRestore.copyBundledRealmFile(realmBackupRestore.getBackupPath(), "default.realm");

            Helper.showMessage(this, "Restored", "" + "Notes have been restored",
                    MotionToast.TOAST_SUCCESS);

            // update image paths from restored database so it knows where the images are
            Realm realm = RealmDatabase.setUpDatabase(context);
            updateAlarms(realm.where(Note.class)
                    .equalTo("archived", false)
                    .equalTo("trash", false).findAll());
            updateImages(images);
            updateRecordings(recordings);
            resetWidgets();

            // delete all zip files
            Helper.deleteZipFile(context);
            close();
        } catch (Exception e) {
            Helper.showLoading("", progressDialog, context, false);
            Helper.showMessage(this, "Error", "" +
                    "Restoring Error to device, try again", MotionToast.TOAST_ERROR);
        }
    }

    private void restoreBackupBeta(Intent data) {
        if (data != null) {
            Uri uri = data.getData();
            Cursor returnCursor = context.getContentResolver()
                    .query(uri, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            String fileName = returnCursor.getString(nameIndex);

            if (fileName.contains(".zip")) {
                RealmBackupRestore realmBackupRestore = new RealmBackupRestore(this);
                try {
                    // close realm before restoring
                    RealmConfiguration configuration = RealmSingleton.get(this).getConfiguration();
                    RealmSingleton.get(this).close();
                    Realm.deleteRealm(configuration);

                    // delete all files first
                    FilesKt.deleteRecursively(new File(context.getExternalFilesDir(null) + ""));

                    // initialize backup object
                    realmBackupRestore = new RealmBackupRestore(this);
                    // make a copy of the backup zip file selected by user and unzip it
                    realmBackupRestore.restore(uri, "backup.zip", true);

                    ArrayList<String> images = realmBackupRestore.getImagesPath();
                    ArrayList<String> recordings = realmBackupRestore.getRecordingsPath();
                    realmBackupRestore.copyBundledRealmFile(realmBackupRestore.getBackupPath(), "default.realm");

                    // update image paths from restored database so it knows where the images are
                    Realm realm = RealmSingleton.getInstance(context);
                    updateAlarms(realm.where(Note.class)
                            .equalTo("archived", false)
                            .equalTo("trash", false).findAll());
                    updateImages(images);
                    updateRecordings(recordings);
                    resetWidgets();

                    Helper.showMessage(this, "Restored", "" +
                            "Notes have been restored", MotionToast.TOAST_SUCCESS);

                    Helper.showLoading("", progressDialog, context, false);

                    // delete all zip files
                    Helper.deleteZipFile(context);

                    close();
                } catch (Exception e) {
                    Helper.showLoading("", progressDialog, context, false);
                    if (tryAgain) {
                        Helper.showMessage(this, "Error", "Clear storage via App Settings",
                                MotionToast.TOAST_ERROR);
                        appSettings.setBackgroundColor(UiHelper.getColorFromTheme(this, R.attr.primaryButtonColor));
                        tryAgain = false;
                    } else {
                        Helper.showMessage(this, "Error", "attempting to fix error...try again",
                                MotionToast.TOAST_ERROR);
                        tryAgain = true;
                    }
                }
            } else {
                Helper.showLoading("", progressDialog, context, false);
                Helper.showMessage(this, "Error\uD83D\uDE14", "" +
                        "Filename needs to end in '.zip'. Try again!", MotionToast.TOAST_ERROR);
            }
        }
    }

    private void restoreBackup(Intent data) {
        if (data != null) {
            Uri uri = data.getData();
            Cursor returnCursor = context.getContentResolver()
                    .query(uri, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            String fileName = returnCursor.getString(nameIndex);

            if (fileName.contains(".realm")) {
                try {
                    RealmConfiguration configuration = RealmSingleton.get(this).getConfiguration();
                    RealmSingleton.get(this).close();
                    Realm.deleteRealm(configuration);

                    RealmBackupRestore realmBackupRestore = new RealmBackupRestore(this);
                    realmBackupRestore.restore(uri, "default.realm", false);
                    Helper.showMessage(this, "Restored", "" +
                            "Notes have been restored", MotionToast.TOAST_SUCCESS);

                    Realm realm = RealmSingleton.getInstance(context);
                    updateAlarms(realm.where(Note.class)
                            .equalTo("archived", false)
                            .equalTo("trash", false).findAll());

                    Helper.showLoading("", progressDialog, context, false);

                    close();
                } catch (Exception e) {
                    if (tryAgain) {
                        Helper.showMessage(this, "Error", "Clear storage via App Settings", MotionToast.TOAST_ERROR);
                        appSettings.setBackgroundColor(UiHelper.getColorFromTheme(this, R.attr.primaryButtonColor));
                        tryAgain = false;
                    } else {
                        Helper.showMessage(this, "Error", "attempting to fix error...try again", MotionToast.TOAST_ERROR);
                        tryAgain = true;
                    }
                }
            } else
                Helper.showMessage(this, "Error\uD83D\uDE14", "" +
                        "Filename needs to end in '.realm'. Try again!", MotionToast.TOAST_ERROR);
        }
    }

    private void updateAlarms(RealmResults<Note> allNotes) {
        for (int i = 0; i < allNotes.size(); i++) {
            Note currentNote = allNotes.get(i);
            if (null != currentNote.getReminderDateTime() && !currentNote.getReminderDateTime().isEmpty())
                Helper.startAlarm(this, currentNote, RealmSingleton.get(this));
        }
    }

    private void resetWidgets() {
        RealmSingleton.get(this).beginTransaction();
        RealmSingleton.get(this).where(Note.class).not().equalTo("widgetId", 0).findAll().setInt("widgetId", 0);
        RealmSingleton.get(this).commitTransaction();
    }

    private void updateImages(ArrayList<String> imagePaths) {
        if (imagePaths.size() != 0) {
            for (int i = 0; i < imagePaths.size(); i++) {
                String imagePath = imagePaths.get(i).substring(imagePaths.get(i).lastIndexOf("/") + 1);
                if (imagePaths.get(i).contains("~")) {
                    CheckListItem currentChecklistPhoto = RealmSingleton.get(this).where(CheckListItem.class).contains("itemImage", imagePath).findFirst();
                    if (currentChecklistPhoto != null) {
                        RealmSingleton.get(this).beginTransaction();
                        currentChecklistPhoto.setItemImage(imagePaths.get(i));
                        RealmSingleton.get(this).commitTransaction();
                    }
                } else {
                    Photo currentPhoto = RealmSingleton.get(this).where(Photo.class).contains("photoLocation", imagePath).findFirst();
                    if (currentPhoto != null) {
                        RealmSingleton.get(this).beginTransaction();
                        currentPhoto.setPhotoLocation(imagePaths.get(i));
                        RealmSingleton.get(this).commitTransaction();
                    }
                }
            }
        }
    }

    private void updateRecordings(ArrayList<String> recordingsPath) {
        if (recordingsPath.size() != 0) {
            for (int i = 0; i < recordingsPath.size(); i++) {
                String fullRecordingPath = recordingsPath.get(i);
                String recordingPath = fullRecordingPath.substring(fullRecordingPath.lastIndexOf("/") + 1);
                CheckListItem checkListItem = RealmSingleton.get(this).where(CheckListItem.class).contains("audioPath", recordingPath).findFirst();
                if (checkListItem != null) {
                    RealmSingleton.get(this).beginTransaction();
                    checkListItem.setAudioPath(fullRecordingPath);
                    RealmSingleton.get(this).commitTransaction();
                }
            }
        }
    }

    private void showLineNumberMenu(TextView lines, SwitchCompat reminderDropDown) {
        linesMenu = new CustomPowerMenu.Builder<>(context, new IconMenuAdapter(true))
                .addItem(new IconPowerMenuItem(null, "1"))
                .addItem(new IconPowerMenuItem(null, "2"))
                .addItem(new IconPowerMenuItem(null, "3"))
                .addItem(new IconPowerMenuItem(null, "4"))
                .addItem(new IconPowerMenuItem(null, "5"))
                .addItem(new IconPowerMenuItem(null, "6"))
                .addItem(new IconPowerMenuItem(null, "7"))
                .addItem(new IconPowerMenuItem(null, "8"))
                .setOnMenuItemClickListener(onIconMenuItemClickListener)
                .setAnimation(MenuAnimation.SHOW_UP_CENTER)
                .setWidth(300)
                .setMenuRadius(15f)
                .setMenuShadow(10f)
                .build();

        linesMenu.showAsDropDown(lines);
    }

    private void expandListMenu(List<IconPowerMenuItem> list, TextView textView) {
        linesMenu = new CustomPowerMenu.Builder<>(context, new IconMenuAdapter(true))
                .addItemList(list)
                .setOnMenuItemClickListener(onIconMenuItemClickListener)
                .setAnimation(MenuAnimation.SHOW_UP_CENTER)
                .setWidth(300)
                .setMenuRadius(15f)
                .setMenuShadow(10f)
                .setOnDismissListener(() -> clearEditingStatus())
                .build();

        linesMenu.showAsDropDown(textView);
    }

    private final OnMenuItemClickListener<IconPowerMenuItem> onIconMenuItemClickListener = new OnMenuItemClickListener<IconPowerMenuItem>() {
        @Override
        public void onItemClick(int position, IconPowerMenuItem item) {
            User currentUser = getUser();
            if (checkEditingStatus()) {
                RealmSingleton.get(SettingsScreen.this).beginTransaction();
                String text = item.getTitle();
                if (isEditingChecklistSep) {
                    currentUser.setItemsSeparator(text);
                    checklistSeparator.setText(text);
                } else if (isEditingSublistSep) {
                    if (text.equals("space")) {
                        currentUser.setItemsSeparator("newline");
                        checklistSeparator.setText("newline");
                    }
                    currentUser.setSublistSeparator(text);
                    sublistSeparator.setText(text);
                } else if (isEditingBudgetSymbol) {
                    currentUser.setBudgetCharacter(text);
                    budgetSymbol.setText(text);
                } else if (isEditingExpenseSymbol) {
                    currentUser.setExpenseCharacter(text);
                    expenseSymbol.setText(text);
                }
                RealmSingleton.get(SettingsScreen.this).commitTransaction();
            } else
                updateSelectedLines(position + 1);
            linesMenu.dismiss();
        }
    };

    private void clearEditingStatus() {
        isEditingChecklistSep = false;
        isEditingSublistSep = false;
        isEditingBudgetSymbol = false;
        isEditingExpenseSymbol = false;
    }

    private boolean checkEditingStatus() {
        return isEditingChecklistSep || isEditingSublistSep ||
                isEditingBudgetSymbol || isEditingExpenseSymbol;
    }

    private void updateSelectedLines(int position) {
        User currentUser = getUser();
        if (isTitleSelected) {
            RealmSingleton.get(this).beginTransaction();
            currentUser.setTitleLines(position);
            RealmSingleton.get(this).commitTransaction();
            titleLines.setText(String.valueOf(position));
        } else {
            RealmSingleton.get(this).beginTransaction();
            currentUser.setContentLines(position);
            RealmSingleton.get(this).commitTransaction();
            previewLines.setText(String.valueOf(position));
        }
    }

    private User getUser(){
        return RealmHelper.getUser(this, "settings");
    }

    private void openAppInPlayStore() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.akapps.dailynote"));
            intent.setPackage("com.android.vending");
            startActivity(intent);
        } catch (Exception exception) {
        }
    }

    private void openReddit() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.reddit.com/r/darknoteapp/")));
        } catch (Exception exception) {
        }
    }

    private void contactMe() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ak.apps.2019@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Dark Note: Checklists & Budget Feedback");
        startActivity(intent);
    }

    private void close() {
        RealmSingleton.setCloseRealm(false);
        Log.d("Here", "Keep realm open in SettingsScreen");
        Intent intent = new Intent(context, Homepage.class);
        startActivity(intent);
        finish();
        if (!AppData.isDisableAnimation) {
            overridePendingTransition(R.anim.show_from_bottom, R.anim.hide_to_bottom);
            Log.d("Here", "---------------------- Transition --------------------------");
        }
    }

    private void openAppInSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        startActivity(intent);
    }
}