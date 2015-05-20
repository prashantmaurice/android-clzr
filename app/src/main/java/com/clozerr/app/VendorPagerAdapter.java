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
            case 0:VendorHomeFragment vendorhomefragment=new VendorHomeFragment();
                return vendorhomefragment;
            //TODO Interchange the code in MyStampsFragment and FreebiesFragment and interchange 2 and 1 here
            case 2:MyStampsFragment mystampsfragment=new MyStampsFragment();
                return mystampsfragment;
            case 1:FreebiesFragment freebiesfragment = new FreebiesFragment();
                return freebiesfragment;
            case 3:FreebiesFragment freebiesfragment1 = new FreebiesFragment();
                return freebiesfragment1;
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
