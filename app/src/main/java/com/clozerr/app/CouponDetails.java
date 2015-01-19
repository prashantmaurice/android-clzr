package com.clozerr.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
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
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class CouponDetails extends ActionBarActivity {

    private FrameLayout detailsLayout;
    private Toolbar backToolbar;
    private TextView titleView, locView, nextOfferView, detailsView;
    private ImageView itemImageView;
    private CircleImageView callButton, dirButton, rateButton;
    private TextView checkinButton;
    private Bundle detailsBundle;
    private String pinNumber, gcmId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon_details);
        System.out.println("set content view");
        Intent callingIntent = getIntent();
        initViews();

        detailsBundle = new Bundle();
        String vendor_id = callingIntent.getStringExtra("vendor_id");
        String offer_id = callingIntent.getStringExtra("offer_id");
        String offer_text = callingIntent.getStringExtra("offer_text");

        detailsBundle.putString("offerId", offer_id);
        detailsBundle.putString("offerText", offer_text);
        final ArrayList<String> ques_arr = new ArrayList<String>();


        //Toast.makeText(this, "id - " + vendor_id, Toast.LENGTH_SHORT).show();
        //Log.e("idvendor", vendor_id);
        String url_coupon = "http://api.clozerr.com/vendor/get?vendor_id=" + vendor_id;
        //Toast.makeText(this, "url - " + url_coupon, Toast.LENGTH_SHORT).show();
        //Log.e("urlcoupon", url_coupon);
        new AsyncGet(CouponDetails.this, url_coupon, new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                //Log.i("ghfv","Inside gotResu");
                //Toast.makeText(CouponDetails.this, "Inside gotResult()", Toast.LENGTH_SHORT).show();
                String phonenumber="0123456789";
                String vendorDescription="No Restaurant Description Available Now";
                try {
                    Log.e("resultAsync", s);
                    JSONObject object = new JSONObject(s);
                    phonenumber = object.getString("phone");
                    vendorDescription = object.getString("description");
                    Log.e("description", vendorDescription);
                    CardModel currentItem = new CardModel(
                            object.getString("name"),
                            phonenumber,
                            vendorDescription,
                            object.getJSONArray("offers"),
                            object.getJSONArray("location").getDouble(0),
                            object.getJSONArray("location").getDouble(1),
                            object.getString("image"),
                            object.getString("fid"),object.getString("_id"),
                            0
                    );
                     JSONArray question= object.getJSONArray("question");
                    for(int i=0;i<question.length();i++){
                      ques_arr.add(question.getString(i));
                    }
                    //Log.e("title", currentItem.getTitle());
                    //Toast.makeText(CouponDetails.this, "title - " + currentItem.getTitle(), Toast.LENGTH_SHORT).show();
                    detailsBundle.putString("vendorTitle", currentItem.getTitle());
                    //detailsBundle.putString("offerText", currentItem.getOfferDescription() );
                    detailsBundle.putString("vendorId", currentItem.getVendorId());
                    detailsBundle.putString("description", vendorDescription);
                    //detailsBundle.putString("offerId", currentItem.getOfferId());
                    detailsBundle.putString("vendorImage", currentItem.getImageId());
                    detailsBundle.putDouble("latitude", currentItem.getLat());
                    detailsBundle.putDouble("longitude", currentItem.getLong());
                    detailsBundle.putString("distance", currentItem.getDistance());
                    detailsBundle.putString("phonenumber", phonenumber);

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
                    //TODO get description
                    detailsView.setText(detailsBundle.getString("description"));
                    //ScrollView scroller = new ScrollView(this);
                    //detailsView.setText(detailsBundle.getString("Restaurant Description"));
                    //  scroller.addView(detailsView);
                    locView.setText(detailsBundle.getString("distance"));

                    checkinButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showCheckinPopup();
                        }
                    });

                    callButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                            dialIntent.setData(Uri.parse("tel:"+detailsBundle.getString("phonenumber")));
                            startActivity(dialIntent);
                        }
                    });

                    dirButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                    Uri.parse("http://maps.google.com/maps?daddr="+detailsBundle.getDouble("latitude")+","+detailsBundle.getDouble("longitude")));
                            startActivity(intent);
                        }
                    });

                    rateButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Context c = CouponDetails.this;
                            LayoutInflater lf = LayoutInflater.from(c);
                            View feedbackView = lf.inflate(R.layout.dialog_reviews,null);
                            RecyclerView rv = (RecyclerView) feedbackView.findViewById(R.id.list_questions);
                            rv.setLayoutManager(new LinearLayoutManager(c));
                            rv.setItemAnimator(new DefaultItemAnimator());
                            rv.setHasFixedSize(true);


       /*                     ques_arr.add("How would you rate the ambiance of the restaurant?");
                            ques_arr.add("How would you rate the ambiance of the restaurant?");
                            ques_arr.add("How would you rate the ambiance of the restaurant?");
                            ques_arr.add("How would you rate the ambiance of the restaurant?");
                            ques_arr.add("How would you rate the ambiance of the restaurant?");*/
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
                                }
                            });
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
}

    private void initViews() {
        detailsLayout = (FrameLayout) findViewById(R.id.detailsLayout);
        detailsLayout.getForeground().setAlpha(0);
        backToolbar = (Toolbar) findViewById(R.id.backToolbar);
        itemImageView = (ImageView) findViewById(R.id.itemImageView);
        titleView = (TextView) findViewById(R.id.itemTitleView);
        locView = (TextView) findViewById(R.id.itemLocView);
        nextOfferView = (TextView) findViewById(R.id.itemNextOfferView);
        detailsView = (TextView) findViewById(R.id.itemDetailsView);
        checkinButton = (TextView) findViewById(R.id.checkinButton);
        callButton = (CircleImageView) findViewById(R.id.itemCallButton);
        dirButton = (CircleImageView) findViewById(R.id.itemDirButton);
        rateButton = (CircleImageView) findViewById(R.id.itemRateButton);
        setSupportActionBar(backToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        slidingMyCards();
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

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        if (id == R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

   /* private void slidingMyCards() {
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
                        offer_recyclerAdapter Cardadapter = new offer_recyclerViewAdapter(convertRowMyCard(s), CouponDetails.this);
                        ;
                        mRecyclerView.setAdapter(Cardadapter);

                        //l1.setAdapter(adapter);
                    }
                });

            }

        });


    }*/
    private List<MyOffer> convertRowMyCard(String s) {
        List<MyOffer> rowItems;
        rowItems = new ArrayList<MyOffer>();
        JSONObject temp;
        temp = null;
        JSONArray array = null;
        try {
            Log.e("stringfunction", s);
            temp = new JSONObject(s);
            array = temp.getJSONArray("offers");

            for(int i = 0 ; i < array.length() ; i++){
                MyOffer item = new MyOffer(array.getJSONObject(i).getString("caption"),i+1);
                rowItems.add(item);
                System.out.println(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowItems;
    }

  /*  private void slidingMyCards() {
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

                        /*ArrayAdapter<MyOffer> offAdapter = new ArrayAdapter<MyOffer>(getApplicationContext(),R.layout.offer_list,convertRowMyCard(s));//offerAdapter(convertRowMyCard(s),CouponDetails.this);
                        offAdapter.add(new MyOffer("dummy",2));
                        offAdapter.add(new MyOffer("dummy",2));
                        MyOfferAdapter offerAdapter = new MyOfferAdapter(convertRowMyCard(s),getApplicationContext());
                        mListView.setAdapter(offerAdapter);
                    }
                });
            }
        });


    }*/
  private void slidingMyCards() {
      SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.sliding_drawer1);
      final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.sliding_list);
      mRecyclerView.setLayoutManager(new LinearLayoutManager(CouponDetails.this));
      mRecyclerView.setItemAnimator(new DefaultItemAnimator());
      mRecyclerView.setHasFixedSize(true);
      drawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {

          @Override

          public void onDrawerOpened() {

              Button offers_menu = (Button) findViewById(R.id.myoffers);
              offers_menu.setText(RecyclerViewAdapter.vendor_name_temp);
              offers_menu.setTextColor(Color.parseColor("#FFFFFF"));
              offers_menu.setBackgroundColor(Color.parseColor("#EF6C00"));
              SharedPreferences status = getSharedPreferences("USER", 0);
              String TOKEN = status.getString("token", "");

              String urlVisited ="http://api.clozerr.com/vendor/get?vendor_id=" + detailsBundle.getString("vendorId")+"&access_token="+TOKEN;
              String urlUser = "http://api.clozerr.com/auth?fid="+detailsBundle.getString("fid")+"&access_token=" + TOKEN;

              Log.e("urlslide", urlVisited);
              //Toast.makeText(getApplicationContext(),urlVisited,Toast.LENGTH_SHORT).show();

              new AsyncGet(CouponDetails.this, urlVisited , new AsyncGet.AsyncResult() {
                  @Override
                  public void gotResult(String s) {
                      //  t1.setText(s);

                      Log.e("resultSlide", s);
                     // Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();


                      RecyclerViewAdapter1 Cardadapter = new RecyclerViewAdapter1(convertRowMyCard(s), CouponDetails.this);
                      mRecyclerView.setAdapter(Cardadapter);

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
            }
        });


  }


    public PopupWindow getNewPopupWindow(final FrameLayout parent, int layoutId)
    {
        parent.getForeground().setAlpha(Color.alpha(getResources().getColor(R.color.dimmer)));
        final PopupWindow popupWindow = new PopupWindow(this);
        LayoutInflater inflater = (LayoutInflater)getApplicationContext().
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View displayView = inflater.inflate(layoutId, null);
        popupWindow.setFocusable(true);
        popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
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

    public void showCheckinPopup() {
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
                /*case R.id.dialogOfferView: ((TextView) child).setText(detailsBundle.getString("offerText"));
                                           break;*/
                /*case R.id.webview_offer: ((WebView) child).setWebViewClient(new MyWebViewClient());
                                         ((WebView) child).lo*/
            }
        }
        checkinPopup.showAtLocation(detailsLayout, Gravity.CENTER, 0, 0);
    }

    public void feedback(View v)
    {
        Context c = getApplicationContext();
        LayoutInflater lf = LayoutInflater.from(c);
        View feedbackView = lf.inflate(R.layout.dialog_reviews,null);
        RecyclerView rv = (RecyclerView) feedbackView.findViewById(R.id.list_questions);

        ArrayList<String> ques_arr = new ArrayList<String>();
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
            }
        });

    }

    public void showConfirmPopup(String pin) {

        final PopupWindow confirmPopup = getNewPopupWindow(detailsLayout, R.layout.checkin_pin_confirm);
        final LinearLayout displayView = (LinearLayout) confirmPopup.getContentView();


        for (int i = 0; i < displayView.getChildCount(); ++i) {
            View child = displayView.getChildAt(i);
            switch(child.getId()) {
                case R.id.confirmButton: child.setOnClickListener
                        (new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                               // Intent homeIntent = new Intent(getApplicationContext(), Home.class);
                               // homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                              //  getApplicationContext().startActivity(homeIntent);
                                CouponDetails.this.finish();
                            }
                        });
                    break;
                case R.id.pinView:  ((TextView) child).setText(pin);
                    break;
                case R.id.confirmTitleView: ((TextView) child).setText(detailsBundle.getString("vendorTitle"));
                    break;
            }
        }
        confirmPopup.showAtLocation(detailsLayout, Gravity.CENTER, 0, 0);
    }

    public void JSONParseCheckin(String input)
    {
        try {
            JSONObject reader = new JSONObject(input);
            pinNumber = reader.getString("pin");
            gcmId = reader.getString("gcm_id");
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "String is not JSON", Toast.LENGTH_SHORT).show();
        }
    }
    public void onBackPressed() {
        SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.sliding_drawer1);
        if(drawer.isOpened()) {
            drawer.close();
        }
        else {
            super.onBackPressed();
        }

        //DrawerLayout d1 = (DrawerLayout) findViewById(R.id.drawerLayout);
    }
}