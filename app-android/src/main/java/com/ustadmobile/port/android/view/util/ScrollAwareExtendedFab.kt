package com.ustadmobile.port.android.view.util

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

/**
 * This class is really a dummy class so that we can create a child class of the protected
 * ExtendedFloatingActionButtonBehavior
 */
class ScrollAwareExtendedFab(context: Context, attributeSet: AttributeSet) : ExtendedFloatingActionButton(context, attributeSet) {

    protected class ScrollAwareExtendedFabBehavior(context: Context, attributeSet: AttributeSet): ExtendedFloatingActionButtonBehavior<ExtendedFloatingActionButton>(context, attributeSet) {
        override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: ExtendedFloatingActionButton, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
            return axes == ViewCompat.SCROLL_AXIS_VERTICAL
                    || return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
        }

        override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: ExtendedFloatingActionButton, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int, consumed: IntArray) {
            super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed)

            //Don't change the fab when it is not visible
            if(!child.isVisible)
                return

            val verticalOffset = (target as? RecyclerView)?.computeVerticalScrollOffset()

            if(dyConsumed > 0 && child.isExtended) {
                child.shrink()
            }else if(dyConsumed < 0 && !child.isExtended && verticalOffset == 0){
                child.extend()
            }
        }
    }

}
