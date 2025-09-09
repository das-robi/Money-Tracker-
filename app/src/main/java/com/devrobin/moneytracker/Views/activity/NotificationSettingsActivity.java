package com.devrobin.moneytracker.Views.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.devrobin.moneytracker.R;
import com.devrobin.moneytracker.databinding.ActivityNotificationSettingsBinding;

import utils.NotificationHelper;

public class NotificationSettingsActivity extends AppCompatActivity {

    private ActivityNotificationSettingsBinding binding;
    private NotificationHelper notificationHelper;
    private static final String TAG = "NotificationSettings";

    // Permission launcher for notification permission
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "Notification permission granted");
                    notificationHelper.setNotificationPermissionGranted(true);
                    updatePermissionStatus();
                    Toast.makeText(this, "Notification permission granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "Notification permission denied");
                    notificationHelper.setNotificationPermissionGranted(false);
                    updatePermissionStatus();
                    showPermissionDeniedDialog();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityNotificationSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize notification helper
        notificationHelper = new NotificationHelper(this);

        // Setup click listeners
        setupClickListeners();

        // Update UI based on current settings
        updateUI();

    }

    private void setupClickListeners() {

        // Back button
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        // Notification toggle
        binding.switchNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean isChecked) {
                notificationHelper.setNotificationPermissionGranted(isChecked);
                updatePermissionStatus();

                if (isChecked && !notificationHelper.isNotificationPermissionGranted()) {
                    // Request permission if not granted
                    requestNotificationPermission();
                }
            }
        });

        // Sound effects toggle
        binding.switchSoundEffects.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save sound effects preference
            getSharedPreferences("NotificationSettings", MODE_PRIVATE)
                    .edit()
                    .putBoolean("sound_effects_enabled", isChecked)
                    .apply();
        });

        // Test notification button
        binding.btnTestNotification.setOnClickListener(v -> {
            if (notificationHelper.isNotificationPermissionGranted()) {
                notificationHelper.sendTestNotification();
                Toast.makeText(this, "Test notification sent!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please grant notification permission first", Toast.LENGTH_LONG).show();
                requestNotificationPermission();
            }
        });

        // Permission request button
        binding.btnRequestPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestNotificationPermission();
            }
        });
    }

    private void updateUI() {
        // Update notification toggle
        binding.switchNotification.setChecked(notificationHelper.areNotificationsEnabled());

        // Update sound effects toggle
        boolean soundEnabled = getSharedPreferences("NotificationSettings", MODE_PRIVATE)
                .getBoolean("sound_effects_enabled", true);
        binding.switchSoundEffects.setChecked(soundEnabled);

        // Update permission status
        updatePermissionStatus();
    }


    private void updatePermissionStatus() {
        boolean permissionGranted = notificationHelper.isNotificationPermissionGranted();

        if (permissionGranted) {
            binding.tvPermissionStatus.setText("Granted");
            binding.tvPermissionStatus.setTextColor(getColor(R.color.green));
            binding.btnRequestPermission.setVisibility(View.GONE);
        } else {
            binding.tvPermissionStatus.setText("Not Granted");
            binding.tvPermissionStatus.setTextColor(getColor(R.color.red));
            binding.btnRequestPermission.setVisibility(View.VISIBLE);
        }
    }


    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Requesting notification permission");
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                Log.d(TAG, "Notification permission already granted");
                notificationHelper.setNotificationPermissionGranted(true);
                updatePermissionStatus();
            }
        } else {
            // For older versions, permission is granted by default
            notificationHelper.setNotificationPermissionGranted(true);
            updatePermissionStatus();
            Toast.makeText(this, "Notification permission is automatically granted on this device", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Notification Permission Required")
                .setMessage("To receive reminders and budget alerts, please grant notification permission. You can enable it in Settings.")
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    // Open app settings
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
