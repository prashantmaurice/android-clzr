package com.clozerr.app.Activities.HomeScreens;

import android.content.Context;
import android.content.Intent;
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

import com.clozerr.app.Activities.VendorScreens.VendorActivity;
import com.clozerr.app.AsyncGet;
import com.clozerr.app.CardModel;
import com.clozerr.app.MainApplication;
import com.clozerr.app.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MyClubsRecyclerViewAdapter extends RecyclerView.Adapter<MyClubsRecyclerViewAdapter.ListItemViewHolder> {

    private List<CardModel> items;
    static Context c;

    public MyClubsRecyclerViewAdapter(List<CardModel> modelData, Context c) {
        if (modelData == null) {
            throw new IllegalArgumentException(
                    "modelData must not be null");
        }
        this.items = modelData;
        MyClubsRecyclerViewAdapter.c = c;
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
                if( result != null )
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

//        SharedPreferences status;
        boolean NotNow;
        ArrayList<String> fav;
        JSONArray favourites;

        public void updateFavs(){
//            status = c.getSharedPreferences("USER",0);
            NotNow = MainApplication.getInstance().data.userMain.notNow;
//            NotNow = status.getString("notNow","false");
            fav = new ArrayList<String>();
//            if(NotNow.equals("false")) {
            if(!NotNow) {
                try {
//                    JSONObject userobj = new JSONObject(status.getString("user", "null"));
                    String jsonTxt = (MainApplication.getInstance().data.userMain.user.isEmpty())?"null":MainApplication.getInstance().data.userMain.user;
                    JSONObject userobj = new JSONObject(jsonTxt);
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
//                    SharedPreferences status = c.getSharedPreferences("USER", 0);
//                    String TOKEN = status.getString("token", "");
                    String TOKEN = MainApplication.getInstance().tokenHandler.clozerrtoken;
                    //Log.i("name",getResources().getResourceName(R.id.favorites));
                    if(fav.indexOf(currentItem.getVendorId())==-1)
                    {
                        like.setImageResource(R.drawable.favorited);
                        Log.e("MyCardRVA", "favs url - " + "http://api.clozerr.com/v2/user/add/favourites?vendor_id="+currentItem.getVendorId()+"&access_token="+TOKEN);
                        new AsyncGet(c, "http://api.clozerr.com/v2/user/add/favourites?vendor_id="+currentItem.getVendorId()+"&access_token="+TOKEN, new AsyncGet.AsyncResult() {
                            @Override
                            public void gotResult(String s) {
                                //l1.setAdapter(adapter);
                                //try {
                                    if (s == null/* || !(new JSONObject(s).getBoolean("result"))*/) {
                                        Toast.makeText(c, "Connection error", Toast.LENGTH_SHORT).show();
                                        like.setImageResource(R.drawable.like);
                                    } else {
                                        fav.add(currentItem.getVendorId());
//                                        final SharedPreferences.Editor editor = c.getSharedPreferences("USER", 0).edit();
//                                        editor.putString("user", s);
//                                        editor.apply();
                                        MainApplication.getInstance().data.userMain.changeUser(s);
                                        Toast.makeText(c, "Favorited and added to My Clubs.", Toast.LENGTH_LONG).show();
                                    }
                                /*} catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(c, "Connection error", Toast.LENGTH_SHORT).show();
                                    like.setImageResource(R.drawable.like);
                                }*/
                                // Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();


                          /*RecyclerViewAdapter1 Cardadapter = new RecyclerViewAdapter1(convertRowMyOffers(s), CouponDetails.this);
                          mRecyclerView.setAdapter(Cardadapter);*/

                            }
                        },false);

                    }
                    else
                    {
                        like.setImageResource(R.drawable.like);
                        Log.e("MyCardRVA", "favs url - " + "http://api.clozerr.com/v2/user/remove/favourites?vendor_id="+currentItem.getVendorId()+"&access_token="+TOKEN);
                        new AsyncGet(c, "http://api.clozerr.com/v2/user/remove/favourites?vendor_id="+currentItem.getVendorId()+"&access_token="+TOKEN, new AsyncGet.AsyncResult() {
                            @Override
                            public void gotResult(String s) {
                                //l1.setAdapter(adapter);
                                //try {
                                    if (s == null/* || !(new JSONObject(s).getBoolean("result"))*/) {
                                        Toast.makeText(c, "Connection error", Toast.LENGTH_SHORT).show();
                                        like.setImageResource(R.drawable.favorited);
                                    } else {
                                        fav.remove(fav.indexOf(currentItem.getVendorId()));
//                                        final SharedPreferences.Editor editor = c.getSharedPreferences("USER", 0).edit();
//                                        editor.putString("user", s);
//                                        editor.apply();
                                        MainApplication.getInstance().data.userMain.changeUser(s);
                                        Toast.makeText(c, "Unfavorited and removed from My Clubs.", Toast.LENGTH_LONG).show();
                                    }
                                    // Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
                                /*} catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(c, "Connection error", Toast.LENGTH_SHORT).show();
                                    like.setImageResource(R.drawable.favorited);
                                }*/

                          /*RecyclerViewAdapter1 Cardadapter = new RecyclerViewAdapter1(convertRowMyOffers(s), CouponDetails.this);
                          mRecyclerView.setAdapter(Cardadapter);*/

                            }
                        },false);
                    }
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    SharedPreferences status = c.getSharedPreferences("USER", 0);
//                    String NotNow = status.getString("notNow", "false");
                    NotNow = MainApplication.getInstance().data.userMain.notNow;
                    if (VendorActivity.i == 0 && !NotNow)
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