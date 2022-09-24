package melville37;

import android.os.Looper;
import android.os.Handler;

import androidx.annotation.MainThread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class MelvilleExecutor {

    private final ExecutorService executor;
    private boolean running = false;

    public MelvilleExecutor() {
        this.executor = Executors.newSingleThreadExecutor();
    }

    private void startBackground() {
        running = true;
        onPreExecute();
        executor.execute(() -> {
            doInBackground();
            new Handler(Looper.getMainLooper()).post(() -> {
                onPostExecute();
                running = false;
            });
        });
    }

    public void run() {
        startBackground();
    }

    public boolean isRunning() {
        return running;
    }

    public abstract void onPreExecute(); 

    public abstract void doInBackground();

    public abstract void onPostExecute();
}