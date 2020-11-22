package com.ustadmobile.port.android.view

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.aakira.napier.Napier
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentNetworkNodeListBinding
import com.toughra.ustadmobile.databinding.ItemNetworkNodeBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.NetworkNodeListPresenter
import com.ustadmobile.core.networkmanager.LocalAvailabilityManager
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.NetworkNodeListView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.NetworkNode
import com.ustadmobile.port.android.view.util.ListSubmitObserver
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class NetworkNodeListFragment : UstadBaseFragment(), NetworkNodeListView {


    @RequiresApi(26)
    inner class CompanionDeviceHelper() {

        private val companionDeviceManager: CompanionDeviceManager by lazy(LazyThreadSafetyMode.NONE){
            requireActivity().getSystemService(CompanionDeviceManager::class.java)
        }

        fun request() {
            val serviceUuidVal = localAvailabilityManager?.serviceUuid ?:
                throw IllegalStateException("Cannot find serviceuuid with no localavailabilitymanager")
            val deviceFilter = BluetoothDeviceFilter.Builder()
                    .addServiceUuid(ParcelUuid.fromString(serviceUuidVal), null)
                    .build()
            val pairingRequest = AssociationRequest.Builder()
                    .addDeviceFilter(deviceFilter)
                    .setSingleDevice(false)
                    .build()

            Napier.d("Companion device manager : associate for service uuid: $serviceUuidVal")

            companionDeviceManager.associate(pairingRequest, object : CompanionDeviceManager.Callback() {

                override fun onDeviceFound(chooserLauncher: IntentSender) {
                    registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()){
                        val device : BluetoothDevice = it.data?.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)
                                ?: return@registerForActivityResult
                        device.createBond()
                    }.launch(IntentSenderRequest.Builder(chooserLauncher).build())
                }

                override fun onFailure(error: CharSequence?) {

                }
            }, null)
        }
    }


    class NetworkNodeRecyclerViewAdapter(): ListAdapter<NetworkNode, NetworkNodeRecyclerViewAdapter.NetworkNodeViewHolder>(DIFFUTIL_NETWORKNODE) {

        class NetworkNodeViewHolder(var binding: ItemNetworkNodeBinding): RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NetworkNodeViewHolder {
            return NetworkNodeViewHolder(
                    ItemNetworkNodeBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: NetworkNodeViewHolder, position: Int) {
            holder.binding.networkNode = getItem(position)
        }
    }

    override var deviceName: String? = null
        get() = field
        set(value) {
            field = value
        }

    var networkNodeListObserver: ListSubmitObserver<NetworkNode>? = null

    var mNetworkNodeRecyclerViewAdapter: NetworkNodeRecyclerViewAdapter? = null

    override var deviceList: DoorLiveData<List<NetworkNode>>? = null
        get() = field
        set(value) {
            if(view == null)
                return

            networkNodeListObserver?.also {
                field?.removeObserver(it)
                value?.observe(viewLifecycleOwner, it)
            }

            field = value
        }

    var binding: FragmentNetworkNodeListBinding? = null

    var mPresenter: NetworkNodeListPresenter? = null

    private var companionDeviceHelper: CompanionDeviceHelper? = null

    private var localAvailabilityManager: LocalAvailabilityManager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        binding = FragmentNetworkNodeListBinding.inflate(LayoutInflater.from(requireContext()),
                container, false).also {
            rootView = it.root
        }

        mNetworkNodeRecyclerViewAdapter = NetworkNodeRecyclerViewAdapter().also {
            networkNodeListObserver = ListSubmitObserver(it)
        }



        mPresenter = NetworkNodeListPresenter(requireContext(), arguments.toStringMap(), this,
                di)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val accountManager : UstadAccountManager = di.direct.instance()
        localAvailabilityManager = di.direct.on(accountManager.activeAccount).instance<LocalAvailabilityManager>()

        if(Build.VERSION.SDK_INT >= 26) {
            companionDeviceHelper = CompanionDeviceHelper()
        }

        fabManager?.visible = true
        fabManager?.text = requireContext().getString(R.string.add_device)
        fabManager?.onClickListener = {
            if(Build.VERSION.SDK_INT >= 26) {
                companionDeviceHelper?.request()
            }
        }

        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.networkNodeListRecycler?.adapter = null
        binding = null
        mPresenter = null
    }

    companion object {
        val DIFFUTIL_NETWORKNODE = object: DiffUtil.ItemCallback<NetworkNode>() {
            override fun areItemsTheSame(oldItem: NetworkNode, newItem: NetworkNode): Boolean {
                return oldItem.nodeId == newItem.nodeId
            }

            override fun areContentsTheSame(oldItem: NetworkNode, newItem: NetworkNode): Boolean {
                return oldItem.nodeName == newItem.nodeName &&
                        oldItem.bluetoothBondState == newItem.bluetoothBondState
            }
        }

        const val LOGTAG = "LocalAvailabilityList"

        const val SELECT_DEVICE_REQUEST_CODE = 42
    }
}