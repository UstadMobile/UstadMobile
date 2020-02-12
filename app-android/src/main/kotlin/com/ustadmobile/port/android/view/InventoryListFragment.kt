package com.ustadmobile.port.android.view


import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.InventoryListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.InventoryListView
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.SaleProductWithInventoryCount
import com.ustadmobile.staging.core.view.FabListener
import com.ustadmobile.staging.core.view.SearchableListener
import ru.dimorinny.floatingtextbutton.FloatingTextButton

class InventoryListFragment : UstadBaseFragment, InventoryListView, SearchableListener, FabListener {

    override val viewContext: Any
        get() = context!!


    private lateinit var rootContainer: View
    private var mRecyclerView: RecyclerView? = null
    private var mPresenter: InventoryListPresenter? = null
    private var fab: FloatingTextButton? = null

    private var sortSpinner: Spinner? = null
    internal lateinit var sortSpinnerPresets: Array<String?>

    init {

    }


    override fun handleClickFAB() {
        mPresenter!!.handleClickAddItems()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        rootContainer = inflater.inflate(R.layout.fragment_inventory_list, container, false)
        setHasOptionsMenu(true)

        mRecyclerView = rootContainer.findViewById(R.id.activity_inventory_list_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(context)
        mRecyclerView!!.layoutManager = mRecyclerLayoutManager

        fab = rootContainer.findViewById(R.id.activity_inventory_list_fab)

        sortSpinner = rootContainer.findViewById(R.id.fragment_inventory_list_sort_by_spinner)

        //set up Presenter
        mPresenter = InventoryListPresenter(context!!,
                UMAndroidUtil.bundleToMap(arguments), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))


        //Sort handler
        sortSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                mPresenter!!.handleSortChanged(id)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        fab!!.setOnClickListener({ v -> mPresenter!!.handleClickAddItems() })
        fab!!.visibility = View.GONE


        return rootContainer
    }

    override fun updateSortSpinner(presets: Array<String?>) {
        this.sortSpinnerPresets = presets
        val adapter = ArrayAdapter(context!!,
                R.layout.item_simple_spinner_gray, sortSpinnerPresets)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner!!.adapter = adapter
    }

    fun searchInventory(searchValue: String) {
        if(mPresenter != null) {
            mPresenter!!.setQuerySearch(searchValue)
            mPresenter!!.updateProviders()
        }
    }

    override fun onSearchButtonClick() {}

    override fun onSearchQueryUpdated(query: String) {
        searchInventory(query)
    }

    /**
     * Tints the drawable to the color. This method supports the Context compat tinting on drawables.
     *
     * @param drawable  The drawable to be tinted
     * @param color     The color of the tint
     */
    fun getTintedDrawable(drawable: Drawable, color: Int):Drawable {
        var drawable = drawable
        drawable = DrawableCompat.wrap(drawable)
        val tintColor = ContextCompat.getColor(context!!, color)
        DrawableCompat.setTint(drawable, tintColor)

        return drawable
    }

    override fun finish() {

    }

    override fun setListProvider(factory: DataSource.Factory<Int, SaleProductWithInventoryCount>) {

        val recyclerAdapter = InventoryListRecyclerAdapter(
                DIFF_SALEPRODUCTWITHINVENTORYCOUNT_CALLBACK, mPresenter!!,
                activity!!, context!!)

        val data = factory.asRepositoryLiveData(UmAccountManager.getRepositoryForActiveAccount(context!!).inventoryItemDao)

        data.observe(this,
                Observer<PagedList<SaleProductWithInventoryCount>> { recyclerAdapter.submitList(it) })

        mRecyclerView!!.adapter = recyclerAdapter
    }

    constructor()  {
        val args = Bundle()
        arguments = args
        icon = R.drawable.ic_assignment_24px
        title = R.string.inventory
    }

    constructor(args:Bundle) : this() {
        arguments = args
    }

    companion object {
        val icon = R.drawable.ic_assignment_24px

        fun newInstance(): InventoryListFragment {
            val fragment = InventoryListFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(args:Bundle): InventoryListFragment {
            val fragment = InventoryListFragment()
            fragment.arguments = args
            return fragment
        }

        /**
         * The DIFF CALLBACK
         */
        val DIFF_SALEPRODUCTWITHINVENTORYCOUNT_CALLBACK
                : DiffUtil.ItemCallback<SaleProductWithInventoryCount> =
                object : DiffUtil.ItemCallback<SaleProductWithInventoryCount>() {
            override fun areItemsTheSame(oldItem: SaleProductWithInventoryCount,
                                         newItem: SaleProductWithInventoryCount): Boolean {
                return oldItem.saleProductUid == newItem.saleProductUid
            }

            override fun areContentsTheSame(oldItem: SaleProductWithInventoryCount,
                                            newItem: SaleProductWithInventoryCount): Boolean {
                return oldItem == newItem
            }
        }
    }


}
