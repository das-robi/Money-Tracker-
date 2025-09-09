package utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsManager {

    private static final String PREF_NAME = "MoneyTrackerPrefs";
    private static final String KEY_APP_LANGUAGE = "app_language";
    private static final String KEY_DEFAULT_CURRENCY = "default_currency";
    private static final String KEY_LAST_RATES_UPDATE = "last_rates_update";
    private static final String KEY_PROFILE_NAME = "profile_name";
    private static final String KEY_PROFILE_EMAIL = "profile_email";
    private static final String KEY_PROFILE_PHOTO_URI = "profile_photo_uri";
    private static final String KEY_AUTO_SYNC_ENABLED = "auto_sync_enabled";
    private static final String KEY_APP_THEME = "app_theme";
    private static final String KEY_APP_FONT_SCALE = "app_font_scale";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_USER_JOINED = "user_joined";
    private static final String KEY_USER_PHOTO_URI = "user_photo_uri";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_LAST_SYNCED_AT = "last_synced_at";

    private static SharedPrefsManager instance;
    private SharedPreferences sharedPreferences;

    private SharedPrefsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefsManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefsManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Save default currency
     */
    public void saveDefaultCurrency(String currency) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_DEFAULT_CURRENCY, currency);
        editor.apply();
    }

    /**
     * Get default currency
     */
    public String getDefaultCurrency() {
        return sharedPreferences.getString(KEY_DEFAULT_CURRENCY, "BDT");
    }

    /**
     * Save app language (e.g., en, bn, hi, ar, es).
     */
    public void saveAppLanguage(String languageCode) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_APP_LANGUAGE, languageCode);
        editor.apply();
    }

    /**
     * Get app language with English as fallback.
     */
    public String getAppLanguage() {
        return sharedPreferences.getString(KEY_APP_LANGUAGE, "en");
    }

    /**
     * Save last rates update time
     */
    public void saveLastRatesUpdate(long timestamp) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_LAST_RATES_UPDATE, timestamp);
        editor.apply();
    }

    /**
     * Get last rates update time
     */
    public long getLastRatesUpdate() {
        return sharedPreferences.getLong(KEY_LAST_RATES_UPDATE, 0);
    }

    /**
     * Profile: Name
     */
    public void saveProfileName(String name) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PROFILE_NAME, name);
        editor.apply();
    }

    public String getProfileName() {
        return sharedPreferences.getString(KEY_PROFILE_NAME, "");
    }

    /**
     * Profile: Email
     */
    public void saveProfileEmail(String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PROFILE_EMAIL, email);
        editor.apply();
    }

    public String getProfileEmail() {
        return sharedPreferences.getString(KEY_PROFILE_EMAIL, "");
    }

    /**
     * Profile: Photo Uri
     */
    public void saveProfilePhotoUri(String uri) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PROFILE_PHOTO_URI, uri);
        editor.apply();
    }

    public String getProfilePhotoUri() {
        return sharedPreferences.getString(KEY_PROFILE_PHOTO_URI, "");
    }

    /**
     * Auto Sync toggle
     */
    public void setAutoSyncEnabled(boolean enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_AUTO_SYNC_ENABLED, enabled);
        editor.apply();
    }

    public boolean isAutoSyncEnabled() {
        return sharedPreferences.getBoolean(KEY_AUTO_SYNC_ENABLED, false);
    }

    /**
     * Persist selected app theme (style resource id)
     */
    public void saveAppTheme(int themeResId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_APP_THEME, themeResId);
        editor.apply();
    }

    /**
     * Get saved app theme, falling back to default blue theme
     */
    public int getAppTheme() {
        return sharedPreferences.getInt(KEY_APP_THEME, com.devrobin.moneytracker.R.style.Theme_MoneyTracker);
    }

    /**
     * Persist selected app font scale (e.g., 0.85f, 1.0f, 1.15f)
     */
    public void saveAppFontScale(float scale) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(KEY_APP_FONT_SCALE, scale);
        editor.apply();
    }

    /**
     * Get saved app font scale, falling back to medium (1.0f)
     */
    public float getAppFontScale() {
        return sharedPreferences.getFloat(KEY_APP_FONT_SCALE, 1.0f);
    }

    /**
     * Persist and retrieve signed-in user profile info
     */
    public void saveUserName(String name) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "Guest User");
    }

    public void saveUserEmail(String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "guest@example.com");
    }

    public void saveUserPhone(String phone) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_PHONE, phone);
        editor.apply();
    }

    public String getUserPhone() {
        return sharedPreferences.getString(KEY_USER_PHONE, "");
    }

    public void saveUserJoined(long timestamp) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_USER_JOINED, timestamp);
        editor.apply();
    }

    public long getUserJoined() {
        return sharedPreferences.getLong(KEY_USER_JOINED, 0L);
    }

    public void saveUserPhotoUri(String uri) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_PHOTO_URI, uri);
        editor.apply();
    }

    public String getUserPhotoUri() {
        return sharedPreferences.getString(KEY_USER_PHOTO_URI, null);
    }

    public void setLoggedIn(boolean loggedIn) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, loggedIn);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // (Auto Sync handled above)

    /**
     * Last synced timestamp
     */
    public void setLastSyncedAt(long timestamp) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_LAST_SYNCED_AT, timestamp);
        editor.apply();
    }

    public long getLastSyncedAt() {
        return sharedPreferences.getLong(KEY_LAST_SYNCED_AT, 0L);
    }

    /**
     * Clear all preferences
     */
    public void clearAll() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}


