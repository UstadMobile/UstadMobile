package com.ustadmobile.util.ext

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role

fun UmAppDatabase.grantClazzRoleToPerson(person: Person, clazz: Clazz, role: Role) {
    if(person.personUid != 0L) {

    }
    //insert the person, make a persongroup, insert role, make role assignment..
}