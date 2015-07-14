package com.clozerr.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class FreebieDescription extends ActionBarActivity {
    String offerid="";
    String vendorid="",caption="",description="", name="";
    SharedPreferences status;
    String NotNow;
    ArrayList<String> pinned;
    ImageButton pin;
    ImageButton whatsappshare;
    ImageButton share;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_freebie_description);
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        final FrameLayout freebielayout=(FrameLayout)findViewById(R.id.freebiesdesclayout);
        freebielayout.getForeground().setAlpha(0);
        pin = (ImageButton) findViewById(R.id.pin);
        whatsappshare = (ImageButton) findViewById(R.id.whatsappshare);
        share = (ImageButton) findViewById(R.id.share);
        Intent intent = getIntent();
        updatePind();
        try {
            offerid += intent.getStringExtra("offerid");
        }catch (Exception e){

        }
        try {
            vendorid += intent.getStringExtra("vendorid");
        }catch (Exception e){

        }
        try {
            caption += intent.getStringExtra("caption");
        }catch (Exception e){

        }
        try {
            description += intent.getStringExtra("description");
        }catch (Exception e){

        }
        try {
            name += intent.getStringExtra("vendorName");
        }catch (Exception e) {

        }
        if(!caption.equals("")){
            ((TextView)findViewById(R.id.caption)).setText(caption);
            ((TextView)findViewById(R.id.title)).setText(caption);
        }
        if(!description.equals("")){
            ((TextView)findViewById(R.id.description)).setText(description);
        }
        if(pinned.indexOf(offerid)==-1)
           pin.setImageResource(R.drawable.pin100);
        else
           pin.setImageResource(R.drawable.pinfilled);
        findViewById(R.id.useit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://api.clozerr.com/v2/vendor/offers/checkin?access_token="+Home.TOKEN+"&offer_id="+offerid+"&vendor_id="+vendorid+"&gcm_id="+ GCMRegistrar.getRegistrationId(getApplicationContext());
                Log.d("FreebieDescription", "checkin url - " + url);
                //Toast.makeText(getApplicationContext(),url,Toast.LENGTH_LONG).show();
                new AsyncGet(getApplicationContext(), url, new AsyncGet.AsyncResult() {
                    @Override
                    public void gotResult(String s) {
                        try {
                            final JSONObject jsonObject = new JSONObject(s);
//                            if(jsonObject.getString("result").equals("false"))
//                                Toast.makeText(getApplicationContext(),"Sorry you are yet to unlock the reward",Toast.LENGTH_SHORT).show();
                            //Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
                            if(jsonObject.getString("vendor")!=null){
                                //Toast.makeText(getApplicationContext(),"Checked In Successfully. Please contact the billing staff.",Toast.LENGTH_SHORT).show();
                                final PopupWindow confirmPopup = getNewPopupWindow(freebielayout, R.layout.checkin_pin_confirm);
                                final LinearLayout displayView = ((LinearLayout)((CardView)((LinearLayout)(confirmPopup.getContentView())).
                                        getChildAt(0)).getChildAt(0));
                                freebielayout.getForeground().mutate().setAlpha(255);
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
                                        case R.id.pinView:  ((TextView) child).setText(jsonObject.getString("pin"));
                                            break;
                                        case R.id.confirmTitleView: ((TextView) child).setText(name);
                                            break;
                                        case R.id.confirmOfferView: ((TextView) child).setText(caption);
                                            break;
                                        case R.id.qrButton: child.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent qrIntent = new Intent(FreebieDescription.this, QRActivity.class);
                                                qrIntent.putExtra("vendorId", vendorid);
                                                qrIntent.putExtra("offerId", offerid);
                                                try {
                                                    qrIntent.putExtra("checkinId", jsonObject.getString("_id"));
                                                } catch (JSONException ex) {
                                                    ex.printStackTrace();
                                                }
                                                startActivity(qrIntent);
                                            }
                                        });
                                    }
                                }
                                confirmPopup.showAtLocation(freebielayout, Gravity.CENTER, 0, 0);
                            }
                            else
                                Toast.makeText(getApplicationContext(),"Check In Failed. Please try again",Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        findViewById(R.id.pin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String urlPinning = "http://api.clozerr.com/v2/user/add/pinned?access_token=" + Home.TOKEN +"&offer_id="+offerid;
                if(pinned.indexOf(offerid)==-1)
                {
                    new AsyncGet(getApplicationContext(), urlPinning, new AsyncGet.AsyncResult() {
                        @Override
                        public void gotResult(String s) {
                            try {
                                JSONObject obj = new JSONObject(s);
                                final SharedPreferences.Editor editor = getSharedPreferences("USER", 0).edit();
                                editor.putString("user",s);
                                editor.apply();
                                //Toast.makeText(getActivity(),s,Toast.LENGTH_SHORT).show();
                                pin.setImageResource(R.drawable.pinfilled);
                                Toast.makeText(getApplicationContext(), "Added to pinned offers", Toast.LENGTH_SHORT).show();
                                //Toast.makeText(getActivity(),vendors.toString(),Toast.LENGTH_SHORT).show();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //  t1.setText(s);
                            if (s == null) {
                                Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                            }
                            //l1.setAdapter(adapter);
                        }
                    });
                }
                else
                {
                    new AsyncGet(getApplicationContext(), "http://api.clozerr.com/v2/user/remove/pinned?access_token=" + Home.TOKEN +"&offer_id="+offerid, new AsyncGet.AsyncResult() {
                        @Override
                        public void gotResult(String s) {
                            try {
                                JSONObject obj = new JSONObject(s);
                                final SharedPreferences.Editor editor = getSharedPreferences("USER", 0).edit();
                                editor.putString("user",s);
                                editor.apply();
                                //Toast.makeText(getActivity(),s,Toast.LENGTH_SHORT).show();
                                pin.setImageResource(R.drawable.pin100);
                                Toast.makeText(getApplicationContext(), "Removed from pinned offers", Toast.LENGTH_SHORT).show();
                                //Toast.makeText(getActivity(),vendors.toString(),Toast.LENGTH_SHORT).show();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //  t1.setText(s);
                            if (s == null) {
                                Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                            }
                            //l1.setAdapter(adapter);
                        }
                    });
                }
            }
        });
        whatsappshare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickWhatsApp();
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "Check out the offer I got @ " +VendorActivity.detailsBundle.getString("vendorTitle")+" using Clozerr: "+description+" https://play.google.com/store/apps/details?id=com.clozerr.app";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Clozerr");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        });
    }
    public void updatePind(){
        status = getSharedPreferences("USER", 0);
        NotNow = status.getString("notNow","false");
        pinned = new ArrayList<String>();
        JSONArray pind ;
        if(NotNow.equals("false")) {
            try {
                JSONObject userobj = new JSONObject(status.getString("user", "null"));
                pind = userobj.getJSONArray("pinned");
                Log.i("pinned", pind.toString());
                if (pind != null) {
                    int len = pind.length();
                    for (int i = 0; i < len; i++) {
                        pinned.add(pind.get(i).toString());
                    }
                }

            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_freebie_description, menu);
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
    public void onClickWhatsApp() {

        PackageManager pm=getPackageManager();
        try {

            Intent waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setType("text/plain");
            String text = "Check out the offer I got @ " +VendorActivity.detailsBundle.getString("vendorTitle")+" using Clozerr: "+description+" https://play.google.com/store/apps/details?id=com.clozerr.app";

            PackageInfo info=pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            //Check if package exists or not. If not then code
            //in catch block will be called
            waIntent.setPackage("com.whatsapp");
            waIntent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(waIntent, "Share with"));

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(getApplicationContext(), "WhatsApp not Installed", Toast.LENGTH_SHORT)
                    .show();
        }

    }
}
