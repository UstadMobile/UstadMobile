package com.ustadmobile.lib.database.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/**
 * This class is used by PreparedStatementArrayProxy to ensure that when the ResultSet is closed,
 * the generated PreparedStatement is also closed
 */
class PreparedStatementResultSetWrapper implements ResultSet {

    private ResultSet resultSet;

    private PreparedStatement stmt;

    PreparedStatementResultSetWrapper(ResultSet resultSet, PreparedStatement stmt) {
        this.resultSet = resultSet;
        this.stmt = stmt;
    }

    @Override
    public boolean next() throws SQLException {
        return resultSet.next();
    }

    @Override
    public void close() throws SQLException {
        try {
            if(!resultSet.isClosed())
                resultSet.close();
        }catch(SQLException e) {
            throw e;
        }finally {
            if(!stmt.getConnection().isClosed() && !stmt.isClosed())
                stmt.close();
        }
    }

    @Override
    public boolean wasNull() throws SQLException {
        return resultSet.wasNull();
    }

    @Override
    public String getString(int i) throws SQLException {
        return resultSet.getString(i);
    }

    @Override
    public boolean getBoolean(int i) throws SQLException {
        return resultSet.getBoolean(i);
    }

    @Override
    public byte getByte(int i) throws SQLException {
        return resultSet.getByte(i);
    }

    @Override
    public short getShort(int i) throws SQLException {
        return resultSet.getShort(i);
    }

    @Override
    public int getInt(int i) throws SQLException {
        return resultSet.getInt(i);
    }

    @Override
    public long getLong(int i) throws SQLException {
        return resultSet.getLong(i);
    }

    @Override
    public float getFloat(int i) throws SQLException {
        return resultSet.getFloat(i);
    }

    @Override
    public double getDouble(int i) throws SQLException {
        return resultSet.getDouble(i);
    }

    @Override
    @Deprecated
    public BigDecimal getBigDecimal(int i, int i1) throws SQLException {
        return resultSet.getBigDecimal(i, i1);
    }

    @Override
    public byte[] getBytes(int i) throws SQLException {
        return resultSet.getBytes(i);
    }

    @Override
    public Date getDate(int i) throws SQLException {
        return resultSet.getDate(i);
    }

    @Override
    public Time getTime(int i) throws SQLException {
        return resultSet.getTime(i);
    }

    @Override
    public Timestamp getTimestamp(int i) throws SQLException {
        return resultSet.getTimestamp(i);
    }

    @Override
    public InputStream getAsciiStream(int i) throws SQLException {
        return resultSet.getAsciiStream(i);
    }

    @Override
    @Deprecated
    public InputStream getUnicodeStream(int i) throws SQLException {
        return resultSet.getUnicodeStream(i);
    }

    @Override
    public InputStream getBinaryStream(int i) throws SQLException {
        return resultSet.getBinaryStream(i);
    }

    @Override
    public String getString(String s) throws SQLException {
        return resultSet.getString(s);
    }

    @Override
    public boolean getBoolean(String s) throws SQLException {
        return resultSet.getBoolean(s);
    }

    @Override
    public byte getByte(String s) throws SQLException {
        return resultSet.getByte(s);
    }

    @Override
    public short getShort(String s) throws SQLException {
        return resultSet.getShort(s);
    }

    @Override
    public int getInt(String s) throws SQLException {
        return resultSet.getInt(s);
    }

    @Override
    public long getLong(String s) throws SQLException {
        return resultSet.getLong(s);
    }

    @Override
    public float getFloat(String s) throws SQLException {
        return resultSet.getFloat(s);
    }

    @Override
    public double getDouble(String s) throws SQLException {
        return resultSet.getDouble(s);
    }

    @Override
    @Deprecated
    public BigDecimal getBigDecimal(String s, int i) throws SQLException {
        return resultSet.getBigDecimal(s, i);
    }

    @Override
    public byte[] getBytes(String s) throws SQLException {
        return resultSet.getBytes(s);
    }

    @Override
    public Date getDate(String s) throws SQLException {
        return resultSet.getDate(s);
    }

    @Override
    public Time getTime(String s) throws SQLException {
        return resultSet.getTime(s);
    }

    @Override
    public Timestamp getTimestamp(String s) throws SQLException {
        return resultSet.getTimestamp(s);
    }

    @Override
    public InputStream getAsciiStream(String s) throws SQLException {
        return resultSet.getAsciiStream(s);
    }

    @Override
    @Deprecated
    public InputStream getUnicodeStream(String s) throws SQLException {
        return resultSet.getUnicodeStream(s);
    }

    @Override
    public InputStream getBinaryStream(String s) throws SQLException {
        return resultSet.getBinaryStream(s);
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return resultSet.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        resultSet.clearWarnings();
    }

    @Override
    public String getCursorName() throws SQLException {
        return resultSet.getCursorName();
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return resultSet.getMetaData();
    }

    @Override
    public Object getObject(int i) throws SQLException {
        return resultSet.getObject(i);
    }

    @Override
    public Object getObject(String s) throws SQLException {
        return resultSet.getObject(s);
    }

    @Override
    public int findColumn(String s) throws SQLException {
        return resultSet.findColumn(s);
    }

    @Override
    public Reader getCharacterStream(int i) throws SQLException {
        return resultSet.getCharacterStream(i);
    }

    @Override
    public Reader getCharacterStream(String s) throws SQLException {
        return resultSet.getCharacterStream(s);
    }

    @Override
    public BigDecimal getBigDecimal(int i) throws SQLException {
        return resultSet.getBigDecimal(i);
    }

    @Override
    public BigDecimal getBigDecimal(String s) throws SQLException {
        return resultSet.getBigDecimal(s);
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        return resultSet.isBeforeFirst();
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        return resultSet.isAfterLast();
    }

    @Override
    public boolean isFirst() throws SQLException {
        return resultSet.isFirst();
    }

    @Override
    public boolean isLast() throws SQLException {
        return resultSet.isLast();
    }

    @Override
    public void beforeFirst() throws SQLException {
        resultSet.beforeFirst();
    }

    @Override
    public void afterLast() throws SQLException {
        resultSet.afterLast();
    }

    @Override
    public boolean first() throws SQLException {
        return resultSet.first();
    }

    @Override
    public boolean last() throws SQLException {
        return resultSet.last();
    }

    @Override
    public int getRow() throws SQLException {
        return resultSet.getRow();
    }

    @Override
    public boolean absolute(int i) throws SQLException {
        return resultSet.absolute(i);
    }

    @Override
    public boolean relative(int i) throws SQLException {
        return resultSet.relative(i);
    }

    @Override
    public boolean previous() throws SQLException {
        return resultSet.previous();
    }

    @Override
    public void setFetchDirection(int i) throws SQLException {
        resultSet.setFetchDirection(i);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return resultSet.getFetchDirection();
    }

    @Override
    public void setFetchSize(int i) throws SQLException {
        resultSet.setFetchSize(i);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return resultSet.getFetchSize();
    }

    @Override
    public int getType() throws SQLException {
        return resultSet.getType();
    }

    @Override
    public int getConcurrency() throws SQLException {
        return resultSet.getConcurrency();
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        return resultSet.rowUpdated();
    }

    @Override
    public boolean rowInserted() throws SQLException {
        return resultSet.rowInserted();
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        return resultSet.rowDeleted();
    }

    @Override
    public void updateNull(int i) throws SQLException {
        resultSet.updateNull(i);
    }

    @Override
    public void updateBoolean(int i, boolean b) throws SQLException {
        resultSet.updateBoolean(i, b);
    }

    @Override
    public void updateByte(int i, byte b) throws SQLException {
        resultSet.updateByte(i, b);
    }

    @Override
    public void updateShort(int i, short i1) throws SQLException {
        resultSet.updateShort(i, i1);
    }

    @Override
    public void updateInt(int i, int i1) throws SQLException {
        resultSet.updateInt(i, i1);
    }

    @Override
    public void updateLong(int i, long l) throws SQLException {
        resultSet.updateLong(i, l);
    }

    @Override
    public void updateFloat(int i, float v) throws SQLException {
        resultSet.updateFloat(i, v);
    }

    @Override
    public void updateDouble(int i, double v) throws SQLException {
        resultSet.updateDouble(i, v);
    }

    @Override
    public void updateBigDecimal(int i, BigDecimal bigDecimal) throws SQLException {
        resultSet.updateBigDecimal(i, bigDecimal);
    }

    @Override
    public void updateString(int i, String s) throws SQLException {
        resultSet.updateString(i, s);
    }

    @Override
    public void updateBytes(int i, byte[] bytes) throws SQLException {
        resultSet.updateBytes(i, bytes);
    }

    @Override
    public void updateDate(int i, Date date) throws SQLException {
        resultSet.updateDate(i, date);
    }

    @Override
    public void updateTime(int i, Time time) throws SQLException {
        resultSet.updateTime(i, time);
    }

    @Override
    public void updateTimestamp(int i, Timestamp timestamp) throws SQLException {
        resultSet.updateTimestamp(i, timestamp);
    }

    @Override
    public void updateAsciiStream(int i, InputStream inputStream, int i1) throws SQLException {
        resultSet.updateAsciiStream(i, inputStream, i1);
    }

    @Override
    public void updateBinaryStream(int i, InputStream inputStream, int i1) throws SQLException {
        resultSet.updateBinaryStream(i, inputStream, i1);
    }

    @Override
    public void updateCharacterStream(int i, Reader reader, int i1) throws SQLException {
        resultSet.updateCharacterStream(i, reader, i1);
    }

    @Override
    public void updateObject(int i, Object o, int i1) throws SQLException {
        resultSet.updateObject(i, o, i1);
    }

    @Override
    public void updateObject(int i, Object o) throws SQLException {
        resultSet.updateObject(i, o);
    }

    @Override
    public void updateNull(String s) throws SQLException {
        resultSet.updateNull(s);
    }

    @Override
    public void updateBoolean(String s, boolean b) throws SQLException {
        resultSet.updateBoolean(s, b);
    }

    @Override
    public void updateByte(String s, byte b) throws SQLException {
        resultSet.updateByte(s, b);
    }

    @Override
    public void updateShort(String s, short i) throws SQLException {
        resultSet.updateShort(s, i);
    }

    @Override
    public void updateInt(String s, int i) throws SQLException {
        resultSet.updateInt(s, i);
    }

    @Override
    public void updateLong(String s, long l) throws SQLException {
        resultSet.updateLong(s, l);
    }

    @Override
    public void updateFloat(String s, float v) throws SQLException {
        resultSet.updateFloat(s, v);
    }

    @Override
    public void updateDouble(String s, double v) throws SQLException {
        resultSet.updateDouble(s, v);
    }

    @Override
    public void updateBigDecimal(String s, BigDecimal bigDecimal) throws SQLException {
        resultSet.updateBigDecimal(s, bigDecimal);
    }

    @Override
    public void updateString(String s, String s1) throws SQLException {
        resultSet.updateString(s, s1);
    }

    @Override
    public void updateBytes(String s, byte[] bytes) throws SQLException {
        resultSet.updateBytes(s, bytes);
    }

    @Override
    public void updateDate(String s, Date date) throws SQLException {
        resultSet.updateDate(s, date);
    }

    @Override
    public void updateTime(String s, Time time) throws SQLException {
        resultSet.updateTime(s, time);
    }

    @Override
    public void updateTimestamp(String s, Timestamp timestamp) throws SQLException {
        resultSet.updateTimestamp(s, timestamp);
    }

    @Override
    public void updateAsciiStream(String s, InputStream inputStream, int i) throws SQLException {
        resultSet.updateAsciiStream(s, inputStream, i);
    }

    @Override
    public void updateBinaryStream(String s, InputStream inputStream, int i) throws SQLException {
        resultSet.updateBinaryStream(s, inputStream, i);
    }

    @Override
    public void updateCharacterStream(String s, Reader reader, int i) throws SQLException {
        resultSet.updateCharacterStream(s, reader, i);
    }

    @Override
    public void updateObject(String s, Object o, int i) throws SQLException {
        resultSet.updateObject(s, o, i);
    }

    @Override
    public void updateObject(String s, Object o) throws SQLException {
        resultSet.updateObject(s, o);
    }

    @Override
    public void insertRow() throws SQLException {
        resultSet.insertRow();
    }

    @Override
    public void updateRow() throws SQLException {
        resultSet.updateRow();
    }

    @Override
    public void deleteRow() throws SQLException {
        resultSet.deleteRow();
    }

    @Override
    public void refreshRow() throws SQLException {
        resultSet.refreshRow();
    }

    @Override
    public void cancelRowUpdates() throws SQLException {
        resultSet.cancelRowUpdates();
    }

    @Override
    public void moveToInsertRow() throws SQLException {
        resultSet.moveToInsertRow();
    }

    @Override
    public void moveToCurrentRow() throws SQLException {
        resultSet.moveToCurrentRow();
    }

    @Override
    public Statement getStatement() throws SQLException {
        return resultSet.getStatement();
    }

    @Override
    public Object getObject(int i, Map<String, Class<?>> map) throws SQLException {
        return resultSet.getObject(i, map);
    }

    @Override
    public Ref getRef(int i) throws SQLException {
        return resultSet.getRef(i);
    }

    @Override
    public Blob getBlob(int i) throws SQLException {
        return resultSet.getBlob(i);
    }

    @Override
    public Clob getClob(int i) throws SQLException {
        return resultSet.getClob(i);
    }

    @Override
    public Array getArray(int i) throws SQLException {
        return resultSet.getArray(i);
    }

    @Override
    public Object getObject(String s, Map<String, Class<?>> map) throws SQLException {
        return resultSet.getObject(s, map);
    }

    @Override
    public Ref getRef(String s) throws SQLException {
        return resultSet.getRef(s);
    }

    @Override
    public Blob getBlob(String s) throws SQLException {
        return resultSet.getBlob(s);
    }

    @Override
    public Clob getClob(String s) throws SQLException {
        return resultSet.getClob(s);
    }

    @Override
    public Array getArray(String s) throws SQLException {
        return resultSet.getArray(s);
    }

    @Override
    public Date getDate(int i, Calendar calendar) throws SQLException {
        return resultSet.getDate(i, calendar);
    }

    @Override
    public Date getDate(String s, Calendar calendar) throws SQLException {
        return resultSet.getDate(s, calendar);
    }

    @Override
    public Time getTime(int i, Calendar calendar) throws SQLException {
        return resultSet.getTime(i, calendar);
    }

    @Override
    public Time getTime(String s, Calendar calendar) throws SQLException {
        return resultSet.getTime(s, calendar);
    }

    @Override
    public Timestamp getTimestamp(int i, Calendar calendar) throws SQLException {
        return resultSet.getTimestamp(i, calendar);
    }

    @Override
    public Timestamp getTimestamp(String s, Calendar calendar) throws SQLException {
        return resultSet.getTimestamp(s, calendar);
    }

    @Override
    public URL getURL(int i) throws SQLException {
        return resultSet.getURL(i);
    }

    @Override
    public URL getURL(String s) throws SQLException {
        return resultSet.getURL(s);
    }

    @Override
    public void updateRef(int i, Ref ref) throws SQLException {
        resultSet.updateRef(i, ref);
    }

    @Override
    public void updateRef(String s, Ref ref) throws SQLException {
        resultSet.updateRef(s, ref);
    }

    @Override
    public void updateBlob(int i, Blob blob) throws SQLException {
        resultSet.updateBlob(i, blob);
    }

    @Override
    public void updateBlob(String s, Blob blob) throws SQLException {
        resultSet.updateBlob(s, blob);
    }

    @Override
    public void updateClob(int i, Clob clob) throws SQLException {
        resultSet.updateClob(i, clob);
    }

    @Override
    public void updateClob(String s, Clob clob) throws SQLException {
        resultSet.updateClob(s, clob);
    }

    @Override
    public void updateArray(int i, Array array) throws SQLException {
        resultSet.updateArray(i, array);
    }

    @Override
    public void updateArray(String s, Array array) throws SQLException {
        resultSet.updateArray(s, array);
    }

    @Override
    public RowId getRowId(int i) throws SQLException {
        return resultSet.getRowId(i);
    }

    @Override
    public RowId getRowId(String s) throws SQLException {
        return resultSet.getRowId(s);
    }

    @Override
    public void updateRowId(int i, RowId rowId) throws SQLException {
        resultSet.updateRowId(i, rowId);
    }

    @Override
    public void updateRowId(String s, RowId rowId) throws SQLException {
        resultSet.updateRowId(s, rowId);
    }

    @Override
    public int getHoldability() throws SQLException {
        return resultSet.getHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return resultSet.isClosed();
    }

    @Override
    public void updateNString(int i, String s) throws SQLException {
        resultSet.updateNString(i, s);
    }

    @Override
    public void updateNString(String s, String s1) throws SQLException {
        resultSet.updateNString(s, s1);
    }

    @Override
    public void updateNClob(int i, NClob nClob) throws SQLException {
        resultSet.updateNClob(i, nClob);
    }

    @Override
    public void updateNClob(String s, NClob nClob) throws SQLException {
        resultSet.updateNClob(s, nClob);
    }

    @Override
    public NClob getNClob(int i) throws SQLException {
        return resultSet.getNClob(i);
    }

    @Override
    public NClob getNClob(String s) throws SQLException {
        return resultSet.getNClob(s);
    }

    @Override
    public SQLXML getSQLXML(int i) throws SQLException {
        return resultSet.getSQLXML(i);
    }

    @Override
    public SQLXML getSQLXML(String s) throws SQLException {
        return resultSet.getSQLXML(s);
    }

    @Override
    public void updateSQLXML(int i, SQLXML sqlxml) throws SQLException {
        resultSet.updateSQLXML(i, sqlxml);
    }

    @Override
    public void updateSQLXML(String s, SQLXML sqlxml) throws SQLException {
        resultSet.updateSQLXML(s, sqlxml);
    }

    @Override
    public String getNString(int i) throws SQLException {
        return resultSet.getNString(i);
    }

    @Override
    public String getNString(String s) throws SQLException {
        return resultSet.getNString(s);
    }

    @Override
    public Reader getNCharacterStream(int i) throws SQLException {
        return resultSet.getNCharacterStream(i);
    }

    @Override
    public Reader getNCharacterStream(String s) throws SQLException {
        return resultSet.getNCharacterStream(s);
    }

    @Override
    public void updateNCharacterStream(int i, Reader reader, long l) throws SQLException {
        resultSet.updateNCharacterStream(i, reader, l);
    }

    @Override
    public void updateNCharacterStream(String s, Reader reader, long l) throws SQLException {
        resultSet.updateNCharacterStream(s, reader, l);
    }

    @Override
    public void updateAsciiStream(int i, InputStream inputStream, long l) throws SQLException {
        resultSet.updateAsciiStream(i, inputStream, l);
    }

    @Override
    public void updateBinaryStream(int i, InputStream inputStream, long l) throws SQLException {
        resultSet.updateBinaryStream(i, inputStream, l);
    }

    @Override
    public void updateCharacterStream(int i, Reader reader, long l) throws SQLException {
        resultSet.updateCharacterStream(i, reader, l);
    }

    @Override
    public void updateAsciiStream(String s, InputStream inputStream, long l) throws SQLException {
        resultSet.updateAsciiStream(s, inputStream, l);
    }

    @Override
    public void updateBinaryStream(String s, InputStream inputStream, long l) throws SQLException {
        resultSet.updateBinaryStream(s, inputStream, l);
    }

    @Override
    public void updateCharacterStream(String s, Reader reader, long l) throws SQLException {
        resultSet.updateCharacterStream(s, reader, l);
    }

    @Override
    public void updateBlob(int i, InputStream inputStream, long l) throws SQLException {
        resultSet.updateBlob(i, inputStream, l);
    }

    @Override
    public void updateBlob(String s, InputStream inputStream, long l) throws SQLException {
        resultSet.updateBlob(s, inputStream, l);
    }

    @Override
    public void updateClob(int i, Reader reader, long l) throws SQLException {
        resultSet.updateClob(i, reader, l);
    }

    @Override
    public void updateClob(String s, Reader reader, long l) throws SQLException {
        resultSet.updateClob(s, reader, l);
    }

    @Override
    public void updateNClob(int i, Reader reader, long l) throws SQLException {
        resultSet.updateNClob(i, reader, l);
    }

    @Override
    public void updateNClob(String s, Reader reader, long l) throws SQLException {
        resultSet.updateNClob(s, reader, l);
    }

    @Override
    public void updateNCharacterStream(int i, Reader reader) throws SQLException {
        resultSet.updateNCharacterStream(i, reader);
    }

    @Override
    public void updateNCharacterStream(String s, Reader reader) throws SQLException {
        resultSet.updateNCharacterStream(s, reader);
    }

    @Override
    public void updateAsciiStream(int i, InputStream inputStream) throws SQLException {
        resultSet.updateAsciiStream(i, inputStream);
    }

    @Override
    public void updateBinaryStream(int i, InputStream inputStream) throws SQLException {
        resultSet.updateBinaryStream(i, inputStream);
    }

    @Override
    public void updateCharacterStream(int i, Reader reader) throws SQLException {
        resultSet.updateCharacterStream(i, reader);
    }

    @Override
    public void updateAsciiStream(String s, InputStream inputStream) throws SQLException {
        resultSet.updateAsciiStream(s, inputStream);
    }

    @Override
    public void updateBinaryStream(String s, InputStream inputStream) throws SQLException {
        resultSet.updateBinaryStream(s, inputStream);
    }

    @Override
    public void updateCharacterStream(String s, Reader reader) throws SQLException {
        resultSet.updateCharacterStream(s, reader);
    }

    @Override
    public void updateBlob(int i, InputStream inputStream) throws SQLException {
        resultSet.updateBlob(i, inputStream);
    }

    @Override
    public void updateBlob(String s, InputStream inputStream) throws SQLException {
        resultSet.updateBlob(s, inputStream);
    }

    @Override
    public void updateClob(int i, Reader reader) throws SQLException {
        resultSet.updateClob(i, reader);
    }

    @Override
    public void updateClob(String s, Reader reader) throws SQLException {
        resultSet.updateClob(s, reader);
    }

    @Override
    public void updateNClob(int i, Reader reader) throws SQLException {
        resultSet.updateNClob(i, reader);
    }

    @Override
    public void updateNClob(String s, Reader reader) throws SQLException {
        resultSet.updateNClob(s, reader);
    }

    @Override
    public <T> T getObject(int i, Class<T> aClass) throws SQLException {
        return resultSet.getObject(i, aClass);
    }

    @Override
    public <T> T getObject(String s, Class<T> aClass) throws SQLException {
        return resultSet.getObject(s, aClass);
    }

    @Override
    public void updateObject(int i, Object o, SQLType sqlType, int i1) throws SQLException {
        resultSet.updateObject(i, o, sqlType, i1);
    }

    @Override
    public void updateObject(String s, Object o, SQLType sqlType, int i) throws SQLException {
        resultSet.updateObject(s, o, sqlType, i);
    }

    @Override
    public void updateObject(int i, Object o, SQLType sqlType) throws SQLException {
        resultSet.updateObject(i, o, sqlType);
    }

    @Override
    public void updateObject(String s, Object o, SQLType sqlType) throws SQLException {
        resultSet.updateObject(s, o, sqlType);
    }

    @Override
    public <T> T unwrap(Class<T> aClass) throws SQLException {
        return resultSet.unwrap(aClass);
    }

    @Override
    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
        return resultSet.isWrapperFor(aClass);
    }
}
