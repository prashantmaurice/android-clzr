package com.clozerr.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by srivatsan on 12/5/15.
 */
public class NearbyFragment extends Fragment {
    /*public static MyFragment getInstance(int Position){
        MyFragment myFragment=new MyFragment();
        Bundle args=new Bundle();
        args.getInt("position",Position);
        myFragment.setArguments(args);
        return myFragment;
    }*/
    static int scolled = 0;
    Context c;
    public static String TOKEN = "";
    static Toolbar mToolbar;
    ObservableRecyclerView mRecyclerView;
    static View swipetab;
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
    static View SearchCard;
    static float SEARCH_CARD_INI_POS = 0;
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        View layout=inflater.inflate(R.layout.activity_nearby_fragment,container,false);
        mRecyclerView = (ObservableRecyclerView) layout.findViewById(R.id.list);
        searchView = (SearchView)layout.findViewById(R.id.searchView);
        SearchCard = layout.findViewById(R.id.card_view);
        SEARCH_CARD_INI_POS = ViewHelper.getTranslationY(SearchCard);
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
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                countDownTimer = new CountDownTimer(1000, 1000) {//CountDownTimer(edittext1.getText()+edittext2.getText()) also parse it to long

                    public void onTick(long millisUntilFinished) {

                    }

                    public void onFinish() {
                        countDownTimer.cancel();
                        String url;

                        if (!query.equals("")) {
                            url = "http://api.clozerr.com/v2/vendor/search/name?access_token=" + TOKEN + "&text=" + query.replace(" ", "%20") + "&latitude=" + Home.lat + "&longitude=" + Home.longi;
                        } else {
                            mCardsLeft = true;
                            mOffset = 0;
                            url = "http://api.clozerr.com/vendor/get/near?latitude=" + Home.lat + "&longitude=" + Home.longi + "&access_token=" + TOKEN
                                    + "&offset=" + mOffset + "&limit=" + INITIAL_LOAD_LIMIT;
                            Log.d("urlsearch", url);
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
                                    if (query.equals("")) {

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
        mToolbar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                ViewTreeObserver obs = mToolbar.getViewTreeObserver();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    obs.removeOnGlobalLayoutListener(this);
                } else {
                    obs.removeGlobalOnLayoutListener(this);
                }
                swipetab.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        ViewTreeObserver obs = swipetab.getViewTreeObserver();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            obs.removeOnGlobalLayoutListener(this);
                        } else {
                            obs.removeGlobalOnLayoutListener(this);
                        }
                        SearchCard.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                            @Override
                            public void onGlobalLayout() {
                                ViewTreeObserver obs = SearchCard.getViewTreeObserver();

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    obs.removeOnGlobalLayoutListener(this);
                                } else {
                                    obs.removeGlobalOnLayoutListener(this);
                                }
                                mRecyclerView.addItemDecoration(new SpaceItemDecoration(dpToPx(12) + (mToolbar.getHeight() + swipetab.getHeight() + SearchCard.getHeight()), 0));
                            }

                        });
                    }

                });
            }

        });
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(c);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //mLayoutManager.offsetChildrenVertical(dpToPx(52));
        mOnScrollListener = new EndlessRecyclerOnScrollListener(
                (LinearLayoutManager)mLayoutManager) {
            @Override
            public void onLoadMore() {
                loadMoreItems();
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                move(dy);
            }
            public void onScrollStateChanged (RecyclerView recyclerView, int newState){
                super.onScrollStateChanged(recyclerView,newState);
                if(newState == 0){
                    if(ViewHelper.getTranslationY(mToolbar)<0 && ViewHelper.getTranslationY(mToolbar)>=-mToolbar.getHeight()/2){
                        showToolbar();
                        return;
                    }else if(ViewHelper.getTranslationY(mToolbar)<-mToolbar.getHeight()/2 && ViewHelper.getTranslationY(mToolbar)>-mToolbar.getHeight()){
                        hideToolbar();
                        return;
                    }
                    if(ViewHelper.getTranslationY(SearchCard)<-mToolbar.getHeight() && ViewHelper.getTranslationY(SearchCard)>=-mToolbar.getHeight()-SearchCard.getHeight()/2){
                        showSearchbar();
                    }
                    else if(ViewHelper.getTranslationY(SearchCard)<-mToolbar.getHeight()-SearchCard.getHeight()/2 && ViewHelper.getTranslationY(SearchCard)>-mToolbar.getHeight()-SearchCard.getHeight()-dpToPx(10)){
                        hideSearchbar();
                    }
                }
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
//            addMargin();
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
    void move(float dy){
        scolled+=dy;
        Log.d("Scrolling", dy + "//" + ViewHelper.getTranslationY(mToolbar) + "//" + mToolbar.getHeight() + "//" + SEARCH_CARD_INI_POS + "//" + ViewHelper.getTranslationY(SearchCard));
        if(ViewHelper.getTranslationY(SearchCard)>=SEARCH_CARD_INI_POS-mToolbar.getHeight() && ViewHelper.getTranslationY(SearchCard)<=SEARCH_CARD_INI_POS)
        if((!(ViewHelper.getTranslationY(mToolbar)<=-mToolbar.getHeight()) && dy>=0) || ((ViewHelper.getTranslationY(mToolbar)<0)&& dy<=0)) {
            if (ViewHelper.getTranslationY(mToolbar) - dy > 0) {
                dy = ViewHelper.getTranslationY(mToolbar);
            }
            if (ViewHelper.getTranslationY(mToolbar)-dy<-mToolbar.getHeight())
                dy = ViewHelper.getTranslationY(mToolbar)+mToolbar.getHeight();
            ViewHelper.setTranslationY(mToolbar, ViewHelper.getTranslationY(mToolbar) - dy);
            ViewHelper.setTranslationY(swipetab, ViewHelper.getTranslationY(swipetab) - dy);
            ViewHelper.setTranslationY(SearchCard, ViewHelper.getTranslationY(SearchCard) - dy);
        }
        if(ViewHelper.getTranslationY(mToolbar)==-mToolbar.getHeight())
            if( (dy >=0 && ViewHelper.getTranslationY(SearchCard)+mToolbar.getHeight()>=-SearchCard.getHeight()-dpToPx(10)) || (dy<=0 && ViewHelper.getTranslationY(SearchCard)<=SEARCH_CARD_INI_POS-mToolbar.getHeight())){
                if(ViewHelper.getTranslationY(SearchCard)-dy<-SearchCard.getHeight()-dpToPx(10)-mToolbar.getHeight()){
                    dy = ViewHelper.getTranslationY(SearchCard)+SearchCard.getHeight()+dpToPx(10)+mToolbar.getHeight();
                }
                if(ViewHelper.getTranslationY(SearchCard)-dy>SEARCH_CARD_INI_POS - mToolbar.getHeight()){
                    dy = ViewHelper.getTranslationY(SearchCard)-SEARCH_CARD_INI_POS+mToolbar.getHeight();
                }
                ViewHelper.setTranslationY(SearchCard, ViewHelper.getTranslationY(SearchCard) - dy);
            }
        /*if((!(swipetab.getTranslationY()<=SWIPE_TAB_INI_POS-swipetab.getHeight()) && dy>=0) || ((swipetab.getTranslationY()<SWIPE_TAB_INI_POS)&& dy<=0)) {
            if (swipetab.getTranslationY() - dy > SWIPE_TAB_INI_POS)
                dy = swipetab.getTranslationY()-SWIPE_TAB_INI_POS;
        }*/
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
    public static void showToolbar() {
        moveToolbar(0);
    }

    private void hideToolbar() {
        moveToolbar(-mToolbar.getHeight());
    }
    private static void moveToolbar(float toTranslationY) {
        if(scolled<2*mToolbar.getHeight()){
            toTranslationY = 0;
        }
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
                ViewHelper.setTranslationY(swipetab, translationY);
                ViewHelper.setTranslationY(SearchCard, translationY);
            }
        });
        animator.start();
    }
    public static void showSearchbar() {
        moveSearchbar(-mToolbar.getHeight());
    }
    public static void showSearchbarToInitial() {
        moveSearchbar(SEARCH_CARD_INI_POS);
    }
    private void hideSearchbar() {
        moveSearchbar(-SearchCard.getHeight() - dpToPx(10) - mToolbar.getHeight());
    }
    private static void moveSearchbar(float toTranslationY) {
        if (toTranslationY==SEARCH_CARD_INI_POS){

        }
        else if(scolled < 2*mToolbar.getHeight()){
            toTranslationY = -mToolbar.getHeight();
        }
        if (ViewHelper.getTranslationY(SearchCard) == toTranslationY) {
            return;
        }
        ValueAnimator animator = ValueAnimator.ofFloat(ViewHelper.getTranslationY(SearchCard), toTranslationY).setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float translationY = (float) animation.getAnimatedValue();

                ViewHelper.setTranslationY(SearchCard, translationY);
            }
        });
        animator.start();
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = c.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

}