package com.devrobin.moneytracker.MVVM.Repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.devrobin.moneytracker.MVVM.DAO.AccountDAO;
import com.devrobin.moneytracker.MVVM.DAO.BudgetDAO;
import com.devrobin.moneytracker.MVVM.Model.AccountModel;
import com.devrobin.moneytracker.MVVM.Model.BudgetModel;
import com.devrobin.moneytracker.MVVM.Model.CategoryChartData;
import com.devrobin.moneytracker.MVVM.Model.TransactionModel;
import com.devrobin.moneytracker.MVVM.DAO.TransactionDao;
import com.devrobin.moneytracker.MVVM.TransactionDatabase;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import utils.CurrencyConverter;
import utils.DailyChartData;
import utils.DailySummer;
import utils.MonthlyChartData;
import utils.MonthlySummary;
import utils.NotificationHelper;

public class TransRepository{

    private TransactionDao transDao;
    private AccountDAO accountDao;
    private BudgetDAO budgetDao;

    private LiveData<List<TransactionModel>> allTransaction;

    public TransRepository(Application application){

        TransactionDatabase database = TransactionDatabase.getInstance(application);

        transDao = database.transDao();
        accountDao = database.accountDao();
        budgetDao = database.budgetDao();
        allTransaction = transDao.getAllTransaction();
    }


    public LiveData<List<TransactionModel>> getTransactionsByDate(Date date){

        return transDao.getTransactionByDate(date.getTime());
    }


    public LiveData<DailySummer> getDailySummer(Date date){
        return transDao.getDailySummery(date.getTime());
    }

    /**
     * Get daily summary with currency conversion to default currency
     */
    public LiveData<DailySummer> getDailySummerWithConversion(Date date, String defaultCurrency){
        androidx.lifecycle.MediatorLiveData<DailySummer> result = new androidx.lifecycle.MediatorLiveData<>();

        LiveData<List<TransactionModel>> source = transDao.getTransactionByDate(date.getTime());
        result.addSource(source, transactions -> {
            TransactionDatabase.databaseWriteExecutor.execute(() -> {
                try {
                    double totalIncome = 0.0;
                    double totalExpense = 0.0;
                    int transactionCount = transactions != null ? transactions.size() : 0;

                    if (transactions != null) {
                        for (TransactionModel transaction : transactions) {
                            double amount = transaction.getAmount();
                            String transactionCurrency = getTransactionCurrency(transaction.getAccountId());

                            if (!transactionCurrency.equals(defaultCurrency)) {
                                amount = CurrencyConverter.convert(amount, transactionCurrency, defaultCurrency);
                            }

                            if ("INCOME".equals(transaction.getType())) {
                                totalIncome += amount;
                            } else if ("EXPENSE".equals(transaction.getType())) {
                                totalExpense += amount;
                            }
                        }
                    }

                    DailySummer dailySummer = new DailySummer(totalIncome, totalExpense, transactionCount);
                    new Handler(Looper.getMainLooper()).post(() -> result.setValue(dailySummer));
                } catch (Exception e) {
                    LiveData<DailySummer> fallback = transDao.getDailySummery(date.getTime());
                    new Handler(Looper.getMainLooper()).post(() -> result.addSource(fallback, result::setValue));
                }
            });
        });

        return result;
    }

    /**
     * Get transaction currency from account
     */
    private String getTransactionCurrency(int accountId) {
        try {
            // Get account currency from database
            AccountModel account = accountDao.getAccountByIdSync(accountId);
            return account != null ? account.getCurrency() : "BDT"; // Default to BDT
        } catch (Exception e) {
            return "BDT"; // Default fallback
        }
    }

    public LiveData<MonthlySummary> getMonthlySummer(Date date){
        return transDao.getMonthlySummary(date.getTime());
    }

    /**
     * Get monthly summary with currency conversion to default currency
     */
    public LiveData<MonthlySummary> getMonthlySummaryWithConversion(Date date, String defaultCurrency){
        androidx.lifecycle.MediatorLiveData<MonthlySummary> result = new androidx.lifecycle.MediatorLiveData<>();

        // Observe all transactions and compute month summary when they change
        result.addSource(allTransaction, transactions -> {
            TransactionDatabase.databaseWriteExecutor.execute(() -> {
                try {
                    double monthlyIncome = 0.0;
                    double monthlyExpense = 0.0;
                    int monthlyTransaction = 0;

                    if (transactions != null) {
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        cal.setTime(date);
                        int y = cal.get(java.util.Calendar.YEAR);
                        int m = cal.get(java.util.Calendar.MONTH);

                        for (TransactionModel transaction : transactions) {
                            java.util.Calendar tcal = java.util.Calendar.getInstance();
                            tcal.setTime(transaction.getTransactionDate());
                            if (tcal.get(java.util.Calendar.YEAR) == y && tcal.get(java.util.Calendar.MONTH) == m) {
                                double amount = transaction.getAmount();
                                String transactionCurrency = getTransactionCurrency(transaction.getAccountId());
                                if (!transactionCurrency.equals(defaultCurrency)) {
                                    amount = CurrencyConverter.convert(amount, transactionCurrency, defaultCurrency);
                                }
                                if ("INCOME".equals(transaction.getType())) {
                                    monthlyIncome += amount;
                                } else if ("EXPENSE".equals(transaction.getType())) {
                                    monthlyExpense += amount;
                                }
                                monthlyTransaction++;
                            }
                        }
                    }

                    MonthlySummary monthlySummary = new MonthlySummary(monthlyIncome, monthlyExpense, monthlyTransaction);
                    new Handler(Looper.getMainLooper()).post(() -> result.setValue(monthlySummary));
                } catch (Exception e) {
                    LiveData<MonthlySummary> fallback = transDao.getMonthlySummary(date.getTime());
                    new Handler(Looper.getMainLooper()).post(() -> result.addSource(fallback, result::setValue));
                }
            });
        });

        return result;
    }

    public LiveData<List<TransactionModel>> getAllTransaction(){
        return transDao.getAllTransaction();
    }


    //Chart Methods
    public LiveData<List<CategoryChartData>> getCategoryChartData(Date date) {
        return transDao.getCategoryChartData(date.getTime());
    }

    public LiveData<List<DailyChartData>> getDailyChartData(Date date) {
        return transDao.getDailyChartData(date.getTime());
    }

    public LiveData<List<MonthlyChartData>> getMonthlyChartData(Date date) {
        return transDao.getMonthlyChartData(date.getTime());
    }





    //Insert Transaction
    public void InsertTrans(TransactionModel transModel){

        ExecutorService executors = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.myLooper());

        executors.execute(new Runnable() {
            @Override
            public void run() {
                transDao.insertTransaction(transModel);

                // If this is an expense, add it to the matching budget's spent amount
                if ("EXPENSE".equals(transModel.getType())) {
                    try {
                        // Find budgets for this category
                        java.util.List<BudgetModel> budgets = budgetDao.getBudgetsByCategorySync(transModel.getCategory());
                        if (budgets != null && !budgets.isEmpty()) {
                            // Simple rule: apply to the most recent budget record for that category
                            // Optionally you can filter by same month/year as transaction
                            BudgetModel target = budgets.get(0);

                            // Prefer matching by year/month of transaction if available
                            java.util.Calendar cal = java.util.Calendar.getInstance();
                            cal.setTime(transModel.getTransactionDate());
                            int tYear = cal.get(java.util.Calendar.YEAR);
                            int tMonth = cal.get(java.util.Calendar.MONTH) + 1;

                            for (BudgetModel b : budgets) {
                                if (b.getYear() == tYear && b.getMonth() == tMonth) {
                                    target = b;
                                    break;
                                }
                            }

                            budgetDao.updateBudgetSpent(target.getBudgetId(), transModel.getAmount());
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        });

    }


    //Update Transaction
    public void UpdateTrans(TransactionModel transModel){

        ExecutorService executors = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.myLooper());

        executors.execute(new Runnable() {
            @Override
            public void run() {
                transDao.updateTransaction(transModel);
            }
        });

    }


    //Delete Transaction
    public void DeleteTrans(TransactionModel transModel){

        ExecutorService executors = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.myLooper());

        executors.execute(new Runnable() {
            @Override
            public void run() {
                transDao.deleteTransaction(transModel);
            }
        });

    }

}

