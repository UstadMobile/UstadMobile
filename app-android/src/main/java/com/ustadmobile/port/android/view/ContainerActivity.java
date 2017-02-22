package com.ustadmobile.port.android.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.joanzapata.pdfview.PDFView;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.MessageIDConstants;
import com.ustadmobile.core.controller.ContainerController;
import com.ustadmobile.core.controller.ControllerReadyListener;
import com.ustadmobile.core.controller.UstadController;
import com.ustadmobile.core.epubnav.EPUBNavDocument;
import com.ustadmobile.core.epubnav.EPUBNavItem;
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
import com.ustadmobile.port.android.impl.http.HTTPService;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;

public class ContainerActivity extends UstadBaseActivity implements ContainerPageFragment.OnFragmentInteractionListener, ControllerReadyListener, ContainerView, AppViewChoiceListener, TinCanResultListener, ListView.OnItemClickListener {


    /** The ViewPager used to swipe between epub pages */
    private ViewPager mPager;

    /** The Page Adapter used to manage swiping between epub pages */
    private ContainerViewPagerAdapter mPagerAdapter;

    private String onpageSelectedJS = "";


    private String mContainerURI;

    private String mMimeType;

    private HTTPService mHttpService;

    private boolean mBound = false;

    protected boolean inUse = false;

    private ContainerController mContainerController;

    private String mBaseURL = null;

    private String mMountedPath;

    //Key when saving state for the current page
    private static final String OUTSTATE_CURRENTITEM = "currentitem";

    //Key when saving state for the current mount point
    private static final String OUTSTATE_MOUNTPOINT = "mountpt";

    private int mSavedPosition = -1;

    private String mSavedMountPoint;

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


    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);

        setContentView(R.layout.activity_container_epubpager);

        InputStream is = null;
        try {
            AssetManager asMgr = getApplicationContext().getAssets();
            is = asMgr.open("onpageshow.js");
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
            mSavedMountPoint = saved.getString(OUTSTATE_MOUNTPOINT);
        }

        Toolbar toolbar = (Toolbar)findViewById(R.id.container_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.container_drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

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
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerList = (ListView)findViewById(R.id.container_tocdrawer);
        mDrawerList.setOnItemClickListener(this);
    }

    @Override
    public void onHttpServiceConnected(HTTPService service) {
        super.onHttpServiceConnected(service);
        mHttpService = service;
        onpageSelectedJS = onpageSelectedJS.replace("__ASSETSURL__", mHttpService.getAssetsBaseURL());
        ContainerActivity.this.initContent();
    }

    @Override
    public void onHttpServiceDisconnected() {
        super.onHttpServiceDisconnected();
        mHttpService = null;
    }

    /*

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            HTTPService.HTTPBinder binder = (HTTPService.HTTPBinder)service;
            mHttpService = binder.getService();

            onpageSelectedJS = onpageSelectedJS.replace("__ASSETSURL__", mHttpService.getAssetsBaseURL());
            ContainerActivity.this.initContent();
        }

        public void onServiceDisconnected(ComponentName arg0) {
            mHttpService = null;
        }

    };
    */

    public void initContent() {
        mMountedPath = mHttpService.mountZIP(ContainerActivity.this.mContainerURI, mSavedMountPoint);
        mBaseURL = UMFileUtil.joinPaths(new String[]{mHttpService.getBaseURL(), mMountedPath});
        mArgs.put(ContainerController.ARG_OPENPATH, mBaseURL);
        UstadMobileSystemImpl.l(UMLog.INFO, 365, mContainerURI + "on " + mBaseURL + " type "
                + mMimeType);
        ContainerController.makeControllerForView(this, mArgs, this);
    }

    @Override
    public void controllerReady(final UstadController controller, int flags) {
        final Context ctx = this;
        String navDocHREF = null;
        try {
            /*
            Load the navigation document here - this event handler is on the thread that loaded
            the controller itself - thus off the UI thread.  The controller itself won't load this
            as the J2ME version only loads the navigation when the user goes to the TOC page
            */
            ContainerController containerCtrl =(ContainerController)controller;
            navDocHREF = containerCtrl.resolveHREFS(containerCtrl.getActiveOPF(),
                    new String[]{containerCtrl.getActiveOPF().navItem.href}, null)[0];
            HTTPResult navDocResult= UstadMobileSystemImpl.getInstance().makeRequest(navDocHREF,
                    null, null);

            navDocument = EPUBNavDocument.load(new ByteArrayInputStream(navDocResult.getResponse()));
        }catch(Exception e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 166, navDocHREF, e);
        }


        runOnUiThread(new Runnable() {
            public void run() {
                if (controller != null) {
                    mContainerController = (ContainerController) controller;
                    setupFromController((ContainerController) controller);
                } else {
                    UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
                    impl.getAppView(ctx).showAlertDialog(impl.getString(MessageIDConstants.error),
                            impl.getString(MessageIDConstants.could_not_open_file));
                }
            }
        });
    }



    protected void setupFromController(ContainerController controller) {
        //TODO: Deal with other content types here - but for right now we only have EPUB
        setBaseController(controller);
        mContainerController.setUIStrings();
        if(mMimeType.startsWith("application/epub+zip")) {
            showEPUB();
        }else if(mMimeType.startsWith("application/pdf")) {
            showPDF();
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
        mContainerController.setRegistrationUUID(resumableRegistrations[choice].uuid);
        showEPUB();
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
     * Show a PDF container using
     */
    public void showPDF() {
        com.joanzapata.pdfview.PDFView pdfView;
        RelativeLayout container = (RelativeLayout)findViewById(R.id.container_relative_layout);
        container.removeView(findViewById(R.id.container_epubrunner_pager));
        pdfView = (PDFView)getLayoutInflater().inflate(R.layout.item_container_pdfview, null);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.BELOW, R.id.container_toolbar);
        container.addView(pdfView, params);
        setContainerTitle(UMFileUtil.getFilename(mContainerURI));
        pdfView.fromFile(new File(UMFileUtil.stripPrefixIfPresent("file:///", mContainerURI)))
            .enableSwipe(true).load();
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

    public void showEPUB() {
        String[] urlArray = null;
        Exception exc = null;
        try {
            urlArray = mContainerController.getActiveOPF().getLinearSpineHREFs();
            UstadMobileSystemImpl.getInstance().queueTinCanStatement(
                mContainerController.makeLaunchedStatement(), getContext());
        }catch(Exception e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 163, null, e);
            exc = e;
        }

        if(urlArray != null) {
            setContainerTitle(mContainerController.getActiveOPF().title);
            mPager = (ViewPager) findViewById(R.id.container_epubrunner_pager);
            final int numPages = urlArray.length;
            mPagerAdapter = new ContainerViewPagerAdapter(getSupportFragmentManager(),
                    mContainerController.getOPFBasePath(mContainerController.getActiveOPF()),
                    mContainerController.getActiveOPF().getLinearSpineHREFs(),
                    mContainerController.getXAPIQuery());
            mPager.setOffscreenPageLimit(1);
            mPager.setAdapter(mPagerAdapter);
            if(mSavedPosition != -1) {
                mPager.setCurrentItem(mSavedPosition);
            }



            mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int pos) {
                    ContainerPageFragment frag = (ContainerPageFragment) mPagerAdapter.getItem(pos);
                    frag.evaluateJavascript(onpageSelectedJS);
                    frag.showPagePosition(pos+1, numPages);
                    updateTOCSelection(frag.getPageHref());
                    String pageTitle = frag.getPageTitle();
                    if(pageTitle != null) {
                        setTitle(frag.getPageTitle());
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

        }else {
            UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            String message = "what a terrible failure: " + exc.toString();
            exc.printStackTrace();
            impl.getAppView(this).showAlertDialog(impl.getString(MessageIDConstants.error), message);
        }

        if(navDocument != null) {
            //show TOC
            Vector navVector = navDocument.getNavById("toc").getChildrenRecursive(new Vector());
            drawerNavItems = new EPUBNavItem[navVector.size()];
            navVector.toArray(drawerNavItems);

            mDrawerList.setAdapter(new ArrayAdapter<EPUBNavItem>(this, R.layout.item_containerview_toc,
                    drawerNavItems));
            mDrawerList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            mDrawerList.setItemChecked(0, true);
        }
    }

    @Override
    public boolean refreshURLs() {
        boolean success = false;
        try {
            mPagerAdapter.updatePageProps(mContainerController.getOPFBasePath(
                    mContainerController.getActiveOPF()),
                    mContainerController.getActiveOPF().getLinearSpineHREFs(),
                    mContainerController.getXAPIQuery(), true);
            success = true;
        }catch(Exception e) {
            UstadMobileSystemImpl.getInstance().l(UMLog.ERROR, 197, null, e);
        }

        return success;
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

    @Override
    public void onResume() {
        super.onResume();
        inUse = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        inUse = false;
    }

    public void onStop() {
        super.onStop();
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityStop(this);
    }

    public void handlePageTitleUpdated(int index, String title) {
        if(mPager.getCurrentItem() == index && mContainerController != null) {
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
            mHttpService.ummountZIP(mMountedPath);
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
        }

        switch(item.getItemId()) {
            case ContainerController.CMD_RESUME_SESSION:
                mContainerController.handleClickResumableRegistrationMenuItem();
                return true;
            case R.id.action_leavecontainer:
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
                        ContainerPageFragment.newInstance(baseURI, hrefList[position], query, position);

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

}
