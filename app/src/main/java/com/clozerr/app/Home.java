package com.clozerr.app;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.plus.Plus;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import static com.clozerr.app.R.drawable.rest1;
import static com.clozerr.app.R.drawable.rest2;
import static com.clozerr.app.R.drawable.rest4;
import static com.clozerr.app.R.drawable.rest6;
import static com.clozerr.app.R.drawable.rest7;

public class Home  extends ActionBarActivity {

    static final String SENDER_ID = "496568600186";  // project id from Google Console
    public static String TOKEN = "";
    public static String USERNAME = "";
    public static String USERID = "";
    public static String USER_PIC_URL = "";
    public static Context c;
    Button button;
    static double lat;
    static double longi;

    private Toolbar toolbar;
    private ViewPager pager;
    private SlidingTabLayout mtabs;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ListView leftDrawerList;
    //private ArrayAdapter<String> navigationDrawerAdapter;
    private NavDrawAdapter nav;
    private String[] leftSliderData = {"About us","FAQ's","Like/Follow Clozerr","Rate Clozerr", "Tell Friends about Clozerr", "Settings", "Log out"};
    private FrameLayout freebielayout;
    //private boolean nav_drawer_open = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GeofenceManagerService.startService(this);
        /*try
        {
            Tracker t = ((Analytics) getApplication()).getTracker(Analytics.TrackerName.APP_TRACKER);

            t.setScreenName("home");

            t.send(new HitBuilders.AppViewBuilder().build());
        }
        catch(Exception  e)
        {
            Toast.makeText(getApplicationContext(), "Error"+e.getMessage(), Toast.LENGTH_LONG).show();
        }
        */

        if (logincheck()==0)
            return;

           // Enable if strict location is required
        c = Home.this;
        setContentView(R.layout.activity_my);
        nitView();
        if (toolbar != null) {
            //toolbar.setTitle(getString(R.string.app_name));

            toolbar.setTitle("");
            ((TextView) toolbar.findViewById(R.id.appTitleView)).setTypeface(Typeface.createFromAsset(
                    getAssets(), "fonts/comfortaa.ttf"
            ));
            setSupportActionBar(toolbar);

        }
        initDrawer();
        freebielayout=(FrameLayout)findViewById(R.id.homeframe);
        //freebielayout.getForeground().setAlpha(0);
        offerdialog();
        SharedPreferences status2 = getSharedPreferences("USER", 0);
        TOKEN = status2.getString("token", "");
        TimeZone tz = TimeZone.getTimeZone("GMT+0530");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());
        final String analyticsurl= Constants.URLBuilders.ANALYTICS
                .appendQueryParameter("metric","Clozerr_Home_Screen")
                .appendQueryParameter("dimensions[device]", "Android API " + Build.VERSION.SDK_INT)
                .appendQueryParameter("dimensions[id]", Settings.Secure.getString(this.getContentResolver(),
                        Settings.Secure.ANDROID_ID))
                .appendQueryParameter("time", nowAsISO)
                .appendQueryParameter("access_token", TOKEN)
                .build().toString();
        //Toast.makeText(getApplicationContext(),analyticsurl,Toast.LENGTH_SHORT).show();
        //+"?metric=Clozerr+Home+Screen&dimensions%5Bdevice%5D=Android+API+"+ Build.VERSION.SDK_INT+"&dimensions%5Bid%5D=,jau65asas76&time="+nowAsISO+"&access_token="+TOKEN;
        new AsyncGet(Home.this, analyticsurl, new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                Log.e(analyticsurl,"");
                Constants.URLBuilders.ANALYTICS.clearQuery();
                //Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
                //Toast.makeText(getApplicationContext(),analyticsurl,Toast.LENGTH_SHORT).show();
            }
        },false);
        TextView username = (TextView)findViewById(R.id.nav_text);
        if(USERNAME.length()!=0)
            username.setText(USERNAME);

        Log.e("pic", USER_PIC_URL);

        new DownloadImageTask((de.hdodenhof.circleimageview.CircleImageView)findViewById(R.id.nav_image))
                .execute(USER_PIC_URL);

        //slidingMyCards();

        GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);
        final String regId = GCMRegistrar.getRegistrationId(this);
        if (regId.equals("")) {
            GCMRegistrar.register(this, SENDER_ID);
        }else{
            SharedPreferences status = getSharedPreferences("USER", 0);
            TOKEN = status.getString("token", "");
            new AsyncGet(Home.this, "http://api.clozerr.com/auth/update/gcm?gcm_id=" + regId + "&access_token=" + TOKEN, new AsyncGet.AsyncResult() {
                @Override
                public void gotResult(String s) {
                    Log.e("gcm_update_result",s);
                }
            });
        }
        pager=(ViewPager)findViewById(R.id.pager);
        pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager(),Home.this));
        mtabs=(SlidingTabLayout)findViewById(R.id.tabs);
        mtabs.setDistributeEvenly(true);
        mtabs.setCustomTabView(R.layout.custom_tab_view_home, R.id.tabtitle);
        mtabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.colorAccent);
            }
        });
        mtabs.setViewPager(pager);


    }

    @Override
    protected void onResume() {
        //stopService(new Intent(this, LocationService.class));
        Log.d("HOME","start");
        super.onResume();
        //PeriodicBFS.checkAndStartScan(getApplicationContext());
    }

//    private void locationEnabledCheck() {
//        boolean gps_enabled=false ,network_enabled=false;
//        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//        try{
//            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        }catch(Exception ex){ex.printStackTrace();}
//        try{
//            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//        }catch(Exception ex){ex.printStackTrace();}
//        //LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//        if(!gps_enabled && !network_enabled) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setTitle("Location not enabled");  // GPS not found
//            builder.setCancelable(false);
//            builder.setMessage("Please enable location services to continue"); // Want to enable?
//            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//                }
//            });
//            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    finish();
//                    //startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//                }
//            });
//            builder.create().show();
//        }
//    }
    public String resetImageSize( String url ){
        return url.split("\\?")[0] + "?sz=200";
    }
    public int logincheck(){
        SharedPreferences status = getSharedPreferences("USER", 0);
        TOKEN = status.getString("token", "");
        if (status.contains("fb_id"))
        {
            USERID = status.getString("fb_id", "");
            USERNAME = status.getString("fb_name", "");
            USER_PIC_URL = "https://graph.facebook.com/" + USERID + "/picture?type=large&width=200&height=200";
            Login.googleOrFb = 1;
        }
        else if (status.contains("gplus_id"))
        {
            USERID = status.getString("gplus_id", "");
            USERNAME = status.getString("gplus_name", "");
            USER_PIC_URL = status.getString("gplus_pic", "");
            USER_PIC_URL = resetImageSize( USER_PIC_URL );
            Login.googleOrFb = 2;
        }
        Log.i("all saved prefs", status.getAll().toString());
        String loginskipped = status.getString("loginskip", "false");
        if(!loginskipped.equals("true")){
            startActivity(new Intent(this, Login.class));
            finish();
            return 0;
        }
        return 1;
    }
      private void slidingMyCards() {
          SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.sliding_drawer);
          Log.e("app", "in slidingmycards");


    }
    private void nitView() {
       // Toast.makeText(this, "in nitview", Toast.LENGTH_SHORT).show();
        leftDrawerList = (ListView) findViewById(R.id.nav_listView);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        //navigationDrawerAdapter=new ArrayAdapter<String>( Home.this, android.R.layout.simple_list_item_1, leftSliderData);
       // navigationDrawerAdapter=new ArrayAdapter<String>( Home.this, R.layout.navdrawlist,R.id.textView, leftSliderData);
       // Log.i("omy",leftSliderData[0]);
        List<String> l = Arrays.asList(leftSliderData);

// if List<String> isnt specific enough:
        ArrayList<String> al = new ArrayList<>(l);
       // ArrayList<String> arr ;
       // arr= (ArrayList<String>) Arrays.asList(leftSliderData);
        nav=new NavDrawAdapter(this,al);
        leftDrawerList.setAdapter(nav);
        leftDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Toast.makeText(Home.this, i+"", Toast.LENGTH_SHORT).show();

                if(i==leftSliderData.length-1){ // this is basically the logout button
                    AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
                    builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();

                }
                //"ABOUT US","FAQ'S","LIKE/FOLLOW CLOZERR","RATE CLOZERR", "LOGOUT"
                if(i==1){
                    startActivity(new Intent(Home.this,FAQ.class));
                }
                else if(i==0){
                    startActivity(new Intent(Home.this,AboutUs.class));
                }

                else if(i==5){
                    startActivity(new Intent(Home.this,SettingsActivity.class));
                }

                else if(i==4) {
                    Intent textShareIntent = new Intent(Intent.ACTION_SEND);
                    textShareIntent.putExtra(Intent.EXTRA_TEXT, "Try out Clozerr, an app which lets you try out new restaurants near you and rewards you for your loyalty. https://play.google.com/store/apps/details?id=com.clozerr.app ");
                    textShareIntent.setType("text/plain");
                    startActivity(Intent.createChooser(textShareIntent, "Share with..."));
                }


                else if(i==2) {
                    Uri uri = null;
                    if (Login.googleOrFb == 1)
                    {
                        uri = Uri.parse("https://www.facebook.com/clozerrdeals");
                    }
                    else if (Login.googleOrFb == 2)
                    {
                        uri = Uri.parse("https://plus.google.com/112342093373744098489/about");
                    }
                    if (uri == null) Log.e("navdraw", "null" + Login.googleOrFb);
                    Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                    /*// Create and start the chooser
                    Intent chooser = Intent.createChooser(intent, "Open with");*/
                    startActivity(intent/*chooser*/);
                }
                else if(i==3) {
                    final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                }
                }

        });
        VendorActivity.i=0;
        button=(Button)findViewById(R.id.button);
       // Toast.makeText(this, "end nitview", Toast.LENGTH_SHORT).show();
    }
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    Toast.makeText(Home.this, "Logging out", Toast.LENGTH_SHORT).show();
                    SharedPreferences example = getSharedPreferences("USER", 0);
                    SharedPreferences.Editor editor = example.edit();
                    editor.clear();
                    editor.apply();
                    USER_PIC_URL = USERNAME = USERID = TOKEN = "";
                    if (Login.googleOrFb == 2 && Login.mGoogleApiClient != null)
                    {
                        if (Login.mGoogleApiClient.isConnected()) {
                            Plus.AccountApi.clearDefaultAccount(Login.mGoogleApiClient);
                            Login.mGoogleApiClient.disconnect();
                        }
                    }
                    else if (Login.googleOrFb == 1)
                    {
                        Session session = Session.getActiveSession();
                        if (session != null) {
                            if (!session.isClosed()) {
                                session.closeAndClearTokenInformation();
                            }
                        } else {
                            session = new Session(Home.this);
                            Session.setActiveSession(session);
                            session.closeAndClearTokenInformation();
                        }
                    }
                    //BeaconFinderService.disallowScanning(Home.this);
                    startActivity(new Intent(Home.this, Login.class));
                    finish();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    //Toast.makeText(Home.this, "Thanks for staying. You are awesome.", Toast.LENGTH_SHORT).show();
                    //No button clicked
                    break;
            }
        }
    };

    private void initDrawer() {

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                int draw= rest1;
                Random rand=new Random();
                int test=rand.nextInt(7)+1;
                if(test==1) draw = rest1;
                else if(test==2)draw=rest2;
                else if(test==3)draw=R.drawable.rest3;
                else if(test==4)draw=rest4;
                else if(test==5)draw=R.drawable.rest5;
                else if(test==6)draw=rest6;
                else if(test==7)draw=rest7;
                LinearLayout user=(LinearLayout)findViewById(R.id.user);
                LightingColorFilter lcf = new LightingColorFilter( 0x888888, 0x000000);
                //imageView.setColorFilter(lcf);
                user.setBackground(getResources().getDrawable(draw));
                user.getBackground().setColorFilter(lcf);
                super.onDrawerClosed(drawerView);

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                //new code
                super.onDrawerOpened( drawerView );


            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
    }

    @Override
    protected void onPause(){

       /* try{
            GCMRegistrar.onDestroy(this);
            Log.i("onDestroy", "Peacefull");
        }catch(Exception e){
            Log.i("GCM Error", "device not registered yet");
        }*/
        Log.d("HOME","destroy");
        //startService(new Intent(this, LocationService.class));
        super.onPause();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my, menu);
        Drawable myIcon = getResources().getDrawable( R.drawable.giftbox );
        ColorFilter filter = new LightingColorFilter( Color.WHITE, Color.WHITE );
        myIcon.setColorFilter(filter);
        menu.findItem(R.id.search).setIcon(myIcon);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.search) {
            Intent giftboxintent = new Intent(this,GiftBoxActivity.class);
            startActivity(giftboxintent);
        }

        return super.onOptionsItemSelected(item);
    }


    public void offerdialog()
    {
        freebielayout.getForeground().setAlpha(255);
        LayoutInflater li = LayoutInflater.from(c);
        View ltdofferView = li.inflate(R.layout.popupofferlayout, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                c);
        final Button okbutton = (Button) ltdofferView.findViewById(R.id.okbutton);
        final ImageButton cancelbutton= (ImageButton) ltdofferView.findViewById(R.id.cancelbutton);

        alertDialogBuilder.setView(ltdofferView);
        alertDialogBuilder
                .setCancelable(true);
        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(alertDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity= Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL;
        //alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        alertDialog.getWindow().setAttributes(lp);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        okbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                freebielayout.getForeground().setAlpha(0);
            }
        });
        cancelbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                freebielayout.getForeground().setAlpha(0);
            }
        });
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                freebielayout.getForeground().setAlpha(0);
            }
        });
        //alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        // show it
        //alertDialog.show();
    }

    public void prompt(View v)
    { // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(c);
        View promptsView = li.inflate(R.layout.prompts, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                c);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText name = (EditText) promptsView.findViewById(R.id.editText1);
        final EditText remark = (EditText) promptsView.findViewById(R.id.text2);


        //final String s2=remark.getText().toString();

        // set dialog message
        alertDialogBuilder
                .setCancelable(true)
                /*.setNeutralButton("Submit",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                Toast.makeText(c,"Submitted Successfully.Thank you.", Toast.LENGTH_SHORT);
                                new AsyncGet(c, "http://api.clozerr.com/vendor/request?access_token=" + TOKEN + "&name=" + s1 +"&remarks=" + s2, new AsyncGet.AsyncResult() {
                                    @Override
                                    public void gotResult(String s) {
                                        // t1.setText(s);
                                        Log.i("result", s);

                                        //RecyclerViewAdapter adapter = new RecyclerViewAdapter(convertRow(s), Home.this);
                                        //mRecyclerView.setAdapter(adapter);

                                        //l1.setAdapter(adapter);
                                    }
                                });

                            }})
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        })*/;




        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(alertDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        //alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        alertDialog.getWindow().setAttributes(lp);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        // show it
        //alertDialog.show();
        Button submitButton = (Button) promptsView.findViewById(R.id.submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String s1=name.getText().toString();
                final String s2=remark.getText().toString();

                if (s1.equals("")) {
                    Toast.makeText(Home.this, "Please enter a restaurant name.", Toast.LENGTH_LONG).show();
                    name.setBackgroundColor(Color.RED);
                }
                else
                try {
                    new AsyncGet(c, "http://api.clozerr.com/vendor/request?access_token=" + TOKEN + "&name=" + URLEncoder.encode(s1, "UTF-8") + "&remarks=" + URLEncoder.encode(s2, "UTF-8"), new AsyncGet.AsyncResult() {
                        @Override
                        public void gotResult(String s) {
                            // t1.setText(s);
                            Log.i("result", s);
                            Toast.makeText(c, "Thank you. The restaurant will be added soon.", Toast.LENGTH_LONG).show();
                            //RecyclerViewAdapter adapter = new RecyclerViewAdapter(convertRow(s), Home.this);
                            //mRecyclerView.setAdapter(adapter);

                            //l1.setAdapter(adapter);
                            if (s == null) {
                                Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    alertDialog.dismiss();
                }catch( Exception e ){
                    e.printStackTrace();
                }
            }
        });
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onBackPressed() {
        //boolean superBackPressed = false;
        SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.sliding_drawer);
        LinearLayout drawerContentLayout = (LinearLayout) findViewById(R.id.drawerContentLayout);
        if(drawer != null && drawer.isOpened()) {
            drawer.animateClose();
        }
        /*else if(drawerLayout.isDrawerOpen(Gravity.LEFT | Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.LEFT | Gravity.START);
        }*/
        else if (drawerContentLayout != null && drawerLayout.isDrawerOpen(drawerContentLayout))
            drawerLayout.closeDrawer(drawerContentLayout);
        else{
            super.onBackPressed();
            //Button cancel=(Button)findViewById(R.id.cancel);

        }
    }


    @Override
    protected void onStart(){
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }
    @Override
    protected void onStop(){
        AsyncGet.dismissDialog();
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }
}
