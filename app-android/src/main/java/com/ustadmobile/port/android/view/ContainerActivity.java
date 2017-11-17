package com.ustadmobile.port.android.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.controller.ContainerController;
import com.ustadmobile.core.controller.ControllerReadyListener;
import com.ustadmobile.core.controller.UstadController;
import com.ustadmobile.core.epubnav.EPUBNavDocument;
import com.ustadmobile.core.epubnav.EPUBNavItem;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.tincan.Registration;
import com.ustadmobile.core.tincan.TinCanResultListener;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.view.AppViewChoiceListener;
import com.ustadmobile.core.view.ContainerView;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;
import com.ustadmobile.port.android.netwokmanager.NetworkServiceAndroid;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;

public class ContainerActivity extends UstadBaseActivity implements ContainerPageFragment.OnFragmentInteractionListener,
        ContainerView, AppViewChoiceListener, TinCanResultListener, ListView.OnItemClickListener {


    /** The ViewPager used to swipe between epub pages */
    private ViewPager mPager;

    /** The Page Adapter used to manage swiping between epub pages */
    private ContainerViewPagerAdapter mPagerAdapter;

    private String onpageSelectedJS = "";


    private String mContainerURI;

    private String mMimeType;

    private NetworkServiceAndroid mNetworkService;

    private boolean mBound = false;

    private ContainerController mContainerController;

    private String mBaseURL = null;

    private String mMountedPath;

    //Key when saving state for the current page
    private static final String OUTSTATE_CURRENTITEM = "currentitem";

    //Key when saving state for the current mount point
    private static final String OUTSTATE_MOUNTPOINT = "mountpt";

    private int mSavedPosition = -1;

    private Hashtable mArgs;

    private Registration[] resumableRegistrations;

    private DrawerLayout mDrawerLayout;

    private ActionBarDrawerToggle mDrawerToggle;

    private ListView mDrawerList;

    private EPUBNavDocument navDocument;

    private int drawerSelectedIndex = -1;

    /**
     * Navigation items in the order in which they appear in the drawer on the left
     */
    private EPUBNavItem[] drawerNavItems;


    private Vector<Runnable> runWhenContentMounted = new Vector<>();


    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);

        setContentView(R.layout.activity_container_epubpager);
        DrawerLayout drawerLayout = (DrawerLayout)findViewById(R.id.container_drawer_layout);


        InputStream is = null;
        try {
            AssetManager asMgr = getApplicationContext().getAssets();
            is = asMgr.open("http/onpageshow.js");
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            UMIOUtils.readFully(is, bout, 1024);
            onpageSelectedJS = "javascript:" + new String(bout.toByteArray(), "UTF-8");
        }catch(IOException e) {
            System.err.println("Error loading javascript for page changing");
            e.printStackTrace();
        }finally {
            UMIOUtils.closeInputStream(is);
        }

        mContainerURI = getIntent().getStringExtra(ContainerController.ARG_CONTAINERURI);
        mMimeType = getIntent().getStringExtra(ContainerController.ARG_MIMETYPE);
        mArgs = UMAndroidUtil.bundleToHashtable(getIntent().getExtras());

        if(saved != null) {
            if(saved.getInt(OUTSTATE_CURRENTITEM, -1) != -1) {
                mSavedPosition = saved.getInt(OUTSTATE_CURRENTITEM);
            }
        }

        Toolbar toolbar = (Toolbar)findViewById(R.id.container_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.container_drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, toolbar, R.string.open, R.string.closed) {

            public void onDrawerOpened(View drawerView) {
                drawerSelectedIndex = -1;
            }

            public void onDrawerClosed(View drawerView) {
                if(drawerSelectedIndex != -1) {
                    if(drawerNavItems != null) {
                        final int fragPos = mPagerAdapter.getFragmentIndexByHREF(drawerNavItems[drawerSelectedIndex].href);
                        if(fragPos != -1) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    mPager.setCurrentItem(fragPos, true);
                                }
                            });

                            drawerSelectedIndex = -1;
                        }
                    }
                }
            }

        };

        if(!CoreBuildConfig.EPUB_TOC_ENABLED) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            mDrawerToggle.setDrawerIndicatorEnabled(false);
        }
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerList = (ListView)findViewById(R.id.container_tocdrawer);
        mDrawerList.setOnItemClickListener(this);

        mPager = (ViewPager) findViewById(R.id.container_epubrunner_pager);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder iBinder) {
        super.onServiceConnected(name, iBinder);
        if(name.getClassName().equals(NetworkServiceAndroid.class.getName())) {
            mNetworkService = ((NetworkServiceAndroid.LocalServiceBinder)iBinder).getService();
            onpageSelectedJS = onpageSelectedJS.replace("__ASSETSURL__",
                mNetworkService.getNetworkManager().getHttpAndroidAssetsUrl());
            mContainerController = new ContainerController(this, this);
            mContainerController.onCreate(UMAndroidUtil.bundleToHashtable(getIntent().getExtras()),
                    null);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        if(name.getClassName().equals(NetworkServiceAndroid.class.getName())) {
            mNetworkService = null;
        }

        super.onServiceDisconnected(name);
    }

    public String getBaseURL() {
        return mBaseURL;
    }

    public String getXapiQuery() {
        return mContainerController.getXAPIQuery();
    }

    /**
     * A runnable posted here will be run when the controller is ready. If the controller is currently
     * ready the method will be run immediately. Otherwise it will be added to a vector of Runnables
     * to run when the controller is ready.
     *
     * @param runnable
     */
    public void runWhenMounted(Runnable runnable) {
        if(mContainerController != null) {
            runnable.run();
        }else {
            runWhenContentMounted.add(runnable);
        }
    }


    @Override
    /**
     * We requested the implementation to find resumable XAPI
     * registrations - that has now been completed
     *
     *
     */
    public void resultReady(Object result) {

    }

    /**
     * The user was asked to choose from a list of available registrations: handle choice
     *
     * @param commandId The command id that was supplied when using showChoiceDialog
     * @param choice
     */
    @Override
    public void appViewChoiceSelected(int commandId, int choice) {

    }

    /**
     * Override the onCreateOptionsMenu : In Container mode we don't show the standard app menu
     * options like logout, about etc.  We show only a close button in the top right to make things
     * simple
     *
     * @param menu
     *
     * @return true as we will have added items
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_container, menu);
        return true;
    }



    /**
     * Update the selected item in the drawer
     */
    private void updateTOCSelection(String currentPageHREF) {
        if(drawerNavItems != null) {
            for(int i = 0; i < drawerNavItems.length; i++) {
                int numVisible = mDrawerList.getLastVisiblePosition() - mDrawerList.getFirstVisiblePosition();
                if(drawerNavItems[i].href != null && drawerNavItems[i].href.equals(currentPageHREF)) {
                    mDrawerList.setItemChecked(i, true);
                    mDrawerList.setSelection(Math.max(i-(numVisible/2), 0));//Put the selected item in the middle
                    return;
                }
            }
        }
    }

    /**
     * Handle when the user has tapped an item from the table of contents on the drawer
     *
     * @param adapterView
     * @param view
     * @param i
     * @param l
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        drawerSelectedIndex = i;
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void setSpineUrls(String basePath, String[] spineUrls, String query) {
        mBaseURL = basePath;
        mPagerAdapter = new ContainerViewPagerAdapter(getSupportFragmentManager(),
                basePath, spineUrls, query);
        mPager.setOffscreenPageLimit(1);
        mPager.setAdapter(mPagerAdapter);
    }

    @Override
    public void setPageTitle(String pageTitle) {
        setTitle(pageTitle);
    }

    public String getAutoplayRunJavascript() {
        return onpageSelectedJS;
    }

    public void onStart() {
        super.onStart();
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityStart(this);
    }

    public void onStop() {
        super.onStop();
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityStop(this);
    }

    public void handlePageTitleUpdated(int index, String title) {
        if(mPager != null && mPager.getCurrentItem() == index && mContainerController != null) {
            mContainerController.handlePageTitleUpdated(title);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mPager != null) {
            outState.putInt(OUTSTATE_CURRENTITEM, mPager.getCurrentItem());
        }
        if(mMountedPath != null) {
            outState.putString(OUTSTATE_MOUNTPOINT, mMountedPath);
        }
    }

    public void onDestroy() {
        if(mMountedPath != null) {
            mNetworkService.getNetworkManager().unmountZipFromHttp(mMountedPath);
        }

        mContainerURI = null;
        mMimeType = null;
        mSavedPosition = -1;

        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(handleClickAppMenuItem(item, mContainerController)) {
            return true;
        } else if(item.getItemId() == R.id.action_leavecontainer) {
            finish();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void setController(ContainerController controller) {

    }

    @Override
    public void setContainerTitle(String title) {
        ((Button)findViewById(R.id.container_tocdrawer_upbutton)).setText(title);
        setTitle(title);
    }

    @Override
    public Object getContext() {
        return this;
    }


    /**
     * A simple pager adapter that uses an array of urls (as a string
     * array) to generate a fragment that has a webview showing that
     * URL
     *
     */
    private static class ContainerViewPagerAdapter extends FragmentStatePagerAdapter {


        WeakHashMap<Integer, ContainerPageFragment> pagesMap;

        /**
         * Array of the page HREF items to be shown
         */
        private String[] hrefList;

        /**
         * Base URL of pages (directory name)
         */
        private String baseURI;

        /**
         * Query string to append to the end of each page
         */
        private String query;

        public ContainerViewPagerAdapter(FragmentManager fm, String baseURI, String[] hrefList, String query) {
            super(fm);
            this.baseURI = baseURI;
            this.hrefList = hrefList;
            this.query = query;
            this.pagesMap = new WeakHashMap<>();
        }

        @Override
        /**
         * Generate the Fragment for that position
         *
         * @see com.ustadmobile.contentviewpager.ContentViewPagerPageFragment
         *
         * @param position Position in the list of fragment to create
         */
        public Fragment getItem(int position) {
            ContainerPageFragment existingFrag = pagesMap.get(new Integer(position));

            //something wrong HERE
            if(existingFrag != null) {
                return existingFrag;
            }else {
                ContainerPageFragment frag =
                        ContainerPageFragment.newInstance(hrefList[position], position);

                this.pagesMap.put(Integer.valueOf(position), frag);
                return frag;
            }
        }

        public int getFragmentIndexByHREF(String href) {
            return Arrays.asList(hrefList).indexOf(href);
        }


        public void updatePageProps(String baseURI, String[] hrefList, String query, boolean reload) {
            this.baseURI = baseURI;
            this.hrefList = hrefList;
            this.query = query;

            Iterator<Map.Entry<Integer, ContainerPageFragment>> iterator = pagesMap.entrySet().iterator();
            ContainerPageFragment frag;
            Map.Entry<Integer, ContainerPageFragment> entry;
            while(iterator.hasNext()) {
                entry = iterator.next();
                frag = entry.getValue();
                frag.setBaseURI(baseURI, false);
                frag.setPageHref(hrefList[entry.getKey()], false);
                frag.setQuery(query, reload);
            }
        }

        @Override
        public int getCount() {
            return hrefList.length;
        }
    }


    public String getBaseUrl() {
        return mBaseURL;
    }

}
