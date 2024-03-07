package com.akapps.dailynote.classes.other.insertsheet;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.TextView;

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
import org.jetbrains.annotations.NotNull;

import java.io.File;

import jp.wasabeef.richeditor.RichEditor;
import www.sanju.motiontoast.MotionToast;

public class InsertLinkSheet extends RoundedBottomSheetDialogFragment {

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
    private String path;
    private String html;
    private String srcImage;
    private int maxWidth = 0;
    private int width = 150;
    private int height = 150;


    public InsertLinkSheet() {
    }

    public InsertLinkSheet(String imageSrc, String html, int width, int height) {
        this.srcImage = imageSrc;
        this.html = html;
        this.width = width;
        this.height = height;
        isEditing = true;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_insert_link, container, false);

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

        maxWidth = (int) Helper.getScreenWidth(getActivity()) - 30;
        Log.d("Here", "width " + width + ", height " + height);
        setText(widthInput, width);
        setText(heightInput, height);

        linkLayout.setVisibility(View.VISIBLE);
        confirm.setVisibility(View.VISIBLE);

        confirm.setOnClickListener(view1 -> {
            Log.d("Here", "Button pressed");
            if (confirm.getText().toString().equals("Add Link")) {
                String inputText = linkInput.getText().toString();
                if (URLUtil.isValidUrl(inputText)) {
                    confirm.setText("Confirm");
                    title.setText(isEditing ? "Edit Image" : "Image Added");
                    linkLayout.setVisibility(View.VISIBLE);
                    imageEditor.setVisibility(View.VISIBLE);
                    setLinkImage(inputText, false, false);
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
                    //path = downloadImage("download_from_link_" + Helper.getRandomUUID(), inputLink);
                    path = "";
                    if(!path.isEmpty()) {
                        note.insertImage(path, "image", width, height);
                        Helper.showMessage(getActivity(), "Image Downloading...",
                                "After download complete, close and open note to see it",
                                MotionToast.TOAST_WARNING);
                    }
                    else {
                        note.insertImage(inputLink, "image", width, height);
                    }
                    dismiss();
                }
            }
        });

        if (isEditing) {
            confirm.setText("Add Link");
            linkInput.setText(srcImage);
            confirm.callOnClick();
        } else {
            title.setText("Insert Image Link");
            confirm.setText("Add Link");
            widthLayout.setVisibility(View.GONE);
            heightLayout.setVisibility(View.GONE);
        }

        return view;
    }

    private String downloadImage(String filename, String downloadUrlOfImage){
        try{
            DownloadManager dm = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
            Uri downloadUri = Uri.parse(downloadUrlOfImage);
            DownloadManager.Request request = new DownloadManager.Request(downloadUri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle(filename)
                    .setMimeType("image/png")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalFilesDir(getContext(), Environment.DIRECTORY_DOCUMENTS, File.separator + filename + ".png");
            dm.enqueue(request);
            String fileName = getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + File.separator + filename + ".png";
            Log.d("Here", "Success " + fileName);
            return fileName;
        } catch (Exception e){
            Log.d("Here", "Fail");
        }
        return "";
    }

    private void addSizeListeners() {
        TextWatcher widthTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (checkNumberAndSetError(s.toString(), widthLayout, true)) {
                    setLinkImage(linkInput.getText().toString(), true, false);
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
                    setLinkImage(linkInput.getText().toString(), false, true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
        heightInput.addTextChangedListener(heightTextWatcher);
    }

    private void setLinkImage(String path, boolean isEditingWidth, boolean isEditingHeight) {
        imageEditor.setEnabled(true);
        imageEditor.focusEditor();
        imageEditor.setHtml("");
        imageEditor.insertImage(path, "image", width, height);
        imageEditor.clearFocusEditor();
        imageEditor.setEnabled(false);
        if (isEditingWidth) widthInput.requestFocus();
        else if (isEditingHeight) heightInput.requestFocus();
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

    private void setText(TextInputEditText field, int value) {
        field.setText(String.valueOf(value));
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