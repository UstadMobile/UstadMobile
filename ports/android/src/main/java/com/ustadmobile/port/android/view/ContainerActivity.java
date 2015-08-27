package com.ustadmobile.port.android.view;

import android.content.res.AssetManager;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.ocf.UstadOCF;
import com.ustadmobile.core.opf.UstadJSOPF;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.WeakHashMap;

public class ContainerActivity extends AppCompatActivity implements ContainerPageFragment.OnFragmentInteractionListener {

    private ContainerViewAndroid containerView;

    private int viewId;

    /** The ViewPager used to swipe between epub pages */
    private ViewPager mPager;

    /** The Page Adapter used to manage swiping between epub pages */
    private ContainerViewPagerAdapter mPagerAdapter;

    private String onpageSelectedJS = "javascript: document.body.innerHTML = 'all ur bases are us';";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_container);


        viewId = getIntent().getIntExtra(UstadMobileSystemImplAndroid.EXTRA_VIEWID, 0);
        containerView = ContainerViewAndroid.getViewById(viewId);
        containerView.setContainerActivity(this);
        String mTitle = containerView.getTitle() != null ? containerView.getTitle() : "Content";
        setTitle(mTitle);

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

        initByContentType();

        Toolbar toolbar = (Toolbar)findViewById(R.id.container_toolbar);
        setSupportActionBar(toolbar);
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

    public void onDestroy() {
        super.onDestroy();
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityDestroy(this);
    }


    public void initByContentType() {
        if(containerView.getContainerController().getMimeType().startsWith("application/epub+zip")) {
            initEPUB();
        }
    }

    public void initEPUB() {
        UstadOCF ocf = null;
        String firstURL = null;

        try {
            setContentView(R.layout.activity_container_epubpager);
            mPager = (ViewPager) findViewById(R.id.container_epubrunner_pager);


            ocf = containerView.getContainerController().getOCF();
            String opfPath = UMFileUtil.joinPaths(new String[]{
                    containerView.getContainerController().getOpenPath(), ocf.rootFiles[0].fullPath});

            //TODO: One Open Container File (.epub zipped file) can contain in theory multiple publications: Show user a choice
            UstadJSOPF opf = containerView.getContainerController().getOPF(0);

            String[] hrefArray = opf.getLinearSpineURLS();
            String[] urlArray = new String[hrefArray.length];
            for(int i = 0; i < hrefArray.length; i++) {
                urlArray[i] = UMFileUtil.resolveLink(opfPath, hrefArray[i]);
            }

            mPagerAdapter = new ContainerViewPagerAdapter(getSupportFragmentManager(), urlArray);
            mPager.setAdapter(mPagerAdapter);
            mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    ContainerPageFragment frag = (ContainerPageFragment) mPagerAdapter.getItem(position);
                    frag.evaluateJavascript(onpageSelectedJS);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }catch(Exception e) {
            e.printStackTrace();
        }
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

    /**
     * A simple pager adapter that uses an array of urls (as a string
     * array) to generate a fragment that has a webview showing that
     * URL
     *
     */
    private class ContainerViewPagerAdapter extends FragmentStatePagerAdapter {


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
