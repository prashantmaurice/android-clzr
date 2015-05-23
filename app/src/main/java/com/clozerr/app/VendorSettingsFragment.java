package com.clozerr.app;

/**
 * Created by Aravind.S on 20-05-2015.
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;


public class VendorSettingsFragment extends Fragment {

    Context c;
    FrameLayout layout;
    private SettingsFragment mSettingsFragment;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = (FrameLayout) inflater.inflate(R.layout.activity_vendor_settings_fragment, container, false);
        mSettingsFragment = new SettingsFragment();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
        preferences.registerOnSharedPreferenceChangeListener(mSettingsFragment);
        if (savedInstanceState == null) {
            getActivity().getFragmentManager().beginTransaction()
                    .add(R.id.container, mSettingsFragment)
                    .commit();
        }
        return layout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        c = activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViews();
    }

    private void initViews() {
        //layout.getForeground().mutate().setAlpha(0);
    }
    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        public SettingsFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

        }

        @Override
        public void onDestroy() {
            super.onDestroy();

        }
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.beacon_detection))) {
                if (sharedPreferences.getBoolean(key, true))
                    BeaconFinderService.allowScanning(getActivity());
                else
                    BeaconFinderService.disallowScanning(getActivity());
            }
        }
        // TODO implement turning off notifications
    }
}

