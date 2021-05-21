package material.hunter;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class MHSSFBT extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pelmeshek);
        new Handler().postDelayed((Runnable) () -> {
            Intent i = new Intent(this, AppNavHomeActivity.class);
            startActivity(i);
            finish();
        }, (long) (1.5 * 1000));
    }
}