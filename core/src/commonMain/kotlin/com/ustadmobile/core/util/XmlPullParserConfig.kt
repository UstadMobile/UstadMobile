package com.ustadmobile.core.util

/**
 * This class represents the configuration required to get a XmlPullParser in a Kotlin multiplatform
 * way. The config includes the source text and/or input url and settings that should be applied
 * to the XmlPullParserFactory (e.g. namespaceAware, features, etc).
 */
class XmlPullParserConfig internal constructor(val namespaceAware: Boolean,
                                               val features: Map<String, Boolean>,
                                                internal val xmlText: String? = null) {

    class XmlPullParserConfigBuilder(var namespaceAware: Boolean = false,
                                     internal val features: MutableMap<String, Boolean> = mutableMapOf()) {
        fun setFeature(name: String, state: Boolean) {
            features[name] = state
        }
    }

    companion object {
        fun fromString(xmlText: String, configAction: XmlPullParserConfigBuilder.() -> Unit= {}) : XmlPullParserConfig{
            val config = XmlPullParserConfigBuilder().apply(configAction)
            return XmlPullParserConfig(config.namespaceAware, config.features.toMap(), xmlText)
        }
    }
}