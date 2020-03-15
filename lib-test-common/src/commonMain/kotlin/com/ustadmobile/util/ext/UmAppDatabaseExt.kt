package com.ustadmobile.util.ext

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.*

data class PersonWithClazzandRole(val person: Person, val clazz: Clazz, val role: Role)

fun UmAppDatabase.grantClazzRoleToPerson(person: Person, clazz: Clazz, role: Role)
        : PersonWithClazzandRole {
    if(person.personUid == 0L) {
        //Create the person if 0
        person.personUid = personDao.insert(person)
    }
    //Check if persongroup created. If not, create it.
    var personGroup : PersonGroup? = null
    personGroup = personGroupDao.findPersonIndividualGroupSync(person.personUid)
    if(personGroup == null){
        personGroup = PersonGroup()
        personGroup.groupPersonUid = person.personUid
        personGroup.groupUid = personGroupDao.insert(personGroup)
    }

    //Assign person to person group member if not assigned.
    var groupMember : PersonGroupMember ? = null
    var groupMembers : List<PersonGroupMember>
    groupMembers = personGroupMemberDao.finAllMembersWithGroupIdSync(personGroup.groupUid)
    if(groupMembers.isEmpty()){
        //Create new
        groupMember = PersonGroupMember(person.personUid, personGroup.groupUid)
        groupMember.groupMemberActive = true
        groupMember.groupMemberUid = personGroupMemberDao.insert(groupMember)

    }

    if(clazz.clazzUid == 0L){
        //Insert clazz
        clazz.clazzUid = clazzDao.insert(clazz)
    }
    if(role.roleUid == 0L){
        //Insert role
        role.roleUid = roleDao.insert(role)
    }

    //Check if role assignment exists. If it doesn't, create it.
    var entityRole : EntityRole? = null
    var entityRoles : List<EntityRole> ? = null
    entityRoles = entityRoleDao.findGroupByRoleAndEntityTypeAndUidAndPersonGroupUid(
            role.roleUid,
            Clazz.TABLE_ID, clazz.clazzUid,
            personGroup.groupPersonUid)
    if(entityRoles.isEmpty()){
        //Create it
        entityRole = EntityRole(Clazz.TABLE_ID, clazz.clazzUid, personGroup.groupUid, role.roleUid)
        entityRole.erActive = true
        entityRole.erEntityUid = entityRoleDao.insert(entityRole)
    }else{
        entityRole = entityRoles[0]
    }

    return PersonWithClazzandRole(person, clazz, role)

}

fun UmAppDatabase.createTeacherRole(): Role{
    val teacherPermissions = Role.PERMISSION_CLAZZ_ADD_STUDENT or
            Role.PERMISSION_CLAZZ_SELECT or                  //See Clazzes

            Role.PERMISSION_CLAZZ_UPDATE or                  //Update Clazz

            Role.PERMISSION_CLAZZ_LOG_ACTIVITY_SELECT or     //See Clazz Activity

            Role.PERMISSION_CLAZZ_LOG_ACTIVITY_UPDATE or     //Update Clazz Activity

            Role.PERMISSION_CLAZZ_LOG_ACTIVITY_INSERT or     //Add/Take Clazz Activities

            Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT or   //See Attendance

            Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_INSERT or   //Take attendance

            Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_UPDATE or   //Update attendance

            Role.PERMISSION_PERSON_SELECT or                //See People

            Role.PERMISSION_PERSON_PICTURE_INSERT or         //Insert Person Picture

            Role.PERMISSION_PERSON_PICTURE_SELECT or         //See Person Picture

            Role.PERMISSION_PERSON_PICTURE_UPDATE or          //Update Person picture
            Role.PERMISSION_CLAZZ_ASSIGNMENT_READ_WRITE     //Clazz Assignments

    var teacherRole: Role? = null
    teacherRole = roleDao.findByNameSync(Role.ROLE_NAME_TEACHER)
    if(teacherRole == null){
        //Create teacher role
        teacherRole = Role(Role.ROLE_NAME_TEACHER, teacherPermissions)
        teacherRole.roleUid = roleDao.insert(teacherRole)
    }

    return teacherRole

}