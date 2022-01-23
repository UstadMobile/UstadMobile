package com.ustadmobile.core.db

import com.ustadmobile.door.DoorDatabaseCallbackStatementList
import com.ustadmobile.door.DoorDbType
import com.ustadmobile.door.DoorSqlDatabase
import com.ustadmobile.door.ext.dbType

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
            DatabaseTriggers.sqliteContentJobItemTriggers.toList()
        }else {
            DatabaseTriggers.postgresContentJobItemTriggers.toList()
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

        const val CHILD_ID = "NEW.cjiUid"

        const val PARENT_ID = "NEW.cjiParentCjiUid"

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

}