package com.akapps.dailynote.classes.data;

import java.util.ArrayList;

public class Expense {

    private int color;
    private String expenseName;
    private double expenseAmountPercentage;
    private double totalExpenseAmount;
    private ArrayList<SubExpense> subExpensesList;

    public Expense(){}

    public Expense(int color, String expenseName, double expenseAmountPercentage,
                   double totalExpenseAmount, ArrayList<SubExpense> subExpensesList) {
        this.color = color;
        this.expenseName = expenseName;
        this.expenseAmountPercentage = expenseAmountPercentage;
        this.totalExpenseAmount = totalExpenseAmount;
        this.subExpensesList = subExpensesList;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getExpenseName() {
        return expenseName;
    }

    public void setExpenseName(String expenseName) {
        this.expenseName = expenseName;
    }

    public double getExpenseAmountPercentage() {
        return expenseAmountPercentage;
    }

    public void setExpenseAmountPercentage(double expenseAmountPercentage) {
        this.expenseAmountPercentage = expenseAmountPercentage;
    }

    public double getTotalExpenseAmount() {
        return totalExpenseAmount;
    }

    public void setTotalExpenseAmount(double totalExpenseAmount) {
        this.totalExpenseAmount = totalExpenseAmount;
    }

    public ArrayList getSubExpensesList() {
        return subExpensesList;
    }

    public void setSubExpensesList(ArrayList subExpensesList) {
        this.subExpensesList = subExpensesList;
    }
}
