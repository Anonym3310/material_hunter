package material.hunter.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import androidx.annotation.Nullable;

import material.hunter.MainActivity;
import material.hunter.R;
import material.hunter.utils.PathsUtil;
import material.hunter.utils.ShellExecuter;

public class CompatCheckService extends IntentService {

    private String message = "";
    private int RESULTCODE = -1;
    private SharedPreferences prefs;

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
        prefs = MainActivity.context.getSharedPreferences("material.hunter", MODE_PRIVATE);
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
        /*
            Uses for checking chroot status and
            reporting results to app
        */

        final int status = new ShellExecuter().RunAsRootReturnValue(PathsUtil.APP_SCRIPTS_PATH + "/chrootmgr -c \"status\" -p " + PathsUtil.CHROOT_PATH());

        if (RESULTCODE == -1) {
            if (status != 0) {
                if (status == 3) {
                    // Chroot corrupted
                } else {
                    // Remind mount
                }
                MainActivity.setChrootInstalled(false);
            } else {
                MainActivity.setChrootInstalled(true);
            }
        } else if (RESULTCODE == 0) {
            MainActivity.setChrootInstalled(true);
        } else {
            // Remind mount
            MainActivity.setChrootInstalled(false);
        }
    }
}