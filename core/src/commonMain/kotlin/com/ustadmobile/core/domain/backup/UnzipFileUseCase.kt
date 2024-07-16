package com.ustadmobile.core.domain.backup

import com.ustadmobile.core.util.ZipProgress
import kotlinx.coroutines.flow.Flow


interface UnzipFileUseCase {
    operator fun invoke(zipFilePath: String): Flow<ZipProgress>
}
