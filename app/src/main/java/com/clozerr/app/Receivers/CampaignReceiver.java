package com.clozerr.app.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.clozerr.app.Handlers.CampaignHandler;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by maurice on 13/08/15.
 *
 */

public class CampaignReceiver extends BroadcastReceiver {
    String TAG = "CAMPAIGNRECEIVER";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, " Recieved a Broadcast : " + intent.getStringExtra("referrer"));

        String referrer = intent.getStringExtra("referrer");

        CampaignHandler campaignHandler = CampaignHandler.getInstance(context);



        if ((referrer == null) || (referrer.length() == 0)) {
            //No campaign found
            return;
        }

        campaignHandler.campaignRaw = referrer;

        try {
            referrer = URLDecoder.decode(referrer, "UTF-8");

            //SAVE DATA IN OUR LOCAL
            Map<String, String> params = getQueryMap(referrer);

            campaignHandler.utm_source = URLDecoder.decode(params.get("utm_source"), "UTF-8");
            campaignHandler.utm_medium = URLDecoder.decode(params.get("utm_medium"), "UTF-8");
            campaignHandler.utm_term = URLDecoder.decode(params.get("utm_term"), "UTF-8");
            campaignHandler.utm_content = URLDecoder.decode(params.get("utm_content"), "UTF-8");
            campaignHandler.utm_campaign = URLDecoder.decode(params.get("utm_campaign"), "UTF-8");



        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        campaignHandler.saveCampaignDataLocally();

        //SEND DATA TO OUR SERVER
        campaignHandler.sendCampaignDataIfNotSent();



    }
    public static Map<String, String> getQueryMap(String query)
    {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params)
        {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }
}