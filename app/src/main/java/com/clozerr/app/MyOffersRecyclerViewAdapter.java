package com.clozerr.app;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

public class MyOffersRecyclerViewAdapter extends RecyclerView.Adapter<MyOffersRecyclerViewAdapter.ListItemViewHolder> {
    private static final String TAG = "MyOffersRVAdapter";
    //private List<MyOffersCardModel> items;
    private ArrayList<MyOffer> mItems;
    static Context c;

    /*MyOffersRecyclerViewAdapter(List<MyOffer> allOffers, MyOffer currentOffer, Context context) {
        c = context;
        items = new ArrayList<>();
        Log.e("offerlist", String.valueOf(allOffers.size()));
        if (currentOffer != null) {
            List<MyOffer> usedOffersList = (currentOffer.getStamps() - 1 > 0 && currentOffer.getStamps() - 1 - 1 < allOffers.size()) ?
                    allOffers.subList(0, currentOffer.getStamps() - 1) : null;
            List<MyOffer> upcomingOffersList = (currentOffer.getStamps() - 1 < allOffers.size()) ?
                    allOffers.subList(currentOffer.getStamps() - 1, currentOffer.getStamps()) : null;
            List<MyOffer> laterOffersList = (currentOffer.getStamps() < allOffers.size()) ?
                    allOffers.subList(currentOffer.getStamps(), allOffers.size()) : null;
            if (usedOffersList != null) items.add(new MyOffersCardModel("USED", c, usedOffersList));
            if (upcomingOffersList != null) items.add(new MyOffersCardModel("UPCOMING", c, upcomingOffersList));
            if (laterOffersList != null) items.add(new MyOffersCardModel("LATER", c, laterOffersList));
        }
        else items.add(new MyOffersCardModel("USED", c, allOffers)); // no offers left
    }*/

    public MyOffersRecyclerViewAdapter(ArrayList<MyOffer> offers, Context context) {
        c = context;
        Log.e(TAG, "size - " + offers.size());
        mItems = offers;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(
            ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                /*inflate(R.layout.offers_card,*/
                inflate(R.layout.offer_list,
                        viewGroup,
                        false);
        return new ListItemViewHolder(itemView);
    }



    @Override
    public void onBindViewHolder(final ListItemViewHolder viewHolder, int position) {
        /*Log.e("nullptr", "pos " + String.valueOf(position) + " - " + items.get(position).getHeading());
        viewHolder.currentItem = items.get(position);
        viewHolder.headingView.setText(viewHolder.currentItem.getHeading());
        viewHolder.listRecyclerView.setAdapter(viewHolder.currentItem.getMyOfferAdapter());*/

        /*viewHolder.txtTitle.setText(model.getTitle());
        viewHolder.txtTitle.setText(model.getCaption());
        viewHolder.txtDist.setText(model.getDistance());*/

        //viewHolder.txtrating.setText(model.getRating());
        // new DownloadImageTask(viewHolder.imageView).execute(model.getImageId());

        // viewHolder.txtDist.setText(model.getDesc());

        /*Ion.with(c).load(model.getImageId()).withBitmap().placeholder(R.drawable.defoffer).transform(new com.koushikdutta.ion.bitmap.Transform() {
            public Bitmap transform(Bitmap bitmap) {
                Log.d("Bitmap tranform", "wd:" + viewHolder.imageView.getWidth() + " ht:" + viewHolder.imageView.getHeight());
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
        }).intoImageView(viewHolder.imageView);*/
        MyOffer currentItem = mItems.get(position);
        viewHolder.mCaptionView.setText(currentItem.getCaption());
        viewHolder.mDescriptionView.setText(currentItem.getDescription());
        if (currentItem.getImageUrl() != null && !currentItem.getImageUrl().isEmpty() &&
                !currentItem.getImageUrl().equalsIgnoreCase("null"))
            Ion.with(c).load(currentItem.getImageUrl()).withBitmap()
                    .intoImageView(viewHolder.mMainImageView);
        if (currentItem.getOptionalImageUrl() != null && !currentItem.getOptionalImageUrl().isEmpty() &&
                !currentItem.getOptionalImageUrl().equalsIgnoreCase("null"))
            Ion.with(c).load(currentItem.getOptionalImageUrl()).withBitmap()
                    .intoImageView(viewHolder.mOptionalImageView);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public final static class ListItemViewHolder extends RecyclerView.ViewHolder {
        public ImageView mMainImageView, mOptionalImageView;
        public TextView mCaptionView, mDescriptionView;

        public ListItemViewHolder(final View itemView) {
            super(itemView);

            mMainImageView = (ImageView) itemView.findViewById(R.id.offer_label);
            // find optional view
            mCaptionView = (TextView) itemView.findViewById(R.id.txtOffer);
            mDescriptionView = (TextView) itemView.findViewById(R.id.txtDesc);
        }
        /*ImageView imageView;
        TextView txtTitle;
        TextView txtDist;
        TextView txtTitle;*/
        //TextView txtrating;
        /*public MyOffersCardModel currentItem;
        public TextView headingView;
        public RecyclerView listRecyclerView;

        public ListItemViewHolder(final View itemView) {
            super(itemView);
            listRecyclerView = (RecyclerView) itemView.findViewById(R.id.list_offers);
            listRecyclerView.setLayoutManager(new MyLinearLayoutManager(c, LinearLayoutManager.VERTICAL, false ));
            listRecyclerView.setItemAnimator(new DefaultItemAnimator());
            //listRecyclerView.setHasFixedSize(true)


            headingView = (TextView) itemView.findViewById(R.id.cardHeadingView);
            *//*txtDist = (TextView) itemView.findViewById(R.id.textDistance);
            txtTitle = (TextView) itemView.findViewById(R.id.textTitle);
            txtTitle = (TextView) itemView.findViewById(R.id.txtTitle);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);*//*
            //txtrating=(TextView) itemView.findViewById(R.id.txtrating);

            *//*itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences status = c.getSharedPreferences("USER", 0);
                    String NotNow = status.getString("notNow", "false");
                    if (CouponPage.i == 0 && NotNow.equals("false"))
                    {
                        Intent detailIntent = new Intent(c, CouponDetails.class);
                        // pass selected vendor's image to details activity to avoid re-download
                        *//**//*imageView.buildDrawingCache();
                        Bitmap image = imageView.getDrawingCache();
                        Bundle vendorBundle = new Bundle();
                      // vendorBundle.putParcelable("vendorImage", image);

                    vendorBundle.putString("phoneNumber", phone number);
                    vendorBundle.putString("location", location);
                    vendorBundle.putString("description", description);*//**//**//**//*
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
                        detailIntent.putExtra("detailsBundle", vendorBundle);*//**//*
                        vendor_name_temp = currentItem.getTitle();
                        detailIntent.putExtra("vendor_id", currentItem.getVendorId());
                        detailIntent.putExtra("offer_id", currentItem.getOfferId());
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
            });*//*
        }*/
    }


}
