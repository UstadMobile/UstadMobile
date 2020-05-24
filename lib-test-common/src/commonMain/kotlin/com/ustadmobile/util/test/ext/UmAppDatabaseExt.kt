package com.ustadmobile.util.test.ext

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzMember
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.util.getSystemTimeInMillis

suspend fun UmAppDatabase.insertClazzAndClazzMembers(numClazzMembers: Int): Pair<Clazz, List<ClazzMember>> {
    val mockClazz = Clazz("Test Clazz").apply {
        clazzTimeZone = "Asia/Dubai"
        clazzUid = clazzDao.insertAsync(this)
    }

    val mockPeople = (1 .. numClazzMembers).map {
        Person("user$it", "Bob", "Jones").apply {
            personUid = personDao.insertAsync(this)
        }
    }

    val clazzJoinTime = getSystemTimeInMillis() - 1000

    val mockClazzMembers = mockPeople.map {
        ClazzMember(mockClazz.clazzUid, it.personUid).apply {
            clazzMemberDateJoined = clazzJoinTime
            clazzMemberUid = clazzMemberDao.insertAsync(this)
        }
    }

    return Pair(mockClazz, mockClazzMembers)
}