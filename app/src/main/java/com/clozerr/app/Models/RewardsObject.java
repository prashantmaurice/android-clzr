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
public class RewardsObject {

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
    public static final String TYPE_STAMPS= "S1";

    //runtime variables
    public String vendorId;


    public RewardsObject() {}


    //SERVER ENCODERS
    /** Called when starting the app to fill data at start */
    public static RewardsObject decodeFromServer(JSONObject obj){
        RewardsObject reward = new RewardsObject();
        try {
            JSONObject params = obj.getJSONObject("params");

            reward.rewardId = (obj.has("_id"))?obj.getString("_id"):"";
            reward.type = (obj.has("type"))?obj.getString("type"):"";
            reward.caption = (obj.has("caption"))?obj.getString("caption"):"";
            reward.description = (obj.has("description"))?obj.getString("description"):"";
            reward.image = (obj.has("image"))?obj.getString("image"):"";
            reward.unlocked = (obj.has("unlocked"))?obj.getBoolean("unlocked"):true;

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
            }else if(reward.type.equals(TYPE_STAMPS)){
                reward.unlocked = (params.has("unlocked"))?params.getBoolean("unlocked"):true;
                reward.used = (params.has("used"))?params.getBoolean("used"):true;
                reward.stamps = (params.has("stamps"))?params.getInt("stamps"):0;
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return reward;
    }

    public static ArrayList<RewardsObject> decodeFromServer(JSONArray arr){
        ArrayList<RewardsObject> arrayList = new ArrayList<>();
        for(int i=0;i<arr.length();i++){
            try {
                arrayList.add(decodeFromServer(arr.getJSONObject(i)));
            } catch (JSONException e) {e.printStackTrace();}
        }
        return arrayList;
    }

    public boolean getVisitedstatus() {
        return used;
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
