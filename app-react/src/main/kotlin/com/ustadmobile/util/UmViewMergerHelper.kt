package com.ustadmobile.util

import react.RBuilder
import react.ReactElement
import styled.styledDiv

open class UmViewMergerHelper: RBuilder() {

    private val views: MutableList<ReactElement> = mutableListOf()

    fun addView(view: ReactElement){
        views.add(view)
    }

    fun getViews(): ReactElement {
        return styledDiv {
            this@UmViewMergerHelper.views.forEach {
                it
            }
        }
    }
}