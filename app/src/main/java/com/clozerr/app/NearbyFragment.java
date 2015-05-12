package com.clozerr.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by srivatsan on 12/5/15.
 */
public class NearbyFragment extends Fragment {
    /*public static MyFragment getInstance(int Position){
        MyFragment myFragment=new MyFragment();
        Bundle args=new Bundle();
        args.getInt("position",Position);
        myFragment.setArguments(args);
        return myFragment;
    }*/
    Context c;
    public static String TOKEN = "";

    private RecyclerViewAdapter mMainPageAdapter;
    private ArrayList<CardModel> mMainCardsList;
    private RecyclerView.LayoutManager mLayoutManager;
    private EndlessRecyclerOnScrollListener mOnScrollListener;
    private int mOffset;
    private boolean mCardsLeft = true;
    private final int ITEMS_PER_PAGE = 7, INITIAL_LOAD_LIMIT = 8;


    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        View layout=inflater.inflate(R.layout.activity_nearby_fragment,container,false);
        final RecyclerView mRecyclerView = (RecyclerView) layout.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(c));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(c);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mOnScrollListener = new EndlessRecyclerOnScrollListener(
                (LinearLayoutManager)mLayoutManager) {
            @Override
            public void onLoadMore() {
                loadMoreItems();
            }
        };
        mRecyclerView.setOnScrollListener(mOnScrollListener);



        //startService(new Intent(this, LocationService.class));
        Home.lat = 13;
        Home.longi = 80.2;


        SharedPreferences status = c.getSharedPreferences("USER", 0);
        final String cards = status.getString("home_cards", "");
        if(!cards.equals("")){
            Log.e("Cached Card", cards);
            mMainCardsList = convertRow(cards);
            mMainPageAdapter = new RecyclerViewAdapter(mMainCardsList, c);
            mRecyclerView.setAdapter(mMainPageAdapter);
        } else {
            mOffset = 0;
            String url = "http://api.clozerr.com/vendor/get/near?latitude=" + Home.lat + "&longitude=" + Home.longi + "&access_token=" + TOKEN
                    + "&offset=" + mOffset + "&limit=" + INITIAL_LOAD_LIMIT;
            Log.e("url", url);
            new AsyncGet(c, url, new AsyncGet.AsyncResult() {
                @Override
                public void gotResult(String s) {
                    Log.e("result",s);
                    if(s==null) {
                        Toast.makeText(c, "No internet connection", Toast.LENGTH_SHORT).show();
                    }

                    ArrayList<CardModel> CardList = convertRow(s);
                    if (CardList.size() != 0) {
                        mMainCardsList = CardList;
                        mMainPageAdapter = new RecyclerViewAdapter(mMainCardsList, c);
                        mRecyclerView.setAdapter(mMainPageAdapter);
                        final SharedPreferences.Editor editor = c.getSharedPreferences("USER", 0).edit();
                        editor.putString("home_cards", s);
                        editor.apply();
                        Log.e("app", "editing done");
                    }
                    else {
                        Log.d("app", "no cards to show");
                        mCardsLeft = false;
                    }
                }
            });
        }


        new MyLocation().getLocation(c, new MyLocation.LocationResult(){
            @Override
            public void gotLocation (Location location) {
                Log.e("location stuff","Location Callback called.");
                try{
                    Home.lat=location.getLatitude();
                    Home.longi=location.getLongitude();
                    Log.e("lat", Home.lat + "");
                    Log.e("long", Home.longi + "");
                }catch (Exception e){
                    e.printStackTrace();
                }
                SharedPreferences status = c.getSharedPreferences("USER", 0);
                TOKEN = status.getString("token", "");
                String url;
                mOffset = 0;
                if(!TOKEN.equals(""))
                    url = "http://api.clozerr.com/vendor/get/near?latitude="+Home.lat+"&longitude="+Home.longi+"&access_token="+TOKEN
                            + "&offset=" + mOffset + "&limit=" + INITIAL_LOAD_LIMIT;
                else
                    url = "http://api.clozerr.com/vendor/get/near?latitude="+Home.lat+"&longitude="+Home.longi
                            + "&offset=" + mOffset + "&limit=" + INITIAL_LOAD_LIMIT;
                Log.e("url", url);

                new AsyncGet(c, url, new AsyncGet.AsyncResult() {
                    @Override
                    public void gotResult(String s) {
                        ArrayList<CardModel> CardList = convertRow(s);
                        if(CardList.size()!=0){
                            mMainCardsList = CardList;
                            mMainPageAdapter = new RecyclerViewAdapter(mMainCardsList, c);
                            mRecyclerView.setAdapter(mMainPageAdapter);

                            final SharedPreferences.Editor editor = c.getSharedPreferences("USER", 0).edit();
                            editor.putString("home_cards", s);
                            editor.apply();
                        }
                        else {
                            mCardsLeft = false;
                        }
                    }
                });
            }

        });

        return layout;
    }
    public void loadMoreItems() {
        Log.e("load", "in loadMoreItems()");
        if (mCardsLeft) {
            mOffset += (mOffset == 0) ? INITIAL_LOAD_LIMIT : ITEMS_PER_PAGE;
            String url = "";
            if (!TOKEN.equals(""))
                url = "http://api.clozerr.com/vendor/get/near?latitude=" + Home.lat + "&longitude=" + Home.longi + "&access_token=" + TOKEN
                        + "&offset=" + mOffset + "&limit=" + ITEMS_PER_PAGE;
            else
                url = "http://api.clozerr.com/vendor/get/near?latitude=" + Home.lat + "&longitude=" + Home.longi
                        + "&offset=" + mOffset + "&limit=" + ITEMS_PER_PAGE;
            Log.e("url", url);
            new AsyncGet(c, url, new AsyncGet.AsyncResult() {
                @Override
                public void gotResult(String s) {
                    Log.e("result", s);
                    if (s == null) {
                        Toast.makeText(c, "No internet connection", Toast.LENGTH_SHORT).show();
                    }
                    ArrayList<CardModel> CardList = convertRow(s);
                    if (CardList.size() != 0) {
                        mMainCardsList.addAll(convertRow(s));
                        final SharedPreferences.Editor editor = c.getSharedPreferences("USER", 0).edit();
                        editor.putString("home_cards", s);
                        editor.apply();
                        Log.e("app", "editing done");
                        mMainPageAdapter.notifyDataSetChanged();
                        //Toast.makeText(getApplicationContext(), "More items ready", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("app", "no cards to show");
                        mCardsLeft = false;
                        mOffset = mMainCardsList.size();
                    }
                }
            });
        }
    }
    private ArrayList<CardModel> convertRow(String s) {
        ArrayList<CardModel> rowItems = new ArrayList<>();
        JSONArray array;
        try {
            array = new JSONArray(s);
            for(int i = 0 ; i < array.length() ; i++){
                String phonenumber;
                try {
                    phonenumber = array.getJSONObject(i).getString("phonenumber");
                }
                catch(Exception e)
                {
                    phonenumber="0123456789";
                }
                String vendorDescription;
                try {
                    vendorDescription = array.getJSONObject(i).getString("description");
                }
                catch(Exception e)
                {
                    vendorDescription="No Restaurant Description Available Now";
                }
                Log.e("description", vendorDescription);
                CardModel item = new CardModel(
                        array.getJSONObject(i).getString("name"),
                        phonenumber, vendorDescription,
                        array.getJSONObject(i).getJSONArray("offers"),
                        array.getJSONObject(i).getJSONArray("location").getDouble(0),
                        array.getJSONObject(i).getJSONArray("location").getDouble(1),
                        array.getJSONObject(i).getString("image"),
                        array.getJSONObject(i).getString("fid"),array.getJSONObject(i).getString("_id"),0
                );
                rowItems.add(item);
            }
        } catch (Exception e) {
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