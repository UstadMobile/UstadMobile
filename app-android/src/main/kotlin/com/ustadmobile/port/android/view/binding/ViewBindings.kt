package com.ustadmobile.port.android.view.binding

import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import com.ustadmobile.port.android.view.util.PagedItemSelectionListener
import com.ustadmobile.port.android.view.util.SelectableViewHelper

@BindingAdapter("android:layout_marginTop")
fun View.setMarginTopValue(marginValue: Float) =
        (layoutParams as? ViewGroup.MarginLayoutParams)?.apply { topMargin = marginValue.toInt() }

@BindingAdapter("android:layout_marginBottom")
fun View.setMarginBottomValue(marginValue: Float) =
        (layoutParams as? ViewGroup.MarginLayoutParams)?.apply { bottomMargin = marginValue.toInt() }

@BindingAdapter("android:layout_marginStart")
fun View.setMarginStartValue(marginValue: Float) =
        (layoutParams as? ViewGroup.MarginLayoutParams)?.apply { leftMargin = marginValue.toInt() }

@BindingAdapter("android:layout_marginEnd")
fun View.setMarginEndValue(marginValue: Float) =
        (layoutParams as? ViewGroup.MarginLayoutParams)?.apply { rightMargin = marginValue.toInt() }

@BindingAdapter("onLongPress")
fun View.setOnLongPress(onLongClick: View.OnClickListener) {
    setOnLongClickListener {
        onLongClick.onClick(this)

        true
    }
}


interface OnSelectionStateChangedListener {
    fun onSelectionStateChanged(view: View)
}

/**
 * Convenience binder for handling events with a selectable view (e.g. an item in a list).
 */
@BindingAdapter(value=["selectableViewHelper", "onSelectableItemClicked", "onSelectedStateChanged"], requireAll = false)
fun <T> View.setSelectableViewHelper(selectableViewHelper: SelectableViewHelper?,
                                     onSelectableItemClicked: View.OnClickListener?,
                                     onSelectedStateChanged: OnSelectionStateChangedListener?) {

    setOnClickListener {
        if(selectableViewHelper?.isInSelectionMode != true) {
            onSelectableItemClicked?.onClick(it)
        }else {
            it.isSelected = !it.isSelected
            onSelectedStateChanged?.onSelectionStateChanged(it)
        }
    }

    setOnLongClickListener {
        it.isSelected = !it.isSelected
        onSelectedStateChanged?.onSelectionStateChanged(it)
        true
    }
}
