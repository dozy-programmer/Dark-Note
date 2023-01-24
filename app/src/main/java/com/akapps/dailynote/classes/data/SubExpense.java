package com.akapps.dailynote.classes.data;

public class SubExpense {

    private String expenseName;
    private double totalExpenseAmount;

    public SubExpense(){}

    public SubExpense(String expenseName, double totalExpenseAmount) {
        this.expenseName = expenseName;
        this.totalExpenseAmount = totalExpenseAmount;
    }

    public String getExpenseName() {
        return expenseName;
    }

    public void setExpenseName(String expenseName) {
        this.expenseName = expenseName;
    }

    public double getTotalExpenseAmount() {
        return totalExpenseAmount;
    }

    public void setTotalExpenseAmount(double totalExpenseAmount) {
        this.totalExpenseAmount = totalExpenseAmount;
    }
}
