package com.ustadmobile.staging.port.android.view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.AdapterView
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
import com.ustadmobile.core.controller.SelectMultipleTreeDialogPresenter
import com.ustadmobile.core.db.dao.LocationDao
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.view.DismissableDialog
import com.ustadmobile.core.view.SelectMultipleTreeDialogView
import com.ustadmobile.lib.db.entities.Location
import com.ustadmobile.port.android.view.UstadDialogFragment
import io.reactivex.annotations.NonNull
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tellh.com.recyclertreeview_lib.TreeNode
import tellh.com.recyclertreeview_lib.TreeViewAdapter
import tellh.com.recyclertreeview_lib.TreeViewBinder
import java.util.*

/**
 * The activity that is a full screen dialog that selects items from a tree structure.
 * Designed to be common for both Location and Classes (although classes wont be in a tree structure).
 * The data should be provided to it to load.
 */
class SelectMultipleTreeDialogFragment : UstadDialogFragment(), SelectMultipleTreeDialogView,
        AdapterView.OnItemSelectedListener, DialogInterface.OnClickListener,
        DialogInterface.OnShowListener, View.OnClickListener, DismissableDialog {
    override val viewContext: Any
        get() = context!!


    internal lateinit var dialog: AlertDialog
    internal lateinit var rootView: View

    //Recycler view for the tree
    private var recyclerView: RecyclerView? = null
    //Adapter for tree
    private var adapter: TreeViewAdapter? = null

    //Context (activity calling this)
    private var mAttachedContext: Context? = null

    internal lateinit var toolbar: Toolbar
    internal lateinit var locationDao: LocationDao

    internal var selectedOptions: HashMap<String, Long>? = null

    internal lateinit var mPresenter: SelectMultipleTreeDialogPresenter

    internal var selectedLocationList: List<Long>? = null

    interface MultiSelectTreeDialogListener {

        fun onLocationResult(selected: HashMap<String, Long>)

    }

    private inner class PopulateTreeNodeCallback(private val node: TreeNode<*>)
        : UmCallback<List<Location>> {

        override fun onSuccess(result: List<Location>?) {
            runOnUiThread (Runnable{
                for (childLocations in result!!) {
                    val childLocationUid = childLocations.locationUid
                    var selected = false
                    if (selectedLocationList!!.contains(childLocationUid)) {
                        selected = true
                    }

                    node.addChild(TreeNode(
                            LocationLayoutType(childLocations.title!!,
                                    childLocationUid, selected, false)))
                }
                if (!result.isEmpty()) {
                    (node.getContent() as LocationLayoutType).leaf = false
                } else {
                    (node.getContent() as LocationLayoutType).leaf = true
                }

            })

            if (!result!!.isEmpty()) {
                (node.getContent() as LocationLayoutType).leaf = false
            } else {
                (node.getContent() as LocationLayoutType).leaf = true
            }
        }

        override fun onFailure(exception: Throwable?) {
            print(exception!!)
        }
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

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = context!!.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        rootView = inflater.inflate(R.layout.fragment_select_multiple_tree_dialog, null)

        //Set up Recycler view
        initView()

        //Get initial data
        //List<TreeNode> nodes = initTestData();

        toolbar = rootView.findViewById(R.id.um_toolbar)
        toolbar.setTitle(R.string.select_locations)


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

        val repository = UmAccountManager.getRepositoryForActiveAccount(context!!)
        locationDao = repository.locationDao

        mPresenter = SelectMultipleTreeDialogPresenter(context!!,
                UMAndroidUtil.bundleToMap(arguments), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        selectedLocationList = mPresenter.selectedLocationsList

        dialog = AlertDialog.Builder(context!!, R.style.FullScreenDialogStyle)
                .setView(rootView)
                .setTitle("")
                .create()
        return dialog

    }

    fun getTintedDrawable(drawable: Drawable?, color: Int): Drawable {
        var drawable = drawable
        drawable = DrawableCompat.wrap(drawable!!)
        val tintColor = ContextCompat.getColor(context!!, color)
        DrawableCompat.setTint(drawable!!, tintColor)
        return drawable
    }

    /**
     * Custom TreeView Adapter written so that we can work with onBindView and manipulate the
     * view on every tree node.
     *
     */
    inner class TreeViewAdapterWithBind(nodes: List<TreeNode<*>>,
                                        viewBinders:List<TreeViewBinder<*>>)
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

            val displayNodeContent = displayNode!!.getContent() as LocationLayoutType
            val locationUid = displayNodeContent.uid

            if (selectedLocationList != null && selectedLocationList!!.contains(locationUid)) {
                locationCB.isChecked = true
            } else {
                locationCB.isChecked = false
            }

            arrowIV.visibility = if (displayNodeContent.leaf) View.INVISIBLE else View.VISIBLE

        }
    }

    private fun initView() {
        //Set recycler view
        recyclerView = rootView.findViewById(R.id.fragment_select_multiple_tree_dialog_recyclerview)
        recyclerView!!.layoutManager = LinearLayoutManager(context)
    }

    override fun populateTopLocation(locations: List<Location>) {
        val nodes = ArrayList<TreeNode<*>>()

        for (every_location in locations) {
            val childLocationUid = every_location.locationUid
            var selected = false
            if (selectedLocationList!!.contains(childLocationUid)) {
                selected = true
            }
            val app = TreeNode(
                    LocationLayoutType(
                            every_location.title!!, every_location.locationUid,
                            selected, false
                    )
            )
            nodes.add(app)
        }

        for (childNode in nodes) {
            val childLocationUid = (childNode.getContent() as LocationLayoutType).uid
            GlobalScope.launch {
                val result = locationDao.findAllChildLocationsForUidAsync(childLocationUid)
                val p = PopulateTreeNodeCallback(childNode)
                p.onSuccess(result)
            }
        }

        //Init adapter with the location node binder as types of data to accept
        adapter = TreeViewAdapterWithBind(nodes, Arrays.asList(
                LocationNodeBinder(mPresenter)))

        //Set adapter to Recycler view.
        runOnUiThread (Runnable{ recyclerView!!.adapter = adapter })


        //Set adapter listener
        adapter!!.setOnTreeNodeListener(object : TreeViewAdapter.OnTreeNodeListener {

            override fun onClick(treeNode: TreeNode<*>, viewHolder: RecyclerView.ViewHolder):
                    Boolean {
                if (!treeNode.isLeaf()) {
                    val nodeList = treeNode.getChildList()
                    for (childNode in nodeList) {
                        if (childNode.isLeaf()) {
                            //Find all child's children and add then to the node
                            // (via PopulateTreeNodeCallback class)
                            val childLocationUid = (childNode.getContent() as LocationLayoutType).uid
                            GlobalScope.launch {
                                val result = locationDao.findAllChildLocationsForUidAsync(childLocationUid)
                                val p = PopulateTreeNodeCallback(childNode)
                                p.onSuccess(result)
                            }
                        }
                    }
                    onToggle(treeNode.isExpand(), viewHolder)
                }

                return false
            }

            override fun onToggle(b: Boolean, viewHolder: RecyclerView.ViewHolder) {

                //Change icon of the item.
                val locationViewHolder = viewHolder as LocationNodeBinder.ViewHolder
                val arrowImage = locationViewHolder.ivArrow
                val rotateDegree = if (b) 90 else -90
                arrowImage.animate().rotationBy(rotateDegree.toFloat()).start()

            }
        })
    }

    override fun finish() {
        selectedOptions = mPresenter.selectedOptions
        if (mAttachedContext is MultiSelectTreeDialogListener) {
            (mAttachedContext as MultiSelectTreeDialogListener).onLocationResult(selectedOptions!!)
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

    override fun onClick(dialog: DialogInterface, which: Int) {

    }

    override fun onShow(dialog: DialogInterface) {

    }

    override fun onClick(v: View) {

    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

    }

    override fun onNothingSelected(parent: AdapterView<*>) {

    }
}
