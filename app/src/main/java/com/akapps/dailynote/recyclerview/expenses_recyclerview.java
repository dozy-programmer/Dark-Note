package com.akapps.dailynote.recyclerview;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.data.Expense;
import com.akapps.dailynote.classes.data.Photo;
import com.akapps.dailynote.classes.other.InfoSheet;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.stfalcon.imageviewer.StfalconImageViewer;

import java.io.File;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;

import io.realm.RealmResults;

public class expenses_recyclerview extends RecyclerView.Adapter<expenses_recyclerview.MyViewHolder>{

    // project data
    private ArrayList<Expense> expenses;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView expenseColor;
        private final TextView expenseName;
        private final TextView expensePercentage;
        private final View view;

        public MyViewHolder(View v) {
            super(v);
            expenseColor = v.findViewById(R.id.color);
            expenseName = v.findViewById(R.id.expense_name);
            expensePercentage = v.findViewById(R.id.expense_percentage);
            view = v;
        }
    }

    public expenses_recyclerview(ArrayList<Expense> expenses) {
        this.expenses = expenses;
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
        holder.expenseName.setText(currentExpense.getExpenseName());
        holder.expensePercentage.setText(formatDouble(currentExpense.getExpenseAmount()));
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
