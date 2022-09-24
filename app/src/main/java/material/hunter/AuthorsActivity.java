package material.hunter;

import android.content.res.AssetManager;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import material.hunter.RecyclerViewAdapter.AuthorsRecyclerViewAdapter;
import material.hunter.models.AuthorsModel;
import material.hunter.utils.PathsUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AuthorsActivity extends ThemedActivity {

    private static ActionBar actionBar;

    private RecyclerView recycler;
    private List<AuthorsModel> list = new ArrayList<AuthorsModel>();

    MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authors_activity);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        AssetManager assetManager = getAssets();
        String authorsDir = "authors";
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;
        try {
            String[] assets = assetManager.list(authorsDir);
            if (assets == null || assets.length == 0) return;

            for (String asset : assets) {
                reader = new BufferedReader(new InputStreamReader(assetManager.open(authorsDir + "/" + asset), "UTF-8"));

                int value;
                while ((value = reader.read()) != -1) {
                    builder.append((char) value);
                }
                String[] builded = builder.toString().split("\n", 3);
                list.add(
                        new AuthorsModel(
                                asset,
                                builded.length > 0 ? builded[0] : "",
                                builded.length > 1 ? builded[1] : "",
                                builded.length > 2 ? builded[2] : ""));
                builder = new StringBuilder();
            }
        } catch (IOException e) {
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    PathsUtil.showMessage(this, e.toString(), true);
                }
            }
        }

        AuthorsRecyclerViewAdapter adapter = new AuthorsRecyclerViewAdapter(this, this, list);

        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recycler.setAdapter(adapter);
    }
}