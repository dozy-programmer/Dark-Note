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

    private int noteId;
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

        backgroundIcon.setOnClickListener(v -> openDialog("background"));
        titleColorIcon.setOnClickListener(v -> openDialog("title"));
        textColorIcon.setOnClickListener(v -> openDialog("text"));
        lastEditIcon.setOnClickListener(v -> openDialog("lastEdit"));

        if(RealmHelper.getUser(getContext(), "color sheet").isUsePreviewColorAsBackground()){
            useAsBackgroundSwitch.setVisibility(View.VISIBLE);
            lastEditBackground.setVisibility(View.VISIBLE);
            iconsTransparencyBackground.setVisibility(View.VISIBLE);
            if(RealmHelper.getCurrentNote(getContext(), noteId).getLastEditFolderTextColor() != 0) {
                lastEditText.setTextColor(RealmHelper.getCurrentNote(getContext(), noteId).getLastEditFolderTextColor());
            }
            useAsBackgroundSwitch.setChecked(RealmHelper.getCurrentNote(getContext(), noteId).isUsePreviewAsNoteBackground());
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

        if (RealmHelper.getUser(getContext(), "color sheet").getScreenMode() == User.Mode.Dark) {
            backgroundText.setTextColor(RealmHelper.getCurrentNote(getContext(), noteId).getBackgroundColor());
            backgroundColor.setStrokeColor(RealmHelper.getCurrentNote(getContext(), noteId).getBackgroundColor());
            backgroundColor.setCardBackgroundColor(UiHelper.getColorFromTheme(getActivity(), R.attr.quaternaryBackgroundColor));
        } else {
            backgroundColor.setCardBackgroundColor(RealmHelper.getCurrentNote(getContext(), noteId).getBackgroundColor());
            backgroundColor.setStrokeColor(RealmHelper.getCurrentNote(getContext(), noteId).getBackgroundColor());
            checkColor();
        }
        titleColor.setTextColor(RealmHelper.getCurrentNote(getContext(), noteId).getTitleColor());
        textColor.setTextColor(RealmHelper.getTextColorBasedOnTheme(getContext(), noteId));

        return view;
    }

    private void openDialog(String colorChanging) {
        // if it's a new note, initial color is gray. Otherwise it is set to color of note
        int initialColor;
        switch (colorChanging) {
            case "background":
                initialColor = RealmHelper.getCurrentNote(getContext(), noteId).getBackgroundColor();
                break;
            case "title":
                initialColor = RealmHelper.getCurrentNote(getContext(), noteId).getTitleColor();
                break;
            case "lastEdit":
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
                .setTitle("Select Color")
                .initialColor(initialColor)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(10)
                .setPositiveButton("SELECT", (dialog, selectedColor, allColors) -> {
                    Realm realm = RealmSingleton.getInstance(getContext());
                    realm.beginTransaction();
                    if (colorChanging.equals("background")) {
                        RealmHelper.getCurrentNote(getContext(), noteId).setBackgroundColor(selectedColor);
                        if(RealmHelper.getUser(getContext(), "color sheet").isUsePreviewColorAsBackground()){
                            ((NoteEdit) getActivity()).updateOtherColors();
                            realm.commitTransaction();
                            updateColors();
                            return;
                        }
                    } else if (colorChanging.equals("title"))
                        RealmHelper.getCurrentNote(getContext(), noteId).setTitleColor(selectedColor);
                    else if(colorChanging.equals("lastEdit")){
                        RealmHelper.getCurrentNote(getContext(), noteId).setLastEditFolderTextColor(selectedColor);
                        ((NoteEdit) getActivity()).updateOtherColors();
                        realm.commitTransaction();
                        updateColors();
                        return;
                    }
                    else {
                        RealmHelper.setTextColorBasedOnTheme(getContext(), noteId, selectedColor);
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

    private void checkColor() {
        if (!Helper.isColorDark(RealmHelper.getCurrentNote(getContext(), noteId).getBackgroundColor())) {
            backgroundText.setTextColor(getContext().getResources().getColor(R.color.black));
            backgroundIcon.setColorFilter(getContext().getResources().getColor(R.color.black));
        } else {
            backgroundText.setTextColor(getContext().getResources().getColor(R.color.white));
            backgroundIcon.setColorFilter(getContext().getResources().getColor(R.color.white));
        }
    }

    private void updateColors() {
        if (RealmHelper.getUser(getContext(), "color sheet").getScreenMode() == User.Mode.Dark) {
            backgroundText.setTextColor(RealmHelper.getCurrentNote(getContext(), noteId).getBackgroundColor());
            backgroundColor.setStrokeColor(RealmHelper.getCurrentNote(getContext(), noteId).getBackgroundColor());
            backgroundColor.setCardBackgroundColor(UiHelper.getColorFromTheme(getActivity(), R.attr.quaternaryBackgroundColor));
        } else {
            backgroundColor.setStrokeColor(RealmHelper.getCurrentNote(getContext(), noteId).getBackgroundColor());
            backgroundColor.setCardBackgroundColor(RealmHelper.getCurrentNote(getContext(), noteId).getBackgroundColor());
            checkColor();
        }
        titleColor.setTextColor(RealmHelper.getCurrentNote(getContext(), noteId).getTitleColor());
        textColor.setTextColor(RealmHelper.getTextColorBasedOnTheme(getContext(), noteId));
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