package com.clozerr.app.Models;

import android.content.Context;

import com.clozerr.app.Storage.SharedPrefs;

import org.json.JSONException;

/**
 * This contains all the User data excluding kids,
 */
public class UserMain {
    private Context mContext;
    private static UserMain instance;
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


    private UserMain(Context context) {
        mContext = context;
        pullUserDataFromLocal();
    }
    public static UserMain getInstance(Context context) {
        if(instance == null) {
            instance = new UserMain(context);
        }
        return instance;
    }


    //LOCAL STORAGE ENCODERS
    public void pullUserDataFromLocal() {
        sPrefs = SharedPrefs.getInstance(mContext);
        try {
            gplus_name = (sPrefs.userData.has("gplus_name"))?sPrefs.userData.getString("gplus_name"):"";
            gplus_id = (sPrefs.userData.has("gplus_id"))?sPrefs.userData.getString("gplus_id"):"";
            gplus_pic = (sPrefs.userData.has("gplus_pic"))?sPrefs.userData.getString("gplus_pic"):"";
            home_cards = (sPrefs.userData.has("home_cards"))?sPrefs.userData.getString("home_cards"):"";
            categories_cards = (sPrefs.userData.has("categories_cards"))?sPrefs.userData.getString("categories_cards"):"";
            latitude = (sPrefs.userData.has("latitude"))?sPrefs.userData.getString("latitude"):"";
            longitude = (sPrefs.userData.has("longitude"))?sPrefs.userData.getString("longitude"):"";

            authProvider = (sPrefs.userData.has("authProvider"))?sPrefs.userData.getString("authProvider"):AUTH_NONE;
            fb_name = (sPrefs.userData.has("fb_name"))?sPrefs.userData.getString("fb_name"):"";
            user = (sPrefs.userData.has("user"))?sPrefs.userData.getString("user"):"";
            name = (sPrefs.userData.has("name"))?sPrefs.userData.getString("name"):"";
            phone = (sPrefs.userData.has("phone"))?sPrefs.userData.getString("phone"):"";
            email = (sPrefs.userData.has("email"))?sPrefs.userData.getString("email"):"";
            gcmId = (sPrefs.userData.has("gcmId"))?sPrefs.userData.getString("gcmId"):"";
            userId = (sPrefs.userData.has("userId"))?sPrefs.userData.getString("userId"):"";
            address = (sPrefs.userData.has("address"))?sPrefs.userData.getString("address"):"";
            imageUrl = (sPrefs.userData.has("imageUrl"))?sPrefs.userData.getString("imageUrl"):"";
            coverPic= (sPrefs.userData.has("coverPic"))?sPrefs.userData.getString("coverPic"):"";
            facebookId = (sPrefs.userData.has("facebookId"))?sPrefs.userData.getString("facebookId"):"";
            isMale = (sPrefs.userData.has("isMale"))?sPrefs.userData.getBoolean("isMale"):false;
            loginSkip = (sPrefs.userData.has("loginSkip"))?sPrefs.userData.getBoolean("loginSkip"):false;
            notNow = (sPrefs.userData.has("notNow"))?sPrefs.userData.getBoolean("notNow"):false;

        } catch (JSONException e) {e.printStackTrace();}
    }
    public void saveUserDataLocally() {
        try {
            sPrefs.userData.put("fb_name", fb_name);
            sPrefs.userData.put("authProvider", authProvider);
            sPrefs.userData.put("facebookId", facebookId);
            sPrefs.userData.put("home_cards", home_cards);
            sPrefs.userData.put("categories_cards", categories_cards);

            sPrefs.userData.put("latitude", latitude);
            sPrefs.userData.put("longitude", longitude);
            sPrefs.userData.put("loginSkip", loginSkip);
            sPrefs.userData.put("imageUrl", imageUrl);
            sPrefs.userData.put("coverPic", coverPic);
            sPrefs.userData.put("address", address);
            sPrefs.userData.put("userId", userId);
            sPrefs.userData.put("email", email);
            sPrefs.userData.put("phone", phone);
            sPrefs.userData.put("name", name);
            sPrefs.userData.put("user", user);
            sPrefs.userData.put("gcmId", gcmId);
            sPrefs.userData.put("isMale", isMale);
            sPrefs.userData.put("notNow", notNow);

            sPrefs.userData.put("gplus_name", gplus_name);
            sPrefs.userData.put("gplus_id", gplus_id);
            sPrefs.userData.put("gplus_pic", gplus_pic);
        } catch (JSONException e) {e.printStackTrace();}
        sPrefs.saveUserData();
    }

//    public void saveUserImagesLocally() {
//        try {
//            sPrefs.userData.put("imageUrl", imageUrl);
//            sPrefs.userData.put("coverPic", coverPic);
//        } catch (JSONException e) {e.printStackTrace();}
//        sPrefs.saveUserData();
//    }

    //SERVER ENCODERS
    /** Called when starting the app to fill data at start */
//    public void decodeFromServer(JSONObject obj){
//        try {
//            name = (obj.has("name"))?obj.getString("name"):"";
//            phone = (obj.has("phone"))?obj.getString("phone"):"";
//            email = (obj.has("email"))?obj.getString("email"):"";
//            userId = (obj.has("userId"))?obj.getString("userId"):"";
//            address = (obj.has("address"))?obj.getString("address"):"";
//            imageUrl = (obj.has("profilepic"))?obj.getString("profilepic"):"";//i think panse changed this to 'profilepic' in API
//            coverPic = (obj.has("coverpic"))?obj.getString("coverpic"):"";
//            facebookId = (obj.has("facebookId")) ? obj.getString("facebookId"):"";
//            gcmId = (obj.has("gcmId"))?obj.getString("gcmId") : "";
//            isMale = (obj.has("isMale"))?obj.getBoolean("isMale"):false;
//
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//    public JSONObject encodeForServer(){
//        JSONObject jsonObject = new JSONObject();
//        try {
//            if (facebookId != null && !facebookId.isEmpty()) jsonObject.put("facebookId",facebookId);
//            if (imageUrl != null && !imageUrl.isEmpty()) jsonObject.put("profilepic",imageUrl);
//            if (coverPic != null && !coverPic.isEmpty()) jsonObject.put("coverpic",coverPic);
//            if (address != null && !address.isEmpty()) jsonObject.put("address",address);
//            if (userId != null && !userId.isEmpty()) jsonObject.put("userId",userId);
//            if (phone != null && !phone.isEmpty()) jsonObject.put("phone",phone);
//            if (email != null && !email.isEmpty()) jsonObject.put("email",email);
//            if (gcmId != null && !gcmId.isEmpty()) jsonObject.put("gcmId", gcmId);
//            if (name != null && !name.isEmpty()) jsonObject.put("name",name);
//            if (isMale != null) jsonObject.put("isMale", isMale);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return jsonObject;
//    }


    /** This pushes and updates user object in server */
//    public void serverSyncPush(){
//        String url = Router.User.sync();
//        JSONObject jsonObject = encodeForServer();
//        Log.e("MAIN","Sending  serverSyncPush");
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                Logg.m("MAIN", "Successfully synced user with server : "+response.toString());
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.d("ERROR", "Failed to sync user with server : "+error.getLocalizedMessage());
//            }
//        });
//        MainApplication.getInstance().getRequestQueue().add(jsonObjectRequest);
//    }


    /** SOME PUBLIC PUT FUNCTIONS */
    public void changeUser(String userString){
        user = userString;
        saveUserDataLocally();
    }
    public void changeNotNow(boolean notnow){
        this.notNow = notnow;
        saveUserDataLocally();
    }

    public void changeCategoryCards(String s) {
        categories_cards = s;
        saveUserDataLocally();
    }
    public void changeHomeCards(String s) {
        categories_cards = s;
        saveUserDataLocally();
    }

}
