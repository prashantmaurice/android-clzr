package com.clozerr.app.Activities.HomeScreens;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.clozerr.app.AsyncGet;
import com.clozerr.app.CardModel;
import com.clozerr.app.EndlessRecyclerOnScrollListener;
import com.clozerr.app.MainApplication;
import com.clozerr.app.MyLocation;
import com.clozerr.app.R;
import com.clozerr.app.SpaceItemDecoration;
import com.clozerr.app.Utils.Router;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

import org.json.JSONArray;

import java.net.URLEncoder;
import java.util.ArrayList;

/**
 *  This is used in HomeScreen activity
 */
public class NearbyFragment extends Fragment {
    
    static int scolled = 0;
    Context c;
    static Toolbar mToolbar;
    ObservableRecyclerView mRecyclerView;
    static View swipetab;
    private NearbyFragmentAdapter mMainPageAdapter;
    private ArrayList<CardModel> mMainCardsList = new ArrayList<>();
    private RecyclerView.LayoutManager mLayoutManager;
    private EndlessRecyclerOnScrollListener mOnScrollListener;
    private ImageView locationimage;
    private boolean mCardsLeft = true;
    private final int ITEMS_PER_PAGE = 7, INITIAL_LOAD_LIMIT = 8;
    View mScrollable;
    SearchView searchView;
    String searchQuery;     //Stores current query String
    CountDownTimer countDownTimer;
    static View SearchCard;
    static float SEARCH_CARD_INI_POS = 0;
    SwipeRefreshLayout mSwipeRefreshLayout;

    
    public static NearbyFragment getInstance(){
        NearbyFragment myFragment=new NearbyFragment();
        return myFragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        View layout=inflater.inflate(R.layout.activity_nearby_fragment,container,false);
        mRecyclerView = (ObservableRecyclerView) layout.findViewById(R.id.list);
        searchView = (SearchView)layout.findViewById(R.id.searchView);
        SearchCard = layout.findViewById(R.id.searchview);
        locationimage=(ImageView)layout.findViewById(R.id.locationunavailable);
        SEARCH_CARD_INI_POS = ViewHelper.getTranslationY(SearchCard);
        mScrollable=getActivity().findViewById(R.id.drawerLayout);
        mToolbar=(Toolbar)getActivity().findViewById(R.id.toolbar_home);
        swipetab=getActivity().findViewById(R.id.tabs);
        final TextView searchHint = (TextView)layout.findViewById(R.id.searchHint);
        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setProgressViewOffset(false, 300, 500);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                mCardsLeft = true;
                mMainCardsList.clear();
                fetchResultsForCurrentLocation();
            }
        });
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
                
                //Search after a small interval so that too many calls wont be made under fast typing
                countDownTimer = new CountDownTimer(1000, 1000) {

                    public void onTick(long millisUntilFinished) {}

                    public void onFinish() {
                        countDownTimer.cancel();
                        searchQuery = query;
                        showToolbar();
                        if (!query.equals("")) {
                            fetchResultsForCurrentLocation();
                        } else {
                            //Refresh the while list as user has removed his query
                            mCardsLeft = true;
                            mMainCardsList.clear();
                            fetchResultsForCurrentLocation();
                        }
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
                                mRecyclerView.addItemDecoration(new SpaceItemDecoration(dpToPx(14) + (mToolbar.getHeight() + swipetab.getHeight() + SearchCard.getHeight()), 0));
                            }

                        });
                    }

                });
            }

        });
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(c);
        mRecyclerView.setLayoutManager(mLayoutManager);
        locationEnabledCheck();

        //mLayoutManager.offsetChildrenVertical(dpToPx(52));
        mOnScrollListener = new EndlessRecyclerOnScrollListener((LinearLayoutManager)mLayoutManager) {
            @Override
            public void onLoadMore() {
                fetchResultsForCurrentLocation();
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
        mMainPageAdapter = new NearbyFragmentAdapter(mMainCardsList, c);
        mRecyclerView.setAdapter(mMainPageAdapter);



        //Load initial cards from cache or start a new request
        final String cards = MainApplication.getInstance().data.userMain.home_cards;
        if(!cards.isEmpty()){
            Log.e("Cached Card", cards);
            mMainCardsList.clear();
            mMainCardsList.addAll(convertRow(cards));
            mMainPageAdapter.notifyDataSetChanged();
        }
        
        //Fetch new batch of results nevertheless
        fetchResultsForCurrentLocation();


        //On location changed, refresh the complete list
        new MyLocation().getLocation(c, new MyLocation.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                Log.e("location stuff", "Location Callback called.");
                try {
                    MainApplication.getInstance().location.setLatitude(location.getLatitude());
                    MainApplication.getInstance().location.setLongitude(location.getLongitude());
                    Log.e("latlong", location.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mMainCardsList.clear();
                fetchResultsForCurrentLocation();
            }

        });



        return layout;
    }
    
    private void fetchResultsForCurrentLocation(){
        String requestSeatch = (searchQuery==null)?null:((searchQuery.equals(""))?null:searchQuery);
        String url = Router.Homescreen.getNearbyRestaurents(MainApplication.getInstance().location,mMainCardsList.size(),ITEMS_PER_PAGE, requestSeatch);
        Log.e("url", url);
        new AsyncGet(c, url, new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                mSwipeRefreshLayout.setRefreshing(false);
                Log.e("result",s);
                if(s==null) {
                    Toast.makeText(c, "No internet connection", Toast.LENGTH_SHORT).show();
                }
                ArrayList<CardModel> CardList = convertRow(s);
                if (CardList.size() != 0) {
                    mMainCardsList.addAll(CardList);
                    mMainPageAdapter.notifyDataSetChanged();
                    MainApplication.getInstance().data.userMain.changeHomeCards(s);
                    Log.e("app", "editing done");
                }
                else {
                    Log.d("app", "no cards to show");
                    mCardsLeft = false;
                }
            }
        });
    }
    
    
    private void move(float dy){
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
                        array.getJSONObject(i).getString("image_base") + URLEncoder.encode(array.getJSONObject(i).getString("resource_name"),"UTF-8"),
                        fid,array.getJSONObject(i).getString("_id"),0,array.getJSONObject(i).getString("caption"),array.getJSONObject(i).getBoolean("active")
                );
                rowItems.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowItems;
    }

    private void locationEnabledCheck() {
        boolean gps_enabled=false ,network_enabled=false;
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        try{
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch(Exception ex){ex.printStackTrace();}
        try{
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch(Exception ex){ex.printStackTrace();}
        //LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if(!gps_enabled && !network_enabled) {
//            locationimage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d("NEARBYFRAGMENT", "onAttach");
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


    @Override
    public void onResume() {
        super.onResume();
        Log.d("NEARBYFRAGMENT", "onResume");
        locationEnabledCheck();
        fetchResultsForCurrentLocation();
    }
}