package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemPersonListItemBinding
import com.ustadmobile.core.controller.PersonListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.ListViewAddMode
import com.ustadmobile.core.view.PersonListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography

interface InviteWithLinkHandler{
    fun handleClickInviteWithLink()
}

class PersonListFragment() : UstadListViewFragment<Person, PersonWithDisplayDetails>(),
        PersonListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener,
        InviteWithLinkHandler{


    override fun handleClickInviteWithLink() {
        mPresenter?.handleClickInviteWithLink()

    }

    private var mPresenter: PersonListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in PersonWithDisplayDetails>?
        get() = mPresenter

    private var inviteWithLinkRecyclerViewAdapter: InviteWithLinkRecyclerViewAdapter? = null

    override var autoMergeRecyclerViewAdapter: Boolean = false

    override var inviteViaLinkVisibile: Boolean
        get() = mUstadListHeaderRecyclerViewAdapter?.newItemVisible ?: false
        set(value) {
            inviteWithLinkRecyclerViewAdapter?.visible = value
        }

    override var addMode: ListViewAddMode = ListViewAddMode.NONE
        get() = field
        set(value) {
            mDataBinding?.addMode = value
            mUstadListHeaderRecyclerViewAdapter?.newItemVisible = (value == ListViewAddMode.FIRST_ITEM)
            fabManager?.visible = (value == ListViewAddMode.FAB)

            field = value
        }

    class PersonListViewHolder(val itemBinding: ItemPersonListItemBinding)
        : RecyclerView.ViewHolder(itemBinding.root)


    class PersonListRecyclerAdapter(var presenter: PersonListPresenter?)
        : SelectablePagedListAdapter<PersonWithDisplayDetails, PersonListViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonListViewHolder {
            val itemBinding = ItemPersonListItemBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false)
            itemBinding.presenter = presenter
            itemBinding.selectablePagedListAdapter = this
            return PersonListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: PersonListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.person = item
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_search).isVisible = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = PersonListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, di, viewLifecycleOwner).withViewLifecycle()
        
        inviteWithLinkRecyclerViewAdapter = InviteWithLinkRecyclerViewAdapter(this, mPresenter)
        inviteWithLinkRecyclerViewAdapter?.code = arguments?.get(UstadView.ARG_CODE)?.toString()
        inviteWithLinkRecyclerViewAdapter?.entityName = arguments?.get(UstadView.ARG_ENTITY_NAME)?.toString()
        inviteWithLinkRecyclerViewAdapter?.tableId = arguments?.get(UstadView.ARG_CODE_TABLE)?.toString()?.toInt()?:0

        mDataRecyclerViewAdapter = PersonListRecyclerAdapter(mPresenter)
        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(this,
                requireContext().getString(R.string.add_a_new_person),
                onClickSort = this, sortOrderOption = mPresenter?.sortOptions?.get(0))

        mListStatusAdapter = ListStatusRecyclerViewAdapter(viewLifecycleOwner)
        mMergeRecyclerViewAdapter = ConcatAdapter(mUstadListHeaderRecyclerViewAdapter,
                inviteWithLinkRecyclerViewAdapter,
                mDataRecyclerViewAdapter , mListStatusAdapter)
        mDataBinding?.fragmentListRecyclerview?.adapter = mMergeRecyclerViewAdapter
        return view
    }

    override fun onResume() {
        //Set text first so that it will expand to the correct size as required
        mActivityWithFab?.activityFloatingActionButton?.text =
                requireContext().getString(R.string.person)
        super.onResume()
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(v: View?) {
        if (v?.id == R.id.item_createnew_layout)
            mPresenter?.handleClickAddNewItem()
        else {
            super.onClick(view)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.personDao

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<PersonWithDisplayDetails> = object
            : DiffUtil.ItemCallback<PersonWithDisplayDetails>() {
            override fun areItemsTheSame(oldItem: PersonWithDisplayDetails,
                                         newItem: PersonWithDisplayDetails): Boolean {
                return oldItem.personUid == newItem.personUid
            }

            override fun areContentsTheSame(oldItem: PersonWithDisplayDetails,
                                            newItem: PersonWithDisplayDetails): Boolean {
                return oldItem.firstNames == newItem.firstNames &&
                        oldItem.lastName == newItem.lastName
            }
        }
    }
}

data class PersonObject(
    val firstName: String,
    val lastName: String,
    val image: String
)

@Composable
fun PersonListScreen(){

    val peopleList: List<PersonObject> = remember {
        listOf(PersonObject("admin",
            "ustadMobile", "")
        )
    }

    Column(
        modifier = Modifier.fillMaxHeight()
            .fillMaxWidth().background(Color.White),
    ) {


        Spacer(modifier = Modifier.height(20.dp))

        Content(peopleList)
    }
}

@Composable
private fun Content(peopleList: List<PersonObject>){

    Column(
        modifier = Modifier
            .padding(8.dp, 0.dp, 8.dp, 8.dp)
            .fillMaxHeight()
    ) {
        Header()

        Spacer(modifier = Modifier.height(15.dp))

        Box(modifier = Modifier.weight(1f)){
            PeopleList(peopleList)
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ){
            AddPersonButton{}
        }
    }
}

@Composable
private fun Header(){
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.first_name),
            style = Typography.body2,
            color = Color.Black)

        Spacer(modifier = Modifier.width(5.dp))

        Image(modifier = Modifier.size(15.dp),
            painter = painterResource(id = R.drawable.ic_arrow_downward_24),
            contentDescription = null)
    }
}

@Composable
private fun PeopleList(peopleList: List<PersonObject>){

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        state = LazyListState(),
    ) {
        itemsIndexed(peopleList) { _, item ->
            PersonItem(item)
        }
    }
}

@Composable
private fun PersonItem(person: PersonObject){

    Row(verticalAlignment = Alignment.CenterVertically) {

        CircleImageView(R.drawable.ic_account_circle_black_24dp)

        Spacer(modifier = Modifier.width(5.dp))

        Column() {
            Text(person.firstName,
                style = Typography.h4)

            Text(person.lastName,
                style = Typography.body1,
                color = Color.Gray)
        }
    }
}

@Composable
private fun CircleImageView(image: Int) {
    Image(
        painter = painterResource(image),
        contentDescription = "avatar",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(45.dp)
            .clip(CircleShape)
    )
}

@Composable
private fun AddPersonButton(onClick: () -> Unit) {
    Button(
        shape = RoundedCornerShape(50),
        onClick = {onClick()},
        modifier = Modifier.padding(12.dp)
            .height(45.dp)
            .width(120.dp),
        elevation = null,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colorResource(R.color.secondaryColor),
            contentColor = Color.Transparent,
            disabledBackgroundColor = Color.Transparent,),
    ) {
        Row (
            horizontalArrangement = Arrangement.End) {
            Image(
                painter = painterResource(id = R.drawable.ic_add_black_24dp),
                contentDescription = null,
                modifier = Modifier.width(25.dp),
                colorFilter = ColorFilter.tint(color = Color.Black))
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = stringResource(R.string.person),
                color = Color.Black,
                textAlign = TextAlign.Center,
                style = Typography.h5
            )
        }
    }
}

@Preview
@Composable
private fun PersonEditPreview() {
    PersonListScreen()
}