package com.ustadmobile.core.viewmodel.parentalconsentmanagement

import com.ustadmobile.lib.db.entities.*

data class ParentalConsentManagementUiState(

    val personParentJoin: PersonParentJoinWithMinorPerson? = null,

    val relationshipError: String? = null,

    val siteTerms: SiteTerms? = null,

    val fieldsEnabled: Boolean = true,

    val appName: String = "Ustad Mobile"

) {

    val relationshipVisible: Boolean
        get() = personParentJoin?.ppjParentPersonUid  != null
                && personParentJoin.ppjParentPersonUid == 0L

    val consentVisible: Boolean
        get() = personParentJoin?.ppjParentPersonUid  != null
                && personParentJoin.ppjParentPersonUid == 0L

    val dontConsentVisible: Boolean
        get() = personParentJoin?.ppjParentPersonUid  != null
                && personParentJoin.ppjParentPersonUid == 0L

    val changeConsentVisible: Boolean
        get() = personParentJoin?.ppjParentPersonUid  != null
                && personParentJoin.ppjParentPersonUid == 0L
}

class ParentalConsentManagementViewModel  {

    companion object {

        const val DEST_NAME = "ParentalConsentManagement"

    }
}
