package com.clozerr.app;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by srivatsan on 12/5/15.
 */
class MyPagerAdapter extends FragmentPagerAdapter {
    Context c;
    String[] tabheadings;
    public MyPagerAdapter(FragmentManager fm,Context c) {
        super(fm);
        this.c=c;
        tabheadings=c.getResources().getStringArray(R.array.tabheadings);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:NearbyFragment myFragment=new NearbyFragment();
                 return myFragment;
            case 1:NearbyFragment nearbyFragment=new NearbyFragment();
                 return nearbyFragment;
            case 2:MyClubsFragment myClubsFragment = new MyClubsFragment();
                return myClubsFragment;
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
        return 3;
    }
}
