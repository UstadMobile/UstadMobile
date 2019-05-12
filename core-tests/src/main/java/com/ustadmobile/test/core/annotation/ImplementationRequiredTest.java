package com.ustadmobile.test.core.annotation;

/**
 * Used to indicate that this test is like an "abstract" class - it must be extended and run within
 * a given implementation, and cannot be run on it's own.
 *
 * Normally, we would just make the core of the test abstract and then extend it in each implementation,
 * but for some reason the AndroidTestRunner will attempt to instantiate it, and throw an exception.
 * Therefor we use this annotation as an argument for the "notAnnotation" with the
 * testInstrumentationRunnerArgument option in build.gradle to avoid the test runner attempting to
 * run it.
 */
public @interface ImplementationRequiredTest {
}
