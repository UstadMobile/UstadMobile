package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.Person

/**
 * SEL Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface SELAnswerListView : UstadView {

    /**
     * Sets Current SEL answers by students list
     *
     * This method's purpose is to set the provider given to it to the view.
     * On Android it will be set to the recycler view..
     *
     * @param selAnswersProvider The provider data
     */
    fun setSELAnswerListProvider(selAnswersProvider: DataSource.Factory<Int, Person>)

    fun showFAB(show: Boolean)

    companion object {

        //View name
        val VIEW_NAME = "SELAnswerList"
    }
}
