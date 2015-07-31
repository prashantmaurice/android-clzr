package com.clozerr.app;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
public class Login extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {
    private static final String TAG = "clozerr";
    // Keys for persisting instance variables in savedInstanceState
    private static final String KEY_IS_RESOLVING = "is_resolving";
    private static final String KEY_SHOULD_RESOLVE = "should_resolve";
    public static String userName;
    public static String dispPicUrl;
    static int googleOrFb=3;
    public static GoogleApiClient googleApiClient;
    private ImageButton mSignInButton;
    public static ProgressDialog pDialog;
    private static final int SLIDES_COUNT = 5;
    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;
    int i=0;

    // Is there a ConnectionResult resolution in progress?
    private boolean mIsResolving = false;
    // Should we automatically resolve ConnectionResults when possible?
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
        mPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        // ViewPager and its adapters use support library fragments, so use getSupportFragmentManager.
        DemoCollectionPagerAdapter mDemoCollectionPagerAdapter = new DemoCollectionPagerAdapter(getSupportFragmentManager());

//        mPager.setPageTransformer(true, new ZoomOutPageTransformer());
        /*
            The pager adapter, which provides the pages to the view pager widget.
        */
//        PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
        mPager.setAdapter(mDemoCollectionPagerAdapter);
        final View pagerDot_1 = findViewById(R.id.pagerDot_1);
        final View pagerDot_2 = findViewById(R.id.pagerDot_2);
        final View pagerDot_3 = findViewById(R.id.pagerDot_3);
        final View pagerDot_4 = findViewById(R.id.pagerDot_4);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                pagerDot_1.setBackground(getResources().getDrawable(R.drawable.signup_dot));
                pagerDot_2.setBackground(getResources().getDrawable(R.drawable.signup_dot));
                pagerDot_3.setBackground(getResources().getDrawable(R.drawable.signup_dot));
                pagerDot_4.setBackground(getResources().getDrawable(R.drawable.signup_dot));
                switch (position + 1) {
                    case 1:
                        pagerDot_1.setBackground(getResources().getDrawable(R.drawable.signup_dot_active));
                        break;
                    case 2:
                        pagerDot_2.setBackground(getResources().getDrawable(R.drawable.signup_dot_active));
                        break;
                    case 3:
                        pagerDot_3.setBackground(getResources().getDrawable(R.drawable.signup_dot_active));
                        break;
                    case 4:
                        pagerDot_4.setBackground(getResources().getDrawable(R.drawable.signup_dot_active));
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
//        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
//            @Override
//            public void onPageSelected(int position) {
//                ImageView slide1=(ImageView)findViewById(R.id.slide1);
//                ImageView slide2=(ImageView)findViewById(R.id.slide2);
//                ImageView slide3=(ImageView)findViewById(R.id.slide3);
//                ImageView slide4=(ImageView)findViewById(R.id.slide4);
//                ImageView slide5=(ImageView)findViewById(R.id.slide5);
////slide1.setAlpha(0.0f);
//                slide1.setBackground(reso.getDrawable(R.drawable.image_slider));
//                slide2.setBackground(reso.getDrawable(R.drawable.image_slider));
//                slide3.setBackground(reso.getDrawable(R.drawable.image_slider));
//                slide4.setBackground(reso.getDrawable(R.drawable.image_slider));
//                slide5.setBackground(reso.getDrawable(R.drawable.image_slider));
//                i=position;
//                switch (position){
//                    case 0:
//                        slide1.setBackground(reso.getDrawable(R.drawable.image_slider_current));
//                        break;
//                    case 1:
//                        slide2.setBackground(reso.getDrawable(R.drawable.image_slider_current));
//                        break;
//                    case 2:
//                        slide3.setBackground(reso.getDrawable(R.drawable.image_slider_current));
//                        break;
//                    case 3:
//                        slide4.setBackground(reso.getDrawable(R.drawable.image_slider_current));
//                        break;
//                    case 4:
//                        slide5.setBackground(reso.getDrawable(R.drawable.image_slider_current));
//                        break;
//                }
//                invalidateOptionsMenu();
//            }
//        });
//        if (savedInstanceState != null) {
//            mSignInProgress = savedInstanceState
//                    .getInt(SAVED_PROGRESS, STATE_DEFAULT);
//        }
        googleApiClient = buildGoogleApiClient();
        //int found=0;
        //slideToImage(2);
        //change();
    }
    /*public void change(){
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
    }*/
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
        //googleApiClient.connect();
    }
    @Override
    public void onStop() {
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
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

        if (requestCode == Constants.RequestCodes.GOOGLE_SIGN_IN_ACTIVITY) {
            // If the error resolution was not successful we should not resolve further errors.
            if (resultCode != RESULT_OK) {
                mShouldResolve = false;
            }

            mIsResolving = false;
            googleApiClient.connect();
        }
        else
            Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
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
                                            if (res.getBoolean("result")) {
                                                editor.putString("loginskip", "true");
                                                editor.putString("token", res.getString("token"));
                                                editor.putString("user",res.getString("user"));
                                                editor.apply();
                                                startActivity(new Intent(Login.this, Home.class));
                                                finish();
                                            } else {
                                                //Toast.makeText(Login.this,session.getAccessToken(),Toast.LENGTH_SHORT).show();
                                                Toast.makeText(Login.this, "Something went wrong, please try again after some time", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (JSONException e) {
                                            Toast.makeText(Login.this, "Something went wrong, please try again after some time", Toast.LENGTH_SHORT).show();
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
    /*public void slide(View v) {
        //final Resources reso = this.getResources();
        switch (v.getId()) {
            case R.id.slide1:
                mPager.setCurrentItem(0);
                i=0;
*//*slide1.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider_current));
slide2.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider));
slide3.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider));
slide4.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider));*//*
                break;
            case R.id.slide2:
                mPager.setCurrentItem(1);
                i=1;
*//* slide2.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider_current));
slide1.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider));
slide3.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider));
slide4.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider));*//*
                break;
            case R.id.slide3:
                mPager.setCurrentItem(2);
                i=2;
*//*slide3.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider_current));
slide2.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider));
slide1.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider));
slide4.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider));*//*
                break;
            case R.id.slide4:
                mPager.setCurrentItem(3);
                i=3;
*//* slide4.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider_current));
slide2.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider));
slide3.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider));
slide1.setBackground((GradientDrawable)reso.getDrawable(R.drawable.image_slider));*//*
                break;
            case R.id.slide5:
                mPager.setCurrentItem(4);
                i=4;
        }
    }*/
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

        final Person currentUser = Plus.PeopleApi.getCurrentPerson(googleApiClient);
        userName = currentUser.getDisplayName();
        dispPicUrl = currentUser.getImage().getUrl();

        final SharedPreferences.Editor editor = getSharedPreferences("USER", 0).edit();
        editor.putString("gplus_name", userName);
        editor.putString("gplus_id", currentUser.getId());
        editor.putString("gplus_pic", dispPicUrl);
        editor.apply();


        new AsyncTokenGet(this, new AsyncTokenGet.AsyncTokenResult() {
            @Override
            public void onError() {
                updateUI(false);
                GenUtils.putToast(Login.this, "Something went wrong, please try again after some time", Toast.LENGTH_SHORT);
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
                            Log.e(TAG, "result - " + s);
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
                                GenUtils.putToast(Login.this, "Something went wrong, please try again after some time", Toast.LENGTH_SHORT);
                                Log.e(TAG, "error : " + res.get("err").toString());
                            }
                        } catch (Exception e) {
                            updateUI(false);
                            GenUtils.putToast(Login.this, "Something went wrong, please try again after some time", Toast.LENGTH_SHORT);
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
        switch (v.getId()) {
            case R.id.sign_in_button:
                // User clicked the sign-in button, so begin the sign-in process and automatically
                // attempt to resolve any errors that occur.
                mShouldResolve = true;
                googleApiClient.connect();
                break;
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
                    connectionResult.startResolutionForResult(this, Constants.RequestCodes.GOOGLE_SIGN_IN_ACTIVITY);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Could not resolve ConnectionResult.", e);
                    mIsResolving = false;
                    googleApiClient.connect();
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
            GooglePlayServicesUtil.getErrorDialog(errorCode, this, Constants.RequestCodes.GOOGLE_SIGN_IN_ACTIVITY,
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
        mSignInButton.setClickable(!isSignedIn);
        if (!isSignedIn && googleOrFb == 2 && googleApiClient.isConnected())
            googleApiClient.disconnect();
    }

    /*public void slideToImage(int position){
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
    }*/



    ArrayList<String> slidesQuotes = new ArrayList<>(Arrays.asList("", "<span style='font-size=16px'>tap &#9825; and add<br>clubs to <b>my clubs</b></span>", "unlock rewards","<span style='font-size=16px'>tap <b>check in</b> and choose<br>reward to redeem</span>","<span style='font-size=16px'>tap <b>check in</b> and choose<br>reward to redeem</span>"));
    ArrayList<Integer> slidesImages = new ArrayList<>(Arrays.asList(R.drawable.signup_slide_2,R.drawable.signup_slide_2,R.drawable.signup_slide_3,R.drawable.signup_slide_4,R.drawable.signup_slide_5));

    //OUR MAIN SLIDE VIEW FRAGMENT ADAPTER */
    public class DemoCollectionPagerAdapter extends FragmentPagerAdapter {
        public DemoCollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if(i==0) return new IntroSlideFragment();

            Fragment fragment = new SlideFragment();
            Bundle args = new Bundle();
            args.putString(SlideFragment.QUOTE, slidesQuotes.get(i));
            args.putInt(SlideFragment.IMAGE, slidesImages.get(i));
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return SLIDES_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position + 1);
        }
    }
    //OUR MAIN SLIDE VIEW FRAGMENTS
    public static class SlideFragment extends Fragment {
        public static final String QUOTE = "object";
        public static final String IMAGE = "image";
        @Override
        public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_signup_slide, container, false);
            Bundle args = getArguments();
            TextView myTextView = ((TextView) rootView.findViewById(R.id.textView));
            ImageView imageView = (ImageView) rootView.findViewById(R.id.imageView);
            myTextView.setText(Html.fromHtml(args.getString(QUOTE)));
            imageView.setImageResource(args.getInt(IMAGE));

            return rootView;
        }
    }
    public static class IntroSlideFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_signup_slide_first, container, false);
        }
    }


}