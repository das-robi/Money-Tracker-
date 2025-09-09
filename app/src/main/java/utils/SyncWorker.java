package utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

public class SyncWorker extends Worker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (!SharedPrefsManager.getInstance(getApplicationContext()).isAutoSyncEnabled()) {
            return Result.success();
        }
        final Result[] result = {Result.success()};
        final Object lock = new Object();

        RealtimeSyncManager.syncNow(getApplicationContext(), new RealtimeSyncManager.SyncCallback() {
            @Override
            public void onSuccess() {
                synchronized (lock) { lock.notify(); }
            }

            @Override
            public void onError(@NonNull Exception e) {
                result[0] = Result.retry();
                synchronized (lock) { lock.notify(); }
            }
        });

        try {
            synchronized (lock) { lock.wait(30000); }
        } catch (InterruptedException ignored) {}
        return result[0];
    }

    public static void schedulePeriodic(Context context) {
        if (!SharedPrefsManager.getInstance(context).isAutoSyncEnabled()) return;
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .setInitialDelay(15, TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(context).enqueue(request);
    }
}
