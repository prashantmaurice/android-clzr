package com.clozerr.app.Activities.VendorScreens.Subviews;

/**
 * Created by Adarsh on 20-05-2015.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.clozerr.app.Activities.VendorScreens.VendorActivity;
import com.clozerr.app.AsyncGet;
import com.clozerr.app.DownloadImageTask;
import com.clozerr.app.MainApplication;
import com.clozerr.app.Models.VendorDetailsObject;
import com.clozerr.app.R;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class VendorHomeFragment extends Fragment {

    private Context c;
    private FrameLayout layout;

    private ImageView mVendorImageView,mVendorLogoView;
    private TextView mVendorTitleView;

    private TextView mVendorAddressView, mVendorDescriptionView;
    private ImageButton mCallButton, mDirButton, favorites, whatsappshare,fb,gplus,twitter,share, mRateButton, mChatButton;
    private RecyclerView gallerylist;

    //Data variables
    String vendorId;
    private VendorDetailsObject vendorDetailsObject;

    public static VendorHomeFragment newInstance(VendorDetailsObject vendorDetailsObject) {
        VendorHomeFragment myFragment = new VendorHomeFragment();
        myFragment.vendorId = vendorDetailsObject.vendorId;
        myFragment.vendorDetailsObject = vendorDetailsObject;
        return myFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        layout = (FrameLayout) inflater.inflate(R.layout.activity_vendor_home_fragment, container, false);
        initViews();

        Ion.with(mVendorImageView).load(vendorDetailsObject.getImageUrl()) ;
        if(!vendorDetailsObject.getImageUrl().isEmpty())
              new DownloadImageTask(mVendorLogoView).execute(vendorDetailsObject.getLogoImageUrl());

        VendorActivity.logoView = mVendorLogoView;

        mVendorTitleView.setText(vendorDetailsObject.name);
        //mVendorAboutView.setText(VendorActivity.detailsBundle.getString("description"));
        mVendorAddressView.setText(vendorDetailsObject.address);
        mVendorDescriptionView.setText(vendorDetailsObject.description);
//        final SharedPreferences status = c.getSharedPreferences("USER",0);
//        final String NotNow = status.getString("notNow","false");

        final boolean NotNow = MainApplication.getInstance().data.userMain.notNow;


        final ArrayList<String> fav = new ArrayList<String>();
        JSONArray favourites ;
        if(!NotNow) {
//        if(NotNow.equals("false")) {
            try {
//                JSONObject userobj = new JSONObject(status.getString("user", "null"));
                String jsonTxt = (MainApplication.getInstance().data.userMain.user.isEmpty())?"null":MainApplication.getInstance().data.userMain.user;
                JSONObject userobj = new JSONObject(jsonTxt);
                favourites = userobj.getJSONArray("favourites");
                Log.i("favourites", favourites.toString());
                int len = favourites.length();
                for (int i = 0; i < len; i++) {
                    fav.add(favourites.get(i).toString());
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        Log.i("fav","changing favs");
        if(fav.indexOf(vendorDetailsObject.vendorId)!=-1)
            favorites.setImageResource(R.drawable.favorited);
        else
            favorites.setImageResource(R.drawable.unfavorited);
        Log.i("fave", "leaving favs");
        // TODO remove this AsyncGet altogether. Favorite details must be taken from elsewhere (vendor/get/details maybe)
        mCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vendorDetailsObject.phone.isEmpty()) {
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                    dialIntent.setData(Uri.parse("tel:" + vendorDetailsObject.phone));
                    startActivity(dialIntent);
                } else
                    Toast.makeText(getActivity(), "Sorry, the phone number has not been provided by the vendor.",
                            Toast.LENGTH_SHORT).show();
            }
        });
        mDirButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (VendorActivity.detailsBundle.getDouble("latitude") && !VendorActivity.detailsBundle.getString("longitude").isEmpty() ) {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://maps.google.com/maps?daddr=" +vendorDetailsObject.lat
                                    + "," + vendorDetailsObject.longg));
                    startActivity(intent);
                //}
                //else
                //    Toast.makeText(getActivity(), "Sorry, the location details have not been provided by the vendor.",
                //            Toast.LENGTH_SHORT).show();
            }
        });
        /*mRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(),"Coming soon!!", Toast.LENGTH_LONG).show();
            }
        });
        mChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(),"Coming soon!!", Toast.LENGTH_LONG).show();
            }
        });*/
        favorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                SharedPreferences status = getActivity().getSharedPreferences("USER", 0);
//                String TOKEN = status.getString("token", "");
                String TOKEN = MainApplication.getInstance().tokenHandler.clozerrtoken;
                Log.i("name", getResources().getResourceName(R.id.favorites));
                if (fav.indexOf(vendorDetailsObject.vendorId) == -1) {
                    Log.e("VendorHomeFragment", "favs url - " + "http://api.clozerr.com/v2/user/add/favourites?vendor_id=" + vendorId + "&access_token=" + TOKEN);
                    favorites.setImageResource(R.drawable.favorited);
                    new AsyncGet(getActivity(), "http://api.clozerr.com/v2/user/add/favourites?vendor_id=" + vendorId + "&access_token=" + TOKEN, new AsyncGet.AsyncResult() {
                        @Override
                        public void gotResult(String s) {
                            //l1.setAdapter(adapter);
                            //try {
                            if (s == null/* || !(new JSONObject(s).getBoolean("result"))*/) {
                                Toast.makeText(getActivity(), "Connection error", Toast.LENGTH_SHORT).show();
                                favorites.setImageResource(R.drawable.unfavorited);
                            } else {
                                fav.add(vendorDetailsObject.vendorId);
//                                final SharedPreferences.Editor editor = c.getSharedPreferences("USER", 0).edit();
//                                editor.putString("user", s);
//                                editor.apply();
                                MainApplication.getInstance().data.userMain.changeUser(s);
                                Toast.makeText(getActivity(), "Favorited and added to My Clubs.", Toast.LENGTH_LONG).show();
                            }
                            // Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
                            /*} catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getActivity(), "Connection error", Toast.LENGTH_SHORT).show();
                                favorites.setImageResource(R.drawable.unfavorited);
                            }*/

                          /*RecyclerViewAdapter1 Cardadapter = new RecyclerViewAdapter1(convertRowMyOffers(s), CouponDetails.this);
                          mRecyclerView.setAdapter(Cardadapter);*/

                        }
                    }, false);
                } else {
                    Log.e("VendorHomeFragment", "favs url - " + "http://api.clozerr.com/v2/user/remove/favourites?vendor_id=" + vendorId + "&access_token=" + TOKEN);
                    favorites.setImageResource(R.drawable.unfavorited);
                    new AsyncGet(getActivity(), "http://api.clozerr.com/v2/user/remove/favourites?vendor_id=" + vendorId + "&access_token=" + TOKEN, new AsyncGet.AsyncResult() {
                        @Override
                        public void gotResult(String s) {
                            //l1.setAdapter(adapter);
                            //try {
                            if (s == null/* || !(new JSONObject(s).getBoolean("result"))*/) {
                                Toast.makeText(getActivity(), "Connection error", Toast.LENGTH_SHORT).show();
                                favorites.setImageResource(R.drawable.favorited);
                            } else {
                                fav.remove(vendorDetailsObject.vendorId);
//                                final SharedPreferences.Editor editor = c.getSharedPreferences("USER", 0).edit();
//                                editor.putString("user", s);
//                                editor.apply();
                                MainApplication.getInstance().data.userMain.changeUser(s);
                                Toast.makeText(getActivity(), "Unfavorited and removed from My Clubs.", Toast.LENGTH_LONG).show();
                            }
                            // Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
                            /*} catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getActivity(), "Connection error", Toast.LENGTH_SHORT).show();
                                favorites.setImageResource(R.drawable.favorited);
                            }*/

                          /*RecyclerViewAdapter1 Cardadapter = new RecyclerViewAdapter1(convertRowMyOffers(s), CouponDetails.this);
                          mRecyclerView.setAdapter(Cardadapter);*/
                        }
                    }, false);
                }
            }
        });

        whatsappshare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickWhatsApp();
            }
        });
        twitter.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
//                if(!VendorActivity.detailsBundle.getString("twitter").equals(""))
//                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(VendorActivity.detailsBundle.getString("twitter"))));
//                else
//                    Toast.makeText(getActivity(),"This Vendor Doesnt have a active twitter page",Toast.LENGTH_SHORT).show();
            }
        });
        fb.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
//                String facebookUrl = VendorActivity.detailsBundle.getString("fb");
//
//                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl)));
            }
        });
        gplus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(!VendorActivity.detailsBundle.getString("gplus").equals(""))
//                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(VendorActivity.detailsBundle.getString("gplus"))));
//                else
//                    Toast.makeText(getActivity(),"This Vendor Doesnt have a active google plus page",Toast.LENGTH_SHORT).show();
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "Check out this place I found on Clozerr: "+vendorDetailsObject.name+" https://play.google.com/store/apps/details?id=com.clozerr.app";;
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Clozerr");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        });
        /*gallerylist.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        gallerylist.setItemAnimator(new DefaultItemAnimator());
        gallerylist.setHasFixedSize(true);
        String[] eStr = {""};
        GalleryAdapter adapter = new GalleryAdapter(eStr, getActivity());
        gallerylist.setAdapter(adapter);*/

        return layout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        c=activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void initViews() {
        layout.getForeground().mutate().setAlpha(0);
        mVendorImageView = (ImageView) layout.findViewById(R.id.vendorImageView);
        //gallerylist = (RecyclerView) layout.findViewById(R.id.GalleryRecyclerView);
        mVendorTitleView = (TextView) layout.findViewById(R.id.vendorTitleView);
        mVendorAddressView = (TextView) layout.findViewById(R.id.addressView);
        mVendorDescriptionView = (TextView) layout.findViewById(R.id.descriptionView);
        mCallButton = (ImageButton) layout.findViewById(R.id.callButton);
        mDirButton = (ImageButton) layout.findViewById(R.id.dirButton);
        //mRateButton = (ImageButton) layout.findViewById(R.id.rateButton);
        //mChatButton = (ImageButton) layout.findViewById(R.id.chatButton);
        favorites = (ImageButton) layout.findViewById(R.id.favorites);
        whatsappshare=(ImageButton) layout.findViewById(R.id.whatsappshare);
        fb = (ImageButton) layout.findViewById(R.id.fb);
        gplus = (ImageButton) layout.findViewById(R.id.gplus);
        twitter = (ImageButton) layout.findViewById(R.id.twitter);
        share = (ImageButton) layout.findViewById(R.id.share);
        mVendorLogoView = (ImageView) layout.findViewById(R.id.logo);
    }

    public void onClickWhatsApp() {

        PackageManager pm=getActivity().getPackageManager();
        try {

            Intent waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setType("text/plain");
            String text = "Check out this place I found on Clozerr: "+vendorDetailsObject.name+" https://play.google.com/store/apps/details?id=com.clozerr.app";

            PackageInfo info=pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            //Check if package exists or not. If not then code
            //in catch block will be called
            waIntent.setPackage("com.whatsapp");
            waIntent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(waIntent, "Share with"));

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(getActivity(), "WhatsApp not Installed", Toast.LENGTH_SHORT).show();
        }

    }
}



