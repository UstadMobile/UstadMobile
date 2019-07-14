package com.ustadmobile.port.android.view


import android.graphics.drawable.Drawable
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
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
import com.ustadmobile.core.view.SaleListView
import com.ustadmobile.lib.db.entities.SaleListDetail


import java.util.Objects


import ru.dimorinny.floatingtextbutton.FloatingTextButton

class SaleListFragment : UstadBaseFragment(), SaleListView {
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

        fab = rootContainer.findViewById<View>(R.id.activity_sale_list_fab)

        sortSpinner = rootContainer.findViewById(R.id.fragment_sale_list_sort_by_spinner)


        allSalesCounter = rootContainer.findViewById(R.id.fragment_sale_list_filter_all_sales_counter)
        preOrderCounter = rootContainer.findViewById(R.id.fragment_sale_list_filter_pre_orders_counter)
        paymentsDueCounter = rootContainer.findViewById(R.id.fragment_sale_list_filter_payments_due_counter)

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
            getTintedDrawable(allSalesButton!!.background, R.color.fab)

        }
        preOrdersButton!!.setOnClickListener { v ->
            disableAllButtonSelected()
            mPresenter!!.filterPreOrder()
            getTintedDrawable(preOrdersButton!!.background, R.color.fab)
        }
        paymentsDueButton!!.setOnClickListener { v ->
            disableAllButtonSelected()
            mPresenter!!.filterPaymentDue()
            getTintedDrawable(paymentsDueButton!!.background, R.color.fab)
        }

        //Sort handler
        sortSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                mPresenter!!.handleChangeSortOrder(id)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        fab!!.setOnClickListener({ v -> mPresenter!!.handleClickPrimaryActionButton() })

        allSalesButton!!.callOnClick()

        return rootContainer
    }

    override fun updateSortSpinner(presets: Array<String?>) {
        this.sortSpinnerPresets = presets
        val adapter = ArrayAdapter(Objects.requireNonNull(context),
                R.layout.item_simple_spinner, sortSpinnerPresets)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner!!.adapter = adapter
    }

    override fun updatePreOrderCounter(count: Int) {
        if (count == 0) {
            preOrderCounter!!.visibility = View.INVISIBLE
        } else {
            preOrderCounter!!.text = count.toString()
            preOrderCounter!!.visibility = View.VISIBLE
        }
    }

    override fun updatePaymentDueCounter(count: Int) {
        if (count == 0) {
            paymentsDueCounter!!.visibility = View.INVISIBLE
        } else {
            paymentsDueCounter!!.text = count.toString()
            paymentsDueCounter!!.visibility = View.VISIBLE
        }
    }

    fun disableAllButtonSelected() {
        runOnUiThread ( Runnable {

            getTintedDrawable(allSalesButton!!.background, R.color.color_gray)
            getTintedDrawable(preOrdersButton!!.background, R.color.color_gray)
            getTintedDrawable(paymentsDueButton!!.background, R.color.color_gray)
            }
        )
    }

    /**
     * Tints the drawable to the color. This method supports the Context compat tinting on drawables.
     *
     * @param drawable  The drawable to be tinted
     * @param color     The color of the tint
     */
    fun getTintedDrawable(drawable: Drawable, color: Int) {
        var drawable = drawable
        drawable = DrawableCompat.wrap(drawable)
        val tintColor = ContextCompat.getColor(Objects.requireNonNull(context), color)
        DrawableCompat.setTint(drawable, tintColor)
    }

    override fun finish() {

    }

    override fun setListProvider(factory: DataSource.Factory<Int, SaleListDetail>,
                                 paymentsDueTab: Boolean,
                                 preOrderTab: Boolean) {

        val recyclerAdapter = SaleListRecyclerAdapter(DIFF_CALLBACK, mPresenter!!,
                paymentsDueTab, preOrderTab,this, context!!)

        val data = LivePagedListBuilder(factory, 20).build()
        data.observe(this, Observer<PagedList<SaleListDetail>> { recyclerAdapter.submitList(it) })

        mRecyclerView!!.adapter = recyclerAdapter
    }

    companion object {

        fun newInstance(): SaleListFragment {
            val fragment = SaleListFragment()
            val args = Bundle()
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
                return oldItem == newItem
            }
        }
    }


}
