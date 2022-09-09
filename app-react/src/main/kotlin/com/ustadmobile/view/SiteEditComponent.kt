package com.ustadmobile.view

import com.ustadmobile.core.controller.SiteEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.SiteEditView
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage
import com.ustadmobile.mui.components.GridSize
import com.ustadmobile.mui.components.umTextField
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.view.ext.renderListSectionTitle
import com.ustadmobile.view.ext.umItem
import mui.material.FormControlVariant
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class SiteEditComponent(props: UmProps): UstadEditComponent<Site>(props), SiteEditView {

    private var mPresenter: SiteEditPresenter? = null

    private var siteTermsWithLanguageList: List<SiteTermsWithLanguage> = listOf()

    override val mEditPresenter: UstadEditPresenter<*, Site>?
        get() = mPresenter

    private val siteTermsObserver = ObserverFnWrapper<List<SiteTermsWithLanguage>>{
        if(it.isEmpty()) return@ObserverFnWrapper
        setState {
            siteTermsWithLanguageList = it
        }
    }

    override var siteTermsList: LiveData<List<SiteTermsWithLanguage>>? = null
        set(value) {
            value?.removeObserver(siteTermsObserver)
            value?.observe(this, siteTermsObserver)
            field = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }


    override var entity: Site? = null
        get() = field
        set(value) {
            setState {
                field = value
                ustadComponentTitle = value?.siteName
            }
        }

    private var nameLabel = FieldLabel(text = getString(MessageID.name))

    override fun onCreateView() {
        super.onCreateView()
        mPresenter = SiteEditPresenter(this, arguments, this, this, di)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                +StyleManager.contentContainer
                +StyleManager.defaultPaddingTop
            }


            umItem(GridSize.cells12){
                umTextField(label = "${nameLabel.text}",
                    helperText = nameLabel.errorText,
                    value = entity?.siteName,
                    error = nameLabel.error,
                    disabled = !fieldsEnabled,
                    variant = FormControlVariant.outlined,
                    onChange = {
                        setState {
                            entity?.siteName = it
                        }
                    })
            }

            createSwitchItem(getString(MessageID.guest_login_enabled),
                entity?.guestLogin == true){
                setState {
                    entity?.guestLogin = !(entity?.guestLogin ?: false)
                }
            }

            createSwitchItem(getString(MessageID.registration_allowed),
                entity?.registrationAllowed == true){
                setState {
                    entity?.registrationAllowed = !(entity?.registrationAllowed ?: false)
                }
            }

            umItem(GridSize.cells12){

                renderListSectionTitle(getString(MessageID.terms_and_policies))

                val newItem = CreateNewItem(true, getString(MessageID.terms_and_policies)){
                    mPresenter?.siteTermsOneToManyJoinListener?.onClickNew()
                }

                renderSiteTerms(
                    mPresenter?.siteTermsOneToManyJoinListener,
                    siteTermsWithLanguageList,
                    createNewItem = newItem,
                    withDelete = true){
                    mPresenter?.siteTermsOneToManyJoinListener?.onClickEdit(it)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
    }
}