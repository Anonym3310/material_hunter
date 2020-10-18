package material.hunter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import material.hunter.gps.KaliGPSUpdates;
import material.hunter.utils.NhPaths;
import material.hunter.utils.ShellExecuter;


public class KaliGpsServiceFragment extends Fragment implements KaliGPSUpdates.Receiver {

    private static final String TAG = "KaliGpsServiceFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private KaliGPSUpdates.Provider gpsProvider = null;
    private TextView gpsTextView;
    private Context context;

    public KaliGpsServiceFragment() {
    }

    public static KaliGpsServiceFragment newInstance(int sectionNumber) {
        KaliGpsServiceFragment fragment = new KaliGpsServiceFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.gps, container, false);
        addClickListener(R.id.start_kismet, v -> {

            if (gpsProvider != null) {
                gpsProvider.onLocationUpdatesRequested(KaliGpsServiceFragment.this);
                gpsTextView.append("Starting gps updates \n");
            }
        }, rootView);

        addClickListener(R.id.gps_stop, v -> {
            if (gpsProvider != null) {
                gpsProvider.onStopRequested();
                gpsTextView.append("Stopping gps updates \n");
                new Thread(() -> {
                    ShellExecuter exe = new ShellExecuter();
                    String command = "su -c '" + NhPaths.APP_SCRIPTS_PATH + "/stop-gpsd'";
                    exe.RunAsRootOutput(command);
                }).start();
            }
        }, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gpsTextView = view.findViewById(R.id.gps_textview);
    }

    private void addClickListener(int buttonId, View.OnClickListener onClickListener, View rootView) {
        rootView.findViewById(buttonId).setOnClickListener(onClickListener);
    }


    @Override
    public void onAttach(Context context) {
        if (context instanceof KaliGPSUpdates.Provider) {
            this.gpsProvider = (KaliGPSUpdates.Provider) context;
        }

        super.onAttach(context);
    }

    @Override
    public void onPositionUpdate(String nmeaSentences) {

    }

    @Override
    public void onFirstPositionUpdate() {
        gpsTextView.append("First fix received. Starting Kismet \n");
        startKismet();
    }

    private void startKismet() {
        try {
            Intent intent = new Intent("com.offsec.nhterm.RUN_SCRIPT_NH");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("com.offsec.nhterm.iInitialCommand", "/usr/bin/start-kismet");
            startActivity(intent);
        } catch (Exception e) {
            NhPaths.showSnack(getView(), getString(R.string.toast_install_terminal), 1);
        }
    }
}

