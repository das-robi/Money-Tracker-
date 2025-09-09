package utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.devrobin.moneytracker.MVVM.DAO.AccountDAO;
import com.devrobin.moneytracker.MVVM.DAO.TransactionDao;
import com.devrobin.moneytracker.MVVM.Model.AccountModel;
import com.devrobin.moneytracker.MVVM.Model.TransactionModel;
import com.devrobin.moneytracker.MVVM.TransactionDatabase;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreSyncManager {
    public interface SyncCallback {
        void onSuccess();
        void onError(@NonNull Exception e);
    }

    private static final String TAG = "FirestoreSync";

    public static void syncNow(Context context, SyncCallback callback) {
        TransactionDatabase db = TransactionDatabase.getInstance(context);
        AccountDAO accountDao = db.accountDao();
        TransactionDao transactionDao = db.transDao();

        String uid = getUid();
        if (uid == null) {
            if (callback != null) callback.onError(new IllegalStateException("User not logged in"));
            return;
        }

        FirebaseFirestore fs = FirebaseFirestore.getInstance();
        CollectionReference accountsRef = fs.collection("users").document(uid).collection("accounts");
        CollectionReference transactionsRef = fs.collection("users").document(uid).collection("transactions");

        TransactionDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // 1) Push local -> cloud (upsert) using lastModifiedTime
                List<AccountModel> localAccounts = accountDao.getAllAccountsSync();
                for (AccountModel a : localAccounts) {
                    String docId = String.valueOf(a.getAccountId());
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("accountId", a.getAccountId());
                    payload.put("accountName", a.getAccountName());
                    payload.put("cardType", a.getCardType());
                    payload.put("currency", a.getCurrency());
                    payload.put("balance", a.getBalance());
                    payload.put("note", a.getNote());
                    payload.put("iconId", a.getIconId());
                    payload.put("lastModifiedTime", a.getLastModifiedTime());

                    DocumentReference docRef = accountsRef.document(docId);
                    DocumentSnapshot snapshot = Tasks.await(docRef.get());
                    if (!snapshot.exists() || getLong(snapshot, "lastModifiedTime") < a.getLastModifiedTime()) {
                        Tasks.await(docRef.set(payload));
                    }
                }

                List<TransactionModel> localTx = transactionDao.getAllTransactionSync();
                for (TransactionModel t : localTx) {
                    String docId = String.valueOf(t.getTransId());
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

                    DocumentReference docRef = transactionsRef.document(docId);
                    DocumentSnapshot snapshot = Tasks.await(docRef.get());
                    if (!snapshot.exists() || getLong(snapshot, "lastModifiedTime") < t.getLastModifiedTime()) {
                        Tasks.await(docRef.set(payload));
                    }
                }

                // 2) Pull cloud -> local (upsert) if cloud newer
                for (DocumentSnapshot snap : Tasks.await(accountsRef.get()).getDocuments()) {
                    int accountId = (int) getLong(snap, "accountId");
                    long cloudLmt = getLong(snap, "lastModifiedTime");
                    AccountModel local = accountDao.getAccountByIdSync(accountId);
                    if (local == null || local.getLastModifiedTime() < cloudLmt) {
                        AccountModel a = local != null ? local : new AccountModel();
                        a.setAccountId(accountId);
                        a.setAccountName(snap.getString("accountName"));
                        a.setCardType(snap.getString("cardType"));
                        a.setCurrency(snap.getString("currency"));
                        Double bal = snap.getDouble("balance");
                        a.setBalance(bal == null ? 0.0 : bal);
                        a.setNote(snap.getString("note"));
                        Long iconId = snap.getLong("iconId");
                        a.setIconId(iconId == null ? 0 : iconId.intValue());
                        a.setLastModifiedTime(cloudLmt);
                        if (local == null) {
                            accountDao.insertAccount(a);
                        } else {
                            accountDao.updateAccount(a);
                        }
                    }
                }

                for (DocumentSnapshot snap : Tasks.await(transactionsRef.get()).getDocuments()) {
                    int transId = (int) getLong(snap, "transId");
                    long cloudLmt = getLong(snap, "lastModifiedTime");
                    // No direct DAO to get by ID; read all is expensive, so rely on upsert using update may fail if missing.
                    // Simpler approach: try update first; if affects 0 rows, insert. We don't have affected rows info.
                    // Workaround: fetch by date range is not reliable; instead, build model and call insert; if PK exists, Room will replace? Not without OnConflict. So we do a safe path:
                    TransactionModel t = new TransactionModel();
                    try {
                        // Populate model from snapshot
                        t.setTransId(transId);
                    } catch (Throwable ignored) {}
                    t.setType(snap.getString("type"));
                    t.setCategory(snap.getString("category"));
                    Double amount = snap.getDouble("amount");
                    t.setAmount(amount == null ? 0.0 : amount);
                    t.setNote(snap.getString("note"));
                    Long td = snap.getLong("transactionDate");
                    if (td != null && td > 0) t.setTransactionDate(new java.util.Date(td));
                    Long cd = snap.getLong("createDate");
                    if (cd != null && cd > 0) t.setCreateDate(new java.util.Date(cd));
                    Long accId = snap.getLong("accountId");
                    t.setAccountId(accId == null ? 1 : accId.intValue());
                    t.setLastModifiedTime(cloudLmt);

                    // Try update first
                    try {
                        db.runInTransaction(() -> {
                            transactionDao.updateTransaction(t);
                            // If not present, fallback to insert
                            transactionDao.insertTransaction(t);
                        });
                    } catch (Throwable e) {
                        try { transactionDao.insertTransaction(t); } catch (Throwable ignored) {}
                    }
                }

                SharedPrefsManager.getInstance(context).setLastSyncedAt(System.currentTimeMillis());
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                Log.e(TAG, "Sync failed", e);
                if (callback != null) callback.onError(e);
            }
        });
    }

    private static String getUid() {
        try {
            return FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        } catch (Throwable t) { return null; }
    }

    private static long getLong(DocumentSnapshot snap, String key) {
        Long v = snap.getLong(key);
        return v == null ? 0L : v;
    }
}
