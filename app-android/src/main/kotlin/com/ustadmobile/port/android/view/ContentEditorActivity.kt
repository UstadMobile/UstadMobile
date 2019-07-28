package com.ustadmobile.port.android.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.view.MenuItem.SHOW_AS_ACTION_ALWAYS
import android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.annotation.Nullable
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ContentEditorPresenter
import com.ustadmobile.core.controller.ContentEditorPresenterCommon.Companion.EDITOR_BASE_DIR_NAME
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMAndroidUtil.bundleToMap
import com.ustadmobile.core.impl.UMAndroidUtil.convertDpToPixel
import com.ustadmobile.core.impl.UMAndroidUtil.getCurrentLocale
import com.ustadmobile.core.impl.UMAndroidUtil.getDirectionality
import com.ustadmobile.core.impl.UMAndroidUtil.getDisplayWidth
import com.ustadmobile.core.impl.UMAndroidUtil.getMimeType
import com.ustadmobile.core.impl.UMAndroidUtil.getSpanCount
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmResultCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil.joinPaths
import com.ustadmobile.core.view.ContentEditorView
import com.ustadmobile.core.view.ContentEditorView.Companion.CONTENT_STORAGE_OPTION
import com.ustadmobile.lib.util.Base64Coder
import com.ustadmobile.port.android.umeditor.*
import com.ustadmobile.port.android.umeditor.UmEditorAnimatedViewSwitcher.Companion.ANIMATED_CONTENT_OPTION_PANEL
import com.ustadmobile.port.android.umeditor.UmEditorAnimatedViewSwitcher.Companion.ANIMATED_SOFT_KEYBOARD_PANEL
import com.ustadmobile.port.android.umeditor.UmEditorAnimatedViewSwitcher.Companion.MAX_SOFT_KEYBOARD_DELAY
import com.ustadmobile.port.android.umeditor.UmWebContentEditorClient.Companion.executeJsFunction
import com.ustadmobile.port.android.view.ContentEditorActivity.UmFormatHelper.Companion.isTobeHighlighted
import com.ustadmobile.sharedse.network.AndroidAssetsHandler
import com.ustadmobile.sharedse.network.NetworkManagerBle
import id.zelory.compressor.Compressor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import java.io.File
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

open class ContentEditorActivity : UstadBaseWithContentOptionsActivity(),
        ContentEditorView, UmWebContentEditorChromeClient.JsLoadingCallback, UmEditorActionView.OnQuickActionMenuItemClicked, UmEditorAnimatedViewSwitcher.OnAnimatedViewsClosedListener {

    private lateinit var presenter: ContentEditorPresenter

    private var viewSwitcher: UmEditorAnimatedViewSwitcher? = null

    private val impl = UstadMobileSystemImpl.instance

    private val assetsDir = String.format("assets-%s",
            SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date()))

    @VisibleForTesting
    var mediaSourceBottomSheetBehavior: BottomSheetBehavior<NestedScrollView>? = null

    @VisibleForTesting
    var contentOptionsBottomSheetBehavior: BottomSheetBehavior<NestedScrollView>? = null

    private var umBottomToolbarHolder: AppBarLayout? = null

    private var mWebView: WebView? = null

    private var toolbar: Toolbar? = null

    private var args: MutableMap<String, String>? = null

    private var mimeType = ""

    private var mFormat: UmFormat? = null

    private var fileFromCamera: File? = null

    private var rootView: View? = null

    private var isDoneEditing = false

    private var progressDialog: ProgressBar? = null

    private var mSavedInstance: Bundle? = null

    private var mViewPager: ViewPager ? = null

    private var mTabLayout: TabLayout ? = null

    @VisibleForTesting
    fun insertTestContent(content: String) {
        presenter.handleEditorActions(ContentEditorView.ACTION_INSERT_CONTENT, content)
    }


    override fun updateDocument(title: String, description: String) {
        presenter.handleUpdateDocumentMetaInfo(title, description)
    }

    @VisibleForTesting
    fun selectAllTestContent() {
        presenter.handleEditorActions(ContentEditorView.ACTION_SELECT_ALL, null)
    }

    @VisibleForTesting
    fun clearAll() {
        presenter.handleEditorActions(ContentEditorView.ACTION_CLEAR_ALL, null)
    }

    @get:VisibleForTesting
    var umFormatHelper: UmFormatHelper? = null
        private set

    val isEditorInitialized: Boolean
        @VisibleForTesting
        get() = ::presenter.isInitialized && presenter.isEditorInitialized


    override fun onBleNetworkServiceBound(networkManagerBle: NetworkManagerBle) {
        super.onBleNetworkServiceBound(networkManagerBle)
        val embeddedHttp =  networkManagerBle.httpd

        embeddedHttp.addRoute("$assetsDir(.)+",AndroidAssetsHandler::class.java, applicationContext)
        presenter = ContentEditorPresenter(this, args!!, this,
                args!![CONTENT_STORAGE_OPTION]) {

            val mountedPath: String = embeddedHttp.mountContainer(it, null)!!
            val counterMountedUrl: String = joinPaths(embeddedHttp.localHttpUrl,
                    mountedPath)
            counterMountedUrl
        }
        presenter.onCreate(bundleToMap(mSavedInstance))

        val adapter = ContentFormattingPagerAdapter(supportFragmentManager)
        mViewPager!!.adapter = adapter
        mTabLayout!!.setupWithViewPager(mViewPager!!)
    }


    /**
     * Class which represent a link inside the editor.
     */
    inner class UmLink {

        @SerializedName("linkText")
        internal val linkText: String? = null

        @SerializedName("linkUrl")
        internal val linkUrl: String? = null
    }


    /**
     * Class which represent a format control state
     */

    @Serializable
    data class UmFormatState(val command: String?, val status: Boolean = false)


    /**
     * Class which handles all formatting from preparing and updating them when necessary.
     */
    class UmFormatHelper internal constructor() {

        private val formatList: MutableList<UmFormat>

        /**
         * Construct quick action menu items
         * @return list of all quick action menus
         */
        val quickActions: List<UmFormat>

        private val dispatcherList = CopyOnWriteArrayList<UmFormatStateChangeListener>()

        init {
            formatList = prepareFormattingList()
            quickActions = prepareQuickActionFormats()
        }

        /**
         * Prepare all UmEditor format
         * @return list of all formats.
         */
        private fun prepareFormattingList(): MutableList<UmFormat> {
            val mText = ArrayList<UmFormat>()
            val mDirection = ArrayList<UmFormat>()
            val mParagraph = ArrayList<UmFormat>()

            val mFont = ArrayList<UmFormat>()

            mText.add(UmFormat(R.drawable.ic_format_bold_black_24dp, ContentEditorView.TEXT_FORMAT_TYPE_BOLD,
                    false, FORMATTING_TEXT_INDEX, R.id.content_action_bold))
            mText.add(UmFormat(R.drawable.ic_format_italic_black_24dp, ContentEditorView.TEXT_FORMAT_TYPE_ITALIC,
                    false, FORMATTING_TEXT_INDEX, R.id.content_action_italic))
            mText.add(UmFormat(R.drawable.ic_format_underlined_black_24dp,
                    ContentEditorView.TEXT_FORMAT_TYPE_UNDERLINE, false, FORMATTING_TEXT_INDEX,
                    R.id.content_action_underline))
            mText.add(UmFormat(R.drawable.ic_format_strikethrough_black_24dp,
                    ContentEditorView.TEXT_FORMAT_TYPE_STRIKE, false, FORMATTING_TEXT_INDEX,
                    R.id.content_action_strike_through))
            mText.add(UmFormat(R.drawable.ic_number_superscript,
                    ContentEditorView.TEXT_FORMAT_TYPE_SUP, false, FORMATTING_TEXT_INDEX))
            mText.add(UmFormat(R.drawable.ic_number_subscript,
                    ContentEditorView.TEXT_FORMAT_TYPE_SUB, false, FORMATTING_TEXT_INDEX))
            mText.add(UmFormat(R.drawable.ic_format_size_black_24dp,
                    ContentEditorView.TEXT_FORMAT_TYPE_FONT, false, FORMATTING_TEXT_INDEX))

            mParagraph.add(UmFormat(R.drawable.ic_format_align_justify_black_24dp,
                    ContentEditorView.PARAGRAPH_FORMAT_ALIGN_JUSTIFY, false, FORMATTING_PARAGRAPH_INDEX))
            mParagraph.add(UmFormat(R.drawable.ic_format_align_right_black_24dp,
                    ContentEditorView.PARAGRAPH_FORMAT_ALIGN_RIGHT, false, FORMATTING_PARAGRAPH_INDEX))
            mParagraph.add(UmFormat(R.drawable.ic_format_align_center_black_24dp,
                    ContentEditorView.PARAGRAPH_FORMAT_ALIGN_CENTER, false, FORMATTING_PARAGRAPH_INDEX))
            mParagraph.add(UmFormat(R.drawable.ic_format_align_left_black_24dp,
                    ContentEditorView.PARAGRAPH_FORMAT_ALIGN_LEFT, false, FORMATTING_PARAGRAPH_INDEX))
            mParagraph.add(UmFormat(R.drawable.ic_format_list_numbered_black_24dp,
                    ContentEditorView.PARAGRAPH_FORMAT_LIST_ORDERED, false, FORMATTING_PARAGRAPH_INDEX,
                    R.id.content_action_ordered_list))
            mParagraph.add(UmFormat(R.drawable.ic_format_list_bulleted_black_24dp,
                    ContentEditorView.PARAGRAPH_FORMAT_LIST_UNORDERED, false, FORMATTING_PARAGRAPH_INDEX,
                    R.id.content_action_uordered_list))
            mParagraph.add(UmFormat(R.drawable.ic_format_indent_increase_black_24dp,
                    ContentEditorView.PARAGRAPH_FORMAT_INDENT_INCREASE, false, FORMATTING_PARAGRAPH_INDEX,
                    R.id.content_action_indent))
            mParagraph.add(UmFormat(R.drawable.ic_format_indent_decrease_black_24dp,
                    ContentEditorView.PARAGRAPH_FORMAT_INDENT_DECREASE, false, FORMATTING_PARAGRAPH_INDEX,
                    R.id.content_action_outdent))

            mDirection.add(UmFormat(R.drawable.ic_format_textdirection_l_to_r_white_24dp,
                    ContentEditorView.ACTION_TEXT_DIRECTION_LTR, true, LANGUAGE_DIRECTIONALITY,
                    R.id.direction_leftToRight, R.string.content_direction_ltr))
            mDirection.add(UmFormat(R.drawable.ic_format_textdirection_r_to_l_white_24dp,
                    ContentEditorView.ACTION_TEXT_DIRECTION_RTL, false, LANGUAGE_DIRECTIONALITY,
                    R.id.direction_rightToLeft, R.string.content_direction_rtl))

            mFont.add(UmFormat(0, ContentEditorView.TEXT_FORMAT_TYPE_FONT, false,
                    FORMATTING_FONT_INDEX, 8, R.string.content_font_8))
            mFont.add(UmFormat(0, ContentEditorView.TEXT_FORMAT_TYPE_FONT, false,
                    FORMATTING_FONT_INDEX, 10, R.string.content_font_10))
            mFont.add(UmFormat(0, ContentEditorView.TEXT_FORMAT_TYPE_FONT, false,
                    FORMATTING_FONT_INDEX, 12, R.string.content_font_12))
            mFont.add(UmFormat(0, ContentEditorView.TEXT_FORMAT_TYPE_FONT, false,
                    FORMATTING_FONT_INDEX, 14, R.string.content_font_14))
            mFont.add(UmFormat(0, ContentEditorView.TEXT_FORMAT_TYPE_FONT, false,
                    FORMATTING_FONT_INDEX, 18, R.string.content_font_18))
            mFont.add(UmFormat(0, ContentEditorView.TEXT_FORMAT_TYPE_FONT, false,
                    FORMATTING_FONT_INDEX, 24, R.string.content_font_24))
            mFont.add(UmFormat(0, ContentEditorView.TEXT_FORMAT_TYPE_FONT, false,
                    FORMATTING_FONT_INDEX, 36, R.string.content_font_36))

            val allFormats = ArrayList<UmFormat>()
            allFormats.addAll(mText)
            allFormats.addAll(mParagraph)
            allFormats.addAll(mDirection)
            allFormats.addAll(mFont)
            return allFormats
        }

        private fun prepareQuickActionFormats(): List<UmFormat> {
            val quickActions = ArrayList<UmFormat>()
            try {
                quickActions.add(getFormatByCommand(ContentEditorView.TEXT_FORMAT_TYPE_BOLD)!!)
                quickActions.add(getFormatByCommand(ContentEditorView.TEXT_FORMAT_TYPE_ITALIC)!!)
                quickActions.add(getFormatByCommand(ContentEditorView.TEXT_FORMAT_TYPE_UNDERLINE)!!)
                quickActions.add(getFormatByCommand(ContentEditorView.TEXT_FORMAT_TYPE_STRIKE)!!)
                quickActions.add(getFormatByCommand(ContentEditorView.PARAGRAPH_FORMAT_LIST_ORDERED)!!)
                quickActions.add(getFormatByCommand(ContentEditorView.PARAGRAPH_FORMAT_LIST_UNORDERED)!!)
                quickActions.add(getFormatByCommand(ContentEditorView.PARAGRAPH_FORMAT_INDENT_INCREASE)!!)
                quickActions.add(getFormatByCommand(ContentEditorView.PARAGRAPH_FORMAT_INDENT_DECREASE)!!)
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }

            return quickActions
        }


        /**
         * Get list of all formats by type
         * @param formatType type to be found
         * @return found list of all formats of
         */
        fun getFormatListByType(formatType: Int): List<UmFormat> {
            val formats = ArrayList<UmFormat>()
            for (format in formatList) {
                if (format.formatType == formatType) {
                    formats.add(format)
                }
            }
            return formats
        }

        /**
         * Get UmFormat by its command
         * @param command formatting command to be found
         * @return found content format
         */
        internal fun getFormatByCommand(command: String?): UmFormat? {
            var umFormat: UmFormat? = null
            for (format in formatList) {
                if (format.formatCommand == command) {
                    umFormat = format
                    break
                }
            }
            return umFormat
        }

        private fun getFormattingIndex(formatCommand: String?): Int {
            for (format in formatList) {
                if (format.formatCommand == formatCommand) {
                    return formatList.indexOf(format)
                }
            }

            return -1
        }


        fun updateFormat(formatStates: Array<UmFormatState>) {

            for (state in formatStates) {
                val index = getFormattingIndex(state.command)
                if(index != -1){
                    formatList[index].active = state.status
                }
            }

            for (dispatcher in dispatcherList) {
                dispatcher.onStateChanged(formatList)
            }
        }


        /**
         * Get list of all directionality formatting
         * @param activeFormat Directionality format to be activated
         * @return List of all formats
         */
        internal fun getLanguageDirectionalityList(activeFormat: UmFormat?): List<UmFormat> {
            val directionality = getFormatListByType(LANGUAGE_DIRECTIONALITY)
            for (format in directionality) {
                if (activeFormat != null) {
                    directionality[directionality.indexOf(format)]
                            .active = format.formatId == activeFormat.formatId

                }
            }
            return directionality
        }

        /**
         * Get all font format list
         * @param activeFormat Font format to be activated
         * @return List of all fonts
         */
        internal fun getFontList(activeFormat: UmFormat?): List<UmFormat> {
            val fontList = getFormatListByType(FORMATTING_FONT_INDEX)
            for (format in fontList) {
                if (activeFormat != null) {
                    fontList[fontList.indexOf(format)]
                            .active = format.formatId == activeFormat.formatId
                }
            }
            return fontList
        }

        /**
         * Prevent all justification to be active at the same time, only one type at a time.
         * @param command current active justification command.
         */
        internal fun updateOtherJustificationFormatState(command: String?) {
            val mTag = "Justify"
            val paragraphFormatList = getFormatListByType(FORMATTING_PARAGRAPH_INDEX)
            for (format in paragraphFormatList) {
                if (format.formatCommand!!.contains(mTag) && command!!.contains(mTag)
                        && format.formatCommand != command) {
                    val index = paragraphFormatList.indexOf(format)
                    format.active = false
                    formatList[index] = format
                }
            }
        }

        /**
         * Prevent all list types to active at the same time, only one at a time.
         * @param command current active list type command.
         */
        internal fun updateOtherListFormatState(command: String?) {
            val mTag = "List"
            val listOrdersTypes = listOrderFormats()
            for (format in listOrdersTypes) {
                if (format.formatCommand!!.contains(mTag) && command!!.contains(mTag)
                        && format.formatCommand != command) {
                    val index = listOrdersTypes.indexOf(format)
                    format.active = false
                    formatList[index] = format
                }
            }
        }

        private fun listOrderFormats(): List<UmFormat> {
            val listOrders = ArrayList<UmFormat>()
            for (format in formatList) {
                if (format.formatCommand!!.contains("List")) {
                    listOrders.add(format)
                }
            }
            return listOrders
        }

        /**
         * Set format view state change listener
         * @param listener listener object
         */
        fun setStateChangeListener(listener: UmFormatStateChangeListener) {
            dispatcherList.add(listener)
        }


        /**
         * Delete all listeners from the list on activity destroy
         */
        fun destroy() {
            if (dispatcherList.size > 0) {
                dispatcherList.clear()
            }
        }

        companion object {

            /**
             * Flag to indicate all text formats
             */
            private const val FORMATTING_TEXT_INDEX = 0

            /**
             * Flag to indicate all paragraph formats
             */
            private const val FORMATTING_PARAGRAPH_INDEX = 1

            /**
             * Flag to indicate all font formats
             */
            private const val FORMATTING_FONT_INDEX = 2

            /**
             * Flag to indicate all toolbar action formats
             */
            const val ACTIONS_TOOLBAR_INDEX = 3

            /**
             * Flag to indicate all language directionality formats
             */
            private const val LANGUAGE_DIRECTIONALITY = 4


            /**
             * Check if the format is valid for highlight or not, for those format which
             * deals with increment like indentation they are not fit for active state
             * @param command format command to be checked for validity
             * @return True if valid otherwise False.
             */
            fun isTobeHighlighted(command: String): Boolean {
                return (command != ContentEditorView.TEXT_FORMAT_TYPE_FONT
                        && command != ContentEditorView.PARAGRAPH_FORMAT_INDENT_DECREASE
                        && command != ContentEditorView.PARAGRAPH_FORMAT_INDENT_INCREASE)
            }
        }
    }


    /**
     * Content formatting adapter which handles formatting sections
     */
    private inner class ContentFormattingPagerAdapter internal constructor(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getCount(): Int {
            return contentFormattingTypeLabel.size
        }

        internal var contentFormattingTypeLabel = arrayOf<String>(resources.getString(R.string.content_format_text),
                resources.getString(R.string.content_format_paragraph))


        override fun getItem(position: Int): Fragment {
            return FormattingFragment.newInstance(position,
                    umFormatHelper!!, presenter)
        }

        @Nullable
        override fun getPageTitle(position: Int): CharSequence {
            return contentFormattingTypeLabel[position]
        }
    }


    /**
     * Fragment to handle bottom formatting type  UI (Text formatting & Paragraph formatting)
     */
    class FormattingFragment : Fragment(), UmFormatStateChangeListener {

        private var adapter: FormatsAdapter? = null

        private var umFormats: List<UmFormat>? = null


        private inner class FormatsAdapter : RecyclerView.Adapter<FormatsAdapter.FormatViewHolder>() {

            private var umFormats: List<UmFormat>? = ArrayList()

            override fun getItemCount(): Int {
               return if (umFormats != null) umFormats!!.size else 0
            }

            internal fun setUmFormats(umFormats: List<UmFormat>?) {
                this.umFormats = umFormats
                notifyDataSetChanged()
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FormatViewHolder {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_content_formatting_type, parent, false)
                return FormatViewHolder(view)
            }

            override fun onBindViewHolder(holder: FormatViewHolder, position: Int) {
                val format = umFormats!![position]
                val mIcon :ImageView = holder.itemView.findViewById(R.id.format_icon)
                val mLayout:RelativeLayout = holder.itemView.findViewById(R.id.format_holder)
                mIcon.setImageResource(format.formatIcon)
                changeState(mIcon, mLayout, format.active)
                if (!isTobeHighlighted(format.formatCommand!!)) {
                    changeState(mIcon, mLayout, false)
                }
                mLayout.setOnClickListener {
                    if (format.formatCommand != ContentEditorView.TEXT_FORMAT_TYPE_FONT) {
                        changeState(mIcon, mLayout, true)
                        umFormatHelper!!.updateOtherJustificationFormatState(format.formatCommand)
                        umFormatHelper!!.updateOtherListFormatState(format.formatCommand)
                        mPresenter!!.handleEditorActions(format.formatCommand!!, null)
                        notifyDataSetChanged()
                    } else {

                        val popUpView = UmEditorPopUpView(activity!!, holder.itemView)
                                .setMenuList(umFormatHelper!!.getFontList(null))
                                .showIcons(false).setWidthDimen(getDisplayWidth(activity!!),
                                        false)
                        popUpView.showWithListener(object : UmEditorPopUpView.OnPopUpMenuClickListener{
                            override fun onMenuClicked(format: UmFormat) {
                                mPresenter!!.handleEditorActions(format.formatCommand!!,
                                        format.formatId.toString())
                                popUpView.setMenuList(umFormatHelper!!.getFontList(format))
                            }

                        })
                    }
                }
            }

            /**
             * Change state of the view based status.
             */
            private fun changeState(imageIcon: ImageView, iconHolder: RelativeLayout,
                                    isActivated: Boolean) {
                imageIcon.setColorFilter(ContextCompat.getColor(context!!,
                        if (isActivated) R.color.icons else R.color.text_secondary))
                iconHolder.setBackgroundColor(ContextCompat.getColor(context!!,
                        if (isActivated) R.color.content_icon_active else R.color.icons))
            }

            internal inner class FormatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_content_formatting,
                    container, false)
            val mRecyclerView:RecyclerView = rootView.findViewById(R.id.formats_list)
            umFormats = umFormatHelper!!.getFormatListByType(arguments!!.getInt(FORMAT_TYPE, 0))
            umFormatHelper!!.setStateChangeListener(this)

            adapter = FormatsAdapter()
            adapter!!.setUmFormats(umFormats)

            val spanCount = getSpanCount(activity!!,
                    resources.getInteger(R.integer.format_item_width))
            val spacing = convertDpToPixel(resources
                    .getInteger(R.integer.format_item_spacing))

            val mLayoutManager = GridLayoutManager(context, spanCount)
            mRecyclerView.addItemDecoration(UmGridSpacingItemDecoration(spanCount, spacing,
                    true))
            mRecyclerView.layoutManager = mLayoutManager
            mRecyclerView.adapter = adapter

            return rootView
        }


        override fun onStateChanged(formatList: List<UmFormat>) {
            if (adapter != null) {
                adapter!!.notifyDataSetChanged()
            }
        }

        companion object {

            private const val FORMAT_TYPE = "format_type"

            private  var mPresenter: ContentEditorPresenter? = null

            private  var umFormatHelper: UmFormatHelper? = null


            /**
             * Create new instance of a content formatting fragment
             */
            fun newInstance(formatType: Int, formatHelper: UmFormatHelper,
                            presenter: ContentEditorPresenter): FormattingFragment {
                val fragment = FormattingFragment()
                mPresenter = presenter
                umFormatHelper = formatHelper
                val bundle = Bundle()
                bundle.putInt(FORMAT_TYPE, formatType)
                fragment.arguments = bundle
                return fragment
            }
        }


    }


    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isPortrait = resources.getBoolean(R.bool.orientation_portrait)
        requestedOrientation = if (isPortrait)
            SCREEN_ORIENTATION_PORTRAIT
        else {
            SCREEN_ORIENTATION_UNSPECIFIED
        }

        umDb = UmAppDatabase.getInstance(applicationContext)

        umRepo = UmAccountManager.getRepositoryForActiveAccount(applicationContext)

        setContentView(R.layout.activity_content_editor)

        this.mSavedInstance = savedInstanceState

        this.coordinatorLayout = findViewById(R.id.coordinationLayout)

        val formattingBottomSheetBehavior = BottomSheetBehavior
                .from(findViewById<NestedScrollView>(R.id.bottom_sheet_container))
        mediaSourceBottomSheetBehavior = BottomSheetBehavior
                .from(findViewById(R.id.bottom_multimedia_source_sheet_container))

        contentOptionsBottomSheetBehavior = BottomSheetBehavior
                .from(findViewById(R.id.bottom_content_option_sheet_container))

        toolbar = findViewById(R.id.um_toolbar)
        val mInsertMultimedia = findViewById<RelativeLayout>(R.id.content_option_multimedia)
        val mInsertMultipleChoice = findViewById<RelativeLayout>(R.id.content_option_multiplechoice)
        val mInsertFillBlanks = findViewById<RelativeLayout>(R.id.content_option_filltheblanks)
        val mInsertLink = findViewById<RelativeLayout>(R.id.content_option_link)
        mWebView = findViewById(R.id.editor_content)
        progressDialog = findViewById(R.id.progressBar)
        val mFromCamera = findViewById<RelativeLayout>(R.id.multimedia_from_camera)
        val mFromDevice = findViewById<RelativeLayout>(R.id.multimedia_from_device)
        rootView = findViewById(R.id.coordinationLayout)
        umBottomToolbarHolder = findViewById(R.id.um_appbar_bottom)
        val umEditorActionView = findViewById<UmEditorActionView>(R.id.um_toolbar_bottom)

        mWebView!!.setBackgroundColor(Color.TRANSPARENT)


        viewSwitcher = UmEditorAnimatedViewSwitcher.instance
                .with(this, this)
                .setViews(rootView!!, mWebView!!, contentOptionsBottomSheetBehavior!!,
                        formattingBottomSheetBehavior, mediaSourceBottomSheetBehavior!!)
        viewSwitcher!!.closeActivity(false)

        mViewPager = findViewById<ViewPager>(R.id.content_types_viewpager)
        mTabLayout = findViewById<TabLayout>(R.id.content_types_tabs)


        umFormatHelper = UmFormatHelper()
        args = bundleToMap(intent.extras) as HashMap<String, String>

        if (toolbar != null) {
            toolbar!!.title = ""
            setSupportActionBar(toolbar)
            umEditorActionView.setUmFormatHelper(umFormatHelper!!)
            umEditorActionView.inflateMenu(R.menu.menu_content_editor_quick_actions, true)
            umEditorActionView.setQuickActionMenuItemClickListener(this)
        }



        handleClipBoardContentChanges()

        progressDialog!!.max = 100
        progressDialog!!.progress = 0

        findViewById<ImageView>(R.id.action_close_tab_formats).setOnClickListener {viewSwitcher!!.closeAnimatedView(UmEditorAnimatedViewSwitcher.ANIMATED_FORMATTING_PANEL) }
        findViewById<ImageView>(R.id.action_close_tab_multimedia_options).setOnClickListener { viewSwitcher!!.closeAnimatedView(UmEditorAnimatedViewSwitcher.ANIMATED_MEDIA_TYPE_PANEL) }
        findViewById<ImageView>(R.id.action_close_tab_content_options).setOnClickListener {viewSwitcher!!.closeAnimatedView(ANIMATED_CONTENT_OPTION_PANEL) }

        mFromDevice.setOnClickListener {
            isOpeningFilePickerOrCamera = true
            viewSwitcher!!.closeAnimatedView(UmEditorAnimatedViewSwitcher.ANIMATED_MEDIA_TYPE_PANEL)
            browseFiles(object : UmResultCallback<String>{
                override fun onDone(result: String?) {
                    try {
                        mimeType = getMimeType(applicationContext, selectedFileUri!!).toString()
                        insertMedia(File(result))
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }, "image/*", "video/*", "audio/*")
        }

        mFromCamera.setOnClickListener {
            isOpeningFilePickerOrCamera = true
            viewSwitcher!!.closeAnimatedView(UmEditorAnimatedViewSwitcher.ANIMATED_MEDIA_TYPE_PANEL)
            runAfterGrantingPermission(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    Runnable { this.showMediaTypeDialog() }, getString(R.string.permission_camera_title),
                    getString(R.string.permission_camera_message))
        }


        mInsertMultimedia.setOnClickListener { viewSwitcher!!.animateView(UmEditorAnimatedViewSwitcher.ANIMATED_MEDIA_TYPE_PANEL) }

        mInsertMultipleChoice.setOnClickListener {
            presenter.handleEditorActions(ContentEditorView.CONTENT_INSERT_MULTIPLE_CHOICE_QN, null)
            viewSwitcher!!.closeAnimatedView(ANIMATED_CONTENT_OPTION_PANEL)
        }

        mInsertFillBlanks.setOnClickListener {
            presenter.handleEditorActions(ContentEditorView.CONTENT_INSERT_FILL_THE_BLANKS_QN, null)
            viewSwitcher!!.closeAnimatedView(ANIMATED_CONTENT_OPTION_PANEL)
        }

        mInsertLink.setOnClickListener {
            viewSwitcher!!.closeAnimatedView(ANIMATED_CONTENT_OPTION_PANEL)
            executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + "getLinkProperties",
                    this@ContentEditorActivity)
        }


        val webSettings = mWebView!!.settings
        webSettings.javaScriptEnabled = true
        webSettings.allowFileAccess = true
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        mWebView!!.webChromeClient = UmWebContentEditorChromeClient(this)
        mWebView!!.addJavascriptInterface(
                UmWebContentEditorInterface(this, this), "UmEditor")
        mWebView!!.setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }


    override fun onAttachFragment(fragment: Fragment?) {
        if (fragment is ContentEditorPageListFragment) {
            fragment.setUmFileHelper(presenter)
            fragment.setCurrentPage(presenter.currentPage)
        } else if (fragment is ContentEntryEditFragment) {
            fragment.setActionListener(this)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_content_editor_top_actions, menu)

        val showPreviewIcon = resources.getBoolean(R.bool.menu_preview_visible)
        val showDirectionalityIcon = resources.getBoolean(
                R.bool.menu_directionality_visible)
        val showPageIcon = resources.getBoolean(R.bool.menu_pages_visible)

        menu.findItem(R.id.content_action_preview).setShowAsAction(if (showPreviewIcon)
            SHOW_AS_ACTION_ALWAYS
        else
            SHOW_AS_ACTION_IF_ROOM)
        menu.findItem(R.id.content_action_direction).setShowAsAction(if (showDirectionalityIcon)
            SHOW_AS_ACTION_ALWAYS
        else
            SHOW_AS_ACTION_IF_ROOM)

        menu.findItem(R.id.content_action_pages).setShowAsAction(if (showPageIcon)
            SHOW_AS_ACTION_ALWAYS
        else
            SHOW_AS_ACTION_IF_ROOM)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.content_action_pages -> {
                presenter.isPageManagerOpen = true
                saveContent()
                viewSwitcher!!.closeActivity(false)

            }
            R.id.content_action_format -> viewSwitcher!!.animateView(UmEditorAnimatedViewSwitcher.ANIMATED_FORMATTING_PANEL)
            R.id.content_action_preview -> {
                presenter.isOpenPreviewRequest = true
                presenter.isPageManagerOpen = false
                requestContentAutoSave(false)

            }
            R.id.content_action_insert -> viewSwitcher!!.animateView(ANIMATED_CONTENT_OPTION_PANEL)
            R.id.content_action_undo -> presenter.handleEditorActions(ContentEditorView.ACTION_UNDO, null)
            R.id.content_action_redo -> presenter.handleEditorActions(ContentEditorView.ACTION_REDO, null)
            R.id.content_action_direction -> {
                val popUpView = UmEditorPopUpView(this, toolbar!!)
                        .setMenuList(umFormatHelper!!.getLanguageDirectionalityList(null))
                        .setWidthDimen(getDisplayWidth(this), true)
                popUpView.showWithListener(object : UmEditorPopUpView.OnPopUpMenuClickListener{
                    override fun onMenuClicked(format: UmFormat) {
                        presenter.handleEditorActions(format.formatCommand!!, null)
                        popUpView.setMenuList(umFormatHelper!!.getLanguageDirectionalityList(format))
                        mFormat = format
                        invalidateOptionsMenu()
                    }

                })

            }
            R.id.content_action_done -> requestContentAutoSave(true)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (mFormat != null) {
            menu.findItem(R.id.content_action_direction).setIcon(mFormat!!.formatIcon)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(::presenter.isInitialized){
            outState.putString(PAGE_NAME_TAG, presenter.currentPage)
            outState.putSerializable(DIRECTION_TAG, mFormat)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
       if(::presenter.isInitialized){
           presenter.handleSelectedPage(savedInstanceState.getString(PAGE_NAME_TAG)!!)
           mFormat = savedInstanceState.getSerializable(DIRECTION_TAG) as UmFormat
       }
    }


    override fun showErrorMessage(message: String) {
        showBaseMessage(message)
    }


    override fun onCallbackReceived(value: String) {
        if (value.contains("action")) {
            val callback = Gson().fromJson(value, UmWebJsResponse::class.java)
            processJsCallLogValues(callback)
        } else {
            handleFinishActivity()
        }
    }

    private fun handleFinishActivity() {
        if (presenter.isEditorInitialized && isDoneEditing) {
            presenter.isEditorInitialized = false
            presenter.isInEditorPreview = false
            isDoneEditing = false
            viewSwitcher!!.closeAnimatedView(ANIMATED_SOFT_KEYBOARD_PANEL)
            finish()
        }
    }


    override fun onAllAnimatedViewsClosed(finish: Boolean) {
        if(::presenter.isInitialized){
            presenter.handlePageManager(finish)
        }
    }

    override fun onFocusRequested() {

    }

    override fun onProgressChanged(newProgress: Int) {
        progressDialog!!.progress = newProgress
    }


    override fun onPageFinishedLoading() {
        progressDialog!!.visibility = View.GONE
        if (presenter.isInEditorPreview) {
            presenter.isInEditorPreview = false
        }
    }

    /**
     * Process values returned from JS calls
     * @param callback object returned
     */
    @UseExperimental(ImplicitReflectionSerializer::class)
    private fun processJsCallLogValues(callback: UmWebJsResponse) {

        val content = Base64Coder.decodeBase64(callback.content!!)

        when (callback.action) {

            //Callback received to notify that the page is loaded successfully
            ContentEditorView.ACTION_PAGE_LOADED -> executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + "onCreate",
                    this@ContentEditorActivity, getCurrentLocale(this),
                    getDirectionality(this))

            //Callback received to notify that the editor is ready for editing
            ContentEditorView.ACTION_EDITOR_INITIALIZED -> executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + "enableEditingMode", this)

            //callback received when editor is switched on
            ContentEditorView.ACTION_ENABLE_EDITING -> {
                presenter.isEditorInitialized = content.toBoolean()
                if (presenter.isEditorInitialized) {
                    handleWebViewMargin()
                    mWebView!!.postDelayed({
                        if (!presenter.isPageManagerOpen) {
                            viewSwitcher!!.animateView(ANIMATED_SOFT_KEYBOARD_PANEL)
                        }
                    },
                            MAX_SOFT_KEYBOARD_DELAY)
                }
                progressDialog!!.visibility = View.GONE
                viewSwitcher!!.setEditorActivated(presenter.isEditorInitialized)
                handleQuickActions()
            }

            //callback received to trigger save event
            ContentEditorView.ACTION_SAVE_CONTENT -> {
                presenter.handleSaveContent(content)
                viewSwitcher!!.closeActivity(true)
            }

            //Callback received upon completion of all controls status check
            ContentEditorView.ACTION_CONTROLS_ACTIVATED -> {
                val gson = Gson()
                umFormatHelper!!.updateFormat(gson.fromJson(content, Array<UmFormatState>::class.java))
            }

            //Callback received when there is a content cut event
            ContentEditorView.ACTION_CONTENT_CUT -> try {
                val utf8Content = URLDecoder.decode(content, "UTF-8")
                val clipboard = getSystemService(
                        Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(EDITOR_BASE_DIR_NAME, utf8Content)
                clipboard.primaryClip = clip
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }

            //Callback received when there is a link check event on selected text
            ContentEditorView.ACTION_LINK_CHECK -> {
                val umLink = Gson().fromJson(content, UmLink::class.java)
                val builder = AlertDialog.Builder(this)

                val view = layoutInflater.inflate(R.layout.content_link_config_view, null, false)
                val linkText = view.findViewById<TextInputEditText>(R.id.linkText)
                val linkUrl = view.findViewById<TextInputEditText>(R.id.linkUrl)
                linkText.setText(umLink.linkText)
                linkUrl.setText(umLink.linkUrl)
                builder.setView(view)
                builder.setNegativeButton(R.string.cancel
                ) { dialog, _ -> dialog.dismiss() }
                if (umLink.linkUrl?.isNotEmpty()!!) {
                    builder.setNeutralButton(R.string.content_editor_link_remove) { _, _ ->
                        executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + "removeLink",
                                this@ContentEditorActivity)
                    }
                }
                builder.setPositiveButton(R.string.content_editor_link_insert) { _, _ ->
                    val text = Objects.requireNonNull(linkText.text).toString()
                    val url = Objects.requireNonNull(linkUrl.text).toString()
                    if (text.isNotEmpty() && url.isNotEmpty()) {
                        executeJsFunction(mWebView!!,
                                EDITOR_METHOD_PREFIX + "insertLink", this,
                                url, text, (content.isNotEmpty()).toString())
                    } else {
                        Snackbar.make(rootView!!, R.string.content_editor_link_error,
                                Snackbar.LENGTH_LONG).show()
                    }
                }
                builder.show()
            }
        }
    }

    override fun onStop() {

        if(::presenter.isInitialized && !presenter.isOpenPreviewRequest){
            presenter.handlePreviewAndFilePicker(false, isOpeningFilePickerOrCamera)
        }

        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        if(::presenter.isInitialized){
            presenter.isEditorInitialized = true
            presenter.isOpenPreviewRequest = false
        }
    }

    override fun onPause() {
        super.onPause()
        if (viewSwitcher != null) {
            viewSwitcher!!.closeAnimatedView(ANIMATED_SOFT_KEYBOARD_PANEL)
        }
    }


    override fun onDestroy() {
        if (umFormatHelper != null) {
            umFormatHelper!!.destroy()
        }
        super.onDestroy()
    }

    private fun requestContentAutoSave(doneEditing: Boolean) {
        isDoneEditing = doneEditing
        presenter.handleEditorActions(ContentEditorView.ACTION_SAVE_CONTENT, null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_IMAGE_CAPTURE_REQUEST) {
                try {
                    insertMedia(fileFromCamera)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }

    /**
     * Clean unused files
     */
    override fun cleanUnUsedResources() {
        GlobalScope.launch {
            val result =  presenter.handleRemoveUnUsedResources()
            if(result){
                runOnUiThread {
                    if (presenter.isOpenPreviewRequest) {
                        presenter.handlePreviewAndFilePicker(true, isOpeningFilePickerOrCamera)
                    } else {
                        handleFinishActivity()
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        viewSwitcher!!.closeActivity(true)
    }

    override fun onQuickMenuItemClicked(command: String?) {
        val format = umFormatHelper!!.getFormatByCommand(command)
        if (format != null) {
            presenter.handleEditorActions(format.formatCommand!!, null)
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onQuickMenuViewClicked(itemId: Int) {

    }

    /**
     * Show /Hide quick action menus on top of the keyboard
     */
    private fun handleQuickActions() {
        umBottomToolbarHolder!!.visibility = if (presenter.isEditorInitialized) View.VISIBLE else View.GONE
    }

    /**
     * Open camera ans start acquire media content
     * @param isImage True if media is of image type otherwise video.
     */
    private fun startCameraIntent(isImage: Boolean) {
        val imageId = System.currentTimeMillis().toString()
        val cameraIntent = Intent(if (isImage)
            MediaStore.ACTION_IMAGE_CAPTURE
        else
            MediaStore.ACTION_VIDEO_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        fileFromCamera = File(dir, imageId + if (isImage) "_image.png" else "_video.mp4")
        val sourceUri = FileProvider.getUriForFile(this,
                "$packageName.provider", fileFromCamera!!)
        mimeType = getMimeType(this, sourceUri).toString()
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, sourceUri)
        startActivityForResult(cameraIntent, CAMERA_IMAGE_CAPTURE_REQUEST)

    }

    /**
     * Handle choice between video and image from the camera.
     */
    private fun showMediaTypeDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(impl.getString(MessageID.content_media_title, this))
        builder.setMessage(impl.getString(MessageID.content_media_message, this))
        builder.setPositiveButton(impl.getString(MessageID.content_media_photo, this)
        ) { _, _ -> startCameraIntent(true) }
        builder.setNegativeButton(impl.getString(MessageID.content_media_video, this)
        ) { _, _ -> startCameraIntent(false) }
        builder.setCancelable(false)
        builder.show()
    }


    /**
     * Handle clipboard action completion
     */
    private fun handleClipBoardContentChanges() {
        val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.addPrimaryClipChangedListener { viewSwitcher!!.closeAnimatedView(UmEditorAnimatedViewSwitcher.ANIMATED_FORMATTING_PANEL) }
    }


    /**
     * Set bottom margin dynamically to the WebView to make sure it starts on top of the quick
     * action menus when editing mode is ON
     */
    private fun handleWebViewMargin() {
        val attrs = theme.obtainStyledAttributes(
                intArrayOf(android.R.attr.actionBarSize))
        val actionBarSize = attrs.getDimension(0, 0f).toInt()
        attrs.recycle()
        val marginBottomValue = (if (presenter.isEditorInitialized) actionBarSize + 8 else 0).toFloat()
        val params = findViewById<RelativeLayout>(R.id.umEditorHolder).layoutParams as ViewGroup.MarginLayoutParams
        params.bottomMargin = marginBottomValue.toInt()

    }


    /**
     * Insert media file to the editor
     * @throws IOException Exception thrown when something is wrong
     */
    @Throws(IOException::class)
    private fun insertMedia(rawFile: File?) {
        progressDialog!!.visibility = View.VISIBLE

        val mFile = if (mimeType.contains("image"))
            Compressor(this)
                    .setQuality(75)
                    .setCompressFormat(Bitmap.CompressFormat.WEBP)
                    .compressToFile(rawFile)
        else
            rawFile

        GlobalScope.launch {
            val inserted = presenter.handleAddMediaContent(mFile!!.absolutePath, mimeType)
            if(inserted){
                isOpeningFilePickerOrCamera = false
                runOnUiThread {
                    progressDialog!!.visibility = View.GONE
                    executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + "insertMediaContent",
                            this@ContentEditorActivity, mFile.name, mimeType)
                }
            }else{
                showErrorMessage(getString(R.string.failed))
            }

        }
    }


    override fun setContentBold() {
        executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + "textFormattingBold", this)
    }

    override fun setContentItalic() {
        executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + "textFormattingItalic", this)
    }

    override fun setContentUnderlined() {
        executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + "textFormattingUnderline", this)
    }

    override fun setContentStrikeThrough() {
        executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + "textFormattingStrikeThrough", this)
    }

    override fun setContentFontSize(fontSize: String) {
        executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + "setFontSize", this, fontSize)
    }

    override fun setContentSuperscript() {
        executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + "textFormattingSuperScript", this)
    }

    override fun setContentSubScript() {
        executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + "textFormattingSubScript", this)
    }

    override fun setContentJustified() {
        executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + "paragraphFullJustification", this)
    }

    override fun setContentCenterAlign() {
        executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + "paragraphCenterJustification", this)
    }

    override fun setContentLeftAlign() {
        executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + "paragraphLeftJustification", this)
    }

    override fun setContentRightAlign() {
        executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + "paragraphRightJustification", this)
    }

    override fun setContentOrderedList() {
        executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + "paragraphOrderedListFormatting", this)
    }

    override fun setContentUnOrderList() {
        executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + "paragraphUnOrderedListFormatting", this)
    }

    override fun setContentIncreaseIndent() {
        executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + "paragraphIndent", this)
    }

    override fun setContentDecreaseIndent() {
        executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + "paragraphOutDent", this)
    }

    override fun setContentRedo() {
        executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + "editorActionRedo", this)
    }

    override fun setContentUndo() {
        executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + "editorActionUndo", this)
    }

    override fun setContentTextDirection(command: String) {
        executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + if (command == ContentEditorView.ACTION_TEXT_DIRECTION_RTL)
            "textDirectionRightToLeft"
        else
            "textDirectionLeftToRight", this)
        invalidateOptionsMenu()
    }

    override fun insertMultipleChoiceQuestion() {
        executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + "insertMultipleChoiceWidget", this)
    }

    override fun insertFillTheBlanksQuestion() {
        executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + "insertFillTheBlanksWidget", this)
    }

    override fun saveContent() {
        executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + "saveContent", this, "true")
    }

    override fun insertContent(content: String) {
        executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + "insertContentRaw",
                this, content)
    }

    override fun selectAllContent() {
        executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + ContentEditorView.ACTION_SELECT_ALL, this)
    }

    override fun clearEditableSection() {
        executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + ContentEditorView.ACTION_CLEAR_ALL, this)
    }


    override fun focusNextLink() {
        executeJsFunction(mWebView!!, EDITOR_METHOD_PREFIX + ContentEditorView.ACTION_FOCUS_NEXT_LINK, this)
    }

    override fun loadPage(pageUrl: String) {
        mWebView!!.webViewClient = UmWebContentEditorClient(this, false)
        mWebView!!.clearCache(true)
        mWebView!!.clearHistory()
        mWebView!!.loadUrl(pageUrl)
        progressDialog!!.visibility = View.VISIBLE

    }


    companion object {

        private const val PAGE_NAME_TAG = "page_name"

        private const val DIRECTION_TAG = "directionality"

        const val CAMERA_IMAGE_CAPTURE_REQUEST = 900

        const val EDITOR_METHOD_PREFIX = "UmEditorCore."

        private var umRepo : UmAppDatabase ? = null

        private var umDb: UmAppDatabase ? = null
    }
}
