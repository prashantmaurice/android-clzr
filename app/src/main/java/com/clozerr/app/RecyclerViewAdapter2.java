package com.clozerr.app;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Raakesh Kamal on 7/8/2015.
 */
public class RecyclerViewAdapter2 extends RecyclerView.Adapter<RecyclerViewAdapter2.ListItemViewHolder> {
    public ArrayList<MyOffer> rewards;
    public LayoutInflater navinflater;
    private Context context;
    int i;
    public RecyclerViewAdapter2(Context context, ArrayList<MyOffer> rewards) {
        super();
        this.rewards = rewards;
        this.context = context;
    }
    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.freebies_item_layout,
                            parent,
                            false);
            return new ListItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ListItemViewHolder holder, int position) {
      holder.rewardText.setText(rewards.get(position).getCaption());
        holder.smallText.setText(rewards.get(position).getDescription());

    }

    @Override
    public int getItemCount() {
        return 0;
    }
    public final static class ListItemViewHolder extends RecyclerView.ViewHolder{
        ImageView rewardImage;
        TextView rewardText;
        TextView smallText;
        ImageView pin;


        public ListItemViewHolder(final View itemView) {
            super(itemView);
            rewardImage = (ImageView) itemView.findViewById(R.id.freebieimage);
            rewardText = (TextView) itemView.findViewById(R.id.freebiename);
            smallText = (TextView) itemView.findViewById(R.id.freebiedescription);
            pin = (ImageView) itemView.findViewById(R.id.pinimage);
        }
    }
}
