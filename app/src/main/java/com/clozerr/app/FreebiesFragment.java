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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class FreebiesFragment extends Fragment {

    Context c;
    View layout;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.activity_freebies_fragment, container, false);
        final ListView listview=(ListView)layout.findViewById(R.id.freebietypes);
        final String[] values = new String[] { "Welcome Reward",
                "Facebook Ckeck-In Reward",
                "Happy Hour Reward",
                "Lucky Reward"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),R.layout.freebies_item_layout, R.id.freebiename, values);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        return layout;
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        c=activity;
    }
}

