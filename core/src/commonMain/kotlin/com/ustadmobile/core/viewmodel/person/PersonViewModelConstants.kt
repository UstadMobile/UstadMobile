package com.ustadmobile.core.viewmodel.person

object PersonViewModelConstants {

    /**
     * Provide a destination (e.g. DestName?arg=value... ) to navigate to when a person is
     * selected/created. This is used by PersonList and PersonEdit. Provides support for navigation flows
     * such as
     * ClazzMemberList (select to enrol new member) - PersonList - PersonEdit (if creating new person) - ClazzEnrolmentEdit
     */
    const val ARG_GO_TO_ON_PERSON_SELECTED = "goToOnPersonSelected"

}