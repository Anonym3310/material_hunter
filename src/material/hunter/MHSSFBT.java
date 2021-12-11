package material.hunter;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MHSSFBT extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pelmeshek);
        Intent i = new Intent(this, AppNavHomeActivity.class);
        startActivity(i);
        finish();
    }
}
