package material.hunter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import java.io.File;
import java.util.Objects;
import material.hunter.Extensions.InstallerInterface;
import material.hunter.SQL.MaterialHunterSQL;
import material.hunter.SQL.ServicesSQL;
import material.hunter.SQL.USBArmorySQL;
import material.hunter.service.CompatCheckService;
import material.hunter.utils.CheckForRoot;
import material.hunter.utils.NhPaths;
import material.hunter.utils.PermissionCheck;
import material.hunter.utils.SharePrefTag;
import material.hunter.utils.ShellExecuter;
import mirivan.TransparentQ;

public class AppNavHomeActivity extends AppCompatActivity {
  public static final String TAG = "AppNavHomeActivity";
  public static final String CHROOT_INSTALLED_TAG = "CHROOT_INSTALLED_TAG";
  public static final String BOOT_CHANNEL_ID = "BOOT_CHANNEL";
  public static MenuItem lastSelectedMenuItem;
  public static boolean isBackPressDisabled = false;
  public static Context context;

  private DrawerLayout mDrawerLayout;
  private ActionBarDrawerToggle mDrawerToggle;
  private NavigationView navigationView;
  private CharSequence mTitle = "MaterialHunter";
  private SharedPreferences prefs;
  private BroadcastReceiver materialhunterReceiver;
  private NhPaths nhPaths;
  private PermissionCheck permissionCheck;
  private boolean updateServiceBound = false;
  private boolean keyword = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    DynamicColors.applyIfAvailable(this);
    prefs = getSharedPreferences("material.hunter", Context.MODE_PRIVATE);
    if (prefs.getBoolean("show_wallpaper", false)) {
      getWindow()
          .setFlags(
              WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER,
              WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
      int alpha_level = prefs.getInt("background_alpha_level", 100);
      TypedValue typedValue = new TypedValue();
      getTheme().resolveAttribute(R.attr.colorSurface, typedValue, true);
      String color =
          Integer.toHexString(ContextCompat.getColor(this, typedValue.resourceId)).substring(2);
      getWindow()
          .getDecorView()
          .setBackground(new ColorDrawable(Color.parseColor(TransparentQ.p2c(color, alpha_level))));
    }
    context = getApplicationContext();
    nhPaths = NhPaths.getInstance(context);
    permissionCheck = new PermissionCheck(this, context);
    materialhunterReceiver = new MaterialHunterReceiver();
    IntentFilter AppNavHomeIntentFilter = new IntentFilter();
    AppNavHomeIntentFilter.addAction(MaterialHunterReceiver.CHECKCOMPAT);
    AppNavHomeIntentFilter.addAction(MaterialHunterReceiver.CHECKCHROOT);
    AppNavHomeIntentFilter.addAction(MaterialHunterReceiver.WORKING);
    registerReceiver(materialhunterReceiver, new IntentFilter(AppNavHomeIntentFilter));

    // Initialize installer
    InstallerInterface iface = new InstallerInterface(context, this);
    // We must not attempt to copy files unless we have storage permissions
    if (permissionCheck.isAllPermitted(PermissionCheck.PERMISSIONS)) {
      iface.Install();
      setRootView();
    } else {
      // Crude way of waiting for the permissions to be granted before we continue
      permissionCheck.requestPermissions(PermissionCheck.PERMISSIONS, PermissionCheck.REQUEST_CODE);
      int t=0;
      while (!permissionCheck.isAllPermitted(PermissionCheck.PERMISSIONS)) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) { }
        t++;
        if (t>=10) {
          break;
        }
      }
      if (permissionCheck.isAllPermitted(PermissionCheck.PERMISSIONS)) {
        iface.Install();
        setRootView();
        showWarningDialog("Everything fine", "Please restart application to finalize setup.", true);
      } else {
        showWarningDialog("Bad request", "Please restart application and grant all required permissions.", true);
	  }
    }
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (mDrawerToggle.onOptionsItemSelected(item)) {
      return true;
    }
    if (item.getItemId() == android.R.id.home) {
      if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
        mDrawerLayout.closeDrawers();
      } else {
        mDrawerLayout.openDrawer(GravityCompat.START);
      }
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == PermissionCheck.REQUEST_CODE) {
      for (int grantResult : grantResults) {
        if (grantResult != 0) {
          if (context.getPackageManager().getLaunchIntentForPackage("com.termux") == null) {
            showWarningDialog(
                "MaterialHunter app can't be run properly",
                "Termux isn`t installed yet, please install it from F-Droid!",
                false);
            return;
          }
          showWarningDialog(
              "MaterialHunter app can't be run properly",
              "Please grant all the permission requests from outside the app or restart the app to"
                  + " grant the rest of permissions again.",
              true);
          return;
        }
      }
    }
  }

  @Override
  public void onBackPressed() {
    if (!isBackPressDisabled) {
      if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
        mDrawerLayout.closeDrawers();
      } else {
        mDrawerLayout.openDrawer(GravityCompat.START);
      }
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    startService(new Intent(context, CompatCheckService.class));
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (materialhunterReceiver != null) {
      unregisterReceiver(materialhunterReceiver);
    }
    if (nhPaths != null) {
      nhPaths.onDestroy();
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  private void setRootView() {
    setContentView(R.layout.base_layout);
    MaterialToolbar tb = findViewById(R.id.appbar);
    setSupportActionBar(tb);
    ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setHomeButtonEnabled(true);
      ab.setDisplayHomeAsUpEnabled(true);
    }

    mDrawerLayout = findViewById(R.id.drawer_layout);

    navigationView = findViewById(R.id.navigation_view);
    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    @SuppressLint("InflateParams")
    LinearLayout navigationHeadView =
        (LinearLayout) inflater.inflate(R.layout.sidenav_header, null);
    navigationView.addHeaderView(navigationHeadView);

    TextView easter_egg = navigationHeadView.findViewById(R.id.easter_egg);
    easter_egg.setOnLongClickListener(
        view -> {
          if (keyword) {
            showLicense();
          } else {
            NhPaths.showMessage(context, "¯\\_(ツ)_/¯", false);
            keyword = true;
          }
          return false;
        });

    if (navigationView != null) {
      setupDrawerContent(navigationView);
    }

    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.container, MaterialHunterFragment.newInstance(R.id.materialhunter_item))
        .commit();

    // disable all fragment first until it passes the compat check.
    navigationView.getMenu().setGroupEnabled(R.id.chrootDependentGroup, false);

    if (lastSelectedMenuItem == null) { // only in the 1st create
      lastSelectedMenuItem = navigationView.getMenu().getItem(0);
      lastSelectedMenuItem.setChecked(true);
    }
    mDrawerToggle =
        new ActionBarDrawerToggle(
            this, mDrawerLayout, R.string.drawer_opened, R.string.drawer_closed);
    mDrawerLayout.setDrawerListener(mDrawerToggle);
    mDrawerToggle.syncState();
    startService(new Intent(context, CompatCheckService.class));
  }

  private void showLicense() {
    final View rootView = getLayoutInflater().inflate(R.layout.license_layout, null);
    MaterialAlertDialogBuilder adb = new MaterialAlertDialogBuilder(this);
    adb.setView(rootView).setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
    adb.setCancelable(true);
    AlertDialog ad = adb.create();
    ad.show();
    ((TextView) ad.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
  }

  @SuppressLint("NonConstantResourceId")
  private void setupDrawerContent(NavigationView navigationView) {
    navigationView.setNavigationItemSelectedListener(
        menuItem -> {
          int itemId = menuItem.getItemId();
          if (itemId != R.id.terminal_item) {
            if (lastSelectedMenuItem != menuItem) {
              if (lastSelectedMenuItem != null) lastSelectedMenuItem.setChecked(false);
              lastSelectedMenuItem = menuItem;
            }
            menuItem.setChecked(true);
            mTitle = menuItem.getTitle();
          }
          if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) mDrawerLayout.closeDrawers();

          changeDrawer(itemId);
          restoreActionBar();
          return true;
        });
  }

  private void changeDrawer(int itemId) {
    FragmentManager fragmentManager = getSupportFragmentManager();
    switch (itemId) {
      case R.id.materialhunter_item:
        changeFragment(fragmentManager, MaterialHunterFragment.newInstance(itemId));
        break;
      case R.id.createchroot_item:
        changeFragment(fragmentManager, ChrootManagerFragment.newInstance(itemId));
        break;
      case R.id.usbarmory_item:
        if (new File("/config/usb_gadget").exists()) {
          changeFragment(fragmentManager, USBArmoryFragment.newInstance(itemId));
        } else {
          showWarningDialog("", "Your kernel doesn't support this function.", false);
        }
        break;
      case R.id.mhsettings_item:
        changeFragment(fragmentManager, MHSettingsFragment.newInstance(itemId));
        break;
      case R.id.terminal_item:
        try {
          Intent intent = new Intent();
          intent.setClassName("com.termux", "com.termux.app.RunCommandService");
          intent.setAction("com.termux.RUN_COMMAND");
          intent.putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/su");
          intent.putExtra(
              "com.termux.RUN_COMMAND_ARGUMENTS",
              new String[] {"-c", NhPaths.APP_SCRIPTS_PATH + "/bootroot_login"});
          // To be honest, I didn't like this method, but it still works great, so I will continue
          // to use it..and hardly refuse.
          // intent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", new String[]{"-c", "echo 'Hello
          // world!'"});
          intent.putExtra("com.termux.RUN_COMMAND_WORKDIR", "/data/data/com.termux/files/home");
          intent.putExtra("com.termux.RUN_COMMAND_BACKGROUND", false);
          intent.putExtra("com.termux.RUN_COMMAND_SESSION_ACTION", "0");
          startService(intent);
        } catch (RuntimeException e) {
          NhPaths.showMessage(this, "Termux uid isn't exists.", true);
        }
        break;
      case R.id.custom_commands_item:
        changeFragment(fragmentManager, CustomCommandsFragment.newInstance(itemId));
        break;
      case R.id.services_item:
        changeFragment(fragmentManager, ServicesFragment.newInstance(itemId));
        break;
    }
  }

  public void restoreActionBar() {
    ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setHomeButtonEnabled(true);
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setDisplayShowTitleEnabled(true);
      mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
      ab.setTitle(mTitle);
    }
  }

  public void blockActionBar() {
    ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setHomeButtonEnabled(false);
      ab.setDisplayHomeAsUpEnabled(false);
      mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }
  }

  private void changeFragment(FragmentManager fragmentManager, Fragment fragment) {
    fragmentManager
        .beginTransaction()
        .replace(R.id.container, fragment)
        .addToBackStack(null)
        .commit();
  }

  public void showWarningDialog(String title, String message, boolean NeedToExit) {
    MaterialAlertDialogBuilder warningAD = new MaterialAlertDialogBuilder(this);
    warningAD.setCancelable(false);
    warningAD.setTitle(title);
    warningAD.setMessage(message);
    warningAD.setPositiveButton(
        "CLOSE",
        (dialog, which) -> {
          dialog.dismiss();
          if (NeedToExit) System.exit(1);
        });
    warningAD.create().show();
  }

  public class MaterialHunterReceiver extends BroadcastReceiver {
    public static final String CHECKCOMPAT = "material.hunter.CHECKCOMPAT";
    public static final String CHECKCHROOT = "material.hunter.CHECKCHROOT";
    public static final String CHROOT_CORRUPTED = "material.hunter.CHROOT_CORRUPTED";
    public static final String WORKING = "material.hunter.WORKING";

    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getAction() != null) {
        switch (intent.getAction()) {
          case CHECKCOMPAT:
            showWarningDialog(
                "MaterialHunter app can't be run properly",
                intent.getStringExtra("message"),
                true);
            break;
          case CHROOT_CORRUPTED:
            navigationView.getMenu().setGroupEnabled(R.id.chrootDependentGroup, false);
          case CHECKCHROOT:
            try {
              if (intent.getBooleanExtra("ENABLEFRAGMENT", false)) {
                navigationView.getMenu().setGroupEnabled(R.id.chrootDependentGroup, true);
              } else {
                navigationView.getMenu().setGroupEnabled(R.id.chrootDependentGroup, false);
              }
            } catch (Exception e) {
            }
            break;
          case WORKING:
            isBackPressDisabled = intent.getBooleanExtra("working", true);
            if (isBackPressDisabled) {
              blockActionBar();
            } else {
              restoreActionBar();
            }
            break;
        }
      }
    }
  }
}