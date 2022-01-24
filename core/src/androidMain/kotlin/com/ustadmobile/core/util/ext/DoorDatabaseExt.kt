package com.ustadmobile.core.util.ext

import androidx.room.InvalidationTracker
import com.ustadmobile.door.ChangeListenerRequest
import com.ustadmobile.door.DoorDatabase


internal class ChangeListenerRequestInvalidationObserver(
    private val changeListenerRequest: ChangeListenerRequest
) : InvalidationTracker.Observer(changeListenerRequest.tableNames.toTypedArray()) {
    override fun onInvalidated(tables: MutableSet<String>) {
        changeListenerRequest.onInvalidated.onTablesInvalidated(tables.toList())
    }

    override fun equals(other: Any?): Boolean {
        return if(other is ChangeListenerRequestInvalidationObserver)
            other.changeListenerRequest == this.changeListenerRequest
        else
            false
    }

    override fun hashCode(): Int {
        return changeListenerRequest.hashCode()
    }
}

actual fun DoorDatabase.addInvalidationListener(changeListenerRequest: ChangeListenerRequest) {
    invalidationTracker.addObserver(ChangeListenerRequestInvalidationObserver(changeListenerRequest))
}

actual fun DoorDatabase.removeInvalidationListener(changeListenerRequest: ChangeListenerRequest) {
    invalidationTracker.removeObserver(ChangeListenerRequestInvalidationObserver(changeListenerRequest))
}
