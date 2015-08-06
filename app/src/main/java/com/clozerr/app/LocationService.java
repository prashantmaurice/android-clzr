package com.clozerr.app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.clozerr.app.Activities.HomeScreens.HomeActivity;
import com.clozerr.app.Activities.VendorScreens.VendorActivity;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Srivatsan on 3/8/2015.
 */
public class LocationService extends Service {

    public static int ONE_SECOND = 1000;
    public static int SECONDS_PER_MINUTE = 60;
    public static int ONE_MINUTE = ONE_SECOND * SECONDS_PER_MINUTE;
    public static int NO_OF_MINUTES = 30;
    public static long INTERVAL = 5000;//NO_OF_MINUTES * ONE_MINUTE;
    public static float MIN_DISTANCE=0;
    public float RADIUS=200;
    public static int MIN_NEAR_VENDOR=5;
    // no. of providers available
    private static enum PROVIDER_STATUS { BOTH_AVAILABLE, ONE_AVAILABLE, UNAVAILABLE };
    // id numbers for notifications
    private static enum NOTIFICATION_ID { LOCATION_CHANGE, PROVIDER_ENABLED, PROVIDER_DISABLED };

    private LocationManager mLocationManager;
    //private String mProvider;
    private MyLocationListener mListener;
    //private Criteria mCriteria;
    //private PROVIDER_STATUS mProviderStatus;
    private boolean mProviderUnavailableDialogDisplayed;


    private NotificationManager mNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        // Get the location manager
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Define the criteria how to select the location provider
        //mCriteria = new Criteria();
        //mCriteria.setAccuracy(Criteria.ACCURACY_COARSE);	//default
        //mCriteria.setCostAllowed(false);

        // get the best provider depending on the criteria
        //mProvider = mLocationManager.getBestProvider(mCriteria, false);

        // initialize notification object

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        // initialize other variables to default values
        mListener = new MyLocationListener();
        //mProviderStatus = PROVIDER_STATUS.BOTH_AVAILABLE;
        mProviderUnavailableDialogDisplayed = false;

        // location updates: at least 1 unit (depending on accuracy)
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, INTERVAL, MIN_DISTANCE, mListener);

    };

    public LocationService() {
        // TODO Auto-generated constructor stub
    }

    public void setNotification(NOTIFICATION_ID id, CharSequence text, PendingIntent intent,PendingIntent pIntent)
    {
        //Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mNotificationBuilder;
        mNotificationBuilder = new NotificationCompat.Builder(getApplicationContext())

                .setContentTitle(getResources().getString(R.string.app_name))
                //.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notif_logo))
                .setSmallIcon(R.drawable.ic_notif_logo)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                //.setSound(soundUri)
                .setOnlyAlertOnce(true)
                .setColor(getResources().getColor(R.color.white))
                ;
        mNotificationBuilder.setContentText(text)
                .setContentIntent(intent)
                .setWhen(System.currentTimeMillis());
        mNotificationBuilder.addAction(R.drawable.ic_action_accept,"YES",pIntent);
        mNotificationManager.notify(id.ordinal(), mNotificationBuilder.build());
        mNotificationBuilder.setContentTitle(getResources().getString(R.string.app_name));
    }

    public void dismissNotification(NOTIFICATION_ID id)
    {
        mNotificationManager.cancel(id.ordinal());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mLocationManager.removeUpdates(mListener);
        mNotificationManager.cancelAll();
        super.onDestroy();
    };

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(final Location location) {
            Log.d("change","location");
            Log.d("accuracy",""+location.getAccuracy());
            String url ="http://api.clozerr.com/v2/vendor/search/near?latitude=" + location.getLatitude() + "&longitude=" + location.getLongitude() + "&limit=" + MIN_NEAR_VENDOR;
            new AsyncGet(LocationService.this, url , new AsyncGet.AsyncResult() {
                @Override
                public void gotResult(String s) {
                    JSONArray array;
                    try{
                        array = new JSONArray(s);
                        for(int i = 0 ; i < array.length() ; i++){
                            Log.d("locationnotif",s);
                            JSONObject jsonObject=array.getJSONObject(i);
                            Location loc=new Location("vendorLocation");
                            double lat=jsonObject.getJSONArray("location").getDouble(0);
                            double longi=jsonObject.getJSONArray("location").getDouble(1);
                            loc.setLatitude(lat);
                            loc.setLongitude(longi);
                            float d=loc.distanceTo(location)/1000;
                            try {
                                JSONObject neighbour = jsonObject.getJSONObject("settings").getJSONObject("neighbourhoodperks");

                            if(!neighbour.getBoolean("activated")){
                                continue;
                            }
                            RADIUS=neighbour.getInt("distance");
                            if(d > RADIUS){
                                continue;
                            }
                            }
                            catch (Exception e){
                                continue;
                            }
//                            SharedPreferences example = getSharedPreferences("USER", 0);
//                            SharedPreferences.Editor editor = example.edit();
//                            editor.putString("latitude", location.getLatitude()+"");
//                            editor.putString("longitude", location.getLongitude() + "");
//                            editor.apply();
                            MainApplication.getInstance().data.userMain.latitude = location.getLatitude()+"";
                            MainApplication.getInstance().data.userMain.longitude =location.getLongitude()+"";
                            MainApplication.getInstance().data.userMain.saveUserDataLocally();

                            Intent intent = new Intent(LocationService.this, VendorActivity.class);
                            intent.putExtra("vendor_id", jsonObject.getString("_id"));
                            /*JSONArray jsonArray=jsonObject.getJSONArray("offers");
                            if(jsonArray.length()==0){
                                intent.putExtra("offer_caption","No offers available");
                            }
                            else {
                                intent.putExtra("offer_id", jsonArray.getJSONObject(0).getString("_id"));
                                intent.putExtra("offer_caption", jsonArray.getJSONObject(0).getString("caption"));
                                intent.putExtra("offer_text", jsonArray.getJSONObject(0).getString("description"));
                            }*/
                            intent.putExtra("Notification",true);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP |
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            PendingIntent pIntent = PendingIntent.getActivity(LocationService.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                            Intent intent1 = new Intent(LocationService.this, HomeActivity.class);
                            PendingIntent pIntent1 = PendingIntent.getActivity(LocationService.this, 0, intent1, 0);
                            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            if(sharedPref.getBoolean("nbd_detect", true)) {
                                Log.d("Preferences", "read");
                                setNotification(NOTIFICATION_ID.LOCATION_CHANGE, jsonObject.getString("name") + " is near you. Would you like to Checkin? ", pIntent1,pIntent);
                            }
                        }
                    }catch (Exception e){
                        Log.d("JSON","exception");
                    }
                }
            });
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

            //if (mProviderStatus == PROVIDER_STATUS.UNAVAILABLE)
              //  mProviderStatus = PROVIDER_STATUS.ONE_AVAILABLE;
            //else if (mProviderStatus == PROVIDER_STATUS.ONE_AVAILABLE)
              //  mProviderStatus = PROVIDER_STATUS.BOTH_AVAILABLE;

            if (mProviderUnavailableDialogDisplayed == true)
                mProviderUnavailableDialogDisplayed = false;
        }

        @Override
        public void onProviderDisabled(String provider) {
            //if (mProviderStatus != PROVIDER_STATUS.UNAVAILABLE)
            {

                //if (mProvider == LocationManager.NETWORK_PROVIDER)
                  //  mProvider = LocationManager.GPS_PROVIDER;
                //else mProvider = LocationManager.NETWORK_PROVIDER;

//                if (mProviderStatus == PROVIDER_STATUS.BOTH_AVAILABLE)
  //                  mProviderStatus = PROVIDER_STATUS.ONE_AVAILABLE;
    //            else mProviderStatus = PROVIDER_STATUS.UNAVAILABLE;

      //          mLocationManager.requestLocationUpdates(mProvider, INTERVAL, MIN_DISTANCE, this);
            }



        }
    }

}