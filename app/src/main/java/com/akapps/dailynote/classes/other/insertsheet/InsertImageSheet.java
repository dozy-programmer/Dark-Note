package com.akapps.dailynote.classes.other.insertsheet;

import static com.akapps.dailynote.classes.helpers.RealmHelper.getRealm;

import android.Manifest;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.classes.data.Photo;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.ImagePickerManager;
import com.akapps.dailynote.classes.helpers.MediaHelper;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.akapps.dailynote.classes.other.GenericInfoSheet;
import com.akapps.dailynote.classes.other.MediaSelectionSheet;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.richeditor.RichEditor;

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

    private String tempCameraPhotoPath;

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicture(),
                    result -> {
                        if (tempCameraPhotoPath != null) {
                            Log.d("Here", "returning camera image");
                            filePaths.add(tempCameraPhotoPath);
                            setImage(false, false);
                            message.setVisibility(View.VISIBLE);
                            imageEditor.setVisibility(View.VISIBLE);
                            widthLayout.setVisibility(View.VISIBLE);
                            heightLayout.setVisibility(View.VISIBLE);
                            confirm.setVisibility(View.VISIBLE);
                            title.setText("Photo Selected");
                            tempCameraPhotoPath = null;
                        }
                    });

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(10), uris -> {
                if (!uris.isEmpty()) {
                    Log.d("Here", "returning images " + uris.size());
                    for (int i = 0; i < uris.size(); i++) {
                        File newFile = Helper.createFile(getActivity(), "image", ".png");
                        filePaths.add(Helper.createFile(getContext(), uris.get(i), newFile).getPath());

                        if (i == 0) setImage(false, false);
                    }
                    message.setVisibility(View.VISIBLE);
                    imageEditor.setVisibility(View.VISIBLE);
                    widthLayout.setVisibility(View.VISIBLE);
                    heightLayout.setVisibility(View.VISIBLE);
                    confirm.setVisibility(View.VISIBLE);
                    if (uris.size() > 1) {
                        numImages.setText("+" + (uris.size() - 1));
                        numImages.setVisibility(View.VISIBLE);
                        title.setText("Photo(s) Selected");
                    } else
                        title.setText("Photo Selected");
                } else {
                    Log.d("Here", "No media selected");
                }
            });

    private final ActivityResultLauncher<String[]> mediaPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            isGranted -> {
                if (isGranted.containsValue(false)) {
                    GenericInfoSheet infoSheet = new GenericInfoSheet("Media Permission", "This " +
                            "permission is required so that you can select images from your phone.\n\n" +
                            "To enable, go to Dark Note Settings -> App Settings -> Permissions\n\nor Click Proceed", "Proceed", 1);
                    infoSheet.show(getActivity().getSupportFragmentManager(), infoSheet.getTag());
                } else {
                    MediaHelper.openMedia(pickMultipleMedia);
                }
            });

    private final ActivityResultLauncher<String[]> cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            isGranted -> {
                if (isGranted.containsValue(false)) {
                    GenericInfoSheet infoSheet = new GenericInfoSheet("Camera Permission", "This " +
                            "permission is required so that you can use the camera.\n\n" +
                            "To enable, go to Dark Note Settings -> App Settings -> Permissions\n\nor Click Proceed", "Proceed", 1);
                    infoSheet.show(getActivity().getSupportFragmentManager(), infoSheet.getTag());
                } else {
                    tempCameraPhotoPath = MediaHelper.openCamera(getActivity(), getContext(), takePictureLauncher);
                }
            });

    private ImagePickerManager imagePickerManager;

    public InsertImageSheet() {
    }

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

        if (isEditing) {
            if (srcImage != null && !srcImage.isEmpty()) {
                filePaths.add(srcImage);
            }
            message.setVisibility(View.VISIBLE);
            imageEditor.setVisibility(View.VISIBLE);
            setImage(srcImage);
            widthLayout.setVisibility(View.VISIBLE);
            heightLayout.setVisibility(View.VISIBLE);
            confirm.setVisibility(View.VISIBLE);
        } else {
            showMediaSelection();
        }

        confirm.setOnClickListener(view1 -> {
            if (!note.hasFocus()) note.focusEditor();
            if (imagesValid()) {
                for (String filePath : filePaths) {
                    note.insertImage(filePath, "image", width, height);
                }
                dismiss();
            }
        });

        imagePickerManager = ImagePickerManager.register(
                this,
                requireContext(),
                new ImagePickerManager.ImagePickCallback() {

                    @Override
                    public void onImagesPicked(List<Uri> uris) {
                        if (!uris.isEmpty()) {
                            Log.d("Here", "returning images " + uris.size());
                            for (int i = 0; i < uris.size(); i++) {
                                File newFile = Helper.createFile(getActivity(), "image", ".png");
                                filePaths.add(Helper.createFile(getContext(), uris.get(i), newFile).getPath());

                                if (i == 0) setImage(false, false);
                            }
                            message.setVisibility(View.VISIBLE);
                            imageEditor.setVisibility(View.VISIBLE);
                            widthLayout.setVisibility(View.VISIBLE);
                            heightLayout.setVisibility(View.VISIBLE);
                            confirm.setVisibility(View.VISIBLE);
                            if (uris.size() > 1) {
                                numImages.setText("+" + (uris.size() - 1));
                                numImages.setVisibility(View.VISIBLE);
                                title.setText("Photo(s) Selected");
                            } else
                                title.setText("Photo Selected");
                        } else {
                            Log.d("Here", "No media selected");
                        }
                    }

                    @Override
                    public void onCanceled() {
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                }
        );

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
            } else if (number > maxWidth && isWidth) {
                layout.setError("Max is " + maxWidth);
                layout.setErrorEnabled(true);
                confirm.setEnabled(false);
            } else {
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

    public void openMediaSelect(int selection) {
        // open gallery
        if (selection == 0) {
            if (MediaHelper.hasMediaPermissions(getContext())) {
                MediaHelper.openMedia(pickMultipleMedia);
            } else {
                mediaPermissionLauncher.launch(MediaHelper.getMediaPermissions());
            }
        }
        // open camera
        else if (selection == 1) {
            if (MediaHelper.hasPermission(getContext(), Manifest.permission.CAMERA)) {
                tempCameraPhotoPath = MediaHelper.openCamera(getActivity(), getContext(), takePictureLauncher);
            } else {
                cameraPermissionLauncher.launch(MediaHelper.getCameraPermission());
            }
        } else if(selection == 3) {
            imagePickerManager.openImagePicker(true);
        }
    }

    private void showMediaSelection() {
        MediaSelectionSheet mediaSelectionSheet = new MediaSelectionSheet(this);
        mediaSelectionSheet.show(getActivity().getSupportFragmentManager(), mediaSelectionSheet.getTag());
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