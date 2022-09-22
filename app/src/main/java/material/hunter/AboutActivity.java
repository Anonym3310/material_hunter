package material.hunter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;

import com.google.android.material.appbar.MaterialToolbar;

public class AboutActivity extends ThemedActivity {

    private static ActionBar actionBar;

    private TextView app_name;
    private TextView author;
    private Button open_developers;
    private Button open_licenses;
    private Button open_github;

    MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        app_name = findViewById(R.id.app_name);
        author = findViewById(R.id.author);
        open_developers = findViewById(R.id.open_developers);
        open_licenses = findViewById(R.id.open_licenses);
        open_github = findViewById(R.id.open_github);

        app_name.setText(getString(R.string.app_name) + " " + version.name);
        author.setText("by " + version.author);

        open_developers.setOnClickListener(v -> {
            Intent intent = new Intent(this, AuthorsActivity.class);
            startActivity(intent);
        });

        open_licenses.setOnClickListener(v -> {
            Intent intent = new Intent(this, LicensesActivity.class);
            startActivity(intent);
        });

        open_github.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/mirivan/material_hunter"));
            startActivity(intent);
        });
    }
}