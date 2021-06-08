package com.ustadmobile.core.util.ext

import com.ustadmobile.core.view.UstadViewChoiceDialogListener
import com.ustadmobile.core.view.UstadView

/**
 * Show a basic choice dialog. This is an expect-actual. There is no real actual implementation
 * on JVM (as this is a server-only platform) with no GUI.
 *
 * @param context Context object
 * @param title Title to display, or null
 * @param items String array of choices that should be shown to the user
 * @param listener listener that will be called when one the items is chosen
 *
 * Note for "morning" - it might be worth creating an interface and accessing that through the
 * DI to make it testable.
 *
 */
expect fun UstadView.showChoiceDialog(context: Any,
                                      title: String?,
                                      items: Array<String>,
                                      listener: UstadViewChoiceDialogListener)
