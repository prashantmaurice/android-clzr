package com.clozerr.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Created by junaid on 2/11/14.
 */
public class AsyncGet extends AsyncTask<String, String, String> {

    AsyncResult asyncResult;
    String Url;
    ProgressDialog pDialog;
    public AsyncGet(Context context, String url, AsyncResult as) {
        if(isNetworkAvailable(context)) {
            asyncResult=as;
            this.Url = url;

            pDialog = new ProgressDialog(context);
            pDialog.setMessage("Loading...");
            //pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pDialog.setCancelable(false);
            pDialog.show();
            this.execute(url);
        }
        else Toast.makeText(context,"Network error. Check your network connections and try again.",Toast.LENGTH_LONG).show();
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected String doInBackground(String... strings) {
        String content = "";
        HttpClient hc = new DefaultHttpClient();
        HttpGet hGet = new HttpGet(Url);
        ResponseHandler<String> rHand = new BasicResponseHandler();
        try {
            content = hc.execute(hGet,rHand);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return content;
    }
    @Override
    protected void onPostExecute(String result) {
        asyncResult.gotResult(result);
        pDialog.hide();
    }

    public static abstract class AsyncResult{
        public abstract void gotResult(String s);
    }
}