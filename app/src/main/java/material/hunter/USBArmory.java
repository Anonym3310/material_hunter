package material.hunter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import material.hunter.SQL.USBArmorySQL;
import material.hunter.ThemedActivity;
import material.hunter.models.USBArmoryUSBSwitchModel;
import material.hunter.utils.PathsUtil;
import material.hunter.utils.ShellExecuter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class USBArmory extends ThemedActivity {

    private static boolean is_init_exists = false;

    private Activity activity;
    private Context context;
    private ActionBar actionBar;
    private AutoCompleteTextView targetOSSpinner;
    private AutoCompleteTextView usbFuncSpinner;
    private AutoCompleteTextView adbSpinner;
    private AutoCompleteTextView imgFileSpinner;
    private Button reloadUSBStateImageButton;
    private Button reloadMountStateButton;
    private Button setUSBIfaceButton;
    private Button mountImgButton;
    private Button unmountImgButton;
    private Button saveUSBFunctionConfigButton;
    private CheckBox readOnlyCheckBox;
    private ExecutorService executor;
    private LinearLayout imageMounterLL;
    private TextInputLayout adbSpinnerLayout;
    private TextView usbStatusTextView;
    private TextView mountedImageTextView;
    private TextView mountedImageHintTextView;
    private View _view;

    private ShellExecuter exe = new ShellExecuter();
    private EditText[] usbSwitchInfoEditTextGroup = new TextInputEditText[5];

    MaterialToolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        activity = this;
        executor = Executors.newSingleThreadExecutor();

        setContentView(R.layout.usbarmory_activity);

        _view = getWindow().getDecorView();
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        targetOSSpinner = findViewById(R.id.f_usbarmory_spr_targetplatform);
        usbFuncSpinner = findViewById(R.id.f_usbarmory_spr_usbfunctions);
        adbSpinnerLayout = findViewById(R.id.f_usbarmory_spr_adb_lauout);
        adbSpinner = findViewById(R.id.f_usbarmory_spr_adb);
        imgFileSpinner = findViewById(R.id.f_usbarmory_spr_img_files);
        setUSBIfaceButton = findViewById(R.id.f_usbarmory_btn_setusbinterface);
        mountImgButton = findViewById(R.id.f_usbarmory_btn_mountImage);
        unmountImgButton = findViewById(R.id.f_usbarmory_btn_unmountImage);
        reloadUSBStateImageButton = findViewById(R.id.f_usbarmory_imgbtn_reloadUSBStatus);
        reloadMountStateButton = findViewById(R.id.f_usbarmory_imgbtn_reloadMountStatus);
        saveUSBFunctionConfigButton = findViewById(R.id.f_usbarmory_btn_saveusbfuncswitch);
        readOnlyCheckBox = findViewById(R.id.f_usbarmory_chkbox_ReadOrWrite);
        usbStatusTextView = findViewById(R.id.f_usbarmory_tv_current_usb_state);
        mountedImageTextView = findViewById(R.id.f_usbarmory_tv_mount_state);
        mountedImageHintTextView = findViewById(R.id.f_usbarmory_ll_tv_imagemounter_hint);
        imageMounterLL = findViewById(R.id.f_usbarmory_ll_imageMounter_sub2);

        usbSwitchInfoEditTextGroup[0] = findViewById(R.id.f_usbarmory_et_idvendor);
        usbSwitchInfoEditTextGroup[1] = findViewById(R.id.f_usbarmory_et_idproduct);
        usbSwitchInfoEditTextGroup[2] = findViewById(R.id.f_usbarmory_et_manufacturer);
        usbSwitchInfoEditTextGroup[3] = findViewById(R.id.f_usbarmory_et_product);
        usbSwitchInfoEditTextGroup[4] = findViewById(R.id.f_usbarmory_et_serialnumber);

        File sql_folder = new File(PathsUtil.APP_SD_SQLBACKUP_PATH);
        if (!sql_folder.exists()) {
            PathsUtil.showSnack(_view, "Creating directory for backing up dbs...", false);
            try {
                sql_folder.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                PathsUtil.showSnack(
                        _view,
                        "Failed to create directory " + PathsUtil.APP_SD_SQLBACKUP_PATH,
                        false);
            }
        }

        ArrayAdapter<String> usb_target =
                new ArrayAdapter<String>(
                        activity, R.layout.mh_spinner_item, new String[] {"Windows", "Linux", "Mac OS"});
        ArrayAdapter<String> usb_functions =
                new ArrayAdapter<String>(
                        activity,
                        R.layout.mh_spinner_item,
                        getResources().getStringArray(R.array.usbarmory_usb_states_win_lin));
        ArrayAdapter<String> usb_functions_mac =
                new ArrayAdapter<String>(
                        activity,
                        R.layout.mh_spinner_item,
                        getResources().getStringArray(R.array.usbarmory_usb_states_mac));
        ArrayAdapter<String> adb_enable =
                new ArrayAdapter<String>(
                        activity, R.layout.mh_spinner_item, new String[] {"Enable", "Disable"});

        targetOSSpinner.setAdapter(usb_target);
        usbFuncSpinner.setAdapter(usb_functions);
        adbSpinner.setAdapter(adb_enable);

        adbSpinnerLayout.setEnabled(false);

        ArrayAdapter<String> usbFuncWinArrayAdapter =
                new ArrayAdapter<>(activity, R.layout.mh_spinner_item, new ArrayList<>());
        ArrayAdapter<String> usbFuncMACArrayAdapter =
                new ArrayAdapter<>(activity, R.layout.mh_spinner_item, new ArrayList<>());

        executor.execute(
                () -> {
                    if (exe.RunAsRootReturnValue("[ -f /init.nethunter.rc ]") == 0) {
                        is_init_exists = true;
                        String result =
                                exe.RunAsRootOutput(
                                        "cat /init.nethunter.rc | grep -E -o"
                                                + " 'sys.usb.config=([a-zA-Z,_]+)' | sed"
                                                + " 's/sys.usb.config=//' | sort | uniq");
                        ArrayList<String> usbFuncArray =
                                new ArrayList<>(Arrays.asList(result.split("\\n")));
                        List<String> usbFuncWinArray =
                                Lists.newArrayList(
                                        Collections2.filter(
                                                usbFuncArray, Predicates.containsPattern("win")));
                        List<String> usbFuncMacArray =
                                Lists.newArrayList(
                                        Collections2.filter(
                                                usbFuncArray, Predicates.containsPattern("mac")));
                        usbFuncWinArrayAdapter.clear();
                        usbFuncWinArrayAdapter.addAll(usbFuncWinArray);
                        usbFuncMACArrayAdapter.clear();
                        usbFuncMACArrayAdapter.addAll(usbFuncMacArray);
                        new Handler(Looper.getMainLooper())
                                .post(
                                        () -> {
                                            for (EditText infoEditText :
                                                    usbSwitchInfoEditTextGroup) {
                                                infoEditText.setEnabled(false);
                                            }
                                            saveUSBFunctionConfigButton.setEnabled(false);
                                            adbSpinnerLayout.setEnabled(false);
                                        });
                    } else {
                        is_init_exists = false;
                        usbFuncWinArrayAdapter.clear();
                        usbFuncWinArrayAdapter.addAll(
                                getResources()
                                        .getStringArray(R.array.usbarmory_usb_states_win_lin));
                        usbFuncMACArrayAdapter.clear();
                        usbFuncMACArrayAdapter.addAll(
                                getResources().getStringArray(R.array.usbarmory_usb_states_mac));
                    }
                });

        targetOSSpinner.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View v, int i, long l) {
                        if (i == 2) {
                            usbFuncSpinner.setAdapter(usbFuncMACArrayAdapter);
                        } else {
                            usbFuncSpinner.setAdapter(usbFuncWinArrayAdapter);
                        }
                        refreshUSBSwitchInfos(
                                gettargetOSSpinnerString(), getusbFuncSpinnerString());
                    }
                });

        usbFuncSpinner.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(
                            AdapterView<?> parent, View view, int position, long id) {
                        if (position == 0) {
                            adbSpinnerLayout.setEnabled(false);
                            adbSpinner.setText("Disable", false);
                        } else {
                            adbSpinnerLayout.setEnabled(true);
                        }
                        refreshUSBSwitchInfos(
                                gettargetOSSpinnerString(), getusbFuncSpinnerString());
                    }
                });

        adbSpinner.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(
                            AdapterView<?> parent, View view, int position, long id) {
                        refreshUSBSwitchInfos(
                                gettargetOSSpinnerString(), getusbFuncSpinnerString());
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
                                                : targetOSSpinner
                                                                .getText()
                                                                .toString()
                                                                .equals("Mac OS")
                                                        ? "mac"
                                                        : "";
                        String functions = usbFuncSpinner.getText().toString();
                        String adbEnable =
                                adbSpinner.getText().toString().equals("Enable") ? ",adb" : "";
                        String idVendor =
                                " -v '" + usbSwitchInfoEditTextGroup[0].getText().toString() + "'";
                        String idProduct =
                                " -p '" + usbSwitchInfoEditTextGroup[1].getText().toString() + "'";
                        String manufacturer =
                                usbSwitchInfoEditTextGroup[2].getText().toString().isEmpty()
                                        ? ""
                                        : " -m '"
                                                + usbSwitchInfoEditTextGroup[2].getText().toString()
                                                + "'";
                        String product =
                                usbSwitchInfoEditTextGroup[3].getText().toString().isEmpty()
                                        ? ""
                                        : " -P '"
                                                + usbSwitchInfoEditTextGroup[3].getText().toString()
                                                + "'";
                        String serialnumber =
                                usbSwitchInfoEditTextGroup[4].getText().toString().isEmpty()
                                        ? ""
                                        : " -s '"
                                                + usbSwitchInfoEditTextGroup[4].getText().toString()
                                                + "'";

                        executor.execute(
                                () -> {
                                    int result =
                                            exe.RunAsRootReturnValue(
                                                    "[ -f /init.nethunter.rc ] && setprop"
                                                            + " sys.usb.config "
                                                            + functions
                                                            + " || "
                                                            + PathsUtil.APP_SCRIPTS_PATH
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
                                                            + serialnumber);
                                    new Handler(Looper.getMainLooper())
                                            .post(
                                                    () -> {
                                                        if (result != 0) {
                                                            PathsUtil.showSnack(
                                                                    _view,
                                                                    "Failed to set USB function.",
                                                                    false);
                                                        } else {
                                                            PathsUtil.showSnack(
                                                                    _view,
                                                                    "USB function set"
                                                                            + " successfully.",
                                                                    false);
                                                            reloadUSBStateImageButton
                                                                    .performClick();
                                                        }
                                                        setUSBIfaceButton.setEnabled(true);
                                                    });
                                });
                    }
                });

        reloadUSBStateImageButton.setOnClickListener(
                v -> {
                    executor.execute(
                            () -> {
                                String result =
                                        exe.RunAsRootOutput(
                                                "find /config/usb_gadget/g1/configs/b.1 -type l"
                                                        + " -exec readlink -e {} \\; | xargs echo");
                                new Handler(Looper.getMainLooper())
                                        .post(
                                                () -> {
                                                    if (result.equals("")) {
                                                        usbStatusTextView.setText(
                                                                "No USB function has been enabled");
                                                        imageMounterLL.setVisibility(View.GONE);
                                                        mountedImageHintTextView.setVisibility(
                                                                View.VISIBLE);
                                                    } else {
                                                        usbStatusTextView.setText(
                                                                result.replaceAll(
                                                                                "/config/usb_gadget/g1/functions/",
                                                                                "")
                                                                        .replaceAll(
                                                                                "/config/usb_gadget/g1/functions",
                                                                                "gsi.rndis")
                                                                        .replaceAll(" ", "\n"));
                                                        if (usbStatusTextView
                                                                .getText()
                                                                .toString()
                                                                .contains("mass_storage")) {
                                                            imageMounterLL.setVisibility(
                                                                    View.VISIBLE);
                                                            mountedImageHintTextView.setVisibility(
                                                                    View.GONE);
                                                            getImageFiles();
                                                        } else {
                                                            imageMounterLL.setVisibility(View.GONE);
                                                            mountedImageHintTextView.setVisibility(
                                                                    View.VISIBLE);
                                                        }
                                                    }
                                                });
                            });
                });

        reloadMountStateButton.setOnClickListener(
                v -> {
                    executor.execute(
                            () -> {
                                String result =
                                        exe.RunAsRootOutput(
                                                "cat /config/usb_gadget/g1/functions/mass_storage.0/lun.0/file");
                                new Handler(Looper.getMainLooper())
                                        .post(
                                                () -> {
                                                    if (result.equals("")) {
                                                        mountedImageTextView.setText(
                                                                "No image is mounted");
                                                    } else {
                                                        mountedImageTextView.setText(result);
                                                    }
                                                    getImageFiles();
                                                });
                            });
                });

        mountImgButton.setOnClickListener(
                v -> {
                    if (imgFileSpinner.getText().toString().isEmpty()) {
                        PathsUtil.showSnack(_view, "No image file is selected.", false);
                    } else {
                        mountImgButton.setEnabled(false);
                        unmountImgButton.setEnabled(false);
                        executor.execute(
                                () -> {
                                    int result = 1;
                                    if (readOnlyCheckBox.isChecked())
                                        result =
                                                exe.RunAsRootReturnValue(
                                                        String.format(
                                                                "%s%s && echo '%s/%s' >"
                                                                    + " /config/usb_gadget/g1/functions/mass_storage.0/lun.0/file",
                                                                "echo '1' >"
                                                                    + " /config/usb_gadget/g1/functions/mass_storage.0/lun.0/ro",
                                                                imgFileSpinner
                                                                                .getText()
                                                                                .toString()
                                                                                .contains(".iso")
                                                                        ? " && echo '1' >"
                                                                              + " /config/usb_gadget/g1/functions/mass_storage.0/lun.0/cdrom"
                                                                        : " && echo '0' >"
                                                                              + " /config/usb_gadget/g1/functions/mass_storage.0/lun.0/cdrom",
                                                                PathsUtil.APP_SD_FILES_IMG_PATH,
                                                                imgFileSpinner
                                                                        .getText()
                                                                        .toString()));
                                    else
                                        result =
                                                exe.RunAsRootReturnValue(
                                                        String.format(
                                                                "%s%s && echo '%s/%s' >"
                                                                    + " /config/usb_gadget/g1/functions/mass_storage.0/lun.0/file",
                                                                "echo '0' >"
                                                                    + " /config/usb_gadget/g1/functions/mass_storage.0/lun.0/ro",
                                                                imgFileSpinner
                                                                                .getText()
                                                                                .toString()
                                                                                .contains(".iso")
                                                                        ? " && echo '1' >"
                                                                              + " /config/usb_gadget/g1/functions/mass_storage.0/lun.0/cdrom"
                                                                        : " && echo '0' >"
                                                                              + " /config/usb_gadget/g1/functions/mass_storage.0/lun.0/cdrom",
                                                                PathsUtil.APP_SD_FILES_IMG_PATH,
                                                                imgFileSpinner
                                                                        .getText()
                                                                        .toString()));
                                    if (result == 0)
                                        PathsUtil.showSnack(
                                                _view,
                                                imgFileSpinner.getText().toString()
                                                        + " has been mounted.",
                                                false);
                                    else
                                        PathsUtil.showSnack(
                                                _view,
                                                "Failed to mount image "
                                                        + imgFileSpinner.getText().toString(),
                                                false);
                                    new Handler(Looper.getMainLooper())
                                            .post(
                                                    () -> {
                                                        reloadMountStateButton.performClick();
                                                        mountImgButton.setEnabled(true);
                                                        unmountImgButton.setEnabled(true);
                                                    });
                                });
                    }
                });

        unmountImgButton.setOnClickListener(
                v -> {
                    mountImgButton.setEnabled(false);
                    unmountImgButton.setEnabled(false);
                    executor.execute(
                            () -> {
                                int result =
                                        exe.RunAsRootReturnValue(
                                                "echo '' >"
                                                    + " /config/usb_gadget/g1/functions/mass_storage.0/lun.0/file"
                                                    + " && echo '0' >"
                                                    + " /config/usb_gadget/g1/functions/mass_storage.0/lun.0/ro"
                                                    + " && echo '0' >"
                                                    + " /config/usb_gadget/g1/functions/mass_storage.0/lun.0/cdrom");
                                if (result == 0) {
                                    PathsUtil.showSnack(
                                            _view,
                                            imgFileSpinner.getText().toString()
                                                    + " has been unmounted.",
                                            false);
                                    reloadMountStateButton.performClick();
                                } else {
                                    PathsUtil.showSnack(
                                            _view,
                                            "Failed to unmount image "
                                                    + imgFileSpinner.getText().toString()
                                                    + ". Your drive may be still be in use by the"
                                                    + " host, please eject your drive on the host"
                                                    + " first, and then try to umount the image"
                                                    + " again.",
                                            true);
                                }
                                reloadMountStateButton.performClick();
                                new Handler(Looper.getMainLooper())
                                        .post(
                                                () -> {
                                                    mountImgButton.setEnabled(true);
                                                    unmountImgButton.setEnabled(true);
                                                });
                            });
                });

        saveUSBFunctionConfigButton.setOnClickListener(
                v -> {
                    if (isAllUSBInfosValid()) {
                        for (int i = 0; i < usbSwitchInfoEditTextGroup.length; i++) {
                            if (!USBArmorySQL.getInstance(context)
                                    .setUSBSwitchColumnData(
                                            getusbFuncSpinnerString(),
                                            i + 2,
                                            targetOSSpinner.getText().toString(),
                                            usbSwitchInfoEditTextGroup[i].getText().toString())) {
                                PathsUtil.showSnack(
                                        _view,
                                        "Something wrong when processing key "
                                                + Integer.toString(i),
                                        false);
                            }
                            PathsUtil.showSnack(_view, "Done!", false);
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshUSBSwitchInfos(gettargetOSSpinnerString(), getusbFuncSpinnerString());
        reloadUSBStateImageButton.performClick();
        if (imageMounterLL.getVisibility() == View.VISIBLE) reloadMountStateButton.performClick();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.usbarmory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        final View promptView = inflater.inflate(R.layout.input_dialog_view, null);
        final EditText storedpathEditText = promptView.findViewById(R.id.cdw_et);

        switch (item.getItemId()) {
            case R.id.f_usbarmory_menu_backupDB:
                storedpathEditText.setText(PathsUtil.APP_SD_SQLBACKUP_PATH + "/FragmentUSBArsenal");
                MaterialAlertDialogBuilder adbBackup = new MaterialAlertDialogBuilder(activity);
                adbBackup.setTitle("Full path to where you want to save the database:");
                adbBackup.setView(promptView);
                adbBackup.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                adbBackup.setPositiveButton("OK", (dialog, which) -> {});
                final AlertDialog adBackup = adbBackup.create();
                adBackup.setOnShowListener(dialog -> {
                    final Button buttonOK = adBackup.getButton(DialogInterface.BUTTON_POSITIVE);
                    buttonOK.setOnClickListener(v -> {
                        String returnedResult =
                            USBArmorySQL.getInstance(context)
                                    .backupData(
                                        storedpathEditText
                                                .getText()
                                                .toString());
                        if (returnedResult == null) {
                            PathsUtil.showSnack(
                                _view,
                                "db is successfully backup to "
                                        + storedpathEditText.getText().toString(),
                                false);
                        } else {
                            PathsUtil.showSnack(
                                _view,
                                "Failed to backup the DB.",
                                false);
                        }
                        dialog.dismiss();
                    });
                });
                adBackup.show();
                break;
            case R.id.f_usbarmory_menu_restoreDB:
                storedpathEditText.setText(PathsUtil.APP_SD_SQLBACKUP_PATH + "/FragmentUSBArsenal");
                MaterialAlertDialogBuilder adbRestore = new MaterialAlertDialogBuilder(activity);
                adbRestore.setTitle("Full path of the db file from where you want to restore:");
                adbRestore.setView(promptView);
                adbRestore.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                adbRestore.setPositiveButton("OK", (dialog, which) -> {});
                final AlertDialog adRestore = adbRestore.create();
                adRestore.setOnShowListener(dialog -> {
                    final Button buttonOK = adRestore.getButton(DialogInterface.BUTTON_POSITIVE);
                    buttonOK.setOnClickListener(v -> {
                        String returnedResult =
                            USBArmorySQL.getInstance(context)
                                    .restoreData(
                                        storedpathEditText
                                                .getText()
                                                .toString());
                        if (returnedResult == null) {
                            PathsUtil.showSnack(
                                _view,
                                "db is successfully restored to "
                                        + storedpathEditText.getText().toString(),
                                false);
                            refreshUSBSwitchInfos(
                                gettargetOSSpinnerString(),
                                getusbFuncSpinnerString());
                        } else {
                            PathsUtil.showSnack(
                                _view,
                                "Failed to restore the DB.",
                                false);
                        }
                        dialog.dismiss();
                    });
                });
                adRestore.show();
                break;
            case R.id.f_usbarmory_menu_ResetToDefault:
                if (USBArmorySQL.getInstance(context).resetData()) {
                    PathsUtil.showSnack(_view, "db is successfully reset to default.", false);
                    refreshUSBSwitchInfos(gettargetOSSpinnerString(), getusbFuncSpinnerString());
                } else {
                    PathsUtil.showSnack(_view, "Failed to reset the db to default.", false);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getImageFiles() {
        mountImgButton.setEnabled(false);
        unmountImgButton.setEnabled(false);
        ArrayList<String> result = new ArrayList<>();
        File image_folder = new File(PathsUtil.APP_SD_FILES_IMG_PATH);
        if (!image_folder.exists()) {
            PathsUtil.showSnack(_view, "Creating directory for storing image files...", false);
            try {
                image_folder.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                PathsUtil.showSnack(
                        _view,
                        "Failed to get images files from " + PathsUtil.SD_PATH + "/MassStorage",
                        false);
                return;
            }
        }
        try {
            File[] filesInFolder = image_folder.listFiles();
            assert filesInFolder != null;
            for (File file : filesInFolder) {
                if (!file.isDirectory()) {
                    if (file.getName().matches(".*\\.(img|iso)$")) {
                        result.add(file.getName());
                    }
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        ArrayAdapter<String> imageAdapter =
                new ArrayAdapter<>(activity, R.layout.mh_spinner_item, result);
        imgFileSpinner.setAdapter(imageAdapter);

        if (result.size() > 0) imgFileSpinner.setText(result.get(0).toString(), false);
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
            if (!usbSwitchInfoEditTextGroup[0].getText().toString().matches("^0x[A-f0-9]{4}$")) {
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Invalid format")
                        .setMessage("The id vendor must be mathes ^0x[A-f0-9]{4}$")
                        .create()
                        .show();
                return false;
            }
            if (!usbSwitchInfoEditTextGroup[1].getText().toString().matches("^0x[A-f0-9]{4}$")) {
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Invalid format")
                        .setMessage("The id product must be matches ^0x[A-f0-9]{4}$")
                        .create()
                        .show();
                return false;
            }
            if (!usbSwitchInfoEditTextGroup[2]
                    .getText()
                    .toString()
                    .matches("^[A-z0-9.,\\- ]+$|^$")) {
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Invalid format")
                        .setMessage("The manufacturer must be matches ^[A-z0-9.,\\- ]+$")
                        .create()
                        .show();
                return false;
            }
            if (!usbSwitchInfoEditTextGroup[3]
                    .getText()
                    .toString()
                    .matches("^[A-z0-9.,\\- ]+$|^$")) {
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Invalid format")
                        .setMessage("The product must be matches ^[A-z0-9.,\\- ]+$")
                        .create()
                        .show();
                return false;
            }
            if (!usbSwitchInfoEditTextGroup[4]
                    .getText()
                    .toString()
                    .matches("^[A-z0-9]{4,10}$|^$")) {
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Invalid format")
                        .setMessage("The serial number must be matches ^[A-z0-9]{4,10}$")
                        .create()
                        .show();
                return false;
            }
        }
        return true;
    }

    private void refreshUSBSwitchInfos(String targetOSName, String functionName) {
        executor.execute(() -> {
            USBArmoryUSBSwitchModel result =
                USBArmorySQL.getInstance(context)
                        .getUSBSwitchColumnData(targetOSName, functionName);
            String manufacturer =
                (result).getmanufacturer().isEmpty()
                        ? exe.RunAsRootOutput(
                                "cat /config/usb_gadget/g1/strings/0x409/manufacturer")
                        : (result).getmanufacturer();
            String product =
                (result).getproduct().isEmpty()
                        ? exe.RunAsRootOutput(
                                "cat /config/usb_gadget/g1/strings/0x409/product")
                        : (result).getproduct();
            String serialnumber =
                (result).getserialnumber().isEmpty()
                        ? exe.RunAsRootOutput(
                                "cat /config/usb_gadget/g1/strings/0x409/serialnumber")
                        : (result).getserialnumber();
            new Handler(Looper.getMainLooper()).post(() -> {
                usbSwitchInfoEditTextGroup[0].setText((result).getidVendor());
                usbSwitchInfoEditTextGroup[1].setText((result).getidProduct());
                usbSwitchInfoEditTextGroup[2].setText(manufacturer);
                usbSwitchInfoEditTextGroup[3].setText(product);
                usbSwitchInfoEditTextGroup[4].setText(serialnumber);
            });
        });
    }
}