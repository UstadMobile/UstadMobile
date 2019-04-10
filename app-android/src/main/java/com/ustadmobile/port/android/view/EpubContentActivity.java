package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.EpubContentPresenter;
import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem;
import com.ustadmobile.core.impl.AppConfig;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.EpubContentView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Hashtable;
import java.util.List;
import java.util.WeakHashMap;

import static com.ustadmobile.port.android.util.UMAndroidUtil.bundleToMap;

public class EpubContentActivity extends ZippedContentActivity implements
        EpubContentView, ListView.OnItemClickListener,
        TocListView.OnItemClickListener, EpubContentPageFragment.TapToHideToolbarHandler{


    /** The ViewPager used to swipe between epub pages */
    private ViewPager mPager;

    /** The Page Adapter used to manage swiping between epub pages */
    private ContainerViewPagerAdapter mPagerAdapter;

    private EpubContentPresenter mEpubContentPresenter;

    private String mBaseURL = null;

    private DrawerLayout mDrawerLayout;

    private TocListView tocList;

    private ImageView coverImageView;

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);

        setContentView(R.layout.activity_epub_content);
        mDrawerLayout = findViewById(R.id.container_drawer_layout);

        Toolbar toolbar = findViewById(R.id.um_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(!UstadMobileSystemImpl.Companion.getInstance().getAppConfigBoolean(AppConfig.INSTANCE.getKEY_EPUB_TOC_ENABLED(),
                getContext())) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

        mPager = (ViewPager) findViewById(R.id.container_epubrunner_pager);
        tocList = (TocListView)findViewById(R.id.activity_container_epubpager_toclist);
        coverImageView = (ImageView)findViewById(R.id.item_basepoint_cover_img);

        mEpubContentPresenter = new EpubContentPresenter(this,
                bundleToMap(getIntent().getExtras()), this);
        Hashtable savedHt = UMAndroidUtil.bundleToHashtable(saved);
        mEpubContentPresenter.onCreate(savedHt);
    }

    public String getBaseURL() {
        return mBaseURL;
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
        inflater.inflate(R.menu.menu_epub_content, menu);
        return true;
    }


    @Override
    public void onTap(int pageIndex) {
        if(getSupportActionBar().isShowing()) {
            getSupportActionBar().hide();
        }else {
            getSupportActionBar().show();
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
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void setSpineUrls(String[] spineUrls) {
        mPagerAdapter = new ContainerViewPagerAdapter(getSupportFragmentManager(),
                spineUrls);
        mPager.setOffscreenPageLimit(1);
        mPager.setAdapter(mPagerAdapter);
    }

    @Override
    public void setPageTitle(String pageTitle) {
        setTitle(pageTitle);
    }

    public void onDestroy() {
        mEpubContentPresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_epub_content_showtoc) {
            mDrawerLayout.openDrawer(Gravity.END);
            return true;
        }else if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setContainerTitle(String title) {
        setTitle(title);
    }

    @Override
    public void setProgressBarVisible(boolean progressVisible) {
        findViewById(R.id.progressBar).setVisibility(progressVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setProgressBarProgress(int progress) {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        if(progress == -1) {
            progressBar.setIndeterminate(true);
        }else {
            progressBar.setProgress(progress);
        }
    }

    @Override
    public void setCoverImage(String imageUrl) {
        Picasso.with(this).load(imageUrl).into(coverImageView);
    }

    @Override
    public Object getContext() {
        return this;
    }

    @Override
    public void setTableOfContents(EpubNavItem tocNavItem) {
        tocList.setAdapter(new ContainerTocListAdapter(tocNavItem));
        tocList.setOnItemClickListener(this);
    }

    @Override
    public void onClick(Object item, View view) {
        EpubNavItem navItem = (EpubNavItem)item;
        mEpubContentPresenter.handleClickNavItem(navItem);
        mDrawerLayout.closeDrawers();
    }


    @Override
    public void goToLinearSpinePosition(int spinePos) {
        mPager.setCurrentItem(spinePos, true);
    }

    private class ContainerTocListAdapter extends TocListView.TocListViewAdapter{

        private EpubNavItem rootItem;

        private ContainerTocListAdapter(EpubNavItem rootItem) {
            this.rootItem = rootItem;
        }

        @Override
        public Object getRoot() {
            return rootItem;
        }

        @Override
        public List getChildren(Object node) {
            return ((EpubNavItem)node).getChildren();
        }

        @Override
        public int getNumChildren(Object node) {
            return ((EpubNavItem)node).size();
        }

        @Override
        public View getNodeView(Object node, View recycleView, int depth) {
            if(recycleView == null) {
                LayoutInflater inflater = LayoutInflater.from(EpubContentActivity.this);
                recycleView = inflater.inflate(R.layout.item_epubview_child, null);
            }

            TextView expandedTextView = recycleView.findViewById(R.id.expandedListItem);
            expandedTextView.setText(node.toString());

            return recycleView;
        }
    }



    /**
     * A simple pager adapter that uses an array of urls (as a string
     * array) to generate a fragment that has a webview showing that
     * URL
     *
     */
    private static class ContainerViewPagerAdapter extends FragmentStatePagerAdapter {


        WeakHashMap<Integer, EpubContentPageFragment> pagesMap;

        /**
         * Array of the page HREF items to be shown
         */
        private String[] urlList;

        public ContainerViewPagerAdapter(FragmentManager fm, String[] urlList) {
            super(fm);
            this.urlList = urlList;
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
            EpubContentPageFragment existingFrag = pagesMap.get(position);

            if(existingFrag != null) {
                return existingFrag;
            }else {
                EpubContentPageFragment frag =
                        EpubContentPageFragment.newInstance(urlList[position], position);

                this.pagesMap.put(position, frag);
                return frag;
            }
        }

        @Override
        public int getCount() {
            return urlList.length;
        }
    }

    @Override
    public void setAuthorName(String authorName) {
        ((TextView)findViewById(R.id.activity_container_epubpager_auuthor_text))
                .setText(authorName);
    }
}
