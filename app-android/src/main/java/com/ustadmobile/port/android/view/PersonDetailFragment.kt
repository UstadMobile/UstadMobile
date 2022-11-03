package com.ustadmobile.port.android.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentPersonDetailBinding
import com.toughra.ustadmobile.databinding.ItemClazzEnrolmentWithClazzDetailBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.PersonDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.ui.theme.ui.theme.UstadMobileTheme
import com.ustadmobile.port.android.view.binding.MODE_START_OF_DAY
import com.ustadmobile.port.android.view.util.ForeignKeyAttachmentUriAdapter
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import kotlin.text.Typography

class PersonDetailFragment: UstadDetailFragment<PersonWithPersonParentJoin>(), PersonDetailView{

    private var mBinding: FragmentPersonDetailBinding? = null

    private var mPresenter: PersonDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    var dbRepo: UmAppDatabase? = null

    class ClazzEnrolmentWithClazzRecyclerAdapter(var presenter: PersonDetailPresenter?)
        : ListAdapter<ClazzEnrolmentWithClazzAndAttendance,
                ClazzEnrolmentWithClazzRecyclerAdapter.ClazzEnrolmentWithClazzViewHolder>(
                    DIFFUTIL_CLAZZMEMBERWITHCLAZZ) {

            class ClazzEnrolmentWithClazzViewHolder(val binding: ItemClazzEnrolmentWithClazzDetailBinding)
                    : RecyclerView.ViewHolder(binding.root)

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                    : ClazzEnrolmentWithClazzViewHolder {

                return ClazzEnrolmentWithClazzViewHolder(ItemClazzEnrolmentWithClazzDetailBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false).apply {
                    mPresenter = presenter
                })
            }

            override fun onBindViewHolder(holder: ClazzEnrolmentWithClazzViewHolder, position: Int) {
                holder.binding.clazzEnrolmentWithClazz = getItem(position)
            }
        }

    override var clazzes: DataSource.Factory<Int, ClazzEnrolmentWithClazzAndAttendance>? = null
        get() = field
        set(value) {
            clazzesLiveData?.removeObserver(clazzMemberWithClazzObserver)
            field = value
            val clazzMemberDao = dbRepo?.clazzEnrolmentDao ?: return
            clazzesLiveData = value?.asRepositoryLiveData(clazzMemberDao)
            clazzesLiveData?.observe(viewLifecycleOwner, clazzMemberWithClazzObserver)
        }

    override var changePasswordVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.changePasswordVisibility = if(value) View.VISIBLE else View.GONE
        }

    override var chatVisibility: Boolean = false
        set(value) {
            field = value
            mBinding?.chatVisibility = if(value) View.VISIBLE else View.GONE
        }

    override var showCreateAccountVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.createAccountVisibility = if(value) View.VISIBLE else View.GONE
        }

    private var clazzesLiveData: LiveData<PagedList<ClazzEnrolmentWithClazzAndAttendance>>? = null

    private var clazzEnrolmentWithClazzRecyclerAdapter: ClazzEnrolmentWithClazzRecyclerAdapter? = null

    private val clazzMemberWithClazzObserver = Observer<PagedList<ClazzEnrolmentWithClazzAndAttendance>?> {
        t -> clazzEnrolmentWithClazzRecyclerAdapter?.submitList(t)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val rootView: View

        clazzEnrolmentWithClazzRecyclerAdapter = ClazzEnrolmentWithClazzRecyclerAdapter(
            null)
        mBinding = FragmentPersonDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.createAccountVisibility = View.GONE
            it.changePasswordVisibility = View.GONE
            it.chatVisibility = View.GONE
            it.classesRecyclerview.layoutManager = LinearLayoutManager(requireContext())
            it.classesRecyclerview.adapter = clazzEnrolmentWithClazzRecyclerAdapter
        }

        val accountManager: UstadAccountManager by instance()
        dbRepo = on(accountManager.activeAccount).direct.instance(tag = DoorTag.TAG_REPO)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = PersonDetailPresenter(requireContext(), arguments.toStringMap(), this,
            di, viewLifecycleOwner).withViewLifecycle()
        clazzEnrolmentWithClazzRecyclerAdapter?.presenter = mPresenter
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())
        mBinding?.presenter = mPresenter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding?.classesRecyclerview?.adapter = null
        clazzEnrolmentWithClazzRecyclerAdapter = null
        dbRepo = null
        mBinding = null
        mPresenter = null
        entity = null
    }

    override fun onResume() {
        super.onResume()

        if(mBinding?.person != null) {
            (activity as? AppCompatActivity)?.supportActionBar?.title =
                    mBinding?.person?.firstNames + " " + mBinding?.person?.lastName
        }
    }

    override var entity: PersonWithPersonParentJoin? = null
        get() = field
        set(value) {
            field = value
            mBinding?.person = value
            ustadFragmentTitle = value?.fullName()
            mBinding?.dateTimeMode = MODE_START_OF_DAY
            mBinding?.timeZoneId = "UTC"
        }


    companion object {

        val DIFFUTIL_CLAZZMEMBERWITHCLAZZ =
                object: DiffUtil.ItemCallback<ClazzEnrolmentWithClazzAndAttendance>() {
            override fun areItemsTheSame(oldItem: ClazzEnrolmentWithClazzAndAttendance,
                                         newItem: ClazzEnrolmentWithClazzAndAttendance): Boolean {
                return oldItem.clazzEnrolmentUid == newItem.clazzEnrolmentUid
            }

            override fun areContentsTheSame(oldItem: ClazzEnrolmentWithClazzAndAttendance,
                                            newItem: ClazzEnrolmentWithClazzAndAttendance): Boolean {
                return oldItem == newItem
            }
        }

        @JvmStatic
        val FOREIGNKEYADAPTER_PERSON = object: ForeignKeyAttachmentUriAdapter {
            override suspend fun getAttachmentUri(foreignKey: Long, dbToUse: UmAppDatabase): String? {
                return dbToUse.personPictureDao.findByPersonUidAsync(foreignKey)?.personPictureUri
            }
        }

        @JvmField
        val FIELD_ICON_ID_MAP : Map<Int, Int> =
            mapOf(CustomField.ICON_PHONE to R.drawable.ic_phone_black_24dp,
                    CustomField.ICON_PERSON to R.drawable.ic_person_black_24dp,
                    CustomField.ICON_CALENDAR to R.drawable.ic_event_black_24dp,
                    CustomField.ICON_EMAIL to R.drawable.ic_email_black_24dp,
                    CustomField.ICON_ADDRESS to R.drawable.ic_location_pin_24dp)

    }

}

@Composable
private fun PersonDetailScreen(){
    Column(
        modifier = Modifier.fillMaxHeight()
            .fillMaxWidth().background(Color.White),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            TitleBar()

            Spacer(modifier = Modifier.height(20.dp))

            Content()
        }

        Row(
            horizontalArrangement = Arrangement.End
        ) {
            Box(modifier = Modifier.weight(0.6F)){}

            Box(modifier = Modifier.weight(0.4F)){
                EditButton(){}
            }
        }
    }
}

@Composable
private fun TitleBar(){
    Row (
        modifier = Modifier
            .background(colorResource(R.color.primaryColor))
            .height(60.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackButton(){}

            Text(
                stringResource(R.string.edit_person),
                style = Typography.h6,
                color = Color.White)
        }

        AccountButton(){}
    }
}

@Composable
private fun BackButton(onClick: () -> Unit){
    TextButton(onClick = {onClick},
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colorResource(R.color.primaryColor),
            disabledBackgroundColor = colorResource(R.color.primaryColor)
        ),){
        Image(
            painter = painterResource(id = R.drawable.ic_arrow_back_white_24dp),
            contentDescription = null)
    }
}

@Composable
private fun AccountButton(onClick: () -> Unit){
    Button(onClick = {onClick},
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colorResource(R.color.primaryColor),
            disabledBackgroundColor = colorResource(R.color.primaryColor)
        ),){
        Image(
            painter = painterResource(id = R.drawable.ic_account_circle_black_24dp),
            contentDescription = null,
            colorFilter = ColorFilter.tint(color = Color.White),
            modifier = Modifier
                .size(35.dp))
    }
}


@Composable
private fun Content(){
    Column(
        modifier = Modifier
            .fillMaxWidth(),
    ) {

        ChangePasswordButton(stringResource(R.string.change_password), R.drawable.person_with_key){}

        Spacer(modifier = Modifier.height(10.dp))

        Divider(color = Color.LightGray, modifier = Modifier.fillMaxWidth().width(1.dp))

        Spacer(modifier = Modifier.height(10.dp))

        Column(modifier = Modifier.padding(12.dp)) {

            Text(stringResource(R.string.import_content),
                style = Typography.body1,
                color = Color.Black)

            Spacer(modifier = Modifier.height(20.dp))

            UserRow()

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(onClick = {}){
                Text(stringResource(R.string.contact_details),
                    style = Typography.body1,
                    color = Color.Black)
            }

            TextButton(onClick = {}){
                Text(stringResource(R.string.classes),
                    style = Typography.body1,
                    color = Color.Black)
            }

            TextButton(onClick = {}){
                Text(stringResource(R.string.roles_and_permissions),
                    style = Typography.body1,
                    color = Color.Black)
            }
        }
    }
}
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ChangePasswordButton(text: String, icon: Int, onClick: () -> Unit) {
    var selected by remember { mutableStateOf(false) }
    var imageColor by remember { mutableStateOf(ColorFilter.tint(color = Color.DarkGray)) }
    var primaryColor = colorResource(R.color.primaryColor)
    Button(
        elevation = null,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.Transparent,
            contentColor = if (selected) colorResource(R.color.primaryColor) else Color.Black,
            disabledBackgroundColor = Color.Transparent,),
        onClick = { onClick() },
        modifier = Modifier
            .padding(0.dp)
            .pointerInteropFilter {
                when (it.action) {
                    MotionEvent.ACTION_DOWN -> {
                        selected = true
                        imageColor = ColorFilter.tint(color = primaryColor)
                    }

                    MotionEvent.ACTION_UP  -> {
                        selected = false
                        imageColor = ColorFilter.tint(color = Color.DarkGray)
                    }
                }
                true
            }
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .padding(0.dp)
        ){
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,)
            {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = "",
                    modifier = Modifier.size(35.dp),
                    colorFilter = imageColor)
                Spacer(modifier = Modifier.height(5.dp))
                Text(text)
            }
        }
    }
}

@Composable
private fun EditButton(onClick: () -> Unit) {
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
            Text(
                text = stringResource(R.string.edit),
                color = Color.Black,
                textAlign = TextAlign.Center,
                style = Typography.body1
            )
            Spacer(modifier = Modifier.width(5.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_edit_white_24dp),
                contentDescription = null,
                modifier = Modifier.width(25.dp),
                colorFilter = ColorFilter.tint(color = Color.Black))
        }
    }
}

@Composable
private fun UserRow(){
    Row {
        Image(
            painter = painterResource(id = R.drawable.ic_account_circle_black_24dp),
            contentDescription = null,
            colorFilter = ColorFilter.tint(color = Color.DarkGray),
            modifier = Modifier
                .size(35.dp))

        Spacer(modifier = Modifier.width(5.dp))

        Column {
            Text(stringResource(R.string.change_password),
                style = Typography.body1,
                color = Color.Black)

            Text(stringResource(R.string.username),
                style = Typography.body1,
                color = Color.Gray)
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun PersonDetailPreview() {
    UstadMobileTheme {
        PersonDetailScreen()
    }
}

