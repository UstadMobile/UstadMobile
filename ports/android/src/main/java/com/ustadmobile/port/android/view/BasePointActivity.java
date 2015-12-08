package com.ustadmobile.port.android.view;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.U;
import com.ustadmobile.core.controller.BasePointController;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.BasePointView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;

import java.util.Hashtable;
import java.util.WeakHashMap;

public class BasePointActivity extends UstadBaseActivity implements BasePointView{

    protected BasePointController mBasePointController;

    protected BasePointPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_point);
        Hashtable args = UMAndroidUtil.bundleToHashtable(getIntent().getExtras());

        //make OPDS fragments and set them here
        mBasePointController = BasePointController.makeControllerForView(this, args);
        setBaseController(mBasePointController);
        setUMToolbar();

        mPagerAdapter = new BasePointPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager)findViewById(R.id.basepoint_pager);
        viewPager.setAdapter(mPagerAdapter);
        TabLayout tabLayout = (TabLayout)findViewById(R.id.basepoint_tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_sd_card_black_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_public_black_24dp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_basepoint_addfeed:
                int x = 100;
                int y = x +1;
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class BasePointPagerAdapter extends FragmentStatePagerAdapter {

        private int[] tabTitles = new int[]{U.id.downloaded_items, U.id.browse_feeds};

        private WeakHashMap<Integer, CatalogOPDSFragment> fragmentMap;

        private int[] tabIconsIds = new int[]{R.drawable.ic_sd_storage_white_24dp,
                R.drawable.ic_public_white_24dp};


        public BasePointPagerAdapter(FragmentManager fm) {
            super(fm);
            fragmentMap = new WeakHashMap<>();
        }


        @Override
        public Fragment getItem(int position) {
            CatalogOPDSFragment fragment = fragmentMap.get(position);
            if(fragment == null) {
                Hashtable posArgs =
                    BasePointActivity.this.mBasePointController.getCatalogOPDSArguments(position);
                Bundle bundle = UMAndroidUtil.hashtableToBundle(posArgs);
                if(position == BasePointController.INDEX_BROWSEFEEDS) {
                    bundle.putInt(CatalogOPDSFragment.ARG_MENUID, R.menu.menu_basepoint_remotefeeds);
                }
                fragment = CatalogOPDSFragment.newInstance(bundle);
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            Drawable image = ContextCompat.getDrawable(BasePointActivity.this,
                    tabIconsIds[position]);
            int w = image.getIntrinsicWidth();
            int h = image.getIntrinsicHeight();
            image.setBounds(0, 0, w,h);
            //SpannableString sb = new SpannableString(
            //        UstadMobileSystemImpl.getInstance().getString(tabTitles[position]));
            SpannableString sb = new SpannableString(" ");
            ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
            sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


            //return sb;
            return UstadMobileSystemImpl.getInstance().getString(tabTitles[position]);
        }


    }
}
