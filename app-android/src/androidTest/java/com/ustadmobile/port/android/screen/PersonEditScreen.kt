package com.ustadmobile.port.android.screen

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.fragment.findNavController
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.agoda.kakao.common.views.KSwipeView
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.edit.KTextInputLayout
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.scroll.KScrollView
import com.agoda.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.kaspersky.kaspresso.testcases.api.testcase.BaseTestCase
import com.kaspersky.kaspresso.testcases.core.testcontext.BaseTestContext
import com.soywiz.klock.DateTime
import com.toughra.ustadmobile.R
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultGson
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.EntityRoleWithNameAndRole
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.view.PersonEditFragment
import com.ustadmobile.test.port.android.util.*
import com.ustadmobile.test.rules.SystemImplTestNavHostRule

object PersonEditScreen : KScreen<PersonEditScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_person_edit
    override val viewClass: Class<*>?
        get() = PersonEditFragment::class.java

    val nestedView = KSwipeView { withId(R.id.nested_view) }

    fun scrollToBottom() {
        nestedView.swipeUp()
    }

    val clazzListRecyclerView: KRecyclerView = KRecyclerView({ withId(R.id.clazzlist_recyclerview) },
            itemTypeBuilder = {
            })

    val clazzListHeaderTextView: KTextView = KTextView { withId(R.id.clazzlist_header_textview) }

    val rolesList: KRecyclerView = KRecyclerView({ withId(R.id.roles_and_permissions_rv) },
            itemTypeBuilder = {
            })

    val roleHeaderTextView: KTextView = KTextView { withId(R.id.roles_and_permissions_header_textview) }

    val usernameTextInput = KTextInputLayout { withId(R.id.username_textinputlayout) }

    val passwordTextInput = KTextInputLayout { withId(R.id.username_textinputlayout) }

    val firstNameTextInput = KTextInputLayout { withId(R.id.firstnames_textinputlayout)}

    val lastNameTextInput = KTextInputLayout { withId(R.id.lastname_textInputLayout)}

    val confirmPassTextInput = KTextInputLayout { withId(R.id.confirm_password_textinputlayout) }

    val birthdayTextInput = KTextInputLayout { withId(R.id.birthday_textinputlayout) }

    val phoneNumberTextInput=  KTextInputLayout { withId(R.id.phonenumber_textinputlayout)}

    val emailTextInput = KTextInputLayout { withId(R.id.email_textinputlayout)}

    val addressTextInput = KTextInputLayout { withId(R.id.address_textinputlayout)}

    val genderValue = KView { withId(R.id.gender_value) }


    fun launchFragment(registrationMode: Boolean = false, misMatchPassword: Boolean = false,
                               leftOutPassword: Boolean = false, leftOutUsername: Boolean = false,
                               fillForm: Boolean = true,
                               entityRoles: List<EntityRoleWithNameAndRole> = listOf(),
                               entityRolesOnForm: List<EntityRoleWithNameAndRole>? = null,
                               personUid: Long = 0, leftOutDateOfBirth: Boolean = false,
                               selectedDateOfBirth: Long = DateTime(1990, 10, 18).unixMillisLong,
                                serverUrl: String, systemImplNavRule: SystemImplTestNavHostRule,
                                impl: UstadMobileSystemImpl, context: Any)
            : FragmentScenario<PersonEditFragment> {

        val password = "password"
        val confirmedPassword = if (misMatchPassword) "password1" else password

        var args = mapOf(PersonEditView.ARG_REGISTRATION_MODE to registrationMode.toString(),
                UstadView.ARG_SERVER_URL to serverUrl)
        if (personUid != 0L) {
            args = mapOf(PersonEditView.ARG_REGISTRATION_MODE to registrationMode.toString(),
                    UstadView.ARG_SERVER_URL to serverUrl,
                    UstadView.ARG_ENTITY_UID to personUid.toString())
        }

        val scenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = args.toBundle()) {
            PersonEditFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        //Soft keyboard tend to hide views, when try to type will throw exception so
        // instead of type we'll replace text
        if (fillForm) {

            val personOnForm = scenario.letOnFragment { it.entity }

            val person = Person().apply {
                firstNames = "Jane"
                lastName = "Doe"
                phoneNum = "00000000000"
                gender = Person.GENDER_MALE
                dateOfBirth = selectedDateOfBirth
                emailAddr = "email@dummy.com"
                personAddress = "dummy address, 101 dummy"
                if (!leftOutUsername) {
                    username = "jane.doe"
                }
            }

            person.firstNames.takeIf { it != personOnForm?.firstNames }?.also {
                firstNameTextInput{
                    edit{
                        replaceText(it)
                    }
                }
            }

            person.lastName.takeIf { it != personOnForm?.lastName }?.also {
                lastNameTextInput{
                    edit{
                        replaceText(it)
                    }
                }
            }

            person.gender.takeIf { it != personOnForm?.gender }?.also {
                setMessageIdOption(R.id.gender_value, impl.getString(MessageID.male, context))
            }

            person.phoneNum.takeIf { it != personOnForm?.phoneNum }?.also {
                //scroll
                phoneNumberTextInput{
                    edit{
                        replaceText(it)
                    }
                }
            }

            person.emailAddr.takeIf { it != personOnForm?.emailAddr }?.also {
                emailTextInput{
                    edit{
                        replaceText(it)
                    }
                }
            }

            person.personAddress.takeIf { it != personOnForm?.personAddress }?.also {
                addressTextInput{
                    edit{
                        replaceText(it)
                    }
                }
            }

            if (!leftOutDateOfBirth) {
                person.dateOfBirth.takeIf { it != personOnForm?.dateOfBirth }?.also {
                    setDateField(R.id.birthday_text, it)
                }
            }

            if (!leftOutUsername) {
                //scroll
                scrollToBottom()
                person.username.takeIf { it != personOnForm?.username }?.also {
                    Espresso.onView(withId(R.id.username_text)).perform(ViewActions.replaceText(it))
                }
            }

            if (!leftOutPassword) {
                //scroll
                scrollToBottom()
                passwordTextInput{
                    edit{
                        replaceText(password)
                    }
                }
                confirmPassTextInput{
                    edit{
                        replaceText(confirmedPassword)
                    }
                }
            }

            scenario.clickOptionMenu(R.id.menu_done)
        }

        //Add Roles and assignments

        if (entityRoles.isNotEmpty()) {
            entityRoles.filter { entityRolesOnForm == null || it !in entityRolesOnForm }.forEach { entityRole ->
                scenario.onFragment {
                    it.findNavController().currentBackStackEntry?.savedStateHandle
                            ?.set("EntityRoleWithNameAndRole", defaultGson().toJson(listOf(entityRole)))
                }
            }
        }

        return scenario
    }

}