package material.hunter;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import material.hunter.service.CompatCheckService;
import material.hunter.utils.ActiveShellExecuter;
import material.hunter.utils.DownloadChroot;
import material.hunter.utils.MHRepo;
import material.hunter.utils.PathsUtil;
import material.hunter.utils.ShellExecuter;

import melville37.contract.JSON;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ManagerFragment extends Fragment {

    private Activity activity;
    private Context context;
    private ActionBar actionBar;
    private ExecutorService executor;
    private ShellExecuter exe = new ShellExecuter();
    private TextView resultViewerLoggerTextView;
    private Button mountChrootButton;
    private Button unmountChrootButton;
    private Button installChrootButton;
    private Button removeChrootButton;
    private Button backupChrootButton;
    private SharedPreferences sharedPreferences;
    private LinearProgressIndicator progressbar;

    private static final int IS_MOUNTED = 0;
    private static final int IS_UNMOUNTED = 1;
    private static final int NEED_TO_INSTALL = 2;
    private static final int CHROOT_CORRUPTED = 3;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        activity = getActivity();
        context = getContext();
        actionBar = MainActivity.getActionBarView();
        executor = Executors.newSingleThreadExecutor();
        sharedPreferences = context.getSharedPreferences("material.hunter", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.manager_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressbar = view.findViewById(R.id.progressbar);
        resultViewerLoggerTextView = view.findViewById(R.id.logger);
        mountChrootButton = view.findViewById(R.id.start);
        unmountChrootButton = view.findViewById(R.id.stop);
        installChrootButton = view.findViewById(R.id.install);
        backupChrootButton = view.findViewById(R.id.backup);
        removeChrootButton = view.findViewById(R.id.remove);

        setStopButton();
        setStartButton();
        setInstallButton();
        setRemoveButton();
        setBackupButton();

        showBanner();
        compatCheck();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.manager, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_logger:
                resultViewerLoggerTextView.setText("");
                break;
            case R.id.update_chroot_status:
                compatCheck();
                break;
            case R.id.edit_chroot_folder:
                MaterialAlertDialogBuilder edit = new MaterialAlertDialogBuilder(activity);
                View view_edit =
                        getLayoutInflater().inflate(R.layout.manager_dialog_edit, null);
                AutoCompleteTextView path =
                        view_edit.findViewById(R.id.path);
                String path_now = sharedPreferences.getString("chroot_directory", "chroot");
                path.setText(path_now);
                ArrayList<String> chroots = new ArrayList<>();
                for (String file : exe.RunAsRootOutput("for i in $(ls " + PathsUtil.SYSTEM_PATH() + "); do test -d " + PathsUtil.SYSTEM_PATH() + "/$i && echo $i; done").split("\n")) {
                    if (!file.isEmpty()) chroots.add(file);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, R.layout.mh_spinner_item, chroots);
                path.setAdapter(adapter);
                edit.setView(view_edit);
                edit.setPositiveButton("Apply", (dialogInterface, i) -> {});
                edit.setNegativeButton("Cancel", (dialogInterface2, i2) -> {});
                final AlertDialog editAd = edit.create();
                editAd.setOnShowListener(dialog -> {
                    final Button apply = editAd.getButton(DialogInterface.BUTTON_POSITIVE);
                    apply.setOnClickListener(v -> {
                        if (path.getText().toString().matches("^[A-z0-9.\\/\\-_~]+$")) {
                            sharedPreferences
                                .edit()
                                .putString("chroot_directory", path.getText().toString())
                                .apply();
                            editAd.dismiss();
                            compatCheck();
                        } else PathsUtil.showSnack(getView(), "Invalid chroot directory name.", false);
                    });
                });
                editAd.show();
                break;
            case R.id.change_system_path:
                MaterialAlertDialogBuilder change_system_path = new MaterialAlertDialogBuilder(context);
                View view_change_system_path = getLayoutInflater().inflate(R.layout.manager_dialog_settings_change_system_path, null);
                TextInputEditText input1 = view_change_system_path.findViewById(R.id.new_system_path);

                input1.setText(sharedPreferences.getString("chroot_system_path", "/data/local/nhsystem"));

                change_system_path.setView(view_change_system_path);
                change_system_path.setPositiveButton("Apply", (dialogInterface, i) -> {});
                change_system_path.setNegativeButton("Cancel", (dialogInterface, i) -> {});
                final AlertDialog ad1 = change_system_path.create();
                ad1.setOnShowListener(dialog -> {
                    final Button apply = ad1.getButton(DialogInterface.BUTTON_POSITIVE);
                    apply.setOnClickListener(v -> {
                        String inputText = input1.getText().toString();
                        if (!inputText.matches("^[A-z0-9.\\/\\-_~]+$")) {
                            PathsUtil.showSnack(getView(), "Invalid system path.", false);
                        } else {
                            sharedPreferences.edit().putString("chroot_system_path", inputText).apply();
                            ad1.dismiss();
                            compatCheck();
                        }
                    });
                });
                ad1.show();
                break;
            case R.id.security_settings:
                MaterialAlertDialogBuilder security_settings = new MaterialAlertDialogBuilder(context);
                View view_security_settings = getLayoutInflater().inflate(R.layout.manager_dialog_settings_security, null);
                SwitchMaterial sdcard = view_security_settings.findViewById(R.id.mount_sdcard);
                SwitchMaterial system = view_security_settings.findViewById(R.id.mount_system);
                SwitchMaterial data = view_security_settings.findViewById(R.id.mount_data);
                TextInputEditText hostname = view_security_settings.findViewById(R.id.hostname);

                sdcard.setChecked(sharedPreferences.getBoolean("mount_sdcard", false));
                system.setChecked(sharedPreferences.getBoolean("mount_system", false));
                data.setChecked(sharedPreferences.getBoolean("mount_data", false));
                hostname.setText(sharedPreferences.getString("hostname", "android"));

                security_settings.setView(view_security_settings);
                security_settings.setPositiveButton("Apply", (dialogInterface, i) -> {});
                security_settings.setNegativeButton("Cancel", (dialogInterface, i) -> {});
                final AlertDialog ad2 = security_settings.create();
                ad2.setOnShowListener(dialog -> {
                    final Button apply = ad2.getButton(DialogInterface.BUTTON_POSITIVE);
                    apply.setOnClickListener(v -> {
	                    String _hostname = hostname.getText().toString();
                        if (!_hostname.matches("([a-zA-Z0-9-]){2,253}")) {
                            PathsUtil.showSnack(getView(), "Invalid hostname.", false);
                        } else {
                            sharedPreferences.edit().putBoolean("mount_sdcard", sdcard.isChecked()).apply();
                            sharedPreferences.edit().putBoolean("mount_system", system.isChecked()).apply();
                            sharedPreferences.edit().putBoolean("mount_data", data.isChecked()).apply();
                            sharedPreferences.edit().putString("hostname", _hostname).apply();
                            ad2.dismiss();
                            compatCheck();
                        }
                    });
                });
                ad2.show();
                break;
            case R.id.rename_chroot_folder:
                MaterialAlertDialogBuilder rename_chroot_folder = new MaterialAlertDialogBuilder(context);
                View view_rename_chroot_folder = getLayoutInflater().inflate(R.layout.manager_dialog_settings_rename_chroot_folder, null);
                TextInputEditText input2 = view_rename_chroot_folder.findViewById(R.id.new_chroot_folder_name);

                input2.setText(sharedPreferences.getString("chroot_directory", "chroot"));

                rename_chroot_folder.setView(view_rename_chroot_folder);
                rename_chroot_folder.setPositiveButton("Apply", (dialogInterface, i) -> {});
                rename_chroot_folder.setNegativeButton("Cancel", (dialogInterface, i) -> {});
                final AlertDialog ad3 = rename_chroot_folder.create();
                ad3.setOnShowListener(dialog -> {
                    final Button apply = ad3.getButton(DialogInterface.BUTTON_POSITIVE);
                    apply.setOnClickListener(v -> {
                        String inputText = input2.getText().toString();
                        if (!inputText.matches("^[A-z0-9.\\/\\-_~]+$"))
                            PathsUtil.showSnack(getView(), "Invalid folder name.", false);
                        else {
                            exe.RunAsRoot("mv " + PathsUtil.CHROOT_PATH() + " " + PathsUtil.SYSTEM_PATH() + "/" + inputText);
                            sharedPreferences.edit().putString("chroot_directory", inputText).apply();
                            ad3.dismiss();
                            compatCheck();
                        }
                    });
                });
                ad3.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setStartButton() {
        mountChrootButton.setOnClickListener(view -> {
            new ActiveShellExecuter(activity, context) {
                @Override
                public void onPrepare() {
                    setAllButtonEnable(false);
                }

                @Override
                public void onNewLine(String line) {}

                @Override
                public void onFinished(int code) {
                    if (code == 0) {
                        setButtonVisibilty(IS_MOUNTED);
                        setMountStatsTextView(IS_MOUNTED);
                        setAllButtonEnable(true);
                        compatCheck();
                    }
                }
            }.exec(PathsUtil.APP_SCRIPTS_PATH + "/bootroot", resultViewerLoggerTextView);
        });
    }

    private void setStopButton() {
        unmountChrootButton.setOnClickListener(view -> {
            new ActiveShellExecuter(activity, context) {
                @Override
                public void onPrepare() {
                    setAllButtonEnable(false);
                }

                @Override
                public void onNewLine(String line) {}

                @Override
                public void onFinished(int code) {
                    if (code == 0) {
                        setButtonVisibilty(IS_MOUNTED);
                        setMountStatsTextView(IS_MOUNTED);
                        setAllButtonEnable(true);
                        compatCheck();
                    }
                }
            }.exec(PathsUtil.APP_SCRIPTS_PATH + "/killroot", resultViewerLoggerTextView);
        });
    }

    private void setInstallButton() {
        installChrootButton.setOnClickListener(view -> {
            MaterialAlertDialogBuilder adb = new MaterialAlertDialogBuilder(context);
            View rootView = getLayoutInflater().inflate(R.layout.manager_dialog_install, null);
            Button db = rootView.findViewById(R.id.downloadButton);
            Button r = rootView.findViewById(R.id.repositoryButton);
            Button rb = rootView.findViewById(R.id.restoreButton);
            adb.setTitle("Install");
            adb.setView(rootView);
            AlertDialog ad = adb.create();
            db.setOnClickListener(view1 -> {
                ad.dismiss();
                MaterialAlertDialogBuilder adb1 = new MaterialAlertDialogBuilder(activity);
                View promtDownloadView = getLayoutInflater().inflate(R.layout.manager_dialog_download, null);
                final TextInputEditText input = promtDownloadView.findViewById(R.id.input);
                input.setText(sharedPreferences.getString("chroot_download_url_prev", ""));
                adb1.setView(promtDownloadView);
                adb1.setPositiveButton("Setup", (dialogInterface, i) -> {});
                adb1.setNegativeButton("Cancel", (dialogInterface, i) -> {});
                final AlertDialog adb1Ad = adb1.create();
                adb1Ad.setOnShowListener(dialog -> {
                    final Button setup = adb1Ad.getButton(DialogInterface.BUTTON_POSITIVE);
                    setup.setOnClickListener(v -> {
                        String chroot_url = input.getText().toString();
                        if (!chroot_url.equals("")) {
                            if (!chroot_url.matches("^(http|https):\\/\\/.*$")) {
                                chroot_url = "http://" + chroot_url;
                            }
                            if (!chroot_url.matches(".*\\.(tar\\.xz|tar\\.gz)$")) {
                                PathsUtil.showSnack(getView(), "Tarball must be xz or gz compression.", true);
                                return;
                            }
                        } else {
                            PathsUtil.showSnack(getView(), "URL can't be empty!", false);
                            return;
                        }
                        sharedPreferences.edit().putString("chroot_download_url_prev", chroot_url).apply();
                        String filename = chroot_url.substring(chroot_url.lastIndexOf('/') + 1, chroot_url.length());
                        File chroot = new File(PathsUtil.APP_PATH + "/" + filename);

                        new DownloadChroot(activity, context) {

                            @Override
                            public void onPrepare() {
                                adb1Ad.dismiss();
                                disableToolbarMenu(true);
                                setAllButtonEnable(false);
                                progressbar.setIndeterminate(false);
                                progressbar.show();
                                progressbar.setProgress(0);
                                progressbar.setMax(100);
                            }

                            @Override
                            public void onNewLine(String line) {}

                            @Override
                            public void onProgressUpdate(int progress) {
                                progressbar.setProgress(progress);
                            }

                            @Override
                            public void onFinished(int resultCode) {
                                setAllButtonEnable(true);
                                if (resultCode == 0) {
                                    new ActiveShellExecuter(activity, context) {

                                        @Override
                                        public void onPrepare() {
                                            setAllButtonEnable(false);
                                            progressbar.setIndeterminate(true);
                                        }

                                        @Override
                                        public void onNewLine(String line) {}

                                        @Override
                                        public void onFinished(int code) {
                                            disableToolbarMenu(false);
                                            setAllButtonEnable(true);
                                            compatCheck();
                                            progressbar.hide();
                                            progressbar.setIndeterminate(false);
                                            chroot.delete();
                                        }
                                    }.exec(
                                        PathsUtil.APP_SCRIPTS_PATH
                                                + "/chrootmgr -c \"restore "
                                                + chroot
                                                + " "
                                                + PathsUtil.CHROOT_PATH()
                                                + "\"",
                                        resultViewerLoggerTextView);
                                } else {
                                    progressbar.hide();
                                }
                            }
                        }.exec(chroot_url, chroot, resultViewerLoggerTextView);
                    });
                });
                adb1Ad.show();
            });
            r.setOnClickListener(view1 -> {
                ad.dismiss();
                MaterialAlertDialogBuilder adb2 = new MaterialAlertDialogBuilder(activity);
                View repov = getLayoutInflater().inflate(R.layout.manager_dialog_repository, null);
                TextView instruction = repov.findViewById(R.id.repository_instruction);
                TextInputLayout layout = repov.findViewById(R.id.repository_input_layout);
                final TextInputEditText input = repov.findViewById(R.id.repository_input);
                final AutoCompleteTextView selector = repov.findViewById(R.id.chroot_selector);
                int[] position = {0};
                instruction.setText(
                        Html.fromHtml("Create your own repository:\n"
                                + "<a href='https://github.com/Mirivan/dev-root-project/blob/main/REPOSITORY.md'>according to this instruction</a>",
                                Html.FROM_HTML_MODE_LEGACY));
                instruction.setMovementMethod(LinkMovementMethod.getInstance());
                input.setText(sharedPreferences.getString("chroot_prev_repository", context.getResources().getString(R.string.mh_repository)));

                ExecutorService executor = Executors.newSingleThreadExecutor();

                layout.setEndIconOnClickListener(v -> {
                    executor.execute(() -> {
                        try {
                            String repositoryUrl = input.getText().toString();
                            if (repositoryUrl.equals(""))
                                throw new NullPointerException();
                            if (!repositoryUrl.matches("^(http|https):\\/\\/.*$"))
                                repositoryUrl = "http://" + repositoryUrl;

                            JSONObject repo = JSON.getFromWeb(repositoryUrl);
                            if (!MHRepo.setRepo(repo))
                                throw new JSONException("Bad MaterialHunter repository.");
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.mh_spinner_item, MHRepo.getMainKeys());
                            new Handler(Looper.getMainLooper()).post(() -> {
                                selector.setText(adapter.getItem(0));
                                selector.setAdapter(adapter);
                            });
                        } catch (IOException e) {
                            PathsUtil.showSnack(getView(), "No internet connection, please try again later.", true);
                        } catch (JSONException e) {
                            PathsUtil.showSnack(getView(), "Bad repository, contact it's author.", true);
                        } catch (NullPointerException e) {
                            PathsUtil.showSnack(getView(), "Repository url must not be empty.", true);
                        }
                    });
                });

                selector.setOnItemClickListener((parent, v, p, l) -> {
                    position[0] = p;
                });

                adb2.setView(repov);
                adb2.setPositiveButton("Download", (dialogInterface, i) -> {});
                adb2.setNegativeButton("Cancel", (dialogInterface, i) -> {});
                final AlertDialog adb2Ad = adb2.create();
                adb2Ad.setOnShowListener(dialog -> {
                    final Button download = adb2Ad.getButton(DialogInterface.BUTTON_POSITIVE);
                    download.setOnClickListener(v -> {
                        String chroot_url = "";
                        String[] chroot_author = {""};

                        try {
                            String chroot_string = MHRepo.getKeyData(Integer.toString(position[0]));
                            JSONObject chroot_json = new JSONObject(chroot_string);

                            if (chroot_json.has("url"))
                                chroot_url = chroot_json.getString("url");
                            else if (chroot_json.has("file")) {
                                String inputText = input.getText().toString();
                                chroot_url = inputText.substring(inputText.lastIndexOf('/') + 1, inputText.length()) + "/" + chroot_json.getString("file");
                            } else throw new NullPointerException();
                            chroot_author[0] = chroot_json.getString("author");

                            if (chroot_url.equals(""))
                                throw new NullPointerException();

                            if (!chroot_url.matches("^(http|https):\\/\\/.*$"))
                                chroot_url = "http://" + chroot_url;

                            if (!chroot_url.matches(".*\\.(tar\\.xz|tar\\.gz)$")) {
                                PathsUtil.showSnack(getView(), "Bad chroot type, contact repository author.", true);
                                return;
                            }

                        } catch (JSONException e) {
                            PathsUtil.showSnack(getView(), "Bad repository skeleton.", true);
                            return;
                        } catch (NullPointerException e) {
                            PathsUtil.showSnack(getView(), "Chroot url must not be empty.", true);
                            return;
                        }

                        sharedPreferences.edit().putString("chroot_prev_repository", input.getText().toString()).apply();
                        String filename = chroot_url.substring(chroot_url.lastIndexOf('/') + 1, chroot_url.length());
                        File chroot = new File(PathsUtil.APP_PATH + "/" + filename);

                        new DownloadChroot(activity, context) {

                            @Override
                            public void onPrepare() {
                                adb2Ad.dismiss();
                                disableToolbarMenu(true);
                                setAllButtonEnable(false);
                                PathsUtil.showSnack(getView(), "Downloading chroot by: " + chroot_author[0], false);
                                progressbar.setIndeterminate(false);
                                progressbar.show();
                                progressbar.setProgress(0);
                                progressbar.setMax(100);
                            }

                            @Override
                            public void onNewLine(String line) {}

                            @Override
                            public void onProgressUpdate(int progress) {
                                progressbar.setProgress(progress);
                            }

                            @Override
                            public void onFinished(int resultCode) {
                                setAllButtonEnable(true);
                                if (resultCode == 0) {
                                    new ActiveShellExecuter(activity, context) {

                                        @Override
                                        public void onPrepare() {
                                            setAllButtonEnable(false);
                                            progressbar.setIndeterminate(true);
                                        }

                                        @Override
                                        public void onNewLine(String line) {}

                                        @Override
                                        public void onFinished(int code) {
                                            disableToolbarMenu(false);
                                            setAllButtonEnable(true);
                                            compatCheck();
                                            progressbar.hide();
                                            progressbar.setIndeterminate(false);
                                            chroot.delete();
                                        }
                                    }.exec(
                                        PathsUtil.APP_SCRIPTS_PATH
                                                + "/chrootmgr -c \"restore "
                                                + chroot
                                                + " "
                                                + PathsUtil.CHROOT_PATH()
                                                + "\"",
                                        resultViewerLoggerTextView);
                                } else {
                                    progressbar.hide();
                                }
                            }
                        }.exec(chroot_url, chroot, resultViewerLoggerTextView); 
                    });
                });
                adb2Ad.show();
            });
            rb.setOnClickListener(view1 -> {
                ad.dismiss();
                MaterialAlertDialogBuilder adb3 = new MaterialAlertDialogBuilder(activity);
                View rootViewR = getLayoutInflater().inflate(R.layout.manager_dialog_restore, null);
                final TextInputEditText et = rootViewR.findViewById(R.id.input);
                et.setText(sharedPreferences.getString("chroot_restore_path", ""));
                adb3.setView(rootViewR);
                adb3.setPositiveButton("Restore", (dialogInterface, i) -> {});
                adb3.setNegativeButton("Cancel", (dialogInterface, i) -> {});
                final AlertDialog adb3Ad = adb3.create();
                adb3Ad.setOnShowListener(dialog -> {
                    final Button restore = adb3Ad.getButton(DialogInterface.BUTTON_POSITIVE);
                        restore.setOnClickListener(v -> {
                            sharedPreferences
                            .edit()
                            .putString("chroot_restore_path", et.getText().toString())
                            .apply();

                        new ActiveShellExecuter(activity, context) {

                            @Override
                            public void onPrepare() {
                                adb3Ad.dismiss();
                                disableToolbarMenu(true);
                                setAllButtonEnable(false);
                                progressbar.show();
                                progressbar.setIndeterminate(true);
                            }

                            @Override
                            public void onNewLine(String line) {}

                            @Override
                            public void onFinished(int code) {
                                disableToolbarMenu(false);
                                setAllButtonEnable(true);
                                compatCheck();
                                progressbar.hide();
                                progressbar.setIndeterminate(false);
                            }
                        }.exec(
                            PathsUtil.APP_SCRIPTS_PATH
                                    + "/chrootmgr -c \"restore "
                                    + et.getText().toString()
                                    + " "
                                    + PathsUtil.CHROOT_PATH()
                                    + "\"",
                            resultViewerLoggerTextView);
                    });
                });
                adb3Ad.show();
            });
            ad.show();
        });
    }

    private void setRemoveButton() {
        removeChrootButton.setOnClickListener(view -> {
            MaterialAlertDialogBuilder adb = new MaterialAlertDialogBuilder(activity);
            MaterialAlertDialogBuilder removing = new MaterialAlertDialogBuilder(activity);
            removing.setTitle("Removing...");
            removing.setView(R.layout.duck_cleaner);
            removing.setPositiveButton("Hide", (dI, ii) -> {});
            AlertDialog removingDialog = removing.create();
            adb.setTitle("Confirmation");
            adb.setMessage(
                "The chroot environment will be deleted, including the following data:"
                        + "\n"
                        + "\n• files stored inside the environment"
                        + "\n• installed packages"
                        + "\n• environment settings"
                        + "\n• other data");
            adb.setPositiveButton("I'm sure.", (dialogInterface, i) -> {
                new ActiveShellExecuter(activity, context) {
                    @Override
                    public void onPrepare() {
                        disableToolbarMenu(true);
                        setAllButtonEnable(false);
                        progressbar.show();
                        progressbar.setIndeterminate(true);
                        removingDialog.show();
                    }

                    @Override
                    public void onNewLine(String line) {}

                    @Override
                    public void onFinished(int code) {
                        disableToolbarMenu(false);
                        setAllButtonEnable(true);
                        compatCheck();
                        progressbar.hide();
                        progressbar.setIndeterminate(false);
                        removingDialog.dismiss();
                    }
                }.exec(
                    PathsUtil.APP_SCRIPTS_PATH
                            + "/chrootmgr -c \"remove "
                            + PathsUtil.CHROOT_PATH()
                            + "\"", resultViewerLoggerTextView);
            });
            adb.setNegativeButton("Cancel", (dialogInterface, i) -> {});
            adb.show();
        });
    }

    private void setBackupButton() {
        backupChrootButton.setOnClickListener(view -> {
            MaterialAlertDialogBuilder adb = new MaterialAlertDialogBuilder(activity);
            View v = getLayoutInflater().inflate(R.layout.manager_dialog_backup, null);
            TextInputEditText path = v.findViewById(R.id.input);
            path.setText(
                sharedPreferences.getString("chroot_backup_path", ""));
            adb.setView(v);
            adb.setPositiveButton("Do", (dialogInterface, i) -> {
                new ActiveShellExecuter(activity, context) {
                    @Override
                    public void onPrepare() {
                        sharedPreferences.edit().putString("chroot_backup_path", path.getText().toString()).apply();
                        disableToolbarMenu(true);
                        setAllButtonEnable(false);
                        progressbar.show();
                        progressbar.setIndeterminate(true);
                    }

                    @Override
                    public void onNewLine(String line) {}

                    @Override
                    public void onFinished(int code) {
                        disableToolbarMenu(false);
                        setAllButtonEnable(true);
                        progressbar.hide();
                        progressbar.setIndeterminate(true);
                    }
                }.exec(
                    PathsUtil.APP_SCRIPTS_PATH
                            + "/chrootmgr -c \"backup "
                            + PathsUtil.CHROOT_PATH() + " " + path.getText().toString() + "\"", resultViewerLoggerTextView);
            });
            adb.show();
        });
    }

    private void showBanner() {
        new ActiveShellExecuter(activity, context) {
            @Override
            public void onPrepare() {}

            @Override
            public void onNewLine(String line) {}

            @Override
            public void onFinished(int code) {}
        }.exec(PathsUtil.APP_SCRIPTS_PATH + "/mhbanner", resultViewerLoggerTextView);
    }

    private void compatCheck() {
        new ActiveShellExecuter(activity, context) {
            @Override
            public void onPrepare() {
                disableToolbarMenu(true);
            }

            @Override
            public void onNewLine(String line) {}

            @Override
            public void onFinished(int code) {
                disableToolbarMenu(false);
                setButtonVisibilty(code);
                setMountStatsTextView(code);
                setAllButtonEnable(true);
                context.startService(new Intent(context, CompatCheckService.class).putExtra("RESULTCODE", code));
            }
        }.exec(
            PathsUtil.APP_SCRIPTS_PATH
                + "/chrootmgr -c \"status\" -p "
                + PathsUtil.CHROOT_PATH(), resultViewerLoggerTextView);
    }

    private void setMountStatsTextView(int MODE) {
        if (MODE == IS_MOUNTED) {
            actionBar.setSubtitle("Chroot is now running.");
        } else if (MODE == IS_UNMOUNTED) {
            actionBar.setSubtitle("Chroot hasn't yet started.");
        } else if (MODE == NEED_TO_INSTALL) {
            actionBar.setSubtitle("Chroot isn't yet installed.");
        } else if (MODE == CHROOT_CORRUPTED) {
            actionBar.setSubtitle("Chroot corrupted!");
        }
    }

    private void setButtonVisibilty(int MODE) {
        switch (MODE) {
            case IS_MOUNTED:
                mountChrootButton.setVisibility(View.GONE);
                unmountChrootButton.setVisibility(View.VISIBLE);
                installChrootButton.setVisibility(View.GONE);
                removeChrootButton.setVisibility(View.GONE);
                backupChrootButton.setVisibility(View.GONE);
                break;
            case IS_UNMOUNTED:
                mountChrootButton.setVisibility(View.VISIBLE);
                unmountChrootButton.setVisibility(View.GONE);
                installChrootButton.setVisibility(View.GONE);
                removeChrootButton.setVisibility(View.VISIBLE);
                backupChrootButton.setVisibility(View.VISIBLE);
                break;
            case NEED_TO_INSTALL:
                mountChrootButton.setVisibility(View.GONE);
                unmountChrootButton.setVisibility(View.GONE);
                installChrootButton.setVisibility(View.VISIBLE);
                removeChrootButton.setVisibility(View.GONE);
                backupChrootButton.setVisibility(View.GONE);
                break;
            case CHROOT_CORRUPTED:
                mountChrootButton.setVisibility(View.GONE);
                unmountChrootButton.setVisibility(View.GONE);
                installChrootButton.setVisibility(View.GONE);
                removeChrootButton.setVisibility(View.VISIBLE);
                backupChrootButton.setVisibility(View.GONE);
        }
    }

    private void setAllButtonEnable(boolean isEnable) {
        mountChrootButton.setEnabled(isEnable);
        unmountChrootButton.setEnabled(isEnable);
        installChrootButton.setEnabled(isEnable);
        removeChrootButton.setEnabled(isEnable);
        backupChrootButton.setEnabled(isEnable);
    }

    private void disableToolbarMenu(boolean working) {
        setHasOptionsMenu(!working);
    }
}