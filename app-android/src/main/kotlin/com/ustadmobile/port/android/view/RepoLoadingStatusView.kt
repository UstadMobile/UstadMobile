package com.ustadmobile.port.android.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.LifecycleOwner
import com.toughra.ustadmobile.R
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.door.RepositoryLoadHelper
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_FAILED_CONNECTION_ERR
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_FAILED_NOCONNECTIVITYORPEERS
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_LOADED_NODATA
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_LOADING_CLOUD
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_LOADING_MIRROR
import com.ustadmobile.door.ext.isRepositoryLiveData
import kotlinx.android.synthetic.main.view_repo_loading_status.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * View that is used to show the status of a recyclerview loading from repository data.
 *
 * 1. Call reset() if this view might have shown loading status connected to a previous dataset
 * 2. Set the RecyclerviewAdapter onFirstItemLoaded to this view
 * 3. Call observeRepoStatus with the LiveData that has been returned from a RepositoryLoadHelper.wrapLiveData
 * call. This LiveData also has a LiveData object with the loading status.
 *
 */
class RepoLoadingStatusView: CoordinatorLayout, FistItemLoadedListener,
        DoorObserver<RepositoryLoadHelper.RepoLoadStatus> {

    data class RepoLoadingStatusInfo(val progressVisible: Boolean,
                                     var imageResourceToShow: Int,
                                     var textIdToShow: Int)

    private val statusToStatusInfoMap = mapOf(
            STATUS_LOADING_CLOUD to RepoLoadingStatusInfo(true,
                    R.drawable.ic_cloud_download_black_24dp, R.string.repo_loading_status_loading_cloud ),
            STATUS_LOADING_MIRROR to RepoLoadingStatusInfo(true,
                    R.drawable.ic_loading_from_nearby_device, R.string.repo_loading_status_loading_mirror),
            STATUS_LOADED_NODATA to RepoLoadingStatusInfo(false,
                    R.drawable.ic_file_download_black_24dp, R.string.repo_loading_status_loaded_empty),
            STATUS_FAILED_CONNECTION_ERR to RepoLoadingStatusInfo(false,
                    R.drawable.ic_error_black_24dp, R.string.repo_loading_status_failed_connection_error),
            STATUS_FAILED_NOCONNECTIVITYORPEERS to RepoLoadingStatusInfo(false,
                    R.drawable.ic_signal_cellular_connected_no_internet_4_bar_black_24dp, R.string.repo_loading_status_failed_noconnection))

    var emptyStatusText: Int
        get() = statusToStatusInfoMap[STATUS_LOADED_NODATA]?.textIdToShow ?: -1
        set(value) {
            statusToStatusInfoMap[STATUS_LOADED_NODATA]?.textIdToShow = value
        }
    var emptyStatusImage: Int
        get() = statusToStatusInfoMap[STATUS_LOADED_NODATA]?.imageResourceToShow ?: -1
        set(value) {
            statusToStatusInfoMap[STATUS_LOADED_NODATA]?.imageResourceToShow = value
        }

    private var statusLiveData: DoorLiveData<RepositoryLoadHelper.RepoLoadStatus>? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    fun init(){
        View.inflate(context, R.layout.view_repo_loading_status, this)
    }

    fun observerRepoStatus(liveData: DoorLiveData<*>, lifecycleOwner: LifecycleOwner){
        if(liveData.isRepositoryLiveData()) {
            statusLiveData?.removeObserver(this)
            val newStatusLiveData = (liveData as RepositoryLoadHelper<*>.LiveDataWrapper2<*>).loadingStatus
            newStatusLiveData.observe(lifecycleOwner, this)
            statusLiveData = newStatusLiveData
        }
    }

    override fun onChanged(t: RepositoryLoadHelper.RepoLoadStatus?) {
        val loadingStatusInfo = if(t != null) statusToStatusInfoMap[t.loadStatus] else null
        if(loadingStatusInfo != null){
            GlobalScope.launch(Dispatchers.Main) {
                statusViewProgress.visibility = if(loadingStatusInfo.progressVisible) View.VISIBLE else View.GONE
                statusViewTextInner.visibility = if(loadingStatusInfo.progressVisible) View.VISIBLE else View.GONE
                statusViewText.visibility = if(!loadingStatusInfo.progressVisible) View.VISIBLE else View.GONE
                statusViewImageInner.visibility = if(loadingStatusInfo.progressVisible) View.VISIBLE else View.GONE
                statusViewImageInner.tag = loadingStatusInfo.imageResourceToShow
                statusViewImage.visibility = if(!loadingStatusInfo.progressVisible) View.VISIBLE else View.GONE
                if(loadingStatusInfo.progressVisible){
                    statusViewTextInner.text = context.getString(loadingStatusInfo.textIdToShow)
                    statusViewImageInner.setImageResource(loadingStatusInfo.imageResourceToShow)
                }else{
                    statusViewText.text = context.getString(loadingStatusInfo.textIdToShow)
                    statusViewImage.setImageResource(loadingStatusInfo.imageResourceToShow)
                }
            }
        }
    }

    override fun onFirstItemLoaded() {
        this.visibility = View.GONE
    }

    fun reset() {
        this.visibility = View.VISIBLE
    }
}