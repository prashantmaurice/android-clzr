package com.clozerr.app.Activities.VendorScreens.Subviews;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.clozerr.app.Models.RewardsObject;
import com.clozerr.app.R;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.List;

/**
 *  This is one of the fragments user in Vendors page
 */

public class RewardsFragmentAdapter extends RecyclerView.Adapter<RewardsFragmentAdapter.ListItemViewHolder> {

    private List<RewardsObject> items;
    Context c;


    RewardsFragmentAdapter(ArrayList<RewardsObject> modelData, Context c) {
        this.items = modelData;
        this.c = c;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(
        ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.freebies_item_layout,
                viewGroup,
                false);



        return new ListItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ListItemViewHolder viewHolder, int position) {
        final RewardsObject model = items.get(position);
        viewHolder.currentItem = items.get(position);
        viewHolder.name.setText(model.description);
        viewHolder.caption.setText(model.caption);

        if(!model.unlocked) viewHolder.overlay.setVisibility(View.VISIBLE);
        else viewHolder.overlay.setVisibility(View.GONE);

        Ion.with((viewHolder.imageView))
                //   .placeholder(R.drawable.call)
                //   .error(R.drawable.bat)
                //    .animateLoad(spinAnimation)
                //    .animateIn(fadeInAnimation)
                .load(model.image);


        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckInAlertController.openCheckinDirectly(c, model);
            }
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public final static class ListItemViewHolder extends RecyclerView.ViewHolder {
        View view, overlay;
        ImageView imageView;
        TextView name;
        TextView caption;
        public RewardsObject currentItem;

        public ListItemViewHolder(final View itemView) {
            super(itemView);
            view = itemView;
            imageView = (ImageView) itemView.findViewById(R.id.freebieimage);
            name = (TextView)itemView.findViewById(R.id.freebiename);
            caption = (TextView)itemView.findViewById(R.id.freebiedescription);
            overlay = itemView.findViewById(R.id.overlay);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {


//                    Intent intent = new Intent(c,FreebieDescription.class);
//                    intent.putExtra("offerid",currentItem.RewardId);
//                    intent.putExtra("vendorid", VendorActivity.vendorId);
//                    intent.putExtra("caption",currentItem.Caption);
//                    intent.putExtra("description",currentItem.Description);
//                    if( currentItem.Unlocked )
//                        c.startActivity(intent);
////                        openCheckinDirectly();
//                }
//            });
        }

    }

}
