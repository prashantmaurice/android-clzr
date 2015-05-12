package com.clozerr.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class MyClubsFragment extends Fragment {
    Context c;
    View layout;
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.activity_my_clubs_fragment, container, false);
        final RecyclerView mRecyclerView = (RecyclerView) layout.findViewById(R.id.sliding_list);
        mRecyclerView.setLayoutManager(new GridLayoutManager(c,2));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        Log.e("app", "in slidingmycards; set recycler");


                SharedPreferences status = c.getSharedPreferences("USER", 0);
                String TOKEN = status.getString("token", "");

                String urlVisited = "http://api.clozerr.com/vendor/get/visitedV2?access_token="+TOKEN;
                Log.e("urlslide", urlVisited);

                new AsyncGet(c, urlVisited , new AsyncGet.AsyncResult() {
                    @Override
                    public void gotResult(String s) {
                        //  t1.setText(s);

                        Log.e("resultSlide", s);

                        MyCardRecyclerViewAdapter Cardadapter = new MyCardRecyclerViewAdapter(convertRowMyCard(s), c);
                        mRecyclerView.setAdapter(Cardadapter);
                        try
                        {
                            Tracker t = ((Analytics) c.getApplicationContext()).getTracker(Analytics.TrackerName.APP_TRACKER);

                            t.setScreenName("MyCards");

                            t.send(new HitBuilders.AppViewBuilder().build());
                        }
                        catch(Exception  e)
                        {
                            Toast.makeText(c, "Error" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        if(s==null) {
                            Toast.makeText(c,"No internet connection",Toast.LENGTH_SHORT).show();
                        }
                        //l1.setAdapter(adapter);
                    }
                });


        return layout;
    }
    private ArrayList<CardModel> convertRowMyCard(String s) {
        ArrayList<CardModel> rowItems = new ArrayList<>();
        JSONObject temp;
        JSONArray array;
        try {
            //Log.e("stringfunction", s);
            Log.e("stringfunction", "processing..");
            temp = new JSONObject(s);
            array = temp.getJSONArray("data");

            ImageView loyaltyempty=(ImageView)layout.findViewById(R.id.loyaltyempty);
            if(array.length()==0){
                Log.e("arrayLength", array.length()+"");
                loyaltyempty.setVisibility(View.VISIBLE);
            }
            for(int i = 0 ; i < array.length() ; i++){
                loyaltyempty.setVisibility(View.GONE);
                Log.e("stringfunction", "processing..");
                CardModel item = new CardModel(
                        array.getJSONObject(i).getString("name"),
                        array.getJSONObject(i).getString("phone"),
                        array.getJSONObject(i).getString("description"),
                        array.getJSONObject(i).getJSONArray("offers"),
                        array.getJSONObject(i).getJSONArray("location").getDouble(0),
                        array.getJSONObject(i).getJSONArray("location").getDouble(1),
                        array.getJSONObject(i).getString("image"),
                        array.getJSONObject(i).getString("fid"),
                        array.getJSONObject(i).getString("_id"),
                        array.getJSONObject(i).getInt("stamps")
                );
                Log.e("stringfunction", "processed");
                rowItems.add(item);
            }
        } catch (Exception e) {
            Log.e("uhoh",e.getMessage());
            e.printStackTrace();
        }
        return rowItems;
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        c=activity;
    }
}
