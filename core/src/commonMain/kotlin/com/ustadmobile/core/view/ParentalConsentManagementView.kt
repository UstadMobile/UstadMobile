package com.ustadmobile.core.view

import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.PersonParentJoinWithMinorPerson
import com.ustadmobile.lib.db.entities.SiteTerms


interface ParentalConsentManagementView: UstadEditView<PersonParentJoinWithMinorPerson> {

    var infoText: String?

    var siteTerms: SiteTerms?

    var relationshipFieldOptions: List<IdOption>?

    companion object {

        const val VIEW_NAME = "ParentAccountLandingEditView"

    }

}