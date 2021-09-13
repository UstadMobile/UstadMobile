package com.ustadmobile.core.db

import com.ustadmobile.door.DoorDatabaseCallback
import com.ustadmobile.door.DoorDbType
import com.ustadmobile.door.DoorSqlDatabase
import com.ustadmobile.door.ext.dbType
import com.ustadmobile.door.ext.execSqlBatch

/**
 * These triggers manage recursive progress tracking for ContentJobItem. Each ContentJobItem
 * can have multiple children, e.g.
 *
 * ContentJobItem (parent)
 *  - ContentJobItem (child)
 *  - ContentJobItem (child)
 *    - ContentJobItem (grandchild)
 *
 * A ContentJobItem can contain thousands of child items. Normally if the user wants to see the
 * progress of the parent item, this would create a complex recursive query (infeasible to use
 * to display regular progress).
 *
 * The recursive trigger system works by posting progress up the tree when an update occurs.
 */
class ContentJobItemTriggersCallback: DoorDatabaseCallback {

    override fun onCreate(db: DoorSqlDatabase) {
        if(db.dbType() == DoorDbType.SQLITE) {
            db.execSqlBatch(arrayOf(
                """
                CREATE TRIGGER ContentJobItem_InsertTrigger 
                AFTER INSERT ON ContentJobItem
                BEGIN
                UPDATE ContentJobItem 
                   SET cjiRecursiveProgress = NEW.cjiItemProgress,
                       cjiRecursiveTotal = NEW.cjiItemTotal
                WHERE ContentJobItem.cjiUid = NEW.cjiUid;
                END;
                """,
                """
                CREATE TRIGGER ContentJobItem_UpdateRecursiveTotals 
                AFTER UPDATE ON ContentJobItem
                FOR EACH ROW WHEN (
                    NEW.cjiItemProgress != OLD.cjiItemProgress
                        OR NEW.cjiItemTotal != OLD.cjiItemTotal)
                BEGIN
                UPDATE ContentJobItem 
                   SET cjiRecursiveProgress = (cjiRecursiveProgress + (NEW.cjiItemProgress - OLD.cjiItemProgress)),
                       cjiRecursiveTotal = (cjiRecursiveTotal + (NEW.cjiItemTotal - OLD.cjiItemTotal))
                 WHERE ContentJobItem.cjiUid = NEW.cjiUid;
                END;
                """,
                """
                CREATE TRIGGER ContentJobItem_UpdateParents
                AFTER UPDATE ON ContentJobItem
                FOR EACH ROW WHEN (
                        NEW.cjiParentCjiUid != 0 
                    AND (NEW.cjiRecursiveProgress != OLD.cjiRecursiveProgress
                         OR NEW.cjiRecursiveTotal != OLD.cjiRecursiveTotal))
                BEGIN
                UPDATE ContentJobItem 
                   SET cjiRecursiveProgress = (cjiRecursiveProgress + (NEW.cjiRecursiveProgress - OLD.cjiRecursiveProgress)),
                       cjiRecursiveTotal = (cjiRecursiveTotal + (NEW.cjiRecursiveTotal - OLD.cjiRecursiveTotal))
                 WHERE ContentJobItem.cjiUid = NEW.cjiParentCjiUid;
                END;
                """
            ))
        }

    }

    override fun onOpen(db: DoorSqlDatabase) {

    }

}