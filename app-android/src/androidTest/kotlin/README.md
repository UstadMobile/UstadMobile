Ustad Mobile Core Tests

J2ME does not have the same reflection API that is used by JUnit;
so we use the prebop preprocessor to preprocess test files from 
the same source for the appropriate target.

Platform is defined as a number in the umplatform variable:
1 - Android
2 - J2ME

For example the import line becomes

<pre>
/* $if umplatform == 2  $
    import org.j2meunit.framework.TestCase;
 $else$ */
    import junit.framework.TestCase;
/* $endif$ */
</pre>

Each platform port has a build-preprocess-tests.xml ant job that
uses the preprocessor to import common tests and tweak them for
the platform.






