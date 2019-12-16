package com.ustadmobile.port.android.view

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SelectTwoDatesDialogPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.DismissableDialog
import com.ustadmobile.core.view.SelectSaleTypeDialogView
import io.reactivex.annotations.NonNull
import java.util.*

/**
 * SelectSaleTypeDialogFragment Android fragment extends UstadBaseFragment
 */
class SelectSaleTypeDialogFragment : UstadDialogFragment(), SelectSaleTypeDialogView,
         DismissableDialog {


    override val viewContext: Any
        get() = context!!

    internal lateinit var rootView: View
    internal lateinit var dialog: AlertDialog

    internal lateinit var soldNow : TextView
    internal lateinit var preOrder : TextView

    //Context (Activity calling this)
    private var mAttachedContext: Context? = null

    interface SaleTypeDialogListener {
        fun onSaleTypeSelected(sale: Boolean)
    }

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {


        val currentLocale = resources.configuration.locale

        val inflater = context!!.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        rootView = inflater.inflate(R.layout.fragment_select_sale_type, null)

        soldNow = rootView.findViewById(R.id.fragment_select_sale_type_sole_now)
        preOrder = rootView.findViewById(R.id.fragment_select_sale_type_pre_order)

        soldNow.setOnClickListener {
            if (mAttachedContext is SaleTypeDialogListener) {
                (mAttachedContext as SaleTypeDialogListener).onSaleTypeSelected(true)
                finish()
            }
        }

        preOrder.setOnClickListener {
            if (mAttachedContext is SaleTypeDialogListener) {
                (mAttachedContext as SaleTypeDialogListener).onSaleTypeSelected(false)
                finish()
            }
        }

        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(R.string.select_sale_type)
        builder.setView(rootView)

        dialog = builder.create()

        return dialog

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.mAttachedContext = context
    }

    override fun onDetach() {
        super.onDetach()
        this.mAttachedContext = null

    }

    override fun finish() {
        dialog.dismiss()
    }


    companion object {

        /**
         * Generates a new Fragment for a page fragment
         *
         * @return A new instance of fragment SelectTwoDatesDialogFragment.
         */
        fun newInstance(): SelectSaleTypeDialogFragment {
            val fragment = SelectSaleTypeDialogFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
