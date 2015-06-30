package com.clozerr.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MyClubsFragment extends Fragment {
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
    public static ArrayList<CardModel> rowItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.activity_my_clubs_fragment, container, false);
        mRecyclerView = (ObservableRecyclerView) layout.findViewById(R.id.sliding_list);
        searchView = (SearchView) layout.findViewById(R.id.searchView);
        SearchCard = layout.findViewById(R.id.card_view);
        rowItems = new ArrayList<>();
        SEARCH_CARD_INI_POS = ViewHelper.getTranslationY(SearchCard);
        mScrollable = getActivity().findViewById(R.id.drawerLayout);
        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        swipetab = getActivity().findViewById(R.id.tabs);
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
                                mRecyclerView.addItemDecoration(new SpaceItemDecoration(dpToPx(12) + (mToolbar.getHeight() + swipetab.getHeight() + SearchCard.getHeight()), 1));
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
        Log.e("app", "in slidingmycards; set recycler");


        SharedPreferences status = c.getSharedPreferences("USER", 0);
        String TOKEN = status.getString("token", "");

        /*String urlVisited = "http://api.clozerr.com/vendor/get/visitedV2?access_token=" + TOKEN;
        Log.e("urlslide", urlVisited);

        new AsyncGet(c, urlVisited, new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                //  t1.setText(s);

                Log.e("resultSlide", s);
                convertRowMyCard(s);
                MyCardRecyclerViewAdapter Cardadapter = new MyCardRecyclerViewAdapter(rowItems, c);
                mRecyclerView.setAdapter(Cardadapter);
                try {
                    Tracker t = ((Analytics) c.getApplicationContext()).getTracker(Analytics.TrackerName.APP_TRACKER);

                    t.setScreenName("MyCards");

                    t.send(new HitBuilders.AppViewBuilder().build());
                } catch (Exception e) {
                    Toast.makeText(c, "Error" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                if (s == null) {
                    Toast.makeText(c, "No internet connection", Toast.LENGTH_SHORT).show();
                }
                //l1.setAdapter(adapter);
            }
        });*/

        String urlFavorites = "http://api.clozerr.com/v2/user/favourites/list?access_token=" + TOKEN;
        // TODO remove vendor/get/details async get from the loop
        new AsyncGet(c, urlFavorites, new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
/*                    JSONArray obj = new JSONArray(s);
                    //JSONObject fav=obj.getJSONObject("favorites");
                    //JSONArray vendors = fav.getJSONArray("vendor");
                    for (int i = 0; i < obj.length(); ++i) {
                        Boolean status = true;
                        Toast.makeText(getActivity(), Integer.toString(obj.size()), Toast.LENGTH_SHORT).show();
*//*                        for (int j = 0; j < obj.size(); j++) {
                           if (obj.get(j).getVendorId().equals(vendors.getString(i))) {
                                status = false;
                                break;
                            }
                        }*//*
                        S
                        if (status == true) {
                            String urlFavVendor = "http://api.clozerr.com/v2/vendor/get/details?vendor_id=" + vendors.getString(i);

                            new AsyncGet(c, urlFavVendor, new AsyncGet.AsyncResult() {
                                @Override
                                public void gotResult(String s) {
                                    String address = "", phoneNumber = "", vendorDescription = "";
                                    double latitude = 0.0, longitude = 0.0;
                                    JSONObject object = null;
                                    try {
                                        object = new JSONObject(s);
                                        phoneNumber = object.getString("phone");
                                        if (phoneNumber.equalsIgnoreCase("undefined"))
                                            phoneNumber = "";
                                        vendorDescription = object.getString("description");
                                        if (vendorDescription.equalsIgnoreCase("undefined"))
                                            vendorDescription = "";
                                        latitude = object.getJSONArray("location").getDouble(0);
                                        if (latitude <= 0.0)
                                            latitude = 0.0;
                                        longitude = object.getJSONArray("location").getDouble(1);
                                        if (longitude <= 0.0)
                                            longitude = 0.0;
                                        CardModel item = new CardModel(
                                                object.getString("name"),
                                                phoneNumber,
                                                vendorDescription,
                                                object.getJSONArray("offers"),
                                                latitude,
                                                longitude,
                                                object.getString("image"),
                                                object.getString("fid"), object.getString("_id"),
                                                0
                                        );
                                        rowItems.add(item);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }

                    }*/
                    convertRowMyCard(s);
                    MyCardRecyclerViewAdapter Cardadapter = new MyCardRecyclerViewAdapter(rowItems, c);
                    mRecyclerView.setAdapter(Cardadapter);
                //  t1.setText(s);
                if (s == null) {
                    Toast.makeText(c, "No internet connection", Toast.LENGTH_SHORT).show();
                }
                //l1.setAdapter(adapter);
            }
        });


        return layout;
    }

    private void convertRowMyCard(String s) {
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
                        array.getJSONObject(i).getString("image"),
                        array.getJSONObject(i).getString("fid"),
                        array.getJSONObject(i).getString("_id"),
                        //array.getJSONObject(i).getInt("stamps")
                        0
                );
                Log.e("stringfunction", "processed");
                rowItems.add(item);
            }
        } catch (Exception e) {
            Log.e("uhoh", e.getMessage());
            e.printStackTrace();
        }
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
        Log.d("Scrolling", dy + "//" + ViewHelper.getTranslationY(mToolbar) + "//" + mToolbar.getHeight() + "//" + SEARCH_CARD_INI_POS + "//" + ViewHelper.getTranslationY(SearchCard));
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
