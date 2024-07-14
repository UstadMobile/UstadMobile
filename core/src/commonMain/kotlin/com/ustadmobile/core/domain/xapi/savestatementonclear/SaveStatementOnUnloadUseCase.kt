package com.ustadmobile.core.domain.xapi.savestatementonclear


/**
 * Like SaveStatementOnClearUseCase... however on the web we have another issue: when the user
 * navigates away from the app itself. There isn't anything like Quartz or WorkManager on the
 * browser. sendBeacon allows queueing up a HTTP post request that won't delay navigation.
 *
 * This should only be used when the app is being unloaded. If the user is navigating within the
 * app we can just use a coroutine launch
 */
interface SaveStatementOnUnloadUseCase: SaveStatementOnClearUseCase
