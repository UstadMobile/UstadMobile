package com.ustadmobile.port.android.screen

import android.content.Context
import androidx.fragment.app.testing.FragmentScenario
import androidx.navigation.fragment.findNavController
import com.agoda.kakao.edit.KTextInputLayout
import com.kaspersky.kaspresso.screens.KScreen
import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.SiteEditFragment
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultGson

object WorkSpaceEditScreen : KScreen<WorkSpaceEditScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_report_edit
    override val viewClass: Class<*>?
        get() = SiteEditFragment::class.java

    val WorkSpaceTitleInput = KTextInputLayout { withId(R.id.id_of_textfield_input_layout)}


    fun fillFields(fragmentScenario: FragmentScenario<SiteEditFragment>,
                   workSpace: WorkSpace,
                   workSpaceOnForm: WorkSpace?,
                   setFieldsRequiringNavigation: Boolean = true,
                   impl: UstadMobileSystemImpl, context: Context,
                   testContext: TestContext<Unit>) {
        //TODO: set these values on the form using Espresso.

        workSpace.workSpaceName?.takeIf {it != workSpaceOnForm?.workSpaceName }?.also {
            WorkSpaceTitleInput{
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
            fragment.takeIf {workSpace.relatedEntity != workSpaceOnForm?.relatedEntity }
                    ?.findNavController()?.currentBackStackEntry?.savedStateHandle
                    ?.set("RelatedEntityName", defaultGson().toJson(listOf(workSpace.relatedEntity)))
        }

    }


}