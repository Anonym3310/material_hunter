package material.hunter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import material.hunter.Extensions.InstallerInterface;
import material.hunter.utils.Checkers;
import material.hunter.utils.NotificationsUtil;
import material.hunter.utils.PathsUtil;
import material.hunter.utils.PermissionsUtil;
import material.hunter.utils.TerminalUtil;

import mirivan.TransparentQ;

public class MainActivity extends ThemedActivity {

    public static Activity activity;
    public static Context context;
    private static ActionBar actionBar;
    private static MenuItem lastSelectedMenuItem;
    private BottomNavigationView bn;
    private PermissionsUtil permissions;
    private SharedPreferences prefs;
    private static boolean chroot_installed = false;
    private static float kernel_base = 1.0f;

    MaterialToolbar toolbar;
    HomeFragment _home;
    ManagerFragment _manager;
    MenuFragment _menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("material.hunter", Context.MODE_PRIVATE);
        final int theme = prefs.getInt("theme", 0);
        if (theme == 0) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (theme == 1) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
        if (prefs.getBoolean("show_wallpaper", false)) {
            getWindow()
                    .setFlags(
                            WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER,
                            WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
            int alpha_level = prefs.getInt("background_alpha_level", 10);
            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(R.attr.colorSurface, typedValue, true);
            String color =
                    Integer.toHexString(ContextCompat.getColor(this, typedValue.resourceId))
                            .substring(2);
            getWindow()
                    .getDecorView()
                    .setBackground(
                            new ColorDrawable(
                                    Color.parseColor(TransparentQ.p2c(color, alpha_level))));
        }

        context = this;
        activity = this;

        // Setup utils
        PathsUtil.getInstance(context);
        NotificationsUtil.getInstance(context);

        // Setup rootview content
        setRootView();

        // Check root and etc
        if (Checkers.isRoot()) {
            // Nothing to do
        } else {
            showDialog(
                    getResources().getString(R.string.app_name),
                    "Root required. The app cannot work without root privileges.",
                    true);
        }

        /* Check folders (in sdcard, data, etc..),
            copy scripts if they doesn't exciting;
            Setup default preferences
        */
        new InstallerInterface(activity, context);

        // Check is termux api external apps enabled
        TerminalUtil terminal = new TerminalUtil(activity, context);
        if (prefs.getString("terminal_type", TerminalUtil.TERMINAL_TYPE_TERMUX).equals(TerminalUtil.TERMINAL_TYPE_TERMUX)
                && terminal.isTermuxInstalled()
                && terminal.isTermuxApiSupported()) {
            terminal.termuxApiExternalAppsRequired();
        }
    }

    private void setRootView() {
        setContentView(R.layout.main_activity);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        bn = findViewById(R.id.bottomnavigation);

        bn.setOnItemSelectedListener(item -> {
            return changeFragment(item.getItemId(), item);
        });

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fmt = fm.beginTransaction();

        if (lastSelectedMenuItem == null) lastSelectedMenuItem = bn.getMenu().getItem(0);

        prepareBottomNav();
        getSupportActionBar().setTitle("Home");

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        if (lastSelectedMenuItem == null) {
            return;
        }
        String data = intent.getDataString();
        if (data != null && !data.equals("")) {
            switch (data) {
                case "home":
                    bn.setSelectedItemId(R.id.home);
                    break;
                case "manager":
                    bn.setSelectedItemId(R.id.manager);
                    break;
                case "menu":
                    bn.setSelectedItemId(R.id.menu);
                    break;
            }
        }
    }

    protected void prepareBottomNav() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fmt = fm.beginTransaction();

        if ((_home = (HomeFragment) fm.findFragmentByTag("home")) == null)
            fmt.add(R.id.frame, (_home = new HomeFragment()), "home");

        if ((_manager = (ManagerFragment) fm.findFragmentByTag("manager")) == null)
            fmt.add(R.id.frame, (_manager = new ManagerFragment()), "manager").hide(_manager);

        if ((_menu = (MenuFragment) fm.findFragmentByTag("menu")) == null)
            fmt.add(R.id.frame, (_menu = new MenuFragment()), "menu").hide(_menu);

        fmt.commit();
    }

    private boolean changeFragment(int id, MenuItem item) {

        FragmentTransaction fmt = getSupportFragmentManager().beginTransaction();

        if (lastSelectedMenuItem != item) {
            toolbar.setAlpha(0.5f);
            toolbar.animate().alpha(1).setDuration(384);
            lastSelectedMenuItem = item;
        }

        switch (id) {
            case R.id.home:
                fmt.show(_home).hide(_manager).hide(_menu).commit();
                getSupportActionBar().setTitle("Home");
                break;
            case R.id.manager:
                fmt.show(_manager).hide(_menu).hide(_home).commit();
                getSupportActionBar().setTitle("Manager");
                break;
            case R.id.menu:
                fmt.show(_menu).hide(_home).hide(_manager).commit();
                getSupportActionBar().setTitle("Menu");
                break;
        }
        return true;
    }

    public void showDialog(String title, String message, boolean NeedToExit) {
        MaterialAlertDialogBuilder adb = new MaterialAlertDialogBuilder(this);
        adb.setCancelable(false);
        adb.setTitle(title);
        adb.setMessage(message);
        if (NeedToExit)
            adb.setPositiveButton(
                    "Exit",
                    (dialog, which) -> {
                        finishAffinity();
                    });
        else adb.setPositiveButton("Cancel", (dialog, which) -> {});
        adb.show();
    }

    public static ActionBar getActionBarView() {
        return actionBar;
    }

    public static MenuItem getLastSelectedMenuItem() {
        return lastSelectedMenuItem;
    }

    public static boolean isChrootInstalled() {
        return chroot_installed;
    }

    public static void setChrootInstalled(boolean _chroot_installed) {
        chroot_installed = _chroot_installed;
        new Handler(Looper.getMainLooper()).post(() -> {
            MenuFragment.compatVerified(_chroot_installed);
        });
    }

    public static float getKernelBase() {
        return kernel_base;
    }

    public static void setKernelBase(float _kernel_base) {
        kernel_base = _kernel_base;
    }
}