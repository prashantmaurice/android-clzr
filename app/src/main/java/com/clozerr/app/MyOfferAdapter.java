package com.clozerr.app;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by raakesh on 10/1/15.
 */
public class MyOfferAdapter extends ArrayAdapter<MyOffer> {

    private List<MyOffer> offerList;
    private Context context;

    public MyOfferAdapter(List<MyOffer> offerList, Context ctx) {
        super(ctx, android.R.layout.simple_list_item_1, offerList);
        this.offerList = offerList;
        this.context = ctx;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        // First let's verify the convertView is not null
        if (convertView == null) {
            // This a new view we inflate the new layout
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.offer_list, parent, false);
        }

        if (!(convertView instanceof LinearLayout))
            Log.e("app","layout error");
        // Now we can fill the layout with the right values
        else
        {
            TextView txtNum = (TextView) convertView.findViewById(R.id.txtNum);
            TextView txtOffer = (TextView) convertView.findViewById(R.id.txtOffer);

            MyOffer off = offerList.get(position);
            txtNum.setText(off.getStamps());
            txtOffer.setText(off.getCaption());

            return convertView;
        }
        return null;
    }
}

