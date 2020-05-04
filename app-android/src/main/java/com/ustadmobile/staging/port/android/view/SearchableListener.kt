package com.ustadmobile.staging.core.view


interface SearchableListener{
    abstract fun onSearchButtonClick()

    abstract fun onSearchQueryUpdated(query: String)
}