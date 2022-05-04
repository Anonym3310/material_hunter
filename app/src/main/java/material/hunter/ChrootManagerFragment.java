package material.hunter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import material.hunter.AppNavHomeActivity;
import material.hunter.AsyncTask.ChrootManagerAsynctask;
import material.hunter.service.CompatCheckService;
import material.hunter.service.NotificationChannelService;
import material.hunter.utils.PathsUtil;
import material.hunter.utils.ShellExecuter;

public class ChrootManagerFragment extends Fragment {

  private static final String ARG_SECTION_NUMBER = "section_number";
  private static final int IS_MOUNTED = 0;
  private static final int IS_UNMOUNTED = 1;
  private static final int NEED_TO_INSTALL = 2;
  private static final int CHROOT_CORRUPTED = 3;
  public static boolean isAsyncTaskRunning = false;
  private Intent backPressedintent = new Intent();
  private ShellExecuter exe = new ShellExecuter();
  private TextView resultViewerLoggerTextView;
  private Button mountChrootButton;
  private Button unmountChrootButton;
  private Button optionsChrootButton;
  private Button installChrootButton;
  private Button removeChrootButton;
  private Button backupChrootButton;
  private SharedPreferences sharedPreferences;
  private ChrootManagerAsynctask chrootManagerAsynctask;
  private Context context;
  private Activity activity;
  private LinearProgressIndicator progressbar;

  public static ChrootManagerFragment newInstance(int sectionNumber) {
    ChrootManagerFragment fragment = new ChrootManagerFragment();
    Bundle args = new Bundle();
    args.putInt(ARG_SECTION_NUMBER, sectionNumber);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
    context = getContext();
    activity = getActivity();
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.chroot_manager, container, false);
    sharedPreferences = activity.getSharedPreferences("material.hunter", Context.MODE_PRIVATE);
    resultViewerLoggerTextView = rootView.findViewById(R.id.f_chrootmanager_viewlogger);
    mountChrootButton = rootView.findViewById(R.id.f_chrootmanager_mount_btn);
    unmountChrootButton = rootView.findViewById(R.id.f_chrootmanager_unmount_btn);
    optionsChrootButton = rootView.findViewById(R.id.f_chrootmanager_options_btn);
    installChrootButton = rootView.findViewById(R.id.f_chrootmanager_install_btn);
    removeChrootButton = rootView.findViewById(R.id.f_chrootmanager_removechroot_btn);
    backupChrootButton = rootView.findViewById(R.id.f_chrootmanager_backupchroot_btn);
    progressbar = rootView.findViewById(R.id.progressbar);
    return rootView;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    resultViewerLoggerTextView.setMovementMethod(new ScrollingMovementMethod());
    setStopKaliButton();
    setOptionsButton();
    setStartKaliButton();
    setInstallChrootButton();
    setRemoveChrootButton();
    setBackupChrootButton();

    File temp = new File(PathsUtil.APP_SD_PATH + "/Temporary");
    if (!temp.exists()) {
      PathsUtil.showSnack(getView(), "Creating temporary directory...", false);
      try {
        temp.mkdir();
      } catch (Exception e) {
        e.printStackTrace();
        PathsUtil.showSnack(
            getView(),
            "Failed to create directory " + temp.getPath().toString(),
            false);
        return;
      }
    }
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.chroot_manager, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.edit:
        MaterialAlertDialogBuilder adb = new MaterialAlertDialogBuilder(activity);
        final View rootView = getLayoutInflater().inflate(R.layout.chroot_manager_edit, null);
        final AutoCompleteTextView path =
          rootView.findViewById(R.id.f_chroot_manager_chroot_select_path);
        String path_now =
          sharedPreferences.getString("", "chroot");
        path.setText(path_now);
        final ArrayList<String> chroots = new ArrayList<>();
        for (String file : exe.RunAsRootOutput("for i in $(ls " + PathsUtil.SYSTEM_PATH + "); do test -d " + PathsUtil.SYSTEM_PATH + "/$i && echo $i; done").split("\n")) {
          if (!file.equals("kalifs") && !file.equals("mhbinder") && !file.equals(""))
            chroots.add(file);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, R.layout.mhspinner, chroots);
        path.setAdapter(adapter);
        adb.setCancelable(true);
        adb.setTitle("Edit");
        adb.setView(rootView);
        adb.setPositiveButton(
            "Apply",
            (dialogInterface, i) -> {
              if (path.getText().toString().matches("^[a-zA-Z.\\/_~]+$")) {
                PathsUtil.ARCH_FOLDER = path.getText().toString();
                sharedPreferences
                    .edit()
                    .putString("chroot_directory", PathsUtil.ARCH_FOLDER)
                    .apply();
                sharedPreferences
                    .edit()
                    .putString("chroot_directory", PathsUtil.CHROOT_PATH())
                    .apply();
                new ShellExecuter()
                    .RunAsRootOutput(
                        "ln -sfn " + PathsUtil.CHROOT_PATH() + " " + PathsUtil.CHROOT_SYMLINK_PATH);
                compatCheck();
              } else
                PathsUtil.showSnack(getView(), "Invalid name.", false);
              dialogInterface.dismiss();
            });
        adb.setNegativeButton("Cancel", (dialogInterface2, i2) -> { });
        adb.show();
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onStart() {
    super.onStart();
    if (!isAsyncTaskRunning) {
      showBanner();
      compatCheck();
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    resultViewerLoggerTextView = null;
    mountChrootButton = null;
    unmountChrootButton = null;
    optionsChrootButton = null;
    installChrootButton = null;
    removeChrootButton = null;
    backupChrootButton = null;
    chrootManagerAsynctask = null;
    progressbar = null;
  }

  private void setStartKaliButton() {
    mountChrootButton.setOnClickListener(
        view -> {
          chrootManagerAsynctask = new ChrootManagerAsynctask(ChrootManagerAsynctask.MOUNT_CHROOT);
          chrootManagerAsynctask.setListener(
              new ChrootManagerAsynctask.ChrootManagerAsyncTaskListener() {
                @Override
                public void onAsyncTaskPrepare() {
                  setAllButtonEnable(false);
                }

                @Override
                public void onAsyncTaskProgressUpdate(int progress) {}

                @Override
                public void onAsyncTaskFinished(int resultCode, ArrayList<String> resultString) {
                  if (resultCode == 0) {
                    setButtonVisibilty(IS_MOUNTED);
                    setMountStatsTextView(IS_MOUNTED);
                    setAllButtonEnable(true);
                    compatCheck();
                    context.startService(
                        new Intent(context, NotificationChannelService.class)
                            .setAction(NotificationChannelService.FINE));
                  }
                }
              });
          chrootManagerAsynctask.execute(resultViewerLoggerTextView);
        });
  }

  private void setStopKaliButton() {
    unmountChrootButton.setOnClickListener(
        view -> {
          chrootManagerAsynctask =
              new ChrootManagerAsynctask(ChrootManagerAsynctask.UNMOUNT_CHROOT);
          chrootManagerAsynctask.setListener(
              new ChrootManagerAsynctask.ChrootManagerAsyncTaskListener() {
                @Override
                public void onAsyncTaskPrepare() {
                  setAllButtonEnable(false);
                }

                @Override
                public void onAsyncTaskProgressUpdate(int progress) {}

                @Override
                public void onAsyncTaskFinished(int resultCode, ArrayList<String> resultString) {
                  if (resultCode == 0) {
                    setMountStatsTextView(IS_UNMOUNTED);
                    setButtonVisibilty(IS_UNMOUNTED);
                    setAllButtonEnable(true);
                    compatCheck();
                  }
                }
              });
          chrootManagerAsynctask.execute(resultViewerLoggerTextView);
        });
  }

  private void setOptionsButton() {
    optionsChrootButton.setOnClickListener(
        view -> {
          MaterialAlertDialogBuilder adb = new MaterialAlertDialogBuilder(context);
          final View rootView = getLayoutInflater().inflate(R.layout.chroot_manager_options, null);
          final TextInputEditText editText = rootView.findViewById(R.id.hostname);
          editText.setText(sharedPreferences.getString("hostname", "android"));
          adb.setView(rootView);
          adb.setPositiveButton(
              "Setup",
              (dialogInterface, i) -> {
                final String hostname = editText.getText().toString();
                if (!hostname.matches("([a-zA-Z0-9-]){2,253}")) {
                  PathsUtil.showSnack(getView(), "Invalid hostname", false);
                  return;
                }
                sharedPreferences.edit().putString("hostname", hostname).apply();
                PathsUtil.showSnack(getView(), "Need remounting chroot!", false);
              });
          adb.setNegativeButton("Cancel", (dialogInterface, i) -> {});
          adb.show();
        });
  }

  private void setInstallChrootButton() {
    installChrootButton.setOnClickListener(
        view -> {
          MaterialAlertDialogBuilder adb = new MaterialAlertDialogBuilder(context);
          final View rootView =
              getLayoutInflater().inflate(R.layout.chroot_manager_download_dialog, null);
          Button db = rootView.findViewById(R.id.downloadButton);
          Button rb = rootView.findViewById(R.id.restoreButton);
          adb.setTitle("Install");
          adb.setView(rootView);
          AlertDialog ad = adb.create();
          db.setOnClickListener(
              view1 -> {
                ad.dismiss();
                MaterialAlertDialogBuilder adb1 = new MaterialAlertDialogBuilder(activity);
                final View promtDownloadView =
                    getLayoutInflater().inflate(R.layout.chroot_manager_prepare_dialog, null);
                final TextInputEditText storepathEditText =
                    promtDownloadView.findViewById(R.id.link);
                storepathEditText.setText(
                    sharedPreferences.getString(
                        "chroot_download_url_prev", ""));
                adb1.setTitle("Download");
                adb1.setView(promtDownloadView);
                adb1.setPositiveButton(
                    "Setup",
                    (dialogInterface, i) -> {
                      String chroot_url = storepathEditText.getText().toString();
                      if (!chroot_url.equals("")) {
                        if (!chroot_url.startsWith("http://")
                            && !chroot_url.startsWith("https://")) {
                          chroot_url = "http://" + chroot_url;
                        }
                        if (!chroot_url.endsWith(".xz") && !chroot_url.endsWith(".gz")) {
                          PathsUtil.showSnack(
                              getView(), "Tarball must be xz or gz compression.", true);
                          return;
                        }
                      } else {
                        PathsUtil.showSnack(getView(), "URL is incorrect!", false);
                        return;
                      }
                      sharedPreferences
                          .edit()
                          .putString(
                              "chroot_download_url_prev", chroot_url)
                          .apply();
                      String filename = chroot_url.substring(chroot_url.lastIndexOf('/') + 1, chroot_url.length());
                      context.startService(
                          new Intent(context, NotificationChannelService.class)
                              .setAction(NotificationChannelService.DOWNLOADING));
                      File chroot = new File(PathsUtil.SD_PATH + "/Download/" + filename);
                      chrootManagerAsynctask =
                          new ChrootManagerAsynctask(ChrootManagerAsynctask.DOWNLOAD_CHROOT);
                      chrootManagerAsynctask.setListener(
                          new ChrootManagerAsynctask.ChrootManagerAsyncTaskListener() {
                            @Override
                            public void onAsyncTaskPrepare() {
                              broadcastWorking(true);
                              setAllButtonEnable(false);
                              progressbar.show();
                              progressbar.setProgress(0);
                              progressbar.setMax(100);
                            }

                            @Override
                            public void onAsyncTaskProgressUpdate(int progress) {
                              progressbar.setProgress(progress);
                            }

                            @Override
                            public void onAsyncTaskFinished(
                                int resultCode, ArrayList<String> resultString) {
                              broadcastWorking(false);
                              setAllButtonEnable(true);
                              if (resultCode == 0) {
                                chrootManagerAsynctask =
                                    new ChrootManagerAsynctask(ChrootManagerAsynctask.INSTALL_CHROOT);
                                chrootManagerAsynctask.setListener(
                                    new ChrootManagerAsynctask.ChrootManagerAsyncTaskListener() {
                                      @Override
                                      public void onAsyncTaskPrepare() {
                                        context.startService(
                                            new Intent(context, NotificationChannelService.class)
                                                .setAction(NotificationChannelService.INSTALLING));
                                        broadcastWorking(true);
                                        setAllButtonEnable(false);
                                        progressbar.setIndeterminate(true);
                                      }

                                      @Override
                                      public void onAsyncTaskProgressUpdate(int progress) {}

                                      @Override
                                      public void onAsyncTaskFinished(
                                          int resultCode, ArrayList<String> resultString) {
                                        broadcastWorking(false);
                                        setAllButtonEnable(true);
                                        compatCheck();
                                        progressbar.hide();
                                        chroot.delete();
                                      }
                                    });
                                chrootManagerAsynctask.execute(
                                    resultViewerLoggerTextView, chroot, PathsUtil.CHROOT_PATH());
                              } else {
                                progressbar.hide();
                              }
                            }
                          });
                      chrootManagerAsynctask.execute(
                          resultViewerLoggerTextView, chroot_url, chroot);
                    });
                adb1.create().show();
              });
          rb.setOnClickListener(
              view12 -> {
                ad.dismiss();
                MaterialAlertDialogBuilder adb2 = new MaterialAlertDialogBuilder(activity);
                final View rootViewR = getLayoutInflater().inflate(R.layout.chroot_restore, null);
                final TextInputEditText et = rootViewR.findViewById(R.id.chrootRestorePath);
                et.setText(
                    sharedPreferences.getString(
                        "chroot_restore_path", ""));
                adb2.setTitle("Restore");
                adb2.setView(rootViewR);
                adb2.setPositiveButton(
                    "OK",
                    (dialogInterface, i) -> {
                      sharedPreferences
                          .edit()
                          .putString(
                              "chroot_restore_path",
                              et.getText().toString())
                          .apply();
                      chrootManagerAsynctask =
                          new ChrootManagerAsynctask(ChrootManagerAsynctask.INSTALL_CHROOT);
                      chrootManagerAsynctask.setListener(
                          new ChrootManagerAsynctask.ChrootManagerAsyncTaskListener() {
                            @Override
                            public void onAsyncTaskPrepare() {
                              context.startService(
                                  new Intent(context, NotificationChannelService.class)
                                      .setAction(NotificationChannelService.INSTALLING));
                              broadcastWorking(true);
                              setAllButtonEnable(false);
                              progressbar.show();
                              progressbar.setIndeterminate(true);
                            }

                            @Override
                            public void onAsyncTaskProgressUpdate(int progress) {}

                            @Override
                            public void onAsyncTaskFinished(
                                int resultCode, ArrayList<String> resultString) {
                              broadcastWorking(false);
                              setAllButtonEnable(true);
                              compatCheck();
                              progressbar.hide();
                            }
                          });
                      chrootManagerAsynctask.execute(
                          resultViewerLoggerTextView,
                          et.getText().toString(),
                          PathsUtil.CHROOT_PATH());
                    });
                adb2.show();
              });
          ad.show();
        });
  }

  private void setRemoveChrootButton() {
    removeChrootButton.setOnClickListener(
        view -> {
          MaterialAlertDialogBuilder adb =
              new MaterialAlertDialogBuilder(activity)
                  .setTitle("Warning!")
                  .setView(R.layout.noroot)
                  .setMessage(
                      "Are you sure to remove the below Chroot folder?\n" + PathsUtil.CHROOT_PATH())
                  .setPositiveButton(
                      "I'm sure.",
                      (dialogInterface, i) -> {
                        chrootManagerAsynctask =
                            new ChrootManagerAsynctask(ChrootManagerAsynctask.REMOVE_CHROOT);
                        chrootManagerAsynctask.setListener(
                            new ChrootManagerAsynctask.ChrootManagerAsyncTaskListener() {
                              @Override
                              public void onAsyncTaskPrepare() {
                                broadcastWorking(true);
                                setAllButtonEnable(false);
                                progressbar.show();
                                progressbar.setIndeterminate(true);
                              }

                              @Override
                              public void onAsyncTaskProgressUpdate(int progress) {}

                              @Override
                              public void onAsyncTaskFinished(
                                  int resultCode, ArrayList<String> resultString) {
                                broadcastWorking(false);
                                setAllButtonEnable(true);
                                progressbar.hide();
                                compatCheck();
                              }
                            });
                        chrootManagerAsynctask.execute(resultViewerLoggerTextView);
                      })
                  .setNegativeButton("Cancel", (dialogInterface, i) -> {});
          adb.show();
        });
  }

  private void setBackupChrootButton() {
    backupChrootButton.setOnClickListener(
        view -> {
          MaterialAlertDialogBuilder adb = new MaterialAlertDialogBuilder(activity);
          EditText backupFullPathEditText = new EditText(activity);
          LinearLayout ll = new LinearLayout(activity);
          LinearLayout.LayoutParams layoutParams =
              new LinearLayout.LayoutParams(
                  LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
          ll.setOrientation(LinearLayout.VERTICAL);
          ll.setLayoutParams(layoutParams);
          LinearLayout.LayoutParams editTextParams =
              new LinearLayout.LayoutParams(
                  LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
          editTextParams.setMargins(58, 40, 58, 0);
          backupFullPathEditText.setLayoutParams(editTextParams);
          ll.addView(backupFullPathEditText);
          adb.setTitle("Backup Chroot");
          adb.setMessage(
              "Create a backup of the your chroot environment.\n\nPath: \""
                  + PathsUtil.CHROOT_PATH()
                  + "\" to:");
          backupFullPathEditText.setText(
              sharedPreferences.getString("chroot_backup_path", ""));
          adb.setView(ll);
          adb.setPositiveButton(
              "OK",
              (dialogInterface, i) -> {

// TODO: fix this shit
                sharedPreferences
                    .edit()
                    .putString(
                        "chroot_backup_path",
                        backupFullPathEditText.getText().toString())
                    .apply();
                if (new File(backupFullPathEditText.getText().toString()).exists()) {
                  dialogInterface.dismiss();
                  MaterialAlertDialogBuilder ad2 = new MaterialAlertDialogBuilder(activity);
                  ad2.setMessage("File exists already, do you want ot overwrite it" + " anyway?");
                  ad2.setPositiveButton(
                      "Yes",
                      (dialogInterface1, i1) -> {
                        chrootManagerAsynctask =
                            new ChrootManagerAsynctask(ChrootManagerAsynctask.BACKUP_CHROOT);
                        chrootManagerAsynctask.setListener(
                            new ChrootManagerAsynctask.ChrootManagerAsyncTaskListener() {
                              @Override
                              public void onAsyncTaskPrepare() {
                                context.startService(
                                    new Intent(context, NotificationChannelService.class)
                                        .setAction(NotificationChannelService.BACKINGUP));
                                broadcastWorking(true);
                                setAllButtonEnable(false);
                                progressbar.show();
                                progressbar.setIndeterminate(true);
                              }

                              @Override
                              public void onAsyncTaskProgressUpdate(int progress) {}

                              @Override
                              public void onAsyncTaskFinished(
                                  int resultCode, ArrayList<String> resultString) {
                                broadcastWorking(false);
                                setAllButtonEnable(true);
                                progressbar.hide();
                              }
                            });
                        chrootManagerAsynctask.execute(
                            resultViewerLoggerTextView,
                            PathsUtil.CHROOT_PATH(),
                            backupFullPathEditText.getText().toString());
                      });
                  ad2.setNegativeButton("Cancel", (dialogInterface2, i2) -> {});
                  ad2.show();
                } else {
                  chrootManagerAsynctask =
                      new ChrootManagerAsynctask(ChrootManagerAsynctask.BACKUP_CHROOT);
                  chrootManagerAsynctask.setListener(
                      new ChrootManagerAsynctask.ChrootManagerAsyncTaskListener() {
                        @Override
                        public void onAsyncTaskPrepare() {
                          context.startService(
                              new Intent(context, NotificationChannelService.class)
                                  .setAction(NotificationChannelService.BACKINGUP));
                          broadcastWorking(true);
                          setAllButtonEnable(false);
                          progressbar.show();
                          progressbar.setIndeterminate(true);
                        }

                        @Override
                        public void onAsyncTaskProgressUpdate(int progress) {}

                        @Override
                        public void onAsyncTaskFinished(
                            int resultCode, ArrayList<String> resultString) {
                          broadcastWorking(false);
                          setAllButtonEnable(true);
                          progressbar.hide();
                        }
                      });
                  chrootManagerAsynctask.execute(
                      resultViewerLoggerTextView,
                      PathsUtil.CHROOT_PATH(),
                      backupFullPathEditText.getText().toString());
                }
              });
          adb.show();
        });
  }

  private void showBanner() {
    chrootManagerAsynctask = new ChrootManagerAsynctask(ChrootManagerAsynctask.ISSUE_BANNER);
    chrootManagerAsynctask.execute(resultViewerLoggerTextView, "MaterialHunter 3");
  }

  private void compatCheck() {
    chrootManagerAsynctask = new ChrootManagerAsynctask(ChrootManagerAsynctask.CHECK_CHROOT);
    chrootManagerAsynctask.setListener(
        new ChrootManagerAsynctask.ChrootManagerAsyncTaskListener() {
          @Override
          public void onAsyncTaskPrepare() {
            broadcastWorking(true);
          }

          @Override
          public void onAsyncTaskProgressUpdate(int progress) {}

          @Override
          public void onAsyncTaskFinished(int resultCode, ArrayList<String> resultString) {
            broadcastWorking(false);
            setButtonVisibilty(resultCode);
            setMountStatsTextView(resultCode);
            setAllButtonEnable(true);
            context.startService(
                new Intent(context, CompatCheckService.class).putExtra("RESULTCODE", resultCode));
          }
        });
    chrootManagerAsynctask.execute(
        resultViewerLoggerTextView,
        sharedPreferences.getString("chroot_directory_path", ""));
  }

  private void setMountStatsTextView(int MODE) {
    if (MODE == IS_MOUNTED) {
      AppNavHomeActivity.actionBar.setSubtitle("Chroot is now running.");
    } else if (MODE == IS_UNMOUNTED) {
      AppNavHomeActivity.actionBar.setSubtitle("Chroot hasn't yet started.");
    } else if (MODE == NEED_TO_INSTALL) {
      AppNavHomeActivity.actionBar.setSubtitle("Chroot isn't yet installed.");
    } else if (MODE == CHROOT_CORRUPTED) {
      AppNavHomeActivity.actionBar.setSubtitle("Chroot corrupted!");
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

  private void broadcastWorking(boolean working) {
    context.sendBroadcast(
        new Intent().putExtra("working", working).setAction("material.hunter.WORKING"));
    setHasOptionsMenu(!working);
  }
}