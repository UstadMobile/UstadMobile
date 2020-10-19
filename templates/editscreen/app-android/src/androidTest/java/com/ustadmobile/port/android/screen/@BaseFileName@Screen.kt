package com.ustadmobile.port.android.screen

import android.content.Context
import androidx.fragment.app.testing.FragmentScenario
import androidx.navigation.fragment.findNavController
import com.agoda.kakao.common.views.KSwipeView
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.edit.KTextInputLayout
import com.agoda.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ReportEditPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultGson
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.ReportWithFilters
import com.ustadmobile.lib.db.entities.VerbDisplay
import com.ustadmobile.port.android.view.ReportEditFragment
import com.ustadmobile.test.port.android.KNestedScrollView
import com.ustadmobile.test.port.android.util.setDateField
import com.ustadmobile.test.port.android.util.setMessageIdOption

object @BaseFileName@Screen : KScreen<@BaseFileName@Screen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_report_edit
    override val viewClass: Class<*>?
        get() = @BaseFileName@Fragment::class.java

    val @Entity@TitleInput = KTextInputLayout { withId(R.id.id_of_textfield_input_layout)}


    fun fillFields(fragmentScenario: FragmentScenario<@BaseFileName@Fragment>,
                   @Entity_VariableName@: @EditEntity@,
                   @Entity_VariableName@OnForm: @EditEntity@?,
                   setFieldsRequiringNavigation: Boolean = true,
                   impl: UstadMobileSystemImpl, context: Context,
                   testContext: TestContext<Unit>) {
        //TODO: set these values on the form using Espresso.

        @Entity_VariableName@.@Entity_VariableName@Name?.takeIf {it != @Entity_VariableName@OnForm?.@Entity_VariableName@Name }?.also {
            @Entity@TitleInput{
                edit{
                    clearText()
                    typeText(it)
                }
            }
        }

        if(!setFieldsRequiringNavigation) {
            return
        }

        //TODO: if required, use the savedstatehandle to add link entities

        fragmentScenario.onFragment { fragment ->
            fragment.takeIf {@Entity_VariableName@.relatedEntity != @Entity_VariableName@OnForm?.relatedEntity }
                    ?.findNavController()?.currentBackStackEntry?.savedStateHandle
                    ?.set("RelatedEntityName", defaultGson().toJson(listOf(@Entity_VariableName@.relatedEntity)))
        }

    }


}