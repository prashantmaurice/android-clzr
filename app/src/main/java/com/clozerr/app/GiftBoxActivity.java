package com.clozerr.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.clozerr.app.Utils.Router;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class GiftBoxActivity extends ActionBarActivity {
    private Toolbar toolbar;
    private GiftBoxRecyclerViewAdapter mMainPageAdapter;
    private RecyclerView mRecyclerView;
    public static String GIFTBOXDATA = "giftboxdatajson";
    Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gift_box);
        toolbar = (Toolbar) findViewById(R.id.toolbar_giftbox);
        mRecyclerView = (RecyclerView) findViewById(R.id.giftboxoffers);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mContext = this;
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setTitle("");
        }

        String jsonString = getIntent().getExtras().getString(GIFTBOXDATA);

        if(jsonString!=null) { //data is passed by previous activity
            try {
                JSONArray rewards = new JSONArray(jsonString);
                ArrayList<MyOffer> rowItems = new ArrayList<>();
                for (int i = 0; i < rewards.length(); i++) {
                    JSONObject obj = rewards.getJSONObject(i);
                    rowItems.add(MyOffer.parseFromServer(obj));
                }
                //Toast.makeText(getApplicationContext(),String.valueOf(rowItems.size()),Toast.LENGTH_SHORT).show();
                if (rowItems.size() == 0) {
                    GenUtils.showDebugToast(this,"Showing empty giftbox");
                    findViewById(R.id.alertgiftbox).setVisibility(View.VISIBLE);
                }
                mMainPageAdapter = new GiftBoxRecyclerViewAdapter(rowItems, mContext);
                mRecyclerView.setAdapter(mMainPageAdapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        else { //no data was passed, maybe this is the first activity

            new AsyncGet(this, Router.Homescreen.getVendorsGifts(), new AsyncGet.AsyncResult() {
                @Override
                public void gotResult(String s) {
                    try {
                        JSONArray rewards = new JSONArray(s);
                        ArrayList<MyOffer> rowItems = new ArrayList<>();
                        for (int i = 0; i < rewards.length(); i++) {
                            JSONObject obj = rewards.getJSONObject(i);
                            rowItems.add(MyOffer.parseFromServer(obj));
                        }
                        if (rowItems.size() == 0) {
                            findViewById(R.id.alertgiftbox).setVisibility(View.VISIBLE);
                        }
                        mMainPageAdapter = new GiftBoxRecyclerViewAdapter(rowItems, mContext);
                        mRecyclerView.setAdapter(mMainPageAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gift_box, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
