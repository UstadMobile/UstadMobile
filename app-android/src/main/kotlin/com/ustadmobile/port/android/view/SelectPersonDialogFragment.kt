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
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SelectPersonDialogPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.DismissableDialog
import com.ustadmobile.core.view.SelectPersonDialogView
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.Person
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.dimorinny.floatingtextbutton.FloatingTextButton

/**
 * SelectPersonDialogFragment Android fragment extends UstadBaseFragment
 */
class SelectPersonDialogFragment : UstadDialogFragment(),
        SelectPersonDialogView, DismissableDialog {


    override val viewContext: Any
        get() = context!!

    internal lateinit var dialog: AlertDialog
    internal lateinit var rootView: View

    //RecyclerView
    private var mRecyclerView: RecyclerView? = null
    lateinit var fab: FloatingTextButton

    private var mPresenter: SelectPersonDialogPresenter? = null
    //Context (Activity calling this)
    private var mAttachedContext: Context? = null

    internal lateinit var toolbar: Toolbar

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

        rootView = inflater.inflate(R.layout.fragment_select_person_dialog, null)

        //Set up Recycler View
        initView()

        fab = rootView.findViewById(R.id.fragment_select_person_dialog_fab)

        //Toolbar:
        toolbar = rootView.findViewById(R.id.fragment_select_person_dialog_toolbar)
        toolbar.setTitle(R.string.select_customers)

        var upIcon = AppCompatResources.getDrawable(context!!,
                R.drawable.ic_arrow_back_white_24dp)

        upIcon = getTintedDrawable(upIcon, R.color.icons)

        toolbar.navigationIcon = upIcon
        toolbar.setNavigationOnClickListener { v -> finish() }

        mPresenter = SelectPersonDialogPresenter(context!!,
                UMAndroidUtil.bundleToMap(arguments), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        fab.setOnClickListener{v -> mPresenter!!.handleClickNewCustomer()}

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
        mRecyclerView = rootView.findViewById(R.id.fragment_select_person_dialog_recyclerview)
        mRecyclerView!!.layoutManager = LinearLayoutManager(context)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.mAttachedContext = context
    }

    override fun onDetach() {
        super.onDetach()
        this.mAttachedContext = null
    }

    override fun finish() {
        dialog.dismiss()
    }

    override fun setListProvider(factory: DataSource.Factory<Int, Person>) {
        val mAdapter = PersonSelectedRecyclerAdapter(DIFF_CALLBACK, context!!,
                this, mPresenter!!)

        val data = factory.asRepositoryLiveData(
                UmAccountManager.getRepositoryForActiveAccount(context!!).personDao)

        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<Person>> { mAdapter.submitList(it) })
        }


        mRecyclerView!!.adapter = mAdapter
    }

    companion object {
        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<Person> = object
            : DiffUtil.ItemCallback<Person>() {
            override fun areItemsTheSame(oldItem: Person,
                                         newItem: Person): Boolean {
                return oldItem.personUid == newItem.personUid
            }

            override fun areContentsTheSame(oldItem: Person,
                                            newItem: Person): Boolean {
                return oldItem.personUid == newItem.personUid
            }
        }
    }
}
