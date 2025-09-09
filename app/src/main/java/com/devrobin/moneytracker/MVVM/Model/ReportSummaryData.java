package com.devrobin.moneytracker.MVVM.Model;

import androidx.room.ColumnInfo;

public class ReportSummaryData {
    @ColumnInfo(name = "totalIncome")
    private double totalIncome;

    @ColumnInfo(name = "totalExpense")
    private double totalExpense;

    @ColumnInfo(name = "totalTransactions")
    private int totalTransactions;

    // Constructor
    public ReportSummaryData(double totalIncome, double totalExpense, int totalTransactions) {
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.totalTransactions = totalTransactions;
    }

    // Getters
    public double getTotalIncome() { return totalIncome; }
    public double getTotalExpense() { return totalExpense; }
    public int getTotalTransactions() { return totalTransactions; }

    // Calculate net balance (income - expense)
    public double getNetBalance() { return totalIncome - totalExpense; }

    // Calculate total activity (income + expense)
    public double getTotalActivity() { return totalIncome + totalExpense; }
}
