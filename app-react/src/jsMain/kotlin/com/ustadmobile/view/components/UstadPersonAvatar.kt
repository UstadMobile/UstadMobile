package com.ustadmobile.view.components

import com.ustadmobile.hooks.URI_NOT_READY
import com.ustadmobile.hooks.collectAttachmentUriSrc
import com.ustadmobile.hooks.useActiveDatabase
import mui.material.Avatar
import mui.material.Icon
import react.FC
import react.Props
import react.useMemo

external interface UstadPersonAvatarProps: Props {

    var personUid: Long

}

val UstadPersonAvatar = FC<UstadPersonAvatarProps> { props ->
    /* Disabled until update to MVVM. Query cancellation might cause issues
    val db = useActiveDatabase()
    val personFlow = useMemo(props.personUid) {
        db.personPictureDao.findByPersonUidAsFlow(props.personUid)
    }

    val personPictureUri = personFlow.collectAttachmentUriSrc(
        initialState = URI_NOT_READY,
        revokeOnCleanup = true,
    ) {
        it?.personPictureUri
    }

    /**
     * If there is an image already stored in the database, we want to avoid the flicker that would
     * otherwise be caused by an initial null value in the flow. collectAttachmetnUriSrc will
     * return URI_NOT_READY as a first value.
     */
    if(personPictureUri?.toString() != URI_NOT_READY.toString()) {
        Avatar {
            src = personPictureUri?.toString()
        }
    }*/
    Avatar {

    }

}
