package com.clozerr.app.Activities.VendorScreens;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.clozerr.app.AsyncGet;
import com.clozerr.app.Models.RewardsObject;
import com.clozerr.app.QRActivity;
import com.clozerr.app.R;
import com.clozerr.app.Utils.Router;
import com.google.android.gcm.GCMRegistrar;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *  This is one of the fragments user in Vendors page
 */

public class RewardsFragmentAdapter extends RecyclerView.Adapter<RewardsFragmentAdapter.ListItemViewHolder> {

    private List<RewardsObject> items;
    Context c;


    RewardsFragmentAdapter(ArrayList<RewardsObject> modelData, Context c) {
        this.items = modelData;
        this.c = c;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(
        ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.freebies_item_layout,
                                viewGroup,
                                false);



        return new ListItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ListItemViewHolder viewHolder, int position) {
        final RewardsObject model = items.get(position);
        viewHolder.currentItem = items.get(position);
        viewHolder.name.setText(model.description);
        viewHolder.caption.setText(model.caption);

        if( !model.unlocked )
            viewHolder.layout.setBackgroundColor(Color.parseColor("#AAAAAA"));

        Ion.with((viewHolder.imageView))
                //   .placeholder(R.drawable.call)
                //   .error(R.drawable.bat)
                //    .animateLoad(spinAnimation)
                //    .animateIn(fadeInAnimation)
                .load(model.image);


        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCheckinDirectly(c, model);
            }
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public final static class ListItemViewHolder
            extends RecyclerView.ViewHolder {
        View view;
        ImageView imageView;
        TextView name;
        TextView caption;
        LinearLayout layout;
        public RewardsObject currentItem;

        public ListItemViewHolder(final View itemView) {
            super(itemView);
            view = itemView;
            imageView = (ImageView) itemView.findViewById(R.id.freebieimage);
            name = (TextView)itemView.findViewById(R.id.freebiename);
            caption = (TextView)itemView.findViewById(R.id.freebiedescription);
            layout = (LinearLayout)itemView.findViewById(R.id.freebielayout);


//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {


//                    Intent intent = new Intent(c,FreebieDescription.class);
//                    intent.putExtra("offerid",currentItem.RewardId);
//                    intent.putExtra("vendorid", VendorActivity.vendorId);
//                    intent.putExtra("caption",currentItem.Caption);
//                    intent.putExtra("description",currentItem.Description);
//                    if( currentItem.Unlocked )
//                        c.startActivity(intent);
////                        openCheckinDirectly();
//                }
//            });
        }

    }
    private void openCheckinDirectly(final Context c, final RewardsObject model){
        String url = Router.VendorScreen.checkInAReward(model.rewardId,model.vendorId,GCMRegistrar.getRegistrationId(c));
        Log.d("FreebieDescription", "checkin url - " + url);
        if(!model.unlocked){
            Toast.makeText(c,"Sorry you are yet to unlock the reward",Toast.LENGTH_SHORT).show();
            return;
        }

        new AsyncGet(c, url, new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                try {
                    final JSONObject jsonObject = new JSONObject(s);
                    if(jsonObject.getString("vendor")!=null){
                        Toast.makeText(c,"Checked In Successfully. Please contact the billing staff.",Toast.LENGTH_SHORT).show();
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
    public void getNewPopupWindow(final Context c, final JSONObject jsonObject, final RewardsObject rewardsObject) throws JSONException {
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
