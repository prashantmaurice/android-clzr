package com.clozerr.app;

import android.content.SharedPreferences;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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


public class CatogoryDetail extends ActionBarActivity implements ObservableScrollViewCallbacks{

    public static String TOKEN = "";
    Toolbar mToolbar;
    private RecyclerViewAdapter mMainPageAdapter;
    private ArrayList<CardModel> mMainCardsList;
    private RecyclerView.LayoutManager mLayoutManager;
    private EndlessRecyclerOnScrollListener mOnScrollListener;
    private int mOffset;
    private boolean mCardsLeft = true;
    private final int ITEMS_PER_PAGE = 7, INITIAL_LOAD_LIMIT = 8;
    View mScrollable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catogory_detail);
        final ObservableRecyclerView mRecyclerView = (ObservableRecyclerView) findViewById(R.id.list);
        mRecyclerView.setScrollViewCallbacks(this);
        final SearchView searchView = (SearchView)findViewById(R.id.searchView);
        mScrollable=findViewById(R.id.layout);
        mToolbar=(Toolbar)findViewById(R.id.toolbar);
        final TextView searchHint = (TextView)findViewById(R.id.searchHint);
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
        findViewById(R.id.searchLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
                searchHint.setVisibility(View.GONE);
                searchView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
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


        SharedPreferences status = getSharedPreferences("USER", 0);
        final String cards = status.getString("home_cards", "");
        if(!cards.equals("")){
            Log.e("Cached Card", cards);
            mMainCardsList = convertRow(cards);
            mMainPageAdapter = new RecyclerViewAdapter(mMainCardsList, this);
            mRecyclerView.setAdapter(mMainPageAdapter);
        } else {
            mOffset = 0;
            String url = "http://api.clozerr.com/vendor/get/near?latitude=" + Home.lat + "&longitude=" + Home.longi + "&access_token=" + TOKEN
                    + "&offset=" + mOffset + "&limit=" + INITIAL_LOAD_LIMIT;
            Log.e("url", url);
            new AsyncGet(this, url, new AsyncGet.AsyncResult() {
                @Override
                public void gotResult(String s) {
                    Log.e("result",s);
                    if(s==null) {
                        Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                    }

                    ArrayList<CardModel> CardList = convertRow(s);
                    if (CardList.size() != 0) {
                        mMainCardsList = CardList;
                        mMainPageAdapter = new RecyclerViewAdapter(mMainCardsList, CatogoryDetail.this);
                        mRecyclerView.setAdapter(mMainPageAdapter);
                        final SharedPreferences.Editor editor = getSharedPreferences("USER", 0).edit();
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


        new MyLocation().getLocation(this, new MyLocation.LocationResult(){
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
                SharedPreferences status = getSharedPreferences("USER", 0);
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

                new AsyncGet(CatogoryDetail.this, url, new AsyncGet.AsyncResult() {
                    @Override
                    public void gotResult(String s) {
                        ArrayList<CardModel> CardList = convertRow(s);
                        if(CardList.size()!=0){
                            mMainCardsList = CardList;
                            mMainPageAdapter = new RecyclerViewAdapter(mMainCardsList,CatogoryDetail.this );
                            mRecyclerView.setAdapter(mMainPageAdapter);

                            final SharedPreferences.Editor editor = getSharedPreferences("USER", 0).edit();
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
            new AsyncGet(CatogoryDetail.this, url, new AsyncGet.AsyncResult() {
                @Override
                public void gotResult(String s) {
                    Log.e("result", s);
                    if (s == null) {
                        Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                    }
                    ArrayList<CardModel> CardList = convertRow(s);
                    if (CardList.size() != 0) {
                        mMainCardsList.addAll(convertRow(s));
                        final SharedPreferences.Editor editor = getSharedPreferences("USER", 0).edit();
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
                Log.e("description", vendorDescription);
                CardModel item = new CardModel(
                        array.getJSONObject(i).getString("name"),
                        phonenumber, vendorDescription,
                        array.getJSONObject(i).getJSONArray("offers"),
                        array.getJSONObject(i).getJSONArray("location").getDouble(0),
                        array.getJSONObject(i).getJSONArray("location").getDouble(1),
                        array.getJSONObject(i).getString("image"),
                        array.getJSONObject(i).getString("fid"),array.getJSONObject(i).getString("_id"),0
                );
                rowItems.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowItems;
    }


    @Override
    public void onScrollChanged(int i, boolean b, boolean b2) {

    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

        if (scrollState == ScrollState.UP) {
            if (toolbarIsShown()) {
                hideToolbar();
            }
        } else if (scrollState == ScrollState.DOWN) {
            if (toolbarIsHidden()) {
                showToolbar();
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

    private int getScreenHeight() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        return height;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_catogory_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
