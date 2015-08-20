package com.clozerr.app.Activities.VendorScreens;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.clozerr.app.Analytics;
import com.clozerr.app.AsyncGet;
import com.clozerr.app.GenUtils;
import com.clozerr.app.MainApplication;
import com.clozerr.app.Models.VendorDetailsObject;
import com.clozerr.app.PeriodicBFS;
import com.clozerr.app.R;
import com.clozerr.app.SlidingTabLayout;
import com.clozerr.app.UnusedOffersActivity;
import com.clozerr.app.Utils.Constants;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONObject;


public class VendorActivity extends ActionBarActivity {

    private static final String TAG = "VENDORACTIVITY";
    public static final String EXTRA_VENDORID = "vendor_id";

    private boolean fromPeriodicBFS = false;
    private Toolbar toolbar;
    private ViewPager pager;
    private SlidingTabLayout mtabs;
    private Intent callingIntent;
    private FloatingActionButton mCheckInButton;
    public static String Rewards="";
//    public static Bundle detailsBundle;
    public static ImageView logoView;
    static String vendorTitle;
    public static int i=1;
    public String analyticsurlvendor=null;
    public static String TOKEN="";


    //Data variables
    String vendorId, vendorName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor);
        Drawable myIcon = getResources().getDrawable( R.drawable.checkinbutton );
        ColorFilter filter = new LightingColorFilter( Color.WHITE, Color.WHITE );
        myIcon.setColorFilter(filter);
        Rewards = "";

        callingIntent = getIntent();


        //TODO : @sai : check if this is deprecated, like what is the
        fromPeriodicBFS = callingIntent != null && callingIntent.getBooleanExtra("from_periodic_scan", false);
        if (fromPeriodicBFS)
            PeriodicBFS.dismissNotifications(this);

        mCheckInButton = (FloatingActionButton) findViewById(R.id.checkinButton);
        mCheckInButton.setImageDrawable(myIcon);

//        detailsBundle = new Bundle();



        vendorId = callingIntent.getStringExtra(EXTRA_VENDORID);

//        TOKEN = getSharedPreferences("USER", 0).getString("token", "");
        TOKEN = MainApplication.getInstance().tokenHandler.clozerrtoken;
        final String urlVendor = "http://api.clozerr.com/v2/vendor/get/details?vendor_id=" + vendorId;
        Log.e(TAG, "vendor url - " + urlVendor);
        new AsyncGet(this, urlVendor, new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                try {
                    //BeaconFinderService.BeaconDBParams params = null;
                    JSONObject object = new JSONObject(s);
                    VendorDetailsObject vendorDetailsObject = VendorDetailsObject.decodeFromServer(object);

                    /*if (object.has("beacons") && object.getJSONObject("beacons").has("major") &&
                            object.getJSONObject("beacons").has("minor")) {
                        params = new BeaconFinderService.BeaconDBParams(object.getJSONObject("beacons"));
                        Log.e(TAG, "BDB params - " + params.toString());
                    }*/


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
                        toolbar.setTitle(vendorDetailsObject.name);
                    }
                    pager = (ViewPager) findViewById(R.id.pager_vendor);
                    pager.setAdapter(new VendorPagerAdapter(getSupportFragmentManager(), VendorActivity.this,vendorDetailsObject));
                    pager.setOffscreenPageLimit(VendorPagerAdapter.OFFSET_PAGE_LIMIT);
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

                    vendorTitle = vendorDetailsObject.name;
                    //currentItem.getQuestions();

                    analyticsurlvendor = GenUtils.getDefaultAnalyticsUriBuilder(getApplicationContext(), Constants.Metrics.VENDOR_SCREEN)
                            .appendQueryParameter("dimensions[vendor]", vendorDetailsObject.vendorId )
                            .build().toString();
                    GenUtils.putAnalytics(getApplicationContext(), TAG, analyticsurlvendor);

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
                    try {
                        builder.show();
                        // Catch exception when user destroys VendorActivity before the Dialog is shown.
                    }catch( Exception ex ){
                        ex.printStackTrace();
                    }
                }
            }
        });

        analyticsurlvendor = GenUtils.getDefaultAnalyticsUriBuilder(this, vendorName+Constants.Metrics.SUFFIX_VENDOR_SCREEN)
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
//                detailsBundle.putString("Alloffers", s);
                //Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();

                try {
                    Tracker t = ((Analytics) getApplication()).getTracker(Analytics.TrackerName.APP_TRACKER);

                    t.setScreenName(vendorId + "_offer");

                    t.send(new HitBuilders.AppViewBuilder().build());
                } catch (Exception e) {
                    //Toast.makeText(getApplicationContext(), "Error" + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
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
//                detailsBundle.putString("unlockedoffers", s);
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
//                detailsBundle.putString("PinnedOffers", s);
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
