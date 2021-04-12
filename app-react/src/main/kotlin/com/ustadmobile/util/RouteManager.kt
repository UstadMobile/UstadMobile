package com.ustadmobile.util

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.*
import com.ustadmobile.model.UmReactDestination
import com.ustadmobile.model.statemanager.UmAppState
import com.ustadmobile.view.ContentEntryDetailComponent
import com.ustadmobile.view.ContentEntryListComponent
import kotlinx.browser.window
import react.*
import react.dom.link
import react.router.dom.*
import styled.styledDiv

object UmRouting {

    val destinationList = listOf(
        UmReactDestination("library_books", MessageID.content, ContentEntryList2View.VIEW_NAME,
            ContentEntryListComponent::class, true),
        UmReactDestination("school", MessageID.schools,SchoolListView.VIEW_NAME,
            ContentEntryDetailComponent::class),
        UmReactDestination("people", MessageID.classes,SchoolListView.VIEW_NAME,
            ContentEntryDetailComponent::class),
        UmReactDestination("person", MessageID.people, PersonListView.VIEW_NAME,
            ContentEntryDetailComponent::class),
        UmReactDestination("pie_chart", MessageID.reports, ReportListView.VIEW_NAME,
            ContentEntryDetailComponent::class, divider = true),
        UmReactDestination("settings", MessageID.settings, SettingsView.VIEW_NAME,
            ContentEntryDetailComponent::class),
        UmReactDestination( labelId= MessageID.accounts, view = AccountListView.VIEW_NAME,
             component = ContentEntryDetailComponent::class)
    )

    val queryParams: String = window.location.hash

    fun findDestination(view: String?): UmReactDestination? {
        return destinationList.firstOrNull{it.view == view}
    }

    fun RBuilder.umRouter() {
        hashRouter {
            switch{
                route("/", destinationList.first().component, exact = true)
                destinationList.forEach {
                    route("/${it.view}", it.component, exact = true)
                }
            }

        }
    }

    fun getPathName () : String{
        return  window.location.hash.split("?").first().replace("#/","")
    }
}