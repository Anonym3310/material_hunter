package material.hunter.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

public class PermissionsUtil {
	
	public static final int REQUEST_CODE = 1;
    public static final String[] PERMISSIONS = {
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        "com.termux.permission.RUN_COMMAND",
        "com.offsec.nhterm.permission.RUN_SCRIPT_SU"
    };
    private final Activity activity;
    private final Context context;

    public PermissionsUtil(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
    }

    public void requestPermissions(String[] PERMISSIONS, int REQUEST_CODE) {
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
        for (String permission : PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}