package com.clozerr.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONObject;


public class AboutUs extends ActionBarActivity {
    private WebView webView;

    public static Context c;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);
        nitView();
        if (toolbar != null) {
            toolbar.setTitle("");
            setSupportActionBar(toolbar);
            Log.d("faq", "toolbar");
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        // Create reference to UI elements
        webView  = (WebView) findViewById(R.id.webview_faq);

        // workaround so that the default browser doesn't take over
        webView.setWebViewClient(new MyWebViewClient());

        // Setup click listener
        openURL();
    }

    private void openURL() {

        String sum = MainApplication.getInstance().data.cacheMain.about_us_html;
        if(sum.isEmpty()){
            sum = "loading....";
        }
        webView.loadData(sum,"text/html",null);
        new AsyncGet(this, "http://api.clozerr.com/content?key=about_us", new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                JSONObject html = null;
                try {
                    html = new JSONObject(s);
                    String about_us_html=html.getString("data");
                    MainApplication.getInstance().data.cacheMain.about_us_html = about_us_html;
                    MainApplication.getInstance().data.cacheMain.saveCacheDataLocally();
                    webView.loadData(about_us_html,"text/html",null);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(s==null) {
                    Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                }

            }
        });

        webView.requestFocus();
    }

    class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    private void nitView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        Log.d("faq", "nitView");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_faq, menu);
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
        if (id == R.id.home) {
//            finish();
            NavUtils.navigateUpFromSameTask(this);
        }


        return super.onOptionsItemSelected(item);
    }
}
