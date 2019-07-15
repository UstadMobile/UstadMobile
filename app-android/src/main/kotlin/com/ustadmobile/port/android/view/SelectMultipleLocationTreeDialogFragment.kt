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
import com.ustadmobile.core.controller.SelectMultipleLocationTreeDialogPresenter
import com.ustadmobile.core.db.dao.LocationDao
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.DismissableDialog
import com.ustadmobile.core.view.SelectMultipleLocationTreeDialogView
import com.ustadmobile.lib.db.entities.Location
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
class SelectMultipleLocationTreeDialogFragment : UstadDialogFragment(), SelectMultipleLocationTreeDialogView, DismissableDialog {
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
    internal lateinit var locationDao: LocationDao

    //Presenter
    internal lateinit var mPresenter: SelectMultipleLocationTreeDialogPresenter

    //Selected Location Items
    internal var selectedOptions: HashMap<String, Long>? = null
    internal var selectedLocationList: List<Long>? = null

    //The interface that the hosting activity will implement. The internal method will get called
    // back with the values
    interface MultiSelectLocationTreeDialogListener {
        fun onLocationResult(selected: HashMap<String, Long>)
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
        recyclerView = rootView.findViewById(R.id.fragment_select_multiple_tree_dialog_recyclerview)
        recyclerView!!.layoutManager = LinearLayoutManager(context)

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
                finish() //This will send back the selection to the activity calling this fragment.
            }
            false
        }
        toolbar.setTitle(R.string.select_locations)

        //TODO: Get dao - Were using DAOs in the Fragment - Something we should consider changing.
        val repository = UmAccountManager.getRepositoryForActiveAccount(context!!)
        locationDao = repository.locationDao

        //Presenter
        mPresenter = SelectMultipleLocationTreeDialogPresenter(context!!,
                UMAndroidUtil.bundleToMap(arguments), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //Get any locations from given Uids (from previous activity)
        selectedLocationList = mPresenter.getSelectedLocationsList()

        dialog = AlertDialog.Builder(context!!, R.style.FullScreenDialogStyle)
                .setView(rootView)
                .setTitle("")
                .create()
        return dialog

    }

    fun populateTreeNode(childNode: TreeNode<EntityLayoutType>, childLocationUid: Long) {
        GlobalScope.launch {
            val locationList = locationDao.findAllChildLocationsForUidAsync(childLocationUid)
            (childNode.content as EntityLayoutType).leaf = locationList.isEmpty()
            runOnUiThread(Runnable {
                for (everyLocation in locationList) {
                    val locationUid = everyLocation.locationUid
                    var selected = false
                    if (selectedLocationList!!.contains(locationUid)) {
                        selected = true
                    }

                    childNode.addChild(TreeNode(
                            EntityLayoutType(everyLocation.title!!,
                                    locationUid, selected, false)))
                }

                (childNode.content as EntityLayoutType).leaf = locationList.isEmpty()
            })
        }
    }

    override fun populateTopLocation(locations: List<Location>) {
        //1. Create a list of nodes - represents an entry in the Tree list
        val nodes = mutableListOf<TreeNode<EntityLayoutType>>()

        //Add every Tree entry to the list of notes (ie: Every Location)
        for (everyTopLocation in locations) {
            val topLocationUid = everyTopLocation.locationUid
            var selected = false
            if (selectedLocationList!!.contains(topLocationUid)) {
                selected = true
            }

            val topLocationEntry = TreeNode(
                    EntityLayoutType(
                            everyTopLocation.title!!,
                            everyTopLocation.locationUid,
                            selected, false
                    )
            )

            //Add the layout entry to nodes
            nodes.add(topLocationEntry)
        }


        //Load children of every Top locations into PopulateLocationTreeNodeCallback
//        for (childNode in nodes) {
//            val childLocationUid = (childNode.getContent() as EntityLayoutType).uid!!
//            locationDao.findAllChildLocationsForUidAsync(childLocationUid,
//                    PopulateLocationTreeNodeCallback(childNode))
//        }

        for (childNode in nodes) {
            val childLocationUid = (childNode.content as EntityLayoutType).uid
            populateTreeNode(childNode, childLocationUid!!)
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
                            val childLocationUid = (childNode.getContent() as EntityLayoutType).uid!!
                            //Get child locations :
                            populateTreeNode(childNode as TreeNode<EntityLayoutType>, childLocationUid)
//                            locationDao.findAllChildLocationsForUidAsync(childLocationUid,
//                                    PopulateLocationTreeNodeCallback(childNode))
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
        if (mAttachedContext is MultiSelectLocationTreeDialogListener) {
            (mAttachedContext as MultiSelectLocationTreeDialogListener).onLocationResult(selectedOptions!!)
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


    /**
     * Helper method to get color a given drawable
     * @param drawable  The drawable to color
     * @param color     The color
     * @return          The colored drawable
     */
    fun getTintedDrawable(drawable: Drawable?, color: Int): Drawable {
        var drawable = drawable
        drawable = DrawableCompat.wrap(drawable!!)
        val tintColor = ContextCompat.getColor(mAttachedContext!!, color)
        DrawableCompat.setTint(drawable!!, tintColor)
        return drawable
    }

//    /**
//     * A Custom callback - on success will check the return list of entities,
//     * loop over them, create child nodes and add them to the parent TreeNode variable.
//     * If there are no entities returned, it will treat the current node as a leaf.
//     */
//    private inner class PopulateLocationTreeNodeCallback private constructor(private val node: TreeNode) : UmCallback<List<Location>> {
//
//        override fun onSuccess(result: List<Location>) {
//            runOnUiThread {
//                for (everyLocation in result) {
//                    val locationUid = everyLocation.locationUid
//                    var selected = false
//                    if (selectedLocationList!!.contains(locationUid)) {
//                        selected = true
//                    }
//
//                    node.addChild(TreeNode(
//                            EntityLayoutType(everyLocation.title!!,
//                                    locationUid, selected, false)))
//                }
//
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
     * view on every tree node.
     *
     */
    inner class TreeViewAdapterWithBind(
            private val selectMultipleTreeDialogFragment: SelectMultipleLocationTreeDialogFragment,
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

            if (selectMultipleTreeDialogFragment.selectedLocationList != null
                    && selectMultipleTreeDialogFragment.selectedLocationList!!.contains(locationUid)) {
                locationCB.isChecked = true
            } else {
                locationCB.isChecked = false
            }

            arrowIV.visibility = if (displayNodeContent.leaf) View.INVISIBLE else View.VISIBLE

        }
    }
}
