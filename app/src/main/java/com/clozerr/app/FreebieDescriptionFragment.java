package com.clozerr.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by srivatsan on 20/5/15.
 */
public class FreebieDescriptionFragment extends Fragment {
    View layout;
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.activity_freebie_description_fragment, container, false);

        return layout;
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }
}
