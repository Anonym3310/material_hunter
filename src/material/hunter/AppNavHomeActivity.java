package material.hunter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.util.Objects;
import java.util.Stack;

import material.hunter.AsyncTask.CopyBootFilesAsyncTask;
import material.hunter.GPS.KaliGPSUpdates;
import material.hunter.GPS.LocationUpdateService;
import material.hunter.SQL.CustomCommandsSQL;
import material.hunter.SQL.MaterialHunterSQL;
import material.hunter.SQL.ServicesSQL;
import material.hunter.SQL.USBArmorySQL;
import material.hunter.service.CompatCheckService;
import material.hunter.utils.CheckForRoot;
import material.hunter.utils.NhPaths;
import material.hunter.utils.PermissionCheck;
import material.hunter.utils.SharePrefTag;

public class AppNavHomeActivity extends AppCompatActivity implements KaliGPSUpdates.Provider {
    public final static String TAG = "AppNavHomeActivity";
    public static final String CHROOT_INSTALLED_TAG = "CHROOT_INSTALLED_TAG";
    public static final String GPS_BACKGROUND_FRAGMENT_TAG = "BG_FRAGMENT_TAG";
    public static final String BOOT_CHANNEL_ID = "BOOT_CHANNEL";
    public static MenuItem lastSelectedMenuItem;
    public static Boolean isBackPressEnabled = true;
    private final Stack<String> titles = new Stack<>();
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView navigationView;
    private CharSequence mTitle = "MaterialHunter";
    private SharedPreferences prefs;
    private boolean locationUpdatesRequested = false;
    private KaliGPSUpdates.Receiver locationUpdateReceiver;
    private NhPaths nhPaths;
    private PermissionCheck permissionCheck;
    private BroadcastReceiver materialhunterReceiver;
    private int desiredFragment = -1;
    private LocationUpdateService locationService;
    private boolean updateServiceBound = false;
    private final ServiceConnection locationServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            LocationUpdateService.ServiceBinder binder = (LocationUpdateService.ServiceBinder) service;
            locationService = binder.getService();
            updateServiceBound = true;
            if (locationUpdatesRequested) {
                locationService.requestUpdates(locationUpdateReceiver);
            }
        }

        public void onServiceDisconnected(ComponentName arg0) {
            updateServiceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nhPaths = NhPaths.getInstance(getApplicationContext());
        permissionCheck = new PermissionCheck(this, getApplicationContext());
        materialhunterReceiver = new MaterialHunterReceiver();
        IntentFilter AppNavHomeIntentFilter = new IntentFilter();
        AppNavHomeIntentFilter.addAction(MaterialHunterReceiver.CHECKCOMPAT);
        AppNavHomeIntentFilter.addAction(MaterialHunterReceiver.BACKPRESSED);
        AppNavHomeIntentFilter.addAction(MaterialHunterReceiver.CHECKCHROOT);
        AppNavHomeIntentFilter.addAction("ChrootManager");
        this.registerReceiver(materialhunterReceiver, new IntentFilter(AppNavHomeIntentFilter));
        prefs = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

        // Start copying the app files to the corresponding path.
        ProgressDialog progressDialog = new ProgressDialog(this);
        CopyBootFilesAsyncTask copyBootFilesAsyncTask = new CopyBootFilesAsyncTask(getApplicationContext(), this, progressDialog);
        copyBootFilesAsyncTask.setListener(new CopyBootFilesAsyncTask.CopyBootFilesAsyncTaskListener() {
            @Override
            public void onAsyncTaskPrepare() {
            }

            @Override
            public void onAsyncTaskFinished(Object result) {

                // Fetch the busybox path again after the busybox_nh is copied.
                NhPaths.BUSYBOX = NhPaths.getBusyboxPath();

                // Now Initiate all SQL singleton in MainActivity so that it can be less lagged when switching fragments,
                // because it takes time to retrieve data from database.
                MaterialHunterSQL.getInstance(getApplicationContext());
                ServicesSQL.getInstance(getApplicationContext());
                CustomCommandsSQL.getInstance(getApplicationContext());
                USBArmorySQL.getInstance(getApplicationContext());

                // Setup the default SharePreference value.
                setDefaultSharePreference();

                // After finishing copying app files, we do a compatibility check before allowing user to use it.
                // First, check if the app has gained the root already.
                if (!CheckForRoot.isRoot()) {
                    showWarningDialog("MaterialHunter app cannot be run properly", "Root permission is required!", true);
                }

                // Secondly, check if busybox is present.
                // if (!CheckForRoot.isBusyboxInstalled()) {
                // showWarningDialog("MaterialHunter app cannot be run properly", "No busybox is detected, please make sure you have busybox installed!", true);
                // }

                // Thirdly, check if NetHunter terminal app has been installed.
                if (getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.offsec.nhterm") == null) {
                    showWarningDialog("MaterialHunter app cannot be run properly", "NetHunter terminal is not installed yet.", true);
                }

                // Lastly, check if all required permissions are granted, if yes, show the view to user.
                if (isAllRequiredPermissionsGranted()) {
                    setRootView();
                }
            }
        });
        // We must not attempt to copy files unless we have storage permissions
        if (isAllRequiredPermissionsGranted()) {
            copyBootFilesAsyncTask.execute();
        } else {
            // Crude way of waiting for the permissions to be granted before we continue
            int t=0;
            while (!permissionCheck.isAllPermitted(PermissionCheck.DEFAULT_PERMISSIONS)) {
                try {
                    Thread.sleep(1000);
                    t++;
                    Log.d(TAG, "Permissions missing. Waiting ..." + t);
                } catch (InterruptedException e) {
                    Log.d(TAG, "Permissions missing. Waiting ...");
                }
                if (t>=10) {
                    break;
                }
            }
            if (permissionCheck.isAllPermitted(PermissionCheck.DEFAULT_PERMISSIONS)) {
                copyBootFilesAsyncTask.execute();
            } else {
                showWarningDialog("Permissions required", "Please restart application to finalize setup", true);
            }
        }

        int menuFragment = getIntent().getIntExtra("menuFragment", -1);
        if (menuFragment != -1) {
            desiredFragment = menuFragment;
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
        if (requestCode == PermissionCheck.DEFAULT_PERMISSION_RQCODE || requestCode == PermissionCheck.NH_TERM_PERMISSIONS_RQCODE) {
            for (int grantResult : grantResults) {
                if (grantResult != 0) {
                    if (getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.offsec.nhterm") == null) {
                        showWarningDialog("MaterialHunter app cannot be run properly", "NetHunter Terminal is not installed yet, please install from the store!", true);
                        return;
                    }
                    showWarningDialog("MaterialHunter app cannot be run properly", "Please grant all the permission requests from outside the app or restart the app to grant the rest of permissions again.", true);
                    return;
                }
            }
            if (isAllRequiredPermissionsGranted()) {
                setRootView();
            }
        }
    }

    @Override
    public boolean onReceiverReattach(KaliGPSUpdates.Receiver receiver) {
        if (LocationUpdateService.isInstanceCreated()) {
            // there is already a service running, we should re-attach to it
            this.locationUpdateReceiver = receiver;
            if (locationService != null) {
                locationService.requestUpdates(locationUpdateReceiver);
                return true; // reattached
            } else { // the app was probably re-launched.  the service is running but we've not bound it
                onLocationUpdatesRequested(receiver);
                return true;
            }
        }
        return false; // nothing to reattach to
    }

    @Override
    public void onLocationUpdatesRequested(KaliGPSUpdates.Receiver receiver) {
        locationUpdatesRequested = true;
        this.locationUpdateReceiver = receiver;
        Intent intent = new Intent(getApplicationContext(), LocationUpdateService.class);
        bindService(intent, locationServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onBackPressed() {
        //If isBackPressEnable is false then not allow user to press back button.
        if (isBackPressEnabled) {
            super.onBackPressed();
            if (titles.size() > 1) {
                titles.pop();
                mTitle = titles.peek();
            }
            Menu menuNav = navigationView.getMenu();
            int i = 0;
            int mSize = menuNav.size();
            while (i < mSize) {
                if (menuNav.getItem(i).getTitle() == mTitle) {
                    MenuItem _current = menuNav.getItem(i);
                    if (lastSelectedMenuItem != _current) {
                        lastSelectedMenuItem.setChecked(false);
                        lastSelectedMenuItem = _current;
                    }
                    //set checked
                    _current.setChecked(true);
                    i = mSize;
                }
                i++;
            }
            restoreActionBar();
        }
    }

    @Override
    public void onStopRequested() {
        locationUpdatesRequested = false;
        if (locationService != null) {
            locationService.stopUpdates();
            locationService = null;
        }
        if (updateServiceBound) {
            updateServiceBound = false;
            unbindService(locationServiceConnection);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (navigationView != null)
            startService(new Intent(getApplicationContext(), CompatCheckService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lastSelectedMenuItem = null;
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
        @SuppressLint("InflateParams") LinearLayout navigationHeadView = (LinearLayout) inflater.inflate(R.layout.sidenav_header, null);
        navigationView.addHeaderView(navigationHeadView);

        FloatingActionButton readmeButton = navigationHeadView.findViewById(R.id.info_fab);
        readmeButton.setOnClickListener(view -> showLicense());

        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        ImageView o = findViewById(R.id.w);
        o.setAlpha(prefs.getInt(SharePrefTag.BACKGROUND_ALPHA_LEVEL, 0));
        Drawable b = WallpaperManager.getInstance(this).getDrawable();
        o.setImageDrawable(b);

        @SuppressLint("CutPasteId") NavigationView o0 = findViewById(R.id.navigation_view);
        o0.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBars));

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, MaterialHunterFragment.newInstance(R.id.materialhunter_item))
                .commit();

        // and put the title in the queue for when you need to back through them
        titles.push(navigationView.getMenu().getItem(0).getTitle().toString());
        // disable all fragment first until it passes the compat check.
        navigationView.getMenu().setGroupEnabled(R.id.chrootDependentGroup, false);
        // if the nav bar hasn't been seen, let's show it
        if (!prefs.getBoolean("seenNav", false)) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            SharedPreferences.Editor ed = prefs.edit();
            ed.putBoolean("seenNav", true);
            ed.apply();
        }

        if (lastSelectedMenuItem == null) { // only in the 1st create
            lastSelectedMenuItem = navigationView.getMenu().getItem(0);
            lastSelectedMenuItem.setChecked(true);
        }
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_opened, R.string.drawer_closed);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        startService(new Intent(getApplicationContext(), CompatCheckService.class));

        if (desiredFragment != -1) {
            changeDrawer(desiredFragment);
            desiredFragment = -1;
        }
    }

    private void showLicense() {
        final View rootView = getLayoutInflater().inflate(R.layout.license_layout, null);
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setView(rootView)
                .setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        AlertDialog ad = adb.create();
        if (ad.getWindow() != null) {
            ad.getWindow().getAttributes().windowAnimations = R.style.DialogStyle;
            ad.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        ad.setCancelable(true);
        ad.show();
        ((TextView) Objects.requireNonNull(ad.findViewById(android.R.id.message))).setMovementMethod(LinkMovementMethod.getInstance());
    }

    @SuppressLint("NonConstantResourceId")
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    // only change it if is not the same as the last one
                    if (lastSelectedMenuItem != menuItem) {
                        //remove last
                        if (lastSelectedMenuItem != null)
                            lastSelectedMenuItem.setChecked(false);
                        // update for the next
                        lastSelectedMenuItem = menuItem;
                    }
                    //set checked
                    menuItem.setChecked(true);
                    mDrawerLayout.closeDrawers();
                    mTitle = menuItem.getTitle();
                    titles.push(mTitle.toString());

                    int itemId = menuItem.getItemId();
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
            case R.id.deauth_item:
                if (new File(NhPaths.CHROOT_SYMLINK_PATH + "/usr/sbin/iw").exists()) {
                    changeFragment(fragmentManager, DeAuthFragment.newInstance(itemId));
                } else {
                    showWarningDialog("", getString(R.string.toast_need_iw), false);
                }
                break;
            case R.id.services_item:
                changeFragment(fragmentManager, ServicesFragment.newInstance(itemId));
                break;
            case R.id.custom_commands_item:
                changeFragment(fragmentManager, CustomCommandsFragment.newInstance(itemId));
                break;
            case R.id.hid_item:
                changeFragment(fragmentManager, HidFragment.newInstance(itemId));
                break;
            case R.id.duckhunter_item:
                changeFragment(fragmentManager, DuckHunterFragment.newInstance(itemId));
                break;
            case R.id.usbarmory_item:
                if (new File("/config/usb_gadget").exists()) {
                    changeFragment(fragmentManager, USBArmoryFragment.newInstance(itemId));
                } else {
                    showWarningDialog("", getString(R.string.toast_need_configfs), false);
                }
                break;
	    case R.id.badusb_item:
                changeFragment(fragmentManager, BadusbFragment.newInstance(itemId));
                break;
            case R.id.mana_item:
                changeFragment(fragmentManager, ManaFragment.newInstance(itemId));
                break;
            case R.id.bt_item:
                changeFragment(fragmentManager, BTFragment.newInstance(itemId));
                break;
            case R.id.macchanger_item:
                changeFragment(fragmentManager, MacchangerFragment.newInstance(itemId));
                break;
            case R.id.createchroot_item:
                changeFragment(fragmentManager, ChrootManagerFragment.newInstance(itemId));
                break;
            case R.id.mpc_item:
                changeFragment(fragmentManager, MPCFragment.newInstance(itemId));
                break;
            case R.id.mitmf_item:
                changeFragment(fragmentManager, MITMfFragment.newInstance(itemId));
                break;
            case R.id.vnc_item:
                if (getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.offsec.nethunter.kex") == null) {
                    showWarningDialog("", getString(R.string.toast_install_kex), false);
                } else {
                    changeFragment(fragmentManager, VNCFragment.newInstance(itemId));
                }
                break;
            case R.id.searchsploit_item:
                if (new File(NhPaths.CHROOT_SYMLINK_PATH + "/usr/share/exploitdb").exists()) {
                    changeFragment(fragmentManager, SearchSploitFragment.newInstance(itemId));
                } else {
                    showWarningDialog("", getString(R.string.toast_install_exploitdb), false);
                }
                break;
            case R.id.nmap_item:
                changeFragment(fragmentManager, NmapFragment.newInstance(itemId));
                break;
            case R.id.pineapple_item:
                changeFragment(fragmentManager, PineappleFragment.newInstance(itemId));
                break;
            case R.id.gps_item:
                if (new File(NhPaths.CHROOT_SYMLINK_PATH + "/usr/sbin/gpsd").exists()) {
                    changeFragment(fragmentManager, GpsServiceFragment.newInstance(itemId));
                } else {
                    showWarningDialog("", getString(R.string.toast_install_gpsd), false);
                }
                break;
            case R.id.mhsettings_item:
                changeFragment(fragmentManager, MHSettingsFragment.newInstance(itemId));
                break;
            case R.id.settings_item:
                changeFragment(fragmentManager, SettingsFragment.newInstance(itemId));
                break;
            case R.id.terminal_item:
                try {
                    Intent intent = new Intent("com.offsec.nhterm.RUN_SCRIPT_NH");
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.putExtra("com.offsec.nhterm.iInitialCommand", "");
                    startActivity(intent);
                } catch (Exception e) {
                    NhPaths.showMessage(this, getString(R.string.toast_install_terminal));
                }
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

    public void setDefaultSharePreference() {
        if (prefs.getString(SharePrefTag.DUCKHUNTER_LANG_SHAREPREF_TAG, null) == null) {
            prefs.edit().putString(SharePrefTag.DUCKHUNTER_LANG_SHAREPREF_TAG, "us").apply();
        }
        if (prefs.getString(SharePrefTag.CHROOT_DEFAULT_BACKUP_SHAREPREF_TAG, null) == null) {
            prefs.edit().putString(SharePrefTag.CHROOT_DEFAULT_BACKUP_SHAREPREF_TAG, NhPaths.SD_PATH + "/kalifs-backup.tar.xz").apply();
        }
        if (prefs.getString(SharePrefTag.CHROOT_DEFAULT_STORE_DOWNLOAD_SHAREPREF_TAG, null) == null) {
            prefs.edit().putString(SharePrefTag.CHROOT_DEFAULT_STORE_DOWNLOAD_SHAREPREF_TAG, NhPaths.SD_PATH + "/Download").apply();
        }
    }

    private void changeFragment(FragmentManager fragmentManager, Fragment fragment) {
        fragmentManager
                .beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private boolean isAllRequiredPermissionsGranted() {
        if (permissionCheck.isAllPermitted(PermissionCheck.DEFAULT_PERMISSIONS)) {
            permissionCheck.checkPermissions(PermissionCheck.DEFAULT_PERMISSIONS, PermissionCheck.DEFAULT_PERMISSION_RQCODE);
            return false;
        } else if (permissionCheck.isAllPermitted(PermissionCheck.NH_TERM_PERMISSIONS)) {
            permissionCheck.checkPermissions(PermissionCheck.NH_TERM_PERMISSIONS, PermissionCheck.NH_TERM_PERMISSIONS_RQCODE);
            return false;
        }
        return true;
    }

    public void showWarningDialog(String title, String message, boolean NeedToExit) {
        android.app.AlertDialog.Builder warningAD = new android.app.AlertDialog.Builder(this);
        warningAD.setCancelable(false);
        warningAD.setTitle(title);
        warningAD.setMessage(message);
        warningAD.setPositiveButton("CLOSE", (dialog, which) -> {
            dialog.dismiss();
            if (NeedToExit)
                System.exit(1);
        });
        warningAD.create().show();
    }

    public class MaterialHunterReceiver extends BroadcastReceiver {
        public static final String CHECKCOMPAT = BuildConfig.APPLICATION_ID + ".CHECKCOMPAT";
        public static final String BACKPRESSED = BuildConfig.APPLICATION_ID + ".BACKPRESSED";
        public static final String CHECKCHROOT = BuildConfig.APPLICATION_ID + ".CHECKCHROOT";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case CHECKCOMPAT:
                        showWarningDialog("MaterialHunter app cannot be run properly",
                                intent.getStringExtra("message"),
                                true);
                        break;
                    case BACKPRESSED:
                        isBackPressEnabled = (intent.getBooleanExtra("isEnable", true));
                        if (isBackPressEnabled) {
                            restoreActionBar();
                        } else {
                            blockActionBar();
                        }
                        break;
                    case CHECKCHROOT:
                        try {
                            if (intent.getBooleanExtra("ENABLEFRAGMENT", false)) {
                                navigationView.getMenu().setGroupEnabled(R.id.chrootDependentGroup, true);
                            } else {
                                navigationView.getMenu().setGroupEnabled(R.id.chrootDependentGroup, false);
                                if (lastSelectedMenuItem.getItemId() != R.id.materialhunter_item &&
                                        lastSelectedMenuItem.getItemId() != R.id.createchroot_item) {
                                    FragmentManager fragmentManager = getSupportFragmentManager();
                                    changeFragment(fragmentManager, MaterialHunterFragment.newInstance(R.id.materialhunter_item));
                                }
                            }
                        } catch (Exception e) {
                            if (e.getMessage() != null) {
                                Log.e(AppNavHomeActivity.TAG, e.getMessage());
                            } else {
                                Log.e(AppNavHomeActivity.TAG, "e.getMessage is Null.");
                            }
                        }
                        break;
                }
            }
        }
    }
}
