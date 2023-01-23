package com.akapps.dailynote.classes.other;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.activity.NoteLockScreen;
import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.data.Expense;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.Photo;
import com.akapps.dailynote.classes.data.SubCheckListItem;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.recyclerview.expenses_recyclerview;
import com.akapps.dailynote.recyclerview.photos_recyclerview;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import app.futured.donut.DonutProgressView;
import app.futured.donut.DonutSection;
import io.realm.RealmList;
import io.realm.RealmResults;
import www.sanju.motiontoast.MotionToast;

public class BudgetSheet extends RoundedBottomSheetDialogFragment{

    private Note currentNote;
    private ArrayList<Expense> expensesList = new ArrayList<>();
    List<DonutSection> expensesListGraph = new ArrayList<>();

    // layout
    private BottomSheetDialog dialog;
    DonutProgressView budgetProgress;
    TextView errorMessage;
    TextView budgetText;
    RecyclerView expensesRecyclerview;

    public BudgetSheet(){}

    public BudgetSheet(Note currentNote){
        this.currentNote = currentNote;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_budget, container, false);

        if (AppData.getAppData().isLightMode)
            view.setBackgroundColor(getContext().getColor(R.color.light_mode));
        else
            view.setBackgroundColor(getContext().getColor(R.color.gray));

        double budget = 0;
        String errorMessageString = "";

        ImageButton budgetInfo = view.findViewById(R.id.budget_info);
        budgetProgress = view.findViewById(R.id.budget_progress_view);
        errorMessage = view.findViewById(R.id.error_message);
        budgetText = view.findViewById(R.id.budget);
        expensesRecyclerview = view.findViewById(R.id.expenses_recyclerview);
        expensesRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));

        budget = getBudgetAmount(currentNote.getChecklist());
        if(budget == 0){
            errorMessageString = "\nNo budget found\n";
            updateErrorMessage(errorMessageString);
        }
        else if(budget > 0){
            errorMessage.setVisibility(View.GONE);
            budgetProgress.setCap((float) budget);
            expensesList.sort(Comparator.comparing(Expense::getExpenseAmount, Comparator.reverseOrder()));
            expensesListGraph = getListOfExpenses(currentNote.getChecklist(), errorMessage, budget);
            if(expensesListGraph != null) {
                budgetProgress.submitData(expensesListGraph);
                RecyclerView.Adapter expensesAdapter = new expenses_recyclerview(expensesList);
                expensesRecyclerview.setAdapter(expensesAdapter);
                expensesRecyclerview.setNestedScrollingEnabled(false);
                DecimalFormat df = new DecimalFormat("#,##0.00");
                budgetText.setText("$" + df.format(budget) + "\nTotal");
            }
        }
        else
            updateErrorMessage(errorMessage.getText().toString());

        budgetInfo.setOnClickListener(view1 -> {
            InfoSheet info = new InfoSheet(10);
            info.show(getParentFragmentManager(), info.getTag());
        });

        return view;
    }

    private void updateErrorMessage(String errorMessageString){
        errorMessage.setVisibility(View.VISIBLE);
        errorMessage.setText(errorMessageString);
        budgetProgress.setVisibility(View.GONE);
        expensesRecyclerview.setVisibility(View.GONE);
        budgetText.setVisibility(View.GONE);
    }

    private List<DonutSection> getListOfExpenses(RealmList<CheckListItem> noteChecklist,
                                                 TextView errorMessage, double budget){
        List<DonutSection> expensesList = new ArrayList<>();
        ArrayList <String> wrongFormatList= new ArrayList();
        String wrongFormatMessage = "";
        double totalExpenses = 0, leftOver = 0;
        String leftOverText = "";
        int colorCounter = 0;
        int[] randomColor = getContext().getResources().getIntArray(R.array.budgetColors);

        for (CheckListItem currentItem: noteChecklist){
            double currentExpenseAmount = 0;

            String checklistString = currentItem.getText();

            if(checklistString.contains("$")) {
                String[] checklistStringTokens = getTokenArray(checklistString);

                for (String currentToken : checklistStringTokens) {
                    if (currentToken.contains("$") && !currentToken.contains("+$")) {
                        String currentTokenTrimmed = currentToken.substring(currentToken.indexOf("$") + 1)
                                .trim().replaceAll("[$]+", "\\$")
                                .replaceAll(",", "");
                        try {
                            Double currentTokenDouble = Double.parseDouble(currentTokenTrimmed);
                            currentExpenseAmount += currentTokenDouble;
                        } catch (Exception e) {
                            wrongFormatList.add("$" + currentTokenTrimmed);
                        }
                    }
                }
            }

            for (SubCheckListItem sublistItem : currentItem.getSubChecklist()) {
                String sublistString = sublistItem.getText();

                if (sublistString.contains("$")) {
                    String[] sublistStringTokens = getTokenArray(sublistString);

                    for (String currentToken : sublistStringTokens) {
                        if (currentToken.contains("$") && !currentToken.contains("+$")) {
                            String currentTokenTrimmed = currentToken.substring(currentToken.indexOf("$") + 1).trim()
                                    .replaceAll(",", "");
                            try {
                                Double currentTokenDouble = Double.parseDouble(currentTokenTrimmed);
                                currentExpenseAmount += currentTokenDouble;
                            } catch (Exception e) {
                                wrongFormatList.add("$" + currentTokenTrimmed);
                            }
                        }
                    }
                }
            }

            if(currentExpenseAmount > 0) {
                totalExpenses += currentExpenseAmount;
                expensesList.add(new DonutSection(currentItem.getText().trim(), randomColor[colorCounter], (float) currentExpenseAmount));
                this.expensesList.add(new Expense(randomColor[colorCounter++], currentItem.getText().trim(),
                        (currentExpenseAmount / budget)));

                if(colorCounter > getContext().getResources().getIntArray(R.array.budgetColors).length)
                    colorCounter = 0;
            }
        }

        if(wrongFormatList.size() > 0) {
            wrongFormatMessage = "\nFormat errors found: " + wrongFormatList + "\n";
            errorMessage.setText(wrongFormatMessage);
            return null;
        }
        else{
            if(budget - totalExpenses > 0) {
                leftOver = budget - totalExpenses;
                leftOverText = "Left to Spend";
            }
            else if(totalExpenses - budget > 0) {
                leftOver = totalExpenses - budget;
                leftOverText = "Over Budget";
            }

            expensesList.add(new DonutSection(leftOverText, randomColor[colorCounter], (float) leftOver));
            this.expensesList.add(new Expense(randomColor[colorCounter], leftOverText, leftOver/budget));
        }

        return expensesList;
    }

    private double getBudgetAmount(RealmList<CheckListItem> noteChecklist){
        double budget = 0;

        for (CheckListItem currentItem: noteChecklist){
            String checklistString = currentItem.getText();

            if(checklistString.contains("$")) {
                String[] checklistStringTokens = getTokenArray(checklistString);

                for (String currentToken : checklistStringTokens) {
                    budget = findBudget(currentToken, budget);
                    if(budget == -1)
                        return budget;
                }
            }

            for (SubCheckListItem sublistItem : currentItem.getSubChecklist()) {
                String sublistString = sublistItem.getText();

                if (sublistString.contains("$")) {
                    String[] sublistStringTokens = getTokenArray(sublistString);

                    for (String currentToken : sublistStringTokens) {
                        budget = findBudget(currentToken, budget);
                        if(budget == -1)
                            return budget;
                    }
                }
            }
        }

        return budget;
    }

    private String[] getTokenArray(String word){
       return word.replaceAll("\n", " ")
                .replaceAll(",", "")
                .replaceAll("\\$+", "\\$")
                .split(" ");
    }

    private double findBudget(String currentToken, double budget){
        String wrongFormatMessage = "";

        if (currentToken.contains("+$")) {
            try {
                budget = Double.parseDouble(currentToken.replaceAll(",", "")
                        .replace("+$", ""));
            } catch (Exception e) {
                wrongFormatMessage = "\nFormat errors found: " + currentToken + "\n";
                errorMessage.setText(wrongFormatMessage);
                budget = -1;
            }
        }
        return budget;
    }

    @Override
    public int getTheme() {
        if(AppData.getAppData().isLightMode)
            return R.style.BaseBottomSheetDialogLight;
        else
            return R.style.BaseBottomSheetDialog;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.dismiss();
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.getViewTreeObserver()
                .addOnGlobalLayoutListener(() -> {
                    dialog =(BottomSheetDialog) getDialog ();
                    if (dialog != null) {
                        FrameLayout bottomSheet = dialog.findViewById (R.id.design_bottom_sheet);
                        if (bottomSheet != null) {
                            BottomSheetBehavior behavior = BottomSheetBehavior.from (bottomSheet);
                            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                    }
                });
    }

}