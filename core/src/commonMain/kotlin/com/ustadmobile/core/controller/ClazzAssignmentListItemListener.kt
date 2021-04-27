package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics


interface ClazzAssignmentListItemListener {

    fun onClickAssignment(clazzAssignment: ClazzAssignmentWithMetrics)

}