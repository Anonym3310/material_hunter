package material.hunter.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import material.hunter.MainActivity;
import material.hunter.R;

public class NotificationsUtil {

    private static Context context;
    private static NotificationsUtil instance;
    private static String CHANNEL_ID = "MaterialHunterNotifyChannel";

    private NotificationsUtil(Context mContext) {
        context = mContext;
    }

    public static synchronized NotificationsUtil getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationsUtil(context);
        }
        return instance;
    }

    public static void showNotification(String title, int id) {
        showNotification(title, null, null, false, id);
    }

    public static void showNotification(String title, String subtitle, int id) {
        showNotification(title, subtitle, null, false, id);
    }

    public static void showNotification(
            String title, String subtitle, String openedSubtitle, int id) {
        showNotification(title, subtitle, openedSubtitle, false, id);
    }

    public static void showNotification(
            String title, String subtitle, String openedSubtitle, boolean alertOnce, int id) {
        registerChannel();

        NotificationManagerCompat notificationManagerCompat =
                NotificationManagerCompat.from(context);
        Intent appIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setAutoCancel(false)
                        .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
                        .setOnlyAlertOnce(alertOnce)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(openedSubtitle))
                        .setContentTitle(title)
                        .setContentText(subtitle)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setContentIntent(pendingIntent)
                        .build();
        notificationManagerCompat.notify(id, notification);
    }

    private static void registerChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "MaterialHunter: Notification",
                            NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}