package com.ustadmobile.view

import com.ustadmobile.core.navigation.UstadSavedStateHandleJs
import com.ustadmobile.core.viewmodel.ViewModel
import com.ustadmobile.hooks.collectAsState
import com.ustadmobile.hooks.useViewModel
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.mui.components.DIContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mui.material.*
import react.FC
import react.Props
import react.useContext

data class DummyUiState(
    val personState: Flow<Person?> = flowOf(null)
)

class DummyViewModel() : ViewModel(UstadSavedStateHandleJs()) {

    private val _uiState: MutableStateFlow<DummyUiState>

    val uiState: Flow<DummyUiState>
        get() = _uiState.asStateFlow()

    init {
        _uiState = MutableStateFlow(
            DummyUiState(
                personState = flowOf(Person().apply {
                    firstNames = "Bob"
                }
            )
        ))


        viewModelScope.launch {
            delay(2000)
            _uiState.update {
                it.copy(personState = flowOf(Person().apply {
                    firstNames = "Lisa"
                }))
            }
        }
    }

}

val PersonDetailScreen = FC<Props>() {
    val di = useContext(DIContext)

    val viewModel = useViewModel { DummyViewModel() }

    val dummyUiState: DummyUiState by viewModel.uiState.collectAsState(DummyUiState())

    val person: Person? by dummyUiState.personState.collectAsState(null)

    Typography {
        + "Hello ${person?.firstNames}"
    }

}