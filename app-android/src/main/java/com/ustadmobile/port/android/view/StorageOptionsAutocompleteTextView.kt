package com.ustadmobile.port.android.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import java.io.File

class StorageOptionsAutocompleteTextView: DropDownListAutoCompleteTextView<UMStorageDir> {

    private val messageIdDropdownAdapter = object: DropDownListAutoCompleteAdapter<UMStorageDir> {

        override fun getId(item: UMStorageDir): Long {return 0}

        @SuppressLint("UsableSpace")
        override fun getText(item: UMStorageDir): String {
            return String.format(UstadMobileSystemImpl.instance.getString(
                    MessageID.download_storage_option_device, context as Any), item.name,
                    UMFileUtil.formatFileSize(File(item.dirURI).usableSpace))
        }

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

    private fun init() {
        dropDownListAdapter = messageIdDropdownAdapter
    }

}