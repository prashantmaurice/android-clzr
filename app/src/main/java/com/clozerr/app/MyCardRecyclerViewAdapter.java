package com.clozerr.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MyCardRecyclerViewAdapter extends RecyclerView.Adapter<MyCardRecyclerViewAdapter.ListItemViewHolder> {

    private List<CardModel> items;
    static Context c;

    MyCardRecyclerViewAdapter(List<CardModel> modelData, Context c) {
        if (modelData == null) {
            throw new IllegalArgumentException(
                    "modelData must not be null");
        }
        this.items = modelData;
        MyCardRecyclerViewAdapter.c = c;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(
            ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_layout_myclubs,
                        viewGroup,
                        false);
        return new ListItemViewHolder(itemView);
    }



    @Override
    public void onBindViewHolder(final ListItemViewHolder viewHolder, int position) {
        viewHolder.currentItem = items.get(position);
        CardModel model = items.get(position);
        viewHolder.txtTitle.setText(model.getTitle());
        //viewHolder.txtStamps.setText(model.getStampString());
        viewHolder.txtDist.setText(model.getDistanceString());
        Ion.with(c)
             //   .placeholder(R.drawable.call)
             //   .error(R.drawable.bat)
                        //    .animateLoad(spinAnimation)
                        //    .animateIn(fadeInAnimation)
                .load(model.getImageId()).asBitmap().setCallback(new FutureCallback<Bitmap>() {
            @Override
            public void onCompleted(Exception e, Bitmap result) {
                viewHolder.imageView.setImageBitmap(Bitmap.createScaledBitmap(result,1600,900,false));
            }
        });
            }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public final static class ListItemViewHolder
            extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView txtTitle;
        TextView txtDist;
        TextView txtStamps;
        public CardModel currentItem;
        ImageButton like;

        SharedPreferences status;
        String NotNow;
        ArrayList<String> fav;
        JSONArray favourites;

        public void updateFavs(){
            status = c.getSharedPreferences("USER",0);
            NotNow = status.getString("notNow","false");
            fav = new ArrayList<String>();
            if(NotNow.equals("false")) {
                try {
                    JSONObject userobj = new JSONObject(status.getString("user", "null"));
                    favourites = userobj.getJSONArray("favourites");
                    Log.i("favourites", favourites.toString());
                    if (favourites != null) {
                        int len = favourites.length();
                        for (int i = 0; i < len; i++) {
                            fav.add(favourites.get(i).toString());
                        }
                    }

                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        public ListItemViewHolder(View itemView) {
            super(itemView);
            updateFavs();

            txtTitle = (TextView) itemView.findViewById(R.id.textTitle);
            txtStamps = (TextView) itemView.findViewById(R.id.txtCaption);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            txtDist=(TextView)itemView.findViewById(R.id.textDistance);
            like = (ImageButton) itemView.findViewById(R.id.like);
            like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Log.i("tag",like.getTag().toString());
                    //Toast.makeText(c,"clicked",Toast.LENGTH_SHORT).show();
                    SharedPreferences status = c.getSharedPreferences("USER", 0);
                    String TOKEN = status.getString("token", "");
                    //Log.i("name",getResources().getResourceName(R.id.favorites));
                    if(fav.indexOf(currentItem.getVendorId())==-1)
                    {
                        like.setImageResource(R.drawable.favorited);
                        new AsyncGet(c, "http://api.clozerr.com/v2/user/add/favourites?vendor_id="+currentItem.getVendorId()+"&access_token="+TOKEN, new AsyncGet.AsyncResult() {
                            @Override
                            public void gotResult(String s) {
                                final SharedPreferences.Editor editor = c.getSharedPreferences("USER", 0).edit();
                                editor.putString("user",s);
                                editor.apply();
                                Toast.makeText(c, "Favorited and added to My Clubs.", Toast.LENGTH_LONG).show();

                                //l1.setAdapter(adapter);
                                if (s == null) {
                                    Toast.makeText(c, "No internet connection", Toast.LENGTH_SHORT).show();
                                    like.setImageResource(R.drawable.like);
                                }

                                // Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();


                          /*RecyclerViewAdapter1 Cardadapter = new RecyclerViewAdapter1(convertRowMyOffers(s), CouponDetails.this);
                          mRecyclerView.setAdapter(Cardadapter);*/
                                fav.add(currentItem.getVendorId());
                            }
                        },false);

                    }
                    else
                    {
                        like.setImageResource(R.drawable.like);
                        new AsyncGet(c, "http://api.clozerr.com/v2/user/remove/favourites?vendor_id="+currentItem.getVendorId()+"&access_token="+TOKEN, new AsyncGet.AsyncResult() {
                            @Override
                            public void gotResult(String s) {
                                final SharedPreferences.Editor editor = c.getSharedPreferences("USER", 0).edit();
                                editor.putString("user",s);
                                editor.apply();
                                Toast.makeText(c,"Unfavorited and removed to My Clubs.", Toast.LENGTH_LONG).show();
                                //l1.setAdapter(adapter);
                                if (s == null) {
                                    Toast.makeText(c, "No internet connection", Toast.LENGTH_SHORT).show();
                                    like.setImageResource(R.drawable.like);
                                }

                                // Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();


                          /*RecyclerViewAdapter1 Cardadapter = new RecyclerViewAdapter1(convertRowMyOffers(s), CouponDetails.this);
                          mRecyclerView.setAdapter(Cardadapter);*/
                                fav.remove(fav.indexOf(currentItem.getVendorId()));
                            }
                        },false);
                    }
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences status = c.getSharedPreferences("USER", 0);
                    String NotNow = status.getString("notNow", "false");
                    if (VendorActivity.i == 0 && NotNow.equals("false"))
                    {
                        Intent detailIntent = new Intent(c, VendorActivity.class);
                        detailIntent.putExtra("vendor_id", currentItem.getVendorId());
                        /*detailIntent.putExtra("stamps", currentItem.getStamps());
                        detailIntent.putExtra("offer_id", currentItem.getOfferId());
                        detailIntent.putExtra("offer_text", currentItem.getOfferDescription());*/
                        //RecyclerViewAdapter.vendor_name_temp = currentItem.getTitle();
                        c.startActivity(detailIntent);
                    }

                }
            });
        }
    }


}