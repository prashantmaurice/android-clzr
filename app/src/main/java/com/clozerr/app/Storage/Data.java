package com.clozerr.app.Storage;

import android.content.Context;

import com.clozerr.app.Models.UserMain;

/**
 *  Instance of Data object contains all access to the complete date model underneath.
 *  Just pull the Data instance in your screen and make modifications to exposed objects
 *  underneath as you wish. Make sure you commit to changes after making changes in order
 *  to take effect in Server and LocalDb
 *
 *  @author maurice
 */


public class Data {
    private static Data instance;
    public UserMain userMain;

    private Data(Context context) {
        userMain = UserMain.getInstance(context);
    }

    //use this to retreive an instance of Data
    public static Data getInstance(Context context) {
        if(instance == null) instance = new Data(context);
        return instance;
    }

    //Refill function to generate all previous data
//    public void refillCompleteData(JSONObject response){
//        try {
//            userMain.decodeFromServer(response.getJSONObject("user"));
//            userMain.saveUserDataLocally();
//
//        } catch (JSONException e) {e.printStackTrace();}
//    };

//    public void pullDataFromServer(final String email, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) throws JSONException {
//        String url = Router.User.getWIthEmailComplete(email);
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("email",email);
//        Logg.m("MAIN", "Pulling complete data from server : " + email);
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, jsonObject, listener,errorListener);
//        MainApplication.getInstance().getRequestQueue().add(jsonObjectRequest);
//    }

    public void saveCompleteDataLocally(){
        userMain.saveUserDataLocally();
    }

}
