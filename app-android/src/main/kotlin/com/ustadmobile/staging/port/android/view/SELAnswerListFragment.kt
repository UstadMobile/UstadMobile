package com.ustadmobile.staging.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SELAnswerListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.ClazzListView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.core.view.SELAnswerListView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.view.UstadBaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.dimorinny.floatingtextbutton.FloatingTextButton


/**
 * SELAnswerListFragment Android fragment extends UstadBaseFragment -  is responsible for
 * showing the Answer list (ie: students who have taken the SEL in the SEL tab of Clazz.
 * It should also show a primary action button to record new SEL.
 *
 */
class SELAnswerListFragment : UstadBaseFragment(), SELAnswerListView, View.OnClickListener,
        View.OnLongClickListener {
    override val viewContext: Any
        get() = context!!

    internal lateinit var rootContainer: View
    //RecyclerView
    private var mRecyclerView: RecyclerView? = null
    private var mPresenter: SELAnswerListPresenter? = null
    private var fab: FloatingTextButton? = null

    override fun setSELAnswerListProvider(factory: DataSource.Factory<Int, Person>) {
        // Specify the mAdapte
        val recyclerAdapter = SimplePeopleListRecyclerAdapter(
                DIFF_CALLBACK, context!!, this, mPresenter!!)

        // get the provider, set , observe, etc.
        val data = LivePagedListBuilder(factory, 20).build()
        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<Person>> { recyclerAdapter.submitList(it) })
        }

        //set the adapter
        mRecyclerView!!.adapter = recyclerAdapter

    }

    override fun showFAB(show: Boolean) {
        fab!!.isEnabled = show
        fab!!.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        rootContainer = inflater.inflate(R.layout.fragment_sel_answer_list,
                container, false)
        setHasOptionsMenu(true)

        // Set mRecyclerView..
        mRecyclerView = rootContainer.findViewById(R.id.fragment_sel_answer_list_recyclerview)

        // Use Layout: set layout manager. Change defaults
        val mRecyclerLayoutManager = LinearLayoutManager(context)
        mRecyclerView!!.layoutManager = mRecyclerLayoutManager
        val dividerItemDecoration = DividerItemDecoration(
                mRecyclerView!!.context, LinearLayoutManager.VERTICAL)
        mRecyclerView!!.addItemDecoration(dividerItemDecoration)

        fab = rootContainer.findViewById(R.id.fragment_sel_answer_list_record_sel_fab)

        // Set the presenter
        mPresenter = SELAnswerListPresenter(this,
                UMAndroidUtil.bundleToMap(arguments), this)

        // Call Presenter's onCreate:
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //FAB's onClickListener:
        fab!!.setOnClickListener { v -> mPresenter!!.handleClickRecordSEL() }

        return rootContainer
    }


    override fun onClick(v: View) {

    }

    override fun onLongClick(v: View): Boolean {
        return false
    }

    companion object {

        val DIFF_CALLBACK: DiffUtil.ItemCallback<Person> = object : DiffUtil.ItemCallback<Person>() {
            override fun areItemsTheSame(oldItem: Person,
                                         newItem: Person): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: Person,
                                            newItem: Person): Boolean {
                return oldItem.personUid == newItem.personUid
            }
        }

        /**
         * Generates a new Fragment for a page fragment
         *
         * @return A new instance of fragment SELAnswerListFragment.
         */
        fun newInstance(clazzUid: Long): SELAnswerListFragment {
            val fragment = SELAnswerListFragment()
            val args = Bundle()
            args.putLong(ARG_CLAZZ_UID, clazzUid)
            fragment.arguments = args
            return fragment
        }
        fun newInstance(args: Bundle): SELAnswerListFragment {
            val fragment = SELAnswerListFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
