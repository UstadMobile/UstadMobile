package com.ustadmobile.staging.port.android.view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
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
import com.ustadmobile.core.controller.SelectClazzesDialogPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.DismissableDialog
import com.ustadmobile.core.view.SelectClazzesDialogView
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents
import com.ustadmobile.port.android.view.UstadDialogFragment
import io.reactivex.annotations.NonNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

/**
 * SelectClazzesDialogFragment Android fragment extends UstadBaseFragment
 */
class SelectClazzesDialogFragment : UstadDialogFragment(), SelectClazzesDialogView,
        AdapterView.OnItemSelectedListener, DialogInterface.OnClickListener,
        DialogInterface.OnShowListener, View.OnClickListener, DismissableDialog {
    override val viewContext: Any
        get() = context!!


    internal lateinit var dialog: AlertDialog
    internal lateinit var rootView: View

    //RecyclerView
    private var mRecyclerView: RecyclerView? = null
    private val mRecyclerLayoutManager: RecyclerView.LayoutManager? = null
    private val mAdapter: RecyclerView.Adapter<*>? = null

    private var mPresenter: SelectClazzesDialogPresenter? = null
    //Context (Activity calling this)
    private var mAttachedContext: Context? = null

    internal lateinit var toolbar: Toolbar

    internal var selectedClazzes: HashMap<String, Long>? = null

    //Main Activity should implement this ?
    interface ClazzSelectDialogListener {
        fun onSelectClazzesResult(selected: HashMap<String, Long>?)
    }


    fun getTintedDrawable(drawable: Drawable?, color: Int): Drawable {
        var drawable = drawable
        drawable = DrawableCompat.wrap(drawable!!)
        val tintColor = ContextCompat.getColor(context!!, color)
        DrawableCompat.setTint(drawable!!, tintColor)
        return drawable
    }

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = context!!.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        rootView = inflater.inflate(R.layout.fragment_select_clazzes_dialog, null)

        //Set up Recycler View
        initView()

        //Toolbar:
        toolbar = rootView.findViewById(R.id.fragment_select_clazzes_dialog_toolbar)
        toolbar.setTitle(R.string.select_classes)

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

        mPresenter = SelectClazzesDialogPresenter(context!!,
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
        mRecyclerView = rootView.findViewById(R.id.fragment_select_clazzes_dialog_recyclerview)
        mRecyclerView!!.layoutManager = LinearLayoutManager(context)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mAttachedContext = context
        this.selectedClazzes = HashMap()
    }

    override fun onDetach() {
        super.onDetach()
        this.mAttachedContext = null
        this.selectedClazzes = null
    }

    override fun finish() {
        selectedClazzes = mPresenter!!.clazzes
        if (mAttachedContext is ClazzSelectDialogListener) {
            (mAttachedContext as ClazzSelectDialogListener).onSelectClazzesResult(selectedClazzes)
        }
        dialog.dismiss()
    }

    override fun setClazzListProvider(factory: DataSource.Factory<Int, ClazzWithNumStudents>) {
        val mAdapter = ClazzListReturnSelectedRecyclerAdapter(DIFF_CALLBACK, context!!,
                this, mPresenter!!)
        val data = LivePagedListBuilder(factory, 20).build()

        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<ClazzWithNumStudents>> { mAdapter.submitList(it) })
        }

        mRecyclerView!!.adapter = mAdapter
    }

    // This event is triggered soon after onCreateView().
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Setup any handles to view objects here

    }

    override fun onClick(v: View) {

    }

    override fun onClick(dialog: DialogInterface, which: Int) {

    }

    override fun onShow(dialog: DialogInterface) {

    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

    }

    override fun onNothingSelected(parent: AdapterView<*>) {

    }

    override fun dismiss() {

    }

    companion object {

        /**
         * Generates a new Fragment for a page fragment
         *
         * @return A new instance of fragment SelectClazzesDialogFragment.
         */
        fun newInstance(): SelectClazzesDialogFragment {
            val fragment = SelectClazzesDialogFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }


        fun getTintedDrawable(@NonNull context: Context,
                              @DrawableRes drawableRes: Int, @ColorRes colorRes: Int): Drawable {
            var d = ContextCompat.getDrawable(context, drawableRes)
            d = DrawableCompat.wrap(d!!)
            DrawableCompat.setTint(d!!.mutate(), ContextCompat.getColor(context, colorRes))
            return d
        }

        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ClazzWithNumStudents> = object
            : DiffUtil.ItemCallback<ClazzWithNumStudents>() {
            override fun areItemsTheSame(oldItem: ClazzWithNumStudents,
                                         newItem: ClazzWithNumStudents): Boolean {
                return oldItem.clazzUid == newItem.clazzUid
            }

            override fun areContentsTheSame(oldItem: ClazzWithNumStudents,
                                            newItem: ClazzWithNumStudents): Boolean {
                return oldItem.clazzUid == newItem.clazzUid
            }
        }
    }
}
