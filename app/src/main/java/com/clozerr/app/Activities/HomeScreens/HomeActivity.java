package com.clozerr.app.Activities.HomeScreens;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
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
import com.clozerr.app.Utils.Constants;
import com.clozerr.app.DownloadImageTask;
import com.clozerr.app.FAQ;
import com.clozerr.app.GenUtils;
import com.clozerr.app.GeofenceManagerService;
import com.clozerr.app.GiftBoxActivity;
import com.clozerr.app.Handlers.TokenHandler;
import com.clozerr.app.MainApplication;
import com.clozerr.app.Models.UserMain;
import com.clozerr.app.MyOffer;
import com.clozerr.app.NavDrawAdapter;
import com.clozerr.app.PinnedOffersActivity;
import com.clozerr.app.R;
import com.clozerr.app.SettingsActivity;
import com.clozerr.app.SlidingTabLayout;
import com.clozerr.app.Storage.Data;
import com.clozerr.app.Utils.Router;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.plus.Plus;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.clozerr.app.R.drawable.rest1;
import static com.clozerr.app.R.drawable.rest2;
import static com.clozerr.app.R.drawable.rest4;
import static com.clozerr.app.R.drawable.rest6;
import static com.clozerr.app.R.drawable.rest7;

public class HomeActivity extends ActionBarActivity {

    private static final String TAG = "Home";

    public static final String SENDER_ID = "496568600186";  // project id from Google Console
    public static String USERNAME = "";
    public static String USERID = "";
    public static String USER_PIC_URL = "";
    public static Context c;
    Button button;
    public static double lat;
    public static double longi;

    private Toolbar toolbar;
    private ViewPager pager;
    private SlidingTabLayout mtabs;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ListView leftDrawerList;
    //private ArrayAdapter<String> navigationDrawerAdapter;
    private NavDrawAdapter nav;
    private String[] leftSliderData = {"About us","FAQ's","Like/Follow Clozerr","Rate Clozerr", "Tell Friends about Clozerr", "My Pinned Offers", "Settings", "Log out"};
    private FrameLayout freebielayout;
    private String giftboxstring;
    //private boolean nav_drawer_open = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (GenUtils.isFirstRun(this)) {
            onFirstRun();
            GenUtils.updateFirstRun(this);
        }
        GeofenceManagerService.checkAndStartService(this);

        TokenHandler tokenHandler = MainApplication.getInstance().tokenHandler;
        if(!tokenHandler.isLoggedIn()&&!tokenHandler.hasSkippedLogin()){
            //first time user
            startActivityForResult(new Intent(this, SignupActivity.class),11000);
        }

//        if (logincheck()==0)
//            return;

           // Enable if strict location is required
        c = HomeActivity.this;
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
        ImageView giftbox = (ImageView)toolbar.findViewById(R.id.giftbox);
        giftbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent giftboxintent = new Intent(HomeActivity.this, GiftBoxActivity.class);
                giftboxintent.putExtra("giftboxstring",giftboxstring);
                startActivity(giftboxintent);
            }
        });

        String TOKEN = MainApplication.getInstance().tokenHandler.clozerrtoken;
//        String TOKEN = getSharedPreferences("USER", 0).getString("token", "");
        new AsyncGet(this, "http://api.clozerr.com/v2/vendor/offers/rewardspage?access_token=" +TOKEN, new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                try {
                    giftboxstring=s;
                    JSONArray rewards = new JSONArray(s);
                    ArrayList<MyOffer> rowItems = new ArrayList<>();
                    TextView notification=(TextView)toolbar.findViewById(R.id.notifcount);
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
        TextView username = (TextView)findViewById(R.id.nav_text);
        if(USERNAME.length()!=0)
            username.setText(USERNAME);

        Log.e("pic", USER_PIC_URL);

        new DownloadImageTask((de.hdodenhof.circleimageview.CircleImageView)findViewById(R.id.nav_image))
                .execute(USER_PIC_URL);

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
        pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager(),HomeActivity.this));
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
    private void nitView() {
       // Toast.makeText(this, "in nitview", Toast.LENGTH_SHORT).show();
        leftDrawerList = (ListView) findViewById(R.id.nav_listView);
        toolbar = (Toolbar) findViewById(R.id.toolbar_home);
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                    builder.setTitle("Confirm log out")
                            .setMessage("If you choose to log out, you will no longer receive Clozerr's notifications about your rewards in nearby places " +
                                    "until your next log in.\nDo you still want to log out?")
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();

                }
                //"ABOUT US","FAQ'S","LIKE/FOLLOW CLOZERR","RATE CLOZERR", "LOGOUT"
                if(i==1){
                    startActivity(new Intent(HomeActivity.this,FAQ.class));
                }
                else if(i==0){
                    startActivity(new Intent(HomeActivity.this,AboutUs.class));
                }

                else if(i==5){
                    startActivity(new Intent(HomeActivity.this,PinnedOffersActivity.class));
                }

                else if(i==6){
                    startActivity(new Intent(HomeActivity.this,SettingsActivity.class));
                }

                else if(i==4) {
                    Intent textShareIntent = new Intent(Intent.ACTION_SEND);
                    textShareIntent.putExtra(Intent.EXTRA_TEXT, "Try out Clozerr, an app which lets you try out new restaurants near you and rewards you for your loyalty. https://play.google.com/store/apps/details?id=com.clozerr.app&referrer=utm_source%3Dclozerr%26utm_medium%3Dappshare%26utm_term%3Dshared%252Bthrough%252Bclozerr");
                    textShareIntent.setType("text/plain");
                    startActivity(Intent.createChooser(textShareIntent, "Share with..."));
                }


                else if(i==2) {
                    Uri uri = null;
                    if (LoginActivity.googleOrFb == 1)
                    {
                        uri = Uri.parse("https://www.facebook.com/clozerrdeals");
                    }
                    else if (LoginActivity.googleOrFb == 2)
                    {
                        uri = Uri.parse("https://plus.google.com/112342093373744098489/about");
                    }
                    if (uri == null) Log.e("navdraw", "null" + LoginActivity.googleOrFb);
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
                    Toast.makeText(HomeActivity.this, "Logging out", Toast.LENGTH_SHORT).show();
                    SharedPreferences example = getSharedPreferences("USER", 0);
                    SharedPreferences.Editor editor = example.edit();
                    editor.clear();
                    editor.apply();
                    String TOKEN = MainApplication.getInstance().tokenHandler.clozerrtoken;
                    USER_PIC_URL = USERNAME = USERID = TOKEN = "";
                    if (LoginActivity.googleOrFb == 2 && LoginActivity.googleApiClient != null)
                    {
                        if (LoginActivity.googleApiClient.isConnected()) {
                            Plus.AccountApi.clearDefaultAccount(LoginActivity.googleApiClient);
                            LoginActivity.googleApiClient.disconnect();
                        }
                    }
                    else if (LoginActivity.googleOrFb == 1)
                    {
//                        Session session = Session.getActiveSession();
//                        if (session != null) {
//                            if (!session.isClosed()) {
//                                session.closeAndClearTokenInformation();
//                            }
//                        } else {
//                            session = new Session(Home.this);
//                            Session.setActiveSession(session);
//                            session.closeAndClearTokenInformation();
//                        }
                    }
                    //BeaconFinderService.disallowScanning(Home.this);
                    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
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

        new AsyncGet(c, Router.Homescreen.offerdialog(), new AsyncGet.AsyncResult() {
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
                        LayoutInflater li = LayoutInflater.from(c);
                        View ltdofferView = li.inflate(R.layout.popupofferlayout, null);

                        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                c);
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
                    Toast.makeText(HomeActivity.this, "Please enter a restaurant name.", Toast.LENGTH_LONG).show();
                    name.setBackgroundColor(Color.RED);
                }
                else
                try {
                    String TOKEN = MainApplication.getInstance().tokenHandler.clozerrtoken;
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
