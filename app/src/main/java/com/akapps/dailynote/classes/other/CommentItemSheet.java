package com.akapps.dailynote.classes.other;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.data.SubCheckListItem;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

import io.realm.Realm;

public class CommentItemSheet extends RoundedBottomSheetDialogFragment {

    private CheckListItem currentItem = null;
    private SubCheckListItem currentSubItem = null;
    private boolean isItemSelected = false;
    private int position;
    private String initialComment;

    private TextInputEditText commentEditText;
    private BottomSheetDialog dialog;
    private RecyclerView.Adapter adapter;

    public CommentItemSheet() {
    }

    public CommentItemSheet(CheckListItem currentItem, RecyclerView.Adapter adapter, int position) {
        this.currentItem = currentItem;
        this.adapter = adapter;
        this.position = position;
        isItemSelected = true;
    }

    public CommentItemSheet(SubCheckListItem currentSubItem, RecyclerView.Adapter adapter, int position) {
        this.currentSubItem = currentSubItem;
        this.adapter = adapter;
        this.position = position;
        isItemSelected = false;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_comment_item, container, false);

        if (savedInstanceState != null)
            this.dismiss();

        MaterialButton confirmFilter = view.findViewById(R.id.confirm_filter);

        TextInputLayout commentLayout = view.findViewById(R.id.comment_layout);
        commentEditText = view.findViewById(R.id.comment);

        if (isItemSelected) {
            if (currentItem.getComment() != null) {
                commentEditText.setText(currentItem.getComment());
            }
        } else {
            if (currentSubItem.getComment() != null) {
                commentEditText.setText(currentSubItem.getComment());
            }
        }
        initialComment = commentEditText.getText().toString();
        commentEditText.requestFocusFromTouch();

        commentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                commentLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        confirmFilter.setOnClickListener(v -> {
            String comment = commentEditText.getText().toString();
            if (comment.isBlank() && initialComment.isBlank()) {
                commentLayout.setError("Comment is empty...");
            } else if (isItemSelected) {
                updateItem(currentItem, comment);
                this.dismiss();
            } else {
                updateSubItem(currentSubItem, comment);
                this.dismiss();
            }
        });

        return view;
    }


    private void updateItem(CheckListItem checkListItem, String comment) {
        getRealm().beginTransaction();
        checkListItem.setComment(comment);
        Log.d("Here", "item comment -> " + comment);
        getRealm().commitTransaction();
        adapter.notifyItemChanged(position);
    }

    private void updateSubItem(SubCheckListItem subListItem, String comment) {
        getRealm().beginTransaction();
        subListItem.setComment(comment);
        Log.d("Here", "sublist comment -> " + comment);
        getRealm().commitTransaction();
        adapter.notifyDataSetChanged();
    }

    private Realm getRealm() {
        return RealmSingleton.getInstance(getContext());
    }

    @Override
    public int getTheme() {
        return UiHelper.getBottomSheetTheme(getContext());
    }

    @Override
    public void onPause() {
        super.onPause();
        Helper.updateKeyboardStatus(getActivity());
        Helper.toggleKeyboard(getContext(), commentEditText, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        commentEditText.requestFocus();
        if (AppData.getInstance().isKeyboardOpen()) {
            Helper.toggleKeyboard(getContext(), commentEditText, true);
            AppData.getInstance().setKeyboardOpen(false);
        }
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dialog = (BottomSheetDialog) getDialog();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(true);
        UiHelper.setBottomSheetBehavior(view, dialog);
    }

}