package com.clozerr.app.Models;

import android.content.Context;

import com.clozerr.app.Storage.SharedPrefs;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *  This is a basic model for the data being sent in Users Data get call
 */
public class UserMainLive {
    private Context mContext;
    private static UserMainLive instance;
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


    private UserMainLive() {
//        mContext = context;
//        pullUserDataFromServer();
    }

    private void pullUserDataFromServer() {

    }

//    public static UserMainLive getInstance(Context context) {
//        if(instance == null) {
//            instance = new UserMainLive(context);
//        }
//        return instance;
//    }


    //LOCAL STORAGE ENCODERS

//    public void saveUserImagesLocally() {
//        try {
//            sPrefs.userData.put("imageUrl", imageUrl);
//            sPrefs.userData.put("coverPic", coverPic);
//        } catch (JSONException e) {e.printStackTrace();}
//        sPrefs.saveUserData();
//    }

    //SERVER ENCODERS
    /** Called when starting the app to fill data at start */
    public static UserMainLive decodeFromServer(JSONObject obj){
        UserMainLive user = new UserMainLive();
        try {
            JSONObject profile = obj.getJSONObject("profile");


            user.name = (profile.has("name"))?profile.getString("name"):"";
            user.isMale = (profile.has("gender")) && profile.getString("gender").equals("male");
            user.imageUrl = (profile.has("picture"))?profile.getString("picture"):"";


//            user.coverPic = (obj.has("coverpic"))?obj.getString("coverpic"):"";
//            user.phone = (obj.has("phone"))?obj.getString("phone"):"";
//            user.email = (obj.has("email"))?obj.getString("email"):"";
//            user.userId = (obj.has("userId"))?obj.getString("userId"):"";
//            user.address = (obj.has("address"))?obj.getString("address"):"";
//            user.facebookId = (obj.has("facebookId")) ? obj.getString("facebookId"):"";
//            user.gcmId = (obj.has("gcmId"))?obj.getString("gcmId") : "";


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return user;
    }
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
//    public void changeUser(String userString){
//        user = userString;
//        saveUserDataLocally();
//    }
//    public void changeNotNow(boolean notnow){
//        this.notNow = notnow;
//        saveUserDataLocally();
//    }
//
//    public void changeCategoryCards(String s) {
//        categories_cards = s;
//        saveUserDataLocally();
//    }
//    public void changeHomeCards(String s) {
//        categories_cards = s;
//        saveUserDataLocally();
//    }

}
