package com.ustadmobile.port.android.view

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.webkit.*
import android.widget.AdapterView
import android.widget.TextView
import androidx.annotation.Keep
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.*
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ActivityEpubContentBinding
import com.toughra.ustadmobile.databinding.ItemEpubcontentViewBinding
import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem
import com.ustadmobile.core.controller.EpubContentPresenter
import com.ustadmobile.core.impl.UMAndroidUtil.bundleToMap
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.EpubContentView
import com.ustadmobile.port.android.view.ext.adjustHeightToDisplayHeight
import com.ustadmobile.port.android.view.ext.adjustHeightToWrapContent
import com.ustadmobile.core.util.ext.dpAsPx
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.port.android.view.ext.scrollToAnchor
import com.ustadmobile.sharedse.network.NetworkManagerBle
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.android.x.closestDI
import org.kodein.di.bind
import org.kodein.di.provider

class EpubContentActivity : UstadBaseActivity(),EpubContentView, AdapterView.OnItemClickListener,
        TocListView.OnItemClickListener, DIAware {

    /**
     * Javascript interface that is used as part of the system to manage scrolling to a hash link
     * e.g. #anchor
     */
    inner class ScrollDownJavascriptInterface() {

        @JavascriptInterface
        @Keep
        fun scrollDown(amount: Float) {
            mBinding.epubPageRecyclerView.post {
                //Note: the measurement as it comes from Javascript will be in density pixels (dp)
                //We need to convert this into actual pixels before scrolling
                mBinding.epubPageRecyclerView.scrollBy(0, amount.dpAsPx)
            }
        }

    }

    /**
     * Override and create a child DI to provide access to the multiplatform
     * NavController implementation.
     */
    override val di by DI.lazy {
        val closestDi: DI by closestDI(applicationContext)
        extend(closestDi)

        bind<CoroutineScope>(DiTag.TAG_PRESENTER_COROUTINE_SCOPE) with provider {
            lifecycle.coroutineScope
        }

        bind<LifecycleOwner>() with provider {
            this@EpubContentActivity
        }
    }


    private val mScrollDownInterface = ScrollDownJavascriptInterface()

    /** The Page Adapter used to manage swiping between epub pages  */
    private var mContentPagerAdapter: EpubContentPagerAdapter? = null

    private var mPresenter: EpubContentPresenter? = null

    private var mSavedInstanceState: Bundle? = null

    private lateinit var mBinding: ActivityEpubContentBinding

    private lateinit var recyclerViewLinearLayout: LinearLayoutManager

    override var loading: Boolean
        get() = (findViewById<View>(R.id.progressBar)?.visibility == View.VISIBLE)
        set(value) {
            findViewById<View>(R.id.progressBar)?.visibility = if(value) View.VISIBLE else View.GONE
        }

    override var containerTitle: String? = null
        set(value) {
            field = value
            mBinding.containerTitle = value
        }

    override var windowTitle: String? = null
        set(value) {
            field = value
            title = value
        }

    override var spineUrls: List<String>? = null
        set(value) {
            value?.also { mContentPagerAdapter?.submitList(value) }
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
            mBinding.appbar.progressBar.visibility = if(progressVisible) View.VISIBLE else View.GONE
        }

    override var progressValue: Int = 0
        set(value) {
            progressVisible = progressValue != 100
            if (progressValue == -1) {
                mBinding.appbar.progressBar.isIndeterminate = true
            } else {
                mBinding.appbar.progressBar.progress = value
            }
            field = value
        }

    override fun scrollToSpinePosition(spinePosition: Int, hashAnchor: String?) {
        mBinding.epubPageRecyclerView.post {
            recyclerViewLinearLayout.scrollToPositionWithOffset(spinePosition, 0)
            mContentPagerAdapter?.focusChildPosition(spinePosition)
            mPresenter?.handlePageChanged(spinePosition)
        }

        if(!hashAnchor.isNullOrEmpty())
            mContentPagerAdapter?.scrollToAnchor(spinePosition, hashAnchor)

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

    class EpubWebViewClient: WebViewClient() {

        @Volatile
        var targetAnchor: String? = null

        var loaded: Boolean = false

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            loaded = false
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            loaded = true
            view?.adjustHeightToWrapContent()

            val targetAnchorVal = targetAnchor
            if(targetAnchorVal != null) {
                view?.scrollToAnchor(targetAnchorVal)
                targetAnchor = null
            }
        }
    }

    class EpubWebChromeClient(private val viewHolder: EpubContentViewHolder): WebChromeClient() {

        override fun onReceivedTitle(view: WebView?, title: String?) {
            viewHolder.pageTitle = title
        }
    }

    inner class EpubContentViewHolder internal constructor(val mBinding: ItemEpubcontentViewBinding,
                                                     val epubWebViewClient: EpubWebViewClient) :
            RecyclerView.ViewHolder(mBinding.root) {

        var pageIndex: Int = -1

        var pageTitle: String? = null
            set(value) {
                field = value

                //Ignore an autogenerated (url) title
                val pageTitleVal = if(value != null && !value.contains("127.0.0.1")) {
                    value
                }else {
                    null
                }

                mPresenter?.handlePageTitleChanged(pageIndex, pageTitleVal)
            }
    }

    inner class EpubContentPagerAdapter(var scrollDownInterface: ScrollDownJavascriptInterface?) :
            ListAdapter<String, EpubContentViewHolder>(URL_DIFFUTIL) {

        private var webViewTouchHandler: Handler = Handler()

        private var gestureDetector: GestureDetectorCompat? = null

        private val boundHolders = mutableListOf<EpubContentViewHolder>()

        //This is a map of any anchors that should be scrolled to.
        private val anchorsToScrollTo = mutableMapOf<Int, String?>()

        private var nextFocus: Int = -1

        @SuppressLint("SetJavaScriptEnabled", "ObsoleteSdkInt", "ClickableViewAccessibility")
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpubContentViewHolder {
            val mBinding = ItemEpubcontentViewBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false)

            if (Build.VERSION.SDK_INT >= 17) {
                mBinding.epubContentview.settings.mediaPlaybackRequiresUserGesture = false
            }

            mBinding.epubContentview.settings.javaScriptEnabled = true
            mBinding.epubContentview.settings.domStorageEnabled = true
            mBinding.epubContentview.settings.cacheMode = WebSettings.LOAD_DEFAULT
            val epubWebViewClient = EpubWebViewClient().also {
                mBinding.epubContentview.webViewClient = it
            }

            scrollDownInterface?.also {
                mBinding.epubContentview.addJavascriptInterface(it, SCROLL_DOWN_JAVASCRIPT_INTERFACE_NAME)
            }

            mBinding.epubContentview.adjustHeightToDisplayHeight()

            return EpubContentViewHolder(mBinding, epubWebViewClient).also {
                mBinding.epubContentview.webChromeClient = EpubWebChromeClient(it)
            }
        }

        override fun onBindViewHolder(holderContent: EpubContentViewHolder, position: Int) {
            val url =getItem(position)
            holderContent.mBinding.epubContentview.loadUrl(url)
            holderContent.mBinding.epubContentview.tag = url.substringAfterLast("/")
            holderContent.pageIndex = position

            val scrollToAnchor = anchorsToScrollTo[position]
            if(scrollToAnchor != null) {
                //if we need to scroll to an anchor for this page - then set this on the WebViewClient
                // The WebViewClient will then initiate the scroll once it received the
                // onPageFinished callback
                anchorsToScrollTo[position] = null
                holderContent.epubWebViewClient.targetAnchor = scrollToAnchor
            }

            if(nextFocus == position) {
                holderContent.mBinding.root.requestFocus()
                nextFocus = -1
            }

            boundHolders += holderContent
        }

        override fun onViewRecycled(holder: EpubContentViewHolder) {
            super.onViewRecycled(holder)

            holder.mBinding.epubContentview.adjustHeightToDisplayHeight()
            holder.mBinding.epubContentview.loadUrl("about:blank")
            boundHolders -= holder
        }

        /**
         * Scroll to the given anchor on the page at the given page position. If the page is
         * currently bound in the recyclerView, the scroll will happen immediately. If not, the
         * scroll to anchor will be triggered as soon as this page is bound
         *
         * @param position index of the page in the recyclerview that should scroll to an anchor
         * @param anchorName the name of the anchor to scroll to
         */
        fun scrollToAnchor(position: Int, anchorName: String) {
            val boundHolder = boundHolders.filter { it.pageIndex == position }.firstOrNull()
            if(boundHolder != null) {
                boundHolder.mBinding.epubContentview.scrollToAnchor(anchorName)
            }else {
                anchorsToScrollTo[position] = anchorName
            }
        }

        /**
         * Move the focus to the given child. This will ensure that the primary page (e.g. the
         * first visible item that takes up at least half the screen) will stay in place when other
         * views move around as WebViews load and heights are changed.
         *
         * Otherwise the first (even slightly) visible item will be given focus. When scrolling up,
         * the previous page would load, get taller, and then suddenly what the user was reading
         * would no longer be visible.
         */
        fun focusChildPosition(position: Int) {
            val boundHolder = boundHolders.filter { it.pageIndex == position }.firstOrNull()

            if(boundHolder != null) {
                val posOnWindow = IntArray(2).apply {
                    boundHolder.mBinding.root.getLocationInWindow(this)
                }

                if(posOnWindow[1] + boundHolder.mBinding.root.height < (window.decorView.height / 2)) {
                    focusChildPosition(position + 1)
                }else {
                    boundHolder.mBinding.root.requestFocus()
                    nextFocus = -1
                }
            }else {
                nextFocus = position
            }
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            gestureDetector = null
            scrollDownInterface = null
        }

        val HANDLER_CLICK_ON_VIEW = 2
    }

    private val mOnScrollListener = object: RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                val firstVisiblePosition = recyclerViewLinearLayout.findFirstVisibleItemPosition()
                mPresenter?.handlePageChanged(firstVisiblePosition)

                //if the first position bottom is above the middle of the screen, choose the next one
                mContentPagerAdapter?.focusChildPosition(firstVisiblePosition)
            }
        }
    }


    override fun onClick(item: Any?, view: View) {
        val navItem = item as EpubNavItem?
        if(navItem != null)
            mPresenter?.handleClickNavItem(navItem)

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
        mSavedInstanceState = savedInstanceState
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_epub_content)

        setSupportActionBar(mBinding.appbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mContentPagerAdapter = EpubContentPagerAdapter(mScrollDownInterface)
        recyclerViewLinearLayout = NoFocusScrollLinearLayoutManager(this)

        mBinding.epubPageRecyclerView.setItemViewCacheSize(2)
        mBinding.epubPageRecyclerView.layoutManager = recyclerViewLinearLayout
        mBinding.epubPageRecyclerView.addItemDecoration(DividerItemDecoration(this,
            DividerItemDecoration.VERTICAL))
        mBinding.epubPageRecyclerView.adapter = mContentPagerAdapter
        mBinding.epubPageRecyclerView.addOnScrollListener(mOnScrollListener)

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

        const val SCROLL_DOWN_JAVASCRIPT_INTERFACE_NAME = "UstadEpub"

    }

}
