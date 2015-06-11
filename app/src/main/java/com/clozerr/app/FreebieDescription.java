package com.clozerr.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


public class FreebieDescription extends ActionBarActivity {
    String offerid="";
    String vendorid="",caption="",description="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_freebie_description);
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Intent intent = getIntent();
        try {
            offerid += intent.getStringExtra("offerid");
        }catch (Exception e){

        }
        try {
            vendorid += intent.getStringExtra("vendorid");
        }catch (Exception e){

        }
        try {
            caption += intent.getStringExtra("caption");
        }catch (Exception e){

        }
        try {
            description += intent.getStringExtra("description");
        }catch (Exception e){

        }
        if(!caption.equals("")){
            ((TextView)findViewById(R.id.caption)).setText(caption);
            ((TextView)findViewById(R.id.title)).setText(caption);
        }
        if(!description.equals("")){
            ((TextView)findViewById(R.id.description)).setText(description);
        }
        findViewById(R.id.useit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://api.clozerr.com/v2/vendor/offers/checkin&access_token="+Home.TOKEN+"&offer_id="+offerid+"&vendor_id="+vendorid;
                Log.d(url,"cheked in");
                new AsyncGet(getApplicationContext(), url, new AsyncGet.AsyncResult() {
                    @Override
                    public void gotResult(String s) {
                        try {
                            JSONObject jsonObject = new JSONObject(s);
                            if(jsonObject.getBoolean("result")){
                                Toast.makeText(getApplicationContext(),"Successfully Checked In",Toast.LENGTH_SHORT).show();
                            }
                            else
                                Toast.makeText(getApplicationContext(),"Checked In Failed",Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_freebie_description, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
