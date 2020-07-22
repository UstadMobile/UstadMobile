package com.ustadmobile.port.android.view

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
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
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ActivityEpubContentBinding
import com.toughra.ustadmobile.databinding.ItemEpubcontentViewBinding
import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem
import com.ustadmobile.core.controller.EpubContentPresenter
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UMAndroidUtil.bundleToMap
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.EpubContentView
import com.ustadmobile.sharedse.network.NetworkManagerBle
import kotlinx.android.synthetic.main.appbar_material_with_progress.view.*
import kotlinx.coroutines.CompletableDeferred

class EpubContentActivity : UstadBaseActivity(),EpubContentView, AdapterView.OnItemClickListener, TocListView.OnItemClickListener {


    /** The Page Adapter used to manage swiping between epub pages  */
    private var mContentPagerAdapter: EpubContentPagerAdapter? = null

    private var mPresenter: EpubContentPresenter? = null

    private var mSavedInstanceState: Bundle? = null

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
        mContentPagerAdapter = urls?.let { EpubContentPagerAdapter(this,it) }
        mBinding.containerEpubrunnerPager.offscreenPageLimit = 1
        mBinding.containerEpubrunnerPager.adapter = mContentPagerAdapter
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


    private class EpubContentPagerAdapter internal constructor(val context: Context,
                                                               private val urls: Array<String>):
            RecyclerView.Adapter<EpubContentPagerAdapter.EpubContentViewHolder>() {

        private var webViewTouchHandler: Handler = Handler()

        private var gestureDetector: GestureDetectorCompat? = null

        inner class EpubContentViewHolder internal constructor(val mBinding: ItemEpubcontentViewBinding) :
                RecyclerView.ViewHolder(mBinding.root) {

            @SuppressLint("SetJavaScriptEnabled", "ObsoleteSdkInt", "ClickableViewAccessibility")
            fun bind(spineUrl: String){
                //Android after Version 17 (4.4) by default requires a gesture before any media playback happens
                if (Build.VERSION.SDK_INT >= 17) {
                    mBinding.epubContentview.settings?.mediaPlaybackRequiresUserGesture = false
                }

                mBinding.epubContentview.settings.javaScriptEnabled = true
                mBinding.epubContentview.settings.domStorageEnabled = true
                mBinding.epubContentview.settings.cacheMode = WebSettings.LOAD_DEFAULT
                mBinding.epubContentview.webViewClient = WebViewClient()
                mBinding.epubContentview.webChromeClient = WebChromeClient()
                mBinding.epubContentview.loadUrl(spineUrl)
                mBinding.epubContentview.setDownloadListener { url, _, _, _, _ ->
                    val request = DownloadManager.Request(Uri.parse(url))
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                            UMFileUtil.getFilename(url))
                    val downloadManager = context.getSystemService(
                            Context.DOWNLOAD_SERVICE) as DownloadManager
                    downloadManager.enqueue(request)
                }
                gestureDetector = GestureDetectorCompat(mBinding.epubContentview.context,
                        object : GestureDetector.SimpleOnGestureListener() {
                            override fun onSingleTapUp(e: MotionEvent): Boolean {
                                webViewTouchHandler.sendEmptyMessageDelayed(HANDLER_CLICK_ON_VIEW, 200)
                                return super.onSingleTapUp(e)
                            } })

                mBinding.epubContentview.setOnTouchListener { _, motionEvent ->
                    gestureDetector?.onTouchEvent(motionEvent)?:false}
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpubContentViewHolder {
            val mBinding = ItemEpubcontentViewBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false)
            return EpubContentViewHolder(mBinding)
        }

        override fun getItemCount(): Int  = urls.size

        override fun onBindViewHolder(holderContent: EpubContentViewHolder, position: Int) {
            holderContent.bind(urls[position])
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

        mPresenter = EpubContentPresenter(this, bundleToMap(intent.extras),
                this, di)
        loading = true
        mPresenter?.onCreate(savedInstanceState.toStringMap())
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
        mBinding.containerEpubrunnerPager.adapter = null
        mSavedInstanceState = null
        mPresenter = null
        mContentPagerAdapter = null
        coverImageUrl = null
        containerTitle = null
        tableOfContents = null
        pageTitle = null
        super.onDestroy()
    }

}
