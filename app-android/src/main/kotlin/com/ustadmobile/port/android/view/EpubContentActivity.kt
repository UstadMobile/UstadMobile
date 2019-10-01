package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem
import com.ustadmobile.core.controller.EpubContentPresenter
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UMAndroidUtil.bundleToMap
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.EpubContentView
import java.util.*

class EpubContentActivity : ZippedContentActivity(), EpubContentView, AdapterView.OnItemClickListener, TocListView.OnItemClickListener {


    /** The ViewPager used to swipe between epub pages  */
    private var mPager: ViewPager? = null

    /** The Page Adapter used to manage swiping between epub pages  */
    private var mPagerAdapter: ContainerViewPagerAdapter? = null

    private var mEpubContentPresenter: EpubContentPresenter? = null

    val baseURL: String? = null

    private var mDrawerLayout: DrawerLayout? = null

    private var tocList: TocListView? = null

    private var coverImageView: ImageView? = null

    override val viewContext: Any
        get() = this

    override fun onCreate(saved: Bundle?) {
        super.onCreate(saved)

        setContentView(R.layout.activity_epub_content)
        mDrawerLayout = findViewById(R.id.container_drawer_layout)

        val toolbar = findViewById<Toolbar>(R.id.um_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        if (!UstadMobileSystemImpl.instance.getAppConfigBoolean(AppConfig.KEY_EPUB_TOC_ENABLED,
                          this)) {
            mDrawerLayout!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }

        mPager = findViewById<View>(R.id.container_epubrunner_pager) as ViewPager
        tocList = findViewById<View>(R.id.activity_container_epubpager_toclist) as TocListView
        coverImageView = findViewById<View>(R.id.item_basepoint_cover_img) as ImageView

        mEpubContentPresenter = EpubContentPresenter(this,
                bundleToMap(intent.extras), this)
        val savedHt = UMAndroidUtil.bundleToHashtable(saved)
        mEpubContentPresenter!!.onCreate(savedHt)
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
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_epub_content, menu)
        return true
    }

    /**
     * Handle when the user has tapped an item from the table of contents on the drawer
     *
     * @param adapterView
     * @param view
     * @param i
     * @param l
     */
    override fun onItemClick(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
        mDrawerLayout!!.closeDrawers()
    }

    override fun setSpineUrls(urls: Array<String>?, index : Int) {
        mPagerAdapter = ContainerViewPagerAdapter(supportFragmentManager,
                urls!!)
        mPager!!.offscreenPageLimit = 1
        mPager!!.adapter = mPagerAdapter
        mPager!!.setCurrentItem(index, true)
    }

    override fun setPageTitle(pageTitle: String?) {
        title = pageTitle
    }

    override fun onDestroy() {
        mEpubContentPresenter!!.onDestroy()
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_epub_content_showtoc) {
            mDrawerLayout!!.openDrawer(GravityCompat.END)
            return true
        } else if (item.itemId == android.R.id.home) {
            finish()
            return true
        }


        return super.onOptionsItemSelected(item)
    }

    override fun setContainerTitle(title: String) {
        setTitle(title)
    }

    override fun setProgressBarVisible(progressVisible: Boolean) {
        findViewById<View>(R.id.progressBar).visibility = if (progressVisible) View.VISIBLE else View.GONE
    }

    override fun setProgressBarProgress(progress: Int) {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        if (progress == -1) {
            progressBar.isIndeterminate = true
        } else {
            progressBar.progress = progress
        }
    }

    override fun setCoverImage(imageUrl: String) {
        Picasso.get().load(imageUrl).into(coverImageView)
    }

    override fun setTableOfContents(tocNavItem: EpubNavItem) {
        tocList!!.setAdapter(ContainerTocListAdapter(tocNavItem))
        tocList!!.setOnItemClickListener(this)
    }

    override fun onClick(item: Any?, view: View) {
        val navItem = item as EpubNavItem?
        mEpubContentPresenter!!.handleClickNavItem(navItem!!)
        mDrawerLayout!!.closeDrawers()
    }


    override fun goToLinearSpinePosition(spinePos: Int) {
        mPager!!.setCurrentItem(spinePos, true)
    }

    private inner class ContainerTocListAdapter(private val rootItem: EpubNavItem) : TocListView.TocListViewAdapter() {

        override val root: Any
            get() = rootItem

        override fun getChildren(node: Any?): List<*>? {
            return (node as EpubNavItem).getChildren()
        }

        override fun getNumChildren(node: Any?): Int {
            return (node as EpubNavItem).size()
        }

        override fun getNodeView(node: Any, recycleView: View?, depth: Int): View {
            var recycleView = recycleView
            if (recycleView == null) {
                val inflater = LayoutInflater.from(this@EpubContentActivity)
                recycleView = inflater.inflate(R.layout.item_epubview_child, null)
            }

            val expandedTextView = recycleView!!.findViewById<TextView>(R.id.expandedListItem)
            expandedTextView.text = node.toString()

            return recycleView
        }
    }


    /**
     * A simple pager adapter that uses an array of urls (as a string
     * array) to generate a fragment that has a webview showing that
     * URL
     *
     */
    private class ContainerViewPagerAdapter(fm: FragmentManager,
                                            /**
                                             * Array of the page HREF items to be shown
                                             */
                                            private val urlList: Array<String>) : FragmentStatePagerAdapter(fm) {


        internal var pagesMap: WeakHashMap<Int, EpubContentPageFragment> = WeakHashMap()

        override
                /**
                 * Generate the Fragment for that position
                 *
                 * @see com.ustadmobile.contentviewpager.ContentViewPagerPageFragment
                 *
                 *
                 * @param position Position in the list of fragment to create
                 */
        fun getItem(position: Int): Fragment {
            val existingFrag = pagesMap[position]

            if (existingFrag != null) {
                return existingFrag
            } else {
                val frag = EpubContentPageFragment.newInstance(urlList[position], position)

                this.pagesMap[position] = frag
                return frag
            }
        }

        override fun getCount(): Int {
            return urlList.size
        }
    }

    override fun setAuthorName(authorName: String) {
        (findViewById<View>(R.id.activity_container_epubpager_auuthor_text) as TextView).text = authorName
    }
}
