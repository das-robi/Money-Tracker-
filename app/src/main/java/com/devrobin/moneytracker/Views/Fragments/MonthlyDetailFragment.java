package com.devrobin.moneytracker.Views.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

import com.devrobin.moneytracker.MVVM.DAO.TransactionDao;
import com.devrobin.moneytracker.MVVM.Model.MonthlyReportData;
import com.devrobin.moneytracker.MVVM.Model.TransactionModel;
import com.devrobin.moneytracker.MVVM.TransactionDatabase;
import com.devrobin.moneytracker.R;
import com.devrobin.moneytracker.adapter.TransactionAdapter;
import com.devrobin.moneytracker.databinding.FragmentMonthlyDetailBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import utils.CurrencyConverter;
import utils.SharedPrefsManager;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MonthlyDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MonthlyDetailFragment extends Fragment {

    private static final String TAG = "MonthlyDetailFragment";
    private static final String ARG_MONTHLY_REPORT = "monthly_report";

    private FragmentMonthlyDetailBinding binding;
    private MonthlyReportData monthlyReport;
    private SharedPrefsManager prefsManager;
    private String defaultCurrency;

    private TransactionAdapter transactionAdapter;
    private List<TransactionModel> transactionList;

    private int selectedYear;
    private int selectedMonth;

    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public MonthlyDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Create new instance with monthly report data
     */
    public static MonthlyDetailFragment newInstance(MonthlyReportData monthlyReport) {
        MonthlyDetailFragment fragment = new MonthlyDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_MONTHLY_REPORT, monthlyReport);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            monthlyReport = getArguments().getParcelable(ARG_MONTHLY_REPORT);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMonthlyDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            initializeComponents();
            setupRecyclerView();
            setupYearSpinner();
            setupClickListeners();
            loadMonthlyData();
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated: ", e);
            showError("Failed to initialize monthly detail view. Please try again.");
        }
    }

    /**
     * Initialize components and dependencies
     */
    private void initializeComponents() {
        try {
            // Initialize SharedPrefsManager
            prefsManager = SharedPrefsManager.getInstance(requireContext());

            // Initialize CurrencyConverter
            CurrencyConverter.init(requireContext());

            // Get default currency
            defaultCurrency = prefsManager.getDefaultCurrency();
            if (defaultCurrency == null) {
                defaultCurrency = "BDT"; // Default to BDT if not set
            }

            // Set selected year and month from monthly report
            if (monthlyReport != null) {
                selectedYear = monthlyReport.getYear();
                selectedMonth = monthlyReport.getMonth();
            } else {
                // Default to current year and month
                Calendar calendar = Calendar.getInstance();
                selectedYear = calendar.get(Calendar.YEAR);
                selectedMonth = calendar.get(Calendar.MONTH);
            }

            // Initialize transaction list
            transactionList = new ArrayList<>();

        } catch (Exception e) {
            Log.e(TAG, "Error initializing components: ", e);
            throw e;
        }
    }

    /**
     * Setup RecyclerView for transactions
     */
    private void setupRecyclerView() {
        try {
            transactionAdapter = new TransactionAdapter(requireContext(), new ArrayList<>(transactionList), null);
            binding.rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.rvTransactions.setAdapter(transactionAdapter);

        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView: ", e);
            throw e;
        }
    }

    /**
     * Setup click listeners for interactive elements
     */
    private void setupClickListeners() {
        try {
            // Back button click listener
            binding.ivBack.setOnClickListener(v -> {
                try {
                    requireActivity().onBackPressed();
                } catch (Exception e) {
                    Log.e(TAG, "Error handling back button: ", e);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: ", e);
        }
    }

    /**
     * Setup year spinner for filtering
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
            binding.spinnerYear.setAdapter(yearAdapter);

            // Set current year as default selection
            binding.spinnerYear.setSelection(0);

            // Setup year selection listener
            binding.spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedYearStr = parent.getItemAtPosition(position).toString();
                    selectedYear = Integer.parseInt(selectedYearStr);
                    loadMonthlyData();
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
     * Load monthly data for selected year
     */
    private void loadMonthlyData() {
        try {
            showLoadingState(true);

            executor.execute(() -> {
                try {
                    TransactionDatabase database = TransactionDatabase.getInstance(requireContext());
                    TransactionDao transactionDao = database.transDao();

                    List<TransactionModel> monthlyTransactions = new ArrayList<>();

                    // Generate data for all 12 months
                    for (int month = 0; month < 12; month++) {
                        List<TransactionModel> monthTransactions = getTransactionsForMonth(transactionDao, selectedYear, month);
                        monthlyTransactions.addAll(monthTransactions);
                    }

                    // Update UI on main thread
                    mainHandler.post(() -> {
                        try {
                            transactionList.clear();
                            transactionList.addAll(monthlyTransactions);
                            transactionAdapter.notifyDataSetChanged();
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
            Log.e(TAG, "Error in loadMonthlyData: ", e);
            showLoadingState(false);
        }
    }

    /**
     * Get transactions for a specific month
     */
    private List<TransactionModel> getTransactionsForMonth(TransactionDao transactionDao, int year, int month) {
        try {
            // Calculate start and end of month
            Calendar startOfMonth = Calendar.getInstance();
            startOfMonth.set(year, month, 1, 0, 0, 0);
            startOfMonth.set(Calendar.MILLISECOND, 0);

            Calendar endOfMonth = Calendar.getInstance();
            endOfMonth.set(year, month, endOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
            endOfMonth.set(Calendar.MILLISECOND, 999);

            // Get transactions for this month
            return transactionDao.getTransactionByDateRange(
                    startOfMonth.getTimeInMillis(),
                    endOfMonth.getTimeInMillis()
            );

        } catch (Exception e) {
            Log.e(TAG, "Error getting transactions for month " + year + "-" + month + ": ", e);
            return new ArrayList<>();
        }
    }

    /**
     * Show or hide loading state
     */
    private void showLoadingState(boolean show) {
        try {
            if (binding != null && binding.progressBar != null) {
                binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing loading state: ", e);
        }
    }

    /**
     * Update empty state visibility
     */
    private void updateEmptyState() {
        try {
            if (binding != null) {
                if (transactionList.isEmpty()) {
                    binding.tvNoData.setVisibility(View.VISIBLE);
                    binding.rvTransactions.setVisibility(View.GONE);
                } else {
                    binding.tvNoData.setVisibility(View.GONE);
                    binding.rvTransactions.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating empty state: ", e);
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

    @Override
    public void onDestroyView() {
        try {
            super.onDestroyView();

            // Cleanup resources
            if (executor != null && !executor.isShutdown()) {
                executor.shutdown();
            }

            // Clear binding
            binding = null;

        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroyView: ", e);
        }
    }
}