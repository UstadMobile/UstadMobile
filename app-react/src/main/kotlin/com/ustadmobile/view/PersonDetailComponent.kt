package com.ustadmobile.view

import androidx.paging.DataSource
import com.ccfraser.muirwik.components.*
import com.ustadmobile.core.controller.PersonDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithClazzAndAttendance
import com.ustadmobile.lib.db.entities.EntityRoleWithNameAndRole
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.util.CssStyleManager
import com.ustadmobile.util.CssStyleManager.personDetailComponentActionIcon
import com.ustadmobile.util.CssStyleManager.personDetailComponentActions
import com.ustadmobile.util.CssStyleManager.personDetailComponentContainer
import com.ustadmobile.util.CssStyleManager.personDetailComponentInfo
import com.ustadmobile.util.RouteManager.getArgs
import com.ustadmobile.view.ext.handleCall
import com.ustadmobile.view.ext.handleMail
import com.ustadmobile.view.ext.handleSMS
import kotlinx.css.Display
import kotlinx.css.display
import org.w3c.dom.events.Event
import react.RBuilder
import react.RProps
import react.setState
import styled.css
import styled.styledDiv

class PersonDetailComponent(mProps: RProps): UstadDetailComponent<PersonWithDisplayDetails>(mProps), PersonDetailView {

    private lateinit var mPresenter: PersonDetailPresenter

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override val viewName: String
        get() = PersonDetailView.VIEW_NAME

    override fun onComponentReady() {
        mPresenter = PersonDetailPresenter(this,getArgs(),this,di,this)
        mPresenter.onCreate(mapOf())
    }

    override var clazzes: DataSource.Factory<Int, ClazzEnrolmentWithClazzAndAttendance>? = null
        get() = field
        set(value) {
            field = value
        }

    override var changePasswordVisible: Boolean = false
        get() = field
        set(value) {
            setState { field = value}
        }

    override var showCreateAccountVisible: Boolean = false
        get() = field
        set(value) {
            setState { field = value}
        }

    override var rolesAndPermissions: DataSource.Factory<Int, EntityRoleWithNameAndRole>? = null
        get() = field
        set(value) {
            field = value
        }

    override var entity: PersonWithDisplayDetails? = null
        get() = field
        set(value) {
            field = value
        }

    override fun RBuilder.render() {
        styledDiv {
            css{+personDetailComponentContainer}
            mGridContainer(spacing= MGridSpacing.spacing9){

                mGridItem {
                    css{display = if(entity?.phoneNum != null) Display.flex else Display.none}
                    mPaper(variant = MPaperVariant.elevation) {
                        attrs {
                            onClick = { handleCall(entity?.phoneNum) }
                        }
                        css { +personDetailComponentActions }
                        mIcon("call"){
                            css{+personDetailComponentActionIcon}
                        }
                        mTypography(
                            systemImpl.getString(MessageID.call, this),
                            variant = MTypographyVariant.body1, gutterBottom = true){
                            css(CssStyleManager.alignTextToStart)
                        }
                    }
                }

                mGridItem {
                    css{display = if(entity?.phoneNum != null) Display.flex else Display.none}
                    mPaper(variant = MPaperVariant.elevation) {
                        attrs {
                            onClick = { handleSMS(entity?.phoneNum) }
                        }
                        css { +personDetailComponentActions }
                        mIcon("message"){
                            css{+personDetailComponentActionIcon}
                        }
                        mTypography(
                            systemImpl.getString(MessageID.text, this),
                            variant = MTypographyVariant.body1, gutterBottom = true){
                            css(CssStyleManager.alignTextToStart)
                        }
                    }
                }

                mGridItem {
                    css{display = if(entity?.emailAddr != null) Display.flex else Display.none}
                    mPaper(variant = MPaperVariant.elevation) {
                        attrs {
                            onClick = { handleMail(entity?.emailAddr) }
                        }
                        css { +personDetailComponentActions }
                        mIcon("email"){
                            css{+personDetailComponentActionIcon}
                        }
                        mTypography(
                            systemImpl.getString(MessageID.email, this),
                            variant = MTypographyVariant.body1, gutterBottom = true){
                            css(CssStyleManager.alignTextToStart)
                        }
                    }
                }


                mGridItem {
                    css{display = if(changePasswordVisible) Display.flex else Display.none}
                    mPaper(variant = MPaperVariant.elevation) {
                        attrs {
                            onClick = { mPresenter?.handleChangePassword() }
                        }
                        css { +personDetailComponentActions }
                        mIcon("vpn_key"){
                            css{+personDetailComponentActionIcon}
                        }
                        mTypography(
                            systemImpl.getString(MessageID.change_password, this),
                            variant = MTypographyVariant.body1, gutterBottom = true){
                            css(CssStyleManager.alignTextToStart)
                        }
                    }
                }

                mGridItem {
                    css{display = if(showCreateAccountVisible) Display.flex else Display.none}
                    mPaper(variant = MPaperVariant.elevation) {
                        attrs {
                            onClick = { mPresenter?.handleCreateAccount() }
                        }
                        css { +personDetailComponentActions }
                        mIcon("person_add"){
                            css{+personDetailComponentActionIcon}
                        }
                        mTypography(
                            systemImpl.getString(MessageID.create_account, this),
                            variant = MTypographyVariant.body1, gutterBottom = true){
                            css(CssStyleManager.alignTextToStart)
                        }
                    }
                }
            }
            styledDiv {
                css { +personDetailComponentInfo}

                +"hello there"
            }
        }
    }

}