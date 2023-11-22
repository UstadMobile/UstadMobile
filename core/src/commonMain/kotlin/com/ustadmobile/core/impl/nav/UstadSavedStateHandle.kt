package com.ustadmobile.core.impl.nav

/**
 * SavedStateHandle interface is based on https://developer.android.com/reference/androidx/lifecycle/SavedStateHandle
 * It is used by ViewModels to save state data as the user edits. The data edited by the user may
 * or may not be saved to the database (e.g. user can discard changes).
 *
 * The default saved state handle provided to a ViewModel will also contain all arguments passed to
 * the viewmodel.
 *
 * On Android this is implemented by the SavedStateHandle itself (as provided by Jetpack Navigation)
 * On the web this is implemented by the history state API.
 */
interface UstadSavedStateHandle {

    operator fun set(key: String, value: String?)

    operator fun get(key: String): String?

    val keys: Set<String>


}