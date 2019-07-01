package com.ustadmobile.port.android.view;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.CommonEntityHandlerPresenter;

import tellh.com.recyclertreeview_lib.TreeNode;
import tellh.com.recyclertreeview_lib.TreeViewBinder;

public class EntityNodeBinder extends TreeViewBinder<EntityNodeBinder.ViewHolder> {

    CommonEntityHandlerPresenter mPresenter;

    EntityNodeBinder(CommonEntityHandlerPresenter presenter){
        this.mPresenter = presenter;
    }

    @Override
    public ViewHolder provideViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public void bindView(ViewHolder viewHolder, int i, TreeNode treeNode) {
        EntityLayoutType locationNode = (EntityLayoutType) treeNode.getContent();
        viewHolder.tvName.setText(locationNode.name);
        viewHolder.locationUid = locationNode.uid;
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_select_multiple_tree_dialog;
    }

    public class ViewHolder extends TreeViewBinder.ViewHolder{

        private ImageView ivArrow;
        TextView tvName;
        CheckBox checkBox;
        Long locationUid;


        public ImageView getIvArrow() {
            return ivArrow;
        }

        public CheckBox getCheckBox() {
            return checkBox;
        }

        public ViewHolder(View rootView) {
            super(rootView);

            this.tvName = rootView.findViewById(R.id.item_select_multiple_tree_dialog_name);
            this.ivArrow = rootView.findViewById(R.id.item_select_multiple_tree_dialog_arrow);
            this.checkBox = rootView.findViewById(R.id.item_select_multiple_tree_dialog_checkbox);

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                this.checkBox.setChecked(isChecked);
                mPresenter.entityChecked(tvName.getText().toString(), locationUid, isChecked);

            });
        }
    }
}
