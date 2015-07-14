package com.clozerr.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class UnusedOffersActivity extends ActionBarActivity {
    ArrayList<String> offerid = new ArrayList<>();
    ArrayList<String> caption = new ArrayList<>();
    ArrayList<String> description = new ArrayList<>();
    JSONArray arrpinned=null;
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
        final RecyclerView mRecyclerView=(RecyclerView) findViewById(R.id.unusedoffers);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        final ArrayList<MyOffer> values = convertRowMyOffers(VendorActivity.detailsBundle.getString("unlockedoffers"));
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.freebies_item_layout, R.id.freebiename, values);
        UnusedOffersAdapter adapter = new UnusedOffersAdapter(values,this);
        mRecyclerView.setAdapter(adapter);
        String urlPinning = "http://api.clozerr.com/v2/user/add/pinned?access_token=" + Home.TOKEN;
        new AsyncGet(getApplicationContext(), urlPinning, new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                try {
                    JSONObject obj = new JSONObject(s);
                    arrpinned=obj.getJSONArray("pinned");
                    //Toast.makeText(getActivity(),vendors.toString(),Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //  t1.setText(s);
                if (s == null) {
                    Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                }
                //l1.setAdapter(adapter);
            }
        });
/*        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Intent intent = new Intent(getApplicationContext(), FreebieDescription.class);
                intent.putExtra("offerid", offerid.get(position));
                intent.putExtra("vendorid", VendorActivity.detailsBundle.getString("vendorId"));
                intent.putExtra("caption", caption.get(position));
                intent.putExtra("description", description.get(position));
                startActivity(intent);
                final ImageView pin=(ImageView)view.findViewById(R.id.pinimage);
                pin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String urlPinning = "http://api.clozerr.com/v2/user/add/pinned?access_token=" + Home.TOKEN +"&offer_id="+offerid.get(position);
                        new AsyncGet(getApplicationContext(), urlPinning, new AsyncGet.AsyncResult() {
                            @Override
                            public void gotResult(String s) {
                                try {
                                    JSONObject obj = new JSONObject(s);
                                    //Toast.makeText(getActivity(),s,Toast.LENGTH_SHORT).show();
                                    pin.setImageResource(R.drawable.pinfilled);
                                    Toast.makeText(getApplicationContext(), "Pin clicked", Toast.LENGTH_SHORT).show();
                                    //Toast.makeText(getActivity(),vendors.toString(),Toast.LENGTH_SHORT).show();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                //  t1.setText(s);
                                if (s == null) {
                                    Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                                }
                                //l1.setAdapter(adapter);
                            }
                        });
                    }
                });
            }
        });*/

/*        String pinnedoffers = VendorActivity.detailsBundle.getString("PinnedOffers");
        Toast.makeText(getApplicationContext(),pinnedoffers,Toast.LENGTH_SHORT).show();
        try {
            JSONObject obj=new JSONObject(pinnedoffers);
            JSONArray pinned = obj.getJSONArray("pinned");
            View v;
            ImageView pin;
            for(int i=0;i<listview.getCount();i++) {
                v=listview.getChildAt(i);
                //pin = (ImageView) v.findViewById(R.id.pinimage);
                for(int j=0;j<pinned.length();j++) {

                    if (offerid.get(i).equals(pinned.getString(j))) {
                        //pin.setImageResource(R.drawable.pinfilled);
                        break;
                    }
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_unused_offers, menu);
        return true;
    }

    private ArrayList<MyOffer> convertRowMyOffers(String s){
        ArrayList<MyOffer> rowItems = new ArrayList<MyOffer>();

        MyOffer item = null;
        try {
            //Log.e(TAG, "json passed - " + s);
            JSONObject offerObject = new JSONObject(s);
            JSONObject params ;
            String description;
            JSONArray array = offerObject.getJSONArray("offers");
            MyOffer.SXOfferExtras extras = null;
            for (int i = 0; i < array.length(); ++i) {
                offerObject = array.getJSONObject(i);
                Log.i("id",offerObject.getString("_id"));
                //params = offerObject.getJSONObject("params");

                extras = null;
                //String type = offerObject.getString("type");
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
                    //rowItems.add(offerObject.getString("caption"));
                    //offerid.add(offerObject.getString("_id"));
                    //caption.add(offerObject.getString("caption"));
                    //description.add(offerObject.getString("description"));
                //}
                item = new MyOffer(offerObject.getString("type"),
                        null,
                        null,
                        offerObject.getString("caption"),
                        offerObject.getString("description"),
                        //params.getInt("stamps"),
                        0,
                        //params.getBoolean("used"),
                        //params.getBoolean("unlocked"),
                        false,true,null,
                        offerObject.getString("_id")
                        );
                //if(params.getBoolean("used")==false&&params.getBoolean("unlocked")==true)
                rowItems.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"An error occurred. Please try again later.",Toast.LENGTH_SHORT).show();
        }
        if(rowItems.size()==0)
        {
            findViewById(R.id.alertoffer).setVisibility(View.VISIBLE);
            //Toast.makeText(getApplicationContext(),"No offer unlocked. Marking checkin as a visit",Toast.LENGTH_SHORT).show();
            String url = "http://api.clozerr.com/v2/vendor/checkin?access_token="+Home.TOKEN+"&vendor_id="+VendorActivity.vendorId+"&gcm_id="+ GCMRegistrar.getRegistrationId(getApplicationContext());
            new AsyncGet(getApplicationContext(), url, new AsyncGet.AsyncResult() {
                @Override
                public void gotResult(String s) {
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        if(jsonObject.getString("vendor").equals(VendorActivity.vendorId))
                        Toast.makeText(getApplicationContext(),"No offer unlocked. Marking checkin as a visit",Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
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
