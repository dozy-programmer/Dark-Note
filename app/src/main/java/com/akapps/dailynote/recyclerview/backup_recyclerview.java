package com.akapps.dailynote.recyclerview;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.CategoryScreen;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.activity.SettingsScreen;
import com.akapps.dailynote.adapter.IconMenuAdapter;
import com.akapps.dailynote.classes.data.Backup;
import com.akapps.dailynote.classes.data.Folder;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.SubCheckListItem;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.other.FolderItemSheet;
import com.akapps.dailynote.classes.other.IconPowerMenuItem;
import com.akapps.dailynote.classes.other.InfoSheet;
import com.akapps.dailynote.classes.other.LockSheet;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.skydoves.powermenu.CustomPowerMenu;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;

import io.realm.Realm;
import io.realm.RealmResults;
import www.sanju.motiontoast.MotionToast;

public class backup_recyclerview extends RecyclerView.Adapter<backup_recyclerview.MyViewHolder>{

    // project data
    private RealmResults<Backup> allBackups;
    private User currentUser;
    private final FragmentActivity activity;
    private final Context context;
    private final boolean isLightMode;

    // database
    private final Realm realm;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView file_name;
        private final TextView file_date;
        private final TextView file_size;
        private final MaterialButton delete;
        private final MaterialButton sync;
        private View view;
        private LinearLayout background;

        public MyViewHolder(View v) {
            super(v);
            file_name = v.findViewById(R.id.file_name);
            file_date = v.findViewById(R.id.file_date);
            file_size = v.findViewById(R.id.file_size);
            delete = v.findViewById(R.id.delete);
            sync = v.findViewById(R.id.sync);
            view = v;
            background = v.findViewById(R.id.background);
        }
    }

    public backup_recyclerview(RealmResults<Backup> allBackups, User currentUser, Realm realm,
                               FragmentActivity activity, Context context) {
        this.allBackups = allBackups;
        this.currentUser = currentUser;
        this.realm = realm;
        this.activity = activity;
        this.context = context;
        isLightMode = realm.where(User.class).findFirst().isModeSettings();
    }

    @Override
    public backup_recyclerview.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_backup_layout, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        // retrieves current photo object
        Backup currentBackup = allBackups.get(position);

        String fileName = currentBackup.getFileName().replace("_backup.zip", "");

        try {
            holder.file_name.setText(fileName.split("~")[1] + "_backup.zip");
            holder.file_date.setText(fileName.split("~")[0].replace("_", " "));
            holder.file_size.setText("~ " + fileName.split("~")[2] + " MB");
        }catch (Exception e){
            holder.file_name.setText(fileName);
            holder.file_date.setText("Date Unknown");
            holder.file_size.setText("         ~ ? MB");
        }

        holder.view.setOnClickListener(view -> ((SettingsScreen) activity).restoreFromDatabase(currentBackup.getFileName()));

        holder.sync.setOnClickListener(view -> ((SettingsScreen) activity).restoreFromDatabase(currentBackup.getFileName()));

        holder.delete.setOnClickListener(view -> {
            deleteBackupFile(currentBackup);
        });

        holder.background.setBackgroundColor(isLightMode ? context.getColor(R.color.light_mode) : context.getColor(R.color.gray));
    }

    @Override
    public int getItemCount() {
        return allBackups.size();
    }

    private void deleteBackupFile(Backup currentBackup){
        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference storageRef = storage.getReference().child("users")
                .child(currentUser.getEmail())
                .child(currentBackup.getFileName());

        // Delete the file
        storageRef.delete().addOnSuccessListener(aVoid -> {
            // File deleted successfully
            realm.beginTransaction();
            currentBackup.deleteFromRealm();
            realm.commitTransaction();
            notifyDataSetChanged();
        }).addOnFailureListener(exception -> {
            Helper.showMessage(activity, "Backup Error", "Not Deleted. Try again", MotionToast.TOAST_ERROR);
        });
    }
}
