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
                CREATE TRIGGER ContentJobItem_UpdateRecursiveStatus
                AFTER UPDATE ON ContentJobItem
                FOR EACH ROW WHEN (NEW.cjiStatus != OLD.cjiStatus)
                BEGIN 
                UPDATE ContentJobItem
                    SET cjiRecursiveStatus = ${statusCheck(CHILD_ID)}
                    WHERE contentJobItem.cjiUid = NEW.cjiUid;
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
                """,
                """
                CREATE TRIGGER ContentJobItem_UpdateStatusParent
                AFTER UPDATE ON ContentJobItem
                FOR EACH ROW WHEN (
                         NEW.cjiParentCjiUid != 0
                    AND (New.cjiRecursiveStatus != OLD.cjiRecursiveStatus))
                BEGIN
                UPDATE ContentJobItem
                   SET cjiRecursiveStatus = ${statusCheck(PARENT_ID)}
                 WHERE ContentJobItem.cjiUid = NEW.cjiParentCjiUid;
                 END;
                """

            ))
        }else {
            db.execSqlBatch(arrayOf(
                """
                CREATE OR REPLACE FUNCTION contentjobiteminsert_fn() RETURNS TRIGGER AS ${'$'}${'$'} 
                BEGIN
                UPDATE ContentJobItem 
                   SET cjiRecursiveProgress = NEW.cjiItemProgress,
                       cjiRecursiveTotal = NEW.cjiItemTotal
                 WHERE ContentJobItem.cjiUid = NEW.cjiUid;
                RETURN NULL; 
                END ${'$'}${'$'} LANGUAGE plpgsql
                """,
                """
                CREATE TRIGGER contentjobiteminsert_trig 
                AFTER INSERT ON ContentJobItem
                FOR EACH ROW EXECUTE PROCEDURE contentjobiteminsert_fn()    
                """,

                """
                CREATE OR REPLACE FUNCTION contentjobitem_updaterecursivetotals_fn() RETURNS TRIGGER AS ${'$'}${'$'}
                BEGIN
                UPDATE ContentJobItem 
                   SET cjiRecursiveProgress = (cjiRecursiveProgress + (NEW.cjiItemProgress - OLD.cjiItemProgress)),
                       cjiRecursiveTotal = (cjiRecursiveTotal + (NEW.cjiItemTotal - OLD.cjiItemTotal))
                 WHERE (NEW.cjiItemProgress != OLD.cjiItemProgress OR NEW.cjiItemTotal != OLD.cjiItemTotal)
                   AND ContentJobItem.cjiUid = NEW.cjiUid;
                RETURN NULL;
                END ${'$'}${'$'} LANGUAGE plpgsql
                """,
                """
                CREATE TRIGGER contentjobitem_updaterecursivetotals_trig
                AFTER UPDATE ON ContentJobItem
                FOR EACH ROW EXECUTE PROCEDURE contentjobitem_updaterecursivetotals_fn();
                """,

                """
                CREATE OR REPLACE FUNCTION contentjobitem_updateparents_fn() RETURNS TRIGGER AS ${'$'}${'$'}
                BEGIN 
                UPDATE ContentJobItem 
                   SET cjiRecursiveProgress = (cjiRecursiveProgress + (NEW.cjiRecursiveProgress - OLD.cjiRecursiveProgress)),
                       cjiRecursiveTotal = (cjiRecursiveTotal + (NEW.cjiRecursiveTotal - OLD.cjiRecursiveTotal))
                 WHERE (NEW.cjiRecursiveProgress != OLD.cjiRecursiveProgress
                        OR NEW.cjiRecursiveTotal != OLD.cjiRecursiveTotal)
                    AND ContentJobItem.cjiUid = NEW.cjiParentCjiUid
                    AND NEW.cjiParentCjiUid != 0;  
                RETURN NULL;
                END ${'$'}${'$'} LANGUAGE plpgsql
                """,
                """
                CREATE TRIGGER contentjobitem_updateparents_trig
                AFTER UPDATE ON ContentJobItem
                FOR EACH ROW EXECUTE PROCEDURE contentjobitem_updateparents_fn();    
                """
            ))
        }

    }

    override fun onOpen(db: DoorSqlDatabase) {

    }

    companion object {

        const val CHILD_ID = "NEW.cjiUid"

        const val PARENT_ID = "NEW.cjiParentCjiUid"

        fun getStatus(id: String): String {
            return """
                    SELECT cjiRecursiveStatus AS status 
                     FROM ContentJobItem 
                    WHERE cjiParentCjiUid = $id
              UNION
                    SELECT cjiStatus AS status
                      FROM ContentJobItem 
                     WHERE cjiUid = $id
            """
        }

        fun statusCheck(id: String): String {
            return """
                  (CASE WHEN 
							(SELECT Count(*) FROM (${getStatus(id)})) = 
							(SELECT Count(*) 
							   FROM (${getStatus(id)}) 
							  WHERE status =  ${JobStatus.COMPLETE}) 
					      THEN  ${JobStatus.COMPLETE} 
                          WHEN (SELECT Count(*) FROM (${getStatus(id)})) = 
                            (SELECT Count(*) 
							   FROM (${getStatus(id)}) 
							  WHERE status =  ${JobStatus.FAILED}) 
                         THEN ${JobStatus.FAILED}
                         WHEN EXISTS (SELECT status
										FROM (${getStatus(id)}) 
									    WHERE status = ${JobStatus.FAILED})
						  THEN ${JobStatus.PARTIAL_FAILED}
						  WHEN EXISTS (SELECT status 
										FROM (${getStatus(id)}) 	
										WHERE status = ${JobStatus.RUNNING})
						  THEN ${JobStatus.RUNNING}
						  WHEN EXISTS (SELECT status
										FROM (${getStatus(id)}) 
									    WHERE status = ${JobStatus.WAITING_FOR_CONNECTION})
						  THEN ${JobStatus.WAITING_FOR_CONNECTION} 
						  ELSE ${JobStatus.QUEUED} END)  
            """
        }
    }

}