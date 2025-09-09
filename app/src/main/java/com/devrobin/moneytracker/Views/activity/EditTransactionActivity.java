package com.devrobin.moneytracker.Views.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.devrobin.moneytracker.MVVM.MainViewModel.CategoryViewModel;
import com.devrobin.moneytracker.MVVM.MainViewModel.TransViewModel;
import com.devrobin.moneytracker.MVVM.Model.AccountModel;
import com.devrobin.moneytracker.MVVM.Model.CategoryModel;
import com.devrobin.moneytracker.MVVM.Model.TransactionModel;
import com.devrobin.moneytracker.MVVM.TransactionDatabase;
import com.devrobin.moneytracker.R;
import com.devrobin.moneytracker.adapter.CategoryAdapter;
import com.devrobin.moneytracker.databinding.ActivityEditTransactionBinding;

public class EditTransactionActivity extends AppCompatActivity {

    private TransViewModel transViewModel;
    private CategoryViewModel categoryViewModel;

    private TextView expenseBtn;
    private TextView incomeBtn;
    // Category selection is shown in a dialog; main screen uses a TextView label
    private TextView accountText;
    private TextView btnSave;
    private TextView btnCalendar;
    private TextView btnDelete;
    private EditText inputMoney;
    private EditText inputNote;

    private String selectedType = "EXPENSE";
    private CategoryModel selectedCategory;
    private AccountModel selectedAccount;
    private java.util.Date selectedDate = new java.util.Date();
    private int editingTransId = -1;
    private final java.util.ArrayList<AccountModel> accountList = new java.util.ArrayList<>();
    private final java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_edit_transaction);

        expenseBtn = findViewById(R.id.expenseBtn);
        incomeBtn = findViewById(R.id.incomeBtn);
        // Category list is opened via dialog from a label; no RecyclerView in the layout
        accountText = findViewById(R.id.account);
        btnSave = findViewById(R.id.btnSave);
        btnCalendar = findViewById(R.id.dateField);
        btnDelete = findViewById(R.id.btnDelete);
        inputMoney = findViewById(R.id.inputMoney);
        inputNote = findViewById(R.id.inputNote);

        transViewModel = new ViewModelProvider(this).get(TransViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        setupTypeButtons();
        setupCategoryList();
        setupAccountSelector();
        setupDatePicker();
        setupSaveButton();
        setupDeleteButton();
        prefillIfEditing();


        transViewModel = new ViewModelProvider(this).get(TransViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);


        setupTypeButtons();
        setupCategoryList();
        setupAccountSelector();
        setupDatePicker();
        setupSaveButton();
        setupDeleteButton();
        prefillIfEditing();

    }

    private void setupTypeButtons() {
        if (expenseBtn != null) {
            expenseBtn.setOnClickListener(v -> {
                selectedType = "EXPENSE";
                try {
                    expenseBtn.setBackgroundResource(com.devrobin.moneytracker.R.drawable.selected_chip_bg);
                    if (incomeBtn != null) incomeBtn.setBackgroundResource(com.devrobin.moneytracker.R.drawable.defaultbtn_bg);
                } catch (Throwable ignored) {}
            });
        }
        if (incomeBtn != null) {
            incomeBtn.setOnClickListener(v -> {
                selectedType = "INCOME";
                try {
                    incomeBtn.setBackgroundResource(com.devrobin.moneytracker.R.drawable.selected_chip_bg);
                    if (expenseBtn != null) expenseBtn.setBackgroundResource(com.devrobin.moneytracker.R.drawable.defaultbtn_bg);
                } catch (Throwable ignored) {}
            });
        }
    }

    private void setupCategoryList() {
        final TextView categoryText = findViewById(R.id.categoryText);
        if (categoryText == null) return;

        final java.util.ArrayList<com.devrobin.moneytracker.MVVM.Model.CategoryModel> cache = new java.util.ArrayList<>();
        CategoryAdapter adapter = new CategoryAdapter(this, cache, category -> {
            selectedCategory = category;
            categoryText.setText(category.getCategoryName());
        });

        categoryViewModel.getAllCategories().observe(this, categories -> {
            cache.clear();
            if (categories != null) cache.addAll(categories);
        });

        categoryText.setOnClickListener(v -> {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            androidx.recyclerview.widget.RecyclerView rv = new androidx.recyclerview.widget.RecyclerView(this);
            rv.setLayoutManager(new GridLayoutManager(this, 4));
            rv.setAdapter(adapter);
            builder.setView(rv);
            builder.setTitle("Select Category");
            builder.setNegativeButton("Close", (d, w) -> d.dismiss());
            builder.show();
        });
    }

    private void setupAccountSelector() {
        transViewModel.getAccountViewModel().getAllAccounts().observe(this, accounts -> {
            accountList.clear();
            if (accounts != null) accountList.addAll(accounts);
            if (selectedAccount == null && !accountList.isEmpty()) {
                selectedAccount = accountList.get(0);
                accountText.setText(selectedAccount.getAccountName());
            }
        });
        accountText.setOnClickListener(v -> showAccountSelectionDialog());
    }

    private void showAccountSelectionDialog() {
        if (accountList.isEmpty()) return;
        String[] names = new String[accountList.size()];
        for (int i = 0; i < accountList.size(); i++) {
            AccountModel a = accountList.get(i);
            names[i] = a.getAccountName();
        }
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select Account")
                .setItems(names, (d, which) -> {
                    selectedAccount = accountList.get(which);
                    accountText.setText(selectedAccount.getAccountName());
                })
                .show();
    }

    private void setupDatePicker() {
        btnCalendar.setOnClickListener(v -> {
            final java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(selectedDate != null ? selectedDate : new java.util.Date());
            DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                java.util.Calendar c = java.util.Calendar.getInstance();
                c.set(java.util.Calendar.YEAR, year);
                c.set(java.util.Calendar.MONTH, month);
                c.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth);
                if (selectedDate != null) {
                    java.util.Calendar old = java.util.Calendar.getInstance();
                    old.setTime(selectedDate);
                    c.set(java.util.Calendar.HOUR_OF_DAY, old.get(java.util.Calendar.HOUR_OF_DAY));
                    c.set(java.util.Calendar.MINUTE, old.get(java.util.Calendar.MINUTE));
                    c.set(java.util.Calendar.SECOND, old.get(java.util.Calendar.SECOND));
                    c.set(java.util.Calendar.MILLISECOND, old.get(java.util.Calendar.MILLISECOND));
                }
                selectedDate = c.getTime();
            }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH));
            dialog.show();
        });
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> {
            String note = inputNote.getText() != null ? inputNote.getText().toString() : "";
            String amountText = inputMoney.getText() != null ? inputMoney.getText().toString() : "0";
            double parsedAmount;
            try { parsedAmount = Double.parseDouble(amountText); } catch (Exception ignored) { parsedAmount = 0d; }
            String categoryName = selectedCategory != null ? selectedCategory.getCategoryName() : "Others";
            int accountId = selectedAccount != null ? selectedAccount.getAccountId() : 1;

            final String noteFinal = note;
            final double amountFinal = parsedAmount;
            final String categoryNameFinal = categoryName;
            final int accountIdFinal = accountId;
            final String selectedTypeFinal = selectedType;
            final java.util.Date selectedDateFinal = selectedDate != null ? selectedDate : new java.util.Date();

            if (editingTransId > 0) {
                executor.execute(() -> {
                    TransactionModel existing = TransactionDatabase.getInstance(getApplicationContext()).transDao().getTransactionByIdSync(editingTransId);
                    if (existing != null) {
                        existing.setType(selectedTypeFinal);
                        existing.setCategory(categoryNameFinal);
                        existing.setAmount(amountFinal);
                        existing.setNote(noteFinal);
                        existing.setAccountId(accountIdFinal);
                        existing.setTransactionDate(selectedDateFinal);
                        existing.setLastModifiedTime(System.currentTimeMillis());
                        runOnUiThread(() -> {
                            transViewModel.updateOldTrans(existing);
                            finish();
                        });
                    } else {
                        TransactionModel t = new TransactionModel(selectedTypeFinal, categoryNameFinal, amountFinal, noteFinal, selectedDateFinal, accountIdFinal);
                        runOnUiThread(() -> {
                            transViewModel.addNewTrans(t);
                            finish();
                        });
                    }
                });
            } else {
                TransactionModel t = new TransactionModel(selectedTypeFinal, categoryNameFinal, amountFinal, noteFinal, selectedDateFinal, accountIdFinal);
                transViewModel.addNewTrans(t);
                finish();
            }
        });
    }

    private void setupDeleteButton() {
        btnDelete.setOnClickListener(v -> {
            if (editingTransId <= 0) { finish(); return; }
            executor.execute(() -> {
                TransactionModel existing = TransactionDatabase.getInstance(getApplicationContext()).transDao().getTransactionByIdSync(editingTransId);
                if (existing != null) {
                    runOnUiThread(() -> {
                        transViewModel.deleteOldTrans(existing);
                        finish();
                    });
                } else {
                    runOnUiThread(this::finish);
                }
            });
        });
    }

    private void prefillIfEditing() {
        if (getIntent() == null) return;
        editingTransId = getIntent().getIntExtra("transId", -1);
        if (editingTransId > 0) {
            executor.execute(() -> {
                TransactionModel existing = TransactionDatabase.getInstance(getApplicationContext()).transDao().getTransactionByIdSync(editingTransId);
                if (existing != null) {
                    selectedDate = existing.getTransactionDate();
                    runOnUiThread(() -> {
                        selectedType = existing.getType();
                        if ("INCOME".equals(selectedType)) {
                            if (incomeBtn != null) incomeBtn.performClick();
                        } else {
                            if (expenseBtn != null) expenseBtn.performClick();
                        }
                        inputMoney.setText(String.valueOf(existing.getAmount()));
                        inputNote.setText(existing.getNote());
                        TextView categoryText = findViewById(R.id.categoryText);
                        if (categoryText != null) categoryText.setText(existing.getCategory());
                        TextView accountTv = findViewById(R.id.account);
                        try {
                            com.devrobin.moneytracker.MVVM.DAO.AccountDAO aDao = TransactionDatabase.getInstance(getApplicationContext()).accountDao();
                            com.devrobin.moneytracker.MVVM.Model.AccountModel acc = aDao.getAccountByIdSync(existing.getAccountId());
                            if (accountTv != null && acc != null) accountTv.setText(acc.getAccountName());
                        } catch (Throwable ignored) {}
                    });
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}