package com.ustadmobile.core.view

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage


interface SiteEditView: UstadEditView<Site> {

    var siteTermsList: DoorLiveData<List<SiteTermsWithLanguage>>?

    companion object {

        const val VIEW_NAME = "SiteEditView"

    }

}