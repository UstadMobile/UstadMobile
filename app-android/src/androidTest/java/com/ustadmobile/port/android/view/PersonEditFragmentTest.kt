package com.ustadmobile.port.android.view

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.soywiz.klock.DateTime
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.insertPersonOnlyAndGroup
import com.ustadmobile.lib.db.entities.EntityRoleWithNameAndRole
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.port.android.generated.MessageIDMap
import com.ustadmobile.port.android.screen.PersonEditScreen
import com.ustadmobile.test.port.android.UmViewActions.hasInputLayoutError
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.*


@AdbScreenRecord("PersonEdit screen Test")
class PersonEditFragmentTest : TestCase() {

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    private val context = ApplicationProvider.getApplicationContext<Application>()

    private lateinit var mockWebServer: MockWebServer

    private lateinit var serverUrl: String

    val impl = UstadMobileSystemImpl.instance

    @Before
    fun setUp() {
        impl.messageIdMap = MessageIDMap.ID_MAP
        mockWebServer = MockWebServer()
        mockWebServer.start()
        serverUrl = mockWebServer.url("/").toString()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }


    @AdbScreenRecord("given person edit opened in normal mode classes should be shown")
    @Test
    fun givenPersonEditOpened_whenInNoRegistrationMode_thenClassesShouldBeShown() {


        init {

        }.run {

            PersonEditScreen {
                launchFragment(false, fillForm = false,
                        serverUrl = serverUrl, systemImplNavRule = systemImplNavRule,
                        impl = impl, context = context)
                scrollToBottom()
                clazzListRecyclerView {
                    isDisplayed()
                }
                clazzListHeaderTextView {
                    isDisplayed()
                }
            }

        }


    }


    // TODO roles have changed here, need to update
    @AdbScreenRecord("given person edit opened in normal mode and admin, " +
            "roles and permissions should be shown")
    //@Test
    fun givenPersonEditOpenedAsAdmin_whenInNoRegistrationMode_thenRolesShouldBeShown() {

        var entityRoles: List<EntityRoleWithNameAndRole> = listOf()
        init {
            val newRole = Role().apply {
                roleName = "Role A"
                roleUid = dbRule.db.roleDao.insert(this)
            }
            val schoolA = School().apply {
                schoolName = "School A"
                schoolActive = true
                schoolUid = dbRule.db.schoolDao.insert(this)
            }
            entityRoles = listOf(EntityRoleWithNameAndRole().apply {
                entityRoleRole = newRole
                entityRoleScopeName = "Role A @ School A"
                erGroupUid = 0
                erEntityUid = schoolA.schoolUid
                erTableId = School.TABLE_ID
                erActive = true
            })


        }.run {

            PersonEditScreen {


                launchFragment(false, leftOutPassword = true, leftOutUsername = true,
                        fillForm = true, entityRoles = entityRoles,
                        serverUrl = serverUrl, systemImplNavRule = systemImplNavRule,
                        impl = impl, context = context)

                scrollToBottom()
                rolesList {
                    isDisplayed()
                }
                roleHeaderTextView {
                    isDisplayed()
                }

                val allPeople = dbRule.db.personDao.getAllPerson()
                Assert.assertTrue(allPeople.isNotEmpty())
            }

        }

    }

    // TODO roles have changed here, need to update
    @AdbScreenRecord("given person edit for existing opened in normal mode and admin, " +
            "roles and permissions should be shown")
    //@Test
    fun givenPersonEditOpenedDoeExistingAsAdmin_whenInNoRegistrationMode_thenRolesShouldBeShown() {

        var person: Person? = null
        var entityRoles: List<EntityRoleWithNameAndRole> = listOf()
        init {

            val newRole = Role().apply {
                roleName = "Role A"
                roleUid = dbRule.db.roleDao.insert(this)
            }
            val schoolA = School().apply {
                schoolName = "School A"
                schoolActive = true
                schoolUid = dbRule.db.schoolDao.insert(this)
            }
            entityRoles = listOf(EntityRoleWithNameAndRole().apply {
                entityRoleRole = newRole
                entityRoleScopeName = "School A"
                erGroupUid = 0
                erEntityUid = schoolA.schoolUid
                erTableId = School.TABLE_ID
                erActive = true
            })

            person = Person().apply {
                firstNames = "Person"
                lastName = "One"
                active = true
                admin = false
                personUid = dbRule.db.insertPersonOnlyAndGroup(this).personUid
            }


        }.run {
            PersonEditScreen {

                launchFragment(false, leftOutPassword = true, leftOutUsername = true,
                        fillForm = false, entityRoles = entityRoles, personUid = person!!.personUid,
                        serverUrl = serverUrl, systemImplNavRule = systemImplNavRule,
                        impl = impl, context = context)


                scrollToBottom()
                rolesList {
                    isDisplayed()
                }
                roleHeaderTextView {
                    isDisplayed()
                }

                val allPeople = dbRule.db.personDao.getAllPerson()

                Assert.assertTrue(allPeople.isNotEmpty())
                GlobalScope.launch {
                    val savedRoles = dbRule.db.entityRoleDao.filterByPersonWithExtraAsList(
                            person!!.personGroupUid)
                    Assert.assertTrue(savedRoles.isNotEmpty())
                }
            }

        }


    }

    @AdbScreenRecord("given person edit opened in normal mode username and password " +
            "should be hidden")
    @Test
    fun givenPersonEditOpened_whenInNoRegistrationMode_thenUsernameAndPasswordShouldBeHidden() {

        init {

        }.run {
            PersonEditScreen {
                launchFragment(false, fillForm = false, serverUrl = serverUrl, systemImplNavRule = systemImplNavRule,
                        impl = impl, context = context)

                usernameTextInput {
                    isNotDisplayed()
                }
                passwordTextInput {
                    isNotDisplayed()
                }
                confirmPassTextInput {
                    isNotDisplayed()
                }
            }
        }


    }

    @AdbScreenRecord("given person edit opened in registration mode classes should be hidden")
    @Test
    fun givenPersonEditOpened_whenInRegistrationMode_thenClassesShouldBeHidden() {

        init {

        }.run {
            PersonEditScreen {

                launchFragment(true, fillForm = false, serverUrl = serverUrl, systemImplNavRule = systemImplNavRule,
                        impl = impl, context = context)

                scrollToBottom()
                clazzListRecyclerView {
                    isNotDisplayed()
                }
                clazzListHeaderTextView {
                    isNotDisplayed()
                }
            }
        }
    }

    @AdbScreenRecord("given person edit opened in registration mode when username and " +
            "password are not filled and save is clicked should show errors")
    @Test
    fun givenPersonEditOpenedInRegistrationMode_whenUserNameAndPasswordAreNotFilled_thenShouldShowErrors() {

        init {

        }.run {
            PersonEditScreen {

                launchFragment(registrationMode = true, leftOutPassword = true, leftOutUsername = true,
                        serverUrl = serverUrl, systemImplNavRule = systemImplNavRule,
                        impl = impl, context = context)


                scrollToBottom()
                usernameTextInput {
                    hasInputLayoutError(context.getString(R.string.field_required_prompt))
                }
                passwordTextInput {
                    hasInputLayoutError(context.getString(R.string.field_required_prompt))
                }
            }
        }

    }

    @AdbScreenRecord("given person edit opened in registration mode when dateOfBirth " +
            "is not filled and save is clicked should show errors")
    @Test
    fun givenPersonEditOpenedInRegistrationMode_whenDateOfBirthAreNotFilled_thenShouldShowErrors() {

        init {


        }.run {
            PersonEditScreen {


                launchFragment(registrationMode = true, leftOutDateOfBirth = true, serverUrl = serverUrl, systemImplNavRule = systemImplNavRule,
                        impl = impl, context = context)


                birthdayTextInput {
                    hasInputLayoutError(context.getString(R.string.field_required_prompt))
                }
            }
        }
    }

    @AdbScreenRecord("given person edit opened in registration mode when dateOfBirth " +
            "is less than 13 years of age and save is clicked should show errors")
    @Test
    fun givenPersonEditOpenedInRegistrationMode_whenDateOfBirthIsLessThan13YearsOfAge_thenShouldShowErrors() {

        init {

        }.run {
            PersonEditScreen {

                launchFragment(registrationMode = true,
                        selectedDateOfBirth = DateTime(2010, 10, 24).unixMillisLong, serverUrl = serverUrl, systemImplNavRule = systemImplNavRule,
                        impl = impl, context = context)

                birthdayTextInput {
                    hasInputLayoutError(context.getString(R.string.underRegistrationAgeError))
                }
            }
        }
    }


    @AdbScreenRecord("given person edit opened in registration mode when password " +
            "doesn't match and save is clicked should show errors")
    @Test
    fun givenPersonEditOpenedInRegistrationMode_whenPasswordDoNotMatch_thenShouldShowErrors() {

        init {

        }.run {
            PersonEditScreen {

                launchFragment(registrationMode = true, misMatchPassword = true, serverUrl = serverUrl, systemImplNavRule = systemImplNavRule,
                        impl = impl, context = context)

                scrollToBottom()
                passwordTextInput {
                    hasInputLayoutError(context.getString(R.string.filed_password_no_match))
                }
                confirmPassTextInput {
                    hasInputLayoutError(context.getString(R.string.filed_password_no_match))
                }
            }
        }
    }

    @AdbScreenRecord("given person edit opened in registration mode when try to register " +
            "existing person should show errors")
    @Test
    fun givenPersonEditOpenedInRegistrationMode_whenTryToRegisterExistingPerson_thenShouldShowErrors() {


        init {
            mockWebServer.enqueue(MockResponse().setResponseCode(409))


        }.run {

            PersonEditScreen {

                launchFragment(registrationMode = true, misMatchPassword = false, leftOutUsername = false, serverUrl = serverUrl, systemImplNavRule = systemImplNavRule,
                        impl = impl, context = context)

                scrollToBottom()
                usernameTextInput {
                    hasInputLayoutError(context.getString(R.string.person_exists))
                }
            }

        }
    }


}
