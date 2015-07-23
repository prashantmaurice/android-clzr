package com.clozerr.app;

import android.content.Context;

import com.jaalee.sdk.Beacon;

import java.util.List;

/**
 * Created by aravind on 19/7/15.
 */
public class InStoreInstallBFS extends OneTimeBFS {
    private static final String TAG = "ISIBFS";

    public InStoreInstallBFS() { super(); }

    @Override
    public void onRanged(List<Beacon> beacons) {
        Beacon nearest = null;
        for (Beacon beacon : beacons) {
            if (nearest == null || nearest.getRssi() < beacon.getRssi())
                nearest = beacon;
        }
        if (nearest != null) {
            final Context applicationContext = getApplicationContext();
            final String analyticsURL = GenUtils.getDefaultAnalyticsUriBuilder(applicationContext, Constants.Metrics.IN_STORE_INSTALL)
                    .appendQueryParameter("dimensions[beacon_major]", String.valueOf(nearest.getMajor()))
                    .appendQueryParameter("dimensions[beacon_minor]", String.valueOf(nearest.getMinor()))
                    .build().toString();
            GenUtils.putAnalytics(applicationContext, TAG, analyticsURL);
        }
    }
}
