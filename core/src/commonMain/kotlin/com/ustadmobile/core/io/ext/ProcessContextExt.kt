package com.ustadmobile.core.io.ext

import com.ustadmobile.core.contentjob.ProcessContext
import com.ustadmobile.door.DoorUri
import org.kodein.di.DI


expect suspend fun ProcessContext.getLocalUri(fileUri: DoorUri,  context: Any, di: DI): DoorUri