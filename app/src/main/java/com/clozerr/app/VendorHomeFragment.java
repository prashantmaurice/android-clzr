package com.clozerr.app;

/**
 * Created by Adarsh on 20-05-2015.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
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

import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class VendorHomeFragment extends Fragment {

    private Context c;
    private FrameLayout layout;

    private ImageView mVendorImageView,mVendorGalleryImageView;
    private TextView mVendorTitleView;
    private TextView mVendorAddressView;
    private ImageButton mCallButton, mDirButton, favorites, whatsappshare,fb,gplus,twitter,share;
    private RecyclerView gallerylist;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        layout = (FrameLayout) inflater.inflate(R.layout.activity_vendor_home_fragment, container, false);
        initViews();

        Ion.with(mVendorImageView).load(VendorActivity.detailsBundle.getString("vendorImage")) ;
        if(!VendorActivity.detailsBundle.getString("logo").equalsIgnoreCase("undefined"))
              new DownloadImageTask(mVendorGalleryImageView).execute(VendorActivity.detailsBundle.getString("logo"));
        mVendorTitleView.setText(VendorActivity.detailsBundle.getString("vendorTitle"));
        mVendorAddressView.setText(VendorActivity.detailsBundle.getString("address"));
        final SharedPreferences status = c.getSharedPreferences("USER",0);
        final String NotNow = status.getString("notNow","false");
        final ArrayList<String> fav = new ArrayList<String>();
        JSONArray favourites ;
        if(NotNow.equals("false")) {
            try {
                JSONObject userobj = new JSONObject(status.getString("user", "null"));
                favourites = userobj.getJSONArray("favourites");
                Log.i("favourites", favourites.toString());
                if (favourites != null) {
                    int len = favourites.length();
                    for (int i = 0; i < len; i++) {
                        fav.add(favourites.get(i).toString());
                    }
                }

            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        Log.i("fav","changing favs");
        if(fav.indexOf(VendorActivity.detailsBundle.getString("vendorId"))!=-1)
            favorites.setImageResource(R.drawable.favorited);
        else
            favorites.setImageResource(R.drawable.unfavorited);
        Log.i("fave","leaving favs");
        // TODO remove this AsyncGet altogether. Favorite details must be taken from elsewhere (vendor/get/details maybe)
        mCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!VendorActivity.detailsBundle.getString("phoneNumber").isEmpty()) {
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                    dialIntent.setData(Uri.parse("tel:" + VendorActivity.detailsBundle.getString("phoneNumber")));
                    startActivity(dialIntent);
                } else
                    Toast.makeText(getActivity(), "Sorry, the phone number has not been provided by the vendor.",
                            Toast.LENGTH_SHORT).show();
            }
        });
        mDirButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!VendorActivity.detailsBundle.getString("distanceString").isEmpty()) {
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("http://maps.google.com/maps?daddr=" + VendorActivity.detailsBundle.getDouble("latitude")
                                    + "," + VendorActivity.detailsBundle.getDouble("longitude")));
                    startActivity(intent);
                }
                else
                    Toast.makeText(getActivity(), "Sorry, the location details have not been provided by the vendor.",
                            Toast.LENGTH_SHORT).show();
            }
        });
        favorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences status = getActivity().getSharedPreferences("USER", 0);
                String TOKEN = status.getString("token", "");
                Log.i("name",getResources().getResourceName(R.id.favorites));
                if(fav.indexOf(VendorActivity.detailsBundle.getString("vendorId"))==-1)
                {
                    favorites.setImageResource(R.drawable.favorited);
                    new AsyncGet(getActivity(), "http://api.clozerr.com/v2/user/add/favourites?vendor_id="+VendorActivity.vendorId+"&access_token="+TOKEN, new AsyncGet.AsyncResult() {
                        @Override
                        public void gotResult(String s) {
                            final SharedPreferences.Editor editor = c.getSharedPreferences("USER", 0).edit();
                            editor.putString("user",s);
                            editor.apply();
                            Toast.makeText(getActivity(),"Favorited and added to My Clubs.", Toast.LENGTH_LONG).show();
                            
                            //l1.setAdapter(adapter);
                            if (s == null) {
                                Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();
                                favorites.setImageResource(R.drawable.like);
                            }

                            // Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();


                          /*RecyclerViewAdapter1 Cardadapter = new RecyclerViewAdapter1(convertRowMyOffers(s), CouponDetails.this);
                          mRecyclerView.setAdapter(Cardadapter);*/
                        }
                    },false);
                }
                else
                {
                    favorites.setImageResource(R.drawable.unfavorited);
                    new AsyncGet(getActivity(), "http://api.clozerr.com/v2/user/remove/favourites?vendor_id="+VendorActivity.vendorId+"&access_token="+TOKEN, new AsyncGet.AsyncResult() {
                        @Override
                        public void gotResult(String s) {
                            final SharedPreferences.Editor editor = c.getSharedPreferences("USER", 0).edit();
                            editor.putString("user",s);
                            editor.apply();
                            Toast.makeText(getActivity(),"Unfavorited and removed to My Clubs.", Toast.LENGTH_LONG).show();
                            //l1.setAdapter(adapter);
                            if (s == null) {
                                Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();
                                favorites.setImageResource(R.drawable.like);
                            }

                            // Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();


                          /*RecyclerViewAdapter1 Cardadapter = new RecyclerViewAdapter1(convertRowMyOffers(s), CouponDetails.this);
                          mRecyclerView.setAdapter(Cardadapter);*/
                        }
                    },false);
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
                if(!VendorActivity.detailsBundle.getString("twitter").equals(""))
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(VendorActivity.detailsBundle.getString("twitter"))));
                else
                    Toast.makeText(getActivity(),"This Vendor Doesnt have a active twitter page",Toast.LENGTH_SHORT).show();
            }
        });
        fb.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String facebookUrl = VendorActivity.detailsBundle.getString("fb");

                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl)));
            }
        });
        gplus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!VendorActivity.detailsBundle.getString("gplus").equals(""))
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(VendorActivity.detailsBundle.getString("gplus"))));
                else
                    Toast.makeText(getActivity(),"This Vendor Doesnt have a active google plus page",Toast.LENGTH_SHORT).show();
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "Check out this place I found on Clozerr: "+VendorActivity.detailsBundle.getString("vendorTitle")+" https://play.google.com/store/apps/details?id=com.clozerr.app";;
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Clozerr");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        });
        gallerylist.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        gallerylist.setItemAnimator(new DefaultItemAnimator());
        gallerylist.setHasFixedSize(true);
        final String[] values = new String[] { "1","2","3","4","5","6","7","8","9","10" };
        GalleryAdapter adapter = new GalleryAdapter(values, getActivity());
        gallerylist.setAdapter(adapter);

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
        gallerylist = (RecyclerView) layout.findViewById(R.id.GalleryRecyclerView);
        mVendorTitleView = (TextView) layout.findViewById(R.id.vendorTitleView);
        mVendorAddressView = (TextView) layout.findViewById(R.id.addressView);
        mCallButton = (ImageButton) layout.findViewById(R.id.callButton);
        mDirButton = (ImageButton) layout.findViewById(R.id.dirButton);
        favorites = (ImageButton) layout.findViewById(R.id.favorites);
        whatsappshare=(ImageButton) layout.findViewById(R.id.whatsappshare);
        fb = (ImageButton) layout.findViewById(R.id.fb);
        gplus = (ImageButton) layout.findViewById(R.id.gplus);
        twitter = (ImageButton) layout.findViewById(R.id.twitter);
        share = (ImageButton) layout.findViewById(R.id.share);
        mVendorGalleryImageView = (ImageView) layout.findViewById(R.id.logo);
    }

    public void onClickWhatsApp() {

        PackageManager pm=getActivity().getPackageManager();
        try {

            Intent waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setType("text/plain");
            String text = "Check out this place I found on Clozerr: "+VendorActivity.detailsBundle.getString("vendorTitle")+" https://play.google.com/store/apps/details?id=com.clozerr.app";

            PackageInfo info=pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            //Check if package exists or not. If not then code
            //in catch block will be called
            waIntent.setPackage("com.whatsapp");
            waIntent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(waIntent, "Share with"));

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(getActivity(), "WhatsApp not Installed", Toast.LENGTH_SHORT)
                    .show();
        }

    }
}



