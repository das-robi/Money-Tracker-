package com.devrobin.moneytracker.MVVM.Repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import com.devrobin.moneytracker.MVVM.DAO.AccountDAO;
import com.devrobin.moneytracker.MVVM.Model.AccountModel;
import com.devrobin.moneytracker.MVVM.TransactionDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AccountRepository {

    private AccountDAO accountDao;
    private LiveData<List<AccountModel>> allAccounts;
    private LiveData<Double> totalBalance;

    public AccountRepository(Application application) {
        TransactionDatabase database = TransactionDatabase.getInstance(application);
        accountDao = database.accountDao();
        allAccounts = accountDao.getAllAccounts();
        totalBalance = accountDao.getTotalBalance();
    }

    public void insertAccount(AccountModel accountModel) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> accountDao.insertAccount(accountModel));
    }

    public void updateAccount(AccountModel accountModel) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> accountDao.updateAccount(accountModel));
    }

    public void deleteAccount(AccountModel accountModel) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> accountDao.deleteAccount(accountModel));
    }

    public LiveData<List<AccountModel>> getAllAccounts() {
        return allAccounts;
    }

    public LiveData<AccountModel> getAccountById(int accountId) {
        return accountDao.getAccountById(accountId);
    }

    public LiveData<Double> getTotalBalance() {
        return totalBalance;
    }

    public void updateAccountBalance(int accountId, double amount) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> accountDao.updateAccountBalance(accountId, amount));
    }

    public void deleteAllAccounts() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> accountDao.deleteAllAccounts());
    }
}