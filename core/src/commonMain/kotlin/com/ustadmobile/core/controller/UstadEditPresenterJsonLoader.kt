package com.ustadmobile.core.controller

/**
 * This is separated out into an interface to faciltiate testing for OneToManyJoinHelper
 */
interface UstadEditPresenterJsonLoader {

    fun addJsonLoadListener(loadListener: UstadEditPresenter.JsonLoadListener): Boolean

    fun removeJsonLoadListener(loadListener: UstadEditPresenter.JsonLoadListener): Boolean

}