package com.clozerr.app.Activities.VendorScreens;

/**
 * Created by Aravind.S on 20-05-2015.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.clozerr.app.MainApplication;
import com.clozerr.app.R;


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
            addPreferencesFromResource(R.xml.vendor_preferences);
            Preference button = (Preference)findPreference("shortcut");
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference)
                { //code for what you want it to do return true;
                    Intent shortcutIntent = new Intent(getActivity(), VendorActivity.class);
                    shortcutIntent.putExtra("vendor_id",VendorActivity.vendorId);
//                    SharedPreferences example = getActivity().getSharedPreferences("USER", 0);
//                    SharedPreferences.Editor editor = example.edit();
//                    editor.putString("latitude", Home.lat+"");
//                    editor.putString("longitude", Home.longi+"");
//                    editor.apply();
                    MainApplication.getInstance().data.userMain.latitude = MainApplication.getInstance().location.getLatitude()+"";
                    MainApplication.getInstance().data.userMain.longitude = MainApplication.getInstance().location.getLongitude()+"";
                    MainApplication.getInstance().data.userMain.saveUserDataLocally();

                    shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    Intent addIntent = new Intent();
                    addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, VendorActivity.vendorTitle);

                    ImageView image = VendorActivity.logoView;
                    Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();


                    addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap);
                    addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
                    addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                    getActivity().sendBroadcast(addIntent);
                    Toast.makeText(getActivity(), "Pinned To Home Screen", Toast.LENGTH_SHORT).show();
                    return true;
                } });


        }

        @Override
        public void onDestroy() {
            super.onDestroy();

        }
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            /*if (key.equals(getString(R.string.beacon_detection))) {
                if (sharedPreferences.getBoolean(key, true))
                    BeaconFinderService.allowScanning(getActivity());
                else
                    BeaconFinderService.disallowScanning(getActivity());
            }*/
            if(key.equals("shortcut")){

            }
        }

    }
}

