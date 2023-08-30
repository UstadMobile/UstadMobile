package com.ustadmobile.port.android.view.binding

import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.BindingAdapter
import com.toughra.ustadmobile.R
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.CourseBlockWithEntity
import com.ustadmobile.core.R as CR

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
    "onClickUnIndentBlockPopupMenu", "onClickDeleteBlockPopupMenu","blockPopupMenu"], requireAll = false)
fun View.setOnBlockPopupMenuItemClickListener(
        onClickHideBlockPopupMenu: View.OnClickListener?,
        onClickIndentBlockPopupMenu: View.OnClickListener?,
        onClickUnIndentBlockPopupMenu: View.OnClickListener?,
        onClickDeleteBlockPopupMenu: View.OnClickListener?,
        block: CourseBlockWithEntity?){
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
        if(block?.cbType == CourseBlock.BLOCK_MODULE_TYPE){
            popupMenu.menu.findItem(R.id.block_indent).isVisible = false
            popupMenu.menu.findItem(R.id.block_unindent).isVisible = false
        }
        if(block?.cbIndentLevel == 2){
            popupMenu.menu.findItem(R.id.block_indent).isVisible = false
        }
        if(block?.cbIndentLevel == 0){
            popupMenu.menu.findItem(R.id.block_unindent).isVisible = false
        }
        if(block?.cbHidden == true){
            popupMenu.menu.findItem(R.id.block_hide).setTitle(CR.string.unhide)
        }
        popupMenu.show()
    }
}