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
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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
        private MaterialCardView background;

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

    public backup_recyclerview(RealmResults<Backup> allBackups, Realm realm,
                               FragmentActivity activity, Context context) {
        this.allBackups = allBackups;
        this.realm = realm;
        this.activity = activity;
        this.context = context;
        currentUser = RealmSingleton.getUser();
        isLightMode = currentUser.isModeSettings();
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

        if(AppData.getAppData().isDarkerMode){
            holder.background.setCardBackgroundColor(context.getColor(R.color.darker_mode));
            holder.background.setStrokeColor(context.getColor(R.color.light_gray));
            holder.background.setStrokeWidth(5);
        }
        else
            holder.background.setCardBackgroundColor(context.getColor(R.color.light_gray));

        String fileName = currentBackup.getFileName().replace("_backup.zip", "");

        String fileSize = "";

        // February_16_2023~04_37_PM~33.40 kB

        try {
            holder.file_name.setText(fileName.split("~")[1] + "_backup.zip");
            holder.file_date.setText(fileName.split("~")[0].replace("_", " "));
            fileSize = fileName.split("~")[2].replace("_", " ");
            holder.file_size.setText(fileSize);
        }catch (Exception e){
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
