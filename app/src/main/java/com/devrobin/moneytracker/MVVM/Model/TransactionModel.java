package com.devrobin.moneytracker.MVVM.Model;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "transaction_table")
public class TransactionModel {

    @PrimaryKey(autoGenerate = true)
    private int transId;

    @ColumnInfo(name = "type")
    private String type;

    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "amount")
    private double amount;

    @ColumnInfo(name = "note")
    private String note;

    @ColumnInfo(name = "transactionDate")
    private Date transactionDate;

    @ColumnInfo(name = "createDate")
    private Date createDate;

    @ColumnInfo(name = "accountId")
    private int accountId;

    @ColumnInfo(name = "lastModifiedTime")
    private long lastModifiedTime; // millis since epoch for conflict resolution


    public TransactionModel() {
    }

    //Constructor for use saving trans;
    public TransactionModel(String type, String category, double amount, String note, Date transactionDate, int accountId) {

        this.type = type;
        this.category = category;
        this.amount = amount;
        this.note = note;
        this.transactionDate = transactionDate;
        this.createDate = new Date();
        this.transId = 0;
        this.accountId = accountId;
        this.lastModifiedTime = System.currentTimeMillis();
    }

    //Full Constructor
    public TransactionModel(int transId, String type, String category, double amount, String note, Date transactionDate, Date createDate, int accountId) {
        this.transId = transId;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.note = note;
        this.transactionDate = transactionDate;
        this.createDate = createDate;
        this.accountId = accountId;
        this.lastModifiedTime = System.currentTimeMillis();
    }

    // Legacy constructor for backward compatibility
    public TransactionModel(String type, String category, double amount, String note, Date transactionDate) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.note = note;
        this.transactionDate = transactionDate;
        this.createDate = new Date();
        this.transId = 0;
        this.accountId = 1; // Default to first account
        this.lastModifiedTime = System.currentTimeMillis();
    }

    // Legacy full constructor for backward compatibility
    public TransactionModel(int transId, String type, String category, double amount, String note, Date transactionDate, Date createDate) {
        this.transId = transId;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.note = note;
        this.transactionDate = transactionDate;
        this.createDate = createDate;
        this.accountId = 1; // Default to first account
        this.lastModifiedTime = System.currentTimeMillis();
    }


    public int getTransId() {
        return transId;
    }

    public void setTransId(int transId) {
        this.transId = transId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }
}
