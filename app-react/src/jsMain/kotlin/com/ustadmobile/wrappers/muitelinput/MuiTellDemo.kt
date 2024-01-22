package com.ustadmobile.wrappers.muitelinput

import com.ustadmobile.core.wrappers.libphonenumber.PhoneNumber
import com.ustadmobile.core.wrappers.libphonenumber.parsePhoneNumber
import react.FC
import react.Props
import react.dom.html.ReactHTML.br
import react.useEffect
import react.useState

val MuiTelInputDemo = FC<Props> {
    var phoneNum by useState { "+18187187466" }

    var libphoneResult: PhoneNumber? by useState { null }

    useEffect(phoneNum) {
        libphoneResult = try {
            parsePhoneNumber(phoneNum, null, null)
        }catch(e: Throwable) {
            null
        }
    }

    MuiTelInput {
        value = phoneNum
        onChange = { number, muiInfo ->
            phoneNum = number
        }
    }

    br()

    + ("Str: " + phoneNum)
    br()
    + ("LibphoneNumber: " + libphoneResult?.number)
    br()
    + ("valid: " + libphoneResult?.isValid())
    br()
    + ("possible: " + libphoneResult?.isPossible())
}
