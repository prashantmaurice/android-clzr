package com.clozerr.app;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.koushikdutta.ion.Ion;
import com.nineoldandroids.view.ViewHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;


public class CouponDetails extends ActionBarActivity implements ObservableScrollViewCallbacks {

    private static final String TAG = "CouponDetails";
    private static final long CHECK_IN_SCAN_TIMEOUT = TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS);

    private FrameLayout detailsLayout;
    private View mToolbarView;
    private ObservableScrollView mScrollView;
    private int mParallaxImageHeight;
    private TextView titleView, locView, nextOfferView, detailsView;
    private ImageView itemImageView;
    private CircleImageView callButton, dirButton, rateButton;
    private TextView checkinButton;
    private Bundle detailsBundle;
    private String pinNumber, gcmId,someNickname;
    private Intent i;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon_details);
        System.out.println("set content view");

        final Intent callingIntent = getIntent();
        i=callingIntent;
        if(callingIntent.getBooleanExtra("Notification",false)){
            NotificationManager mNotificationManager;
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(0);

        }
        initViews();
        detailsBundle = new Bundle();
        final String vendor_id = callingIntent.getStringExtra("vendor_id");
        try
        {
            Tracker t = ((Analytics) getApplication()).getTracker(Analytics.TrackerName.APP_TRACKER);

            t.setScreenName(vendor_id+"_screen");

            t.send(new HitBuilders.AppViewBuilder().build());
        }
        catch(Exception  e)
        {
            Toast.makeText(getApplicationContext(), "Error"+e.getMessage(), Toast.LENGTH_LONG).show();
        }
        final String offer_id = callingIntent.getStringExtra("offer_id");
        String offer_text = callingIntent.getStringExtra("offer_text");
        final String offer_caption = callingIntent.getStringExtra("offer_caption");



        if (offer_text == null || offer_text.equals(""))
            offer_text = "No further offers available";

        detailsBundle.putString("offerId", offer_id);
        detailsBundle.putString("offerText", offer_text);
        detailsBundle.putString("offerCaption", offer_caption);
        final ArrayList<String> ques_arr = new ArrayList<>();
        SharedPreferences status = getSharedPreferences("USER", 0);
        if(status.contains("latitude") && status.contains("longitude")){
            Home.lat=Double.parseDouble(status.getString("latitude",""));
            Home.longi=Double.parseDouble(status.getString("longitude",""));
        }

        //Toast.makeText(this, "id - " + vendor_id, Toast.LENGTH_SHORT).show();
        //Log.e("idvendor", vendor_id);
        final String url_coupon = "http://api.clozerr.com/vendor/get?vendor_id=" + vendor_id;
        //Toast.makeText(this, "url - " + url_coupon, Toast.LENGTH_SHORT).show();
        //Log.e("urlcoupon", url_coupon);
        new AsyncGet(CouponDetails.this, url_coupon, new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                //Log.i("ghfv","Inside gotResu");
                //Toast.makeText(CouponDetails.this, "Inside gotResult()", Toast.LENGTH_SHORT).show();
                String phonenumber="";
                String vendorDescription="";
                BeaconFinderService.BeaconDBParams params = null;
                double latitude = 0.0, longitude = 0.0;
                try {
                    Log.e("resultAsync", s);
                    JSONObject object = new JSONObject(s);
                    try {
                        phonenumber = object.getString("phone");
                        if (phonenumber.equalsIgnoreCase("undefined"))
                            phonenumber = "";
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
                    if (object.getJSONObject("beacons").has("major")) {
                        params = new BeaconFinderService.BeaconDBParams(object.getJSONObject("beacons").getInt("major"),
                                                                        object.getJSONObject("beacons").getInt("minor"));
                        Log.e(TAG, "BDB params - " + params.toString());
                    }
                    Log.e("description", vendorDescription);
                    final CardModel currentItem = new CardModel(
                            object.getString("name"),
                            phonenumber,
                            vendorDescription,
                            object.getJSONArray("offers"),
                            latitude,
                            longitude,
                            object.getString("image"),
                            object.getString("fid"),object.getString("_id"),
                            0
                    );
                    ArrayList<String> stringArray = new ArrayList<String>();
                    JSONArray jsonArray = object.getJSONArray("question");
                    for(int i = 0, count = jsonArray.length(); i< count; i++)
                    {
                        try {
                            stringArray.add(jsonArray.getString(i));
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    detailsBundle.putStringArrayList("questions",stringArray);
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
                    //detailsBundle.putString("offerId", currentItem.getOfferId());
                    detailsBundle.putString("vendorImage", currentItem.getImageId());
                    detailsBundle.putDouble("latitude", latitude);
                    detailsBundle.putDouble("longitude", longitude);
                    detailsBundle.putString("distance", currentItem.getDistance());
                    detailsBundle.putString("phonenumber", phonenumber);
                    //currentItem.getQuestions();

                    Log.i("fvgh",detailsBundle.toString());

                    try {
                        Ion.with(itemImageView)
                                // .placeholder(R.drawable.call)
                                // .error(R.drawable.bat)
                                //    .animateLoad(spinAnimation)
                                //    .animateIn(fadeInAnimation)
                                .load(detailsBundle.getString("vendorImage"));
                        Log.e("abc",currentItem.getImageId());
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                        Log.e("sh",e.toString());
                        // Log.e("img",detailsBundle.getString("vendorImage"));
                        Log.e("abc",currentItem.getImageId());
                    }

                    titleView.setText(detailsBundle.getString("vendorTitle"));
                    nextOfferView.setText(detailsBundle.getString("offerText"));
                    detailsView.setText(detailsBundle.getString("description"));
                    locView.setText(detailsBundle.getString("distance"));

                    if (!callingIntent.getBooleanExtra("from_periodic_scan", false))
                        OneTimeBFS.checkAndStartScan(getApplicationContext(), params);
                    else PeriodicBFS.dismissNotifications(CouponDetails.this);

                    checkinButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (detailsBundle.getString("offerId") == null)
                                Toast.makeText(getApplicationContext(),"No further offers available.",Toast.LENGTH_SHORT).show();
                            else
                                showConfirmPopup();
                        }
                    });

                    callButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!detailsBundle.getString("phonenumber").isEmpty()) {
                                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                                dialIntent.setData(Uri.parse("tel:" + detailsBundle.getString("phonenumber")));
                                startActivity(dialIntent);
                            }
                            else
                                Toast.makeText(CouponDetails.this, "Sorry, the phone number is unavailable now...",
                                        Toast.LENGTH_SHORT).show();
                        }
                    });

                    dirButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!currentItem.getDistance().isEmpty()) {
                                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                        Uri.parse("http://maps.google.com/maps?daddr=" + detailsBundle.getDouble("latitude") + "," + detailsBundle.getDouble("longitude")));
                                startActivity(intent);
                            }
                            else
                                Toast.makeText(CouponDetails.this, "Sorry, the location details haven't been provided by the vendor.",
                                        Toast.LENGTH_SHORT).show();
                        }
                    });

                    /*rateButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Context c = CouponDetails.this;
                            LayoutInflater lf = LayoutInflater.from(c);
                            View feedbackView = lf.inflate(R.layout.dialog_reviews,null);
                            RecyclerView rv = (RecyclerView) feedbackView.findViewById(R.id.list_questions);
                            rv.setLayoutManager(new LinearLayoutManager(c));
                            rv.setItemAnimator(new DefaultItemAnimator());
                            rv.setHasFixedSize(true);


                          ques_arr.add("How would you rate the ambiance of the restaurant?");
                            ques_arr.add("How would you rate the ambiance of the restaurant?");
                            ques_arr.add("How would you rate the ambiance of the restaurant?");
                            ques_arr.add("How would you rate the ambiance of the restaurant?");
                            ques_arr.add("How would you rate the ambiance of the restaurant?");
                            ReviewQuestionsAdapter rqa = new ReviewQuestionsAdapter(ques_arr,c);
                            rv.setAdapter(rqa);

                            final AlertDialog.Builder adbuilder = new AlertDialog.Builder(c);
                            adbuilder.setView(feedbackView);

                            adbuilder.setCancelable(true);
                            final AlertDialog alertDialog = adbuilder.create();
                            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                            // show it
                            alertDialog.show();
                            Button submitButton = (Button) feedbackView.findViewById(R.id.submit_feedback);

                            submitButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alertDialog.dismiss();
                                    //submit the reviews
                                    final String url_review = "";
                                    new AsyncGet(CouponDetails.this, url_review, new AsyncGet.AsyncResult() {

                                        @Override
                                        public void gotResult(String s) {

                                        }
                                    });
                                }


                            });
                        }
                    });*/

                    if(callingIntent.getBooleanExtra("from_notify_review",false)) {
                        final String checkin_id_review = callingIntent.getStringExtra("checkin_id");
                        // load dialogs only when context is valid
                        findViewById(R.id.detailsLayout).post(new Runnable() {
                            @Override
                            public void run() {
                                feedback(checkin_id_review);
                            }
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    AlertDialog.Builder builder = new AlertDialog.Builder(CouponDetails.this);
                    builder.setTitle("Error")
                            .setCancelable(false)
                            .setMessage("Sorry, but the details could not be loaded.")
                            .setNeutralButton("Go back", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    CouponDetails.this.onBackPressed();
                                }
                            });
                    builder.show();
                }
            }

        });
    }

    private void initToolbar() {
        setSupportActionBar((Toolbar) findViewById(R.id.backToolbar));
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolbarView = findViewById(R.id.backToolbar);
        mToolbarView.setBackgroundColor(ScrollUtils.getColorWithAlpha(0, getResources().getColor(R.color.colorPrimary)));
    }

    private void initViews() {
        detailsLayout = (FrameLayout) findViewById(R.id.detailsLayout);
        detailsLayout.getForeground().setAlpha(0);

        itemImageView = (ImageView) findViewById(R.id.itemImageView);
        titleView = (TextView) findViewById(R.id.itemTitleView);
        locView = (TextView) findViewById(R.id.itemLocView);
        nextOfferView = (TextView) findViewById(R.id.itemNextOfferView);
        detailsView = (TextView) findViewById(R.id.itemDetailsView);
        checkinButton = (TextView) findViewById(R.id.checkinButton);
        callButton = (CircleImageView) findViewById(R.id.itemCallButton);
        dirButton = (CircleImageView) findViewById(R.id.itemDirButton);
        //rateButton = (CircleImageView) findViewById(R.id.itemRateButton);
        initToolbar();

        slidingMyOffers();


        mScrollView = (ObservableScrollView) findViewById(R.id.scroll);
        mScrollView.setScrollViewCallbacks(this);
        mParallaxImageHeight = getResources().getDimensionPixelSize(R.dimen.parallax_image_height);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        onScrollChanged(mScrollView.getCurrentScrollY(), false, false);
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        int baseColor = getResources().getColor(R.color.colorPrimary);
        float alpha = 1 - (float) Math.max(0, mParallaxImageHeight - scrollY) / mParallaxImageHeight;
        mToolbarView.setBackgroundColor(ScrollUtils.getColorWithAlpha(alpha, baseColor));
        ViewHelper.setTranslationY(itemImageView, scrollY / 2);
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_coupon_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id==R.id.imageView2){
            Intent shortcutIntent = new Intent(getApplicationContext(), CouponDetails.class);
            shortcutIntent.putExtra("vendor_id", i.getStringExtra("vendor_id"));
            shortcutIntent.putExtra("offer_id", i.getStringExtra("offer_id"));
            shortcutIntent.putExtra("offer_caption", i.getStringExtra("offer_caption"));
            shortcutIntent.putExtra("offer_text", i.getStringExtra("offer_text"));
            SharedPreferences example = getSharedPreferences("USER", 0);
            SharedPreferences.Editor editor = example.edit();
            editor.putString("latitude", Home.lat+"");
            editor.putString("longitude", Home.longi+"");
            editor.apply();
            shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Intent addIntent = new Intent();
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, detailsBundle.getString("vendorTitle"));
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.ic_launcher));
            addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            getApplicationContext().sendBroadcast(addIntent);
            Toast.makeText(getApplicationContext(),"Pinned To Home Screen",Toast.LENGTH_SHORT).show();
        }
        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        if (id == android.R.id.home) {
            /*NavUtils.navigateUpFromSameTask(this);*/
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* private void slidingMyOffers() {
        SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.sliding_drawer);
        final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.sliding_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        drawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {

            @Override

            public void onDrawerOpened() {

                SharedPreferences status = getSharedPreferences("USER", 0);
                String TOKEN = status.getString("token", "");

                String myoffers = "http://api.clozerr.com/vendor/get/vendor_id=" + detailsBundle.getString("vendorId");
                Log.e("myoffer", myoffers);

                new AsyncGet(CouponDetails.this, myoffers, new AsyncGet.AsyncResult() {
                    @Override
                    public void gotResult(String s) {
                        //  t1.setText(s);

                        Log.e("resultSlide", s);

                        //MyCardRecyclerViewAdapter Cardadapter = new
                        offer_recyclerAdapter Cardadapter = new offer_recyclerViewAdapter(convertRowMyOffers(s), CouponDetails.this);
                        ;
                        mRecyclerView.setAdapter(Cardadapter);

                        //l1.setAdapter(adapter);
                    }
                });

            }

        });
    }*/
    private ArrayList<MyOffer> convertRowMyOffers(String s) {
        ArrayList<MyOffer> rowItems = new ArrayList<>();
        /*JSONObject temp = null;
        JSONArray array = null;
        int i=0;
        try {
            Log.e("stringfunction", s);
            temp = new JSONObject(s);
            array = temp.getJSONArray("offers");

            for(i = 0 ; i < array.length() ; i++){
                MyOffer item = new MyOffer(array.getJSONObject(i).getString("caption"),i+1);
                rowItems.add(item);
                System.out.println(item);
            }
        } catch (Exception e) {

            e.printStackTrace();
        }*/
        MyOffer item = null;
        try {
            Log.e(TAG, "json passed - " + s);
            JSONObject offerObject = null;
            JSONArray array = new JSONArray(s);
            MyOffer.SXOfferExtras extras = null;
            for (int i = 0; i < array.length(); ++i) {
                offerObject = array.getJSONObject(i);
                extras = null;
                String type = offerObject.getString("type");
                if (type.equalsIgnoreCase("SX"))
                    extras = new MyOffer.SXOfferExtras(offerObject.getJSONObject("stampStatus").getInt("total"),
                            offerObject.getDouble("billAmt"));
                item = new MyOffer(type,
                                   offerObject.getString("image"),
                                   offerObject.getString("optionalImage"),
                                   offerObject.getString("caption"),
                                   offerObject.getString("description"),
                                   offerObject.getInt("stamps"),
                                   extras);
                rowItems.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowItems;
    }

    /*  private void slidingMyOffers() {
            SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.sliding_drawer);
            final ListView mListView= (ListView) findViewById(R.id.sliding_list);

            drawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {

                @Override
                public void onDrawerOpened() {
                    SharedPreferences status = getSharedPreferences("USER", 0);
                    String TOKEN = status.getString("token", "");

                    String myoffers = "http://api.clozerr.com/vendor/get/vendor_id=" + detailsBundle.getString("vendorId");
                    Toast.makeText(getApplicationContext(),myoffers,Toast.LENGTH_SHORT);
                    Log.e("myoffer", myoffers);

                    new AsyncGet(CouponDetails.this, myoffers, new AsyncGet.AsyncResult() {
                        @Override
                        public void gotResult(String s) {
                            //  t1.setText(s);

                            Log.e("resultSlide", s);

                            /*ArrayAdapter<MyOffer> offAdapter = new ArrayAdapter<MyOffer>(getApplicationContext(),R.layout.offer_list,convertRowMyOffers(s));//offerAdapter(convertRowMyOffers(s),CouponDetails.this);
                            offAdapter.add(new MyOffer("dummy",2));
                            offAdapter.add(new MyOffer("dummy",2));
                            MyOfferAdapter offerAdapter = new MyOfferAdapter(convertRowMyOffers(s),getApplicationContext());
                            mListView.setAdapter(offerAdapter);
                        }
                    });
                }
            });
    }*/
    private void slidingMyOffers() {
        SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.sliding_drawer1);
        final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.sliding_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(CouponDetails.this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        drawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {

            @Override

            public void onDrawerOpened() {


                  Button offers_menu = (Button) findViewById(R.id.myoffers);
                  offers_menu.setText(detailsBundle.getString("vendorTitle"));
                  offers_menu.setTextColor(Color.parseColor("#FFFFFF"));
                  offers_menu.setBackgroundColor(Color.parseColor("#EF6C00"));
                  SharedPreferences status = getSharedPreferences("USER", 0);
                  String TOKEN = status.getString("token", "");
                  CouponDetails.this.getSupportActionBar().hide();
/*

                Button offers_menu = (Button) findViewById(R.id.myoffers);
                offers_menu.setText(RecyclerViewAdapter.vendor_name_temp);
                offers_menu.setTextColor(Color.parseColor("#FFFFFF"));
                offers_menu.setBackgroundColor(Color.parseColor("#EF6C00"));
                SharedPreferences status = getSharedPreferences("USER", 0);
                String TOKEN = status.getString("token", "");

*/

                String urlVisited ="http://api.clozerr.com/vendor/offers/myofferspage?vendor_id=" + detailsBundle.getString("vendorId")+"&access_token="+TOKEN;
                String urlUser = "http://api.clozerr.com/auth?fid="+detailsBundle.getString("fid")+"&access_token=" + TOKEN;

                Log.e("urlslide", urlVisited);
                //Toast.makeText(getApplicationContext(),urlVisited,Toast.LENGTH_SHORT).show();

                new AsyncGet(CouponDetails.this, urlVisited , new AsyncGet.AsyncResult() {
                    @Override
                    public void gotResult(String s) {
                        //  t1.setText(s);

                        Log.e("resultSlide", s);
                        // Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();


                          /*RecyclerViewAdapter1 Cardadapter = new RecyclerViewAdapter1(convertRowMyOffers(s), CouponDetails.this);
                          mRecyclerView.setAdapter(Cardadapter);*/

                        ArrayList<MyOffer> myOffers = convertRowMyOffers(s);
                        /*MyOffer currentOffer = getCurrentOffer(s);

                        MyOffersRecyclerViewAdapter myOffersAdapter = new MyOffersRecyclerViewAdapter(myOffers, currentOffer, CouponDetails.this);
                        mRecyclerView.setAdapter(myOffersAdapter);*/
                        MyOffersRecyclerViewAdapter adapter = new MyOffersRecyclerViewAdapter(myOffers, CouponDetails.this);
                        mRecyclerView.setAdapter(adapter);
                        try
                        {
                            Tracker t = ((Analytics) getApplication()).getTracker(Analytics.TrackerName.APP_TRACKER);

                            t.setScreenName(detailsBundle.getString("vendorId")+"_offer");

                            t.send(new HitBuilders.AppViewBuilder().build());
                        }
                        catch(Exception  e)
                        {
                            Toast.makeText(getApplicationContext(), "Error"+e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        //l1.setAdapter(adapter);
                        if(s==null) {
                            Toast.makeText(getApplicationContext(),"No internet connection",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        });
        drawer.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {
            @Override
            public void onDrawerClosed() {
                Button offers_menu = (Button) findViewById(R.id.myoffers);
                offers_menu.setText("My Offers");
                offers_menu.setTextColor(Color.parseColor("#EF6C00"));
                offers_menu.setBackgroundColor(Color.parseColor("#FFFFFF"));
                //initToolbar();
                CouponDetails.this.getSupportActionBar().show();
            }
        });


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

    /*public void showCheckinPopup() {
        final PopupWindow checkinPopup = getNewPopupWindow(detailsLayout, R.layout.checkin_popup);
        final LinearLayout displayView = (LinearLayout) checkinPopup.getContentView();

        for (int i = 0; i < displayView.getChildCount(); ++i) {
            View child = displayView.getChildAt(i);
            class MyWebViewClient extends WebViewClient {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            }
            switch(child.getId()) {
                case R.id.redeemButton: child.setOnClickListener
                        (new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                SharedPreferences status = getSharedPreferences("USER", 0);
                                String TOKEN = status.getString("token", "");

                                ((Button)displayView.findViewById(R.id.redeemButton)).setEnabled(false);
                                String url="http://api.clozerr.com/checkin/create?access_token=" + TOKEN + "&vendor_id=" + detailsBundle.getString("vendorId") +"&offer_id=" + detailsBundle.getString("offerId");

                                String gcm_id = GCMRegistrar.getRegistrationId(getApplicationContext());
                                if( !gcm_id.equals("") )
                                    url += "&gcm_id=" + gcm_id;

                                Log.e("CouponDetails url", url);

                               // Login.showProgressDialog(getApplicationContext());
                                new AsyncGet(CouponDetails.this, url, new AsyncGet.AsyncResult() {
                                    @Override
                                    public void gotResult(String s) {
                                        Log.e("pin", s);
                                        try {
                                              pinNumber = new JSONObject(s).getJSONObject("checkin").getString("pin");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        showConfirmPopup(pinNumber);
                                        checkinPopup.dismiss();
                                        if(s==null) {
                                            Toast.makeText(getApplicationContext(),"No internet connection",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                //showConfirmPopup(pinNumber);
                                //checkinPopup.dismiss();

                            }
                        });
                    break;
                case R.id.dialogTitleView: ((TextView) child).setText(detailsBundle.getString("vendorTitle"));
                    break;
                *//*case R.id.dialogOfferView: ((TextView) child).setText(detailsBundle.getString("offerText"));
                                           break;*//*
                *//*case R.id.webview_offer: ((WebView) child).setWebViewClient(new MyWebViewClient());
                                         ((WebView) child).lo*//*
            }
        }
        checkinPopup.showAtLocation(detailsLayout, Gravity.CENTER, 0, 0);
    }*/

    public void feedback(final String checkin_id)
    {
        Context c = CouponDetails.this;
        LayoutInflater lf = LayoutInflater.from(c);
        View feedbackView = lf.inflate(R.layout.dialog_reviews,null);
        RecyclerView rv = (RecyclerView) feedbackView.findViewById(R.id.list_questions);
        final EditText tv = (EditText) feedbackView.findViewById(R.id.text_remarks);

        final ArrayList<String> ques_arr = detailsBundle.getStringArrayList("questions");

        final ReviewQuestionsAdapter rqa = new ReviewQuestionsAdapter(ques_arr,c);
        rv.setAdapter(rqa);
        rv.setLayoutManager(new MyLinearLayoutManager(CouponDetails.this, LinearLayoutManager.VERTICAL, false ));

        final AlertDialog.Builder adbuilder = new AlertDialog.Builder(CouponDetails.this);
        adbuilder.setView(feedbackView);

        adbuilder.setCancelable(true);
        final AlertDialog alertDialog = adbuilder.show();

        /*WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(alertDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        //alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        alertDialog.getWindow().setAttributes(lp);*/
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button submitButton = (Button) feedbackView.findViewById(R.id.submit_feedback);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                //submit the reviews

                SharedPreferences status = getSharedPreferences("USER", 0);
                String TOKEN = status.getString("token", "");
                String remarks = tv.getText().toString();
                String url_review = "http://api.clozerr.com/review/create?access_token=" + TOKEN + "&checkin_id=" + checkin_id ;           //fill the url - use function getStarCount(position) to get the stars
                try{
                    url_review += "&remarks=" + URLEncoder.encode( remarks, "UTF-8");
                }catch( Exception e ){
                    e.printStackTrace();
                }

                for( int i = 0; i < ques_arr.size(); i ++ ) {
                    url_review += "&stars=" + rqa.getStarCount(i);
                }
                new AsyncGet(CouponDetails.this, url_review, new AsyncGet.AsyncResult() {

                    @Override
                    public void gotResult(String s) {
                        Log.e("review_response",s);
                        //Toast.makeText(getApplicationContext(),"Thank you for your reviews",Toast.LENGTH_SHORT).show();
                        finish();
                        System.exit(0);
                    }
                });
            }
        });

    }

    public void showConfirmPopup() {

        SharedPreferences status = getSharedPreferences("USER", 0);
        String TOKEN = status.getString("token", "");

        String url="http://api.clozerr.com/checkin/create?access_token=" + TOKEN + "&vendor_id=" + detailsBundle.getString("vendorId") +"&offer_id=" + detailsBundle.getString("offerId");
        String gcm_id = GCMRegistrar.getRegistrationId(getApplicationContext());
        if( !gcm_id.equals("") )
            url += "&gcm_id=" + gcm_id;

        Log.e(TAG, "url - " + url);

        // Login.showProgressDialog(getApplicationContext());
        new AsyncGet(CouponDetails.this, url, new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                Log.e("pin", s);
                BeaconFinderService.pauseScanningFor(CouponDetails.this, CHECK_IN_SCAN_TIMEOUT);
                try {
                    pinNumber = new JSONObject(s).getJSONObject("checkin").getString("pin");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(s==null) {
                    Toast.makeText(getApplicationContext(),"No internet connection",Toast.LENGTH_SHORT).show();
                } else {
                    final PopupWindow confirmPopup = getNewPopupWindow(detailsLayout, R.layout.checkin_pin_confirm);
                    final LinearLayout displayView = ((LinearLayout)((CardView)((LinearLayout)(confirmPopup.getContentView())).
                            getChildAt(0)).getChildAt(0));
                    detailsLayout.getForeground().mutate().setAlpha(255);
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
                    confirmPopup.showAtLocation(detailsLayout, Gravity.CENTER, 0, 0);
                }
            }
        });
    }

    /*public void JSONParseCheckin(String input)
    {
        try {
            JSONObject reader = new JSONObject(input);
            pinNumber = reader.getString("pin");
            gcmId = reader.getString("gcm_id");
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "String is not JSON", Toast.LENGTH_SHORT).show();
        }
    }*/
    @Override
    public void onBackPressed() {
        SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.sliding_drawer1);
        if(drawer.isOpened()) {
            drawer.animateClose();
        }
        else {
            super.onBackPressed();
        }
    }

    // ALTERNATE
    @Override
    public void onPause() {
        OneTimeBFS.checkAndStopScan(getApplicationContext());
        Log.d("HOME","destroy");
        //startService(new Intent(this, LocationService.class));
        super.onPause();
    }

    /*public MyOffer getCurrentOffer(String data) {
        try {
            JSONObject currentOfferJson = new JSONObject(data).getJSONArray("offers_qualified").getJSONObject(0);
            MyOffer currentOffer = new MyOffer(currentOfferJson.getString("caption"),
                    currentOfferJson.getInt("stamps"));
            return currentOffer;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/


    @Override
    public void onResume(){
        Log.d("destroyonstart","onstart");
        //stopService(new Intent(this, LocationService.class));
        super.onResume();
    }
    @Override
    protected void onStart(){
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }
    @Override
    protected void onStop(){
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }
}