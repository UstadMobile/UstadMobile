package com.ustadmobile.port.android.view

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.ustadmobile.port.android.view.util.CrudEditActivityResultContract
import com.ustadmobile.core.controller.UstadSingleEntityPresenter
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ActivitySelquestionandoptionsEditBinding
import com.toughra.ustadmobile.databinding.ItemSelquestionoptionBinding
import com.ustadmobile.core.controller.SelQuestionAndOptionsEditPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SelQuestionAndOptionsEditView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.SelQuestionAndOptions
import com.ustadmobile.lib.db.entities.SelQuestionOption

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.ustadmobile.port.android.util.ext.putExtraResultAsJson
import com.ustadmobile.port.android.view.util.AbstractCrudActivityResultContract.Companion.EXTRA_RESULT_KEY
import com.ustadmobile.port.android.view.ext.setEditActivityTitle



fun ComponentActivity.prepareSelQuestionAndOptionsEditCall(
        callback: (List<SelQuestionAndOptions>?) -> Unit) = prepareCall(
            CrudEditActivityResultContract(this, SelQuestionAndOptions::class.java,
                                            SelQuestionAndOptionsEditActivity::class.java,
                    {it.selQuestion.selQuestionUid})
) {
    callback.invoke(it)
}

fun ActivityResultLauncher<CrudEditActivityResultContract.CrudEditInput<SelQuestionAndOptions>>
        .launchSelQuestionAndOptionsEdit(schedule: SelQuestionAndOptions?,
                                         extraArgs: Map<String, String> = mapOf()) {
    //TODOne: Set PersistenceMode to JSON or DB here
    launch(CrudEditActivityResultContract.CrudEditInput(schedule,
            UstadSingleEntityPresenter.PersistenceMode.JSON, extraArgs))
}



interface SelQuestionAndOptionsEditActivityEventHandler {
    fun onClickNewQuestionOption()
    fun updateQuestionOptionText(questionOption: SelQuestionOption)
    fun handleRemoveSelQuestionOption(questionOption: SelQuestionOption)
}

class SelQuestionAndOptionsEditActivity : UstadBaseActivity(), SelQuestionAndOptionsEditView,
    SelQuestionAndOptionsEditActivityEventHandler {

    private var rootView: ActivitySelquestionandoptionsEditBinding? = null

    private lateinit var mPresenter: SelQuestionAndOptionsEditPresenter

    /*
     * TODOne: Add any required one to many join relationships - use the following templates (then hit tab)
     *  onetomanyadapter - adds a recycler adapter, observer, and handler methods for a one-many field
     *  diffutil - adds a diffutil.itemcallback for an entity (put in the companion object)
     */

    
    class EntityClassRecyclerAdapter(val activityEventHandler: SelQuestionAndOptionsEditActivityEventHandler,
            var presenter: SelQuestionAndOptionsEditPresenter?)
        : ListAdapter<SelQuestionOption,
            EntityClassRecyclerAdapter.EntityClassViewHolder>(DIFF_CALLBACK_SEL_QUESTION_OPTION) {
    
            class EntityClassViewHolder(val binding: ItemSelquestionoptionBinding)
                : RecyclerView.ViewHolder(binding.root)
    
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntityClassViewHolder {
                val viewHolder = EntityClassViewHolder(ItemSelquestionoptionBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false))
                viewHolder.binding.mPresenter = presenter
                viewHolder.binding.mActivityEventHandler = activityEventHandler
                return viewHolder
            }


            override fun onBindViewHolder(holder: EntityClassViewHolder, position: Int) {
                val option = getItem(position)
                holder.binding.selQuestionOption = option

                var textWatcher = object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        presenter?.updateQuestionOptionTitle(option, s.toString())
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        //Nothing
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        //Nothing
                    }
                }
                holder.binding.itemSelQuestionOptionText.addTextChangedListener(textWatcher)
            }
        }
    
    override var selQuestionOptionList: DoorLiveData<List<SelQuestionOption>>? = null
        get() = field
        set(value) {
            field?.removeObserver(entityClassObserver)
            field = value
            value?.observe(this, entityClassObserver)
        } 
    
    private var entityClassRecyclerAdapter: EntityClassRecyclerAdapter? = null
    
    private var entityClassRecyclerView: RecyclerView? = null
    
    private val entityClassObserver = Observer<List<SelQuestionOption>?> {
        t -> entityClassRecyclerAdapter?.submitList(t)
    }

    /* 
    TODO 3
    Make a layout for the item in the recyclerview named item_EntityClass (PS: convert EntityClass to snake case).
    Use the Ustad Edit Screen 1-N ListItem XML (right click on res/layout, click new, and select the template)
    */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rootView = DataBindingUtil.setContentView(this,
                R.layout.activity_selquestionandoptions_edit)
        rootView?.activityEventHandler = this

        val toolbar = findViewById<Toolbar>(R.id.activity_selquestionandoptions_edit_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setEditActivityTitle(R.string.sel_new_question_title)

        entityClassRecyclerView = findViewById(R.id.activity_selquestionandoptions_edit_rv)
        entityClassRecyclerAdapter = EntityClassRecyclerAdapter(this, null)
        entityClassRecyclerView?.adapter = entityClassRecyclerAdapter
        entityClassRecyclerView?.layoutManager = LinearLayoutManager(this)

        mPresenter = SelQuestionAndOptionsEditPresenter(this, intent.extras.toStringMap(),
                this,this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(this),
                UmAccountManager.getRepositoryForActiveAccount(this))
        mPresenter.onCreate(savedInstanceState.toNullableStringMap())

        entityClassRecyclerAdapter?.presenter = mPresenter
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(mutableMapOf<String, String>().apply {
            mPresenter.onSaveInstanceState(this) }.toBundle())
    }

    override var entity: SelQuestionAndOptions? = null
        get() = field
        set(value) {
            field = value
            rootView?.selquestionandoptions = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            rootView?.fieldsEnabled = value
        }

    override fun finishWithResult(result: SelQuestionAndOptions) {
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
                val entityVal = rootView?.selquestionandoptions ?: return false
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

        val DIFF_CALLBACK_SEL_QUESTION_OPTION = object : DiffUtil.ItemCallback<SelQuestionOption>() {
            override fun areItemsTheSame(oldItem: SelQuestionOption, newItem: SelQuestionOption): Boolean {
                return oldItem.selQuestionOptionUid == newItem.selQuestionOptionUid
            }

            override fun areContentsTheSame(oldItem: SelQuestionOption, newItem: SelQuestionOption): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onClickNewQuestionOption() {
        mPresenter.addNewBlankQuestionOption()
        //TODO Check
    }

    override fun updateQuestionOptionText(questionOption: SelQuestionOption) {
        //TODO
    }

    override fun handleRemoveSelQuestionOption(questionOption: SelQuestionOption) {
        //TODO
    }

}
