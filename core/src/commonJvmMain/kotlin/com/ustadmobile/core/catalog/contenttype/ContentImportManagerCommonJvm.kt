package com.ustadmobile.core.catalog.contenttype

open class ContentImportManagerCommonJvm(val plugins: List<ContentTypePlugin>) : ContentImportManager{

    override suspend fun extractMetadata(filePath: String) : ImportedContentEntryMetaData? {
        //TODO: run this here as it is common
    }


}