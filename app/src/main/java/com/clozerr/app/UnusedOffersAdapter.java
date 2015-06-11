package com.clozerr.app;

/**
 * Created by Adarsh on 23-05-2015.
 */

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class UnusedOffersAdapter extends RecyclerView.Adapter<UnusedOffersAdapter.ListItemViewHolder> {

    private ArrayList<MyOffer> values;
    static Context c;
    Resources reso;

    UnusedOffersAdapter(ArrayList<MyOffer> offers, Context c) {
        this.values = offers;
        this.c = c;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.freebies_item_layout,viewGroup,false);
        return new ListItemViewHolder(itemView);
    }



    @Override
    public void onBindViewHolder(final ListItemViewHolder viewHolder, int position) {
        ///viewHolder.currentItem = items.get(position);
        MyOffer current=values.get(position);
        //viewHolder.txtvisitno.setText(model.getStamps()+"");
        viewHolder.caption.setText(current.getCaption());
//        viewHolder.stampnumber.setOnTouchListener(new View.OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View arg0, MotionEvent arg1) {
//                switch (arg1.getActionMasked()) {
//                    case MotionEvent.ACTION_DOWN: {
//                        viewHolder.stampnumber.setBackground(reso.getDrawable(R.drawable.cirkbackhover));
//                        return true;
//
//                    }
//                    case MotionEvent.ACTION_UP:
//                        // TODO Auto-generated method stub
//                    {
//                        viewHolder.stampnumber.setBackground(reso.getDrawable(R.drawable.circularback));
//                        return true;
//                    }
//                }
//                return false;
//            }
//        });
        // viewHolder.txtDist.setText(model.getDesc());

    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    public final static class ListItemViewHolder
            extends RecyclerView.ViewHolder {

        TextView caption;
        /*public MyOffer currentItem;*/

        public ListItemViewHolder(View itemView) {
            super(itemView);
            //txtvisitno = (TextView) itemView.findViewById(R.id.txtNum);
            caption = (TextView) itemView.findViewById(R.id.freebiename);

        }
    }


}

