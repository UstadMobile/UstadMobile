package com.ustadmobile.core.db

import com.ustadmobile.door.DoorDatabaseCallbackStatementList
import com.ustadmobile.door.DoorDbType
import com.ustadmobile.door.DoorSqlDatabase
import com.ustadmobile.door.ext.dbType
import com.ustadmobile.door.ext.minifySql

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
class ContentJobItemTriggersCallback: DoorDatabaseCallbackStatementList {

    override fun onCreate(db: DoorSqlDatabase) : List<String> {
        return if(db.dbType() == DoorDbType.SQLITE) {
            sqliteContentJobItemTriggers.toList()
        }else {
            postgresContentJobItemTriggers.toList()
        }

    }

    override fun onOpen(db: DoorSqlDatabase) : List<String> {
        return if(db.dbType() == DoorDbType.SQLITE) {
            listOf("""
                PRAGMA recursive_triggers = ON;
            """)
        }else {
            listOf()
        }
    }

    companion object {

        private val sqliteContentJobItemTriggers = arrayOf(
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
                   SET cjiRecursiveStatus = ${recursiveStatusCaseClause("NEW.cjiUid")}
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
                   SET cjiRecursiveStatus = ${recursiveStatusCaseClause("NEW.cjiParentCjiUid")}
                 WHERE ContentJobItem.cjiUid = NEW.cjiParentCjiUid;
                 END;
                """

        )

        private val postgresContentJobItemTriggers = arrayOf(
            """
                CREATE OR REPLACE FUNCTION contentjobiteminsert_fn() RETURNS TRIGGER AS ${'$'}${'$'} 
                BEGIN
                UPDATE ContentJobItem 
                   SET cjiRecursiveProgress = NEW.cjiItemProgress,
                       cjiRecursiveTotal = NEW.cjiItemTotal
                 WHERE ContentJobItem.cjiUid = NEW.cjiUid;
                RETURN NEW; 
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
                RETURN NEW;
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
                RETURN NEW;
                END ${'$'}${'$'} LANGUAGE plpgsql
                """,
            """
                CREATE TRIGGER contentjobitem_updateparents_trig
                AFTER UPDATE ON ContentJobItem
                FOR EACH ROW EXECUTE PROCEDURE contentjobitem_updateparents_fn();    
                """,
            """
                 CREATE OR REPLACE FUNCTION contentjobitem_updatestatus_fn() RETURNS TRIGGER AS ${'$'}${'$'}
                 BEGIN 
                 UPDATE ContentJobItem
                    SET cjiRecursiveStatus = ${recursiveStatusCaseClause("NEW.cjiUid")}
                  WHERE contentJobItem.cjiUid = NEW.cjiUid 
                    AND NEW.cjiStatus != OLD.cjiStatus;
                 RETURN NEW;     
                 END ${'$'}${'$'} LANGUAGE plpgsql  
                 """,
            """
                 CREATE TRIGGER contentjobitem_updatestatus_trig
                 AFTER UPDATE ON ContentJobItem
                 FOR EACH ROW EXECUTE PROCEDURE contentjobitem_updatestatus_fn();    
                 """,
            """
                 CREATE OR REPLACE FUNCTION contentjobitem_updatestatusparents_fn() RETURNS TRIGGER AS ${'$'}${'$'}
                 BEGIN
                 UPDATE ContentJobItem
                   SET cjiRecursiveStatus = ${recursiveStatusCaseClause("NEW.cjiParentCjiUid")}
                 WHERE NEW.cjiParentCjiUid != 0 
                   AND NEW.cjiRecursiveStatus != OLD.cjiRecursiveStatus
                   AND ContentJobItem.cjiUid = NEW.cjiParentCjiUid;     
                 RETURN NEW;     
                 END ${'$'}${'$'} LANGUAGE plpgsql     
                 """,
            """
                 CREATE TRIGGER contentjobitem_updatestatusparents_trig
                 AFTER UPDATE ON ContentJobItem
                 FOR EACH ROW EXECUTE PROCEDURE contentjobitem_updatestatusparents_fn();        
                 """
        )

        /**
         * This is a convenience function that can be called from a test. Use it to get a list of
         * all statements to copy/paste for migration purposes.
         */
        fun dumpSqlStatements() {
            println("SQLITE:")
            println(sqliteContentJobItemTriggers.joinToString(prefix = "\"", postfix = "\"", separator = "\",\n\"") {
                it.minifySql()
            })
            println("POSTGRES")
            println(postgresContentJobItemTriggers.joinToString(prefix = "\"", postfix = "\"", separator = "\",\n\"") {
                it.minifySql()
            })
        }



        /**
         * Creates a UNION statement to select the status columns of the given ContentJobItem and
         * all of its direct children.
         */
        private fun getStatusOfItemAndChildrenSql(contentJobItemUidExpression: String): String {
            return """
                  (SELECT cjiRecursiveStatus AS status 
                     FROM ContentJobItem 
                    WHERE cjiParentCjiUid = $contentJobItemUidExpression
              UNION
                   SELECT cjiStatus AS status
                     FROM ContentJobItem 
                    WHERE cjiUid = $contentJobItemUidExpression) AS JobStatus
            """
        }

        fun recursiveStatusCaseClause(contentJobItemUidExpression: String): String {
            //Note: Common Table Expressions are not supported for use inside SQLite triggers as per
            //  https://www.sqlite.org/lang_createtrigger.html section 2.1
            return """
                  (CASE WHEN 
							(SELECT Count(*) FROM ${getStatusOfItemAndChildrenSql(contentJobItemUidExpression)}) = 
							(SELECT Count(*) 
							   FROM ${getStatusOfItemAndChildrenSql(contentJobItemUidExpression)} 
							  WHERE status = ${JobStatus.COMPLETE}) 
					      THEN  ${JobStatus.COMPLETE} 
                          WHEN (SELECT Count(*) FROM ${getStatusOfItemAndChildrenSql(contentJobItemUidExpression)}) = 
                            (SELECT Count(*) 
							   FROM ${getStatusOfItemAndChildrenSql(contentJobItemUidExpression)} 
							  WHERE status = ${JobStatus.FAILED}) 
                          THEN ${JobStatus.FAILED}
                          WHEN(SELECT COUNT(*) FROM ${getStatusOfItemAndChildrenSql(contentJobItemUidExpression)}) = 
                             (SELECT COUNT(*)
                                FROM ${getStatusOfItemAndChildrenSql(contentJobItemUidExpression)}
                               WHERE status = ${JobStatus.CANCELED})
                          THEN ${JobStatus.CANCELED}
						  WHEN EXISTS (SELECT status 
										FROM ${getStatusOfItemAndChildrenSql(contentJobItemUidExpression)}	
										WHERE status = ${JobStatus.RUNNING})
						  THEN ${JobStatus.RUNNING}
                          WHEN EXISTS (SELECT status
										FROM ${getStatusOfItemAndChildrenSql(contentJobItemUidExpression)} 
									    WHERE (status = ${JobStatus.FAILED}
                                           OR status = ${JobStatus.PARTIAL_FAILED}))
						  THEN ${JobStatus.PARTIAL_FAILED}
						  WHEN EXISTS (SELECT status
										FROM ${getStatusOfItemAndChildrenSql(contentJobItemUidExpression)} 
									    WHERE status = ${JobStatus.WAITING_FOR_CONNECTION})
						  THEN ${JobStatus.WAITING_FOR_CONNECTION} 
						  ELSE ${JobStatus.QUEUED} END)  
            """
        }
    }

}