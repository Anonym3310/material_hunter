package material.hunter;

import android.annotation.SuppressLint;
import android.app.Application;
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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.util.Objects;
import java.util.Stack;

import material.hunter.AsyncTask.CopyBootFilesAsyncTask;
import material.hunter.SQL.MaterialHunterSQL;
import material.hunter.service.CompatCheckService;
import material.hunter.utils.CheckForRoot;
import material.hunter.utils.NhPaths;
import material.hunter.utils.PermissionCheck;
import material.hunter.utils.SharePrefTag;

import material.hunter.SQL.ServicesSQL;
import material.hunter.SQL.USBArmorySQL;

public class AppNavHomeActivity extends AppCompatActivity {
    public final static String TAG = "AppNavHomeActivity";
    public static final String CHROOT_INSTALLED_TAG = "CHROOT_INSTALLED_TAG";
    public static final String GPS_BACKGROUND_FRAGMENT_TAG = "BG_FRAGMENT_TAG";
    public static final String BOOT_CHANNEL_ID = "BOOT_CHANNEL";
	public static MenuItem lastSelectedMenuItem;
    private final Stack<String> titles = new Stack<>();
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
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
        nhPaths = NhPaths.getInstance(getApplicationContext());
        permissionCheck = new PermissionCheck(this, getApplicationContext());
		materialhunterReceiver = new MaterialHunterReceiver();
        IntentFilter AppNavHomeIntentFilter = new IntentFilter();
        AppNavHomeIntentFilter.addAction(MaterialHunterReceiver.CHECKCOMPAT);
        AppNavHomeIntentFilter.addAction(MaterialHunterReceiver.CHECKCHROOT);
        AppNavHomeIntentFilter.addAction("ChrootManager");
        registerReceiver(materialhunterReceiver, new IntentFilter(AppNavHomeIntentFilter));
        prefs = getSharedPreferences("material.hunter", Context.MODE_PRIVATE);

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
                USBArmorySQL.getInstance(getApplicationContext());

                // Setup the default SharePreference value.
                setDefaultSharePreference();

                // After finishing copying app files, we do a compatibility check before allowing user to use it.
                // First, check if the app has gained the root already.
                if (!CheckForRoot.isRoot()) {
                    showWarningDialog("MaterialHunter app cannot be run properly", "Root permission is required!", true);
                }

                // Grant "Manage All Files" permission for Android 11+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (!Environment.isExternalStorageManager()){
                        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        intent.addCategory("android.intent.category.DEFAULT");
                        intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                        startActivity(intent);
			        }
		        }

                // Lastly, show the view to user.
				setRootView();
            }
        });
        // We must not attempt to copy files unless we have storage permissions
        if (isAllRequiredPermissionsGranted()) {
            copyBootFilesAsyncTask.execute();
        } else {
            // Crude way of waiting for the permissions to be granted before we continue
            int t=0;
            while (!permissionCheck.isAllPermitted(PermissionCheck.DEFAULT)) {
                try {
                    Thread.sleep(1000);
                    t++;
                    Log.d(TAG, "Permissions missing. Waiting... " + t);
                } catch (InterruptedException e) {
                    Log.d(TAG, "Permissions missing. Waiting...");
                }
                if (t>=10) {
					Log.d(TAG, "Failed to fetch permissions.");
                    break;
                }
            }
            if (permissionCheck.isAllPermitted(PermissionCheck.DEFAULT)) {
                copyBootFilesAsyncTask.execute();
            } else {
                showWarningDialog("Permissions required", "Please restart application to finalize setup.", true);
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
        if (requestCode == PermissionCheck.DEFAULT_RCODE || requestCode == PermissionCheck.STAFF_RCODE) {
            for (int grantResult : grantResults) {
                if (grantResult != 0) {
                    if (getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.termux") == null) {
                        showWarningDialog("MaterialHunter app cannot be run properly", "Termux isn`t installed yet, please install it from F-Droid!", false);
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
    public void onBackPressed() {
		if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        } else {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startService(new Intent(getApplicationContext(), CompatCheckService.class));
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
        @SuppressLint("InflateParams") LinearLayout navigationHeadView = (LinearLayout) inflater.inflate(R.layout.sidenav_header, null);
        navigationView.addHeaderView(navigationHeadView);

        TextView easter_egg = navigationHeadView.findViewById(R.id.easter_egg);
		easter_egg.setOnLongClickListener(view -> {
			if (keyword) {
		        showLicense();
			} else {
				NhPaths.showMessage(getApplicationContext(), "¯\\_(ツ)_/¯", false);
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

        // and put the title in the queue for when you need to back through them
        titles.push(navigationView.getMenu().getItem(0).getTitle().toString());
        // disable all fragment first until it passes the compat check.
        navigationView.getMenu().setGroupEnabled(R.id.chrootDependentGroup, false);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_opened, R.string.drawer_closed);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        startService(new Intent(getApplicationContext(), CompatCheckService.class));
    }

    private void showLicense() {
        final View rootView = getLayoutInflater().inflate(R.layout.license_layout, null);
        MaterialAlertDialogBuilder adb = new MaterialAlertDialogBuilder(this);
        adb.setView(rootView)
                .setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
	    adb.setCancelable(true);
		AlertDialog ad = adb.create();
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
                return true;
            }
		);
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
				    intent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", new String[]{"-c", NhPaths.APP_SCRIPTS_PATH + "/bootroot_login"});
				    intent.putExtra("com.termux.RUN_COMMAND_WORKDIR", "/data/data/com.termux/files/home");
			        intent.putExtra("com.termux.RUN_COMMAND_BACKGROUND", false);
			        intent.putExtra("com.termux.RUN_COMMAND_SESSION_ACTION", "0");
		            startService(intent);
                } catch (Exception e) {
                    NhPaths.showMessage(this, "Termux isn`t installed.", true);
                }
                break;
			case R.id.services_item:
                changeFragment(fragmentManager, ServicesFragment.newInstance(itemId));
                break;
        }
    }

    public void setDefaultSharePreference() {
        if (prefs.getString(SharePrefTag.CHROOT_DEFAULT_BACKUP_SHAREPREF_TAG, null) == null) {
            prefs.edit().putString(SharePrefTag.CHROOT_DEFAULT_BACKUP_SHAREPREF_TAG, NhPaths.SD_PATH + "/mh-backup.tar.xz").apply();
        }
        if (prefs.getString(SharePrefTag.CHROOT_DEFAULT_STORE_DOWNLOAD_SHAREPREF_TAG, null) == null) {
			prefs.edit().putString(SharePrefTag.CHROOT_DEFAULT_STORE_DOWNLOAD_SHAREPREF_TAG, "");
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
        if (permissionCheck.isAllPermitted(PermissionCheck.DEFAULT)) {
            permissionCheck.checkPermissions(PermissionCheck.DEFAULT, PermissionCheck.DEFAULT_RCODE);
            return false;
        } else if (permissionCheck.isAllPermitted(PermissionCheck.STAFF)) {
            permissionCheck.checkPermissions(PermissionCheck.STAFF, PermissionCheck.STAFF_RCODE);
            return false;
        }
        return true;
    }

    public void showWarningDialog(String title, String message, boolean NeedToExit) {
        MaterialAlertDialogBuilder warningAD = new MaterialAlertDialogBuilder(this);
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
        public static final String CHECKCOMPAT = "material.hunter.CHECKCOMPAT";
        public static final String CHECKCHROOT = "material.hunter.CHECKCHROOT";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case CHECKCOMPAT:
                        showWarningDialog("MaterialHunter app cannot be run properly",
                            intent.getStringExtra("message"),
                            true);
                        break;
                    case CHECKCHROOT:
                        try {
                            if (intent.getBooleanExtra("ENABLEFRAGMENT", false)) {
                                navigationView.getMenu().setGroupEnabled(R.id.chrootDependentGroup, true);
                            } else {
                                navigationView.getMenu().setGroupEnabled(R.id.chrootDependentGroup, false);
                            }
                        } catch (Exception e) { }
                        break;
                }
            }
        }
	}
}
