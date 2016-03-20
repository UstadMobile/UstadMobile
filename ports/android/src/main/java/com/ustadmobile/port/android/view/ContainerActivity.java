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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.joanzapata.pdfview.PDFView;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.U;
import com.ustadmobile.core.controller.ContainerController;
import com.ustadmobile.core.controller.ControllerReadyListener;
import com.ustadmobile.core.controller.LoginController;
import com.ustadmobile.core.controller.UstadController;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.ocf.UstadOCF;
import com.ustadmobile.core.opf.UstadJSOPF;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.util.UMTinCanUtil;
import com.ustadmobile.core.view.ContainerView;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;
import com.ustadmobile.port.android.impl.http.HTTPService;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.WeakHashMap;

public class ContainerActivity extends UstadBaseActivity implements ContainerPageFragment.OnFragmentInteractionListener, ControllerReadyListener, ContainerView {


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

    private UstadJSOPF mOPF;

    //Key when saving state for the current page
    private static final String OUTSTATE_CURRENTITEM = "currentitem";

    private int mSavedPosition = -1;

    private Hashtable mArgs;

    private int lastPageShown = -1;

    @Override
    protected void onCreate(Bundle saved) {
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityCreate(this, saved);
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

        if(saved != null && saved.getInt(OUTSTATE_CURRENTITEM, -1) != -1) {
            mSavedPosition = saved.getInt(OUTSTATE_CURRENTITEM);
        }

        Toolbar toolbar = (Toolbar)findViewById(R.id.container_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //now bind to the HTTPService - the onServiceConnected method will call initContent
        Intent intent = new Intent(this, HTTPService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            HTTPService.HTTPBinder binder = (HTTPService.HTTPBinder)service;
            mHttpService = binder.getService();
            ContainerActivity.this.initContent();
        }

        public void onServiceDisconnected(ComponentName arg0) {
            mHttpService = null;
        }

    };



    public void initContent() {
        mBaseURL = mHttpService.mountZIP(ContainerActivity.this.mContainerURI);
        mArgs.put(ContainerController.ARG_OPENPATH, mBaseURL);
        UstadMobileSystemImpl.l(UMLog.INFO, 365, mContainerURI + "on " + mBaseURL + " type "
                + mMimeType);
        ContainerController.makeControllerForView(this, mArgs, this);
    }

    @Override
    public void controllerReady(final UstadController controller, int flags) {
        final Context ctx = this;
        runOnUiThread(new Runnable() {
            public void run() {
                if (controller != null) {
                    mContainerController = (ContainerController) controller;
                    setupFromController((ContainerController) controller);
                } else {
                    UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
                    impl.getAppView(ctx).showAlertDialog(impl.getString(U.id.error),
                            impl.getString(U.id.could_not_open_file));
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

    /**
     * Show a PDF container using
     */
    protected void showPDF() {
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

    protected void showEPUB() {
        UstadOCF ocf = null;
        String[] urlArray = null;
        Exception exc = null;
        try {
            ocf = mContainerController.getOCF();
            String opfPath = UMFileUtil.joinPaths(new String[]{mBaseURL, ocf.rootFiles[0].fullPath});

            //TODO: One Open Container File (.epub zipped file) can contain in theory multiple publications: Show user a choice
            mOPF = mContainerController.getActiveOPF();
            mContainerController.logContainerOpened(mOPF);

            String[] hrefArray = mOPF.getLinearSpineURLS();
            String endpoint = "";
            String username = UstadMobileSystemImpl.getInstance().getActiveUser(this);
            String password = UstadMobileSystemImpl.getInstance().getActiveUserAuth(this);

            String xAPIParams = "?actor=" +
                    URLEncoder.encode(UMTinCanUtil.makeActorFromActiveUser(this).toString()) +
                    "&auth=" + URLEncoder.encode(LoginController.encodeBasicAuth(username, password)) +
                    "&endpoint=" + URLEncoder.encode(LoginController.LLRS_XAPI_ENDPOINT);

            urlArray = new String[hrefArray.length];
            for(int i = 0; i < hrefArray.length; i++) {
                urlArray[i] = UMFileUtil.resolveLink(opfPath, hrefArray[i])
                        + xAPIParams;
            }
        }catch(Exception e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 163, null, e);
            exc = e;
        }

        if(urlArray != null) {
            setContainerTitle(mOPF.title);
            mPager = (ViewPager) findViewById(R.id.container_epubrunner_pager);
            final int numPages = urlArray.length;
            mPagerAdapter = new ContainerViewPagerAdapter(getSupportFragmentManager(), urlArray);
            mPager.setAdapter(mPagerAdapter);
            if(mSavedPosition != -1) {
                mPager.setCurrentItem(mSavedPosition);
            }



            mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    ContainerPageFragment frag = (ContainerPageFragment) mPagerAdapter.getItem(
                        position);
                    frag.evaluateJavascript(onpageSelectedJS);
                    frag.showPagePosition(position+1, numPages);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }else {
            UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            String message = "what a terrible failure: " + exc.toString();
            exc.printStackTrace();
            impl.getAppView(this).showAlertDialog(impl.getString(U.id.error), message);
        }
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mPager != null) {
            outState.putInt(OUTSTATE_CURRENTITEM, mPager.getCurrentItem());
        }
    }

    public void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        mContainerURI = null;
        mMimeType = null;
        mSavedPosition = -1;

        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityDestroy(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(handleClickAppMenuItem(item, mContainerController)) {
            return true;
        }

        switch(item.getItemId()) {
            case android.R.id.home:
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
         * Array of pages to be shown
         */
        private String[] pageList;

        public ContainerViewPagerAdapter(FragmentManager fm, String[] pageList) {
            super(fm);
            this.pageList = pageList;
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
                        ContainerPageFragment.newInstance(pageList[position]);

                this.pagesMap.put(Integer.valueOf(position), frag);
                return frag;
            }
        }

        @Override
        public int getCount() {
            return pageList.length;
        }
    }

}
