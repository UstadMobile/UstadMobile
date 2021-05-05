package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.SchoolMember

class AlreadyEnroledInSchoolException(var existingMember: SchoolMember): Exception()
