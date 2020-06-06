package com.ustadmobile.util.test.ext

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.getSystemTimeInMillis

data class TestClazzAndMembers (val clazz: Clazz, val teacherList: List<ClazzMember>, val studentList: List<ClazzMember>)

private fun Person.asClazzMember(clazzUid: Long, clazzMemberRole: Int, joinTime: Long): ClazzMember {
    return ClazzMember(clazzUid, this.personUid, clazzMemberRole).apply {
        clazzMemberDateJoined = joinTime
    }
}

suspend fun UmAppDatabase.insertTestClazzAndMembers(numClazzStudents: Int, numClazzTeachers: Int = 1,
    studentNamer: (Int) -> Pair<String, String> = {"Test" to "Student $it"},
    teacherNamer: (Int) -> Pair<String, String> = {"Test" to "Teacher $it"}): TestClazzAndMembers {
    val mockClazz = Clazz("Test Clazz").apply {
        clazzTimeZone = "Asia/Dubai"
        clazzUid = clazzDao.insertAsync(this)
    }

    val testStudents = (1 .. numClazzStudents).map {
        val (firstName, lastName) = studentNamer(it)
        Person("studentuser$it", firstName, lastName).apply {
            personUid = personDao.insertAsync(this)
        }
    }

    val testTeachers = (1 .. numClazzTeachers).map {
        val (firstName, lastName) = teacherNamer(it)
        Person("studentuser$it", firstName, lastName).apply {
            personUid = personDao.insertAsync(this)
        }
    }

    val clazzJoinTime = getSystemTimeInMillis() - 1000

    val testStudentClazzMembers = testStudents.map {
        it.asClazzMember(mockClazz.clazzUid, ClazzMember.ROLE_STUDENT, clazzJoinTime).apply {
            clazzMemberUid = clazzMemberDao.insertAsync(this)
        }
    }

    val testTeacherClazzMembers = testTeachers.map {
        it.asClazzMember(mockClazz.clazzUid, ClazzMember.ROLE_TEACHER, clazzJoinTime).apply {
            clazzMemberUid = clazzMemberDao.insertAsync(this)
        }
    }

    return TestClazzAndMembers(mockClazz, testTeacherClazzMembers, testStudentClazzMembers)
}

suspend fun UmAppDatabase.insertClazzLogs(clazzUid: Long, numLogs: Int, logMaker: (Int) -> ClazzLog): List<ClazzLog> {
    return (0 until numLogs).map {index ->
        logMaker(index).apply {
            clazzLogClazzUid = clazzUid
            clazzLogUid = clazzLogDao.insertAsync(this)
        }
    }
}

suspend fun UmAppDatabase.insertContentEntryWithParentChildJoinAndMostRecentContainer(
        numEntries: Int, parentEntryUid: Long, isLeaf: Boolean = true): List<Container> {
    return (1 .. numEntries).map {
        val entry = ContentEntry().apply {
            title = "Dummy title $it"
            leaf = isLeaf
            description = "Dummy description $it"
            contentEntryUid = contentEntryDao.insertAsync(this)
        }
        ContentEntryParentChildJoin().apply {
            cepcjChildContentEntryUid = entry.contentEntryUid
            cepcjParentContentEntryUid = parentEntryUid
            cepcjUid = contentEntryParentChildJoinDao.insertAsync(this)
        }

        Container().apply {
            fileSize = 10000
            cntLastModified = getSystemTimeInMillis()
            containerContentEntryUid = entry.contentEntryUid
            containerUid = containerDao.insertAsync(this)

        }
    }
}