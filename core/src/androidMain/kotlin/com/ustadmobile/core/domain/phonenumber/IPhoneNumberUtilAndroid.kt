package com.ustadmobile.core.domain.phonenumber

import io.michaelrocks.libphonenumber.android.PhoneNumberUtil

class IPhoneNumberUtilAndroid(
    private val phoneNumberUtil: PhoneNumberUtil
): IPhoneNumberUtil {
    override fun getAsYouTypeFormatter(regionCode: String?): IAsYouTypeFormatter {
        return phoneNumberUtil.getAsYouTypeFormatter(regionCode).asIAsYouTypeFormatter()
    }

    override fun getSupportedRegions(): Set<String> {
        return phoneNumberUtil.supportedRegions
    }

    override fun getCountryCodeForRegion(region: String): Int {
        return phoneNumberUtil.getCountryCodeForRegion(region)
    }

    override fun getRegionForCountryCode(countryCode: Int) :String {
        return phoneNumberUtil.getRegionCodeForCountryCode(countryCode)
    }

    override fun parse(numberToParse: String, defaultRegion: String?): IPhoneNumber {
        return PhoneNumberAndroid(phoneNumberUtil.parse(numberToParse, defaultRegion))
    }

}
