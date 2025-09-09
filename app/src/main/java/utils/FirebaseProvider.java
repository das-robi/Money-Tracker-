package utils;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class FirebaseProvider {
    private static volatile boolean initialized = false;
    private static FirebaseFirestore firestoreInstance;

    private FirebaseProvider() { }

    public static synchronized void initialize(@NonNull Context context) {
        if (initialized) return;

        FirebaseApp.initializeApp(context);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        firestore.setFirestoreSettings(settings);

        firestoreInstance = firestore;
        initialized = true;
    }

    public static FirebaseFirestore getFirestore() {
        if (!initialized) {
            throw new IllegalStateException("FirebaseProvider not initialized. Call initialize(context) in Application.onCreate().");
        }
        return firestoreInstance;
    }
}