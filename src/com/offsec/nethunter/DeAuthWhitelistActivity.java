package com.offsec.nethunter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import java.util.Locale;

public class DeAuthWhitelistActivity extends AppCompatActivity {

    private final ShellExecuter exe = new ShellExecuter();
    private Activity activity;

    @SuppressLint("SdCardPath")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(R.layout.deauth_whitelist);
        if (Build.VERSION.SDK_INT >= 21) {
            // detail for android 5 devices
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorBars));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorBars));
        }

        EditText whitelist = findViewById(R.id.deauth_modify);
        whitelist.setText(String.format(Locale.getDefault(), getString(R.string.loading_file), "/sdcard/nh_files/deauth/whitelist.txt"));
        exe.ReadFile_ASYNC("/sdcard/nh_files/deauth/whitelist.txt", whitelist);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        NhPaths.showMessage(activity, "File Loaded");
    }

    public void updatewhitelist(View view) {
        EditText source = findViewById(R.id.deauth_modify);
        String newSource = source.getText().toString();
        @SuppressLint("SdCardPath") boolean isSaved = exe.SaveFileContents(newSource, "/sdcard/nh_files/deauth/whitelist.txt");
        if (isSaved) {
            NhPaths.showMessage(activity, "Source updated");
        } else {
            NhPaths.showMessage(activity, "Source not updated");
        }
    }
}