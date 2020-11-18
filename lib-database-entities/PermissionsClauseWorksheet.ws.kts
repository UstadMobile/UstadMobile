/**
 * This worksheet simplifies generating permission clauses for entities. It
 * will provide functions for all major scopes (e.g. permission by person,
 * by class, or by school).
 *
 * To use:
 * Right click and choose "Run" so that you can respond to the prompts.
 */


/**
 *
 * @param personUidFieldName
 */
fun makePersonPermissionJoins(joinedEntity: String = "Person",
                               personUidFieldName: String = "Person.personUid",
                               permissionRequired: String = "${'$'}{Role.Role.PERMISSION_PERSON_SELECT}",
                               accountPersonUid: String = ":accountPersonUid",
                               autoGrantPermissionToSelf: Boolean = true): String = """
           /* Put your SELECT here e.g. Person.*, ClazzMember.* etc, */
            FROM
            PersonGroupMember
            LEFT JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid
            LEFT JOIN Role ON EntityRole.erRoleUid = Role.roleUid AND (Role.rolePermissions & $permissionRequired) > 0
            LEFT JOIN $joinedEntity ON
                        CAST((SELECT admin FROM Person Person_Admin WHERE Person_Admin.personUid = $accountPersonUid) AS INTEGER) = 1
                        ${if(autoGrantPermissionToSelf) "OR ($personUidFieldName = $accountPersonUid)" else ""}
                        OR ((EntityRole.erTableId= ${'$'}{Person.TABLE_ID} AND EntityRole.erEntityUid = $personUidFieldName)
                        OR (EntityRole.erTableId = ${'$'}{Clazz.TABLE_ID} AND EntityRole.erEntityUid IN (SELECT DISTINCT clazzMemberClazzUid FROM ClazzMember WHERE clazzMemberPersonUid = $personUidFieldName))
                        OR (EntityRole.erTableId = ${'$'}{School.TABLE_ID} AND EntityRole.erEntityUid IN (SELECT DISTINCT schoolMemberSchoolUid FROM SchoolMember WHERE schoolMemberPersonUid = $personUidFieldName)) OR
                        (EntityRole.erTableId = ${'$'}{School.TABLE_ID} AND EntityRole.erEntityUid IN (
                        SELECT DISTINCT Clazz.clazzSchoolUid 
                        FROM Clazz
                        JOIN ClazzMember ON ClazzMember.clazzMemberClazzUid = Clazz.clazzUid AND ClazzMember.clazzMemberPersonUid = $personUidFieldName
                        )))
            
            WHERE
            PersonGroupMember.groupMemberPersonUid = $accountPersonUid
            GROUP BY Person.personUid
        """.trimIndent()

fun makeClazzPermissionJoins(joinedEntity: String,
                             clazzUidFieldName: String,
                             permissionRequired: String,
                             accountPersonUid: String = ":accountPersonUid"): String {

    //If this query is joining school anyhow, then we can just use clazzSchoolUid
    //otherwise we need to use a subquery to select it
    val schoolUidSelect = if(joinedEntity.equals("clazz", ignoreCase = true)) {
        "Clazz.clazzSchoolUid"
    }else {
        "(SELECT clazzSchoolUid FROM Clazz Clazz_Int WHERE Clazz_Int.clazzUid = $clazzUidFieldName)"
    }

    return """FROM 
    PersonGroupMember
    LEFT JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid
    LEFT JOIN Role ON EntityRole.erRoleUid = Role.roleUid AND (Role.rolePermissions &  $permissionRequired) > 0
    LEFT JOIN $joinedEntity ON 
        CAST((SELECT admin FROM Person Person_Admin WHERE Person_Admin.personUid = $accountPersonUid) AS INTEGER) = 1
    	OR (EntityRole.erTableId = ${'$'}{Clazz.TABLE_ID} AND EntityRole.erEntityUid = $clazzUidFieldName) 
    	OR (EntityRole.erTableId = ${'$'}{School.TABLE_ID} AND EntityRole.erEntityUid = $schoolUidSelect)
    WHERE
    PersonGroupMember.groupMemberPersonUid = $accountPersonUid
    """.trimIndent()
}

fun makeSchoolPermissionJoins(joinedEntity: String,
                              schoolUidFieldName: String,
                              permissionRequired: String,
                              accountPersonUid: String): String =
        """FROM 
            PersonGroupMember
            LEFT JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid
            LEFT JOIN Role ON EntityRole.erRoleUid = Role.roleUid AND (Role.rolePermissions & $permissionRequired) > 0
            LEFT JOIN $joinedEntity ON 
                CAST((SELECT admin FROM Person Person_Admin WHERE Person_Admin.personUid = $accountPersonUid) AS INTEGER) = 1
                OR (EntityRole.erTableId = ${'$'}{School.TABLE_ID} AND EntityRole.erEntityUid = $schoolUidFieldName)
            WHERE
            PersonGroupMember.groupMemberPersonUid = $accountPersonUid
        """.trimIndent()


var repeat: Boolean = false
do {
    println("Which entity is your permission based on (Person, Clazz, School)")
    val entityChoice = readLine() ?: ""
    when(entityChoice.toLowerCase()) {
        "person" -> {
            println("What entity are you selecting (e.g. Person, ClazzMember, etc)?")
            val joinToEntityName = readLine() ?: ""
            println("What field in your query will contain the personUid field? This could be " +
                    "Person.personUid itself, but you can also use the personUid foreign key field " +
                    "(e.g. ClazzMember.clazzMemberPersonUid) to avoid needing another join")
            val personUidFieldName = readLine() ?: ""
            println("What field or variable in the query would be used to refer to the Permission " +
                    "that is required? This could be a variable in the query (e.g. :permissionRequired) " +
                    "or it could be a constant (e.g. ${'$'}{Role.PERMISSION_PERSON_SELECT}.")
            val permissionRequired = readLine() ?: ""
            println("Is this permission automatically granted if the person we are checking " +
                    "permission on is the same as the person logged in? (y/n)")
            val autoGrantPermissionToSelf = readLine() ?: ""

            println("What field or variable in the query has the personUid for the account of the " +
                    "active user against which we are checking for permission? E.g. :accountPersonUid " +
                    "or DeviceSession.dsPersonUid")
            val accountPersonUid = readLine() ?: ""
            println(makePersonPermissionJoins(joinToEntityName, personUidFieldName, permissionRequired,
                    accountPersonUid, autoGrantPermissionToSelf != "n"))
        }

        "clazz" -> {
            println("What Entity are you selecting (e.g. Clazz, ClazzLog, ClazzWork, etc)?")
            val joinToEntityName = readLine() ?: ""
            println("What field in your query will contain the clazzUid field? This could be" +
                    "Clazz.clazzUid itself, but you can also use the ClazzUid foreign key field" +
                    "eg. ClazzLog.clazzLogClazzUid)?")
            val clazzUidFieldName = readLine() ?: ""
            println("What field or variable in the query would be used to refer to the Permission " +
                    "that is required? This could be a variable in the query (e.g. :permissionRequired) " +
                    "or it could be a constant (e.g. ${'$'}{Role.PERMISSION_PERSON_SELECT}.")
            val permissionRequired = readLine() ?: ""
            println("What field or variable in the query has the personUid for the account of the " +
                    "active user against which we are checking for permission? E.g. :accountPersonUid " +
                    "or DeviceSession.dsPersonUid")
            val accountPersonUid = readLine() ?: ""
            println(makeClazzPermissionJoins(joinToEntityName, clazzUidFieldName, permissionRequired,
                accountPersonUid))
        }

        "school" -> {
            println("What Entity are you selecting (e.g. School, SchoolMember, etc.)?")
            val joinToEntityName = readLine() ?: ""

            println("What field in your query will contain the schoolUid field? This could be" +
                    "School.schoolUid itself, but you can also use the schoolUid foreign key field" +
                    "eg. schoolMember.schoolMemberSchoolUid)?")
            val schoolUidFieldName = readLine() ?: ""

            println("What field or variable in the query would be used to refer to the Permission " +
                    "that is required? This could be a variable in the query (e.g. :permissionRequired) " +
                    "or it could be a constant (e.g. ${'$'}{Role.PERMISSION_PERSON_SELECT}.")
            val permissionRequired = readLine() ?: ""

            println("What field or variable in the query has the personUid for the account of the " +
                    "active user against which we are checking for permission? E.g. :accountPersonUid " +
                    "or DeviceSession.dsPersonUid")
            val accountPersonUid = readLine() ?: ""
            println(makeSchoolPermissionJoins(joinToEntityName, schoolUidFieldName, permissionRequired,
                accountPersonUid))
        }



        else -> {
            println("Didn't recognize $entityChoice")
        }
    }

    println("Run again? (y/n)")
    var runAgain = readLine() ?: "y"
    repeat = runAgain != "n"
}while(repeat)


