/*
package com.ustadmobile.port.android.view

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.MergeAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentListBinding
import com.toughra.ustadmobile.databinding.ItemSimplepersonBinding
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.door.RepositoryLoadHelper
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_LOADED_NODATA
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_LOADING_CLOUD
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import org.hamcrest.Matchers.equalTo
import org.junit.Assume
import org.junit.Rule
import org.junit.Test

class ListStatusRecyclerViewAdapterTestFragment : Fragment() {

    var mergeAdapter: MergeAdapter? = null

    var listStatusAdapter: ListStatusRecyclerViewAdapter<PersonWithDisplayDetails>? = null

    var mDataAdapter: SimplePageListAdapter? = null

    var binding: FragmentListBinding? = null

    class SimplePageListAdapter : PagedListAdapter<PersonWithDisplayDetails, SimplePageListAdapter.SimplePageListViewHolder>(SIMPLE_DIFF_UTIL) {

        class SimplePageListViewHolder(val binding: ItemSimplepersonBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimplePageListViewHolder {
            return SimplePageListViewHolder(ItemSimplepersonBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false))
        }

        override fun onBindViewHolder(holder: SimplePageListViewHolder, position: Int) {
            holder.binding.person = getItem(position)
            holder.itemView.tag = getItem(position)?.personUid
            holder.itemView.setOnClickListener {
                //do nothing but make Espresso think it is clickable
            }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View

        listStatusAdapter = ListStatusRecyclerViewAdapter(viewLifecycleOwner)
        mDataAdapter = SimplePageListAdapter()
        mergeAdapter = MergeAdapter(mDataAdapter, listStatusAdapter)

        binding = FragmentListBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.fragmentListRecyclerview.adapter = mergeAdapter
            it.fragmentListRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        }

        return rootView
    }

    companion object {
        val SIMPLE_DIFF_UTIL = object : DiffUtil.ItemCallback<PersonWithDisplayDetails>() {
            override fun areItemsTheSame(oldItem: PersonWithDisplayDetails, newItem: PersonWithDisplayDetails): Boolean {
                return oldItem.firstNames == newItem.firstNames && oldItem.lastName == newItem.lastName
            }

            override fun areContentsTheSame(oldItem: PersonWithDisplayDetails, newItem: PersonWithDisplayDetails): Boolean {
                return oldItem.personUid == newItem.personUid
            }
        }
    }
}

class ListStatusRecyclerViewAdapterTest {

    @JvmField
    @Rule
    val dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    val dataBindingIdlingResourceRule = ScenarioIdlingResourceRule(DataBindingIdlingResource())

    @JvmField
    @Rule
    val systemImplRule = SystemImplTestNavHostRule()

    data class ListStatusScenario(val fragmentScenario: FragmentScenario<ListStatusRecyclerViewAdapterTestFragment>,
                                  val recyclerViewIdlingResource: RecyclerViewIdlingResource,
                                  val loadingStatusLiveData: MutableLiveData<RepositoryLoadHelper.RepoLoadStatus>)

    private fun createScenario(
            loadingStatus: RepositoryLoadHelper.RepoLoadStatus = RepositoryLoadHelper.RepoLoadStatus(STATUS_LOADED_NODATA),
            excludePersonUids: List<Long> = listOf()): ListStatusScenario {

        val adminPerson = Person().apply {
            firstNames = "Admin"
            lastName = "Admin"
            admin = true
            personUid = dbRule.account.personUid
            dbRule.db.personDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App) {
            ListStatusRecyclerViewAdapterTestFragment()
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)

        lateinit var recyclerViewIdlingResource: RecyclerViewIdlingResource
        val loadingStatusLiveData = MutableLiveData(loadingStatus)
        fragmentScenario.onFragment { fragment ->
            val dataSource = dbRule.db.personDao.findPersonsWithPermission(0, 0, 0, excludePersonUids,
                    adminPerson.personUid, PersonDao.SORT_FIRST_NAME_ASC)
            val livePagedList = LivePagedListBuilder(dataSource, 20).build()
            fragment.listStatusAdapter?.pagedListLiveData = livePagedList
            livePagedList.observe(fragment.viewLifecycleOwner, Observer {
                fragment.mDataAdapter?.submitList(it)
            })

            fragment.listStatusAdapter!!.repositoryLoadStatus = loadingStatusLiveData
            recyclerViewIdlingResource = RecyclerViewIdlingResource(fragment.binding!!.fragmentListRecyclerview)
        }
        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)

        return ListStatusScenario(fragmentScenario, recyclerViewIdlingResource, loadingStatusLiveData)
    }

    @Test
    fun givenEmptyList_whenDisplayed_thenShouldShowEmptyMessage() {
        val (scenario, recyclerViewIdlingResource) = createScenario(excludePersonUids =
            listOf(dbRule.account.personUid))

        onView(withText(R.string.nothing_here)).check(matches(isDisplayed()))

        IdlingRegistry.getInstance().unregister(recyclerViewIdlingResource)
    }

    @Test
    fun givenRepoLoading_whenDisplayed_thenShouldShowFirstItemAndLoading() {
        Assume.assumeTrue("Display of progress indicator test requires SDK > 24",
                Build.VERSION.SDK_INT > 24)
        val person = Person().apply {
            firstNames = "Test Entity"
            lastName = "McLast"
            personUid = dbRule.db.personDao.insert(this)
        }

        val (scenario, recyclerViewIdlingResource, repoLiveData) = createScenario(
                RepositoryLoadHelper.RepoLoadStatus(STATUS_LOADING_CLOUD))

        onView(withText(R.string.repo_loading_status_loading_cloud)).check(matches(isDisplayed()))
        onView(withId(R.id.fragment_list_recyclerview)).perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(withTagValue(equalTo(person.personUid)),
                        click()))
        IdlingRegistry.getInstance().unregister(recyclerViewIdlingResource)
    }

    @Test
    fun givenLoadingComplete_whenDisplayed_thenShouldNotShowLoadingStatus() {
        val person = Person().apply {
            firstNames = "Test Entity"
            lastName = "McLast"
            personUid = dbRule.db.personDao.insert(this)
        }

        val (scenario, recyclerViewIdlingResource, repoLiveData) = createScenario(
                RepositoryLoadHelper.RepoLoadStatus(STATUS_LOADED_NODATA))

        onView(withId(R.id.fragment_list_recyclerview)).perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(withTagValue(equalTo(person.personUid)),
                        click()))
        onView(withId(R.id.status_text)).check(doesNotExist())

        IdlingRegistry.getInstance().unregister(recyclerViewIdlingResource)
    }

    fun givenCannotLoadDueToNoConnectionWithCachePresent_whenDisplayed_thenShouldNotShowLoadingStatus() {

    }

}
*/
