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
import android.widget.Toast;

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
                        /*MyOffer currentOffer = getCurrentOffer(s);

                        MyOffersRecyclerViewAdapter myOffersAdapter = new MyOffersRecyclerViewAdapter(myOffers, currentOffer, CouponDetails.this);
                        mRecyclerView.setAdapter(myOffersAdapter);*/
        MyOffersRecyclerViewAdapter adapter = new MyOffersRecyclerViewAdapter(myOffers, getActivity());
        recyclerview.setAdapter(adapter);
        final String[] values = new String[] { "1","2","3","4","5","6","7","8","9","10" };
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
            JSONObject offerObject = null;
            JSONArray array = new JSONArray(s);
            Toast.makeText(getActivity(),s, Toast.LENGTH_LONG).show();
            MyOffer.SXOfferExtras extras = null;
            for (int i = 0; i < array.length(); ++i) {
                offerObject = array.getJSONObject(i);
                extras = null;
                String type = offerObject.getString("type");
                if (type.equalsIgnoreCase("SX"))
                    extras = new MyOffer.SXOfferExtras(offerObject.getJSONObject("stampStatus").getInt("total"),
                            offerObject.getDouble("billAmt"));
                item = new MyOffer(type,
                        null,
                        null,
                        offerObject.getString("caption"),
                        offerObject.getString("description"),
                        offerObject.getInt("stamps"),
                        extras);
                rowItems.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowItems;
    }

    private void initViews() {
        layout.getForeground().mutate().setAlpha(0);
    }
}
