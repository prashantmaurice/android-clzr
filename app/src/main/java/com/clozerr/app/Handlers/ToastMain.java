package com.clozerr.app.Handlers;

import android.content.Context;

import com.clozerr.app.GenUtils;
import com.clozerr.app.Utils.Settings;

/**
 *  Use this to show toasts to user, this shows small text if in production mode and shows detailed toast
 *  in debug mode for testers to identify problems.
 *
 */

public class ToastMain {

    public static void showSmartToast(Context context, String smallText, String detailedText){
        if(Settings.showDebugToasts) {
            if(detailedText!=null) GenUtils.showToast(context, detailedText);
        }else{
            if(smallText!=null) GenUtils.showToast(context, smallText);
        }
    }
    public static void showSmartToast(Context context, String generalText){
        if(generalText!=null) GenUtils.showToast(context, generalText);
    }

}
