package com.ustadmobile.libuicompose.util.phonenum

import androidx.compose.runtime.Composable
import com.ustadmobile.core.domain.phonenumber.IPhoneNumberUtil

@Composable
expect fun guessInitialPhoneCountryCode(
    phoneUtil: IPhoneNumberUtil
) : Int?
