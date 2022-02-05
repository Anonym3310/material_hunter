package material.hunter.service;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import material.hunter.AppNavHomeActivity;
import material.hunter.AsyncTask.CustomCommandsAsyncTask;
import material.hunter.R;

public class NotificationChannelService extends IntentService {
  public static final String CHANNEL_ID = "MaterialHunterNotifyChannel";
  public static final int NOTIFY_ID = 1002;
  public static final String REMINDMOUNTCHROOT = "material.hunter.REMINDMOUNTCHROOT";
  public static final String USENETHUNTER = "material.hunter.USENETHUNTER";
  public static final String DOWNLOADING = "material.hunter.DOWNLOADING";
  public static final String INSTALLING = "material.hunter.INSTALLING";
  public static final String BACKINGUP = "material.hunter.BACKINGUP";
  public static final String CUSTOMCOMMAND_START = "material.hunter.CUSTOMCOMMAND_START";
  public static final String CUSTOMCOMMAND_FINISH = "material.hunter.CUSTOMCOMMAND_FINISH";
  public Intent resultIntent = null;
  public PendingIntent resultPendingIntent = null;
  public TaskStackBuilder stackBuilder = null;

  public NotificationChannelService() {
    super("NotificationChannelService");
  }

  @Override
  public void onCreate() {
    super.onCreate();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel serviceChannel =
          new NotificationChannel(
              CHANNEL_ID, "MaterialHunterChannelService", NotificationManager.IMPORTANCE_DEFAULT);
      NotificationManager manager = getSystemService(NotificationManager.class);
      manager.createNotificationChannel(serviceChannel);
    }
  }

  @Override
  protected void onHandleIntent(@Nullable Intent intent) {
    if (intent != null) {
      if (intent.getAction() != null) {
        NotificationCompat.Builder builder;
        NotificationManagerCompat notificationManagerCompat =
            NotificationManagerCompat.from(AppNavHomeActivity.context);
        notificationManagerCompat.cancelAll();
        resultIntent = new Intent();
        stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        switch (intent.getAction()) {
          case REMINDMOUNTCHROOT:
            builder =
                new NotificationCompat.Builder(AppNavHomeActivity.context, CHANNEL_ID)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
                    .setStyle(
                        new NotificationCompat.BigTextStyle()
                            .bigText(
                                "Please open MaterialHunter and navigate to Chroot Manager to setup"
                                    + " your chroot."))
                    .setContentTitle("Chroot isn't up or isn't installed.")
                    .setContentText("Please navigate to Chroot Manager to setup your Chroot.")
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentIntent(resultPendingIntent);
            notificationManagerCompat.notify(NOTIFY_ID, builder.build());
            break;
          case USENETHUNTER:
            builder =
                new NotificationCompat.Builder(AppNavHomeActivity.context, CHANNEL_ID)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
                    .setTimeoutAfter(10000)
                    .setContentTitle("Chroot botted successfully.")
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentIntent(resultPendingIntent);
            notificationManagerCompat.notify(NOTIFY_ID, builder.build());
            break;
          case DOWNLOADING:
            builder =
                new NotificationCompat.Builder(AppNavHomeActivity.context, CHANNEL_ID)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
                    .setTimeoutAfter(15000)
                    .setStyle(
                        new NotificationCompat.BigTextStyle()
                            .bigText("Please don't kill the app! The download will be cancelled."))
                    .setContentTitle("Downloading chroot")
                    .setContentText("Please don't kill the app! The download will be cancelled.")
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentIntent(resultPendingIntent);
            notificationManagerCompat.notify(NOTIFY_ID, builder.build());
            break;
          case INSTALLING:
            builder =
                new NotificationCompat.Builder(AppNavHomeActivity.context, CHANNEL_ID)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
                    .setTimeoutAfter(15000)
                    .setStyle(
                        new NotificationCompat.BigTextStyle()
                            .bigText(
                                "Please don't kill the app as it will still keep running on the"
                                    + " background! Otherwise you'll need to kill the tar process"
                                    + " by yourself."))
                    .setContentTitle("Installing chroot")
                    .setContentText(
                        "Please don't kill the app as it will still keep running on the background!"
                            + " Otherwise you'll need to kill the tar process by yourself.")
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentIntent(resultPendingIntent);
            notificationManagerCompat.notify(NOTIFY_ID, builder.build());
            break;
          case BACKINGUP:
            builder =
                new NotificationCompat.Builder(AppNavHomeActivity.context, CHANNEL_ID)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
                    .setTimeoutAfter(15000)
                    .setStyle(
                        new NotificationCompat.BigTextStyle()
                            .bigText(
                                "Please don't kill the app as it will still keep running on the"
                                    + " background! Otherwise you'll need to kill the tar process"
                                    + " by yourself."))
                    .setContentTitle("Creating chroot backup")
                    .setContentText(
                        "Please don't kill the app as it will still keep running on the background!"
                            + " Otherwise you'll need to kill the tar process by yourself.")
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentIntent(resultPendingIntent);
            notificationManagerCompat.notify(NOTIFY_ID, builder.build());
            break;
          case CUSTOMCOMMAND_START:
            builder =
                new NotificationCompat.Builder(AppNavHomeActivity.context, CHANNEL_ID)
                    .setAutoCancel(false)
                    .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
                    .setStyle(
                        new NotificationCompat.BigTextStyle()
                            .bigText(
                                "Command: \""
                                    + intent.getStringExtra("CMD")
                                    + "\" is being run in background and in "
                                    + intent.getStringExtra("ENV")
                                    + " environment."))
                    .setContentTitle("Custom Commands")
                    .setContentText(
                        "Command: \""
                            + intent.getStringExtra("CMD")
                            + "\" is being run in background and in "
                            + intent.getStringExtra("ENV")
                            + " environment.")
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentIntent(resultPendingIntent);
            notificationManagerCompat.notify(NOTIFY_ID, builder.build());
            break;
          case CUSTOMCOMMAND_FINISH:
            final int returnCode = intent.getIntExtra("RETURNCODE", 0);
            final String CMD = intent.getStringExtra("CMD");
            String resultString = "";
            if (returnCode == CustomCommandsAsyncTask.ANDROID_CMD_SUCCESS) {
              resultString =
                  "Return success.\nCommand: \""
                      + CMD
                      + "\" has been executed in android environment.";
            } else if (returnCode == CustomCommandsAsyncTask.ANDROID_CMD_FAIL) {
              resultString =
                  "Return error.\nCommand: \""
                      + CMD
                      + "\" has been executed in android environment.";
            } else if (returnCode == CustomCommandsAsyncTask.CHROOT_CMD_SUCCESS) {
              resultString =
                  "Return success.\nCommand: \""
                      + CMD
                      + "\" has been executed in chroot environment.";
            } else if (returnCode == CustomCommandsAsyncTask.CHROOT_CMD_FAIL) {
              resultString =
                  "Return error.\nCommand: \""
                      + CMD
                      + "\" has been executed in chroot environment.";
            }
            builder =
                new NotificationCompat.Builder(AppNavHomeActivity.context, CHANNEL_ID)
                    .setAutoCancel(false)
                    .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(resultString))
                    .setContentTitle("Custom Commands")
                    .setContentText(resultString)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentIntent(resultPendingIntent);
            notificationManagerCompat.notify(NOTIFY_ID, builder.build());
            break;
        }
      }
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return super.onBind(intent);
  }

  @Override
  public void onTaskRemoved(Intent rootIntent) {
    super.onTaskRemoved(rootIntent);
  }
}
