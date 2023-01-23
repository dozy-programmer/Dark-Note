package com.akapps.dailynote.classes.data;

public class Expense {

    private int color;
    private String expenseName;
    private double expenseAmount;

    public Expense(){}

    public Expense(int color, String expenseName, double expenseAmount) {
        this.color = color;
        this.expenseName = expenseName;
        this.expenseAmount = expenseAmount;
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

    public double getExpenseAmount() {
        return expenseAmount;
    }

    public void setExpenseAmount(double expenseAmount) {
        this.expenseAmount = expenseAmount;
    }
}
