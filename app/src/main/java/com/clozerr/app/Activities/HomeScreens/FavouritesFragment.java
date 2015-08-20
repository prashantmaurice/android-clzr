package com.clozerr.app.Activities.HomeScreens;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.clozerr.app.AsyncGet;
import com.clozerr.app.CardModel;
import com.clozerr.app.Handlers.LocalBroadcastHandler;
import com.clozerr.app.R;
import com.clozerr.app.SpaceItemDecoration;
import com.clozerr.app.Utils.Router;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class FavouritesFragment extends Fragment {
    String TAG = "MYCLUBSFRAGMENT";
    Context c;
    View layout;
    View mScrollable;
    static Toolbar mToolbar;
    static View swipetab;
    SearchView searchView;
    static int scolled = 0;
    ObservableRecyclerView mRecyclerView;
    static View SearchCard;
    GridLayoutManager gridLayoutManager;
    int length_of_array = 0;
    static float SEARCH_CARD_INI_POS = 0;
    public static ArrayList<CardModel> rowItems = new ArrayList<>();
    FavouritesRecyclerViewAdapter cardadapter;
    ImageButton like;
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.activity_my_clubs_fragment, container, false);
        mRecyclerView = (ObservableRecyclerView) layout.findViewById(R.id.sliding_list);
        searchView = (SearchView) layout.findViewById(R.id.searchView);
        SearchCard = layout.findViewById(R.id.searchview);
        SEARCH_CARD_INI_POS = ViewHelper.getTranslationY(SearchCard);
        mScrollable = getActivity().findViewById(R.id.drawerLayout);
        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_home);
        swipetab = getActivity().findViewById(R.id.tabs);
        like = (ImageButton)getActivity().findViewById(R.id.like);

        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setProgressViewOffset(false, 300, 500);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshContent();
            }
        });

        final TextView searchHint = (TextView) layout.findViewById(R.id.searchHint);
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

        gridLayoutManager = new GridLayoutManager(c, 2);
        mRecyclerView.setLayoutManager(gridLayoutManager);
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
                                mRecyclerView.addItemDecoration(new SpaceItemDecoration(dpToPx(14) + (mToolbar.getHeight() + swipetab.getHeight() + SearchCard.getHeight()), 1));
                            }

                        });
                    }

                });
            }

        });
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == 0) {
                    if (ViewHelper.getTranslationY(mToolbar) < 0 && ViewHelper.getTranslationY(mToolbar) >= -mToolbar.getHeight() / 2) {
                        showToolbar();
                        return;
                    } else if (ViewHelper.getTranslationY(mToolbar) < -mToolbar.getHeight() / 2 && ViewHelper.getTranslationY(mToolbar) > -mToolbar.getHeight()) {
                        hideToolbar();
                        return;
                    }
                    if (ViewHelper.getTranslationY(SearchCard) < -mToolbar.getHeight() && ViewHelper.getTranslationY(SearchCard) >= -mToolbar.getHeight() - SearchCard.getHeight() / 2) {
                        showSearchbar();
                    } else if (ViewHelper.getTranslationY(SearchCard) < -mToolbar.getHeight() - SearchCard.getHeight() / 2 && ViewHelper.getTranslationY(SearchCard) > -mToolbar.getHeight() - SearchCard.getHeight() - dpToPx(10)) {
                        hideSearchbar();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                move(dy);
            }
        });

        //Add local broadcast listeners
        LocalBroadcastManager.getInstance(c).registerReceiver(mMyClubsUpdateReceiver,
            new IntentFilter(LocalBroadcastHandler.MYCLUBS_CHANGED));

        cardadapter = new FavouritesRecyclerViewAdapter(rowItems, c);
        mRecyclerView.setAdapter(cardadapter);
        refreshContent();

        return layout;
    }


    private BroadcastReceiver mMyClubsUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"onReceive : mMyClubsUpdateReceiverD");
            refreshContent();
        }
    };

    @Override
    public void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(c).unregisterReceiver(mMyClubsUpdateReceiver);
        super.onDestroy();
    }

    private void refreshContent(){
        String urlFavorites = Router.Myclubs.getMyclubs();
        Log.e("TEST2","sending : "+urlFavorites);
        new AsyncGet(c, urlFavorites, new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                mSwipeRefreshLayout.setRefreshing(false);
                Log.e("TEST2","result : "+s);
                rowItems.clear();
                rowItems.addAll(convertRowMyCard(s));
                cardadapter.notifyDataSetChanged();
                if (s == null) {
                    Toast.makeText(c, "No internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private List<CardModel> convertRowMyCard(String s) {
        List<CardModel> resultList = new ArrayList<>();
        JSONObject temp;
        JSONArray array;
        try {
            //Log.e("stringfunction", s);
            //Log.e("stringfunction", "processing..");
            //temp = new JSONObject(s);
            array = new JSONArray(s);

            ImageView loyaltyempty = (ImageView) layout.findViewById(R.id.loyaltyempty);
            if (array.length() == 0) {
                Log.e("arrayLength", array.length() + "");
                loyaltyempty.setVisibility(View.VISIBLE);
            }
            length_of_array = array.length();
            for (int i = 0; i < array.length(); i++) {
                loyaltyempty.setVisibility(View.GONE);
                Log.e("stringfunction", "processing..");
                CardModel item = new CardModel(
                        array.getJSONObject(i).getString("name"),
                        array.getJSONObject(i).getString("phone"),
                        array.getJSONObject(i).getString("description"),
                        array.getJSONObject(i).getJSONArray("offers"),
                        array.getJSONObject(i).getJSONArray("location").getDouble(0),
                        array.getJSONObject(i).getJSONArray("location").getDouble(1),
                        array.getJSONObject(i).getString("image_base") + URLEncoder.encode(array.getJSONObject(i).getString("resource_name"), "UTF-8"),
                        array.getJSONObject(i).getString("fid"),
                        array.getJSONObject(i).getString("_id"),
                        //array.getJSONObject(i).getInt("stamps")
                        0,"", true
                );
                Log.e("stringfunction", "processed");
                resultList.add(item);
            }
        } catch (Exception e) {
            Log.e("uhoh", e.getMessage());
            e.printStackTrace();
        }
        return resultList;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        c = activity;
    }

    static public void showToolbar() {
        moveToolbar(0);
    }

    private void hideToolbar() {
        moveToolbar(-mToolbar.getHeight());
    }

    static private void moveToolbar(float toTranslationY) {
        if (scolled < 2 * mToolbar.getHeight()) {
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
                float x = translationY;
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
        if (toTranslationY == SEARCH_CARD_INI_POS) {

        } else if (scolled < 2 * mToolbar.getHeight()) {
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

    void move(float dy) {
        scolled += dy;
//        Log.d("Scrolling", dy + "//" + ViewHelper.getTranslationY(mToolbar) + "//" + mToolbar.getHeight() + "//" + SEARCH_CARD_INI_POS + "//" + ViewHelper.getTranslationY(SearchCard));
        if (ViewHelper.getTranslationY(SearchCard) >= SEARCH_CARD_INI_POS - mToolbar.getHeight() && ViewHelper.getTranslationY(SearchCard) <= SEARCH_CARD_INI_POS)
            if ((!(ViewHelper.getTranslationY(mToolbar) <= -mToolbar.getHeight()) && dy >= 0) || ((ViewHelper.getTranslationY(mToolbar) < 0) && dy <= 0)) {
                if (ViewHelper.getTranslationY(mToolbar) - dy > 0) {
                    dy = ViewHelper.getTranslationY(mToolbar);
                }
                if (ViewHelper.getTranslationY(mToolbar) - dy < -mToolbar.getHeight())
                    dy = ViewHelper.getTranslationY(mToolbar) + mToolbar.getHeight();
                ViewHelper.setTranslationY(mToolbar, ViewHelper.getTranslationY(mToolbar) - dy);
                ViewHelper.setTranslationY(swipetab, ViewHelper.getTranslationY(swipetab) - dy);
                ViewHelper.setTranslationY(SearchCard, ViewHelper.getTranslationY(SearchCard) - dy);
            }
        if (ViewHelper.getTranslationY(mToolbar) == -mToolbar.getHeight())
            if ((dy >= 0 && ViewHelper.getTranslationY(SearchCard) + mToolbar.getHeight() >= -SearchCard.getHeight() - dpToPx(10)) || (dy <= 0 && ViewHelper.getTranslationY(SearchCard) <= SEARCH_CARD_INI_POS - mToolbar.getHeight())) {
                if (ViewHelper.getTranslationY(SearchCard) - dy < -SearchCard.getHeight() - dpToPx(10) - mToolbar.getHeight()) {
                    dy = ViewHelper.getTranslationY(SearchCard) + SearchCard.getHeight() + dpToPx(10) + mToolbar.getHeight();
                }
                if (ViewHelper.getTranslationY(SearchCard) - dy > SEARCH_CARD_INI_POS - mToolbar.getHeight()) {
                    dy = ViewHelper.getTranslationY(SearchCard) - SEARCH_CARD_INI_POS + mToolbar.getHeight();
                }
                ViewHelper.setTranslationY(SearchCard, ViewHelper.getTranslationY(SearchCard) - dy);
            }
        /*if((!(swipetab.getTranslationY()<=SWIPE_TAB_INI_POS-swipetab.getHeight()) && dy>=0) || ((swipetab.getTranslationY()<SWIPE_TAB_INI_POS)&& dy<=0)) {
            if (swipetab.getTranslationY() - dy > SWIPE_TAB_INI_POS)
                dy = swipetab.getTranslationY()-SWIPE_TAB_INI_POS;
        }*/
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = c.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }
}
