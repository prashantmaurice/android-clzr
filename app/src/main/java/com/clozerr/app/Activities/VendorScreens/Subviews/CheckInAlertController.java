package com.clozerr.app.Activities.VendorScreens.Subviews;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.clozerr.app.Activities.UtilActivities.QRActivity;
import com.clozerr.app.AsyncGet;
import com.clozerr.app.Models.RewardsObject;
import com.clozerr.app.R;
import com.clozerr.app.Utils.Router;
import com.google.android.gcm.GCMRegistrar;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by maurice on 21/08/15.
 */
public class CheckInAlertController {
    public static void openCheckinDirectly(final Context c, final RewardsObject model){
        String url = Router.VendorScreen.checkInAReward(model.rewardId,model.vendorId, GCMRegistrar.getRegistrationId(c));
        Log.d("FreebieDescription", "checkin url - " + url);
        if(!model.unlocked){
            Toast.makeText(c, "Sorry you are yet to unlock the reward", Toast.LENGTH_SHORT).show();
            return;
        }

        new AsyncGet(c, url, new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                try {
                    final JSONObject jsonObject = new JSONObject(s);
                    if(jsonObject.getString("vendor")!=null){
                        Toast.makeText(c,"Please show the code to the staff to claim your reward",Toast.LENGTH_SHORT).show();
                        getNewPopupWindow(c,jsonObject,model);
                    }else{
                        Toast.makeText(c, "Check In Failed. Please try again", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public static void getNewPopupWindow(final Context c, final JSONObject jsonObject, final RewardsObject rewardsObject) throws JSONException {
        final Dialog dialog = new Dialog(c, R.style.PopupWindowAnimation);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.checkin_pin_confirm);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.dimAmount = .7f;
        wlp.gravity = Gravity.CENTER;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setAttributes(wlp);
        dialog.setCanceledOnTouchOutside(true);

        //TODO : cancel on outside touch not working



        /**INITIALIZE UI*/
        //Date time layout
        View dateTimeLayout = dialog.findViewById(R.id.dateTimeLayout);
        String date = new SimpleDateFormat("dd.MM.yyyy").format(new Date(System.currentTimeMillis()));
        String time = new SimpleDateFormat("HH:mm").format(new Date(System.currentTimeMillis())) + " hrs";
        ((TextView)(dateTimeLayout.findViewById(R.id.timeView))).setText(time);
        ((TextView)(dateTimeLayout.findViewById(R.id.dateView))).setText(date);

        ((TextView) dialog.findViewById(R.id.tv_rewardUniqueCode)).setText(jsonObject.getString("pin"));
        ((TextView) dialog.findViewById(R.id.tv_title1)).setText(rewardsObject.caption);
        ((TextView) dialog.findViewById(R.id.tv_title2)).setText(rewardsObject.description);

        dialog.findViewById(R.id.qrButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent qrIntent = new Intent(c, QRActivity.class);
                qrIntent.putExtra(QRActivity.EXTRA_VENDORID, rewardsObject.vendorId);
                qrIntent.putExtra(QRActivity.EXTRA_OFFERID, rewardsObject.rewardId);
                try {
                    qrIntent.putExtra(QRActivity.EXTRA_OFFERID, jsonObject.getString("_id"));
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
                c.startActivity(qrIntent);
            }
        });

        dialog.setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                }
        );

        dialog.show();
    }
}
