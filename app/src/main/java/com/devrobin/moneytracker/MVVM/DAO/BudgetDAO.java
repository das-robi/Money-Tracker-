package com.devrobin.moneytracker.MVVM.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.devrobin.moneytracker.MVVM.Model.BudgetModel;

import java.util.List;

@Dao
public interface BudgetDAO {

    @Insert
    void insertBudget(BudgetModel budgetModel);

    @Update
    void updateBudget(BudgetModel budgetModel);

    @Delete
    void deleteBudget(BudgetModel budgetModel);

    @Query("SELECT * FROM budget_table ORDER BY year DESC, month DESC, day DESC")
    LiveData<List<BudgetModel>> getAllBudgets();

    @Query("SELECT * FROM budget_table ORDER BY year DESC, month DESC, day DESC")
    List<BudgetModel> getAllBudgetsSync();

    @Query("SELECT * FROM budget_table WHERE budgetId = :budgetId")
    LiveData<BudgetModel> getBudgetById(int budgetId);

    @Query("SELECT * FROM budget_table WHERE budgetType = :budgetType")
    LiveData<List<BudgetModel>> getBudgetsByType(String budgetType);

    @Query("SELECT * FROM budget_table WHERE category = :category")
    LiveData<List<BudgetModel>> getBudgetsByCategory(String category);

    @Query("SELECT * FROM budget_table WHERE category = :category")
    List<BudgetModel> getBudgetsByCategorySync(String category);

    @Query("UPDATE budget_table SET spentAmount = spentAmount + :amount WHERE budgetId = :budgetId")
    void updateBudgetSpent(int budgetId, double amount);

    @Query("DELETE FROM budget_table")
    void deleteAllBudgets();

    // Additional queries for better budget management
    @Query("SELECT * FROM budget_table WHERE year = :year AND month = :month")
    LiveData<List<BudgetModel>> getBudgetsByMonth(int year, int month);

    @Query("SELECT * FROM budget_table WHERE year = :year")
    LiveData<List<BudgetModel>> getBudgetsByYear(int year);

    @Query("SELECT SUM(budgetAmount) FROM budget_table WHERE year = :year AND month = :month")
    LiveData<Double> getTotalBudgetForMonth(int year, int month);

    @Query("SELECT SUM(spentAmount) FROM budget_table WHERE year = :year AND month = :month")
    LiveData<Double> getTotalSpentForMonth(int year, int month);
}
