package com.offsec.nethunter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.SharePrefTag;

public class SettingsFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";

    public static SettingsFragment newInstance(int sectionNumber) {
        SettingsFragment f = new SettingsFragment();
        Bundle a = new Bundle();
        a.putInt(ARG_SECTION_NUMBER, sectionNumber);
        f.setArguments(a);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.settings, container, false);
        final SharedPreferences oa = getActivity().getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        final SwitchMaterial o = rootView.findViewById(R.id.settings_rob);
        if (oa.getBoolean(SharePrefTag.BOOT_RECIVIE, true)) {
            o.setChecked(true);
        } else {
            o.setChecked(false);
        }
        o.setOnClickListener(view -> {
            if (o.isChecked()) {
                oa.edit().putBoolean(SharePrefTag.BOOT_RECIVIE, true).commit();
            }else{
                oa.edit().putBoolean(SharePrefTag.BOOT_RECIVIE, false).commit();
            }
        });
        final SeekBar c = rootView.findViewById(R.id.settings_swtl_bar);
        int ca = oa.getInt(SharePrefTag.BACKGROUND_ALPHA_LEVEL,0);
        int cb = ca/10;
        c.setProgress(cb);
        c.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int ca = c.getProgress();
                int cb = ca*10;
                oa.edit().putInt(SharePrefTag.BACKGROUND_ALPHA_LEVEL, cb).commit();
                Snackbar.make(rootView, "Need restart.", Snackbar.LENGTH_LONG).show();
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        return rootView;
    }
}