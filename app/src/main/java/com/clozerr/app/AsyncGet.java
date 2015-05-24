package com.clozerr.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
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
    Context c;
    ProgressDialog pDialog;
    Handler handler;
    public AsyncGet(Context context, String url, AsyncResult as) {
        handler = new Handler(Looper.getMainLooper());
        if(isNetworkAvailable(context)) {
            c=context;
            asyncResult=as;
            this.Url = url;
            if (context instanceof Activity) {
                try {
                    pDialog = new ProgressDialog(context);
                    pDialog.setMessage("Loading...");
                    //pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    pDialog.setCancelable(false);
                    pDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
                this.execute(url);

        }
        else if (context instanceof Activity)
            Toast.makeText(context,"Network error. Check your network connections and try again.",Toast.LENGTH_LONG).show();
    }

    static boolean isNetworkAvailable(Context context) {
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
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(c, "Network error. Check your network connections and try again.",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
        return content;
    }
    @Override
    protected void onPostExecute(String result) {
        asyncResult.gotResult(result);
        if (c instanceof Activity)
            pDialog.hide();
    }

    public static abstract class AsyncResult{
        public abstract void gotResult(String s);
    }
}