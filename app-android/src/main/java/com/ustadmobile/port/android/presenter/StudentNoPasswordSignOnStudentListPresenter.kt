package com.ustadmobile.port.android.presenter

import android.content.Context
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.controller.UstadBaseController
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Person
import org.kodein.di.DI
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ExternalAppPermission
import com.ustadmobile.port.android.authenticator.IAuthenticatorActivity
import com.ustadmobile.port.android.domain.CreateExternalAccessPermissionUseCase
import com.ustadmobile.port.android.domain.GrantExternalAccessUseCase
import com.ustadmobile.port.android.util.ext.getActivityContext
import com.ustadmobile.port.android.view.StudentNoPasswordSignOnStudentListView
import kotlinx.coroutines.launch
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class StudentNoPasswordSignOnStudentListPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: StudentNoPasswordSignOnStudentListView,
    di: DI,
    private val grantExternalAccessUseCase: GrantExternalAccessUseCase = GrantExternalAccessUseCase(di),
): UstadBaseController<StudentNoPasswordSignOnStudentListView>(
    context, arguments, view, di
) {

    data class NoPasswordStudentList(
        val students: DataSourceFactory<Int, Person>,
        val endpoint: Endpoint,
    )

    private lateinit var mEndpoint: Endpoint

    private var clazzUid: Long = 0

    private lateinit var authenticatorActivity: IAuthenticatorActivity

    private var mExtAccessPermission: ExternalAppPermission? = null

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        mEndpoint = arguments[UstadView.ARG_ACCOUNT_ENDPOINT]?.let { Endpoint(it) }
            ?: throw IllegalArgumentException("No endpoint specified")
        clazzUid = arguments[UstadView.ARG_CLAZZUID]?.toLong() ?: 0

        val db: UmAppDatabase = on(mEndpoint).direct.instance(tag = DoorTag.TAG_DB)

        view.studentList = NoPasswordStudentList(
            students = db.clazzEnrolmentDao.findActiveStudentsInCourse(clazzUid, systemTimeInMillis()),
            endpoint = mEndpoint
        )

        val dataStore = (context as Context).pendingRequestsDataStore
        val activity = (context as Context).getActivityContext()
        authenticatorActivity = (activity as IAuthenticatorActivity)

        presenterScope.launch {
            mExtAccessPermission = CreateExternalAccessPermissionUseCase(json, di).invoke(
                eapUidArg = 0,
                pendingRequestDataStore = dataStore,
                authenticatorActivity = authenticatorActivity,
            )
        }
    }

    fun onClickPerson(person: Person) {
        val extAccessPermission = mExtAccessPermission ?: return

        presenterScope.launch {
            try {
                grantExternalAccessUseCase(
                    endpoint = mEndpoint,
                    pendingRequestDataStore = (context as Context).pendingRequestsDataStore,
                    extAccessPermission = extAccessPermission,
                    activeAccountName = person.fullName(),
                    personUid = person.personUid,
                    authenticatorActivity = authenticatorActivity,
                    returnAccountName = true,
                )
            }catch(e: Exception) {
                view.showSnackBar("ERROR")
            }
        }
    }
}
