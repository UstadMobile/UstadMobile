package com.ustadmobile.port.android.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.toughra.ustadmobile.R;

import java.util.HashMap;
import java.util.List;

public class CustomExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> expandableListTitle;
    HashMap<String, ExpandableListDataReports> detailAndMore;

    public CustomExpandableListAdapter(Context context,
                                       HashMap<String,ExpandableListDataReports> expandableListDataReportsHashMap,
                                       List<String> expandableListTitle
                                       ) {
        this.context = context;
        this.expandableListTitle = expandableListTitle;
        this.detailAndMore = expandableListDataReportsHashMap;

    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {

        return this.detailAndMore.get(this.expandableListTitle.get(listPosition))
                .children.get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String expandedListText =
        ((ExpandableListDataReports) getChild(listPosition, expandedListPosition)).name;
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.expandable_list_item, null);
        }
        TextView expandedListTextView = convertView
                .findViewById(R.id.expandable_list_item_text);
        expandedListTextView.setText(expandedListText);
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return this.detailAndMore.get(this.expandableListTitle.get(listPosition))
                .children.size();
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.expandableListTitle.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.expandableListTitle.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        String listTitle = (String) getGroup(listPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.expandable_list_item_with_image, null);
        }
        TextView listTitleTextView = convertView
                .findViewById(R.id.expandable_list_item_with_image_title);
        listTitleTextView.setTypeface(null);
        listTitleTextView.setText(listTitle);

        //Change main image?
        Integer listImage = this.detailAndMore.get(listTitle).icon;
        ImageView titleImage = convertView.findViewById(R.id.expandable_list_item_with_image_image);
        titleImage.setImageResource(listImage);

        ImageView groupHolder = convertView.findViewById(R.id.expandable_list_item_with_image_arrow);
        if (isExpanded) {
            groupHolder.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
        } else {
            groupHolder.setImageResource(R.drawable.ic_keyboard_arrow_right_black_24dp);
        }
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }
}
