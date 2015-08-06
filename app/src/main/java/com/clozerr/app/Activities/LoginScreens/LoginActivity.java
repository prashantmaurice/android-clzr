package com.clozerr.app.Activities.LoginScreens;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
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

import com.clozerr.app.AsyncGet;
import com.clozerr.app.AsyncTokenGet;
import com.clozerr.app.Utils.Constants;
import com.clozerr.app.GenUtils;
import com.clozerr.app.Activities.HomeScreens.HomeActivity;
import com.clozerr.app.MainApplication;
import com.clozerr.app.Models.UserMain;
import com.clozerr.app.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 *  @deprecated : This activity is deprecated and is only present for reference purposes
 */
public class LoginActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    private static final String TAG = "clozerr";
    // Keys for persisting instance variables in savedInstanceState
    private static final String KEY_IS_RESOLVING = "is_resolving";
    private static final String KEY_SHOULD_RESOLVE = "should_resolve";
    public static String userName;
    public static String dispPicUrl;
    public static GoogleApiClient googleApiClient;
    public static int googleOrFb;
    private ImageButton btn_login_facebook, btn_login_google;
    public static ProgressDialog pDialog;
    private static final int SLIDES_COUNT = 5;
    private ViewPager mPager;

    // Is there a ConnectionResult resolution in progress?
    private boolean mIsResolving = false;
    // Should we automatically resolve ConnectionResults when possible?
    private boolean mShouldResolve = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mIsResolving = savedInstanceState.getBoolean(KEY_IS_RESOLVING);
            mShouldResolve = savedInstanceState.getBoolean(KEY_SHOULD_RESOLVE);
        }

        setContentView(R.layout.activity_loginpage);
        mPager = (ViewPager) findViewById(R.id.pager);
        btn_login_facebook = (ImageButton) findViewById(R.id.btn_login_facebook);
        btn_login_google = (ImageButton) findViewById(R.id.btn_login_google);
        btn_login_facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btn_login_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShouldResolve = true;
                googleApiClient.connect();
            }
        });

        //Add intro slides UI
        initializeSliderUI();

//        if (savedInstanceState != null) {
//            mSignInProgress = savedInstanceState
//                    .getInt(SAVED_PROGRESS, STATE_DEFAULT);
//        }
        googleApiClient = buildGoogleApiClient();
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
                .addScope(new Scope("https://www.googleapis.com/auth/plus.profile.emails.read"))
                .build();
    }
    @Override
    public void onStart() {
        super.onStart();
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
//        else
//            Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }
    private static final List<String> PERMISSIONS = Arrays.asList(
            "email");
    public void facebook(View v) {
//        googleOrFb=1;
//        Session.openActiveSession(this, true, new Session.StatusCallback() {
//            @Override
//            public void call(final Session session, SessionState state, Exception exception) {
//                if (session.isOpened()) {
//                    List<String> permissions = session.getPermissions();
//                    if (!isSubsetOf(PERMISSIONS, permissions)) {
//                        Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(
//                                LoginActivity.this, PERMISSIONS);
//                        session.requestNewReadPermissions(newPermissionsRequest);
//                        return;
//                    }
//                    Request.newMeRequest(session, new Request.GraphUserCallback() {
//                        @Override
//                        public void onCompleted(GraphUser user, Response response) {
//                            if (user != null) {
//                                Log.i("user", user.getName());
//                                UserMain userMain = MainApplication.getInstance().data.userMain;
//                                userMain.fb_name = user.getName();
//                                userMain.facebookId = user.getId();
//                                userMain.saveUserDataLocally();
//
//                                new AsyncGet(LoginActivity.this, "http://api.clozerr.com/auth/login/facebook?token=" + session.getAccessToken(), new AsyncGet.AsyncResult() {
//                                    @Override
//                                    public void gotResult(String s) {
//                                        /*Log.i("urltest","http://api.clozerr.com/auth/login/facebook?token=" + session.getAccessToken());
//                                        Log.i("token result", s);*/
//                                        try {
//                                            JSONObject res = new JSONObject(s);
//                                            if (res.getBoolean("result")) {
//                                                UserMain userMain = MainApplication.getInstance().data.userMain;
//                                                userMain.loginSkip = true;
//                                                userMain.token = res.getString("token");
//                                                userMain.user = res.getString("user");
//                                                userMain.saveUserDataLocally();
//
//                                                startActivity(new Intent(LoginActivity.this, Home.class));
//                                                finish();
//                                            } else {
//                                                //Toast.makeText(Login.this,session.getAccessToken(),Toast.LENGTH_SHORT).show();
//                                                Toast.makeText(LoginActivity.this, "Something went wrong, please try again after some time", Toast.LENGTH_SHORT).show();
//                                            }
//                                        } catch (JSONException e) {
//                                            Toast.makeText(LoginActivity.this, "Something went wrong, please try again after some time", Toast.LENGTH_SHORT).show();
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                });
//                            }
//                        }
//                    }).executeAsync();
//                }
//            }
//        });
    }
    public void skiplogin(View v) {
        UserMain userMain = MainApplication.getInstance().data.userMain;
        userMain.loginSkip = true;
        userMain.notNow = true;
        userMain.saveUserDataLocally();

        startActivity(new Intent(this, HomeActivity.class));
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
        // m1SignInButton.setEnabled(false);*/

        final Person currentUser = Plus.PeopleApi.getCurrentPerson(googleApiClient);
        userName = currentUser.getDisplayName();
        dispPicUrl = currentUser.getImage().getUrl();

        UserMain userMain = MainApplication.getInstance().data.userMain;
        userMain.gplus_id = currentUser.getId();
        userMain.gplus_name = userName;
        userMain.gplus_pic = dispPicUrl;
        userMain.saveUserDataLocally();


        new AsyncTokenGet(this, new AsyncTokenGet.AsyncTokenResult() {
            @Override
            public void onError() {
                updateUI(false);
                GenUtils.putToast(LoginActivity.this, "Something went wrong, please try again after some time", Toast.LENGTH_SHORT);
            }

            @Override
            public void onResult(final String s) {
                String clozerrAuthURL = GenUtils.getClearedUriBuilder(Constants.URLBuilders.AUTH_LOGIN_GOOGLE)
                                                .appendQueryParameter("token", s)
                                                .build().toString();
                Log.e(TAG, "token generating url - " + clozerrAuthURL);
                new AsyncGet(LoginActivity.this, clozerrAuthURL, new AsyncGet.AsyncResult() {
                    @Override
                    public void gotResult(String s) {
                        try {
                            Log.e(TAG, "result - " + s);
                            JSONObject res = new JSONObject(s);
                            if (res.getBoolean("result")) {

                                UserMain userMain = MainApplication.getInstance().data.userMain;
                                userMain.loginSkip = true;
//                                userMain.token = res.getString("token");
                                userMain.user = res.getString("user");
                                userMain.saveUserDataLocally();



//                                editor.putString("loginskip", "true");
//                                editor.putString("token", res.getString("token"));
//                                editor.putString("user",res.getString("user"));
//                                editor.apply();
                                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                finish();
                            } else {
                                updateUI(false);
                                GenUtils.putToast(LoginActivity.this, "Something went wrong, please try again after some time", Toast.LENGTH_SHORT);
                                Log.e(TAG, "error : " + res.get("err").toString());
                            }
                        } catch (Exception e) {
                            updateUI(false);
                            GenUtils.putToast(LoginActivity.this, "Something went wrong, please try again after some time", Toast.LENGTH_SHORT);
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
//    @Override
//    public void onClick(View v) {
//        googleOrFb = 2;
//        switch (v.getId()) {
//            case R.id.btn_login_google:
//                // User clicked the sign-in button, so begin the sign-in process and automatically
//                // attempt to resolve any errors that occur.
//                mShouldResolve = true;
//                googleApiClient.connect();
//                break;
//        }
//    }
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
        btn_login_facebook.setClickable(!isSignedIn);
        if (!isSignedIn && googleOrFb == 2 && googleApiClient.isConnected())
            googleApiClient.disconnect();
    }


    private void initializeSliderUI(){
        mPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        DemoCollectionPagerAdapter mDemoCollectionPagerAdapter = new DemoCollectionPagerAdapter(getSupportFragmentManager());
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
    }


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