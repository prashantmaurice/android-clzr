package com.clozerr.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Girish on 1/7/2015.
 */

public class mycards_offers extends ActionBarActivity {

String TOKEN="";
    String vendorid="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mycards_offerlist);
        SharedPreferences status = getSharedPreferences("USER", 0);
        TOKEN = status.getString("token", "");
        Intent offerIntent = getIntent();
        vendorid=offerIntent.getStringExtra("VendorId");
        final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.list1);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);

        Intent callingIntent = getIntent();
        ImageView restImage=(ImageView)findViewById(R.id.itemImageView1);
        Ion.with(restImage)
              //  .placeholder(R.drawable.call)
              //  .error(R.drawable.bat)
                        //    .animateLoad(spinAnimation)
                        //    .animateIn(fadeInAnimation)
                .load(callingIntent.getStringExtra("ImageUrl"));
        TextView Name=(TextView)findViewById(R.id.Name);
        Name.setText(callingIntent.getStringExtra("Name"));
        TextView Distance=(TextView)findViewById(R.id.Distance);
        Distance.setText(callingIntent.getStringExtra("Distance"));
        String url="http://api.clozerr.com/vendor/get?vendor_id="+vendorid+"&access_token="+TOKEN;
        new AsyncGet(this, url, new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                //  t1.setText(s);
                Log.i("result", s);
/*
                RecyclerViewAdapter1 adapter = new RecyclerViewAdapter1(convertRow(s), mycards_offers.this);
                mRecyclerView.setAdapter(adapter);*/


            }
        });
    }
    private List<mycards_cardmodel> convertRow(String s) {
        List<mycards_cardmodel> rowItems;
        rowItems = new ArrayList<mycards_cardmodel>();
        JSONObject array = null;
        try {
            array = new JSONObject(s);
            for(int i = 0 ; i < array.getJSONArray("offers").length() ; i++){
                Log.d("image",array.getJSONArray("offers").getJSONObject(i).getString("stamps"));
                mycards_cardmodel item = new mycards_cardmodel(
                        "Stamp : "+ array.getJSONArray("offers").getJSONObject(i).getString("stamps"),
                        array.getJSONArray("offers").getJSONObject(i).getString("caption")
                );
                rowItems.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowItems;
    }
}