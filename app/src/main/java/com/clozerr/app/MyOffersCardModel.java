package com.clozerr.app;

import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * Created by saipraveenb on 19/01/15.
 */
public class MyOffersCardModel {
    private RecyclerViewAdapter1 mMyOfferAdapter;
    private String mHeading;

    public MyOffersCardModel(String heading, Context context, List<MyOffer> items) {
        Log.e("itemssize", items.size() + " - " + heading);
        this.mHeading = heading;
        mMyOfferAdapter = new RecyclerViewAdapter1(items, context);
    }

    public String getHeading() { return mHeading; }
    public RecyclerViewAdapter1 getMyOfferAdapter() { return mMyOfferAdapter; }
}
