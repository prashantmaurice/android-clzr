package com.clozerr.app.Activities.UtilActivities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.clozerr.app.AsyncGet;
import com.clozerr.app.GenUtils;
import com.clozerr.app.MainApplication;
import com.clozerr.app.R;
import com.clozerr.app.Utils.Constants;
import com.google.android.gcm.GCMRegistrar;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import org.json.JSONObject;

import java.util.Arrays;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 *  This activity is used when you check-in a restaurant, and a popup shows,
 *  it can call this activity to scan QR codes
 */

public class QRActivity extends FragmentActivity implements ZXingScannerView.ResultHandler {

    private static final String TAG = "QRActivity";
    private static final BarcodeFormat[] barcodeFormats =
            new BarcodeFormat[]{ BarcodeFormat.QR_CODE };
    private ZXingScannerView mScannerView;
    private String mVendorId = null, mOfferId = null, mCheckinId = null;

    public static String EXTRA_VENDORID = "vendorId";
    public static String EXTRA_CHECKINID = "checkinId";
    public static String EXTRA_OFFERID = "offerId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        mScannerView = (ZXingScannerView) findViewById(R.id.qrScannerView);
        if (savedInstanceState == null) {
            Intent callingIntent = getIntent();
            if (mVendorId == null && callingIntent.hasExtra(EXTRA_VENDORID))
                mVendorId = callingIntent.getStringExtra(EXTRA_VENDORID);
            if (mOfferId == null && callingIntent.hasExtra(EXTRA_OFFERID))
                mOfferId = callingIntent.getStringExtra(EXTRA_OFFERID);
            if (mCheckinId == null && callingIntent.hasExtra(EXTRA_CHECKINID))
                mCheckinId = callingIntent.getStringExtra(EXTRA_CHECKINID);
        }
        // the other condition is handled on onRestoreInstanceState
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_qr, menu);
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_VENDORID, mVendorId);
        outState.putString(EXTRA_OFFERID, mOfferId);
        outState.putString(EXTRA_CHECKINID, mCheckinId);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        mVendorId = savedInstanceState.getString(EXTRA_VENDORID);
        mOfferId = savedInstanceState.getString(EXTRA_OFFERID);
        mCheckinId = savedInstanceState.getString(EXTRA_CHECKINID);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onPause() {
        mScannerView.stopCamera();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mVendorId != null && mOfferId != null && mCheckinId != null) {
            mScannerView.setResultHandler(this);
            mScannerView.setFormats(Arrays.asList(barcodeFormats));
            mScannerView.startCamera();
        }
        else {
            Toast.makeText(QRActivity.this, "An error occurred, please try again.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void handleResult(Result rawResult) {
        /*Log.e(TAG, "result - " + rawResult.getText());
        Log.e(TAG, "format - " + rawResult.getBarcodeFormat().toString());*/
//        String TOKEN = getSharedPreferences("USER", 0).getString("token", "");
        String TOKEN = MainApplication.getInstance().tokenHandler.clozerrtoken;
        String gcmId = GCMRegistrar.getRegistrationId(getApplicationContext());
        final String validateURL = GenUtils.getClearedUriBuilder(Constants.URLBuilders.QRCODE_VALIDATE)
                                            .appendQueryParameter("access_token", TOKEN)
                                            .appendQueryParameter("vendor_id", mVendorId)
                                            .appendQueryParameter("checkin_id", mCheckinId)
                                            .appendQueryParameter("gcm_id", gcmId)
                                            .appendQueryParameter("qrcode", rawResult.getText())
                                            .build().toString();
        Log.e(TAG, "validating with url - " + validateURL);
        new AsyncGet(this, validateURL, new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                try {
                    JSONObject result = new JSONObject(s);
                    if (result.has("_id")) {
                        Toast.makeText(QRActivity.this, "Your check-in has been validated successfully.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else {
                        Toast.makeText(QRActivity.this, "Wrong QR code scanned. Please try again.", Toast.LENGTH_SHORT).show();
                        mScannerView.startCamera();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(QRActivity.this, "Connection error, please check your internet connection and try again.", Toast.LENGTH_SHORT).show();
                    mScannerView.startCamera();
                }
            }
        });
    }
}
