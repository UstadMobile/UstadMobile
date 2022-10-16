package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.navGraphViewModels
import androidx.savedstate.SavedStateRegistryOwner
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.nav.SavedStateHandleAdapter
import com.ustadmobile.core.viewmodel.PersonDetailViewModel
import com.ustadmobile.lib.db.entities.Person
import org.kodein.di.DI
import org.kodein.di.android.x.closestDI

class PersonDetailFragment2 : Fragment(){

    val di: DI by closestDI()

    val viewModel: PersonDetailViewModel by navGraphViewModels(R.id.mobile_navigation) {
        provideFactory(di, requireActivity(), arguments)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MaterialTheme {
                    PersonDetailView(viewModel)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        return
    }

    companion object {


        fun provideFactory(
            di: DI,
            owner: SavedStateRegistryOwner,
            defaultArgs: Bundle? = null,
        ): AbstractSavedStateViewModelFactory = object: AbstractSavedStateViewModelFactory(owner, defaultArgs) {
            override fun <T : ViewModel?> create(
                key: String,
                modelClass: Class<T>,
                handle: SavedStateHandle
            ): T {
                return PersonDetailViewModel(di, SavedStateHandleAdapter(handle)) as T
            }
        }
    }
}


@Composable
fun PersonDetailView(viewModel: PersonDetailViewModel) {



}

@Composable
@Preview
fun PersonDetailViewPreview(person: Person = Person().apply { firstNames = "Tester T"}) {
    Text(person.firstNames ?: "No Name")
}
