package com.devrobin.moneytracker.MVVM.ViewModel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.devrobin.moneytracker.MVVM.Model.AccountReportData;
import com.devrobin.moneytracker.MVVM.Model.CategoryReportData;
import com.devrobin.moneytracker.MVVM.Model.MonthlyComparisonData;
import com.devrobin.moneytracker.MVVM.Model.ReportSummaryData;
import com.devrobin.moneytracker.MVVM.Repository.ReportRepository;

import java.util.Calendar;
import java.util.List;

public class ReportViewModel extends AndroidViewModel {

    private static final String TAG = "ReportViewModel";

    private ReportRepository reportRepository;
    private MutableLiveData<Long> startDate;
    private MutableLiveData<Long> endDate;
    private MutableLiveData<String> selectedCategory;
    private MutableLiveData<String> selectedAccount;

    public ReportViewModel(Application application) {
        super(application);
        reportRepository = new ReportRepository(application);

        // Initialize with current month
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        startDate = new MutableLiveData<>(calendar.getTimeInMillis());

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        endDate = new MutableLiveData<>(calendar.getTimeInMillis());

        // Initialize filter selections
        selectedCategory = new MutableLiveData<>("All");
        selectedAccount = new MutableLiveData<>("All");
    }

    /**
     * Set the date range for reports
     */
    public void setDateRange(long startDate, long endDate) {
        Log.d(TAG, "Setting date range from " + startDate + " to " + endDate);
        this.startDate.setValue(startDate);
        this.endDate.setValue(endDate);
    }

    /**
     * Get the start date
     */
    public LiveData<Long> getStartDate() {
        return startDate;
    }

    /**
     * Get the end date
     */
    public LiveData<Long> getEndDate() {
        return endDate;
    }

    /**
     * Set selected category filter
     */
    public void setSelectedCategory(String category) {
        selectedCategory.setValue(category);
    }

    /**
     * Set selected account filter
     */
    public void setSelectedAccount(String account) {
        selectedAccount.setValue(account);
    }

    /**
     * Get selected category filter
     */
    public LiveData<String> getSelectedCategory() {
        return selectedCategory;
    }

    /**
     * Get selected account filter
     */
    public LiveData<String> getSelectedAccount() {
        return selectedAccount;
    }

    /**
     * Get account-wise report data for current date range
     */
    public LiveData<List<AccountReportData>> getAccountReportData() {
        Long start = startDate.getValue();
        Long end = endDate.getValue();
        String account = selectedAccount.getValue();
        if (start != null && end != null && account != null) {
            return reportRepository.getAccountReportData(start, end, account);
        }
        return new MutableLiveData<>();
    }

    /**
     * Get category-wise report data for current date range
     */
    public LiveData<List<CategoryReportData>> getCategoryReportData() {
        Long start = startDate.getValue();
        Long end = endDate.getValue();
        String category = selectedCategory.getValue();
        if (start != null && end != null && category != null) {
            return reportRepository.getCategoryReportData(start, end, category);
        }
        return new MutableLiveData<>();
    }

    /**
     * Get overall report summary for current date range
     */
    public LiveData<ReportSummaryData> getReportSummary() {
        Long start = startDate.getValue();
        Long end = endDate.getValue();
        String category = selectedCategory.getValue();
        String account = selectedAccount.getValue();
        if (start != null && end != null && category != null && account != null) {
            return reportRepository.getReportSummary(start, end, category, account);
        }
        return new MutableLiveData<>();
    }

    /**
     * Get monthly comparison data for insights
     */
    public LiveData<List<MonthlyComparisonData>> getMonthlyComparisonData() {
        Long start = startDate.getValue();
        Long end = endDate.getValue();
        String category = selectedCategory.getValue();
        if (start != null && end != null && category != null) {
            return reportRepository.getMonthlyComparisonData(start, end, category);
        }
        return new MutableLiveData<>();
    }

    /**
     * Set date range to current month
     */
    public void setCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        startDate.setValue(calendar.getTimeInMillis());

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        endDate.setValue(calendar.getTimeInMillis());
    }

    /**
     * Set date range to current year
     */
    public void setCurrentYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        startDate.setValue(calendar.getTimeInMillis());

        calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        endDate.setValue(calendar.getTimeInMillis());
    }

    /**
     * Set date range to last 30 days
     */
    public void setLast30Days() {
        Calendar calendar = Calendar.getInstance();
        endDate.setValue(calendar.getTimeInMillis());

        calendar.add(Calendar.DAY_OF_MONTH, -30);
        startDate.setValue(calendar.getTimeInMillis());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (reportRepository != null) {
            reportRepository.cleanup();
        }
    }
}
