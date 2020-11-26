package com.ustadmobile.port.android.view.util

import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

/**
 * This is intended to observe the view LifeCycle of a fragment in order to manage a floating
 * action button.
 */
class FabManagerLifecycleObserver(var extendedFab: ExtendedFloatingActionButton?, visible: Boolean,
                                  icon: Int, text: CharSequence?) : DefaultLifecycleObserver {

    private var active: Boolean = false

    var onClickListener: ((View) -> Unit)? = null
        set(value) {
            field = value
            extendedFab?.takeIf { active }?.setOnClickListener(value)
        }

    var icon: Int = icon
        set(value){
            field = value
            extendedFab?.takeIf { active && icon != 0 }?.also {
                it.icon = ContextCompat.getDrawable(it.context, value)
            }
        }

    var text: CharSequence? = text
        set(value) {
            field = value
            extendedFab?.takeIf { active && text != null }?.text = value
        }

    var visible: Boolean = visible
        set(value) {
            if(value == true && extendedFab?.isExtended == false)
                extendedFab?.extend()

            field = value

            extendedFab?.takeIf { active }?.visibility = if(value) View.VISIBLE else View.GONE
        }

    override fun onResume(owner: LifecycleOwner) {
        extendedFab?.also {
            it.takeIf { icon != 0 }?.icon = ContextCompat.getDrawable(it.context, icon)
            it.takeIf { text != null }?.text = text
            it.setOnClickListener(onClickListener)
            it.visibility = if(visible) View.VISIBLE else View.GONE
            if(visible && extendedFab?.isExtended == false)
                extendedFab?.extend()
        }

        active = true
    }

    override fun onPause(owner: LifecycleOwner) {
        active = false
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        extendedFab = null
        onClickListener = null
    }
}