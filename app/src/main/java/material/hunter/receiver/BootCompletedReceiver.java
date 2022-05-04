package material.hunter.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import material.hunter.service.RunAtBootService;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                ComponentName component =
                        new ComponentName(
                                context.getPackageName(), RunAtBootService.class.getName());
                RunAtBootService.enqueueWork(context, (intent.setComponent(component)));
            }
        }
    }
}