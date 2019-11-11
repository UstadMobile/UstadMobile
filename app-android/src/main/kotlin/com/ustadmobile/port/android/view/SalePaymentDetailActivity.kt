package com.ustadmobile.port.android.view

import android.app.DatePickerDialog
import android.content.res.Resources
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.DatePicker
import android.widget.EditText
import android.widget.NumberPicker

import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView

import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SalePaymentDetailPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.SalePaymentDetailView
import com.ustadmobile.lib.db.entities.SalePayment

import java.util.Calendar
import java.util.Objects

class SalePaymentDetailActivity : UstadBaseActivity(), SalePaymentDetailView {

    private var toolbar: Toolbar? = null
    private var mPresenter: SalePaymentDetailPresenter? = null

    private var amountNP: NumberPicker? = null
    private var paymentDateET: EditText? = null
    private var amountNPET: EditText? = null

    private var menu: Menu? = null

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
            mPresenter!!.handleClickSave()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun updateMaxPaymentValue(value: Long){
        val intVal = value.toInt()
        if(intVal != null) {
            amountNP!!.maxValue = value.toInt()
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_sale_payment_detail)

        //Toolbar:
        toolbar = findViewById(R.id.activity_sale_payment_detail_toolbar)
        toolbar!!.title = getText(R.string.add_payment)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        amountNP = findViewById(R.id.activity_sale_payment_detail_amount_np)
        amountNP!!.minValue = 1
        amountNP!!.maxValue = 9999999
        amountNP!!.value = 1

        val np = amountNP
        np!!.maxValue = 0 // max value 1000
        np.minValue = 1   // min value 0
        np.value = 1
        np.wrapSelectorWheel = false
        val m_oldFocus = np.descendantFocusability
        np.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        np.setOnTouchListener { view, motionEvent ->
            np.descendantFocusability = m_oldFocus
            false
        }

        paymentDateET = findViewById(R.id.activity_sale_payment_detail_payment_date_et)

        val myCalendar = Calendar.getInstance()

        //A Time picker listener that sets the from time.
        val dateListener = { view: DatePicker, year:Int, month:Int, dayOfMonth:Int ->
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.YEAR, year)

            val dateString = UMCalendarUtil.getPrettyDateSuperSimpleFromLong(myCalendar.timeInMillis)
            mPresenter!!.handleDateUpdated(myCalendar.timeInMillis)
            paymentDateET!!.setText(dateString)
        }

        //Default view: not focusable.
        paymentDateET!!.isFocusable = false

        //From time on click -> opens a timer picker.
        paymentDateET!!.setOnClickListener { v ->
            DatePickerDialog(this, dateListener,
                    myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        //Call the Presenter
        mPresenter = SalePaymentDetailPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        amountNPET = amountNP!!.findViewById(Resources.getSystem()
                .getIdentifier("numberpicker_input", "id", "android"))

        amountNPET!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (s.toString() != "") {
                    val newVal = Integer.valueOf(s.toString())
                    mPresenter!!.handleAmountUpdated(newVal.toLong())
                }
            }
        })

    }

    override fun updateSalePaymentOnView(payment: SalePayment) {
        amountNP!!.value = payment.salePaymentPaidAmount.toInt()

        paymentDateET!!.setText(UMCalendarUtil.getPrettyDateSuperSimpleFromLong(
                payment.salePaymentPaidDate))
    }

    override fun updateDefaultValue(value: Long) {
        updateMaxPaymentValue(value)
        amountNP!!.value = value.toInt()
    }
}
