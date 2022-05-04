package material.hunter.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;

public class PathsUtil implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static String APP_PATH;
    public static String APP_DATABASE_PATH;
    public static String APP_INITD_PATH;
    public static String APP_SCRIPTS_PATH;
    public static String APP_SCRIPTS_BIN_PATH;
    public static String SD_PATH;
    public static String APP_SD_PATH;
    public static String SYSTEM_PATH;
    public static String ARCH_FOLDER;
    public static String CHROOT_SD_PATH;
    public static String CHROOT_SUDO;
    public static String CHROOT_INITD_SCRIPT_PATH;
    public static String CHROOT_SYMLINK_PATH;
    public static String APP_SD_SQLBACKUP_PATH;
    public static String APP_SD_FILES_IMG_PATH;
    public static String BUSYBOX;
    public static String MAGISK_DB_PATH;
    private static PathsUtil instance;
    private final SharedPreferences sharedPreferences;

    @SuppressLint("SdCardPath")
    private PathsUtil(Context context) {
        sharedPreferences =
            context
                .getApplicationContext()
                .getSharedPreferences("material.hunter", Context.MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        APP_PATH = context.getApplicationContext().getFilesDir().getPath();
        APP_DATABASE_PATH = APP_PATH.replace("/files", "/databases");
        APP_INITD_PATH = APP_PATH + "/etc/init.d";
        APP_SCRIPTS_PATH = APP_PATH + "/scripts";
        APP_SCRIPTS_BIN_PATH = APP_SCRIPTS_PATH + "/bin";
        SD_PATH = getSdcardPath();
        APP_SD_PATH = SD_PATH + "/MaterialHunter";
        APP_SD_SQLBACKUP_PATH = SD_PATH + "/MaterialHunter/sql_backup";
        APP_SD_FILES_IMG_PATH = SD_PATH + "/MaterialHunter/Images";
        SYSTEM_PATH = "/data/local" + "/nhsystem";
        ARCH_FOLDER = sharedPreferences.getString("chroot_directory", "chroot");
        CHROOT_SUDO = "/usr/bin/sudo";
        CHROOT_INITD_SCRIPT_PATH = APP_INITD_PATH + "/80postservices";
        CHROOT_SD_PATH = "/sdcard";
        CHROOT_SYMLINK_PATH = SYSTEM_PATH + "/kalifs";
        BUSYBOX = getBusyboxPath();
        MAGISK_DB_PATH = "/data/adb/magisk.db";
    }

    public static synchronized PathsUtil getInstance(Context context) {
        if (instance == null) {
            instance = new PathsUtil(context);
        }
        return instance;
    }

    public static String CHROOT_PATH() {
        return SYSTEM_PATH + "/" + ARCH_FOLDER;
    }

    private static String getSdcardPath() {
        return Environment.getExternalStorageDirectory().toString();
    }

    public static String getBusyboxPath() {
        String[] BB_PATHS = {"/system/xbin/busybox", "/system/bin/busybox"};
        for (String BB_PATH : BB_PATHS) {
            File busybox = new File(BB_PATH);
            if (busybox.exists()) {
                return BB_PATH;
            }
        }
        return "";
    }

    public static String getBusyboxRaw() {
        String[] BB_PATHS = {"/system/xbin", "/system/bin"};
        for (String BB_RAW : BB_PATHS) {
            File busybox = new File(BB_RAW + "/busybox");
            if (busybox.exists()) {
                return BB_RAW;
            }
        }
        return "";
    }

    public static void showMessage(Context context, String msg, boolean is_long) {
        if (is_long)
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showSnack(View view, String msg, boolean is_long) {
        if (is_long)
            Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show();
        else
            Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("chroot_directory"))
            ARCH_FOLDER = sharedPreferences.getString("chroot_directory", "chroot");
    }

    public void onDestroy() {
        if (sharedPreferences != null)
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }
}