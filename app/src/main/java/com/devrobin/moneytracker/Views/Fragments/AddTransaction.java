package com.devrobin.moneytracker.Views.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.devrobin.moneytracker.MVVM.MainViewModel.CategoryViewModel;
import com.devrobin.moneytracker.MVVM.MainViewModel.TransViewModel;
import com.devrobin.moneytracker.MVVM.Model.AccountModel;
import com.devrobin.moneytracker.MVVM.Model.CategoryModel;
import com.devrobin.moneytracker.MVVM.Model.TransactionModel;
import com.devrobin.moneytracker.R;
import com.devrobin.moneytracker.Views.activity.MainActivity;
import com.devrobin.moneytracker.adapter.AccountAdapter;
import com.devrobin.moneytracker.adapter.CategoryAdapter;
import com.devrobin.moneytracker.databinding.FragmentAddTransactionBinding;
import com.devrobin.moneytracker.databinding.ListItemDialogBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import utils.Constant;


public class AddTransaction extends BottomSheetDialogFragment {

    //Widget
    private FragmentAddTransactionBinding addBinding;

    // Widget
    private CategoryAdapter categoryAdapter;
    private TransactionModel transModel;
    private TransViewModel transViewModel;
    private CategoryViewModel categoryViewModel;

    private Date selectedDate;
    private AccountModel selectedAccount;
    private ArrayList<AccountModel> accountList = new ArrayList<>();

    // User Id
    private String currentUserName;
    private String currentUserId;

    // FireBase Connection
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;


//

    public AddTransaction() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        addBinding = FragmentAddTransactionBinding.inflate(inflater, container, false);

        // Initialize ViewModels
        transViewModel = new ViewModelProvider(requireActivity()).get(TransViewModel.class);
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);

        authStateListener = firebaseAuth -> {
            firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser != null) {
                // User is signed in
            } else {
                // User is signed out
            }
        };

        // Get selected date from ViewModel
        selectedDate = transViewModel.getSelectedDate().getValue();
        if (selectedDate == null) {
            selectedDate = new Date(); // fallback to current date
        }

        // Income & Expense Btn
        transModel = new TransactionModel();

        addBinding.expenseBtn.setOnClickListener(view -> {
            addBinding.expenseBtn.setBackground(getContext().getDrawable(R.drawable.btnselector_bg));
            addBinding.expenseBtn.setTextColor(getContext().getColor(R.color.white));

            addBinding.incomeBtn.setBackground(getContext().getDrawable(R.drawable.defaultbtn_bg));
            addBinding.incomeBtn.setTextColor(getContext().getColor(R.color.black));

            transModel.setType(Constant.EXPENSE);
        });

        addBinding.incomeBtn.setOnClickListener(view -> {
            addBinding.incomeBtn.setBackground(getContext().getDrawable(R.drawable.btnselector_bg));
            addBinding.incomeBtn.setTextColor(getContext().getColor(R.color.white));

            addBinding.expenseBtn.setBackground(getContext().getDrawable(R.drawable.defaultbtn_bg));
            addBinding.expenseBtn.setTextColor(getContext().getColor(R.color.black));

            transModel.setType(Constant.INCOME);
        });

        // Select Date when adding transactions
        addBinding.date.setOnClickListener(view -> {
            DatePickerDialog datePicker = new DatePickerDialog(getContext());

            datePicker.setOnDateSetListener((datePicker1, day, month, year) -> {
                Calendar calendar = Calendar.getInstance();

                calendar.set(Calendar.DAY_OF_MONTH, datePicker1.getDayOfMonth());
                calendar.set(Calendar.MONTH, datePicker1.getMonth());
                calendar.set(Calendar.YEAR, datePicker1.getYear());

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                String dateToshow = simpleDateFormat.format(calendar.getTime());

                addBinding.date.setText(dateToshow);
            });

            datePicker.show();
        });

        // Select category when adding Transactions
        // Select category when adding Transactions
        addBinding.category.setOnClickListener(view -> {
            // Get categories from database
            categoryViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {

                if (categories != null && !categories.isEmpty()) {
                    ListItemDialogBinding listItemDialog = ListItemDialogBinding.inflate(inflater);
                    AlertDialog categoryAlertDialog = new AlertDialog.Builder(getContext()).create();
                    categoryAlertDialog.setView(listItemDialog.getRoot());

                    // Prepare ArrayList and ensure it is cleared, then add unique category names only
                    ArrayList<CategoryModel> categoryArrayList = new ArrayList<>();
                    java.util.HashSet<String> seenNames = new java.util.HashSet<>();
                    for (CategoryModel c : categories) {
                        if (c != null && c.getCategoryName() != null && seenNames.add(c.getCategoryName())) {
                            categoryArrayList.add(c);
                        }
                    }

                    categoryAdapter = new CategoryAdapter(getContext(), categoryArrayList, category -> {
                        addBinding.category.setText(category.getCategoryName());
                        transModel.setCategory(category.getCategoryName());
                        categoryAlertDialog.dismiss();
                    });

                    listItemDialog.categoryItems.setLayoutManager(new GridLayoutManager(getContext(), 3));
                    listItemDialog.categoryItems.setAdapter(categoryAdapter);

                    categoryAlertDialog.show();
                } else {
                    Toast.makeText(getContext(), "No categories available. Please add categories first.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Setup account spinner
        setupAccountSpinner();

        addBinding.saveTransactionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String amountStr = addBinding.amount.getText().toString().trim();
                String date = addBinding.date.toString();
                String note = addBinding.note.getText().toString().trim();
                String category = addBinding.category.getText().toString();
                String accountName = addBinding.account.getText().toString();


                // Validation inputs
                if (amountStr.isEmpty()) {
                    addBinding.amount.setError("Amount is required");
                    return;
                }

                double amount;
                try {
                    amount = Double.parseDouble(amountStr);
                    if (amount <= 0) {
                        addBinding.amount.setError("Amount must be greater than 0");
                        return;
                    }
                } catch (NumberFormatException e) {
                    addBinding.amount.setError("Invalid amount");
                    return;
                }



                // Get Transaction Type and normalize to uppercase for storage/queries
                String type = Constant.EXPENSE.equalsIgnoreCase(transModel.getType()) ? "EXPENSE" : "INCOME";

                Date transactionDate = selectedDate != null ? selectedDate : new Date();

                Calendar selectedDate = Calendar.getInstance();
                selectedDate.setTime(transactionDate);

                Calendar currentDate = Calendar.getInstance();
                selectedDate.set(Calendar.HOUR_OF_DAY, currentDate.get(Calendar.HOUR_OF_DAY));
                selectedDate.set(Calendar.MINUTE, currentDate.get(Calendar.MINUTE));
                selectedDate.set(Calendar.SECOND, currentDate.get(Calendar.SECOND));
                selectedDate.set(Calendar.MILLISECOND, currentDate.get(Calendar.MILLISECOND));

                // Get account ID from selected account (fallback to first available account)
                int accountId = 0;
                if (selectedAccount != null) {
                    accountId = selectedAccount.getAccountId();
                } else if (!accountList.isEmpty()) {
                    selectedAccount = accountList.get(0);
                    accountId = selectedAccount.getAccountId();
                    addBinding.account.setText(String.format("%s (%s %.0f)",
                            selectedAccount.getAccountName(),
                            selectedAccount.getCurrencySymbol(),
                            selectedAccount.getBalance()));
                }

                // Create Transaction
                TransactionModel transactionModel = new TransactionModel(type, category, amount, note, selectedDate.getTime(), accountId);

                // Use the local transViewModel instead of casting to MainActivity
                transViewModel.addNewTrans(transactionModel);
                transViewModel.getTransactionList();
                dismiss();
            }
        });


        return addBinding.getRoot();
    }

    public void setSelectedDate(Date selectedDate){
        this.selectedDate = selectedDate;
    }



    private void setupAccountSpinner() {
        // Get accounts from ViewModel
        transViewModel.getAccountViewModel().getAllAccounts().observe(getViewLifecycleOwner(), accounts -> {
            if (accounts != null && !accounts.isEmpty()) {
                accountList.clear();
                accountList.addAll(accounts);
                // Preselect first account if none selected yet
                if (selectedAccount == null) {
                    selectedAccount = accountList.get(0);
                    addBinding.account.setText(String.format("%s (%s %.0f)",
                            selectedAccount.getAccountName(),
                            selectedAccount.getCurrencySymbol(),
                            selectedAccount.getBalance()));
                }
            }
        });

        // Handle account selection via dialog
        addBinding.account.setOnClickListener(v -> showAccountSelectionDialog());
    }

    private void showAccountSelectionDialog() {
        if (accountList.isEmpty()) {
            Toast.makeText(getContext(), "No accounts available", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] accountNames = new String[accountList.size()];
        for (int i = 0; i < accountList.size(); i++) {
            AccountModel account = accountList.get(i);
            accountNames[i] = String.format("%s (%s %.0f)",
                    account.getAccountName(),
                    account.getCurrencySymbol(),
                    account.getBalance());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Account");
        builder.setItems(accountNames, (dialog, which) -> {
            selectedAccount = accountList.get(which);
            addBinding.account.setText(accountNames[which]);
        });
        builder.show();
    }

}

