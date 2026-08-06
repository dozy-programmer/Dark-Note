package com.akapps.dailynote.classes.other;

import android.Manifest;
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
import com.akapps.dailynote.classes.helpers.AppConstants;
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

    private int position;
    private Photo currentPhoto;
    private RealmResults<Photo> allNotePhotos;
    private RecyclerView.Adapter adapter;

    private RealmResults<Backup> allBackups;
    private RecyclerView backupRecyclerview;
    public RecyclerView.Adapter backupAdapter;

    private String userSecurityWord;
    private String tempCode;
    boolean isAppLocked;
    private int attempts;
    private String messageText;
    private boolean deleteMultipleNotes;
    private Fragment fragmentActivity;
    private boolean isTrashSelected;
    private String titleText;
    private String permission;

    private MaterialButton redirectNote;

    private ArrayList<String> unUsedFiles;

    private boolean isInMainScreen = false;

    public InfoSheet() {
    }

    public InfoSheet(int message) {
        this.message = message;
    }

    public InfoSheet(int message, ArrayList<String> unUsedFiles) {
        this.message = message;
        this.unUsedFiles = unUsedFiles;
    }

    public InfoSheet(String messageText, int message) {
        this.messageText = messageText;
        this.message = message;
    }

    public InfoSheet(int message, int position) {
        this.message = message;
        this.position = position;
    }

    public InfoSheet(int message, MaterialButton redirectNote) {
        this.message = message;
        this.redirectNote = redirectNote;
    }

    public InfoSheet(int message, String userSecurityWord, String tempCode, boolean isAppLocked) {
        this.message = message;
        this.userSecurityWord = userSecurityWord;
        this.tempCode = tempCode;
        this.isAppLocked = isAppLocked;
    }

    public InfoSheet(String title, String permission) {
        message = 13;
        this.titleText = title;
        this.permission = permission;
    }

    public InfoSheet(int message, boolean deleteMultipleNotes, Fragment fragmentActivity, boolean isTrashSelected, boolean isInMainScreen) {
        this.message = message;
        this.deleteMultipleNotes = deleteMultipleNotes;
        this.fragmentActivity = fragmentActivity;
        this.isTrashSelected = isTrashSelected;
        this.isInMainScreen = isInMainScreen;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_info, container, false);

        if (savedInstanceState != null) {
            message = savedInstanceState.getInt("message");
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

        if (message == -1) {
            title.setText(R.string.info);
            backup.setVisibility(View.GONE);
            securityWord.setVisibility(View.GONE);
            info.setText(R.string.msg_info_lock_screen_reminder);
            info.setGravity(Gravity.CENTER);
        } else if (message == 0) {
            title.setText(R.string.msg_guide);
            backup.setVisibility(View.GONE);
            securityWord.setVisibility(View.GONE);
            info.setText(R.string.msg_guide_folder_info);
        } else if (message == 1) {
            title.setText(R.string.msg_unused_files);
            securityWord.setVisibility(View.GONE);
            backup.setVisibility(View.VISIBLE);
            backup.setText(R.string.confirm);
            StringBuilder formattedFilesNames = new StringBuilder();
            if (unUsedFiles != null) {
                for (int i = 0; i < unUsedFiles.size(); i++) {
                    formattedFilesNames.append("\n").append(i + 1).append(": ").append(unUsedFiles.get(i));
                }
            }
            info.setText(getString(R.string.msg_unused_files_prompt, formattedFilesNames.toString()));
            info.setGravity(Gravity.CENTER);
        } else if (message == 3 || message == -3 || message == 14) {
            title.setText(R.string.msg_deleting);
            backup.setVisibility(View.VISIBLE);
            securityWord.setVisibility(View.GONE);
            backup.setText(R.string.delete);
            if (message == 14) {
                title.setText(R.string.msg_deleting_link);
                info.setText(R.string.msg_confirm_delete_link);
                backup.setText(R.string.msg_yes);
                delete.setText(R.string.msg_no);
                delete.setVisibility(View.VISIBLE);
            } else {
                if (message == 3 && !isTrashSelected) {
                    delete.setVisibility(View.VISIBLE);
                    backup.setText(R.string.msg_trash_button);
                    info.setVisibility(View.GONE);
                } else if (message == -3) {
                    backup.setText(R.string.delete);
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
            title.setText(R.string.msg_deleting);
            backup.setVisibility(View.VISIBLE);
            backup.setText(R.string.delete);
            info.setText(R.string.msg_are_you_sure);
            info.setGravity(Gravity.CENTER);
            securityWord.setVisibility(View.GONE);
        } else if (message == 5) {
            securityWordLayout.setVisibility(View.VISIBLE);
            securityWord.setVisibility(View.VISIBLE);
            unlock.setVisibility(View.VISIBLE);
            title.setText(R.string.msg_unlock);
            backup.setVisibility(View.GONE);
            info.setText(getString(R.string.msg_enter_security_word_to_unlock, isAppLocked ? getString(R.string.msg_app) : getString(R.string.msg_note_target)));
            info.setGravity(Gravity.CENTER);
        } else if (message == 6) {
            User currentUser = RealmHelper.getUser(getContext(), "bottom sheet");
            allBackups = RealmSingleton.get(getContext()).where(Backup.class).equalTo("userId", currentUser.getUserId()).findAll();

            if (allBackups.size() <= 250) {
                ((SettingsScreen) getActivity()).uploadData();
                this.dismiss();
            } else {
                // initialize layout
                title.setText(R.string.upload);
                info.setText(R.string.msg_max_uploads_reached);
                info.setGravity(Gravity.CENTER);
                securityWord.setVisibility(View.GONE);
            }
        } else if (message == 7) {
            // initialize layout
            title.setText(R.string.msg_backups);
            info.setGravity(Gravity.CENTER);
            securityWord.setVisibility(View.GONE);

            User currentUser = RealmHelper.getUser(getContext(), "bottom sheet");
            // recyclerview
            allBackups = RealmSingleton.get(getContext()).where(Backup.class).equalTo("userId", currentUser.getUserId()).findAll();
            info.setText(R.string.msg_select_file_loading);
            backupRecyclerview = view.findViewById(R.id.backup_recyclerview);
            backupRecyclerview.setVisibility(View.VISIBLE);
            backupRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));

            StorageReference backupFiles = FirebaseStorage.getInstance().getReference().child("users")
                    .child(currentUser.getEmail());

            backupFiles.listAll()
                    .addOnSuccessListener(listResult -> {
                        RealmSingleton.get(getContext()).beginTransaction();
                        RealmSingleton.get(getContext()).where(Backup.class).equalTo("userId", currentUser.getUserId()).findAll().deleteAllFromRealm();
                        RealmSingleton.get(getContext()).commitTransaction();
                        for (StorageReference item : listResult.getItems()) {
                            RealmSingleton.get(getContext()).beginTransaction();
                            RealmSingleton.get(getContext()).insert(new Backup(currentUser.getUserId(), item.getName(), addDate(item.getName()), 0));
                            RealmSingleton.get(getContext()).commitTransaction();
                        }
                        allBackups = RealmSingleton.get(getContext()).where(Backup.class).equalTo("userId", currentUser.getUserId())
                                .sort("upLoadTime", Sort.DESCENDING).findAll();

                        if (allBackups.size() == 0)
                            info.setText(R.string.msg_no_files_to_sync);
                        else {
                            info.setVisibility(View.GONE);
                            backupAdapter = new backup_recyclerview(allBackups, getActivity(), getContext());
                            backupRecyclerview.setAdapter(backupAdapter);
                        }
                    })
                    .addOnFailureListener(e -> {
                        info.setText(R.string.msg_error_retrieving_internet);
                        // Uh-oh, an error occurred!
                        Helper.showMessage(getActivity(), getString(R.string.msg_error),
                                getString(R.string.msg_failed_to_get_backups), MotionToast.TOAST_ERROR);
                        dismiss();
                    });
        } else if (message == 8) {
            // initialize layout
            title.setText(R.string.msg_logout);
            backup.setVisibility(View.VISIBLE);
            backup.setText(R.string.msg_logout_button);
            info.setText(R.string.msg_are_you_sure);
            info.setGravity(Gravity.CENTER);
            securityWord.setVisibility(View.GONE);
        } else if (message == 9) {
            title.setText(R.string.msg_remove_all_formatting);
            backup.setVisibility(View.VISIBLE);
            securityWord.setVisibility(View.GONE);
            backup.setText(R.string.msg_remove);
            info.setText(R.string.msg_are_you_sure);
            info.setGravity(Gravity.CENTER);
        } else if (message == 10) {
            String budgetChar = ((NoteEdit) getActivity()).getUser(getContext()).getBudgetCharacter();
            String expenseChar = ((NoteEdit) getActivity()).getUser(getContext()).getExpenseCharacter();
            title.setText(R.string.msg_budget_guide);
            backup.setVisibility(View.GONE);
            securityWord.setVisibility(View.GONE);
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
                    .withBackgroundColor(UiHelper.getColorFromTheme(getActivity(), R.attr.primaryBackgroundColor))
                    .allowZooming(true)
                    .withDismissListener(() -> {
                        budgetChecklist.setImageDrawable(null);
                        budgetChecklist.setImageDrawable(getContext().getDrawable(R.drawable.budget_checklist));
                    })
                    .allowSwipeToDismiss(true)
                    .withHiddenStatusBar(false)
                    .withStartPosition(0)
                    .withTransitionFrom(budgetChecklist)
                    .show());

            budgetGraph.setOnClickListener(view13 -> new StfalconImageViewer.Builder<>(getContext(), imageList, (imageView, image) ->
                    Glide.with(getContext()).load(image).into(imageView))
                    .withBackgroundColor(UiHelper.getColorFromTheme(getActivity(), R.attr.primaryBackgroundColor))
                    .allowZooming(true)
                    .withDismissListener(() -> {
                        budgetGraph.setImageDrawable(null);
                        budgetGraph.setImageDrawable(getContext().getDrawable(R.drawable.budget_graph));
                    })
                    .allowSwipeToDismiss(true)
                    .withHiddenStatusBar(false)
                    .withStartPosition(1)
                    .withTransitionFrom(budgetGraph)
                    .show());
        } else if (message == 11) {
            title.setText(R.string.msg_audio_info);
            securityWord.setVisibility(View.GONE);
            info.setText(messageText);
            info.setGravity(Gravity.LEFT);
        } else if (message == 12) {
            title.setText(R.string.msg_export_info);
            securityWord.setVisibility(View.GONE);
            info.setText(messageText);
            info.setGravity(Gravity.LEFT);
        } else if (message == 13) {
            title.setText(titleText);
            backup.setVisibility(View.GONE);
            securityWord.setVisibility(View.GONE);
            String feature = getString(R.string.backup);
            if (permission.equals(Manifest.permission.POST_NOTIFICATIONS))
                feature = getString(R.string.reminders);
            else if (permission.equals(Manifest.permission.RECORD_AUDIO))
                feature = getString(R.string.audio);
            else if (permission.equals(Manifest.permission.READ_MEDIA_IMAGES))
                feature = getString(R.string.photos).trim();
            info.setText(getString(R.string.msg_permission_explanation, feature,
                    permission.equals(Manifest.permission.READ_MEDIA_IMAGES) ? getString(R.string.msg_photos_allow_all_hint) : ""));
            info.setGravity(Gravity.CENTER);
            backup.setVisibility(View.VISIBLE);
            backup.setText(R.string.proceed);
        }

        unlock.setOnClickListener(v -> {
            attempts++;
            if (securityWord.getText().toString().equals(userSecurityWord)) {
                this.dismiss();
                ((NoteLockScreen) getActivity()).openNote(false);
            } else if(securityWord.getText().toString().equals(tempCode)){
                this.dismiss();
                ((NoteLockScreen) getActivity()).openNote(true);
            }
            else
                info.setText(getString(R.string.msg_enter_security_word_attempts, attempts));
        });

        backup.setOnClickListener(v -> {
            if (message == 1) {
                if (unUsedFiles != null & unUsedFiles.size() > 0) {
                    Helper.showFloatingFiles(getActivity(), true);
                    Helper.showMessage(getActivity(), getString(R.string.msg_files_deleted),
                            getString(R.string.msg_unused_files_deleted), MotionToast.TOAST_WARNING);
                }
            } else if (message == 3 || message == -3 || message == 14) {
                if (message == 14) {
                    redirectNote.setText("");
                    redirectNote.setVisibility(View.GONE);
                    dismiss();
                } else if (deleteMultipleNotes && isInMainScreen)
                    ((notes) fragmentActivity).deleteMultipleNotes(false);
                else {
                    ((NoteEdit) getActivity()).deleteNote(false);
                }
            } else if (message == 4) {
                // delete file
                File fileDelete = new File(currentPhoto.getPhotoLocation());
                if (fileDelete.exists())
                    fileDelete.delete();
                // delete from database
                RealmSingleton.get(getContext()).beginTransaction();
                ((NoteEdit) getActivity()).getPhotos().get(position).deleteFromRealm();
                RealmSingleton.get(getContext()).commitTransaction();
                adapter.notifyDataSetChanged();
                Helper.showMessage(getActivity(), getString(R.string.msg_delete_status),
                        getString(R.string.msg_photo_deleted), MotionToast.TOAST_SUCCESS);
            } else if (message == 8) {
                FirebaseAuth.getInstance().signOut();
                User currentUser = RealmHelper.getUser(getContext(), "");
                RealmSingleton.get(getContext()).beginTransaction();
                currentUser.setEmail("");
                currentUser.setProUser(false);
                RealmSingleton.get(getContext()).commitTransaction();
                Helper.restart(getActivity());
            } else if (message == 9)
                ((NoteEdit) getActivity()).removeFormatting();
            else if (message == 13) {
                switch (permission) {
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        ((SettingsScreen) getActivity()).requestBackupPermissions();
                        break;
                    case Manifest.permission.POST_NOTIFICATIONS:
                        ((NoteEdit) getActivity()).checkNotificationPermission();
                        break;
                    case Manifest.permission.RECORD_AUDIO:
                        ((NoteEdit) getActivity()).checkMicrophonePermission();
                        break;
                }
            }

            this.dismiss();
        });

        delete.setOnClickListener(view1 -> {
            if (message == 14)
                this.dismiss();
            else if (deleteMultipleNotes)
                ((notes) fragmentActivity).deleteMultipleNotes(true);
            else
                // delete note without sending to trash
                ((NoteEdit) getActivity()).deleteNote(true);
            this.dismiss();
        });

        return view;
    }

    private Date addDate(String currentDate) {
        String fileName = currentDate.replace("_" + AppConstants.BACKUP_ZIP_FILE_NAME, "");
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