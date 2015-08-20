package com.clozerr.app.Activities.VendorScreens.Subviews;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.clozerr.app.AsyncGet;
import com.clozerr.app.Handlers.LocalBroadcastHandler;
import com.clozerr.app.Models.RewardsObject;
import com.clozerr.app.R;
import com.clozerr.app.Utils.Router;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MyStampsFragment extends Fragment {

    Context c;
    FrameLayout layout;
    MyStampsFragmentAdapter adapter;

    //Data variables
    String vendorId;
    ArrayList<RewardsObject> rewardsObjects = new ArrayList<>();

    public static MyStampsFragment newInstance(String vendorId) {
        MyStampsFragment myFragment = new MyStampsFragment();
        myFragment.vendorId = vendorId;
        return myFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        layout = (FrameLayout) inflater.inflate(R.layout.activity_mystamps_fragment, container, false);
        final RecyclerView recyclerview=(RecyclerView)layout.findViewById(R.id.stampslist);
        recyclerview.setLayoutManager(new GridLayoutManager(c, 3));
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        recyclerview.setHasFixedSize(true);
        adapter = new MyStampsFragmentAdapter(rewardsObjects, getActivity());
        recyclerview.setAdapter(adapter);
        final TextView textView = (TextView) layout.findViewById(R.id.stampdesc);
//        textView.setText(VendorActivity.detailsBundle.getString("policy"));
        reloadData();


        //Add local broadcast listeners
        LocalBroadcastManager.getInstance(c).registerReceiver(mMyStampsUpdateReceiver,
                new IntentFilter(LocalBroadcastHandler.MYSTAMPS_CHANGED));

        return layout;
    }

    private BroadcastReceiver mMyStampsUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MYSTAMPS","onReceive : mMyStampsUpdateReceiverD");
            reloadData();
        }
    };

    public void reloadData(){
        final String url = Router.VendorScreen.getVendorOffersData(vendorId);
        new AsyncGet(c, url, new AsyncGet.AsyncResult() {
            @Override

            public void gotResult(String s) {
                Log.d("stampsUrl", url);
                try {
                    JSONObject result = new JSONObject(s);
                    rewardsObjects.clear();
                    rewardsObjects.addAll(RewardsObject.decodeFromServer(result.getJSONArray("offers")));
                    adapter.currentStamps = result.getInt("stamps");

                    //add additional variables if needed by adapter
                    for(RewardsObject reward : rewardsObjects){
                        reward.vendorId = vendorId;
                    }
                } catch (JSONException e) {e.printStackTrace();}
                adapter.notifyDataSetChangedCustom();
            }
        }, true);
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

    @Override
    public void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(c).unregisterReceiver(mMyStampsUpdateReceiver);
        super.onDestroy();
    }


    private void initViews() {
        layout.getForeground().mutate().setAlpha(0);
    }
}
