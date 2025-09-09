package utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class User extends Application {

    private String userName;
    private String userId;
    private String userEmail;

    private static User instance;

    public static User getInstance(){

        if (instance == null){
            instance = new User();
        }

        return instance;

    }

    public User() { }

    @Override
    public void onCreate() {
        super.onCreate();
        // Apply persisted locale at process start
        LocaleManager.initializeLocale(this);

        // Apply saved theme at process start so first activity uses it
        int themeResId = SharedPrefsManager.getInstance(this).getAppTheme();
        setTheme(themeResId);

        // Initialize and apply saved font scale globally
        FontScaleUtil.initialize(this);

        // Initialize Firebase / Firestore
        FirebaseProvider.initialize(this);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                track(activity);
            }
            @Override public void onActivityStarted(Activity activity) { }
            @Override public void onActivityResumed(Activity activity) { }
            @Override public void onActivityPaused(Activity activity) { }
            @Override public void onActivityStopped(Activity activity) { }
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }
            @Override public void onActivityDestroyed(Activity activity) { }
        });
    }

    private static final List<WeakReference<Activity>> openActivities = new ArrayList<>();

    private static void track(Activity activity) {
        openActivities.add(new WeakReference<>(activity));
    }

    public static void recreateAllActivities() {
        for (WeakReference<Activity> ref : openActivities) {
            Activity a = ref.get();
            if (a != null) {
                a.recreate();
            }
        }
    }

    public User(String userName, String userId, String userEmail) {
        this.userName = userName;
        this.userId = userId;
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public static void setInstance(User instance) {
        User.instance = instance;
    }
}
