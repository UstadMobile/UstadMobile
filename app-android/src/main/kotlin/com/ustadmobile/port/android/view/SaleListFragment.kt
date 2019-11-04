package com.ustadmobile.port.android.view


import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SaleListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.PersonWithSaleInfoDetailView
import com.ustadmobile.core.view.SaleListView
import com.ustadmobile.lib.db.entities.SaleListDetail
import ru.dimorinny.floatingtextbutton.FloatingTextButton

class SaleListFragment : UstadBaseFragment, SaleListView {
    override val viewContext: Any
        get() = context!!


    private lateinit var rootContainer: View
    private var mRecyclerView: RecyclerView? = null
    private var mPresenter: SaleListPresenter? = null
    private var fab: FloatingTextButton? = null

    private var sortSpinner: Spinner? = null
    internal lateinit var sortSpinnerPresets: Array<String?>

    private var allSalesButton: Button? = null
    private var preOrdersButton: Button? = null
    private var paymentsDueButton: Button? = null

    private var allSalesCounter: TextView? = null
    private var preOrderCounter: TextView? = null
    private var paymentsDueCounter: TextView? = null

    private var personUid : Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun goToSearch() {
        mPresenter!!.handleClickSearch()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        rootContainer = inflater.inflate(R.layout.fragment_sale_list, container, false)
        setHasOptionsMenu(true)

        mRecyclerView = rootContainer.findViewById(R.id.activity_sale_list_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(context)
        mRecyclerView!!.layoutManager = mRecyclerLayoutManager

        fab = rootContainer.findViewById(R.id.activity_sale_list_fab)

        sortSpinner = rootContainer.findViewById(R.id.fragment_sale_list_sort_by_spinner)

        allSalesCounter = rootContainer.findViewById(R.id.fragment_sale_list_filter_all_sales_counter)
        preOrderCounter = rootContainer.findViewById(R.id.fragment_sale_list_filter_pre_orders_counter)
        paymentsDueCounter = rootContainer.findViewById(R.id.fragment_sale_list_filter_payments_due_counter)

        if(arguments!!.containsKey(PersonWithSaleInfoDetailView.ARG_WE_UID)){
            personUid = arguments!!.get(PersonWithSaleInfoDetailView.ARG_WE_UID).toString().toLong()
        }

        //set up Presenter
        mPresenter = SaleListPresenter(context!!,
                UMAndroidUtil.bundleToMap(arguments), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //Button
        allSalesButton = rootContainer.findViewById(R.id.fragment_sale_list_filter_all_sales)
        preOrdersButton = rootContainer.findViewById(R.id.fragment_sale_list_filter_pre_orders)
        paymentsDueButton = rootContainer.findViewById(R.id.fragment_sale_list_filter_payments_due)

        paymentsDueButton!!.visibility = View.VISIBLE

        allSalesButton!!.setOnClickListener { v ->
            disableAllButtonSelected()
            mPresenter!!.filterAll()
            allSalesButton!!.background = getTintedDrawable(allSalesButton!!.background, R.color.fab)
            //getTintedDrawable(allSalesButton!!.background, R.color.fab)

        }
        preOrdersButton!!.setOnClickListener { v ->
            disableAllButtonSelected()
            mPresenter!!.filterPreOrder()
            preOrdersButton!!.background = getTintedDrawable(preOrdersButton!!.background, R.color.fab)
        }
        paymentsDueButton!!.setOnClickListener { v ->
            disableAllButtonSelected()
            mPresenter!!.filterPaymentDue()
            paymentsDueButton!!.background = getTintedDrawable(paymentsDueButton!!.background, R.color.fab)
        }

        //Sort handler
        sortSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                mPresenter!!.handleChangeSortOrder(id)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        fab!!.setOnClickListener({ v -> mPresenter!!.handleClickPrimaryActionButton() })

        if(personUid != 0L){
            allSalesButton!!.visibility = View.GONE
            allSalesCounter!!.visibility = View.GONE
            preOrderCounter!!.visibility = View.GONE
            preOrdersButton!!.visibility = View.GONE
            paymentsDueButton!!.visibility = View.GONE
            paymentsDueCounter!!.visibility = View.GONE
            fab!!.visibility = View.GONE

        }else{
            allSalesButton!!.callOnClick()
        }

        return rootContainer
    }

    override fun updateSortSpinner(presets: Array<String?>) {
        this.sortSpinnerPresets = presets
        val adapter = ArrayAdapter(context!!,
                R.layout.item_simple_spinner_gray, sortSpinnerPresets)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner!!.adapter = adapter
    }

    override fun updatePreOrderCounter(count: Int) {
        if(personUid == 0L) {
            if (count == 0) {
                preOrderCounter!!.visibility = View.INVISIBLE
            } else {
                preOrderCounter!!.text = count.toString()
                preOrderCounter!!.visibility = View.VISIBLE
            }
        }else{
            preOrderCounter!!.visibility = View.GONE
        }
    }

    override fun updatePaymentDueCounter(count: Int) {
        if(personUid == 0L) {
            if (count == 0) {
                paymentsDueCounter!!.visibility = View.INVISIBLE
            } else {
                paymentsDueCounter!!.text = count.toString()
                paymentsDueCounter!!.visibility = View.VISIBLE
            }
        }else{
            paymentsDueCounter!!.visibility = View.GONE
        }
    }

    fun disableAllButtonSelected() {
        allSalesButton!!.invalidateDrawable(allSalesButton!!.getBackground())
        preOrdersButton!!.invalidateDrawable(preOrdersButton!!.getBackground())
        paymentsDueButton!!.invalidateDrawable(paymentsDueButton!!.getBackground())

        allSalesButton!!.setBackground(
                getTintedDrawable(allSalesButton!!.getBackground(), R.color.color_gray))

        preOrdersButton!!.setBackground(
                getTintedDrawable(preOrdersButton!!.getBackground(), R.color.color_gray))

        paymentsDueButton!!.setBackground(
                getTintedDrawable(paymentsDueButton!!.getBackground(), R.color.color_gray))
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

    override fun setListProvider(factory: DataSource.Factory<Int, SaleListDetail>,
                                 paymentsDueTab: Boolean,
                                 preOrderTab: Boolean) {

        val boundaryCallback = UmAccountManager.getRepositoryForActiveAccount(viewContext)
                .saleDaoBoundaryCallbacks.findAllActiveAsSaleListDetailProvider(factory)

        val recyclerAdapter = SaleListRecyclerAdapter(DIFF_CALLBACK, mPresenter!!,
                paymentsDueTab, preOrderTab,this, context!!)

        val data =
                LivePagedListBuilder(factory, 20)
                        .setBoundaryCallback(boundaryCallback)
                        .build()
        data.observe(this,
                Observer<PagedList<SaleListDetail>> { recyclerAdapter.submitList(it) })

        mRecyclerView!!.adapter = recyclerAdapter
    }

    constructor()  {
        val args = Bundle()
        arguments = args
        icon = R.drawable.ic_payment_note_cash_black_24dp
        title = R.string.sales
    }

    constructor(args:Bundle) : this() {
        arguments = args
    }

    companion object {

        fun newInstance(): SaleListFragment {
            val fragment = SaleListFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(args:Bundle): SaleListFragment {
            val fragment = SaleListFragment()
            fragment.arguments = args
            return fragment
        }

        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<SaleListDetail> = object : DiffUtil.ItemCallback<SaleListDetail>() {
            override fun areItemsTheSame(oldItem: SaleListDetail,
                                         newItem: SaleListDetail): Boolean {
                return oldItem.saleUid == newItem.saleUid
            }

            override fun areContentsTheSame(oldItem: SaleListDetail,
                                            newItem: SaleListDetail): Boolean {
                return oldItem.saleUid == newItem.saleUid
            }
        }
    }


}
