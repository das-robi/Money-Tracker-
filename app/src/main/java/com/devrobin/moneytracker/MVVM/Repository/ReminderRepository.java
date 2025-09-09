package com.devrobin.moneytracker.MVVM.Repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.devrobin.moneytracker.MVVM.DAO.ReminderDAO;
import com.devrobin.moneytracker.MVVM.Model.ReminderModel;
import com.devrobin.moneytracker.MVVM.TransactionDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReminderRepository {

    private ReminderDAO reminderDAO;
    private LiveData<List<ReminderModel>> allReminders;

    public ReminderRepository(Application application) {

        TransactionDatabase database = TransactionDatabase.getInstance(application);

        reminderDAO = database.reminderDao();
        allReminders = reminderDAO.getAllReminders();
    }

    public LiveData<List<ReminderModel>> getAllReminders() {
        return allReminders;
    }

    public LiveData<List<ReminderModel>> getActiveReminders() {
        return reminderDAO.getActiveReminders();
    }

    public LiveData<ReminderModel> getReminderById(int reminderId) {
        return reminderDAO.getReminderById(reminderId);
    }

    public LiveData<List<ReminderModel>> getRemindersByCategory(String category) {
        return reminderDAO.getRemindersByCategory(category);
    }

    public LiveData<List<ReminderModel>> getRemindersByFrequency(String frequency) {
        return reminderDAO.getRemindersByFrequency(frequency);
    }


    public void insertReminder(ReminderModel reminderModel) {

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                reminderDAO.insertReminder(reminderModel);
            }
        });
    }

    public void updateReminder(ReminderModel reminderModel) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                reminderDAO.updateReminder(reminderModel);
            }
        });
    }

    public void deleteReminder(ReminderModel reminderModel) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                reminderDAO.deleteReminder(reminderModel);
            }
        });
    }

    public void deleteReminderById(int reminderId) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                reminderDAO.deleteReminderById(reminderId);
            }
        });
    }




    public void updateReminderStatus(int reminderId, boolean isActive) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                reminderDAO.updateReminderStatus(reminderId, isActive);
            }
        });
    }

}
