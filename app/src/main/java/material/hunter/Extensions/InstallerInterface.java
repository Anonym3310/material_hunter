package material.hunter.Extensions;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import material.hunter.AppNavHomeActivity;
import material.hunter.version;
import material.hunter.SQL.MaterialHunterSQL;
import material.hunter.SQL.ServicesSQL;
import material.hunter.SQL.USBArmorySQL;
import material.hunter.utils.CheckForRoot;
import material.hunter.utils.NhPaths;
import material.hunter.utils.SharePrefTag;
import material.hunter.utils.ShellExecuter;

public class InstallerInterface {
  private Context context;
  private File scriptsDir = new File(NhPaths.APP_SCRIPTS_PATH);
  private File etcDir = new File(NhPaths.APP_INITD_PATH);
  private SharedPreferences prefs;
  private ShellExecuter exe = new ShellExecuter();

  public InstallerInterface(Context context, Activity activity) {
    this.context = context;
    this.scriptsDir = new File(NhPaths.APP_SCRIPTS_PATH);
    this.etcDir = new File(NhPaths.APP_INITD_PATH);
    this.prefs = context.getSharedPreferences("material.hunter", Context.MODE_PRIVATE);
  }

  public void Install() {
    if (intAssist() || !scriptsDir.isDirectory() || !etcDir.isDirectory()) {
      copyBootFiles();
	  prefs.edit().putInt(
        "version", version.latest
      ).commit();
    }
  }

  private void copyBootFiles() {
    if (CheckForRoot.isRoot()) {
      prefs.edit().putBoolean(AppNavHomeActivity.CHROOT_INSTALLED_TAG, false).apply();
      return;
    }
    // copy (recursive) of the assets/{scripts, etc, wallpapers} folders to /data/data/...
    assetsToFiles(NhPaths.APP_PATH, "", "data");
    exe.RunAsRoot(
        new String[] {
          "chmod -R 700 " + NhPaths.APP_SCRIPTS_PATH + "/*",
          "chmod -R 700 " + NhPaths.APP_INITD_PATH + "/*"
        });
    // disable the magisk notification for materialhunter app as it will keep popping up bunch of
    // toast message when executing runtime command.
    disableMagiskNotification();

    String command =
        "if [ -d " + NhPaths.CHROOT_PATH() + " ]; then echo Exists; fi"; // check the dir existence
    final String _res = exe.RunAsRootOutput(command);
    if (_res.equals("Exists")) {
      prefs.edit()
        .putBoolean(AppNavHomeActivity.CHROOT_INSTALLED_TAG, true)
        .commit();

      // Mount suid /data && fix sudo - this is definitely needed as of 02/2020, Re4son
      exe.RunAsRoot(
          new String[]{
    	  NhPaths.BUSYBOX
              + " mount -o remount,suid /data && chmod +s "
              + NhPaths.CHROOT_PATH()
              + "/usr/bin/sudo"
              + " && echo \"Initial setup done!\""});
    }

    // Fetch the busybox path again after the busybox_nh is copied.
    NhPaths.BUSYBOX = NhPaths.getBusyboxPath();

    // Now Initiate all SQL singleton in MainActivity so that it can be less lagged when switching fragments,
    // because it takes time to retrieve data from database.
    MaterialHunterSQL.getInstance(context);
    ServicesSQL.getInstance(context);
    USBArmorySQL.getInstance(context);

    // Setup the default SharePreference value.
    if (prefs.getString(SharePrefTag.CHROOT_DEFAULT_BACKUP_SHAREPREF_TAG, null) == null) {
      prefs
          .edit()
          .putString(
              SharePrefTag.CHROOT_DEFAULT_BACKUP_SHAREPREF_TAG,
              NhPaths.SD_PATH + "/mh-backup.tar.gz")
          .apply();
    }
    if (prefs.getString(SharePrefTag.CHROOT_DEFAULT_STORE_DOWNLOAD_SHAREPREF_TAG, null) == null) {
      prefs.edit().putString(SharePrefTag.CHROOT_DEFAULT_STORE_DOWNLOAD_SHAREPREF_TAG, "");
    }

    // Grant "Manage All Files" permission for Android 11+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      if (!Environment.isExternalStorageManager()){
        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setData(Uri.parse(String.format("package:%s", context.getPackageName())));
        context.startActivity(intent);
      }
    }
  }

  private Boolean pathIsAllowed(String path, String copyType) {
    if (!path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit")) {
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
            ShellExecuter create = new ShellExecuter();
            create.RunAsRoot(new String[] {"mkdir " + fullPath});
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
    } catch (IOException ex) { }
  }

  private void copyFile(String TARGET_BASE_PATH, String filename) {
    if (filename.matches("^.*/services$|^.*/runonboot_services$")) {
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

  // This rename the filename which suffix is either [name]-arm64 or [name]-armhf to [name]
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
    int now = prefs.getInt("version", 10);
	if (now < version.latest) return true;
	else return false;
  }

  private void disableMagiskNotification() {
    if (exe.RunAsRootReturnValue("[ -f " + NhPaths.MAGISK_DB_PATH + " ]") == 0) {
      if (exe.RunAsRootOutput(
              NhPaths.APP_SCRIPTS_BIN_PATH
                  + "/sqlite3 "
                  + NhPaths.MAGISK_DB_PATH
                  + " \"UPDATE policies SET logging='0',notification='0' WHERE package_name='"
                  + "material.hunter"
                  + "';\"")
          .isEmpty()) { }
    } else { }
  }
}