package com.devrobin.moneytracker.MVVM.Model;

import androidx.room.ColumnInfo;

public class CategoryReportData {
    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "totalIncome")
    private double totalIncome;

    @ColumnInfo(name = "totalExpense")
    private double totalExpense;

    @ColumnInfo(name = "transactionCount")
    private int transactionCount;

    // Constructor
    public CategoryReportData(String category, double totalIncome, double totalExpense, int transactionCount) {
        this.category = category;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.transactionCount = transactionCount;
    }

    // Getters
    public String getCategory() { return category; }
    public double getTotalIncome() { return totalIncome; }
    public double getTotalExpense() { return totalExpense; }
    public int getTransactionCount() { return transactionCount; }

    // Calculate net amount (income - expense)
    public double getNetAmount() { return totalIncome - totalExpense; }

    // Calculate total activity (income + expense)
    public double getTotalActivity() { return totalIncome + totalExpense; }
}