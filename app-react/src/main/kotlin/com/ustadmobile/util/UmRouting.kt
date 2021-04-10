package com.ustadmobile.util

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.*
import com.ustadmobile.model.UmReactDestination
import com.ustadmobile.view.ContentEntryListComponent
import react.RBuilder
import react.router.dom.hashRouter
import react.router.dom.route
import react.router.dom.switch

object UmRouting {

    val destinationList = listOf(
        UmReactDestination("library_books", MessageID.content, ContentEntryList2View.VIEW_NAME,
            ContentEntryListComponent::class, true),
        UmReactDestination("school", MessageID.schools,SchoolListView.VIEW_NAME,
            ContentEntryListComponent::class),
        UmReactDestination("people", MessageID.classes,SchoolListView.VIEW_NAME,
            ContentEntryListComponent::class),
        UmReactDestination("person", MessageID.people, PersonListView.VIEW_NAME,
            ContentEntryListComponent::class),
        UmReactDestination("pie_chart", MessageID.reports, ReportListView.VIEW_NAME,
            ContentEntryListComponent::class, divider = true),
        UmReactDestination("settings", MessageID.settings, SettingsView.VIEW_NAME,
            ContentEntryListComponent::class),
        UmReactDestination( labelId= MessageID.accounts, view = AccountListView.VIEW_NAME,
             component = ContentEntryListComponent::class)
    )

    fun findDestination(view: String): UmReactDestination? {
        return destinationList.firstOrNull{it.view == view}
    }

    fun RBuilder.umRouter() {
        hashRouter {
            switch{
                route("/", destinationList.first().component, exact = true)
                destinationList.forEach {
                    route("/${it.view}", it.component, exact = false)
                }
            }
        }
    }
}