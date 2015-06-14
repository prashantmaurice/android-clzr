package com.clozerr.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by srivatsan on 12/5/15.
 */
public class NearbyFragment extends Fragment implements ObservableScrollViewCallbacks {
    /*public static MyFragment getInstance(int Position){
        MyFragment myFragment=new MyFragment();
        Bundle args=new Bundle();
        args.getInt("position",Position);
        myFragment.setArguments(args);
        return myFragment;
    }*/
    Context c;
    public static String TOKEN = "";
    Toolbar mToolbar;
    ObservableRecyclerView mRecyclerView;
    View swipetab;
    private RecyclerViewAdapter mMainPageAdapter;
    private ArrayList<CardModel> mMainCardsList;
    private RecyclerView.LayoutManager mLayoutManager;
    private EndlessRecyclerOnScrollListener mOnScrollListener;
    private int mOffset;
    private boolean mCardsLeft = true;
    private final int ITEMS_PER_PAGE = 7, INITIAL_LOAD_LIMIT = 8;
    View mScrollable;
    SearchView searchView;
    CountDownTimer countDownTimer;
    View SearchCard;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        View layout=inflater.inflate(R.layout.activity_nearby_fragment,container,false);
        mRecyclerView = (ObservableRecyclerView) layout.findViewById(R.id.list);
        mRecyclerView.setScrollViewCallbacks(this);
        searchView = (SearchView)layout.findViewById(R.id.searchView);
        SearchCard = layout.findViewById(R.id.card_view);
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
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(final String query) {
                if(countDownTimer!=null) {
                    countDownTimer.cancel();
                }
                countDownTimer = new CountDownTimer(1000, 1000) {//CountDownTimer(edittext1.getText()+edittext2.getText()) also parse it to long

                    public void onTick(long millisUntilFinished) {

                    }

                    public void onFinish() {
                        countDownTimer.cancel();
                        String url;

                        if(!query.equals("")) {
                            url = "http://api.clozerr.com/v2/vendor/search/name?access_token=" + TOKEN + "&text=" + query.replace(" ","%20") + "&latitude=" + Home.lat + "&longitude=" + Home.longi;
                        }
                        else{
                            mCardsLeft = true;
                            mOffset = 0;
                            url = "http://api.clozerr.com/vendor/get/near?latitude=" + Home.lat + "&longitude=" + Home.longi + "&access_token=" + TOKEN
                                    + "&offset=" + mOffset + "&limit=" + INITIAL_LOAD_LIMIT;
                            Log.d("urlsearch",url);
                        }

                        new AsyncGet(c, url, new AsyncGet.AsyncResult() {
                            @Override
                            public void gotResult(String s) {
                                Log.e("result", s);
                                if (s == null) {
                                    Toast.makeText(c, "No internet connection", Toast.LENGTH_SHORT).show();
                                }
                                ArrayList<CardModel> CardList = convertRow(s);
                                if (CardList.size() != 0) {

                                    mMainCardsList = CardList;
                                    mMainPageAdapter = new RecyclerViewAdapter(mMainCardsList, c);
                                    mRecyclerView.setAdapter(mMainPageAdapter);
                                    if(query.equals("")) {

                                        mRecyclerView.setOnScrollListener(mOnScrollListener);
                                    }
                                    final SharedPreferences.Editor editor = c.getSharedPreferences("USER", 0).edit();
                                    editor.putString("home_cards", s);
                                    editor.apply();
                                    Log.e("app", "editing done");
                                } else {
                                    Log.d("app", "no cards to show");
                                    mCardsLeft = false;
                                }
                            }
                        });
                    }
                }.start();
                return false;
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(c));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(c);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mOnScrollListener = new EndlessRecyclerOnScrollListener(
                (LinearLayoutManager)mLayoutManager) {
            @Override
            public void onLoadMore() {
                loadMoreItems();
            }
        };
        mRecyclerView.setOnScrollListener(mOnScrollListener);



        //startService(new Intent(this, LocationService.class));
        Home.lat = 13;
        Home.longi = 80.2;


        SharedPreferences status = c.getSharedPreferences("USER", 0);
        final String cards = status.getString("home_cards", "");
        if(!cards.equals("")){
            Log.e("Cached Card", cards);
            mMainCardsList = convertRow(cards);
            mMainPageAdapter = new RecyclerViewAdapter(mMainCardsList, c);
            mRecyclerView.setAdapter(mMainPageAdapter);
        } else {
            mOffset = 0;
            String url = "http://api.clozerr.com/vendor/get/near?latitude=" + Home.lat + "&longitude=" + Home.longi + "&access_token=" + TOKEN
                    + "&offset=" + mOffset + "&limit=" + INITIAL_LOAD_LIMIT;
            Log.e("url", url);
            new AsyncGet(c, url, new AsyncGet.AsyncResult() {
                @Override
                public void gotResult(String s) {
                    Log.e("result",s);
                    if(s==null) {
                        Toast.makeText(c, "No internet connection", Toast.LENGTH_SHORT).show();
                    }

                    ArrayList<CardModel> CardList = convertRow(s);
                    if (CardList.size() != 0) {
                        mMainCardsList = CardList;
                        mMainPageAdapter = new RecyclerViewAdapter(mMainCardsList, c);
                        mRecyclerView.setAdapter(mMainPageAdapter);

                        final SharedPreferences.Editor editor = c.getSharedPreferences("USER", 0).edit();
                        editor.putString("home_cards", s);
                        editor.apply();
                        Log.e("app", "editing done");
                    }
                    else {
                        Log.d("app", "no cards to show");
                        mCardsLeft = false;
                    }
                }
            });
        }


        new MyLocation().getLocation(c, new MyLocation.LocationResult(){
            @Override
            public void gotLocation (Location location) {
                Log.e("location stuff","Location Callback called.");
                try{
                    Home.lat=location.getLatitude();
                    Home.longi=location.getLongitude();
                    Log.e("lat", Home.lat + "");
                    Log.e("long", Home.longi + "");
                }catch (Exception e){
                    e.printStackTrace();
                }
                SharedPreferences status = c.getSharedPreferences("USER", 0);
                TOKEN = status.getString("token", "");
                String url;
                mOffset = 0;
                if(!TOKEN.equals(""))
                    url = "http://api.clozerr.com/vendor/get/near?latitude="+Home.lat+"&longitude="+Home.longi+"&access_token="+TOKEN
                            + "&offset=" + mOffset + "&limit=" + INITIAL_LOAD_LIMIT;
                else
                    url = "http://api.clozerr.com/vendor/get/near?latitude="+Home.lat+"&longitude="+Home.longi
                            + "&offset=" + mOffset + "&limit=" + INITIAL_LOAD_LIMIT;
                Log.e("url", url);

                new AsyncGet(c, url, new AsyncGet.AsyncResult() {
                    @Override
                    public void gotResult(String s) {
                        ArrayList<CardModel> CardList = convertRow(s);
                        if(CardList.size()!=0){
                            mMainCardsList = CardList;
                            mMainPageAdapter = new RecyclerViewAdapter(mMainCardsList, c);
                            mRecyclerView.setAdapter(mMainPageAdapter);

                            final SharedPreferences.Editor editor = c.getSharedPreferences("USER", 0).edit();
                            editor.putString("home_cards", s);
                            editor.apply();
                        }
                        else {
                            mCardsLeft = false;
                        }
                    }
                });
            }

        });

        return layout;
    }
    public void loadMoreItems() {
        Log.e("load", "in loadMoreItems()");
        if (mCardsLeft) {
            mOffset += (mOffset == 0) ? INITIAL_LOAD_LIMIT : ITEMS_PER_PAGE;
            String url = "";
            if (!TOKEN.equals(""))
                url = "http://api.clozerr.com/vendor/get/near?latitude=" + Home.lat + "&longitude=" + Home.longi + "&access_token=" + TOKEN
                        + "&offset=" + mOffset + "&limit=" + ITEMS_PER_PAGE;
            else
                url = "http://api.clozerr.com/vendor/get/near?latitude=" + Home.lat + "&longitude=" + Home.longi
                        + "&offset=" + mOffset + "&limit=" + ITEMS_PER_PAGE;
            Log.e("url", url);
            new AsyncGet(c, url, new AsyncGet.AsyncResult() {
                @Override
                public void gotResult(String s) {
                    Log.e("result", s);
                    if (s == null) {
                        Toast.makeText(c, "No internet connection", Toast.LENGTH_SHORT).show();
                    }
                    ArrayList<CardModel> CardList = convertRow(s);
                    if (CardList.size() != 0) {
                        mMainCardsList.addAll(convertRow(s));
                        final SharedPreferences.Editor editor = c.getSharedPreferences("USER", 0).edit();
                        editor.putString("home_cards", s);
                        editor.apply();
                        Log.e("app", "editing done");
                        mMainPageAdapter.notifyDataSetChanged();
                        //Toast.makeText(getApplicationContext(), "More items ready", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("app", "no cards to show");
                        mCardsLeft = false;
                        mOffset = mMainCardsList.size();
                    }
                }
            });
        }
    }
    private ArrayList<CardModel> convertRow(String s) {
        ArrayList<CardModel> rowItems = new ArrayList<>();
        JSONArray array;
        try {
            array = new JSONArray(s);
            for(int i = 0 ; i < array.length() ; i++){
                String phonenumber;
                try {
                    phonenumber = array.getJSONObject(i).getString("phonenumber");
                }
                catch(Exception e)
                {
                    phonenumber="0123456789";
                }
                String vendorDescription;
                try {
                    vendorDescription = array.getJSONObject(i).getString("description");
                }
                catch(Exception e)
                {
                    vendorDescription="No Restaurant Description Available Now";
                }
                String fid;
                try {
                    fid=array.getJSONObject(i).getString("fid");
                }catch (Exception e){
                    fid="";
                }
                JSONArray offers;
                try{
                    offers = array.getJSONObject(i).getJSONArray("offers");
                }catch (Exception e){
                    offers = new JSONArray("[{}]");
                }
                Log.e("description", vendorDescription);
                CardModel item = new CardModel(
                        array.getJSONObject(i).getString("name"),
                        phonenumber, vendorDescription,
                        offers,
                        array.getJSONObject(i).getJSONArray("location").getDouble(0),
                        array.getJSONObject(i).getJSONArray("location").getDouble(1),
                        array.getJSONObject(i).getString("image"),
                        fid,array.getJSONObject(i).getString("_id"),0
                );
                rowItems.add(item);
            }
        } catch (Exception e) {
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
            if (toolbarIsShown()) {
                hideToolbar();
            }
            if (searchbarIsShown()) {
                hideSearchbar();
            }
        } else if (scrollState == ScrollState.DOWN) {
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
                float x=translationY;

                ViewHelper.setTranslationY( mScrollable, x);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) ( mScrollable).getLayoutParams();
                lp.height = (int) -x + getScreenHeight() - lp.topMargin;
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