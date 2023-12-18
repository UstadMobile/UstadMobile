package com.ustadmobile.core.domain.phonenumber

import com.ustadmobile.core.wrappers.libphonenumber.parsePhoneNumber

class PhoneNumberUtilJs: IPhoneNumberUtil {
    override fun getAsYouTypeFormatter(regionCode: String?): IAsYouTypeFormatter {
        throw IllegalStateException("getAsYouTypeFormatter not supported on JS")
    }

    override fun getSupportedRegions(): Set<String> {
        TODO("Not yet implemented")
    }

    override fun getCountryCodeForRegion(region: String): Int {
        TODO("Not yet implemented")
    }

    override fun getRegionForCountryCode(countryCode: Int): String {
        TODO("Not yet implemented")
    }

    override fun parse(numberToParse: String, defaultRegion: String?): IPhoneNumber {
        return PhoneNumberJs(parsePhoneNumber(numberToParse, defaultRegion, null))
    }

    override fun isValidNumber(number: IPhoneNumber): Boolean {
        return (number as PhoneNumberJs).phoneNumber.isValid()
    }

    override fun formatInternational(number: IPhoneNumber): String {
        return (number as PhoneNumberJs).phoneNumber.formatInternational()
    }
}