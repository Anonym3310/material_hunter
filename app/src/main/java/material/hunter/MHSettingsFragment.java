package material.hunter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.SeekBar;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.ArrayList;
import java.util.Objects;
import material.hunter.utils.PathsUtil;
import mirivan.TransparentQ;

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

  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    final View rootView = inflater.inflate(R.layout.mhsettings, container, false);
    final SharedPreferences oa = getActivity().getSharedPreferences("material.hunter", Context.MODE_PRIVATE);

    final SwitchMaterial o = rootView.findViewById(R.id.settings_rob);
    o.setChecked(oa.getBoolean("mh_runonboot_enabled", true));
    o.setOnClickListener(
        view -> {
          if (o.isChecked()) {
            oa.edit().putBoolean("mh_runonboot_enabled", true).apply();
          } else {
            oa.edit().putBoolean("mh_runonboot_enabled", false).apply();
          }
        });

    final SwitchMaterial swb = rootView.findViewById(R.id.settings_swb);
    swb.setChecked(oa.getBoolean("show_wallpaper", false));

    final SeekBar bal = rootView.findViewById(R.id.settings_bal);
    if (swb.isChecked()) bal.setEnabled(true);
    else bal.setEnabled(false);

    int ca = oa.getInt("background_alpha_level", 0);
    TypedValue typedValue = new TypedValue();
    getActivity().getTheme().resolveAttribute(R.attr.colorSurface, typedValue, true);
    String color =
        Integer.toHexString(
                ContextCompat.getColor(AppNavHomeActivity.context, typedValue.resourceId))
            .substring(2);
    bal.setProgress(ca / 10);
    bal.setOnSeekBarChangeListener(
        new SeekBar.OnSeekBarChangeListener() {
          @Override
          public void onProgressChanged(SeekBar seekBar, int i, boolean b) {}

          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {}

          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {
            oa.edit().putInt("background_alpha_level", seekBar.getProgress() * 10).apply();
            /*getActivity()
                .getWindow()
                .getDecorView()
                .setBackground(
                    new ColorDrawable(
                        Color.parseColor(TransparentQ.p2c(color, seekBar.getProgress() * 10))));*/
            PathsUtil.showSnack(rootView, "Need restart!", false);
          }
        });

    swb.setOnClickListener(
        view -> {
          if (swb.isChecked()) {
            oa.edit().putBoolean("show_wallpaper", true).apply();
            bal.setEnabled(true);
          } else {
            oa.edit().putBoolean("show_wallpaper", false).apply();
            bal.setEnabled(false);
          }
          PathsUtil.showSnack(getView(), "Need restart!", false);
        });

    final SwitchMaterial msdp = rootView.findViewById(R.id.settings_msdp);
    if (oa.getString("sdcard_part", "") != "") {
      msdp.setChecked(true);
    }
    msdp.setOnClickListener(
        view -> {
          if (msdp.isChecked()) {
            oa.edit().putString("sdcard_part", "sdcard").apply();
          } else {
            oa.edit().putString("sdcard_part", "").apply();
          }
        });

    final SwitchMaterial msysp = rootView.findViewById(R.id.settings_msysp);
    if (oa.getString("system_part", "") != "") {
      msysp.setChecked(true);
    }
    msysp.setOnClickListener(
        view -> {
          if (msysp.isChecked()) {
            oa.edit().putString("system_part", "system").apply();
          } else {
            oa.edit().putString("system_part", "").apply();
          }
        });

    final SwitchMaterial sts = rootView.findViewById(R.id.settings_sts);
    sts.setChecked(oa.getBoolean("show_timestamp", false));
    sts.setOnClickListener(
        view -> {
          if (sts.isChecked()) {
            oa.edit().putBoolean("show_timestamp", true).apply();
          } else {
            oa.edit().putBoolean("show_timestamp", false).apply();
          }
        });

    final SwitchMaterial mih = rootView.findViewById(R.id.settings_mih);
    mih.setChecked(oa.getBoolean("magisk_info_hide", false));
    mih.setOnClickListener(
        view -> {
          if (mih.isChecked()) {
            oa.edit().putBoolean("magisk_info_hide", true).apply();
          } else {
            oa.edit().putBoolean("magisk_info_hide", false).apply();
          }
        });

    final AutoCompleteTextView them = rootView.findViewById(R.id.settings_apptheme);
    final int theme = oa.getInt("theme", 0);
    if (theme == 0) {
      them.setText("Night");
    } else if (theme == 1) {
      them.setText("Day");
    } else {
      them.setText("Follow system");
    }
    final ArrayList<String> themes = new ArrayList<>();
    themes.add("Night");
    themes.add("Day");
    themes.add("Follow system");
    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.mhspinner, themes);
    them.setAdapter(adapter);
    them.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View v, int i, long l) {
          if (i == 0) {
            oa.edit().putInt("theme", 0).apply();
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
          } else if (i == 1) {
            oa.edit().putInt("theme", 1).apply();
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
          } else {
            oa.edit().putInt("theme", 2).apply();
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
          }
        }
    });

    return rootView;
  }
}