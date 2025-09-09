package com.devrobin.moneytracker.MVVM.Model;

import androidx.room.ColumnInfo;

public class AccountReportData {
    @ColumnInfo(name = "accountId")
    private int accountId;

    @ColumnInfo(name = "accountName")
    private String accountName;

    @ColumnInfo(name = "currency")
    private String currency;

    @ColumnInfo(name = "totalIncome")
    private double totalIncome;

    @ColumnInfo(name = "totalExpense")
    private double totalExpense;

    @ColumnInfo(name = "currentBalance")
    private double currentBalance;

    // Constructor
    public AccountReportData(int accountId, String accountName, String currency,
                             double totalIncome, double totalExpense, double currentBalance) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.currency = currency;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.currentBalance = currentBalance;
    }

    // Getters
    public int getAccountId() { return accountId; }
    public String getAccountName() { return accountName; }
    public String getCurrency() { return currency; }
    public double getTotalIncome() { return totalIncome; }
    public double getTotalExpense() { return totalExpense; }
    public double getCurrentBalance() { return currentBalance; }

    // Calculate net balance (income - expense)
    public double getNetBalance() { return totalIncome - totalExpense; }

    // Calculate total activity (income + expense)
    public double getTotalActivity() { return totalIncome + totalExpense; }
}
