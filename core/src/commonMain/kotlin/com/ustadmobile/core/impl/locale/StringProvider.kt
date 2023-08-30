package com.ustadmobile.core.impl.locale

import dev.icerock.moko.resources.StringResource

/**
 * StringProvider is a simple interface for use by multiplatform code to access strings using the
 * current display locale. This is implemented as follows:
 *
 *  On Android: display locale is set in the context, there is a single instance that uses
 *  applicationContext to lookup strings.
 *  On JVM/JS: display locale is set in a preference key. The StringProvider knows the current
 *  locale and will use MokoResource .localized function to return the correct string.
 *
 *  The display locale might not be the same as the system locale (e.g. a user phone might be set
 *  to use English or Farsi, but the user wants the app interface in Pashto etc).
 */
interface StringProvider {

    operator fun get(stringResource: StringResource): String

}