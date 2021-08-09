package com.ustadmobile.mocks.db

import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.core.db.dao.ReportDao
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorQuery
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.mocks.DoorLiveDataJs

class ReportDaoJs: ReportDao() {
    override fun getResults(query: DoorQuery): List<Report> {
        TODO("Not yet implemented")
    }

    override fun findAllActiveReport(
        searchBit: String,
        personUid: Long,
        sortOrder: Int,
        isTemplate: Boolean
    ): DoorDataSourceFactory<Int, Report> {
        return DataSourceFactoryJs(ENTRIES.filter { it.isTemplate == isTemplate })
    }

    override suspend fun findByUid(entityUid: Long): Report? {
        return  ENTRIES.first { it.reportUid == entityUid }
    }

    override suspend fun updateAsync(entity: Report) {
        TODO("Not yet implemented")
    }

    override fun findByUidLive(uid: Long): DoorLiveData<Report?> {
        return DoorLiveDataJs(ENTRIES.first { it.reportUid == uid })
    }

    override fun findAllActiveReportLive(isTemplate: Boolean): DoorLiveData<List<Report>> {
        return DoorLiveDataJs(ENTRIES.filter { it.isTemplate == isTemplate })
    }

    override fun findAllActiveReportList(isTemplate: Boolean): List<Report> {
        return ENTRIES.filter { it.isTemplate == isTemplate }
    }

    override fun updateReportInactive(inactive: Boolean, uid: Long) {
        TODO("Not yet implemented")
    }

    override fun findByUidList(uidList: List<Long>): List<Long> {
        return ENTRIES.filter { uidList.indexOf(it.reportUid) != -1}.map { it.reportUid }
    }

    override suspend fun toggleVisibilityReportItems(
        toggleVisibility: Boolean,
        selectedItem: List<Long>) {}

    override fun replaceList(entityList: List<Report>) {
        TODO("Not yet implemented")
    }

    override fun insert(entity: Report): Long {
        TODO("Not yet implemented")
    }

    override suspend fun insertAsync(entity: Report): Long {
        TODO("Not yet implemented")
    }

    override fun insertList(entityList: List<Report>) {
        TODO("Not yet implemented")
    }

    override fun updateList(entityList: List<Report>) {
        TODO("Not yet implemented")
    }

    override fun update(entity: Report) {
        TODO("Not yet implemented")
    }

    companion object {
        val ENTRIES = listOf(
            Report().apply {
                reportUid = 1
                reportOwnerUid = PersonDaoJs.ENTRIES[0].personUid
            }
        )
    }
}