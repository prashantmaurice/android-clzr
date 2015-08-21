package com.clozerr.app.Models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * This contains all the User data excluding kids,
 *
 *
 *
 * general object :
 * {
     _id: "55cdcaf5ef8e165055027d57",
     name: "Habanero",
     location: [
     12.9914032,
     80.21649409999998
     ],
     distance: 280.484,
     image: "http://s3-ap-southeast-1.amazonaws.com/clozerr/app/coupons-alpha/",
     image_base: "https://s3-ap-southeast-1.amazonaws.com/clozerr/app/coupons-alpha/",
     gallery: [ ],
     address: "#F47, 1st floor, Phoenix Market City, Velachery, Chennai, Tamil Nadu 600042",
     resource_name: "55cdcaf5ef8e165055027d57",
     caption: "Coming Soon...",
     active: false,
     favourite: false
 }

 TODO : Send direct restaurent image Url instead of resource name and base
 */
public class NearbyRestaurentObject {

    //variables
    public String restaurentId;
    public String name;
    public String image;
    public String resourceName;
    public String imageBase;
    public String caption;
    public String address;
    public boolean active;
    public boolean favourite;
    private int title;

    public NearbyRestaurentObject() {}


    //SERVER ENCODERS
    /** Called when starting the app to fill data at start */
    public static NearbyRestaurentObject decodeFromServer(JSONObject obj){
        NearbyRestaurentObject reward = new NearbyRestaurentObject();
        try {
            reward.restaurentId = (obj.has("_id"))?obj.getString("_id"):"";
            reward.name = (obj.has("name"))?obj.getString("name"):"";
            reward.caption = (obj.has("caption"))?obj.getString("caption"):"";
            reward.address = (obj.has("address"))?obj.getString("address"):"";
            reward.image = (obj.has("image"))?obj.getString("image"):"";
            reward.resourceName = (obj.has("resource_name"))?obj.getString("resource_name"):"";
            reward.imageBase = (obj.has("image_base"))?obj.getString("image_base"):"";
            reward.active = (obj.has("active"))?obj.getBoolean("active"):true;
            reward.favourite = (obj.has("favourite"))?obj.getBoolean("favourite"):true;


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return reward;
    }

    public static ArrayList<NearbyRestaurentObject> decodeFromServer(JSONArray arr){
        ArrayList<NearbyRestaurentObject> arrayList = new ArrayList<>();
        for(int i=0;i<arr.length();i++){
            try {
                arrayList.add(decodeFromServer(arr.getJSONObject(i)));
            } catch (JSONException e) {e.printStackTrace();}
        }
        return arrayList;
    }


    public String getTitle() {
        return name;
    }
    public String getCaption() {
        return "";//Not yet implemented yet
    }
    public String getDistanceString() {
        return caption;
    }
    public boolean isFavourite() {
        return favourite;
    }
    public String getImageUrl() {
        try {
            return imageBase+URLEncoder.encode(resourceName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }
    public String getVendorId() {
        return restaurentId;
    }
    public boolean isActive() {
        return active;
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
