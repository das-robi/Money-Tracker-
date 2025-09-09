package com.devrobin.moneytracker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devrobin.moneytracker.MVVM.Model.BudgetVsActualData;
import com.devrobin.moneytracker.R;

import java.util.List;

import utils.CurrencyConverter;
import utils.SharedPrefsManager;

public class BudgetVsActualAdapter extends RecyclerView.Adapter<BudgetVsActualAdapter.BudgetVsActualViewHolder> {

    private Context context;
    private List<BudgetVsActualData> budgetVsActualList;
    private SharedPrefsManager prefsManager;

    public BudgetVsActualAdapter(Context context, List<BudgetVsActualData> budgetVsActualList) {
        this.context = context;
        this.budgetVsActualList = budgetVsActualList;
        this.prefsManager = SharedPrefsManager.getInstance(context);
    }

    @NonNull
    @Override
    public BudgetVsActualViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_budget_vs_actual, parent, false);
        return new BudgetVsActualViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetVsActualViewHolder holder, int position) {
        BudgetVsActualData budgetData = budgetVsActualList.get(position);
        holder.bind(budgetData);
    }

    @Override
    public int getItemCount() {
        return budgetVsActualList.size();
    }

    public void updateData(List<BudgetVsActualData> newData) {
        this.budgetVsActualList = newData;
        notifyDataSetChanged();
    }

    class BudgetVsActualViewHolder extends RecyclerView.ViewHolder {

        private TextView tvCategory, tvBudgetType, tvBudgetAmount, tvSpentAmount;
        private TextView tvRemainingAmount, tvProgressPercentage, tvStatus;
        private ProgressBar progressBar;

        public BudgetVsActualViewHolder(@NonNull View itemView) {
            super(itemView);

            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvBudgetType = itemView.findViewById(R.id.tvBudgetType);
            tvBudgetAmount = itemView.findViewById(R.id.tvBudgetAmount);
            tvSpentAmount = itemView.findViewById(R.id.tvSpentAmount);
            tvRemainingAmount = itemView.findViewById(R.id.tvRemainingAmount);
            tvProgressPercentage = itemView.findViewById(R.id.tvProgressPercentage);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            progressBar = itemView.findViewById(R.id.progressBar);
        }

        public void bind(BudgetVsActualData budgetData) {
            // Set category
            tvCategory.setText(budgetData.getCategory());

            // Set budget type
            tvBudgetType.setText(budgetData.getBudgetType());

            // Get default currency for display
            String defaultCurrency = prefsManager.getDefaultCurrency();
            String currencySymbol = CurrencyConverter.getCurrencySymbol(defaultCurrency);

            // Set budget amount
            tvBudgetAmount.setText(String.format("%s %.0f", currencySymbol, budgetData.getBudgetAmount()));

            // Set spent amount
            tvSpentAmount.setText(String.format("%s %.0f", currencySymbol, budgetData.getSpentAmount()));

            // Set remaining amount
            double remaining = budgetData.getRemainingAmount();
            tvRemainingAmount.setText(String.format("%s %.0f", currencySymbol, remaining));

            // Set progress percentage
            int progress = budgetData.getProgressPercentage();
            tvProgressPercentage.setText(progress + "%");

            // Set progress bar
            progressBar.setProgress(progress);

            // Set status and color
            String status = budgetData.getStatus();
            tvStatus.setText(status);

            // Set status color based on budget status
            if (status.equals("Over Budget")) {
                tvStatus.setTextColor(context.getResources().getColor(R.color.red));
                progressBar.setProgressTintList(context.getResources().getColorStateList(R.color.red));
            } else if (status.equals("Near Limit")) {
                tvStatus.setTextColor(context.getResources().getColor(R.color.yellow));
                progressBar.setProgressTintList(context.getResources().getColorStateList(R.color.yellow));
            } else {
                tvStatus.setTextColor(context.getResources().getColor(R.color.green));
                progressBar.setProgressTintList(context.getResources().getColorStateList(R.color.green));
            }

            // Set remaining amount color
            if (remaining >= 0) {
                tvRemainingAmount.setTextColor(context.getResources().getColor(R.color.green));
            } else {
                tvRemainingAmount.setTextColor(context.getResources().getColor(R.color.red));
            }
        }
    }
}