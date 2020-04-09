package com.ustadmobile.port.android.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.toughra.ustadmobile.R
import com.ustadmobile.core.util.MessageIdOption

class MessageIdAutocompleteTextView: androidx.appcompat.widget.AppCompatAutoCompleteTextView, AdapterView.OnItemClickListener{

    interface OnMessageIdOptionSelectedListener {

        fun onMessageIdOptionSelected(view: AdapterView<*>?, messageIdOption: MessageIdOption)

        fun onNoMessageIdOptionSelected(view: AdapterView<*>?)

    }

    private var realItemSelectedListener: AdapterView.OnItemClickListener? = null

    var messageIdOptionSelectedListener: OnMessageIdOptionSelectedListener? = null

    private var mMessageIdArrayAdapter: ArrayAdapter<MessageIdOption>? = null

    private var selectedItem: MessageIdOption? = null

    var messageIdOptions = listOf<MessageIdOption>()
        set(value) {
            mMessageIdArrayAdapter = ArrayAdapter(context, R.layout.autocomplete_list_item,
                    value.toTypedArray()).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            field = value
            setAdapter(mMessageIdArrayAdapter)
        }

    var selectedMessageIdOption: Int
        get() = selectedItem?.code ?: -1

        set(value) {
            val itemIndex = messageIdOptions.indexOfFirst { it.code == value }
            if(itemIndex == -1)
                return

            selectedItem = messageIdOptions[itemIndex]
            setText(adapter?.getItem(itemIndex)?.toString() ?: "", false)
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
        super.setOnItemClickListener(this)
        inputType = 0
    }

    override fun setOnItemClickListener(listener: AdapterView.OnItemClickListener?) {
        realItemSelectedListener = listener
    }



    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val messageIdOptionSelected = mMessageIdArrayAdapter?.getItem(position)
        if(messageIdOptionSelected != null) {
            selectedItem = messageIdOptionSelected
            messageIdOptionSelectedListener?.onMessageIdOptionSelected(parent, messageIdOptionSelected)
        }
        realItemSelectedListener?.onItemClick(parent, view, position, id)
    }

}