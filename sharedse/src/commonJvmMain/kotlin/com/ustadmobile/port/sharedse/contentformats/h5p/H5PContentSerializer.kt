package com.ustadmobile.port.sharedse.contentformats.h5p

import kotlinx.serialization.Serializable

@Serializable
data class H5PContentSerializer(
        val license: String? = null,
        val embedTypes: List<String?>? = null,
        val metaKeywords: String? = null,
        val mainLibrary: String? = null,
        val preloadedDependencies: List<PreloadedDependenciesItem?>? = null,
        val author: String? = null,
        val language: String? = null,
        val title: String? = null,
        val contentType: String? = null,
        val metaDescription: String? = null
)

@Serializable
data class PreloadedDependenciesItem(
        val majorVersion: Int? = null,
        val minorVersion: Int? = null,
        val machineName: String? = null
)