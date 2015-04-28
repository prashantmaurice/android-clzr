package com.clozerr.app;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.util.Predicate;
import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.Region;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@TargetApi(18)
public class PeriodicBFS extends BeaconFinderService {

    private static final String TAG = "PBFS";
    private static final String ACTION_REMOVE_VENDOR = "RemoveVendor";
    private static final String ACTION_FIRE_ALARM_SCAN = "FireAlarmScan";

    private static final long INTERVAL = TimeUnit.MILLISECONDS.convert(20L, TimeUnit.SECONDS);
                                    // TODO make this 10 min
    private static final long SCAN_PERIOD = TimeUnit.MILLISECONDS.convert(6L, TimeUnit.SECONDS);
                                    // TODO modify as required
    private static enum RequestCodes {
        CODE_ALARM_INTENT(1000),
        CODE_DETAILS_INTENT(1234),
        CODE_REFUSE_INTENT(1235),
        CODE_VENDOR_LIST_INTENT(1236);

        private int mCode;

        private RequestCodes(int code) { mCode = code; }
        public int code() { return mCode; }
    }

    private static final int PERIODIC_SCAN_BEACON_LIMIT = 1;
    private static final int NOTIFICATION_ID = 0;
    private static final ConcurrentHashMap<String, DeviceParams> periodicScanDeviceMap = new ConcurrentHashMap<>();
    private static NotificationCompat.Builder notificationBuilder;
    private static NotificationManager notificationManager;
    private static WakeLockManager wakeLockManager;
    private static AlarmManager alarmManager;
    private static ScanStarter scanStarter;
    private static boolean running = false;

    public static final String MAP_CONTENTS_FILE_NAME = "mapContents.txt";

    //private Timer mTimer;
    //private BeaconCheckTask mCheckTask;

    @Override
    public void onCreate() {
        super.onCreate();
        readHashMapFromFile();

        //mTimer = new Timer();
        //mCheckTask = new BeaconCheckTask();

        wakeLockManager = new WakeLockManager();
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        scanStarter = new ScanStarter();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = getDefaultNotificationBuilder(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        Context applicationContext = getApplicationContext();
        dismissNotifications(applicationContext);
        running = false;
        //mCheckTask.stopScanning();
        //mTimer.cancel();
        alarmManager.cancel(getScanStarterPendingIntent(applicationContext));
        disableScanStarter(applicationContext);
        if (!isScanningAllowed) {               // if this was because scanning was disallowed by settings
            periodicScanDeviceMap.clear();
            writeHashMapToFile();               // next time scan starts, start with an empty hash map
        }
        wakeLockManager.releaseWakeLock();
        super.onDestroy();
    }

    private static void enableScanStarter(Context context) {
        ComponentName receiver = new ComponentName(context, ScanStarter.class);
        context.getPackageManager().setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    private static void disableScanStarter(Context context) {
        ComponentName receiver = new ComponentName(context, ScanStarter.class);
        context.getPackageManager().setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    private static PendingIntent getScanStarterPendingIntent(Context context) {
        enableScanStarter(context);
        Intent intentToSend = new Intent(context, ScanStarter.class);
        intentToSend.setAction(ACTION_FIRE_ALARM_SCAN);
        return PendingIntent.getBroadcast(context, RequestCodes.CODE_ALARM_INTENT.code(), intentToSend,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private void readHashMapFromFile() {
        try {
            FileOutputStream dummyOutputStream = openFileOutput(MAP_CONTENTS_FILE_NAME, MODE_APPEND);
                                                // create file if not created yet
            dummyOutputStream.close();
            FileInputStream fileInputStream = openFileInput(MAP_CONTENTS_FILE_NAME);
            byte[] dataBytes = new byte[fileInputStream.available()];
            fileInputStream.read(dataBytes);
            fileInputStream.close();
            String data = new String(dataBytes);
            if (!data.isEmpty()) {
                JSONObject rootObject = new JSONObject(data);
                JSONObject hashMapObject = rootObject.getJSONObject("hashMap");
                for (Iterator<String> iterator = hashMapObject.keys(); iterator.hasNext(); ) {
                    String uuid = iterator.next();
                    DeviceParams params = new DeviceParams(hashMapObject.getInt(uuid), false);
                    periodicScanDeviceMap.put(uuid, params);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeHashMapToFile() {
        try {
            FileOutputStream fileOutputStream = openFileOutput(MAP_CONTENTS_FILE_NAME, Context.MODE_PRIVATE);
            JSONObject hashMapObject = new JSONObject("{}");
            for (String uuid : periodicScanDeviceMap.keySet())
                hashMapObject.put(uuid, periodicScanDeviceMap.get(uuid).mCount);
            JSONObject rootObject = new JSONObject("{}");
            rootObject.put("hashMap", hashMapObject);
            String fileData = rootObject.toString();
            fileOutputStream.write(fileData.getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static NotificationCompat.Builder getDefaultNotificationBuilder(Context context) {
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notif_logo)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSound(soundUri)
                .setAutoCancel(true);
    }

    private static void showNotificationForVendor(Context context, String uuid)
    {
        Log.e(TAG, "UUID for notification - " + uuid);
        dismissNotifications(context);
        try {
            ArrayList<VendorParams> rootArray = VendorParams.readVendorParamsFromFile(context);
            for (final VendorParams vendorParams : rootArray) {
                if (areUuidsEqual(vendorParams.mUUID, uuid)) {
                    String title = "Clozerr - " + vendorParams.mName;
                    Log.e(TAG, "vendor - " + vendorParams.mName);
                    final String contentText = vendorParams.mNextOfferCaption;

                    Intent detailIntent = vendorParams.getDetailsIntent(context);
                    detailIntent.putExtra("from_periodic_scan", true);
                    PendingIntent detailPendingIntent = PendingIntent.getActivity(context,
                            RequestCodes.CODE_DETAILS_INTENT.code(),
                            detailIntent, PendingIntent.FLAG_ONE_SHOT);

                    Intent refuseIntent = new Intent(ACTION_REMOVE_VENDOR);
                    PendingIntent refusePendingIntent = PendingIntent.getBroadcast(context,
                            RequestCodes.CODE_REFUSE_INTENT.code(),
                            refuseIntent, PendingIntent.FLAG_ONE_SHOT);
                    context.registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            if (intent.getAction().equals(ACTION_REMOVE_VENDOR)) {
                                turnOffNotificationsForVendor(context, vendorParams);
                                notificationManager.cancel(NOTIFICATION_ID);
                                context.unregisterReceiver(this);
                            }
                        }
                    }, new IntentFilter(ACTION_REMOVE_VENDOR));

                    notificationBuilder = getDefaultNotificationBuilder(context);
                    notificationBuilder.setContentTitle(title)
                            .setTicker(title)
                            .setContentText(contentText)
                            //.setContentIntent(detailPendingIntent)
                            .setWhen(System.currentTimeMillis())
                            .addAction(R.drawable.ic_action_accept, "Check in", detailPendingIntent)
                            .addAction(R.drawable.ic_refuse, "Turn off for this vendor", refusePendingIntent);
                    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showNotifications(Context context) {
        dismissNotifications(context);
        ArrayList<String> uuidList = new ArrayList<>();
        for (String uuid : periodicScanDeviceMap.keySet())
            if (periodicScanDeviceMap.get(uuid).mToBeNotified)
                uuidList.add(uuid);
        if (uuidList.size() > 0) {
            Log.e(TAG, "Setting all notifications");
            if (uuidList.size() == 1)
                showNotificationForVendor(context, uuidList.get(0));
            else
                showNotificationForVendorList(context, uuidList);
        }
    }

    private static void showNotificationForVendorList(Context context, ArrayList<String> uuids) {
        Log.e(TAG, "Multiple vendors, size " + uuids.size());
        dismissNotifications(context);
        try {
            String title = uuids.size() + " restaurants near you";
            String contentText = "Tap to view offers and/or check in.";
            Intent vendorListIntent = new Intent(context, VendorListActivity.class);
            vendorListIntent.putStringArrayListExtra("uuidList", uuids);
            PendingIntent vendorListPendingIntent = PendingIntent.getActivity(context,
                    RequestCodes.CODE_VENDOR_LIST_INTENT.code(),
                    vendorListIntent, PendingIntent.FLAG_ONE_SHOT);
            notificationBuilder = getDefaultNotificationBuilder(context);
            notificationBuilder.setContentTitle(title)
                    .setTicker(title)
                    .setContentText(contentText)
                    .setContentIntent(vendorListPendingIntent)
                    .setWhen(System.currentTimeMillis());
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void dismissNotifications(Context context) {
        notificationManager.cancelAll();
        notificationBuilder = getDefaultNotificationBuilder(context);
    }

    public static void turnOffNotificationsForVendor(Context context, VendorParams vendorParams) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(vendorParams.mUUID, "don't notify");   // dummy string value
        editor.apply();
        vendorParams.mIsNotifiable = false;
        putToast(context, "Turned OFF beacon notifications for " + vendorParams.mName,
                Toast.LENGTH_LONG);
        if (!areAnyVendorsNotifiable(context))
            BeaconFinderService.disallowScanning(context);
    }

    public static void turnOnNotificationsForVendor(Context context, VendorParams vendorParams) {
        boolean wereAnyVendorsNotifiable = areAnyVendorsNotifiable(context);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.remove(vendorParams.mUUID);
        editor.apply();
        vendorParams.mIsNotifiable = true;
        putToast(context, "Turned ON beacon notifications for " + vendorParams.mName,
                Toast.LENGTH_LONG);
        if (areAnyVendorsNotifiable(context) && !wereAnyVendorsNotifiable)
            BeaconFinderService.allowScanning(context);
    }

    public static void turnOnNotificationsForVendor(Context context, final String uuid) {
        turnOnNotificationsForVendor(context, VendorParams.findVendorParamsInFile(context,
                            new Predicate<VendorParams>() {
                                @Override
                                public boolean apply(VendorParams vendorParams) {
                                    return areUuidsEqual(uuid, vendorParams.mUUID);
                                }
                            }));
    }

    public static void turnOffNotificationsForVendor(Context context, final String uuid) {
        turnOffNotificationsForVendor(context, VendorParams.findVendorParamsInFile(context,
                new Predicate<VendorParams>() {
                    @Override
                    public boolean apply(VendorParams vendorParams) {
                        return areUuidsEqual(uuid, vendorParams.mUUID);
                    }
                }));
    }

    private static boolean areAnyVendorsNotifiable(Context context) {
        if (uuidDatabase != null) {
            for (String uuid : uuidDatabase)
                if (VendorParams.isVendorWithThisUUIDNotifiable(context, uuid))
                    return true;
            return false;
        }
        else return true;
    }

    @Override
    protected Region createRegion() {
        return new Region(REGION_UNIQUE_ID, null, null, null);  // search for multiple beacons, so no rules
    }

    @Override
    protected void onRangedBeacons(final List<Beacon> beaconList) {
        Log.e(TAG, "Ranged; size - " + beaconList.size());
        for (int i = 0; i < beaconList.size(); ++i) {
            Beacon beacon = beaconList.get(i);
            final String uuid = beacon.getProximityUUID();
            Log.e(TAG, "UUID scanned - " + uuid.toUpperCase());
            Log.e(TAG, "major - " + beacon.getMajor() + "; minor - " + beacon.getMinor());
            if (uuidDatabase.contains(uuid.toUpperCase())) {
                VendorParams vendorParams = VendorParams.findVendorParamsInFile(getApplicationContext(),
                        new Predicate<VendorParams>() {
                            @Override
                            public boolean apply(VendorParams vendorParams) {
                                return areUuidsEqual(uuid, vendorParams.mUUID);
                            }
                        });
                if (vendorParams.mIsNotifiable) {
                    DeviceParams deviceParams;
                    int beaconLimit = (vendorParams.mPaymentType.equalsIgnoreCase("counter")) ? 1 :
                                        PERIODIC_SCAN_BEACON_LIMIT;
                    if (periodicScanDeviceMap.containsKey(uuid)) {
                        deviceParams = periodicScanDeviceMap.get(uuid);
                        if (!deviceParams.mFoundInThisScan) {
                            ++(deviceParams.mCount);
                            deviceParams.mFoundInThisScan = true;
                        } else continue;
                    } else {
                        periodicScanDeviceMap.put(uuid, new DeviceParams(1, true));
                        deviceParams = periodicScanDeviceMap.get(uuid);
                    }
                    Log.e(TAG, "count-" + deviceParams.mCount +
                            ";lim-" + beaconLimit);
                    if (deviceParams.mCount == beaconLimit) {
                        deviceParams.mCount = 0;
                        deviceParams.mToBeNotified = true;
                    }
                    writeHashMapToFile();
                }
            }
        }
    }

    @Override
    protected void scan() {
        if (!running) {
            running = true;
            //mTimer.schedule(mCheckTask, 0, INTERVAL);
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, INTERVAL,
                                            getScanStarterPendingIntent(getApplicationContext()));
        }
    }

    public static boolean isRunning() { return running; }

    public static void checkAndStartScan(Context context) {
        if (!running && isBLESupported && areAnyVendorsNotifiable(context)) {
            context.startService(new Intent(context, PeriodicBFS.class));
        }
    }

    public static void checkAndStopScan(Context context) {
        if (running) {
            context.stopService(new Intent(context, PeriodicBFS.class));
            UUIDDownloadBaseReceiver.stopDownloads(context);
        }
    }

    public static class ScanStarter extends BroadcastReceiver {
        private static final String TAG = "ScanStarter";

        private Runnable mScanningRunnable;

        public ScanStarter() {}

        public void startScanning(Context context) {
            turnOnBluetooth();
            for (DeviceParams params : periodicScanDeviceMap.values())
                params.mFoundInThisScan = params.mToBeNotified = false;
            Log.e(TAG, "Started Scan");
            uiThreadHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    beaconManager.startRangingAndDiscoverDevice(scanningRegion);
                }
            }, SCAN_START_DELAY); // delay required as scanning will not work right upon enabling BT
        }

        public void stopScanning(Context context) {
            beaconManager.stopRanging(scanningRegion);
            turnOffBluetooth();
            Log.e(TAG, "Stopped Scan");

            for (String uuid : periodicScanDeviceMap.keySet())
                if (!periodicScanDeviceMap.get(uuid).mFoundInThisScan)
                    periodicScanDeviceMap.remove(uuid);
            showNotifications(context
            );
            wakeLockManager.releaseWakeLock();
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            Log.e(TAG, "Received");
            if (intent.getAction() != null && intent.getAction().equals(ACTION_FIRE_ALARM_SCAN)) {
                wakeLockManager.acquireWakeLock(context, TAG);
                readUUIDsFromFile(context);
                mScanningRunnable = new Runnable() {
                    @Override
                    public void run() {
                        startScanning(context);
                        uiThreadHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                stopScanning(context);
                            }
                        }, SCAN_PERIOD);
                    }
                };
                if (uuidDatabase != null && areAnyVendorsNotifiable(context))
                    uiThreadHandler.post(mScanningRunnable);
            }
        }
    }

    private class DeviceParams {
        public int mCount;
        public boolean mFoundInThisScan;
        public boolean mToBeNotified;

        public DeviceParams(int count, boolean foundInThisScan) {
            mCount = count;
            mFoundInThisScan = foundInThisScan;
            mToBeNotified = false;
        }
    }

    public static class VendorListActivity extends ActionBarActivity {

        private static final String TAG = "VendorListActivity";

        public static ArrayList<VendorParams> vendorList = null;

        private ListView mVendorListView;
        private VendorListAdapter mVendorListAdapter;
        private Button mDismissButton;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_vendor_list);
            getVendorListFromIntent();
            initViews();
        }

        private void initViews() {
            mVendorListView = (ListView) findViewById(R.id.vendorListView);
            mVendorListAdapter = new VendorListAdapter(this, vendorList);
            mVendorListView.setAdapter(mVendorListAdapter);
            mDismissButton = (Button) findViewById(R.id.dismissButton);
            mDismissButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    System.exit(0);
                }
            });
        }

        private void getVendorListFromIntent() {
            ArrayList<String> uuids = getIntent().getStringArrayListExtra("uuidList");
            vendorList = new ArrayList<>();
            for (final String uuid : uuids) {
                VendorParams vendorParams = VendorParams.findVendorParamsInFile(this, new Predicate<VendorParams>() {
                    @Override
                    public boolean apply(VendorParams vendorParams) {
                        return areUuidsEqual(vendorParams.mUUID, uuid);
                    }
                });
                if (vendorParams != null && vendorParams.mIsNotifiable) {
                    vendorList.add(vendorParams);
                    Log.e(TAG, "vendor added - " + vendorParams.mName);
                }
            }
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_vendor_list, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                return true;
            }

            return super.onOptionsItemSelected(item);
        }

        private class VendorListAdapter extends BaseAdapter {
            private static final String TAG = "VendorListAdapter";
            private static final long REMOVE_ANIMATION_DURATION = 500;

            private ArrayList<VendorParams> mVendorList;
            private Context mContext;
            private ViewHolder mViewHolder;

            public VendorListAdapter(Context context, ArrayList<VendorParams> vendorList) {
                super();
                mVendorList = vendorList;
                mContext = context;
            }

            @Override
            public int getCount () {
                return mVendorList.size();
            }

            @Override
            public long getItemId (int position) {
                return position;
            }

            @Override
            public Object getItem (int position) {
                return mVendorList.get(position);
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    int idToInflate = ((position + 1) % 2 == 0) ? R.layout.vendor_list_item_even :
                            R.layout.vendor_list_item_odd;
                    convertView = inflater.inflate(idToInflate, null);
                }

                final VendorParams params = mVendorList.get(position);

                mViewHolder = new ViewHolder(convertView);
                mViewHolder.mTitleView.setText(params.mName);
                mViewHolder.mNextOfferView.setText(params.mNextOfferCaption);
                mViewHolder.mViewVendorButton.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent detailIntent = params.getDetailsIntent(mContext);
                        detailIntent.putExtra("from_periodic_scan", true);
                        mContext.startActivity(detailIntent);
                    }
                });
                mViewHolder.mTurnOffVendorButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PeriodicBFS.turnOffNotificationsForVendor(mContext, params);
                        Animation removalAnimation = AnimationUtils.loadAnimation(mContext,
                                                    android.R.anim.slide_out_right);
                        removalAnimation.setDuration(REMOVE_ANIMATION_DURATION);
                        removalAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {}

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                mVendorList.remove(params);
                                mVendorListAdapter.notifyDataSetChanged();
                                if (mVendorList.isEmpty()) {
                                    finish();
                                    System.exit(0);
                                }
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {}
                        });
                        mVendorListView.getChildAt(position).startAnimation(removalAnimation);
                    }
                });
                return convertView;
            }

            private class ViewHolder {
                public TextView mTitleView;
                public TextView mNextOfferView;
                public Button mViewVendorButton;
                public Button mTurnOffVendorButton;

                public ViewHolder(View parentView) {
                    mTitleView = (TextView) parentView.findViewById(R.id.vendorNameView);
                    mNextOfferView = (TextView) parentView.findViewById(R.id.nextOfferView);
                    mViewVendorButton = (Button) parentView.findViewById(R.id.viewVendorButton);
                    mTurnOffVendorButton = (Button) parentView.findViewById(R.id.turnOffVendorButton);
                }
            }
        }
    }
}
