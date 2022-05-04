package material.hunter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.nio.charset.Charset;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.net.MalformedURLException;
import java.net.URLConnection;
import java.net.URL;

import material.hunter.utils.PathsUtil;
import material.hunter.utils.ShellExecuter;

import rajankur.AndroidHardwareCodename;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MaterialHunterFragment extends Fragment {

    public static TextView mh_news;
    public static ImageView expander;
    public static MaterialCardView magisk;
    public static TextView sys_info;
    public static TextView material_info;
    public static ImageView materialhunter_license;
    public static MaterialCardView support;
    public static MaterialCardView selinux_card;
    public static TextView selinux_status;

    private static final String ARG_SECTION_NUMBER = "section_number";
    private Context context;
    private Activity activity;
    private ExecutorService executor;
    private SharedPreferences prefs;
    private ShellExecuter exe = new ShellExecuter();
    private boolean news_expanded = false;
    private boolean selinux_enforcing = true;
    private String selinux_now = "enforcing";

    public static MaterialHunterFragment newInstance(int sectionNumber) {
        MaterialHunterFragment fragment = new MaterialHunterFragment();
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
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        prefs = getActivity().getSharedPreferences("material.hunter", Context.MODE_PRIVATE);
        return inflater.inflate(R.layout.materialhunter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mh_news = view.findViewById(R.id.mh_news);
        expander = view.findViewById(R.id.expander);
        magisk = view.findViewById(R.id.magisk_card);
        sys_info = view.findViewById(R.id.sys_info);
        material_info = view.findViewById(R.id.material_info);
        materialhunter_license = view.findViewById(R.id.materialhunter_license);
        support = view.findViewById(R.id.telegram_card);
        selinux_card = view.findViewById(R.id.selinux_card);
        selinux_status = view.findViewById(R.id.selinux_status);

        executor.execute(() -> {
            if (exe.RunAsRootOutput("getenforce").equals("Enforcing")) {
                selinux_enforcing = true;
                selinux_now = "enforcing";
            } else {
                selinux_enforcing = false;
                selinux_now = "permissive";
            }

            new Handler(Looper.getMainLooper()).post(() -> {
                    selinux_status.setText("Selinux status: " + selinux_now + ". Click to change it.");
            });
        });

        executor.execute(() -> {
            try {
                URL url = new URL("https://raw.githubusercontent.com/Mirivan/dev-root-project/main/.materialhunter");
                URLConnection connection = url.openConnection();
                connection.connect();

                BufferedReader r = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    sb.append(line).append("\n");
                }

                final String res = sb.toString();
                prefs.edit().putString("last_news", res).commit();
                new Handler(Looper.getMainLooper()).post(() -> {
                    mh_news.setText(res.endsWith("\n") ? res.substring(0, res.length()-1) : res);
                });
            } catch (MalformedURLException e){

            } catch (IOException e) {
                String res = prefs.getString("last_news", "\u0410\u0432\u0442\u043E\u0440 \u043A\u043B\u0438\u0435\u043D\u0442\u0430 \u0443\u0432\u0430\u0436\u0430\u0435\u0442 \u041A\u043E\u043C\u0430\u0440\u0443, \u0431\u043E\u043B\u044C\u0448\u0435 \u043D\u043E\u0432\u043E\u0441\u0442\u0435\u0439 \u043D\u0435\u0442.");
                mh_news.setText(res.endsWith("\n") ? res.substring(0, res.length()-1) : res);
            }
        });

        expander.setOnClickListener(v -> {
            if (news_expanded) {
                mh_news.setMaxLines(4);
                expander.setImageResource(R.drawable.expand_more);
                news_expanded = false;
            } else {
                mh_news.setMaxLines(Integer.MAX_VALUE);
                expander.setImageResource(R.drawable.expand_less);
                news_expanded = true;
            }
        });

        executor.execute(() -> {
            if (magiskPassed()) {
                // nothing to do
            } else {
                if (!prefs.getBoolean("magisk_info_hide", false)) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        magisk.setVisibility(View.VISIBLE);
                    });
                }
            }
        });
        magisk.setOnClickListener(v -> {
            if (exe.RunAsRootReturnValue("[ -f " + PathsUtil.MAGISK_DB_PATH + " ]") == 0) {
                if (exe.RunAsRootOutput(PathsUtil.APP_SCRIPTS_BIN_PATH + "/sqlite3 " + PathsUtil.MAGISK_DB_PATH + " \"SELECT * from policies\" | grep material.hunter").startsWith("material.hunter")) {
                    if (exe.RunAsRootOutput(PathsUtil.APP_SCRIPTS_BIN_PATH + "/sqlite3 " + PathsUtil.MAGISK_DB_PATH + " \"UPDATE policies SET logging='0',notification='0' WHERE package_name='material.hunter';\"").isEmpty()) {}
                } else {
                    if (exe.RunAsRootOutput(PathsUtil.APP_SCRIPTS_BIN_PATH + "/sqlite3 " + PathsUtil.MAGISK_DB_PATH + " \"UPDATE policies SET logging='0',notification='0' WHERE uid='$(stat -c %u /data/data/material.hunter)';\"").isEmpty()) {}
                }
            }
            magisk.setVisibility(View.GONE);
        });
        magisk.setOnLongClickListener(v -> {
            prefs.edit().putBoolean("magisk_info_hide", true).commit();
            magisk.setVisibility(View.GONE);
            return true;
        });

        executor.execute(() -> {
            StringBuilder sb = new StringBuilder();

            sb.append(
                "Model: "
                        + Build.BRAND
                        + " "
                        + Build.MODEL
                        + " ("
                        + new AndroidHardwareCodename().getCodename()
                        + ")\n");
            sb.append(
                "OS Version: Android "
                        + Build.VERSION.RELEASE
                        + ", SDK "
                        + Build.VERSION.SDK_INT
                        + "\n\n");

            sb.append(
                "System-as-root: "
                        + exe.RunAsRootOutput("grep ' / ' /proc/mounts | grep -qv 'rootfs' || grep -q ' /system_root ' /proc/mounts && echo true || echo false"));

            new Handler(Looper.getMainLooper()).post(() -> {
                sys_info.setText(sb.toString());
            });
        });

        String package_name = context.getPackageName();
        String app_version = "";
        try {
            PackageInfo app_info = context.getPackageManager().getPackageInfo(package_name, 0);
            app_version = app_info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            app_version = version.name;
        }
        material_info.append("Made with \u2764\uFE0F by @mirivan\n");
        material_info.append("Version: " + app_version);

        materialhunter_license.setOnClickListener(v -> showLicense());

        support.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/mirivan"));
            startActivity(intent);
        });

        selinux_card.setOnClickListener(v -> {
            executor.execute(() -> {
                if (selinux_enforcing) {
                    exe.RunAsRoot("setenforce 0");
                    selinux_enforcing = false;
                    selinux_now = "permissive";
                } else {
                    exe.RunAsRoot("setenforce 1");
                    selinux_enforcing = true;
                    selinux_now = "enforcing";
                }
                new Handler(Looper.getMainLooper()).post(() -> {
                    selinux_status.setText("Selinux status: " + selinux_now + ". Click to change it.");
                });
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private boolean magiskPassed() {
        boolean result;
        boolean res = exe.RunAsRootOutput(PathsUtil.APP_SCRIPTS_BIN_PATH + "/sqlite3 " + PathsUtil.MAGISK_DB_PATH + " \"SELECT * from policies\" | grep material.hunter").startsWith("(material.hunter");
        if (res) {
            result = exe.RunAsRootOutput(PathsUtil.APP_SCRIPTS_BIN_PATH + "/sqlite3 " + PathsUtil.MAGISK_DB_PATH + " \"SELECT notification from policies WHERE package_name='material.hunter'\"").equals("0");
        } else {
            result = exe.RunAsRootOutput(PathsUtil.APP_SCRIPTS_BIN_PATH + "/sqlite3 " + PathsUtil.MAGISK_DB_PATH + " \"SELECT notification from policies WHERE uid='$(stat -c %u /data/data/material.hunter)'\"").equals("0");
        }
        return result;
    }

    private void showLicense() {
        final View root = getLayoutInflater().inflate(R.layout.license_layout, null);
        MaterialAlertDialogBuilder adb = new MaterialAlertDialogBuilder(context);
        final TextView license = root.findViewById(R.id.license_text);
        license.setText(
            Html.fromHtml(
                "<a href='https://t.me/kali_nh'>@kali_nh</a> - our royal hub<br>" +
                "<a href='https://t.me/mirivan'>@mirivan</a> - developer<br>" +
                "<a href='https://t.me/hilledkinged'>@hilledkinged</a> - idea author<br>" +
                "<a href='https://t.me/zalexdev'>@zalexdev</a> - stryker/mh developer<br>" +
                "<a href='https://t.me/aVadamiao'>@aVadamiao</a> - legcay icon designer<br>" +
                "<br>" +
                "Also thanks to:<br>" +
                "<a href='https://t.me/AetherMage'>@AetherMage</a> - for f**king mh selinux checker<br>" +
                "<a href='https://t.me/zalexdev'>@zalexdev</a> - ways to solve many problems, spiritual motivation<br>" +
                "<br>" +
                "There was also some person who made the MaterialHunter logo, thank you very much",
                Html.FROM_HTML_MODE_LEGACY
            )
        );
        license.setMovementMethod(LinkMovementMethod.getInstance());
        adb.setView(root).setNegativeButton("Close", (dialog, which) -> {});
        adb.setCancelable(true);
        adb.show();
    }
}