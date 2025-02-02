package com.ustadmobile.core.domain.bulkaddusers

import com.ustadmobile.core.account.AuthManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.clazz.CreateNewClazzUseCase
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.EnrolIntoCourseUseCase
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import com.ustadmobile.core.domain.person.bulkadd.BulkAddPersonsUseCaseImpl
import com.ustadmobile.core.domain.phonenumber.PhoneNumValidatorUseCase
import com.ustadmobile.core.domain.validateemail.ValidateEmailUseCase
import com.ustadmobile.core.io.ext.readString
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.Person
import kotlinx.coroutines.runBlocking
import org.mockito.kotlin.any
import org.mockito.kotlin.argWhere
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking
import java.util.concurrent.atomic.AtomicLong
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class BulkAddUsersUseCaseJvmTest {

    private lateinit var mockAddNewPersonUseCase: AddNewPersonUseCase

    private lateinit var mockValidateEmailUseCase: ValidateEmailUseCase

    private lateinit var mockValidatePhoneUseCase: PhoneNumValidatorUseCase

    private lateinit var mockAuthManager: AuthManager

    private lateinit var mockEnrolUseCase: EnrolIntoCourseUseCase

    private lateinit var mockCreateNewClazzUseCase: CreateNewClazzUseCase

    private lateinit var activeDb: UmAppDatabase

    private val personIdCounter = AtomicLong()

    private val personsAdded = mutableListOf<Person>()

    private val clazzIdCounter = AtomicLong()

    private val clazzesAdded = mutableListOf<Clazz>()

    @BeforeTest
    fun setup() {
        clazzesAdded.clear()

        mockAddNewPersonUseCase = mock {
            onBlocking { invoke(any(), any(), any(), any()) }.thenAnswer { invocation ->
                val personToAdd = invocation.arguments.first() as Person
                val newUid = personIdCounter.incrementAndGet()
                personsAdded.add(personToAdd.copy(personUid = newUid))
                newUid
            }

        }
        mockValidateEmailUseCase = mock {
            on { invoke(any()) }.thenAnswer {
                it.arguments.first() as String
            }
        }
        mockValidatePhoneUseCase = mock {
            on { isValid(any()) }.thenReturn(true)
        }
        mockAuthManager = mock { }
        mockEnrolUseCase = mock { }

        mockCreateNewClazzUseCase = mock {
            onBlocking { invoke(any()) }.thenAnswer {
                clazzesAdded += it.arguments.first() as Clazz
                clazzIdCounter.incrementAndGet()
            }
        }

        activeDb = DatabaseBuilder.databaseBuilder(UmAppDatabase::class,
            "jdbc:sqlite::memory:", 1L)
            .build()
    }

    private fun readCsv(fileName: String): String = this::class.java.getResourceAsStream(
        "/com/ustadmobile/core/domain/bulkaddusers/$fileName"
    )!!.readString()

    @Test
    fun givenValidImportRows_whenInvoked_thenWillAddNewPerson() {
        val csvData = readCsv("valid-import.csv")

        val importUseCase = BulkAddPersonsUseCaseImpl(
            addNewPersonUseCase = mockAddNewPersonUseCase,
            validateEmailUseCase = mockValidateEmailUseCase,
            validatePhoneNumUseCase = mockValidatePhoneUseCase,
            authManager = mockAuthManager,
            enrolUseCase = mockEnrolUseCase,
            activeDb = activeDb,
            createNewClazzUseCase = mockCreateNewClazzUseCase,
            activeRepo = null,
        )
        runBlocking {
            importUseCase(
                csv = csvData,
                onProgress = { _, _ -> }
            )
        }
    }

    @Test
    fun givenMissingRequiredColumns_whenInvoked_thenWillThrowIllegalArgumentException() {
        val csvData = readCsv("missing-columns.csv")
        var exception: Throwable? = null

        runBlocking {
            try {
                BulkAddPersonsUseCaseImpl(
                    addNewPersonUseCase = mockAddNewPersonUseCase,
                    validateEmailUseCase = mockValidateEmailUseCase,
                    validatePhoneNumUseCase = mockValidatePhoneUseCase,
                    authManager = mockAuthManager,
                    enrolUseCase = mockEnrolUseCase,
                    activeDb = activeDb,
                    createNewClazzUseCase = mockCreateNewClazzUseCase,
                    activeRepo = null,
                ).invoke(
                    csv = csvData,
                    onProgress = { _, _ -> }
                )
            }catch(e: Throwable) {
                exception  = e
            }
            assertTrue(exception is IllegalArgumentException)
        }
    }

    @Test
    fun givenClazzDoesNotExist_whenInvoked_thenWillCreateNewClazzAndProcessEnrolment() {
        val csvData = readCsv("needs-new-clazz.csv")

        val importUseCase = BulkAddPersonsUseCaseImpl(
            addNewPersonUseCase = mockAddNewPersonUseCase,
            validateEmailUseCase = mockValidateEmailUseCase,
            validatePhoneNumUseCase = mockValidatePhoneUseCase,
            authManager = mockAuthManager,
            enrolUseCase = mockEnrolUseCase,
            activeDb = activeDb,
            createNewClazzUseCase = mockCreateNewClazzUseCase,
            activeRepo = null,
        )
        runBlocking {
            importUseCase(
                csv = csvData,
                onProgress = { _, _ -> }
            )

            verifyBlocking(mockCreateNewClazzUseCase) {
                invoke(argWhere { it.clazzName == "newclazz1" })
            }
            val newClazz = clazzesAdded.first { it.clazzName == "newclazz1" }
            val personAdded = personsAdded.first { it.firstNames == "Bob" }

            verifyBlocking(mockEnrolUseCase) {
                invoke(
                    enrolment = argWhere {
                        it.clazzEnrolmentClazzUid == newClazz.clazzUid &&
                                it.clazzEnrolmentClazzUid == personAdded.personUid &&
                                it.clazzEnrolmentRole == ClazzEnrolment.ROLE_STUDENT
                    },
                    timeZoneId = any()
                )
            }

        }
    }


}