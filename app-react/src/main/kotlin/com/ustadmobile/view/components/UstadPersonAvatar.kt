package com.ustadmobile.view.components

import com.ustadmobile.hooks.collectAttachmentUriSrc
import com.ustadmobile.hooks.useActiveDatabase
import mui.material.Avatar
import react.FC
import react.Props
import react.useMemo

external interface UstadPersonAvatarProps: Props {

    var personUid: Long

}

val UstadPersonAvatar = FC<UstadPersonAvatarProps> { props ->
    val db = useActiveDatabase()
    val personFlow = useMemo(props.personUid) {
        db.personPictureDao.findByPersonUidAsFlow(props.personUid)
    }

    val personPictureUri = personFlow.collectAttachmentUriSrc {
        it?.personPictureUri
    }

    Avatar {
        src = personPictureUri?.toString()
    }

}
