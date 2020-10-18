package material.hunter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import material.hunter.AsyncTask.MacchangerAsyncTask;
import material.hunter.utils.NhPaths;

import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MacchangerFragment extends Fragment {

    private static final String TAG = "MacchangerFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static int lastSelectedIfacePosition = 0;
    private static HashMap<String, String> iFaceAndMacHashMap = new HashMap<>();
    private Spinner interfaceSpinner;
    private Spinner macModeSpinner;
    private Button changeMacButton;
    private Button setHostNameButton;
    private Button resetMacButton;
    private Button regenerateMacButton;
    private Button clearMacButton;
    private TextInputEditText netHostNameEditText;
    private EditText mac1;
    private EditText mac2;
    private EditText mac3;
    private EditText mac4;
    private EditText mac5;
    private EditText mac6;
    private TextView currentMacTextView;
    private Button reloadImageButton;
    private Context context;
    private Activity activity;

    private Button setKHost;
    private TextInputEditText KHost;

    public MacchangerFragment() {

    }

    public static MacchangerFragment newInstance(int sectionNumber) {
        MacchangerFragment fragment = new MacchangerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    private static void getIfaceAndMacAddr() {
        if (iFaceAndMacHashMap.size() != 0) iFaceAndMacHashMap.clear();
        try {
            List<NetworkInterface> allIface = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface iface : allIface) {
                byte[] macBytes = iface.getHardwareAddress();
                if (macBytes == null) {
                    continue;
                }
                StringBuilder macaddrStringBuilder = new StringBuilder();
                for (byte b : macBytes) {
                    macaddrStringBuilder.append(String.format("%02X:", b));
                }

                if (macaddrStringBuilder.length() > 0) {
                    macaddrStringBuilder.deleteCharAt(macaddrStringBuilder.length() - 1);
                }
                iFaceAndMacHashMap.put(iface.getName().toLowerCase(), macaddrStringBuilder.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        context = getContext();
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.macchanger, container, false);
        interfaceSpinner = rootView.findViewById(R.id.f_macchanger_interface_opts_spr);
        macModeSpinner = rootView.findViewById(R.id.f_macchanger_mode_opts_spr);
        changeMacButton = rootView.findViewById(R.id.f_macchanger_set_mac_btn);
        setHostNameButton = rootView.findViewById(R.id.f_macchanger_setHostname_btn);
        resetMacButton = rootView.findViewById(R.id.f_macchanger_reset_mac_btn);
        netHostNameEditText = rootView.findViewById(R.id.f_macchanger_phone_name_et);
        currentMacTextView = rootView.findViewById(R.id.f_macchanger_currMac_tv);
        reloadImageButton = rootView.findViewById(R.id.f_macchanger_reloadMAC_imgbtn);
        regenerateMacButton = rootView.findViewById(R.id.f_macchanger_regenerate_mac_btn);
        clearMacButton = rootView.findViewById(R.id.f_macchanger_clear_mac_btn);
        mac1 = rootView.findViewById(R.id.f_macchanger_mac1_et);
        mac2 = rootView.findViewById(R.id.f_macchanger_mac2_et);
        mac3 = rootView.findViewById(R.id.f_macchanger_mac3_et);
        mac4 = rootView.findViewById(R.id.f_macchanger_mac4_et);
        mac5 = rootView.findViewById(R.id.f_macchanger_mac5_et);
        mac6 = rootView.findViewById(R.id.f_macchanger_mac6_et);

        setKHost = rootView.findViewById(R.id.f_macchanger_setKHostname_btn);
        KHost = rootView.findViewById(R.id.f_macchanger_khost_et);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getIfaceAndMacAddr();
        setHostNameEditText();
        setSetHostnameButton();
        setIfaceSpinner();
        setMacModeSpinner();
        setReloadImageButton();
        setChangeMacButton();
        setResetMacButton();
        setRegenerateMacButton();
        setClearMacButton();

        setKHostName();
        getKHostName();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        interfaceSpinner = null;
        macModeSpinner = null;
        changeMacButton = null;
        setHostNameButton = null;
        resetMacButton = null;
        regenerateMacButton = null;
        clearMacButton = null;
        netHostNameEditText = null;
        mac1 = null;
        mac2 = null;
        mac3 = null;
        mac4 = null;
        mac5 = null;
        mac6 = null;
        currentMacTextView = null;
        //currentHostNameTextView = null;
        reloadImageButton = null;
    }

    private void genRandomMACAddress() {
        SecureRandom random = new SecureRandom();
        byte[] macAddr = new byte[6];
        random.nextBytes(macAddr);
        //the second-least-significant bit and the least-significant bit of the first octet of the address must be 1 and 0 respectively
        mac1.setText(String.format("%02x", ((macAddr[0] & 0xfc) | 0x2)));
        mac2.setText(String.format("%02x", macAddr[1]));
        mac3.setText(String.format("%02x", macAddr[2]));
        mac4.setText(String.format("%02x", macAddr[3]));
        mac5.setText(String.format("%02x", macAddr[4]));
        mac6.setText(String.format("%02x", macAddr[5]));
    }

    private void setHostNameEditText() {
        MacchangerAsyncTask macchangerAsyncTask = new MacchangerAsyncTask(MacchangerAsyncTask.GETHOSTNAME);
        macchangerAsyncTask.setListener(new MacchangerAsyncTask.MacchangerAsyncTaskListener() {
            @Override
            public void onAsyncTaskPrepare() {}

            @Override
            public void onAsyncTaskFinished(Object result) {
                if (result != null) {
                    netHostNameEditText.setText(result.toString());
                } else {
                    netHostNameEditText.setText("");
                }
            }
        });
        macchangerAsyncTask.execute();
    }

    private void setSetHostnameButton() {
        setHostNameButton.setOnClickListener(v -> {
            MacchangerAsyncTask macchangerAsyncTask = new MacchangerAsyncTask(MacchangerAsyncTask.SETHOSTNAME);
            macchangerAsyncTask.setListener(new MacchangerAsyncTask.MacchangerAsyncTaskListener() {
                @Override
                public void onAsyncTaskPrepare() {}

                @Override
                public void onAsyncTaskFinished(Object result) {
                    NhPaths.showSnack(getView(), "Android Hostname is set to " + netHostNameEditText.getText().toString(), 1);
                    setHostNameEditText();
                }
            });
            macchangerAsyncTask.execute(netHostNameEditText.getText().toString());
        });
    }

    private void setIfaceSpinner() {
        List<String> keys = new ArrayList<>(iFaceAndMacHashMap.keySet());
        Collections.sort(keys, Collections.reverseOrder());
        String[] iFaceStrings = keys.toArray(new String[0]);
        ArrayAdapter<String> iFaceArrayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, iFaceStrings);
        iFaceArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        interfaceSpinner.setAdapter(iFaceArrayAdapter);
        interfaceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                lastSelectedIfacePosition = position;
                currentMacTextView.setText(iFaceAndMacHashMap.get(interfaceSpinner.getSelectedItem().toString().toLowerCase()));
                changeMacButton.setText(getString(R.string.changeMAC) + " " + interfaceSpinner.getSelectedItem().toString().toUpperCase());
                resetMacButton.setText(getString(R.string.resetMAC) + " " + interfaceSpinner.getSelectedItem().toString().toUpperCase());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setMacModeSpinner() {
        if (macModeSpinner.getSelectedItemPosition() == 0) {
            regenerateMacButton.setVisibility(View.VISIBLE);
            clearMacButton.setVisibility(View.GONE);
        } else {
            regenerateMacButton.setVisibility(View.GONE);
            clearMacButton.setVisibility(View.VISIBLE);
        }
        macModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    regenerateMacButton.setVisibility(View.VISIBLE);
                    clearMacButton.setVisibility(View.GONE);
                    genRandomMACAddress();
                } else if (position == 1) {
                    regenerateMacButton.setVisibility(View.GONE);
                    clearMacButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setRegenerateMacButton() {
        regenerateMacButton.setOnClickListener(v -> genRandomMACAddress());
    }

    private void setClearMacButton() {
        clearMacButton.setOnClickListener(v -> {
            mac1.setText("");
            mac2.setText("");
            mac3.setText("");
            mac4.setText("");
            mac5.setText("");
            mac6.setText("");
        });
    }

    private void setReloadImageButton() {
        reloadImageButton.setOnClickListener(v -> {
            getIfaceAndMacAddr();
            List<String> keys = new ArrayList<>(iFaceAndMacHashMap.keySet());
            Collections.sort(keys, Collections.reverseOrder());
            String[] iFaceStrings = keys.toArray(new String[0]);
            ArrayAdapter<String> iFaceArrayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, iFaceStrings);
            iFaceArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            interfaceSpinner.setAdapter(iFaceArrayAdapter);
            interfaceSpinner.setSelection(lastSelectedIfacePosition);
        });
    }

    private void setResetMacButton() {
        resetMacButton.setOnClickListener(v -> {
            MacchangerAsyncTask macchangerAsyncTask = new MacchangerAsyncTask(MacchangerAsyncTask.GETORIGINMAC);
            macchangerAsyncTask.setListener(new MacchangerAsyncTask.MacchangerAsyncTaskListener() {
                @Override
                public void onAsyncTaskPrepare() {

                }

                @Override
                public void onAsyncTaskFinished(Object result) {
                    AlertDialog.Builder adb = new AlertDialog.Builder(activity);
                    final AlertDialog ad = adb.create();
                    String originalMac = result.toString();
                    ad.setTitle("Warning!");
                    ad.setMessage("Are you sure to change the " + interfaceSpinner.getSelectedItem().toString().toLowerCase() +
                            "'s MAC address back to the original MAC address: " + originalMac + " ?");
                    ad.setButton(Dialog.BUTTON_POSITIVE, "YES", (dialog, which) -> {
                        ad.dismiss();
                        AlertDialog.Builder adb1 = new AlertDialog.Builder(activity);
                        final AlertDialog ad1 = adb1.create();
                        ad1.setCancelable(false);
                        ad1.setMessage("Changing MAC address on " + interfaceSpinner.getSelectedItem().toString().toLowerCase() + ". Please wait..");

                        MacchangerAsyncTask macchangerAsyncTask1 = new MacchangerAsyncTask(MacchangerAsyncTask.SETMAC);
                        macchangerAsyncTask1.setListener(new MacchangerAsyncTask.MacchangerAsyncTaskListener() {
                            @Override
                            public void onAsyncTaskPrepare() {
                                ad1.show();
                            }

                            @Override
                            public void onAsyncTaskFinished(Object result1) {
                                ad1.dismiss();
                                if ((int) result1 == 0) {
                                    NhPaths.showSnack(getView(), "The MAC address of " + interfaceSpinner.getSelectedItem().toString().toLowerCase() +
                                            " has been successfully changed to " + originalMac, 1);
                                } else {
                                    NhPaths.showSnack(getView(), "Failed changing the MAC address on " + interfaceSpinner.getSelectedItem().toString().toLowerCase(), 2);
                                }
                                reloadImageButton.performClick();
                            }
                        });
                        macchangerAsyncTask1.execute(interfaceSpinner.getSelectedItem().toString().toLowerCase(), result.toString());
                    });
                    ad.show();
                }
            });
            macchangerAsyncTask.execute(interfaceSpinner.getSelectedItem().toString().toLowerCase());
        });
    }

    private void setChangeMacButton() {
        changeMacButton.setOnClickListener(v -> {
            String macAddress = mac1.getText().toString().toLowerCase() + ":" +
                    mac2.getText().toString().toLowerCase() + ":" +
                    mac3.getText().toString().toLowerCase() + ":" +
                    mac4.getText().toString().toLowerCase() + ":" +
                    mac5.getText().toString().toLowerCase() + ":" +
                    mac6.getText().toString().toLowerCase();

            AlertDialog.Builder adb = new AlertDialog.Builder(activity);
            final AlertDialog ad = adb.create();
            ad.setCancelable(false);
            ad.setMessage("Changing MAC address on " + interfaceSpinner.getSelectedItem().toString().toLowerCase() + ". Please wait..");

            MacchangerAsyncTask macchangerAsyncTask = new MacchangerAsyncTask(MacchangerAsyncTask.SETMAC);
            macchangerAsyncTask.setListener(new MacchangerAsyncTask.MacchangerAsyncTaskListener() {
                @Override
                public void onAsyncTaskPrepare() {
                    ad.show();
                }

                @Override
                public void onAsyncTaskFinished(Object result) {
                    ad.dismiss();
                    if ((int) result == 0) {
                        NhPaths.showSnack(getView(), "The MAC address of " + interfaceSpinner.getSelectedItem().toString().toLowerCase() +
                                " has been successfully changed to " + macAddress, 1);
                    } else {
                        AlertDialog.Builder adb = new AlertDialog.Builder(activity);
                        final AlertDialog ad = adb.create();
                        ad.setTitle("Failed changing the MAC address on " + interfaceSpinner.getSelectedItem().toString().toLowerCase());
                        ad.setMessage("Please try to change to other MAC address as not all MAC address is valid to your system.");
                        ad.show();
                    }
                    reloadImageButton.performClick();
                }
            });
            macchangerAsyncTask.execute(interfaceSpinner.getSelectedItem().toString().toLowerCase(), macAddress);
        });
    }

    private void setKHostName(){
        setKHost.setOnClickListener(v -> {
            MacchangerAsyncTask macchangerAsyncTask = new MacchangerAsyncTask(MacchangerAsyncTask.SETKHOSTNAME);
            macchangerAsyncTask.setListener(new MacchangerAsyncTask.MacchangerAsyncTaskListener() {
                @Override
                public void onAsyncTaskPrepare() {}

                @Override
                public void onAsyncTaskFinished(Object result) {
                    NhPaths.showSnack(getView(), "Kernel Hostname is set to " + KHost.getText().toString(), 2);
                    getKHostName();
                }
            });
            if (KHost.getText().toString().isEmpty()) {
                NhPaths.showSnack(getView(), "Kernel Hostname must not be null" + KHost.getText().toString(), 2);
            } else {
                macchangerAsyncTask.execute(KHost.getText().toString());
            }
        });
    }

    private void getKHostName() {
        MacchangerAsyncTask macchangerAsyncTask = new MacchangerAsyncTask(MacchangerAsyncTask.GETKHOSTNAME);
        macchangerAsyncTask.setListener(new MacchangerAsyncTask.MacchangerAsyncTaskListener() {
            @Override
            public void onAsyncTaskPrepare() {}

            @Override
            public void onAsyncTaskFinished(Object result) {
                if (result != null) {
                    KHost.setText(result.toString());
                } else {
                    KHost.setText("");
                }
            }
        });
        macchangerAsyncTask.execute();
    }
}
