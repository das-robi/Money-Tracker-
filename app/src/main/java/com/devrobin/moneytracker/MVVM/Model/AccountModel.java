package com.devrobin.moneytracker.MVVM.Model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "account_table")
public class AccountModel {

    @PrimaryKey(autoGenerate = true)
    private int accountId;

    @ColumnInfo(name = "accountName")
    private String accountName;

    @ColumnInfo(name = "cardType")
    private String cardType; // Cash, Credit Card, Debit Card, Bank Account, etc.

    @ColumnInfo(name = "currency")
    private String currency; // BDT, USD, EUR, etc.

    @ColumnInfo(name = "balance")
    private double balance;

    @ColumnInfo(name = "note")
    private String note;

    @ColumnInfo(name = "iconId")
    private int iconId; // Resource ID for the selected icon

    @ColumnInfo(name = "lastModifiedTime")
    private long lastModifiedTime; // millis since epoch for conflict resolution

    public AccountModel() {
    }

    public AccountModel(String accountName, String cardType, String currency, double balance) {
        this.accountName = accountName;
        this.cardType = cardType;
        this.currency = currency;
        this.balance = balance;
        this.note = "";
        this.iconId = 0; // Default icon
        this.lastModifiedTime = System.currentTimeMillis();
    }

    public AccountModel(String accountName, String cardType, String currency, double balance, String note, int iconId) {
        this.accountName = accountName;
        this.cardType = cardType;
        this.currency = currency;
        this.balance = balance;
        this.note = note;
        this.iconId = iconId;
        this.lastModifiedTime = System.currentTimeMillis();
    }



    // Legacy constructor for backward compatibility
    public AccountModel(String accountName, double balance) {
        this.accountName = accountName;
        this.cardType = "Cash";
        this.currency = "BDT";
        this.balance = balance;
        this.note = "";
        this.iconId = 0;
        this.lastModifiedTime = System.currentTimeMillis();
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public String getFormattedBalance() {
        return String.format("%.0f", balance);
    }

    public String getCurrencySymbol() {
        return utils.CurrencyConverter.getCurrencySymbol(currency);
    }
}
