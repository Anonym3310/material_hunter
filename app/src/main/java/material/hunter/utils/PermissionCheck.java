package material.hunter.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

public class PermissionCheck {
    public static final int DEFAULT_RCODE = 1;
    public static final int STAFF_RCODE = 2;
    public static final String[] DEFAULT = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static final String[] STAFF = {"com.termux.permission.RUN_COMMAND"};
    private static final String TAG = "PermissionCheck";
    private final Activity activity;
    private final Context context;

    public PermissionCheck(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
    }

    public void checkPermissions(String[] PERMISSIONS, int REQUEST_CODE) {
        if (!hasPermissions(context, PERMISSIONS)) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS, REQUEST_CODE);
        }
    }

    private boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isAllPermitted(String[] PERMISSIONS) {
        for (String permissions : PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(context, permissions)
                    != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }
}
