package material.hunter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import material.hunter.utils.ShellExecuter;

public class AireplayNgFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private final ShellExecuter exe = new ShellExecuter();
    private Context context;
    private Activity activity;

    public static AireplayNgFragment newInstance(int sectionNumber) {
        AireplayNgFragment fragment = new AireplayNgFragment();
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
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.deauth, container, false);
        return rootView;
    }
}
