package com.devrobin.moneytracker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devrobin.moneytracker.MVVM.Model.AccountReportData;
import com.devrobin.moneytracker.R;

import java.util.List;

import utils.CurrencyConverter;
import utils.SharedPrefsManager;

public class AccountReportAdapter  extends RecyclerView.Adapter<AccountReportAdapter.AccountReportViewHolder> {

    private Context context;
    private List<AccountReportData> accountReportList;
    private SharedPrefsManager prefsManager;

    public AccountReportAdapter(Context context, List<AccountReportData> accountReportList) {
        this.context = context;
        this.accountReportList = accountReportList;
        this.prefsManager = SharedPrefsManager.getInstance(context);
    }

    @NonNull
    @Override
    public AccountReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_account_report, parent, false);
        return new AccountReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountReportViewHolder holder, int position) {
        AccountReportData accountData = accountReportList.get(position);
        holder.bind(accountData);
    }

    @Override
    public int getItemCount() {
        return accountReportList.size();
    }

    public void updateData(List<AccountReportData> newData) {
        this.accountReportList = newData;
        notifyDataSetChanged();
    }

    class AccountReportViewHolder extends RecyclerView.ViewHolder {

        private TextView tvAccountName, tvCurrency, tvIncome, tvExpense, tvNetBalance, tvCurrentBalance;

        public AccountReportViewHolder(@NonNull View itemView) {
            super(itemView);

            tvAccountName = itemView.findViewById(R.id.tvAccountName);
            tvCurrency = itemView.findViewById(R.id.tvCurrency);
            tvIncome = itemView.findViewById(R.id.tvIncome);
            tvExpense = itemView.findViewById(R.id.tvExpense);
            tvNetBalance = itemView.findViewById(R.id.tvNetBalance);
            tvCurrentBalance = itemView.findViewById(R.id.tvCurrentBalance);
        }

        public void bind(AccountReportData accountData) {
            // Set account name
            tvAccountName.setText(accountData.getAccountName());

            // Set currency
            tvCurrency.setText(accountData.getCurrency());

            // Get default currency for conversion
            String defaultCurrency = prefsManager.getDefaultCurrency();
            String currencySymbol = CurrencyConverter.getCurrencySymbol(defaultCurrency);

            // Set income with currency symbol
            if (accountData.getTotalIncome() > 0) {
                tvIncome.setText(String.format("%s %.0f", currencySymbol, accountData.getTotalIncome()));
            } else {
                tvIncome.setText(String.format("%s 0", currencySymbol));
            }

            // Set expense with currency symbol
            if (accountData.getTotalExpense() > 0) {
                tvExpense.setText(String.format("%s %.0f", currencySymbol, accountData.getTotalExpense()));
            } else {
                tvExpense.setText(String.format("%s 0", currencySymbol));
            }

            // Set net balance with currency symbol
            double netBalance = accountData.getNetBalance();
            tvNetBalance.setText(String.format("%s %.0f", currencySymbol, netBalance));

            // Set current balance with currency symbol
            tvCurrentBalance.setText(String.format("%s %.0f", currencySymbol, accountData.getCurrentBalance()));

            // Set text colors based on values
            if (netBalance >= 0) {
                tvNetBalance.setTextColor(context.getResources().getColor(R.color.green));
            } else {
                tvNetBalance.setTextColor(context.getResources().getColor(R.color.red));
            }
        }
    }
}