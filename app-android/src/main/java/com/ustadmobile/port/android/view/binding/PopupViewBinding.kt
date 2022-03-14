package com.ustadmobile.port.android.view.binding

import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.BindingAdapter
import com.toughra.ustadmobile.R

@BindingAdapter(value = ["onClickEditPopupMenu", "onClickDeletePopupMenu"], requireAll = false)
fun View.setOnPopupMenuItemClickListener(onClickEditPopupMenu: View.OnClickListener?, onClickDeletePopupMenu: View.OnClickListener?) {
    this.setOnClickListener {
        val popupMenu = PopupMenu(this.context, this)
        popupMenu.setOnMenuItemClickListener {item ->
            when(item.itemId) {
                R.id.edit -> onClickEditPopupMenu?.onClick(this)
                R.id.delete -> onClickDeletePopupMenu?.onClick(this)
            }
            true
        }

        popupMenu.inflate(R.menu.menu_edit_delete)
        popupMenu.show()
    }
}


@BindingAdapter(value = ["onClickHideBlockPopupMenu","onClickIndentBlockPopupMenu",
    "onClickUnIndentBlockPopupMenu", "onClickDeleteBlockPopupMenu"], requireAll = false)
fun View.setOnBlockPopupMenuItemClickListener(
        onClickHideBlockPopupMenu: View.OnClickListener?,
        onClickIndentBlockPopupMenu: View.OnClickListener?,
        onClickUnIndentBlockPopupMenu: View.OnClickListener?,
        onClickDeleteBlockPopupMenu: View.OnClickListener?){
    setOnClickListener{
        val popupMenu = PopupMenu(this.context, this)
        popupMenu.setOnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.block_hide -> onClickHideBlockPopupMenu?.onClick(this)
                R.id.block_indent -> onClickIndentBlockPopupMenu?.onClick(this)
                R.id.block_unindent -> onClickUnIndentBlockPopupMenu?.onClick(this)
                R.id.block_delete -> onClickDeleteBlockPopupMenu?.onClick(this)
            }
            true
        }
        popupMenu.inflate(R.menu.menu_course_block_options)
        popupMenu.show()
    }
}