package com.ustadmobile.lib.rest.domain.account

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.account.SendConsentRequestToParentUseCase
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UstadUrlComponents
import com.ustadmobile.core.util.ext.toQueryString
import com.ustadmobile.core.viewmodel.person.child.AddChildProfilesViewModel
import com.ustadmobile.lib.rest.NotificationSender
import com.ustadmobile.lib.rest.domain.invite.email.SendEmailUseCase
import com.ustadmobile.lib.rest.domain.invite.email.SendEmailUseCaseImpl
import io.github.aakira.napier.Napier


/**
 * UseCase server-side implementation to send consent request to parent via email, sms
 */
class SendConsentRequestToParentServerImpl(
    private val sendEmailUseCase: SendEmailUseCase,
    private val learningSpace: LearningSpace,
) : SendConsentRequestToParentUseCase {

    override suspend fun invoke(request: SendConsentRequestToParentUseCase.SendConsentRequestToParentRequest) {
        try {
            val link = UstadUrlComponents(
                learningSpace.url,
                AddChildProfilesViewModel.DEST_NAME,
                queryString = mapOf(
                    AddChildProfilesViewModel.ARG_CHILD_NAME to request.childFullName,
                    AddChildProfilesViewModel.ARG_CHILD_GENDER to request.childGender.toString(),
                    AddChildProfilesViewModel.ARG_CHILD_DATE_OF_BIRTH to request.childDateOfBirth.toString(),
                ).toQueryString()
            ).fullUrl()
            sendEmailUseCase.invoke(
                subject = "Parental Consent Required for ${request.childFullName}’s Registration",
                email = request.parentContact,
                link = link
            )
        } catch (e: Exception) {
            Napier.e(e) { "SendConsentRequestToParentUseCase: ${e.message}" }
            throw e
        }
    }
}
