package com.ustadmobile.port.android.view

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.DrawableCompat.setTint
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SelectMultipleLocationTreeDialogPresenter
import com.ustadmobile.core.db.dao.LocationDao
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UMAndroidUtil.bundleToMap
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.DismissableDialog
import com.ustadmobile.core.view.SelectMultipleLocationTreeDialogView
import com.ustadmobile.lib.db.entities.Location
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tellh.com.recyclertreeview_lib.TreeNode
import tellh.com.recyclertreeview_lib.TreeViewAdapter
import tellh.com.recyclertreeview_lib.TreeViewBinder


class SelectMultipleLocationTreeDialogFragment : UstadDialogFragment(), SelectMultipleLocationTreeDialogView, DismissableDialog {

    override val viewContext: Any
        get() = activity!!

    lateinit var toolbar: Toolbar
    lateinit var dialog: AlertDialog
    lateinit var rootView: View

    //Recycler view for the tree
    private lateinit var recyclerView: RecyclerView
    //Adapter for tree - tellh's TreeViewAdapter
    private lateinit var adapter: TreeViewAdapter

    //Context (activity calling this)
    private var mAttachedContext: Context? = null

    //Daos
    lateinit var locationDao: LocationDao

    //Presenter
    lateinit var mPresenter: SelectMultipleLocationTreeDialogPresenter

    //Selected Location Items
    var selectedOptions: MutableMap<String, Long>? = null
    var selectedLocationList: List<Long>? = null

    interface MultiSelectLocationTreeDialogListener {
        fun onLocationResult(selected: MutableMap<String, Long>)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = context!!.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        rootView = inflater.inflate(R.layout.fragment_select_multiple_tree_dialog, null)

        //Set up Recycler view
        recyclerView = rootView.findViewById(R.id.fragment_select_multiple_tree_dialog_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)

        //Toolbar
        toolbar = rootView.findViewById(R.id.fragment_select_multiple_tree_dialog_toolbar)

        //Set up icon to toolbar
        var upIcon = AppCompatResources.getDrawable(context!!,
                R.drawable.ic_arrow_back_white_24dp)
        upIcon = getTintedDrawable(upIcon!!, R.color.icons)
        toolbar.navigationIcon = upIcon
        toolbar.setNavigationOnClickListener { dialog.dismiss() }
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
                bundleToMap(arguments), this, repository.locationDao)
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState))

        //Get any locations from given Uids (from previous activity)
        selectedLocationList = mPresenter.selectedLocationsList

        dialog = AlertDialog.Builder(context!!, R.style.FullScreenDialogStyle)
                .setView(rootView)
                .setTitle("")
                .create()
        return dialog

    }

    override fun setTitle(title: String) {
        toolbar.title = title
    }


    override fun populateTopLocation(locations: List<Location>) {
        val nodes = mutableListOf<TreeNode<EntityLayoutType>>()

        //Add every Tree entry to the list of notes (ie: Every Location)
        for (everyTopLocation in locations) {
            val topLocationUid = everyTopLocation.locationUid
            var selected = false
            if (selectedLocationList?.contains(topLocationUid) == true) {
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
        for (childNode in nodes) {
            val childLocationUid = (childNode.content as EntityLayoutType).uid
            populateTreeNode(childNode, childLocationUid)
        }

        //Init adapter with the location node binder as types of data to accept
        adapter = TreeViewAdapterWithBind(this, nodes,
                listOf(EntityNodeBinder(mPresenter)))

        //Set adapter to Recycler view.
        runOnUiThread(Runnable {
            recyclerView.adapter = adapter
        })

        //Set adapter listener
        adapter.setOnTreeNodeListener(object : TreeViewAdapter.OnTreeNodeListener {

            override fun onClick(treeNode: TreeNode<*>, viewHolder: RecyclerView.ViewHolder): Boolean {
                if (!treeNode.isLeaf) {
                    //A warning is expected
                    val nodeList = treeNode.childList
                    for (childNode in nodeList) {
                        if (childNode.isLeaf) {
                            //Find all child's children and add then to the node
                            // (via PopulateTreeNodeCallback class)
                            val childLocationUid = (childNode.content as EntityLayoutType).uid
                            //Get child locations :
                            populateTreeNode(childNode as TreeNode<EntityLayoutType>, childLocationUid)
                        }
                    }
                    onToggle(treeNode.isExpand, viewHolder)
                } else {

                    val arrowIV = viewHolder.itemView.findViewById<View>(
                            R.id.item_select_multiple_tree_dialog_arrow)

                    arrowIV.visibility = if ((treeNode.content as EntityLayoutType).leaf)
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

    override fun finish() {
        selectedOptions = mPresenter.selectedOptions
        if (mAttachedContext is MultiSelectLocationTreeDialogListener) {
            (mAttachedContext as MultiSelectLocationTreeDialogListener).onLocationResult(selectedOptions!!)
        }
        dialog.dismiss()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mAttachedContext = context
        this.selectedOptions = mutableMapOf()
    }

    override fun onDetach() {
        super.onDetach()
        this.mAttachedContext = null
        this.selectedOptions = null
    }

    private fun getTintedDrawable(drawable: Drawable, color: Int): Drawable {
        var drawable = drawable
        drawable = DrawableCompat.wrap(drawable)
        val tintColor = ContextCompat.getColor(mAttachedContext!!, color)
        setTint(drawable, tintColor)
        return drawable
    }


    /**
     * Custom TreeView Adapter written so that we can work with onBindView and manipulate the
     * view on every tree node.
     *
     */
    inner class TreeViewAdapterWithBind(private val selectMultipleTreeDialogFragment: SelectMultipleLocationTreeDialogFragment,
                                        nodes: List<TreeNode<*>>,
                                        viewBinders: List<TreeViewBinder<*>>) : TreeViewAdapter(nodes, viewBinders) {


        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            super.onBindViewHolder(holder, position)

            val locationCB = holder.itemView.findViewById<CheckBox>(
                    R.id.item_select_multiple_tree_dialog_checkbox)
            val arrowIV = holder.itemView.findViewById<View>(
                    R.id.item_select_multiple_tree_dialog_arrow)

            val displayNodesIterator = displayNodesIterator
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

            val displayNodeContent = displayNode!!.content as EntityLayoutType
            val locationUid = displayNodeContent.uid

            locationCB.isChecked = selectMultipleTreeDialogFragment.selectedLocationList != null && selectMultipleTreeDialogFragment.selectedLocationList!!.contains(locationUid)

            arrowIV.visibility = if (displayNodeContent.leaf) View.INVISIBLE else View.VISIBLE

        }
    }

}