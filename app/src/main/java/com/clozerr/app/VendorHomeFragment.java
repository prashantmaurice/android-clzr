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
import org.json.JSONException;
import org.json.JSONObject;


public class VendorHomeFragment extends Fragment {

    private Context c;
    private FrameLayout layout;

    private ImageView mVendorImageView;
    private TextView mVendorTitleView;
    private TextView mVendorAddressView;
    private ImageButton mCallButton, mDirButton, favorites, whatsappshare;
    private RecyclerView gallerylist;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        layout = (FrameLayout) inflater.inflate(R.layout.activity_vendor_home_fragment, container, false);
        initViews();

        Ion.with(mVendorImageView).load(VendorActivity.detailsBundle.getString("vendorImage"));
        mVendorTitleView.setText(VendorActivity.detailsBundle.getString("vendorTitle"));
        mVendorAddressView.setText(VendorActivity.detailsBundle.getString("address"));
        // TODO remove this AsyncGet altogether. Favorite details must be taken from elsewhere (vendor/get/details maybe)
        String urlFavorites = "http://api.clozerr.com/v2/user/add/favourites?access_token=" +Home.TOKEN;
        new AsyncGet(c, urlFavorites , new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                try {
                    JSONObject obj=new JSONObject(s);
                    //Toast.makeText(getActivity(),s,Toast.LENGTH_SHORT).show();
                    JSONObject fav=obj.getJSONObject("favourites");
                    JSONArray vendors=fav.getJSONArray("vendor");
                    //Toast.makeText(getActivity(),vendors.toString(),Toast.LENGTH_SHORT).show();
                    for (int i = 0; i < vendors.length(); ++i) {

                        if(VendorActivity.vendorId.equals(vendors.getString(i)))
                        {
                            favorites.setImageResource(R.drawable.favorited);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //  t1.setText(s);
                if(s==null) {
                    Toast.makeText(c,"No internet connection",Toast.LENGTH_SHORT).show();
                }
                //l1.setAdapter(adapter);
            }
        });


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
                new AsyncGet(getActivity(), "http://api.clozerr.com/v2/user/add/favourites?vendor_id="+VendorActivity.vendorId+"&access_token="+TOKEN, new AsyncGet.AsyncResult() {
                    @Override
                    public void gotResult(String s) {
                        Toast.makeText(getActivity(),"Favorited and added to My Clubs.", Toast.LENGTH_LONG).show();
                        //l1.setAdapter(adapter);
                        favorites.setImageResource(R.drawable.favorited);
                        if (s == null) {
                            Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();
                        }

                        // Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();


                          /*RecyclerViewAdapter1 Cardadapter = new RecyclerViewAdapter1(convertRowMyOffers(s), CouponDetails.this);
                          mRecyclerView.setAdapter(Cardadapter);*/
                    }
                });


            }
        });

        whatsappshare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickWhatsApp();
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



