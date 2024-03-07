package com.akapps.dailynote.classes.other.insertsheet;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nguyenhoanglam.imagepicker.helper.Constants;
import com.nguyenhoanglam.imagepicker.model.CustomMessage;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.model.ImagePickerConfig;
import com.nguyenhoanglam.imagepicker.model.StatusBarContent;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePickerLauncher;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

import jp.wasabeef.richeditor.RichEditor;
import www.sanju.motiontoast.MotionToast;

public class InsertYoutubeVideoSheet extends RoundedBottomSheetDialogFragment {

    private RichEditor note;
    private RichEditor imageEditor;
    private TextView title;
    private TextView message;
    private TextInputLayout linkLayout;
    private TextInputEditText linkInput;
    private TextInputLayout widthLayout;
    private TextInputEditText widthInput;
    private TextInputLayout heightLayout;
    private TextInputEditText heightInput;
    private MaterialButton confirm;

    private boolean isEditing;
    private String srcImage;
    private String html;
    private int width = 0;
    private int maxWidth = 0;
    private int height = 150;

    public InsertYoutubeVideoSheet() {
    }

    public InsertYoutubeVideoSheet(String imageSrc, String html, int width, int height) {
        this.srcImage = imageSrc;
        this.html = html;
        this.width = width;
        this.height = height;
        isEditing = true;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_insert_youtube, container, false);

        /**
         * TODO
         * create another sheet for editing, add all logic there for editing
         * for inserting, finish supporting local image, online images, and youtube video support
         * for inserting, edit UI for inserting youtube link
         *
         * FAQ:
         * Start and Finish
         */

        note = ((NoteEdit) getActivity()).note;

        title = view.findViewById(R.id.title);
        message = view.findViewById(R.id.message);
        imageEditor = view.findViewById(R.id.image);
        widthLayout = view.findViewById(R.id.width_layout);
        widthInput = view.findViewById(R.id.width);
        linkLayout = view.findViewById(R.id.image_link_layout);
        linkInput = view.findViewById(R.id.image_link);
        heightLayout = view.findViewById(R.id.height_layout);
        heightInput = view.findViewById(R.id.height);
        confirm = view.findViewById(R.id.confirm);

        imageEditor.getSettings().setAllowFileAccess(true);
        imageEditor.setBackgroundColor(UiHelper.getColorFromTheme(getActivity(), R.attr.primaryBackgroundColor));
        imageEditor.setVisibility(View.GONE);
        message.setVisibility(View.GONE);
        widthLayout.setVisibility(View.GONE);
        heightLayout.setVisibility(View.GONE);
        confirm.setVisibility(View.GONE);

        title.setText("Inserting Youtube Video...");
        linkLayout.setVisibility(View.VISIBLE);
        confirm.setVisibility(View.VISIBLE);
        confirm.setText("Add Video");

        maxWidth = (int) Helper.getScreenWidth(getActivity()) - 15;
        width = maxWidth;
        setText(widthInput, width);
        setText(heightInput, height);
        Log.d("Here", "width " + width);
        Log.d("Here", "height " + height);

        confirm.setOnClickListener(view1 -> {
            if (confirm.getText().toString().equals("Add Video")) {
                String inputText = linkInput.getText().toString();
                if (URLUtil.isValidUrl(inputText)) {
                    confirm.setText("Confirm");
                    title.setText("Youtube Video Retrieved");
                    linkLayout.setVisibility(View.GONE);
                    imageEditor.setVisibility(View.VISIBLE);
                    setYoutubeVideo(inputText, false, true);
                    widthLayout.setVisibility(View.VISIBLE);
                    heightLayout.setVisibility(View.VISIBLE);
                    message.setVisibility(View.VISIBLE);
                    addSizeListeners();
                } else {
                    Helper.showMessage(getActivity(), "Link Error", "Link is not valid, try again!", MotionToast.TOAST_ERROR);
                }
            } else {
                if (!note.hasFocus()) note.focusEditor();
                String inputLink = linkInput.getText().toString();
                if (!inputLink.isEmpty() && URLUtil.isValidUrl(inputLink)) {
                    note.insertYoutubeVideo(inputLink, width, height);
                    dismiss();
                }
            }
        });

        return view;
    }

    private void addSizeListeners() {
        TextWatcher widthTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (checkNumberAndSetError(s.toString(), widthLayout, true)) {
                    setYoutubeVideo(linkInput.getText().toString(), true, false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
        widthInput.addTextChangedListener(widthTextWatcher);

        TextWatcher heightTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (checkNumberAndSetError(s.toString(), heightLayout, false)) {
                    setYoutubeVideo(linkInput.getText().toString(), false, true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
        heightInput.addTextChangedListener(heightTextWatcher);
    }

    private boolean checkNumberAndSetError(String numberString, TextInputLayout layout, boolean isWidth) {
        try {
            if (!isNumberDigitsOnly(numberString)) throw new NumberFormatException();
            int number = Integer.parseInt(numberString);
            if (number < 50) {
                layout.setError("Number must be at least 50");
                layout.setErrorEnabled(true);
                confirm.setEnabled(false);
            }
            else if(number > maxWidth && isWidth){
                layout.setError("Max is " + maxWidth);
                layout.setErrorEnabled(true);
                confirm.setEnabled(false);
            }
            else {
                layout.setError(null);
                layout.setErrorEnabled(false);
                confirm.setEnabled(true);
                if (isWidth)
                    width = number;
                else
                    height = number;
            }
        } catch (NumberFormatException e) {
            layout.setError("Please enter a valid number");
        }
        return true;
    }


    private void setYoutubeVideo(String path, boolean isEditingWidth, boolean isEditingHeight) {
        imageEditor.setEnabled(true);
        imageEditor.focusEditor();
        imageEditor.setHtml("");
        imageEditor.insertYoutubeVideo(path, width, height);
        imageEditor.clearFocusEditor();
        imageEditor.setEnabled(false);
        if (isEditingWidth) widthInput.requestFocus();
        else if (isEditingHeight) heightInput.requestFocus();
    }

    private void setText(TextInputEditText field, int value) {
        field.setText(String.valueOf(value));
    }

    private boolean isNumberDigitsOnly(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            widthInput.addTextChangedListener(null);
            heightInput.addTextChangedListener(null);
        } catch (Exception ignored) {
        }
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