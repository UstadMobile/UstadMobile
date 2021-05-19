package com.ustadmobile.util

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_CONTENT_FILTER
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_LIBRARIES_CONTENT
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.core.view.UstadView.Companion.MASTER_SERVER_ROOT_ENTRY_UID
import com.ustadmobile.model.UmReactDestination
import com.ustadmobile.view.*
import kotlinx.browser.window
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
        UmReactDestination("library_books", MessageID.content, ContentEntryListTabsView.VIEW_NAME,
            ContentEntryListTabsComponent::class, true,
            args = mapOf(ARG_PARENT_ENTRY_UID to MASTER_SERVER_ROOT_ENTRY_UID.toString(),
                ARG_CONTENT_FILTER to ARG_LIBRARIES_CONTENT)),
        UmReactDestination("school", MessageID.schools,SchoolListView.VIEW_NAME,
            PlaceHolderComponent::class),
        UmReactDestination("people", MessageID.classes,ClazzList2View.VIEW_NAME,
            PlaceHolderComponent::class),
        UmReactDestination("person", MessageID.people, PersonListView.VIEW_NAME,
            PersonListComponent::class),
        UmReactDestination("pie_chart", MessageID.reports, ReportListView.VIEW_NAME,
            PlaceHolderComponent::class, divider = true),
        UmReactDestination("settings", MessageID.settings, SettingsView.VIEW_NAME,
            PlaceHolderComponent::class),
        UmReactDestination(view = Login2View.VIEW_NAME, labelId = MessageID.login,
            component = LoginComponent::class, showNavigation = false),
        UmReactDestination( labelId= MessageID.accounts, view = AccountListView.VIEW_NAME,
             component = PlaceHolderComponent::class),
        UmReactDestination(view = ContentEntryDetailView.VIEW_NAME,
            component = ContentEntryDetailComponent::class),
        UmReactDestination(view = XapiPackageContentView.VIEW_NAME,
            component = XapiPackageContentComponent::class),
        UmReactDestination(view = EpubContentView.VIEW_NAME,
            component = EpubContentComponent::class),
        UmReactDestination(view = ContentEntryList2View.VIEW_NAME,
            component = ContentEntryListComponent::class),
        UmReactDestination(view = VideoContentView.VIEW_NAME,
            component = VideoContentComponent::class),
        UmReactDestination(view = WebChunkView.VIEW_NAME,
            component = WebChunkComponent::class)
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
    fun getPathName(path: String? = null): String? {
        var pathName = (path ?: window.location.href).substringAfter("#/")
        if(pathName.indexOf("?") != -1)
            pathName = pathName.substringBefore("?")
        return if(pathName.isEmpty() || pathName.startsWith("http")) null else pathName
    }

    /**
     * Parse query params into map of arguments
     */
    fun getArgs(args: Map<String,String> = mapOf()): MutableMap<String, String> {
        val href = window.location.href
        val arguments = if(href.indexOf("?") != -1)
            UMFileUtil.parseURLQueryString(href.substringAfter("?"))
                .toMutableMap() else mutableMapOf()
        arguments.putAll(args)
        return arguments
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