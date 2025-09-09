package com.devrobin.moneytracker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devrobin.moneytracker.MVVM.Model.MonthlyReportData;
import com.devrobin.moneytracker.R;

import java.util.List;

public class MonthlyReportAdapter extends RecyclerView.Adapter<MonthlyReportAdapter.MonthlyReportViewHolder> {

    private Context context;
    private List<MonthlyReportData> monthlyReportsList;
    private OnMonthlyReportClickListener clickListener;

    public interface OnMonthlyReportClickListener {
        void onMonthlyReportClick(MonthlyReportData monthlyReport);
    }

    public MonthlyReportAdapter(Context context, List<MonthlyReportData> monthlyReportsList, OnMonthlyReportClickListener clickListener) {
        this.context = context;
        this.monthlyReportsList = monthlyReportsList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public MonthlyReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_monthly_report, parent, false);
        return new MonthlyReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthlyReportViewHolder holder, int position) {
        MonthlyReportData monthlyReport = monthlyReportsList.get(position);

        if (monthlyReport != null) {
            // Set month name and year
            holder.tvMonthName.setText(monthlyReport.getMonthName());
            holder.tvYear.setText(String.valueOf(monthlyReport.getYear()));

            // Set financial data
            holder.tvExpenses.setText(monthlyReport.getFormattedExpense());
            holder.tvIncome.setText(monthlyReport.getFormattedIncome());
            holder.tvBalance.setText(monthlyReport.getFormattedBalance());

            // Set click listener for the entire card
            holder.itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onMonthlyReportClick(monthlyReport);
                }
            });

            // Set click listener for chevron icon
            holder.ivChevron.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onMonthlyReportClick(monthlyReport);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return monthlyReportsList != null ? monthlyReportsList.size() : 0;
    }

    /**
     * Update the data list
     */
    public void updateData(List<MonthlyReportData> newData) {
        this.monthlyReportsList = newData;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for monthly report items
     */
    public static class MonthlyReportViewHolder extends RecyclerView.ViewHolder {

        TextView tvMonthName, tvYear, tvExpenses, tvIncome, tvBalance;
        ImageView ivChevron;

        public MonthlyReportViewHolder(@NonNull View itemView) {
            super(itemView);

            tvMonthName = itemView.findViewById(R.id.tvMonthName);
            tvYear = itemView.findViewById(R.id.tvYear);
            tvExpenses = itemView.findViewById(R.id.tvExpenses);
            tvIncome = itemView.findViewById(R.id.tvIncome);
            tvBalance = itemView.findViewById(R.id.tvBalance);
            ivChevron = itemView.findViewById(R.id.ivChevron);
        }
    }
}