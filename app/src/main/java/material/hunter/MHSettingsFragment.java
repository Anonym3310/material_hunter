package material.hunter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.Objects;
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

    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.mhsettings, container, false);
        final SharedPreferences oa =
                Objects.requireNonNull(getActivity())
                        .getSharedPreferences("material.hunter", Context.MODE_PRIVATE);
        final SwitchMaterial o = rootView.findViewById(R.id.settings_rob);
        o.setChecked(oa.getBoolean(SharePrefTag.BOOT_RECIVIE, true));
        o.setOnClickListener(
                view -> {
                    if (o.isChecked()) {
                        oa.edit().putBoolean(SharePrefTag.BOOT_RECIVIE, true).apply();
                    } else {
                        oa.edit().putBoolean(SharePrefTag.BOOT_RECIVIE, false).apply();
                    }
                });
        return rootView;
    }
}
