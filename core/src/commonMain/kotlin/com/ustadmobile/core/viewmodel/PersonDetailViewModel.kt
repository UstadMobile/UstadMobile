package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.door.ext.onDbThenRepoWithTimeout
import com.ustadmobile.lib.db.entities.Person
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

class PersonDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): DetailViewModel<Person>(di, savedStateHandle) {

    private val _personEntity = MutableStateFlow<Person?>(null)

    override val entity: StateFlow<Person?>
        get() = _personEntity.asStateFlow()

    init {
        val db: UmAppDatabase by instance()


    }



}