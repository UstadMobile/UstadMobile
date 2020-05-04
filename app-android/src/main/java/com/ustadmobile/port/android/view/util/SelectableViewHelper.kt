package com.ustadmobile.port.android.view.util

/**
 * This interface works with ViewBindings setSelectableViewHelper. When managing the selection of
 * items in a list, we need to track if the list is in selection mode (or not) to be able to
 * correctly interpret events as follows:
 *
 * Long click anytime = toggle selection
 * Normal click when no items are selected = run normal onClick handler
 * Normal click when items are selected = toggle selection
 */
interface SelectableViewHelper {

    val isInSelectionMode: Boolean

}