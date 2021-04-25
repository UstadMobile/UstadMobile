package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.ClazzAssignment


interface ClazzAssignmentListItemListener {

    fun onClickAssignment(clazzAssignment: ClazzAssignment)

}