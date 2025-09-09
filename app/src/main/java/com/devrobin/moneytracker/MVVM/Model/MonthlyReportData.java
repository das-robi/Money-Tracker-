package com.devrobin.moneytracker.MVVM.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class MonthlyReportData implements Parcelable {
    private String monthName;
    private int year;
    private int month; // 0-11 (Calendar.MONTH)
    private double totalIncome;
    private double totalExpense;
    private double netBalance;
    private String currencySymbol;
    private int transactionCount;

    public MonthlyReportData(String monthName, int year, int month, double totalIncome,
                             double totalExpense, double netBalance, String currencySymbol, int transactionCount) {
        this.monthName = monthName;
        this.year = year;
        this.month = month;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.netBalance = netBalance;
        this.currencySymbol = currencySymbol;
        this.transactionCount = transactionCount;
    }

    // Getters
    public String getMonthName() { return monthName; }
    public int getYear() { return year; }
    public int getMonth() { return month; }
    public double getTotalIncome() { return totalIncome; }
    public double getTotalExpense() { return totalExpense; }
    public double getNetBalance() { return netBalance; }
    public String getCurrencySymbol() { return currencySymbol; }
    public int getTransactionCount() { return transactionCount; }

    // Setters
    public void setMonthName(String monthName) { this.monthName = monthName; }
    public void setYear(int year) { this.year = year; }
    public void setMonth(int month) { this.month = month; }
    public void setTotalIncome(double totalIncome) { this.totalIncome = totalIncome; }
    public void setTotalExpense(double totalExpense) { this.totalExpense = totalExpense; }
    public void setNetBalance(double netBalance) { this.netBalance = netBalance; }
    public void setCurrencySymbol(String currencySymbol) { this.currencySymbol = currencySymbol; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }

    /**
     * Get month name from month number (0-11)
     */
    public static String getMonthName(int month) {
        String[] monthNames = {
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        };
        return monthNames[month];
    }

    /**
     * Get full month name from month number (0-11)
     */
    public static String getFullMonthName(int month) {
        String[] monthNames = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        return monthNames[month];
    }

    /**
     * Format amount with currency symbol
     */
    public String formatAmount(double amount) {
        return String.format("%s %.2f", currencySymbol, amount);
    }

    /**
     * Get formatted income string
     */
    public String getFormattedIncome() {
        return formatAmount(totalIncome);
    }

    /**
     * Get formatted expense string
     */
    public String getFormattedExpense() {
        return formatAmount(totalExpense);
    }

    /**
     * Get formatted balance string
     */
    public String getFormattedBalance() {
        return formatAmount(netBalance);
    }

    // ==================== PARCELABLE IMPLEMENTATION ====================

    protected MonthlyReportData(Parcel in) {
        monthName = in.readString();
        year = in.readInt();
        month = in.readInt();
        totalIncome = in.readDouble();
        totalExpense = in.readDouble();
        netBalance = in.readDouble();
        currencySymbol = in.readString();
        transactionCount = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(monthName);
        dest.writeInt(year);
        dest.writeInt(month);
        dest.writeDouble(totalIncome);
        dest.writeDouble(totalExpense);
        dest.writeDouble(netBalance);
        dest.writeString(currencySymbol);
        dest.writeInt(transactionCount);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MonthlyReportData> CREATOR = new Creator<MonthlyReportData>() {
        @Override
        public MonthlyReportData createFromParcel(Parcel in) {
            return new MonthlyReportData(in);
        }

        @Override
        public MonthlyReportData[] newArray(int size) {
            return new MonthlyReportData[size];
        }
    };
}
