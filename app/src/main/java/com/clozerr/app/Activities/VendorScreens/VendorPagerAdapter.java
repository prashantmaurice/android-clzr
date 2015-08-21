package com.clozerr.app.Activities.VendorScreens;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.clozerr.app.Activities.VendorScreens.Subviews.RewardsFragment;
import com.clozerr.app.Activities.VendorScreens.Subviews.VendorHomeFragment;
import com.clozerr.app.Activities.VendorScreens.Subviews.VendorSettingsFragment;
import com.clozerr.app.AsyncGet;
import com.clozerr.app.Activities.VendorScreens.Subviews.MyStampsFragment;
import com.clozerr.app.Models.VendorDetailsObject;
import com.clozerr.app.R;

/**
 *  This is the main pager adapter for Vendors activity
 */
class VendorPagerAdapter extends FragmentPagerAdapter {
    private final VendorDetailsObject vendorDetailsObject;
    Context c;
    String[] tabheadings;
    public static int OFFSET_PAGE_LIMIT = 3;

    //data variables
    String vendorId,vendorTitle;

    public VendorPagerAdapter(FragmentManager fm,Context c, VendorDetailsObject vendorDetailsObject) {
        super(fm);
        this.c = c;
        this.vendorId = vendorDetailsObject.vendorId;
        this.vendorTitle = vendorDetailsObject.name;
        this.vendorDetailsObject = vendorDetailsObject;
        tabheadings = c.getResources().getStringArray(R.array.vendortabheadings);
    }

    @Override
    public Fragment getItem(int position) {
        AsyncGet.dismissDialog();
        switch (position){
            case 0: return VendorHomeFragment.newInstance(vendorDetailsObject);
            case 1: return RewardsFragment.newInstance(vendorId);
            case 2: return MyStampsFragment.newInstance(vendorId);
            case 3: return VendorSettingsFragment.newInstance(vendorDetailsObject);

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
