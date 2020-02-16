package com.ustadmobile.port.android.view

import android.app.Dialog
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SelectMultipleProductTypeTreeDialogPresenter
import com.ustadmobile.core.db.dao.SaleProductParentJoinDao
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.DismissableDialog
import com.ustadmobile.core.view.SelectMultipleProductTypeTreeDialogView
import com.ustadmobile.lib.db.entities.SaleProduct
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tellh.com.recyclertreeview_lib.TreeNode
import tellh.com.recyclertreeview_lib.TreeViewAdapter
import tellh.com.recyclertreeview_lib.TreeViewBinder

import java.util.Arrays
import java.util.HashMap


/**
 * The activity that is a full screen dialog that selects items from a tree structure.
 * Designed to be common for both Location and Classes (although classes wont be in a tree structure).
 * The data should be provided to it to load.
 */
class SelectMultipleProductTypeTreeDialogFragment : UstadDialogFragment(),
        SelectMultipleProductTypeTreeDialogView, DismissableDialog {
    override val viewContext: Any
        get() = context!!

    //Fragment view components:
    internal lateinit var toolbar: Toolbar
    internal lateinit var dialog: AlertDialog
    internal lateinit var rootView: View

    //Recycler view for the tree
    private lateinit var recyclerView: RecyclerView
    //Adapter for tree - tellh's TreeViewAdapter
    private var adapter: TreeViewAdapter? = null

    //Context (activity calling this)
    private var mAttachedContext: Context? = null

    //Daos
    internal lateinit var joinDao: SaleProductParentJoinDao

    //Presenter
    internal lateinit var mPresenter: SelectMultipleProductTypeTreeDialogPresenter

    //Selected Location Items
    internal var selectedOptions: HashMap<String, Long>? = null
    internal var selectedSaleProductUidList: List<Long>? = null

    //The interface that the hosting activity will implement. The internal method will get called
    // back with the values
    interface MultiSelectProductTypeTreeDialogListener {

        fun onProductTypesResult(selected: HashMap<String, Long>)

    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)

        val item = menu!!.findItem(R.id.menu_done)

        //Get the icon itself.
        val itemIcon = resources.getDrawable(R.drawable.ic_check_white_24dp)
        itemIcon.setTint(resources.getColor(R.color.icons))
        itemIcon.setColorFilter(resources.getColor(R.color.icons), PorterDuff.Mode.SRC_IN)
        item.icon = itemIcon

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = context!!.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        rootView = inflater.inflate(R.layout.fragment_select_multiple_tree_dialog, null)

        //Set up Recycler view
        initView()

        //Toolbar
        toolbar = rootView.findViewById(R.id.fragment_select_multiple_tree_dialog_toolbar)

        //Set up icon to toolbar
        var upIcon = AppCompatResources.getDrawable(context!!,
                R.drawable.ic_arrow_back_white_24dp)
        upIcon = getTintedDrawable(upIcon, R.color.icons)
        toolbar.navigationIcon = upIcon
        toolbar.setNavigationOnClickListener { v -> dialog.dismiss() }

        toolbar.inflateMenu(R.menu.menu_done)
        toolbar.setOnMenuItemClickListener { item ->
            val i = item.itemId
            if (i == R.id.menu_done) {
                mPresenter.handleClickPrimaryActionButton()
            }
            false
        }
        toolbar.setTitle(R.string.select_product_types)

        //TODO: Get dao - Were using DAOs in the Fragment - Something we should consider changing.
        val repository = UmAccountManager.getRepositoryForActiveAccount(context!!)
        joinDao = repository.saleProductParentJoinDao

        mPresenter = SelectMultipleProductTypeTreeDialogPresenter(context!!,
                UMAndroidUtil.bundleToMap(arguments), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        selectedSaleProductUidList = mPresenter.getSelectedProductTypeUidsList()

        dialog = AlertDialog.Builder(context!!, R.style.FullScreenDialogStyle)
                .setView(rootView)
                .setTitle("")
                .create()
        return dialog

    }

    fun getTintedDrawable(drawable: Drawable?, color: Int): Drawable {
        var drawable = drawable
        drawable = DrawableCompat.wrap(drawable!!)
        val tintColor = ContextCompat.getColor(mAttachedContext!!, color)
        DrawableCompat.setTint(drawable!!, tintColor)
        return drawable
    }

    private fun initView() {
        //Set recycler view
        recyclerView = rootView.findViewById(R.id.fragment_select_multiple_tree_dialog_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)
    }


    fun populateTreeNode(childNode: TreeNode<EntityLayoutType>, childLocationUid: Long) {
        GlobalScope.launch {
            val locationList = joinDao.findAllChildProductTypesForUidAsync(childLocationUid)
            (childNode.content as EntityLayoutType).leaf = locationList.isEmpty()
            runOnUiThread(Runnable {
                for (everyProduct in locationList) {
                    val locationUid = everyProduct.saleProductUid
                    var selected = false
                    if (selectedSaleProductUidList!!.contains(locationUid)) {
                        selected = true
                    }

                    childNode.addChild(TreeNode(
                            EntityLayoutType(everyProduct.saleProductName!!,
                                    locationUid, selected, false)))
                }

                (childNode.content as EntityLayoutType).leaf = locationList.isEmpty()
            })
        }
    }


    override fun populateTopProductType(productTypes: List<SaleProduct>) {
        val nodes = mutableListOf<TreeNode<EntityLayoutType>>()

        for (everyProductType in productTypes) {
            val childLocationUid = everyProductType.saleProductUid
            var selected = false
            if (selectedSaleProductUidList!!.contains(childLocationUid)) {
                selected = true
            }
            val app = TreeNode(
                    EntityLayoutType(
                            everyProductType.getNameLocale(UstadMobileSystemImpl.instance.getLocale(activity!!.applicationContext)),
                            everyProductType.saleProductUid,
                            selected, false
                    )
            )
            nodes.add(app)
        }

//        for (childNode in nodes) {
//            val childProductTypeUid = (childNode.getContent() as EntityLayoutType).uid!!
//            joinDao.findAllChildProductTypesForUidAsync(childProductTypeUid,
//                    PopulateSaleProductTreeNodeCallback(childNode))
//        }

        for (childNode in nodes) {
            val childProductTypeUid = (childNode.content as EntityLayoutType).uid
            populateTreeNode(childNode , childProductTypeUid!!)
        }

        //Init adapter with the location node binder as types of data to accept
        adapter = TreeViewAdapterWithBind(this, nodes,
                Arrays.asList(EntityNodeBinder(mPresenter)))

        //Set adapter to Recycler view.
        runOnUiThread (Runnable{ recyclerView.adapter = adapter })

        //Set adapter listener
        adapter!!.setOnTreeNodeListener(object : TreeViewAdapter.OnTreeNodeListener {

            override fun onClick(treeNode: TreeNode<*>, viewHolder: RecyclerView.ViewHolder): Boolean {
                if (!treeNode.isLeaf()) {
                    //A warning is expected
                    val nodeList = treeNode.getChildList()
                    for (childNode in nodeList) {
                        if (childNode.isLeaf()) {
                            //Find all child's children and add then to the node
                            // (via PopulateTreeNodeCallback class)
                            val childProductTypeUid = (childNode.getContent() as EntityLayoutType).uid!!
                            //Get child locations :
                            populateTreeNode(childNode as TreeNode<EntityLayoutType>, childProductTypeUid)
//                            joinDao.findAllChildProductTypesForUidAsync(childProductTypeUid,
//                                    PopulateSaleProductTreeNodeCallback(childNode))
                        }
                    }
                    onToggle(treeNode.isExpand(), viewHolder)
                } else {

                    val arrowIV = viewHolder.itemView.findViewById<ImageView>(
                            R.id.item_select_multiple_tree_dialog_arrow)

                    arrowIV.visibility = if ((treeNode.getContent() as EntityLayoutType).leaf)
                        View.INVISIBLE
                    else
                        View.VISIBLE
                }

                return false
            }

            override fun onToggle(b: Boolean, viewHolder: RecyclerView.ViewHolder) {

                //Change icon of the item.
                val locationViewHolder = viewHolder as EntityNodeBinder.TreeHolder
                val arrowImage = locationViewHolder.ivArrow
                val rotateDegree = if (b) 90 else -90
                arrowImage.animate().rotationBy(rotateDegree.toFloat()).start()

            }
        })
    }

    override fun setTitle(title: String) {
        toolbar.title = title
    }

    override fun finish() {
        //Get the selected Option, and pass it to the activity implementing this
        // (parent activity usually)
        selectedOptions = mPresenter.selectedOptions
        if (mAttachedContext is MultiSelectProductTypeTreeDialogListener) {
            (mAttachedContext as
                    MultiSelectProductTypeTreeDialogListener).onProductTypesResult(selectedOptions!!)
        }
        dialog.dismiss()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.mAttachedContext = context
        this.selectedOptions = HashMap()
    }

    override fun onDetach() {
        super.onDetach()
        this.mAttachedContext = null
        this.selectedOptions = null
    }


//    /**
//     * A Custom callback - on success will check the return list of entities,
//     * loop over them, and add them to the node as children on the view.
//     * If there are no entities returned, it will treat the current node as a leaf.
//     */
//    private inner class PopulateSaleProductTreeNodeCallback private constructor(private val node: TreeNode) : UmCallback<List<SaleProduct>> {
//
//        override fun onSuccess(result: List<SaleProduct>) {
//            runOnUiThread {
//                for (childLocations in result) {
//                    val childLocationUid = childLocations.saleProductUid
//                    var selected = false
//                    if (selectedSaleProductUidList!!.contains(childLocationUid)) {
//                        selected = true
//                    }
//
//                    node.addChild(TreeNode(
//                            EntityLayoutType(childLocations.saleProductName!!,
//                                    childLocationUid, selected, false)))
//                }
//                if (!result.isEmpty()) {
//                    (node.getContent() as EntityLayoutType).leaf = false
//                } else {
//                    (node.getContent() as EntityLayoutType).leaf = true
//                }
//
//            }
//
//            if (!result.isEmpty()) {
//                (node.getContent() as EntityLayoutType).leaf = false
//            } else {
//                (node.getContent() as EntityLayoutType).leaf = true
//            }
//        }
//
//        override fun onFailure(exception: Throwable) {
//            exception.printStackTrace()
//        }
//    }

    /**
     * Custom TreeView Adapter written so that we can work with onBindView and manipulate the
     * view on every tree node. The type is of the EntityLayoutType POJO
     *
     */
    inner class TreeViewAdapterWithBind(
            private val selectMultipleTreeDialogFragment: SelectMultipleProductTypeTreeDialogFragment,
            nodes: List<TreeNode<*>>,
            viewBinders: List<TreeViewBinder<*>>)
        : TreeViewAdapter(nodes, viewBinders) {


        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            super.onBindViewHolder(holder, position)

            val locationCB = holder.itemView.findViewById<CheckBox>(
                    R.id.item_select_multiple_tree_dialog_checkbox)
            val arrowIV = holder.itemView.findViewById<ImageView>(
                    R.id.item_select_multiple_tree_dialog_arrow)

            val displayNodesIterator = getDisplayNodesIterator()
            var displayNode: TreeNode<*>? = null
            var i = 0
            while (displayNodesIterator.hasNext()) {
                displayNode = displayNodesIterator.next()
                if (i >= position) {
                    break
                } else {
                    i++
                }
            }

            val displayNodeContent = displayNode!!.getContent() as EntityLayoutType
            val locationUid = displayNodeContent.uid!!

            if (selectMultipleTreeDialogFragment.selectedSaleProductUidList != null
                    && selectMultipleTreeDialogFragment.selectedSaleProductUidList!!.contains(locationUid)) {
                locationCB.isChecked = true
            } else {
                locationCB.isChecked = false
            }

            arrowIV.visibility = if (displayNodeContent.leaf) View.INVISIBLE else View.VISIBLE

        }
    }
}
