package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.ClazzEnrolment

class AlreadyEnroledInClassException(var existingClazzEnrolment: ClazzEnrolment) : Exception()
