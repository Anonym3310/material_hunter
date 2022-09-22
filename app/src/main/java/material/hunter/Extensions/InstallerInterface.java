package material.hunter.Extensions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;

import material.hunter.MainActivity;
import material.hunter.utils.Checkers;
import material.hunter.utils.PathsUtil;
import material.hunter.utils.PermissionsUtil;
import material.hunter.utils.ShellExecuter;
import material.hunter.version;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InstallerInterface {

    private Activity activity;
    private Context context;
    private File scriptsDir = new File(PathsUtil.APP_SCRIPTS_PATH);
    private File sdcardDir = new File(PathsUtil.APP_SD_PATH);
    private File etcDir = new File(PathsUtil.APP_INITD_PATH);
    private PermissionsUtil permissions;
    private SharedPreferences prefs;
    private ShellExecuter exe = new ShellExecuter();

    public InstallerInterface(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
        this.prefs = context.getSharedPreferences("material.hunter", Context.MODE_PRIVATE);
        this.permissions = new PermissionsUtil(this.activity, this.context);
        if (!sdcardDir.isDirectory() || intAssist() || !scriptsDir.isDirectory() || !etcDir.isDirectory()) {
            copyBootFiles();
            prefs.edit().putInt("version", version.latest).commit();
        }
    }

    private void copyBootFiles() {
        if (!Checkers.isRoot()) {
            return;
        }

        if (!permissions.isAllPermitted(PermissionsUtil.PERMISSIONS)) {
            permissions.requestPermissions(
                    PermissionsUtil.PERMISSIONS, PermissionsUtil.REQUEST_CODE);
        }

        // copy (recursive) of the assets/{scripts, etc, wallpapers} folders to /data/data/...
        assetsToFiles(PathsUtil.APP_PATH, "", "data");
        exe.RunAsRoot(
                new String[] {
                    "chmod -R 700 " + PathsUtil.APP_SCRIPTS_PATH + "/*",
                    "chmod -R 700 " + PathsUtil.APP_INITD_PATH + "/*"
                });

        File mh_folder = new File(PathsUtil.APP_SD_PATH);
        if (!mh_folder.exists()) {
            try {
                mh_folder.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                PathsUtil.showMessage(
                        context,
                        "Failed to create MaterialHunter directory.",
                        false);
                return;
            }
        }

        // Fetch the busybox path again after the busybox_nh is copied.
        PathsUtil.BUSYBOX = PathsUtil.getBusyboxPath();

        // Setup the default SharePreference value.
        if (prefs.getString("chroot_backup_path", null) == null) {
            prefs.edit()
                    .putString("chroot_backup_path", PathsUtil.SD_PATH + "/mh-backup.tar.gz")
                    .apply();
        }
        if (prefs.getString("chroot_restore_path", null) == null) {
            prefs.edit()
                    .putString("chroot_restore_path", PathsUtil.SD_PATH + "/mh-backup.tar.gz")
                    .apply();
        }

        // Request to remove battery optimization mode and request overlay permission for Termux
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) context.getSystemService(context.POWER_SERVICE);
            String packageName = context.getPackageName();
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                Intent intent = new Intent();
                intent.setAction(
                        android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse(String.format("package:%s", packageName)));
                context.startActivity(intent);
            }

            packageName = "com.termux";

            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                Intent intent = new Intent();
                intent.setAction(
                        android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse(String.format("package:%s", packageName)));
                context.startActivity(intent);
            }

            Intent intent = new Intent();
            intent.setAction(
                    android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            PathsUtil.showMessage(context, "Please, grant overlay permission for Termux", true);
        }

        // Request "Manage All Files" permission for Android 11+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent =
                        new Intent(
                                android.provider.Settings
                                        .ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse(String.format("package:%s", context.getPackageName())));
                context.startActivity(intent);
            }
        }
    }

    private Boolean pathIsAllowed(String path, String copyType) {
        if (!path.matches("^(authors|licenses|images|sounds|webkit)")) {
            if (copyType.equals("data")) {
                if (path.equals("")) {
                    return true;
                } else if (path.startsWith("scripts")) {
                    return true;
                } else if (path.startsWith("wallpapers")) {
                    return true;
                } else return path.startsWith("etc");
            }
            return false;
        }
        return false;
    }

    private void assetsToFiles(String TARGET_BASE_PATH, String path, String copyType) {
        AssetManager assetManager = context.getAssets();
        String[] assets;
        try {
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFile(TARGET_BASE_PATH, path);
            } else {
                String fullPath = TARGET_BASE_PATH + "/" + path;
                File dir = new File(fullPath);
                if (!dir.exists() && pathIsAllowed(path, copyType)) {
                    if (!dir.mkdirs()) {
                        ShellExecuter exe = new ShellExecuter();
                        exe.RunAsRoot("mkdir " + fullPath);
                    }
                }
                for (String asset : assets) {
                    String p;
                    if (path.equals("")) {
                        p = "";
                    } else {
                        p = path + "/";
                    }
                    if (pathIsAllowed(path, copyType)) {
                        assetsToFiles(TARGET_BASE_PATH, p + asset, copyType);
                    }
                }
            }
        } catch (IOException ex) {
        }
    }

    private void copyFile(String TARGET_BASE_PATH, String filename) {
        if (filename.matches("^.*/(services|runonboot_services)$")) {
            return;
        }
        AssetManager assetManager = context.getAssets();
        InputStream in;
        OutputStream out;
        String newFileName = null;
        try {
            in = assetManager.open(filename);
            newFileName = TARGET_BASE_PATH + "/" + filename;
            out = new FileOutputStream(renameAssetIfneeded(newFileName));
            byte[] buffer = new byte[8092];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            ShellExecuter copy = new ShellExecuter();
            copy.RunAsRoot(new String[] {"cp " + filename + " " + TARGET_BASE_PATH});
        }
    }

    // This rename the filename which suffix is either [name]-arm64 or [name]-armeabi to [name]
    // according to the user's CPU ABI.
    private String renameAssetIfneeded(String asset) {
        if (asset.matches("^.*-arm64$")) {
            if (Build.CPU_ABI.equals("arm64-v8a")) {
                return (asset.replaceAll("-arm64$", ""));
            }
        } else if (asset.matches("^.*-armeabi$")) {
            if (!Build.CPU_ABI.equals("arm64-v8a")) {
                return (asset.replaceAll("-armeabi$", ""));
            }
        }
        return asset;
    }

    private boolean intAssist() {
        int now = prefs.getInt("version", 0);
        if (version.latest != now) return true;
        else return false;
    }
}