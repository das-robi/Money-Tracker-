package com.devrobin.moneytracker.Views.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.devrobin.moneytracker.MVVM.MainViewModel.BudgetViewModel;
import com.devrobin.moneytracker.MVVM.Model.BudgetModel;
import com.devrobin.moneytracker.R;
import com.devrobin.moneytracker.adapter.BudgetAdapter;
import com.devrobin.moneytracker.databinding.ActivityBudgetBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import utils.NotificationHelper;

public class BudgetActivity extends AppCompatActivity {

    private ActivityBudgetBinding budgetBinding;
    private BudgetViewModel budgetViewModel;
    private BudgetAdapter budgetAdapter;
    private ArrayList<BudgetModel> budgetList;
    private NotificationHelper notificationHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);


        budgetBinding = ActivityBudgetBinding.inflate(getLayoutInflater());
        setContentView(budgetBinding.getRoot());

        // Initialize notification helper
        notificationHelper = new NotificationHelper(this);

        // Initialize ViewModel
        budgetViewModel = new ViewModelProvider(this).get(BudgetViewModel.class);

        // Initialize RecyclerView
        budgetList = new ArrayList<>();
        budgetAdapter = new BudgetAdapter(this, budgetList, budgetModel -> {
            // Handle budget item click - open edit mode
            openAddBudgetActivity(budgetModel);
        });

        // Set edit click listener
        budgetAdapter.setEditClickListener(new BudgetAdapter.onEditClickListener() {
            @Override
            public void onEditClick(BudgetModel budgetModel) {
                openAddBudgetActivity(budgetModel);
            }
        });


        // Setup RecyclerView with full width and proper layout
        budgetBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        budgetBinding.recyclerView.setAdapter(budgetAdapter);
        budgetBinding.recyclerView.setHasFixedSize(false); // Allow variable height
        budgetBinding.recyclerView.setNestedScrollingEnabled(true);

        // Setup Back buttons
        budgetBinding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // Setup add buttons
        budgetBinding.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddBudgetActivity(null);
            }
        });

        budgetBinding.btnAddBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddBudgetActivity(null);
            }
        });

        // Observe budgets
        budgetViewModel.getAllBudgets().observe(this, new Observer<List<BudgetModel>>() {
            @Override
            public void onChanged(List<BudgetModel> budgetModels) {
                budgetList.clear();
                if (budgetModels != null) {
                    budgetList.addAll(budgetModels);

                    // Check budget notifications for each budget
                    for (BudgetModel budget : budgetModels) {
                        notificationHelper.checkBudgetNotifications(budget);
                    }
                }
                budgetAdapter.notifyDataSetChanged();
                updateBudgetSummary(budgetModels);
            }
        });
    }

    private void updateBudgetSummary(List<BudgetModel> budgets) {
        if (budgets == null || budgets.isEmpty()) {
            // No budgets
            budgetBinding.tvTotalBudget.setText("৳ 0");
            budgetBinding.tvTotalSpent.setText("৳ 0");
            budgetBinding.tvRemaining.setText("৳ 0");
            budgetBinding.tvProgressPercentage.setText("0%");
            budgetBinding.progressBar.setProgress(0);
            budgetBinding.tvBudgetCount.setText("0 budgets");
            return;
        }

        // Calculate totals
        double totalBudget = 0;
        double totalSpent = 0;

        for (BudgetModel budget : budgets) {
            totalBudget += budget.getBudgetAmount();
            totalSpent += budget.getSpentAmount();
        }

        double remaining = totalBudget - totalSpent;
        double progressPercentage = totalBudget > 0 ? (totalSpent / totalBudget) * 100 : 0;

        // Update UI
        budgetBinding.tvTotalBudget.setText(String.format(Locale.getDefault(), "৳ %.0f", totalBudget));
        budgetBinding.tvTotalSpent.setText(String.format(Locale.getDefault(), "৳ %.0f", totalSpent));
        budgetBinding.tvRemaining.setText(String.format(Locale.getDefault(), "৳ %.0f", remaining));
        budgetBinding.tvProgressPercentage.setText(String.format(Locale.getDefault(), "%.0f%%", progressPercentage));
        budgetBinding.progressBar.setProgress((int) progressPercentage);
        budgetBinding.tvBudgetCount.setText(String.format(Locale.getDefault(), "%d budgets", budgets.size()));

        // Set progress bar color based on percentage
        if (progressPercentage >= 90) {
            budgetBinding.progressBar.setProgressTintList(getColorStateList(R.color.red));
        } else if (progressPercentage >= 75) {
            budgetBinding.progressBar.setProgressTintList(getColorStateList(R.color.yellow));
        } else {
            budgetBinding.progressBar.setProgressTintList(getColorStateList(R.color.white));
        }
    }

    private void openAddBudgetActivity(BudgetModel budgetModel) {
        Intent intent = new Intent(this, AddBudgetActivity.class);
        if (budgetModel != null) {
            intent.putExtra("budget_id", budgetModel.getBudgetId());
            intent.putExtra("budget_category", budgetModel.getCategory());
            intent.putExtra("budget_type", budgetModel.getBudgetType());
            intent.putExtra("budget_amount", budgetModel.getBudgetAmount());
            intent.putExtra("budget_note", budgetModel.getNote());
            Toast.makeText(this, "Editing budget: " + budgetModel.getCategory(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Adding new budget", Toast.LENGTH_SHORT).show();
        }
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh budget list when returning from AddBudgetActivity
        if (budgetViewModel != null) {
            budgetViewModel.getAllBudgets();
        }
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}