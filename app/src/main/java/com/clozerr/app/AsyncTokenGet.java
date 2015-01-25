package com.clozerr.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.plus.Plus;

import java.io.IOException;

/**
 * Created by S.ARAVIND on 1/23/2015.
 */
public class AsyncTokenGet extends AsyncTask<Void, Void, String> {

    Context mContext;
    AsyncTokenResult mResult;
    ProgressDialog pDialog;
    public AsyncTokenGet(Context context, AsyncTokenResult result) {
        mContext = context;
        mResult = result;

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
                    mContext,
                    Plus.AccountApi.getAccountName(Login.mGoogleApiClient),
                    scopes);
            // Log.e("AccessToken", token);
        } catch (IOException transientEx) {
            // network or server error, the call is expected to succeed if you try again later.
            // Don't attempt to call again immediately - the request is likely to
            // fail, you'll hit quotas or back-off.
            return null;
        } catch (UserRecoverableAuthException e) {
            // Recover
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
        pDialog.hide();
        mResult.gotResult(result);
    }

    public static abstract class AsyncTokenResult{
        public abstract void gotResult(String s);
    }
}
