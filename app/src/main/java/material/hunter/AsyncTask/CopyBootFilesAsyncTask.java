package material.hunter.AsyncTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import material.hunter.AppNavHomeActivity;
import material.hunter.utils.CheckForRoot;
import material.hunter.utils.NhPaths;
import material.hunter.utils.SharePrefTag;
import material.hunter.utils.ShellExecuter;

public class CopyBootFilesAsyncTask extends AsyncTask<String, String, String> {

    private final String TAG = "CopyBootFilesAsyncTask";
    private final WeakReference<ProgressDialog> progressDialogRef;
    private final WeakReference<Context> context;
    private File scriptsDir;
    private File etcDir;
    private String buildTime;
    private Boolean shouldRun;
    private CopyBootFilesAsyncTaskListener listener;
    private String result = "";
    private SharedPreferences prefs;
    private ShellExecuter exe = new ShellExecuter();

    public CopyBootFilesAsyncTask(Context context, Activity activity, ProgressDialog progressDialog) {
        this.context = new WeakReference<>(context);
        this.progressDialogRef = new WeakReference<>(progressDialog);
        this.scriptsDir = new File(NhPaths.APP_SCRIPTS_PATH);
        this.etcDir = new File(NhPaths.APP_INITD_PATH);
        this.prefs = context.getSharedPreferences("material.hunter", Context.MODE_PRIVATE);
        this.shouldRun = true;
    }

    @Override
    protected void onPreExecute() {
        // Check if it is a new build and inflates the materialhunter files again if yes.
        // Added versionCode tag to shareprefence to check if the versionCode is different from previous install, this fix the new updated installation not copying files.
        if (!scriptsDir.isDirectory() || !etcDir.isDirectory()) {
            ProgressDialog progressDialog = progressDialogRef.get();
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setTitle("New app build detected");
            progressDialog.setMessage("Copying new files...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        } else {
            shouldRun = false;
        }
        super.onPreExecute();
        if (listener != null) {
            listener.onAsyncTaskPrepare();
        }
    }

    @Override
    protected String doInBackground(String... strings) {
        // setup
        if (shouldRun) {
            if (!CheckForRoot.isRoot()) {
                prefs.edit().putBoolean(AppNavHomeActivity.CHROOT_INSTALLED_TAG, false).apply();
                return "Root permission is required!";
            }
            // 1:1 copy (recursive) of the assets/{scripts, etc, wallpapers} folders to /data/data/...
            publishProgress("Doing app files update.");
            assetsToFiles(NhPaths.APP_PATH, "", "data");
            publishProgress("Fixing permissions for new files");
            exe.RunAsRoot(new String[]{"chmod -R 700 " + NhPaths.APP_SCRIPTS_PATH + "/*", "chmod -R 700 " + NhPaths.APP_INITD_PATH + "/*"});
            // disable the magisk notification for materialhunter app as it will keep popping up bunch of toast message when executing runtime command.
            disableMagiskNotification();
            SharedPreferences.Editor ed = prefs.edit();
            ed.putString(TAG, buildTime);
            ed.apply();

            publishProgress("Checking for chroot...");
            String command = "if [ -d " + NhPaths.CHROOT_PATH() + " ];then echo 1; fi"; //check the dir existence
            final String _res = exe.RunAsRootOutput(command);
            if (_res.equals("1")) {
                ed = prefs.edit();
                ed.putBoolean(AppNavHomeActivity.CHROOT_INSTALLED_TAG, true);
                ed.commit();
                publishProgress("Chroot Found!");

                // Mount suid /data && fix sudo - this is definitely needed as of 02/2020, Re4son
                publishProgress(exe.RunAsRootOutput(NhPaths.BUSYBOX + " mount -o remount,suid /data && chmod +s " +
                        NhPaths.CHROOT_PATH() + "/usr/bin/sudo" +
                        " && echo \"Initial setup done!\""));
            } else {
                publishProgress("Chroot not Found, install it in Chroot Manager");
            }
        }
        return result;
    }

    private Boolean pathIsAllowed(String path, String copyType) {
        // never copy images, sounds or webkit
        if (!path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit")) {
            if (copyType.equals("data")) {
                if (path.equals("")) {
                    return true;
                } else if (path.startsWith("scripts")) {
                    return true;
                } else if (path.startsWith("wallpapers")) {
                    return true;
                } else return path.startsWith("etc");
            }
            return false;
        }
        return false;
    }

    // now this only copies the folders: scripts, etc , wallpapers to /data/data...
    private void assetsToFiles(String TARGET_BASE_PATH, String path, String copyType) {
        AssetManager assetManager = context.get().getAssets();
        String[] assets;
        try {
            // Log.i("tag", "assetsTo" + copyType +"() "+path);
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFile(TARGET_BASE_PATH, path);
            } else {
                String fullPath = TARGET_BASE_PATH + "/" + path;
                // Log.i("tag", "path="+fullPath)

                File dir = new File(fullPath);
                if (!dir.exists() && pathIsAllowed(path, copyType)) { // copy those dirs
                    if (!dir.mkdirs()) {
                        ShellExecuter create = new ShellExecuter();
                        create.RunAsRoot(new String[]{"mkdir " + fullPath});
                        if (!dir.exists()) {
                            Log.i(TAG, "could not create dir " + fullPath);
                        }
                    }
                }
                for (String asset : assets) {
                    String p;
                    if (path.equals("")) {
                        p = "";
                    } else {
                        p = path + "/";
                    }
                    if (pathIsAllowed(path, copyType)) {
                        assetsToFiles(TARGET_BASE_PATH, p + asset, copyType);
                    }
                }

            }
        } catch (IOException ex) {
            Log.e(TAG, "I/O Exception", ex);
        }
    }

    private void copyFile(String TARGET_BASE_PATH, String filename) {
        if (filename.matches("^.*/services$|^.*/runonboot_services$")) {
            return;
        }
        AssetManager assetManager = context.get().getAssets();
        InputStream in;
        OutputStream out;
        String newFileName = null;
        try {
            // Log.i("tag", "copyFile() "+filename);
            in = assetManager.open(filename);
            newFileName = TARGET_BASE_PATH + "/" + filename;
            /* rename the file name if its suffix is either -arm64 or -armhf before copying the file.*/
            out = new FileOutputStream(renameAssetIfneeded(newFileName));
            byte[] buffer = new byte[8092];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            ShellExecuter copy = new ShellExecuter();
            copy.RunAsRoot(new String[]{"cp " + filename + " " + TARGET_BASE_PATH});
        }

    }

    // Check for symlink for bootkali
    // http://stackoverflow.com/questions/813710/java-1-6-determine-symbolic-links/813730#813730
    private boolean isSymlink(File file) throws IOException {
        if (file == null)
            throw new NullPointerException("File must not be null");
        File canon;
        if (file.getParent() == null) {
            canon = file;
        } else {
            File canonDir = file.getParentFile().getCanonicalFile();
            canon = new File(canonDir, file.getName());
        }
        return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
    }

    private void MakeSYSWriteable() {
        Log.d(TAG, "Making /system writeable for symlink");
        exe.RunAsRoot(new String[]{"mount -o rw,remount /"});
    }

    private void MakeSYSReadOnly() {
        Log.d(TAG, "Making /system readonly for symlink");
        exe.RunAsRoot(new String[]{"mount -o ro,remount /"});
    }

    private void NotFound(String filename) {
        Log.d(TAG, "Symlinking: " + filename);
        exe.RunAsRoot(new String[]{"ln -sf " + NhPaths.APP_SCRIPTS_PATH + "/" + filename + " /system/xbin/" + filename});
    }

    // Get a list of files from a directory
    private ArrayList<String> FetchFiles(String folder) {

        ArrayList<String> filenames = new ArrayList<String>();
        File directory = new File(folder);

        if (directory.exists()) {
            try {
                File[] files = directory.listFiles();
                assert files != null;
                for (File file : files) {
                    String file_name = file.getName();
                    filenames.add(file_name);
                }
            } catch (NullPointerException e) {
                Log.e(TAG, folder + " is an empty folder, filenames is returned as a empty String ArrayList.");
                e.printStackTrace();
            }
        }
        return filenames;
    }

    // This rename the filename which suffix is either [name]-arm64 or [name]-armhf to [name] according to the user's CPU ABI.
    private String renameAssetIfneeded(String asset) {
        if (asset.matches("^.*-arm64$")) {
            if (Build.CPU_ABI.equals("arm64-v8a")) {
                return (asset.replaceAll("-arm64$", ""));
            }
        } else if (asset.matches("^.*-armeabi$")) {
            if (!Build.CPU_ABI.equals("arm64-v8a")) {
                return (asset.replaceAll("-armeabi$", ""));
            }
        }
        return asset;
    }

    private void disableMagiskNotification() {
        if (exe.RunAsRootReturnValue("[ -f " + NhPaths.MAGISK_DB_PATH + " ]") == 0) {
            Log.d(TAG, "Disabling magisk notifcication and log for MaterialHunter.");
            if (exe.RunAsRootOutput(NhPaths.APP_SCRIPTS_BIN_PATH + "/sqlite3 " +
                    NhPaths.MAGISK_DB_PATH +
                    " \"UPDATE policies SET logging='0',notification='0' WHERE package_name='" +
                    "material.hunter" + "';\"").isEmpty()) {
                Log.d(TAG, "Updated magisk db successfully.");
            } else {
                Log.e(TAG, "Failed updating to magisk db.");
            }
        } else {
            Log.e(TAG, NhPaths.MAGISK_DB_PATH + " not found, skip disabling the magisk notification for MaterialHunter.");
        }
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        super.onProgressUpdate(progress);
        if (progressDialogRef.get() != null)
            progressDialogRef.get().setMessage(progress[0]);
    }

    @Override
    protected void onPostExecute(String objects) {
        super.onPostExecute(objects);
        if (progressDialogRef.get() != null && progressDialogRef.get().isShowing())
            progressDialogRef.get().dismiss();
        if (listener != null) {
            listener.onAsyncTaskFinished(result);
        }
    }

    public void setListener(CopyBootFilesAsyncTaskListener listener) {
        this.listener = listener;
    }

    public interface CopyBootFilesAsyncTaskListener {
        void onAsyncTaskPrepare();

        void onAsyncTaskFinished(Object result);
    }
}
