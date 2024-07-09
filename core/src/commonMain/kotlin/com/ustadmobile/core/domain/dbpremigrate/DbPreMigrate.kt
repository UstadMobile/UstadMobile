package com.ustadmobile.core.domain.dbpremigrate

interface DbPreMigrate {

    suspend operator fun invoke()

}