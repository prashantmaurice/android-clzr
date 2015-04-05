package com.clozerr.app;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Girish on 1/7/2015.
 */
public class RecyclerViewAdapter1 extends RecyclerView.Adapter<RecyclerViewAdapter1.ListItemViewHolder> {

    private List<MyOffer> items;
    static Context c;

    RecyclerViewAdapter1(List<MyOffer> modelData, Context c) {
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
                inflate(R.layout.offer_list,
                        viewGroup,
                        false);
        return new ListItemViewHolder(itemView);
    }



    @Override
    public void onBindViewHolder(ListItemViewHolder viewHolder, int position) {
        ///viewHolder.currentItem = items.get(position);
        MyOffer model = items.get(position);
        //viewHolder.txtvisitno.setText(model.getStamps()+"");
        viewHolder.txtoffer.setText(model.getCaption());

        // viewHolder.txtDist.setText(model.getDesc());

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public final static class ListItemViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtvisitno;

        TextView txtoffer;
        /*public MyOffer currentItem;*/

        public ListItemViewHolder(View itemView) {
            super(itemView);
            //txtvisitno = (TextView) itemView.findViewById(R.id.txtNum);
            txtoffer = (TextView) itemView.findViewById(R.id.txtOffer);
        }
    }


}
