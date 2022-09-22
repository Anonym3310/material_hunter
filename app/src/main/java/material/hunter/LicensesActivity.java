package material.hunter;

import android.content.res.AssetManager;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import material.hunter.models.LicenseModel;
import material.hunter.RecyclerViewAdapter.LicensesRecyclerViewAdapter;
import material.hunter.utils.PathsUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LicensesActivity extends ThemedActivity {

    private static ActionBar actionBar;

    private RecyclerView recycler;
    private List<LicenseModel> list = new ArrayList<LicenseModel>();

    MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.licenses_activity);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        AssetManager assetManager = getAssets();
        String licensesDir = "licenses";
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;
        try {
            String[] dirs = assetManager.list(licensesDir);
            if (dirs == null || dirs.length == 0) return;
            for (String dir : dirs) {

                String[] assets = assetManager.list(licensesDir + "/" + dir);
                
                for (String asset : assets) {
                    reader = new BufferedReader(new InputStreamReader(assetManager.open(licensesDir + "/" + dir + "/" + asset), "UTF-8"));

                    int value;
                    while ((value = reader.read()) != -1) {
                        builder.append((char) value);
                    }
                    String builded = builder.toString();
                    list.add(new LicenseModel(asset, builded.substring(0, builded.length() -1)));
                    builder = new StringBuilder();
                }
            }
        } catch (IOException e) {
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }

        LicensesRecyclerViewAdapter adapter = new LicensesRecyclerViewAdapter(this, this, list);

        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recycler.setAdapter(adapter);
    }
}