package material.hunter.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import material.hunter.BuildConfig;
import material.hunter.R;

import java.io.File;


public class NhPaths implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "NhPaths";
    public static String APP_PATH;
    public static String APP_DATABASE_PATH;
    public static String APP_INITD_PATH;
    public static String APP_SCRIPTS_PATH;
    public static String APP_SCRIPTS_BIN_PATH;
    public static String NH_SD_FOLDER_NAME;
    public static String SD_PATH;
    public static String APP_SD_FILES_PATH;
    public static String NH_SYSTEM_PATH;
    public static String ARCH_FOLDER;
    public static String CHROOT_SD_PATH;
    public static String CHROOT_SUDO;
    public static String CHROOT_INITD_SCRIPT_PATH;
    public static String CHROOT_SYMLINK_PATH;
    public static String APP_SD_SQLBACKUP_PATH;
    public static String APP_SD_FILES_IMG_PATH;
    public static String BUSYBOX;
    public static String MAGISK_DB_PATH;
    private static NhPaths instance;
    private static String BASE_PATH;
    private SharedPreferences sharedPreferences;
    private ShellExecuter exe;

    private NhPaths(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        APP_PATH = context.getApplicationContext().getFilesDir().getPath();
        APP_DATABASE_PATH = APP_PATH.replace("/files", "/databases");
        APP_INITD_PATH = APP_PATH + "/etc/init.d";
        APP_SCRIPTS_PATH = APP_PATH + "/scripts";
        APP_SCRIPTS_BIN_PATH = APP_SCRIPTS_PATH + "/bin";
        SD_PATH = getSdcardPath();
        NH_SD_FOLDER_NAME = "nh_files";
        APP_SD_FILES_PATH = SD_PATH + "/" + NH_SD_FOLDER_NAME;
        APP_SD_FILES_IMG_PATH = APP_SD_FILES_PATH + "/diskimage";
        APP_SD_SQLBACKUP_PATH = APP_SD_FILES_PATH + "/nh_sql_backups";
        BASE_PATH = "/data/local";
        NH_SYSTEM_PATH = BASE_PATH + "/nhsystem";
        ARCH_FOLDER = sharedPreferences.getString(SharePrefTag.CHROOT_ARCH_SHAREPREF_TAG, "kali-arm64");
        CHROOT_SUDO = "/usr/bin/sudo";
        CHROOT_INITD_SCRIPT_PATH = APP_INITD_PATH + "/80postservices";
        CHROOT_SD_PATH = "/sdcard";
        CHROOT_SYMLINK_PATH = NH_SYSTEM_PATH + "/kalifs";
        BUSYBOX = getBusyboxPath();
        MAGISK_DB_PATH = "/data/adb/magisk.db";
    }

    public synchronized static NhPaths getInstance(Context context) {
        if (instance == null) {
            instance = new NhPaths(context);
        }
        return instance;
    }

    public static String CHROOT_PATH() {
        return NH_SYSTEM_PATH + "/" + ARCH_FOLDER;
    }

    private static String getSdcardPath() {
        return Environment.getExternalStorageDirectory().toString();
    }

    public static String getBusyboxPath() {
        String[] BB_PATHS = {
                "/system/xbin/busybox_nh",
                "/system/bin/busybox_nh",
                APP_SCRIPTS_BIN_PATH + "/busybox_nh"
        };
        for (String BB_PATH : BB_PATHS) {
            File busybox = new File(BB_PATH);
            if (busybox.exists()) {
                return BB_PATH;
            }
        }
        return "";
    }

    public static String makeTermTitle(String title) {
        return "echo -ne \"\\033]0;" + title + "\\007\" && clear;";
    }

    public static void showMessage(Context context, String msg) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    public static void showMessage_long(Context context, String msg) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    // Material Feature
    public static void showSnack(View view, String msg, int duration) {
        if (duration == 1) {
            Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show();
        } else if (duration == 2) {
            Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show();
        }
    }
    //

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SharePrefTag.CHROOT_ARCH_SHAREPREF_TAG)) {
            ARCH_FOLDER = sharedPreferences.getString(SharePrefTag.CHROOT_ARCH_SHAREPREF_TAG, "kali-arm64");
        }
    }

    public void onDestroy() {
        if (sharedPreferences != null) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }
    }
}