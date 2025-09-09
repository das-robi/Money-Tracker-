package com.devrobin.moneytracker.Views.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.devrobin.moneytracker.R;
import com.devrobin.moneytracker.databinding.ActivityProfileSettingsBinding;

import utils.SharedPrefsManager;

public class ProfileSettingsActivity extends AppCompatActivity {


    private ActivityProfileSettingsBinding  profileBinding;
    private SharedPrefsManager prefs;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    profileBinding.profileImage.setImageURI(uri);
                    prefs.saveProfilePhotoUri(uri.toString());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        profileBinding = ActivityProfileSettingsBinding.inflate(getLayoutInflater());
        setContentView(profileBinding.getRoot());

        setSupportActionBar(profileBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Profile");
        }


        prefs = SharedPrefsManager.getInstance(this);

        loadProfile();

        profileBinding.editPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageLauncher.launch("image/*");
            }
        });

        profileBinding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });

        profileBinding.autoSyncSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.setAutoSyncEnabled(isChecked);
            if (isChecked) {
                utils.SyncWorker.schedulePeriodic(this);
                utils.RealtimeSyncManager.syncNow(this, new utils.RealtimeSyncManager.SyncCallback() {
                    @Override public void onSuccess() { }
                    @Override public void onError(@NonNull Exception e) { }
                });
            }
        });

        profileBinding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // For now, just go back to login; guest mode is simply not logging in
                try { com.google.firebase.auth.FirebaseAuth.getInstance().signOut(); } catch (Throwable ignored) {}
                Toast.makeText(ProfileSettingsActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(ProfileSettingsActivity.this, RegisterActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void loadProfile() {
        String name = prefs.getProfileName();
        String email = prefs.getProfileEmail();
        String photo = prefs.getProfilePhotoUri();
        boolean autoSync = prefs.isAutoSyncEnabled();

        if (!TextUtils.isEmpty(name)) profileBinding.nameEdit.setText(name);
        if (!TextUtils.isEmpty(email)) profileBinding.emailEdit.setText(email);
        if (!TextUtils.isEmpty(photo)) {
            profileBinding.profileImage.setImageURI(Uri.parse(photo));
        }
        profileBinding.autoSyncSwitch.setChecked(autoSync);
    }

    private void saveProfile() {
        String name = profileBinding.nameEdit.getText() != null ? profileBinding.nameEdit.getText().toString().trim() : "";
        String email = profileBinding.emailEdit.getText() != null ? profileBinding.emailEdit.getText().toString().trim() : "";

        prefs.saveProfileName(name);
        prefs.saveProfileEmail(email);

        Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();
        finish();
    }

}