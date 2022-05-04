package material.hunter.service;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import androidx.annotation.Nullable;

import material.hunter.AppNavHomeActivity;
import material.hunter.R;
import material.hunter.utils.CheckForRoot;
import material.hunter.utils.PathsUtil;
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

        checkCompat();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = AppNavHomeActivity.context.getSharedPreferences("material.hunter", MODE_PRIVATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    private void checkCompat() {
        if (AppNavHomeActivity.lastSelectedMenuItem != null && AppNavHomeActivity.lastSelectedMenuItem.getItemId() != R.id.createchroot_item) {
            if (!CheckForRoot.isRoot()) {
                message = "Root permission isn't granted.";
                AppNavHomeActivity.context.sendBroadcast(
	                new Intent().putExtra("message", message).putExtra("cancelable", false).setAction("material.hunter.CHECKCOMPAT"));
                return;
            }
        }

        // All of the code start from here will always be executed every time this service is
        // started.
        // And remember no any return true or false except the last line of this function.
        /* Other compat checks start from here */

        // Check only for the first installation, find out the possible chroot folder and point it
        // to the chroot path.
        if (sharedPreferences.getString("chroot_directory", null) == null) {
            String[] chrootDirs =
                new ShellExecuter()
                    .RunAsRootOutput(PathsUtil.APP_SCRIPTS_PATH + "/chrootmgr -c \"findchroot\"")
                        .split("\\n");
            // if findchroot returns empty string then default the chroot path to chroot, else
            // default to the first valid chroot directory.
            if (chrootDirs[0].equals("")) {
                sharedPreferences
                    .edit()
                    .putString("chroot_directory", "chroot")
                    .commit();
                sharedPreferences
                    .edit()
                    .putString("chroot_directory_path", PathsUtil.SYSTEM_PATH + "/chroot")
                    .commit();
                new ShellExecuter()
                    .RunAsRootOutput("ln -sfn " + PathsUtil.SYSTEM_PATH + "/chroot " + PathsUtil.CHROOT_SYMLINK_PATH);
            } else {
                sharedPreferences
                    .edit()
                    .putString("chroot_directory", chrootDirs[0])
                    .commit();
                sharedPreferences
                    .edit()
                    .putString("chroot_directory_path", PathsUtil.SYSTEM_PATH + "/" + chrootDirs[0])
                    .commit();
                new ShellExecuter()
                    .RunAsRootOutput("ln -sfn " + PathsUtil.SYSTEM_PATH + "/" + chrootDirs[0] + " " + PathsUtil.CHROOT_SYMLINK_PATH);
            }
        }

        // Check chroot status, push notification to user and disable all the fragments if chroot is
        // not yet up.
        // if intent is NOT sent by chrootmanager, run the check asynctask again.
        final int status = new ShellExecuter().RunAsRootReturnValue(
            PathsUtil.APP_SCRIPTS_PATH + "/chrootmgr -c \"status\" -p " + PathsUtil.CHROOT_PATH());
        if (RESULTCODE == -1) {
            if (status != 0) {
                if (status == 3) {
                    startService(
                        new Intent(AppNavHomeActivity.context, NotificationChannelService.class)
                        .setAction(NotificationChannelService.CHROOT_CORRUPTED));
                    AppNavHomeActivity.context.sendBroadcast(
                        new Intent()
                        .setAction(AppNavHomeActivity.MaterialHunterReceiver.CHROOT_CORRUPTED));
	        	} else if (AppNavHomeActivity.lastSelectedMenuItem != null && AppNavHomeActivity.lastSelectedMenuItem.getItemId() != R.id.createchroot_item) {
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
        } else if (RESULTCODE != 0) {
            // if intent is sent by chrootmanager, no need to run the check asynctask again.
            if (AppNavHomeActivity.lastSelectedMenuItem != null && AppNavHomeActivity.lastSelectedMenuItem.getItemId() != R.id.createchroot_item) {
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
}