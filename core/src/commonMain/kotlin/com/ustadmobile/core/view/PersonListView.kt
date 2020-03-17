package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.Person

/**
 * Created by mike on 3/8/18.
 */

interface PersonListView : UstadView {

    fun setProvider(provider: DataSource.Factory<Int, Person>)

    companion object {

        const val VIEW_NAME = "PersonList"
    }

}
