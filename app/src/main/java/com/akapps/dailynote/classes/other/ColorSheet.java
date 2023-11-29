package com.akapps.dailynote.classes.other;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmHelper;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import io.realm.Realm;

public class ColorSheet extends RoundedBottomSheetDialogFragment {

    private Note currentNote;
    private AlertDialog colorPickerView;
    private TextView titleColor;
    private TextView textColor;
    private MaterialCardView backgroundColor;
    private TextView backgroundText;
    private ImageView backgroundIcon;

    public ColorSheet() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_color, container, false);

        if (RealmHelper.getUser(getContext(), "bottom sheet").getScreenMode() == User.Mode.Dark)
            view.setBackgroundColor(getContext().getColor(R.color.darker_mode));
        else if (RealmHelper.getUser(getContext(), "bottom sheet").getScreenMode() == User.Mode.Gray)
            view.setBackgroundColor(getContext().getColor(R.color.gray));
        else if (RealmHelper.getUser(getContext(), "bottom sheet").getScreenMode() == User.Mode.Light) {

        }

        titleColor = view.findViewById(R.id.title_color);
        textColor = view.findViewById(R.id.text_color);
        backgroundIcon = view.findViewById(R.id.background_color_icon);
        backgroundColor = view.findViewById(R.id.background);
        ImageView titleColorIcon = view.findViewById(R.id.title_color_icon);
        ImageView textColorIcon = view.findViewById(R.id.text_color_icon);
        backgroundText = view.findViewById(R.id.background_text);

        currentNote = ((NoteEdit) getActivity()).getCurrentNote();

        backgroundIcon.setOnClickListener(v -> {
            openDialog("b");
        });

        titleColorIcon.setOnClickListener(v -> openDialog("title"));

        textColorIcon.setOnClickListener(v -> openDialog("text"));

        backgroundColor.setCardBackgroundColor(currentNote.getBackgroundColor());
        titleColor.setTextColor(currentNote.getTitleColor());
        textColor.setTextColor(currentNote.getTextColor());

        checkColor();

        return view;
    }

    private void openDialog(String colorChanging) {
        // if it's a new note, initial color is gray. Otherwise it is set to color of note
        int initialColor;
        if (colorChanging.equals("b"))
            initialColor = currentNote.getBackgroundColor();
        else if (colorChanging.equals("title"))
            initialColor = currentNote.getTitleColor();
        else
            initialColor = currentNote.getTextColor();

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
                    if (colorChanging.equals("b"))
                        currentNote.setBackgroundColor(selectedColor);
                    else if (colorChanging.equals("title"))
                        currentNote.setTitleColor(selectedColor);
                    else
                        currentNote.setTextColor(selectedColor);
                    currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
                    realm.commitTransaction();
                    ((NoteEdit) getActivity()).updateDateEdited();
                    ((NoteEdit) getActivity()).updateColors();
                    updateColors();
                })
                .setNegativeButton("CLOSE", (dialog, which) ->
                        dialog.dismiss())
                .build();
        colorPickerView.show();
    }

    private void checkColor() {
        if (!Helper.isColorDark(currentNote.getBackgroundColor())) {
            backgroundText.setTextColor(getContext().getColor(R.color.black));
            backgroundIcon.setColorFilter(getContext().getColor(R.color.black));
        } else {
            backgroundText.setTextColor(getContext().getColor(R.color.white));
            backgroundIcon.setColorFilter(getContext().getColor(R.color.white));
        }
    }

    private void updateColors() {
        backgroundColor.setCardBackgroundColor(currentNote.getBackgroundColor());
        titleColor.setTextColor(currentNote.getTitleColor());
        textColor.setTextColor(currentNote.getTextColor());
        checkColor();
    }

    @Override
    public int getTheme() {
        if (RealmHelper.getUser(getContext(), "bottom sheet").getScreenMode() == User.Mode.Dark)
            return R.style.BaseBottomSheetDialogLight;
        else if (RealmHelper.getUser(getContext(), "bottom sheet").getScreenMode() == User.Mode.Gray)
            return R.style.BaseBottomSheetDialog;
        else if (RealmHelper.getUser(getContext(), "bottom sheet").getScreenMode() == User.Mode.Light) {
        }
        return 0;
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.getViewTreeObserver()
                .addOnGlobalLayoutListener(() -> {
                    BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
                    if (dialog != null) {
                        FrameLayout bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
                        if (bottomSheet != null) {
                            BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
                            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                    }
                });
    }

}