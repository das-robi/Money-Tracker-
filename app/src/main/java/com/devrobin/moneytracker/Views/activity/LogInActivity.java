package com.devrobin.moneytracker.Views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.devrobin.moneytracker.databinding.ActivityLogInBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LogInActivity extends AppCompatActivity {

    private ActivityLogInBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLogInBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());


        firebaseAuth = FirebaseAuth.getInstance();

        // Prefill saved email if available
        String savedEmail = utils.SharedPrefsManager.getInstance(this).getUserEmail();
        if (!TextUtils.isEmpty(savedEmail) && !"guest@example.com".equals(savedEmail)) {
            binding.logEmail.setText(savedEmail);
            binding.checkBox.setChecked(true);
        }

        binding.btnlog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                attemptLogin();

            }
        });

        binding.regTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent goActivity = new Intent(LogInActivity.this, RegisterActivity.class);
                startActivity(goActivity);

            }
        });



    }

    private void attemptLogin() {
        String email = binding.logEmail.getText() != null ? binding.logEmail.getText().toString().trim() : "";
        String password = binding.logPaswrd.getText() != null ? binding.logPaswrd.getText().toString() : "";

        boolean validEmail = validateEmail(email);
        boolean validPassword = validatePassword(password);

        if (!validEmail || !validPassword) {
            return;
        }

        // Disable button to prevent multiple taps
        binding.btnlog.setEnabled(false);
        binding.btnlog.setAlpha(0.6f);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        binding.btnlog.setEnabled(true);
                        binding.btnlog.setAlpha(1f);

                        if (task.isSuccessful()) {
                            utils.SharedPrefsManager prefs = utils.SharedPrefsManager.getInstance(LogInActivity.this);
                            prefs.setLoggedIn(true);
                            if (binding.checkBox.isChecked()) {
                                prefs.saveUserEmail(email);
                            } else {
                                prefs.saveUserEmail("guest@example.com");
                            }

                            // Try to capture Firebase user's display name/email
                            FirebaseUser fbUser = firebaseAuth.getCurrentUser();
                            if (fbUser != null) {
                                String displayName = fbUser.getDisplayName();
                                String userEmail = fbUser.getEmail();
                                if (displayName != null && !displayName.isEmpty()) {
                                    prefs.saveUserName(displayName);
                                }
                                if (userEmail != null && !userEmail.isEmpty()) {
                                    prefs.saveUserEmail(userEmail);
                                }
                                if (fbUser.getPhotoUrl() != null) {
                                    prefs.saveUserPhotoUri(fbUser.getPhotoUrl().toString());
                                }
                            }

                            Intent logIntent = new Intent(LogInActivity.this, MainActivity.class);
                            startActivity(logIntent);
                            finish();
                        } else {
                            String message = task.getException() != null ? task.getException().getMessage() : "Login failed";
                            Toast.makeText(LogInActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            binding.logEmail.setError("Email is required");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.logEmail.setError("Enter a valid email address");
            return false;
        }
        String lower = email.toLowerCase();
        if (isLikelyFakeEmail(lower)) {
            binding.logEmail.setError("Please use your real email address");
            return false;
        }
        if (isDisposableDomain(lower)) {
            binding.logEmail.setError("Disposable email domains are not allowed");
            return false;
        }
        // Clear any previous error
        binding.logEmail.setError(null);
        return true;
    }

    private boolean validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            binding.logPaswrd.setError("Password is required");
            return false;
        }
        if (password.length() < 8) {
            binding.logPaswrd.setError("At least 8 characters");
            return false;
        }
        boolean hasLetter = password.matches(".*[A-Za-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        if (!hasLetter || !hasDigit) {
            binding.logPaswrd.setError("Use letters and numbers");
            return false;
        }
        binding.logPaswrd.setError(null);
        return true;
    }

    private boolean isLikelyFakeEmail(String emailLower) {
        // quick checks for common placeholders
        String localPart = emailLower.contains("@") ? emailLower.substring(0, emailLower.indexOf('@')) : emailLower;
        if (localPart.length() <= 2) return true;
        String[] fakeHints = new String[]{
                "test", "testing", "example", "fake", "asdf", "qwerty", "noemail", "noreply", "temp"
        };
        for (String hint : fakeHints) {
            if (localPart.contains(hint)) {
                return true;
            }
        }
        // domain sanity
        if (!emailLower.contains("@")) return true;
        String domain = emailLower.substring(emailLower.indexOf('@') + 1);
        if (!domain.contains(".")) return true; // require TLD
        String[] parts = domain.split("\\.");
        String tld = parts[parts.length - 1];
        return tld.length() < 2;
    }

    private boolean isDisposableDomain(String emailLower) {
        String[] disposable = new String[]{
                "mailinator.com", "10minutemail.com", "tempmail.com", "guerrillamail.com",
                "yopmail.com", "trashmail.com", "getnada.com", "dispostable.com"
        };
        int at = emailLower.indexOf('@');
        if (at == -1) return true;
        String domain = emailLower.substring(at + 1);
        for (String d : disposable) {
            if (domain.equals(d) || domain.endsWith("." + d)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = firebaseAuth.getCurrentUser();


        if (currentUser != null){
            Intent goHome = new Intent(LogInActivity.this, MainActivity.class);
            startActivity(goHome);
            finish();
        }

    }
}