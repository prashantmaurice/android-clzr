package com.clozerr.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class UnusedOffersActivity extends ActionBarActivity {
    ArrayList<String> offerid = new ArrayList<>();
    ArrayList<String> caption = new ArrayList<>();
    ArrayList<String> description = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unused_offers);
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        final ListView listview=(ListView)findViewById(R.id.unusedoffers);
        final ArrayList<String> values = convertRowMyOffers(VendorActivity.detailsBundle.getString("Alloffers"));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.freebies_item_layout, R.id.freebiename, values);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), FreebieDescription.class);
                intent.putExtra("offerid",offerid.get(position));
                intent.putExtra("vendorid",VendorActivity.detailsBundle.getString("vendorId"));
                intent.putExtra("caption",caption.get(position));
                intent.putExtra("description",description.get(position));
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_unused_offers, menu);
        return true;
    }

    private ArrayList<String> convertRowMyOffers(String s){
        ArrayList<String> rowItems = new ArrayList<String>();

        MyOffer item = null;
        try {
            //Log.e(TAG, "json passed - " + s);
            JSONObject offerObject = new JSONObject(s);
            JSONArray array = offerObject.getJSONArray("offers");
            MyOffer.SXOfferExtras extras = null;
            for (int i = 0; i < array.length(); ++i) {
                offerObject = array.getJSONObject(i);
                extras = null;
                String type = offerObject.getString("type");
//                if (type.equalsIgnoreCase("SX"))
//                    extras = new MyOffer.SXOfferExtras(offerObject.getJSONObject("stampStatus").getInt("total"),
//                            offerObject.getDouble("billAmt"));
//                item = new MyOffer(type,
//                        offerObject.getString("image"),
//                        offerObject.getString("optionalImage"),
//                        offerObject.getString("caption"),
//                        offerObject.getString("description"),
//                        offerObject.getInt("stamps"),
//                        extras);
                //if((offerObject.getJSONObject("params").getBoolean("unlocked")==true) && (offerObject.getJSONObject("params").getBoolean("used")==false)) {
                    rowItems.add(offerObject.getString("caption"));
                    offerid.add(offerObject.getString("_id"));
                    caption.add(offerObject.getString("caption"));
                    description.add(offerObject.getString("description"));
                //}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowItems;
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
