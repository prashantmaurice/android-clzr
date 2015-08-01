package com.clozerr.app;

import android.content.Context;
import android.content.SharedPreferences;
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


        SharedPreferences status = getSharedPreferences("USER", 0);
        final String sum = status.getString("about_us_html", /*"<html><body><div><div><div><div><div><h5>What is CLOZERR?</h5></div><div id=\\\"collapseOne\\\"><div>Clozerr is an android application which allows you to try out awesome places around you by giving you an attractive offer on your first visit followed by a loyalty program to reward you on your subsequent visits. </div></div></div><div><div><h5>How can I redeem an offer in CLOZERR?</h5></div><div id=\\\"collapseTwo\\\"><div>Visit the place listed on Clozerr and tap the 'Check In' button, that's it sit back and enjoy the offer.</div></div></div><div><div><h5 >What are loyalty rewards?</h5></div><div id=\\\"collapseThree\\\"><div>Loyalty rewards are those offers which are provided to you on your subsequent visits to the same place before the campaign ends. For example a restaurant can give you a free pizza on your 4th visit. You need to mark your visits at the store so as to use this loyalty offer. </div>   </div></div><div><div ><h5>What kind of places can I find on CLOZERR?</h5></div><div id=\\\"collapseFour\\\"><div>Clozerr has tie-ups with various kinds of businesses ranging from general departmental store to high end restaurants. It is an app which can be used by all sorts of customers for all needs.</div></div></div><div><div><h5>Will I get intelligent push notifications from the app?</h5></div><div id=\\\"collapseSix\\\"><div>Yes indeed. You will get intelligent push notifications from the app.x</div></div>   </div><div><div><h5>Can I suggest any particular place in my neighbourhood to CLOZERR?</h5></div><div id=\\\"collapseSeven\\\"><div>Yes, you can suggest any new restaurant/salon/boutique/cafe which you would like to be a part of Clozerr. You can contact us and we will contact the shop owner. </div></div></div><div><div><h5>How do I reach out to CLOZERR?</h5></div><div id=\\\"collapseEight\\\"><div>Please mail us at- mail@clozerr.com</div></div></div></div></div></body></html>"*/ "About Us");
//        final String sum = status.getString("about_us_html", "About Us");
        webView.loadData(sum,"text/html",null);
        new AsyncGet(this, "http://api.clozerr.com/content?key=about_us", new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                JSONObject html = null;
                try {
                    html = new JSONObject(s);
                    String about_us_html=html.getString("data");
                    if(sum.equals(about_us_html)) {

                    }
                    else{


//                        final SharedPreferences.Editor editor = getSharedPreferences("USER", 0).edit();
//                        editor.putString("about_us_html", about_us_html);
//                        editor.apply();
                        webView.loadData(about_us_html,"text/html",null);
                    }

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
            NavUtils.navigateUpFromSameTask(this);
        }


        return super.onOptionsItemSelected(item);
    }
}
