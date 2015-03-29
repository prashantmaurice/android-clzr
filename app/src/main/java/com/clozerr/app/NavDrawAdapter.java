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
 * Created by raakesh on 18/1/15.
 */
public class NavDrawAdapter extends ArrayAdapter<String> {
    public ArrayList<String> text;
    public LayoutInflater navinflater;
    private Context context;
    int i;

    public NavDrawAdapter(Context context, ArrayList<String> texts) {
        super(context, R.layout.navdrawlist, texts);
        text = texts;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int navlist[];
        navlist = new int[]{R.drawable.aboutclozerr, R.drawable.aboutus, R.drawable.facebooklike, R.drawable.rate, R.drawable.share, R.drawable.settings, R.drawable.logout};
        if (convertView == null) {
            // This a new view we inflate the new layout
            navinflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = navinflater.inflate(R.layout.navdrawlist, parent, false);
        }

        if (!(convertView instanceof LinearLayout))
            Log.e("app", "layout error");
            // Now we can fill the layout with the right values
        else {
            TextView txtNum = (TextView) convertView.findViewById(R.id.textView);
            //TextView txtOffer = (TextView) convertView.findViewById(R.id.txtOffer);
            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
            // MyOffer off = offerList.get(position);
            //Log.i("shit",Integer.toString(position));
            icon.setImageResource(navlist[position]);
            txtNum.setText(text.get(position));
            return convertView;
        }
        return null;

    }

}
