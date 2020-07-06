package com.ustadmobile.port.android.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner
import com.toughra.ustadmobile.R
import com.ustadmobile.core.util.MessageIdOption

/**
 * This is a convenience wrapper for a Spinner that contains a fixed list of choices (e.g.
 * field dropdowns, sort options, etc). Each choice has a Message ID that is used to determine
 * the string to show to the user, and a code that is the value to be stored.
 */
@Deprecated("Use MessageIdAutoCompleteTextView instead to get the correct Material Design styling")
class MessageIdSpinner: AppCompatSpinner, AdapterView.OnItemSelectedListener{

    interface OnMessageIdOptionSelectedListener {

        fun onMessageIdOptionSelected(view: AdapterView<*>?, messageIdOption: MessageIdOption)

        fun onNoMessageIdOptionSelected(view: AdapterView<*>?)

    }

    private var realItemSelectedListener: OnItemSelectedListener? = null

    var messageIdOptionSelectedListener: OnMessageIdOptionSelectedListener? = null

    private var mMessageIdArrayAdapter: ArrayAdapter<MessageIdOption>? = null

    var messageIdOptions = listOf<MessageIdOption>()
        set(value) {
            mMessageIdArrayAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item,
                    value.toTypedArray()).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            field = value
            adapter = mMessageIdArrayAdapter
        }

    var selectedMessageIdOption: Int
        get() = (selectedItem as? MessageIdOption)?.code ?: -1

        set(value) {
            val itemIndex = messageIdOptions.indexOfFirst { it.code == value }
            takeIf{ itemIndex != -1}?.setSelection(itemIndex)
        }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        super.setOnItemSelectedListener(this)
    }

    override fun setOnItemSelectedListener(listener: OnItemSelectedListener?) {
        realItemSelectedListener = listener
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        realItemSelectedListener?.onNothingSelected(parent)
        messageIdOptionSelectedListener?.onNoMessageIdOptionSelected(parent)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        realItemSelectedListener?.onItemSelected(parent, view, position, id)
        val messageIdOptionSelected = mMessageIdArrayAdapter?.getItem(position)
        if(messageIdOptionSelected != null) {
            messageIdOptionSelectedListener?.onMessageIdOptionSelected(parent, messageIdOptionSelected)
        }
    }

}