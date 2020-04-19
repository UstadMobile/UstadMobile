package com.ustadmobile.port.android.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ActivitySelquestionsetEditBinding
import com.toughra.ustadmobile.databinding.ItemSelquestionBinding
import com.ustadmobile.core.controller.SelQuestionSetEditPresenter
import com.ustadmobile.core.controller.UstadSingleEntityPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SelQuestionSetEditView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.SelQuestion
import com.ustadmobile.lib.db.entities.SelQuestionAndOptions
import com.ustadmobile.lib.db.entities.SelQuestionSet
import com.ustadmobile.port.android.util.ext.putExtraResultAsJson
import com.ustadmobile.port.android.view.ext.setEditActivityTitle
import com.ustadmobile.port.android.view.util.AbstractCrudActivityResultContract.Companion.EXTRA_RESULT_KEY
import com.ustadmobile.port.android.view.util.CrudEditActivityResultContract


fun ComponentActivity.prepareSelQuestionSetEditCall(callback: (List<SelQuestionSet>?) -> Unit) =
        prepareCall(CrudEditActivityResultContract(this, SelQuestionSet::class.java,
        SelQuestionSetEditActivity::class.java,
        SelQuestionSet::selQuestionSetUid)) {
    callback.invoke(it)
}

fun ActivityResultLauncher<CrudEditActivityResultContract.CrudEditInput<SelQuestionSet>>
        .launchSelQuestionSetEdit(schedule: SelQuestionSet?,
                                  extraArgs: Map<String, String> = mapOf()) {
    launch(CrudEditActivityResultContract.CrudEditInput(schedule,
            UstadSingleEntityPresenter.PersistenceMode.DB, extraArgs))
}

interface SelQuestionSetEditActivityEventHandler {
    fun onClickEditSelQuestion(selQuestion: SelQuestionAndOptions?)
    fun onClickNewSelQuestion()
    fun handleRemoveSelQuestion(selQuestion: SelQuestionAndOptions)
}

class SelQuestionSetEditActivity : UstadBaseActivity(), SelQuestionSetEditView,
    SelQuestionSetEditActivityEventHandler {

    private var rootView: ActivitySelquestionsetEditBinding? = null

    private lateinit var mPresenter: SelQuestionSetEditPresenter

    /*
     * required one to many join relationships - use the following templates (then hit tab)
     *  onetomanyadapter - adds a recycler adapter, observer, and handler methods for a one-many field
     *  diffutil - adds a diffutil.itemcallback for an entity (put in the companion object)
     */

    class SelQuestionRecyclerAdapter(val activityEventHandler: SelQuestionSetEditActivityEventHandler,
            var presenter: SelQuestionSetEditPresenter?)
        : ListAdapter<SelQuestionAndOptions,
            SelQuestionRecyclerAdapter.SelQuestionViewHolder>(DIFF_CALLBACK_SELQUESTIONANDOPTIONS) {

            class SelQuestionViewHolder(val binding: ItemSelquestionBinding)
                : RecyclerView.ViewHolder(binding.root)

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelQuestionViewHolder {
                val viewHolder = SelQuestionViewHolder(ItemSelquestionBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false))
                viewHolder.binding.mPresenter = presenter
                viewHolder.binding.mActivity = activityEventHandler
                return viewHolder
            }

            override fun onBindViewHolder(holder: SelQuestionViewHolder, position: Int) {
                //TODO: Check this
                holder.binding.selQuestion = getItem(position)
            }
        }

    override var selQuestionList: DoorLiveData<List<SelQuestionAndOptions>>? = null
        get() = field
        set(value) {
            field?.removeObserver(selQuestionObserver)
            field = value
            value?.observe(this, selQuestionObserver)
        }

    private var selQuestionRecyclerAdapter: SelQuestionRecyclerAdapter? = null

    private var selQuestionRecyclerView: RecyclerView? = null

    private val selQuestionObserver = Observer<List<SelQuestionAndOptions>?> {
        t -> selQuestionRecyclerAdapter?.submitList(t)
    }

    override fun onClickEditSelQuestion(selQuestion: SelQuestionAndOptions?) {
        prepareSelQuestionAndOptionsEditCall {
            val selQuestionCreated = it?.firstOrNull() ?: return@prepareSelQuestionAndOptionsEditCall
            mPresenter.handleAddOrEditSelQuestion(selQuestionCreated)
        }.launchSelQuestionAndOptionsEdit(selQuestion)
    }

    override fun onClickNewSelQuestion() =
            onClickEditSelQuestion(null)


    override fun handleRemoveSelQuestion(selQuestion: SelQuestionAndOptions) {
        mPresenter.handleRemoveSelQuestion(selQuestion)
    }

    /*16
    TODO 2: put this into onCreate:

    */

    /*
    TODO 3
    Make a layout for the item in the recyclerview named item_SelQuestion (PS: convert SelQuestion to snake case).
    Use the Ustad Edit Screen 1-N ListItem XML (right click on res/layout, click new, and select the template)
    */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rootView = DataBindingUtil.setContentView(this, R.layout.activity_selquestionset_edit)
        rootView?.activityEventHandler = this


        val toolbar = findViewById<Toolbar>(R.id.activity_selquestionset_edit_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setEditActivityTitle(R.string.question_set)

        selQuestionRecyclerView = findViewById(R.id.activity_selquestion_recycleradapter)
        selQuestionRecyclerAdapter = SelQuestionRecyclerAdapter(this, null)
        selQuestionRecyclerView?.adapter = selQuestionRecyclerAdapter
        selQuestionRecyclerView?.layoutManager = LinearLayoutManager(this)

        mPresenter = SelQuestionSetEditPresenter(this, intent.extras.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(this),
                UmAccountManager.getRepositoryForActiveAccount(this))
        mPresenter.onCreate(savedInstanceState.toNullableStringMap())

        //After the presenter is created
        selQuestionRecyclerAdapter?.presenter = mPresenter
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(mutableMapOf<String, String>().apply { mPresenter.onSaveInstanceState(this) }.toBundle())
    }

    override var entity: SelQuestionSet? = null
        get() = field
        set(value) {
            field = value
            rootView?.selquestionset = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            rootView?.fieldsEnabled = value
        }

    override fun finishWithResult(result: SelQuestionSet) {
        setResult(RESULT_OK, Intent().apply {
            putExtraResultAsJson(EXTRA_RESULT_KEY, listOf(result))
        })
        finish()
    }

    override var loading: Boolean = false
        get() = field
        set(value) {
            field = value
            rootView?.loading = value
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_done -> {
                val entityVal = rootView?.selquestionset ?: return false
                mPresenter.handleClickSave(entityVal)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_done, menu)
        return true
    }

    companion object {

        val DIFF_CALLBACK_SELQUESTIONANDOPTIONS = object: DiffUtil.ItemCallback<SelQuestionAndOptions>() {
            override fun areItemsTheSame(oldItem: SelQuestionAndOptions, newItem: SelQuestionAndOptions): Boolean {
                return oldItem.selQuestion.selQuestionUid == newItem.selQuestion.selQuestionUid
            }

            override fun areContentsTheSame(oldItem: SelQuestionAndOptions, newItem: SelQuestionAndOptions): Boolean {
                return oldItem == newItem
            }
        }

    }

}
