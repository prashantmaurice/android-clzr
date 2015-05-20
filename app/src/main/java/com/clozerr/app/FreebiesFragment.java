package com.clozerr.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;


public class FreebiesFragment extends Fragment {

    Context c;
    FrameLayout layout;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        layout = (FrameLayout) inflater.inflate(R.layout.activity_mystamps_fragment, container, false);
        final ListView listview=(ListView)layout.findViewById(R.id.freebietypes);
        final String[] values = new String[] { "Welcome Reward",
                "Facebook Ckeck-In Reward",
                "Happy Hour Reward",
                "Lucky Reward"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),R.layout.freebies_item_layout, R.id.freebiename, values);
        listview.setAdapter(adapter);
        return layout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        c=activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViews();
    }

    private void initViews() {
        layout.getForeground().mutate().setAlpha(0);
    }
}
