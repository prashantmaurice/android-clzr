package com.clozerr.app.Handlers;

import android.content.Context;

import com.clozerr.app.AsyncGet;
import com.clozerr.app.GenUtils;
import com.clozerr.app.Storage.SharedPrefs;
import com.clozerr.app.Utils.Constants;
import com.clozerr.app.Utils.Logg;

import org.json.JSONException;

/**
 * This contains all the Campaign related data that is stored and handled carefully
 *
 */
public class CampaignHandler {
    private Context mContext;
    static String TAG = "CAMPAIGNHANDLER";
    private static CampaignHandler instance;
    private SharedPrefs sPrefs;
    public String name;

    public String campaignRaw = "";
    public String utm_source = "";
    public String utm_medium = "";
    public String utm_term = "";
    public String utm_content = "";
    public String utm_campaign = "";

    public boolean sent = false;

    private CampaignHandler(Context context) {
        mContext = context;
        pullCampaignDataFromLocal();
    }
    public static CampaignHandler getInstance(Context context) {
        if(instance == null) {
            instance = new CampaignHandler(context);
        }
        return instance;
    }


    //LOCAL STORAGE ENCODERS
    public void pullCampaignDataFromLocal() {
        sPrefs = SharedPrefs.getInstance(mContext);
        try {
            campaignRaw = (sPrefs.campaignData.has("campaignRaw"))?sPrefs.campaignData.getString("campaignRaw"):"";
            sent = (sPrefs.campaignData.has("sent"))?sPrefs.campaignData.getBoolean("sent"):false;

        } catch (JSONException e) {e.printStackTrace();}
    }
    public void saveCampaignDataLocally() {
        try {
            sPrefs.campaignData.put("campaignRaw", campaignRaw);
            sPrefs.campaignData.put("sent", sent);
        } catch (JSONException e) {e.printStackTrace();}
        sPrefs.saveCampaignData();
    }

    public void sendCampaignDataIfNotSent(){
        final String analyticsurl= GenUtils.getDefaultAnalyticsUriBuilder(mContext, Constants.Metrics.CAMPAIGN).build().toString();

        if(sent) return; //already sent
        if(campaignRaw.isEmpty()) return; //no campaigndata

        new AsyncGet(mContext, analyticsurl, new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                sent = true;
                saveCampaignDataLocally();
                Logg.i(TAG,"Campaign data successfully sent : "+ analyticsurl + " : " + s);
            }
        },false);
    }


}
