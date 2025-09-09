package com.devrobin.moneytracker.MVVM;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.devrobin.moneytracker.MVVM.DAO.AccountDAO;
import com.devrobin.moneytracker.MVVM.DAO.BudgetDAO;
import com.devrobin.moneytracker.MVVM.DAO.CategoryDAO;
import com.devrobin.moneytracker.MVVM.DAO.FeedbackDAO;
import com.devrobin.moneytracker.MVVM.DAO.ReminderDAO;
import com.devrobin.moneytracker.MVVM.DAO.TransactionDao;
import com.devrobin.moneytracker.MVVM.Model.AccountModel;
import com.devrobin.moneytracker.MVVM.Model.BudgetModel;
import com.devrobin.moneytracker.MVVM.Model.CategoryModel;
import com.devrobin.moneytracker.MVVM.Model.FeedbackModel;
import com.devrobin.moneytracker.MVVM.Model.ReminderModel;
import com.devrobin.moneytracker.MVVM.Model.TransactionModel;
import com.devrobin.moneytracker.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import utils.DateConverter;

@Database(entities = {TransactionModel.class, AccountModel.class, BudgetModel.class, ReminderModel.class, CategoryModel.class, FeedbackModel.class},
        version = 10, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class TransactionDatabase extends RoomDatabase {

    public abstract TransactionDao transDao();
    public abstract AccountDAO accountDao();
    public abstract BudgetDAO budgetDao();
    public abstract ReminderDAO reminderDao();
    public abstract CategoryDAO categoryDao();
    public abstract FeedbackDAO feedbackDao();

    //SingleTon Pattern
    private static TransactionDatabase instance;

    // Database write executor
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    // Migration from version 4 to 5 (adding cardType, currency, note, iconId to account table)
    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add new columns to account_table
            database.execSQL("ALTER TABLE account_table ADD COLUMN cardType TEXT DEFAULT 'Cash'");
            database.execSQL("ALTER TABLE account_table ADD COLUMN currency TEXT DEFAULT 'BDT'");
            database.execSQL("ALTER TABLE account_table ADD COLUMN note TEXT DEFAULT ''");
            database.execSQL("ALTER TABLE account_table ADD COLUMN iconId INTEGER DEFAULT 0");
        }
    };

    // Migration from version 6 to 7 (adding reminder table)
    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Create reminder_table
            database.execSQL("CREATE TABLE reminder_table (" +
                    "reminderId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "reminderName TEXT, " +
                    "category TEXT, " +
                    "frequency TEXT, " +
                    "startDate INTEGER NOT NULL, " +
                    "reminderTime INTEGER NOT NULL, " +
                    "note TEXT, " +
                    "isActive INTEGER NOT NULL DEFAULT 1, " +
                    "createdDate INTEGER NOT NULL)");
        }
    };

    // Migration from version 7 to 8 (adding category table)
    static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Create category_table
            database.execSQL("CREATE TABLE category_table (" +
                    "categoryId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "categoryName TEXT, " +
                    "iconId INTEGER NOT NULL, " +
                    "isDefault INTEGER NOT NULL DEFAULT 0)");
        }
    };

    // Migration from version 8 to 9 (adding feedback table)
    static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS feedback_table (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "rating INTEGER NOT NULL, " +
                    "feedbackText TEXT, " +
                    "contactInfo TEXT, " +
                    "timestamp INTEGER NOT NULL)");
        }
    };

    // Migration from version 9 to 10 (add lastModifiedTime to account_table and transaction_table)
    static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            try {
                database.execSQL("ALTER TABLE account_table ADD COLUMN lastModifiedTime INTEGER NOT NULL DEFAULT 0");
            } catch (Throwable ignored) { }
            try {
                database.execSQL("ALTER TABLE transaction_table ADD COLUMN lastModifiedTime INTEGER NOT NULL DEFAULT 0");
            } catch (Throwable ignored) { }
        }
    };

    public static synchronized TransactionDatabase getInstance(Context context){

        if (instance == null){
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            TransactionDatabase.class,
                            "transaction_table")
                    .addMigrations(MIGRATION_4_5, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
                    // Do NOT wipe user data on schema mismatch; ensure migrations are added instead
                    .addCallback(roomCallback)
                    .build();
        }

        return instance;
    }

    private static final RoomDatabase.Callback roomCallback = new Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            initialData();

        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            // Ensure categories exist on every open; if table is empty, seed defaults
            databaseWriteExecutor.execute(() -> {
                try {
                    if (instance != null) {
                        CategoryDAO categoryDao = instance.categoryDao();
                        int catCount = categoryDao.getCategoryCountSync();
                        if (catCount == 0) {
                            seedDefaultCategories(categoryDao);
                        }

                        // Ensure at least some default accounts exist
                        AccountDAO accountDao = instance.accountDao();
                        int accCount = accountDao.getAccountCountSync();
                        if (accCount == 0) {
                            seedDefaultAccounts(accountDao);
                        }
                    }
                } catch (Throwable ignored) { }
            });
        }
    };

    private static void initialData() {

        AccountDAO accountDao = instance.accountDao();
        TransactionDao transDao = instance.transDao();
        BudgetDAO budgetDao = instance.budgetDao();
        ReminderDAO reminderDao = instance.reminderDao();
        CategoryDAO categoryDao = instance.categoryDao();

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(new Runnable() {
            @Override
            public void run() {

                // Create default accounts with new fields
                seedDefaultAccounts(accountDao);

                // Create sample budget
                BudgetModel foodBudget = new BudgetModel("Food", "Daily", 200.0);
                budgetDao.insertBudget(foodBudget);

                // Create sample reminders
                long currentTime = System.currentTimeMillis();
                ReminderModel sampleReminder1 = new ReminderModel("Cost of Car", "Transport", "Daily", currentTime, currentTime, "Don't forget to note your car expenses");
                ReminderModel sampleReminder2 = new ReminderModel("Cost of Transport", "Transport", "Monthly", currentTime, currentTime, "Monthly transport expense reminder");

                reminderDao.insertReminder(sampleReminder1);
                reminderDao.insertReminder(sampleReminder2);

                seedDefaultCategories(categoryDao);
            }
        });
    }

    private static void seedDefaultCategories(CategoryDAO categoryDao) {
        //  Create default categories with proper drawable resource IDs
        CategoryModel salary = new CategoryModel("Salary", R.drawable.salary, true);
        CategoryModel Business = new CategoryModel("Business", R.drawable.investment, true);
        CategoryModel Shopping = new CategoryModel("Shopping", R.drawable.shopping, true);
        CategoryModel Bike = new CategoryModel("Bike", R.drawable.bike, true);
        CategoryModel Travel = new CategoryModel("Travel", R.drawable.travel, true);
        CategoryModel Rent = new CategoryModel("Rent", R.drawable.rent, true);
        CategoryModel Electronics = new CategoryModel("Electronics", R.drawable.electronics, true);
        CategoryModel Clothing = new CategoryModel("Clothing", R.drawable.clothing, true);
        CategoryModel Health = new CategoryModel("Health", R.drawable.health, true);
        CategoryModel Pet = new CategoryModel("Pet", R.drawable.pet, true);
        CategoryModel Gifts = new CategoryModel("Gifts", R.drawable.gift, true);
        CategoryModel Phone = new CategoryModel("Phone", R.drawable.phone, true);
        CategoryModel Beauty = new CategoryModel("Beauty", R.drawable.beauty, true);
        CategoryModel Social = new CategoryModel("Social", R.drawable.social, true);
        CategoryModel Sport = new CategoryModel("Sport", R.drawable.sports, true);
        CategoryModel Housing = new CategoryModel("House", R.drawable.house, true);
        CategoryModel Marketing = new CategoryModel("Market", R.drawable.market, true);
        CategoryModel Others = new CategoryModel("Others", R.drawable.others, true);


        categoryDao.insertCategory(salary);
        categoryDao.insertCategory(Business);
        categoryDao.insertCategory(Shopping);
        categoryDao.insertCategory(Bike);
        categoryDao.insertCategory(Travel);
        categoryDao.insertCategory(Rent);
        categoryDao.insertCategory(Electronics);
        categoryDao.insertCategory(Clothing);
        categoryDao.insertCategory(Health);
        categoryDao.insertCategory(Pet);
        categoryDao.insertCategory(Gifts);
        categoryDao.insertCategory(Phone);
        categoryDao.insertCategory(Beauty);
        categoryDao.insertCategory(Social);
        categoryDao.insertCategory(Sport);
        categoryDao.insertCategory(Housing);
        categoryDao.insertCategory(Marketing);
        categoryDao.insertCategory(Others);

    }

    private static void seedDefaultAccounts(AccountDAO accountDao) {
        AccountModel cashAccount = new AccountModel("Cash", "Cash", "BDT", 0.0);
        AccountModel bankAccount = new AccountModel("Bank Account", "Bank Account", "BDT", 0.0);
        AccountModel creditCard = new AccountModel("Credit Card", "Credit Card", "BDT", 0.0);
        AccountModel debitCard = new AccountModel("Debit Card", "Debit Card", "BDT", 0.0);

        accountDao.insertAccount(cashAccount);
        accountDao.insertAccount(bankAccount);
        accountDao.insertAccount(creditCard);
        accountDao.insertAccount(debitCard);
    }
}