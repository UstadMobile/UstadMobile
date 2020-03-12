package com.ustadmobile.port.android.view.binding

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.databinding.BindingAdapter
import com.toughra.ustadmobile.R
import com.ustadmobile.core.util.MessageIdOption

interface MessageIdOptionSelectedListener {

    fun onMessageIdOptionSelected(view: AdapterView<*>?, messageIdOption: MessageIdOption)
    
    fun onNoMessageIdOptionSelected(view: AdapterView<*>?)

}

@BindingAdapter("messageIdOptions")
fun Spinner.setSortOptions(messageIdOptions: MutableList<MessageIdOption>?) {
    val sortOptionsToUse = messageIdOptions ?: mutableListOf()
    adapter = ArrayAdapter<MessageIdOption>(context, R.layout.item_simple_spinner_gray, sortOptionsToUse).also {
        it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

}

@BindingAdapter("onMessageIdOptionSelected")
fun Spinner.setOnSortItemSelected(itemSelectedListener: MessageIdOptionSelectedListener?) {
    if(itemSelectedListener == null) {
        onItemSelectedListener = null
    }else {
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                itemSelectedListener.onNoMessageIdOptionSelected(parent)
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val sortOption = this@setOnSortItemSelected.adapter.getItem(position) as MessageIdOption
                itemSelectedListener.onMessageIdOptionSelected(parent, sortOption)
            }
        }
    }


}