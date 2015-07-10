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
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
public class Login extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {
    private static final String TAG = "clozerr";
    /* Keys for persisting instance variables in savedInstanceState */
    private static final String KEY_IS_RESOLVING = "is_resolving";
    private static final String KEY_SHOULD_RESOLVE = "should_resolve";
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
    private static final int RC_SIGN_IN = 9001;
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

    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;

    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
// domain = getString(R.string.domain);

        if (savedInstanceState != null) {
            mIsResolving = savedInstanceState.getBoolean(KEY_IS_RESOLVING);
            mShouldResolve = savedInstanceState.getBoolean(KEY_SHOULD_RESOLVE);
        }

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
        /*
      The pager adapter, which provides the pages to the view pager widget.
     */
        PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
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
                slide1.setBackground(reso.getDrawable(R.drawable.image_slider));
                slide2.setBackground(reso.getDrawable(R.drawable.image_slider));
                slide3.setBackground(reso.getDrawable(R.drawable.image_slider));
                slide4.setBackground(reso.getDrawable(R.drawable.image_slider));
                slide5.setBackground(reso.getDrawable(R.drawable.image_slider));
                i=position;
                switch (position){
                    case 0:
                        slide1.setBackground(reso.getDrawable(R.drawable.image_slider_current));
                        break;
                    case 1:
                        slide2.setBackground(reso.getDrawable(R.drawable.image_slider_current));
                        break;
                    case 2:
                        slide3.setBackground(reso.getDrawable(R.drawable.image_slider_current));
                        break;
                    case 3:
                        slide4.setBackground(reso.getDrawable(R.drawable.image_slider_current));
                        break;
                    case 4:
                        slide5.setBackground(reso.getDrawable(R.drawable.image_slider_current));
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
        //int found=0;
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
    public GoogleApiClient buildGoogleApiClient() {
// When we build the GoogleApiClient we specify where connected and
// connection failed callbacks should be returned, which Google APIs our
// app uses and which OAuth 2.0 scopes our app requests.
        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PLUS_LOGIN))
                .addScope(new Scope(Scopes.PROFILE))
                .build();
    }
    @Override
    public void onStart() {
        super.onStart();
// EasyTracker.getInstance().activityStart(this);
// The rest of your onStart() code.
        //mGoogleApiClient.connect();
    }
    @Override
    public void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        if (pDialog != null)
            pDialog.dismiss();
        AsyncGet.dismissDialog();
        AsyncTokenGet.dismissDialog();
        super.onStop();
//EasyTracker.getInstance().activityStop(this);
// The rest of your onStop() code.
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        if (requestCode == RC_SIGN_IN) {
            // If the error resolution was not successful we should not resolve further errors.
            if (resultCode != RESULT_OK) {
                mShouldResolve = false;
            }

            mIsResolving = false;
            mGoogleApiClient.connect();
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
                                                editor.putString("user",res.getString("user"));
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
        editor.apply();
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
        //final Resources reso = this.getResources();
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
        outState.putBoolean(KEY_IS_RESOLVING, mIsResolving);
        outState.putBoolean(KEY_SHOULD_RESOLVE, mIsResolving);
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


        new AsyncTokenGet(this, new AsyncTokenGet.AsyncTokenResult() {
            @Override
            public void onError() {
                updateUI(false);
                GenUtils.putToast(Login.this, "Something went wrong, please try again", Toast.LENGTH_SHORT);
            }

            @Override
            public void onResult(final String s) {
                String clozerrAuthURL = GenUtils.getClearedUriBuilder(Constants.URLBuilders.AUTH_LOGIN_GOOGLE)
                                                .appendQueryParameter("token", s)
                                                .build().toString();
                Log.e(TAG, "token generating url - " + clozerrAuthURL);
                new AsyncGet(Login.this, clozerrAuthURL, new AsyncGet.AsyncResult() {
                    @Override
                    public void gotResult(String s) {
                        try {
                            JSONObject res = new JSONObject(s);
                            if (res.getBoolean("result")) {
                                editor.putString("loginskip", "true");
                                editor.putString("token", res.getString("token"));
                                editor.putString("user",res.getString("user"));
                                editor.apply();
                                startActivity(new Intent(Login.this, Home.class));
                                finish();
                            } else {
                                updateUI(false);
                                GenUtils.putToast(Login.this, "Something went wrong, please try again", Toast.LENGTH_SHORT);
                                Log.e(TAG, "error : " + res.get("err").toString());
                            }
                        } catch (Exception e) {
                            updateUI(false);
                            GenUtils.putToast(Login.this, "Something went wrong, please try again", Toast.LENGTH_SHORT);
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost. The GoogleApiClient will automatically
        // attempt to re-connect. Any UI elements that depend on connection to Google APIs should
        // be hidden or disabled until onConnected is called again.
        Log.w(TAG, "onConnectionSuspended:" + i);
    }
    @Override
    public void onClick(View v) {
        googleOrFb = 2;
        if (!mGoogleApiClient.isConnecting()) {
// We only process button clicks when GoogleApiClient is not transitioning
// between connected and not connected.
            switch (v.getId()) {
                case R.id.sign_in_button:
                    // User clicked the sign-in button, so begin the sign-in process and automatically
                    // attempt to resolve any errors that occur.
                    mShouldResolve = true;
                    mGoogleApiClient.connect();
                    break;
            }
        }
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);

        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Could not resolve ConnectionResult.", e);
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            } else {
                // Could not resolve the connection result, show the user an
                // error dialog.
                showErrorDialog(connectionResult);
            }
        } else {
            // Show the signed-out UI
            updateUI(false);
        }
    }
    private void showErrorDialog(ConnectionResult connectionResult) {
        int errorCode = connectionResult.getErrorCode();

        if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {
            // Show the default Google Play services error dialog which may still start an intent
            // on our behalf if the user can resolve the issue.
            GooglePlayServicesUtil.getErrorDialog(errorCode, this, RC_SIGN_IN,
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            mShouldResolve = false;
                            updateUI(false);
                        }
                    }).show();
        } else {
            // No default Google Play Services error, display a message to the user.
            String errorString = getString(R.string.play_services_error_fmt, errorCode);
            Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();

            mShouldResolve = false;
            updateUI(false);
        }
    }

    private void updateUI(boolean isSignedIn) {
        mSignInButton.setEnabled(!isSignedIn);
    }

    private void onSignedOut() {
        Toast.makeText(this, "An error occurred while signing in... please try signing in again.",
                Toast.LENGTH_LONG).show();
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