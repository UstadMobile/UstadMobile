package com.ustadmobile.core.view

import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.Person

/**
 * Created by mike on 3/8/18.
 */

interface PersonListView : UstadView {

    fun setProvider(provider: UmProvider<Person>)

    companion object {

        val VIEW_NAME = "PersonList"
    }

}
