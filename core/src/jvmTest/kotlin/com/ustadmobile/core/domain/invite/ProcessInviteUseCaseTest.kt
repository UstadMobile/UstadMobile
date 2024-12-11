package com.ustadmobile.core.domain.invite

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.viewmodel.clazz.inviteviaContact.InviteViaContactChip
import com.ustadmobile.lib.rest.domain.invite.ProcessInviteUseCase
import com.ustadmobile.lib.rest.domain.invite.email.SendEmailUseCase
import com.ustadmobile.lib.rest.domain.invite.message.SendMessageUseCase
import com.ustadmobile.lib.rest.domain.invite.sms.SendSmsUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.kodein.di.DI
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals

class ProcessInviteUseCaseTest {

    private lateinit var sendEmailUseCase: SendEmailUseCase
    private lateinit var sendSmsUseCase: SendSmsUseCase
    private lateinit var checkContactTypeUseCase: CheckContactTypeUseCase
    private lateinit var sendMessageUseCase: SendMessageUseCase
    private lateinit var db: UmAppDatabase
    private lateinit var endpoint: LearningSpace
    private lateinit var processInviteUseCase: ProcessInviteUseCase
    private lateinit var di: DI

    private lateinit var repo: UmAppDatabase

    @Before
    fun setUp() {
        sendEmailUseCase = mock()
        sendSmsUseCase = mock()
        sendMessageUseCase = mock()
        checkContactTypeUseCase = mock()
        db = mock()
        repo = mock()
        endpoint = mock()

        processInviteUseCase = ProcessInviteUseCase(
            sendEmailUseCase,
            sendSmsUseCase,
            sendMessageUseCase,
            checkContactTypeUseCase,
            db,
            endpoint,
            repo
        )
    }

    @Test
    fun given_contacts_when_invoked_then_will_insert_invites_and_return_use_case_names() =
        runBlocking {
            val contacts = listOf(
               "email@example.com",
                "911234567890",
                "internal@Message",
            )
            val clazzUid = 1L
            val role = 2L
            val personUid = 3L

            val token = "test-token"
            val inviteLink = "http://example.com/$token"
            whenever(endpoint.url).thenReturn("http://example.com")

            val result = processInviteUseCase.invoke(contacts, clazzUid, role, personUid)

            assertEquals(ProcessInviteUseCase.InviteResult("invitation sent"), result)
        }
}
