package com.ustadmobile.core.view

import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.door.lifecycle.MutableLiveData
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.PersonParentJoinWithMinorPerson
import com.ustadmobile.lib.db.entities.SiteTerms


interface ParentalConsentManagementView: UstadEditView<PersonParentJoinWithMinorPerson> {

    var infoText: String?

    var siteTerms: SiteTerms?

    var relationshipFieldOptions: List<IdOption>?

    var relationshipFieldError: String?

    companion object {

        const val VIEW_NAME = "ParentConsentManagement"

    }

}