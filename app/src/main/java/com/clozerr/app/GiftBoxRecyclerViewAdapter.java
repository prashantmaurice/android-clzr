package com.clozerr.app;

/**
 * Created by Adarsh on 23-05-2015.
 */

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.clozerr.app.Activities.HomeScreens.HomeActivity;
import com.clozerr.app.Activities.VendorScreens.VendorActivity;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GiftBoxRecyclerViewAdapter extends RecyclerView.Adapter<GiftBoxRecyclerViewAdapter.ListItemViewHolder> {

    private ArrayList<MyOffer> values;
    static Context c;
    Resources reso;
//    public SharedPreferences status;
    public boolean NotNow;
    public static ArrayList<String> pinned;

    GiftBoxRecyclerViewAdapter(ArrayList<MyOffer> offers, Context c) {
        this.values = offers;
        this.c = c;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final View itemView =
                LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.freebies_item_layout,
                                viewGroup, false);
        return new ListItemViewHolder(itemView);
    }



    @Override
    public void onBindViewHolder(final ListItemViewHolder viewHolder, int position) {
        viewHolder.currentItem = values.get(position);
        MyOffer current=values.get(position);
        //viewHolder.txtvisitno.setText(model.getStamps()+"");
        viewHolder.caption.setText(current.getVendorName());
        viewHolder.vendorId = current.getVendorId();
        viewHolder.vendorName = current.getVendorName();
        String desc = current.getDescription();
        if(current.getDescription().length()>=32)
            desc= current.getDescription().substring(0,32)+"...";
        viewHolder.description.setText(current.getCaption());
        updatePind();
        if(pinned.indexOf(viewHolder.currentItem.getOfferid())!=-1)
            viewHolder.pin.setImageResource(R.drawable.pinfilled);
        else
            viewHolder.pin.setImageResource(R.drawable.pin100);
        Log.i("fave","leaving favs");
        Ion.with((viewHolder.image))
                //   .placeholder(R.drawable.call)
                //   .error(R.drawable.bat)
                //    .animateLoad(spinAnimation)
                //    .animateIn(fadeInAnimation)
                .load(current.getImageUrl());

//        viewHolder.stampnumber.setOnTouchListener(new View.OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View arg0, MotionEvent arg1) {
//                switch (arg1.getActionMasked()) {
//                    case MotionEvent.ACTION_DOWN: {
//                        viewHolder.stampnumber.setBackground(reso.getDrawable(R.drawable.cirkbackhover));
//                        return true;
//
//                    }
//                    case MotionEvent.ACTION_UP:
//                        // TODO Auto-generated method stub
//                    {
//                        viewHolder.stampnumber.setBackground(reso.getDrawable(R.drawable.circularback));
//                        return true;
//                    }
//                }
//                return false;
//            }
//        });
        // viewHolder.txtDist.setText(model.getDesc());

    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    public void updatePind(){
//        status = c.getSharedPreferences("USER",0);
        NotNow = MainApplication.getInstance().data.userMain.notNow;
//        NotNow = status.getString("notNow","false");
        pinned = new ArrayList<String>();
        JSONArray pind ;
        if(!NotNow) {
            try {
//                JSONObject userobj = new JSONObject(status.getString("user", "null"));
                String jsonTxt = (MainApplication.getInstance().data.userMain.user.isEmpty())?"null":MainApplication.getInstance().data.userMain.user;
                JSONObject userobj = new JSONObject(jsonTxt);
                pind = userobj.getJSONArray("pinned");
                Log.i("pinned", pind.toString());
                if (pind != null) {
                    int len = pind.length();
                    for (int i = 0; i < len; i++) {
                        pinned.add(pind.get(i).toString());
                    }
                }

            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    public final static class ListItemViewHolder
            extends RecyclerView.ViewHolder {

        TextView caption;
        TextView description;
        public MyOffer currentItem;
        public String vendorId;
        public String vendorName;
        public ImageView image;
        //public Button checkinButton;
        ImageView pin;

        public ListItemViewHolder(View itemView) {
            super(itemView);
            //txtvisitno = (TextView) itemView.findViewById(R.id.txtNum);
            image = (ImageView) itemView.findViewById( R.id.freebieimage );
            caption = (TextView) itemView.findViewById(R.id.freebiename);
            description = (TextView) itemView.findViewById(R.id.freebiedescription);
            vendorId = vendorName = "";
            //checkinButton = (Button) itemView.findViewById(R.id.useit);

            pin=(ImageView)itemView.findViewById(R.id.pinimage);
            pin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String urlPinning = "http://api.clozerr.com/v2/user/add/pinned?access_token=" + HomeActivity.TOKEN +"&offer_id="+currentItem.getOfferid();
                    if(pinned.indexOf(currentItem.getOfferid())==-1)
                    {
                        new AsyncGet(c, urlPinning, new AsyncGet.AsyncResult() {
                            @Override
                            public void gotResult(String s) {
                                try {
                                    JSONObject obj = new JSONObject(s);
//                                    final SharedPreferences.Editor editor = c.getSharedPreferences("USER", 0).edit();
//                                    editor.putString("user",s);
//                                    editor.apply();
                                    MainApplication.getInstance().data.userMain.changeUser(s);
                                    //Toast.makeText(getActivity(),s,Toast.LENGTH_SHORT).show();
                                    pin.setImageResource(R.drawable.pinfilled);
                                    Toast.makeText(c, "Added to pinned offers", Toast.LENGTH_SHORT).show();
                                    //Toast.makeText(getActivity(),vendors.toString(),Toast.LENGTH_SHORT).show();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                //  t1.setText(s);
                                if (s == null) {
                                    Toast.makeText(c, "No internet connection", Toast.LENGTH_SHORT).show();
                                }
                                //l1.setAdapter(adapter);
                            }
                        });
                    }
                    else
                    {
                        new AsyncGet(c, "http://api.clozerr.com/v2/user/remove/pinned?access_token=" + HomeActivity.TOKEN +"&offer_id="+currentItem.getOfferid(), new AsyncGet.AsyncResult() {
                            @Override
                            public void gotResult(String s) {
                                try {
                                    JSONObject obj = new JSONObject(s);
//                                    final SharedPreferences.Editor editor = c.getSharedPreferences("USER", 0).edit();
//                                    editor.putString("user",s);
//                                    editor.apply();
                                    MainApplication.getInstance().data.userMain.changeUser(s);
                                    //Toast.makeText(getActivity(),s,Toast.LENGTH_SHORT).show();
                                    pin.setImageResource(R.drawable.pin100);
                                    Toast.makeText(c, "Removed from pinned offers", Toast.LENGTH_SHORT).show();
                                    //Toast.makeText(getActivity(),vendors.toString(),Toast.LENGTH_SHORT).show();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                //  t1.setText(s);
                                if (s == null) {
                                    Toast.makeText(c, "No internet connection", Toast.LENGTH_SHORT).show();
                                }
                                //l1.setAdapter(adapter);
                            }
                        });
                    }
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(c, FreebieDescription.class);
                    intent.putExtra("offerid", currentItem.getOfferid());
                    intent.putExtra("vendorid", (!vendorId.isEmpty()) ? vendorId : VendorActivity.detailsBundle.getString("vendorId"));
                    intent.putExtra("vendorName", (!vendorName.isEmpty()) ? vendorName : VendorActivity.detailsBundle.getString("vendorTitle"));
                    intent.putExtra("caption", currentItem.getCaption());
                    intent.putExtra("description", currentItem.getDescription());
                    c.startActivity(intent);
                }
            });
        }
    }


}

