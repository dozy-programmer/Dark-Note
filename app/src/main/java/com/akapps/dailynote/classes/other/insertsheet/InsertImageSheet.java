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
import java.util.Objects;

import jp.wasabeef.richeditor.RichEditor;
import www.sanju.motiontoast.MotionToast;

public class InsertImageSheet extends RoundedBottomSheetDialogFragment {

    private ArrayList<String> filePaths = new ArrayList<>();
    private int width = 150;
    private int maxWidth = 0;
    private int height = 150;

    private RichEditor note;
    private RichEditor imageEditor;
    private TextView title;
    private TextView numImages;
    private TextView message;
    private TextInputLayout widthLayout;
    private TextInputEditText widthInput;
    private TextInputLayout heightLayout;
    private TextInputEditText heightInput;
    private MaterialButton confirm;

    private boolean isEditing;
    private String srcImage;
    private String html;

    private ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.EXTRA_IMAGES);
                            if (!images.isEmpty()) {
                                Log.d("Here", "returning images " + images.size());
                                for (int i = 0; i < images.size(); i++) {
                                    File newFile = Helper.createFile(getActivity(), "image", ".png");
                                    filePaths.add(Helper.createFile(getContext(), images.get(i).getUri(), newFile).getPath());

                                    if (i == 0) setImage(false, false);
                                }
                                message.setVisibility(View.VISIBLE);
                                imageEditor.setVisibility(View.VISIBLE);
                                widthLayout.setVisibility(View.VISIBLE);
                                heightLayout.setVisibility(View.VISIBLE);
                                confirm.setVisibility(View.VISIBLE);
                                if (images.size() > 1) {
                                    numImages.setText("+" + (images.size() - 1));
                                    numImages.setVisibility(View.VISIBLE);
                                    title.setText("Photo(s) Selected");
                                } else
                                    title.setText("Photo Selected");
                            }
                        }
                    });

    public InsertImageSheet() {}

    public InsertImageSheet(String imageSrc, String html, int width, int height) {
        this.srcImage = imageSrc;
        this.html = html;
        this.width = width;
        this.height = height;
        isEditing = true;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_insert_image, container, false);

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
        numImages = view.findViewById(R.id.num_images);
        message = view.findViewById(R.id.message);
        imageEditor = view.findViewById(R.id.image);
        widthLayout = view.findViewById(R.id.width_layout);
        widthInput = view.findViewById(R.id.width);
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

        title.setText("Selecting Photo(s)");
        confirm.setText("Confirm");

        maxWidth = (int) Helper.getScreenWidth(getActivity()) - 30;
        Log.d("Here", "width " + width);
        Log.d("Here", "height " + height);
        setText(widthInput, width);
        setText(heightInput, height);
        addSizeListeners();

        if(isEditing){
            if(srcImage != null && !srcImage.isEmpty()){
                filePaths.add(srcImage);
            }
            message.setVisibility(View.VISIBLE);
            imageEditor.setVisibility(View.VISIBLE);
            setImage(srcImage);
            widthLayout.setVisibility(View.VISIBLE);
            heightLayout.setVisibility(View.VISIBLE);
            confirm.setVisibility(View.VISIBLE);
        }
        else
            showImageSelectionDialog();

        confirm.setOnClickListener(view1 -> {
            if (!note.hasFocus()) note.focusEditor();
            if (imagesValid()) {
                for (String filePath : filePaths) {
                    note.insertImage(filePath, "image", width, height);
                }
                dismiss();
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
                    setImage(true, false);
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
                    setImage(false, true);
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

    private boolean imagesValid() {
        return filePaths != null && filePaths.size() > 0;
    }

    private void setImage(boolean isEditingWidth, boolean isEditingHeight) {
        if (!imagesValid()) return;
        imageEditor.setEnabled(true);
        imageEditor.focusEditor();
        imageEditor.setHtml("");
        imageEditor.insertImage(filePaths.get(0), "image", width, height);
        imageEditor.clearFocusEditor();
        imageEditor.setEnabled(false);
        if (isEditingWidth) widthInput.requestFocus();
        else if (isEditingHeight) heightInput.requestFocus();
    }

    private void setImage(String path) {
        imageEditor.setEnabled(true);
        imageEditor.focusEditor();
        imageEditor.setHtml("");
        imageEditor.insertImage(path, "image", width, height);
        imageEditor.clearFocusEditor();
        imageEditor.setEnabled(false);
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

    public void showImageSelectionDialog() {
        ImagePickerConfig config = new ImagePickerConfig();
        config.setShowCamera(true);
        config.setLimitSize(20);
        config.setCustomColor(UiHelper.getImagePickerTheme(getActivity()));
        CustomMessage customMessage = new CustomMessage();
        customMessage.setReachLimitSize("You can only select up to 20 images.");
        customMessage.setNoImage("No image found.");
        customMessage.setNoPhotoAccessPermission("Please allow permission to access photos and media.");
        customMessage.setNoCamera("Please allow permission to access camera.");
        config.setCustomMessage(customMessage);
        config.setStatusBarContentMode(UiHelper.getLightThemePreference(getContext()) ? StatusBarContent.DARK : StatusBarContent.LIGHT);
        Intent intent = ImagePickerLauncher.Companion.createIntent(getContext(), config);
        pickImageLauncher.launch(intent);
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