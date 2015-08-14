package com.clozerr.app.Activities.VendorScreens.Subviews;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.clozerr.app.Models.RewardsObject;
import com.clozerr.app.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyStampsFragmentAdapter extends RecyclerView.Adapter<MyStampsFragmentAdapter.ListItemViewHolder> {
    private static final String TAG = "MyOffersRVAdapter";
    //private List<MyOffersCardModel> items;
    private ArrayList<RewardsObject> mItems;
    private ArrayList<RewardsObject> generatedItems = new ArrayList<>();
    static Context c;
    public int currentStamps;
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

    public MyStampsFragmentAdapter(ArrayList<RewardsObject> offers, Context context) {
        c = context;
        Log.e(TAG, "size - " + offers.size());
        mItems = offers;
        generatedItems.clear();
        generatedItems.addAll(createRestOfTheStamps(mItems));
    }
    public ArrayList<RewardsObject> createRestOfTheStamps(ArrayList<RewardsObject> originalItems){
        ArrayList<RewardsObject> result = new ArrayList<>();
        Map<Integer,RewardsObject> map = new HashMap<>();

        //map available stamps
        int maxStampNum = 0; //to store what is the total number of stamps
        for(RewardsObject reward : originalItems){
            map.put(reward.stamps,reward);
            if(reward.stamps>maxStampNum) maxStampNum = reward.stamps;
        }

        //generate stamps
        for(int i=1;i<=maxStampNum;i++){
            if(map.containsKey(i)){
                //This is a reward stamp
                result.add(map.get(i));
            }else{
                //this is a generated empty stamp

                //add Ui related dummy variables
                RewardsObject rewardDummy = new RewardsObject();
                rewardDummy.caption = "";
                rewardDummy.stamps = i;
                result.add(rewardDummy);
            }
        }
        return result;
    }


    //This s called from parent as we need to generate Custom Array ourselves
    public void notifyDataSetChangedCustom() {
        generatedItems.clear();
        generatedItems.addAll(createRestOfTheStamps(mItems));
        super.notifyDataSetChanged();
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(
            ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).
                /*inflate(R.layout.offers_card,*/
                inflate(R.layout.stamp_layout,
                        viewGroup,
                        false);
        return new ListItemViewHolder(itemView);
    }



    @Override
    public void onBindViewHolder(final ListItemViewHolder viewHolder, int position) {

        RewardsObject currentItem = generatedItems.get(position);
        viewHolder.mCaptionView.setText(currentItem.caption);
        viewHolder.stampnumber.setText(String.valueOf(currentItem.stamps));
        //viewHolder.stampnumber.setBackgroundResource(R.drawable.cirkbackhover);
        if(currentItem.stamps <= currentStamps) {
            viewHolder.stampnumber.setTextColor(Color.WHITE);
            //viewHolder.stampcheck.setVisibility(View.VISIBLE);
            viewHolder.stampnumber.setBackgroundResource(R.drawable.cirkbackhover);
        }

    }

    @Override
    public int getItemCount() {
        return generatedItems.size();
    }

    public final static class ListItemViewHolder extends RecyclerView.ViewHolder {
        public ImageView mMainImageView, mOptionalImageView, stampcheck;
        public TextView mCaptionView, stampnumber;

        public ListItemViewHolder(final View itemView) {
            super(itemView);

            //mMainImageView = (ImageView) itemView.findViewById(R.id.offer_label);
            // find optional view
            mCaptionView = (TextView) itemView.findViewById(R.id.freebiecontent);
            stampnumber = (TextView) itemView.findViewById(R.id.stampnumber);
            stampcheck = (ImageView) itemView.findViewById(R.id.stampcheck);
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
                        vendorBundle.putString("distance", currentItem.getDistanceString());
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
