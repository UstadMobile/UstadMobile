package com.ustadmobile.core.impl.appstate

/**
 * A Context Menu Item as per
 *
 * https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Context_Menu/README.md
 *
 * We need our own data class that can be shared : used on Web/Desktop, ignored on Android.
 */
data class UstadContextMenuItem(
    val label: String,
    val onClick: () -> Unit,
)
