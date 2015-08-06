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

import com.clozerr.app.Models.NavObject;

import java.util.ArrayList;

/**
 * This handles the entire NavDrawer view generations
 */
public class NavDrawAdapter extends ArrayAdapter<NavObject> {
    public ArrayList<NavObject> text;
    public LayoutInflater navinflater;
    private Context context;
    int i;

    public NavDrawAdapter(Context context, ArrayList<NavObject> texts) {
        super(context, R.layout.navdrawlist, texts);
        text = texts;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
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
            icon.setImageResource(text.get(position).iconResId);
            txtNum.setText(text.get(position).title);
            return convertView;
        }
        return null;

    }

}
