package com.devrobin.moneytracker.MVVM.Repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.devrobin.moneytracker.MVVM.DAO.BudgetDAO;
import com.devrobin.moneytracker.MVVM.Model.BudgetModel;
import com.devrobin.moneytracker.MVVM.TransactionDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BudgetRepository {

    private BudgetDAO budgetDao;
    private LiveData<List<BudgetModel>> allBudgets;

    public BudgetRepository(Application application) {
        TransactionDatabase database = TransactionDatabase.getInstance(application);
        budgetDao = database.budgetDao();
        allBudgets = budgetDao.getAllBudgets();
    }

    public void insertBudget(BudgetModel budgetModel) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> budgetDao.insertBudget(budgetModel));
    }

    public void updateBudget(BudgetModel budgetModel) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> budgetDao.updateBudget(budgetModel));
    }

    public void deleteBudget(BudgetModel budgetModel) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> budgetDao.deleteBudget(budgetModel));
    }

    public LiveData<List<BudgetModel>> getAllBudgets() {
        return allBudgets;
    }

    public LiveData<BudgetModel> getBudgetById(int budgetId) {
        return budgetDao.getBudgetById(budgetId);
    }

    public LiveData<List<BudgetModel>> getBudgetsByType(String budgetType) {
        return budgetDao.getBudgetsByType(budgetType);
    }

    public void updateBudgetSpent(int budgetId, double amount) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> budgetDao.updateBudgetSpent(budgetId, amount));
    }

    public void deleteAllBudgets() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> budgetDao.deleteAllBudgets());
    }

    // Additional methods for better budget management
    public LiveData<List<BudgetModel>> getBudgetsByMonth(int year, int month) {
        return budgetDao.getBudgetsByMonth(year, month);
    }

    public LiveData<List<BudgetModel>> getBudgetsByYear(int year) {
        return budgetDao.getBudgetsByYear(year);
    }

    public LiveData<Double> getTotalBudgetForMonth(int year, int month) {
        return budgetDao.getTotalBudgetForMonth(year, month);
    }

    public LiveData<Double> getTotalSpentForMonth(int year, int month) {
        return budgetDao.getTotalSpentForMonth(year, month);
    }
}

