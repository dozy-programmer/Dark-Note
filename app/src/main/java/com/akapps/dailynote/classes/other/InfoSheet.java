package com.akapps.dailynote.classes.other;

import android.Manifest;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.activity.NoteLockScreen;
import com.akapps.dailynote.activity.SettingsScreen;
import com.akapps.dailynote.classes.data.Backup;
import com.akapps.dailynote.classes.data.Photo;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmHelper;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.akapps.dailynote.fragments.notes;
import com.akapps.dailynote.recyclerview.backup_recyclerview;
import com.bumptech.glide.Glide;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.stfalcon.imageviewer.StfalconImageViewer;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

import io.realm.RealmResults;
import io.realm.Sort;
import www.sanju.motiontoast.MotionToast;

public class InfoSheet extends RoundedBottomSheetDialogFragment {

    private int message;
    private boolean deleteAllChecklists;

    private int position;
    private Photo currentPhoto;
    private RealmResults<Photo> allNotePhotos;
    private RecyclerView.Adapter adapter;

    private RealmResults<Backup> allBackups;
    private RecyclerView backupRecyclerview;
    public RecyclerView.Adapter backupAdapter;

    private String userSecurityWord;
    boolean isAppLocked;
    private int attempts;
    private String messageText;
    private boolean deleteMultipleNotes;
    private Fragment fragmentActivity;
    private boolean isTrashSelected;
    private String titleText;
    private String permission;

    public InfoSheet() {
    }

    public InfoSheet(String messageText, int message) {
        this.messageText = messageText;
        this.message = message;
    }

    public InfoSheet(int message) {
        this.message = message;
    }

    public InfoSheet(int message, int position) {
        this.message = message;
        this.position = position;
    }

    public InfoSheet(int message, boolean deleteAllChecklists) {
        this.message = message;
        this.deleteAllChecklists = deleteAllChecklists;
    }

    public InfoSheet(int message, String userSecurityWord, boolean isAppLocked) {
        this.message = message;
        this.userSecurityWord = userSecurityWord;
        this.isAppLocked = isAppLocked;
    }

    public InfoSheet(String title, String permission){
        message = 13;
        this.titleText = title;
        this.permission = permission;
    }

    public InfoSheet(int message, boolean deleteMultipleNotes, Fragment fragmentActivity, boolean isTrashSelected) {
        this.message = message;
        this.deleteMultipleNotes = deleteMultipleNotes;
        this.fragmentActivity = fragmentActivity;
        this.isTrashSelected = isTrashSelected;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_info, container, false);

        if (savedInstanceState != null) {
            message = savedInstanceState.getInt("message");
            deleteAllChecklists = savedInstanceState.getBoolean("check");
            position = savedInstanceState.getInt("position");
            userSecurityWord = savedInstanceState.getString("word");
        }

        TextView title = view.findViewById(R.id.title);
        MaterialButton backup = view.findViewById(R.id.backup);
        MaterialButton delete = view.findViewById(R.id.delete_directly);
        TextView info = view.findViewById(R.id.info);
        TextInputLayout securityWordLayout = view.findViewById(R.id.security_word_layout);
        TextInputEditText securityWord = view.findViewById(R.id.security_word);
        ImageView unlock = view.findViewById(R.id.unlock);
        ImageView budgetChecklist = view.findViewById(R.id.budget_checklist);
        ImageView budgetGraph = view.findViewById(R.id.budget_graph);

        if (RealmHelper.getUser(getContext(), "bottom sheet").getScreenMode() == User.Mode.Dark) {
            securityWordLayout.setBoxBackgroundColor(getContext().getColor(R.color.black));
            securityWordLayout.setHintTextColor(ColorStateList.valueOf(getContext().getColor(R.color.light_gray)));
            securityWordLayout.setDefaultHintTextColor(ColorStateList.valueOf(getContext().getColor(R.color.light_gray)));
            securityWord.setTextColor(getContext().getColor(R.color.gray));
            view.setBackgroundColor(getContext().getColor(R.color.black));
        } else if (RealmHelper.getUser(getContext(), "bottom sheet").getScreenMode() == User.Mode.Gray)
            view.setBackgroundColor(getContext().getColor(R.color.gray));
        else if (RealmHelper.getUser(getContext(), "bottom sheet").getScreenMode() == User.Mode.Light) {

        }

        if (message == -1) {
            title.setText("Info");
            backup.setVisibility(View.GONE);
            securityWord.setVisibility(View.GONE);
            info.setText("Lock screen will not show after clicking reminder notification " +
                    "since reminder was set before locking note. To fix this, just reset reminder ");
            info.setGravity(Gravity.CENTER);
        } else if (message == 0) {
            title.setText("Guide");
            backup.setVisibility(View.GONE);
            securityWord.setVisibility(View.GONE);
            info.setTextColor(getContext().getColor(R.color.pressed_blue));
            info.setText("Change folder name/color/delete" +
                    " by clicking on edit icon on the top right and then select desired folder.\n\n" +
                    "2 ways to add notes to a folder:\n\n" +
                    "You can select multiple notes " +
                    "in the homepage by long clicking a note " +
                    "and then clicking the folder icon\n\n" +
                    "Or individually when editing a note " +
                    "by clicking on \"none\" next to Folder");
        } else if (message == 1) {
            title.setText("Backup");
            securityWord.setVisibility(View.GONE);
            backup.setVisibility(View.VISIBLE);
            info.setText("Backup to Google Drive, OneDrive, and " +
                    "more.\nGoogle Drive is recommended\n\n" +
                    "**Photos will NOT backup**\n" +
                    "Backup file name ends in .realm");
            info.setGravity(Gravity.CENTER);
        } else if (message == 2) {
            title.setText("Backup");
            securityWord.setVisibility(View.GONE);
            backup.setVisibility(View.VISIBLE);
            info.setText("Backup to Google Drive, OneDrive, and " +
                    "more.\nGoogle Drive is recommended\n\n" +
                    "Backup file name ends in .zip");
            info.setGravity(Gravity.CENTER);
        } else if (message == 3 || message == -3) {
            title.setText("Deleting...");
            backup.setVisibility(View.VISIBLE);
            backup.setBackgroundColor(getContext().getColor(R.color.red));
            securityWord.setVisibility(View.GONE);
            backup.setText("DELETE");
            if (deleteAllChecklists) {
                title.setText("Deleting Checklist...");
                info.setText("Are you sure you want to delete checklist?");
            } else {
                if (message == 3 && !isTrashSelected) {
                    delete.setVisibility(View.VISIBLE);
                    backup.setText("TRASH");
                    backup.setBackgroundColor(getContext().getColor(R.color.azure));
                    info.setVisibility(View.GONE);
                } else if (message == -3) {
                    backup.setText("DELETE");
                    info.setVisibility(View.GONE);
                } else if (isTrashSelected)
                    info.setVisibility(View.GONE);
            }
            info.setGravity(Gravity.CENTER);
        } else if (message == 4) {
            // initialize data
            allNotePhotos = ((NoteEdit) getActivity()).getPhotos();
            currentPhoto = allNotePhotos.get(position);
            adapter = ((NoteEdit) getActivity()).scrollAdapter;
            // initialize layout
            title.setText("Deleting...");
            backup.setVisibility(View.VISIBLE);
            backup.setBackgroundColor(getContext().getColor(R.color.red));
            backup.setText("DELETE");
            info.setText("Are you sure?");
            info.setGravity(Gravity.CENTER);
            securityWord.setVisibility(View.GONE);
        } else if (message == 5) {
            securityWordLayout.setVisibility(View.VISIBLE);
            securityWord.setVisibility(View.VISIBLE);
            unlock.setVisibility(View.VISIBLE);
            title.setText("Unlock");
            backup.setVisibility(View.GONE);
            info.setText("Enter Security Word to Unlock " + (isAppLocked ? "App" : "Note"));
            info.setGravity(Gravity.CENTER);
        } else if (message == 6) {
            User currentUser = RealmHelper.getUser(getContext(), "bottom sheet");
            allBackups = RealmSingleton.getInstance(getContext()).where(Backup.class).equalTo("userId", currentUser.getUserId()).findAll();

            if (allBackups.size() <= 50) {
                ((SettingsScreen) getActivity()).upLoadData();
                this.dismiss();
            } else {
                // initialize layout
                title.setText("Upload");
                info.setText("Max Uploads of 20 has been reached. Please delete a backup by pressing sync button.");
                info.setGravity(Gravity.CENTER);
                securityWord.setVisibility(View.GONE);
            }
        } else if (message == 7) {
            // initialize layout
            title.setText("Backups");
            info.setGravity(Gravity.CENTER);
            securityWord.setVisibility(View.GONE);

            User currentUser = RealmHelper.getUser(getContext(), "bottom sheet");
            // recyclerview
            allBackups = RealmSingleton.getInstance(getContext()).where(Backup.class).equalTo("userId", currentUser.getUserId()).findAll();
            info.setText("Select file\n\nLoading...");
            backupRecyclerview = view.findViewById(R.id.backup_recyclerview);
            backupRecyclerview.setVisibility(View.VISIBLE);
            backupRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));

            StorageReference backupFiles = FirebaseStorage.getInstance().getReference().child("users")
                    .child(currentUser.getEmail());

            backupFiles.listAll()
                    .addOnSuccessListener(listResult -> {
                        RealmSingleton.getInstance(getContext()).beginTransaction();
                        RealmSingleton.getInstance(getContext()).where(Backup.class).equalTo("userId", currentUser.getUserId()).findAll().deleteAllFromRealm();
                        RealmSingleton.getInstance(getContext()).commitTransaction();
                        for (StorageReference item : listResult.getItems()) {
                            RealmSingleton.getInstance(getContext()).beginTransaction();
                            RealmSingleton.getInstance(getContext()).insert(new Backup(currentUser.getUserId(), item.getName(), addDate(item.getName()), 0));
                            RealmSingleton.getInstance(getContext()).commitTransaction();
                        }
                        allBackups = RealmSingleton.getInstance(getContext()).where(Backup.class).equalTo("userId", currentUser.getUserId())
                                .sort("upLoadTime", Sort.DESCENDING).findAll();

                        if (allBackups.size() == 0)
                            info.setText("No files to sync");
                        else {
                            info.setVisibility(View.GONE);
                            backupAdapter = new backup_recyclerview(allBackups, getActivity(), getContext());
                            backupRecyclerview.setAdapter(backupAdapter);
                        }
                    })
                    .addOnFailureListener(e -> {
                        info.setText("Error Retrieving, check internet connection");
                        // Uh-oh, an error occurred!
                        Helper.showMessage(getActivity(), "Error", "" +
                                "Failed to get backups", MotionToast.TOAST_ERROR);
                        dismiss();
                    });
        } else if (message == 8) {
            // initialize layout
            title.setText("Logout");
            backup.setVisibility(View.VISIBLE);
            backup.setBackgroundColor(getContext().getColor(R.color.red));
            backup.setText("LOGOUT");
            info.setText("Are you sure?");
            info.setGravity(Gravity.CENTER);
            securityWord.setVisibility(View.GONE);
        } else if (message == 9) {
            title.setText("Remove Formatting");
            backup.setVisibility(View.VISIBLE);
            backup.setBackgroundColor(getContext().getColor(R.color.red));
            securityWord.setVisibility(View.GONE);
            backup.setText("REMOVE");
            info.setText("Are you sure?");
            info.setGravity(Gravity.CENTER);
        } else if (message == 10) {
            String budgetChar = ((NoteEdit) getActivity()).user.getBudgetCharacter();
            String expenseChar = ((NoteEdit) getActivity()).user.getExpenseCharacter();
            title.setText("Budget $ Guide");
            backup.setVisibility(View.GONE);
            securityWord.setVisibility(View.GONE);
            info.setTextColor(getContext().getColor(R.color.pressed_blue));
            info.setText(getContext().getString(R.string.try_out_budget)
                    .replaceAll("\\$", Matcher.quoteReplacement(expenseChar))
                    .replaceAll("\\+" + expenseChar, Matcher.quoteReplacement(budgetChar)));

            budgetChecklist.setVisibility(View.VISIBLE);
            budgetGraph.setVisibility(View.VISIBLE);
            List<Drawable> imageList = new ArrayList<>();
            imageList.add(getContext().getResources().getDrawable(R.drawable.budget_checklist));
            imageList.add(getContext().getResources().getDrawable(R.drawable.budget_graph));

            budgetChecklist.setOnClickListener(view12 -> new StfalconImageViewer.Builder<>(getContext(), imageList, (imageView, image) ->
                    Glide.with(getContext()).load(image).into(imageView))
                    .withBackgroundColor(getContext().getColor(R.color.gray))
                    .allowZooming(true)
                    .allowSwipeToDismiss(true)
                    .withHiddenStatusBar(false)
                    .withStartPosition(0)
                    .withTransitionFrom(budgetChecklist)
                    .show());

            budgetGraph.setOnClickListener(view13 -> new StfalconImageViewer.Builder<>(getContext(), imageList, (imageView, image) ->
                    Glide.with(getContext()).load(image).into(imageView))
                    .withBackgroundColor(getContext().getColor(R.color.gray))
                    .allowZooming(true)
                    .allowSwipeToDismiss(true)
                    .withHiddenStatusBar(false)
                    .withStartPosition(1)
                    .withTransitionFrom(budgetGraph)
                    .show());
        } else if (message == 11) {
            title.setText("Audio Info");
            backup.setBackgroundColor(getContext().getColor(R.color.red));
            securityWord.setVisibility(View.GONE);
            info.setText(messageText);
            info.setGravity(Gravity.LEFT);
        } else if (message == 12) {
            title.setText("Export Info");
            backup.setBackgroundColor(getContext().getColor(R.color.red));
            securityWord.setVisibility(View.GONE);
            info.setText(messageText);
            info.setGravity(Gravity.LEFT);
        } else if(message == 13){
            title.setText(titleText);
            backup.setVisibility(View.GONE);
            securityWord.setVisibility(View.GONE);
            String feature = "Backup";
            if(permission.equals(Manifest.permission.POST_NOTIFICATIONS))
                feature = "Reminders";
            else if(permission.equals(Manifest.permission.RECORD_AUDIO))
                feature = "Audio";
            else if(permission.equals(Manifest.permission.CAMERA))
                feature = "Photos";
            info.setText("This permission needs to be enabled so that you can use the " + feature + " feature. " +
                    "\n\nDark Note will only ever ask for you to allow a permission when it is required by the system " +
                    "in order for a feature to work.");
            info.setGravity(Gravity.CENTER);
            backup.setVisibility(View.VISIBLE);
            backup.setBackgroundColor(getContext().getColor(R.color.blue));
            backup.setText("PROCEED");
        }

        unlock.setOnClickListener(v -> {
            attempts++;
            if (securityWord.getText().toString().equals(userSecurityWord)) {
                this.dismiss();
                ((NoteLockScreen) getActivity()).openNote();
            } else
                info.setText("Enter Security word used to lock note \n(" + attempts + " attempts)");
        });

        backup.setOnClickListener(v -> {
            if (message == 1 || message == 2) {
                if(((SettingsScreen) getActivity()).isBackupPermissionEnabled())
                    ((SettingsScreen) getActivity()).openBackUpRestoreDialog();
                else{
                    InfoSheet permissionInfo = new InfoSheet("Backup Permission Required", Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    permissionInfo.show(getActivity().getSupportFragmentManager(), permissionInfo.getTag());
                    dismiss();
                }
            }
            else if (message == 3 || message == -3) {
                if (deleteMultipleNotes)
                    ((notes) fragmentActivity).deleteMultipleNotes(false);
                else {
                    if (deleteAllChecklists)
                        ((NoteEdit) getActivity()).deleteChecklist();
                    else
                        ((NoteEdit) getActivity()).deleteNote(false);
                }
            } else if (message == 4) {
                // delete file
                File fileDelete = new File(currentPhoto.getPhotoLocation());
                if (fileDelete.exists())
                    fileDelete.delete();
                // delete from database
                RealmSingleton.getInstance(getContext()).beginTransaction();
                ((NoteEdit) getActivity()).getPhotos().get(position).deleteFromRealm();
                RealmSingleton.getInstance(getContext()).commitTransaction();
                adapter.notifyDataSetChanged();
                Helper.showMessage(getActivity(), "Delete Status", "Photo has been deleted",
                        MotionToast.TOAST_SUCCESS);
            } else if (message == 8) {
                FirebaseAuth.getInstance().signOut();
                User currentUser = RealmHelper.getUser(getContext(), "");
                RealmSingleton.getInstance(getContext()).beginTransaction();
                currentUser.setEmail("");
                currentUser.setProUser(false);
                RealmSingleton.getInstance(getContext()).commitTransaction();
                ((SettingsScreen) getActivity()).restart();
            } else if (message == 9)
                ((NoteEdit) getActivity()).removeFormatting();
            else if (message == 13){
                if(permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    ((SettingsScreen) getActivity()).openBackUpRestoreDialog();
                }
                else if(permission.equals(Manifest.permission.POST_NOTIFICATIONS)){
                    ((NoteEdit) getActivity()).checkNotificationPermission();
                }
                else if(permission.equals(Manifest.permission.RECORD_AUDIO)){
                    ((NoteEdit) getActivity()).checkMicrophonePermission();
                }
                else if(permission.equals(Manifest.permission.CAMERA)){
                    ((NoteEdit) getActivity()).showCameraDialog();
                }
            }

            this.dismiss();
        });

        delete.setOnClickListener(view1 -> {
            if (deleteMultipleNotes)
                ((notes) fragmentActivity).deleteMultipleNotes(true);
            else
                // delete note without sending to trash
                ((NoteEdit) getActivity()).deleteNote(true);
            this.dismiss();
        });

        return view;
    }

    private Date addDate(String currentDate) {
        String fileName = currentDate.replace("_backup.zip", "");
        String[] splitDate = fileName.split("~");
        String newDate = splitDate[0] + " " + splitDate[1];
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM_dd_yyyy hh_mm_a");

        Date date = null;
        try {
            date = formatter.parse(newDate);
        } catch (ParseException e) {
            return null;
        }
        return date;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("message", message);
        outState.putInt("position", position);
        outState.putBoolean("check", deleteAllChecklists);
        outState.putString("word", userSecurityWord);
    }

    @Override
    public int getTheme() {
        return UiHelper.getBottomSheetTheme(getContext());
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        UiHelper.setBottomSheetBehavior(view, dialog);
    }

}