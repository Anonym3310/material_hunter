package material.hunter.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import androidx.annotation.Nullable;
import material.hunter.AppNavHomeActivity;
import material.hunter.R;
import material.hunter.utils.CheckForRoot;
import material.hunter.utils.NhPaths;
import material.hunter.utils.SharePrefTag;
import material.hunter.utils.ShellExecuter;

// IntentService class for keep checking the campatibaility every time user switch back to the app.
public class CompatCheckService extends IntentService {

  private String message = "";
  private int RESULTCODE = -1;
  private SharedPreferences sharedPreferences;

  public CompatCheckService() {
    super("CompatCheckService");
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return super.onBind(intent);
  }

  @Override
  protected void onHandleIntent(@Nullable Intent intent) {
    // if no resultCode passed by ChrootManagerFragment, then set RESULTCODE to -1;
    if (intent != null) {
      RESULTCODE = intent.getIntExtra("RESULTCODE", -1);
    }

    // run checkCompat function, and sendbroadcast back to Main activity if user fails the
    // compat check.
    if (!checkCompat()) {
      AppNavHomeActivity.context.sendBroadcast(
          new Intent().putExtra("message", message).setAction("material.hunter.CHECKCOMPAT"));
    }
  }

  @Override
  public void onCreate() {
    super.onCreate();
    sharedPreferences =
        AppNavHomeActivity.context.getSharedPreferences("material.hunter", MODE_PRIVATE);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  @Override
  public void onTaskRemoved(Intent rootIntent) {
    super.onTaskRemoved(rootIntent);
  }

  private boolean checkCompat() {
    // First, check for root access
    if (CheckForRoot.isRoot()) {
      message = "Root permission isn't granted.";
      return false;
    }
    // Second, check if selinux enforced
    if (CheckForRoot.isEnforce()) {
      message = "Selinux enforced. You can't use MH now.";
      return false;
    }

    // All of the code start from here will always be executed every time this service is
    // started.
    // And remember no any return true or false except the last line of this function.
    /* Other compat checks start from here */

    // Check only for the first installation, find out the possible chroot folder and point it
    // to the chroot path.
    if (sharedPreferences.getString(SharePrefTag.CHROOT_ARCH_SHAREPREF_TAG, null) == null) {
      String[] chrootDirs =
          new ShellExecuter()
              .RunAsRootOutput(NhPaths.APP_SCRIPTS_PATH + "/chrootmgr -c \"findchroot\"")
                  .split("\\n");
      // if findchroot returns empty string then default the chroot arch to kali-arm64, else
      // default to the first valid chroot arch.
      if (chrootDirs[0].equals("")) {
        sharedPreferences
            .edit()
            .putString(SharePrefTag.CHROOT_ARCH_SHAREPREF_TAG, "chroot")
            .apply();
        sharedPreferences
            .edit()
            .putString(SharePrefTag.CHROOT_PATH_SHAREPREF_TAG, NhPaths.NH_SYSTEM_PATH + "/chroot")
            .apply();
        new ShellExecuter()
            .RunAsRootOutput(
                "ln -sfn " + NhPaths.NH_SYSTEM_PATH + "/chroot " + NhPaths.CHROOT_SYMLINK_PATH);
      } else {
        sharedPreferences
            .edit()
            .putString(SharePrefTag.CHROOT_ARCH_SHAREPREF_TAG, chrootDirs[0])
            .apply();
        sharedPreferences
            .edit()
            .putString(
                SharePrefTag.CHROOT_PATH_SHAREPREF_TAG,
                NhPaths.NH_SYSTEM_PATH + "/" + chrootDirs[0])
            .apply();
        new ShellExecuter()
            .RunAsRootOutput(
                "ln -sfn "
                    + NhPaths.NH_SYSTEM_PATH
                    + "/"
                    + chrootDirs[0]
                    + " "
                    + NhPaths.CHROOT_SYMLINK_PATH);
      }
    }

    // Check chroot status, push notification to user and disable all the fragments if chroot is
    // not yet up.
    // if intent is NOT sent by chrootmanager, run the check asynctask again.
    if (RESULTCODE == -1) {
      if ((new ShellExecuter()
              .RunAsRootReturnValue(
                  NhPaths.APP_SCRIPTS_PATH + "/chrootmgr -c \"status\" -p " + NhPaths.CHROOT_PATH())
          != 0)) {
        if (AppNavHomeActivity.lastSelectedMenuItem.getItemId() != R.id.createchroot_item) {
          startService(
              new Intent(AppNavHomeActivity.context, NotificationChannelService.class)
                  .setAction(NotificationChannelService.REMINDMOUNTCHROOT));
          AppNavHomeActivity.context.sendBroadcast(
              new Intent()
                  .putExtra("ENABLEFRAGMENT", false)
                  .setAction(AppNavHomeActivity.MaterialHunterReceiver.CHECKCHROOT));
        } else {
          sendBroadcast(
              new Intent()
                  .putExtra("ENABLEFRAGMENT", false)
                  .setAction(AppNavHomeActivity.MaterialHunterReceiver.CHECKCHROOT));
        }
      } else {
        AppNavHomeActivity.context.sendBroadcast(
            new Intent()
                .putExtra("ENABLEFRAGMENT", true)
                .setAction(AppNavHomeActivity.MaterialHunterReceiver.CHECKCHROOT));
      }
    } else {
      // if intent is sent by chrootmanager, no need to run the check asynctask again.
      if (RESULTCODE != 0) {
        if (AppNavHomeActivity.lastSelectedMenuItem.getItemId() != R.id.createchroot_item) {
          startService(
              new Intent(AppNavHomeActivity.context, NotificationChannelService.class)
                  .setAction(NotificationChannelService.REMINDMOUNTCHROOT));
          AppNavHomeActivity.context.sendBroadcast(
              new Intent()
                  .putExtra("ENABLEFRAGMENT", false)
                  .setAction(AppNavHomeActivity.MaterialHunterReceiver.CHECKCHROOT));
        } else {
          sendBroadcast(
              new Intent()
                  .putExtra("ENABLEFRAGMENT", false)
                  .setAction(AppNavHomeActivity.MaterialHunterReceiver.CHECKCHROOT));
        }
      } else {
        AppNavHomeActivity.context.sendBroadcast(
            new Intent()
                .putExtra("ENABLEFRAGMENT", true)
                .setAction(AppNavHomeActivity.MaterialHunterReceiver.CHECKCHROOT));
      }
    }
    return true;
  }
}
