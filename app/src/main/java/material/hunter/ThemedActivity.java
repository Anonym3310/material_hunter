package material.hunter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.color.DynamicColors;

import java.util.HashMap;

public class ThemedActivity extends AppCompatActivity {

    private static HashMap<Activity, Resources.Theme> activities =
            new HashMap<Activity, Resources.Theme>();
    private static boolean dynamicColorsEnabled = false;
    private static int mTargetTheme = 0;
    private SharedPreferences prefs;

    private static void setDynamicColorsEnabled(boolean b) {
        dynamicColorsEnabled = b;
    }

    public static boolean isDynamicColorsEnabled() {
        return dynamicColorsEnabled;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences("material.hunter", Context.MODE_PRIVATE);
        setDynamicColorsEnabled(prefs.getBoolean("enable_monet", false));
        apply(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        removeActivity(this);
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private boolean apply(Activity activity) {
        if (activities.containsKey(activity)) return false;
        else {
            activities.put(activity, activity.getTheme());
            boolean useDynamicColors =
                    isDynamicColorsEnabled() && DynamicColors.isDynamicColorAvailable();
            if (useDynamicColors) {
                setTheme(R.style.ThemeM3_MaterialHunter);
            } else {
                setTheme(R.style.Theme_MaterialHunter);
            }
            return true;
        }
    }

    private boolean removeActivity(Activity activity) {
        if (activities.containsKey(activity)) {
            activities.remove(activity, activity.getTheme());
            return true;
        } else return false;
    }

    public void sync() {
        activities.forEach(
                (activity, theme) -> {
                    activity.recreate();
                });
    }
}