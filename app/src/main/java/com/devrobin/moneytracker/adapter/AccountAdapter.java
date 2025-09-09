package com.devrobin.moneytracker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devrobin.moneytracker.MVVM.Model.AccountModel;
import com.devrobin.moneytracker.R;
import com.devrobin.moneytracker.databinding.AccountListItemsBinding;

import java.util.ArrayList;
import java.util.Locale;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    private Context context;
    private ArrayList<AccountModel> accountList;

    private onAccountItemClickListener itemClickListener;
    private onEditClickListener editClickListener;
    private onDeleteClickListener deleteClickListener;

    public AccountAdapter(Context context, ArrayList<AccountModel> accountList, onAccountItemClickListener itemClickListener) {
        this.context = context;
        this.accountList = accountList;
        this.itemClickListener = itemClickListener;
    }

    public void setEditClickListener(onEditClickListener editClickListener) {
        this.editClickListener = editClickListener;
    }

    public void setDeleteClickListener(onDeleteClickListener deleteClickListener) {
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AccountViewHolder(AccountListItemsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {

        AccountModel accountModel = accountList.get(position);

        holder.itemsBinding.tvAccountName.setText(accountModel.getAccountName());
        holder.itemsBinding.tvAccountType.setText(accountModel.getCardType());
        holder.itemsBinding.tvAccountBalance.setText(String.format(Locale.getDefault(), "%s %.0f",
                accountModel.getCurrencySymbol(), accountModel.getBalance()));

        // Set account icon
        if (accountModel.getIconId() != 0) {
            holder.itemsBinding.ivAccountIcon.setImageResource(accountModel.getIconId());
        } else {
            setDefaultAccountIcon(holder, accountModel.getCardType());
        }

        // Account item click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener != null) {
                    itemClickListener.accountItemClick(accountModel);
                }
            }
        });

        // Edit button click
        holder.itemsBinding.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editClickListener != null) {
                    editClickListener.onEditClick(accountModel);
                }
            }
        });

        // Delete button click
        holder.itemsBinding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (deleteClickListener != null) {
                    deleteClickListener.onDeleteClick(accountModel);
                }
            }
        });
    }

    private void setDefaultAccountIcon(AccountViewHolder holder, String cardType) {
        switch (cardType.toLowerCase()) {
            case "cash":
                holder.itemsBinding.ivAccountIcon.setImageResource(R.drawable.food);
                break;
            case "credit card":
            case "debit card":
                holder.itemsBinding.ivAccountIcon.setImageResource(R.drawable.cards);
                break;
            case "bank account":
            case "savings account":
                holder.itemsBinding.ivAccountIcon.setImageResource(R.drawable.home);
                break;
            case "investment account":
                holder.itemsBinding.ivAccountIcon.setImageResource(R.drawable.charts);
                break;
            default:
                holder.itemsBinding.ivAccountIcon.setImageResource(R.drawable.note);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }

    public class AccountViewHolder extends RecyclerView.ViewHolder {

        AccountListItemsBinding itemsBinding;

        public AccountViewHolder(@NonNull AccountListItemsBinding itemsBinding) {
            super(itemsBinding.getRoot());
            this.itemsBinding = itemsBinding;
        }
    }

    public interface onAccountItemClickListener {
        void accountItemClick(AccountModel accountModel);
    }

    public interface onEditClickListener {
        void onEditClick(AccountModel accountModel);
    }

    public interface onDeleteClickListener {
        void onDeleteClick(AccountModel accountModel);
    }

    public void setAccountList(ArrayList<AccountModel> accountList) {
        this.accountList = accountList;
        notifyDataSetChanged();
    }

}