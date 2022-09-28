package com.ustadmobile.view

import com.ustadmobile.core.controller.CourseGroupSetListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.CourseGroupSetListView
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.mui.components.umList
import com.ustadmobile.mui.components.umListItem
import com.ustadmobile.util.StyleManager.horizontalList
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.Util.stopEventPropagation
import com.ustadmobile.view.ext.renderListItemWithLeftIconTitleAndDescription
import mui.material.styles.TypographyVariant
import react.RBuilder
import react.setState
import styled.css

class CourseGroupSetListComponent(mProps: UmProps): UstadListComponent<CourseGroupSet, CourseGroupSet>(mProps) ,
    CourseGroupSetListView {

    private var mPresenter: CourseGroupSetListPresenter? = null

    override val displayTypeRepo: Any?
        get() = dbRepo?.courseGroupSetDao

    override val listPresenter: UstadListPresenter<*, in CourseGroupSet>?
        get() = mPresenter

    override var individualList: List<CourseGroupSet>? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        ustadComponentTitle = getString(MessageID.groups)
        showCreateNewItem = true
        addNewEntryText = getString(MessageID.add_new_groups)
        fabManager?.text = getString(MessageID.groups)
        mPresenter = CourseGroupSetListPresenter(this, arguments,
            this, di, this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.renderListJoinView() {
        umList {
            css(horizontalList)
            individualList?.forEach { group ->
                umListItem {
                    attrs.onClick = {
                        stopEventPropagation(it)
                        handleClickEntry(group)
                    }
                    renderListItemWithLeftIconTitleAndDescription("groups", group.cgsName,
                        titleVariant = TypographyVariant.h6,
                        onMainList = true)
                }
            }
        }
    }

    override fun RBuilder.renderListItem(item: CourseGroupSet) {
        renderListItemWithLeftIconTitleAndDescription("groups", item.cgsName,
            titleVariant = TypographyVariant.h6,
            onMainList = true)
    }

    override fun handleClickEntry(entry: CourseGroupSet) {
        mPresenter?.handleClickEntry(entry)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
    }
}