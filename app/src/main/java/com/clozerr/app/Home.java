package com.clozerr.app;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.plus.Plus;

import org.json.JSONArray;
import org.json.JSONObject;

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

public class Home  extends ActionBarActivity {

    static final String SENDER_ID = "496568600186";  // project id from Google Console
    public static String TOKEN = "";
    private static String USERNAME = "";
    private static String USERID = "";
    private static String USER_PIC_URL = "";
    public static Context c;
    Button button;
    static double lat;
    static double longi;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ListView leftDrawerList;
    //private ArrayAdapter<String> navigationDrawerAdapter;
    private NavDrawAdapter nav;
    private String[] leftSliderData = {"ABOUT US","FAQ'S","LIKE/FOLLOW CLOZERR","RATE CLOZERR", "LOGOUT"};
    //private boolean nav_drawer_open = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("create", "Creation of Home");

        GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);
        final String regId = GCMRegistrar.getRegistrationId(this);
        if (regId.equals("")) {
            GCMRegistrar.register(this, SENDER_ID);
        }else{
            new AsyncGet(Home.this, "http://api.clozerr.com/auth/update/gcm?gcm_id=" + regId, new AsyncGet.AsyncResult() {
                @Override
                public void gotResult(String s) {
                    Log.e("gcm_update_result",s);
                }
            });
        }

        if (logincheck()==0)
            return;

        locationEnabledCheck();   // Enable if strict location is required
        c = Home.this;
        setContentView(R.layout.activity_my);
        nitView();
        if (toolbar != null) {
            //toolbar.setTitle(getString(R.string.app_name));

            toolbar.setTitle("");
            setSupportActionBar(toolbar);

        }
        initDrawer();
        /*Toast.makeText(this, "inited drawer", Toast.LENGTH_SHORT).show();*/
        Log.d("app", "inited drawer");
        final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        Log.d("app", "inited recycler");

        TextView username = (TextView)findViewById(R.id.nav_text);
        if(USERNAME.length()!=0)
            username.setText(USERNAME);

        Log.e("pic", USER_PIC_URL);

        new DownloadImageTask((de.hdodenhof.circleimageview.CircleImageView)findViewById(R.id.nav_image))
                .execute(USER_PIC_URL);

        Log.d("app", "dld img");
        //startService(new Intent(this, LocationService.class));
        lat = 13;
        longi = 80.2;


        SharedPreferences status = getSharedPreferences("USER", 0);
        String cards = status.getString("home_cards", "");
        Log.d("app", "got home cards");
        if(!cards.equals("")){
            Log.e("Cached Card", cards);
            RecyclerViewAdapter adapter = new RecyclerViewAdapter(convertRow(cards), Home.this);
            mRecyclerView.setAdapter(adapter);

        }else {

            String url = "http://api.clozerr.com/vendor/get/near?latitude=" + lat + "&longitude=" + longi + "&access_token=" + TOKEN;
            Log.e("url", url);
            new AsyncGet(Home.this, url, new AsyncGet.AsyncResult() {
                @Override
                public void gotResult(String s) {
                    Log.e("result",s);
                    if(s==null) {
                        Toast.makeText(getApplicationContext(),"No internet connection",Toast.LENGTH_SHORT).show();
                    }

                    List<CardModel> CardList = convertRow(s);
                    if (CardList.size() != 0) {
                        Log.e("app", "setting adapter");
                        RecyclerViewAdapter adapter = new RecyclerViewAdapter(convertRow(s), Home.this);
                        Log.e("app", "adapter made");
                        mRecyclerView.setAdapter(adapter);
                        Log.e("app", "adapter set");
                        final SharedPreferences.Editor editor = getSharedPreferences("USER", 0).edit();
                        editor.putString("home_cards", s);
                        editor.apply();
                        Log.e("app", "editing done");
                    }
                    else Log.d("app", "no cards to show");
                }
            });
        }


        new MyLocation().getLocation(getApplicationContext(), new MyLocation.LocationResult(){
            @Override
            public void gotLocation (Location location) {
                Log.e("location stuff","Location Callback called.");
                try{
                    lat=location.getLatitude();
                    longi=location.getLongitude();
                    Log.e("lat", lat + "");
                    Log.e("long", longi + "");
                }catch (Exception e){
                    e.printStackTrace();
                }
                SharedPreferences status = getSharedPreferences("USER", 0);
                TOKEN = status.getString("token", "");
                String url;
                if(!TOKEN.equals(""))
                    url = "http://api.clozerr.com/vendor/get/near?latitude="+lat+"&longitude="+longi+"&access_token="+TOKEN;
                else
                    url = "http://api.clozerr.com/vendor/get/near?latitude="+lat+"&longitude="+longi ;
                Log.e("url", url);

                new AsyncGet(Home.this, url, new AsyncGet.AsyncResult() {
                    @Override
                    public void gotResult(String s) {
                        List<CardModel> CardList = convertRow(s);
                        if(CardList.size()!=0){
                            RecyclerViewAdapter adapter = new RecyclerViewAdapter(convertRow(s), Home.this);
                            mRecyclerView.setAdapter(adapter);

                            final SharedPreferences.Editor editor = getSharedPreferences("USER", 0).edit();
                            editor.putString("home_cards", s);
                            editor.apply();
                        }
                        if(s==null) {
                            Toast.makeText(getApplicationContext(),"No internet connection",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        });

        if (!BeaconFinderService.hasStarted)
            startService(new Intent(this, BeaconFinderService.class));
        slidingMyCards();
    }

    private void locationEnabledCheck() {
        boolean gps_enabled=false ,network_enabled=false;
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        try{
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch(Exception ex){ex.printStackTrace();}
        try{
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch(Exception ex){ex.printStackTrace();}
        //LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if(!gps_enabled && !network_enabled) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Location not enabled");  // GPS not found
            builder.setCancelable(false);
            builder.setMessage("Please enable location services to continue"); // Want to enable?
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                    //startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
            builder.create().show();
        }
    }

    public int logincheck(){
        SharedPreferences status = getSharedPreferences("USER", 0);
        TOKEN = status.getString("token", "");
        if (status.contains("fb_id"))
        {
            USERID = status.getString("fb_id", "");
            USERNAME = status.getString("fb_name", "");
            USER_PIC_URL = "https://graph.facebook.com/" + USERID + "/picture?type=large&width=200&height=200";
        }
        else if (status.contains("gplus_id"))
        {
            USERID = status.getString("gplus_id", "");
            USERNAME = status.getString("gplus_name", "");
            USER_PIC_URL = status.getString("gplus_pic", "");
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
        final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.sliding_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(Home.this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        Log.e("app", "in slidingmycards; set recycler");
        drawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {

            @Override

            public void onDrawerOpened() {

                SharedPreferences status = getSharedPreferences("USER", 0);
                String TOKEN = status.getString("token", "");

                String urlVisited = "http://api.clozerr.com/vendor/get/visited?access_token="+TOKEN;
                Log.e("urlslide", urlVisited);

                new AsyncGet(Home.this, urlVisited , new AsyncGet.AsyncResult() {
                    @Override
                    public void gotResult(String s) {
                        //  t1.setText(s);

                        Log.e("resultSlide", s);

                        MyCardRecyclerViewAdapter Cardadapter = new MyCardRecyclerViewAdapter(convertRowMyCard(s), Home.this);
                        mRecyclerView.setAdapter(Cardadapter);

                        if(s==null) {
                            Toast.makeText(getApplicationContext(),"No internet connection",Toast.LENGTH_SHORT).show();
                        }
                        //l1.setAdapter(adapter);
                    }
                });

            }

        });

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
                    Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                    // Create and start the chooser
                    Intent chooser = Intent.createChooser(intent, "Open with");
                    startActivity(chooser);
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
        CouponPage.i=0;
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
                    if (Login.googleOrFb == 2 && Login.mGoogleApiClient.isConnected())
                    {
                        Plus.AccountApi.clearDefaultAccount(Login.mGoogleApiClient);
                        Login.mGoogleApiClient.disconnect();
                        //Login.mGoogleApiClient.connect();
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
                super.onDrawerClosed(drawerView);

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                //new code
                super.onDrawerOpened( drawerView );

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
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
       /* try{
            GCMRegistrar.onDestroy(this);
            Log.i("onDestroy", "Peacefull");
        }catch(Exception e){
            Log.i("GCM Error", "device not registered yet");
        }*/
        //stopService(new Intent(this, LocationService.class));
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
        return true;
    }



    private List<CardModel> convertRow(String s) {
        List<CardModel> rowItems = new ArrayList<>();
        JSONArray array;
        try {
            array = new JSONArray(s);
            for(int i = 0 ; i < array.length() ; i++){
                String phonenumber;
                try {
                    phonenumber = array.getJSONObject(i).getString("phonenumber");
                }
                catch(Exception e)
                {
                    phonenumber="0123456789";
                }
                String vendorDescription;
                try {
                    vendorDescription = array.getJSONObject(i).getString("description");
                }
                catch(Exception e)
                {
                    vendorDescription="No Restaurant Description Available Now";
                }
                Log.e("description", vendorDescription);
                CardModel item = new CardModel(
                        /*array.getJSONObject(i).getJSONObject("vendor").getString("image"),
                        array.getJSONObject(i).getJSONObject("vendor").getString("caption"),
                        array.getJSONObject(i).getJSONObject("coupon").getString("caption"),
                        array.getJSONObject(i).getJSONObject("vendor").getJSONObject("props").getInt("rating"),
                        array.getJSONObject(i).getJSONObject("vendor").getJSONObject("props").getInt("rating")*/
                        // name, offers, image, fid, _id
                        array.getJSONObject(i).getString("name"),
                        phonenumber, vendorDescription,
                        array.getJSONObject(i).getJSONArray("offers"),
                        array.getJSONObject(i).getJSONArray("location").getDouble(0),
                        array.getJSONObject(i).getJSONArray("location").getDouble(1),
                        array.getJSONObject(i).getString("image"),
                        array.getJSONObject(i).getString("fid"),array.getJSONObject(i).getString("_id"),0
                );
                rowItems.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowItems;
    }
    private List<CardModel> convertRowMyCard(String s) {
        List<CardModel> rowItems = new ArrayList<>();
        JSONObject temp;
        JSONArray array;
        try {
            //Log.e("stringfunction", s);
            Log.e("stringfunction", "processing..");
            temp = new JSONObject(s);
            array = temp.getJSONArray("data");

            ImageView loyaltyempty=(ImageView)findViewById(R.id.loyaltyempty);
            if(array.length()==0){
                Log.e("arrayLength", array.length()+"");
                loyaltyempty.setVisibility(View.VISIBLE);
            }
            for(int i = 0 ; i < array.length() ; i++){
                loyaltyempty.setVisibility(View.GONE);
                Log.e("stringfunction", "processing..");
                CardModel item = new CardModel(
                        array.getJSONObject(i).getString("name"),
                        array.getJSONObject(i).getString("phone"),
                        array.getJSONObject(i).getString("description"),
                        array.getJSONObject(i).getJSONArray("offers"),
                        array.getJSONObject(i).getJSONArray("location").getDouble(0),
                        array.getJSONObject(i).getJSONArray("location").getDouble(1),
                        array.getJSONObject(i).getString("image"),
                        array.getJSONObject(i).getString("fid"),
                        array.getJSONObject(i).getString("_id"),
                        array.getJSONObject(i).getInt("stamps")
                );
                Log.e("stringfunction", "processed");
                rowItems.add(item);
            }
        } catch (Exception e) {
            Log.e("uhoh",e.getMessage());
            e.printStackTrace();
        }
        return rowItems;
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
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // show it
        alertDialog.show();
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
        if(drawer.isOpened()) {
            drawer.close();
        }
        /*else if(drawerLayout.isDrawerOpen(Gravity.LEFT | Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.LEFT | Gravity.START);
        }*/
        else{
            super.onBackPressed();
            //Button cancel=(Button)findViewById(R.id.cancel);

        }


    }

}
