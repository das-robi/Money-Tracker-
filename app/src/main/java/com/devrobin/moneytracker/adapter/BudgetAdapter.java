package com.devrobin.moneytracker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devrobin.moneytracker.MVVM.Model.BudgetModel;
import com.devrobin.moneytracker.R;
import com.devrobin.moneytracker.databinding.BudgetListItemsBinding;

import java.util.ArrayList;
import java.util.Locale;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>{

    private Context context;
    private ArrayList<BudgetModel> budgetList;

    private onBudgetItemClickListener itemClickListener;
    private onEditClickListener editClickListener;

    public BudgetAdapter(Context context, ArrayList<BudgetModel> budgetList, onBudgetItemClickListener itemClickListener) {
        this.context = context;
        this.budgetList = budgetList;
        this.itemClickListener = itemClickListener;
    }

    public void setEditClickListener(onEditClickListener editClickListener) {
        this.editClickListener = editClickListener;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        BudgetListItemsBinding binding = BudgetListItemsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new BudgetViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {

        BudgetModel budgetModel = budgetList.get(position);

        holder.itemsBinding.tvCategory.setText(budgetModel.getCategory());
        holder.itemsBinding.tvFrequency.setText(budgetModel.getBudgetType());
        holder.itemsBinding.tvAmount.setText(String.format(Locale.getDefault(), "৳ %.0f", budgetModel.getBudgetAmount()));

        // Set spent amount
        holder.itemsBinding.tvSpent.setText(String.format(Locale.getDefault(), "Spent: ৳ %.0f", budgetModel.getSpentAmount()));

        // Calculate progress
        double progress = budgetModel.getProgressPercentage();
        holder.itemsBinding.progressBar.setProgress((int) progress);
        holder.itemsBinding.tvProgressPercentage.setText(String.format(Locale.getDefault(), "%.0f%%", progress));

        // Set remaining amount
        double remaining = budgetModel.getRemaining();
        holder.itemsBinding.tvRemaining.setText(String.format(Locale.getDefault(), "Remaining: ৳ %.0f", remaining));

        // Set status indicator and text
        setBudgetStatus(holder, progress, remaining);

        // Set category icon
        setCategoryIcon(holder, budgetModel.getCategory());

        // Budget item click - entire card is clickable
        holder.itemView.setOnClickListener(view -> {
            if (itemClickListener != null) {
                itemClickListener.budgetItemClick(budgetModel);
            }
        });

        // Edit button click - separate from item click
        holder.itemsBinding.btnEdit.setOnClickListener(view -> {
            if (editClickListener != null) {
                editClickListener.onEditClick(budgetModel);
            }
        });
    }

    private void setBudgetStatus(BudgetViewHolder holder, double progress, double remaining) {
        if (progress >= 90) {
            // Over budget or very close
            holder.itemsBinding.statusIndicator.setBackgroundResource(R.drawable.status_indicator_over);
            holder.itemsBinding.tvStatus.setText("Over Budget");
            holder.itemsBinding.tvStatus.setTextColor(context.getResources().getColor(R.color.red));
            holder.itemsBinding.progressBar.setProgressTintList(context.getColorStateList(R.color.red));
        } else if (progress >= 75) {
            // Warning zone
            holder.itemsBinding.statusIndicator.setBackgroundResource(R.drawable.status_indicator_warning);
            holder.itemsBinding.tvStatus.setText("Warning");
            holder.itemsBinding.tvStatus.setTextColor(context.getResources().getColor(R.color.yellow));
            holder.itemsBinding.progressBar.setProgressTintList(context.getColorStateList(R.color.yellow));
        } else {
            // Good status
            holder.itemsBinding.statusIndicator.setBackgroundResource(R.drawable.status_indicator_good);
            holder.itemsBinding.tvStatus.setText("On Track");
            holder.itemsBinding.tvStatus.setTextColor(context.getResources().getColor(R.color.gray));
            holder.itemsBinding.progressBar.setProgressTintList(context.getColorStateList(R.color.blue));
        }
    }

    private void setCategoryIcon(BudgetViewHolder holder, String category) {
        // Set different icons based on category
        if (category.toLowerCase().contains("food")) {
            holder.itemsBinding.categoryIcon.setImageResource(R.drawable.food);
        } else if (category.toLowerCase().contains("transport")) {
            holder.itemsBinding.categoryIcon.setImageResource(R.drawable.home);
        } else if (category.toLowerCase().contains("entertainment")) {
            holder.itemsBinding.categoryIcon.setImageResource(R.drawable.profile);
        } else if (category.toLowerCase().contains("shopping")) {
            holder.itemsBinding.categoryIcon.setImageResource(R.drawable.reports);
        } else if (category.toLowerCase().contains("health")) {
            holder.itemsBinding.categoryIcon.setImageResource(R.drawable.user);
        } else if (category.toLowerCase().contains("education")) {
            holder.itemsBinding.categoryIcon.setImageResource(R.drawable.charts);
        } else if (category.toLowerCase().contains("bills")) {
            holder.itemsBinding.categoryIcon.setImageResource(R.drawable.calender);
        } else {
            holder.itemsBinding.categoryIcon.setImageResource(R.drawable.food);
        }
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    public class BudgetViewHolder extends RecyclerView.ViewHolder{

        BudgetListItemsBinding itemsBinding;

        public BudgetViewHolder(@NonNull BudgetListItemsBinding itemsBinding) {
            super(itemsBinding.getRoot());
            this.itemsBinding = itemsBinding;
        }
    }

    public interface onBudgetItemClickListener{
        void budgetItemClick(BudgetModel budgetModel);
    }

    public interface onEditClickListener {
        void onEditClick(BudgetModel budgetModel);
    }

    public void setBudgetList(ArrayList<BudgetModel> budgetList) {
        this.budgetList = budgetList;
        notifyDataSetChanged();
    }
}