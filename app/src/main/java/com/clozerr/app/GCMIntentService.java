package com.clozerr.app;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

import static com.clozerr.app.Home.SENDER_ID;


public class GCMIntentService extends GCMBaseIntentService {

    public GCMIntentService() {
        super(SENDER_ID);
    }

    private static final String TAG = "===GCMIntentService The manual===";

    @Override
    protected void onRegistered(Context arg0, String registrationId) {
        Log.i(TAG, "Device registered: regId = " + registrationId);
      // write gcm key push code here
    }


    @Override
    protected void onUnregistered(Context arg0, String arg1) {
        Log.i(TAG, "unregistered = " + arg1);
    }


    private void notify( String title, String content ){
        Uri sound_uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        //builder.setSound(alarmSound);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon( R.drawable.ic_launcher )
                        .setContentTitle( title )
                        .setContentText( content )
                        .setSound( sound_uri );
// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, Home.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(Home.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        //int mId = 1234;
        mNotificationManager.notify(1234, mBuilder.build());
    }
    
    @SuppressWarnings("deprecation")
	@ Override
    protected void onMessage(Context context, Intent intent) {
        Log.d(TAG, "onMessage - context: " + context);
        String type = intent.getStringExtra("type");
        String message = intent.getStringExtra("message");
        String title = intent.getStringExtra("title");
        // make notifications here
        if( type.equals("STANDARD") ) {
            notify(title, message);
        }

        else if(type.equals("REVIEW")) {
            String checkin_id = intent.getStringExtra("checkin_id");
            String vendor_id = intent.getStringExtra("vendor_id");
            notifyreview( title, message, checkin_id, vendor_id );
        }

    }

    private void notifyreview(String title, String message, String checkin_id, String vendor_id) {
        Uri sound_uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        //builder.setSound(alarmSound);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon( R.drawable.ic_launcher )
                        .setContentTitle( title )
                        .setContentText( message )
                        .setSound( sound_uri )
                        .setAutoCancel( true );

// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, CouponDetails.class);
        resultIntent.putExtra("from_notify_review",true);
        resultIntent.putExtra("checkin_id",checkin_id);
        resultIntent.putExtra("vendor_id", vendor_id);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(Home.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        //int mId = 1234;
        mNotificationManager.notify(1234, mBuilder.build());
    }

    @
    Override
    protected void onError(Context arg0, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
    }

    @
    Override
    protected boolean onRecoverableError(Context context, String errorId) {
        return super.onRecoverableError(context, errorId);
    }
}