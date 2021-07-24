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

    val defaultRoute: KClass<out Component<RProps, *>> = RedirectComponent::class

    val destinationList = listOf(
        UstadDestination("library_books", MessageID.content, ContentEntryListTabsView.VIEW_NAME,
            ContentEntryListTabsComponent::class, true),
        UstadDestination("school", MessageID.schools,SchoolListView.VIEW_NAME, PlaceHolderComponent::class),
        UstadDestination("people", MessageID.classes,ClazzList2View.VIEW_NAME, ClazzListComponent::class),
        UstadDestination("person", MessageID.people, PersonListView.VIEW_NAME, PersonListComponent::class),
        UstadDestination("pie_chart", MessageID.reports, ReportListView.VIEW_NAME, PlaceHolderComponent::class, divider = true),
        UstadDestination("settings", MessageID.settings, SettingsView.VIEW_NAME, PlaceHolderComponent::class),
        UstadDestination(view = ContentEntryList2View.VIEW_NAME, component = ContentEntryListComponent::class),
        UstadDestination(view = Login2View.VIEW_NAME, labelId = MessageID.login, component = LoginComponent::class, showNavigation = false),
        UstadDestination(view = ContentEntryDetailView.VIEW_NAME, component = ContentEntryDetailComponent::class),
        UstadDestination(view = ContentEntryDetailOverviewView.VIEW_NAME, component = ContentEntryDetailOverviewComponent::class),
        UstadDestination(view = EpubContentView.VIEW_NAME, component = EpubContentComponent::class),
        UstadDestination(view = PersonDetailView.VIEW_NAME, component = PersonDetailComponent::class),
        UstadDestination(view = PersonAccountEditView.VIEW_NAME, component = PersonAccountEditComponent::class),
        UstadDestination(view = PersonEditView.VIEW_NAME, component = PersonEditComponent::class),
        UstadDestination(view = XapiPackageContentView.VIEW_NAME, component = XapiPackageContentComponent::class),
        UstadDestination(view = VideoContentView.VIEW_NAME, component = VideoContentComponent::class),
        UstadDestination(view = WebChunkView.VIEW_NAME, component = WebChunkComponent::class),
        UstadDestination(view = RedirectView.VIEW_NAME, component = RedirectComponent::class),
        UstadDestination(view = ClazzDetailView.VIEW_NAME, component = ClazzDetailComponent::class),
        UstadDestination(view = ClazzEdit2View.VIEW_NAME, component = ClazzEditComponent::class),
        UstadDestination(view = ClazzMemberListView.VIEW_NAME, component = ClazzMemberListComponent::class),
        UstadDestination(view = ClazzDetailOverviewView.VIEW_NAME, component = ClazzDetailOverviewComponent::class),
        UstadDestination(labelId= MessageID.accounts, view = AccountListView.VIEW_NAME, component = PlaceHolderComponent::class)
    )

    /**
     * Default destination to navigate to when destination is not specified
     */
    val defaultDestination: UstadDestination = destinationList.first {
        it.view == RedirectView.VIEW_NAME
    }

    /**
     * Find destination given view name from URL
     * @param view: Current view name
     */
    fun lookupDestinationName(view: String?): UstadDestination? {
        return destinationList.firstOrNull{
            it.view == view
        }
    }
}