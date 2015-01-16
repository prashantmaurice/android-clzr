package com.clozerr.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.Session;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by Girish on 1/4/2015.
 */

public class CouponPage extends ActionBarActivity {
    static int i=1;
    static String lat="";
    static String longi="";
    private static String TOKEN = "";
    private static String USERNAME = "";
    private static String USERID = "";
    public static Context c;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ListView leftDrawerList;
    private ArrayAdapter<String> navigationDrawerAdapter;
    private String[] leftSliderData = {"Tutorials", "FAQs", "About Us","Login Page"};
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
       // if (logincheck()==0)
            //return;
        c = CouponPage.this;
        i=1;
        setContentView(R.layout.couponpkg);
        button=(Button)findViewById(R.id.button);
        /*SharedPreferences example = getSharedPreferences("USER", 0);
        SharedPreferences.Editor editor = example.edit();
        editor.clear();
        editor.commit();*/
        RecyclerView r= (RecyclerView)findViewById(R.id.list);

      /*  LinearLayout l= (LinearLayout)findViewById(R.id.layout);
        if(i==2){i=1;AlertDialog.Builder builder = new AlertDialog.Builder(c);
            builder.setMessage("You have to login to view.Want to proceed to login page?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();}
       l.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                AlertDialog.Builder builder = new AlertDialog.Builder(c);
                builder.setMessage("You have to login to view.Want to proceed to login page?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
                return true;
            }
        });

        l.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //CouponPage.i = 2;
                AlertDialog.Builder builder = new AlertDialog.Builder(c);
                builder.setMessage("You have to login to view.Want to proceed to login page?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();


            }
        });
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        // Toast.makeText(c, "Logging out", Toast.LENGTH_SHORT).show();

                        Session session = Session.getActiveSession();
                        if (session != null) {
                            if (!session.isClosed()) {
                                session.closeAndClearTokenInformation();
                            }
                        } else {
                            session = new Session(c);
                            Session.setActiveSession(session);
                            session.closeAndClearTokenInformation();
                        }

                        startActivity(new Intent(c, Login.class));
                        finish();
                       // Activity a=(Activity)c;
                       // a.finish();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //Toast.makeText(Home.this, "Thanks for staying. You are awesome.", Toast.LENGTH_SHORT).show();
                        //No button clicked
                        break;
                }
            }
        };*/

        /*LinearLayout l= (LinearLayout)findViewById(R.id.layout);
        l.setEnabled(false);
        l.*/
       /* RelativeLayout card=(RelativeLayout)findViewById(R.id.card);
        Display display = getWindowManager().getDefaultDisplay();
        Point size=new Point();
        display.getSize(size);
        int w=size.x;

        ViewGroup.LayoutParams params = card.getLayoutParams();
        params.height=w/2;*/


        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(c);
                View promptsView = li.inflate(R.layout.prompts, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        c);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                EditText name = (EditText) promptsView.findViewById(R.id.editText1);
                EditText remark = (EditText) promptsView.findViewById(R.id.text2);
                final String s1=name.getText().toString();
                final String s2=remark.getText().toString();

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setNeutralButton("Submit",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        new AsyncGet(c,"http://api.clozerr.com/vendor/request?access_token=" + TOKEN + "&name=" + s1 +"&remarks=" + s2, new AsyncGet.AsyncResult() {
                                            @Override
                                            public void gotResult(String s) {
                                                // t1.setText(s);
                                                Log.i("result", s);
                                                Toast.makeText(c,"Submitted Successfully.Thank you.", Toast.LENGTH_SHORT);
                                                //RecyclerViewAdapter adapter = new RecyclerViewAdapter(convertRow(s), Home.this);
                                                //mRecyclerView.setAdapter(adapter);

                                                //l1.setAdapter(adapter);
                                                if(s==null) {
                                                    Toast.makeText(getApplicationContext(),"No internet connection",Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                    }})
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });




                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

            }
        });
       nitView();
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.app_name));
            setSupportActionBar(toolbar);
        }
        initDrawer();

        final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);


       // TextView username = (TextView)findViewById(R.id.nav_text);
        //if(USERNAME.length()!=0)
            //username.setText(USERNAME);
        //new DownloadImageTask((ImageView)findViewById(R.id.nav_image))
               // .execute("https://graph.facebook.com/" + USERID + "/picture");

       /* new AsyncGet(this, "http://api.clozerr.com/vendor/get/near?latitude="+lat+"&longitude="+longi+"&type=sx", new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                //  t1.setText(s);
                Log.i("result", s);
                String s1="{}";
                RecyclerViewAdapter adapter = new RecyclerViewAdapter(convertRow(s), CouponPage.this);
                mRecyclerView.setAdapter(adapter);

                //l1.setAdapter(adapter);
            }
        });*/
    }
/*
    public int logincheck(){
        SharedPreferences status = getSharedPreferences("USER", 0);
        TOKEN = status.getString("token", "");
        USERID = status.getString("fb_id", "");
        USERNAME = status.getString("fb_name", "");
        Log.i("all saved prefs", status.getAll().toString());
        String loginskipped = status.getString("loginskip", "false");
        if(!loginskipped.equals("true")){
            startActivity(new Intent(this, Login.class));
            finish();
            return 0;
        }
        return 1;
    }*/

    private void nitView() {
        leftDrawerList = (ListView) findViewById(R.id.nav_listView);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigationDrawerAdapter=new ArrayAdapter<String>( CouponPage.this, android.R.layout.simple_list_item_1, leftSliderData);
        leftDrawerList.setAdapter(navigationDrawerAdapter);
        leftDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Toast.makeText(Home.this, i+"", Toast.LENGTH_SHORT).show();
               if(i==leftSliderData.length-1){ // this is basically the logout button
                   AlertDialog.Builder builder = new AlertDialog.Builder(CouponPage.this);
                    builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();

                }

        }});
    }
   DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                   /* Toast.makeText(CouponPage.this, "Logging out", Toast.LENGTH_SHORT).show();
                    SharedPreferences example = getSharedPreferences("USER", 0);
                    SharedPreferences.Editor editor = example.edit();
                    editor.clear();
                    editor.commit();*/
                    Session session = Session.getActiveSession();
                    if (session != null) {
                        if (!session.isClosed()) {
                            session.closeAndClearTokenInformation();
                        }
                    } else {
                        session = new Session(CouponPage.this);
                        Session.setActiveSession(session);
                        session.closeAndClearTokenInformation();
                    }

                    startActivity(new Intent(CouponPage.this, Login.class));
                    finish();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    //Toast.makeText(Home.this, "Thanks for staying. You are awesome.", Toast.LENGTH_SHORT).show();
                    //No button clicked
                    break;
            }
        }
    };
    public void goback()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setMessage("You have to login to view.Want to proceed to login page?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }
    private void initDrawer() {

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
    }
    /*public void logoff()
    {
        Toast.makeText(CouponPage.this, "Logging out", Toast.LENGTH_SHORT).show();
        SharedPreferences example = getSharedPreferences("USER", 0);
        SharedPreferences.Editor editor = example.edit();
        editor.clear();
        editor.commit();
        Session session = Session.getActiveSession();
        if (session != null) {
            if (!session.isClosed()) {
                session.closeAndClearTokenInformation();
            }
        } else {
            session = new Session(CouponPage.this);
            Session.setActiveSession(session);
            session.closeAndClearTokenInformation();
        }

        startActivity(new Intent(CouponPage.this, Login.class));
        finish();

    }*/
    @Override
   /* protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }*/

    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

   // @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
