package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ActivityEpubContentBinding
import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem
import com.ustadmobile.core.controller.EpubContentPresenter
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UMAndroidUtil.bundleToMap
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.EpubContentView
import com.ustadmobile.core.view.MountedContainerHandler
import com.ustadmobile.sharedse.network.NetworkManagerBle
import kotlinx.android.synthetic.main.appbar_material_with_progress.view.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class EpubContentActivity : UstadBaseActivity(),EpubContentView, AdapterView.OnItemClickListener, TocListView.OnItemClickListener {


    /** The Page Adapter used to manage swiping between epub pages  */
    private var mPagerAdapter: ContainerViewPagerAdapter? = null

    private var mPresenter: EpubContentPresenter? = null

    override val viewContext: Any
        get() = this

    private lateinit var mBinding: ActivityEpubContentBinding

    override var containerTitle: String? = null
        set(value) {
            field = value
            title = value
            mBinding.containerTitle = value
        }

    override fun setSpineUrls(urls: Array<String>?, index : Int) {
        mPagerAdapter = urls?.let { ContainerViewPagerAdapter(supportFragmentManager, it) }
        mBinding.containerEpubrunnerPager.offscreenPageLimit = 1
        mBinding.containerEpubrunnerPager.adapter = mPagerAdapter
        mBinding.containerEpubrunnerPager.setCurrentItem(index, true)
    }

    override var pageTitle: String? = null
        set(value) {
            title = value
            field = value
        }
    override var tableOfContents: EpubNavItem? = null
        set(value) {
            if(value != null){
                mBinding.activityContainerEpubpagerToclist.setAdapter(ContainerTocListAdapter(value))
                mBinding.activityContainerEpubpagerToclist.setOnItemClickListener(this)
            }
            field = value
        }
    override var coverImageUrl: String? = null
        set(value) {
            mBinding.coverImage = value
            field = value
        }

    override var authorName: String = ""
        set(value) {
            mBinding.authorName = value
            field = value
        }
    override var progressVisible: Boolean = false
        set(value) {
            field = value
            mBinding.root.progressBar?.visibility = if(progressVisible) View.VISIBLE else View.GONE
        }

    override var progressValue: Int = 0
        set(value) {
            progressVisible = progressValue != 100
            if (progressValue == -1) {
                mBinding.root.progressBar?.isIndeterminate = true
            } else {
                mBinding.root.progressBar?.progress = value
            }
            field = value
        }

    override var spinePosition: Int = 0
        set(value) {
            mBinding.containerEpubrunnerPager.setCurrentItem(value, true)
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_epub_content_showtoc) {
            mBinding.containerDrawerLayout.openDrawer(GravityCompat.END)
            return true
        } else if (item.itemId == android.R.id.home) {
            finish()
            return true
        }


        return super.onOptionsItemSelected(item)
    }

    override var networkManager: CompletableDeferred<NetworkManagerBle>? = null

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
            var mView = recycleView
            if (mView == null) {
                val inflater = LayoutInflater.from(this@EpubContentActivity)
                mView = inflater.inflate(R.layout.item_epubview_child, null)
            }

            val expandedTextView = mView?.findViewById<TextView>(R.id.expandedListItem)
            expandedTextView?.text = node.toString()

            return mView as View
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
                                            private val urlList: Array<String>) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        internal var pagesMap: WeakHashMap<String, EpubContentPageFragment> = WeakHashMap()

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
            //Using the simple integer key type does not seem to allow the entry to be garbage
            // collected as expected and leads to a memory leak. Somehow the compiler or other
            // uses the same key reference.
            val strKey = "-${urlList[position]}"
            val existingFrag = pagesMap[strKey]

            return if (existingFrag != null) {
                existingFrag
            } else {
                val frag = EpubContentPageFragment.newInstance(urlList[position], position)

                this.pagesMap[strKey] = frag
                frag
            }
        }

        override fun getCount(): Int {
            return urlList.size
        }
    }

    override fun onClick(item: Any?, view: View) {
        val navItem = item as EpubNavItem?
        if(navItem != null)mPresenter?.handleClickNavItem(navItem)
        pageTitle = navItem?.title
        mBinding.containerDrawerLayout.closeDrawers()
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
     */
    override fun onItemClick(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
        mBinding.containerDrawerLayout.closeDrawers()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_epub_content)

        setSupportActionBar(mBinding.root.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (!UstadMobileSystemImpl.instance.getAppConfigBoolean(AppConfig.KEY_EPUB_TOC_ENABLED,
                        this)) {
            mBinding.containerDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }

        GlobalScope.launch(Dispatchers.Main) {
            val networkManagerBle = networkManager?.await()
            mPresenter = EpubContentPresenter(this,
                    bundleToMap(intent.extras), this@EpubContentActivity,
                    networkManagerBle?.httpd as MountedContainerHandler)
            mPresenter?.onCreate(savedInstanceState.toStringMap())
        }
    }

    override fun onBackPressed() {
        if(mBinding.containerDrawerLayout.isDrawerOpen(GravityCompat.END)){
            mBinding.containerDrawerLayout.closeDrawer(GravityCompat.END)
        }else{
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        mPresenter?.onDestroy()
        mPresenter = null
        mPagerAdapter = null
        coverImageUrl = null
        containerTitle = null
        tableOfContents = null
        pageTitle = null
        super.onDestroy()
    }

}
