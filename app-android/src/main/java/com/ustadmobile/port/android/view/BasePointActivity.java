package com.ustadmobile.port.android.view;


import com.toughra.ustadmobile.R;
import com.ustadmobile.core.MessageIDConstants;
import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.controller.BasePointController;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.BasePointMenuItem;
import com.ustadmobile.core.view.BasePointView;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.ustadmobile.port.android.view.slidingtab.SlidingTabLayout;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import java.util.Hashtable;
import java.util.WeakHashMap;

public class BasePointActivity extends UstadBaseActivity implements BasePointView, NavigationView.OnNavigationItemSelectedListener {

    protected BasePointController mBasePointController;

    protected BasePointPagerAdapter mPagerAdapter;

    private int[] tabIconsIds = new int[]{R.drawable.selector_tab_resources,
            R.drawable.selector_tab_classes};


    protected boolean classListVisible;

    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;

    private NavigationView mDrawerNavigationView;

    private static final int BASEPOINT_MENU_CMD_ID_OFFSET = 5000;

    private BasePointMenuItem openItemOnDrawerClose = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_base_point);
        Hashtable args = UMAndroidUtil.bundleToHashtable(getIntent().getExtras());

        //make OPDS fragments and set them here
        mBasePointController = BasePointController.makeControllerForView(this, args);
        setBaseController(mBasePointController);
        setUMToolbar(R.id.um_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPagerAdapter = new BasePointPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager)findViewById(R.id.basepoint_pager);
        viewPager.setAdapter(mPagerAdapter);
        SlidingTabLayout tabs = (SlidingTabLayout)findViewById(R.id.activity_basepoint_sliding_tab_layout);
        tabs.setDistributeEvenly(false);
        tabs.setViewPager(viewPager);
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.primary_text);
            }
        });

        mDrawerLayout = (DrawerLayout)findViewById(R.id.activity_basepoint_drawlayout);
        mDrawerNavigationView = (NavigationView)findViewById(R.id.activity_basepoint_navigationview);
        mDrawerNavigationView.setNavigationItemSelectedListener(this);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open,
                R.string.drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        populateNavigationMenu();
    }

    private void populateNavigationMenu() {
        Menu drawerMenu = mDrawerNavigationView.getMenu();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        MenuItem item;
        String iconName;
        for(int i = 0; i < CoreBuildConfig.BASEPOINT_MENU.length; i++) {
            item = drawerMenu.add(0, BASEPOINT_MENU_CMD_ID_OFFSET+ i, 0, impl.getString(CoreBuildConfig.BASEPOINT_MENU[i].getTitleStringId()));
            iconName = CoreBuildConfig.BASEPOINT_MENU[i].getIconName();
            if(iconName != null){
                int resId = getResources().getIdentifier(iconName, "drawable", getPackageName());
                if(resId > 0){
                    item.setIcon(resId);
                }
            }

        }
    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(!mDrawerLayout.isDrawerOpen(mDrawerNavigationView)){
                    mDrawerLayout.openDrawer(mDrawerNavigationView);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void refreshCatalog(int column) {
        ((CatalogOPDSFragment) mPagerAdapter.getItem(column)).loadCatalog();
    }

    @Override
    public void setClassListVisible(boolean visible) {
        this.classListVisible= visible;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if(itemId >= BASEPOINT_MENU_CMD_ID_OFFSET && itemId < BASEPOINT_MENU_CMD_ID_OFFSET+CoreBuildConfig.BASEPOINT_MENU.length) {
            int basePointIndex = item.getItemId() - BASEPOINT_MENU_CMD_ID_OFFSET;


            //We have to close the drawer; then navigate to avoid trouble -
            //this will be handled in onDrawerClosed
            openItemOnDrawerClose = CoreBuildConfig.BASEPOINT_MENU[basePointIndex];
            UstadMobileSystemImpl.getInstance().go(openItemOnDrawerClose.getDestination(), this);

            if(mDrawerLayout.isDrawerOpen(mDrawerNavigationView)) {
                mDrawerLayout.closeDrawer(mDrawerNavigationView, true);
            }

            return true;
        }

        return false;
    }

    public class BasePointPagerAdapter extends FragmentStatePagerAdapter {

        private int[] tabTitles = new int[]{MessageIDConstants.my_resources, MessageIDConstants.classes};

        private WeakHashMap<Integer, Fragment> fragmentMap;

        public BasePointPagerAdapter(FragmentManager fm) {
            super(fm);
            fragmentMap = new WeakHashMap<>();
        }


        @Override
        public Fragment getItem(int position) {
            Fragment fragment = fragmentMap.get(position);
            if(fragment == null) {
                Bundle bundle = null;
                switch(position) {
                    case BasePointController.INDEX_DOWNLOADEDENTRIES:
                        Hashtable posArgs =
                                BasePointActivity.this.mBasePointController.getCatalogOPDSArguments(position);
                        bundle = UMAndroidUtil.hashtableToBundle(posArgs);
                        fragment = CatalogOPDSFragment.newInstance(bundle);
                        break;
                    case BasePointController.INDEX_CLASSES:
                        bundle = new Bundle();
                        fragment = ClassListFragment.newInstance(bundle);
                        break;
                }
                fragmentMap.put(position, fragment);
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return BasePointActivity.this.classListVisible ? 2 : 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return UstadMobileSystemImpl.getInstance().getString(tabTitles[position]);
        }


    }
}
