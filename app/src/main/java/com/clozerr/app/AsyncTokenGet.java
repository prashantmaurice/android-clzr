package com.clozerr.app;

import android.accounts.Account;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.plus.Plus;

import java.io.IOException;

/**
 * Created by S.ARAVIND on 1/23/2015.
 */
public class AsyncTokenGet extends AsyncTask<Void, Void, String> {

    /*Activity mInvokingActivity;
    AsyncTokenResult mResult;
    static ProgressDialog pDialog;
    public AsyncTokenGet(Context context, AsyncTokenResult result, Activity invokingActivity) {
        mResult = result;
        mInvokingActivity = invokingActivity;

        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Loading...");
        //pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setCancelable(false);
        pDialog.show();
        this.execute();
    }

    @Override
    protected String doInBackground(Void... params) {
        String token;

        String scopes = "oauth2:" + Scopes.PLUS_LOGIN;
                //"https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/plus.login";

        try {
            token = GoogleAuthUtil.getToken(
                    mInvokingActivity,
                    Plus.AccountApi.getAccountName(Login.googleApiClient),
                    scopes);
            // Log.e("AccessToken", token);
        } catch (IOException transientEx) {
            // network or server error, the call is expected to succeed if you try again later.
            // Don't attempt to call again immediately - the request is likely to
            // fail, you'll hit quotas or back-off.
            return null;
        } catch (UserRecoverableAuthException e) {
            // Recover
            mInvokingActivity.startActivityForResult(e.getIntent(), 0);
            token = null;
        } catch (GoogleAuthException authEx) {
            // Failure. The call is not expected to ever succeed so it should not be
            // retried.
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return token;
    }

    @Override
    protected void onPostExecute(String result) {
        dismissDialog();
        mResult.gotResult(result);
    }

    public static void dismissDialog() {
        if (pDialog != null && pDialog.isShowing())
            pDialog.dismiss();
    }

    public static abstract class AsyncTokenResult{
        public abstract void gotResult(String s);
    }*/
    private static final String TAG = "AsyncTokenGet";
    private Activity mActivity;
    private AsyncTokenResult mResult;
    static ProgressDialog pDialog;

    public AsyncTokenGet(Activity activity, AsyncTokenResult result) {
        mActivity = activity;
        mResult = result;

        pDialog = new ProgressDialog(mActivity);
        pDialog.setMessage("Loading...");
        //pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setCancelable(false);
        pDialog.show();

        this.execute();
    }

    @Override
    protected String doInBackground(Void... params) {
        String accountName = Plus.AccountApi.getAccountName(Login.googleApiClient);
        Account account = new Account(accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        //String scopes = "audience:server:client_id:" + Constants.SERVER_CLIENT_ID; // Not the app's client ID.
        String scopes = "oauth2:" + Scopes.PLUS_LOGIN + " " + Scopes.PROFILE;
        try {
            return GoogleAuthUtil.getToken(mActivity, account, scopes);
        } catch (IOException|GoogleAuthException e) {
            Log.e(TAG, "Error retrieving ID token.", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        Log.e(TAG, "ID token: " + result);
        dismissDialog();
        if (result != null) {
            // Successfully retrieved ID Token
            mResult.onResult(result);
        } else {
            // There was some error getting the ID Token
            mResult.onError();
        }
    }

    public static void dismissDialog() {
        if (pDialog != null && pDialog.isShowing())
            pDialog.dismiss();
    }

    public static abstract class AsyncTokenResult{
        public abstract void onResult(String s);
        public abstract void onError();
    }
}
