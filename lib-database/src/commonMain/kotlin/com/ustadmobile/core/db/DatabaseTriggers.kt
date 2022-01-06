package com.ustadmobile.core.db

object DatabaseTriggers {

    const val CHILD_ID = "NEW.cjiUid"

    const val PARENT_ID = "NEW.cjiParentCjiUid"

    val sqliteContentJobItemTriggers = arrayOf(
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
                    SET cjiRecursiveStatus = ${statusCheck(ContentJobItemTriggersCallback.CHILD_ID)}
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

    )

    val postgresContentJobItemTriggers = arrayOf(
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
                    SET cjiRecursiveStatus = ${ContentJobItemTriggersCallback.statusCheck(ContentJobItemTriggersCallback.CHILD_ID)}
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
                   SET cjiRecursiveStatus = ${ContentJobItemTriggersCallback.statusCheck(ContentJobItemTriggersCallback.PARENT_ID)}
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

    fun getStatus(id: String): String {
        return """
                  (SELECT cjiRecursiveStatus AS status 
                     FROM ContentJobItem 
                    WHERE cjiParentCjiUid = $id
              UNION
                   SELECT cjiStatus AS status
                     FROM ContentJobItem 
                    WHERE cjiUid = $id) AS JobStatus
            """
    }

    fun statusCheck(id: String): String {
        return """
                  (CASE WHEN 
							(SELECT Count(*) FROM ${getStatus(id)}) = 
							(SELECT Count(*) 
							   FROM ${getStatus(id)} 
							  WHERE status = ${JobStatus.COMPLETE}) 
					      THEN  ${JobStatus.COMPLETE} 
                          WHEN (SELECT Count(*) FROM ${getStatus(id)}) = 
                            (SELECT Count(*) 
							   FROM ${getStatus(id)} 
							  WHERE status = ${JobStatus.FAILED}) 
                         THEN ${JobStatus.FAILED}
                         WHEN EXISTS (SELECT status
										FROM ${getStatus(id)} 
									    WHERE (status = ${JobStatus.FAILED}
                                           OR status = ${JobStatus.PARTIAL_FAILED}))
						  THEN ${JobStatus.PARTIAL_FAILED}
						  WHEN EXISTS (SELECT status 
										FROM ${getStatus(id)}	
										WHERE status = ${JobStatus.RUNNING})
						  THEN ${JobStatus.RUNNING}
						  WHEN EXISTS (SELECT status
										FROM ${getStatus(id)} 
									    WHERE status = ${JobStatus.WAITING_FOR_CONNECTION})
						  THEN ${JobStatus.WAITING_FOR_CONNECTION} 
						  ELSE ${JobStatus.QUEUED} END)  
            """
    }


}