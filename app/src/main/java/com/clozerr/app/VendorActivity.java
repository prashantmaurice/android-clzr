package com.clozerr.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class VendorActivity extends ActionBarActivity {

    private static final String TAG = "VendorActivity";

    private Toolbar toolbar;
    private ViewPager pager;
    private SlidingTabLayout mtabs;
    private Intent callingIntent;
    private FloatingActionButton mCheckInButton;

    private String pinNumber;
    public static Bundle detailsBundle;
    static String vendorId;
    static String vendorTitle;
    static int i=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor);


        detailsBundle = new Bundle();
        callingIntent = getIntent();
        final String vendor_id = callingIntent.getStringExtra("vendor_id");
        vendorId = vendor_id;
        final String urlVendor = "http://api.clozerr.com/vendor/get?vendor_id=" + vendor_id;
        Log.e(TAG, "vendor url - " + urlVendor);
        new AsyncGet(this, urlVendor, new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                try {
                    String address = "", phoneNumber = "", vendorDescription = "";
                    BeaconFinderService.BeaconDBParams params = null;
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
                    if (object.has("beacons") && object.getJSONObject("beacons").has("major")) {
                        params = new BeaconFinderService.BeaconDBParams(object.getJSONObject("beacons"));
                        Log.e(TAG, "BDB params - " + params.toString());
                    }
                    //Log.e("description", vendorDescription);
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
                    ArrayList<String> stringArray = new ArrayList<String>();
                    JSONArray jsonArray = object.getJSONArray("question");
                    for (int i = 0, count = jsonArray.length(); i < count; i++) {
                        try {
                            stringArray.add(jsonArray.getString(i));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    detailsBundle.putStringArrayList("questions", stringArray);
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
                    //currentItem.getQuestions();

                    if (!callingIntent.getBooleanExtra("from_periodic_scan", false) && params != null)
                        OneTimeBFS.checkAndStartScan(getApplicationContext(), params);
                    else PeriodicBFS.dismissNotifications(VendorActivity.this);

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
                    mtabs.setCustomTabView(R.layout.custom_tab_view, R.id.tabtitle);
                    mtabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
                        @Override
                        public int getIndicatorColor(int position) {
                            return getResources().getColor(R.color.colorAccent);
                        }
                    });
                    mtabs.setViewPager(pager);


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


                    mCheckInButton = (FloatingActionButton) findViewById(R.id.checkinButton);
                    mCheckInButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (detailsBundle.getString("offerId") == null)
                                Toast.makeText(getApplicationContext(), "No further offers available.", Toast.LENGTH_SHORT).show();
                            else
                                checkIn();
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

        SharedPreferences status = getSharedPreferences("USER", 0);
        String TOKEN = status.getString("token", "");

        String urlVisited = "http://api.clozerr.com/vendor/offers/myofferspage?vendor_id=" + vendorId + "&access_token=" + TOKEN;
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


                          /*RecyclerViewAdapter1 Cardadapter = new RecyclerViewAdapter1(convertRowMyOffers(s), CouponDetails.this);
                          mRecyclerView.setAdapter(Cardadapter);*/
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
        if (id == R.id.action_settings) {
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

    private void slidingtaboffers() {
        SharedPreferences status = getSharedPreferences("USER", 0);
        String TOKEN = status.getString("token", "");

        String urlVisited = "http://api.clozerr.com/vendor/offers/myofferspage?vendor_id=" + vendorId + "&access_token=" + TOKEN;
        String urlUser = "http://api.clozerr.com/auth?fid=" + detailsBundle.getString("fid") + "&access_token=" + TOKEN;

        Log.e("urlslide", urlVisited);
        //Toast.makeText(getApplicationContext(),urlVisited,Toast.LENGTH_SHORT).show();

        new AsyncGet(this, urlVisited, new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                //  t1.setText(s);


                    Log.e("resultSlide", s);
                    Toast.makeText(VendorActivity.this,s, Toast.LENGTH_SHORT).show();
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


                          /*RecyclerViewAdapter1 Cardadapter = new RecyclerViewAdapter1(convertRowMyOffers(s), CouponDetails.this);
                          mRecyclerView.setAdapter(Cardadapter);*/
            }
        });
    }

    private void checkIn() {
        SharedPreferences status = getSharedPreferences("USER", 0);
        String TOKEN = status.getString("token", "");

        String url="http://api.clozerr.com/checkin/create?access_token=" + TOKEN + "&vendor_id=" + detailsBundle.getString("vendorId")
                + "&offer_id=" + detailsBundle.getString("offerId");
        String gcm_id = GCMRegistrar.getRegistrationId(getApplicationContext());
        if( !gcm_id.equals("") )
            url += "&gcm_id=" + gcm_id;

        Log.e(TAG, "checkin url - " + url);

        // Login.showProgressDialog(getApplicationContext());
        new AsyncGet(this, url, new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                Log.e("pin", s);
                try {
                    pinNumber = new JSONObject(s).getJSONObject("checkin").getString("pin");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(s==null) {
                    Toast.makeText(getApplicationContext(),"No internet connection",Toast.LENGTH_SHORT).show();
                } else {
                    FrameLayout currentLayout = (FrameLayout) pager.getFocusedChild();
                    final PopupWindow confirmPopup = getNewPopupWindow(currentLayout, R.layout.checkin_pin_confirm);
                    final LinearLayout displayView = ((LinearLayout)((CardView)((LinearLayout)(confirmPopup.getContentView())).
                            getChildAt(0)).getChildAt(0));
                    //currentLayout.getForeground().mutate().setAlpha(255);
                    for (int i = 0; i < displayView.getChildCount(); ++i) {
                        View child = displayView.getChildAt(i);
                        switch(child.getId()) {
                            /*case R.id.confirmFrameLayout:
                                child.findViewById(R.id.confirmButton).setOnClickListener(
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                // Intent homeIntent = new Intent(getApplicationContext(), Home.class);
                                                // homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                //  getApplicationContext().startActivity(homeIntent);
                                                CouponDetails.this.finish();
                                            }
                                        });
                                break;*/
                            case R.id.dateTimeLayout:
                                String date = new SimpleDateFormat("dd.MM.yyyy").format(new Date(System.currentTimeMillis())),
                                        time = new SimpleDateFormat("HH:mm").format(new Date(System.currentTimeMillis())) + " hrs";
                                ((TextView)(child.findViewById(R.id.timeView))).setText(time);
                                ((TextView)(child.findViewById(R.id.dateView))).setText(date);
                                break;
                            case R.id.pinView:  ((TextView) child).setText(pinNumber);
                                break;
                            case R.id.confirmTitleView: ((TextView) child).setText(detailsBundle.getString("vendorTitle"));
                                break;
                            case R.id.confirmOfferView: ((TextView) child).setText(detailsBundle.getString("offerCaption"));
                                break;
                        }
                    }
                    confirmPopup.showAtLocation(currentLayout, Gravity.CENTER, 0, 0);
                }
            }
        });
    }

    @Override
    public void onStop() {
        AsyncGet.dismissDialog();
        super.onStop();
    }
}
