package material.hunter;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import material.hunter.utils.PathsUtil;
import material.hunter.utils.TerminalUtil;

public class TerminalRunActivity extends Activity {

    private Activity activity;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        context = this;

        PathsUtil.getInstance(context);

        try {
            TerminalUtil terminal = new TerminalUtil(activity, context);
            terminal.runCommand(PathsUtil.APP_SCRIPTS_PATH + "/bootroot_login", false);
        } catch (ActivityNotFoundException
                | PackageManager.NameNotFoundException
                | SecurityException e) {
            PathsUtil.showMessage(
                    context, "Something worng, try to open terminal in MaterialHunter.", true);
        }
        finish();
    }
}