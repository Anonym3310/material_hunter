package material.hunter.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ActiveShellExecuter {

    private Activity activity;
    private Context context;
    private ExecutorService executor;
    private int endCode = 0;
    private final SimpleDateFormat timeStamp =
            new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    public ActiveShellExecuter(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
        this.executor = Executors.newSingleThreadExecutor();
    }

    private void init(String command, TextView logger) {
        onPrepare();
        executor.execute(() -> {
            String line;
            try {
                final SharedPreferences prefs =
                        context.getSharedPreferences(
                                "material.hunter", Context.MODE_PRIVATE);
                Process process = Runtime.getRuntime().exec("su -mm");
                OutputStream stdin = process.getOutputStream();
                InputStream stdout = process.getInputStream();
                stdin.write((command + "\n").getBytes());
                stdin.write(("exit\n").getBytes());
                stdin.flush();
                stdin.close();
                BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
                while ((line = br.readLine()) != null) {
                    final String newLine = line;
                    final Spannable tempText = new SpannableString(line + "\n");
                    final Spannable timestamp =
                            prefs.getBoolean("show_timestamp", false)
                                    ? new SpannableString(
                                            "[ " + timeStamp.format(new Date()) + " ]  ")
                                    : new SpannableString("");
                    if (line.startsWith("[!]"))
                        tempText.setSpan(
                                new ForegroundColorSpan(Color.parseColor("#08FBFF")),
                                0,
                                tempText.length(),
                                0);
                    else if (line.startsWith("[+]"))
                        tempText.setSpan(
                                new ForegroundColorSpan(Color.parseColor("#00DC00")),
                                0,
                                tempText.length(),
                                0);
                    else if (line.startsWith("[-]")
                            || line.contains("do not")
                            || line.contains("don't"))
                        tempText.setSpan(
                                new ForegroundColorSpan(Color.parseColor("#D81B60")),
                                0,
                                tempText.length(),
                                0);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        onNewLine(newLine + "\n");
                        logger.append(timestamp);
                        logger.append(tempText);
                    });
                }
                br.close();
                process.waitFor();
                process.destroy();
                endCode = process.exitValue();
                new Handler(Looper.getMainLooper()).post(() -> logger.append("<<<< End with " + endCode + " >>>>\n"));
            } catch (IOException e) {

            } catch (InterruptedException ex) {

            }
            new Handler(Looper.getMainLooper()).post(() -> onFinished(endCode));
        });
    }

    public void exec(String command, TextView logger) {
        init(command, logger);
    }

    public abstract void onPrepare();

    public abstract void onNewLine(String line);

    public abstract void onFinished(int code);
}