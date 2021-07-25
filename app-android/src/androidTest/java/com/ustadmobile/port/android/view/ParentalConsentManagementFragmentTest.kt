package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import com.google.gson.Gson
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.SiteTerms
import com.ustadmobile.port.android.screen.ParentalConsentManagementScreen
import com.ustadmobile.port.android.util.ext.waitUntil2Blocking
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.UmViewActions.hasInputLayoutError
import com.ustadmobile.test.port.android.util.*
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.direct
import org.kodein.di.instance

@AdbScreenRecord("ParentAccountLanding screen Test")
class ParentalConsentManagementFragmentTest : TestCase(){

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @JvmField
    @Rule
    val dataBindingIdlingResourceRule = ScenarioIdlingResourceRule(DataBindingIdlingResource())

    @JvmField
    @Rule
    val crudIdlingResourceRule = ScenarioIdlingResourceRule(CrudIdlingResource())


    lateinit var parent: Person

    lateinit var childPerson: Person

    lateinit var existingPersonParentJoin: PersonParentJoin

    @Before
    fun setup() {
        parent = Person().apply {
            firstNames = "Pit"
            lastName = "The Older"
            username = "olderpit"
        }

        dbRule.insertPersonAndStartSession(parent)

        runBlocking {
            childPerson = dbRule.repo.insertPersonAndGroup(Person().apply {
                firstNames = "Pit"
                lastName = "The Younger"
                dateOfBirth = System.currentTimeMillis() - (10 * 365 * 24 * 60 * 60 * 100L)
                gender = Person.GENDER_MALE
                //personUid = dbRule.repo.personDao.insert(this)
            })


            existingPersonParentJoin = PersonParentJoin().apply {
                ppjMinorPersonUid = childPerson.personUid
                ppjUid = runBlocking { dbRule.repo.personParentJoinDao.insertAsync(this@apply) }
            }
        }



        runBlocking {
            dbRule.repo.siteTermsDao.insertAsync(SiteTerms().apply {
                termsHtml = "<div id='terms'>All your bases are belong to us</div>"
                sTermsLang = "en"
            })
        }

    }

    private fun launchParentAccountLandingFragmentInContainer() : FragmentScenario<ParentalConsentManagementFragment> {
        return launchFragmentInContainer(themeResId = R.style.UmTheme_App,
            fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to existingPersonParentJoin.ppjUid)) {
            ParentalConsentManagementFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
            .withScenarioIdlingResourceRule(crudIdlingResourceRule)
    }

    private fun waitUntilPersonParentJoinBlocking(block: (PersonParentJoin?) -> Boolean) : PersonParentJoin? {
        return dbRule.db.waitUntil2Blocking(setOf("PersonParentJoin"), 5000,
            {dbRule.db.personParentJoinDao.findByUidWithMinorAsync(existingPersonParentJoin.ppjUid)},
            block)
    }

    @AdbScreenRecord("Given Existing Person - Parent Join request, when I consent button clicked, then should be saved")
    @Test
    fun givenPersonParentJoinExists_whenIConsentClicked_thenShouldBeSavedWithConsentGranted() {
        lateinit var fragmentScenario: FragmentScenario<ParentalConsentManagementFragment>

        //Freeze and serialize the value as it was first shown to the user
        val gson: Gson = getApplicationDi().direct.instance()

        init{
            fragmentScenario = launchParentAccountLandingFragmentInContainer()
        }.run{
            val entityLoadedByFragment = fragmentScenario.waitUntilNotNullOnFragmentBlocking(5000) { it.entity }

            ParentalConsentManagementScreen {
                relationshipTextInput {
                    setMessageIdOption("Father")
                }

                consentButton {
                    scrollTo()
                    click()
                }

                val resultSaved = waitUntilPersonParentJoinBlocking {
                        it?.ppjParentPersonUid != 0L
                }

                Assert.assertEquals("Got expected person",
                    "The Younger", entityLoadedByFragment.minorPerson?.lastName )

                Assert.assertEquals("After clicking to consent, consent is marked as granted",
                    PersonParentJoin.STATUS_APPROVED, resultSaved?.ppjStatus)
            }

        }
    }

    @Test
    fun givenExistingParentPersonJoin_whenRoleNotSelected_thenShouldShowFieldRequiredMessage() {
        lateinit var fragmentScenario: FragmentScenario<ParentalConsentManagementFragment>

        init{
            fragmentScenario = launchParentAccountLandingFragmentInContainer()
        }.run {
            val entityLoadedByFragment =
                fragmentScenario.waitUntilNotNullOnFragmentBlocking(5000) { it.entity }

            ParentalConsentManagementScreen {
                consentButton {
                    scrollTo()
                    click()
                }

                relationshipTextInput {
                    hasInputLayoutError(R.string.field_required_prompt)
                }
            }
        }
    }

    @AdbScreenRecord("Given Existing Person - Parent Join request, when I do not consent button clicked, then should be saved")
    @Test
    fun givenPersonParentJoinExists_whenIDoNotConsentClicked_thenShouldBeSavedWithConsentNotGranted() {
        lateinit var fragmentScenario: FragmentScenario<ParentalConsentManagementFragment>

        init{
            fragmentScenario = launchParentAccountLandingFragmentInContainer()
        }.run{
            ParentalConsentManagementScreen {
                relationshipTextInput {
                    setMessageIdOption("Father")
                }

                dontConsentButton{
                    scrollTo()
                    click()
                }

                val resultSaved = waitUntilPersonParentJoinBlocking {
                    it?.ppjParentPersonUid != 0L
                }

                Assert.assertEquals("After clicking do not consent, status is marked as rejected",
                    PersonParentJoin.STATUS_REJECTED, resultSaved?.ppjStatus)
            }
        }
    }

    @AdbScreenRecord("Given existing person - parent join with parent set, revoke consent button should be visible. When clicked should revoke consent")
    @Test
    fun givenPersonParentJoinConsented_whenClickRevokeConsent_thenSHouldBeSavedWithConsentNotGranted() {
        existingPersonParentJoin.apply {
            ppjParentPersonUid = parent.personUid
            ppjRelationship = PersonParentJoin.RELATIONSHIP_MOTHER
            ppjStatus = PersonParentJoin.STATUS_APPROVED
        }

        runBlocking { dbRule.repo.personParentJoinDao.updateAsync(existingPersonParentJoin) }

        init {
            launchParentAccountLandingFragmentInContainer()
        }.run {
            ParentalConsentManagementScreen {
                relationshipTextInput{
                    isNotDisplayed()
                }

                changeConsentButton {
                    hasText(R.string.revoke_consent)
                    click()
                }
            }

            val resultSaved = waitUntilPersonParentJoinBlocking {
                it?.ppjParentPersonUid != 0L && it?.ppjStatus == PersonParentJoin.STATUS_REJECTED
            }

            Assert.assertEquals("After clicking do not consent, status is marked as rejected",
                PersonParentJoin.STATUS_REJECTED, resultSaved?.ppjStatus)
        }
    }

    @AdbScreenRecord("Given existing person - parent join with parent set that was revoked, restore consent button should be visible. When clicked should restore consent")
    @Test
    fun givenPersonParentJoinConsented_whenClickRestoreConsent_thenSHouldBeSavedWithConsentRestored() {
        existingPersonParentJoin.apply {
            ppjParentPersonUid = parent.personUid
            ppjRelationship = PersonParentJoin.RELATIONSHIP_MOTHER
            ppjApprovalTiemstamp = System.currentTimeMillis()
            ppjStatus = PersonParentJoin.STATUS_REJECTED
        }

        runBlocking { dbRule.repo.personParentJoinDao.updateAsync(existingPersonParentJoin) }

        init {
            launchParentAccountLandingFragmentInContainer()
        }.run {
            ParentalConsentManagementScreen {
                relationshipTextInput{
                    isNotDisplayed()
                }

                changeConsentButton {
                    hasText(R.string.restore_consent)
                    click()
                }
            }

            val resultSaved = waitUntilPersonParentJoinBlocking {
                it?.ppjParentPersonUid != 0L && it?.ppjStatus == PersonParentJoin.STATUS_APPROVED
            }

            Assert.assertEquals("After clicking do not consent, status is marked as rejected",
                PersonParentJoin.STATUS_APPROVED, resultSaved?.ppjStatus)
        }
    }
}