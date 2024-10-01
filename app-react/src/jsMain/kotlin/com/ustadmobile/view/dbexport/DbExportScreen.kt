package com.ustadmobile.view.dbexport

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.components.DIContext
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.hooks.useCoroutineScope
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.rootDatabase
import com.ustadmobile.door.room.RoomJdbcImpl
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import react.FC
import react.Props
import react.useEffect
import react.useRequiredContext
import kotlinx.coroutines.launch

val DbExportScreen = FC<Props> {
    val di = useRequiredContext(DIContext)
    val coroutineScope = useCoroutineScope(dependencies = emptyArray())
    useEffect(dependencies = emptyArray()) {
        val accountManager: UstadAccountManager = di.direct.instance()
        val activeDb: UmAppDatabase = di.direct.on(accountManager.activeLearningSpace)
            .instance(tag = DoorTag.TAG_DB)

        coroutineScope.launch {
            (activeDb.rootDatabase as RoomJdbcImpl).jdbcImplHelper.exportToFile()
        }
    }





}
