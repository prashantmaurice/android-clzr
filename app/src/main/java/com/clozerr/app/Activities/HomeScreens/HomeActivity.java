package com.clozerr.app.Activities.HomeScreens;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.internal.view.ContextThemeWrapper;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.clozerr.app.AboutUs;
import com.clozerr.app.Activities.LoginScreens.LoginActivity;
import com.clozerr.app.Activities.LoginScreens.SignupActivity;
import com.clozerr.app.Activities.VendorScreens.VendorActivity;
import com.clozerr.app.AsyncGet;
import com.clozerr.app.BeaconDBDownloadBaseReceiver;
import com.clozerr.app.DownloadImageTask;
import com.clozerr.app.FAQ;
import com.clozerr.app.GenUtils;
import com.clozerr.app.GeofenceManagerService;
import com.clozerr.app.GiftBoxActivity;
import com.clozerr.app.Handlers.TokenHandler;
import com.clozerr.app.MainApplication;
import com.clozerr.app.Models.NavObject;
import com.clozerr.app.Models.UserMain;
import com.clozerr.app.Models.UserMainLive;
import com.clozerr.app.NavDrawAdapter;
import com.clozerr.app.PinnedOffersActivity;
import com.clozerr.app.R;
import com.clozerr.app.SettingsActivity;
import com.clozerr.app.SlidingTabLayout;
import com.clozerr.app.Storage.Data;
import com.clozerr.app.Utils.Constants;
import com.clozerr.app.Utils.Logg;
import com.clozerr.app.Utils.Router;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Random;

import static com.clozerr.app.R.drawable.rest1;
import static com.clozerr.app.R.drawable.rest2;
import static com.clozerr.app.R.drawable.rest4;
import static com.clozerr.app.R.drawable.rest6;
import static com.clozerr.app.R.drawable.rest7;

public class HomeActivity extends ActionBarActivity {

    private static final String TAG = "HomeActivity";

    public String USERNAME = "";
    public String USERID = "";
    public String USER_PIC_URL = "";
    public Context mContext;
    Button button;

    private Toolbar toolbar;
    private ViewPager pager;
    private SlidingTabLayout mtabs;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ListView leftDrawerList;
    private NavDrawAdapter navAdapter;
    private FrameLayout freebielayout;
    private String giftBoxJsonString;
    UserMainLive userMainLive;
    ImageView giftbox;
    TextView nav_username;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        updateUI();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        //Check for first run
        if (GenUtils.isFirstRun(this)) {
            onFirstRun();
            GenUtils.updateFirstRun(this);
        }

        //Upstart services
        GeofenceManagerService.checkAndStartService(this);


        //Check whether this user is first time user
        TokenHandler tokenHandler = MainApplication.getInstance().tokenHandler;
        Logg.d("tokenHandler",tokenHandler.clozerrtoken);
        if(!tokenHandler.isLoggedIn()&&!tokenHandler.hasSkippedLogin()){
            //first time user
            startActivityForResult(new Intent(this, SignupActivity.class),11000);
        }


        setContentView(R.layout.activity_my);
        setupUI();


        //check for location
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            buildAlertMessageNoGps();
        }



        if (toolbar != null) {
            //toolbar.setTitle(getString(R.string.app_name));

            toolbar.setTitle("");
//            ((TextView) toolbar.findViewById(R.id.appTitleView)).setTypeface(Typeface.createFromAsset(
//                    getAssets(), "fonts/comfortaa.ttf"
//            ));
            setSupportActionBar(toolbar);

        }

        giftbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent giftboxintent = new Intent(HomeActivity.this, GiftBoxActivity.class);
                giftboxintent.putExtra(GiftBoxActivity.GIFTBOXDATA, giftBoxJsonString);
                startActivity(giftboxintent);
            }
        });

        new AsyncGet(this, Router.Homescreen.getVendorsGifts(), new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                Logg.i("URL",Router.Homescreen.getVendorsGifts()+ " : "+s);
                try {
                    giftBoxJsonString = s;
                    JSONArray rewards = new JSONArray(s);
                    TextView notification = (TextView) toolbar.findViewById(R.id.notifcount);
                    notification.setText(String.valueOf(rewards.length()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },false);





        initDrawer();
        freebielayout=(FrameLayout)findViewById(R.id.homeframe);
        freebielayout.getForeground().setAlpha(0);
        final String analyticsurl= GenUtils.getDefaultAnalyticsUriBuilder(this, Constants.Metrics.HOME_SCREEN)
                                    .build().toString();
        GenUtils.putAnalytics(this, TAG, analyticsurl);


        nav_username = (TextView)findViewById(R.id.nav_text);



        nav_username.setText("Not logged in");
        updateUI();


        new DownloadImageTask((de.hdodenhof.circleimageview.CircleImageView)findViewById(R.id.nav_image)).execute(USER_PIC_URL);

        //slidingMyCards();

//        GCMRegistrar.checkDevice(this);
//        GCMRegistrar.checkManifest(this);
//        final String regId = GCMRegistrar.getRegistrationId(this);
//        if (regId.equals("")) {
//            GCMRegistrar.register(this, SENDER_ID);
//        }else{
//            SharedPreferences status = getSharedPreferences("USER", 0);
//            TOKEN = MainApplication.getInstance().data.userMain.token;
//            TOKEN = status.getString("token", "");
//            new AsyncGet(HomeActivity.this, "http://api.clozerr.com/auth/update/gcm?gcm_id=" + regId + "&access_token=" + TOKEN, new AsyncGet.AsyncResult() {
//                @Override
//                public void gotResult(String s) {
//                    Log.e("gcm_update_result",s);
//                }
//            });
//        }
        updateGCMIDinServer();


        pager=(ViewPager)findViewById(R.id.pager);
        pager.setAdapter(new HomeActivityPagerAdapter(getSupportFragmentManager(),HomeActivity.this));
        pager.setOffscreenPageLimit(0);
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
        offerdialog();


        //Load initial user data before doing anything
//        new AsyncGet(this, Router.Homescreen.getUserDataComplete(), new AsyncGet.AsyncResult() {
//            @Override
//            public void gotResult(String s) {
//                Logg.i("URL",Router.Homescreen.getUserDataComplete()+ " : "+s);
//                try {
//                    JSONObject userData = new JSONObject(s);
//                    MainApplication.getInstance().userMainLive = UserMainLive.decodeFromServer(userData);
//                    updateUI();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        },false);


    }

    private void updateUI(){
        MainApplication.getInstance().tokenHandler.print();
        if(MainApplication.getInstance().tokenHandler.isLoggedIn()){
            nav_username.setText(MainApplication.getInstance().tokenHandler.username);
            new DownloadImageTask((de.hdodenhof.circleimageview.CircleImageView)findViewById(R.id.nav_image)).execute(MainApplication.getInstance().tokenHandler.picurl);
        }
    }

    private void updateGCMIDinServer(){
        final AsyncTask asyncTask = new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
                Log.i("AsyncTask", "Inside async task, trying to generate GCM id");
                InstanceID instanceID = InstanceID.getInstance(HomeActivity.this);
                String gcmId = "";
                try {
                    gcmId = instanceID.getToken(getString(R.string.GcmProjectId),
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);


                    if(gcmId==null){
                        Log.e("ERROR","GCM gcmId returned as null");
                        return null;
                    }
                    Log.i("AsyncTask", "GCM id is " + gcmId);

                    Data data = MainApplication.getInstance().data;
                    data.userMain.gcmId = gcmId;
                    data.userMain.saveUserDataLocally();
                    new AsyncGet(HomeActivity.this, Router.User.gcmIdUpdate(gcmId), new AsyncGet.AsyncResult() {
                        @Override
                        public void gotResult(String s) {
                            Log.e("gcm_update_result",s);
                        }
                    });

                } catch (IOException e) {
                    Log.e("AsyncTask", "Failed to generate GCM id");
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
            }
        };
        asyncTask.execute(this);
    }

    private void onFirstRun() {
        checkInStoreInstall();
        BeaconDBDownloadBaseReceiver.scheduleDownload(this);
    }

    private void checkInStoreInstall() {
        final String downloadUUIDUrl = GenUtils.getClearedUriBuilder(Constants.URLBuilders.BEACON_DOWNLOAD)
                .build().toString();
        final Context applicationContext = getApplicationContext();
        Ion.with(applicationContext).load(downloadUUIDUrl).asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            e.printStackTrace();
                        } else {
                            PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()
                                    .putString(Constants.SPKeys.BEACON_UUID, result.get("UUID").getAsString())
                                    .commit();
                            //OneTimeBFS.startOneTimeService(applicationContext, InStoreInstallBFS.class,
                            //        null, false, Constants.Timeouts.IN_STORE_INSTALL_DETECTION);
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        //stopService(new Intent(this, LocationService.class));
        Log.d("HOME","onResume");
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
//        SharedPreferences status = getSharedPreferences("USER", 0);
        String TOKEN = MainApplication.getInstance().tokenHandler.clozerrtoken;
//        TOKEN = status.getString("token", "");
//        if (status.contains("fb_id"))
        if (!MainApplication.getInstance().data.userMain.facebookId.isEmpty())
        {
            USERID = MainApplication.getInstance().data.userMain.facebookId;
            USERNAME = MainApplication.getInstance().data.userMain.fb_name;
            USER_PIC_URL = "https://graph.facebook.com/" + USERID + "/picture?type=large&width=200&height=200";
            LoginActivity.googleOrFb = 1;
        }
        else if (!MainApplication.getInstance().data.userMain.gplus_id.isEmpty())
        {
            USERID = MainApplication.getInstance().data.userMain.gplus_id;
            USERNAME = MainApplication.getInstance().data.userMain.gplus_name;
            USER_PIC_URL = MainApplication.getInstance().data.userMain.gplus_pic;
            USER_PIC_URL = resetImageSize( USER_PIC_URL );
            LoginActivity.googleOrFb = 2;
        }
//        Log.i("all saved prefs", use.getAll().toString());
        UserMain userMain = MainApplication.getInstance().data.userMain;
        if(!userMain.loginSkip){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return 0;
        }
        return 1;
    }
      private void slidingMyCards() {
          SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.sliding_drawer);
          Log.e("app", "in slidingmycards");


    }
    private void setupUI() {
        leftDrawerList = (ListView) findViewById(R.id.nav_listView);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        button = (Button) findViewById(R.id.button);
        toolbar = (Toolbar) findViewById(R.id.toolbar_home);
        giftbox = (ImageView) toolbar.findViewById(R.id.giftbox);

        navAdapter = new NavDrawAdapter(this,Constants.getNavList());
        leftDrawerList.setAdapter(navAdapter);
        leftDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                NavObject clickedObject = Constants.getNavList().get(i);

                switch(clickedObject.listId){
                    case ABOUTUS:
                        startActivityForResult(new Intent(HomeActivity.this, AboutUs.class), 0);
                        break;

                    case FAQ:
                        startActivity(new Intent(HomeActivity.this,FAQ.class));
                        break;

                    case PINNED_OFFERS:
                        startActivity(new Intent(HomeActivity.this,PinnedOffersActivity.class));
                        break;

                    case SETTINGS:
                        startActivity(new Intent(HomeActivity.this,SettingsActivity.class));
                        break;

                    case LIKE_CLOZERR:
                        Uri uri;
                        if(MainApplication.getInstance().tokenHandler.loggedByFb()){
                            uri = Uri.parse("https://www.facebook.com/clozerrdeals");
                        }else if(MainApplication.getInstance().tokenHandler.loggedByGoogle()){
                            uri = Uri.parse("https://plus.google.com/112342093373744098489/about");
                        }else{
                            uri = Uri.parse("https://www.facebook.com/clozerrdeals");
                        }
                        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                        startActivity(intent);
                        break;

                    case RATE_CLOZERR:
                        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                        break;

                    case TELL_FRIEND:
                        Intent textShareIntent = new Intent(Intent.ACTION_SEND);
                        textShareIntent.putExtra(Intent.EXTRA_TEXT, "Try out Clozerr, an app which lets you try out new restaurants near you and rewards you for your loyalty. https://play.google.com/store/apps/details?id=com.clozerr.app&referrer=utm_source%3Dclozerr%26utm_medium%3Dappshare%26utm_term%3Dshared%252Bthrough%252Bclozerr");
                        textShareIntent.setType("text/plain");
                        startActivity(Intent.createChooser(textShareIntent, "Share with..."));
                        break;

                    case LOGOUT:
                        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                        builder.setTitle("Confirm log out")
                                .setMessage("If you choose to log out, you will no longer receive Clozerr's notifications about your rewards in nearby places " +
                                        "until your next log in.\nDo you still want to log out?")
                                .setPositiveButton("Yes", dialogLogoutClickListener)
                                .setNegativeButton("No", dialogLogoutClickListener).show();
                        break;
                }
            }

        });
        VendorActivity.i=0;

       // Toast.makeText(this, "end nitview", Toast.LENGTH_SHORT).show();
    }
    DialogInterface.OnClickListener dialogLogoutClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    Toast.makeText(HomeActivity.this, "Logging out", Toast.LENGTH_SHORT).show();
                    MainApplication.getInstance().tokenHandler.logout(HomeActivity.this);

                    USER_PIC_URL = USERNAME = USERID  = "";//Todo : remove this
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    //Toast.makeText(Home.this, "Thanks for staying. You are awesome.", Toast.LENGTH_SHORT).show();
                    //No button clicked
                    break;
            }
        }
    };

    private void initDrawer() {

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onPause(){

       /* try{
            GCMRegistrar.onDestroy(this);
            Log.i("onDestroy", "Peacefull");
        }catch(Exception e){
            Log.i("GCM Error", "device not registered yet");
        }*/
        Log.d("HOME", "onPause");
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
//        Drawable myIcon = getResources().getDrawable( R.drawable.giftbox );
//        ColorFilter filter = new LightingColorFilter( Color.WHITE, Color.WHITE );
//        myIcon.setColorFilter(filter);
//        menu.findItem(R.id.search).setIcon(myIcon);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.search) {
//            Intent giftboxintent = new Intent(this,GiftBoxActivity.class);
//            startActivity(giftboxintent);
//        }
        if(id == android.R.id.home) {
                drawerLayout.openDrawer(GravityCompat.START);  // OPEN DRAWER
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void offerdialog(){

        new AsyncGet(mContext, Router.Homescreen.offerdialog(), new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                Log.i("result", s);
                try {
                    JSONArray notifarray=new JSONArray(s);
                    for(int i=0;i<notifarray.length();i++)
                    {
                        JSONObject notifobject = notifarray.getJSONObject(i);
                        //Toast.makeText(getApplicationContext(),notifobject.toString(),Toast.LENGTH_LONG).show();
                        freebielayout.getForeground().setAlpha(255);
                        LayoutInflater li = LayoutInflater.from(mContext);
                        View ltdofferView = li.inflate(R.layout.popupofferlayout, null);

                        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                        final Button okbutton = (Button) ltdofferView.findViewById(R.id.okbutton);
                        final ImageButton cancelbutton= (ImageButton) ltdofferView.findViewById(R.id.cancelbutton);
                        final TextView caption = (TextView) ltdofferView.findViewById(R.id.offercaption);
                        final TextView description = (TextView) ltdofferView.findViewById(R.id.offerdescription);
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
                        caption.setText(notifobject.getJSONObject("data").getString("title"));
                        description.setText(notifobject.getJSONObject("data").getString("description"));
                    }
                    if (s == null) {
                        Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        },false);


        //alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        // show it
        //alertDialog.show();
    }

    public void prompt(View v)
    { // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(mContext);
        View promptsView = li.inflate(R.layout.prompts, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);

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
                    Toast.makeText(HomeActivity.this, "Please enter a restaurant name.", Toast.LENGTH_LONG).show();
                    name.setBackgroundColor(Color.RED);
                }
                else
                try {
                    String TOKEN = MainApplication.getInstance().tokenHandler.clozerrtoken;
                    new AsyncGet(mContext, "http://api.clozerr.com/vendor/request?access_token=" + TOKEN + "&name=" + URLEncoder.encode(s1, "UTF-8") + "&remarks=" + URLEncoder.encode(s2, "UTF-8"), new AsyncGet.AsyncResult() {
                        @Override
                        public void gotResult(String s) {
                            // t1.setText(s);
                            Log.i("result", s);
                            Toast.makeText(mContext, "Thank you. The restaurant will be added soon.", Toast.LENGTH_LONG).show();
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
        Log.d("HOME", "onStart");
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }
    @Override
    protected void onStop(){
        AsyncGet.dismissDialog();
        Log.d("HOME", "onStop");
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    private void buildAlertMessageNoGps() {
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(new ContextThemeWrapper(HomeActivity.this, R.style.profileDialog));
        builder.setMessage("Your location seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final android.support.v7.app.AlertDialog alert = builder.create();
        alert.show();
    }
}
