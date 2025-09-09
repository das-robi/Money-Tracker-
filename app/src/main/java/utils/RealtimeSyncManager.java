package utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.devrobin.moneytracker.MVVM.DAO.AccountDAO;
import com.devrobin.moneytracker.MVVM.DAO.BudgetDAO;
import com.devrobin.moneytracker.MVVM.DAO.CategoryDAO;
import com.devrobin.moneytracker.MVVM.DAO.TransactionDao;
import com.devrobin.moneytracker.MVVM.Model.AccountModel;
import com.devrobin.moneytracker.MVVM.Model.BudgetModel;
import com.devrobin.moneytracker.MVVM.Model.CategoryModel;
import com.devrobin.moneytracker.MVVM.Model.TransactionModel;
import com.devrobin.moneytracker.MVVM.TransactionDatabase;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RealtimeSyncManager {
    public interface SyncCallback {
        void onSuccess();
        void onError(@NonNull Exception e);
    }

    private static final String TAG = "RealtimeSync";

    public static void syncNow(Context context, SyncCallback callback) {
        String uid = getUid();
        if (uid == null) {
            if (callback != null) callback.onError(new IllegalStateException("User not logged in"));
            return;
        }

        TransactionDatabase db = TransactionDatabase.getInstance(context);
        AccountDAO accountDao = db.accountDao();
        TransactionDao transactionDao = db.transDao();
        BudgetDAO budgetDao = db.budgetDao();
        CategoryDAO categoryDao = db.categoryDao();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        DatabaseReference accountsRef = userRef.child("accounts");
        DatabaseReference transactionsRef = userRef.child("transactions");
        DatabaseReference budgetsRef = userRef.child("budgets");
        DatabaseReference categoriesRef = userRef.child("categories");

        TransactionDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // push local -> cloud
                List<AccountModel> localAccounts = accountDao.getAllAccountsSync();
                for (AccountModel a : localAccounts) {
                    String key = String.valueOf(a.getAccountId());
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("accountId", a.getAccountId());
                    payload.put("accountName", a.getAccountName());
                    payload.put("cardType", a.getCardType());
                    payload.put("currency", a.getCurrency());
                    payload.put("balance", a.getBalance());
                    payload.put("note", a.getNote());
                    payload.put("iconId", a.getIconId());
                    payload.put("lastModifiedTime", a.getLastModifiedTime());

                    DataSnapshot snap = Tasks.await(accountsRef.child(key).get());
                    long cloudLmt = getLong(snap.child("lastModifiedTime"));
                    if (!snap.exists() || cloudLmt < a.getLastModifiedTime()) {
                        Tasks.await(accountsRef.child(key).setValue(payload));
                    }
                }

                List<TransactionModel> localTx = transactionDao.getAllTransactionSync();
                for (TransactionModel t : localTx) {
                    String key = String.valueOf(t.getTransId());
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("transId", t.getTransId());
                    payload.put("type", t.getType());
                    payload.put("category", t.getCategory());
                    payload.put("amount", t.getAmount());
                    payload.put("note", t.getNote());
                    payload.put("transactionDate", t.getTransactionDate() != null ? t.getTransactionDate().getTime() : 0);
                    payload.put("createDate", t.getCreateDate() != null ? t.getCreateDate().getTime() : 0);
                    payload.put("accountId", t.getAccountId());
                    payload.put("lastModifiedTime", t.getLastModifiedTime());

                    DataSnapshot snap = Tasks.await(transactionsRef.child(key).get());
                    long cloudLmt = getLong(snap.child("lastModifiedTime"));
                    if (!snap.exists() || cloudLmt < t.getLastModifiedTime()) {
                        Tasks.await(transactionsRef.child(key).setValue(payload));
                    }
                }

                // push budgets
                List<BudgetModel> budgets = budgetDao.getAllBudgetsSync();
                for (BudgetModel b : budgets) {
                    String key = String.valueOf(b.getBudgetId());
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("budgetId", b.getBudgetId());
                    payload.put("category", b.getCategory());
                    payload.put("budgetType", b.getBudgetType());
                    payload.put("budgetAmount", b.getBudgetAmount());
                    payload.put("spentAmount", b.getSpentAmount());
                    payload.put("month", b.getMonth());
                    payload.put("year", b.getYear());
                    payload.put("day", b.getDay());
                    payload.put("note", b.getNote());
                    Tasks.await(budgetsRef.child(key).setValue(payload));
                }

                // push categories (idempotent by primary key)
                List<CategoryModel> catList = categoryDao.getAllCategoriesSync();
                for (CategoryModel c : catList) {
                    String key = String.valueOf(c.getCategoryId());
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("categoryId", c.getCategoryId());
                    payload.put("categoryName", c.getCategoryName());
                    payload.put("iconId", c.getIconId());
                    payload.put("isDefault", c.isDefault());
                    Tasks.await(categoriesRef.child(key).setValue(payload));
                }

                // pull cloud -> local
                DataSnapshot accRoot = Tasks.await(accountsRef.get());
                for (DataSnapshot snap : accRoot.getChildren()) {
                    int accountId = (int) getLong(snap.child("accountId"));
                    long cloudLmt = getLong(snap.child("lastModifiedTime"));
                    AccountModel local = accountDao.getAccountByIdSync(accountId);
                    if (local == null || local.getLastModifiedTime() < cloudLmt) {
                        AccountModel a = local != null ? local : new AccountModel();
                        a.setAccountId(accountId);
                        a.setAccountName(snap.child("accountName").getValue(String.class));
                        a.setCardType(snap.child("cardType").getValue(String.class));
                        a.setCurrency(snap.child("currency").getValue(String.class));
                        Double bal = getDouble(snap.child("balance"));
                        a.setBalance(bal == null ? 0.0 : bal);
                        a.setNote(snap.child("note").getValue(String.class));
                        Long iconId = snap.child("iconId").getValue(Long.class);
                        a.setIconId(iconId == null ? 0 : iconId.intValue());
                        a.setLastModifiedTime(cloudLmt);
                        if (local == null) {
                            accountDao.insertAccount(a);
                        } else {
                            accountDao.updateAccount(a);
                        }
                    }
                }

                DataSnapshot txRoot = Tasks.await(transactionsRef.get());
                for (DataSnapshot snap : txRoot.getChildren()) {
                    int transId = (int) getLong(snap.child("transId"));
                    long cloudLmt = getLong(snap.child("lastModifiedTime"));

                    TransactionModel t = new TransactionModel();
                    try { t.setTransId(transId); } catch (Throwable ignored) {}
                    t.setType(snap.child("type").getValue(String.class));
                    t.setCategory(snap.child("category").getValue(String.class));
                    Double amount = getDouble(snap.child("amount"));
                    t.setAmount(amount == null ? 0.0 : amount);
                    t.setNote(snap.child("note").getValue(String.class));
                    Long td = snap.child("transactionDate").getValue(Long.class);
                    if (td != null && td > 0) t.setTransactionDate(new java.util.Date(td));
                    Long cd = snap.child("createDate").getValue(Long.class);
                    if (cd != null && cd > 0) t.setCreateDate(new java.util.Date(cd));
                    Long accId = snap.child("accountId").getValue(Long.class);
                    t.setAccountId(accId == null ? 1 : accId.intValue());
                    t.setLastModifiedTime(cloudLmt);

                    try {
                        db.runInTransaction(() -> {
                            transactionDao.updateTransaction(t);
                            transactionDao.insertTransaction(t);
                        });
                    } catch (Throwable e) {
                        try { transactionDao.insertTransaction(t); } catch (Throwable ignored) {}
                    }
                }

                // compute and push summary
                double totalIncome = 0.0;
                double totalExpense = 0.0;
                for (TransactionModel t : localTx) {
                    if ("INCOME".equalsIgnoreCase(t.getType())) totalIncome += t.getAmount();
                    else if ("EXPENSE".equalsIgnoreCase(t.getType())) totalExpense += t.getAmount();
                }
                double totalBalance = 0.0;
                List<AccountModel> accs = accountDao.getAllAccountsSync();
                for (AccountModel a : accs) totalBalance += a.getBalance();
                Map<String, Object> summary = new HashMap<>();
                summary.put("totalIncome", totalIncome);
                summary.put("totalExpense", totalExpense);
                summary.put("totalBalance", totalBalance);
                Tasks.await(userRef.child("summary").setValue(summary));

                SharedPrefsManager.getInstance(context).setLastSyncedAt(System.currentTimeMillis());
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                Log.e(TAG, "Sync failed", e);
                if (callback != null) callback.onError(e);
            }
        });
    }

    private static String getUid() {
        try { return FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null; }
        catch (Throwable t) { return null; }
    }

    private static long getLong(DataSnapshot snap) {
        Long v = snap.getValue(Long.class);
        if (v != null) return v;
        Object o = snap.getValue();
        if (o instanceof Number) return ((Number) o).longValue();
        return 0L;
    }

    private static Double getDouble(DataSnapshot snap) {
        Object o = snap.getValue();
        if (o instanceof Number) return ((Number) o).doubleValue();
        return null;
    }
}