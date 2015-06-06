package com.clozerr.app;

/**
 * Created by Adarsh on 6/4/2015.
 */

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ListItemViewHolder> {

    private String[] numbers;
    static Context c;

    GalleryAdapter(String[] modelData, Context c) {
        this.numbers = modelData;
        this.c = c;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).inflate(R.layout.gallery_item,viewGroup,
                        false);
        return new ListItemViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(ListItemViewHolder viewHolder, int position) {
        viewHolder.currentItem = numbers[position];
        //CategoryModel model = items.get(position);
    }

    @Override
    public int getItemCount() {
        return numbers.length;
    }

    public final static class ListItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public String currentItem;

        public ListItemViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.galleryimage);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //GALLERY ITEM CLICK
                }
            });
        }
    }


}
