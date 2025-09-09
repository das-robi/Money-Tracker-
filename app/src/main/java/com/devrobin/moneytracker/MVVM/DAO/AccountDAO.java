package com.devrobin.moneytracker.MVVM.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.devrobin.moneytracker.MVVM.Model.AccountModel;

import java.util.List;

@Dao
public interface AccountDAO {

    @Insert
    void insertAccount(AccountModel accountModel);

    @Update
    void updateAccount(AccountModel accountModel);

    @Delete
    void deleteAccount(AccountModel accountModel);

    @Query("SELECT * FROM account_table ORDER BY accountName ASC")
    LiveData<List<AccountModel>> getAllAccounts();

    @Query("SELECT * FROM account_table ORDER BY accountName ASC")
    List<AccountModel> getAllAccountsSync();

    @Query("SELECT * FROM account_table WHERE accountId = :accountId")
    LiveData<AccountModel> getAccountById(int accountId);

    @Query("SELECT * FROM account_table WHERE accountId = :accountId")
    AccountModel getAccountByIdSync(int accountId);

    @Query("SELECT SUM(balance) FROM account_table")
    LiveData<Double> getTotalBalance();

    @Query("UPDATE account_table SET balance = balance + :amount WHERE accountId = :accountId")
    void updateAccountBalance(int accountId, double amount);

    @Query("DELETE FROM account_table")
    void deleteAllAccounts();

    @Query("SELECT COUNT(*) FROM account_table")
    int getAccountCountSync();
}
