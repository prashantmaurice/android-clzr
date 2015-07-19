package com.clozerr.app;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class FreebiesFragment extends Fragment {

    Context c;
    FrameLayout layout;
    RecyclerView listview;
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        layout = (FrameLayout) inflater.inflate(R.layout.activity_freebies_fragment, container, false);
        listview=(RecyclerView)layout.findViewById(R.id.freebietypes);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        listview.setLayoutManager(linearLayoutManager);
// if List<String> isnt specific enough:
        if(VendorActivity.Rewards.equals("")) {
            final String rewardsurl = "http://api.clozerr.com/v2/vendor/offers/rewardspage/?vendor_id=" + VendorActivity.vendorId + "&access_token=" + VendorActivity.TOKEN;
            new AsyncGet(c, rewardsurl, new AsyncGet.AsyncResult() {
                @Override
                public void gotResult(String s) {
                    Log.d("rewardsurl", rewardsurl);
                    VendorActivity.Rewards = s;
                    init();
                }
            }, true);
        }
        else init();
        /*listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        });*/
        return layout;
    }
    public void init(){
        ArrayList<RewardItem> al = new ArrayList<>();
        try {
            JSONObject obj = new JSONObject(VendorActivity.Rewards);
            JSONArray arr = obj.getJSONArray("rewards");
            for(int i=0;i<arr.length();i++){
                JSONObject jsonObject = arr.getJSONObject(i);

                // Make image optional.
                String image = "";
                if( jsonObject.has("image") )
                       image = jsonObject.getString("image");
                RewardItem rewardItem = new RewardItem(jsonObject.getJSONObject("params").getString("type"),jsonObject.getString("caption"),jsonObject.getString("description"),image,jsonObject.getString("_id"));
                al.add(rewardItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RewardsAdapter adapter = new RewardsAdapter(al,getActivity());
        listview.setAdapter(adapter);
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

