package com.clozerr.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import java.util.List;

/**
 * Created by Adarsh on 6/17/2015.
 */

public class RewardsAdapter extends RecyclerView.Adapter<RewardsAdapter.ListItemViewHolder> {

    private List<RewardItem> items;
    static Context c;
    RewardsAdapter(List<RewardItem> modelData, Context c) {
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
                inflate(R.layout.freebies_item_layout,
                        viewGroup,
                        false);
        return new ListItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ListItemViewHolder viewHolder, int position) {
        viewHolder.currentItem = items.get(position);
        RewardItem model = items.get(position);
        viewHolder.name.setText(model.Name);
        viewHolder.caption.setText(model.Caption);
        Ion.with((viewHolder.imageView))
                //   .placeholder(R.drawable.call)
                //   .error(R.drawable.bat)
                //    .animateLoad(spinAnimation)
                //    .animateIn(fadeInAnimation)
                .load(model.Image);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public final static class ListItemViewHolder
            extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView name;
        TextView caption;
        public RewardItem currentItem;

        public ListItemViewHolder(final View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.freebieimage);
            name = (TextView)itemView.findViewById(R.id.freebiename);
            caption = (TextView)itemView.findViewById(R.id.freebiedescription);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(c,FreebieDescription.class);
                    intent.putExtra("offerid",currentItem.RewardId);
                    intent.putExtra("vendorid",VendorActivity.vendorId);
                    intent.putExtra("caption",currentItem.Caption);
                    intent.putExtra("description",currentItem.Description);
                }
            });
        }
    }
}