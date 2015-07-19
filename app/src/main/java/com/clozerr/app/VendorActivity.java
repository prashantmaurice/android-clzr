package com.clozerr.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;


public class VendorActivity extends ActionBarActivity {

    private static final String TAG = "VendorActivity";

    private boolean fromPeriodicBFS = false;
    private Toolbar toolbar;
    private ViewPager pager;
    private SlidingTabLayout mtabs;
    private Intent callingIntent;
    private FloatingActionButton mCheckInButton;
    static String Rewards="";
    private String pinNumber;
    public static Bundle detailsBundle;
    public static String vendorId;
    static String vendorTitle;
    static int i=1;
    public String analyticsurlvendor=null;
    static String TOKEN="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor);
        Drawable myIcon = getResources().getDrawable( R.drawable.checkinbutton );
        ColorFilter filter = new LightingColorFilter( Color.WHITE, Color.WHITE );
        myIcon.setColorFilter(filter);

        callingIntent = getIntent();
        fromPeriodicBFS = callingIntent != null && callingIntent.getBooleanExtra("from_periodic_scan", false);
        if (fromPeriodicBFS)
            PeriodicBFS.dismissNotifications(this);

        mCheckInButton = (FloatingActionButton) findViewById(R.id.checkinButton);
        mCheckInButton.setImageDrawable(myIcon);

        detailsBundle = new Bundle();

        final String vendor_id = callingIntent.getStringExtra("vendor_id");
        vendorId = vendor_id;

        TOKEN = getSharedPreferences("USER", 0).getString("token", "");
        final String urlVendor = "http://api.clozerr.com/v2/vendor/get/details?vendor_id=" + vendor_id;
        Log.e(TAG, "vendor url - " + urlVendor);
        new AsyncGet(this, urlVendor, new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                try {
                    //Toast.makeText(getApplicationContext(), Constants.URLBuilders.ANALYTICSCOPY.toString(),Toast.LENGTH_SHORT).show();
                    String address = "", phoneNumber = "", vendorDescription = "",fb = "",gplus = "",twitter = "",logo = "";
                    //BeaconFinderService.BeaconDBParams params = null;
                    double latitude = 0.0, longitude = 0.0;
                    JSONObject object = new JSONObject(s);

                    try {
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
                    } catch (Exception e) {
                        e.printStackTrace();
                        //Toast.makeText(CouponDetails.this, "Error - " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                    /*if (object.has("beacons") && object.getJSONObject("beacons").has("major") &&
                            object.getJSONObject("beacons").has("minor")) {
                        params = new BeaconFinderService.BeaconDBParams(object.getJSONObject("beacons"));
                        Log.e(TAG, "BDB params - " + params.toString());
                    }*/
                    //Log.e("description", vendorDescription);
                    try
                    {
                        logo = object.getString("logo");
                        Log.i("logo",logo);
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                    try
                    {
                        fb =object.getString("fb");
                        if(fb.equalsIgnoreCase("undefined"))
                            fb = "";
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                    try
                    {
                        gplus = object.getString("gplus");
                        if(gplus.equalsIgnoreCase("undefined"))
                            gplus = "";
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                    try
                    {
                        twitter = object.getString("twitter");
                        if(twitter.equalsIgnoreCase("undefined"))
                            twitter = "";
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                    address = object.getString("address");
                    final CardModel currentItem = new CardModel(
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
                    ArrayList<String> questions = new ArrayList<String>();
                    JSONArray questionArray = object.getJSONArray("question");
                    for (int i = 0, count = questionArray.length(); i < count; i++) {
                        try {
                            questions.add(questionArray.getString(i));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    detailsBundle.putStringArrayList("questions", questions);
                    ArrayList<String> gallery = new ArrayList<String>();
                    JSONArray galleryArray = object.getJSONArray("gallery");
                    for (int i = 0, count = galleryArray.length(); i < count; i++) {
                        try {
                            gallery.add(galleryArray.getString(i));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    detailsBundle.putStringArrayList("gallery", gallery);
                     /*JSONArray question= object.getJSONArray("question");
                    for(int i=0;i<question.length();i++){
                      ques_arr.add(question.getString(i));
                    }*/
                    //Log.e("title", currentItem.getTitle());
                    //Toast.makeText(CouponDetails.this, "title - " + currentItem.getTitle(), Toast.LENGTH_SHORT).show();
                    detailsBundle.putString("vendorTitle", currentItem.getTitle());
                    //detailsBundle.putString("offerText", currentItem.getOfferDescription() );
                    detailsBundle.putString("vendorId", currentItem.getVendorId());
                    detailsBundle.putString("description", vendorDescription);
                    detailsBundle.putString("address", address);
                    //detailsBundle.putString("offerId", currentItem.getOfferId());
                    detailsBundle.putString("vendorImage", currentItem.getImageId());
                    detailsBundle.putDouble("latitude", latitude);
                    detailsBundle.putDouble("longitude", longitude);
                    detailsBundle.putDouble("distance", currentItem.getDistance());
                    detailsBundle.putString("distanceString", currentItem.getDistanceString());
                    detailsBundle.putString("phoneNumber", phoneNumber);
                    detailsBundle.putString("fb",fb);
                    detailsBundle.putString("gplus",gplus);
                    detailsBundle.putString("twitter",twitter);
                    detailsBundle.putString("logo",logo);

                    //currentItem.getQuestions();
                    /*try {
                        if (!fromPeriodicBFS && params != null)
                            OneTimeBFS.checkAndStartScan(getApplicationContext(), params);
                        //else PeriodicBFS.dismissNotifications(VendorActivity.this);
                    }catch (Exception e){}*/
                    toolbar = (Toolbar) findViewById(R.id.toolbar_vendor);
                    if (toolbar != null) {
                        setSupportActionBar(toolbar);
                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                        toolbar.setTitle(currentItem.getTitle());
                    }
                    pager = (ViewPager) findViewById(R.id.pager_vendor);
                    pager.setAdapter(new VendorPagerAdapter(getSupportFragmentManager(), VendorActivity.this));
                    mtabs = (SlidingTabLayout) findViewById(R.id.tabs_vendor);
                    mtabs.setDistributeEvenly(true);
                    mtabs.setCustomTabView(R.layout.custom_tab_view_vendor, R.id.tabtitle);
                    mtabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
                        @Override
                        public int getIndicatorColor(int position) {
                            return getResources().getColor(R.color.colorAccent);
                        }
                    });
                    mtabs.setViewPager(pager);
                    if (fromPeriodicBFS)
                        pager.setCurrentItem(1);

                    detailsBundle.putString("vendorTitle", currentItem.getTitle());
                    //detailsBundle.putString("offerText", currentItem.getOfferDescription() );
                    vendorTitle = currentItem.getTitle();
                    detailsBundle.putString("vendorId", currentItem.getVendorId());
                    detailsBundle.putString("description", vendorDescription);
                    detailsBundle.putString("address", address);
                    //detailsBundle.putString("offerId", currentItem.getOfferId());
                    detailsBundle.putString("vendorImage", currentItem.getImageId());
                    detailsBundle.putDouble("latitude", latitude);
                    detailsBundle.putDouble("longitude", longitude);
                    detailsBundle.putDouble("distance", currentItem.getDistance());
                    detailsBundle.putString("distanceString", currentItem.getDistanceString());
                    detailsBundle.putString("phoneNumber", phoneNumber);
                    //currentItem.getQuestions();

                    TimeZone tz = TimeZone.getTimeZone("GMT+0530");
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
                    df.setTimeZone(tz);
                    String nowAsISO = df.format(new Date());
                    analyticsurlvendor= Constants.URLBuilders.ANALYTICS
                            .appendQueryParameter("metric","Vendor Screen")
                            .appendQueryParameter("dimensions[device]", "Android API " + Build.VERSION.SDK_INT)
                            .appendQueryParameter("dimensions[vendor]", detailsBundle.getString("vendorId") )
                            .appendQueryParameter("time", nowAsISO)
                            .appendQueryParameter("access_token",TOKEN)
                            .build().toString();
                    //Toast.makeText(getApplicationContext(),analyticsurlvendor,Toast.LENGTH_SHORT).show();

                    //"?metric=Clozerr+Home+Screen&dimensions%5Bdevice%5D=Android+API+"+ Build.VERSION.SDK_INT+"&dimensions%5Bid%5D=,jau65asas76&time="+nowAsISO+"&access_token="+TOKEN;
                    new AsyncGet(VendorActivity.this, analyticsurlvendor, new AsyncGet.AsyncResult() {
                        @Override
                        public void gotResult(String s) {
                            Log.e(analyticsurlvendor, "");
                            //Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
                            Constants.URLBuilders.ANALYTICS.clearQuery();
                        }
                    },false);

                    mCheckInButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(VendorActivity.this, UnusedOffersActivity.class));
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error loading");
                    e.printStackTrace();
                    AlertDialog.Builder builder = new AlertDialog.Builder(VendorActivity.this);
                    builder.setTitle("Error")
                            .setCancelable(false)
                            .setMessage("Sorry, but the details could not be loaded.")
                            .setNeutralButton("Go back", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    VendorActivity.this.onBackPressed();
                                }
                            });
                    builder.show();
                }
            }
        });

        analyticsurlvendor = GenUtils.getDefaultAnalyticsUriBuilder(this, detailsBundle.getString("vendorTitle")+Constants.Metrics.SUFFIX_VENDOR_SCREEN)
                            .build().toString();
        GenUtils.putAnalytics(this, TAG, analyticsurlvendor);


        /*String urlVisited = "http://api.clozerr.com/v2/vendor/offers/offerspage?vendor_id=" + vendorId + "&access_token=" + TOKEN;
        String urlUser = "http://api.clozerr.com/auth?fid=" + detailsBundle.getString("fid") + "&access_token=" + TOKEN;

        Log.e("urlslide", urlVisited);
        //Toast.makeText(getApplicationContext(),urlVisited,Toast.LENGTH_SHORT).show();

        new AsyncGet(this, urlVisited, new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                //  t1.setText(s);


                Log.e("resultSlide", s);
                detailsBundle.putString("offerstring", s);

                try {
                    Tracker t = ((Analytics) getApplication()).getTracker(Analytics.TrackerName.APP_TRACKER);

                    t.setScreenName(detailsBundle.getString("vendorId") + "_offer");

                    t.send(new HitBuilders.AppViewBuilder().build());
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Error" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                //l1.setAdapter(adapter);
                if (s == null) {
                    Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                }

                // Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();


                          *//*RecyclerViewAdapter1 Cardadapter = new RecyclerViewAdapter1(convertRowMyOffers(s), CouponDetails.this);
                          mRecyclerView.setAdapter(Cardadapter);*//*
            }
        });*/


        String offersPageUrl = "http://api.clozerr.com/v2/vendor/offers/offerspage?access_token="+TOKEN+"&vendor_id="+vendorId;
        Log.e(TAG, "MyStamps URL - " + offersPageUrl);
        new AsyncGet(this, offersPageUrl, new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                //  t1.setText(s);

                Log.e("Offers", s);
                detailsBundle.putString("Alloffers", s);
                //Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();

                try {
                    Tracker t = ((Analytics) getApplication()).getTracker(Analytics.TrackerName.APP_TRACKER);

                    t.setScreenName(detailsBundle.getString("vendorId") + "_offer");

                    t.send(new HitBuilders.AppViewBuilder().build());
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Error" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                //l1.setAdapter(adapter);
                if (s == null) {
                    Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                }

            }
        });

        String unlockedOffersUrl = "http://api.clozerr.com/v2/vendor/offers/unlocked?access_token="+TOKEN+"&vendor_id="+vendorId;
        Log.e(TAG, "MyStamps URL - " + unlockedOffersUrl);
        new AsyncGet(this, unlockedOffersUrl, new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                //  t1.setText(s);

                Log.e("Offers", s);
                //Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
                detailsBundle.putString("unlockedoffers", s);
                //Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                //l1.setAdapter(adapter);
                if (s == null) {
                    Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                }

            }
        });

        new AsyncGet(this, "http://api.clozerr.com/v2/user/add/pinned?access_token="+TOKEN , new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                //  t1.setText(s);

                Log.e("Offers", s);
                detailsBundle.putString("PinnedOffers", s);
                //Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                //l1.setAdapter(adapter);
                if (s == null) {
                    Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_vendor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public PopupWindow getNewPopupWindow(final FrameLayout parent, int layoutId)
    {
        parent.getForeground().mutate().setAlpha(255);
        final PopupWindow popupWindow = new PopupWindow(this);

        LayoutInflater inflater = (LayoutInflater)getApplicationContext().
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View displayView = inflater.inflate(layoutId, null);

        popupWindow.setFocusable(true);
        popupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setAnimationStyle(R.style.PopupWindowAnimation);

        popupWindow.setContentView(displayView);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                parent.getForeground().setAlpha(0);
            }
        });

        return popupWindow;
    }

    @Override
    public void onPause() {
        /*if (isFinishing())
            OneTimeBFS.checkAndStopScan(this);*/
        super.onPause();
    }

    @Override
    public void onStop() {
        AsyncGet.dismissDialog();
        super.onStop();
    }
}
