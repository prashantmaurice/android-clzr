package com.clozerr.app;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class PinnedOffersActivity extends ActionBarActivity {
    private Toolbar toolbar;
    private RecyclerView mRecyclerView;
    private UnusedOffersAdapter mMainPageAdapter;
    private Context c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinned_offers);
        toolbar = (Toolbar) findViewById(R.id.toolbar_pinnedoffers);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


            toolbar.setTitle("");
        }
        mRecyclerView = (RecyclerView) findViewById(R.id.giftboxoffers);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        c= this;
//        String TOKEN = getSharedPreferences("USER", 0).getString("token", "");
        String TOKEN = MainApplication.getInstance().tokenHandler.clozerrtoken;
        new AsyncGet(this, "http://api.clozerr.com/v2/user/pinned/list?access_token=" +TOKEN, new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                try {
                    JSONArray rewards = new JSONArray(s);
                    ArrayList<MyOffer> rowItems = new ArrayList<>();
                    for (int i = 0;i<rewards.length();i++)
                    {
                        JSONObject obj = rewards.getJSONObject(i);
                        String caption = obj.getString("caption");
                        String description = obj.getString("description");
                        int stamps = obj.getInt("stamps");
                        String type = obj.getString("type");
                        //String vendor_id = obj.getString("vendor_id");
                        MyOffer item = new MyOffer(type,"","",caption,description,stamps,false,true,null,obj.getString("_id"),false);
                        rowItems.add(item);
                    }
                    if(rowItems.size()==0)
                    {
                        findViewById(R.id.alertpinned).setVisibility(View.VISIBLE);
                    }
                    mMainPageAdapter = new UnusedOffersAdapter(rowItems,c);
                    mRecyclerView.setAdapter(mMainPageAdapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pinned_offers, menu);
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
