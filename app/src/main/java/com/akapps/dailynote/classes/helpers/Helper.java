package com.akapps.dailynote.classes.helpers;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.text.Html;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;

import com.airbnb.lottie.LottieAnimationView;
import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.other.AppWidget;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import io.realm.Realm;
import kotlin.io.FilesKt;
import www.sanju.motiontoast.MotionToast;
import static android.content.Context.MODE_PRIVATE;

public class Helper {

    public static boolean isTablet(Context context){
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    // returns true if device is in landscape
    public static boolean isLandscape(Context context){
        int orientation = context.getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    // returns true if device is in portrait
    public static boolean isPortrait(Context context){
        int orientation = context.getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    // locks orientation
    public static void setOrientation(Activity activity, Context context){
        if(isPortrait(context))
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        else
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    public static String getCurrentDate(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy\nhh:mm:ss a");
        return sdf.format(c.getTime());
    }

    public static String getBackupDate(String fileSize){
        Calendar c = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM_dd_yyyy~hh_mm_a~");
        // first is file date, then file name aka time_backup.zip, and then file size
        return sdf.format(c.getTime()) + fileSize;
    }

    public static void unSetOrientation(Activity activity, Context context){
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    // returns height of screen
    public static int getHeightScreen(Activity currentActivity){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        currentActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    public static boolean isColorDark(int color){
        final double darkness = 1-(0.299 * Color.red(color) + 0.587 * Color.green(color) +
                0.114 * Color.blue(color))/255;
        return !(darkness < 0.5);
    }

    public static String twentyFourToTwelve(String dateString){
        String date = dateString.split(" ")[0];
        dateString = dateString.split(" ")[1];
        String[] time = dateString.split(":");
        int hour = Integer.parseInt(time[0]);
        String end = "";
        if(hour>=12) {
            end = "PM";
            hour %=12;
        }
        else
            end = "AM";

        if(hour == 0)
            hour = 12;

        return date + " " + hour + ":" + time[1] + ":" + time[2] + " " + end;
    }

    public static Calendar dateToCalender(String dateString){
        dateString = dateString.replace("\n", " ");
        Calendar calendar = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("E, MMM dd, yyyy hh:mm:ss aa");
            String dateInString = dateString;
            Date date = sdf.parse(dateInString);
            calendar = Calendar.getInstance();
            calendar.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
    }

    public static String getTimeDifference(Calendar calendar, boolean before){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                String timeDifference = "";
                LocalDateTime now = LocalDateTime.now();

                LocalDateTime dateSelected = LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());
                long secs, mins, hours, days, months, years;
                if (before) {
                    years = ChronoUnit.YEARS.between(now, dateSelected);
                    months = ChronoUnit.MONTHS.between(now, dateSelected);
                    secs = ChronoUnit.SECONDS.between(now, dateSelected);
                    mins = ChronoUnit.MINUTES.between(now, dateSelected);
                    hours = ChronoUnit.HOURS.between(now, dateSelected);
                    days = ChronoUnit.DAYS.between(now, dateSelected);
                } else {
                    years = ChronoUnit.YEARS.between(dateSelected, now);
                    months = ChronoUnit.MONTHS.between(dateSelected, now);
                    secs = ChronoUnit.SECONDS.between(dateSelected, now);
                    mins = ChronoUnit.MINUTES.between(dateSelected, now);
                    hours = ChronoUnit.HOURS.between(dateSelected, now);
                    days = ChronoUnit.DAYS.between(dateSelected, now);
                }

                if (years > 0) {
                    timeDifference += years + " year";
                    if (years > 1)
                        timeDifference += "s ";
                    else
                        timeDifference += " ";
                }
                if (months % 12 > 0) {
                    timeDifference += months % 12 + " month";
                    if (months % 12 > 1)
                        timeDifference += "s ";
                    else
                        timeDifference += " ";
                }
                if (days % 30 > 0) {
                    timeDifference += days % 30 + " day";
                    if (days % 30 > 1)
                        timeDifference += "s ";
                    else
                        timeDifference += " ";
                }
                if (hours % 24 > 0) {
                    timeDifference += hours % 24 + " hour";
                    if ((hours % 24) > 1)
                        timeDifference += "s ";
                    else
                        timeDifference += " ";
                }
                if (mins % 60 > 0) {
                    timeDifference += mins % 60 + " min";
                    if ((mins % 60) > 1)
                        timeDifference += "s ";
                    else
                        timeDifference += " ";
                }
                if (secs % 60 > 0) {
                    timeDifference += secs % 60 + " sec";
                    if ((secs % 60) > 1)
                        timeDifference += "s";
                }
                return timeDifference;
            } catch (Exception e) {
                return "";
            }
        }
        else
            return "";
    }

    public static void startAlarm(Activity activity, Note currentNote, Realm realm) {
        if(currentNote.getReminderDateTime().length() > 0) {
            Date reminderDate = null;
            try {
                reminderDate = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").parse(currentNote.getReminderDateTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date now = new Date();
            if (!now.after(reminderDate)){
                AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(activity, AlertReceiver.class);
                intent.putExtra("id", currentNote.getNoteId());
                intent.putExtra("title", currentNote.getTitle().replace("\n", " "));
                intent.putExtra("pin", currentNote.getPinNumber());
                intent.putExtra("securityWord", currentNote.getSecurityWord());
                intent.putExtra("fingerprint", currentNote.isFingerprint());
                intent.putExtra("checklist", currentNote.isCheckList());
                PendingIntent pendingIntent;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
                    pendingIntent = PendingIntent.getBroadcast(activity, currentNote.getNoteId(), intent,
                            PendingIntent.FLAG_IMMUTABLE);
                else
                    pendingIntent = PendingIntent.getBroadcast(activity, currentNote.getNoteId(), intent,
                            PendingIntent.FLAG_ONE_SHOT);

                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                        dateToCal(currentNote.getReminderDateTime()).getTimeInMillis(),
                        pendingIntent);
            }
            else {
                if(!realm.isClosed()){
                    realm.beginTransaction();
                    currentNote.setReminderDateTime("");
                    realm.commitTransaction();
                }
            }
        }
    }

    private static Calendar dateToCal(String dateString){
        dateString = dateString.replace("\n", " ");
        Calendar calendar = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
            String dateInString = dateString;
            Date date = sdf.parse(dateInString);
            calendar = Calendar.getInstance();
            calendar.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
    }

    /**
     * Determines if recyclerview is empty and displays empty view
     * @param size   The size of recyclerview
     * @param empty_Layout   This contains an image
     */
    public static void isListEmpty(Context context, int size, ScrollView empty_Layout, TextView title,
                                   TextView subTitle, TextView subSubTitle, boolean isResults,
                                   boolean isChecklist, boolean isChecklistAdded,
                                   LottieAnimationView emptyView){
        if(isResults){
            if(!emptyView.isAnimating())
                emptyView.setAnimation(R.raw.waiting_astronaut);
            title.setText("No Results");
            title.setTextSize(20);
            subTitle.setText("");
            subSubTitle.setText("");
        }
        else if(isChecklist) {
            title.setText("");
            subTitle.setText("\"Houston, we have a problem...\"");
            subTitle.setTextSize(18);
            emptyView.setAnimation(R.raw.waiting_astronaut);
            subSubTitle.setText("Tap the bottom right button to add to checklist");
            subSubTitle.setTextColor(context.getColor(R.color.semi_gray));
            subSubTitle.setTextSize(16);
        }
        else{
            title.setTextSize(18);
            title.setText("Avoid getting lost in the universe trying to remember");
            subTitle.setText("Let me do it for you");
            subSubTitle.setText("Tap the bottom right button to create a note");
            subSubTitle.setTextColor(context.getColor(R.color.semi_gray));
            emptyView.setAnimation(R.raw.astronaut_floating);
        }

        // verify that the lottie graphic is animating
        if(!emptyView.isAnimating())
            emptyView.playAnimation();

        if(isChecklistAdded)
            subSubTitle.setText("");

        if (size == 0)
            empty_Layout.setVisibility(View.VISIBLE);
        else
            empty_Layout.setVisibility(View.GONE);
    }

    // saves a small piece of data
    public static void savePreference(Context context, String data, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("app", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, data);
        editor.apply();
    }

    // retrieved data saved
    public static String getPreference(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("app", MODE_PRIVATE);
        String data = sharedPreferences.getString(key, null);
        return data;
    }

    // saves a small piece of data
    public static void saveBooleanPreference(Context context, boolean data, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("app", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, data);
        editor.apply();
    }

    // retrieved data saved
    public static boolean getBooleanPreference(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("app", MODE_PRIVATE);
        boolean data = sharedPreferences.getBoolean(key, false);
        return data;
    }

    // shows the user a message
    public static void showMessage(Activity activity, String title, String message, String typeOfMessage){
        try {
            MotionToast.Companion.darkColorToast(activity,
                    title,
                    message,
                    typeOfMessage,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(activity, R.font.overpass));
        }
        catch (Exception e){
            Toast.makeText(activity.getBaseContext(), title + "\n" + message, Toast.LENGTH_LONG).show();
        }
    }

    public static Dialog showLoading(String loadingText, Dialog progressDialog, Context context, boolean show){
        try {
            if (show) {
                loadingText += "\nDo not close app";

                if(progressDialog == null) {
                    progressDialog = new Dialog(context);
                    progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    progressDialog.setContentView(R.layout.custom_dialog_progress);
                }

                TextView progressTv = progressDialog.findViewById(R.id.progress_tv);
                progressTv.setText(loadingText);
                progressTv.setGravity(Gravity.CENTER);
                progressTv.setTextColor(ContextCompat.getColor(context, R.color.golden_rod));
                progressTv.setTextSize(19F);
                if (progressDialog.getWindow() != null)
                    progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                progressDialog.setCancelable(false);
                progressDialog.show();
            } else
                progressDialog.cancel();
        }catch (Exception e){ }
        return progressDialog;
    }

    public static void moveBee(LottieAnimationView bee, float max) {
        new Handler().postDelayed(() -> {
            ObjectAnimator animator = ObjectAnimator.ofFloat(bee, "translationX", max);
            animator.setDuration(4000);
            animator.start();
            new Handler().postDelayed(() -> {
                bee.setRotationY(180);
                ObjectAnimator animator2 = ObjectAnimator.ofFloat(bee, "translationX", 0f);
                animator2.setDuration(4000);
                animator2.start();
                new Handler().postDelayed(() -> {
                    ObjectAnimator animator3 = ObjectAnimator.ofFloat(bee, "translationX", -1 * max);
                    animator3.setDuration(4000);
                    animator3.start();
                    new Handler().postDelayed(() -> {
                        bee.setRotationY(0);
                        ObjectAnimator animator4 = ObjectAnimator.ofFloat(bee, "translationX", 0f);
                        animator4.setDuration(4000);
                        animator4.start();
                        new Handler().postDelayed(() -> {
                            moveBee(bee, max);
                        }, 3000);
                    }, 3000);
                }, 3000);
            }, 3000);
        }, 3000);
    }

    // deletes cache directory to ensure app size does not get too big
    public static void deleteCache(Context context) {
        try {
            File cacheDir = context.getCacheDir();
            FilesKt.deleteRecursively(cacheDir);
        }catch (Exception e){}
    }

    public static void deleteAppFiles(Context context){
        try {
            // delete backup folder
            File mainDir = new File(context.getExternalFilesDir(null) + "");
            FilesKt.deleteRecursively(mainDir);
        }catch (Exception e){}
    }

    public static void deleteFile(String path){
        File file = new File(path);
        if (file.exists())
            file.delete();
    }

    public static boolean isFileEmpty(String path){
        File file = new File(path);
        return Integer.parseInt(String.valueOf(file.length() / 1024)) == 0;
    }

    public static void deleteZipFile(Context context){
        try {
            // delete backup folder
            File backupDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/Dark Note");
            FilesKt.deleteRecursively(backupDir);

            // ensure there are no other zip files
            File directory = new File(context.getExternalFilesDir(null) + "");
            deleteZipFiles(directory);
        }catch (Exception e){}
    }

    public static void deleteZipFiles(File directory){
        try {
            File[] files = directory.listFiles();
            for (File file : files) {
                if (file.getName().contains(".zip"))
                    file.delete();
                else if (file.isDirectory())
                    deleteZipFiles(file);
            }
        }catch (Exception e){}
    }

    public static String removeAllMoneyAmounts(String text, String expenseKey){
        String newText = "";
        String[] tokens = text.replaceAll("\n", " ")
                .replaceAll(",", "")
                .split(" ");

        for(int i = 0; i < tokens.length; i++){
            if(!tokens[i].contains(expenseKey))
                newText += tokens[i] + " ";
        }
        return newText.trim();
    }

    public static void updateWidget(Note currentNote, Context context, Realm realm){
        try {
            if(currentNote == null) return;

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(currentNote.getWidgetId());
            if(info != null) {
                if (currentNote.getWidgetId() > 0) {
                    AppWidget.updateAppWidget(context, appWidgetManager, currentNote.getNoteId(), currentNote.getWidgetId());
                    appWidgetManager.notifyAppWidgetViewDataChanged(currentNote.getWidgetId(), R.id.preview_checklist);
                }
            }
            else {
                if(realm != null && !realm.isClosed()) {
                    realm.beginTransaction();
                    currentNote.setWidgetId(-1);
                    realm.commitTransaction();
                }
            }
        } catch (Exception e) {}
    }

    public static int darkenColor(int color, int percentage) {
        return ColorUtils.setAlphaComponent(color, percentage);
    }

    public static String capitalize(String word){
        if(!word.isEmpty())
            word = word.substring(0, 1).toUpperCase() + word.substring(1);

        return word;
    }

    public static void addNotificationNumber(Activity activity, View view, int number, int hOffset,
                                             boolean center, int badgeColor, int badgeTextColor){
        // add size of folder via notification indicator
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            @OptIn(markerClass = com.google.android.material.badge.ExperimentalBadgeUtils.class)
            public void onGlobalLayout() {
                BadgeDrawable badgeDrawable = BadgeDrawable.create(activity);
                badgeDrawable.setNumber(number);
                // change position of Badge
                badgeDrawable.setHorizontalOffset(hOffset);
                badgeDrawable.setVerticalOffset(center ? view.getHeight() / 2 : 25);
                badgeDrawable.setBackgroundColor(activity.getColor(badgeColor));
                badgeDrawable.setBadgeTextColor(activity.getColor(badgeTextColor));

                BadgeUtils.attachBadgeDrawable(badgeDrawable, view, null);
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    public static int getRandomColor(){
        Random rnd = new Random();
        int randomColorGenerated = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        return !isColorDark(randomColorGenerated) ? randomColorGenerated : getRandomColor();
    }

    public static void startTimer(Handler handler, int startTime){
        AppData.timerDuration = startTime;
        handler.postDelayed(new Runnable() {
            public void run() {
                AppData.timerDuration++;
                handler.postDelayed(this, 1000);
            }
        }, 0);
    }

    public static String secondsToDurationText(int totalSecs){
        int hours = totalSecs / 3600;
        int minutes = (totalSecs % 3600) / 60;
        int seconds = totalSecs % 60;
        String timeElapsed = "";

        if(totalSecs == 0)
            return "0:00";

        if (hours > 0)
            timeElapsed += hours + ":" + (minutes == 0 ? "00" : "");
        if (minutes > 0)
            timeElapsed += minutes + ":" + (seconds == 0 ? "00" : "");
        if (seconds > 0) {
            if(minutes == 0)
                timeElapsed += "0:";
            timeElapsed += seconds > 9 ? seconds : "0" + seconds;
        }

        return timeElapsed;
    }

    public static boolean deviceMicExists(Activity activity){
        PackageManager pm = activity.getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
    }

    public static String getFormattedFileSize(Context context, long sizeInBytes){
        return Formatter.formatFileSize(context, sizeInBytes);
    }

    public static File createFile(Context context, Uri uri, File destinationFilename) {
        try (InputStream ins = context.getContentResolver().openInputStream(uri)) {
            createFileFromStream(ins, destinationFilename);
        } catch (Exception ex) {
            Log.e("Save File", ex.getMessage());
            ex.printStackTrace();
        }
        return destinationFilename;
    }

    public static void createFileFromStream(InputStream ins, File destination) {
        try (OutputStream os = new FileOutputStream(destination)) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = ins.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
        } catch (Exception ex) {
            Log.e("Save File", ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static File getInternalFileDir(Activity activity){
        return activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
    }

    public static File createFile(Activity activity, String fileType, String fileExtension){
        String randomString = UUID.randomUUID().toString();
        String file = getInternalFileDir(activity) + File.separator +
                fileType + randomString.substring(0, randomString.length() / 3).replaceAll("-", "_") +
                fileExtension;
        return new File(file);
    }

    public static void sharePhoto(Context context, String photoLocation) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("image/*");
        Uri uri = null;

        // only attaches to email if there are project photos
        File file = new File(photoLocation);
        if(file.exists()) {
            uri = FileProvider.getUriForFile(
                    context,
                    "com.akapps.dailynote.fileprovider",
                    file);
        }

        // adds email subject and email body to intent
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);

        context.startActivity(Intent.createChooser(emailIntent, "Share Photo"));
    }

    public static void deleteUnneededFiles(Activity activity){
        String path = activity.getApplicationContext().getExternalFilesDir("") + "";
        File directory = new File(path);
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            if(files[i].getName().contains(".zip")) {
                FilesKt.deleteRecursively(files[i]);
                break;
            }
        }
    }

}
