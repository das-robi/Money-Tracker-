package com.devrobin.moneytracker.MVVM.Model;

import androidx.room.ColumnInfo;

public class MonthlyComparisonData {
    @ColumnInfo(name = "month")
    private String month;

    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "monthlyExpense")
    private double monthlyExpense;

    // Constructor
    public MonthlyComparisonData(String month, String category, double monthlyExpense) {
        this.month = month;
        this.category = category;
        this.monthlyExpense = monthlyExpense;
    }

    // Getters
    public String getMonth() { return month; }
    public String getCategory() { return category; }
    public double getMonthlyExpense() { return monthlyExpense; }

    // Helper method to get year from month string (YYYY-MM format)
    public int getYear() {
        if (month != null && month.length() >= 4) {
            try {
                return Integer.parseInt(month.substring(0, 4));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    // Helper method to get month number from month string (YYYY-MM format)
    public int getMonthNumber() {
        if (month != null && month.length() >= 7) {
            try {
                return Integer.parseInt(month.substring(5, 7));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}

