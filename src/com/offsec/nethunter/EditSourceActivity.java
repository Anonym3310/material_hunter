package com.offsec.nethunter;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import java.util.Locale;

public class EditSourceActivity extends AppCompatActivity {

    private final ShellExecuter exe = new ShellExecuter();
    private String configFilePath = "";
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        Bundle b = getIntent().getExtras();
        configFilePath = b.getString("path");
        setContentView(R.layout.source);
        if (Build.VERSION.SDK_INT >= 21) {
            // detail for android 5 devices
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorBars));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorBars));
        }

        EditText source = findViewById(R.id.source);
        source.setText(String.format(Locale.getDefault(), getString(R.string.loading_file), configFilePath));
        exe.ReadFile_ASYNC(configFilePath, source);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        NhPaths.showMessage(activity, "File Loaded");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateSource(View view) {
        EditText source = findViewById(R.id.source);
        String newSource = source.getText().toString();
        Boolean isSaved = exe.SaveFileContents(newSource, configFilePath);
        if (isSaved) {
            NhPaths.showMessage(activity, "Source updated");
        } else {
            NhPaths.showMessage(activity, "Source not updated");
        }
    }
}