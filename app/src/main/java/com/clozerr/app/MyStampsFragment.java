package com.clozerr.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.clozerr.app.Activities.VendorScreens.VendorActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MyStampsFragment extends Fragment {

    Context c;
    FrameLayout layout;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        layout = (FrameLayout) inflater.inflate(R.layout.activity_mystamps_fragment, container, false);
        final RecyclerView recyclerview=(RecyclerView)layout.findViewById(R.id.stampslist);
        recyclerview.setLayoutManager(new GridLayoutManager(c,3));
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        recyclerview.setHasFixedSize(true);
        ArrayList<MyOffer> myOffers = convertRowMyOffers(VendorActivity.detailsBundle.getString("Alloffers"));
        //Toast.makeText(getActivity(), VendorActivity.detailsBundle.getString("Alloffers"), Toast.LENGTH_LONG).show();
                        /*MyOffer currentOffer = getCurrentOffer(s);

                        MyOffersRecyclerViewAdapter myOffersAdapter = new MyOffersRecyclerViewAdapter(myOffers, currentOffer, CouponDetails.this);
                        mRecyclerView.setAdapter(myOffersAdapter);*/
        MyOffersRecyclerViewAdapter adapter = new MyOffersRecyclerViewAdapter(myOffers, getActivity());
        recyclerview.setAdapter(adapter);
        final TextView textView = (TextView) layout.findViewById(R.id.stampdesc);
        textView.setText(VendorActivity.detailsBundle.getString("policy"));
        //final String[] values = new String[] { "1","2","3","4","5","6","7","8","9","10" };
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),R.layout.stamp_layout, R.id.stampnumber, values);
        //recyclerview.setAdapter(new MyStampsRecyclerViewAdapter(values,getActivity()));
        return layout;
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

    private ArrayList<MyOffer> convertRowMyOffers(String s){
        ArrayList<MyOffer> rowItems = new ArrayList<>();

        MyOffer item = null;
        try {
            //Log.e(TAG, "json passed - " + s);
            JSONObject someObject = new JSONObject(s);
            JSONArray array = someObject.getJSONArray("offers");
            int maxStamps = getMaxStampCount( array );
            //Log.e(maxStamps);
            //Toast.makeText(getActivity(),s, Toast.LENGTH_LONG).show();
            MyOffer.SXOfferExtras extras = null;
            int i=0;
            for(int j=0;;j++) {
                int flag=0;
                for (i = 0; i < array.length(); ++i) {
                    JSONObject offerObject = array.getJSONObject(i);
                    extras = null;
                    String type = offerObject.getString("type");
                    if (type.equalsIgnoreCase("SX"))
                        extras = new MyOffer.SXOfferExtras(offerObject.getJSONObject("stampStatus").getInt("total"),
                                offerObject.getDouble("billAmt"));
                    if (j == offerObject.getJSONObject("params").getInt("stamps") - 1) {
                        item = new MyOffer(type,
                                null,
                                null,
                                offerObject.getString("caption"),
                                offerObject.getString("description"),
                                offerObject.getJSONObject("params").getInt("stamps"),
                                offerObject.getJSONObject("params").getBoolean("used"),
                                offerObject.getJSONObject("params").getBoolean("unlocked"),
                                extras,
                                offerObject.getString("_id"),
                                someObject.getInt("stamps")>=offerObject.getJSONObject("params").getInt("stamps")?true:false);
                                //offerObject.getJSONObject("params").getBoolean("unlocked"));
                        rowItems.add(item);
                        flag = 1;
                        break;
                    }
                }
                    if(flag==0)
                    {
                        item = new MyOffer(null,
                                null,
                                null,
                                null,
                                null,
                                j+1,
                                false,
                                true,
                                extras,
                                null,
                                someObject.getInt("stamps")>=j+1?true:false);
                        rowItems.add(item);
                    }


                if(j>=maxStamps-1) break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowItems;
    }

    private int getMaxStampCount(JSONArray array) {
        int max = 0;
        for( int k = 0; k < array.length(); k++ ){
            int num = 0;
            try {
                num = array.getJSONObject(k).getJSONObject("params").getInt("stamps");
            }catch(org.json.JSONException ex){
                ex.printStackTrace();
            }
            if( num > max )
                max = num;
        }
        return max;
    }

    private void initViews() {
        layout.getForeground().mutate().setAlpha(0);
    }
}
