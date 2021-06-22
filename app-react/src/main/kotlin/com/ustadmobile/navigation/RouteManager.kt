package com.ustadmobile.navigation

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.*
import com.ustadmobile.view.*
import react.Component
import react.RProps
import kotlin.reflect.KClass

/**
 * Manages all route functionalities like defining the routes and find destinations
 */
object RouteManager {

    val defaultRoute: KClass<out Component<RProps, *>> = PlaceHolderComponent::class

    val destinationList = listOf(
        UstadDestination("library_books", MessageID.content, ContentEntryListTabsView.VIEW_NAME,
            PlaceHolderComponent::class, true),
        UstadDestination("school", MessageID.schools,SchoolListView.VIEW_NAME, PlaceHolderComponent::class),
        UstadDestination("people", MessageID.classes,ClazzList2View.VIEW_NAME, PlaceHolderComponent::class),
        UstadDestination("person", MessageID.people, PersonListView.VIEW_NAME, PlaceHolderComponent::class),
        UstadDestination("pie_chart", MessageID.reports, ReportListView.VIEW_NAME, PlaceHolderComponent::class, divider = true),
        UstadDestination("settings", MessageID.settings, SettingsView.VIEW_NAME, PlaceHolderComponent::class),
        /*Destination(view = Login2View.VIEW_NAME, labelId = MessageID.login, component = LoginComponent::class, showNavigation = false),
        Destination( labelId= MessageID.accounts, view = AccountListView.VIEW_NAME, component = PlaceHolderComponent::class),
        Destination(view = ContentEntryDetailView.VIEW_NAME, component = ContentEntryDetailComponent::class),
        Destination(view = XapiPackageContentView.VIEW_NAME, component = XapiPackageContentComponent::class),
        Destination(view = EpubContentView.VIEW_NAME, component = EpubContentComponent::class),
        Destination(view = ContentEntryList2View.VIEW_NAME, component = ContentEntryListComponent::class),
        Destination(view = VideoContentView.VIEW_NAME, component = VideoContentComponent::class),
        Destination(view = WebChunkView.VIEW_NAME, component = WebChunkComponent::class),
        Destination(view = PersonDetailView.VIEW_NAME, component = PersonDetailComponent::class),
        Destination(view = PersonAccountEditView.VIEW_NAME, component = PersonAccountEditComponent::class),
        Destination(view = PersonEditView.VIEW_NAME, component = PersonEditComponent::class),*/
    )

    /**
     * Default destination to navigate to when destination is not specified
     */
    val defaultDestination: UstadDestination = destinationList.first {
        it.view == ContentEntryListTabsView.VIEW_NAME
    }

    /**
     * Find destination given view name from URL
     * @param view: Current view name
     */
    fun findDestination(view: String?): UstadDestination? {
        return destinationList.firstOrNull{
            it.view == view
        }
    }
}