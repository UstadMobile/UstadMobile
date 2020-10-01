package com.ustadmobile.port.android.view

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.TextView
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ActivityEpubContentBinding
import com.toughra.ustadmobile.databinding.ItemEpubcontentViewBinding
import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem
import com.ustadmobile.core.controller.EpubContentPresenter
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UMAndroidUtil.bundleToMap
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.EpubContentView
import com.ustadmobile.sharedse.network.NetworkManagerBle
import kotlinx.android.synthetic.main.appbar_material_with_progress.view.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking

class EpubContentActivity : UstadBaseActivity(),EpubContentView, AdapterView.OnItemClickListener, TocListView.OnItemClickListener {


    /** The Page Adapter used to manage swiping between epub pages  */
    private var mContentPagerAdapter: EpubContentPagerAdapter? = null

    private var mPresenter: EpubContentPresenter? = null

    private var mSavedInstanceState: Bundle? = null

    override val viewContext: Any
        get() = this

    private lateinit var mBinding: ActivityEpubContentBinding

    private lateinit var recyclerViewLinearLayout: LinearLayoutManager

    override var containerTitle: String? = null
        set(value) {
            field = value
            title = value
            mBinding.containerTitle = value
        }


    override var spineUrls: List<String>? = null
        set(value) {
            value?.also { mContentPagerAdapter?.submitList(value) }
            field = value
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
            loading = false
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
            scrollToSpinePosition(value, "")

            field = value
        }

    override fun scrollToSpinePosition(spinePosition: Int, hashAnchor: String?) {
        mBinding.epubPageRecyclerView.post {
            recyclerViewLinearLayout.scrollToPositionWithOffset(spinePosition, 0)
        }



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


    private class EpubContentPagerAdapter :
            ListAdapter<String, EpubContentPagerAdapter.EpubContentViewHolder>(URL_DIFFUTIL) {

        private var webViewTouchHandler: Handler = Handler()

        private var gestureDetector: GestureDetectorCompat? = null

        private val boundHolders = mutableListOf<EpubContentViewHolder>()

        //This is a map of any anchors that should be scrolled to.
        private val anchorsToScrollTo = mutableMapOf<Int, String?>()

        inner class EpubContentViewHolder internal constructor(val mBinding: ItemEpubcontentViewBinding) :
                RecyclerView.ViewHolder(mBinding.root) {

            var pageIndex: Int = -1

            fun scrollToAnchor(anchor: String) {
                //mBinding.epubContentview.evaluateJavascript()
            }


        }

        @SuppressLint("SetJavaScriptEnabled", "ObsoleteSdkInt", "ClickableViewAccessibility")
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpubContentViewHolder {
            val mBinding = ItemEpubcontentViewBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false)

            if (Build.VERSION.SDK_INT >= 17) {
                mBinding.epubContentview.settings?.mediaPlaybackRequiresUserGesture = false
            }

            mBinding.epubContentview.settings.javaScriptEnabled = true
            mBinding.epubContentview.settings.domStorageEnabled = true
            mBinding.epubContentview.settings.cacheMode = WebSettings.LOAD_DEFAULT
            mBinding.epubContentview.webViewClient = WebViewClient()
            mBinding.epubContentview.webChromeClient = WebChromeClient()

            return EpubContentViewHolder(mBinding)
        }

        override fun onBindViewHolder(holderContent: EpubContentViewHolder, position: Int) {
            holderContent.mBinding.epubContentview.loadUrl(getItem(position))
            holderContent.pageIndex = position

            val scrollToAnchor = anchorsToScrollTo[position]
            if(scrollToAnchor != null) {
                holderContent.scrollToAnchor(scrollToAnchor)
                anchorsToScrollTo[position] = null
            }
        }

        fun scrollToAnchor(position: Int, anchorName: String) {
            val boundHolder = boundHolders.filter { it.pageIndex == position }.firstOrNull()
            if(boundHolder != null) {
                boundHolder.scrollToAnchor(anchorName)
            }else {
                anchorsToScrollTo[position] = anchorName
            }
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            gestureDetector = null
        }

        companion object{
            const val HANDLER_CLICK_ON_VIEW = 2
        }
    }

    override fun onClick(item: Any?, view: View) {
        val navItem = item as EpubNavItem?
        if(navItem != null)mPresenter?.handleClickNavItem(navItem)
        pageTitle = navItem?.title
        mBinding.containerDrawerLayout.closeDrawers()
    }

    val onPageChangeCallback = object: ViewPager2.OnPageChangeCallback() {

        override fun onPageSelected(position: Int) {
            mPresenter?.handlePageChanged(position)
        }

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
        mSavedInstanceState = savedInstanceState
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_epub_content)

        setSupportActionBar(mBinding.root.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (!UstadMobileSystemImpl.instance.getAppConfigBoolean(AppConfig.KEY_EPUB_TOC_ENABLED,
                        this)) {
            mBinding.containerDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }

        mContentPagerAdapter = EpubContentPagerAdapter()
        recyclerViewLinearLayout = LinearLayoutManager(this)

        mBinding.epubPageRecyclerView.layoutManager = recyclerViewLinearLayout
        mBinding.epubPageRecyclerView.adapter = mContentPagerAdapter

        mPresenter = EpubContentPresenter(this, bundleToMap(intent.extras),
                this, di)
        loading = true
        mPresenter?.onCreate(savedInstanceState.toStringMap())
    }

    override fun onStart() {
        super.onStart()

        mPresenter?.onStart()
    }

    override fun onStop() {
        super.onStop()

        mPresenter?.onStop()
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
        mBinding.epubPageRecyclerView.adapter = null
        mSavedInstanceState = null
        mPresenter = null
        mContentPagerAdapter = null
        coverImageUrl = null
        containerTitle = null
        tableOfContents = null
        pageTitle = null
        super.onDestroy()
    }

    companion object {

        private val URL_DIFFUTIL = object:  DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }
        }

    }

}
