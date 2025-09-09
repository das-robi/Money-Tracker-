package com.devrobin.moneytracker.Views.Fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;

import com.devrobin.moneytracker.MVVM.MainViewModel.TransViewModel;
import com.devrobin.moneytracker.MVVM.Model.AccountModel;
import com.devrobin.moneytracker.MVVM.Model.TransactionModel;
import com.devrobin.moneytracker.R;
import com.devrobin.moneytracker.adapter.AccountAdapter;
import com.devrobin.moneytracker.adapter.TransactionAdapter;
import com.devrobin.moneytracker.databinding.ActivityMainBinding;
import com.devrobin.moneytracker.databinding.FragmentHomeBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import utils.Constant;
import utils.CurrencyConverter;
import utils.DailySummer;
import utils.MonthlySummary;
import utils.SharedPrefsManager;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding homeBinding;


    private ListView srchClndrList;
    private ArrayAdapter<String> searchAdapter;
    private Toolbar toolbar;

    public TransViewModel transViewModel;
    private TransactionAdapter transAdapter;
    private AccountAdapter accountAdapter;

    private TransactionAdapter.onTransItemClickListener transItemClickListener;

    private ArrayList<TransactionModel> transModelList;
    private ArrayList<AccountModel> accountList;



    private long transId;
    private int Edit_Trans_RequestCode = 1;


    // Calendar & Date
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault());
    private SimpleDateFormat dayDateFormate = new SimpleDateFormat("EEE", Locale.getDefault());
    private Calendar calendar = Calendar.getInstance();

    private String selectedDate;
    private SharedPrefsManager prefsManager;
    private String defaultCurrency;





    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        homeBinding = FragmentHomeBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);

        transViewModel = new ViewModelProvider(requireActivity()).get(TransViewModel.class);

        // Initialize CurrencyConverter and SharedPrefsManager
        CurrencyConverter.init(requireContext());
        prefsManager = SharedPrefsManager.getInstance(requireContext());
        defaultCurrency = prefsManager.getDefaultCurrency();

        Constant.setCategories();

        // Initialize account list and adapter
        accountList = new ArrayList<>();
        accountAdapter = new AccountAdapter(getContext(), accountList, new AccountAdapter.onAccountItemClickListener() {
            @Override
            public void accountItemClick(AccountModel accountModel) {
                // Handle account click if needed
            }
        });

        transViewModel.getAccountViewModel().getAllAccounts().observe(getViewLifecycleOwner(), new Observer<List<AccountModel>>() {
            @Override
            public void onChanged(List<AccountModel> accountModels) {
                accountList.clear();
                if (accountModels != null){
                    accountList.addAll(accountModels);
                }

                accountAdapter.notifyDataSetChanged();
            }
        });



//
//        srchClndrList = findViewById(R.id.search_calendar);
        searchAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);


        //ViewModel & and RecycleView
        setupTransactionClick();
        loadTransViewModel();

        //ViewModel for DateBase Transaction
        setUpDateForTransactions();



        /// Calendar Handling By clicking right and left Button
        homeBinding.navigatePreviousDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                transViewModel.navigateToPreviousDate();
            }
        });

        homeBinding.navigateNextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                transViewModel.navigateToNextDate();
            }
        });

        homeBinding.currentDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });




        return homeBinding.getRoot();
    }



    // ViewModel for RecycleView
    private void loadTransViewModel() {

        transViewModel.getTransactionList().observe(getViewLifecycleOwner(), transactionModels -> {
            if (transactionModels != null) {
                transModelList = new ArrayList<>(transactionModels);
            } else {
                transModelList = new ArrayList<>();
            }
            loadRecycleView();
        });
    }

    //Load Transaction in RecycleView
    private void loadRecycleView() {

        transAdapter = new TransactionAdapter(getContext(), new ArrayList<>(), transItemClickListener);

        homeBinding.recycleViewList.setLayoutManager(new LinearLayoutManager(getContext()));
        homeBinding.recycleViewList.setHasFixedSize(true);
        homeBinding.recycleViewList.setAdapter(transAdapter);
        transAdapter.setTransList(transModelList);



        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                TransactionModel transModel = transModelList.get(viewHolder.getAdapterPosition());

                transViewModel.deleteOldTrans(transModel);
            }
        }).attachToRecyclerView(homeBinding.recycleViewList);

    }

    private void setupTransactionClick() {
        transItemClickListener = transactionModel -> {
            if (getContext() == null) return;
            android.content.Intent intent = new android.content.Intent(getContext(), com.devrobin.moneytracker.Views.activity.EditTransactionActivity.class);
            intent.putExtra("transId", transactionModel.getTransId());
            startActivity(intent);
        };
    }

    //ViewModel For DateBase Transactions setUp
    public void setUpDateForTransactions(){

        //Observe Selected Date Changes
        transViewModel.getSelectedDate().observe(getViewLifecycleOwner(), new Observer<Date>() {
            @Override
            public void onChanged(Date date) {
                if (date != null){
                    updateDateDisplay(date);
                }
            }
        });


        //Observe transaction
        transViewModel.getTransactionList().observe(getViewLifecycleOwner(), new Observer<List<TransactionModel>>() {
            @Override
            public void onChanged(List<TransactionModel> transactionModels) {

                //Check Here transactionModel dont null value
                if (transactionModels != null && !transactionModels.isEmpty()){

                    transAdapter.setTransList(transModelList);
                    homeBinding.recycleViewList.setVisibility(View.VISIBLE);
                    homeBinding.emptyViewLayout.setVisibility(View.GONE);

                }
                else {
                    homeBinding.recycleViewList.setVisibility(View.GONE);
                    homeBinding.emptyViewLayout.setVisibility(View.VISIBLE);
                }

            }
        });

        // Observe Daily Summary with Currency Conversion
        String currentDefaultCurrency = prefsManager.getDefaultCurrency();
        transViewModel.getDailySummerWithConversion(currentDefaultCurrency).observe(getViewLifecycleOwner(), dailySummer -> {
            if (dailySummer != null) {
                updateDailySummary(dailySummer);
            }
        });


        transViewModel.getMonthlySummaryWithConversion(currentDefaultCurrency).observe(getViewLifecycleOwner(), new Observer<MonthlySummary>() {
            @Override
            public void onChanged(MonthlySummary monthlySummary) {
                if (monthlySummary != null){
                    updateMonthlySummary(monthlySummary);
                }
            }
        });




    }

    private String formatAmountWithCurrency(double amount, String currency) {
        String symbol = CurrencyConverter.getCurrencySymbol(currency);
        return String.format("%s %.0f", symbol, amount);
    }

    @SuppressLint("DefaultLocale")
    private void updateMonthlySummary(MonthlySummary monthlySummary) {

        // Get current default currency
        String currentDefaultCurrency = prefsManager.getDefaultCurrency();

        // Format amounts with currency symbol
        String incomeText = formatAmountWithCurrency(monthlySummary.getMonthlyIncome(), currentDefaultCurrency);
        String expenseText = formatAmountWithCurrency(monthlySummary.getMonthlyExpense(), currentDefaultCurrency);
        String balanceText = formatAmountWithCurrency(monthlySummary.getMonthlyBalance(), currentDefaultCurrency);

        homeBinding.monthlyIncomeAmount.setText(incomeText);
        homeBinding.monthlyExpenseAmount.setText(expenseText);
        homeBinding.monthlyBalanceAmount.setText(balanceText);

    }

    @SuppressLint("DefaultLocale")
    private void updateDailySummary(DailySummer dailySummer) {

        // Get current default currency
        String currentDefaultCurrency = prefsManager.getDefaultCurrency();

        // Convert amounts to default currency if needed
        double dailyIncome = dailySummer.getTotalIncome();
        double dailyExpense = dailySummer.getTotalExpense();

        // Format amounts with currency symbol
        String incomeText = formatAmountWithCurrency(dailyIncome, currentDefaultCurrency);
        String expenseText = formatAmountWithCurrency(dailyExpense, currentDefaultCurrency);

        homeBinding.dailyIncomeAmount.setText(incomeText);
        homeBinding.dailyExpenseAmount.setText(expenseText);
        homeBinding.totalTransaction.setText(String.format("%d", dailySummer.getTransactionCount()));
    }



    private void updateDateDisplay(Date date) {
        homeBinding.currentDate.setText(dateFormat.format(date));
        homeBinding.nameOfDay.setText(dayDateFormate.format(date));
    }


    //Calendar DatePicker
    private void showDatePicker() {

        Calendar calendar = Calendar.getInstance();
        Date currentDate = transViewModel.getSelectedDate().getValue();
        if (currentDate != null){
            calendar.setTime(currentDate);
        }


        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {

                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                transViewModel.setSelectedDate(calendar.getTime());

            }
        },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();

    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.search_calendar_list, menu);

        //SearchView
        MenuItem searchViewItem = menu.findItem(R.id.search);
        MenuItem calenderView = menu.findItem(R.id.calendar);

        SearchView searchView = (SearchView) searchViewItem.getActionView();
        CalendarView calendarView = (CalendarView) calenderView.getActionView();

        if (searchView != null) {
            searchView.setQueryHint("Type here to search");

            // Change background
            searchView.setBackgroundResource(R.color.gray);

            // Try to get and customize EditText
            int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
            EditText searchEditText = searchView.findViewById(id);
            if (searchEditText != null) {
                searchEditText.setHintTextColor(Color.BLACK);
                searchEditText.setTextColor(Color.BLACK);
            }

        }
//
        //CalendarView
        MenuItem clndrItem = menu.findItem(R.id.calendar);

        clndrItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
////                showDatePicker();
                return false;
            }
        });


        // Handle show/hide of ListView
        searchViewItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                srchClndrList.setVisibility(View.VISIBLE);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                srchClndrList.setVisibility(View.GONE);
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchAdapter.getFilter().filter(newText);
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }



    @Override
    public void onResume() {
        super.onResume();
        // Refresh default currency and update displays
        String newDefaultCurrency = prefsManager.getDefaultCurrency();
        if (!newDefaultCurrency.equals(defaultCurrency)) {
            defaultCurrency = newDefaultCurrency;
            // Refresh all currency displays
            refreshCurrencyDisplays();
        }
    }

    /**
     * Refresh all currency displays when default currency changes
     */
    private void refreshCurrencyDisplays() {
        // Force refresh of daily and monthly summaries
        String currentDefaultCurrency = prefsManager.getDefaultCurrency();

        // Refresh daily summary
        transViewModel.getDailySummerWithConversion(currentDefaultCurrency).observe(getViewLifecycleOwner(), dailySummer -> {
            if (dailySummer != null) {
                updateDailySummary(dailySummer);
            }
        });

        // Refresh monthly summary
        transViewModel.getMonthlySummaryWithConversion(currentDefaultCurrency).observe(getViewLifecycleOwner(), monthlySummary -> {
            if (monthlySummary != null) {
                updateMonthlySummary(monthlySummary);
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (transAdapter != null) {
            transAdapter.cleanup();
        }
    }
    
}