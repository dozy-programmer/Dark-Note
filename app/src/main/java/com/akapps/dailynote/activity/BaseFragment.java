package com.akapps.dailynote.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

public abstract class BaseFragment extends Fragment {

    public BaseFragment(int contentLayoutId) {
        super(contentLayoutId);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        applyWindowInsets(view);
    }

    /**
     * Apply system window insets (status bar, navigation bar) to the given view.
     */
    protected void applyWindowInsets(View view) {
        if (view == null) return;

        final int initialPaddingLeft = view.getPaddingLeft();
        final int initialPaddingTop = view.getPaddingTop();
        final int initialPaddingRight = view.getPaddingRight();
        final int initialPaddingBottom = view.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

            v.setPadding(
                    initialPaddingLeft,
                    initialPaddingTop + statusBarHeight,
                    initialPaddingRight,
                    initialPaddingBottom + navigationBarHeight
            );

            // Mark insets as consumed so children won’t double-handle them
            return WindowInsetsCompat.CONSUMED;
        });

        // Request insets immediately
        ViewCompat.requestApplyInsets(view);
    }
}
