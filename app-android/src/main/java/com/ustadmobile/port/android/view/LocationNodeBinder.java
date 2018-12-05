package com.ustadmobile.port.android.view;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.toughra.ustadmobile.R;

import tellh.com.recyclertreeview_lib.TreeNode;
import tellh.com.recyclertreeview_lib.TreeViewBinder;

public class LocationNodeBinder extends TreeViewBinder<LocationNodeBinder.ViewHolder> {

    @Override
    public ViewHolder provideViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public void bindView(ViewHolder viewHolder, int i, TreeNode treeNode) {
        LocationLayoutType locationNode = (LocationLayoutType) treeNode.getContent();
        viewHolder.tvName.setText(locationNode.name);
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_select_multiple_tree_dialog;
    }

    public class ViewHolder extends TreeViewBinder.ViewHolder{

        private ImageView ivArrow;
        TextView tvName;
        CheckBox checkBox;


        public ImageView getIvArrow() {
            return ivArrow;
        }

        public ViewHolder(View rootView) {
            super(rootView);

            this.tvName = rootView.findViewById(R.id.item_select_multiple_tree_dialog_name);
            this.ivArrow = rootView.findViewById(R.id.item_select_multiple_tree_dialog_arrow);
            this.checkBox = rootView.findViewById(R.id.item_select_multiple_tree_dialog_checkbox);


            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

            });
        }
    }
}
