package com.akapps.dailynote.recyclerview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.SettingsScreen;
import com.akapps.dailynote.classes.data.Backup;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmHelper;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import io.realm.RealmResults;
import www.sanju.motiontoast.MotionToast;

public class backup_recyclerview extends RecyclerView.Adapter<backup_recyclerview.MyViewHolder> {

    // project data
    private final RealmResults<Backup> allBackups;
    private final FragmentActivity activity;
    private final Context context;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView file_name;
        private final TextView file_date;
        private final TextView file_size;
        private final MaterialButton delete;
        private final MaterialButton sync;
        private final View view;
        private final MaterialCardView background;

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

    public backup_recyclerview(RealmResults<Backup> allBackups, FragmentActivity activity, Context context) {
        this.allBackups = allBackups;
        this.activity = activity;
        this.context = context;
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

        if (RealmHelper.getUser(context, "in space").getScreenMode() == User.Mode.Dark) {
            holder.background.setCardBackgroundColor(context.getColor(R.color.black));
            holder.background.setStrokeColor(context.getColor(R.color.light_gray));
            holder.background.setStrokeWidth(5);
        } else if (RealmHelper.getUser(context, "in space").getScreenMode() == User.Mode.Gray)
            holder.background.setCardBackgroundColor(context.getColor(R.color.light_gray));
        else if (RealmHelper.getUser(context, "in space").getScreenMode() == User.Mode.Light) {

        }

        String fileName = currentBackup.getFileName().replace("_backup.zip", "");

        String fileSize = "";

        // February_16_2023~04_37_PM~33.40 kB

        try {
            holder.file_name.setText(fileName.split("~")[1] + "_backup.zip");
            holder.file_date.setText(fileName.split("~")[0].replace("_", " "));
            fileSize = fileName.split("~")[2].replace("_", " ");
            holder.file_size.setText(fileSize);
        } catch (Exception e) {
            holder.file_name.setText("00_00_00_" + fileName);
            holder.file_date.setText("Date ?");
            holder.file_size.setText("? MB");
        }

        holder.sync.setOnClickListener(view -> Helper.showMessage(activity, "Sync File", "Long click to sync", MotionToast.TOAST_WARNING));

        String finalFileSize = fileSize;
        holder.sync.setOnLongClickListener(view -> {
            ((SettingsScreen) activity).restoreFromDatabase(currentBackup.getFileName(), finalFileSize);
            return false;
        });

        holder.delete.setOnClickListener(view -> {
            Helper.showMessage(activity, "Delete File", "Long click to delete", MotionToast.TOAST_WARNING);
        });

        holder.delete.setOnLongClickListener(view -> {
            deleteBackupFile(currentBackup);
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return allBackups.size();
    }

    private void deleteBackupFile(Backup currentBackup) {
        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference storageRef = storage.getReference().child("users")
                .child(RealmHelper.getUser(context, "backup_recyclerview").getEmail())
                .child(currentBackup.getFileName());

        // Delete the file
        storageRef.delete().addOnSuccessListener(aVoid -> {
            // File deleted successfully
            RealmSingleton.getInstance(context).beginTransaction();
            currentBackup.deleteFromRealm();
            RealmSingleton.getInstance(context).commitTransaction();
            notifyDataSetChanged();
        }).addOnFailureListener(exception -> {
            Helper.showMessage(activity, "Backup Error", "Not Deleted. Try again", MotionToast.TOAST_ERROR);
        });
    }
}
