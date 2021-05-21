package material.hunter.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import material.hunter.service.RunAtBootService;

public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String TAG = "BootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            //Log.d(TAG, "Actions: " + intent.getAction());
            if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                ComponentName comp = new ComponentName(context.getPackageName(),
                        RunAtBootService.class.getName());
                RunAtBootService.enqueueWork(context, (intent.setComponent(comp)));
                //Log.d(TAG, "MaterialHunter receive boot completed intent!");
            }
        }
    }
}
