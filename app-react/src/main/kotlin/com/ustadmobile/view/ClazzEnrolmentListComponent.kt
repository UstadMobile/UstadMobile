package com.ustadmobile.view

import com.ustadmobile.core.controller.ClazzEnrolmentListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.outcomeToString
import com.ustadmobile.core.util.ext.personFullName
import com.ustadmobile.core.util.ext.roleToString
import com.ustadmobile.core.view.ClazzEnrolmentListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.mui.components.GridSize
import com.ustadmobile.mui.components.GridSpacing
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.format
import com.ustadmobile.util.ext.standardFormat
import com.ustadmobile.util.ext.toDate
import com.ustadmobile.view.ext.*
import mui.material.styles.TypographyVariant
import react.RBuilder
import react.setState

class ClazzEnrolmentListComponent (props: UmProps): UstadListComponent<ClazzEnrolment,
        ClazzEnrolmentWithLeavingReason>(props), ClazzEnrolmentListView {

    private var mPresenter: ClazzEnrolmentListPresenter? = null

    override val displayTypeRepo: Any?
        get() = dbRepo?.clazzEnrolmentDao

    private var selectedPersonUid: Long = 0

    private var headerText = ""

    override val listPresenter: UstadListPresenter<*, in ClazzEnrolmentWithLeavingReason>?
        get() = mPresenter


    override var person: Person? = null
        get() = field
        set(value) {
            field = value
            ustadComponentTitle = value?.personFullName()
        }

    override var clazz: Clazz? = null
        get() = field
        set(value) {
            field = value
            val personInClazzStr = getString(MessageID.person_enrolment_in_class)
                .format(person?.personFullName() ?: "", value?.clazzName ?: "")

            setState {
                headerText = personInClazzStr
            }
        }


    override var enrolmentList: DataSourceFactory<Int, ClazzEnrolmentWithLeavingReason>? = null
        set(value) {
            field = value
            setState {
                list = value
            }
        }

    override var isStudentEnrolmentEditVisible: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var isTeacherEnrolmentEditVisible: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override fun handleClickEntry(entry: ClazzEnrolmentWithLeavingReason) {}

    override fun onCreateView() {
        super.onCreateView()
        fabManager?.visible = false
        selectedPersonUid = arguments[UstadView.ARG_PERSON_UID]?.toLong() ?: 0
        mPresenter = ClazzEnrolmentListPresenter(this, arguments, this,di,this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.renderListHeaderView() {
        umGridContainer(rowSpacing = GridSpacing.spacing2){
            umItem(GridSize.cells12) {
                renderTopMainAction("person",
                    getString(MessageID.view_profile),
                    GridSize.cells6,
                    GridSize.cells2,true){
                    mPresenter?.handleClickProfile(selectedPersonUid)
                }
            }

            umItem (GridSize.cells12){
                renderListSectionTitle(headerText, TypographyVariant.h6)
            }
        }
    }

    override fun RBuilder.renderListItem(item: ClazzEnrolmentWithLeavingReason) {
        umGridContainer {
            val startEndTime = "${item.clazzEnrolmentDateJoined.toDate()?.standardFormat()} " +
                    "- ${item.clazzEnrolmentDateLeft.toDate(true)?.standardFormat() ?: getString(MessageID.present)}"
            renderListItemWithTitleDescriptionAndRightAction(
                title = "${item.roleToString(this, systemImpl)} " +
                        "- ${item.outcomeToString(this, systemImpl)}",
                iconName = "edit",
                withAction = isTeacherEnrolmentEditVisible || isStudentEnrolmentEditVisible,
                description = startEndTime){
                mPresenter?.handleClickClazzEnrolment(item)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
    }
}