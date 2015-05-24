package com.clozerr.app;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

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
        switch (position){
            case 0:VendorHomeFragment vendorHomeFragment=new VendorHomeFragment();
                return vendorHomeFragment;
            case 1:MyStampsFragment myStampsFragment=new MyStampsFragment();
                return myStampsFragment;
            case 2:FreebiesFragment freebiesFragment = new FreebiesFragment();
                return freebiesFragment;
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
