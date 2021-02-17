package material.hunter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import material.hunter.utils.NhPaths;
import material.hunter.utils.SharePrefTag;
import material.hunter.utils.ShellExecuter;

public class SettingsFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private Context context;
    private Activity activity;
    private SharedPreferences prefs;
    private ShellExecuter exe;
    private String selected_animation;
    private String selected_version;

    public static SettingsFragment newInstance(int sectionNumber) {
        SettingsFragment fragment = new SettingsFragment();
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
        this.prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        exe = new ShellExecuter();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuinflater) {
        menuinflater.inflate(R.menu.bt, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setup:
                RunSetup();
                return true;
            case R.id.update:
                RunUpdate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.settings, container, false);
        setHasOptionsMenu(true);

        //First run
        Boolean setupdone = prefs.getBoolean(SharePrefTag.ANIMATION_SETUP_DONE, false);
        if (!setupdone.equals(true))
            SetupDialog();

        //Bootanimation spinner
        String[] animations = new String[]{"Classic", "Burning", "New Kali", "ctOS", "Glitch"};
        Spinner animation_spinner = rootView.findViewById(R.id.animation_spinner);
        TextView pathtobackup = rootView.findViewById(R.id.restorefilename);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        pathtobackup.setHint(NhPaths.APP_SD_FILES_PATH + "_" + currentDateandTime + ".tar");
        animation_spinner.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, animations));

        //Select Animation
        final String[] animation_dir = {""};
        animation_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                final VideoView videoview = rootView.findViewById(R.id.videoView);
                selected_animation = parentView.getItemAtPosition(pos).toString();
                switch (selected_animation) {
                    case "Classic": {
                        String path = ("android.resource://" + context.getPackageName() + "/" + R.raw.boot_classic);
                        videoview.setVideoURI(Uri.parse(path));
                        animation_dir[0] = "src";
                        bootanimation_start();
                        break;
                    }
                    case "Burning": {
                        String path = ("android.resource://" + context.getPackageName() + "/" + R.raw.boot_mk);
                        videoview.setVideoURI(Uri.parse(path));
                        animation_dir[0] = "src_mk";
                        bootanimation_start();
                        break;
                    }
                    case "New Kali": {
                        String path = ("android.resource://" + context.getPackageName() + "/" + R.raw.boot_kali);
                        videoview.setVideoURI(Uri.parse(path));
                        animation_dir[0] = "src_kali";
                        bootanimation_start();
                        break;
                    }
                    case "ctOS": {
                        String path = ("android.resource://" + context.getPackageName() + "/" + R.raw.boot_ctos);
                        videoview.setVideoURI(Uri.parse(path));
                        animation_dir[0] = "src_ctos";
                        bootanimation_start();
                        break;
                    }
                    case "Glitch": {
                    String path = ("android.resource://" + context.getPackageName() + "/" + R.raw.boot_glitch);
                    videoview.setVideoURI(Uri.parse(path));
                    animation_dir[0] = "src_glitch";
                    bootanimation_start();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) { }});

        //   ProgressDialog progressDialog = new ProgressDialog(context);
        //   progressDialog.setMessage("Loading...");
        //   progressDialog.setCancelable(false);
        //   progressDialog.show();
        //        progressDialog.dismiss();

        //Convert Checkbox
        CheckBox ConvertCheckbox = rootView.findViewById(R.id.convert);
        EditText ImageWidth = rootView.findViewById(R.id.image_width);
        EditText ImageHeight = rootView.findViewById(R.id.image_height);
        EditText FinalWidth = rootView.findViewById(R.id.final_width);
        EditText FinalHeight = rootView.findViewById(R.id.final_height);
        ImageWidth.setEnabled(false);
        ImageHeight.setEnabled(false);
        ConvertCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                ImageWidth.setEnabled(true);
                ImageWidth.setTextColor(Color.parseColor("#FFFFFF"));
                ImageHeight.setEnabled(true);
                ImageHeight.setTextColor(Color.parseColor("#FFFFFF"));
            } else {
                ImageWidth.setEnabled(false);
                ImageWidth.setTextColor(Color.parseColor("#40FFFFFF"));
                ImageHeight.setEnabled(false);
                ImageHeight.setTextColor(Color.parseColor("#40FFFFFF"));
            }
        });

        //Preview Checkbox
        View PreView = rootView.findViewById(R.id.pre_view);
        CheckBox PreviewCheckbox = rootView.findViewById(R.id.preview_checkbox);
        PreviewCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                PreView.setVisibility(View.VISIBLE);
            } else {
                PreView.setVisibility(View.GONE);
            }
        });

        // Screen size
        DisplayMetrics displaymetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) activity.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();
        int API_LEVEL = android.os.Build.VERSION.SDK_INT;
        if (API_LEVEL >= 17) {
            disp.getRealMetrics(displaymetrics);
        } else {
            disp.getMetrics(displaymetrics);
        }
        final int screen_height = displaymetrics.heightPixels;
        final int screen_width = displaymetrics.widthPixels;
        final String size = screen_width + "x" + screen_height;
        final TextView ScreenSize = rootView.findViewById(R.id.screen_size);
        ScreenSize.setText(size);

        //Bootanimation path
        EditText BootanimationPath = rootView.findViewById(R.id.bootanimation_path);
        ShellExecuter exe = new ShellExecuter();
        String bootanimation_path = exe.RunAsRootOutput("find /vendor /system -name \"*ootanimation.zip\"");
        BootanimationPath.setText(bootanimation_path);

        //Make bootanimation
        Button MakeBootAnimationButton = rootView.findViewById(R.id.make_bootanimation);
        EditText FPS = rootView.findViewById(R.id.fps);
        addClickListener(MakeBootAnimationButton, v -> {
            String resizeCMD;
            String imagesCMD;
            String foldersCMD;
            if (ConvertCheckbox.isChecked()) {
                if (selected_animation.equals("Burning")) foldersCMD = "";
                else foldersCMD = " new/part1 new/part2";
                resizeCMD = " -resize " + ImageWidth.getText().toString() + "x" + ImageHeight.getText().toString() + " ";
                imagesCMD = " mkdir -p new/part0" + foldersCMD + " && echo \"Converting images...\"" +
                        "&& for i in {0000..0100}; do convert" + resizeCMD + animation_dir[0] + "/part0/$i.jpg new/part0/$i.jpg >/dev/null 2>&1; done; echo \"[+] part0 done\" " +
                        "&& if [ -d new/part1 ]; then for i in {0000..0200}; do convert" + resizeCMD + animation_dir[0] + "/part1/$i.jpg new/part1/$i.jpg >/dev/null 2>&1; done; fi; echo \"[+] part1 done\" " +
                        "&& if [ -d new/part2 ]; then for i in {0000..0100}; do convert" + resizeCMD + animation_dir[0] + "/part2/$i.jpg new/part2/$i.jpg >/dev/null 2>&1; done; fi; echo \"[+] part2 done\" ";
            } else {
                imagesCMD = " mkdir new && cp -r " + animation_dir[0] + "/part* new/";
            }
            String finalRES = FinalWidth.getText().toString() + "x" + FinalHeight.getText().toString();
            String finalFPS = FPS.getText().toString();
            intentClickListener_NH(NhPaths.makeTermTitle("Building animation") + "cd /root/nethunter-bootanimation &&" + imagesCMD + " && cp " + animation_dir[0] +
                    "/desc.txt new/ && sed -i '1s/.*/" + finalRES + " " + finalFPS + "/' new/desc.txt && sed -i 's/x/ /g' new/desc.txt && cd new && zip -0 -FSr -q /sdcard/bootanimation.zip * && cd .. && rm -r new && echo \"Done. Head back to MaterialHunter to install the bootanimation! Exiting in 3secs..\" && sleep 3 && exit");
        });

        //Install bootanimation
        Button InstallBootAnimationButton = rootView.findViewById(R.id.set_bootanimation);
        addClickListener(InstallBootAnimationButton, v -> {
            File AnimationZip = new File(NhPaths.SD_PATH + "/bootanimation.zip");
            if (AnimationZip.length() == 0)
                NhPaths.showSnack(getView(), "Bootanimation zip is not created!", 1);
            else {
                intentClickListener_NHSU(NhPaths.makeTermTitle("Installing animation") + "grep ' / ' /proc/mounts | grep -qv 'rootfs' || grep -q ' /system_root ' /proc/mounts && SYSTEM=/ || SYSTEM=/system && " +
                        "mount -o rw,remount $SYSTEM && cp " + NhPaths.SD_PATH + "/bootanimation.zip " + bootanimation_path +
                        "&& echo \"Done. Please reboot to check the result! Exiting in 3secs..\" && sleep 3 && exit");
            }
        });

        //Backup
        Button BackupButton = rootView.findViewById(R.id.backup);
        addClickListener(BackupButton, v -> {
            exe.RunAsRoot(new String[]{"cd " + NhPaths.SD_PATH + "/nh_files && tar -czvf /sdcard/nh_files_" + currentDateandTime + ".tar *"});
            NhPaths.showSnack(getView(), "Backup has been saved to /sdcard/nh_files_" + currentDateandTime, 2);
        });

        //Restore
        final EditText RestoreFileName = rootView.findViewById(R.id.restorefilename);
        final Button RestoreFileBrowse = rootView.findViewById(R.id.restorefilebrowse);
        RestoreFileBrowse.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/x-tar");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select archive file"), 1001);
        });

        final Button RestoreButton = rootView.findViewById(R.id.restore);
        RestoreButton.setOnClickListener(v -> {
            String RestoreFilePath = RestoreFileName.getText().toString();
            File RestoreFile = new File(RestoreFilePath);
            if (RestoreFile.length() == 0) {
                NhPaths.showSnack(getView(), "Select a backup file to restore!", 1);
            } else {
                exe.RunAsRoot(new String[]{"rm -r " + NhPaths.SD_PATH + "/nh_files/* && tar -xvf " + RestoreFilePath + " -C " + NhPaths.SD_PATH + "/nh_files/"});
                NhPaths.showSnack(getView(), "nh_files has been successfully restored", 1);
            }
        });

        //Uninstall
        final Button UninstallButton = rootView.findViewById(R.id.uninstall_nh);
        File NhSystemApp = new File("/system/app/MaterialHunter/MaterialHunter.apk");
        addClickListener(UninstallButton, v -> {
            if (NhSystemApp.length() == 0) {
                NhPaths.showSnack(getView(), "MaterialHunter was not flashed as system app! Please remove it from Android Settings.", 1);
            } else {
                intentClickListener_NHSU("echo -ne \"\\033]0;Uninstalling MaterialHunter\\007\" && clear;grep ' / ' /proc/mounts | grep -qv 'rootfs' || grep -q ' /system_root ' /proc/mounts && SYSTEM=/ || SYSTEM=/system && " +
                        "mount -o rw,remount $SYSTEM && rm " + NhSystemApp + " && pm clear material.hunter && echo \"Done! Reboot your device to complete the process. Exiting in 3secs..\" && sleep 3 && exit");
            }
        });

        //Busybox
        TextView BusyboxVersion = rootView.findViewById(R.id.busybox_version);

        String busybox_ver = exe.RunAsRootOutput("/system/xbin/busybox | head -n1 | cut -c 10-13");
        BusyboxVersion.setText(busybox_ver);

        final String[] busybox_file = {null};
        Spinner busybox_spinner = rootView.findViewById(R.id.bb_spinner);
        String commandBB = ("ls /system/xbin | grep busybox_nh- | cut -f 2 -d '-'");
        String outputBB = exe.RunAsRootOutput(commandBB);
        final String[] bbArray = outputBB.split("\n");
        ArrayAdapter usersadapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1, bbArray);
        busybox_spinner.setAdapter(usersadapter);

        //Select Version
        busybox_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                selected_version = parentView.getItemAtPosition(pos).toString();
                if (selected_version.equals("1.25")) {
                    busybox_file[0] = "busybox_nh-1.25";
                } else if (selected_version.equals("1.32.0")){
                    busybox_file[0] = "busybox_nh-1.32";
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) { }
        });

        final Button BusyboxButton = rootView.findViewById(R.id.select_bb);
        BusyboxButton.setOnClickListener( v -> {
            File busybox = new File("/system/xbin/" + busybox_file[0]);
            exe.RunAsRoot(new String[]{"grep ' / ' /proc/mounts | grep -qv 'rootfs' || grep -q ' /system_root ' /proc/mounts && SYSTEM=/ || SYSTEM=/system;mount -o rw,remount $SYSTEM && rm /system/xbin/busybox_nh;ln -s " + busybox + " /system/xbin/busybox_nh"});
            NhPaths.showSnack(getView(), "NetHunter BusyBox version has been successfully modified", 1);
        });
        final Button BusyboxSystemButton = rootView.findViewById(R.id.system_bb);
        String busybox_system = exe.RunAsRootOutput("/system/xbin/busybox | head -n1 | grep -iF nethunter");
        if (busybox_system.equals("")) {
            BusyboxSystemButton.setEnabled(true);
            BusyboxSystemButton.setTextColor(Color.parseColor("#FFFFFFFF"));
        } else {
            BusyboxSystemButton.setEnabled(false);
            BusyboxSystemButton.setTextColor(Color.parseColor("#40FFFFFF"));
        }
        BusyboxSystemButton.setOnClickListener( v -> {
            exe.RunAsRoot(new String[]{"grep ' / ' /proc/mounts | grep -qv 'rootfs' || grep -q ' /system_root ' /proc/mounts && SYSTEM=/ || SYSTEM=/system;mount -o rw,remount $SYSTEM && rm /system/xbin/busybox;ln -s /system/xbin/busybox_nh /system/xbin/busybox"});
            NhPaths.showSnack(getView(), "Default system BusyBox has been changed", 1);
        });
        return rootView;
    }

    private void bootanimation_start() {
        final VideoView videoview = getActivity().findViewById(R.id.videoView);
        videoview.requestFocus();
        videoview.setOnPreparedListener(mp -> {
            videoview.start();
            mp.setLooping(true);
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            if (resultCode == Activity.RESULT_OK) {
                EditText ResotreFileName = getActivity().findViewById(R.id.restorefilename);
                String FilePath = data.getData().getPath();
                FilePath = exe.RunAsRootOutput("echo " + FilePath + " | sed -e 's/\\/document\\/primary:/\\/sdcard\\//g' ");
                ResotreFileName.setText(FilePath);
            }
        }
    }

    public void SetupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Welcome to Settings!")
        .setMessage(getString(R.string.bt_welcome_msg))
                .setPositiveButton(getString(R.string.bt_check_and_install), (dialog, which) -> {
                    RunSetup();
                    prefs.edit().putBoolean(SharePrefTag.ANIMATION_SETUP_DONE, true).apply();
                })
                .setCancelable(false).setNegativeButton((R.string.bt_welcome_daa), (dialogInterface, i) -> prefs.edit().putBoolean("animation_setup_done", true).apply())
                .show();
    }

    public void RunSetup() {
        intentClickListener_NH("echo -ne \"\\033]0;Bootanimation Setup\\007\" && clear;if [[ -f /usr/bin/convert ]];then echo \"Imagemagick is installed!\"; else " +
                "apt-get update && apt-get install imagemagick -y;fi; if [[ -f /root/nethunter-bootanimation ]];then echo \"Nethunter-bootanimation is installed!\"; else " +
                "git clone https://gitlab.com/kalilinux/nethunter/apps/kali-nethunter-bootanimation /root/nethunter-bootanimation;fi; echo \"Everything is ready! Closing in 3secs..\"; sleep 3 && exit ");
        prefs.edit().putBoolean(SharePrefTag.ANIMATION_SETUP_DONE, true).apply();
    }

    public void RunUpdate() {
        intentClickListener_NH("echo -ne \"\\033]0;Bootanimation Update\\007\" && clear;apt-get update && apt-get install imagemagick -y;if [[ -d /root/nethunter-bootanimation ]];then cd /root/nethunter-bootanimation;git pull" +
                ";fi; echo \"Done! Closing in 3secs..\"; sleep 3 && exit ");
        prefs.edit().putBoolean(SharePrefTag.ANIMATION_SETUP_DONE, true).apply();
    }


    private void addClickListener(Button _button, View.OnClickListener onClickListener) {
        _button.setOnClickListener(onClickListener);
    }

    private void intentClickListener_NH(final String command) {
        try {
            Intent intent = new Intent("com.offsec.nhterm.RUN_SCRIPT_NH");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("com.offsec.nhterm.iInitialCommand", command);
            startActivity(intent);
        } catch (Exception e) {
            NhPaths.showSnack(getView(), getString(R.string.toast_install_terminal), 1);
        }
    }

    private void intentClickListener_NHSU(final String command) {
        try {
            Intent intent = new Intent("com.offsec.nhterm.RUN_SCRIPT_SU");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("com.offsec.nhterm.iInitialCommand", command);
            startActivity(intent);
        } catch (Exception e) {
            NhPaths.showSnack(getView(), getString(R.string.toast_install_terminal), 1);
        }
    }
}