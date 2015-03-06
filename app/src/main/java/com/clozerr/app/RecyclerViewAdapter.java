package com.clozerr.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.*;
import android.util.Log;

import com.facebook.Session;
import com.koushikdutta.ion.Ion;

import java.util.List;


/**
 * Created by Junaid on 29/11/14.
 * Adapter used in CardView
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ListItemViewHolder> {

    private List<CardModel> items;
    static Context c;
    public static String vendor_name_temp = "";

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



    @Override
    public void onBindViewHolder(final ListItemViewHolder viewHolder, int position) {
        viewHolder.currentItem = items.get(position);
        CardModel model = items.get(position);
        viewHolder.txtTitle.setText(model.getTitle());
        viewHolder.txtCaption.setText(model.getCaption());
        viewHolder.txtDist.setText(model.getDistance());
        //viewHolder.txtrating.setText(model.getRating());
       // new DownloadImageTask(viewHolder.imageView).execute(model.getImageId());

        // viewHolder.txtDist.setText(model.getDesc());

        Ion.with(c).load(model.getImageId()).withBitmap().placeholder(R.drawable.defoffer).transform(new com.koushikdutta.ion.bitmap.Transform() {
            public Bitmap transform(Bitmap bitmap) {
                Log.d("Bitmap transform", "wd:" + viewHolder.imageView.getWidth() + " ht:" + viewHolder.imageView.getHeight() );
                Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(output);

                final int color = 0xff424242;
                final Paint paint = new Paint();
                final Rect rect = new Rect( 0, 0, bitmap.getWidth(), bitmap.getHeight());
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

    public final static class ListItemViewHolder
            extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView txtTitle;
        TextView txtDist;
        TextView txtCaption;
        //TextView txtrating;
        public CardModel currentItem;

        public ListItemViewHolder(final View itemView) {
            super(itemView);
            txtDist = (TextView) itemView.findViewById(R.id.textDistance);
            txtTitle = (TextView) itemView.findViewById(R.id.textTitle);
            txtCaption = (TextView) itemView.findViewById(R.id.txtCaption);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            //txtrating=(TextView) itemView.findViewById(R.id.txtrating);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences status = c.getSharedPreferences("USER", 0);
                    String NotNow = status.getString("notNow", "false");
                    if (CouponPage.i == 0 && NotNow.equals("false"))
                    {
                        Intent detailIntent = new Intent(c, CouponDetails.class);
                        // pass selected vendor's image to details activity to avoid re-download
                        /*imageView.buildDrawingCache();
                        Bitmap image = imageView.getDrawingCache();
                        Bundle vendorBundle = new Bundle();
                      // vendorBundle.putParcelable("vendorImage", image);

                    vendorBundle.putString("phoneNumber", phone number);
                    vendorBundle.putString("location", location);
                    vendorBundle.putString("description", description);*//*
                        vendorBundle.putString("vendorTitle", currentItem.getTitle());
                        vendorBundle.putString("offerText", currentItem.getOfferDescription() );
                        vendorBundle.putString("vendorId", currentItem.getVendorId());
                        vendorBundle.putString("offerId", currentItem.getOfferId());
                        vendorBundle.putString("vendorImage", currentItem.getImageId());
                        vendorBundle.putDouble("latitude", currentItem.getLat());
                        vendorBundle.putDouble("longitude", currentItem.getLong());
                        vendorBundle.putString("distance", currentItem.getDistance());
                        vendorBundle.putString("phonenumber", currentItem.getPhonenumber());
                        vendor_name_temp = currentItem.getTitle();
                        detailIntent.putExtra("detailsBundle", vendorBundle);*/
                        vendor_name_temp = currentItem.getTitle();
                        detailIntent.putExtra("vendor_id", currentItem.getVendorId());
                        detailIntent.putExtra("offer_id", currentItem.getOfferId());
                        detailIntent.putExtra("offer_caption", currentItem.getCaption());
                        detailIntent.putExtra("offer_text", currentItem.getOfferDescription());

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
        }
//suggest rest
        //border -- lines
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        SharedPreferences example = c.getSharedPreferences("USER", 0);
                        SharedPreferences.Editor editor = example.edit();
                        editor.putString("notNow", "false");
                        editor.apply();
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

                        c.startActivity(new Intent(c, Login.class));

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

    public abstract static class EndlessRecyclerOnScrollListener extends
            RecyclerView.OnScrollListener {

        int lastVisibleItem, totalItemCount;
        private LinearLayoutManager mLinearLayoutManager;

        public EndlessRecyclerOnScrollListener(
                LinearLayoutManager linearLayoutManager) {
            this.mLinearLayoutManager = linearLayoutManager;
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            totalItemCount = mLinearLayoutManager.getItemCount();
            lastVisibleItem = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
            if (totalItemCount == lastVisibleItem + 1) onLoadMore();
        }

        public abstract void onLoadMore();
    }

}