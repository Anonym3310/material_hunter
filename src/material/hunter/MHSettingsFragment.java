package material.hunter;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Objects;

import material.hunter.utils.NhPaths;
import material.hunter.utils.SharePrefTag;

public class MHSettingsFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";

    public static MHSettingsFragment newInstance(int sectionNumber) {
        MHSettingsFragment f = new MHSettingsFragment();
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
        final View rootView = inflater.inflate(R.layout.mhsettings, container, false);
        final SharedPreferences oa = Objects.requireNonNull(getActivity()).getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        final SwitchMaterial o = rootView.findViewById(R.id.settings_rob);
        o.setChecked(oa.getBoolean(SharePrefTag.BOOT_RECIVIE, true));
        o.setOnClickListener(view -> {
            if (o.isChecked()) {
                oa.edit().putBoolean(SharePrefTag.BOOT_RECIVIE, true).apply();
            }else{
                oa.edit().putBoolean(SharePrefTag.BOOT_RECIVIE, false).apply();
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
                oa.edit().putInt(SharePrefTag.BACKGROUND_ALPHA_LEVEL, cb).apply();
                NhPaths.showSnack(rootView, getString(R.string.mh_sett_need_restart), 1);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        final Button cd = rootView.findViewById(R.id.settings__clear_data);
        cd.setOnClickListener(view -> ((ActivityManager) Objects.requireNonNull(getContext()).getSystemService(Context.ACTIVITY_SERVICE)).clearApplicationUserData());
        return rootView;
    }
}