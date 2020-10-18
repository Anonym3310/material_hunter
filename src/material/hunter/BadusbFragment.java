package material.hunter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import material.hunter.utils.NhPaths;
import material.hunter.utils.ShellExecuter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BadusbFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private final ShellExecuter exe = new ShellExecuter();
    private String sourcePath;
    private Context context;
    private Activity activity;

    public static BadusbFragment newInstance(int sectionNumber) {
        BadusbFragment fragment = new BadusbFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        activity = getActivity();
        if (Build.VERSION.SDK_INT >= 21) {
            sourcePath = NhPaths.APP_SD_FILES_PATH + "/configs/startbadusb-lollipop.sh";
        } else {
            sourcePath = NhPaths.APP_SD_FILES_PATH + "/configs/startbadusb-kitkat.sh";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.badusb, container, false);
        final Button updateOptions = rootView.findViewById(R.id.updateOptions);
        updateOptions.setOnClickListener(view -> updateOptions());
        final Button startService = rootView.findViewById(R.id.start_service);
        startService.setOnClickListener(view -> start());
        final Button stopService = rootView.findViewById(R.id.stop_service);
        stopService.setOnClickListener(view -> stop());
        loadOptions(rootView);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) {
            loadOptions(getView().getRootView());
        }
    }

    private void updateOptions() {
        String sourceFile = exe.ReadFile_SYNC(sourcePath);
        TextInputEditText ifc = activity.findViewById(R.id.ifc);
        sourceFile = sourceFile.replaceAll("(?m)^INTERFACE=(.*)$", "INTERFACE=" + ifc.getText().toString());
        Boolean r = exe.SaveFileContents(sourceFile, sourcePath);// 1st arg contents, 2nd arg filepath
        if (r) {
            NhPaths.showSnack(getView(), getString(R.string.options_updated), 1);
        } else {
            NhPaths.showSnack(getView(), getString(R.string.options_not_updated), 1);
        }
    }

    private void loadOptions(View rootView) {
        final TextInputEditText ifc = rootView.findViewById(R.id.ifc);
        new Thread(() -> {
            final String text = exe.ReadFile_SYNC(sourcePath);
            ifc.post(() -> {
                String regExpatInterface = "^INTERFACE=(.*)$";
                Pattern pattern = Pattern.compile(regExpatInterface, Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    String ifcValue = matcher.group(1);
                    ifc.setText(ifcValue);
                }
            });
        }).start();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.badusb, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.start_service:
                start();
                return true;
            case R.id.stop_service:
                stop();
                return true;
            case R.id.source_button:
                Intent i = new Intent(activity, EditSourceActivity.class);
                i.putExtra("path", sourcePath);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void start() {
        ShellExecuter exe = new ShellExecuter();
        String[] command = new String[1];
        if (Build.VERSION.SDK_INT >= 21) {
            command[0] = NhPaths.APP_SCRIPTS_PATH + "/start-badusb-lollipop &> " + NhPaths.APP_SD_FILES_PATH + "/badusb.log &";
        } else {
            command[0] = NhPaths.APP_SCRIPTS_PATH + "/start-badusb-kitkat &> " + NhPaths.APP_SD_FILES_PATH + "/badusb.log &";
        }
        exe.RunAsRoot(command);
        NhPaths.showSnack(getView(), getString(R.string.badusb_started), 1);
    }

    private void stop() {
        ShellExecuter exe = new ShellExecuter();
        String[] command = new String[1];
        if (Build.VERSION.SDK_INT >= 21) {
            command[0] = NhPaths.APP_SCRIPTS_PATH + "/stop-badusb-lollipop";
        } else {
            command[0] = NhPaths.APP_SCRIPTS_PATH + "/stop-badusb-kitkat";
        }
        exe.RunAsRoot(command);
        NhPaths.showSnack(getView(), getString(R.string.badusb_stopped), 1);
    }
}