package com.akapps.dailynote.recyclerview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.data.SubExpense;
import com.akapps.dailynote.classes.helpers.Helper;
import com.google.android.material.card.MaterialCardView;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class sub_expenses_recyclerview extends RecyclerView.Adapter<sub_expenses_recyclerview.MyViewHolder>{

    // project data
    private ArrayList<SubExpense> expenses;
    private double totalBudget;
    private String expenseKey;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView expenseName;
        private final TextView expenseAmount;
        private final TextView expensePercentage;
        private final MaterialCardView background;
        private final View view;

        public MyViewHolder(View v) {
            super(v);
            expenseName = v.findViewById(R.id.expense_name);
            expenseAmount = v.findViewById(R.id.expense_amount);
            expensePercentage = v.findViewById(R.id.expense_percentage);
            background = v.findViewById(R.id.background);
            view = v;
        }
    }

    public sub_expenses_recyclerview(ArrayList<SubExpense> expenses, double totalBudget, String expenseKey) {
        this.expenses = expenses;
        this.totalBudget = totalBudget;
        this.expenseKey = expenseKey;
    }

    @Override
    public sub_expenses_recyclerview.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_sub_expense_layout, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        SubExpense currentExpense = expenses.get(position);

        String expenseName = Helper.capitalize(Helper.removeAllMoneyAmounts(currentExpense.getExpenseName(), expenseKey));

        holder.expenseName.setText(expenseName);
        holder.expenseAmount.setText(expenseKey + Helper.formatToTwoDecimalPlaces(currentExpense.getTotalExpenseAmount()));
        holder.expensePercentage.setText(formatPercentage(currentExpense.getTotalExpenseAmount() / totalBudget));

        if(currentExpense.getExpenseName().toLowerCase().contains("total"))
            holder.background.setStrokeColor(holder.view.getContext().getColor(R.color.pressed_blue));
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    private String formatPercentage(double number){
        DecimalFormat df = new DecimalFormat("#,##0.00%");
        return String.format("%7s", df.format(number));
    }
}
