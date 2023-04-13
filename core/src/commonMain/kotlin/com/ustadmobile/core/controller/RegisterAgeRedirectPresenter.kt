package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.isDateOfBirthAMinor
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.RegisterAgeRedirectView
import com.ustadmobile.core.view.SiteTermsDetailView
import com.ustadmobile.door.util.systemTimeInMillis
import kotlinx.datetime.Instant
import org.kodein.di.DI
import org.kodein.di.instance

class RegisterAgeRedirectPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: RegisterAgeRedirectView,
    di: DI
): UstadBaseController<RegisterAgeRedirectView>(context, arguments, view, di, activeSessionRequired = false) {

    val systemImpl: UstadMobileSystemImpl by instance()

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.dateOfBirth = systemTimeInMillis()

    }

    fun handleClickNext() {
        val ageDateTime = Instant.fromEpochMilliseconds(view.dateOfBirth)

        val args = arguments.toMutableMap()
        args[PersonEditView.ARG_DATE_OF_BIRTH] = view.dateOfBirth.toString()

        //If the person is a minor, they do not accept terms and conditions themselves. Their
        // account needs approval
        val viewName =  if(ageDateTime.isDateOfBirthAMinor()) {
            args[PersonEditView.ARG_REGISTRATION_MODE] =
                    (PersonEditView.REGISTER_MODE_ENABLED + PersonEditView.REGISTER_MODE_MINOR).toString()
            PersonEditView.VIEW_NAME_REGISTER
        }else {
            args[PersonEditView.ARG_REGISTRATION_MODE] = PersonEditView.REGISTER_MODE_ENABLED.toString()
            SiteTermsDetailView.VIEW_NAME_ACCEPT_TERMS
        }

        systemImpl.go(viewName, args, context)

    }

}