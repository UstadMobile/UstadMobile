package com.ustadmobile.port.android.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.toughra.ustadmobile.R
import com.ustadmobile.door.RepositoryLoadHelper
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_FAILED_CONNECTION_ERR
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_FAILED_NOCONNECTIVITYORPEERS
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_LOADED_NODATA
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_LOADED_WITHDATA
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_LOADING_CLOUD
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_LOADING_MIRROR
import kotlinx.android.synthetic.main.view_repo_loading_status.view.*

class RepoLoadingStatusView: CoordinatorLayout, RepositoryLoadHelper.RepoLoadCallback {

    private var imageResource: Int = 0

    private var message: String = ""

    data class RepoLoadingStatusInfo(val progressVisible: Boolean,
                                     val imageResourceToShow: Int,
                                     var textIdToShow: Int)

    val statusToStatusInfoMap = mapOf(
            STATUS_LOADING_MIRROR to RepoLoadingStatusInfo(true, 0, 0 ))

    var emptyStatusText: Int
        get() = statusToStatusInfoMap[STATUS_LOADED_NODATA]?.textIdToShow ?: -1
        set(value) {
            statusToStatusInfoMap[STATUS_LOADED_NODATA]?.textIdToShow = value
        }

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

    fun setEmptyView(imageResource: Int, message: String){
        this.imageResource = imageResource
        this.message = message
    }

    override fun onLoadStatusChanged(status: Int, remoteDevice: String?) {
        when(status){
            STATUS_LOADING_CLOUD -> {
                this.visibility = View.VISIBLE
                statusViewProgress.visibility = View.VISIBLE
                statusViewImageInner.tag = R.drawable.ic_cloud_download_black_24dp
                statusViewText.text = context.getString(R.string.repo_loading_status_loading_cloud)
            }

            STATUS_LOADING_MIRROR -> {
                this.visibility = View.VISIBLE
                statusViewImageInner.tag = R.drawable.ic_cloud_download_black_24dp
                statusViewProgress.visibility = View.VISIBLE
                statusViewText.text = context.getString(R.string.repo_loading_status_loading_mirror)
            }

            STATUS_LOADED_WITHDATA -> {
                this.visibility = View.GONE
            }

            STATUS_LOADED_NODATA -> {
                statusViewProgress.visibility = View.GONE
                this.visibility = View.VISIBLE
                statusViewText.text = message
                statusViewImage.setImageResource(imageResource)
            }

            STATUS_FAILED_NOCONNECTIVITYORPEERS -> {
                this.visibility = View.VISIBLE
                statusViewProgress.visibility = View.GONE
                statusViewImageInner.visibility = View.GONE
                statusViewImageInner.tag = R.drawable.ic_signal_cellular_connected_no_internet_4_bar_black_24dp
                statusViewText.text = context.getString(R.string.repo_loading_status_failed_noconnection)
            }

            STATUS_FAILED_CONNECTION_ERR -> {
                this.visibility = View.VISIBLE
                statusViewProgress.visibility = View.GONE
                statusViewImageInner.visibility = View.GONE
                statusViewImageInner.tag = R.drawable.ic_signal_cellular_connected_no_internet_4_bar_black_24dp
                statusViewText.text = context.getString(R.string.repo_loading_status_failed_connection_error)
            }
        }
    }
}