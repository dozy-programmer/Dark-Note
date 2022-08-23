package com.akapps.dailynote.classes.other;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.activity.NoteLockScreen;
import com.akapps.dailynote.activity.SettingsScreen;
import com.akapps.dailynote.classes.data.Backup;
import com.akapps.dailynote.classes.data.Photo;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.recyclerview.backup_recyclerview;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import www.sanju.motiontoast.MotionToast;

public class InfoSheet extends RoundedBottomSheetDialogFragment{

    private int message;
    private boolean deleteAllChecklists;

    private Realm realm;
    private int position;
    private Photo currentPhoto;
    private RealmResults<Photo> allNotePhotos;
    private RecyclerView.Adapter adapter;

    private RealmResults<Backup> allBackups;
    private RecyclerView backupRecyclerview;
    public RecyclerView.Adapter backupAdapter;

    private String userSecurityWord;
    private int attempts;

    public InfoSheet(){
    }

    public InfoSheet(int message){
        this.message = message;
    }

    public InfoSheet(int message, int position){
        this.message = message;
        this.position = position;
    }

    public InfoSheet(int message, String userSecurityWord){
        this.message = message;
        this.userSecurityWord = userSecurityWord;
    }

    public InfoSheet(int message,boolean deleteAllChecklists){
        this.message = message;
        this.deleteAllChecklists = deleteAllChecklists;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_info, container, false);

        if(savedInstanceState!=null) {
            message = savedInstanceState.getInt("message");
            deleteAllChecklists = savedInstanceState.getBoolean("check");
            position = savedInstanceState.getInt("position");
            userSecurityWord = savedInstanceState.getString("word");
        }

        TextView title  =view.findViewById(R.id.title);
        MaterialButton backup = view.findViewById(R.id.backup);
        TextView info  = view.findViewById(R.id.info);

        TextInputLayout securityWordLayout = view.findViewById(R.id.security_word_layout);
        TextInputEditText securityWord = view.findViewById(R.id.security_word);
        ImageView unlock = view.findViewById(R.id.unlock);

        if (AppData.getAppData().isLightMode) {
            securityWordLayout.setBoxBackgroundColor(getContext().getColor(R.color.light_mode));
            securityWordLayout.setHintTextColor(ColorStateList.valueOf(getContext().getColor(R.color.light_gray)));
            securityWordLayout.setDefaultHintTextColor(ColorStateList.valueOf(getContext().getColor(R.color.light_gray)));
            securityWord.setTextColor(getContext().getColor(R.color.gray));
            view.setBackgroundColor(getContext().getColor(R.color.light_mode));
        }
        else
            view.setBackgroundColor(getContext().getColor(R.color.gray));

          if(message == -1){
            title.setText("Info");
            backup.setVisibility(View.GONE);
            securityWord.setVisibility(View.GONE);
            info.setText("Lock screen will not show after clicking reminder notification " +
                    "since reminder was set before locking note. To fix this, just reset reminder ");
              info.setGravity(Gravity.CENTER);
        }
        else if(message == 0){
            title.setText("Guide");
            backup.setVisibility(View.GONE);
            securityWord.setVisibility(View.GONE);
            info.setText("Change folder name/color/delete" +
                    " by clicking on edit icon on the top right and then select desired folder.\n\n" +
                    "2 ways to add notes to a folder:\n\n" +
                    "You can select multiple notes " +
                    "in the homepage by long clicking a note " +
                    "and then clicking the folder icon\n\n" +
                    "Or individually when editing a note " +
                    "by clicking on \"none\" next to Folder");
        }
        else if(message == 1){
            title.setText("Backup");
            securityWord.setVisibility(View.GONE);
            backup.setVisibility(View.VISIBLE);
            info.setText("Backup to Google Drive, OneDrive, and " +
                    "more.\nGoogle Drive is recommended\n\n" +
                    "**Photos will NOT backup**\n" +
                    "Backup file name ends in .realm");
            info.setGravity(Gravity.CENTER);
        }
         else if(message == 2){
            title.setText("Backup");
            securityWord.setVisibility(View.GONE);
            backup.setVisibility(View.VISIBLE);
            info.setText("Backup to Google Drive, OneDrive, and " +
                    "more.\nGoogle Drive is recommended\n\n" +
                    "Backup file name ends in .zip");
            info.setGravity(Gravity.CENTER);
        }
        else if(message == 3 || message == -3){
            title.setText("Deleting...");
            backup.setVisibility(View.VISIBLE);
            backup.setBackgroundColor(getContext().getColor(R.color.red));
            securityWord.setVisibility(View.GONE);
            backup.setText("DELETE");
            if(deleteAllChecklists){
                title.setText("Deleting Checklist...");
                info.setText("Are you sure you want to delete checklist?");
            }
            else {
                if(message == 3)
                    info.setText("Send to trash?");
                else if(message == -3)
                    info.setText("Delete from Trash?");
            }
            info.setGravity(Gravity.CENTER);
        }
        else if(message == 4){
            // initialize data
            realm = ((NoteEdit) getActivity()).realm;
            allNotePhotos =  ((NoteEdit) getActivity()).allNotePhotos;
            currentPhoto = allNotePhotos.get(position);
            adapter =  ((NoteEdit) getActivity()).scrollAdapter;
            // initialize layout
            title.setText("Deleting...");
            backup.setVisibility(View.VISIBLE);
            backup.setBackgroundColor(getContext().getColor(R.color.red));
            backup.setText("DELETE");
            info.setText("Are you sure?");
            info.setGravity(Gravity.CENTER);
            securityWord.setVisibility(View.GONE);
        }
        else if(message == 5){
            securityWordLayout.setVisibility(View.VISIBLE);
            securityWord.setVisibility(View.VISIBLE);
            unlock.setVisibility(View.VISIBLE);
            title.setText("Unlock");
            backup.setVisibility(View.GONE);
            info.setText("Enter Security word used to lock note");
            info.setGravity(Gravity.CENTER);
        }
        else if(message == 6){
              realm = ((SettingsScreen) getActivity()).realm;
              User currentUser = realm.where(User.class).findFirst();
              allBackups = realm.where(Backup.class).equalTo("userId", currentUser.getUserId()).findAll();

              if(allBackups.size() <= 5){
                  ((SettingsScreen) getActivity()).upLoadData();
                  this.dismiss();
              }
              else{
                  // initialize layout
                  title.setText("Upload");
                  info.setText("Max Uploads of 6 has been reached. Please delete a backup by pressing sync button.");
                  info.setGravity(Gravity.CENTER);
                  securityWord.setVisibility(View.GONE);
              }
        }
        else if(message == 7) {
              // initialize layout
              title.setText("Backups");
              info.setGravity(Gravity.CENTER);
              securityWord.setVisibility(View.GONE);

              realm = ((SettingsScreen) getActivity()).realm;
              User currentUser = realm.where(User.class).findFirst();
              // recyclerview
              allBackups = realm.where(Backup.class).equalTo("userId", currentUser.getUserId()).findAll();
              info.setText("Select file\n\nLoading...");
              backupRecyclerview = view.findViewById(R.id.backup_recyclerview);
              backupRecyclerview.setVisibility(View.VISIBLE);
              backupRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));

              StorageReference backupFiles = FirebaseStorage.getInstance().getReference().child("users")
                      .child(currentUser.getEmail());

              backupFiles.listAll()
                      .addOnSuccessListener(listResult -> {
                          realm.beginTransaction();
                          realm.where(Backup.class).equalTo("userId", currentUser.getUserId()).findAll().deleteAllFromRealm();
                          realm.commitTransaction();
                          for (StorageReference item : listResult.getItems()) {
                              realm.beginTransaction();
                              realm.insert(new Backup(currentUser.getUserId(), item.getName(), "", 0));
                              realm.commitTransaction();
                          }
                          allBackups = realm.where(Backup.class).equalTo("userId", currentUser.getUserId()).findAll();

                          if (allBackups.size() == 0)
                              info.setText("No files to sync");
                          else {
                              info.setVisibility(View.GONE);
                              backupAdapter = new backup_recyclerview(allBackups, currentUser, realm, getActivity(), getContext());
                              backupRecyclerview.setAdapter(backupAdapter);
                          }
                      })
                      .addOnFailureListener(e -> {
                          info.setText("Error Retrieving, check internet connection");
                          // Uh-oh, an error occurred!
                          Log.d("Here", "Failure to retrieve backups ");
                          Helper.showMessage(getActivity(), "Error", "" +
                                  "Failed to get backups", MotionToast.TOAST_ERROR);
                          dismiss();
                      });
        }
        else if(message == 8){
              // initialize layout
              title.setText("Logout");
              backup.setVisibility(View.VISIBLE);
              backup.setBackgroundColor(getContext().getColor(R.color.red));
              backup.setText("LOGOUT");
              info.setText("Are you sure?");
              info.setGravity(Gravity.CENTER);
              securityWord.setVisibility(View.GONE);
        }
        else if(message == 9){
              title.setText("Remove Formatting");
              backup.setVisibility(View.VISIBLE);
              backup.setBackgroundColor(getContext().getColor(R.color.red));
              securityWord.setVisibility(View.GONE);
              backup.setText("REMOVE");
              info.setText("Are you sure?");
              info.setGravity(Gravity.CENTER);
       }
        else if(message == 10){
              title.setText("Budget $ Guide");
              backup.setVisibility(View.GONE);
              securityWord.setVisibility(View.GONE);
              info.setText("Budget $ tracks your expenses and makes it easier to budget.\n\n" +
                      "In order to add an expense(s), all you have to do is add the \"$\" symbol with your " +
                      "amount, it is recommended to include a description for future reference.\n\n" +
                      "To add your budget, include a minus \"-\" before the \"$\" symbol, like so \"-$\" ");
          }


        unlock.setOnClickListener(v -> {
            attempts++;
            if(securityWord.getText().toString().equals(userSecurityWord)) {
                this.dismiss();
                ((NoteLockScreen) getActivity()).openNote();
            }
            else
                info.setText("Enter Security word used to lock note \n(" + attempts + " attempts)");
        });

        backup.setOnClickListener(v -> {
            if(message == 1 || message == 2)
                ((SettingsScreen) getActivity()).openBackUpRestoreDialog();
            else if(message == 3 || message == -3){
                if(deleteAllChecklists){
                    ((NoteEdit) getActivity()).deleteChecklist();
                }
                else
                    ((NoteEdit) getActivity()).deleteNote();
            }
            else if(message == 4){
                // delete file
                File fileDelete = new File(currentPhoto.getPhotoLocation());
                if(fileDelete.exists())
                    fileDelete.delete();
                // delete from database
                realm.beginTransaction();
                ((NoteEdit) getActivity()).allNotePhotos.get(position).deleteFromRealm();
                realm.commitTransaction();
                adapter.notifyDataSetChanged();
                Helper.showMessage(getActivity(), "Delete Status", "Photo has been deleted",
                        MotionToast.TOAST_SUCCESS);
            }
            else if(message == 8){
                FirebaseAuth.getInstance().signOut();
                ((SettingsScreen) getActivity()).restart();
            }
            else if(message == 9)
                ((NoteEdit) getActivity()).removeFormatting();

            this.dismiss();
        });

        return view;
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
        if(AppData.getAppData().isLightMode)
            return R.style.BaseBottomSheetDialogLight;
        else
            return R.style.BaseBottomSheetDialog;
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.getViewTreeObserver()
                .addOnGlobalLayoutListener(() -> {
                    BottomSheetDialog dialog =(BottomSheetDialog) getDialog ();
                    if (dialog != null) {
                        FrameLayout bottomSheet = dialog.findViewById (R.id.design_bottom_sheet);
                        if (bottomSheet != null) {
                            BottomSheetBehavior behavior = BottomSheetBehavior.from (bottomSheet);
                            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                    }
                });
    }

}