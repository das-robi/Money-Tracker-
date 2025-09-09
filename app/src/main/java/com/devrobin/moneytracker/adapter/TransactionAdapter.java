package com.devrobin.moneytracker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.devrobin.moneytracker.MVVM.DAO.AccountDAO;
import com.devrobin.moneytracker.MVVM.Model.AccountModel;
import com.devrobin.moneytracker.MVVM.Model.CategoryModel;
import com.devrobin.moneytracker.MVVM.Model.TransactionModel;
import com.devrobin.moneytracker.MVVM.TransactionDatabase;
import com.devrobin.moneytracker.R;
import com.devrobin.moneytracker.databinding.TransactionItemsBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import utils.Constant;
import utils.CurrencyConverter;
import utils.Helper;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private Context context;
    private ArrayList<TransactionModel> transList;
    private ExecutorService executor;
    private AccountDAO accountDAO;
    private String defaultCurrency;

    public onTransItemClickListener transItemClickListener;


    public TransactionAdapter(Context context, ArrayList<TransactionModel> transList, onTransItemClickListener transItemClickListener) {
        this.context = context;
        this.transList = transList;
        this.transItemClickListener = transItemClickListener;

        // Initialize database access
        TransactionDatabase database = TransactionDatabase.getInstance(context);
        this.accountDAO = database.accountDao();
        this.executor = Executors.newSingleThreadExecutor();

        // Initialize CurrencyConverter and get default currency
        CurrencyConverter.init(context);
        this.defaultCurrency = CurrencyConverter.getDefaultCurrency();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TransactionItemsBinding itemsBinding = TransactionItemsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TransactionViewHolder(itemsBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {

        TransactionModel transModel = transList.get(position);

        String transNote = transModel.getNote();

        if (transNote != null && !transNote.trim().isEmpty()){
            holder.itemsBinding.categoryNote.setText(transNote);
        }
        else {
            holder.itemsBinding.categoryNote.setText(transModel.getCategory());
        }

//        holder.itemsBinding.categoryAmount.setText(String.valueOf(transModel.getAmount()));
//        holder.itemsBinding.date.setText(Helper.setDateFormate(transModel.getTransactionDate()));

        //Date Formatting
        Date transactionDate = transModel.getTransactionDate();
        if (transactionDate != null){

        }

        //Set Category Icon and Background
        CategoryModel transCategory = Constant.setCategoryDetails(transModel.getCategory());

        if (transCategory != null){
            holder.itemsBinding.categoryIcons.setImageResource(transCategory.getIconId());
            holder.itemsBinding.categoryIcons.setBackgroundTintList(context.getColorStateList(R.color.blue));
        }
        else {
            holder.itemsBinding.categoryIcons.setImageResource(R.drawable.food);
            holder.itemsBinding.categoryIcons.setBackgroundTintList(context.getColorStateList(R.color.blue));
        }

        // Safe amount and type handling
        String type = transModel.getType();
        double amount = transModel.getAmount();

        // Show a preliminary formatted amount immediately to avoid placeholder flicker
        String prelimSymbol = CurrencyConverter.getCurrencySymbol(defaultCurrency);
        String prelimText;
        if ("INCOME".equals(type)) {
            prelimText = "+" + prelimSymbol + String.format("%.2f", amount);
            holder.itemsBinding.categoryAmount.setTextColor(context.getColor(R.color.blue));
        } else if ("EXPENSE".equals(type)) {
            prelimText = "-" + prelimSymbol + String.format("%.2f", amount);
            holder.itemsBinding.categoryAmount.setTextColor(context.getColor(R.color.red));
        } else {
            prelimText = prelimSymbol + String.format("%.2f", amount);
            holder.itemsBinding.categoryAmount.setTextColor(context.getColor(R.color.black));
        }
        holder.itemsBinding.categoryAmount.setText(prelimText);

        // Get account currency information
        int accountId = transModel.getAccountId();
        if (accountId > 0) {
            executor.execute(() -> {
                AccountModel account = accountDAO.getAccountByIdSync(accountId);
                if (account != null) {
                    String accountCurrency = account.getCurrency();
                    String currencySymbol = CurrencyConverter.getCurrencySymbol(accountCurrency);

                    // Format amount with currency symbol
                    String formattedAmount;
                    if ("INCOME".equals(type)) {
                        formattedAmount = "+" + currencySymbol + String.format("%.2f", amount);
                    } else if ("EXPENSE".equals(type)) {
                        formattedAmount = "-" + currencySymbol + String.format("%.2f", amount);
                    } else {
                        formattedAmount = currencySymbol + String.format("%.2f", amount);
                    }

                    // Update UI on main thread
                    holder.itemView.post(() -> {
                        holder.itemsBinding.categoryAmount.setText(formattedAmount);

                        if ("INCOME".equals(type)) {
                            holder.itemsBinding.categoryAmount.setTextColor(context.getColor(R.color.blue));
                        } else if ("EXPENSE".equals(type)) {
                            holder.itemsBinding.categoryAmount.setTextColor(context.getColor(R.color.red));
                        } else {
                            holder.itemsBinding.categoryAmount.setTextColor(context.getColor(R.color.black));
                        }

                        // Show currency info if different from default
                        if (!accountCurrency.equals(defaultCurrency)) {
                            // Convert to default currency for balance display
                            double convertedAmount = CurrencyConverter.convertToDefault(amount, accountCurrency);
                            String balanceText = String.format("%s %.2f",
                                    CurrencyConverter.getCurrencySymbol(defaultCurrency),
                                    convertedAmount);

                            holder.itemsBinding.currencyInfo.setText(balanceText);
                            holder.itemsBinding.currencyInfo.setVisibility(View.VISIBLE);
                        } else {
                            holder.itemsBinding.currencyInfo.setVisibility(View.GONE);
                        }
                    });
                }
            });
        } else {
            // Fallback for transactions without account
            String formattedAmount;
            if ("INCOME".equals(type)) {
                formattedAmount = String.format("+%.2f", amount);
                holder.itemsBinding.categoryAmount.setTextColor(context.getColor(R.color.blue));
            } else if ("EXPENSE".equals(type)) {
                formattedAmount = String.format("-%.2f", amount);
                holder.itemsBinding.categoryAmount.setTextColor(context.getColor(R.color.red));
            } else {
                formattedAmount = String.format("%.2f", amount);
                holder.itemsBinding.categoryAmount.setTextColor(context.getColor(R.color.black));
            }
            holder.itemsBinding.categoryAmount.setText(formattedAmount);
            holder.itemsBinding.currencyInfo.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return transList.size();
    }


    //ViewHolder
    public class TransactionViewHolder extends RecyclerView.ViewHolder{

        TransactionItemsBinding itemsBinding;

        public TransactionViewHolder(@NonNull TransactionItemsBinding itemsBinding) {
            super(itemsBinding.getRoot());

            this.itemsBinding = itemsBinding;

           itemsBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
               public void onClick(View view) {

                    int position = getAdapterPosition();

                    if (transItemClickListener != null && position != RecyclerView.NO_POSITION){
                        transItemClickListener.onTransItemClick(transList.get(position));
                    }

                }
            });

        }
    }



    public interface onTransItemClickListener {

        void onTransItemClick(TransactionModel transactionModel);

    }

    public void setTransItemClickListener(onTransItemClickListener transItemClickListener) {
        this.transItemClickListener = transItemClickListener;
    }

    public void setTransList(ArrayList<TransactionModel> transList) {
        this.transList = transList;
        notifyDataSetChanged();
    }


    public void cleanup() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
