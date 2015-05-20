package com.clozerr.app;

/**
 * Created by Adarsh on 20-05-2015.
 */

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class VendorHomeFragment extends Fragment {

    Context c;
    View layout;
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.activity_vendor_home_fragment, container, false);
        return layout;
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        c=activity;
    }
}

