package com.ustadmobile.staging.port.android.view


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.LocationDetailPresenter
import com.ustadmobile.core.db.dao.LocationDao
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.view.LocationDetailView
import com.ustadmobile.lib.db.entities.Location
import com.ustadmobile.port.android.view.UstadBaseActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.dimorinny.floatingtextbutton.FloatingTextButton
import tellh.com.recyclertreeview_lib.TreeNode
import tellh.com.recyclertreeview_lib.TreeViewAdapter
import tellh.com.recyclertreeview_lib.TreeViewBinder
import java.util.*
import kotlin.collections.ArrayList

class LocationDetailActivity : UstadBaseActivity(), LocationDetailView {

    private var toolbar: Toolbar? = null
    private var mPresenter: LocationDetailPresenter? = null
    private var mRecyclerView: RecyclerView? = null
    private var locationTitle: EditText? = null
    //Adapter for tree
    private var adapter: TreeViewAdapter? = null
    internal var locationDao: LocationDao ?= null
    internal var selectedOptions: HashMap<String, Long>? = null
    internal var selectedLocationList: ArrayList<Long>? = null
    private var currentLocationUid: Long = 0

    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_location_detail)

        //Toolbar:
        toolbar = findViewById(R.id.activity_location_detail_toolbar)
        toolbar!!.setTitle(getText(R.string.add_edit_location))
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        locationTitle = findViewById(R.id.activity_location_detail_name)

        //RecyclerView
        mRecyclerView = findViewById(R.id.activity_location_detail_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(this)
        mRecyclerView!!.setLayoutManager(mRecyclerLayoutManager)

        val repository = UmAccountManager.getRepositoryForActiveAccount(applicationContext!!)
        locationDao = repository.locationDao

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        //Call the Presenter
        mPresenter = LocationDetailPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        selectedLocationList = mPresenter!!.getSelectedLocationsList()

        locationTitle!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                mPresenter!!.updateLocationTitle(s.toString())
            }
        })

        //FAB and its listener
        val fab = findViewById<FloatingTextButton>(R.id.activity_location_detail_fab)

        fab.setOnClickListener { v -> mPresenter!!.handleClickDone() }


    }

    override fun populateTopLocation(locations: List<Location>) {
        val nodes = ArrayList<TreeNode<*>>()

        for (every_location in locations) {
            val childLocationUid = every_location.locationUid

            if (childLocationUid == currentLocationUid) {
                continue
            }

            var selected = false
            if (selectedLocationList != null && selectedLocationList!!.contains(childLocationUid)) {
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
                val result = locationDao!!.findAllChildLocationsForUidExceptSelectedUidAsync(childLocationUid, currentLocationUid)
                val p = PopulateTreeNodeCallback(childNode)
                p.onSuccess(result)
            }
        }

        //Init adapter with the location node binder as types of data to accept
        adapter = TreeViewAdapterWithBind(nodes,
                Arrays.asList(LocationNodeBinder(mPresenter!!)))

        //Set adapter to Recycler view.
        runOnUiThread { mRecyclerView!!.setAdapter(adapter) }


        //Set adapter listener - on click the whole node itself. - Not the select listener.
        adapter!!.setOnTreeNodeListener(object : TreeViewAdapter.OnTreeNodeListener {

            override fun onClick(treeNode: TreeNode<*>, viewHolder: RecyclerView.ViewHolder): Boolean {
                if (!treeNode.isLeaf()) {
                    //If not a leaf, expand it:

                    val nodeList = treeNode.getChildList()
                    for (childNode in nodeList) {
                        if (childNode.isLeaf()) {
                            //Find all child's children and add then to the node
                            // (via PopulateTreeNodeCallback class)
                            val childLocationUid = (childNode.getContent() as LocationLayoutType).uid
                            GlobalScope.launch {
                                val result = locationDao!!.findAllChildLocationsForUidExceptSelectedUidAsync(
                                        childLocationUid, currentLocationUid)
                                val p = PopulateTreeNodeCallback(childNode)

                            }
                        }
                    }
                    //Toggle it
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

    override fun updateLocationOnView(location: Location) {
        if (location != null) {
            //Set current location uid so that we can avoid it.
            currentLocationUid = location.locationUid

            //Set title and add its uid to selected Location list so we can show it.
            locationTitle!!.setText(location.title)
            selectedLocationList = ArrayList<Long>()
            selectedLocationList!!.add(location.parentLocationUid)
        }
    }

    private inner class PopulateTreeNodeCallback(private val node: TreeNode<*>)
        : UmCallback<List<Location>> {

        override fun onSuccess(result: List<Location>?) {
            runOnUiThread {
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
                if (!result!!.isEmpty()) {
                    (node.getContent() as LocationLayoutType).leaf = false
                } else {
                    (node.getContent() as LocationLayoutType).leaf = true
                }

            }

            if (!result!!.isEmpty()) {
                (node.getContent() as LocationLayoutType).leaf = false
            } else {
                (node.getContent() as LocationLayoutType).leaf = true
            }
        }

        override fun onFailure(exception: Throwable?) {
            print(exception!!.message)
        }
    }

    /**
     * Custom TreeView Adapter written so that we can work with onBindView and manipulate the
     * view on every tree node.
     *
     */
    inner class TreeViewAdapterWithBind(nodes: List<TreeNode<*>>,
                    viewBinders:List<TreeViewBinder<*>>) : TreeViewAdapter(nodes, viewBinders) {


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
                locationCB.setChecked(true)
            } else {
                locationCB.setChecked(false)
            }

            arrowIV.setVisibility(if (displayNodeContent.leaf) View.INVISIBLE else View.VISIBLE)

            locationCB.setOnClickListener{ v ->
                removeAllChecks()

                if (locationCB.isChecked()) {
                    addLocation(locationUid)
                    locationCB.setChecked(true)
                    holder.itemView.post({ notifyDataSetChanged() })
                } else {
                    locationCB.setChecked(false)
                }

            }


        }

        fun addLocation(locationUid: Long) {
            selectedLocationList = ArrayList()
            selectedLocationList!!.add(locationUid)
            mPresenter!!.setSelectedLocationsList(selectedLocationList!!)
        }

        fun removeAllChecks() {
            selectedLocationList = ArrayList()

        }
    }

}
