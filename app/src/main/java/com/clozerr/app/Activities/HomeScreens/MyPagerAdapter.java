package com.clozerr.app.Activities.HomeScreens;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.clozerr.app.AsyncGet;
import com.clozerr.app.MyClubsFragment;
import com.clozerr.app.NearbyFragment;
import com.clozerr.app.R;


public class MyPagerAdapter extends FragmentPagerAdapter {
    Context c;
    String[] tabheadings;
    public MyPagerAdapter(FragmentManager fm,Context c) {
        super(fm);
        this.c=c;
        tabheadings=c.getResources().getStringArray(R.array.tabheadings);
        
    }

    @Override
    public Fragment getItem(int position) {
        AsyncGet.dismissDialog();
        switch (position){
            //case 0:CategoriesFragment myFragment=new CategoriesFragment();
            //     return myFragment;
            case 0:NearbyFragment nearbyFragment=new NearbyFragment();
                 return nearbyFragment;
            case 1:MyClubsFragment myClubsFragment = new MyClubsFragment();
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
        return tabheadings.length;
    }
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
