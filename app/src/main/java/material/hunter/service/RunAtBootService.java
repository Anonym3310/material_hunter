package material.hunter.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import java.util.HashMap;
import java.util.Map;
import material.hunter.AppNavHomeActivity;
import material.hunter.R;
import material.hunter.utils.CheckForRoot;
import material.hunter.utils.NhPaths;
import material.hunter.utils.SharePrefTag;
import material.hunter.utils.ShellExecuter;

public class RunAtBootService extends JobIntentService {

  static final int SERVICE_JOB_ID = 1;
  private static final String TAG = "MaterialHunter: Startup";
  private NotificationCompat.Builder n = null;
  private Context context;

  public static void enqueueWork(Context context, Intent work) {
    enqueueWork(context, RunAtBootService.class, SERVICE_JOB_ID, work);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    context = getApplicationContext();
    NhPaths.getInstance(context);
    createNotificationChannel();
  }

  private void doNotification(String contents) {
    if (n == null) {
      n = new NotificationCompat.Builder(context, AppNavHomeActivity.BOOT_CHANNEL_ID);
    }
    n.setStyle(new NotificationCompat.BigTextStyle().bigText(contents))
        .setContentTitle(RunAtBootService.TAG)
        .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
        .setAutoCancel(true);
    NotificationManager notificationManager =
        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    if (notificationManager != null) {
      notificationManager.notify(999, n.build());
    }
  }

  @Override
  protected void onHandleWork(@NonNull Intent intent) {
    onHandleIntent(intent);
  }

  protected void onHandleIntent(@NonNull Intent intent) {
    SharedPreferences o = this.getSharedPreferences("material.hunter", Context.MODE_PRIVATE);
    if (o.getBoolean(SharePrefTag.BOOT_RECIVIE, true)) {
      // 1. Check root -> 2. Run materialhunter init.d files. -> 3. Push notifications.
      String isOK = "ok";
      doNotification("Doing boot checks...");

      HashMap<String, String> hashMap = new HashMap<>();
      hashMap.put("ROOT", "access isn't granted.");
      hashMap.put("SELINUX", "enforcing.");
      hashMap.put("CHROOT", "isn't yet installed.");

      if (CheckForRoot.isRoot()) {
        hashMap.put("ROOT", isOK);
      }

      ShellExecuter exe = new ShellExecuter();
      exe.RunAsRootOutput(NhPaths.BUSYBOX + " run-parts " + NhPaths.APP_INITD_PATH);
      if (exe.RunAsRootReturnValue(NhPaths.APP_SCRIPTS_PATH + "/chrootmgr -c \"status\"") == 0) {
        exe.RunAsRootOutput("rm -rf " + NhPaths.CHROOT_PATH() + "/tmp/.X1*");
        hashMap.put("CHROOT", isOK);
      }

      if (!CheckForRoot.isEnforce()) {
        hashMap.put("SELINUX", isOK);
	  }

      String resultMsg = "Boot completed.\nEveryting is fine and chroot has been started.";
      for (Map.Entry<String, String> entry : hashMap.entrySet()) {
        if (!entry.getValue().equals(isOK)) {
          resultMsg = "Make sure the above requirements are met.";
          break;
        }
      }
      doNotification(
          "Root: "
              + hashMap.get("ROOT")
              + "\n"
              + "Selinux: "
              + hashMap.get("SELINUX")
              + "\n"
              + "Chroot: "
              + hashMap.get("CHROOT")
              + "\n"
              + resultMsg);
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  private void createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel serviceChannel =
          new NotificationChannel(
              AppNavHomeActivity.BOOT_CHANNEL_ID,
              "MaterialHunter Boot Check Service",
              NotificationManager.IMPORTANCE_HIGH);

      NotificationManager notificationManager = getSystemService(NotificationManager.class);
      if (notificationManager != null) {
        notificationManager.createNotificationChannel(serviceChannel);
      }
    }
  }
}
