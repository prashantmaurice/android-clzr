package com.clozerr.app.Handlers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.clozerr.app.Activities.HomeScreens.HomeActivity;
import com.clozerr.app.GenUtils;
import com.clozerr.app.MainApplication;
import com.clozerr.app.Models.UserMain;
import com.clozerr.app.Storage.SharedPrefs;
import com.clozerr.app.Utils.Logg;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * This contains all the User data excluding kids,
 */
public class TokenHandler {
    static String TAG = "TOKENHANDLER";
    private Context mContext;
    private static TokenHandler instance;
    private SharedPrefs sPrefs;
    public String name;

    public static String AUTH_FACEBOOK = "facebook";
    public static String AUTH_GOOGLE = "google";
    public static String AUTH_NONE = "none";

    public String fb_name;
    public String facebookId;
    public String authProvider = AUTH_NONE;

    public String userId;
    public String user;
    public String email;//this needs to be stored so that google token update can work seamlessly
    public String imageUrl;
    public String gcmId;
    public String phone;

    //actual tracked variables
    public String clozerrtoken;//this is clozerr service token
    public String socialtoken;//this is token of facebook/google
    public String username;
    public String picurl;



    public String address;
    public String coverPic;

    public String gplus_name;
    public String gplus_id;
    public String gplus_pic;

    public String latitude;
    public String longitude;

    public boolean loginSkip = false;
    public boolean notNow = false; //todo : why needing this?
    public boolean isMale = false;

    //Some extra variables
    public String categories_cards;
    public String home_cards;
    private GoogleApiClient finalMGoogleApiClient;//used when logging out


    private TokenHandler(Context context) {
        mContext = context;
        pullTokenDataFromLocal();
        if(hasSocialToken()){
            updateLoginTokens();
        }
    }
    public static TokenHandler getInstance(Context context) {
        if(instance == null) {
            instance = new TokenHandler(context);
        }
        return instance;
    }


    //LOCAL STORAGE ENCODERS
    public void pullTokenDataFromLocal() {
        sPrefs = SharedPrefs.getInstance(mContext);
        try {
            authProvider = (sPrefs.loginData.has("authProvider"))?sPrefs.loginData.getString("authProvider"):AUTH_NONE;
            socialtoken = (sPrefs.loginData.has("socialtoken"))?sPrefs.loginData.getString("socialtoken"):"";
            clozerrtoken = (sPrefs.loginData.has("clozerrtoken"))?sPrefs.loginData.getString("clozerrtoken"):"";
            loginSkip = (sPrefs.loginData.has("loginSkip"))?sPrefs.loginData.getBoolean("loginSkip"):false;
            username = (sPrefs.loginData.has("username"))?sPrefs.loginData.getString("username"):"";
            picurl = (sPrefs.loginData.has("picurl"))?sPrefs.loginData.getString("picurl"):"";
            email = (sPrefs.loginData.has("email"))?sPrefs.loginData.getString("email"):"";
        } catch (JSONException e) {e.printStackTrace();}
    }
    public void saveTokenDataLocally() {
        try {
            sPrefs.loginData.put("authProvider", authProvider);
            sPrefs.loginData.put("clozerrtoken", clozerrtoken);
            sPrefs.loginData.put("socialtoken", socialtoken);
            sPrefs.loginData.put("loginSkip", loginSkip);
            sPrefs.loginData.put("username", username);
            sPrefs.loginData.put("picurl", picurl);
            sPrefs.loginData.put("email", email);
        } catch (JSONException e) {e.printStackTrace();}
        sPrefs.saveLoginData();
    }


    /** SOME PUBLIC GET FUNCTIONS */
    public boolean hasToken(){
        return (!clozerrtoken.isEmpty());
    }
    public boolean isLoggedIn() {
        return (!clozerrtoken.isEmpty());
    }
    public boolean hasSkippedLogin(){
        return loginSkip;
    }
    public boolean hasSocialToken(){
        return (!socialtoken.isEmpty());
    }
    public boolean updateToken(){
        return (!clozerrtoken.isEmpty());
    }
    public boolean loggedByFb(){
        return (authProvider.equals(AUTH_FACEBOOK));
    }
    public boolean loggedByGoogle(){
        return (authProvider.equals(AUTH_GOOGLE));
    }

    /** SOME PUBLIC PUT FUNCTIONS */
    public void logout(Activity activity) {
        clozerrtoken = "";
        email = "";
        socialtoken = "";
        authProvider = AUTH_NONE;
        username = "";
        picurl  = "";
        loginSkip = false;
        saveTokenDataLocally();
        if(loggedByGoogle())logoutGoogle(activity);
        if(loggedByFb())logoutFacebook(activity);
    }

    public void addSocialToken(String token, String authProviderStr){
        socialtoken = token;
        authProvider = authProviderStr;
        saveTokenDataLocally();
    }

    public void getClozerrToken(final ClozerrTokenListener listener){
        String authGuy = (authProvider.equals(AUTH_GOOGLE))?"google":"facebook";
        String url = "http://api.clozerr.com/auth/login/"+authGuy+"?token=" + socialtoken;
        Logg.d("url",url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Logg.m("MAIN", "Response : Email check = " + response.toString());
                    if(response.getBoolean("result")) {
                        String token = response.getString("token");
                        JSONObject profile = response.getJSONObject("user").getJSONObject("profile");
                        MainApplication.getInstance().tokenHandler.clozerrtoken = token;
                        MainApplication.getInstance().tokenHandler.username = profile.getString("name");
                        MainApplication.getInstance().tokenHandler.picurl = profile.getString("picture");
                        MainApplication.getInstance().tokenHandler.saveTokenDataLocally();
                        listener.onClozerTokenUpdated();
                    }else{
                        GenUtils.showDebugToast(mContext.getApplicationContext(), "error in parsing clozerr token");
                    }
                } catch (JSONException e) {
                    GenUtils.showDebugToast(mContext.getApplicationContext(), "error in fetching clozerr token");
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                GenUtils.showDebugToast(mContext.getApplicationContext(), "server did not return clozerr token");

                loginSkip = true;
                listener.onClozerTokenUpdated();
                Log.d("ERROR", "Error in getting all user data" + error.getLocalizedMessage());
            }
        });
        MainApplication.getInstance().getRequestQueue().add(jsonObjectRequest);
    }

    private void updateLoginTokens(){
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                try {
                    if (authProvider.equals(UserMain.AUTH_GOOGLE)) {
                        clozerrtoken = GoogleAuthUtil.getToken(mContext, email, "oauth2:" + Scopes.PLUS_LOGIN);
                        saveTokenDataLocally();
                    }else{
                        AccessToken tokenFb = AccessToken.getCurrentAccessToken();
                        clozerrtoken = tokenFb.getToken();
                        saveTokenDataLocally();
                    }
                }catch (RuntimeException | GoogleAuthException | IOException e) {e.printStackTrace();}
                return null;
            }
        };
        task.execute((Void) null);

    }

    public interface ClozerrTokenListener{
        void onClozerTokenUpdated();
    }

    public void skipLogin(){
        if(!loginSkip){
            loginSkip = true;
            saveTokenDataLocally();
        }
    }

    public void print(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("username",""+MainApplication.getInstance().tokenHandler.username);
            obj.put("picurl",""+MainApplication.getInstance().tokenHandler.picurl);
            obj.put("clozerrtoken",""+MainApplication.getInstance().tokenHandler.clozerrtoken);
            obj.put("socialtoken",""+MainApplication.getInstance().tokenHandler.socialtoken);
            obj.put("isLoggedIn",""+MainApplication.getInstance().tokenHandler.isLoggedIn());
            Log.d("TOKENHANDLER STATE",""+obj.toString(4));
        } catch (JSONException e) {e.printStackTrace();}
    }

    /** LOGOUT STACKS */
    public void logoutGoogle(final Activity activity){
        //Logout from google
        Logg.e(TAG,"Started Google Logout Procedure");
        finalMGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        googleLogoutCallback(activity);
                        Logg.e(TAG, "Successfully logged out from google");
                        Logg.d(TAG, "restarting...");

                        //FInally kill the calling activity and restart app
                        activity.startActivity(new Intent(activity, HomeActivity.class));
                        activity.finish();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        ToastMain.showSmartToast(activity, "Error logging out","Error : google logout : onConnectionSuspended");
                    }
                })
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PROFILE))
                .build();
        finalMGoogleApiClient.connect();
    }

    private void googleLogoutCallback(Context context) {
        if (finalMGoogleApiClient != null) {
            if (finalMGoogleApiClient.isConnected()) {
                Plus.AccountApi.clearDefaultAccount(finalMGoogleApiClient);
                finalMGoogleApiClient.disconnect();
                ToastMain.showSmartToast(context, "Successfully logged out of Google");
                return;
            }
        }
        ToastMain.showSmartToast(context, "Error in logging out of google", "Error, google logout error : 301");
    }

    public void logoutFacebook(Activity activity) {
        LoginManager.getInstance().logOut();
        Logg.e(TAG, "Successfully logged out from Facebook");
        Logg.d(TAG, "restarting...");
        //FInally kill the calling activity and restart app
        activity.startActivity(new Intent(activity, HomeActivity.class));
        activity.finish();
    }
}
