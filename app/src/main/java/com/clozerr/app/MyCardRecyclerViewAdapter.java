package com.clozerr.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

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
    public void onBindViewHolder(ListItemViewHolder viewHolder, int position) {
        viewHolder.currentItem = items.get(position);
        CardModel model = items.get(position);
        viewHolder.txtTitle.setText(model.getTitle());
        viewHolder.txtStamps.setText(model.getStampString());
        viewHolder.txtDist.setText(model.getDistanceString());
        Ion.with((viewHolder.imageView))
             //   .placeholder(R.drawable.call)
             //   .error(R.drawable.bat)
                        //    .animateLoad(spinAnimation)
                        //    .animateIn(fadeInAnimation)
                .load(model.getImageId());
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

        public ListItemViewHolder(View itemView) {
            super(itemView);

            txtTitle = (TextView) itemView.findViewById(R.id.textTitle);
            txtStamps = (TextView) itemView.findViewById(R.id.txtCaption);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            txtDist=(TextView)itemView.findViewById(R.id.textDistance);

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