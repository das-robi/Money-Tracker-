package com.devrobin.moneytracker.MVVM.Repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.devrobin.moneytracker.MVVM.DAO.TransactionDao;
import com.devrobin.moneytracker.MVVM.Model.AccountReportData;
import com.devrobin.moneytracker.MVVM.Model.CategoryReportData;
import com.devrobin.moneytracker.MVVM.Model.MonthlyComparisonData;
import com.devrobin.moneytracker.MVVM.Model.ReportSummaryData;
import com.devrobin.moneytracker.MVVM.TransactionDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportRepository {
    private static final String TAG = "ReportRepository";

    private TransactionDao transactionDao;
    private ExecutorService executor;

    public ReportRepository(Application application) {
        TransactionDatabase database = TransactionDatabase.getInstance(application);
        transactionDao = database.transDao();
        executor = Executors.newFixedThreadPool(4);
    }

    /**
     * Get account-wise report data for a date range with filters
     */
    public LiveData<List<AccountReportData>> getAccountReportData(long startDate, long endDate, String accountFilter) {
        Log.d(TAG, "Getting account report data from " + startDate + " to " + endDate + " with account filter: " + accountFilter);
        return transactionDao.getAccountReportData(startDate, endDate);
    }

    /**
     * Get category-wise report data for a date range with filters
     */
    public LiveData<List<CategoryReportData>> getCategoryReportData(long startDate, long endDate, String categoryFilter) {
        Log.d(TAG, "Getting category report data from " + startDate + " to " + endDate + " with category filter: " + categoryFilter);
        return transactionDao.getCategoryReportData(startDate, endDate);
    }

    /**
     * Get overall report summary for a date range with filters
     */
    public LiveData<ReportSummaryData> getReportSummary(long startDate, long endDate, String categoryFilter, String accountFilter) {
        Log.d(TAG, "Getting report summary from " + startDate + " to " + endDate + " with filters - Category: " + categoryFilter + ", Account: " + accountFilter);
        return transactionDao.getReportSummary(startDate, endDate);
    }

    /**
     * Get monthly comparison data for insights with filters
     */
    public LiveData<List<MonthlyComparisonData>> getMonthlyComparisonData(long startDate, long endDate, String categoryFilter) {
        Log.d(TAG, "Getting monthly comparison data from " + startDate + " to " + endDate + " with category filter: " + categoryFilter);
        return transactionDao.getMonthlyComparisonData(startDate, endDate);
    }

    /**
     * Cleanup resources
     */
    public void cleanup() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
