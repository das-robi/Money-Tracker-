package com.devrobin.moneytracker.Views.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.devrobin.moneytracker.MVVM.MainViewModel.AccountViewModel;
import com.devrobin.moneytracker.MVVM.MainViewModel.CurrencyViewModel;
import com.devrobin.moneytracker.MVVM.Model.AccountModel;
import com.devrobin.moneytracker.R;
import com.devrobin.moneytracker.databinding.ActivityAddAndEditAccountBinding;

import java.util.ArrayList;
import java.util.List;

import utils.CurrencyConverter;

public class AddAndEditAccount extends AppCompatActivity {

    private ActivityAddAndEditAccountBinding editAccountBinding;
    private AccountViewModel accountViewModel;
    private AccountModel currentAccount;
    private boolean isEditMode = false;
    private CurrencyViewModel currencyViewModel;


    private String selectedType = "";
    private String selectedCurrency = "";
    private int selectedIconId = 0;

    private List<String> accountType = new ArrayList<>();
    private List<String> currencies = new ArrayList<>();
    private List<Integer> iconIds = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        editAccountBinding = ActivityAddAndEditAccountBinding.inflate(getLayoutInflater());
        setContentView(editAccountBinding.getRoot());

        // Initialize ViewModel
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
        currencyViewModel = new ViewModelProvider(this).get(CurrencyViewModel.class);

        // Initialize CurrencyConverter
        CurrencyConverter.init(this);

        // Check if existing account
        int accountId = getIntent().getIntExtra("account_id", -1);
        if (accountId != -1){
            isEditMode = true;
            loadAccount(accountId);
        }

        setupData();
        setupViews();
        setupClickListeners();
        setupIconGrid();
    }


    private void setupData() {

        // Account types
        accountType.add("Card");
        accountType.add("Credit Card");
        accountType.add("Debit Card");
        accountType.add("Bank Account");
        accountType.add("Savings Account");
        accountType.add("Investment Account");

        // Currencies - will be populated dynamically from CurrencyViewModel
        loadCurrenciesFromAPI();

        // Account types
        currencies.add("BDT (৳) Bangladeshi Taka");
        currencies.add("USD ($) US Dollar");
        currencies.add("EUR (€) Euro");
        currencies.add("GBP (£) British Pound");
        currencies.add("JPY (¥) Japanese Yen");
        currencies.add("CAD ($) Canadian Dollar");


        // Icon IDs (you can add more icons as needed)
        iconIds.add(R.drawable.creditcard);
        iconIds.add(R.drawable.deviit);
        iconIds.add(R.drawable.bank);
        iconIds.add(R.drawable.bank);
    }

    private void setupViews() {

        // Set title based on mode
        if (isEditMode){
            editAccountBinding.tvTitle.setText(R.string.edit);
            editAccountBinding.btnDelete.setVisibility(View.VISIBLE);
        }
        else {
            editAccountBinding.tvTitle.setText(R.string.add_account);
            editAccountBinding.btnDelete.setVisibility(View.GONE);
        }

    }

    private void setupClickListeners() {

        // Cancel button
        editAccountBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // Save button
        editAccountBinding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAccount();
            }
        });

        // Delete button
        editAccountBinding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAccount();
            }
        });

        // Type dropdown
        editAccountBinding.btnTypeDropdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTypeDialog();
            }
        });

        // Currency dropdown
        editAccountBinding.btnCurrencyDropdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCurrencyDialog();
            }
        });

    }

    private void loadCurrenciesFromAPI() {
        currencies.clear();

        // Get supported currencies from CurrencyViewModel
        String[] supportedCurrencies = currencyViewModel.getSupportedCurrencies();

        for (String currencyCode : supportedCurrencies) {
            String displayName = currencyViewModel.getCurrencyDisplayName(currencyCode);
            String symbol = currencyViewModel.getCurrencySymbol(currencyCode);
            currencies.add(currencyCode + " (" + symbol + ") " + displayName);
        }

        // Fetch latest exchange rates
        currencyViewModel.fetchLatestRates();
    }


    private void setupIconGrid() {

        editAccountBinding.iconGrid.removeAllViews();

        for (int i = 0; i < iconIds.size(); i++) {
            ImageView iconView = new ImageView(this);
            int size = getResources().getDimensionPixelSize(R.dimen.icon_size);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(8, 8, 8, 8);
            iconView.setLayoutParams(params);

            iconView.setImageResource(iconIds.get(i));
            iconView.setBackgroundResource(R.drawable.categoryicons_bg);
            iconView.setPadding(16, 16, 16, 16);

            final int iconId = iconIds.get(i);
            iconView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectIcon(iconId, iconView);
                }
            });

            editAccountBinding.iconGrid.addView(iconView);
        }
    }

    private void selectIcon(int iconId, ImageView selectedView) {

        // Reset all icons
        for (int i = 0; i < editAccountBinding.iconGrid.getChildCount(); i++){

            View child = editAccountBinding.iconGrid.getChildAt(i);
            child.setBackgroundResource(R.drawable.categoryicons_bg);

        }

        // Highlight selected Icon
        selectedView.setBackgroundResource(R.drawable.selected_chip_bg);
        selectedIconId = iconId;
    }

    private void showTypeDialog() {

        String[] types = accountType.toArray(new String[0]);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Select Account Type");
        alertDialogBuilder.setItems(types, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {

                selectedType = types[which];
                editAccountBinding.tvSelectedType.setText(selectedType);

            }
        });

        alertDialogBuilder.show();

    }



    private void showCurrencyDialog() {
        String[] currencyArray = currencies.toArray(new String[0]);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("Select Currency");
        alertBuilder.setItems(currencyArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                selectedCurrency = currencyArray[which];
                editAccountBinding.tvSelectedCurrency.setText(selectedCurrency);
            }
        });

        alertBuilder.show();

    }

    private void loadAccount(int accountId) {
        accountViewModel.getAccountById(accountId).observe(this, accountModel -> {
            if (accountModel != null) {
                currentAccount = accountModel;
                editAccountBinding.etAccountName.setText(accountModel.getAccountName());
                editAccountBinding.tvSelectedType.setText(accountModel.getCardType());
                editAccountBinding.tvSelectedCurrency.setText(accountModel.getCurrency());
                editAccountBinding.etAmount.setText(String.valueOf((int) accountModel.getBalance()));
                editAccountBinding.etNote.setText(accountModel.getNote());

                selectedType = accountModel.getCardType();
                selectedCurrency = accountModel.getCurrency();
                selectedIconId = accountModel.getIconId();

                // Select the icon
                for (int i = 0; i < editAccountBinding.iconGrid.getChildCount(); i++) {
                    View child = editAccountBinding.iconGrid.getChildAt(i);
                    if (child instanceof ImageView) {
                        ImageView iconView = (ImageView) child;
                        if (iconIds.get(i) == selectedIconId) {
                            selectIcon(selectedIconId, iconView);
                            break;
                        }
                    }
                }
            }
        });
    }



    private void saveAccount() {
        String accountName = editAccountBinding.etAccountName.getText().toString().trim();
        String amountStr = editAccountBinding.etAmount.getText().toString().trim();
        String note = editAccountBinding.etNote.getText().toString().trim();

        // Validation
        if (accountName.isEmpty()) {
            editAccountBinding.etAccountName.setError("Please enter account name");
            return;
        }

        if (selectedType.isEmpty()) {
            Toast.makeText(this, "Please select account type", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCurrency.isEmpty()) {
            Toast.makeText(this, "Please select currency", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amountStr.isEmpty()) {
            editAccountBinding.etAmount.setError("Please enter amount");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount < 0) {
                editAccountBinding.etAmount.setError("Amount cannot be negative");
                return;
            }
        } catch (NumberFormatException e) {
            editAccountBinding.etAmount.setError("Invalid amount");
            return;
        }

        // Extract currency code from full name
        String currencyCode = selectedCurrency.split(" ")[0];

        if (isEditMode && currentAccount != null) {
            // Update existing account
            currentAccount.setAccountName(accountName);
            currentAccount.setCardType(selectedType);
            currentAccount.setCurrency(currencyCode);
            currentAccount.setBalance(amount);
            currentAccount.setNote(note);
            currentAccount.setIconId(selectedIconId);
            accountViewModel.updateAccount(currentAccount);
            Toast.makeText(this, "Account updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            // Create new account
            AccountModel newAccount = new AccountModel(accountName, selectedType, currencyCode, amount, note, selectedIconId);
            accountViewModel.insertAccount(newAccount);
            Toast.makeText(this, "Account added successfully", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private void deleteAccount() {
        if (currentAccount != null) {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete this account?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        accountViewModel.deleteAccount(currentAccount);
                        Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}