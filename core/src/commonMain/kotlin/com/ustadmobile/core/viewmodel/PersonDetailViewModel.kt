package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.door.ext.onDbThenRepoWithTimeout
import com.ustadmobile.lib.db.entities.Person
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

class PersonDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): DetailViewModel<Person>(di, savedStateHandle) {

    override val entity: Flow<Person?>

    init {
        val db: UmAppDatabase by instance()

        entity = db.personDao.findByUidAsFlow(savedStateHandle["personUid"]!!)


        viewModelScope.launch {
            entity.collectLatest {  }
        }
    }



}