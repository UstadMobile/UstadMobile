package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.ContentEntry

object LicenceConstants {

    val LICENSE_MESSAGE_IDS = listOf(
        MessageIdOption2(MessageID.licence_type_cc_by, ContentEntry.LICENSE_TYPE_CC_BY),
        MessageIdOption2(MessageID.licence_type_cc_by_sa, ContentEntry.LICENSE_TYPE_CC_BY_SA),
        MessageIdOption2(MessageID.licence_type_cc_by_sa_nc, ContentEntry.LICENSE_TYPE_CC_BY_SA_NC),
        MessageIdOption2(MessageID.licence_type_cc_by_nc, ContentEntry.LICENSE_TYPE_CC_BY_NC),
        MessageIdOption2(MessageID.licence_type_all_rights, ContentEntry.ALL_RIGHTS_RESERVED),
        MessageIdOption2(MessageID.licence_type_cc_by_nc_sa, ContentEntry.LICENSE_TYPE_CC_BY_NC_SA),
        MessageIdOption2(MessageID.licence_type_public_domain, ContentEntry.LICENSE_TYPE_PUBLIC_DOMAIN),
        MessageIdOption2(MessageID.other, ContentEntry.LICENSE_TYPE_OTHER),
        MessageIdOption2(MessageID.license_type_cc_0, ContentEntry.LICENSE_TYPE_CC_0),
    )
}