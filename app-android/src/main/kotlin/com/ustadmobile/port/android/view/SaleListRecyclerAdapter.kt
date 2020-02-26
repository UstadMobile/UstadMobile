package com.ustadmobile.port.android.view

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.CommonHandlerPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.lib.db.entities.SaleListDetail

class SaleListRecyclerAdapter
    : PagedListAdapter<SaleListDetail, SaleListRecyclerAdapter.SaleListViewHolder> {

    internal var theContext: Context
    internal var theActivity: Activity? = null
    internal var theFragment: Fragment ?= null
    internal var mPresenter: CommonHandlerPresenter<*>
    internal var paymentsDueTab = false
    internal var preOrderTab = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleListViewHolder {


        val list = LayoutInflater.from(theContext).inflate(
                R.layout.item_sale, parent, false)
        return SaleListViewHolder(list)

    }

    override fun onBindViewHolder(holder: SaleListViewHolder, position: Int) {

        val entity = getItem(position)

        val saleTitle = holder.itemView.findViewById<TextView>(R.id.item_sale_title)
        val saleAmount = holder.itemView.findViewById<TextView>(R.id.item_sale_amount)
        val saleLocation = holder.itemView.findViewById<TextView>(R.id.item_sale_location)
        val saleOrderDate = holder.itemView.findViewById<TextView>(R.id.item_sale_order_date)
        val saleDueDate = holder.itemView.findViewById<TextView>(R.id.item_sale_order_due_date)
        val saleDueDateImage = holder.itemView.findViewById<ImageView>(R.id.item_sale_order_due_date_image)

        assert(entity != null)

        val impl = UstadMobileSystemImpl.instance
        val currentLocale = impl.getLocale(theContext)
        var saleTitleNameTranslated: String?=null


        if (entity!!.saleTitle != null && !entity.saleTitle!!.isEmpty()) {
            saleTitle.text = entity.saleTitle

            if(currentLocale.equals("fa")){
                saleTitle.text = entity!!.saleTitleGenDari
            }else if(currentLocale.equals("ps")){
                saleTitle.text = entity!!.saleTitleGenPashto
            }else{
                saleTitle.text = entity!!.saleTitleGen
            }


        } else if (entity.saleTitleGen != null && !entity.saleTitleGen!!.isEmpty()) {

            if(currentLocale.equals("fa")){
                saleTitle.text = entity!!.saleTitleGenDari
            }else if(currentLocale.equals("ps")){
                saleTitle.text = entity!!.saleTitleGenPashto
            }else{
                saleTitle.text = entity!!.saleTitleGen
            }

        } else {
            saleTitle.text = ""
        }

        val saleAmountWithCurrency = Math.round(entity.saleAmount).toString() + " " +
                impl.getString(MessageID.currency_afs, theContext)
        saleLocation.text = entity.locationName

        val creationDatePretty = UMCalendarUtil.getPrettyDateSuperSimpleFromLong(entity.saleCreationDate)
        saleOrderDate.text = creationDatePretty

        saleAmount.text = saleAmountWithCurrency
        saleAmount.setTextColor(ContextCompat.getColor(theContext, R.color.text_primary))

        val earliestDueDate = entity.earliestDueDate
        val dueDatePretty = UMCalendarUtil.getPrettyDateSuperSimpleFromLong(earliestDueDate)
        var dueString: String
        if (theActivity != null) {
            dueString = theActivity!!.getText(R.string.due).toString() + " " + dueDatePretty
        } else {
            dueString = theFragment!!.getText(R.string.due).toString() + " " + dueDatePretty
        }

        if (preOrderTab) {
            saleDueDate.visibility = View.VISIBLE
            saleDueDateImage.visibility = View.VISIBLE
            if (earliestDueDate != 0L && earliestDueDate <= System.currentTimeMillis()) {
                saleDueDate.text = dueString

            } else if (!entity.saleItemPreOrder) {

                if (theActivity != null) {
                    dueString = theActivity!!.getText(R.string.not_delivered).toString()
                } else {
                    dueString = theFragment!!.getText(R.string.not_delivered).toString()
                }

                saleDueDate.text = dueString
            } else if (earliestDueDate != 0L && earliestDueDate > System.currentTimeMillis()) {
                saleDueDate.text = dueString
                saleDueDate.setTextColor(ContextCompat.getColor(theContext, R.color.text_primary))
                saleDueDateImage.setColorFilter(ContextCompat.getColor(theContext,
                        R.color.text_primary))
            } else {
                saleDueDate.visibility = View.GONE
                saleDueDateImage.visibility = View.GONE
            }
        } else {
            saleDueDate.visibility = View.GONE
            saleDueDateImage.visibility = View.GONE
        }


        if (paymentsDueTab) {
            //Also change amount to remaining amount and change its color
            val saleAmountRemainingWithCurrency = Math.round(entity.saleAmountDue).toString() + " " +
                    impl.getString(MessageID.currency_afs, theContext)
            saleAmount.text = saleAmountRemainingWithCurrency
            saleAmount.setTextColor(ContextCompat.getColor(theContext, R.color.primary_dark))
        }


        val item = holder.itemView.findViewById<ConstraintLayout>(R.id.item_sale_cl)
        item.setOnClickListener { v ->
            val genTitle = entity.saleTitleGen
            val saleName: String?
            if (genTitle != null && !genTitle.isEmpty()) {
                saleName = genTitle
            } else {
                if(entity.saleTitle != null) {
                    saleName = entity.saleTitle
                }else{
                    saleName = ""
                }
            }
            mPresenter.handleCommonPressed(entity.saleUid, saleName!!)
        }


    }

    inner class SaleListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


    internal constructor(
            diffCallback: DiffUtil.ItemCallback<SaleListDetail>,
            thePresenter: CommonHandlerPresenter<*>,
            paymentsDue: Boolean,
            preOrder: Boolean,
            fragment: Fragment,
            context: Context) : super(diffCallback) {
        mPresenter = thePresenter
        theContext = context
        paymentsDueTab = paymentsDue
        this.theFragment = fragment
        preOrderTab = preOrder
        this.theFragment = fragment
    }

    internal constructor(
            diffCallback: DiffUtil.ItemCallback<SaleListDetail>,
            thePresenter: CommonHandlerPresenter<*>,
            paymentsDue: Boolean,
            preOrder: Boolean,
            activity: Activity,
            context: Context) : super(diffCallback) {
        mPresenter = thePresenter
        theContext = context
        paymentsDueTab = paymentsDue
        this.theActivity = activity
        preOrderTab = preOrder
    }


}
