package material.hunter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Locale;

import material.hunter.utils.NhPaths;
import material.hunter.utils.ShellExecuter;

public class DeAuthWhitelistActivity extends AppCompatActivity {

    private final ShellExecuter exe = new ShellExecuter();

    @SuppressLint("SdCardPath")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deauth_whitelist);
        MaterialToolbar t = findViewById(R.id.appbar);
        setSupportActionBar(t);

        EditText whitelist = findViewById(R.id.deauth_modify);
        whitelist.setText(String.format(Locale.getDefault(), getString(R.string.loading_file), "/sdcard/nh_files/deauth/whitelist.txt"));
        exe.ReadFile_ASYNC("/sdcard/nh_files/deauth/whitelist.txt", whitelist);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        NhPaths.showSnack(findViewById(R.id.deauth_whitelist), getString(R.string.edit_source_loaded), 1);
    }

    public void updatewhitelist(View view) {
        EditText source = findViewById(R.id.deauth_modify);
        String newSource = source.getText().toString();
        @SuppressLint("SdCardPath") boolean isSaved = exe.SaveFileContents(newSource, "/sdcard/nh_files/deauth/whitelist.txt");
        if (isSaved) {
            NhPaths.showSnack(view, getString(R.string.edit_source_updated), 1);
        } else {
            NhPaths.showSnack(view, getString(R.string.edit_source_not_updated), 1);
        }
    }
}