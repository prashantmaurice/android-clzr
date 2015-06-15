package com.clozerr.app;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by srivatsan on 15/6/15.
 */
public class SpaceItemDecoration extends RecyclerView.ItemDecoration {
    private int space;
    private int layoutmanager;
    public SpaceItemDecoration(int space,int layoutmanager) {
        this.space = space;
        this.layoutmanager = layoutmanager;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        // Add top margin only for the first item to avoid double space between items
        if(parent.getChildPosition(view) == 0)
            outRect.top = space;
        if(layoutmanager==1 && parent.getChildPosition(view)==1){
            outRect.top = space;
        }
    }
}
