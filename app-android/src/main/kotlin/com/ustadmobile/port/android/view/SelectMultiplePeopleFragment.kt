package com.ustadmobile.port.android.view

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SelectMultiplePeoplePresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.DismissableDialog
import com.ustadmobile.core.view.SelectMultiplePeopleView
import com.ustadmobile.lib.db.entities.Person
import java.util.*

/**
 * SelectPeopleDialogFragment Android fragment extends UstadBaseFragmentRe
 */
class SelectMultiplePeopleFragment : UstadDialogFragment(),
        SelectMultiplePeopleView, DismissableDialog {
    override val viewContext: Any
        get() = context!!


    internal lateinit var dialog: AlertDialog
    internal lateinit var rootView: View

    //RecyclerView
    private var mRecyclerView: RecyclerView? = null
    private val mRecyclerLayoutManager: RecyclerView.LayoutManager? = null
    private val mAdapter: RecyclerView.Adapter<*>? = null

    private var mPresenter: SelectMultiplePeoplePresenter? = null
    //Context (Activity calling this)
    private var mAttachedContext: Context? = null

    internal var forActor: Boolean = false

    internal lateinit var toolbar: Toolbar

    internal var selectedPeople: HashMap<String, Long>? = null

    //Main Activity should implement this ?
    interface PersonSelectDialogListener {
        fun onSelectPeopleListener(selected: HashMap<String, Long>?, actor: Boolean)
    }


    fun getTintedDrawable(drawable: Drawable?, color: Int): Drawable {
        var drawable = drawable
        drawable = DrawableCompat.wrap(drawable!!)
        val tintColor = ContextCompat.getColor(context!!, color)
        DrawableCompat.setTint(drawable!!, tintColor)
        return drawable
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = context!!.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        rootView = inflater.inflate(R.layout.fragment_select_people_dialog, null)

        //Set up Recycler View
        initView()

        //Toolbar:
        toolbar = rootView.findViewById(R.id.fragment_select_people_dialog_toolbar)
        toolbar.setTitle(R.string.selected_les)

        var upIcon = AppCompatResources.getDrawable(context!!,
                R.drawable.ic_arrow_back_white_24dp)

        upIcon = getTintedDrawable(upIcon, R.color.icons)

        toolbar.navigationIcon = upIcon
        toolbar.setNavigationOnClickListener { v -> dialog.dismiss() }


        toolbar.inflateMenu(R.menu.menu_done)
        //Click the tick button on the toolbar:
        toolbar.setOnMenuItemClickListener { item ->
            val i = item.itemId
            if (i == R.id.menu_done) {
                mPresenter!!.handleCommonPressed(-1)
            }
            false
        }

        mPresenter = SelectMultiplePeoplePresenter(context!!,
                UMAndroidUtil.bundleToMap(arguments), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //Dialog stuff:
        //Set any view components and its listener (post presenter work)
        dialog = AlertDialog.Builder(context!!,
                R.style.FullScreenDialogStyle)
                .setView(rootView)
                .setTitle("")
                .create()
        return dialog

    }

    private fun initView() {
        //Set recycler view
        mRecyclerView = rootView.findViewById(R.id.fragment_select_people_dialog_recyclerview)
        mRecyclerView!!.layoutManager = LinearLayoutManager(context)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.mAttachedContext = context
        this.selectedPeople = HashMap()
    }

    override fun onDetach() {
        super.onDetach()
        this.mAttachedContext = null
        this.selectedPeople = null
    }

    override fun finish() {
        selectedPeople = mPresenter!!.people
        if (mAttachedContext is PersonSelectDialogListener) {
            (mAttachedContext as PersonSelectDialogListener).onSelectPeopleListener(selectedPeople,
                    forActor)
        }
        dialog.dismiss()
    }

    override fun setListProvider(factory: DataSource.Factory<Int, Person>) {
        val mAdapter = PersonListReturnSelectedSimpleRecyclerAdapter(DIFF_CALLBACK, context!!,
                this, mPresenter!!)
        //A warning is expected
        val data = LivePagedListBuilder(factory, 20).build()

        data.observe(this, Observer<PagedList<Person>> { mAdapter.submitList(it) })

        mRecyclerView!!.adapter = mAdapter
    }

    // This event is triggered soon after onCreateView().
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Setup any handles to view objects here

    }

    override fun dismiss() {

    }

    companion object {

        /**
         * Generates a new Fragment for a page fragment
         *
         * @return A new instance of fragment SelectClazzesDialogFragment.
         */
        fun newInstance(): SelectMultiplePeopleFragment {
            val fragment = SelectMultiplePeopleFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }


        fun getTintedDrawable(context: Context,
                              @DrawableRes drawableRes: Int, @ColorRes colorRes: Int): Drawable {
            var d = ContextCompat.getDrawable(context, drawableRes)
            d = DrawableCompat.wrap(d!!)
            DrawableCompat.setTint(d!!.mutate(), ContextCompat.getColor(context, colorRes))
            return d
        }

        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<Person> = object : DiffUtil.ItemCallback<Person>() {
            override fun areItemsTheSame(oldItem: Person,
                                         newItem: Person): Boolean {
                return oldItem.personUid == newItem.personUid
            }

            override fun areContentsTheSame(oldItem: Person,
                                            newItem: Person): Boolean {
                return oldItem == newItem
            }
        }
    }
}
