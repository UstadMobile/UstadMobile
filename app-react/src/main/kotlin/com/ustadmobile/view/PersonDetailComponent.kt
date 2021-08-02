package com.ustadmobile.view

import androidx.paging.DataSource
import com.ccfraser.muirwik.components.*
import com.ustadmobile.core.controller.PersonConstants.GENDER_MESSAGE_ID_MAP
import com.ustadmobile.core.controller.PersonDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.outcomeToString
import com.ustadmobile.core.util.ext.roleToString
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithClazzAndAttendance
import com.ustadmobile.lib.db.entities.PersonWithPersonParentJoin
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.StyleManager.gridListSecondaryItemDesc
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultFullWidth
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.StyleManager.displayProperty
import com.ustadmobile.util.StyleManager.personDetailComponentActionIcon
import com.ustadmobile.util.StyleManager.personDetailComponentActions
import com.ustadmobile.util.ext.format
import com.ustadmobile.util.ext.standardFormat
import com.ustadmobile.view.ext.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.css.display
import kotlinx.css.marginTop
import kotlinx.css.px
import react.RBuilder
import react.RProps
import react.setState
import styled.css
import styled.styledDiv
import kotlin.js.Date

class PersonDetailComponent(mProps: RProps): UstadDetailComponent<PersonWithPersonParentJoin>(mProps),
    PersonDetailView {

    private lateinit var mPresenter: PersonDetailPresenter

    override val detailPresenter: UstadDetailPresenter<*, *>
        get() = mPresenter

    override val viewName: String
        get() = PersonDetailView.VIEW_NAME

    private var clazzList: List<ClazzEnrolmentWithClazzAndAttendance>? = null

    override var clazzes: DataSource.Factory<Int, ClazzEnrolmentWithClazzAndAttendance>? = null
        get() = field
        set(value) {
            field = value
            GlobalScope.launch {
                val data = value?.getData(0,1000)
                setState {
                    clazzList = data
                }
            }
        }

    override var changePasswordVisible: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var showCreateAccountVisible: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var entity: PersonWithPersonParentJoin? = null
        get() = field
        set(value) {
            title = value?.firstNames +" "+ value?.lastName
            setState {
                field = value
            }
        }

    override fun onCreate() {
        super.onCreate()
        mPresenter = PersonDetailPresenter(this,arguments,this,di,this)
        mPresenter.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        styledDiv {
            css{
                +contentContainer
                +defaultPaddingTop
            }
            umGridContainer {
                umItem(MGridSize.cells12){
                    umGridContainer(MGridSpacing.spacing4) {
                        createAction("call",MessageID.call, MGridSize.cells4, MGridSize.cells2,
                            entity?.phoneNum != null){
                            onClickCall(entity?.phoneNum)
                        }
                        createAction("message",MessageID.text, MGridSize.cells4, MGridSize.cells2,
                            entity?.phoneNum != null){
                            onClickSMS(entity?.phoneNum)
                        }
                        createAction("email",MessageID.email, MGridSize.cells4, MGridSize.cells2,
                            entity?.emailAddr != null){
                            onClickEmail(entity?.emailAddr)
                        }
                        createAction("vpn_key",MessageID.change_password, MGridSize.cells6, MGridSize.cells3,
                            changePasswordVisible){
                            mPresenter.handleChangePassword()
                        }
                        createAction("person_add",MessageID.create_account, MGridSize.cells6, MGridSize.cells3,
                            showCreateAccountVisible){
                            mPresenter.handleCreateAccount()
                        }
                    }
                }

                umItem(MGridSize.cells12){
                    mDivider {
                        css{
                            +defaultFullWidth
                            +defaultMarginTop
                        }
                    }
                }

                umItem(MGridSize.cells12){
                    umGridContainer(MGridSpacing.spacing6) {
                        umItem(MGridSize.cells12, MGridSize.cells4){
                            css{
                                marginTop = 12.px
                            }
                            umEntityAvatar(showIcon = false) {}
                        }

                        umItem(MGridSize.cells12, MGridSize.cells8) {
                            css(defaultMarginTop)
                            umGridContainer(MGridSpacing.spacing4){

                                umItem(MGridSize.cells12){
                                    mTypography(getString(MessageID.basic_details),
                                        variant = MTypographyVariant.caption){
                                        css(alignTextToStart)
                                    }
                                }

                                umItem(MGridSize.cells12){
                                    createInformation("event",
                                        Date(entity?.dateOfBirth ?: 0).standardFormat(),
                                        getString(MessageID.birthday))
                                    createInformation(null,
                                        getString(GENDER_MESSAGE_ID_MAP[entity?.gender] ?: 0),
                                        getString(MessageID.field_person_gender))
                                    if(!entity?.personOrgId.isNullOrBlank()){
                                        createInformation("badge", entity?.personOrgId, getString(MessageID.organization_id))
                                    }
                                    createInformation("account_circle", entity?.username, getString(MessageID.username))
                                }

                                umItem(MGridSize.cells12){
                                    mTypography(getString(MessageID.contact_details),
                                        variant = MTypographyVariant.caption){
                                        css(alignTextToStart)
                                    }
                                }

                                umItem(MGridSize.cells12){
                                    createInformation("call",entity?.phoneNum,getString(MessageID.phone_number))
                                    createInformation("email",entity?.emailAddr,getString(MessageID.email))
                                    createInformation("place",entity?.personAddress,getString(MessageID.address))
                                }


                                if(clazzList != null){

                                    umItem(MGridSize.cells12){

                                        createListSectionTitle(getString(MessageID.classes))
                                        clazzList?.let { clazzes ->
                                            child(ClazzEnrolmentWithClazzSimpleListComponent::class){
                                                attrs.entries = clazzes
                                                attrs.onEntryClicked = { clazz ->
                                                    mPresenter.handleClickClazz(clazz)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun RBuilder.createAction(icon: String, messageId: Int, xs: MGridSize,
                                      sm: MGridSize? = null, visible: Boolean = false,
                                      action:() -> Unit){
        umItem(xs, sm){
            css{
                display = displayProperty(visible, true)
            }
            mPaper(variant = MPaperVariant.elevation) {
                attrs.onClick = {
                    action()
                }
                css {
                    +personDetailComponentActions
                }
                mIcon(icon){
                    css{
                        +personDetailComponentActionIcon
                    }
                }
                mTypography(getString(messageId), variant = MTypographyVariant.body1, gutterBottom = true){
                    css(alignTextToStart)
                }
            }
        }
    }

    class ClazzEnrolmentWithClazzSimpleListComponent(mProps: ListProps<ClazzEnrolmentWithClazzAndAttendance>):
        UstadSimpleList<ListProps<ClazzEnrolmentWithClazzAndAttendance>>(mProps){

        override fun RBuilder.renderListItem(item: ClazzEnrolmentWithClazzAndAttendance) {

            val title = "${item.clazz?.clazzName} (${item.roleToString(this, systemImpl)}) " +
                    "- ${item.outcomeToString(this,  systemImpl)}"

            val enrollmentPeriod = "${Date(item.clazzEnrolmentDateJoined).standardFormat()} " +
                    "- ${Date(item.clazzEnrolmentDateLeft).standardFormat()}"


            createListItemWithAttendance("people", title, enrollmentPeriod,
                item.attendance, getString(MessageID.x_percent_attended))

        }
    }
}