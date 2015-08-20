package com.clozerr.app.Models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * This is used in VendorActivity to parse vendor details
 *
 *
 *
 * general object :
 * {
     visitOfferId: "55adea98c626a0bfb3a08c6d",
     _id: "55c31e7fbd510fdc2d863db3",
     location: [
         13.0509259,
         80.21205439999994
     ],
     name: "The Pasta Bar Veneto",
     phone: "8939990001",
     image: "http://s3-ap-southeast-1.amazonaws.com/clozerr/app/coupons-alpha/",
     fid: "9927281564987162",
     date_created: "2015-08-06T08:44:47.561Z",
     dateUpdated: "2015-08-06T10:41:12.708Z",
     settings: {
         sxEnabled: "false",
         billAmt: "100",
         birthday: {
             activated: "false",
             birthdayWish: "Happy birthday",
             notifyFirst: "false",
             notifyExact: "true"
         },
         neighbourhoodperks: {
             distance: "1",
             activated: "false",
             message: "Free coffee.."
         },
         visitreminder: {
             activated: "false",
             days: "7",
             visitMessage: "Get a Coffee free!"
         },
         policy: "One stamp for every visit",
         viewState: {
            active: true
         }
     },
     resource_name: "55c31e7fbd510fdc2d863db3",
     image_base: "https://s3-ap-southeast-1.amazonaws.com/clozerr/app/coupons-alpha/",
     __v: 10,
     address: "1st Floor, Shop No. 113, No:183, The Vijaya Forum Mall, N S K, Salai, Arcot Road, Green Park Private Entrance, Vadapalani, The Forum Vijaya Mall, Chennai, Tamil Nadu 600026",
     category: "Food",
     beacons: {
         minor: "15188",
         major: "48658",
         message: "Welcome to The Pasta Bar Veneto!",
         title: "The Pasta Bar Veneto"
     },
     description: "The Pasta Bar Veneto serves delectable Italian cuisine at unlike Italian prices. Passionately made from real ingredients, the dishes are indeed a treat to your taste buds. Dine in to discover the difference.",
     city: "undefined",
     visible: true,
     geofences: [ ],
     tags: [ ],
     gallery: [ ],
     qrcodes: [ ],
     flags: [ ],
     question: [
     "Food",
     "Ambience",
     "Quality of Service"
     ],
     offers_old: [
         "55c31e81bd510fdc2d863db9",
         "55c3360dbd510fdc2d863e01"
     ],
     offers: [
         "55c31e81bd510fdc2d863db6",
         "55c31e81bd510fdc2d863db7",
         "55c31e81bd510fdc2d863db8",
         "55c31e81bd510fdc2d863dba",
         "55c31fd3bd510fdc2d863dbf",
         "55c31feabd510fdc2d863dc0"
     ]
 }
 */
public class VendorDetailsObject {

    //variables
    public String type;
    public String caption;
    public String description;
    public String image,imageBase,resourceName;
    public boolean unlocked;
    public String rewardId;
    public String vendorId;
    public String vendorLogoUrl;
    public String name;
    public String lat, longg;


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
    public String phone;
    private String fid;
    public String address;
    private String category;
    private String city;
    private boolean visible;


    private VendorDetailsObject() {}


    //SERVER ENCODERS
    /** Called when starting the app to fill data at start */
    public static VendorDetailsObject decodeFromServer(JSONObject obj){
        VendorDetailsObject vendor = new VendorDetailsObject();
        try {
            vendor.vendorId = (obj.has("_id"))?obj.getString("_id"):"";
            vendor.name = (obj.has("name"))?obj.getString("name"):"";
            vendor.phone = (obj.has("phone"))?obj.getString("phone"):"";
            vendor.image = (obj.has("image"))?obj.getString("image"):"";
            vendor.imageBase = (obj.has("image_base"))?obj.getString("image_base"):"";
            vendor.resourceName = (obj.has("resource_name"))?obj.getString("resource_name"):"";
            vendor.fid = (obj.has("fid"))?obj.getString("fid"):"";
            vendor.address = (obj.has("address"))?obj.getString("address"):"";
            vendor.category = (obj.has("category"))?obj.getString("category"):"";
            vendor.description = (obj.has("description"))?obj.getString("description"):"";
            vendor.city = (obj.has("city"))?obj.getString("city"):"";
            vendor.visible = (obj.has("visible"))?obj.getBoolean("visible") :false;


            //TODO:sai : catch variables above and use them here to support Logo
            //new API variables that are yest to be added
            vendor.vendorLogoUrl = (obj.has("vendorLogoUrl"))?obj.getString("vendorLogoUrl"):"";


            if(obj.has("location")){
                JSONArray location = obj.getJSONArray("location");
                vendor.lat = location.getString(0);
                vendor.longg = location.getString(1);
            }

            //TODO : add rest of the data in this object

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return vendor;
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


    public String getImageUrl() {
        try {
            return imageBase+ URLEncoder.encode(resourceName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }
    public String getLogoImageUrl() {
        return vendorLogoUrl;
    }

}
