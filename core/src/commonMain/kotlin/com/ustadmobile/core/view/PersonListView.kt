package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails

interface PersonListView: UstadListView<Person, PersonWithDisplayDetails> {

    var inviteViaLinkVisibile: Boolean

    companion object {
        const val VIEW_NAME = "PersonListView"

        const val VIEW_NAME_HOME = "PersonListHome"

        /**
         * Exclude those who are already in the given class. This is useful for
         * the add to class picker (e.g. to avoid showing people who are already in the
         * given class)
         */
        const val ARG_FILTER_EXCLUDE_MEMBERSOFCLAZZ = "exlcudeFromClazz"

        const val ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL = "excludeFromSchool"

        const val ARG_EXCLUDE_PERSONUIDS_LIST = "excludeAlreadySelectedList"

    }

}