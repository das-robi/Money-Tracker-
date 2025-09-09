package utils;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.devrobin.moneytracker.MVVM.Model.BudgetModel;
import com.devrobin.moneytracker.MVVM.Model.ReminderModel;
import com.devrobin.moneytracker.MVVM.MainViewModel.ReminderViewModel;
import com.devrobin.moneytracker.R;
import com.devrobin.moneytracker.Views.activity.MainActivity;

import java.util.Calendar;

public class NotificationHelper {

    private static final String TAG = "NotificationHelper";

    // Notification Channels
    public static final String CHANNEL_DAILY_REMINDER = "daily_reminder_channel";
    public static final String CHANNEL_BUDGET_ALERTS = "budget_alerts_channel";
    public static final String CHANNEL_CUSTOM_REMINDERS = "custom_reminders_channel";
    public static final String CHANNEL_INACTIVITY = "inactivity_channel";

    // Notification IDs
    public static final int DAILY_REMINDER_ID = 1001;
    public static final int INACTIVITY_REMINDER_ID = 1002;
    public static final int BUDGET_50_PERCENT_ID = 1003;
    public static final int BUDGET_25_PERCENT_ID = 1004;
    public static final int BUDGET_10_PERCENT_ID = 1005;
    public static final int BUDGET_EXPIRED_ID = 1006;
    public static final int CUSTOM_REMINDER_BASE_ID = 2000;

    // SharedPreferences keys
    private static final String PREF_NAME = "NotificationSettings";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_SOUND_EFFECTS_ENABLED = "sound_effects_enabled";
    private static final String KEY_LAST_ACTIVITY = "last_activity_time";
    private static final String KEY_DAILY_REMINDER_TIME = "daily_reminder_time";
    private static final String KEY_NOTIFICATION_PERMISSION_GRANTED = "notification_permission_granted";

    private Context context;
    private NotificationManager notificationManager;
    private AlarmManager alarmManager;
    private SharedPreferences sharedPreferences;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Daily Reminder Channel
            NotificationChannel dailyChannel = new NotificationChannel(
                    CHANNEL_DAILY_REMINDER,
                    "Daily Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            dailyChannel.setDescription("Daily transaction reminders");
            dailyChannel.enableVibration(true);
            dailyChannel.setVibrationPattern(new long[]{0, 500, 200, 500});

            // Budget Alerts Channel
            NotificationChannel budgetChannel = new NotificationChannel(
                    CHANNEL_BUDGET_ALERTS,
                    "Budget Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            budgetChannel.setDescription("Budget limit alerts and warnings");
            budgetChannel.enableVibration(true);
            budgetChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});

            // Custom Reminders Channel
            NotificationChannel customChannel = new NotificationChannel(
                    CHANNEL_CUSTOM_REMINDERS,
                    "Custom Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            customChannel.setDescription("User-defined reminders");
            customChannel.enableVibration(true);

            // Inactivity Channel
            NotificationChannel inactivityChannel = new NotificationChannel(
                    CHANNEL_INACTIVITY,
                    "Inactivity Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            inactivityChannel.setDescription("Reminders when user is inactive");
            inactivityChannel.enableVibration(true);

            notificationManager.createNotificationChannel(dailyChannel);
            notificationManager.createNotificationChannel(budgetChannel);
            notificationManager.createNotificationChannel(customChannel);
            notificationManager.createNotificationChannel(inactivityChannel);
        }
    }

    public boolean areNotificationsEnabled() {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    public boolean areSoundEffectsEnabled() {
        return sharedPreferences.getBoolean(KEY_SOUND_EFFECTS_ENABLED, true);
    }

    public boolean isNotificationPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true; // For older versions, permission is granted by default
    }

    public void setNotificationPermissionGranted(boolean granted) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATION_PERMISSION_GRANTED, granted).apply();
    }

    // Schedule daily reminder notification (35 minutes after app launch)
    public void scheduleDailyReminder() {
        if (!areNotificationsEnabled() || !isNotificationPermissionGranted()) {
            Log.d(TAG, "Daily reminder not scheduled - notifications disabled or permission not granted");
            return;
        }

        try {
            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.setAction("DAILY_REMINDER");
            intent.putExtra("notification_id", DAILY_REMINDER_ID);
            intent.putExtra("title", "Daily Transaction Reminder");
            intent.putExtra("message", "Don't forget to add your daily transactions!");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    DAILY_REMINDER_ID,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Schedule for 35 minutes from now
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, 35);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }

            Log.d(TAG, "Daily reminder scheduled for: " + calendar.getTime().toString());
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling daily reminder", e);
        }
    }

    // Schedule inactivity reminder (2 days + 1 hour)
    public void scheduleInactivityReminder() {
        if (!areNotificationsEnabled() || !isNotificationPermissionGranted()) {
            Log.d(TAG, "Inactivity reminder not scheduled - notifications disabled or permission not granted");
            return;
        }

        try {
            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.setAction("INACTIVITY_REMINDER");
            intent.putExtra("notification_id", INACTIVITY_REMINDER_ID);
            intent.putExtra("title", "We Miss You!");
            intent.putExtra("message", "It's been 2 days since your last transaction. Keep track of your finances!");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    INACTIVITY_REMINDER_ID,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Schedule for 2 days + 1 hour from now
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, 2);
            calendar.add(Calendar.HOUR, 1);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }

            Log.d(TAG, "Inactivity reminder scheduled for: " + calendar.getTime().toString());
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling inactivity reminder", e);
        }
    }

    // Schedule custom reminder
    public void scheduleCustomReminder(ReminderModel reminder) {
        if (!areNotificationsEnabled() || !isNotificationPermissionGranted() || !reminder.isActive()) {
            Log.d(TAG, "Custom reminder not scheduled - notifications disabled, permission not granted, or reminder inactive");
            return;
        }

        try {
            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.setAction("CUSTOM_REMINDER");
            intent.putExtra("notification_id", CUSTOM_REMINDER_BASE_ID + reminder.getReminderId());
            intent.putExtra("title", reminder.getReminderName());
            intent.putExtra("message", reminder.getNote() != null ? reminder.getNote() : "Time to track your expenses!");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    CUSTOM_REMINDER_BASE_ID + reminder.getReminderId(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Get the next reminder time
            long nextReminderTime = reminder.getNextReminderTime();

            Log.d(TAG, "Scheduling custom reminder: " + reminder.getReminderName() +
                    " for: " + new java.util.Date(nextReminderTime).toString());

            // Schedule based on frequency
            switch (reminder.getFrequency().toLowerCase()) {
                case "daily":
                    // For daily reminders, use setRepeating
                    alarmManager.setRepeating(
                            AlarmManager.RTC_WAKEUP,
                            nextReminderTime,
                            AlarmManager.INTERVAL_DAY,
                            pendingIntent
                    );
                    break;
                case "monthly":
                case "yearly":
                    // For monthly/yearly, schedule the next occurrence
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                nextReminderTime,
                                pendingIntent
                        );
                    } else {
                        alarmManager.setExact(
                                AlarmManager.RTC_WAKEUP,
                                nextReminderTime,
                                pendingIntent
                        );
                    }
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling custom reminder", e);
        }
    }

    // Cancel custom reminder
    public void cancelCustomReminder(ReminderModel reminder) {
        try {
            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.setAction("CUSTOM_REMINDER");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    CUSTOM_REMINDER_BASE_ID + reminder.getReminderId(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.cancel(pendingIntent);
            Log.d(TAG, "Cancelled custom reminder: " + reminder.getReminderName());
        } catch (Exception e) {
            Log.e(TAG, "Error cancelling custom reminder", e);
        }
    }

    // Check and send budget notifications
    public void checkBudgetNotifications(BudgetModel budget) {
        if (!areNotificationsEnabled() || !isNotificationPermissionGranted()) {
            return;
        }

        double progress = budget.getProgressPercentage();

        // 50% notification
        if (progress >= 50 && progress < 75) {
            sendBudgetNotification(budget, 50, BUDGET_50_PERCENT_ID);
        }
        // 25% notification
        else if (progress >= 75 && progress < 90) {
            sendBudgetNotification(budget, 25, BUDGET_25_PERCENT_ID);
        }
        // 10% notification
        else if (progress >= 90 && progress < 100) {
            sendBudgetNotification(budget, 10, BUDGET_10_PERCENT_ID);
        }
        // Budget expired notification
        else if (progress >= 100) {
            sendBudgetExpiredNotification(budget);
        }
    }

    private void sendBudgetNotification(BudgetModel budget, int percentage, int notificationId) {
        try {
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    notificationId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            String title = "Budget Alert - " + budget.getCategory();
            String message = String.format("You've used %d%% of your %s budget. Only %.0f৳ remaining!",
                    percentage, budget.getCategory(), budget.getRemaining());

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_BUDGET_ALERTS)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER);

            if (areSoundEffectsEnabled()) {
                builder.setDefaults(NotificationCompat.DEFAULT_SOUND);
            }

            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Budget notification sent: " + title);
        } catch (Exception e) {
            Log.e(TAG, "Error sending budget notification", e);
        }
    }

    private void sendBudgetExpiredNotification(BudgetModel budget) {
        try {
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    BUDGET_EXPIRED_ID,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            String title = "Budget Exceeded - " + budget.getCategory();
            String message = String.format("Your %s budget has been exceeded by %.0f৳!",
                    budget.getCategory(), Math.abs(budget.getRemaining()));

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_BUDGET_ALERTS)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER);

            if (areSoundEffectsEnabled()) {
                builder.setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE);
            }

            notificationManager.notify(BUDGET_EXPIRED_ID, builder.build());
            Log.d(TAG, "Budget exceeded notification sent: " + title);
        } catch (Exception e) {
            Log.e(TAG, "Error sending budget exceeded notification", e);
        }
    }

    // Send immediate notification
    public void sendNotification(String title, String message, int notificationId) {
        if (!areNotificationsEnabled() || !isNotificationPermissionGranted()) {
            Log.d(TAG, "Notification not sent - notifications disabled or permission not granted");
            return;
        }

        try {
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    notificationId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_CUSTOM_REMINDERS)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER);

            if (areSoundEffectsEnabled()) {
                builder.setDefaults(NotificationCompat.DEFAULT_SOUND);
            }

            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Notification sent: " + title);
        } catch (Exception e) {
            Log.e(TAG, "Error sending notification", e);
        }
    }

    // Update last activity time
    public void updateLastActivity() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis());
        editor.apply();
        Log.d(TAG, "Last activity updated");
    }

    // Check if user has been inactive for 2 days
    public boolean isUserInactive() {
        long lastActivity = sharedPreferences.getLong(KEY_LAST_ACTIVITY, 0);
        long currentTime = System.currentTimeMillis();
        long twoDaysInMillis = 2 * 24 * 60 * 60 * 1000L;

        boolean inactive = (currentTime - lastActivity) >= twoDaysInMillis;
        Log.d(TAG, "User inactive check: " + inactive + " (last activity: " + new java.util.Date(lastActivity) + ")");
        return inactive;
    }

    // Reschedule all active reminders
    public void rescheduleAllReminders() {
        if (!areNotificationsEnabled() || !isNotificationPermissionGranted()) {
            Log.d(TAG, "Not rescheduling reminders - notifications disabled or permission not granted");
            return;
        }

        try {
            // This method should be called from MainActivity or when app starts
            // It will reschedule all active reminders
            ReminderViewModel reminderViewModel = new ReminderViewModel((android.app.Application) context.getApplicationContext());
            reminderViewModel.getActiveReminders().observeForever(reminders -> {
                if (reminders != null) {
                    Log.d(TAG, "Rescheduling " + reminders.size() + " active reminders");
                    for (ReminderModel reminder : reminders) {
                        if (reminder.isActive()) {
                            scheduleCustomReminder(reminder);
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error rescheduling reminders", e);
        }
    }

    // Cancel all scheduled notifications
    public void cancelAllNotifications() {
        try {
            // Cancel daily reminder
            Intent dailyIntent = new Intent(context, NotificationReceiver.class);
            dailyIntent.setAction("DAILY_REMINDER");
            PendingIntent dailyPendingIntent = PendingIntent.getBroadcast(
                    context,
                    DAILY_REMINDER_ID,
                    dailyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            alarmManager.cancel(dailyPendingIntent);

            // Cancel inactivity reminder
            Intent inactivityIntent = new Intent(context, NotificationReceiver.class);
            inactivityIntent.setAction("INACTIVITY_REMINDER");
            PendingIntent inactivityPendingIntent = PendingIntent.getBroadcast(
                    context,
                    INACTIVITY_REMINDER_ID,
                    inactivityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            alarmManager.cancel(inactivityPendingIntent);

            // Cancel all notifications
            notificationManager.cancelAll();

            Log.d(TAG, "All notifications cancelled");
        } catch (Exception e) {
            Log.e(TAG, "Error cancelling notifications", e);
        }
    }

    // Test notification method for debugging
    public void sendTestNotification() {
        sendNotification(
                "Test Notification",
                "This is a test notification to verify the system is working!",
                9999
        );
    }
}
