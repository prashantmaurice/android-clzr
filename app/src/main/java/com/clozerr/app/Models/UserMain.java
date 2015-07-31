package com.clozerr.app.Models;

import android.content.Context;

import com.clozerr.app.Storage.SharedPrefs;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This contains all the User data excluding kids,
 */
public class UserMain {
    private Context mContext;
    private static UserMain instance;
    private SharedPrefs sPrefs;
    public String name;
    public String userId;
    public String email;
    public String imageUrl;
    public String gcmId;
    public String facebookId;
    public String phone;
    public String address;
    public String coverPic;
    public Boolean isMale = false;


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

        } catch (JSONException e) {e.printStackTrace();}
    }
    public void saveUserDataLocally() {
        try {
            sPrefs.userData.put("facebookId", facebookId);
            sPrefs.userData.put("imageUrl", imageUrl);
            sPrefs.userData.put("coverPic", coverPic);
            sPrefs.userData.put("address", address);
            sPrefs.userData.put("userId", userId);
            sPrefs.userData.put("email", email);
            sPrefs.userData.put("phone", phone);
            sPrefs.userData.put("name", name);
            sPrefs.userData.put("gcmId", gcmId);
            sPrefs.userData.put("isMale", isMale);
        } catch (JSONException e) {e.printStackTrace();}
        sPrefs.saveUserData();
    }

    public void saveUserImagesLocally() {
        try {
            sPrefs.userData.put("imageUrl", imageUrl);
            sPrefs.userData.put("coverPic", coverPic);
        } catch (JSONException e) {e.printStackTrace();}
        sPrefs.saveUserData();
    }

    //SERVER ENCODERS
    /** Called when starting the app to fill data at start */
    public void decodeFromServer(JSONObject obj){
        try {
            name = (obj.has("name"))?obj.getString("name"):"";
            phone = (obj.has("phone"))?obj.getString("phone"):"";
            email = (obj.has("email"))?obj.getString("email"):"";
            userId = (obj.has("userId"))?obj.getString("userId"):"";
            address = (obj.has("address"))?obj.getString("address"):"";
            imageUrl = (obj.has("profilepic"))?obj.getString("profilepic"):"";//i think panse changed this to 'profilepic' in API
            coverPic = (obj.has("coverpic"))?obj.getString("coverpic"):"";
            facebookId = (obj.has("facebookId")) ? obj.getString("facebookId"):"";
            gcmId = (obj.has("gcmId"))?obj.getString("gcmId") : "";
            isMale = (obj.has("isMale"))?obj.getBoolean("isMale"):false;


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public JSONObject encodeForServer(){
        JSONObject jsonObject = new JSONObject();
        try {
            if (facebookId != null && !facebookId.isEmpty()) jsonObject.put("facebookId",facebookId);
            if (imageUrl != null && !imageUrl.isEmpty()) jsonObject.put("profilepic",imageUrl);
            if (coverPic != null && !coverPic.isEmpty()) jsonObject.put("coverpic",coverPic);
            if (address != null && !address.isEmpty()) jsonObject.put("address",address);
            if (userId != null && !userId.isEmpty()) jsonObject.put("userId",userId);
            if (phone != null && !phone.isEmpty()) jsonObject.put("phone",phone);
            if (email != null && !email.isEmpty()) jsonObject.put("email",email);
            if (gcmId != null && !gcmId.isEmpty()) jsonObject.put("gcmId", gcmId);
            if (name != null && !name.isEmpty()) jsonObject.put("name",name);
            if (isMale != null) jsonObject.put("isMale", isMale);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


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

}
