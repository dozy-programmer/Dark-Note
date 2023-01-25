package com.akapps.dailynote.recyclerview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.data.Expense;
import com.akapps.dailynote.classes.helpers.Helper;
import com.google.android.material.card.MaterialCardView;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class expenses_recyclerview extends RecyclerView.Adapter<expenses_recyclerview.MyViewHolder>{

    // project data
    private ArrayList<Expense> expenses;
    private double totalBudget;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView expenseColor;
        private final TextView expenseName;
        private final TextView expensePercentage;
        private RecyclerView subExpensesRecyclerview;
        private ImageButton dropDown;
        private final View view;

        public MyViewHolder(View v) {
            super(v);
            expenseColor = v.findViewById(R.id.color);
            expenseName = v.findViewById(R.id.expense_name);
            expensePercentage = v.findViewById(R.id.expense_percentage);
            subExpensesRecyclerview = v.findViewById(R.id.sub_expenses);
            dropDown = v.findViewById(R.id.drop_down);
            view = v;
        }
    }

    public expenses_recyclerview(ArrayList<Expense> expenses, double totalBudget) {
        this.expenses = expenses;
        this.totalBudget = totalBudget;
    }

    @Override
    public expenses_recyclerview.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_expense_layout, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Expense currentExpense = expenses.get(position);

        holder.expenseColor.setCardBackgroundColor(currentExpense.getColor());
        holder.expenseName.setText(Helper.removeAllMoneyAmounts(currentExpense.getExpenseName()));
        holder.expensePercentage.setText(formatDouble(currentExpense.getExpenseAmountPercentage()));

        holder.dropDown.setOnClickListener(view -> {
            if(holder.subExpensesRecyclerview.getVisibility() == View.VISIBLE){
                holder.subExpensesRecyclerview.animate()
                        .alpha(0.0f)
                        .setDuration(200)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                holder.subExpensesRecyclerview.setVisibility(View.GONE);
                            }
                        });
                holder.dropDown.setRotation(0);
            }
            else {
                holder.subExpensesRecyclerview.setLayoutManager(new LinearLayoutManager(holder.view.getContext()));
                RecyclerView.Adapter expensesAdapter = new sub_expenses_recyclerview(currentExpense.getSubExpensesList(), totalBudget);
                holder.subExpensesRecyclerview.setAdapter(expensesAdapter);
                holder.subExpensesRecyclerview.animate()
                        .alpha(1.0f)
                        .setDuration(500)
                        .translationY(0)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                                holder.subExpensesRecyclerview.setVisibility(View.VISIBLE);
                            }
                        });
                holder.dropDown.setRotation(180);
            }
        });
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    private String formatDouble(double number){
        DecimalFormat df = new DecimalFormat("#,##0.00%");
        return df.format(number);
    }
}
