package com.devrobin.moneytracker.Views.Fragments;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.devrobin.moneytracker.MVVM.DAO.AccountDAO;
import com.devrobin.moneytracker.MVVM.DAO.BudgetDAO;
import com.devrobin.moneytracker.MVVM.DAO.TransactionDao;
import com.devrobin.moneytracker.MVVM.Model.AccountModel;
import com.devrobin.moneytracker.MVVM.Model.AccountReportData;
import com.devrobin.moneytracker.MVVM.Model.BudgetModel;
import com.devrobin.moneytracker.MVVM.Model.BudgetVsActualData;
import com.devrobin.moneytracker.MVVM.Model.InsightData;
import com.devrobin.moneytracker.MVVM.Model.MonthlyReportData;
import com.devrobin.moneytracker.MVVM.Model.TransactionModel;
import com.devrobin.moneytracker.MVVM.TransactionDatabase;
import com.devrobin.moneytracker.MVVM.ViewModel.ReportViewModel;
import com.devrobin.moneytracker.R;
import com.devrobin.moneytracker.adapter.AccountReportAdapter;
import com.devrobin.moneytracker.adapter.BudgetVsActualAdapter;
import com.devrobin.moneytracker.adapter.InsightsAdapter;
import com.devrobin.moneytracker.adapter.MonthlyReportAdapter;
import com.devrobin.moneytracker.databinding.FragmentTransactionReportsBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import utils.CurrencyConverter;
import utils.SharedPrefsManager;

public class TransactionReports extends Fragment implements MonthlyReportAdapter.OnMonthlyReportClickListener {

    private static final String TAG = "TransactionReports";
    private static final int LOADING_DELAY = 1000;
    private static final int INITIAL_LOADING_DELAY = 1500;

    // View Binding
    private FragmentTransactionReportsBinding reportsBinding;

    // ViewModel and Utilities
    private ReportViewModel reportViewModel;
    private SharedPrefsManager prefsManager;

    // Adapters
    private MonthlyReportAdapter monthlyReportAdapter;
    private AccountReportAdapter accountReportAdapter;
    private BudgetVsActualAdapter budgetVsActualAdapter;
    private InsightsAdapter insightsAdapter;

    // Data lists
    private final List<MonthlyReportData> monthlyReportsList = new ArrayList<>();
    private final List<AccountReportData> accountReportList = new ArrayList<>();
    private final List<BudgetVsActualData> budgetVsActualList = new ArrayList<>();
    private final List<InsightData> insightsList = new ArrayList<>();

    // Current year for monthly reports
    private int selectedYear;

    // Date formatters
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat displayFormat;

    // Default currency
    private String defaultCurrency;

    // Background executor for database operations
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    // Main thread handler for UI updates
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public TransactionReports() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        reportsBinding = FragmentTransactionReportsBinding.inflate(inflater, container, false);
        return reportsBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            initializeComponents();
            setupRecyclerViews();
            setupObservers();
            setupClickListeners();
            loadInitialData();
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated: ", e);
            showError("Failed to initialize reports. Please try again.");
        }
    }

    /**
     * Initialize all components and dependencies
     */
    private void initializeComponents() {
        try {
            // Initialize ViewModel
            reportViewModel = new ViewModelProvider(requireActivity()).get(ReportViewModel.class);

            // Initialize SharedPrefsManager
            prefsManager = SharedPrefsManager.getInstance(requireContext());

            // Initialize CurrencyConverter
            CurrencyConverter.init(requireContext());

            // Get default currency
            defaultCurrency = prefsManager.getDefaultCurrency();

            // Initialize date formatters
            dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

            // Set current year as default
            selectedYear = Calendar.getInstance().get(Calendar.YEAR);

            // Set initial date range (current month)
            reportViewModel.setCurrentMonth();

        } catch (Exception e) {
            Log.e(TAG, "Error initializing components: ", e);
            throw e;
        }
    }

    /**
     * Setup RecyclerViews with adapters
     */
    private void setupRecyclerViews() {
        try {
            // Monthly Reports RecyclerView
            monthlyReportAdapter = new MonthlyReportAdapter(requireContext(), monthlyReportsList, this);
            reportsBinding.rvMonthlyCosts.setLayoutManager(new LinearLayoutManager(requireContext()));
            reportsBinding.rvMonthlyCosts.setAdapter(monthlyReportAdapter);

            // Account Report RecyclerView
            accountReportAdapter = new AccountReportAdapter(requireContext(), accountReportList);
            reportsBinding.rvAccountReport.setLayoutManager(new LinearLayoutManager(requireContext()));
            reportsBinding.rvAccountReport.setAdapter(accountReportAdapter);

            // Budget vs Actual RecyclerView
            budgetVsActualAdapter = new BudgetVsActualAdapter(requireContext(), budgetVsActualList);
            reportsBinding.rvBudgetVsActual.setLayoutManager(new LinearLayoutManager(requireContext()));
            reportsBinding.rvBudgetVsActual.setAdapter(budgetVsActualAdapter);

            // Insights RecyclerView
            insightsAdapter = new InsightsAdapter(requireContext(), insightsList);
            reportsBinding.rvInsights.setLayoutManager(new LinearLayoutManager(requireContext()));
            reportsBinding.rvInsights.setAdapter(insightsAdapter);

        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerViews: ", e);
            throw e;
        }
    }

    /**
     * Setup LiveData observers for real-time updates
     */
    private void setupObservers() {
        try {
            // For now, we'll keep the basic structure
            // Monthly reports are loaded directly without observers

        } catch (Exception e) {
            Log.e(TAG, "Error setting up observers: ", e);
            throw e;
        }
    }

    /**
     * Setup click listeners for all interactive elements
     */
    private void setupClickListeners() {
        try {
            // Setup year spinner for monthly reports
            setupSpinners();

        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: ", e);
            throw e;
        }
    }

    /**
     * Setup year spinner for monthly reports
     */
    private void setupSpinners() {
        try {
            // Setup year spinner for monthly reports
            setupYearSpinner();

        } catch (Exception e) {
            Log.e(TAG, "Error setting up spinners: ", e);
            throw e;
        }
    }

    /**
     * Setup year spinner for monthly reports
     */
    private void setupYearSpinner() {
        try {
            // Create list of years (current year + 5 years back)
            List<String> yearList = new ArrayList<>();
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            for (int i = currentYear; i >= currentYear - 5; i--) {
                yearList.add(String.valueOf(i));
            }

            ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    yearList
            );
            yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            reportsBinding.spinnerYear.setAdapter(yearAdapter);

            // Set current year as default selection
            reportsBinding.spinnerYear.setSelection(0);

            // Setup year selection listener
            reportsBinding.spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedYearStr = parent.getItemAtPosition(position).toString();
                    selectedYear = Integer.parseInt(selectedYearStr);
                    loadMonthlyDataForYear(selectedYear);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Do nothing
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error setting up year spinner: ", e);
            throw e;
        }
    }



    /**
     * Load initial data and setup UI
     */
    private void loadInitialData() {
        try {
            // Load monthly data for current year
            loadMonthlyDataForYear(selectedYear);

            // Load account and budget data
            loadAccountData();
            loadBudgetData();

            // Show initial loading state
            showLoadingState(true);

            // Hide loading state after data loads
            mainHandler.postDelayed(() -> showLoadingState(false), INITIAL_LOADING_DELAY);

        } catch (Exception e) {
            Log.e(TAG, "Error loading initial data: ", e);
            showError("Failed to load initial data. Please refresh.");
        }
    }

    /**
     * Load account data for reports
     */
    private void loadAccountData() {
        try {
            executor.execute(() -> {
                try {
                    TransactionDatabase database = TransactionDatabase.getInstance(requireContext());
                    AccountDAO accountDao = database.accountDao();

                    List<AccountModel> accounts = accountDao.getAllAccountsSync();
                    List<AccountReportData> accountReports = new ArrayList<>();

                    if (accounts != null) {
                        for (AccountModel account : accounts) {
                            if (account != null) {
                                // Get account balance and transaction count
                                double balance = account.getBalance();
                                int transactionCount = 0; // You can implement this if needed

                                AccountReportData reportData = new AccountReportData(
                                        account.getAccountId(),
                                        account.getAccountName(),
                                        account.getCurrency(),
                                        0.0, // totalIncome - placeholder
                                        0.0, // totalExpense - placeholder
                                        balance
                                );
                                accountReports.add(reportData);
                            }
                        }
                    }

                    // Update UI on main thread
                    mainHandler.post(() -> {
                        try {
                            accountReportList.clear();
                            accountReportList.addAll(accountReports);
                            accountReportAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            Log.e(TAG, "Error updating account adapter: ", e);
                        }
                    });

                } catch (Exception e) {
                    Log.e(TAG, "Error loading account data: ", e);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error in loadAccountData: ", e);
        }
    }

    /**
     * Load budget data for reports
     */
    private void loadBudgetData() {
        try {
            executor.execute(() -> {
                try {
                    TransactionDatabase database = TransactionDatabase.getInstance(requireContext());
                    BudgetDAO budgetDao = database.budgetDao();

                    List<BudgetModel> budgets = budgetDao.getAllBudgetsSync();
                    List<BudgetVsActualData> budgetReports = new ArrayList<>();

                    if (budgets != null) {
                        for (BudgetModel budget : budgets) {
                            if (budget != null) {
                                // Get actual spending for this category
                                double actualSpending = getActualSpendingForCategory(budget.getCategory());

                                BudgetVsActualData budgetData = new BudgetVsActualData(
                                        budget.getCategory(),
                                        budget.getBudgetType(),
                                        budget.getBudgetAmount(),
                                        actualSpending
                                );
                                budgetReports.add(budgetData);
                            }
                        }
                    }

                    // Update UI on main thread
                    mainHandler.post(() -> {
                        try {
                            budgetVsActualList.clear();
                            budgetVsActualList.addAll(budgetReports);
                            budgetVsActualAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            Log.e(TAG, "Error updating budget adapter: ", e);
                        }
                    });

                } catch (Exception e) {
                    Log.e(TAG, "Error loading budget data: ", e);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error in loadBudgetData: ", e);
        }
    }

    /**
     * Get actual spending for a category
     */
    private double getActualSpendingForCategory(String category) {
        try {
            // This is a simplified implementation
            // You can enhance this to get actual spending from transactions
            return 0.0; // Placeholder
        } catch (Exception e) {
            Log.e(TAG, "Error getting actual spending for category: " + category, e);
            return 0.0;
        }
    }

    /**
     * Load monthly data for a specific year
     */
    private void loadMonthlyDataForYear(int year) {
        try {
            showLoadingState(true);

            executor.execute(() -> {
                try {
                    TransactionDatabase database = TransactionDatabase.getInstance(requireContext());
                    TransactionDao transactionDao = database.transDao();

                    List<MonthlyReportData> monthlyData = new ArrayList<>();

                    // Generate data for all 12 months
                    for (int month = 0; month < 12; month++) {
                        MonthlyReportData monthData = generateMonthlyData(transactionDao, year, month);
                        monthlyData.add(monthData);
                    }

                    // Update UI on main thread
                    mainHandler.post(() -> {
                        try {
                            monthlyReportsList.clear();
                            monthlyReportsList.addAll(monthlyData);
                            monthlyReportAdapter.notifyDataSetChanged();
                            updateEmptyState();
                            showLoadingState(false);
                        } catch (Exception e) {
                            Log.e(TAG, "Error updating UI: ", e);
                            showLoadingState(false);
                        }
                    });

                } catch (Exception e) {
                    Log.e(TAG, "Error loading monthly data: ", e);
                    mainHandler.post(() -> {
                        showLoadingState(false);
                        showError("Failed to load monthly data. Please try again.");
                    });
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error in loadMonthlyDataForYear: ", e);
            showLoadingState(false);
        }
    }

    /**
     * Generate monthly data for a specific month
     */
    private MonthlyReportData generateMonthlyData(TransactionDao transactionDao, int year, int month) {
        try {
            // Calculate start and end of month
            Calendar startOfMonth = Calendar.getInstance();
            startOfMonth.set(year, month, 1, 0, 0, 0);
            startOfMonth.set(Calendar.MILLISECOND, 0);

            Calendar endOfMonth = Calendar.getInstance();
            endOfMonth.set(year, month, endOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
            endOfMonth.set(Calendar.MILLISECOND, 999);

            // Get transactions for this month
            List<TransactionModel> monthTransactions = transactionDao.getTransactionByDateRange(
                    startOfMonth.getTimeInMillis(),
                    endOfMonth.getTimeInMillis()
            );

            double totalIncome = 0.0;
            double totalExpense = 0.0;
            int transactionCount = 0;

            if (monthTransactions != null) {
                transactionCount = monthTransactions.size();

                for (TransactionModel transaction : monthTransactions) {
                    if (transaction != null) {
                        if ("Income".equals(transaction.getType())) {
                            totalIncome += transaction.getAmount();
                        } else {
                            totalExpense += transaction.getAmount();
                        }
                    }
                }
            }

            double netBalance = totalIncome - totalExpense;
            String monthName = MonthlyReportData.getMonthName(month);
            String currencySymbol = CurrencyConverter.getCurrencySymbol(defaultCurrency != null ? defaultCurrency : "BDT");

            return new MonthlyReportData(monthName, year, month, totalIncome,
                    totalExpense, netBalance, currencySymbol, transactionCount);

        } catch (Exception e) {
            Log.e(TAG, "Error generating monthly data for " + year + "-" + month + ": ", e);
            // Return default data
            String monthName = MonthlyReportData.getMonthName(month);
            String currencySymbol = CurrencyConverter.getCurrencySymbol(defaultCurrency != null ? defaultCurrency : "BDT");
            return new MonthlyReportData(monthName, year, month, 0.0, 0.0, 0.0, currencySymbol, 0);
        }
    }

















    /**
     * Show or hide loading state
     */
    private void showLoadingState(boolean show) {
        try {
            if (reportsBinding != null && reportsBinding.progressBar != null) {
                reportsBinding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing loading state: ", e);
        }
    }

    /**
     * Handle empty states for different sections
     */
    private void updateEmptyState() {
        try {
            if (reportsBinding == null) return;

            // Handle monthly reports empty state
            if (monthlyReportsList.isEmpty()) {
                if (reportsBinding.tvNoMonthlyData != null) {
                    reportsBinding.tvNoMonthlyData.setVisibility(View.VISIBLE);
                }
                if (reportsBinding.rvMonthlyCosts != null) {
                    reportsBinding.rvMonthlyCosts.setVisibility(View.GONE);
                }
            } else {
                if (reportsBinding.tvNoMonthlyData != null) {
                    reportsBinding.tvNoMonthlyData.setVisibility(View.GONE);
                }
                if (reportsBinding.rvMonthlyCosts != null) {
                    reportsBinding.rvMonthlyCosts.setVisibility(View.VISIBLE);
                }
            }

            // Handle account report empty state
            if (accountReportList.isEmpty()) {
                if (reportsBinding.tvNoAccountData != null) {
                    reportsBinding.tvNoAccountData.setVisibility(View.VISIBLE);
                }
                if (reportsBinding.rvAccountReport != null) {
                    reportsBinding.rvAccountReport.setVisibility(View.GONE);
                }
            } else {
                if (reportsBinding.tvNoAccountData != null) {
                    reportsBinding.tvNoAccountData.setVisibility(View.GONE);
                }
                if (reportsBinding.rvAccountReport != null) {
                    reportsBinding.rvAccountReport.setVisibility(View.VISIBLE);
                }
            }

            // Handle budget vs actual empty state
            if (budgetVsActualList.isEmpty()) {
                if (reportsBinding.tvNoBudgetData != null) {
                    reportsBinding.tvNoBudgetData.setVisibility(View.VISIBLE);
                }
                if (reportsBinding.rvBudgetVsActual != null) {
                    reportsBinding.rvBudgetVsActual.setVisibility(View.GONE);
                }
            } else {
                if (reportsBinding.tvNoBudgetData != null) {
                    reportsBinding.tvNoBudgetData.setVisibility(View.GONE);
                }
                if (reportsBinding.rvBudgetVsActual != null) {
                    reportsBinding.rvBudgetVsActual.setVisibility(View.VISIBLE);
                }
            }

            // Handle insights empty state
            if (insightsList.isEmpty()) {
                if (reportsBinding.tvNoInsightsData != null) {
                    reportsBinding.tvNoInsightsData.setVisibility(View.VISIBLE);
                }
                if (reportsBinding.rvInsights != null) {
                    reportsBinding.rvInsights.setVisibility(View.GONE);
                }
            } else {
                if (reportsBinding.tvNoInsightsData != null) {
                    reportsBinding.tvNoInsightsData.setVisibility(View.GONE);
                }
                if (reportsBinding.rvInsights != null) {
                    reportsBinding.rvInsights.setVisibility(View.VISIBLE);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error handling empty states: ", e);
        }
    }

    /**
     * Handle empty states for different sections
     */
    private void handleEmptyStates() {
        try {
            if (reportsBinding == null) return;

            // Handle account report empty state
            if (accountReportList.isEmpty()) {
                if (reportsBinding.tvNoAccountData != null) {
                    reportsBinding.tvNoAccountData.setVisibility(View.VISIBLE);
                }
                if (reportsBinding.rvAccountReport != null) {
                    reportsBinding.rvAccountReport.setVisibility(View.GONE);
                }
            } else {
                if (reportsBinding.tvNoAccountData != null) {
                    reportsBinding.tvNoAccountData.setVisibility(View.GONE);
                }
                if (reportsBinding.rvAccountReport != null) {
                    reportsBinding.rvAccountReport.setVisibility(View.VISIBLE);
                }
            }

            // Handle budget vs actual empty state
            if (budgetVsActualList.isEmpty()) {
                if (reportsBinding.tvNoBudgetData != null) {
                    reportsBinding.tvNoBudgetData.setVisibility(View.VISIBLE);
                }
                if (reportsBinding.rvBudgetVsActual != null) {
                    reportsBinding.rvBudgetVsActual.setVisibility(View.GONE);
                }
            } else {
                if (reportsBinding.tvNoBudgetData != null) {
                    reportsBinding.tvNoBudgetData.setVisibility(View.GONE);
                }
                if (reportsBinding.rvBudgetVsActual != null) {
                    reportsBinding.rvBudgetVsActual.setVisibility(View.VISIBLE);
                }
            }

            // Handle insights empty state
            if (insightsList.isEmpty()) {
                if (reportsBinding.tvNoInsightsData != null) {
                    reportsBinding.tvNoInsightsData.setVisibility(View.VISIBLE);
                }
                if (reportsBinding.rvInsights != null) {
                    reportsBinding.rvInsights.setVisibility(View.GONE);
                }
            } else {
                if (reportsBinding.tvNoInsightsData != null) {
                    reportsBinding.tvNoInsightsData.setVisibility(View.GONE);
                }
                if (reportsBinding.rvInsights != null) {
                    reportsBinding.rvInsights.setVisibility(View.VISIBLE);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error handling empty states: ", e);
        }
    }

    /**
     * Show toast message
     */
    private void showToast(String message) {
        try {
            if (getContext() != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing toast: ", e);
        }
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        try {
            showToast(message);
        } catch (Exception e) {
            Log.e(TAG, "Error showing error message: ", e);
        }
    }

    /**
     * Handle monthly report click - navigate to monthly detail fragment
     */
    @Override
    public void onMonthlyReportClick(MonthlyReportData monthlyReport) {
        try {
            // Navigate to monthly detail fragment
            MonthlyDetailFragment detailFragment = MonthlyDetailFragment.newInstance(monthlyReport);

            // Replace current fragment with detail fragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contentFrameLayout, detailFragment)
                    .addToBackStack(null)
                    .commit();

        } catch (Exception e) {
            Log.e(TAG, "Error navigating to monthly detail: ", e);
            showError("Failed to open monthly detail. Please try again.");
        }
    }

    @Override
    public void onDestroyView() {
        try {
            super.onDestroyView();

            // Cleanup resources
            if (executor != null && !executor.isShutdown()) {
                executor.shutdown();
            }

            // Clear binding
            reportsBinding = null;

        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroyView: ", e);
        }
    }


}