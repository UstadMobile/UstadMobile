/*
 * Copyright 2006 Sun Microsystems, Inc.
 */

package org.json.me;

import java.io.IOException;
import java.io.Writer;

/**
 * A simple StringBuffer-based implementation of StringWriter
 */
public class StringWriter extends Writer {
    
    final private StringBuffer buf;
    
    public StringWriter() {
        super();
        buf = new StringBuffer();
    }
    
    public StringWriter(int initialSize) {
        super();
        buf = new StringBuffer(initialSize);
    }
    
    public void write(char[] cbuf, int off, int len) throws IOException {
        buf.append(cbuf, off, len);
    }

    public void write(String str) throws IOException {
        buf.append(str);
    }

    public void write(String str, int off, int len) throws IOException {
        buf.append(str.substring(off, len));
    }

    public void flush() throws IOException {
    }

    public void close() throws IOException {
    }
}
