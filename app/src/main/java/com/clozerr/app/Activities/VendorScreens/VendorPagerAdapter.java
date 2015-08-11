package com.clozerr.app.Activities.VendorScreens;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.clozerr.app.AsyncGet;
import com.clozerr.app.MyStampsFragment;
import com.clozerr.app.R;

/**
 * Created by Adarsh on 20/5/15.
 */
class VendorPagerAdapter extends FragmentPagerAdapter {
    Context c;
    String[] tabheadings;

    public VendorPagerAdapter(FragmentManager fm,Context c) {
        super(fm);
        this.c=c;
        tabheadings=c.getResources().getStringArray(R.array.vendortabheadings);
    }

    @Override
    public Fragment getItem(int position) {
        AsyncGet.dismissDialog();
        switch (position){
            case 0:VendorHomeFragment vendorHomeFragment=new VendorHomeFragment();
                return vendorHomeFragment;
            case 1:FreebiesFragment freebiesFragment = new FreebiesFragment();
                return freebiesFragment;
            case 2:MyStampsFragment myStampsFragment=new MyStampsFragment();
                return myStampsFragment;
            case 3:VendorSettingsFragment vendorSettingsFragment = new VendorSettingsFragment();
                return vendorSettingsFragment;
            default:return null;
        }

    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        return tabheadings[position];
    }

    @Override
    public int getCount() {
        return tabheadings.length;
    }
}
