package com.clozerr.app;

/**
 * Created by Adarsh on 20-05-2015.
 */

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class FreebiesFragment extends Fragment {

    Context c;
    FrameLayout layout;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        layout = (FrameLayout) inflater.inflate(R.layout.activity_freebies_fragment, container, false);
        final ListView listview=(ListView)layout.findViewById(R.id.freebietypes);
        final String[] values = new String[] { "Welcome Reward",
                "Facebook Check-In Reward",
                "Happy Hour Reward",
                "Lucky Reward"
        };
        List<String> l = Arrays.asList(values);

// if List<String> isnt specific enough:
        ArrayList<String> al = new ArrayList<>(l);
        RewardsAdapter adapter = new RewardsAdapter(getActivity(),al);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position==1) {
                    final Dialog dialog = new Dialog(getActivity());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.facebook_layout);
                    dialog.show();

                    Button cancelButton = (Button) dialog.findViewById(R.id.cancel);
                    cancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Close dialog
                            dialog.dismiss();
                        }
                    });
                }
                else
                    startActivity(new Intent(getActivity(),FreebieDescription.class));
            }
        });
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

