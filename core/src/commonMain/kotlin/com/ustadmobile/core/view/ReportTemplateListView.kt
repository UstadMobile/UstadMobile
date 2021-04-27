package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Report


interface ReportTemplateListView: UstadListView<Report, Report> {



    companion object {

        const val VIEW_NAME = "ReportTemplateListView"
    }

}