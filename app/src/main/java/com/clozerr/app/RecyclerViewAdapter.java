package com.clozerr.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.clozerr.app.Activities.LoginScreens.LoginActivity;
import com.clozerr.app.Activities.VendorScreens.VendorActivity;
import com.facebook.Session;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Junaid on 29/11/14.
 * Adapter used in CardView
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ListItemViewHolder> {

    private List<CardModel> items;
    static Context c;
//    SharedPreferences status;
    boolean NotNow;
    ArrayList<String> fav;
    JSONArray favourites;


    //public static String vendor_name_temp = "";

    RecyclerViewAdapter(List<CardModel> modelData, Context c) {
        if (modelData == null) {
            throw new IllegalArgumentException(
                    "modelData must not be null");
        }
        this.items = modelData;
        this.c = c;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(
            ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_layout,
                        viewGroup,
                        false);
        return new ListItemViewHolder(itemView);
    }
    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = c.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    @Override
    public void onBindViewHolder(final ListItemViewHolder viewHolder, int position) {
        viewHolder.currentItem = items.get(position);
        CardModel model = items.get(position);
        viewHolder.txtTitle.setText(model.getTitle());
        viewHolder.txtCaption.setText(model.getCaption());
        viewHolder.txtDist.setText(model.getDistanceString());
        Log.i("fav","changing favs");
        updateFavs();
        if(fav.indexOf(model.getVendorId())!=-1)
            viewHolder.like.setImageResource(R.drawable.favorited);
        else
            viewHolder.like.setImageResource(R.drawable.like);
        Log.i("fave","leaving favs");
        /*LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(viewHolder.linearLayout.getWidth())/2
        );

        viewHolder.linearLayout.setLayoutParams(params);*/
        //viewHolder.txtrating.setText(model.getRating());
       // new DownloadImageTask(viewHolder.imageView).execute(model.getImageId());
        // viewHolder.txtDist.setText(model.getDesc());

        Ion.with(c).load(model.getImageId()).withBitmap().placeholder(R.drawable.defoffer).transform(new com.koushikdutta.ion.bitmap.Transform() {
            public Bitmap transform(Bitmap bitmap) {
                Log.d("Bitmap transform", "wd:" + viewHolder.imageView.getWidth() + " ht:" + viewHolder.imageView.getHeight() );
                Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth()/2, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(output);

                final int color = 0xff424242;
                final Paint paint = new Paint();
                final Rect rect = new Rect( 0, 0, bitmap.getWidth(), bitmap.getWidth()/2);
                final RectF rectF = new RectF(rect);
                final float roundPx = 10.0f;

                paint.setAntiAlias(true);
                canvas.drawARGB(0, 0, 0, 0);
                paint.setColor(color);
                canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(bitmap, rect, rect, paint);

                return output;
            }

            public String key() {
                Log.d("Bitmap transform key", "ht:");
                return "rounded_rect_40";
            }
        }).intoImageView(viewHolder.imageView);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
    public void updateFavs(){
//        status = c.getSharedPreferences("USER", 0);
//        NotNow = status.getString("notNow", "false");

        NotNow = MainApplication.getInstance().data.userMain.notNow;
//            NotNow = status.getString("notNow","false");
        fav = new ArrayList<String>();
//            if(NotNow.equals("false")) {


        fav = new ArrayList<String>();
//        if(NotNow.equals("false")) {
        if(!NotNow) {
            try {
                String jsonTxt = (MainApplication.getInstance().data.userMain.user.isEmpty())?"null":MainApplication.getInstance().data.userMain.user;
                JSONObject userobj = new JSONObject(jsonTxt);
                favourites = userobj.getJSONArray("favourites");
                Log.i("favourites",favourites.toString());
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

    public final static class ListItemViewHolder
            extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView txtTitle;
        TextView txtDist;
        TextView txtCaption;
        LinearLayout linearLayout;
        ImageButton like;
        //TextView txtrating;
        public CardModel currentItem;

        public ListItemViewHolder(final View itemView) {
            super(itemView);
            txtDist = (TextView) itemView.findViewById(R.id.textDistance);
            txtTitle = (TextView) itemView.findViewById(R.id.textTitle);
            txtCaption = (TextView) itemView.findViewById(R.id.txtCaption);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            linearLayout = (LinearLayout)itemView.findViewById(R.id.cardlayout);
            like = (ImageButton)itemView.findViewById(R.id.like);
            like.setImageResource(R.drawable.favorited);
           // Log.i("currentItem",currentItem.toString());
           /* LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    linearLayout.getWidth()/2
            );

            linearLayout.setLayoutParams(params);*/
            VendorActivity.Rewards = "";
            //txtrating=(TextView) itemView.findViewById(R.id.txtrating);
//            final SharedPreferences status = c.getSharedPreferences("USER", 0);
//            final boolean NotNow = status.getString("notNow", "false");

            final boolean NotNow = MainApplication.getInstance().data.userMain.notNow;


            final ArrayList<String> fav = new ArrayList<String>();
            like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Log.i("tag",like.getTag().toString());
                    //Toast.makeText(c,"clicked",Toast.LENGTH_SHORT).show();
//                    SharedPreferences status = c.getSharedPreferences("USER", 0);
//                    String TOKEN = status.getString("token", "");
                    String TOKEN = MainApplication.getInstance().data.userMain.token;
                    //Log.i("name",getResources().getResourceName(R.id.favorites));
                    if(fav.indexOf(currentItem.getVendorId())==-1)
                    {
                        like.setImageResource(R.drawable.favorited);
                        Log.e("RVA", "favs url - " + "http://api.clozerr.com/v2/user/favourites/add?vendor_id="+currentItem.getVendorId()+"&access_token="+TOKEN);
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
                        Log.e("RVA", "favs url - " + "http://api.clozerr.com/v2/user/favourites/remove?vendor_id="+currentItem.getVendorId()+"&access_token="+TOKEN);
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
                                /*} catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(c, "Connection error", Toast.LENGTH_SHORT).show();
                                    like.setImageResource(R.drawable.favorited);
                                }*/
                                // Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();


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
                    if (VendorActivity.i == 0 && !NotNow)
                    {
                        Intent detailIntent = new Intent(c, VendorActivity.class);
                        detailIntent.putExtra("vendor_id", currentItem.getVendorId());
                        detailIntent.putExtra("offer_id", currentItem.getOfferId());
                        detailIntent.putExtra("offer_caption", currentItem.getCaption());
                        detailIntent.putExtra("offer_text", currentItem.getOfferDescription());
                        if( currentItem.isActive() )
                            c.startActivity(detailIntent);
                    }
                    else  {

                        // CouponPage.i = 2;
                        AlertDialog.Builder builder = new AlertDialog.Builder(c);
                        builder.setMessage("You have to login to view.Want to proceed to login page?").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();
                        // startActivity(new Intent(c,CouponPage.class));


                    }
                }
            });
            //if(currentItem.getVendorId())
        }
//suggest rest
        //border -- lines
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
//                        SharedPreferences example = c.getSharedPreferences("USER", 0);
//                        SharedPreferences.Editor editor = example.edit();
//                        editor.putString("notNow", "false");
//                        editor.apply();
                        MainApplication.getInstance().data.userMain.changeNotNow(false);
                        //Yes button clicked
                           /* Intent mStartActivity = new Intent(c,Login.class);
                            int mPendingIntentId = 123456;
                            PendingIntent mPendingIntent = PendingIntent.getActivity(c, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                            AlarmManager mgr = (AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
                            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                            System.exit(0);*/
                        // Toast.makeText(c, "Logging out", Toast.LENGTH_SHORT).show();

                        Session session = Session.getActiveSession();
                        if (session != null) {
                            if (!session.isClosed()) {
                                session.closeAndClearTokenInformation();
                            }
                        } else {
                            session = new Session(c);
                            Session.setActiveSession(session);
                            session.closeAndClearTokenInformation();
                        }

                        c.startActivity(new Intent(c, LoginActivity.class));

                        if(c instanceof Activity)
                            ((Activity)c).finish();
                        else
                            Toast.makeText(c,"Error", Toast.LENGTH_SHORT);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //Toast.makeText(Home.this, "Thanks for staying. You are awesome.", Toast.LENGTH_SHORT).show();
                        //No button clicked
                        break;
                }
            }
        };
    }
}