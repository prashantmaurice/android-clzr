package com.clozerr.app;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


public class VendorActivity extends ActionBarActivity {

    private Toolbar toolbar;
    private ViewPager pager;
    private SlidingTabLayout mtabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor);
        toolbar=(Toolbar)findViewById(R.id.toolbar_vendor);
        if (toolbar != null) {
            toolbar.setTitle("Club 001");}
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        pager=(ViewPager)findViewById(R.id.pager_vendor);
        pager.setAdapter(new VendorPagerAdapter(getSupportFragmentManager(),VendorActivity.this));
        mtabs=(SlidingTabLayout)findViewById(R.id.tabs_vendor);
        mtabs.setDistributeEvenly(true);
        mtabs.setCustomTabView(R.layout.custom_tab_view, R.id.tabtitle);
        mtabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.colorAccent);
            }
        });
        mtabs.setViewPager(pager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_vendor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
