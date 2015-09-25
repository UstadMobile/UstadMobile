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

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.U;
import com.ustadmobile.core.controller.ContainerController;
import com.ustadmobile.core.controller.ControllerReadyListener;
import com.ustadmobile.core.controller.UstadController;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.ocf.UstadOCF;
import com.ustadmobile.core.opf.UstadJSOPF;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.ViewFactory;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;
import com.ustadmobile.port.android.impl.http.HTTPService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.WeakHashMap;

public class ContainerActivity extends AppCompatActivity implements ContainerPageFragment.OnFragmentInteractionListener, ControllerReadyListener {

    private ContainerViewAndroid containerView;

    private int viewId;

    /** The ViewPager used to swipe between epub pages */
    private ViewPager mPager;

    /** The Page Adapter used to manage swiping between epub pages */
    private ContainerViewPagerAdapter mPagerAdapter;

    private String onpageSelectedJS = "";

    private Bundle mSavedInstanceState;

    private String mContainerURI;

    private String mMimeType;

    private HTTPService mHttpService;

    private boolean mBound = false;

    protected boolean inUse = false;

    @Override
    protected void onCreate(Bundle saved) {
        UstadMobileSystemImplAndroid.handleActivityCreate(this, saved);
        super.onCreate(saved);

        try {
            AssetManager asMgr = getApplicationContext().getAssets();
            InputStream is =asMgr.open("onpageshow.js");
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int bytesRead = 0;
            while((bytesRead = is.read(buf, 0, buf.length)) != -1) {
                bout.write(buf, 0, bytesRead);
            }
            is.close();
            onpageSelectedJS = "javascript:" + new String(bout.toByteArray(), "UTF-8");
        }catch(IOException e) {
            System.err.println("Error loading javascript for page changing");
            e.printStackTrace();
        }

        viewId = getIntent().getIntExtra(UstadMobileSystemImplAndroid.EXTRA_VIEWID, saved != null ?
            saved.getInt(UstadMobileSystemImplAndroid.EXTRA_VIEWID) : 0);
        containerView = ContainerViewAndroid.getViewById(viewId);
        mSavedInstanceState = saved;

        if(containerView != null) {
            mContainerURI = containerView.getContainerController().getFileURI();
            mMimeType = containerView.getContainerController().getMimeType();
        }else {
            mContainerURI = mSavedInstanceState != null && mSavedInstanceState.getString(ContainerController.ARG_CONTAINERURI) != null ?
                    mSavedInstanceState.getString(ContainerController.ARG_CONTAINERURI) : getIntent().getStringExtra(ContainerController.ARG_CONTAINERURI);
            mMimeType = mSavedInstanceState != null && mSavedInstanceState.getString(ContainerController.ARG_MIMETYPE) != null ?
                    mSavedInstanceState.getString(ContainerController.ARG_MIMETYPE) : getIntent().getStringExtra(ContainerController.ARG_MIMETYPE);
        }

        //now bind to the HTTPService - the onServiceConnected method will call initContent
        Intent intent = new Intent(this, HTTPService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void initContent() {
        if(containerView != null) {
            setupFromView(containerView, mSavedInstanceState);
        }else {
            UstadMobileSystemImpl.l(UMLog.INFO, 365, mContainerURI + " type " + mMimeType);
            containerView = (ContainerViewAndroid)ViewFactory.makeContainerView();
            ContainerController.makeControllerForView(containerView, mContainerURI, mMimeType, this);
        }
    }

    @Override
    public void controllerReady(final UstadController controller, int flags) {
        runOnUiThread(new Runnable() {
            public void run() {
                if(controller != null) {
                    containerView.setController((ContainerController)controller);
                    setupFromView(containerView, mSavedInstanceState);
                }else {
                    UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
                    impl.getAppView().showAlertDialog(impl.getString(U.id.error),
                            impl.getString(U.id.could_not_open_file));
                }
            }
        });
    }

    protected void setupFromView(ContainerViewAndroid view, Bundle savedInstanceState) {
        containerView.setContainerActivity(this);
        String mTitle = containerView.getTitle() != null ? containerView.getTitle() : "Content";
        setTitle(mTitle);

        //TODO: Deal with other content types here - but for right now we only have EPUB
        if(containerView.getContainerController().getMimeType().startsWith("application/epub+zip")) {
            setContentView(R.layout.activity_container_epubpager);

            Toolbar toolbar = (Toolbar)findViewById(R.id.container_toolbar);
            setSupportActionBar(toolbar);
            new EPUBLoaderThread(this).start();
        }


    }


    public String getAutoplayRunJavascript() {
        return onpageSelectedJS;
    }

    public ContainerViewAndroid getContainerView() {
        return containerView;
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
        outState.putString(ContainerController.ARG_CONTAINERURI, mContainerURI);
        outState.putString(ContainerController.ARG_MIMETYPE, mMimeType);
        outState.putInt(UstadMobileSystemImplAndroid.EXTRA_VIEWID, viewId);
    }

    public void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        mSavedInstanceState = null;
        mContainerURI = null;
        mMimeType = null;

        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityDestroy(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_container, menu);
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

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            HTTPService.HTTPBinder binder = (HTTPService.HTTPBinder)service;
            mHttpService = binder.getService();
            mHttpService.mountZIP(ContainerActivity.this.mContainerURI);
            ContainerActivity.this.initContent();
        }

        public void onServiceDisconnected(ComponentName arg0) {

        }

    };

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

    private static class EPUBLoaderThread extends Thread {

        private ContainerActivity activity;

        public EPUBLoaderThread(ContainerActivity activity) {
            this.activity = activity;
        }

        public void run() {
            UstadOCF ocf = null;
            String[] urlArray = null;
            Exception exc = null;
            try {
                UstadMobileSystemImplAndroid.getInstanceAndroid().waitForHTTPReady(
                    UstadMobileSystemImplAndroid.HTTP_CHECK_INTERVAL,
                    UstadMobileSystemImplAndroid.HTTP_READY_TIMEOUT);

                ocf = activity.containerView.getContainerController().getOCF();
                String opfPath = UMFileUtil.joinPaths(new String[]{
                        activity.containerView.getContainerController().getOpenPath(), ocf.rootFiles[0].fullPath});

                //TODO: One Open Container File (.epub zipped file) can contain in theory multiple publications: Show user a choice
                UstadJSOPF opf = activity.containerView.getContainerController().getOPF(0);

                String[] hrefArray = opf.getLinearSpineURLS();
                urlArray = new String[hrefArray.length];
                for(int i = 0; i < hrefArray.length; i++) {
                    urlArray[i] = UMFileUtil.resolveLink(opfPath, hrefArray[i]);
                }
            }catch(Exception e) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 163, null, e);
                exc = e;
            }

            if(urlArray != null) {
                final String[] finalURLArray = urlArray;
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        activity.mPager = (ViewPager) activity.findViewById(R.id.container_epubrunner_pager);
                        activity.mPagerAdapter = new ContainerViewPagerAdapter(
                                activity.getSupportFragmentManager(), finalURLArray);
                        activity.mPager.setAdapter(activity.mPagerAdapter);
                        activity.mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                            @Override
                            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                            }

                            @Override
                            public void onPageSelected(int position) {
                                ContainerPageFragment frag =
                                        (ContainerPageFragment) activity.mPagerAdapter.getItem(position);
                                frag.evaluateJavascript(activity.onpageSelectedJS);
                            }

                            @Override
                            public void onPageScrollStateChanged(int state) {

                            }
                        });
                    }
                });
            }else {
                UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
                String message = "what a terrible failure: " + exc.toString();
                exc.printStackTrace();
                impl.getAppView().showAlertDialog(impl.getString(U.id.error), message);
            }

        }

    }


}
