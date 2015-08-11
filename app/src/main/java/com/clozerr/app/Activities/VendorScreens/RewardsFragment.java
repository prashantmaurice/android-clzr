package com.clozerr.app.Activities.VendorScreens;

/**
 * Created by Adarsh on 20-05-2015.
 */

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.clozerr.app.AsyncGet;
import com.clozerr.app.Models.RewardsObject;
import com.clozerr.app.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class RewardsFragment extends Fragment {

    Context c;
    FrameLayout layout;
    RecyclerView listview;
    ArrayList<RewardsObject> rewardsObjects = new ArrayList<>();
    RewardsFragmentAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        layout = (FrameLayout) inflater.inflate(R.layout.activity_freebies_fragment, container, false);
        listview=(RecyclerView)layout.findViewById(R.id.freebietypes);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        listview.setLayoutManager(linearLayoutManager);
        adapter = new RewardsFragmentAdapter(rewardsObjects, getActivity());
        listview.setAdapter(adapter);

        final String rewardsurl = "http://api.clozerr.com/v2/vendor/offers/rewardspage/?vendor_id=" + VendorActivity.vendorId + "&access_token=" + VendorActivity.TOKEN;
        new AsyncGet(c, rewardsurl, new AsyncGet.AsyncResult() {
            @Override

            public void gotResult(String s) {
                Log.d("rewardsurl", rewardsurl);
                try {
                    JSONObject result = new JSONObject(s);
                    rewardsObjects.clear();
                    rewardsObjects.addAll(RewardsObject.decodeFromServer(result.getJSONArray("rewards")));

                    //add additional variables
                    for(RewardsObject reward : rewardsObjects){
                        reward.vendorId = VendorActivity.vendorId;
                    }
                } catch (JSONException e) {e.printStackTrace();}
                adapter.notifyDataSetChanged();
            }
        }, true);

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

