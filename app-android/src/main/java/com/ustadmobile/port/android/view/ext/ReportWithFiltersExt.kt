package com.ustadmobile.port.android.view.ext

import android.content.Context
import com.toughra.ustadmobile.R
import com.ustadmobile.lib.db.entities.ReportWithFilters


class ReportWithFiltersExt {

    companion object {

        @JvmStatic
        fun addToDashboardIcon(report: ReportWithFilters): Int {
            return if (isNewOrInActiveReport(report)) R.drawable.fab_add else R.drawable.ic_remove_black_24dp
        }

        @JvmStatic
        fun addToDashboardText(report: ReportWithFilters, context: Context): String {
            return if (isNewOrInActiveReport(report))
                context.getString(R.string.add_to, context.getString(R.string.dashboard))
            else context.getString(R.string.remove_from, context.getString(R.string.dashboard))
        }

        @JvmStatic
        fun addToDashboardColor(report: ReportWithFilters): Int {
            return if (isNewOrInActiveReport(report)) R.color.primary else R.color.red
        }


        private fun isNewOrInActiveReport(report: ReportWithFilters): Boolean {
            return report.reportUid == 0L && !report.reportInactive
        }


    }


}
