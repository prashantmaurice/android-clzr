package com.clozerr.app;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
public class Login extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {
    private static final String TAG = "clozerr";
    private static final String GOOGLE_PLUS_PROJECT_ID = "key-surf-834";
    private static final String GOOGLE_PLUS_CLIENT_ID = "597460526405-i65fhqcbsn0khe3b25qsfav4cohs9dkn.apps.googleusercontent.com";
    private static final String SHA1_HASH = "71:E4:E8:FC:F5:46:67:14:BB:C1:93:E5:A4:82:9A:84:BF:D8:E9:30";
    public static String userName;
    public static String dispPicUrl;
    private static final int STATE_DEFAULT = 0;
    private static final int STATE_SIGN_IN = 1;
    private static final int STATE_IN_PROGRESS = 2;
    static int googleOrFb=3;
    //ImageView slide1=(ImageView)findViewById(R.id.slide1);
//ImageView slide2=(ImageView)findViewById(R.id.slide2);
//ImageView slide3=(ImageView)findViewById(R.id.slide3);
//ImageView slide4=(ImageView)findViewById(R.id.slide4);
    private static final int RC_SIGN_IN = 0;
    private static final int DIALOG_PLAY_SERVICES_ERROR = 0;
    private static final String SAVED_PROGRESS = "sign_in_progress";
    // GoogleApiClient wraps our service connection to Google Play services and
// provides access to the users sign in state and Google's APIs.
    public static GoogleApiClient mGoogleApiClient;
    // We use mSignInProgress to track whether user has clicked sign in.
// mSignInProgress can be one of three values:
//
// STATE_DEFAULT: The default state of the application before the user
// has clicked 'sign in', or after they have clicked
// 'sign out'. In this state we will not attempt to
// resolve sign in errors and so will display our
// Activity in a signed out state.
// STATE_SIGN_IN: This state indicates that the user has clicked 'sign
// in', so resolve successive errors preventing sign in
// until the user has successfully authorized an account
// for our app.
// STATE_IN_PROGRESS: This state indicates that we have started an intent to
// resolve an error, and so we should not start further
// intents until the current intent completes.
    private int mSignInProgress;
    // Used to store the PendingIntent most recently returned by Google Play
// services until the user clicks 'sign in'.
    private PendingIntent mSignInIntent;
    // Used to store the error code most recently returned by Google Play services
// until the user clicks 'sign in'.
    private int mSignInError;
    private ImageButton mSignInButton;
    public static ProgressDialog pDialog;
    private static final int NUM_PAGES = 5;
    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;
    int i=0;
    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
// domain = getString(R.string.domain);
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
//pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setCancelable(false);
//super.setIntegerProperty("splashscreen", R.drawable.splash);
// this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_loginfb);
        mSignInButton = (ImageButton) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(this);
        final Resources reso = this.getResources();
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setPageTransformer(true, new ZoomOutPageTransformer());
        mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                ImageView slide1=(ImageView)findViewById(R.id.slide1);
                ImageView slide2=(ImageView)findViewById(R.id.slide2);
                ImageView slide3=(ImageView)findViewById(R.id.slide3);
                ImageView slide4=(ImageView)findViewById(R.id.slide4);
                ImageView slide5=(ImageView)findViewById(R.id.slide5);
//slide1.setAlpha(0.0f);
                slide1.setBackground((GradientDrawable) reso.getDrawable(R.drawable.image_slider));
                slide2.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider));
                slide3.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider));
                slide4.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider));
                slide5.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider));
                i=position;
                switch (position){
                    case 0:
                        slide1.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider_current));
                        break;
                    case 1:
                        slide2.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider_current));
                        break;
                    case 2:
                        slide3.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider_current));
                        break;
                    case 3:
                        slide4.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider_current));
                        break;
                    case 4:
                        slide5.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider_current));
                        break;
                }
                invalidateOptionsMenu();
            }
        });
        if (savedInstanceState != null) {
            mSignInProgress = savedInstanceState
                    .getInt(SAVED_PROGRESS, STATE_DEFAULT);
        }
        mGoogleApiClient = buildGoogleApiClient();
        int found=0;
//slideToImage(2);
        change();
    }
    public void change(){
        new CountDownTimer(5000, 5000) {
            public void onTick(long millisUntilFinished) {
                slideToImage(i);
            }
            public void onFinish() {
                ++i;
                if(i==5)
                    i=0;
                change();
            }
        }.start();
    }
    private GoogleApiClient buildGoogleApiClient() {
// When we build the GoogleApiClient we specify where connected and
// connection failed callbacks should be returned, which Google APIs our
// app uses and which OAuth 2.0 scopes our app requests.
        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
    }
    @Override
    public void onStart() {
        super.onStart();
// EasyTracker.getInstance().activityStart(this);
// The rest of your onStart() code.
        mGoogleApiClient.connect();
    }
    @Override
    public void onStop() {
        super.onStop();
//EasyTracker.getInstance().activityStop(this);
// The rest of your onStop() code.
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
    @Override
    protected void onActivityResult(final int requestCode,
                                    final int resultCode, final Intent data) {
        if(googleOrFb==1) {
            try {
                Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
            } catch (Exception e) {
                Toast.makeText(Login.this, "Oops. Something went wrong. Please try again later", Toast.LENGTH_SHORT);
            }
        }
        else{
            switch (requestCode) {
                case RC_SIGN_IN:
                    if (resultCode == RESULT_OK) {
// If the error resolution was successful we should continue
// processing errors.
                        mSignInProgress = STATE_SIGN_IN;
                    } else {
// If the error resolution was not successful or the user canceled,
// we should stop processing errors.
                        mSignInProgress = STATE_DEFAULT;
                    }
                    if (!mGoogleApiClient.isConnecting()) {
// If Google Play services resolved the issue with a dialog then
// onStart is not called so we need to re-attempt connection here.
                        mGoogleApiClient.connect();
                    }
                    break;
            }
        }
    }
    private static final List<String> PERMISSIONS = Arrays.asList(
            "email");
    public void facebook(View v) {
        googleOrFb=1;
        Session.openActiveSession(this, true, new Session.StatusCallback() {
            @Override
            public void call(final Session session, SessionState state, Exception exception) {
                if (session.isOpened()) {
                    List<String> permissions = session.getPermissions();
                    if (!isSubsetOf(PERMISSIONS, permissions)) {
                        Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(
                                Login.this, PERMISSIONS);
                        session.requestNewReadPermissions(newPermissionsRequest);
                        return;
                    }
                    Request.newMeRequest(session, new Request.GraphUserCallback() {
                        @Override
                        public void onCompleted(GraphUser user, Response response) {
                            if (user != null) {
                                Log.i("user", user.getName());
                                final SharedPreferences.Editor editor = getSharedPreferences("USER", 0).edit();
                                editor.putString("fb_name", user.getName());
                                editor.putString("fb_id", user.getId());
                                editor.apply();
                                new AsyncGet(Login.this, "http://api.clozerr.com/auth/login/facebook?token=" + session.getAccessToken(), new AsyncGet.AsyncResult() {
                                    @Override
                                    public void gotResult(String s) {
                                        /*Log.i("urltest","http://api.clozerr.com/auth/login/facebook?token=" + session.getAccessToken());
                                        Log.i("token result", s);*/
                                        try {
                                            JSONObject res = new JSONObject(s);
                                            if (res.getString("result").equals("true")) {
                                                editor.putString("loginskip", "true");
                                                editor.putString("token", res.getString("token"));
                                                editor.apply();
                                                startActivity(new Intent(Login.this, Home.class));
                                                finish();
                                            } else {

                                                Toast.makeText(Login.this,session.getAccessToken(),Toast.LENGTH_SHORT).show();
                                                Toast.makeText(Login.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (JSONException e) {
                                            Toast.makeText(Login.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }
                    }).executeAsync();
                }
            }
        });
    }
    public void skiplogin(View v) {
        SharedPreferences example = getSharedPreferences("USER", 0);
        SharedPreferences.Editor editor = example.edit();
        editor.putString("loginskip", "true");
        editor.putString("notNow","true");
        editor.commit();
        startActivity(new Intent(this, Home.class));
        finish();
    }
    private boolean isSubsetOf(Collection<String> subset,
                               Collection<String> superset) {
        for (String string : subset) {
            if (!superset.contains(string)) {
                return false;
            }
        }
        return true;
    }
    public void slide(View v) {
        final Resources reso = this.getResources();
        switch (v.getId()) {
            case R.id.slide1:
                mPager.setCurrentItem(0);
                i=0;
/*slide1.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider_current));
slide2.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider));
slide3.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider));
slide4.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider));*/
                break;
            case R.id.slide2:
                mPager.setCurrentItem(1);
                i=1;
/* slide2.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider_current));
slide1.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider));
slide3.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider));
slide4.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider));*/
                break;
            case R.id.slide3:
                mPager.setCurrentItem(2);
                i=2;
/*slide3.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider_current));
slide2.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider));
slide1.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider));
slide4.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider));*/
                break;
            case R.id.slide4:
                mPager.setCurrentItem(3);
                i=3;
/* slide4.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider_current));
slide2.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider));
slide3.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider));
slide1.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider));*/
                break;
            case R.id.slide5:
                mPager.setCurrentItem(4);
                i=4;
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_PROGRESS, mSignInProgress);
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "onConnected");
        /*// Update the user interface to reflect that the user is signed in.
        // mSignInButton.setEnabled(false);*/

        final Person currentUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        userName = currentUser.getDisplayName();
        dispPicUrl = currentUser.getImage().getUrl();
        // Indicate that the sign in process is complete.
        mSignInProgress = STATE_DEFAULT;

        final SharedPreferences.Editor editor = getSharedPreferences("USER", 0).edit();
        editor.putString("gplus_name", userName);
        editor.putString("gplus_id", currentUser.getId());
        editor.putString("gplus_pic", dispPicUrl);
        editor.apply();

        /*Thread tokenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String token;
                String scopes = "oauth2:" +
                        "https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/plus.login";
                try {
                    token = GoogleAuthUtil.getToken(
                            Login.this,
                            Plus.AccountApi.getAccountName(mGoogleApiClient),
                            scopes);
                    // Log.e("AccessToken", token);
                } catch (IOException transientEx) {
                    // network or server error, the call is expected to succeed if you try again later.
                    // Don't attempt to call again immediately - the request is likely to
                    // fail, you'll hit quotas or back-off.
                    return;
                } catch (UserRecoverableAuthException e) {
                    // Recover
                    token = null;
                } catch (GoogleAuthException authEx) {
                    // Failure. The call is not expected to ever succeed so it should not be
                    // retried.
                    return;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                //Log.e("AccessToken", token);
                Toast.makeText(Login.this, "G+ Token:\n" + token, Toast.LENGTH_LONG).show();
                final String gplusToken = token;

                new AsyncGet(Login.this, "http://api.clozerr.com/auth/login/google?token=" + gplusToken, new AsyncGet.AsyncResult() {
                    @Override
                    public void gotResult(String s) {
                                        *//*Log.i("urltest","http://api.clozerr.com/auth/login/facebook?token=" + session.getAccessToken());
                                        Log.i("token result", s);*//*
                        try {
                            JSONObject res = new JSONObject(s);
                            if (res.getString("result").equals("true")) {
                                editor.putString("loginskip", "true");
                                editor.putString("token", res.getString("token"));
                                editor.apply();
                                startActivity(new Intent(Login.this, Home.class));
                                finish();
                            } else {
                                Toast.makeText(Login.this, gplusToken,Toast.LENGTH_SHORT).show();
                                Toast.makeText(Login.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(Login.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        tokenThread.start();*/
        final Handler handler = getWindow().getDecorView().getHandler();
        new AsyncTokenGet(this, new AsyncTokenGet.AsyncTokenResult() {
            @Override
            public void gotResult(final String s) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Login.this, "G+ Token:\n" + s, Toast.LENGTH_LONG).show();
                        new AsyncGet(Login.this, "http://api.clozerr.com/auth/login/google?token=" + s, new AsyncGet.AsyncResult() {
                            @Override
                            public void gotResult(String s) {
                                //Log.i("urltest","http://api.clozerr.com/auth/login/facebook?token=" + session.getAccessToken());
                                //Log.i("token result", s);
                                try {
                                    JSONObject res = new JSONObject(s);
                                    if (res.getString("result").equals("true")) {
                                        editor.putString("loginskip", "true");
                                        editor.putString("token", res.getString("token"));
                                        editor.apply();
                                        startActivity(new Intent(Login.this, Home.class));
                                        finish();
                                    } else {
                                        Toast.makeText(Login.this, s,Toast.LENGTH_SHORT).show();
                                        Toast.makeText(Login.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    Toast.makeText(Login.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
            }
        });
    }
    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }
    @Override
    public void onClick(View v) {
        googleOrFb = 2;
        if (!mGoogleApiClient.isConnecting()) {
// We only process button clicks when GoogleApiClient is not transitioning
// between connected and not connected.
            switch (v.getId()) {
                case R.id.sign_in_button:
                    Toast.makeText(this, "Signing in...", Toast.LENGTH_LONG).show();
                    resolveSignInError();
                    break;
/*case R.id.sign_out_button:
// We clear the default account on sign out so that Google Play
// services will not return an onConnected callback without user
// interaction.
Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
mGoogleApiClient.disconnect();
mGoogleApiClient.connect();
break;
case R.id.revoke_access_button:
// After we revoke permissions for the user with a GoogleApiClient
// instance, we must discard it and create a new one.
Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
// Our sample has caches no user data from Google+, however we
// would normally register a callback on revokeAccessAndDisconnect
// to delete user data so that we comply with Google developer
// policies.
Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient);
mGoogleApiClient = buildGoogleApiClient();
mGoogleApiClient.connect();
break;*/
            }
        }
    }
        @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
        if (result.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
// An API requested for GoogleApiClient is not available. The device's current
// configuration might not be supported with the requested API or a required component
// may not be installed, such as the Android Wear application. You may need to use a
// second GoogleApiClient to manage the application's optional APIs.
            Toast.makeText(this, "Google+ API(s) unavailable", Toast.LENGTH_LONG).show();
        } else if (mSignInProgress != STATE_IN_PROGRESS) {
// We do not have an intent in progress so we should store the latest
// error resolution intent for use when the sign in button is clicked.
            Toast.makeText(this, "Storing error resolution intent; click sign in again", Toast.LENGTH_LONG).show();
            mSignInIntent = result.getResolution();
            mSignInError = result.getErrorCode();
            if (mSignInProgress == STATE_SIGN_IN) {
// STATE_SIGN_IN indicates the user already clicked the sign in button
// so we should continue processing errors until the user is signed in
// or they click cancel.
                Toast.makeText(this, "Error processing is going on...", Toast.LENGTH_LONG).show();
                resolveSignInError();
            }
        }
        onSignedOut();
    }
    private void onSignedOut() {
        mSignInButton.setEnabled(true);
    }
    private void resolveSignInError() {
        if (mSignInIntent != null) {
// We have an intent which will allow our user to sign in or
// resolve an error. For example if the user needs to
// select an account to sign in with, or if they need to consent
// to the permissions your app is requesting.
            try {
// Send the pending intent that we stored on the most recent
// OnConnectionFailed callback. This will allow the user to
// resolve the error currently preventing our connection to
// Google Play services.
                mSignInProgress = STATE_IN_PROGRESS;
                startIntentSenderForResult(mSignInIntent.getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                Log.i(TAG, "Sign in intent could not be sent: "
                        + e.getLocalizedMessage());
// The intent was canceled before it was sent. Attempt to connect to
// get an updated ConnectionResult.
                mSignInProgress = STATE_SIGN_IN;
                mGoogleApiClient.connect();
            }
        } else {
// Google Play services wasn't able to provide an intent for some
// error types, so we show the default Google Play services error
// dialog which may still start an intent on our behalf if the
// user can resolve the issue.
            showDialog(DIALOG_PLAY_SERVICES_ERROR);
        }
    }
    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
            case DIALOG_PLAY_SERVICES_ERROR:
                if (GooglePlayServicesUtil.isUserRecoverableError(mSignInError)) {
                    return GooglePlayServicesUtil.getErrorDialog(
                            mSignInError,
                            this,
                            RC_SIGN_IN,
                            new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    Log.e(TAG, "Google Play services resolution cancelled");
                                    mSignInProgress = STATE_DEFAULT;
                                    Toast.makeText(getApplicationContext(),"Signed out",Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    return new AlertDialog.Builder(this)
                            .setMessage("Google Play services is not available. This application will close.")
                            .setPositiveButton("Close",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Log.e(TAG, "Google Play services error could not be "
                                                    + "resolved: " + mSignInError);
                                            mSignInProgress = STATE_DEFAULT;
                                            Toast.makeText(getApplicationContext(),"Signed out",Toast.LENGTH_SHORT).show();
                                        }
                                    }).create();
                }
            default:
                return super.onCreateDialog(id);
        }
    }
    public void slideToImage(int position){
        mPager.setCurrentItem(position);
    }
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            return ScreenSlidePageFragment.create(position);
        }
        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}