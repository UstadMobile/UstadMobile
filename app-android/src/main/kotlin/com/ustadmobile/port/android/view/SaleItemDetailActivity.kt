package com.ustadmobile.port.android.view

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SaleItemDetailPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.SaleItemDetailView
import com.ustadmobile.lib.db.entities.SaleItem
import com.ustadmobile.lib.db.entities.SaleItemReminder
import com.ustadmobile.port.android.impl.ReminderReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class SaleItemDetailActivity : UstadBaseActivity(), SaleItemDetailView {

    private lateinit var toolbar: Toolbar
    private lateinit var mPresenter: SaleItemDetailPresenter
    private lateinit var menu: Menu

    private lateinit var totalTV: TextView
    private lateinit var quantityNP: NumberPicker
    private lateinit var quantityTV: TextView
    private lateinit var pppNP: NumberPicker
    private lateinit var pppNPET: EditText
    private lateinit var quantityNPET: EditText
    private var quantityDefaultValue = 1
    private var pppDefaultValue = 0
    internal var minValue = 0
    internal var maxValue = 99990

    private lateinit var preOrderHline: View
    private lateinit var orderDueDateTV: TextView
    private lateinit var orderDueDateET: EditText

    private lateinit var reminderHline: View
    private lateinit var addReminderTV: TextView
    private lateinit var remindersRV: RecyclerView

    override fun updateItemPrice(itemPrice: Float) {
        pppNP.value = itemPrice.toInt()
    }

    override fun showQuantityTextView(show: Boolean) {

        runOnUiThread(Runnable {
            if (show) {
                quantityNP.visibility = View.GONE
                quantityTV.visibility = View.VISIBLE
            } else {
                quantityNP.visibility = View.VISIBLE
                quantityTV.visibility = View.GONE
            }
        })
    }
    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_save, menu)

        menu.findItem(R.id.menu_save).isVisible = true
        return true
    }

    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val i = item.itemId
        if (i == android.R.id.home) {
            onBackPressed()
            return true

        } else if (i == R.id.menu_save) {
            checkAndSave()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkAndSave(){

        mPresenter.handleClickSave()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout
        setContentView(R.layout.activity_sale_item_detail)

        //Toolbar
        toolbar = findViewById(R.id.activity_sale_item_detail_toolbar)
        toolbar.title = getText(R.string.sale_detail)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        totalTV = findViewById(R.id.activity_sale_item_detail_total_amount)
        quantityNP = findViewById(R.id.activity_sale_item_detail_quantity_numberpicker)
        quantityTV = findViewById(R.id.activity_sale_item_detail_quantity_textview)
        quantityNPET = quantityNP.findViewById(Resources.getSystem().getIdentifier(
                "numberpicker_input", "id", "android"))
        quantityNPET.isFocusable = false

        pppNP = findViewById(R.id.activity_sale_payment_detail_amount_np)
        pppNPET = pppNP.findViewById(Resources.getSystem().getIdentifier("numberpicker_input",
                "id", "android"))
        pppNPET.isFocusable = true

        preOrderHline = findViewById(R.id.activity_sale_item_detail_preorder_hline)
        orderDueDateTV = findViewById(R.id.activity_sale_item_detail_preorder_due_date_tv)
        orderDueDateET = findViewById(R.id.activity_sale_item_detail_order_due_date_date_edittext)

        reminderHline = findViewById(R.id.activity_sale_item_detil_notification_hline)
        addReminderTV = findViewById(R.id.activity_sale_item_detail_add_reminder_tv)
        remindersRV = findViewById(R.id.activity_sale_item_detail_reminder_rv)
        val pRecyclerLayoutManager = LinearLayoutManager(this)
        remindersRV.layoutManager = pRecyclerLayoutManager

        //Date
        val myCalendar = Calendar.getInstance()

        //A Time picker listener that sets the from time.
        val dateListener = { view: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.YEAR, year)

            val dateString = UMCalendarUtil.getPrettyDateSuperSimpleFromLong(myCalendar.timeInMillis)
            mPresenter.handleChangeOrderDueDate(myCalendar.timeInMillis)
            orderDueDateET.setText(dateString)
        }

        //Default view: not focusable.
        orderDueDateET.isFocusable = false

        //From time on click -> opens a timer picker.
        orderDueDateET.setOnClickListener { v ->
            DatePickerDialog(this, dateListener,
                    myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        quantityNP.minValue = 1
        quantityNP.value = quantityDefaultValue
        quantityNP.maxValue = 99999

        pppNP.minValue = minValue
        pppNP.maxValue = maxValue
        pppNP.value = pppDefaultValue

        //Inventory total amount click listenere
        quantityTV.setOnClickListener{
            //TODO: GO to inventory selector
            //Should only need SaleItemUid and LeUid
        }

        //Presenter
        mPresenter = SaleItemDetailPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        quantityNP.setOnValueChangedListener { _, _, newVal ->
            val ppp = pppNP.value
            mPresenter.handleChangeQuantity(newVal)
            mPresenter.updateTotal(newVal, ppp.toLong())
        }

        pppNP.setOnValueChangedListener { _, _, newVal ->
            val q = quantityNP.value
            mPresenter.handleChangePPP(newVal.toLong())
            mPresenter.updateTotal(q, newVal.toLong())
        }

        pppNPET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                var newVal = 0
                val q = quantityNP.value
                if(s.toString().isNotEmpty()) {
                    newVal = Integer.valueOf(s.toString())
                }
            mPresenter.handleChangePPP(newVal.toLong())
                mPresenter.updateTotal(q, newVal.toLong())
            }
        })

        addReminderTV.setOnClickListener { mPresenter.handleClickAddReminder() }
    }

    private fun togglePreOrderView(isChecked : Boolean){
        mPresenter.setPreOrder(isChecked)
        showPreOrder(isChecked)
    }

    override fun updateSaleItemOnView(saleItem: SaleItem) {
        runOnUiThread {
            val q = saleItem.saleItemQuantity
            val ppp = saleItem.saleItemPricePerPiece
            val total = (q * ppp).toLong()

            if (q != 0) {
                quantityNP.value = q
                quantityTV.text = q.toString()
            }
            if (ppp > 0) {
                pppNP.value = ppp.toInt()
            }
            totalTV.text = total.toString()


            this.togglePreOrderView(saleItem.saleItemPreorder)


            val dueDate = saleItem.saleItemDueDate
            if (dueDate > 0) {
                orderDueDateET.setText(UMCalendarUtil.getPrettyDateSuperSimpleFromLong(
                        dueDate, null))
            }
        }
    }

    override fun updateProductTitleOnView(productTitle: String) {
        runOnUiThread {
            if (productTitle !== "") {
                toolbar.title = productTitle
            }
        }
    }

    override fun setReminderProvider(paymentProvider: DataSource.Factory<Int, SaleItemReminder>) {
        val recyclerAdapter =
                SaleItemReminderRecyclerAdapter(DIFF_CALLBACK_REMINDER, mPresenter,
                        this, applicationContext)

        // get the provider, set , observe, etc.
        val data =
                LivePagedListBuilder(paymentProvider, 20).build()

        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<SaleItemReminder>> { recyclerAdapter.submitList(it) })
        }

        //set the adapter
         remindersRV.adapter = recyclerAdapter

    }

    override fun updateTotal(total: Long) {
        totalTV.text = total.toString()
    }

    override fun updatePPP(ppp: Long) {
        pppNP.value = ppp.toInt()
    }

    override fun showPreOrder(show: Boolean) {
        preOrderHline.visibility = if (show) View.VISIBLE else View.INVISIBLE
        orderDueDateTV.visibility = if (show) View.VISIBLE else View.INVISIBLE
        orderDueDateET.visibility = if (show) View.VISIBLE else View.INVISIBLE

        remindersRV.visibility = if (show) View.VISIBLE else View.INVISIBLE
        reminderHline.visibility = if (show) View.VISIBLE else View.INVISIBLE
        addReminderTV.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    override fun setReminderNotification(days: Int, message: String, saleDueDate: Long) {
        val intent = Intent(this, ReminderReceiver::class.java)
        intent.putExtra(SaleItemDetailView.ARG_SALE_ITEM_NAME, message)
        intent.putExtra(SaleItemDetailView.ARG_SALE_ITEM_DUE_DATE, saleDueDate)
        intent.putExtra(SaleItemDetailView.ARG_SALE_DUE_DAYS, days)
        val pendingIntent = PendingIntent.getBroadcast(this,
                REMINER_REQUEST_CODE, intent, 0)
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val reminderMilli = getNextMidnightReminder(days, saleDueDate)
        am.set(AlarmManager.RTC_WAKEUP, reminderMilli, pendingIntent)
    }

    companion object {

        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK_REMINDER: DiffUtil.ItemCallback<SaleItemReminder> =
                object : DiffUtil.ItemCallback<SaleItemReminder>() {
            override fun areItemsTheSame(oldItem: SaleItemReminder,
                                         newItem: SaleItemReminder): Boolean {
                return oldItem.saleItemReminderUid == newItem.saleItemReminderUid
            }

            override fun areContentsTheSame(oldItem: SaleItemReminder,
                                            newItem: SaleItemReminder): Boolean {
                return oldItem.saleItemReminderUid == newItem.saleItemReminderUid
            }
        }

        const val REMINER_REQUEST_CODE = 530

        fun getNextMidnightReminder(days: Int, saleDueDate: Long): Long {
            val cal = Calendar.getInstance()
            cal.timeInMillis = saleDueDate
            cal.add(Calendar.DATE, -days)
            return cal.timeInMillis
        }
    }
}
