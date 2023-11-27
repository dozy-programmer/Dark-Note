package com.akapps.dailynote.classes.helpers;

import static android.content.Context.MODE_PRIVATE;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.text.Html;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
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
import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.Photo;
import com.akapps.dailynote.classes.data.Place;
import com.akapps.dailynote.classes.data.SubCheckListItem;
import com.akapps.dailynote.classes.other.AppWidget;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
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
import io.realm.RealmResults;
import io.realm.Sort;
import kotlin.io.FilesKt;
import www.sanju.motiontoast.MotionToast;

public class Helper {

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    // returns true if device is in landscape
    public static boolean isLandscape(Context context) {
        int orientation = context.getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    // return height of screen
    public static int getDeviceHeight(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    // returns true if device is in portrait
    public static boolean isPortrait(Context context) {
        int orientation = context.getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    // locks orientation
    public static void setOrientation(Activity activity, Context context) {
        if (isPortrait(context))
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        else
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    public static String getCurrentDate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy\nhh:mm:ss a");
        return sdf.format(c.getTime());
    }

    public static String getBackupDate(String fileSize) {
        Calendar c = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM_dd_yyyy~hh_mm_a~");
        // first is file date, then file name aka time_backup.zip, and then file size
        return sdf.format(c.getTime()) + fileSize;
    }

    public static void unSetOrientation(Activity activity, Context context) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    // returns height of screen
    public static int getHeightScreen(Activity currentActivity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        currentActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    public static boolean isColorDark(int color) {
        final double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) +
                0.114 * Color.blue(color)) / 255;
        return !(darkness < 0.5);
    }

    public static String twentyFourToTwelve(String dateString) {
        String date = dateString.split(" ")[0];
        dateString = dateString.split(" ")[1];
        String[] time = dateString.split(":");
        int hour = Integer.parseInt(time[0]);
        String end = "";
        if (hour >= 12) {
            end = "PM";
            hour %= 12;
        } else
            end = "AM";

        if (hour == 0)
            hour = 12;

        return date + " " + hour + ":" + time[1] + ":" + time[2] + " " + end;
    }

    public static Calendar dateToCalender(String dateString) {
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

    public static String getTimeDifference(Calendar calendar, boolean before) {
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
                if (secs % 60 > 0 && (years + months + days + hours + mins == 0)) {
                    timeDifference += secs % 60 + " sec";
                    if ((secs % 60) > 1)
                        timeDifference += "s";
                }
                return timeDifference;
            } catch (Exception e) {
                return "";
            }
        } else
            return "";
    }

    public static void startAlarm(Activity activity, Note currentNote, Realm realm) {
        if (currentNote.getReminderDateTime().length() > 0) {
            Date reminderDate = null;
            try {
                reminderDate = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").parse(currentNote.getReminderDateTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date now = new Date();
            if (!now.after(reminderDate)) {
                AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(activity, AlertReceiver.class);
                intent.putExtra("id", currentNote.getNoteId());
                intent.putExtra("title", currentNote.getTitle().replace("\n", " "));
                intent.putExtra("pin", currentNote.getPinNumber());
                intent.putExtra("securityWord", currentNote.getSecurityWord());
                intent.putExtra("fingerprint", currentNote.isFingerprint());

                PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, currentNote.getNoteId(), intent,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);

                AlarmManager.AlarmClockInfo clock = new AlarmManager.AlarmClockInfo(
                        dateToCal(currentNote.getReminderDateTime()).getTimeInMillis(), pendingIntent);
                alarmManager.setAlarmClock(clock, pendingIntent);
            } else {
                if (!realm.isClosed()) {
                    realm.beginTransaction();
                    currentNote.setReminderDateTime("");
                    realm.commitTransaction();
                }
            }
        }
    }

    private static Calendar dateToCal(String dateString) {
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

    public static String convertToTwentyFourHour(String input) {
        // Parse the original string into a Date object
        SimpleDateFormat originalFormat = new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa");
        Date originalDate = null;
        try {
            originalDate = originalFormat.parse(input);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        // Format the Date object into the desired format
        SimpleDateFormat desiredFormat = new SimpleDateFormat("E, MMM dd, yyyy\nHH:mm:ss");

        return desiredFormat.format(originalDate);
    }

    /**
     * Determines if recyclerview is empty and displays empty view
     *
     * @param size         The size of recyclerview
     * @param empty_Layout This contains an image
     */
    public static void isListEmpty(Context context, int size, ScrollView empty_Layout, TextView title,
                                   TextView subTitle, TextView subSubTitle, boolean isResults,
                                   boolean isChecklist, boolean isChecklistAdded,
                                   LottieAnimationView emptyView) {
        if (isResults) {
            if(RealmSingleton.getUser(context).isDisableAnimation())
                emptyView.setImageResource(R.drawable.no_results_icon);
            else if (!emptyView.isAnimating())
                emptyView.setAnimation(R.raw.waiting_astronaut);
            title.setText("No Results");
            title.setTextSize(20);
            subTitle.setText("");
            subSubTitle.setText("");
        } else if (isChecklist) {
            title.setText("");
            subTitle.setText("\"Houston, we have a problem...\"");
            subTitle.setTextSize(18);
            if(RealmSingleton.getUser(context).isDisableAnimation())
                emptyView.setImageResource(R.drawable.no_results_icon);
            else
                emptyView.setAnimation(R.raw.waiting_astronaut);
            subSubTitle.setText("Tap the bottom right button to add to checklist");
            subSubTitle.setTextColor(context.getColor(R.color.semi_gray));
            subSubTitle.setTextSize(16);
        } else {
            title.setTextSize(18);
            title.setText("Avoid getting lost in the universe trying to remember");
            subTitle.setText("Let me do it for you");
            subSubTitle.setText("Tap the bottom right button to create a note");
            subSubTitle.setTextColor(context.getColor(R.color.semi_gray));
            if(RealmSingleton.getUser(context).isDisableAnimation())
                emptyView.setImageResource(R.drawable.notebook_icon);
            else
                emptyView.setAnimation(R.raw.astronaut_floating);
        }

        // verify that the lottie graphic is animating
        if (!emptyView.isAnimating())
            emptyView.playAnimation();

        if (isChecklistAdded)
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
    public static void showMessage(Activity activity, String title, String message, String typeOfMessage) {
        try {
            MotionToast.Companion.darkColorToast(activity,
                    title,
                    message,
                    typeOfMessage,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(activity, R.font.overpass));
        } catch (Exception e) {
            Toast.makeText(activity, title + "\n" + message, Toast.LENGTH_LONG).show();
        }
    }

    public static Dialog showLoading(String loadingText, Dialog progressDialog, Context context, boolean show) {
        try {
            if (show) {
                loadingText += "\nDo not close app";

                if (progressDialog == null) {
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
        } catch (Exception e) {
        }
        return progressDialog;
    }

    public static void moveBee(LottieAnimationView bee, float max) {
        new Handler().postDelayed(() -> {
            ObjectAnimator animator = ObjectAnimator.ofFloat(bee, "translationX", max);
            animator.setDuration(6000);
            animator.start();
            new Handler().postDelayed(() -> {
                bee.setRotationY(180);
                ObjectAnimator animator2 = ObjectAnimator.ofFloat(bee, "translationX", 0f);
                animator2.setDuration(6000);
                animator2.start();
                new Handler().postDelayed(() -> {
                    ObjectAnimator animator3 = ObjectAnimator.ofFloat(bee, "translationX", -1 * max);
                    animator3.setDuration(6000);
                    animator3.start();
                    new Handler().postDelayed(() -> {
                        bee.setRotationY(0);
                        ObjectAnimator animator4 = ObjectAnimator.ofFloat(bee, "translationX", 0f);
                        animator4.setDuration(6000);
                        animator4.start();
                        new Handler().postDelayed(() -> {
                            moveBee(bee, max);
                        }, 0);
                    }, 5000);
                }, 0);
            }, 5000);
        }, 0);
    }

    // deletes cache directory to ensure app size does not get too big
    public static void deleteCache(Context context) {
        try {
            File cacheDir = context.getCacheDir();
            FilesKt.deleteRecursively(cacheDir);
        } catch (Exception e) {
        }
    }

    public static void deleteAppFiles(Context context) {
        try {
            // delete backup folder
            File mainDir = new File(context.getExternalFilesDir(null) + "");
            FilesKt.deleteRecursively(mainDir);
        } catch (Exception e) {
        }
    }

    public static void deleteFile(String path) {
        File file = new File(path);
        if (file.exists())
            file.delete();
    }

    public static boolean isFileEmpty(String path) {
        File file = new File(path);
        return Integer.parseInt(String.valueOf(file.length() / 1024)) == 0;
    }

    public static void deleteZipFile(Context context) {
        try {
            // delete backup folder
            File backupDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/Dark Note");
            FilesKt.deleteRecursively(backupDir);

            // ensure there are no other zip files
            File directory = new File(context.getExternalFilesDir(null) + "");
            deleteZipFiles(directory);
        } catch (Exception e) {
        }
    }

    public static void deleteZipFiles(File directory) {
        try {
            File[] files = directory.listFiles();
            for (File file : files) {
                if (file.getName().contains(".zip"))
                    file.delete();
                else if (file.isDirectory())
                    deleteZipFiles(file);
            }
        } catch (Exception e) {
        }
    }

    public static String removeAllMoneyAmounts(String text, String expenseKey) {
        String newText = "";
        String[] tokens = text.replaceAll("\n", " ")
                .replaceAll(",", "")
                .split(" ");

        for (int i = 0; i < tokens.length; i++) {
            if (!tokens[i].contains(expenseKey))
                newText += tokens[i] + " ";
        }
        return newText.trim();
    }

    public static void updateWidget(Note currentNote, Context context, Realm realm) {
        try {
            if (currentNote == null) return;

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(currentNote.getWidgetId());
            if (info != null) {
                if (currentNote.getWidgetId() > 0) {
                    AppWidget.updateAppWidget(context, appWidgetManager, currentNote.getNoteId(), currentNote.getWidgetId());
                    appWidgetManager.notifyAppWidgetViewDataChanged(currentNote.getWidgetId(), R.id.preview_checklist);
                }
            } else {
                if (realm != null && !realm.isClosed()) {
                    realm.beginTransaction();
                    currentNote.setWidgetId(-1);
                    realm.commitTransaction();
                }
            }
        } catch (Exception e) {
        }
    }

    public static int darkenColor(int color, int alpha) {
        return ColorUtils.setAlphaComponent(color, alpha);
    }

    public static String capitalize(String word) {
        if (!word.isEmpty())
            word = word.substring(0, 1).toUpperCase() + word.substring(1);

        return word;
    }

    public static void addNotificationNumber(Activity activity, View view, int number, int hOffset,
                                             boolean center, int badgeColor, int badgeTextColor) {
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

    public static int getRandomColor() {
        Random rnd = new Random();
        int randomColorGenerated = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        return !isColorDark(randomColorGenerated) ? randomColorGenerated : getRandomColor();
    }

    public static void startTimer(Handler handler, int startTime) {
        AppData.timerDuration = startTime;
        handler.postDelayed(new Runnable() {
            public void run() {
                AppData.timerDuration++;
                handler.postDelayed(this, 1000);
            }
        }, 0);
    }

    public static String secondsToDurationText(int totalSecs) {
        int hours = totalSecs / 3600;
        int minutes = (totalSecs % 3600) / 60;
        int seconds = totalSecs % 60;
        String timeElapsed = "";

        if (totalSecs == 0)
            return "0:00";

        if (hours > 0)
            timeElapsed += hours + ":" + (minutes == 0 ? "00" : "");
        if (minutes > 0)
            timeElapsed += minutes + ":" + (seconds == 0 ? "00" : "");
        if (seconds > 0) {
            if (minutes == 0)
                timeElapsed += "0:";
            timeElapsed += seconds > 9 ? seconds : "0" + seconds;
        }

        return timeElapsed;
    }

    public static boolean deviceMicExists(Activity activity) {
        PackageManager pm = activity.getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
    }

    public static String getFormattedFileSize(Context context, long sizeInBytes) {
        return Formatter.formatFileSize(context, sizeInBytes);
    }

    public static File createFile(Context context, Uri uri, File destinationFilename) {
        try (InputStream ins = context.getContentResolver().openInputStream(uri)) {
            createFileFromStream(ins, destinationFilename);
        } catch (Exception ex) {
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
        }
    }

    public static File getInternalFileDir(Activity activity) {
        return activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
    }

    public static File createFile(Activity activity, String fileType, String fileExtension) {
        String randomString = UUID.randomUUID().toString();
        String file = getInternalFileDir(activity) + File.separator +
                fileType + randomString.substring(0, randomString.length() / 3).replaceAll("-", "_") +
                fileExtension;
        return new File(file);
    }

    public static void shareFile(Context context, String fileType, String filePath, String bodyText) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType(fileType + "/*");
        Uri uri = null;

        // only attaches to email if there are project photos
        File file = new File(filePath);
        if (file.exists()) {
            uri = FileProvider.getUriForFile(
                    context,
                    "com.akapps.dailynote.fileprovider",
                    file);
        }

        if (!bodyText.isEmpty())
            emailIntent.putExtra(Intent.EXTRA_TEXT, bodyText);
        // adds email subject and email body to intent
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);

        context.startActivity(Intent.createChooser(emailIntent, "Share Photo"));
    }

    public static void shareFile(Activity activity, String text) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_TEXT, text);
        activity.startActivity(Intent.createChooser(emailIntent, "Share Text"));
    }

    private static void shareFiles(Context context, ArrayList<File> files) {
        final Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("text/*");

        // attaching to intent to send folders
        ArrayList<Uri> uris = new ArrayList<>();

        for (File file : files) {
            if (file.exists()) {
                uris.add(FileProvider.getUriForFile(
                        context,
                        "com.akapps.dailynote.fileprovider",
                        file));
            }
        }

        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        context.startActivity(Intent.createChooser(emailIntent, "Export Files"));
    }

    public static void shareNote(Activity activity, int noteId, Realm realm) {
        Note currentNote = realm.where(Note.class).equalTo("noteId", noteId).findFirst();
        final Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("text/plain");

        // attaching to intent to send folders
        ArrayList<Uri> uris = new ArrayList<>();

        // need to get all photos (for note and checklist)
        RealmResults<Photo> allNotePhotos = realm.where(Photo.class).equalTo("noteId", currentNote.getNoteId()).findAll();
        RealmResults<CheckListItem> checkListItems = realm.where(CheckListItem.class).equalTo("id",
                currentNote.getNoteId()).not().isEmpty("itemImage").findAll();
        // need to get all audio files
        RealmResults<CheckListItem> audioPaths = realm.where(CheckListItem.class).equalTo("id",
                currentNote.getNoteId()).not().isEmpty("audioPath").findAll();

        for (int i = 0; i < allNotePhotos.size(); i++) {
            assert allNotePhotos.get(i) != null;
            File file = new File(allNotePhotos.get(i).getPhotoLocation());
            if (file.exists()) {
                uris.add(FileProvider.getUriForFile(
                        activity,
                        "com.akapps.dailynote.fileprovider",
                        file));
            }
        }

        for (int i = 0; i < checkListItems.size(); i++) {
            assert checkListItems.get(i) != null;
            File file = new File(checkListItems.get(i).getItemImage());
            if (file.exists()) {
                uris.add(FileProvider.getUriForFile(
                        activity,
                        "com.akapps.dailynote.fileprovider",
                        file));
            }
        }

        for (int i = 0; i < audioPaths.size(); i++) {
            assert audioPaths.get(i) != null;
            if (audioPaths.get(i).getAudioPath() != null) {
                File file = new File(audioPaths.get(i).getAudioPath());
                if (file.exists()) {
                    uris.add(FileProvider.getUriForFile(
                            activity,
                            "com.akapps.dailynote.fileprovider",
                            file));
                }
            }
        }

        String noteString = currentNote.isCheckList() ? getNoteString(currentNote, realm)
                : Helper.removeMarkdownFormatting(currentNote.getNote());

        // adds email subject and email body to intent
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, currentNote.getTitle());
        emailIntent.putExtra(Intent.EXTRA_TEXT, noteString);

        if (uris.size() > 0)
            emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

        activity.startActivity(Intent.createChooser(emailIntent, "Share Note"));
    }

    public static void deleteUnneededFiles(Activity activity) {
        String path = activity.getApplicationContext().getExternalFilesDir("") + "";
        File directory = new File(path);
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().contains(".zip")) {
                FilesKt.deleteRecursively(files[i]);
                break;
            }
        }
    }

    public static RealmResults<CheckListItem> sortChecklist(Note currentNote, Realm realm) {
        int currentSort = currentNote.getSort();

        RealmResults<CheckListItem> results = currentNote.getChecklist()
                .sort("positionInList", Sort.ASCENDING);

        if (currentSort == -1) {
            realm.beginTransaction();
            currentNote.setSort(5);
            realm.commitTransaction();
        }

        if (currentSort == 1)
            results = results.sort("text", Sort.ASCENDING);
        else if (currentSort == 2)
            results = results.sort("text", Sort.DESCENDING);
        else if (currentSort == 4)
            results = results.sort("lastCheckedDate", Sort.ASCENDING)
                    .sort("checked", Sort.ASCENDING);
        else if (currentSort == 3)
            results = results.sort("lastCheckedDate", Sort.ASCENDING)
                    .sort("checked", Sort.DESCENDING);
        else if (currentSort == 5 || currentSort == 6)
            results = results.sort("positionInList");
        else {
            results = results.sort("positionInList");
        }

        return results;
    }

    public static RealmResults<Note> getSelectedNotes(Realm realm, Activity activity) {
        RealmResults<Note> selectedNotes = realm.where(Note.class).equalTo("isSelected", true).findAll();
        RealmResults<Note> lockedNotes = realm.where(Note.class).equalTo("isSelected", true)
                .greaterThan("pinNumber", 0)
                .findAll();

        if (lockedNotes.size() > 0) {
            selectedNotes = null;
            Helper.showMessage(activity, "Locked Notes", "Locked notes " +
                    "can only be exported individually", MotionToast.TOAST_ERROR);
        } else {
            if (selectedNotes.size() == 0) {
                Helper.showMessage(activity, "Not Deleted", "Nothing was selected " +
                        "and thus not exported", MotionToast.TOAST_ERROR);
            }
        }
        return selectedNotes;
    }

    public static File createTempExportFile(Activity activity, String text, String ext) {
        String uniqueFileNumber = UUID.randomUUID().toString();
        String filename = "export_" + uniqueFileNumber.substring(0, uniqueFileNumber.length() / 5).replaceAll("-", "_");
        File outputFile = new File(activity.getCacheDir(), filename + ext);

        try {
            FileWriter out = new FileWriter(outputFile);
            out.write(text);
            out.close();
        } catch (IOException e) {
        }

        return outputFile;
    }

    public static String getNoteString(Note currentNote, Realm realm) {
        RealmResults<CheckListItem> results = Helper.sortChecklist(currentNote, realm);
        StringBuilder formattedString = new StringBuilder();

        if (currentNote.isCheckList()) {
            for (CheckListItem item : results) {
                String itemString = "";
                if (item.getText().isEmpty() && item.getAudioPath() != null && !item.getAudioPath().isEmpty())
                    itemString = "[Audio]";
                else
                    itemString = item.getText().trim();
                formattedString.append(item.isChecked() ? "[x] " : "[ ] ").append(itemString).append("\n");
                for (SubCheckListItem subCheckListItem : item.getSubChecklist()) {
                    formattedString.append(item.isChecked() ? "    [x] " : "[ ] ").append(subCheckListItem.getText().trim()).append("\n");
                }
            }
        }

        return formattedString.toString();
    }

    public static void exportFiles(String extension, Activity activity,
                                   RealmResults<Note> selectedNotes, Realm realm) {

        if (selectedNotes.size() == 0)
            return;

        ArrayList<File> exportFiles = new ArrayList<>();

        for (Note selectedNote : selectedNotes) {
            if (extension.equals(".md"))
                exportFiles.add(createTempExportFile(activity, selectedNote.isCheckList() ?
                        getNoteString(selectedNote, realm) : selectedNote.getNote(), extension));
            else if (extension.equals(".txt")) {
                String text = selectedNote.isCheckList() ?
                        getNoteString(selectedNote, realm) :
                        Helper.removeMarkdownFormatting(selectedNote.getNote());
                exportFiles.add(createTempExportFile(activity, text, extension));
            }
        }

        if (exportFiles.size() > 0)
            shareFiles(activity, exportFiles);
    }

    public static String removeMarkdownFormatting(String text) {
        return Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT).toString()
                .replaceAll("&nbsp;", " ")
                .replaceAll("<br>", "\n");
    }

    public static void openMapView(Activity activity, Place place) {
        try {
            String mapTo = "https://www.google.com/maps/search/?api=1&query=" + place.getLatitude()
                    + "," + place.getLongitude() + "&query_place_id=" + place.getPlaceId();
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(mapTo));
            intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
            activity.startActivity(intent);
        }catch (Exception e){}
    }

    public static void cancelNotification(Context context, int notificationId) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationId);
    }

    public static void updateKeyboardStatus(Activity activity) {
        // get the root view of your activity
        View rootView = activity.getWindow().getDecorView().getRootView();

        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        int heightDiff = rootView.getRootView().getHeight() - (r.bottom - r.top);

        if (heightDiff > 300) {
            AppData.getAppData().isKeyboardOpen = true;
        } else {
            AppData.getAppData().isKeyboardOpen = false;
        }
    }

    public static void toggleKeyboard(Context context, View view, boolean open) {
        if (context != null && view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                if (open) {
                    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
                } else {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }
    }

    public static String formatToTwoDecimalPlaces(double number) {
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        return decimalFormat.format(number);
    }

    public static void refreshActivity(Activity activity){
        RealmSingleton.getInstance(activity);
        activity.finish();
        Intent refreshActivity = new Intent(activity, activity.getClass());
        activity.startActivity(refreshActivity);
        if(!AppData.isDisableAnimation)
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
