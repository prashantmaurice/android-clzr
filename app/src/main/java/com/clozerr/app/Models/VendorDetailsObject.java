package com.clozerr.app.Models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * This contains all the User data excluding kids,
 *
 *
 *
 * general object :
 * {
     _id: "55c31feabd510fdc2d863dc0",
     type: "S0",
     caption: "Happy Hour Reward",
     description: "Buy 2 get 1 Pizza free",
     image: "https://s3-ap-southeast-1.amazonaws.com/clozerr/app/general/icons/happy+hour.png",
     params: {
         type: "happyHour",
         startHour: "16",
         endHour: "19",
         days: [
             "1",
             "2",
             "3",
             "4",
             "5"
         ]
     },
     unlocked: false
   }
 */
public class VendorDetailsObject {

    //variables
    public String type;
    public String caption;
    public String description;
    public String image;
    public boolean unlocked;
    public String rewardId;

    //param variables
        //type = loyalty
        public boolean used;
        public int stamps;

        //type = loyalty
        public int startHour;
        public int endHour;
        public ArrayList<Integer> days = new ArrayList<>();


    //Constants
    public static final String TYPE_LOYALTY = "loyalty";
    public static final String TYPE_HAPPYHOUR = "happyHour";

    //runtime variables
    public String vendorId;
    private String name;
    private String phone;
    private String fid;
    private String address;
    private String category;
    private String city;
    private boolean visible;


    private VendorDetailsObject() {}


    //SERVER ENCODERS
    /** Called when starting the app to fill data at start */
    public static VendorDetailsObject decodeFromServer(JSONObject obj){
        VendorDetailsObject reward = new VendorDetailsObject();
        try {
            JSONObject params = obj.getJSONObject("params");


            reward.name = (obj.has("name"))?obj.getString("name"):"";
            reward.phone = (obj.has("phone"))?obj.getString("phone"):"";
            reward.image = (obj.has("image"))?obj.getString("image"):"";
            reward.fid = (obj.has("fid"))?obj.getString("fid"):"";
            reward.address = (obj.has("address"))?obj.getString("address"):"";
            reward.category = (obj.has("category"))?obj.getString("category"):"";
            reward.description = (obj.has("description"))?obj.getString("description"):"";
            reward.city = (obj.has("city"))?obj.getString("city"):"";
            reward.visible = (obj.has("visible"))?obj.getBoolean("visible") :false;


            reward.type = (obj.has("type"))?obj.getString("type") :"";
            reward.caption = (obj.has("caption"))?obj.getString("caption") :"";
            reward.description = (obj.has("description"))?obj.getString("description") :"";

            reward.type = (params.has("type"))?params.getString("type"):TYPE_LOYALTY;
            if(reward.type.equals(TYPE_LOYALTY)){
                reward.used = (params.has("used"))?params.getBoolean("used"):true;
                reward.stamps = (params.has("stamps"))?params.getInt("stamps"):0;
            }else if(reward.type.equals(TYPE_HAPPYHOUR)){
                reward.startHour = (params.has("startHour"))?Integer.parseInt(params.getString("startHour")):0;
                reward.endHour = (params.has("endHour"))?Integer.parseInt(params.getString("endHour")):23;
                JSONArray arr = params.getJSONArray("days");
                for(int i=0;i<arr.length();i++){
                    reward.days.add(Integer.parseInt(arr.getString(i)));
                }

            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return reward;
    }

    public static ArrayList<VendorDetailsObject> decodeFromServer(JSONArray arr){
        ArrayList<VendorDetailsObject> arrayList = new ArrayList<>();
        for(int i=0;i<arr.length();i++){
            try {
                arrayList.add(decodeFromServer(arr.getJSONObject(i)));
            } catch (JSONException e) {e.printStackTrace();}
        }
        return arrayList;
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
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return jsonObject;
//    }


}
