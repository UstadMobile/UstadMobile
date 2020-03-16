package com.ustadmobile.port.android.view.binding

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.databinding.BindingAdapter
import com.toughra.ustadmobile.R
import com.ustadmobile.core.util.SortOption

interface SortOptionSelectedListener {

    fun onSortOptionSelected(view: AdapterView<*>?, sortOption: SortOption)
    
    fun onNoSortItemSelected(view: AdapterView<*>?)

}

@BindingAdapter("sortOptions")
fun Spinner.setSortOptions(sortOptions: MutableList<SortOption>?) {
    val sortOptionsToUse = sortOptions ?: mutableListOf()
    adapter = ArrayAdapter<SortOption>(context, R.layout.item_simple_spinner_gray, sortOptionsToUse).also {
        it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

}

@BindingAdapter("onSortItemSelected")
fun Spinner.setOnSortItemSelected(itemSelectedListener: SortOptionSelectedListener?) {
    if(itemSelectedListener == null) {
        onItemSelectedListener = null
    }else {
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                itemSelectedListener.onNoSortItemSelected(parent)
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val sortOption = this@setOnSortItemSelected.adapter.getItem(position) as SortOption
                itemSelectedListener.onSortOptionSelected(parent, sortOption)
            }
        }
    }


}