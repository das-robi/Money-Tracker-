package com.devrobin.moneytracker.Views.activity;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.devrobin.moneytracker.MVVM.MainViewModel.TransViewModel;
import com.devrobin.moneytracker.MVVM.Model.TransactionModel;
import com.devrobin.moneytracker.Views.Fragments.AddTransaction;
import com.devrobin.moneytracker.Views.Fragments.HomeFragment;
import com.devrobin.moneytracker.Views.Fragments.TransactionChart;
import com.devrobin.moneytracker.Views.Fragments.TransactionReports;
import com.devrobin.moneytracker.Views.Fragments.UserProfile;
import com.devrobin.moneytracker.R;
import com.devrobin.moneytracker.adapter.TransactionAdapter;
import com.devrobin.moneytracker.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.Locale;

import utils.NotificationHelper;


public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;
    private NotificationHelper notificationHelper;
    private static final String TAG = "MainActivity";

    private Toolbar toolbar;

    public TransViewModel transViewModel;


    // Calendar & Date
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault());
    private SimpleDateFormat dayDateFormate = new SimpleDateFormat("EEE", Locale.getDefault());
    private Calendar calendar = Calendar.getInstance();

    // Permission launcher for notification permission
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "Notification permission granted");
                    notificationHelper.setNotificationPermissionGranted(true);
                    setupNotifications();
                } else {
                    Log.d(TAG, "Notification permission denied");
                    notificationHelper.setNotificationPermissionGranted(false);
                    Toast.makeText(this, "Notification permission is required for reminders", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);

        binding.bottomNavigation.setOnNavigationItemSelectedListener(this);
        binding.bottomNavigation.setSelectedItemId(R.id.home);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.contentFrameLayout, new HomeFragment());
        transaction.commit();


        //BottomSheetDialogFragment
        binding.addFloatBtn.setOnClickListener(v -> {
            new AddTransaction().show(getSupportFragmentManager(), null);
        });


        // Initialize notification helper
        notificationHelper = new NotificationHelper(this);

        // Check and request notification permission for Android 13+
        checkNotificationPermission();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Requesting notification permission");
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                Log.d(TAG, "Notification permission already granted");
                notificationHelper.setNotificationPermissionGranted(true);
                setupNotifications();
            }
        } else {
            // For older versions, permission is granted by default
            notificationHelper.setNotificationPermissionGranted(true);
            setupNotifications();
        }
    }

    private void setupNotifications() {
        try {
            Log.d(TAG, "Setting up notifications");

            // Update last activity time
            notificationHelper.updateLastActivity();

            // Schedule daily reminder (35 minutes after app launch)
            notificationHelper.scheduleDailyReminder();

            // Schedule inactivity reminder (2 days + 1 hour)
            notificationHelper.scheduleInactivityReminder();

            // Reschedule all active reminders
            notificationHelper.rescheduleAllReminders();

            Log.d(TAG, "Notifications setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up notifications", e);
        }
    }



    //BottomView Menu Items
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        FragmentTransaction transactionMenu = getSupportFragmentManager().beginTransaction();

        int itemId = menuItem.getItemId();

        if (itemId == R.id.home){
            transactionMenu.replace(R.id.contentFrameLayout, new HomeFragment());
            getSupportFragmentManager().popBackStack();


        }
        else if (itemId == R.id.charts){
            transactionMenu.replace(R.id.contentFrameLayout, new TransactionChart());
            transactionMenu.addToBackStack(null);

        }
        else if (itemId == R.id.reports){
            transactionMenu.replace(R.id.contentFrameLayout, new TransactionReports());
            transactionMenu.addToBackStack(null);
        }
        else if (itemId == R.id.profile){
            transactionMenu.replace(R.id.contentFrameLayout, new UserProfile());
            transactionMenu.addToBackStack(null);
        }

        transactionMenu.commit();
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Update last activity time when app comes to foreground
        if (notificationHelper != null) {
            notificationHelper.updateLastActivity();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up if needed
    }
}
