package com.akapps.dailynote.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.akapps.dailynote.R;
import com.akapps.dailynote.adapter.IconMenuAdapter;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.AlertReceiver;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmBackupRestore;
import com.akapps.dailynote.classes.helpers.SecurityForPurchases;
import com.akapps.dailynote.classes.other.IconPowerMenuItem;
import com.akapps.dailynote.classes.other.InfoSheet;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetailsParams;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.skydoves.powermenu.CustomPowerMenu;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import www.sanju.motiontoast.MotionToast;
import static com.android.billingclient.api.BillingClient.SkuType.INAPP;

public class SettingsScreen extends AppCompatActivity implements PurchasesUpdatedListener,
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{

    // activity
    private Context context;
    private int all_Notes;
    private boolean tryAgain;
    private String TITLE_KEY = "title_lines";
    private String PREVIEW_KEY = "preview_lines";
    private boolean initializing;
    private Handler handler;
    private boolean isReminderSelected;
    private Calendar dateSelected;
    private String currentDateTimeSelected;
    private int selectionPosition;
    private int upgradeToProCounter;

    private User currentUser;
    private Realm realm;

    // Toolbar
    private Toolbar toolbar;
    private ImageView close;

    // layout
    private LinearLayout backup;
    private LinearLayout restoreBackup;
    private LinearLayout appSettings;
    private MaterialCardView contact;
    private MaterialCardView review;
    private TextView titleLines;
    private TextView previewLines;
    private LinearLayout titleLayout;
    private LinearLayout previewLayout;
    private CustomPowerMenu linesMenu;
    private boolean isTitleSelected;
    private SwitchCompat showPreview;
    private SwitchCompat backUpReminder;
    private TextView reminderDate;
    private SwitchCompat backUpOnLaunch;
    private TextView backUpLocation;
    private MaterialButton buyPro;
    private MaterialCardView grid;
    private MaterialCardView row;
    private MaterialCardView staggered;
    private TextView about;

    // Billing client
    private BillingClient billingClient;
    private ArrayList<String> purchaseItemIDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_screen);

        context = this;
        Helper.deleteCache(context);

        all_Notes = getIntent().getIntExtra("size", 0);

        boolean backingUp = getIntent().getBooleanExtra("backup", false);

        try {
            realm = Realm.getDefaultInstance();
        } catch (Exception e) {
            Realm.init(context);
            realm = Realm.getDefaultInstance();
        }
        currentUser = realm.where(User.class).findFirst();

        populateUserSettings();
        initializeBilling();

        if(backingUp)
            new Handler().postDelayed(this::openBackUpRestoreDialog, 1000);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(realm.isClosed())
           realm = Realm.getDefaultInstance();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(realm!=null)
            realm.close();

        if(handler!=null)
            handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onBackPressed() {
       close();
    }

    private void initializeLayout(){
        toolbar = findViewById(R.id.toolbar);
        close = findViewById(R.id.close_activity);
        backup = findViewById(R.id.backup);
        restoreBackup = findViewById(R.id.restore_backup);
        appSettings = findViewById(R.id.app_settings);
        contact = findViewById(R.id.contact);
        review = findViewById(R.id.review);
        about = findViewById(R.id.about);
        titleLines = findViewById(R.id.title_lines);
        previewLines = findViewById(R.id.preview_lines);
        titleLayout = findViewById(R.id.title_layout);
        previewLayout = findViewById(R.id.preview_layout);
        backUpReminder = findViewById(R.id.backup_reminder_switch);
        reminderDate = findViewById(R.id.reminder_date);
        showPreview = findViewById(R.id.show_preview_switch);
        backUpOnLaunch = findViewById(R.id.backup_on_launch);
        backUpLocation = findViewById(R.id.backup_location);
        buyPro = findViewById(R.id.buy_pro);
        grid = findViewById(R.id.grid);
        row = findViewById(R.id.row);
        staggered = findViewById(R.id.staggered);

        String titleLinesNumber = Helper.getPreference(context, TITLE_KEY);
        String previewLinesNumber = Helper.getPreference(context, PREVIEW_KEY);

        // sets the current select title lines and preview lines
        // by default it is 3
        titleLines.setText(titleLinesNumber==null ? "3": titleLinesNumber);
        previewLines.setText(previewLinesNumber==null ? "3": previewLinesNumber);

        // toolbar
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        backup.setOnClickListener(v -> showBackupRestoreInfo());

        restoreBackup.setOnClickListener(v -> openFile());

        titleLayout.setOnClickListener(v -> {
            isReminderSelected = false;
            if(currentUser.isProUser()) {
                isTitleSelected = true;
                showLineNumberMenu(titleLines, null);
            }
            else
                Helper.showMessage(this, "Settings", "Upgrade to pro to change preview settings", MotionToast.TOAST_ERROR);
        });

        previewLayout.setOnClickListener(v -> {
            isReminderSelected = false;
            if(currentUser.isProUser()) {
                isTitleSelected = false;
                showLineNumberMenu(previewLines, null);
            }
            else
                Helper.showMessage(this, "Settings", "Upgrade to pro to change preview settings", MotionToast.TOAST_ERROR);
        });

        appSettings.setOnClickListener(v -> openAppInSettings());

        row.setOnClickListener(v -> {
            if(currentUser.isProUser()){
                realm.beginTransaction();
                currentUser.setLayoutSelected("row");
                realm.commitTransaction();
                row.setCardBackgroundColor(context.getColor(R.color.darker_blue));
                grid.setCardBackgroundColor(context.getColor(R.color.gray));
                staggered.setCardBackgroundColor(context.getColor(R.color.gray));
            }
            else
                Helper.showMessage(this, "Settings", "Upgrade to pro to change layout", MotionToast.TOAST_ERROR);
        });

        grid.setOnClickListener(v -> {
            if(currentUser.isProUser()){
                realm.beginTransaction();
                currentUser.setLayoutSelected("grid");
                realm.commitTransaction();
                grid.setCardBackgroundColor(context.getColor(R.color.darker_blue));
                row.setCardBackgroundColor(context.getColor(R.color.gray));
                staggered.setCardBackgroundColor(context.getColor(R.color.gray));
            }
            else
                Helper.showMessage(this, "Settings", "Upgrade to pro to change layout", MotionToast.TOAST_ERROR);
        });

        staggered.setOnClickListener(v -> {
            if(!currentUser.getLayoutSelected().equals("stag")) {
                realm.beginTransaction();
                currentUser.setLayoutSelected("stag");
                realm.commitTransaction();
                staggered.setCardBackgroundColor(context.getColor(R.color.darker_blue));
                grid.setCardBackgroundColor(context.getColor(R.color.gray));
                row.setCardBackgroundColor(context.getColor(R.color.gray));
            }
        });

        contact.setOnClickListener(v -> contactMe());

        review.setOnClickListener(v -> openAppInPlayStore());

        close.setOnClickListener(v -> close());

        backUpReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(buttonView.isPressed()) {
                if (currentUser.isProUser()) {
                    if (isChecked && initializing) {
                        backUpReminder.setChecked(false);
                        isReminderSelected = true;
                        showLineNumberMenu(null, backUpReminder);
                    } else {
                        cancelAlarm();
                        reminderDate.setVisibility(View.GONE);
                    }
                } else if (initializing) {
                    backUpReminder.setChecked(false);
                    Helper.showMessage(this, "Settings", "Upgrade to pro to enable reminders", MotionToast.TOAST_ERROR);
                }
            }
        });

        showPreview.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(currentUser.isProUser()) {
                realm.beginTransaction();
                currentUser.setShowPreview(isChecked);
                realm.commitTransaction();
            }
            else if(initializing) {
                showPreview.setChecked(false);
                Helper.showMessage(this, "Settings", "Upgrade to pro to change preview settings", MotionToast.TOAST_ERROR);
            }
        });

        backUpOnLaunch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(currentUser.isProUser()) {
                if(isChecked != currentUser.isBackUpOnLaunch()) {
                    realm.beginTransaction();
                    currentUser.setBackUpOnLaunch(isChecked);
                    realm.commitTransaction();
                    if(isChecked)
                        Helper.showMessage(this, "Settings", "" +
                                "Go to home page to backup", MotionToast.TOAST_SUCCESS);
                }
                if(handler!=null)
                    handler.removeCallbacksAndMessages(null);
            }
            else if(isChecked && !currentUser.isProUser()) {
                backUpOnLaunch.setChecked(false);
                if(initializing) {
                    Helper.showMessage(this, "Settings", "" +
                            "Upgrade to pro to always save your data on launch", MotionToast.TOAST_ERROR);
                }
            }

            if(!isChecked)
                backUpLocation.setText("Location: ---");
        });

        buyPro.setOnClickListener(v -> {
            if(!currentUser.isProUser()) {
                if (billingClient.isReady())
                    initiatePurchase(purchaseItemIDs.get(0));
                else
                    reconnectBillingService();
            }
            else
                Helper.showMessage(this, "Pro Status", "You are already a " +
                        "Pro User. Thank you for your support!", MotionToast.TOAST_WARNING);
        });

        about.setOnClickListener(v -> upgradeToProCounter++);

        about.setOnLongClickListener(v -> {
            if(upgradeToProCounter == 12)
                upgradeToPro();
            return false;
        });
    }

    private void upgradeToPro(){
        realm.beginTransaction();
        currentUser.setProUser(!currentUser.isProUser());
        realm.commitTransaction();
        if(currentUser.isProUser()) {
            Helper.showMessage(SettingsScreen.this, "Upgrade Successful", "" +
                    "Thank you and Enjoy!\uD83D\uDE04", MotionToast.TOAST_SUCCESS);
        }
        else{
            Helper.showMessage(SettingsScreen.this, "Downgrade Successful", "" +
                    "Enjoy!\uD83D\uDE04", MotionToast.TOAST_SUCCESS);
        }
        Intent intent = new Intent(SettingsScreen.this, SettingsScreen.class);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
        dateSelected.set(Calendar.MILLISECOND, 0);
        currentDateTimeSelected += hourOfDay+ ":" + ((minute<10)? ("0" + minute): minute) + ":00";

        realm.beginTransaction();
        currentUser.setBackReminderOccurrence(selectionPosition);
        currentUser.setBackupReminder(true);
        currentUser.setStartingDate(currentDateTimeSelected);
        realm.commitTransaction();
        backUpReminder.setChecked(true);

        reminderDate.setText("Starting: " + Helper.twentyFourToTwelve(currentUser.getStartingDate()) +
                "\nOccurrence: " + Helper.getOccurrenceInString(currentUser.getBackReminderOccurrence()));
        reminderDate.setVisibility(View.VISIBLE);
        setRepeatingReminder(dateSelected);
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
        dateSelected.set(Calendar.DAY_OF_MONTH, day);
        dateSelected.set(Calendar.HOUR, dateSelected.get(Calendar.HOUR) + 1);

        if (dateSelected.after(Calendar.getInstance())) {
            dateSelected.set(Calendar.MONTH, ++month);
            dateSelected.set(Calendar.HOUR, dateSelected.get(Calendar.HOUR) - 1);
            currentDateTimeSelected = month + "-" + day + "-" + year + " ";
            timeDialog();
        }
        else {
            showDatePickerDialog();
            Helper.showMessage(this, "Reminder not set", "Reminder cannot be in the past", MotionToast.TOAST_ERROR);
        }
    }

    private void setRepeatingReminder(Calendar dateSelected){
        int month = dateSelected.get(Calendar.MONTH)-1;
        dateSelected.set(Calendar.MONTH, month);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        intent.putExtra("id", 1234);
        intent.putExtra("size", all_Notes);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1234, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, dateSelected.getTimeInMillis() + currentUser.getBackReminderOccurrence() * AlarmManager.INTERVAL_DAY,
                currentUser.getBackReminderOccurrence() * AlarmManager.INTERVAL_DAY, pendingIntent);
        dateSelected.add(Calendar.HOUR, 24 * currentUser.getBackReminderOccurrence());
        Helper.showMessage(this, "Reminder set", "Will Remind you " +
                "in " + Helper.getTimeDifference(dateSelected, true), MotionToast.TOAST_SUCCESS);
    }

    private void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        realm.beginTransaction();
        currentUser.setStartingDate("");
        currentUser.setBackupReminder(false);
        currentUser.setBackReminderOccurrence(1);
        realm.commitTransaction();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1234, intent, 0);
        alarmManager.cancel(pendingIntent);
    }


    private void initializeBilling(){
        purchaseItemIDs = new ArrayList() {{
            add("dark_note_pro");
        }};
        // Establish connection to billing client
        // check purchase status from google play store cache on every app start
        billingClient = BillingClient.newBuilder(SettingsScreen.this)
                .enablePendingPurchases().setListener(this).build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if(billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK){
                    Purchase.PurchasesResult queryPurchase = billingClient.queryPurchases(INAPP);
                    List<Purchase> queryPurchases = queryPurchase.getPurchasesList();
                    if(queryPurchases!=null && queryPurchases.size()>0){
                        handlePurchases(queryPurchases);
                    }
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
            }
        });
    }

    private void reconnectBillingService(){
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    initiatePurchase(purchaseItemIDs.get(0));
                }
                else {
                    Helper.showMessage(SettingsScreen.this, "Error", "" +
                            "Issue connecting to Google Play", MotionToast.TOAST_ERROR);
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
            }
        });
    }

    private void initiatePurchase(final String PRODUCT_ID) {
        List<String> skuList = new ArrayList<>();
        skuList.add(PRODUCT_ID);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(INAPP);
        billingClient.querySkuDetailsAsync(params.build(),
                (billingResult, skuDetailsList) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        if (skuDetailsList != null && skuDetailsList.size() > 0) {
                            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                    .setSkuDetails(skuDetailsList.get(0))
                                    .build();
                            billingClient.launchBillingFlow(SettingsScreen.this, flowParams);
                        }
                        else{
                            Helper.showMessage(SettingsScreen.this, "Error", "" +
                                    "Purchase Item "+ PRODUCT_ID +" not Found", MotionToast.TOAST_WARNING);
                        }
                    } else {
                        Helper.showMessage(SettingsScreen.this, "Error", "" +
                                "Try Again", MotionToast.TOAST_WARNING);
                    }
                });
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        //if item newly purchased
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases);
        }
        //if item already purchased then check and reflect changes
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            Purchase.PurchasesResult queryAlreadyPurchasesResult = billingClient.queryPurchases(INAPP);
            List<Purchase> alreadyPurchases = queryAlreadyPurchasesResult.getPurchasesList();
            if(alreadyPurchases!=null){
                handlePurchases(alreadyPurchases);
            }
        }
        //if purchase cancelled
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Helper.showMessage(SettingsScreen.this, "Upgrade Canceled", "" +
                    "\uD83D\uDE2D whhhhhhhhy \uD83D\uDE2D", MotionToast.TOAST_WARNING);
        }
        // Handle any other error msgs
        else {
            Helper.showMessage(SettingsScreen.this, "Error", "" +
                    "Try Again", MotionToast.TOAST_WARNING);
        }
    }

    private void handlePurchases(List<Purchase>  purchases) {
        for(Purchase purchase:purchases) {

            final int index = 0;
            //purchase found
            if(index>-1) {

                //if item is purchased
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED)
                {
                    if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
                        // Invalid purchase
                        // show error to user
                        Helper.showMessage(SettingsScreen.this, "Error", "" +
                                "Try Again", MotionToast.TOAST_WARNING);
                        continue;
                    }
                    // else purchase is valid
                    //if item is purchased and not consumed
                    if (!purchase.isAcknowledged()) {
                        ConsumeParams consumeParams = ConsumeParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();

                        billingClient.consumeAsync(consumeParams, new ConsumeResponseListener() {
                            @Override
                            public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                    int consumeCountValue = getPurchaseCountValueFromPref(purchaseItemIDs.get(index))+1;
                                    savePurchaseCountValueToPref(purchaseItemIDs.get(index),consumeCountValue);
                                    upgradeToPro();
                                }
                            }
                        });
                    }
                }
                //if purchase is pending
                else if(purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                    Helper.showMessage(SettingsScreen.this, "Upgrade Pending", "" +
                            " Purchase is Pending. Please complete Transaction", MotionToast.TOAST_WARNING);
                }
                //if purchase is refunded or unknown
                else if( purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE) {
                    Helper.showMessage(SettingsScreen.this, "Upgrade Error", "" +
                            purchaseItemIDs.get(index)+" Purchase Status Unknown", MotionToast.TOAST_WARNING);

                }
            }

        }
    }

    private SharedPreferences getPreferenceObject() {
        return getApplicationContext().getSharedPreferences("donate", 0);
    }
    private SharedPreferences.Editor getPreferenceEditObject() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("donate", 0);
        return pref.edit();
    }
    private int getPurchaseCountValueFromPref(String PURCHASE_KEY){
        return getPreferenceObject().getInt( PURCHASE_KEY,0);
    }
    private void savePurchaseCountValueToPref(String PURCHASE_KEY, int value){
        getPreferenceEditObject().putInt(PURCHASE_KEY,value).commit();
    }

    private boolean verifyValidSignature(String signedData, String signature) {
        try {
            // To get key go to Developer Console > Select your app > Development Tools > Services &amp; APIs.
            String base64Key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlqExfT33U95ES4pLlToMPGeicHArv0AKfCDfAJGO13yzCEtWnUikI0IW7SEyc7vGSN5VDRFT1bJlbMPloQ/ULSL/wEWSpbqXI/xCLNduZ4T6XQnRzYssPWN3kLq/kzu5QPBYxv6XD0T9n7V6LyRSheI+ldPYaRsAT0y6nHCv14GAzIuW4lAnpc4TMeR7hkkifsu/VltHeonQYyCEF+Z+K1tHkttQxk3Xz0/ABZuqL36rI33hCJYhZPu2v1RCkRap9hqGOyeBGiEoBI5tNLaQ8sRaCcb+bRAraUt+hHKDWHnGFHSN/tcMTP0f2iETKQbReLNEgdXaLkypBTKJIWChBwIDAQAB";
            return SecurityForPurchases.verifyPurchase(base64Key, signedData, signature);
        } catch (IOException e) {
            return false;
        }
    }

    private void populateUserSettings(){
        initializeLayout();
        initializeSettings();
        initializing = true;
    }

    private void initializeSettings(){
        showPreview.setChecked(currentUser.isShowPreview());
        backUpOnLaunch.setChecked(currentUser.isBackUpOnLaunch());

        if(currentUser.isProUser() && currentUser.isBackUpOnLaunch()) {
            handler = new Handler();
            Calendar calendar = Helper.dateToCalender(currentUser.getLastUpdated());

            String location = currentUser.getBackUpLocation();
            String lastTimeUpdated = currentUser.getLastUpdated();
            Calendar finalCalendar = calendar;
            handler.postDelayed(new Runnable() {
                @SuppressLint("SetTextI18n")
                public void run() {
                    backUpLocation.setText("Location: " + location
                            + "\n\nLast Updated: " + lastTimeUpdated +
                            "\n(" + Helper.getTimeDifference(finalCalendar, false) + " ago)");
                    handler.postDelayed(this, 1000);
                }
            }, 0);
        }

        if(currentUser.isProUser()){
            buyPro.setStrokeColor(ColorStateList.valueOf(getColor(R.color.gray)));
            buyPro.setText("PRO USER");
            buyPro.setElevation(0);
        }

        if(!currentUser.isBackUpOnLaunch())
            backUpLocation.setText("Location: ---");

        if(currentUser.getLayoutSelected().equals("row"))
            row.setCardBackgroundColor(context.getColor(R.color.darker_blue));
        else if(currentUser.getLayoutSelected().equals("grid"))
            grid.setCardBackgroundColor(context.getColor(R.color.darker_blue));
        else
            staggered.setCardBackgroundColor(context.getColor(R.color.darker_blue));

        if(currentUser.isBackupReminder()) {
            backUpReminder.setChecked(true);
            if(currentUser.isBackupReminder() && currentUser.getStartingDate().length()>0) {
                reminderDate.setText("Starting: " + Helper.twentyFourToTwelve(currentUser.getStartingDate()) +
                        "\nOccurrence: " + Helper.getOccurrenceInString(currentUser.getBackReminderOccurrence()));
                reminderDate.setVisibility(View.VISIBLE);
            }
        }
        else
            reminderDate.setVisibility(View.GONE);
    }

    public void openBackUpRestoreDialog(){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
        }
        else
            backUpData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                backUpData();
            else
                Helper.showMessage(this, "Accept Permission", "You need " +
                        "to accept permissions to backup", MotionToast.TOAST_ERROR);
        }
    }

    private void backUpData(){
        if(all_Notes!=0) {
            realm.close();
            RealmBackupRestore realmBackupRestore = new RealmBackupRestore(this);
            realmBackupRestore.update(this, context);
            File exportedFilePath = realmBackupRestore.backup_Share();
            shareFile(exportedFilePath);
        }
        else
            Helper.showMessage(this, "Backup Failed", "\uD83D\uDE10No " +
                    "data to backup\uD83D\uDE10", MotionToast.TOAST_ERROR);
    }

    private void showBackupRestoreInfo(){
        InfoSheet info = new InfoSheet(1);
        info.show(getSupportFragmentManager(), info.getTag());
    }

    private void openFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, 1);
    }

    private void shareFile(File backup){
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");

        Uri fileBackingUp  = FileProvider.getUriForFile(
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
            restoreBackup(data);
        }
    }

    private void restoreBackup(Intent data){
        if (data != null) {
            Uri uri = data.getData();
            Cursor returnCursor = context.getContentResolver()
                    .query(uri, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            String fileName = returnCursor.getString(nameIndex);

            if(fileName.contains(".realm")) {
                try {
                    RealmConfiguration configuration = realm.getConfiguration();
                    realm.close();
                    Realm.deleteRealm(configuration);

                    RealmBackupRestore realmBackupRestore = new RealmBackupRestore(this);
                    realmBackupRestore.restore(uri);
                    Helper.showMessage(this, "Restored", "" +
                            "Notes have been restored", MotionToast.TOAST_SUCCESS);
                   close();
                } catch (Exception e) {
                    if(tryAgain){
                        Helper.showMessage(this, "Error", "Clear storage via App Settings", MotionToast.TOAST_ERROR);
                        appSettings.setBackgroundColor(getColor(R.color.flamingo));
                        tryAgain = false;
                    }
                    else {
                        Helper.showMessage(this, "Error","attempting to fix error...try again", MotionToast.TOAST_ERROR);
                        tryAgain = true;
                    }
                }
            }
            else
                Helper.showMessage(this, "Big Error\uD83D\uDE14", "" +
                        "Why are you trying to break my app (only .realm files)", MotionToast.TOAST_ERROR);
        }
    }

    private void showLineNumberMenu(TextView lines, SwitchCompat reminderDropDown){
        if(isReminderSelected){
            linesMenu = new CustomPowerMenu.Builder<>(context, new IconMenuAdapter(true))
                    .addItem(new IconPowerMenuItem(null, "Daily"))
                    .addItem(new IconPowerMenuItem(null, "2 Days"))
                    .addItem(new IconPowerMenuItem(null, "3 Days"))
                    .addItem(new IconPowerMenuItem(null, "4 Days"))
                    .addItem(new IconPowerMenuItem(null, "5 Days"))
                    .addItem(new IconPowerMenuItem(null, "6 Days"))
                    .addItem(new IconPowerMenuItem(null, "Weekly"))
                    .addItem(new IconPowerMenuItem(null, "Monthly"))
                    .setBackgroundColor(getColor(R.color.light_gray))
                    .setOnMenuItemClickListener(onIconMenuItemClickListener)
                    .setAnimation(MenuAnimation.SHOW_UP_CENTER)
                    .setWidth(300)
                    .setMenuRadius(15f)
                    .setMenuShadow(10f)
                    .build();
        }
        else {
            linesMenu = new CustomPowerMenu.Builder<>(context, new IconMenuAdapter(true))
                    .addItem(new IconPowerMenuItem(null, "1"))
                    .addItem(new IconPowerMenuItem(null, "2"))
                    .addItem(new IconPowerMenuItem(null, "3"))
                    .addItem(new IconPowerMenuItem(null, "4"))
                    .addItem(new IconPowerMenuItem(null, "5"))
                    .addItem(new IconPowerMenuItem(null, "6"))
                    .addItem(new IconPowerMenuItem(null, "7"))
                    .addItem(new IconPowerMenuItem(null, "8"))
                    .setBackgroundColor(getColor(R.color.light_gray))
                    .setOnMenuItemClickListener(onIconMenuItemClickListener)
                    .setAnimation(MenuAnimation.SHOW_UP_CENTER)
                    .setWidth(300)
                    .setMenuRadius(15f)
                    .setMenuShadow(10f)
                    .build();
        }

        if(isReminderSelected)
            linesMenu.showAsDropDown(reminderDropDown);
        else
            linesMenu.showAsDropDown(lines);
    }

    private final OnMenuItemClickListener<IconPowerMenuItem> onIconMenuItemClickListener = new OnMenuItemClickListener<IconPowerMenuItem>() {
        @Override
        public void onItemClick(int position, IconPowerMenuItem item) {
            if(isReminderSelected){
                if(position<=6)
                    selectionPosition = position+1;
                else
                    selectionPosition = 30;
                InfoSheet info = new InfoSheet(2);
                info.show(getSupportFragmentManager(), info.getTag());
            }
            else
                updateSelectedLines(position+1);
            linesMenu.dismiss();
        }
    };

    private void updateSelectedLines(int position){
        if(isTitleSelected) {
            Helper.savePreference(context, String.valueOf(position), TITLE_KEY);
            titleLines.setText(String.valueOf(position));
        }
        else {
            Helper.savePreference(context, String.valueOf(position), PREVIEW_KEY);
            previewLines.setText(String.valueOf(position));
        }
    }

    private void openAppInPlayStore(){
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.akapps.dailynote"));
            intent.setPackage("com.android.vending");
            startActivity(intent);
        }
        catch (Exception exception){}
    }

    private void contactMe(){
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ak.apps.2019@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Dark Note App Feedback");
        startActivity(intent);
    }

    private void close(){
        Intent intent = new Intent(this, Homepage.class);
        startActivity(intent);
        finish();
        overridePendingTransition(0, R.anim.hide_to_bottom);
    }

    private void openAppInSettings(){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        startActivity(intent);
    }
}