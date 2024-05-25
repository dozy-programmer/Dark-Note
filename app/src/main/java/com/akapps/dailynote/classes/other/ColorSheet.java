package com.akapps.dailynote.classes.other;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;

import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmHelper;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.slider.Slider;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import io.realm.Realm;

public class ColorSheet extends RoundedBottomSheetDialogFragment {

    private final int noteId;
    private AlertDialog colorPickerView;
    private TextView titleColor;
    private TextView textColor;
    private MaterialCardView backgroundColor;
    private TextView backgroundText;
    private ImageView backgroundIcon;

    private ImageView lastEditIcon;
    private TextView lastEditText;
    private SwitchCompat useAsBackgroundSwitch;
    private MaterialCardView lastEditBackground;
    private MaterialCardView iconsTransparencyBackground;
    private Slider transparencySlider;

    public ColorSheet(int noteId) {
        this.noteId = noteId;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_color, container, false);

        titleColor = view.findViewById(R.id.title_color);
        textColor = view.findViewById(R.id.text_color);
        backgroundIcon = view.findViewById(R.id.background_color_icon);
        backgroundColor = view.findViewById(R.id.background);
        ImageView titleColorIcon = view.findViewById(R.id.title_color_icon);
        ImageView textColorIcon = view.findViewById(R.id.text_color_icon);
        backgroundText = view.findViewById(R.id.background_text);
        useAsBackgroundSwitch = view.findViewById(R.id.use_as_background_switch);
        lastEditBackground = view.findViewById(R.id.last_edit_folder_color_layout);
        iconsTransparencyBackground = view.findViewById(R.id.slider_layout);
        transparencySlider = view.findViewById(R.id.transparent_slider);
        lastEditText = view.findViewById(R.id.last_edit_folder_color);
        lastEditIcon = view.findViewById(R.id.last_edit_folder_color_icon);

        backgroundIcon.setOnClickListener(v -> openDialog("Background"));
        titleColorIcon.setOnClickListener(v -> openDialog("Title"));
        textColorIcon.setOnClickListener(v -> openDialog("Text"));
        lastEditIcon.setOnClickListener(v -> openDialog("Last Edit"));

        updateColors();

        if(RealmHelper.getUser(getContext(), "color sheet").isUsePreviewColorAsBackground()){
            useAsBackgroundSwitch.setVisibility(View.VISIBLE);
            lastEditBackground.setVisibility(View.VISIBLE);
            iconsTransparencyBackground.setVisibility(View.VISIBLE);
            if(RealmHelper.getCurrentNote(getContext(), noteId).getLastEditFolderTextColor() != 0) {
                lastEditText.setTextColor(RealmHelper.getCurrentNote(getContext(), noteId).getLastEditFolderTextColor());
            }
            useAsBackgroundSwitch.setChecked(RealmHelper.getCurrentNote(getContext(), noteId).isUsePreviewAsNoteBackground());
            transparencySlider.setValue((float) Math.round(RealmHelper.getCurrentNote(getContext(), noteId).getEditorIconTransparency() * 10) / 10);
            transparencySlider.addOnChangeListener((slider, value, fromUser) -> {
                RealmSingleton.get(getContext()).beginTransaction();
                RealmHelper.getCurrentNote(getContext(), noteId).setEditorIconTransparency(value);
                RealmSingleton.get(getContext()).commitTransaction();
                ((NoteEdit) getActivity()).updateOtherColors();
            });
            useAsBackgroundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                RealmSingleton.get(getContext()).beginTransaction();
                RealmHelper.getCurrentNote(getContext(), noteId).setUsePreviewAsNoteBackground(isChecked);
                RealmSingleton.get(getContext()).commitTransaction();
                ((NoteEdit) getActivity()).updateOtherColors();
            });
        }

        return view;
    }

    private void openDialog(String colorChanging) {
        // if it's a new note, initial color is gray. Otherwise it is set to color of note
        int initialColor;
        switch (colorChanging) {
            case "Background":
                initialColor = RealmHelper.getCurrentNote(getContext(), noteId).getBackgroundColor();
                break;
            case "Title":
                initialColor = RealmHelper.getCurrentNote(getContext(), noteId).getTitleColor();
                break;
            case "Last Edit":
                initialColor = RealmHelper.getCurrentNote(getContext(), noteId).getLastEditFolderTextColor();
                if(initialColor == 0){
                    initialColor = getContext().getResources().getColor(R.color.white);
                }
                break;
            default:
                initialColor = RealmHelper.getTextColorBasedOnTheme(getContext(), noteId);
                break;
        }

        // opens dialog to choose a color
        colorPickerView = ColorPickerDialogBuilder
                .with(getContext(), R.style.ColorPickerDialogTheme)
                .setTitle("Select Color for " + colorChanging)
                .initialColor(initialColor)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(10)
                .setPositiveButton("SELECT", (dialog, selectedColor, allColors) -> {
                    Realm realm = RealmSingleton.getInstance(getContext());
                    realm.beginTransaction();
                    switch (colorChanging) {
                        case "Background":
                            RealmHelper.getCurrentNote(getContext(), noteId).setBackgroundColor(selectedColor);
                            if (RealmHelper.getUser(getContext(), "color sheet").isUsePreviewColorAsBackground()) {
                                ((NoteEdit) getActivity()).updateOtherColors();
                                realm.commitTransaction();
                                updateColors();
                                return;
                            }
                            break;
                        case "Title":
                            RealmHelper.getCurrentNote(getContext(), noteId).setTitleColor(selectedColor);
                            break;
                        case "Last Edit":
                            RealmHelper.getCurrentNote(getContext(), noteId).setLastEditFolderTextColor(selectedColor);
                            ((NoteEdit) getActivity()).updateOtherColors();
                            realm.commitTransaction();
                            updateColors();
                            return;
                        default:
                            RealmHelper.getCurrentNote(getContext(), noteId).setTextColor(selectedColor);
                            break;
                    }
                    realm.commitTransaction();
                    ((NoteEdit) getActivity()).updateColors();
                    updateColors();
                })
                .setNegativeButton("CLOSE", (dialog, which) ->
                        dialog.dismiss())
                .build();
        colorPickerView.show();
    }

    private void updateColors() {
        backgroundColor.setStrokeColor(RealmHelper.getCurrentNote(getContext(), noteId).getBackgroundColor());
        backgroundColor.setCardBackgroundColor(UiHelper.getColorFromTheme(getActivity(), R.attr.quaternaryBackgroundColor));
        titleColor.setTextColor(RealmHelper.getCurrentNote(getContext(), noteId).getTitleColor());
        textColor.setTextColor(RealmHelper.getCurrentNote(getContext(), noteId).getTextColor());
        backgroundText.setTextColor(UiHelper.getColorFromTheme(getActivity(), R.attr.primaryTextColor));
        if(RealmHelper.getUser(getContext(), "color sheet").isUsePreviewColorAsBackground()){
            lastEditText.setTextColor(RealmHelper.getCurrentNote(getContext(), noteId).getLastEditFolderTextColor());
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