/**
 * This worksheet simplifies generating permission clauses for entities. It
 * will provide functions for all major scopes (e.g. permission by person,
 * by class, or by school).
 */


/**
 *
 * @param personUidFieldName
 */
fun makePersonPermissionClause(personUidFieldName: String = "Person.personUid",
                               permissionRequired: String,
                               accountPersonUid: String = ":accountPersonUid"): String = """
           /* Put your SELECT here e.g. Person.*, ClazzMember.* etc, */
            FROM
            PersonGroupMember
            LEFT JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid
            LEFT JOIN Role ON EntityRole.erRoleUid = Role.roleUid AND (Role.rolePermissions & $permissionRequired) > 0
            LEFT JOIN Person ON
                        CAST((SELECT admin FROM Person Person_Admin WHERE Person_Admin.personUid = $accountPersonUid) AS INTEGER) = 1
                        OR ($personUidFieldName = $accountPersonUid)
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

println(makePersonPermissionClause("Person.personUid", "64"))




