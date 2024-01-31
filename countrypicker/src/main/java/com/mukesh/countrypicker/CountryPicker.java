package com.mukesh.countrypicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mukeshsolanki.R;
import com.google.android.material.card.MaterialCardView;
import com.mukesh.countrypicker.listeners.BottomSheetInteractionListener;
import com.mukesh.countrypicker.listeners.OnCountryPickerListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CountryPicker implements BottomSheetInteractionListener, LifecycleObserver {

    // region Variables
    public static final int SORT_BY_NONE = 0;
    public static final int SORT_BY_NAME = 1;
    public static final int SORT_BY_ISO = 2;
    private int theme;

    private int style;
    private Context context;
    private int sortBy = SORT_BY_NONE;
    private OnCountryPickerListener onCountryPickerListener;

    private List<Country> countries;
    private MaterialCardView swipeDownIcon;
    private TextView message;
    private EditText searchEditText;
    private RecyclerView countriesRecyclerView;
    private LinearLayout rootView;
    private int swipeDownIconColor;
    private int textColor;
    private int hintColor;
    private int backgroundColor;
    private int searchIconId;
    private Drawable searchIcon;
    private CountriesAdapter adapter;
    private List<Country> searchResults;
    private BottomSheetDialogView bottomSheetDialog;

    private CountryPicker() { }

    CountryPicker(Builder builder) {
        sortBy = builder.sortBy;
        if (builder.onCountryPickerListener != null)
            onCountryPickerListener = builder.onCountryPickerListener;
        style = builder.style;
        context = builder.context;
        theme = builder.theme;
        countries = CountryList.getCountries();
    }

    @SuppressLint("SetTextI18n")
    private void sortCountries(@NonNull List<Country> countries, boolean isSearching) {
        if (sortBy == SORT_BY_NAME) {
            countries.sort((country1, country2) ->
                    country1.getName().trim().compareToIgnoreCase(country2.getName().trim()));
        } else if (sortBy == SORT_BY_ISO) {
            countries.sort((country1, country2) ->
                    country1.getCode().trim().compareToIgnoreCase(country2.getCode().trim()));
        }
        if(isSearching && countries.size() == 0)
            message.setText("No countries found...");
        else if(countries.size() == 0 || !isSearching)
            message.setText("Select from the following countries...");
        else
            message.setText(countries.size() + " found, click on search icon to view all results");
    }

    public void showBottomSheet(AppCompatActivity activity) {
        if (countries == null || countries.isEmpty()) {
            throw new IllegalArgumentException(context.getString(R.string.error_no_countries_found));
        } else {
            activity.getLifecycle().addObserver(this);
            bottomSheetDialog = new BottomSheetDialogView(theme);
            bottomSheetDialog.setListener(this);
            bottomSheetDialog.show(activity.getSupportFragmentManager(), "bottomsheet");
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void dismissDialogs() {
        if (bottomSheetDialog != null) bottomSheetDialog.dismiss();
    }

    @Override
    public void setupRecyclerView(View sheetView) {
        searchResults = new ArrayList<>();
        searchResults.addAll(countries);
        sortCountries(searchResults, false);
        adapter = new CountriesAdapter(sheetView.getContext(), searchResults,
                country -> {
                    if (onCountryPickerListener != null) {
                        onCountryPickerListener.onSelectCountry(country);
                        if (bottomSheetDialog != null) bottomSheetDialog.dismiss();
                        bottomSheetDialog = null;
                        swipeDownIconColor = 0;
                        textColor = 0;
                        hintColor = 0;
                        backgroundColor = 0;
                        searchIconId = 0;
                        searchIcon = null;
                    }
                },
                textColor,
                swipeDownIconColor);
        LinearLayoutManager layoutManager = new LinearLayoutManager(sheetView.getContext());
        countriesRecyclerView.setLayoutManager(layoutManager);
        countriesRecyclerView.setAdapter(adapter);
        countriesRecyclerView.addItemDecoration(new DividerItemDecoration(sheetView.getContext(), layoutManager.getOrientation()));
    }

    @Override
    public void setSearchEditText() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable searchQuery) {
                search(searchQuery.toString());
            }
        });
        searchEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            InputMethodManager imm = (InputMethodManager) searchEditText.getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
            }
            return true;
        });
    }

    private void search(String searchQuery) {
        searchResults.clear();
        for (Country country : countries) {
            if (country.getName().toLowerCase(Locale.ENGLISH).contains(searchQuery.toLowerCase()))
                searchResults.add(country);
        }
        sortCountries(searchResults, searchQuery.length() > 0);
        adapter.notifyDataSetChanged();
    }

    @SuppressWarnings("ResourceType")
    @Override
    public void setCustomStyle(View sheetView) {
        if (style != 0) {
            int[] attrs = {
                    android.R.attr.textColor,
                    android.R.attr.textColorHint,
                    android.R.attr.background,
                    android.R.attr.drawable,
                    android.R.attr.actionMenuTextColor,
            };
            TypedArray ta = sheetView.getContext().obtainStyledAttributes(style, attrs);
            textColor = ta.getColor(0, Color.BLACK);
            hintColor = ta.getColor(1, Color.GRAY);
            backgroundColor = ta.getColor(2, Color.WHITE);
            searchIconId = ta.getResourceId(3, R.drawable.ic_search);
            swipeDownIconColor = ta.getColor(4, Color.BLACK);
            message.setTextColor(textColor);
            searchEditText.setTextColor(textColor);
            searchEditText.setHintTextColor(hintColor);
            searchIcon = ContextCompat.getDrawable(searchEditText.getContext(), searchIconId);
            if (searchIconId == R.drawable.ic_search) {
                searchIcon.setColorFilter(new PorterDuffColorFilter(hintColor, PorterDuff.Mode.SRC_ATOP));
            }
            searchEditText.setCompoundDrawablesWithIntrinsicBounds(searchIcon, null, null, null);
            swipeDownIcon.setCardBackgroundColor(swipeDownIconColor);
            rootView.setBackgroundColor(backgroundColor);
            ta.recycle();
        }
    }

    @Override
    public void initiateUi(View sheetView) {
        swipeDownIcon = sheetView.findViewById(R.id.materialCardView8);
        message = sheetView.findViewById(R.id.message);
        searchEditText = sheetView.findViewById(R.id.country_code_picker_search);
        countriesRecyclerView = sheetView.findViewById(R.id.countries_recycler_view);
        rootView = sheetView.findViewById(R.id.rootView);
    }

    public static class Builder {
        private Context context;
        private int sortBy = SORT_BY_NONE;
        private int theme = 2;
        private OnCountryPickerListener onCountryPickerListener;
        private int style;

        public Builder with(@NonNull Context context) {
            this.context = context;
            return this;
        }

        public Builder style(@StyleRes int style) {
            this.style = style;
            return this;
        }

        public Builder listener(@NonNull OnCountryPickerListener onCountryPickerListener) {
            this.onCountryPickerListener = onCountryPickerListener;
            return this;
        }

        public Builder theme(@StyleRes int theme) {
            this.theme = theme;
            return this;
        }

        public CountryPicker build() {
            return new CountryPicker(this);
        }

    }

}
