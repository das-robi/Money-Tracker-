package utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.devrobin.moneytracker.MVVM.MainViewModel.ReminderViewModel;
import com.devrobin.moneytracker.R;
import com.devrobin.moneytracker.Views.activity.MainActivity;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "NotificationReceiver received: " + intent.getAction());

        String action = intent.getAction();
        if (action == null) {
            Log.e(TAG, "Action is null");
            return;
        }

        try {
            switch (action) {
                case "DAILY_REMINDER":
                    handleDailyReminder(context, intent);
                    break;
                case "INACTIVITY_REMINDER":
                    handleInactivityReminder(context, intent);
                    break;
                case "CUSTOM_REMINDER":
                    handleCustomReminder(context, intent);
                    break;
                default:
                    Log.w(TAG, "Unknown action: " + action);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling notification", e);
        }
    }

    private void handleDailyReminder(Context context, Intent intent) {
        Log.d(TAG, "Handling daily reminder");

        int notificationId = intent.getIntExtra("notification_id", NotificationHelper.DAILY_REMINDER_ID);
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        if (title == null) title = "Daily Transaction Reminder";
        if (message == null) message = "Don't forget to add your daily transactions!";

        sendNotification(context, title, message, notificationId, NotificationHelper.CHANNEL_DAILY_REMINDER);
    }

    private void handleInactivityReminder(Context context, Intent intent) {
        Log.d(TAG, "Handling inactivity reminder");

        int notificationId = intent.getIntExtra("notification_id", NotificationHelper.INACTIVITY_REMINDER_ID);
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        if (title == null) title = "We Miss You!";
        if (message == null) message = "It's been 2 days since your last transaction. Keep track of your finances!";

        sendNotification(context, title, message, notificationId, NotificationHelper.CHANNEL_INACTIVITY);
    }

    private void handleCustomReminder(Context context, Intent intent) {
        Log.d(TAG, "Handling custom reminder");

        int notificationId = intent.getIntExtra("notification_id", 0);
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        if (title == null) title = "Reminder";
        if (message == null) message = "Time to track your expenses!";

        sendNotification(context, title, message, notificationId, NotificationHelper.CHANNEL_CUSTOM_REMINDERS);

        // Reschedule the reminder if it's monthly or yearly
        rescheduleReminder(context, notificationId);
    }

    private void sendNotification(Context context, String title, String message, int notificationId, String channelId) {
        try {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // Create notification channel if it doesn't exist (for older devices)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
                if (channel == null) {
                    Log.w(TAG, "Notification channel not found: " + channelId);
                    // Create a default channel
                    channel = new NotificationChannel(
                            channelId,
                            "Default Channel",
                            NotificationManager.IMPORTANCE_DEFAULT
                    );
                    notificationManager.createNotificationChannel(channel);
                }
            }

            // Create intent to open MainActivity
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    notificationId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Build notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER);

            // Add sound and vibration
            NotificationHelper notificationHelper = new NotificationHelper(context);
            if (notificationHelper.areSoundEffectsEnabled()) {
                builder.setDefaults(NotificationCompat.DEFAULT_SOUND);
            }

            // Show notification
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Notification sent successfully: " + title);

        } catch (Exception e) {
            Log.e(TAG, "Error sending notification", e);
        }
    }

    private void rescheduleReminder(Context context, int notificationId) {
        try {
            // Extract reminder ID from notification ID
            int reminderId = notificationId - NotificationHelper.CUSTOM_REMINDER_BASE_ID;

            Log.d(TAG, "Rescheduling reminder ID: " + reminderId);

            // Get the reminder and reschedule it if it's monthly or yearly
            ReminderViewModel reminderViewModel = new ReminderViewModel((android.app.Application) context.getApplicationContext());
            reminderViewModel.getReminderById(reminderId).observeForever(reminder -> {
                if (reminder != null && reminder.isActive()) {
                    String frequency = reminder.getFrequency().toLowerCase();
                    if (frequency.equals("monthly") || frequency.equals("yearly")) {
                        Log.d(TAG, "Rescheduling " + frequency + " reminder: " + reminder.getReminderName());
                        // Reschedule the reminder for the next occurrence
                        NotificationHelper notificationHelper = new NotificationHelper(context);
                        notificationHelper.scheduleCustomReminder(reminder);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error rescheduling reminder", e);
        }
    }
}
