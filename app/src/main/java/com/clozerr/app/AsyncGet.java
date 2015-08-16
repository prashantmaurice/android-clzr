package com.clozerr.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.clozerr.app.Utils.Logg;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by junaid on 2/11/14.
 */
public class AsyncGet extends AsyncTask<String, String, String> {

    AsyncResult asyncResult;
    String Url;
    Context c;
    static ProgressDialog pDialog;
    static Handler handler;
    boolean toDisplayProgress;
    public AsyncGet(Context context, String url, AsyncResult as, boolean displayProgress) {
        handler = new Handler(Looper.getMainLooper());
        if(GenUtils.isNetworkAvailable(context)) {
            c=context;
            asyncResult=as;
            this.Url = url;
            toDisplayProgress = displayProgress;
            if ((pDialog == null || !pDialog.isShowing()) && context instanceof Activity && toDisplayProgress) {
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
        else if (context instanceof Activity && toDisplayProgress)
            Logg.e("ERROR","Network error. Check your network connections and try again.");
        //TODO : disabled to stop nfitications, once check this stack
//            Toast.makeText(context,"Network error. Check your network connections and try again.",Toast.LENGTH_LONG).show();
    }

    public AsyncGet(Context context, String url, AsyncResult as) {
        this(context, url, as, true);
    }

    @Override
    protected String doInBackground(String... strings) {
        String content = "";
        HttpResponse httpResponse = null;
        HttpEntity httpEntity = null;

        int timeout = 3000;
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
        HttpConnectionParams.setSoTimeout(httpParams, timeout);

        DefaultHttpClient hc = new DefaultHttpClient( httpParams );
        HttpGet hGet = new HttpGet(Url);




        try {
            httpResponse = hc.execute(hGet);
            httpEntity = httpResponse.getEntity();
            content= EntityUtils.toString(httpEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            if (toDisplayProgress)
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(c, "Network error. Check your network connections and try again.",
                                Toast.LENGTH_LONG).show();
                    }
                });
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            if (toDisplayProgress)
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(c, "Network error. Check your network connections and try again.",
                                Toast.LENGTH_LONG).show();
                    }
                });
        } catch (IOException e) {
            e.printStackTrace();
            if (toDisplayProgress)
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
        dismissDialog();
        asyncResult.gotResult(result);
    }

    public static void dismissDialog() {
        if (pDialog != null && pDialog.isShowing())
            pDialog.dismiss();
    }

    public static abstract class AsyncResult{
        public abstract void gotResult(String s);
    }
}