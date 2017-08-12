package com.ustadmobile.port.android.view;


import com.toughra.ustadmobile.R;
import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.controller.BasePointController;
import com.ustadmobile.core.controller.WelcomeController;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.BasePointMenuItem;
import com.ustadmobile.core.view.BasePointView;
import com.ustadmobile.core.view.DialogResultListener;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.ustadmobile.port.android.view.slidingtab.SlidingTabLayout;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;

import java.util.Hashtable;
import java.util.WeakHashMap;

public class BasePointActivity extends UstadBaseActivity implements BasePointView,
        NavigationView.OnNavigationItemSelectedListener, DialogResultListener,
        View.OnClickListener {

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

    private BasePointMenuItem[] mNavigationDrawerItems;

    private AlertDialog shareAppDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_base_point);
        Hashtable args = UMAndroidUtil.bundleToHashtable(getIntent().getExtras());
        Hashtable savedInstanceHt = UMAndroidUtil.bundleToHashtable(savedInstanceState);
        String recreateWelcomeVal = UstadMobileSystemImpl.getInstance().getAppPref(
                "recreate-" + BasePointController.ARG_WELCOME_SCREEN_DISPLAYED, this);

        /*
         * When recreate is manually called (e.g. in-app locale change) onSaveInstanceState is not
         * called by Android. Thus we look for a manually saved state.
         */
        if(recreateWelcomeVal != null) {
            UstadMobileSystemImpl.getInstance().setAppPref(
                    "recreate-" + BasePointController.ARG_WELCOME_SCREEN_DISPLAYED, null, this);
        }

        if(savedInstanceHt == null && recreateWelcomeVal != null) {
            savedInstanceHt.put(BasePointController.ARG_WELCOME_SCREEN_DISPLAYED, recreateWelcomeVal);
        }

        //make OPDS fragments and set them here
        mBasePointController = BasePointController.makeControllerForView(this, args, savedInstanceHt);
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

        if(!classListVisible) {
            tabs.setVisibility(View.GONE);
        }

        mDrawerLayout = (DrawerLayout)findViewById(R.id.activity_basepoint_drawlayout);
        mDrawerNavigationView = (NavigationView)findViewById(R.id.activity_basepoint_navigationview);
        mDrawerNavigationView.setNavigationItemSelectedListener(this);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open,
                R.string.drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        setMenuItems(this.mNavigationDrawerItems);

        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 600);
        startActivity(discoverableIntent);
    }

    public void setWelcomeScreenDisplayed(boolean displayed) {
        mBasePointController.setWelcomeScreenDisplayed(displayed);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBasePointController.onResume();
    }

    @Override
    public void recreate() {
        /*
         * When recreate is manually called (e.g. in-app locale change) onSaveInstanceState is not
         * called by Android
         */
        String welcomeScreenDisplayed = String.valueOf(mBasePointController.isWelcomeScreenDisplayed());
        UstadMobileSystemImpl.getInstance().setAppPref("recreate-"
                    + BasePointController.ARG_WELCOME_SCREEN_DISPLAYED, welcomeScreenDisplayed, this);
        super.recreate();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(BasePointController.ARG_WELCOME_SCREEN_DISPLAYED,
                String.valueOf(mBasePointController.isWelcomeScreenDisplayed()));
    }

    @Override
    public void setMenuItems(final BasePointMenuItem[] menuItems) {
        this.mNavigationDrawerItems = menuItems;
        runOnUiThread(new Runnable() {
            public void run() {

                if(mDrawerNavigationView == null)
                    return;//the onCreate process is underway - wait

                mDrawerNavigationView.getMenu().clear();
                if(menuItems == null)
                    return;//There are no items


                Menu drawerMenu = mDrawerNavigationView.getMenu();
                UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
                MenuItem item;
                String iconName;

                for(int i = 0; i < menuItems.length; i++) {
//                    item = drawerMenu.add(0, BASEPOINT_MENU_CMD_ID_OFFSET+ i, 0, impl.getString(menuItems[i].getTitleStringId()));
                    item = drawerMenu.add(0, BASEPOINT_MENU_CMD_ID_OFFSET+ i, 0,
                        impl.getString(menuItems[i].getTitleStringId(), BasePointActivity.this));
                    iconName = menuItems[i].getIconName();
                    if(iconName != null){
                        int resId = getResources().getIdentifier(iconName, "drawable", getPackageName());
                        if(resId > 0){
                            item.setIcon(resId);
                        }
                    }
                }
            }
        });

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
            case BasePointController.CMD_SHARE_APP:
                mBasePointController.handleClickShareApp();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, BasePointController.CMD_SHARE_APP, 0, R.string.share_application);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void refreshCatalog(int column) {
        ((CatalogOPDSFragment) mPagerAdapter.getItem(column)).loadCatalog();
    }

    @Override
    public void onDialogResult(int commandId, DismissableDialog dialog, Hashtable args) {
        mBasePointController.onDialogResult(commandId, dialog, args);
    }

    @Override
    public void setClassListVisible(boolean visible) {
        this.classListVisible= visible;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if(itemId >= BASEPOINT_MENU_CMD_ID_OFFSET && itemId < (BASEPOINT_MENU_CMD_ID_OFFSET + mNavigationDrawerItems.length)) {
            int basePointIndex = item.getItemId() - BASEPOINT_MENU_CMD_ID_OFFSET;
            mBasePointController.handleClickBasePointMenuItem(mNavigationDrawerItems[basePointIndex]);

            //We have to close the drawer; then navigate to avoid trouble -
            //this will be handled in onDrawerClosed
            openItemOnDrawerClose = mNavigationDrawerItems[basePointIndex];

            if(mDrawerLayout.isDrawerOpen(mDrawerNavigationView)) {
                mDrawerLayout.closeDrawer(mDrawerNavigationView, true);
            }

            return true;
        }

        return false;
    }

    @Override
    public void onDestroy() {
        mBasePointController.onDestroy();
        super.onDestroy();
    }


    public class BasePointPagerAdapter extends FragmentStatePagerAdapter {

        private int[] tabTitles = new int[]{MessageID.my_resources, MessageID.classes};

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
            return UstadMobileSystemImpl.getInstance().getString(tabTitles[position],
                    BasePointActivity.this);
        }


    }

    @Override
    public void showShareAppDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.share_application);
        builder.setView(R.layout.fragment_share_app_dialog);
        builder.setPositiveButton(R.string.share, null);
        builder.setNegativeButton(R.string.cancel, null);
        shareAppDialog = builder.create();
        shareAppDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button okButton = shareAppDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                okButton.setOnClickListener(BasePointActivity.this);
            }
        });
        shareAppDialog.show();
    }

    @Override
    public void onClick(View view) {
        CheckBox zipCheckbox = (CheckBox)shareAppDialog.findViewById(
                R.id.fragment_share_app_zip_checkbox);
        mBasePointController.handleClickConfirmShareApp(zipCheckbox.isChecked());
    }

    public void setShareAppDialogProgressVisible(boolean visible) {
        shareAppDialog.findViewById(R.id.fragment_share_app_progres_label).setVisibility(
            visible ? View.VISIBLE: View.GONE);
        shareAppDialog.findViewById(R.id.fragment_share_app_zip_progress_bar).setVisibility(
                visible ? View.VISIBLE: View.GONE);
        shareAppDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!visible);
        shareAppDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(!visible);
    }

    @Override
    public void dismissShareAppDialog() {
        shareAppDialog.dismiss();
        shareAppDialog = null;
    }
}
