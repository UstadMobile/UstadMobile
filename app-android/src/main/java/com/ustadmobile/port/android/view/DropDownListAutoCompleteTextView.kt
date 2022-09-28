package com.ustadmobile.port.android.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.toughra.ustadmobile.R

open class DropDownListAutoCompleteTextView<T: Any>: androidx.appcompat.widget.AppCompatAutoCompleteTextView, AdapterView.OnItemClickListener{

    interface OnDropDownListItemSelectedListener<T> {

        fun onDropDownItemSelected(view: AdapterView<*>?, selectedOption: T)

        fun onNoMessageIdOptionSelected(view: AdapterView<*>?)

    }

    interface DropDownListAutoCompleteAdapter<T: Any> {

        fun getId(item: T): Long

        fun getText(item: T): String

    }

    var dropDownListAdapter: DropDownListAutoCompleteAdapter<T>? = null


    class ListBaseAdapter<T: Any>(var context: Context, itemList: List<T>,
                                  var layoutResId: Int,
                                  var itemAdapter: DropDownListAutoCompleteAdapter<T>?): BaseAdapter(), Filterable {

        private val originalList: List<T> = itemList

        var mCurrentList: List<T> = itemList

        private var filteredData: List<T>? = null

        val baseFilter = object: Filter() {
            override fun performFiltering(prefix: CharSequence?): FilterResults {
                val results = FilterResults()

                if(prefix == null || prefix.length == 0) {
                    results.count = originalList.size
                    results.values = originalList
                }else {
                    val prefixLowerCase = prefix.toString().toLowerCase()
                    val listResults: List<T> = originalList.filter {
                        val itemText: String = itemAdapter?.getText(it) ?: ""
                        val match: Boolean = itemText.toLowerCase().startsWith(prefixLowerCase)
                        match
                    }

                    results.count = listResults.size
                    results.values = listResults
                }

                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if(results == null)
                    return

                mCurrentList = (results.values as List<T>)
                if(results.count > 0) {
                    notifyDataSetChanged()
                }else {
                    notifyDataSetInvalidated()
                }
            }
        }

        var dropDownLayoutResourceId: Int = 0

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val viewToUse = convertView ?: LayoutInflater.from(context).inflate(layoutResId, parent, false)
            val textView = viewToUse.findViewById<TextView>(R.id.line_item)
            textView.text = itemAdapter?.getText(mCurrentList[position])
            return viewToUse
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
            return if(dropDownLayoutResourceId != 0) {
                val viewToUse = convertView ?: LayoutInflater.from(context).inflate(dropDownLayoutResourceId, parent, false)
                (viewToUse as? TextView)?.text = itemAdapter?.getText(mCurrentList[position])
                viewToUse
            }else {
                super.getDropDownView(position, convertView, parent)
            }
        }

        override fun getItem(position: Int): Any {
            return mCurrentList[position]
        }

        fun getItemTyped(position: Int) : T{
            return mCurrentList[position]
        }

        override fun getItemId(position: Int): Long {
            return itemAdapter?.getId(mCurrentList[position]) ?: 0L
        }

        override fun getCount(): Int {
            return mCurrentList.size
        }

        override fun getFilter(): Filter = baseFilter
    }


    private var realItemSelectedListener: AdapterView.OnItemClickListener? = null

    var onDropDownListItemSelectedListener: OnDropDownListItemSelectedListener<T>? = null

    private var mListBaseAdapter: ListBaseAdapter<T>? = null

    private var selectedItem: T? = null

    var dropDownOptions = listOf<T>()
        set(value) {
            mListBaseAdapter = ListBaseAdapter(context, value, R.layout.multiline_list_item,
                    dropDownListAdapter).also {
                it.dropDownLayoutResourceId = R.layout.multiline_list_item
            }
            field = value
            setAdapter(mListBaseAdapter)

        }

    var selectedDropDownOptionId: Long
        get() {
            selectedItem.also {
                if(it != null) {
                    return dropDownListAdapter?.getId(it) ?: 0
                }else {
                    return -1L
                }
            }
        }

        set(value) {
            val itemIndex = dropDownOptions.indexOfFirst { dropDownListAdapter?.getId(it) == value }
            if(itemIndex == -1){
                // setting the text to empty to show no option selected
                setText("")
                return
            }

            selectedItem = dropDownOptions[itemIndex]
            setText(dropDownListAdapter?.getText(dropDownOptions[itemIndex]), false)
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
        val selectedOption = mListBaseAdapter?.getItemTyped(position)
        if(selectedOption != null) {
            selectedItem = selectedOption
            setText(dropDownListAdapter?.getText(selectedOption), false)
            onDropDownListItemSelectedListener?.onDropDownItemSelected(parent, selectedOption)
        }

        realItemSelectedListener?.onItemClick(parent, view, position, id)
    }

}