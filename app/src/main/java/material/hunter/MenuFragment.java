package material.hunter;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.lang.SecurityException;
import java.util.ArrayList;
import java.util.List;

import material.hunter.MainActivity;
import material.hunter.TerminalRunActivity;
import material.hunter.utils.PathsUtil;
import material.hunter.utils.TerminalUtil;

public class MenuFragment extends Fragment {

    private Activity activity;
    private Context context;
    private TerminalUtil terminalUtil;
    private static MaterialCardView help;
    private static MaterialCardView usbarmory;
    private static MaterialCardView terminal;
    private static MaterialCardView custom_commands;
    private static MaterialCardView services;
    private static final String SHORTCUT_ID = "material.hunter.shortcut";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        context = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.menu_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        help = view.findViewById(R.id.help);
        usbarmory = view.findViewById(R.id.usbarmory);
        terminal = view.findViewById(R.id.terminal);
        custom_commands = view.findViewById(R.id.custom_commands);
        services = view.findViewById(R.id.services);
        terminalUtil = new TerminalUtil(activity, context);

        help.setOnClickListener(v -> {
            MaterialAlertDialogBuilder adb = new MaterialAlertDialogBuilder(context);
            adb.setTitle("Menu");
            adb.setMessage(
                "When the chroot isn't running, some elements that interact with it are hidden.");
            adb.setPositiveButton("Ok", (di, i) -> {});
            adb.show();
		});

        usbarmory.setOnClickListener(v -> {
            MaterialAlertDialogBuilder adb = new MaterialAlertDialogBuilder(context);
            adb.setTitle("USB Armory");
            if (MainActivity.getKernelBase() < 3.11f) {
                adb.setMessage(
                    "Your kernel version less than 3.11, ConfigFS isn't supported.");
            } else if (!new File("/config/usb_gadget").exists()) {
                adb.setMessage(
                    "Not supported by the kernel. The error can be caused by selinux.");
            } else {
                Intent intent = new Intent(context, USBArmory.class);
                startActivity(intent);
                return;
            }
            adb.setPositiveButton("Ok", (di, i) -> {});
            adb.show();
        });

        terminal.setOnClickListener(v -> {
            if (!isShortcutPinned()) {
                MaterialAlertDialogBuilder adb = new MaterialAlertDialogBuilder(context);
                adb.setTitle("MaterialHunter");
                adb.setMessage("We recommend creating a shortcut on your desktop to quickly launch the Terminal");
                adb.setPositiveButton("Create", (di, i) -> {
                    createShortcut();
                });
                adb.setNeutralButton("Open in app", (di, i) -> {
                    runTerminalInChroot();
                });
                adb.setNegativeButton("Cancel", (di, i) -> {});
                adb.show();
            } else {
                runTerminalInChroot();
            }
        });

        custom_commands.setOnClickListener(v -> {
            Intent intent = new Intent(context, CustomCommands.class);
            startActivity(intent);
        });

        services.setOnClickListener(v -> {
            Intent intent = new Intent(context, Services.class);
            startActivity(intent);
        });
    }

    public static void compatVerified(boolean is) {
        ArrayList<View> views = new ArrayList<View>();
        // views.add(view);
        views.add(terminal);
        views.add(custom_commands);
        views.add(services);
        for (int i = 0; i < views.size(); i++) {
            View view = views.get(i);
            view.setEnabled(is);
            view.setVisibility(is ? View.VISIBLE : View.GONE);
        }
        help.setVisibility(is ? View.GONE : View.VISIBLE);
    }

    private void runTerminalInChroot() {
        try {
            terminalUtil.runCommand(PathsUtil.APP_SCRIPTS_PATH + "/bootroot_login", false);
        } catch (ActivityNotFoundException | PackageManager.NameNotFoundException e) {
            terminalUtil.showTerminalNotInstalledDialog();
        } catch (SecurityException e) {
            terminalUtil.showPermissionDeniedDialog();
        }
    }

    private boolean isShortcutPinned() {
        ShortcutManager shortcutManager =
                context.getSystemService(ShortcutManager.class);
        for (ShortcutInfo shortcutInfo : shortcutManager.getPinnedShortcuts()) {
            if (SHORTCUT_ID.equals(shortcutInfo.getId()) && shortcutInfo.isPinned()) {
                return true;
            }
        }
        return false;
    }

    private void createShortcut() {
        ShortcutManager shortcutManager =
                context.getSystemService(ShortcutManager.class);

        if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            Intent intent = new Intent(context, TerminalRunActivity.class);
            intent.setAction(Intent.ACTION_VIEW);

            Icon icon = Icon.createWithResource(context, R.mipmap.ic_mh_termux);

            ShortcutInfo shortcut =
                    new ShortcutInfo.Builder(context, SHORTCUT_ID)
                            .setShortLabel("Chroot Terminal")
                            .setIntent(intent)
                            .setIcon(icon)
                            .build();

            shortcutManager.requestPinShortcut(shortcut, null);
        }
    }
}