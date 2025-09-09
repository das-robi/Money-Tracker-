package com.devrobin.moneytracker.Views.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.devrobin.moneytracker.MVVM.MainViewModel.AccountViewModel;
import com.devrobin.moneytracker.MVVM.Model.AccountModel;
import com.devrobin.moneytracker.R;
import com.devrobin.moneytracker.adapter.AccountAdapter;
import com.devrobin.moneytracker.databinding.ActivityAccountManagmentBinding;
import com.devrobin.moneytracker.databinding.DialogAddEditAccountBinding;
import com.devrobin.moneytracker.databinding.FragmentAccountManagementBinding;

import java.util.ArrayList;
import java.util.List;

import utils.CurrencyConverter;
import utils.SharedPrefsManager;

public class AccountManagement extends AppCompatActivity {

    private ActivityAccountManagmentBinding accountBinding;
    private AccountViewModel accountViewModel;
    private AccountAdapter accountAdapter;
    private ArrayList<AccountModel> accountList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        accountBinding = ActivityAccountManagmentBinding.inflate(getLayoutInflater());
        setContentView(accountBinding.getRoot());

        // Initialize ViewModel
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        setupRecyclerView();
        setupClickListeners();
        observeData();
    }

    private void setupRecyclerView() {
        accountList = new ArrayList<>();
        accountAdapter = new AccountAdapter(this, accountList, new AccountAdapter.onAccountItemClickListener() {
            @Override
            public void accountItemClick(AccountModel accountModel) {
                // Handle account item click if needed
            }
        });

        accountBinding.accountsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        accountBinding.accountsRecyclerView.setAdapter(accountAdapter);

        // Set edit and delete click listeners
        accountAdapter.setEditClickListener(new AccountAdapter.onEditClickListener() {
            @Override
            public void onEditClick(AccountModel accountModel) {
                openEditAccount(accountModel);
            }
        });

        accountAdapter.setDeleteClickListener(new AccountAdapter.onDeleteClickListener() {
            @Override
            public void onDeleteClick(AccountModel accountModel) {
                showDeleteConfirmationDialog(accountModel);
            }
        });
    }

    private void setupClickListeners() {
        // Back button
        accountBinding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Add Account button
        accountBinding.btnAddAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddAccount();
            }
        });
    }

    private void observeData() {
        // Observe all accounts
        accountViewModel.getAllAccounts().observe(this, accounts -> {
            if (accounts != null) {
                accountList.clear();
                accountList.addAll(accounts);
                accountAdapter.notifyDataSetChanged();
                updateTotalBalance(accounts);
            }
        });
    }

    private void updateTotalBalance(List<AccountModel> accounts) {

        // Initialize converter and read default currency
        CurrencyConverter.init(this);
        String defaultCurrency = SharedPrefsManager.getInstance(this).getDefaultCurrency();

        double totalBalanceDefault = 0.0;
        for (AccountModel account : accounts) {
            double amount = account.getBalance();
            String fromCurrency = account.getCurrency();
            double converted = CurrencyConverter.convert(amount, fromCurrency, defaultCurrency);
            totalBalanceDefault += converted;
        }

        // Update total balance display in default currency
        String symbol = utils.CurrencyConverter.getCurrencySymbol(defaultCurrency);
        accountBinding.tvTotalBalance.setText(String.format(symbol + " %.0f", totalBalanceDefault));
        accountBinding.tvTotalAccounts.setText(accounts.size() + " Accounts");
    }

    private void openAddAccount() {
        Intent intent = new Intent(this, AddAndEditAccount.class);
        startActivity(intent);
    }

    private void openEditAccount(AccountModel accountModel) {
        Intent intent = new Intent(this, AddAndEditAccount.class);
        intent.putExtra("account_id", accountModel.getAccountId());
        startActivity(intent);
    }

    private void showDeleteConfirmationDialog(AccountModel accountModel) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete '" + accountModel.getAccountName() + "'? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    accountViewModel.deleteAccount(accountModel);
                    Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}