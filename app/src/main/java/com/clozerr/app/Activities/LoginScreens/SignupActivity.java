package com.clozerr.app.Activities.LoginScreens;

import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.clozerr.app.GenUtils;
import com.clozerr.app.Handlers.TokenHandler;
import com.clozerr.app.Home;
import com.clozerr.app.MainApplication;
import com.clozerr.app.Models.UserMain;
import com.clozerr.app.R;
import com.clozerr.app.Utils.Logg;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/** Documented by maurice
 *
 *  This is the main starting point of the app, When you open the app the following thing happens
 *  1) Check it you have userId, if yes, then fetch data from server and go to main app
 *  2) If you don't have userId then show login slides for user to give email
 *  3) You send emailId to server and it returns with an userId. Incase of new user, server creates new user
 *  4) With available userId do the first step to go to main app
 *
 *  Calls that happens are listed below:
 *
 *  Skipped user:   userdata <-- server
 *
 *  New user:       socialToken --> server
 *                  clozerrToken <-- server
 *                  clozerrToken --> server
 *                  userdata <-- server
 *
 *  Existing user:  clozerrToken --> server
 *                  userdata <-- server
 *
 *  CToken expire:  socialToken --> server
 *                  clozerrToken <-- server
 *
 *  TODO : why do we need to fetch user data everytime user opens the app when app is supposed to work offline
 */


public class SignupActivity extends FragmentActivity {

    //SETTINGS VARIABLES
    private static final int RC_SIGN_IN = 0;
    private static final int SLIDES_COUNT = 5;

    //RUNTIME VARIABLES
    private CallbackManager callbackManager; //add callback to facebook signin calls
    private GoogleApiClient mGoogleApiClient; // Google client to interact with Google API
    private boolean mIntentInProgress; // A flag indicating that a PendingIntent is in progress and prevents us from starting further intents from google signin.
    private boolean mSignInClicked;
    private ConnectionResult mConnectionResult;
    UserMain userMain;
    TokenHandler tokenHandler;

    //VIEW VARIABLES
    private ImageButton btn_login_facebook, btn_login_google;
    private ViewPager mPager;



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode != RESULT_OK) mSignInClicked = false;
            mIntentInProgress = false;
            if (!mGoogleApiClient.isConnecting()) mGoogleApiClient.connect();
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tokenHandler = MainApplication.getInstance().tokenHandler;

        if(tokenHandler.isLoggedIn()){ //user variable already created
            //Run is a seperate thread as google token update may create deadlock exception
            AsyncTask task = new AsyncTask() {
                @Override
                protected Object doInBackground(Object... params) {
                    try {
                        loginWithUserId(tokenHandler);
                    } catch (JSONException e) {e.printStackTrace();}
                    return null;
                }
            };
            task.execute((Void) null);
        }else{ //user variable not yet created

            //Initialize facebook login client
            addFacebookLoginCallbacks();

            //Initialize google login client
            addGoogleLoginCallbacks();

            //both the above functions calls registerForEmail function in the end

            //set content to show slides
            setContentView(R.layout.activity_loginpage);
            setUpSlidesUI();
        }
    }

    /** Helper functions */
    private void addFacebookLoginCallbacks(){
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                Logg.m("MAIN", "Facebook Login Success : AccessToken=" + loginResult.getAccessToken().getToken());
                tokenHandler.socialtoken = loginResult.getAccessToken().getToken();
                tokenHandler.authProvider = UserMain.AUTH_FACEBOOK;
            }

            @Override
            public void onCancel() {
                Logg.m("MAIN", "Response  : Facebook Graph = Cancelled");
            }

            @Override
            public void onError(FacebookException e) {
                Logg.m("MAIN", "Response  : Facebook Graph = Error=" + e.toString());
            }
        });
    }
    private void addGoogleLoginCallbacks(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        mSignInClicked = false;
                        GenUtils.showDebugToast(SignupActivity.this, "User is connected to google account!");
                        String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
                        String scope = "oauth2:" + Scopes.PLUS_LOGIN;
                        try {
                            String token = GoogleAuthUtil.getToken(SignupActivity.this.getApplicationContext(), email, scope);
                            tokenHandler.socialtoken = token;
                            tokenHandler.authProvider = UserMain.AUTH_GOOGLE;

                        } catch (IOException | GoogleAuthException e) {
                            GenUtils.showDebugToast(SignupActivity.this,"no token available with google");//should never come here
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        mGoogleApiClient.connect();
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        if (!connectionResult.hasResolution()) {
                            Logg.e("ERROR","Google login : "+connectionResult.toString());
                            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), SignupActivity.this, 0).show();
                            return;
                        }
                        if (!mIntentInProgress) {// Store the ConnectionResult for later usage
                            mConnectionResult = connectionResult;
                            if (mSignInClicked) {
                                // The user has already clicked 'sign-in' so we attempt to
                                // resolve all
                                // errors until the user is signed in, or they cancel.
                                resolveSignInError();
                            }
                        }
                    }
                }).addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();
    }
    private void updateLoginTokens(){
        try {
            if (userMain.authProvider.equals(UserMain.AUTH_GOOGLE)) {
                userMain.token = GoogleAuthUtil.getToken(this,userMain.email, "oauth2:" + Scopes.PLUS_LOGIN);
                userMain.saveUserDataLocally();
            }else{
                AccessToken token = AccessToken.getCurrentAccessToken();
                userMain.token = token.getToken();
                userMain.saveUserDataLocally();
            }
        }catch (RuntimeException | GoogleAuthException | IOException e) {e.printStackTrace();}
    }



    /** Google sign in Logic functions */
    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient!=null) mGoogleApiClient.connect();
    }
    protected void onStop() {
        super.onStop();
        if ((mGoogleApiClient!=null) && mGoogleApiClient.isConnected()) mGoogleApiClient.disconnect();
    }
    private void signInWithGplus() {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }
    }


    /** Registration of user:
     *
     *  When user presses login through facebook/google, both call this function in the end.
     *  After registraton in server, this function automatically calls data fill function
     *
     */
    private void registerForEmail(final String email, final String name, final String facebookId, final String token, final String imageUrl, final boolean isFb){
//        String url = Router.User.getWIthEmailComplete(email);
//        JSONObject params = new JSONObject();
//        try {
//            params.put("name",name);
//            params.put("isFb",isFb);
//            params.put("token",token);
//            params.put("email",email);
//            params.put("profilepic",imageUrl);
//            params.put("facebookId",facebookId);
//        } catch (JSONException e) {e.printStackTrace();}
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
//
//            @Override
//            public void onResponse(JSONObject jsonObject) {
//                try {
//                    JSONObject result = jsonObject.getJSONObject("result");
//                    String userId = result.getString("userId");
//                    userMain.userId = userId;
//                    userMain.token = token;
//                    userMain.email = email;
//                    userMain.authProvider = (isFb)?"facebook":"google";
//                    updateLoginTokens();
//                    userMain.saveUserDataLocally();
//
//                    loginWithUserId(userId);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError volleyError) {
//                Log.e("ERROR","error in registerForEmail");
//            }
//        });
//        MainApplication.getInstance().getRequestQueue().add(jsonObjectRequest);
    }

    /** UI RELATED FUNCTIONS */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        for(int i=0;i<menu.size();i++) menu.getItem(i).setVisible(false);
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

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    private void createShortcutIcon(){

        Intent shortcutIntent = new Intent(getApplicationContext(), SignupActivity.class);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Clozerr");
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.logo));
        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        getApplicationContext().sendBroadcast(addIntent);
    }


    //MAIN USER UPSERT
    /** ANY LOGIN METHOD SHOULD IN THE END CALL THIS FUNCTION
     *
     * This first sends server email to get userId through user object,
     * then updates in local and then syncs with server*/
    private void loginWithUserId(final TokenHandler tokenHandler) throws JSONException {
//        String url = Router.User.getWIthIdComplete(userId);
//        Logg.m("MAIN", "Pulling all data with userID : " + userId);
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(), new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                try {
//                    Logg.m("MAIN", "Response : Email check = " + response.toString());
//                    if(response.getString("status").equalsIgnoreCase("success")) {
//
//                        //Gets if user has previous registration data
//                        MainApplication.getInstance().data.refillCompleteData(response.getJSONObject("result"));
//                        tellserviceToReconnectToOpenfire();
//
//                        loginToMainApp();
//                    }else{
//                        Utils.showDebugToast(getApplicationContext(),"error in creating user");
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.d("ERROR","Error in getting all user data"+error.getLocalizedMessage());
//            }
//        });
//
//        MainApplication.getInstance().getRequestQueue().add(jsonObjectRequest);
    }



    private void tellserviceToReconnectToOpenfire(){
        //TODO : check if this is needed
//        if(XmppClientService.getInstance()!=null){
//            XmppClientService.getInstance().tryConnectWithOpenfireIfNot();
//        }
    }

    //This is the main entrance to the Main APP
//    private void loginToMainApp(){
//        final AsyncTask asyncTask = new AsyncTask() {
//
//            private final String[] TOPICS = {"global"};
//
//            @Override
//            protected Object doInBackground(Object[] params) {
//                Log.i("AsyncTask", "Inside async task, trying to generate GCM id");
//                InstanceID instanceID = InstanceID.getInstance(SignupActivity.this);
//                String token = "";
//                try {
//                    token = instanceID.getToken(getString(R.string.GcmProjectId),
//                            GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
//                    Log.i("AsyncTask", "GCM id is " + token);
//
//                    Data data = MainApplication.getInstance().data;
//                    data.userMain.gcmId = token;
//                    data.userMain.serverSyncPush();
//
//                    for (String topic : TOPICS) {
//                        GcmPubSub pubSub = GcmPubSub.getInstance(SignupActivity.this);
//                        pubSub.subscribe(token, "/topics/" + topic, null);
//                    }
//
//                } catch (IOException e) {
//                    Log.e("AsyncTask", "Failed to generate GCM id");
//                    e.printStackTrace();
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Object o) {
//                super.onPostExecute(o);
//            }
//        };
//        userMain = MainApplication.getInstance().data.userMain;
//        if(!userMain.hasValidUserType()){
//            Intent intent = new Intent(SignupActivity.this, MotherDoctorActivity.class);
//            startActivity(intent);
//            finish();
//        }else{
//            if(userMain.isDoctor()) {
//                if(!userMain.hasValidPhoneNumber()){
//                    Intent intent = new Intent(SignupActivity.this, AddPhoneNumberActivity.class);
//                    startActivity(intent);
//                    finish();
//                }else if(!userMain.isAuthorized()){
//                    Intent intent1 = new Intent(SignupActivity.this, DoctorOfflineActivity.class);
//                    startActivity(intent1);
//                    finish();
//                }else{
//                    Intent intent1 = new Intent(SignupActivity.this, NavDrawerActivity.class);
//                    startActivity(intent1);
//                    finish();
//                }
//            }else{
//                Intent intent1 = new Intent(SignupActivity.this, NavDrawerActivity.class);
//                startActivity(intent1);
//                finish();
//            }
//        }
//        asyncTask.execute(this);
//
//    }
    private void logInToMainApp(){
        Intent intent1 = new Intent(SignupActivity.this, Home.class);
        startActivity(intent1);
        finish();
    }



    /** UI OF SIGNUP SCREEN */
    private void setUpSlidesUI(){

        mPager = (ViewPager) findViewById(R.id.pager);
        btn_login_facebook = (ImageButton) findViewById(R.id.btn_login_facebook);
        btn_login_google = (ImageButton) findViewById(R.id.btn_login_google);

        initializeSliderUI();

        btn_login_facebook = (ImageButton) findViewById(R.id.btn_login_facebook);
        btn_login_facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logInWithReadPermissions(SignupActivity.this, Arrays.asList("email", "public_profile"));
            }
        });

        btn_login_google = (ImageButton) findViewById(R.id.btn_login_google);
        btn_login_google.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                signInWithGplus();
            }
        });
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
