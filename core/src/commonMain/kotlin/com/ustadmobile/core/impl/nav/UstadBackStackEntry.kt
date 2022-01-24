package com.ustadmobile.core.impl.nav

interface UstadBackStackEntry {

    val viewName: String

    val savedStateHandle: UstadSavedStateHandle

    val arguments: Map<String, String>

}