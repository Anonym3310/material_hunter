package material.hunter;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;

import material.hunter.utils.NhPaths;
import material.hunter.utils.ShellExecuter;

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
        MaterialToolbar t = findViewById(R.id.appbar);
        setSupportActionBar(t);

        getWindow().setStatusBarColor(getResources().getColor(R.color.colorBars));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorBars));

        EditText source = findViewById(R.id.source);
        source.setText(String.format(Locale.getDefault(), getString(R.string.loading_file), configFilePath));
        exe.ReadFile_ASYNC(configFilePath, source);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        NhPaths.showSnack(findViewById(R.id.viewSource), getString(R.string.edit_source_loaded), 1);
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
        boolean isSaved = exe.SaveFileContents(newSource, configFilePath);
        if (isSaved) {
            NhPaths.showSnack(view, getString(R.string.edit_source_updated), 1);
        } else {
            NhPaths.showSnack(view, getString(R.string.edit_source_not_updated), 1);
        }
    }
}