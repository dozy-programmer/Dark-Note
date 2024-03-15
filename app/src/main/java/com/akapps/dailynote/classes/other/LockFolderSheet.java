package com.akapps.dailynote.classes.other;

import static com.akapps.dailynote.classes.helpers.RealmHelper.getCurrentFolder;
import static com.akapps.dailynote.classes.helpers.RealmHelper.getRealm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import org.jetbrains.annotations.NotNull;

import io.realm.RealmResults;

public class LockFolderSheet extends RoundedBottomSheetDialogFragment {

    private int folderId;
    private RealmResults<Note> selectedNotes;
    private RealmResults<Note> lockedNotes;
    private boolean isUpdatingUI;
    private Intent goHome;

    public LockFolderSheet() {
    }

    public LockFolderSheet(int folderId, RealmResults<Note> selectedNotes, RealmResults<Note> lockedNotes,
                           boolean isUpdatingUi, Intent goHome) {
        this.folderId = folderId;
        this.selectedNotes = selectedNotes;
        this.lockedNotes = lockedNotes;
        this.isUpdatingUI = isUpdatingUi;
        this.goHome = goHome;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_lock_folder, container, false);

        MaterialButton cancel = view.findViewById(R.id.cancel);
        MaterialButton proceed = view.findViewById(R.id.proceed);
        TextView info = view.findViewById(R.id.info);

        StringBuilder lockedNotesTitles = new StringBuilder();
        for (Note lockedNote : lockedNotes) {
            lockedNotesTitles.append(lockedNote.getTitle().isEmpty() ? "~ No Title ~" : lockedNote.getTitle() + "\n");
        }

        StringBuilder infoMessage = new StringBuilder();
        infoMessage
                .append("You cannot add locked notes to this folder due to security*")
                .append("\n\nPlease unlock these notes to add them to this folder:")
                .append("\n" + lockedNotesTitles)
                .append("\n\nWould you like to put all the remaining unlocked notes in the folder?");
        info.setText(infoMessage);
        info.setGravity(Gravity.CENTER);

        cancel.setOnClickListener(v -> {
            this.dismiss();
        });

        proceed.setOnClickListener(view1 -> {
            Log.d("Here", "Folder -> " + getCurrentFolder(getContext(), folderId).getName());
            Log.d("Here", "Num Notes -> " + selectedNotes.size());
            if (selectedNotes.size() > 0) {
                getRealm(getContext()).beginTransaction();
                selectedNotes.setInt("pinNumber", getCurrentFolder(getContext(), folderId).getPin());
                selectedNotes.setString("securityWord", getCurrentFolder(getContext(), folderId).getSecurityWord());
                selectedNotes.setBoolean("fingerprint", getCurrentFolder(getContext(), folderId).isFingerprintAdded());
                getRealm(getContext()).commitTransaction();
            }
            if (isUpdatingUI) {
                RealmSingleton.getInstance(getContext()).beginTransaction();
                selectedNotes.setString("category", getCurrentFolder(getContext(), folderId).getName());
                selectedNotes.setBoolean("isSelected", false);
                RealmSingleton.getInstance(getContext()).commitTransaction();
                RealmSingleton.setCloseRealm(false);
                Log.d("Here", "keep realm open in categories_recyclerview");
                if (goHome != null) {
                    getActivity().setResult(5, goHome);
                    getActivity().finish();
                }
            }
            this.dismiss();
        });

        return view;
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