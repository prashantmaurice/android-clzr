package com.clozerr.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.Arrays;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class QRActivity extends ActionBarActivity implements ZXingScannerView.ResultHandler {

    private static final String TAG = "QRActivity";
    private static final BarcodeFormat[] barcodeFormats =
            new BarcodeFormat[]{ BarcodeFormat.QR_CODE };
    private ZXingScannerView mScannerView;
    private String mVendorId = null, mOfferId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        mScannerView = (ZXingScannerView) findViewById(R.id.qrScannerView);
        if (savedInstanceState == null) {
            Intent callingIntent = getIntent();
            if (mVendorId == null && callingIntent.hasExtra("vendorId"))
                mVendorId = callingIntent.getStringExtra("vendorId");
            if (mOfferId == null && callingIntent.hasExtra("offerId"))
                mOfferId = callingIntent.getStringExtra("offerId");
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
        outState.putString("vendorId", mVendorId);
        outState.putString("offerId", mOfferId);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        mVendorId = savedInstanceState.getString("vendorId");
        mOfferId = savedInstanceState.getString("offerId");
    }

    @Override
    public void onPause() {
        mScannerView.stopCamera();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.setFormats(Arrays.asList(barcodeFormats));
        mScannerView.startCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        Log.e(TAG, "result - " + rawResult.getText());
        Log.e(TAG, "format - " + rawResult.getBarcodeFormat().toString());
        finish();
    }
}
