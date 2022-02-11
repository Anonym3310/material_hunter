package material.hunter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import material.hunter.HandlerThread.USBArmoryHandlerThread;
import material.hunter.SQL.USBArmorySQL;
import material.hunter.models.USBArmoryUSBSwitchModel;
import material.hunter.utils.NhPaths;

public class USBArmoryFragment extends Fragment {

  private static final String ARG_SECTION_NUMBER = "section_number";
  private static final String TAG = "USBArmoryFragment";
  private static boolean is_init_exists = true;
  private final USBArmoryHandlerThread usbArmoryHandlerThread = new USBArmoryHandlerThread();
  private final Handler uiHandler = new Handler(Looper.getMainLooper());
  private Context context;
  private Activity activity;
  private TextView usbStatusTextView;
  private TextView mountedImageTextView;
  private TextView mountedImageHintTextView;
  private Button reloadUSBStateImageButton;
  private Button reloadMountStateButton;
  private LinearLayout imageMounterLL;
  private Button setUSBIfaceButton;
  private Button mountImgButton;
  private Button unmountImgButton;
  private Button saveUSBFunctionConfigButton;
  private CheckBox readOnlyCheckBox;
  private AutoCompleteTextView targetOSSpinner;
  private AutoCompleteTextView usbFuncSpinner;
  private AutoCompleteTextView adbSpinner;
  private AutoCompleteTextView imgFileSpinner;
  private EditText[] usbSwitchInfoEditTextGroup = new TextInputEditText[5];

  public static USBArmoryFragment newInstance(int sectionNumber) {
    USBArmoryFragment fragment = new USBArmoryFragment();
    Bundle args = new Bundle();
    args.putInt(ARG_SECTION_NUMBER, sectionNumber);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    context = getContext();
    activity = getActivity();
    usbArmoryHandlerThread.start();
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.usbarmory, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    targetOSSpinner = view.findViewById(R.id.f_usbarmory_spr_targetplatform);
    usbFuncSpinner = view.findViewById(R.id.f_usbarmory_spr_usbfunctions);
    adbSpinner = view.findViewById(R.id.f_usbarmory_spr_adb);
    imgFileSpinner = view.findViewById(R.id.f_usbarmory_spr_img_files);
    setUSBIfaceButton = view.findViewById(R.id.f_usbarmory_btn_setusbinterface);
    mountImgButton = view.findViewById(R.id.f_usbarmory_btn_mountImage);
    unmountImgButton = view.findViewById(R.id.f_usbarmory_btn_unmountImage);
    reloadUSBStateImageButton = view.findViewById(R.id.f_usbarmory_imgbtn_reloadUSBStatus);
    reloadMountStateButton = view.findViewById(R.id.f_usbarmory_imgbtn_reloadMountStatus);
    saveUSBFunctionConfigButton = view.findViewById(R.id.f_usbarmory_btn_saveusbfuncswitch);
    readOnlyCheckBox = view.findViewById(R.id.f_usbarmory_chkbox_ReadOrWrite);
    usbStatusTextView = view.findViewById(R.id.f_usbarmory_tv_current_usb_state);
    mountedImageTextView = view.findViewById(R.id.f_usbarmory_tv_mount_state);
    mountedImageHintTextView = view.findViewById(R.id.f_usbarmory_ll_tv_imagemounter_hint);
    imageMounterLL = view.findViewById(R.id.f_usbarmory_ll_imageMounter_sub2);

    usbSwitchInfoEditTextGroup[0] = view.findViewById(R.id.f_usbarmory_et_idvendor);
    usbSwitchInfoEditTextGroup[1] = view.findViewById(R.id.f_usbarmory_et_idproduct);
    usbSwitchInfoEditTextGroup[2] = view.findViewById(R.id.f_usbarmory_et_manufacturer);
    usbSwitchInfoEditTextGroup[3] = view.findViewById(R.id.f_usbarmory_et_product);
    usbSwitchInfoEditTextGroup[4] = view.findViewById(R.id.f_usbarmory_et_serialnumber);

    {
      Message msg = new Message();
      msg.what = USBArmoryHandlerThread.IS_INIT_EXIST;
      msg.obj = "[ -f /init.nethunter.rc ]";
      usbArmoryHandlerThread.getHandler().sendMessage(msg);
    }

    File sql_folder = new File(NhPaths.APP_SD_SQLBACKUP_PATH);
    if (!sql_folder.exists()) {
      NhPaths.showSnack(getView(), "Creating directory for backing up dbs...", false);
      try {
        sql_folder.mkdir();
      } catch (Exception e) {
        e.printStackTrace();
        NhPaths.showSnack(
            getView(),
            "Failed to create directory " + NhPaths.APP_SD_SQLBACKUP_PATH,
            false);
        return;
      }
    }

    ArrayAdapter<String> usb_target =
        new ArrayAdapter<String>(
            activity,
            R.layout.mhspinner,
            new String[] {"Windows", "Linux", "Mac OS"});
    ArrayAdapter<String> usb_functions =
        new ArrayAdapter<String>(
            activity,
            R.layout.mhspinner,
            getResources().getStringArray(R.array.usbarmory_usb_states_win_lin));
    ArrayAdapter<String> usb_functions_mac =
        new ArrayAdapter<String>(
            activity,
            R.layout.mhspinner,
            getResources().getStringArray(R.array.usbarmory_usb_states_mac));
    ArrayAdapter<String> adb_enable =
        new ArrayAdapter<String>(
            activity, R.layout.mhspinner, new String[] {"Enable", "Disable"});

    targetOSSpinner.setAdapter(usb_target);
    usbFuncSpinner.setAdapter(usb_functions);
    adbSpinner.setAdapter(adb_enable);

    ArrayAdapter<String> usbFuncWinArrayAdapter =
        new ArrayAdapter<>(activity, R.layout.mhspinner, new ArrayList<>());
    ArrayAdapter<String> usbFuncMACArrayAdapter =
        new ArrayAdapter<>(activity, R.layout.mhspinner, new ArrayList<>());

    targetOSSpinner.setOnItemClickListener(
        new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> adapterView, View v, int i, long l) {
            if (i == 2) {
              usbFuncSpinner.setAdapter(usbFuncMACArrayAdapter);
            } else {
              usbFuncSpinner.setAdapter(usbFuncWinArrayAdapter);
            }
            refreshUSBSwitchInfos(gettargetOSSpinnerString(), getusbFuncSpinnerString());
          }
        });

    usbFuncSpinner.setOnItemClickListener(
        new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0 || is_init_exists) {
              adbSpinner.setText("Disable");
              adbSpinner.setEnabled(false);
            } else {
              adbSpinner.setAdapter(adb_enable);
              adbSpinner.setEnabled(true);
            }
            refreshUSBSwitchInfos(gettargetOSSpinnerString(), getusbFuncSpinnerString());
          }
        });

    adbSpinner.setOnItemClickListener(
        new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            refreshUSBSwitchInfos(gettargetOSSpinnerString(), getusbFuncSpinnerString());
          }
        });

    setUSBIfaceButton.setOnClickListener(
        v -> {
          if (isAllUSBInfosValid()) {
            setUSBIfaceButton.setEnabled(false);
            String target =
                targetOSSpinner.getText().toString().equals("Windows")
                    ? "win"
                    : targetOSSpinner.getText().toString().equals("Linux")
                        ? "lnx"
                        : targetOSSpinner.getText().toString().equals("Mac OS") ? "mac" : "";
            String functions = usbFuncSpinner.getText().toString();
            String adbEnable = adbSpinner.getText().toString().equals("Enable") ? ",adb" : "";
            String idVendor = " -v '" + usbSwitchInfoEditTextGroup[0].getText().toString() + "'";
            String idProduct = " -p '" + usbSwitchInfoEditTextGroup[1].getText().toString() + "'";
            String manufacturer =
                usbSwitchInfoEditTextGroup[2].getText().toString().isEmpty()
                    ? ""
                    : " -m '" + usbSwitchInfoEditTextGroup[2].getText().toString() + "'";
            String product =
                usbSwitchInfoEditTextGroup[3].getText().toString().isEmpty()
                    ? ""
                    : " -P '" + usbSwitchInfoEditTextGroup[3].getText().toString() + "'";
            String serialnumber =
                usbSwitchInfoEditTextGroup[4].getText().toString().isEmpty()
                    ? ""
                    : " -s '" + usbSwitchInfoEditTextGroup[4].getText().toString() + "'";

            Message msg = new Message();
            msg.what = USBArmoryHandlerThread.SETUSBIFACE;
            msg.obj =
                "[ -f /init.nethunter.rc ] && setprop sys.usb.config "
                    + functions
                    + " || "
                    + NhPaths.APP_SCRIPTS_PATH
                    + "/usbarmory -t '"
                    + target
                    + "' -f '"
                    + functions
                    + adbEnable
                    + "'"
                    + idVendor
                    + idProduct
                    + manufacturer
                    + product
                    + serialnumber;
            usbArmoryHandlerThread.getHandler().sendMessage(msg);
          }
        });

    reloadUSBStateImageButton.setOnClickListener(
        v -> {
          Message msg = new Message();
          msg.what = USBArmoryHandlerThread.RELOAD_USBIFACE;
          msg.obj =
              "find /config/usb_gadget/g1/configs/b.1 -type l -exec readlink -e {} \\; | xargs"
                  + " echo";
          usbArmoryHandlerThread.getHandler().sendMessage(msg);
        });

    reloadMountStateButton.setOnClickListener(
        v -> {
          Message msg = new Message();
          msg.what = USBArmoryHandlerThread.RELOAD_MOUNTSTATUS;
          msg.obj = "cat /config/usb_gadget/g1/functions/mass_storage.0/lun.0/file";
          usbArmoryHandlerThread.getHandler().sendMessage(msg);
          getImageFiles();
        });

    mountImgButton.setOnClickListener(
        v -> {
          if (imgFileSpinner.getText().toString().isEmpty()) {
            NhPaths.showSnack(getView(), "No image file is selected.", false);
          } else {
            mountImgButton.setEnabled(false);
            unmountImgButton.setEnabled(false);
            Message msg = new Message();
            msg.what = USBArmoryHandlerThread.MOUNT_IMAGE;
            if (readOnlyCheckBox.isChecked())
              msg.obj =
                  String.format(
                      "%s%s && echo '%s/%s' >"
                          + " /config/usb_gadget/g1/functions/mass_storage.0/lun.0/file",
                      "echo '1' > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/ro",
                      imgFileSpinner.getText().toString().contains(".iso")
                          ? " && echo '1' >"
                                + " /config/usb_gadget/g1/functions/mass_storage.0/lun.0/cdrom"
                          : " && echo '0' >"
                                + " /config/usb_gadget/g1/functions/mass_storage.0/lun.0/cdrom",
                      NhPaths.APP_SD_FILES_IMG_PATH,
                      imgFileSpinner.getText().toString());
            else
              msg.obj =
                  String.format(
                      "%s%s && echo '%s/%s' >"
                          + " /config/usb_gadget/g1/functions/mass_storage.0/lun.0/file",
                      "echo '0' > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/ro",
                      imgFileSpinner.getText().toString().contains(".iso")
                          ? " && echo '1' >"
                                + " /config/usb_gadget/g1/functions/mass_storage.0/lun.0/cdrom"
                          : " && echo '0' >"
                                + " /config/usb_gadget/g1/functions/mass_storage.0/lun.0/cdrom",
                      NhPaths.APP_SD_FILES_IMG_PATH,
                      imgFileSpinner.getText().toString());
            usbArmoryHandlerThread.getHandler().sendMessage(msg);
          }
        });

    unmountImgButton.setOnClickListener(
        v -> {
          mountImgButton.setEnabled(false);
          unmountImgButton.setEnabled(false);
          Message msg = new Message();
          msg.what = USBArmoryHandlerThread.UNMOUNT_IMAGE;
          msg.obj =
              "echo '' > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/file"
                  + " && echo '0' > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/ro"
                  + " && echo '0' > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/cdrom";
          usbArmoryHandlerThread.getHandler().sendMessage(msg);
        });

    saveUSBFunctionConfigButton.setOnClickListener(
        v -> {
          if (!usbSwitchInfoEditTextGroup[0].getText().toString().matches("0x[0-9a-fA-F]{4}")
              || !usbSwitchInfoEditTextGroup[1].getText().toString().matches("0x[0-9a-fA-F]{4}")) {
            new MaterialAlertDialogBuilder(context)
                .setTitle("Invalid Format")
                .setMessage("The regex must be 0x[0-9a-fA-F]{4}")
                .create()
                .show();
          } else if (!usbSwitchInfoEditTextGroup[2].getText().toString().matches("\\w+|^$")
              || !usbSwitchInfoEditTextGroup[3].getText().toString().matches("\\w+|^$")) {
            new MaterialAlertDialogBuilder(context)
                .setTitle("Invalid Format")
                .setMessage("The regex must be \\w*|^$")
                .create()
                .show();
          } else if (!usbSwitchInfoEditTextGroup[4]
              .getText()
              .toString()
              .matches("[0-9A-Z]{10}|^$")) {
            new MaterialAlertDialogBuilder(context)
                .setTitle("Invalid Format")
                .setMessage("The regex must be [0-9A-Z]{10}|^$")
                .create()
                .show();
          } else {
            for (int i = 0; i < usbSwitchInfoEditTextGroup.length; i++) {
              if (!USBArmorySQL.getInstance(context)
                  .setUSBSwitchColumnData(
                      getusbFuncSpinnerString(),
                      i + 2,
                      targetOSSpinner.getText().toString(),
                      usbSwitchInfoEditTextGroup[i].getText().toString().toLowerCase())) {
                NhPaths.showSnack(
                    getView(),
                    "Something's wrong when processing "
                        + usbSwitchInfoEditTextGroup[i].getText().toString().toLowerCase(),
                    false);
              }
              NhPaths.showSnack(getView(), "Saved", false);
            }
          }
        });

    usbArmoryHandlerThread.setOnShellExecuterFinishedListener(
        (resultObject, actionCode) -> {
          if (getView() != null) {
            switch (actionCode) {
              case USBArmoryHandlerThread.IS_INIT_EXIST:
                if ((int) resultObject == 0) {
                  is_init_exists = true;
                  {
                    Message msg = new Message();
                    msg.what = USBArmoryHandlerThread.RETRIEVE_USB_FUNCS;
                    msg.obj =
                        "cat /init.nethunter.rc | grep -E -o 'sys.usb.config=([a-zA-Z,_]+)' | sed"
                            + " 's/sys.usb.config=//' | sort | uniq";
                    usbArmoryHandlerThread.getHandler().sendMessage(msg);
                  }
                } else {
                  is_init_exists = false;
                  uiHandler.post(
                      () -> {
                        usbFuncWinArrayAdapter.clear();
                        usbFuncWinArrayAdapter.addAll(
                            getResources().getStringArray(R.array.usbarmory_usb_states_win_lin));
                        usbFuncMACArrayAdapter.clear();
                        usbFuncMACArrayAdapter.addAll(
                            getResources().getStringArray(R.array.usbarmory_usb_states_mac));
                      });
                }
                break;
              case USBArmoryHandlerThread.RETRIEVE_USB_FUNCS:
                uiHandler.post(
                    () -> {
                      ArrayList<String> usbFuncArray =
                          new ArrayList<>(Arrays.asList(resultObject.toString().split("\\n")));
                      List<String> usbFuncWinArray =
                          Lists.newArrayList(
                              Collections2.filter(usbFuncArray, Predicates.containsPattern("win")));
                      List<String> usbFuncMacArray =
                          Lists.newArrayList(
                              Collections2.filter(usbFuncArray, Predicates.containsPattern("mac")));
                      usbFuncWinArrayAdapter.clear();
                      usbFuncWinArrayAdapter.addAll(usbFuncWinArray);
                      usbFuncMACArrayAdapter.clear();
                      usbFuncMACArrayAdapter.addAll(usbFuncMacArray);
                      for (EditText infoEditText : usbSwitchInfoEditTextGroup) {
                        infoEditText.setEnabled(false);
                      }
                      saveUSBFunctionConfigButton.setEnabled(false);
                      adbSpinner.setEnabled(false);
                    });
                break;
              case USBArmoryHandlerThread.SETUSBIFACE:
                uiHandler.post(
                    () -> {
                      if ((int) resultObject != 0) {
                        NhPaths.showSnack(getView(), "Failed to set USB function.", false);
                      } else {
                        NhPaths.showSnack(getView(), "USB function set successfully.", false);
                        reloadUSBStateImageButton.performClick();
                      }
                      setUSBIfaceButton.setEnabled(true);
                    });
                break;
              case USBArmoryHandlerThread.RELOAD_USBIFACE:
                uiHandler.post(
                    () -> {
                      if (resultObject.toString().equals("")) {
                        usbStatusTextView.setText("No USB function has been enabled");
                        imageMounterLL.setVisibility(View.GONE);
                        mountedImageHintTextView.setVisibility(View.VISIBLE);
                      } else {
                        usbStatusTextView.setText(
                            resultObject
                                .toString()
                                .replaceAll("/config/usb_gadget/g1/functions/", "")
                                .replaceAll("/config/usb_gadget/g1/functions", "gsi.rndis")
                                .replaceAll(" ", "\n"));
                        if (usbStatusTextView.getText().toString().contains("mass_storage")) {
                          imageMounterLL.setVisibility(View.VISIBLE);
                          mountedImageHintTextView.setVisibility(View.GONE);
                          getImageFiles();
                        } else {
                          imageMounterLL.setVisibility(View.GONE);
                          mountedImageHintTextView.setVisibility(View.VISIBLE);
                        }
                      }
                    });
                break;
              case USBArmoryHandlerThread.RELOAD_MOUNTSTATUS:
                uiHandler.post(
                    () -> {
                      if (resultObject.toString().equals("")) {
                        mountedImageTextView.setText("No image is mounted.");
                      } else {
                        mountedImageTextView.setText(resultObject.toString());
                      }
                    });
                break;
              case USBArmoryHandlerThread.MOUNT_IMAGE:
                uiHandler.post(
                    () -> {
                      if ((int) resultObject == 0) {
                        NhPaths.showSnack(
                            getView(),
                            imgFileSpinner.getText().toString() + " has been mounted.",
                            false);
                      } else {
                        NhPaths.showSnack(
                            getView(),
                            "Failed to mount image " + imgFileSpinner.getText().toString(),
                            false);
                      }
                      reloadMountStateButton.performClick();
                      mountImgButton.setEnabled(true);
                      unmountImgButton.setEnabled(true);
                    });
                break;
              case USBArmoryHandlerThread.UNMOUNT_IMAGE:
                uiHandler.post(
                    () -> {
                      if ((int) resultObject == 0) {
                        NhPaths.showSnack(
                            getView(),
                            imgFileSpinner.getText().toString() + " has been unmounted.",
                            false);
                        reloadMountStateButton.performClick();
                      } else {
                        NhPaths.showSnack(
                            getView(),
                            "Failed to unmount image "
                                + imgFileSpinner.getText().toString()
                                + ". Your drive may be still be in use by the host, please eject"
                                + " your drive on the host first,and then try to umount the image"
                                + " again.",
                            true);
                      }
                      reloadMountStateButton.performClick();
                      mountImgButton.setEnabled(true);
                      unmountImgButton.setEnabled(true);
                    });
                break;
              case USBArmoryHandlerThread.GET_USBSWITCH_SQL_DATA:
                uiHandler.post(
                    () -> {
                      usbSwitchInfoEditTextGroup[0].setText(
                          ((USBArmoryUSBSwitchModel) resultObject).getidVendor());
                      usbSwitchInfoEditTextGroup[1].setText(
                          ((USBArmoryUSBSwitchModel) resultObject).getidProduct());
                      usbSwitchInfoEditTextGroup[2].setText(
                          ((USBArmoryUSBSwitchModel) resultObject).getmanufacturer());
                      usbSwitchInfoEditTextGroup[3].setText(
                          ((USBArmoryUSBSwitchModel) resultObject).getproduct());
                      usbSwitchInfoEditTextGroup[4].setText(
                          ((USBArmoryUSBSwitchModel) resultObject).getserialnumber());
                    });
                break;
            }
          }
        });
  }

  @Override
  public void onStart() {
    super.onStart();
    refreshUSBSwitchInfos(gettargetOSSpinnerString(), getusbFuncSpinnerString());
    reloadUSBStateImageButton.performClick();
    if (imageMounterLL.getVisibility() == View.VISIBLE) {
      reloadMountStateButton.performClick();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    inflater.inflate(R.menu.usbarmory, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @SuppressLint({"NonConstantResourceId", "SetTextI18n"})
  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final LayoutInflater inflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    assert inflater != null;
    final View promptView = inflater.inflate(R.layout.materialhunter_custom_dialog_view, null);
    final EditText storedpathEditText = promptView.findViewById(R.id.cdw_et);

    switch (item.getItemId()) {
      case R.id.f_usbarmory_menu_backupDB:
        storedpathEditText.setText(NhPaths.APP_SD_SQLBACKUP_PATH + "/FragmentUSBArsenal");
        MaterialAlertDialogBuilder adbBackup = new MaterialAlertDialogBuilder(activity);
        adbBackup.setTitle("Full path to where you want to save the database:");
        adbBackup.setView(promptView);
        adbBackup.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        adbBackup.setPositiveButton("OK", (dialog, which) -> {});
        final AlertDialog adBackup = adbBackup.create();
        adBackup.setOnShowListener(
            dialog -> {
              final Button buttonOK = adBackup.getButton(DialogInterface.BUTTON_POSITIVE);
              buttonOK.setOnClickListener(
                  v -> {
                    String returnedResult =
                        USBArmorySQL.getInstance(context)
                            .backupData(storedpathEditText.getText().toString());
                    if (returnedResult == null) {
                      NhPaths.showSnack(
                          getView(),
                          "db is successfully backup to " + storedpathEditText.getText().toString(),
                          false);
                    } else {
                      dialog.dismiss();
                      new AlertDialog.Builder(context)
                          .setTitle("Failed to backup the DB.")
                          .setMessage(returnedResult)
                          .create()
                          .show();
                    }
                    dialog.dismiss();
                  });
            });
        adBackup.show();
        break;
      case R.id.f_usbarmory_menu_restoreDB:
        storedpathEditText.setText(NhPaths.APP_SD_SQLBACKUP_PATH + "/FragmentUSBArsenal");
        MaterialAlertDialogBuilder adbRestore = new MaterialAlertDialogBuilder(activity);
        adbRestore.setTitle("Full path of the db file from where you want to restore:");
        adbRestore.setView(promptView);
        adbRestore.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        adbRestore.setPositiveButton("OK", (dialog, which) -> {});
        final AlertDialog adRestore = adbRestore.create();
        adRestore.setOnShowListener(
            dialog -> {
              final Button buttonOK = adRestore.getButton(DialogInterface.BUTTON_POSITIVE);
              buttonOK.setOnClickListener(
                  v -> {
                    String returnedResult =
                        USBArmorySQL.getInstance(context)
                            .restoreData(storedpathEditText.getText().toString());
                    if (returnedResult == null) {
                      NhPaths.showSnack(
                          getView(),
                          "db is successfully restored to "
                              + storedpathEditText.getText().toString(),
                          false);
                      refreshUSBSwitchInfos(gettargetOSSpinnerString(), getusbFuncSpinnerString());
                    } else {
                      dialog.dismiss();
                      new AlertDialog.Builder(context)
                          .setTitle("Failed to restore the DB.")
                          .setMessage(returnedResult)
                          .create()
                          .show();
                    }
                    dialog.dismiss();
                  });
            });
        adRestore.show();
        break;
      case R.id.f_usbarmory_menu_ResetToDefault:
        if (USBArmorySQL.getInstance(context).resetData()) {
          NhPaths.showSnack(getView(), "db is successfully reset to default.", false);
          refreshUSBSwitchInfos(gettargetOSSpinnerString(), getusbFuncSpinnerString());
        } else {
          NhPaths.showSnack(getView(), "Failed to reset the db to default.", false);
        }
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    usbStatusTextView = null;
    mountedImageTextView = null;
    mountedImageHintTextView = null;
    reloadUSBStateImageButton = null;
    reloadMountStateButton = null;
    imageMounterLL = null;
    setUSBIfaceButton = null;
    mountImgButton = null;
    unmountImgButton = null;
    saveUSBFunctionConfigButton = null;
    targetOSSpinner = null;
    imgFileSpinner = null;
    usbFuncSpinner = null;
    adbSpinner = null;
    Arrays.fill(usbSwitchInfoEditTextGroup, null);
    usbSwitchInfoEditTextGroup = null;
  }

  private void getImageFiles() {
    mountImgButton.setEnabled(false);
    unmountImgButton.setEnabled(false);
    ArrayList<String> result = new ArrayList<>();
    File image_folder = new File(NhPaths.APP_SD_FILES_IMG_PATH);
    if (!image_folder.exists()) {
      NhPaths.showSnack(getView(), "Creating directory for storing image files...", false);
      try {
        image_folder.mkdir();
      } catch (Exception e) {
        e.printStackTrace();
        NhPaths.showSnack(
            getView(),
            "Failed to get images files from " + NhPaths.SD_PATH + "/MassStorage",
            false);
        return;
      }
    }
    try {
      File[] filesInFolder = image_folder.listFiles();
      assert filesInFolder != null;
      for (File file : filesInFolder) {
        if (!file.isDirectory()) {
          if (file.getName().contains(".img") || file.getName().contains(".iso")) {
            result.add(file.getName());
          }
        }
      }
    } catch (NullPointerException e) {
      e.printStackTrace();
      Log.e(TAG, e.toString());
    }
    ArrayAdapter<String> imageAdapter = new ArrayAdapter<>(activity, R.layout.mhspinner, result);
    imgFileSpinner.setAdapter(imageAdapter);
    
    if (result.size() > 0) imgFileSpinner.setText(result.get(0).toString());
    mountImgButton.setEnabled(true);
    unmountImgButton.setEnabled(true);
  }

  private String getusbFuncSpinnerString() {
    return usbFuncSpinner.getText().toString()
        + (adbSpinner.getText().toString().equals("Enable") ? ",adb" : "");
  }

  private String gettargetOSSpinnerString() {
    return targetOSSpinner.getText().toString();
  }

  private boolean isAllUSBInfosValid() {
    if (!is_init_exists) {
      if (!usbSwitchInfoEditTextGroup[0].getText().toString().matches("^0x[0-9a-fA-F]{4}$")) {
        new MaterialAlertDialogBuilder(context)
            .setTitle("Invalid Format")
            .setMessage("The regex must be ^0x[0-9a-fA-F]{4}$")
            .create()
            .show();
        return false;
      }
      if (!usbSwitchInfoEditTextGroup[1].getText().toString().matches("^0x[0-9a-fA-F]{4}$")) {
        new MaterialAlertDialogBuilder(context)
            .setTitle("Invalid Format")
            .setMessage("The regex must be ^0x[0-9a-fA-F]{4}$")
            .create()
            .show();
        return false;
      }
      if (!usbSwitchInfoEditTextGroup[2].getText().toString().matches("^\\w+$|^$")) {
        new MaterialAlertDialogBuilder(context)
            .setTitle("Invalid Format")
            .setMessage("The regex must be ^\\w+$|^$")
            .create()
            .show();
        return false;
      }
      if (!usbSwitchInfoEditTextGroup[3].getText().toString().matches("^\\w+$|^$")) {
        new MaterialAlertDialogBuilder(context)
            .setTitle("Invalid Format")
            .setMessage("The regex must be ^\\w+$|^$")
            .create()
            .show();
        return false;
      }
      if (!usbSwitchInfoEditTextGroup[4].getText().toString().matches("^[0-9A-Z]{10}$|^$")) {
        new MaterialAlertDialogBuilder(context)
            .setTitle("Invalid Format")
            .setMessage("The regex must be ^[0-9A-Z]{10}$|^$")
            .create()
            .show();
        return false;
      }
    }
    return true;
  }

  private void refreshUSBSwitchInfos(String targetOSName, String functionName) {
    Message msg = new Message();
    Bundle bundle = new Bundle();
    bundle.putString("targetOSName", targetOSName);
    bundle.putString("functionName", functionName);
    msg.what = USBArmoryHandlerThread.GET_USBSWITCH_SQL_DATA;
    msg.obj = context;
    msg.setData(bundle);
    usbArmoryHandlerThread.getHandler().sendMessage(msg);
  }
}