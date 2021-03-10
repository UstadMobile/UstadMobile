package com.ustadmobile.core.io

import kotlinx.io.InputStream

/**
 * Our ConcatenatingInputStream and ConcatenatedInputStream descend from kotilinx.io.InputStream. The
 * expect declaration has no constructor. The actual objects have constructors. This creates a
 * compilation failure. This simple workaround class provides an empty constructor in all actual
 * implementations
 */
@Deprecated("This is dead on Kotlin 1.4")
expect abstract class KioInputStream(): InputStream {
}