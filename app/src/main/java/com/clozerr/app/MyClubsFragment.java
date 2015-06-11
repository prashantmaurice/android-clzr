package com.clozerr.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class MyClubsFragment extends Fragment implements ObservableScrollViewCallbacks{
    Context c;
    View layout;
    View mScrollable;
    Toolbar mToolbar;
    View swipetab;
    SearchView searchView;
    ObservableRecyclerView mRecyclerView;
    View SearchCard;
    GridLayoutManager gridLayoutManager;
    int length_of_array=0;
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.activity_my_clubs_fragment, container, false);
        mRecyclerView = (ObservableRecyclerView) layout.findViewById(R.id.sliding_list);
        mRecyclerView.setScrollViewCallbacks(this);
        searchView = (SearchView)layout.findViewById(R.id.searchView);
        SearchCard=layout.findViewById(R.id.card_view);
        mScrollable=getActivity().findViewById(R.id.drawerLayout);
        mToolbar=(Toolbar)getActivity().findViewById(R.id.toolbar);
        swipetab=getActivity().findViewById(R.id.tabs);
        final TextView searchHint = (TextView)layout.findViewById(R.id.searchHint);
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchHint.setVisibility(View.GONE);
                searchView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchHint.setVisibility(View.VISIBLE);
                searchView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                return false;
            }
        });
        searchHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
                searchHint.setVisibility(View.GONE);
                searchView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        });
        layout.findViewById(R.id.searchLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
                searchHint.setVisibility(View.GONE);
                searchView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        });

        gridLayoutManager = new GridLayoutManager(c,2);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        Log.e("app", "in slidingmycards; set recycler");


        SharedPreferences status = c.getSharedPreferences("USER", 0);
        String TOKEN = status.getString("token", "");

        String urlVisited = "http://api.clozerr.com/vendor/get/visitedV2?access_token=" +TOKEN;
        Log.e("urlslide", urlVisited);

        new AsyncGet(c, urlVisited , new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                //  t1.setText(s);

                Log.e("resultSlide", s);

                MyCardRecyclerViewAdapter Cardadapter = new MyCardRecyclerViewAdapter(convertRowMyCard(s), c);
                mRecyclerView.setAdapter(Cardadapter);
                try
                {
                    Tracker t = ((Analytics) c.getApplicationContext()).getTracker(Analytics.TrackerName.APP_TRACKER);

                    t.setScreenName("MyCards");

                    t.send(new HitBuilders.AppViewBuilder().build());
                }
                catch(Exception  e)
                {
                    Toast.makeText(c, "Error" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                if(s==null) {
                    Toast.makeText(c,"No internet connection",Toast.LENGTH_SHORT).show();
                }
                //l1.setAdapter(adapter);
            }
        });


        return layout;
    }
    private ArrayList<CardModel> convertRowMyCard(String s) {
        ArrayList<CardModel> rowItems = new ArrayList<>();
        JSONObject temp;
        JSONArray array;
        try {
            //Log.e("stringfunction", s);
            Log.e("stringfunction", "processing..");
            temp = new JSONObject(s);
            array = temp.getJSONArray("data");

            ImageView loyaltyempty=(ImageView)layout.findViewById(R.id.loyaltyempty);
            if(array.length()==0){
                Log.e("arrayLength", array.length()+"");
                loyaltyempty.setVisibility(View.VISIBLE);
            }
            length_of_array = array.length();
            for(int i = 0 ; i < array.length() ; i++){
                loyaltyempty.setVisibility(View.GONE);
                Log.e("stringfunction", "processing..");
                CardModel item = new CardModel(
                        array.getJSONObject(i).getString("name"),
                        array.getJSONObject(i).getString("phone"),
                        array.getJSONObject(i).getString("description"),
                        array.getJSONObject(i).getJSONArray("offers"),
                        array.getJSONObject(i).getJSONArray("location").getDouble(0),
                        array.getJSONObject(i).getJSONArray("location").getDouble(1),
                        array.getJSONObject(i).getString("image"),
                        array.getJSONObject(i).getString("fid"),
                        array.getJSONObject(i).getString("_id"),
                        array.getJSONObject(i).getInt("stamps")
                );
                Log.e("stringfunction", "processed");
                rowItems.add(item);
            }
        } catch (Exception e) {
            Log.e("uhoh",e.getMessage());
            e.printStackTrace();
        }
        return rowItems;
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        c=activity;
    }

    @Override
    public void onScrollChanged(int i, boolean b, boolean b2) {

    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        ActionBar ab = ((ActionBarActivity)getActivity()).getSupportActionBar();
        if (scrollState == ScrollState.UP) {
            if (toolbarIsShown() && length_of_array>(int)((gridLayoutManager.findLastVisibleItemPosition()- gridLayoutManager.findFirstVisibleItemPosition())*1.5))
            {
                hideToolbar();
            }
            if(searchbarIsShown() && length_of_array>(int)((gridLayoutManager.findLastVisibleItemPosition()- gridLayoutManager.findFirstVisibleItemPosition())*1.5) ){
                hideSearchbar();
            }
        } else if (scrollState == ScrollState.DOWN) {
            if (toolbarIsHidden()) {
                showToolbar();
            }
            if (searchbarIsHidden()) {
                showSearchbar();
            }
        }else{
            if (toolbarIsHidden()) {
                showToolbar();
            }
            if (searchbarIsHidden()) {
                showSearchbar();
            }
        }
    }
    private boolean toolbarIsShown() {
        // Toolbar is 0 in Y-axis, so we can say it's shown.
        return ViewHelper.getTranslationY(mToolbar) == 0;
    }

    private boolean toolbarIsHidden() {
        // Toolbar is outside of the screen and absolute Y matches the height of it.
        // So we can say it's hidden.
        return ViewHelper.getTranslationY(mToolbar) == -mToolbar.getHeight();
    }
    private void showToolbar() {
        moveToolbar(0);
    }

    private void hideToolbar() {
        moveToolbar(-mToolbar.getHeight());
    }
    private void moveToolbar(float toTranslationY) {
        if (ViewHelper.getTranslationY(mToolbar) == toTranslationY) {
            return;
        }
        ValueAnimator animator = ValueAnimator.ofFloat(ViewHelper.getTranslationY(mToolbar), toTranslationY).setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float translationY = (float) animation.getAnimatedValue();

                ViewHelper.setTranslationY(mToolbar, translationY);
                ViewHelper.setTranslationY( mScrollable, translationY);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) ( mScrollable).getLayoutParams();
                lp.height = (int) -translationY + getScreenHeight() - lp.topMargin;
                ((View) mScrollable).requestLayout();
            }
        });
        animator.start();
    }
    private boolean searchbarIsShown() {
        // Toolbar is 0 in Y-axis, so we can say it's shown.
        return ViewHelper.getTranslationY(SearchCard) == 0;
    }

    private boolean searchbarIsHidden() {
        // Toolbar is outside of the screen and absolute Y matches the height of it.
        // So we can say it's hidden.
        return ViewHelper.getTranslationY(SearchCard) == -SearchCard.getHeight()-dpToPx(12);
    }
    private void showSearchbar() {
        moveSearchbar(0);
    }

    private void hideSearchbar() {
        moveSearchbar(-SearchCard.getHeight()-dpToPx(12));
    }
    private void moveSearchbar(float toTranslationY) {
        if (ViewHelper.getTranslationY(SearchCard) == toTranslationY) {
            return;
        }
        ValueAnimator animator = ValueAnimator.ofFloat(ViewHelper.getTranslationY(SearchCard), toTranslationY).setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float translationY = (float) animation.getAnimatedValue();

                ViewHelper.setTranslationY(SearchCard, translationY);
                ViewHelper.setTranslationY( mRecyclerView, translationY);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) ( mRecyclerView).getLayoutParams();
                lp.height = (int) -translationY + getScreenHeight()-SearchCard.getHeight() -lp.topMargin;
                ((View) mScrollable).requestLayout();
            }
        });
        animator.start();
    }

    private int getScreenHeight() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        return height - swipetab.getHeight()-searchView.getHeight()+dpToPx(0);
    }
    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = c.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }
}
