package com.clozerr.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
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
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.clozerr.app.Activities.LoginScreens.LoginActivity;
import com.clozerr.app.Activities.VendorScreens.VendorActivity;
//import com.facebook.Session;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.google.android.gms.plus.Plus;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

import org.json.JSONArray;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CategoryDetail extends ActionBarActivity{

    public static String TOKEN = "";
    Toolbar mToolbar;
    private RecyclerViewAdapter mMainPageAdapter;
    private ArrayList<CardModel> mMainCardsList;
    private RecyclerView.LayoutManager mLayoutManager;
    private EndlessRecyclerOnScrollListener mOnScrollListener;
    private int mOffset;
    static int scolled = 0;
    static float SEARCH_CARD_INI_POS = 0;
    SearchView searchView;
    View SearchCard;
    View swipetab;
    private boolean mCardsLeft = true;
    private final int ITEMS_PER_PAGE = 7, INITIAL_LOAD_LIMIT = 8;
    private Bundle categorybundle;
    View mScrollable;
    private String[] leftSliderData = {"About us","FAQ's","Like/Follow Clozerr","Rate Clozerr", "Tell Friends about Clozerr", "My Pinned Offers", "Settings", "Log out"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_detail);
        categorybundle=getIntent().getExtras();
        nitView();
        TextView username = (TextView)findViewById(R.id.nav_text);
        if(Home.USERNAME.length()!=0)
            username.setText(Home.USERNAME);
        swipetab = findViewById(R.id.tab);
        Log.e("pic", Home.USER_PIC_URL);
        SearchCard = findViewById(R.id.card_view);
        SEARCH_CARD_INI_POS = ViewHelper.getTranslationY(SearchCard);
        new DownloadImageTask((de.hdodenhof.circleimageview.CircleImageView)findViewById(R.id.nav_image))
                .execute(Home.USER_PIC_URL);
        final ObservableRecyclerView mRecyclerView = (ObservableRecyclerView) findViewById(R.id.list);
        searchView = (SearchView)findViewById(R.id.searchView);
        mScrollable=findViewById(R.id.drawerLayout);
        mToolbar=(Toolbar)findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        ((TextView) mToolbar.findViewById(R.id.appTitleView)).setTypeface(Typeface.createFromAsset(
                getAssets(), "fonts/comfortaa.ttf"
        ));
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
//        mOnScrollListener = new EndlessRecyclerOnScrollListener(
//                (LinearLayoutManager)mLayoutManager) {
//            @Override
//            public void onLoadMore() {
//                loadMoreItems();
//            }
//        };
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(dpToPx(150), 0));
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
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
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                move(dy);
            }
        });


        //startService(new Intent(this, LocationService.class));
        Home.lat = 13;
        Home.longi = 80.2;
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        SharedPreferences status = getSharedPreferences("USER", 0);
        final String cards = status.getString(categorybundle.getString("categoryname")+"category_cards", "");
//        TOKEN = status.getString("token", "");
        TOKEN = MainApplication.getInstance().data.userMain.token;
        if(!cards.equals("")){
            Log.e("Cached Card", cards);
            mMainCardsList = convertRow(cards);
            mMainPageAdapter = new RecyclerViewAdapter(mMainCardsList, this);
            mRecyclerView.setAdapter(mMainPageAdapter);
        } else {
            mOffset = 0;
            String url = "http://api.clozerr.com/v2/vendor/search/near?category=" + categorybundle.getString("categoryname").replace(" ", "%20") +
                    "&latitude=" + Home.lat + "&longitude=" + Home.longi + ((TOKEN.isEmpty()) ? "" : ("&access_token=" + TOKEN));
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
                        mMainPageAdapter = new RecyclerViewAdapter(mMainCardsList, CategoryDetail.this);
                        mRecyclerView.setAdapter(mMainPageAdapter);
                        final SharedPreferences.Editor editor = getSharedPreferences("USER", 0).edit();
                        editor.putString(categorybundle.getString("categoryname")+"category_cards", s);
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

                String url;
                mOffset = 0;
                url = "http://api.clozerr.com/v2/vendor/search/near?category="+categorybundle.getString("categoryname").replace(" ","%20") +
                        "&latitude=" + Home.lat + "&longitude=" + Home.longi + ((TOKEN.isEmpty()) ? "" : ("&access_token=" + TOKEN));
                Log.e("url", url);
                // TODO support pagination
                new AsyncGet(CategoryDetail.this, url, new AsyncGet.AsyncResult() {
                    @Override
                    public void gotResult(String s) {
                        if (s != "") {
                            ArrayList<CardModel> CardList = convertRow(s);
                            if (CardList.size() != 0) {
                                mMainCardsList = CardList;
                                mMainPageAdapter = new RecyclerViewAdapter(mMainCardsList, CategoryDetail.this);
                                mRecyclerView.setAdapter(mMainPageAdapter);

                                final SharedPreferences.Editor editor = getSharedPreferences("USER", 0).edit();
                                editor.putString(categorybundle.getString("categoryname")+"category_cards", s);
                                editor.apply();
                            } else {
                                mCardsLeft = false;
                            }
                        }
                    }
                });
            }

        });


    }
//    public void loadMoreItems() {
//        Log.e("load", "in loadMoreItems()");
//        if (mCardsLeft) {
//            mOffset += (mOffset == 0) ? INITIAL_LOAD_LIMIT : ITEMS_PER_PAGE;
//            String url = "";
//            if (!TOKEN.equals(""))
//                url = "http://api.clozerr.com/vendor/get/near?latitude=" + Home.lat + "&longitude=" + Home.longi + "&access_token=" + TOKEN
//                        + "&offset=" + mOffset + "&limit=" + ITEMS_PER_PAGE;
//            else
//                url = "http://api.clozerr.com/vendor/get/near?latitude=" + Home.lat + "&longitude=" + Home.longi
//                        + "&offset=" + mOffset + "&limit=" + ITEMS_PER_PAGE;
//            Log.e("url", url);
//            new AsyncGet(CategoryDetail.this, url, new AsyncGet.AsyncResult() {
//                @Override
//                public void gotResult(String s) {
//                    Log.e("result", s);
//                    if (s == null) {
//                        Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
//                    }
//                    ArrayList<CardModel> CardList = convertRow(s);
//                    if (CardList.size() != 0) {
//                        mMainCardsList.addAll(convertRow(s));
//                        final SharedPreferences.Editor editor = getSharedPreferences("USER", 0).edit();
//                        editor.putString(categorybundle.getString("categoryname")+"category_cards", s);
//                        editor.apply();
//                        Log.e("app", "editing done");
//                        mMainPageAdapter.notifyDataSetChanged();
//                        //Toast.makeText(getApplicationContext(), "More items ready", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Log.d("app", "no cards to show");
//                        mCardsLeft = false;
//                        mOffset = mMainCardsList.size();
//                    }
//                }
//            });
//        }
//    }
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
                    array.getJSONObject(i).getString("image") + URLEncoder.encode(array.getJSONObject(i).getString("resource_name"), "UTF-8"),
                    fid,array.getJSONObject(i).getString("_id"),0,"",true
            );
            rowItems.add(item);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return rowItems;
}
    public void showToolbar() {
        moveToolbar(0);
    }

    private void hideToolbar() {
        moveToolbar(-mToolbar.getHeight());
    }
    private void moveToolbar(float toTranslationY) {
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
    public void showSearchbar() {
        moveSearchbar(-mToolbar.getHeight());
    }
    public void showSearchbarToInitial() {
        moveSearchbar(SEARCH_CARD_INI_POS);
    }
    private void hideSearchbar() {
        moveSearchbar(-SearchCard.getHeight() - dpToPx(10) - mToolbar.getHeight());
    }
    private void moveSearchbar(float toTranslationY) {
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
    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
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
    private void nitView() {
        // Toast.makeText(this, "in nitview", Toast.LENGTH_SHORT).show();
        TextView categoryheader=(TextView)findViewById(R.id.categoryheader);
        categoryheader.setText(categorybundle.getString("categoryname"));
        ListView leftDrawerList = (ListView) findViewById(R.id.nav_listView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        //navigationDrawerAdapter=new ArrayAdapter<String>( Home.this, android.R.layout.simple_list_item_1, leftSliderData);
        // navigationDrawerAdapter=new ArrayAdapter<String>( Home.this, R.layout.navdrawlist,R.id.textView, leftSliderData);
        // Log.i("omy",leftSliderData[0]);
        List<String> l = Arrays.asList(leftSliderData);

// if List<String> isnt specific enough:
        ArrayList<String> al = new ArrayList<>(l);
        // ArrayList<String> arr ;
        // arr= (ArrayList<String>) Arrays.asList(leftSliderData);
        NavDrawAdapter nav = new NavDrawAdapter(this, al);
        leftDrawerList.setAdapter(nav);
        leftDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Toast.makeText(Home.this, i+"", Toast.LENGTH_SHORT).show();
                if(i==leftSliderData.length-1){ // this is basically the logout button
                    AlertDialog.Builder builder = new AlertDialog.Builder(CategoryDetail.this);
                    builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();

                }

                //"ABOUT US","FAQ'S","LIKE/FOLLOW CLOZERR","RATE CLOZERR", "LOGOUT"
                if(i==1){
                    startActivity(new Intent(CategoryDetail.this,FAQ.class));
                }
                else if(i==0){
                    startActivity(new Intent(CategoryDetail.this,AboutUs.class));
                }

                else if(i==5){
                    startActivity(new Intent(CategoryDetail.this,PinnedOffersActivity.class));
                }

                else if(i==6){
                    startActivity(new Intent(CategoryDetail.this,SettingsActivity.class));
                }

                else if(i==4) {
                    Intent textShareIntent = new Intent(Intent.ACTION_SEND);
                    textShareIntent.putExtra(Intent.EXTRA_TEXT, "Try out Clozerr, an app which lets you try out new restaurants near you and rewards you for your loyalty. https://play.google.com/store/apps/details?id=com.clozerr.app ");
                    textShareIntent.setType("text/plain");
                    startActivity(Intent.createChooser(textShareIntent, "Share with..."));
                }


                else if(i==2) {
                    Uri uri = null;
                    if (LoginActivity.googleOrFb == 1)
                    {
                        uri = Uri.parse("https://www.facebook.com/clozerrdeals");
                    }
                    else if (LoginActivity.googleOrFb == 2)
                    {
                        uri = Uri.parse("https://plus.google.com/112342093373744098489/about");
                    }
                    if (uri == null) Log.e("navdraw", "null" + LoginActivity.googleOrFb);
                    Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                    /*// Create and start the chooser
                    Intent chooser = Intent.createChooser(intent, "Open with");*/
                    startActivity(intent/*chooser*/);
                }
                else if(i==3) {
                    final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                }
            }

        });
        VendorActivity.i=0;
        // Toast.makeText(this, "end nitview", Toast.LENGTH_SHORT).show();
    }
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    Toast.makeText(CategoryDetail.this, "Logging out", Toast.LENGTH_SHORT).show();
                    SharedPreferences example = getSharedPreferences("USER", 0);
                    SharedPreferences.Editor editor = example.edit();
                    editor.clear();
                    editor.apply();
                    Home.USER_PIC_URL = Home.USERNAME = Home.USERID = TOKEN = "";
                    if (LoginActivity.googleOrFb == 2 && LoginActivity.googleApiClient != null)
                    {
                        if (LoginActivity.googleApiClient.isConnected()) {
                            Plus.AccountApi.clearDefaultAccount(LoginActivity.googleApiClient);
                            LoginActivity.googleApiClient.disconnect();
                        }
                    }
                    else if (LoginActivity.googleOrFb == 1)
                    {
//                        Session session = Session.getActiveSession();
//                        if (session != null) {
//                            if (!session.isClosed()) {
//                                session.closeAndClearTokenInformation();
//                            }
//                        } else {
//                            session = new Session(CategoryDetail.this);
//                            Session.setActiveSession(session);
//                            session.closeAndClearTokenInformation();
//                        }
                    }
                    //BeaconFinderService.disallowScanning(Home.this);
                    startActivity(new Intent(CategoryDetail.this, LoginActivity.class));
                    finish();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    //Toast.makeText(Home.this, "Thanks for staying. You are awesome.", Toast.LENGTH_SHORT).show();
                    //No button clicked
                    break;
            }
        }
    };
}
