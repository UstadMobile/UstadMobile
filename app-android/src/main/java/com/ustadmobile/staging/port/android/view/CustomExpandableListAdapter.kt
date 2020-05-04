package com.ustadmobile.staging.port.android.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import com.toughra.ustadmobile.R
import java.util.*

class CustomExpandableListAdapter(private val context: Context,
                                  internal var detailAndMore: HashMap<String, ExpandableListDataReports>,
                                  private val expandableListTitle: List<String>
) : BaseExpandableListAdapter() {

    override fun getChild(listPosition: Int, expandedListPosition: Int): Any {

        return this.detailAndMore[this.expandableListTitle[listPosition]]!!
                .children[expandedListPosition]
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    override fun getChildView(listPosition: Int, expandedListPosition: Int,
                              isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val expandedListText = (getChild(listPosition, expandedListPosition) as ExpandableListDataReports).name
        if (convertView == null) {
            val layoutInflater = this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.expandable_list_item, null)
        }
        val expandedListTextView = convertView!!
                .findViewById<TextView>(R.id.expandable_list_item_text)
        expandedListTextView.text = expandedListText
        return convertView
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return this.detailAndMore[this.expandableListTitle[listPosition]]!!
                .children.size
    }

    override fun getGroup(listPosition: Int): Any {
        return this.expandableListTitle[listPosition]
    }

    override fun getGroupCount(): Int {
        return this.expandableListTitle.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getGroupView(listPosition: Int, isExpanded: Boolean,
                              convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        val listTitle = getGroup(listPosition) as String
        if (convertView == null) {
            val layoutInflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.expandable_list_item_with_image, null)
        }
        val listTitleTextView = convertView!!
                .findViewById<TextView>(R.id.expandable_list_item_with_image_title)
        listTitleTextView.typeface = null
        listTitleTextView.text = listTitle

        //Change main image?
        val listImage = this.detailAndMore[listTitle]!!.icon
        val titleImage = convertView.findViewById<ImageView>(R.id.expandable_list_item_with_image_image)
        titleImage.setImageResource(listImage!!)

        val groupHolder = convertView.findViewById<ImageView>(R.id.expandable_list_item_with_image_arrow)
        if (isExpanded) {
            groupHolder.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
        } else {
            groupHolder.setImageResource(R.drawable.ic_keyboard_arrow_right_black_24dp)
        }
        return convertView
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }
}
