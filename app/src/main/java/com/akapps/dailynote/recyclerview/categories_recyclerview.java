package com.akapps.dailynote.recyclerview;

import static com.akapps.dailynote.classes.helpers.RealmHelper.getCurrentFolder;
import static com.akapps.dailynote.classes.helpers.RealmHelper.getRealm;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.CategoryScreen;
import com.akapps.dailynote.classes.data.Folder;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.akapps.dailynote.classes.other.FolderItemSheet;
import com.akapps.dailynote.classes.other.LockFolderSheet;

import io.realm.RealmResults;
import www.sanju.motiontoast.MotionToast;

public class categories_recyclerview extends RecyclerView.Adapter<categories_recyclerview.MyViewHolder> {

    // project data
    private RealmResults<Folder> allCategories;
    private final FragmentActivity activity;
    private final Context context;
    private final int categorySize;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView item_category;
        private final ImageView folder_icon;
        private final ImageView lock_icon;
        private View view;
        private ConstraintLayout background;

        public MyViewHolder(View v) {
            super(v);
            item_category = v.findViewById(R.id.item_category);
            folder_icon = v.findViewById(R.id.folder_icon);
            lock_icon = v.findViewById(R.id.lock_icon);
            view = v;
            background = v.findViewById(R.id.background);
        }
    }

    public categories_recyclerview(RealmResults<Folder> allCategories,
                                   FragmentActivity activity, Context context) {
        this.allCategories = allCategories;
        this.activity = activity;
        this.context = context;
        categorySize = allCategories.size();
    }

    @Override
    public categories_recyclerview.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_category_layout, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        // retrieves current photo object
        Folder currentFolder = allCategories.get(position);
        int folderId = currentFolder.getId();
        int numberOfNotesInCategory =
                RealmSingleton.getInstance(context).where(Note.class)
                        .equalTo("archived", false)
                        .equalTo("trash", false)
                        .equalTo("category", currentFolder.getName())
                        .findAll().size();

        holder.item_category.setText(currentFolder.getName());
        int background = UiHelper.getColorFromTheme(activity, R.attr.primaryStrokeColor);
        int textColor = UiHelper.getColorFromTheme(activity, R.attr.primaryTextColor);
        Helper.addNotificationNumber(activity, holder.folder_icon, numberOfNotesInCategory,
                20, false, background, textColor);

        holder.lock_icon.setVisibility(currentFolder.getPin() > 0 ? View.VISIBLE : View.GONE);

        if (currentFolder.getColor() != 0)
            holder.folder_icon.setColorFilter(currentFolder.getColor());
        else
            holder.folder_icon.setColorFilter(UiHelper.getColorFromTheme(activity, R.attr.primaryButtonColor));

        holder.view.setOnClickListener(v -> {
            RealmResults<Note> allSelectedNotes = RealmSingleton.getInstance(context).where(Note.class).equalTo("isSelected", true).findAll();
            if (((CategoryScreen) activity).isEditing) {
                FolderItemSheet checklistItemSheet = new FolderItemSheet(folderId, this, position, true);
                checklistItemSheet.show(activity.getSupportFragmentManager(), checklistItemSheet.getTag());
            } else {
                Intent home = new Intent();

                home.putExtra("viewing", allSelectedNotes.size() == 0);
                home.putExtra("category", currentFolder.getId());

                RealmResults<Note> lockedNotesInsideFolder = allSelectedNotes.where()
                        .greaterThan("pinNumber", 0)
                        .findAll();

                if (lockedNotesInsideFolder.size() > 0 && currentFolder.getPin() > 0) {
                    // TODO - handle this
                    allSelectedNotes = allSelectedNotes.where()
                            .equalTo("pinNumber", 0)
                            .findAll();
                    // user has selected notes to put in folder
                    LockFolderSheet lockFolderSheet = new LockFolderSheet(currentFolder.getId(), allSelectedNotes, lockedNotesInsideFolder, home);
                    lockFolderSheet.show(activity.getSupportFragmentManager(), lockFolderSheet.getTag());
                }
                else if(currentFolder.getPin() > 0){
                    getRealm(context).beginTransaction();
                    allSelectedNotes.setString("category", currentFolder.getName());
                    allSelectedNotes.setInt("pinNumber", currentFolder.getPin());
                    allSelectedNotes.setString("securityWord", currentFolder.getSecurityWord());
                    allSelectedNotes.setBoolean("fingerprint", currentFolder.isFingerprintAdded());
                    allSelectedNotes.setBoolean("isSelected", false);
                    getRealm(context).commitTransaction();
                    RealmSingleton.setCloseRealm(false);
                    Log.d("Here", "keep realm open in categories_recyclerview");
                    activity.setResult(5, home);
                    activity.finish();
                }else if (allSelectedNotes.size() == 0 && numberOfNotesInCategory == 0) {
                    Helper.showMessage(activity, "Folder Empty", "Folder is empty, cannot open",
                            MotionToast.TOAST_ERROR);
                } else {
                    RealmSingleton.getInstance(context).beginTransaction();
                    allSelectedNotes.setString("category", currentFolder.getName());
                    allSelectedNotes.setBoolean("isSelected", false);
                    RealmSingleton.getInstance(context).commitTransaction();
                    RealmSingleton.setCloseRealm(false);
                    Log.d("Here", "keep realm open in categories_recyclerview");
                    activity.setResult(5, home);
                    activity.finish();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        try {
            return allCategories.size();
        } catch (Exception e) {
            Helper.restart(activity);
            return categorySize;
        }
    }
}
