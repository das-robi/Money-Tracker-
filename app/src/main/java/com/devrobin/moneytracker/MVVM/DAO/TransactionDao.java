package com.devrobin.moneytracker.MVVM.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.devrobin.moneytracker.MVVM.Model.AccountReportData;
import com.devrobin.moneytracker.MVVM.Model.CategoryChartData;
import com.devrobin.moneytracker.MVVM.Model.CategoryReportData;
import com.devrobin.moneytracker.MVVM.Model.MonthlyComparisonData;
import com.devrobin.moneytracker.MVVM.Model.ReportSummaryData;
import com.devrobin.moneytracker.MVVM.Model.TransactionModel;


import java.util.List;

import utils.DailyChartData;
import utils.DailySummer;
import utils.MonthlyChartData;
import utils.MonthlySummary;

@Dao
public interface TransactionDao {

    @Insert
    void insertTransaction(TransactionModel transModel);

    @Update
    void updateTransaction(TransactionModel transModel);

    @Delete
    void deleteTransaction(TransactionModel transModel);

    //Transaction for specific date (use localtime for correct local day boundaries)
    @Query("SELECT * FROM transaction_table WHERE DATE(transactionDate/1000, 'unixepoch','localtime') = DATE(:date/1000, 'unixepoch','localtime') ORDER BY transactionDate DESC, createDate DESC")
    LiveData<List<TransactionModel>> getTransactionByDate(long date);

    //Transaction for specific date (synchronous, localtime)
    @Query("SELECT * FROM transaction_table WHERE DATE(transactionDate/1000, 'unixepoch','localtime') = DATE(:date/1000, 'unixepoch','localtime') ORDER BY transactionDate DESC, createDate DESC")
    List<TransactionModel> getTransactionByDateSync(long date);

    //Transaction for specific month (synchronous, localtime)
    @Query("SELECT * FROM transaction_table WHERE strftime('%Y-%m', transactionDate/1000, 'unixepoch','localtime') = strftime('%Y-%m', :date/1000, 'unixepoch','localtime') ORDER BY transactionDate DESC, createDate DESC")
    List<TransactionModel> getTransactionByMonthSync(long date);

    //Daily Summery for Specific Date
    @Query("SELECT " +
            "COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) as totalIncome, " +
            "COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) as totalExpense," +
            "COUNT(*) transactionCount " +
            "FROM transaction_table " +
            "WHERE DATE(transactionDate/1000, 'unixepoch','localtime') = DATE(:date/1000, 'unixepoch','localtime')")
    LiveData<DailySummer> getDailySummery(long date);

    //Get Monthly Summary for a month
    @Query("SELECT " +
            "COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) as monthlyIncome, " +
            "COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) as monthlyExpense, " +
            "COUNT(*) monthlyTransaction " +
            "FROM transaction_table " +
            "WHERE strftime('%Y-%m', transactionDate/1000, 'unixepoch','localtime') = strftime('%Y-%m', :date/1000, 'unixepoch','localtime')")
    LiveData<MonthlySummary> getMonthlySummary(long date);

    @Query("SELECT * FROM transaction_table ORDER BY transactionDate DESC, createDate DESC")
    LiveData<List<TransactionModel>> getAllTransaction();

    @Query("SELECT * FROM transaction_table ORDER BY transactionDate DESC, createDate DESC")
    List<TransactionModel> getAllTransactionSync();

    @Query("SELECT * FROM transaction_table WHERE transId = :id LIMIT 1")
    TransactionModel getTransactionByIdSync(int id);

    @Query("DELETE FROM transaction_table")
    void deleteAllTransactions();

    // Category wise data for pie chart
    @Query("SELECT category, " +
            "SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) as income, " +
            "SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as expense " +
            "FROM transaction_table " +
            "WHERE strftime('%Y-%m', transactionDate/1000, 'unixepoch','localtime') = strftime('%Y-%m', :date/1000, 'unixepoch','localtime') " +
            "GROUP BY category " +
            "HAVING (income > 0 OR expense > 0) " +
            "ORDER BY (income + expense) DESC")
    LiveData<List<CategoryChartData>> getCategoryChartData(long date);

    // Daily data for current month
    @Query("SELECT DATE(transactionDate/1000, 'unixepoch','localtime') as date, " +
            "SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) as income, " +
            "SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as expense " +
            "FROM transaction_table " +
            "WHERE strftime('%Y-%m', transactionDate/1000, 'unixepoch','localtime') = strftime('%Y-%m', :date/1000, 'unixepoch','localtime') " +
            "GROUP BY DATE(transactionDate/1000, 'unixepoch','localtime') " +
            "HAVING (income > 0 OR expense > 0) " +
            "ORDER BY DATE(transactionDate/1000, 'unixepoch','localtime')")
    LiveData<List<DailyChartData>> getDailyChartData(long date);

    // Monthly data for current year
    @Query("SELECT strftime('%Y-%m', transactionDate/1000, 'unixepoch','localtime') as month, " +
            "SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) as income, " +
            "SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as expense " +
            "FROM transaction_table " +
            "WHERE strftime('%Y', transactionDate/1000, 'unixepoch','localtime') = strftime('%Y', :date/1000, 'unixepoch','localtime') " +
            "GROUP BY strftime('%Y-%m', transactionDate/1000, 'unixepoch','localtime') " +
            "HAVING (income > 0 OR expense > 0) " +
            "ORDER BY strftime('%Y-%m', transactionDate/1000, 'unixepoch','localtime')")
    LiveData<List<MonthlyChartData>> getMonthlyChartData(long date);

    // ========== REPORT QUERIES ==========

    // Account-wise summary for date range
    @Query("SELECT " +
            "a.accountId, " +
            "a.accountName, " +
            "a.currency, " +
            "COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0) as totalIncome, " +
            "COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) as totalExpense, " +
            "a.balance as currentBalance " +
            "FROM account_table a " +
            "LEFT JOIN transaction_table t ON a.accountId = t.accountId " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY a.accountId, a.accountName, a.currency, a.balance " +
            "ORDER BY (totalIncome + totalExpense) DESC")
    LiveData<List<AccountReportData>> getAccountReportData(long startDate, long endDate);

    // Category-wise summary for date range
    @Query("SELECT " +
            "category, " +
            "COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) as totalIncome, " +
            "COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) as totalExpense, " +
            "COUNT(*) as transactionCount " +
            "FROM transaction_table " +
            "WHERE transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY category " +
            "HAVING (totalIncome > 0 OR totalExpense > 0) " +
            "ORDER BY totalExpense DESC")
    LiveData<List<CategoryReportData>> getCategoryReportData(long startDate, long endDate);

    // Total summary for date range
    @Query("SELECT " +
            "COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) as totalIncome, " +
            "COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) as totalExpense, " +
            "COUNT(*) as totalTransactions " +
            "FROM transaction_table " +
            "WHERE transactionDate BETWEEN :startDate AND :endDate")
    LiveData<ReportSummaryData> getReportSummary(long startDate, long endDate);

    // Monthly comparison for insights
    @Query("SELECT " +
            "strftime('%Y-%m', transactionDate/1000, 'unixepoch','localtime') as month, " +
            "category, " +
            "COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) as monthlyExpense " +
            "FROM transaction_table " +
            "WHERE transactionDate BETWEEN :startDate AND :endDate " +
            "AND type = 'EXPENSE' " +
            "GROUP BY strftime('%Y-%m', transactionDate/1000, 'unixepoch','localtime'), category " +
            "ORDER BY month DESC, monthlyExpense DESC")
    LiveData<List<MonthlyComparisonData>> getMonthlyComparisonData(long startDate, long endDate);

    // Get transactions by date range (synchronous)
    @Query("SELECT * FROM transaction_table WHERE transactionDate BETWEEN :startDate AND :endDate ORDER BY transactionDate DESC, createDate DESC")
    List<TransactionModel> getTransactionByDateRange(long startDate, long endDate);
}