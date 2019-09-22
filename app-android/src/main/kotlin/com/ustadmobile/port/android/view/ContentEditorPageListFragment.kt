package com.ustadmobile.port.android.view


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.MotionEventCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.toughra.ustadmobile.R
import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem
import com.ustadmobile.core.controller.ContentEditorPageActionDelegate
import com.ustadmobile.core.controller.ContentEditorPageListPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UMAndroidUtil.getDirectionality
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEditorPageListView
import com.ustadmobile.port.android.umeditor.UmOnStartDragListener
import com.ustadmobile.port.android.umeditor.UmPageItemTouchAdapter
import com.ustadmobile.port.android.umeditor.UmPageItemTouchCallback
import java.util.*


/**
 * Fragment which handles page removal,  creation and updating
 */
class ContentEditorPageListFragment : UstadDialogFragment(),
        UmOnStartDragListener, ContentEditorPageListView {

    override val viewContext: Any
        get() = activity!!

    private var mItemTouchHelper: ItemTouchHelper? = null

    private val impl = UstadMobileSystemImpl.instance

    private var adapter: PageListAdapter? = null

    private var presenter: ContentEditorPageListPresenter? = null

    private val umDb: UmAppDatabase = UmAccountManager.getRepositoryForActiveAccount(activity!!)

    private var isScrollDirectionUp = false

    private var savedInstanceState: Bundle? = null

    private var titleView: TextView? = null

    private inner class PageListAdapter
    internal constructor(private val mDragStartListener: UmOnStartDragListener)
        : RecyclerView.Adapter<PageListAdapter.PageViewHolder>(), UmPageItemTouchAdapter {

        override fun getItemCount(): Int {
            return pageList.size
        }

        private var currentSelectedPage: String? = null

        private var pageList: MutableList<EpubNavItem> = ArrayList()


        internal fun setPageList(pageList: MutableList<EpubNavItem>, href: String?) {
            this.pageList = pageList
            this.currentSelectedPage = href
            notifyDataSetChanged()
        }

        internal fun setSelectedPage(href: String) {
            this.currentSelectedPage = href
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
            return PageViewHolder(LayoutInflater.from(parent.context).inflate(
                    R.layout.umcontenteditor_filelist_item, parent, false))
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
            val pageItem = pageList[holder.adapterPosition]
            holder.pageTitle.text = pageItem.title
            holder.pageReorderHandle.setOnTouchListener { v, event ->
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onDragStarted(holder)
                }
                false
            }

            holder.pageTitle.setTextColor(getColor(holder.itemView.context,
                    if (pageItem.href == currentSelectedPage)
                        R.color.text_primary
                    else
                        R.color.text_secondary))
            holder.pageOptionHandle.setColorFilter(getColor(holder.itemView.context,
                    if (pageItem.href == currentSelectedPage)
                        R.color.text_primary
                    else
                        R.color.text_secondary))
            holder.pageReorderHandle.setColorFilter(getColor(holder.itemView.context,
                    if (pageItem.href == currentSelectedPage)
                        R.color.text_primary
                    else
                        R.color.text_secondary))
            holder.itemHolder.setBackgroundColor(getColor(holder.itemView.context,
                    if (pageItem.href == currentSelectedPage)
                        R.color.secondary_text_light
                    else
                        R.color.icons))

            holder.pageOptionHandle.setOnClickListener { showPopUpMenu(holder.itemView.context,
                    holder.pageOptionHandle, pageItem) }
            holder.itemView.setOnClickListener { presenter!!.handlePageSelected(pageItem.href!!) }
        }

        private fun getColor(content: Context, resource: Int): Int {
            return ContextCompat.getColor(content, resource)
        }

        private fun showPopUpMenu(context: Context, anchorView: View, pageItem: EpubNavItem) {
            val popup = PopupMenu(context, anchorView)
            popup.menuInflater.inflate(R.menu.menu_content_editor_page_option, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.action_page_update) {
                    presenter!!.handlePageOptionsClicked(pageItem)
                } else if (item.itemId == R.id.action_page_delete) {
                    presenter!!.handleRemovePage(Objects.requireNonNull<String>(pageItem.href))
                }
                true
            }
            popup.show()
        }

        override fun onPageItemMove(fromPosition: Int, toPosition: Int) {
            val oldItem = pageList[fromPosition]
            pageList.removeAt(fromPosition)
            pageList.add(toPosition, oldItem)
            notifyItemMoved(fromPosition, toPosition)
            presenter!!.handlePageOrderChanged(pageList)
        }

        internal inner class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var pageReorderHandle: ImageView = itemView.findViewById(R.id.page_handle)
            var pageTitle: TextView = itemView.findViewById(R.id.page_title)
            var pageOptionHandle: ImageView = itemView.findViewById(R.id.page_option)
            var itemHolder: FrameLayout = itemView.findViewById(R.id.page_item)

        }
    }

    fun setUmFileHelper(pageDelegate: ContentEditorPageActionDelegate) {
        adapter = PageListAdapter(this)
        presenter = ContentEditorPageListPresenter(this,
                UMAndroidUtil.bundleToMap(arguments), this,umDb.contentEntryDao, pageDelegate)
        presenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))
    }

    fun setCurrentPage(href: String) {
        adapter!!.setSelectedPage(href)
    }


    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.PageListStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_content_editor_page_list,
                container, false)
        val toolbar:Toolbar = rootView.findViewById(R.id.toolbar)
        toolbar.setNavigationIcon(if (getDirectionality(activity?.applicationContext!!) == "ltr")
            R.drawable.ic_arrow_back_white_24dp
        else
            R.drawable.ic_arrow_forward_white_24dp)
        this.savedInstanceState = savedInstanceState

        titleView = rootView.findViewById(R.id.document_title)

        val pageListView : RecyclerView = rootView.findViewById(R.id.page_list)
        val btnAddPage:View = rootView.findViewById(R.id.btn_add_page)
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        pageListView.layoutManager = layoutManager
        pageListView.adapter = adapter

        val callback = UmPageItemTouchCallback(adapter!!)
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper!!.attachToRecyclerView(pageListView)

        pageListView.clearOnScrollListeners()
        pageListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    btnAddPage.visibility = View.VISIBLE
                } else {
                    btnAddPage.visibility = View.GONE
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
        pageListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                isScrollDirectionUp = dy > 0
                super.onScrolled(recyclerView, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                btnAddPage.visibility =
                        if (newState != RecyclerView.SCROLL_STATE_IDLE && !isScrollDirectionUp)
                    View.VISIBLE
                else
                    View.GONE
                super.onScrollStateChanged(recyclerView, newState)
            }
        })

        btnAddPage.setOnClickListener {
            presenter!!.handlePageOptionsClicked(
                    null)
        }

        titleView!!.setOnClickListener { presenter!!.handleEditDocument() }

        toolbar.setNavigationOnClickListener { presenter!!.handleDismissDialog() }

        return rootView
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
        }
    }

    @SuppressLint("InflateParams")
    override fun showAddOrUpdatePageDialog(@Nullable page: EpubNavItem?, newPage: Boolean) {
        val isNewPage = page == null

        val dialogTitle = when {
            newPage -> impl.getString(MessageID.content_update_document_title,
                    activity!!)
            isNewPage -> impl.getString(MessageID.content_add_page,
                    activity!!)
            else -> impl.getString(MessageID.content_update_page_title, activity!!)
        }

        val positiveBtnLabel = impl.getString(if (isNewPage)
            MessageID.content_add_page
        else
            MessageID.update, context!!)

        val titleToUpdateFrom = if (isNewPage)
            impl.getString(MessageID.content_untitled_page,
                    activity!!)
        else
            page!!.title

        val builder = AlertDialog.Builder(activity)

        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.umcontent_dialog_option_actionview,
                null, false)

        val titleWrapper :TextInputLayout = dialogView.findViewById(R.id.titleWrapper)
        titleWrapper.hint = impl.getString(MessageID.content_editor_page_view_hint,
                activity!!)

        val titleView:TextInputEditText = dialogView.findViewById(R.id.title)
        titleView.setText(titleToUpdateFrom)
        builder.setView(dialogView)
        builder.setTitle(dialogTitle)

        builder.setPositiveButton(positiveBtnLabel) { dialog, _ ->

            if (isNewPage) {
                presenter!!.handleAddPage(titleView.text.toString())
            } else {
                page!!.title = titleView.text.toString()
                presenter!!.handlePageUpdate(page)
            }
            dialog.dismiss()
        }
        builder.setNegativeButton(impl.getString(MessageID.cancel,
                activity!!)) { dialog, _ -> dialog.dismiss() }
        builder.show()
    }


    override fun onDragStarted(viewHolder: RecyclerView.ViewHolder) {
        mItemTouchHelper!!.startDrag(viewHolder)
    }

    override fun dismissDialog() {
        dismiss()
    }

    override fun setDocumentTitle(title: String) {
        titleView!!.text = title
    }

    override fun updatePageList(pageList: MutableList<EpubNavItem>, selectedPage: String?) {
        adapter!!.setPageList(pageList, selectedPage)
    }
}
