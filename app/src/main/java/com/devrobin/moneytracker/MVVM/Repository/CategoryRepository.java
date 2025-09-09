package com.devrobin.moneytracker.MVVM.Repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.devrobin.moneytracker.MVVM.DAO.CategoryDAO;
import com.devrobin.moneytracker.MVVM.Model.CategoryModel;
import com.devrobin.moneytracker.MVVM.TransactionDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryRepository {

    private CategoryDAO categoryDAO;
    private LiveData<List<CategoryModel>> allCategories;

    public CategoryRepository(Application application) {
        TransactionDatabase database = TransactionDatabase.getInstance(application);
        categoryDAO = database.categoryDao();
        allCategories = categoryDAO.getAllCategories();
    }

    public void insertCategory(CategoryModel categoryModel) {
        TransactionDatabase.databaseWriteExecutor.execute(() -> {
            categoryDAO.insertCategory(categoryModel);
        });
    }

    public void updateCategory(CategoryModel categoryModel) {
        TransactionDatabase.databaseWriteExecutor.execute(() -> {
            categoryDAO.updateCategory(categoryModel);
        });
    }

    public void deleteCategory(CategoryModel categoryModel) {
        TransactionDatabase.databaseWriteExecutor.execute(() -> {
            categoryDAO.deleteCategory(categoryModel);
        });
    }

    public void deleteAllCategories() {
        TransactionDatabase.databaseWriteExecutor.execute(() -> {
            categoryDAO.deleteAllCategories();
        });

        ExecutorService executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<CategoryModel>> getAllCategories() {
        return allCategories;
    }

    public LiveData<CategoryModel> getCategoryById(int categoryId) {
        return categoryDAO.getCategoryById(categoryId);
    }

    public LiveData<List<CategoryModel>> getDefaultCategories() {
        return categoryDAO.getDefaultCategories();
    }

    public LiveData<List<CategoryModel>> getCustomCategories() {
        return categoryDAO.getCustomCategories();
    }
}
