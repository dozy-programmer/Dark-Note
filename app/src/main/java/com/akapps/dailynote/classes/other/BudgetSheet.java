package com.akapps.dailynote.classes.other;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
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
import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.data.Expense;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.SubCheckListItem;
import com.akapps.dailynote.classes.data.SubExpense;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.recyclerview.expenses_recyclerview;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import org.jetbrains.annotations.NotNull;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import app.futured.donut.DonutProgressView;
import app.futured.donut.DonutSection;
import io.realm.RealmList;

public class BudgetSheet extends RoundedBottomSheetDialogFragment{

    private Note currentNote;
    private ArrayList<Expense> expensesList = new ArrayList<>();
    private List<DonutSection> expensesListGraph = new ArrayList<>();

    // layout
    private BottomSheetDialog dialog;
    private DonutProgressView budgetProgress;
    private TextView errorMessage;
    private TextView budgetText;
    private RecyclerView expensesRecyclerview;

    private String budgetKey;
    private String expenseKey;

    public BudgetSheet(){}

    public BudgetSheet(Note currentNote, String budgetKey, String expenseKey){
        this.currentNote = currentNote;
        this.budgetKey = budgetKey;
        this.expenseKey = expenseKey;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_budget, container, false);

        if (AppData.getAppData().isDarkerMode)
            view.setBackgroundColor(getContext().getColor(R.color.darker_mode));
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
            List<DonutSection> list = getListOfExpenses(currentNote.getChecklist(), errorMessage, budget);

            if(list.size() > 1){
                Log.d("Here", "@@@ " + Arrays.toString(list.toArray()));
                updateErrorMessage("\n\n" + "Budget is missing, add using " +
                        budgetKey + "XXXX" + "\n\nExample: " + budgetKey + "1000");
            }
            else {
                errorMessageString = getContext().getString(R.string.try_out_budget_message);
                updateErrorMessage("\n" + errorMessageString);
            }
        }
        else if(budget > 0){
            errorMessage.setVisibility(View.GONE);
            budgetProgress.setCap((float) budget);
            expensesListGraph = getListOfExpenses(currentNote.getChecklist(), errorMessage, budget);
            if(expensesListGraph != null) {
                // sort by highest to lowest expense
                expensesListGraph.sort(Comparator.comparing(DonutSection::getAmount, Comparator.reverseOrder()));
                expensesList.sort(Comparator.comparing(Expense::getExpenseAmountPercentage, Comparator.reverseOrder()));
                budgetProgress.submitData(expensesListGraph);
                RecyclerView.Adapter expensesAdapter = new expenses_recyclerview(expensesList, budget, expenseKey);
                expensesRecyclerview.setAdapter(expensesAdapter);
                expensesRecyclerview.setNestedScrollingEnabled(false);
                budgetText.setText(Helper.getLocalCurrency(budget, false));
            }
            else
                updateErrorMessage(errorMessage.getText().toString());
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
        List<DonutSection> currentExpensesList = new ArrayList<>();
        ArrayList <String> wrongFormatList= new ArrayList();
        String wrongFormatMessage = "";
        double totalExpenses = 0, leftOver = 0;
        String leftOverText = "";

        int randomColorGenerated = 0;

        for (CheckListItem currentItem: noteChecklist){
            double currentExpenseAmount = 0.3141592;
            ArrayList<SubExpense> currentExpenseSubList = new ArrayList<>();

            String checklistString = currentItem.getText();
            String expenseKeyRepeatedPattern = "\\\\" + expenseKey + "+";

            if(checklistString.contains(expenseKey)) {
                String[] checklistStringTokens = getTokenArray(checklistString);
                double currentSubExpenseTotal = 0;

                for (String currentToken : checklistStringTokens) {
                    if (currentToken.contains(expenseKey) && !currentToken.contains(budgetKey)) {
                        String currentTokenTrimmed = currentToken.substring(currentToken.indexOf(expenseKey) + 1)
                                .replaceAll(expenseKeyRepeatedPattern, "")
                                .trim().replaceAll(",", "");

                        try {
                            Log.d("Here", "@@@@@@@@@@@" + currentTokenTrimmed);
                            Double currentTokenDouble = Double.parseDouble(currentTokenTrimmed);
                            currentExpenseAmount += currentTokenDouble;
                            currentSubExpenseTotal += currentTokenDouble;
                        } catch (Exception e) {
                            wrongFormatList.add(expenseKey + currentTokenTrimmed);
                        }
                    }
                }
                currentExpenseSubList.add(new SubExpense(checklistString, currentSubExpenseTotal));
            }

            for (SubCheckListItem sublistItem : currentItem.getSubChecklist()) {
                String sublistString = sublistItem.getText();
                double currentSubExpenseTotal = 0;

                if (sublistString.contains(expenseKey)) {
                    String[] sublistStringTokens = getTokenArray(sublistString);

                    for (String currentToken : sublistStringTokens) {
                        if (currentToken.contains(expenseKey) && !currentToken.contains(budgetKey)) {
                            String currentTokenTrimmed = currentToken.substring(currentToken.indexOf(expenseKey) + 1).trim()
                                    .replaceAll(expenseKeyRepeatedPattern, "")
                                    .replaceAll(",", "");

                            try {
                                Double currentTokenDouble = Double.parseDouble(currentTokenTrimmed);
                                currentExpenseAmount += currentTokenDouble;
                                currentSubExpenseTotal += currentTokenDouble;
                            } catch (Exception e) {
                                wrongFormatList.add(expenseKey + currentTokenTrimmed);
                            }
                        }
                    }
                }
                currentExpenseSubList.add(new SubExpense(sublistString, currentSubExpenseTotal));
            }

            if(currentExpenseAmount != 0.3141592) {
                currentExpenseAmount -= 0.3141592;
                randomColorGenerated = Helper.getRandomColor();
                totalExpenses += currentExpenseAmount;
                currentExpensesList.add(new DonutSection(currentItem.getText().trim(), randomColorGenerated, (float) currentExpenseAmount));
                currentExpenseSubList.add(new SubExpense("Total", currentExpenseAmount));
                expensesList.add(new Expense(randomColorGenerated, currentItem.getText().trim(),
                        currentExpenseAmount / budget, currentExpenseAmount, currentExpenseSubList));
            }
        }

        if(wrongFormatList.size() > 0) {
            wrongFormatMessage = "\nFormat errors found: " + wrongFormatList + "\n";
            errorMessage.setText(wrongFormatMessage);
            return null;
        }
        else{
            randomColorGenerated = Helper.getRandomColor();
            ArrayList<SubExpense> currentExpenseSubList = new ArrayList<>();
            if(budget - totalExpenses > 0) {
                leftOver = budget - totalExpenses;
                leftOverText = "Under Budget";
            }
            else if(totalExpenses - budget > 0) {
                leftOver = totalExpenses - budget;
                leftOverText = "Over Budget";
            }
            currentExpenseSubList.add(new SubExpense(leftOverText, leftOver));

            currentExpensesList.add(new DonutSection(leftOverText, randomColorGenerated, (float) leftOver));
            expensesList.add(new Expense(randomColorGenerated, leftOverText,
                    leftOver/budget, leftOver, currentExpenseSubList));
        }

        return currentExpensesList;
    }

    private double getBudgetAmount(RealmList<CheckListItem> noteChecklist){
        double budget = 0;
        double currentBudget = 0;

        for (CheckListItem currentItem: noteChecklist){
            String checklistString = currentItem.getText();

            if(checklistString.contains(budgetKey)) {
                String[] checklistStringTokens = getTokenArray(checklistString);

                for (String currentToken : checklistStringTokens) {
                    currentBudget = findBudget(currentToken);
                    if(currentBudget > 0)
                        budget += currentBudget;
                }
            }

            for (SubCheckListItem sublistItem : currentItem.getSubChecklist()) {
                String sublistString = sublistItem.getText();

                if (sublistString.contains(budgetKey)) {
                    String[] sublistStringTokens = getTokenArray(sublistString);

                    for (String currentToken : sublistStringTokens) {
                        currentBudget = findBudget(currentToken);
                        if(currentBudget > 0)
                            budget += currentBudget;
                    }
                }
            }
        }

        return budget;
    }

    private String[] getTokenArray(String word){
       return word.replaceAll("\n", " ")
                .replaceAll(",", "")
                .split(" ");
    }

    private double findBudget(String currentToken){
        double budget = 0;
        String wrongFormatMessage = "";

        if (currentToken.contains(budgetKey)) {
            try {
                budget = Double.parseDouble(currentToken.replaceAll(",", "")
                        .replace(budgetKey, ""));
            } catch (Exception e) {
                wrongFormatMessage = "\nFormat errors found: " + currentToken + "\n";
                errorMessage.setText(wrongFormatMessage);
                budget = 0;
            }
        }
        return budget;
    }

    @Override
    public int getTheme() {
        if(AppData.getAppData().isDarkerMode)
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
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(true);
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