package com.ustadmobile.core.domain.phonenumber

interface IPhoneNumberUtil {

    fun getAsYouTypeFormatter(regionCode: String?): IAsYouTypeFormatter

    fun getSupportedRegions(): Set<String>

    fun getCountryCodeForRegion(region: String): Int

    fun getRegionForCountryCode(countryCode: Int) :String

    fun parse(numberToParse: String, defaultRegion: String?): IPhoneNumber

}