package com.clozerr.app;

import android.content.Context;
import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;


public class FreebieDescription extends ActionBarActivity {
    String offerid="";
    String vendorid="",caption="",description="";
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
        Intent intent = getIntent();
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
        if(!caption.equals("")){
            ((TextView)findViewById(R.id.caption)).setText(caption);
            ((TextView)findViewById(R.id.title)).setText(caption);
        }
        if(!description.equals("")){
            ((TextView)findViewById(R.id.description)).setText(description);
        }
        findViewById(R.id.useit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://api.clozerr.com/v2/vendor/offers/checkin?access_token="+Home.TOKEN+"&offer_id="+offerid+"&vendor_id="+vendorid;
                Log.d(url, "");
                //Toast.makeText(getApplicationContext(),url,Toast.LENGTH_LONG).show();
                new AsyncGet(getApplicationContext(), url, new AsyncGet.AsyncResult() {
                    @Override
                    public void gotResult(String s) {
                        try {
                            JSONObject jsonObject = new JSONObject(s);
                            //Toast.makeText(getApplicationContext(),jsonObject.getString("vendor"),Toast.LENGTH_LONG).show();
                            if(jsonObject.getString("vendor")!=null){
                                Toast.makeText(getApplicationContext(),"Checked In Successfully. Please contact the billing staff.",Toast.LENGTH_SHORT).show();
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
                                        case R.id.pinView:  ((TextView) child).setText("");
                                            break;
                                        case R.id.confirmTitleView: ((TextView) child).setText(VendorActivity.detailsBundle.getString("vendorTitle"));
                                            break;
                                        case R.id.confirmOfferView: ((TextView) child).setText(caption);
                                            break;
                                        case R.id.qrButton: child.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent qrIntent = new Intent(FreebieDescription.this, QRActivity.class);
                                                qrIntent.putExtra("vendorId", vendorid);
                                                qrIntent.putExtra("offerId", offerid);
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
}
