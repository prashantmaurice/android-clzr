package com.clozerr.app.Activities.VendorScreens.Subviews;

/**
 * Created by Adarsh on 23-05-2015.
 */

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.clozerr.app.R;

public class MyStampsRecyclerViewAdapter extends RecyclerView.Adapter<MyStampsRecyclerViewAdapter.ListItemViewHolder> {

    private String[] values;
    static Context c;
    Resources reso;

    MyStampsRecyclerViewAdapter(String[] numbers, Context c) {
        this.values = numbers;
        this.c = c;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.stamp_layout,viewGroup,false);
        return new ListItemViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(final ListItemViewHolder viewHolder, int position) {
        ///viewHolder.currentItem = items.get(position);
        String current=values[position];
        //viewHolder.txtvisitno.setText(model.getStamps()+"");
        viewHolder.stampnumber.setText(current);
        viewHolder.stampnumber.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN: {
                        viewHolder.stampnumber.setBackground(reso.getDrawable(R.drawable.cirkbackhover));
                        return true;

                    }
                    case MotionEvent.ACTION_UP:
                        // TODO Auto-generated method stub
                    {
                        viewHolder.stampnumber.setBackground(reso.getDrawable(R.drawable.circularback));
                        return true;
                    }
                }
                return false;
            }
        });
        // viewHolder.txtDist.setText(model.getDesc());

    }

    @Override
    public int getItemCount() {
        return values.length;
    }

    public final static class ListItemViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtvisitno;

        TextView stampnumber;
        ImageView stampcheck;
        /*public MyOffer currentItem;*/

        public ListItemViewHolder(View itemView) {
            super(itemView);
            //txtvisitno = (TextView) itemView.findViewById(R.id.txtNum);
            stampnumber = (TextView) itemView.findViewById(R.id.stampnumber);
            stampcheck = (ImageView) itemView.findViewById(R.id.stampcheck);


        }
    }


}

