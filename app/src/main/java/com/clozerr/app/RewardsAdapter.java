package com.clozerr.app;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Adarsh on 6/17/2015.
 */

public class RewardsAdapter extends ArrayAdapter<String> {
    public ArrayList<String> text;
    public LayoutInflater navinflater;
    private Context context;
    int i;

    public RewardsAdapter(Context context, ArrayList<String> texts) {
        super(context, R.layout.freebies_item_layout, texts);
        text = texts;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int navlist[];
        navlist = new int[]{R.drawable.welcomereward, R.drawable.facebookreward, R.drawable.happyhour, R.drawable.lucky};
        if (convertView == null) {
            // This a new view we inflate the new layout
            navinflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = navinflater.inflate(R.layout.freebies_item_layout, parent, false);
        }

        if (!(convertView instanceof LinearLayout))
            Log.e("app", "layout error");
            // Now we can fill the layout with the right values
        else {
            TextView txtNum = (TextView) convertView.findViewById(R.id.freebiename);
            //TextView txtOffer = (TextView) convertView.findViewById(R.id.txtOffer);
            ImageView icon = (ImageView) convertView.findViewById(R.id.freebieimage);
            // MyOffer off = offerList.get(position);
            //Log.i("shit",Integer.toString(position));
            icon.setImageResource(navlist[position]);
            txtNum.setText(text.get(position));
            return convertView;
        }
        return null;

    }

}
