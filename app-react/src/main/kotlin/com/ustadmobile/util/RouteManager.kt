package com.ustadmobile.util

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.core.view.UstadView.Companion.MASTER_SERVER_ROOT_ENTRY_UID
import com.ustadmobile.model.UmReactDestination
import com.ustadmobile.view.ContentEntryDetailComponent
import com.ustadmobile.view.ContentEntryListComponent
import com.ustadmobile.view.PlaceHolderComponent
import kotlinx.browser.window
import org.w3c.dom.url.URL
import react.RBuilder
import react.router.dom.hashRouter
import react.router.dom.route
import react.router.dom.switch

/**
 * Manages all route functionalities like defining the routes, get arguments
 * find destinations, and getting pathname.
 */
object RouteManager {

    val destinationList = listOf(
        UmReactDestination("library_books", MessageID.content, ContentEntryList2View.VIEW_NAME,
            ContentEntryListComponent::class, true,
            args = mapOf(ARG_PARENT_ENTRY_UID to MASTER_SERVER_ROOT_ENTRY_UID.toString())),
        UmReactDestination("school", MessageID.schools,SchoolListView.VIEW_NAME,
            PlaceHolderComponent::class),
        UmReactDestination("people", MessageID.classes,ClazzList2View.VIEW_NAME,
            PlaceHolderComponent::class),
        UmReactDestination("person", MessageID.people, PersonListView.VIEW_NAME,
            PlaceHolderComponent::class),
        UmReactDestination("pie_chart", MessageID.reports, ReportListView.VIEW_NAME,
            PlaceHolderComponent::class, divider = true),
        UmReactDestination("settings", MessageID.settings, SettingsView.VIEW_NAME,
            PlaceHolderComponent::class),
        UmReactDestination( labelId= MessageID.accounts, view = AccountListView.VIEW_NAME,
             component = ContentEntryDetailComponent::class),
        UmReactDestination(view = ContentEntry2DetailView.VIEW_NAME,
            component = ContentEntryDetailComponent::class)
    )

    /**
     * Find destination to navigate to
     * @param view: Current view
     */
    fun findDestination(view: String?): UmReactDestination? {
        return destinationList.firstOrNull{it.view == view}
    }

    /**
     * Get current path name from location
     */
    fun getPathName (path:String? = null) : String {
        val mPath = (path ?: window.location.href).replace("#/","")
        return URL(mPath).pathname.replace("/","")
    }

    /**
     * Parse query params into map of arguments
     */
    fun getArgs(): Map<String, String> {
        val cleanUrl = window.location.href.replace("#/","")
        return if(cleanUrl.indexOf("?") != -1)
            UMFileUtil.parseURLQueryString(cleanUrl) else mapOf()
    }

    /**
     * Create routes based on all defined destinations
     */
    fun RBuilder.renderRoutes() {
        hashRouter {
            switch{
                destinationList.forEach {
                    if(it.view == ContentEntryList2View.VIEW_NAME){
                        route("/", it.component, exact = true)
                    }
                    route("/${it.view}", it.component, exact = true)
                }
            }
        }
    }
}