package com.clozerr.app.Handlers;

import android.content.Context;
import android.os.AsyncTask;

import com.clozerr.app.Models.UserMain;
import com.clozerr.app.Storage.SharedPrefs;
import com.facebook.AccessToken;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.Scopes;

import org.json.JSONException;

import java.io.IOException;

/**
 * This contains all the User data excluding kids,
 */
public class TokenHandler {
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
    public String token;//this is clozerr service token
    public String socialtoken;//this is token of facebook/google
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


    private TokenHandler(Context context) {
        mContext = context;
        pullUserDataFromLocal();
        if(isLoggedIn()){
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
    public void pullUserDataFromLocal() {
        sPrefs = SharedPrefs.getInstance(mContext);
        try {
            authProvider = (sPrefs.userData.has("authProvider"))?sPrefs.loginData.getString("authProvider"):AUTH_NONE;
            socialtoken = (sPrefs.userData.has("socialtoken"))?sPrefs.loginData.getString("socialtoken"):"";
            token = (sPrefs.userData.has("token"))?sPrefs.loginData.getString("token"):"";
            email = (sPrefs.userData.has("email"))?sPrefs.loginData.getString("email"):"";

        } catch (JSONException e) {e.printStackTrace();}
    }
    public void saveUserDataLocally() {
        try {
            sPrefs.loginData.put("authProvider", authProvider);
            sPrefs.loginData.put("socialtoken", socialtoken);
            sPrefs.loginData.put("token", token);
            sPrefs.loginData.put("email", email);
        } catch (JSONException e) {e.printStackTrace();}
        sPrefs.saveLoginData();
    }


    /** SOME PUBLIC GET FUNCTIONS */
    public boolean hasToken(){
        return (!token.isEmpty());
    }
    public boolean isLoggedIn() {
        return (!token.isEmpty());
    }
    public boolean hasSocialToken(){
        return (!socialtoken.isEmpty());
    }
    public boolean updateToken(){
        return (!token.isEmpty());
    }

    /** SOME PUBLIC PUT FUNCTIONS */
    public void logout() {
        token = "";
        email = "";
        socialtoken = "";
        authProvider = AUTH_NONE;
        saveUserDataLocally();
    }
    private void updateLoginTokens(){
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                try {
                    if (authProvider.equals(UserMain.AUTH_GOOGLE)) {
                        token = GoogleAuthUtil.getToken(mContext, email, "oauth2:" + Scopes.PLUS_LOGIN);
                        saveUserDataLocally();
                    }else{
                        AccessToken tokenFb = AccessToken.getCurrentAccessToken();
                        token = tokenFb.getToken();
                        saveUserDataLocally();
                    }
                }catch (RuntimeException | GoogleAuthException | IOException e) {e.printStackTrace();}
                return null;
            }
        };
        task.execute((Void) null);



    }

}
