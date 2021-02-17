package material.hunter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import material.hunter.utils.NhPaths;
import material.hunter.utils.ShellExecuter;

public class PineappleFragment extends Fragment {

    private static final String TAG = "PineappleFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private String start_type = "start ";
    private String proxy_type;

    public static PineappleFragment newInstance(int sectionNumber) {
        PineappleFragment fragment = new PineappleFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.pineapple, container, false);

        Log.d(TAG, NhPaths.APP_SCRIPTS_PATH);

        // Checkbox for No Upstream
        final CheckBox noupCheckbox = rootView.findViewById(R.id.pineapple_noup);
        View.OnClickListener checkBoxListener = v -> {
            if (noupCheckbox.isChecked()) {
                start_type = "start_noup ";
            } else {
                start_type = "start ";
            }
        };
        noupCheckbox.setOnClickListener(checkBoxListener);

        // Checkbox for Transparent Proxy
        final CheckBox transCheckbox = rootView.findViewById(R.id.pineapple_transproxy);
        checkBoxListener = v -> {
            if (noupCheckbox.isChecked()) {
                proxy_type = " start_proxy ";
            } else {
                proxy_type = "";
            }
        };
        transCheckbox.setOnClickListener(checkBoxListener);

        // Start Button
        addClickListener(R.id.pineapple_start_button, v -> {
            new Thread(() -> {
                ShellExecuter exe = new ShellExecuter();
                String command = "su -c '" + NhPaths.APP_SCRIPTS_PATH + "/pine-nano " + start_type + startConnection(rootView) + proxy_type + "'";
                Log.d(TAG, command);
                exe.RunAsRootOutput(command);
            }).start();
            NhPaths.showSnack(getView(), getString(R.string.pineapple_starting), 1);
        }, rootView);

        // Stop|Close Button
        addClickListener(R.id.pineapple_close_button, v -> {
            new Thread(() -> {
                ShellExecuter exe = new ShellExecuter();
                String command = "su -c '" + NhPaths.APP_SCRIPTS_PATH + "/pine-nano stop'";
                Log.d(TAG, command);
                exe.RunAsRootOutput(command);
            }).start();
            NhPaths.showSnack(getView(), getString(R.string.pineapple_bringing), 1);
        }, rootView);

        return rootView;
    }

    private String startConnection(View rootView) {
        EditText port = rootView.findViewById(R.id.pineapple_webport);
        EditText gateway_ip = rootView.findViewById(R.id.pineapple_gatewayip);
        EditText web_ip = rootView.findViewById(R.id.pineapple_clientip);
        EditText CIDR = rootView.findViewById(R.id.pineapple_cidr);
        return web_ip.getText() + " " + CIDR.getText() + " " + gateway_ip.getText() + " " + port.getText();
    }

    private void addClickListener(int buttonId, View.OnClickListener onClickListener, View rootView) {
        rootView.findViewById(buttonId).setOnClickListener(onClickListener);
    }
}