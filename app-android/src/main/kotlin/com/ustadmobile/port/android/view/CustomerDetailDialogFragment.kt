package com.ustadmobile.port.android.view

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.CustomerDetailPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.CustomerDetailView
import com.ustadmobile.core.view.DismissableDialog

class CustomerDetailDialogFragment : UstadDialogFragment(), CustomerDetailView,
        SelectPersonDialogFragment.PersonSelectedDialogListener, DismissableDialog {
    override val viewContext: Any
        get() = context!!


    override fun updateCustomerName(name: String) {
        runOnUiThread(Runnable { customerName!!.setText(name) })

    }

    override fun updateLocationName(name: String) {
        runOnUiThread(Runnable { locationName!!.setText(name) })
    }

    override fun updatePhoneNumber(name: String) {
        runOnUiThread(Runnable { phoneNumber!!.setText(name) })
    }

    lateinit var toolbar: Toolbar
    private var mPresenter: CustomerDetailPresenter? = null

    internal lateinit var dialog: AlertDialog
    internal lateinit var rootView: View

    private var customerUid : Long ? = 0L
    private var customerName: EditText? = null
    private var locationName: EditText? = null
    private var phoneNumber: EditText? = null

    private var mAttachedContext: Context? = null

    private var newCustomer : Boolean = false

    private var pabMenuItem : MenuItem ?= null

    private lateinit var locationSpinner: Spinner


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
                dialog.dismiss()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onSelectPersonListener(personUid: Long) {
        customerUid = personUid
        mPresenter!!.updateCustomerUid(customerUid!!)
    }


    fun getTintedDrawable(drawable: Drawable?, color: Int): Drawable {
        var drawable = drawable
        drawable = DrawableCompat.wrap(drawable!!)
        val tintColor = ContextCompat.getColor(context!!, color)
        DrawableCompat.setTint(drawable!!, tintColor)
        return drawable
    }

    override fun updatePAB(save: Boolean){
        if(save){
            pabMenuItem!!.setTitle(R.string.save)
        }else{
            pabMenuItem!!.setTitle(R.string.select)
        }
    }

    override fun setLocationPresets(locationPresets: Array<String>, selectedPosition: Int) {

        val adapter = ArrayAdapter(activity!!,
                R.layout.item_simple_spinner, locationPresets)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        locationSpinner!!.adapter = adapter
        locationSpinner!!.setSelection(selectedPosition)

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {


        val inflater = context!!.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        rootView = inflater.inflate(R.layout.activity_customer_detail, null)


        //Toolbar:
        toolbar = rootView.findViewById(R.id.activity_customer_detail_toolbar)
        toolbar.setTitle(R.string.customer_details)

        locationSpinner = rootView.findViewById(R.id.activity_customer_detail_location_spinner)

        var upIcon = AppCompatResources.getDrawable(context!!,
                R.drawable.ic_arrow_back_white_24dp)

        upIcon = getTintedDrawable(upIcon, R.color.icons)

        toolbar.navigationIcon = upIcon
        toolbar.setNavigationOnClickListener { v -> dialog.dismiss() }

        toolbar.inflateMenu(R.menu.menu_save)
        pabMenuItem = toolbar.menu.findItem(R.id.menu_save)
        pabMenuItem!!.setTitle(R.string.select)
        pabMenuItem!!.isVisible = true


        //Click the tick button on the toolbar:
        toolbar.setOnMenuItemClickListener { item ->
            val i = item.itemId
            if (i == R.id.menu_save) {
                mPresenter!!.doneSelecting(locationName!!.text.toString(), phoneNumber!!.text.toString(),
                        customerName!!.text.toString())
            }
            false
        }


        customerName = rootView.findViewById(R.id.activity_customer_detail_name)
        locationName = rootView.findViewById(R.id.activity_customer_detail_location)
        phoneNumber = rootView.findViewById(R.id.activity_customer_detail_phone)


        //Call the Presenter
        mPresenter = CustomerDetailPresenter(context!!,
                UMAndroidUtil.bundleToMap(arguments), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        locationName!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                updatePAB(true)
            }
        })

        phoneNumber!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                updatePAB(true)
            }
        })

        customerName!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                updatePAB(true)
            }
        })

        //Location spinner
        locationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                mPresenter!!.handleLocationSelected(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        if (arguments!!.containsKey(CustomerDetailView.ARG_CUSTOMER_UID)) {
            newCustomer = false
        }else{
            newCustomer = true
        }

        if(!newCustomer) {
            customerName!!.setOnTouchListener(View.OnTouchListener { view, motionEvent ->

                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    mPresenter!!.selectCustomer();
                }
                true

            })
        }

        //Dialog stuff:
        //Set any view components and its listener (post presenter work)
        dialog = AlertDialog.Builder(context!!,
                R.style.FullScreenDialogStyle)
                .setView(rootView)
                .setTitle("")
                .create()
        return dialog

    }

    //Main Activity should implement this ?
    interface ChoosenCustomerListener {
        fun onSelectCustomerListener(customerUid: Long, customerName: String)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.mAttachedContext = context
    }

    override fun onDetach() {
        super.onDetach()
        this.mAttachedContext = null
    }

    override fun updateAndDismiss(personUid: Long, customerName: String){
        if(personUid != 0L) {
            customerUid = personUid
            if (mAttachedContext is ChoosenCustomerListener && customerUid != 0L) {
                (mAttachedContext as ChoosenCustomerListener)
                        .onSelectCustomerListener(customerUid!!, customerName)
            }
        }
        finish()
    }

    override fun finish() {
        dialog.dismiss()
    }
}
