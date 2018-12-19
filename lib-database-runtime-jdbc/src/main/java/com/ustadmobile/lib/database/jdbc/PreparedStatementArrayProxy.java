package com.ustadmobile.lib.database.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Some JDBC drivers do not support java.sql.Array . This proxy class is a workaround that will
 * generate a new PreparedStatement under the hood for each time it is invoked, and substitutes a
 * single array parameter ? for the number of elements in the array, and then sets them all
 * individually. This will not deliver the same performance, but it will execute the query.
 *
 * This class can be used roughly as follows:
 *
 * int[] myUids = new int[]{1,2,3};
 * Array myArray = PreparedStatementArrayProxy.createArrayOf("JDBCTYPE", myUids);
 * PreparedStatement preparedStmt = new PreparedStatementArrayProxy("SELECT * FROM TABLE WHERE UID in (?)", connection);
 * preparedStmt.setArray(1, myArray);
 * ResultSet result = preparedStmt.executeQuery();
 *
 */
public class PreparedStatementArrayProxy implements PreparedStatement {

    private Map<Integer, Object> queryParams = new TreeMap<>();

    private Map<Integer, Integer> queryTypes = new TreeMap<>();

    private String query;

    private Connection connection;

    private static class JdbcArrayProxy implements Array {

        private String typeName;

        private Object[] objects;

        private int baseType;

        public JdbcArrayProxy(String typeName, Object[] objects) {
            this.typeName = typeName;
            this.objects = objects;

            switch(typeName) {
                case "INTEGER":
                    baseType = Types.INTEGER;
                    break;
                case "VARCHAR":
                    baseType = Types.VARCHAR;
                    break;
                case "BIGINT":
                    baseType = Types.BIGINT;
                    break;
                case "SHORT":
                    baseType = Types.SMALLINT;
                    break;
                case "BOOLEAN":
                    baseType = Types.BOOLEAN;
                    break;
                case "TEXT":
                    baseType = Types.LONGVARCHAR;
                    break;



            }
        }

        protected Object[] getObjects() {
            return objects;
        }

        @Override
        public String getBaseTypeName() throws SQLException {
            return typeName;
        }

        @Override
        public int getBaseType() throws SQLException {
            return baseType;
        }

        @Override
        public Object getArray() throws SQLException {
            return this;
        }

        @Override
        public Object getArray(Map<String, Class<?>> map) throws SQLException {
            return null;
        }

        @Override
        public Object getArray(long l, int i) throws SQLException {
            return null;
        }

        @Override
        public Object getArray(long l, int i, Map<String, Class<?>> map) throws SQLException {
            return null;
        }

        @Override
        public ResultSet getResultSet() throws SQLException {
            return null;
        }

        @Override
        public ResultSet getResultSet(Map<String, Class<?>> map) throws SQLException {
            return null;
        }

        @Override
        public ResultSet getResultSet(long l, int i) throws SQLException {
            return null;
        }

        @Override
        public ResultSet getResultSet(long l, int i, Map<String, Class<?>> map) throws SQLException {
            return null;
        }

        @Override
        public void free() throws SQLException {

        }
    }


    /**
     * Create a new PreparedStatementArrayProxy
     *
     * @param query The query to execute (as per a normal PreparedStatement using ? for parameters)
     * @param connection the JDBC connection to run the query on
     */
    public PreparedStatementArrayProxy(String query, Connection connection) {
        this.query = query;
        this.connection = connection;
    }

    /**
     * Create a proxy array, using the same method parameters as a JDBC connection uses to create it.
     *
     * @param arrayType the JDBC data type e.g. "VARCHAR", "INTEGER", etc
     * @param objects The objects contained in the array
     *
     * @return A java.sql.Array object that can be used as a parameter with this class
     */
    public static Array createArrayOf(String arrayType, Object[] objects) {
        return new JdbcArrayProxy(arrayType, objects);
    }

    protected PreparedStatement prepareStatement() throws SQLException{
        int arrayOffset = 0;
        SortedMap<Integer, Object> paramValues = new TreeMap<>();
        SortedMap<Integer, Integer> paramTypes = new TreeMap<>();
        String adjustedQuery = query;
        for(int paramIndex : queryParams.keySet()) {
            Object value = queryParams.get(paramIndex);
            if(value instanceof Array) {
                JdbcArrayProxy arrayProxy = (JdbcArrayProxy)value;
                Object[] objects = arrayProxy.getObjects();
                int arrayParamPos = ordinalIndexOf(adjustedQuery, '?', paramIndex);
                adjustedQuery = adjustedQuery.substring(0, arrayParamPos) +
                        makeArrayPlaceholders(objects.length) + adjustedQuery.substring(arrayParamPos + 1);
                for(int i = 0; i < objects.length; i++) {
                    int paramPos = paramIndex + arrayOffset + i;
                    paramValues.put(paramPos, objects[i]);
                    paramTypes.put(paramPos, arrayProxy.getBaseType());
                }

                arrayOffset += (objects.length - 1);
            }else {
                paramValues.put(paramIndex + arrayOffset, value);
                paramTypes.put(paramIndex + arrayOffset, queryTypes.get(paramIndex));
            }
        }


        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(adjustedQuery);
            for(int paramIndex : paramValues.keySet()) {
                Object value = paramValues.get(paramIndex);
                switch(paramTypes.get(paramIndex)) {
                    case Types.INTEGER:
                        stmt.setInt(paramIndex, (Integer)value);
                        break;

                    case Types.BOOLEAN:
                        stmt.setBoolean(paramIndex, (Boolean)value);
                        break;

                    case Types.VARCHAR:
                    case Types.LONGVARCHAR:
                        stmt.setString(paramIndex, (String)value);
                        break;

                    case Types.BIGINT:
                        stmt.setLong(paramIndex, (Long)value);
                        break;

                    case Types.FLOAT:
                        stmt.setFloat(paramIndex, (Float)value);
                        break;




                }

            }


        }catch(SQLException e) {
            if(stmt != null) {
                stmt.close();
            }

            throw e;
        }

        return stmt;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        PreparedStatement stmt = prepareStatement();
        ResultSet resultSet = stmt.executeQuery();
        return new PreparedStatementResultSetWrapper(resultSet, stmt);
    }


    private String makeArrayPlaceholders(int numPlaceholders) {
        StringBuffer sb = new StringBuffer(Math.max(0, (2*numPlaceholders)-1));

        for(int i = 0; i < numPlaceholders; i++){
            if(i != 0)
                sb.append(',');

            sb.append('?');
        }

        return sb.toString();
    }

    private static int ordinalIndexOf(String str, char c, int n) {
        int pos = str.indexOf(c);
        while(--n > 0 && pos != -1)
            pos = str.indexOf(c, pos + 1);

        return pos;
    }

    @Override
    public void setNull(int i, int i1) throws SQLException {

    }

    @Override
    public void setBoolean(int i, boolean b) throws SQLException {
        queryParams.put(i, b);
        queryTypes.put(i, Types.BOOLEAN);
    }

    @Override
    public void setByte(int i, byte b) throws SQLException {
        queryParams.put(i, b);
        queryTypes.put(i, Types.SMALLINT);
    }

    @Override
    public void setShort(int i, short i1) throws SQLException {
        queryParams.put(i, i1);
        queryTypes.put(i, Types.SMALLINT);
    }

    @Override
    public void setInt(int i, int i1) throws SQLException {
        queryParams.put(i, i1);
        queryTypes.put(i, Types.INTEGER);
    }

    @Override
    public void setLong(int i, long l) throws SQLException {
        queryParams.put(i, l);
        queryTypes.put(i, Types.BIGINT);
    }

    @Override
    public void setFloat(int i, float v) throws SQLException {
        queryParams.put(i, v);
        queryTypes.put(i, Types.FLOAT);
    }

    @Override
    public void setDouble(int i, double v) throws SQLException {
        queryParams.put(i, v);
        queryTypes.put(i, Types.DOUBLE);
    }

    @Override
    public void setBigDecimal(int i, BigDecimal bigDecimal) throws SQLException {

    }

    @Override
    public void setString(int i, String s) throws SQLException {
        queryParams.put(i, s);
        queryTypes.put(i, Types.VARCHAR);
    }

    @Override
    public void setBytes(int i, byte[] bytes) throws SQLException {

    }

    @Override
    public void setDate(int i, Date date) throws SQLException {

    }

    @Override
    public void setTime(int i, Time time) throws SQLException {

    }

    @Override
    public void setTimestamp(int i, Timestamp timestamp) throws SQLException {

    }

    @Override
    public void setAsciiStream(int i, InputStream inputStream, int i1) throws SQLException {

    }

    @Override
    public void setUnicodeStream(int i, InputStream inputStream, int i1) throws SQLException {

    }

    @Override
    public void setBinaryStream(int i, InputStream inputStream, int i1) throws SQLException {

    }

    @Override
    public void clearParameters() throws SQLException {

    }

    @Override
    public void setObject(int i, Object o, int i1) throws SQLException {

    }

    @Override
    public void setObject(int i, Object o) throws SQLException {

    }

    @Override
    public boolean execute() throws SQLException {
        try (
                PreparedStatement stmt = prepareStatement();
        ) {
            return stmt.execute();
        }catch(SQLException e) {
            throw e;
        }
    }

    @Override
    public void addBatch() throws SQLException {

    }

    @Override
    public void setCharacterStream(int i, Reader reader, int i1) throws SQLException {

    }

    @Override
    public void setRef(int i, Ref ref) throws SQLException {

    }

    @Override
    public void setBlob(int i, Blob blob) throws SQLException {

    }

    @Override
    public void setClob(int i, Clob clob) throws SQLException {

    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return null;
    }

    @Override
    public void setDate(int i, Date date, Calendar calendar) throws SQLException {

    }

    @Override
    public void setTime(int i, Time time, Calendar calendar) throws SQLException {

    }

    @Override
    public void setTimestamp(int i, Timestamp timestamp, Calendar calendar) throws SQLException {

    }

    @Override
    public void setNull(int i, int i1, String s) throws SQLException {

    }

    @Override
    public void setURL(int i, URL url) throws SQLException {

    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return null;
    }

    @Override
    public void setRowId(int i, RowId rowId) throws SQLException {

    }

    @Override
    public void setNString(int i, String s) throws SQLException {

    }

    @Override
    public void setNCharacterStream(int i, Reader reader, long l) throws SQLException {

    }

    @Override
    public void setNClob(int i, NClob nClob) throws SQLException {

    }

    @Override
    public void setClob(int i, Reader reader, long l) throws SQLException {

    }

    @Override
    public void setBlob(int i, InputStream inputStream, long l) throws SQLException {

    }

    @Override
    public void setNClob(int i, Reader reader, long l) throws SQLException {

    }

    @Override
    public void setSQLXML(int i, SQLXML sqlxml) throws SQLException {

    }

    @Override
    public void setObject(int i, Object o, int i1, int i2) throws SQLException {

    }

    @Override
    public void setAsciiStream(int i, InputStream inputStream, long l) throws SQLException {

    }

    @Override
    public void setBinaryStream(int i, InputStream inputStream, long l) throws SQLException {

    }

    @Override
    public void setCharacterStream(int i, Reader reader, long l) throws SQLException {

    }

    @Override
    public void setAsciiStream(int i, InputStream inputStream) throws SQLException {

    }

    @Override
    public void setBinaryStream(int i, InputStream inputStream) throws SQLException {

    }

    @Override
    public void setCharacterStream(int i, Reader reader) throws SQLException {

    }

    @Override
    public void setNCharacterStream(int i, Reader reader) throws SQLException {

    }

    @Override
    public void setClob(int i, Reader reader) throws SQLException {

    }

    @Override
    public void setBlob(int i, InputStream inputStream) throws SQLException {

    }

    @Override
    public void setNClob(int i, Reader reader) throws SQLException {

    }

    @Override
    public ResultSet executeQuery(String s) throws SQLException {
        return null;
    }

    @Override
    public int executeUpdate(String s) throws SQLException {
        return 0;
    }

    @Override
    public void close() throws SQLException {

    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return 0;
    }

    @Override
    public void setMaxFieldSize(int i) throws SQLException {

    }

    @Override
    public int getMaxRows() throws SQLException {
        return 0;
    }

    @Override
    public void setMaxRows(int i) throws SQLException {

    }

    @Override
    public void setEscapeProcessing(boolean b) throws SQLException {

    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return 0;
    }

    @Override
    public void setQueryTimeout(int i) throws SQLException {

    }

    @Override
    public void cancel() throws SQLException {

    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public void setCursorName(String s) throws SQLException {

    }

    @Override
    public boolean execute(String s) throws SQLException {
        return false;
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return null;
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return 0;
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return false;
    }

    @Override
    public void setFetchDirection(int i) throws SQLException {

    }

    @Override
    public int getFetchDirection() throws SQLException {
        return 0;
    }

    @Override
    public void setFetchSize(int i) throws SQLException {

    }

    @Override
    public int getFetchSize() throws SQLException {
        return 0;
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return 0;
    }

    @Override
    public int getResultSetType() throws SQLException {
        return 0;
    }

    @Override
    public void addBatch(String s) throws SQLException {

    }

    @Override
    public void clearBatch() throws SQLException {

    }

    @Override
    public int[] executeBatch() throws SQLException {
        return new int[0];
    }

    @Override
    public Connection getConnection() throws SQLException {
        return connection;
    }

    @Override
    public boolean getMoreResults(int i) throws SQLException {
        return false;
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return null;
    }

    @Override
    public int executeUpdate(String s, int i) throws SQLException {
        return 0;
    }

    @Override
    public int executeUpdate(String s, int[] ints) throws SQLException {
        return 0;
    }

    @Override
    public int executeUpdate(String s, String[] strings) throws SQLException {
        return 0;
    }

    @Override
    public boolean execute(String s, int i) throws SQLException {
        return false;
    }

    @Override
    public boolean execute(String s, int[] ints) throws SQLException {
        return false;
    }

    @Override
    public boolean execute(String s, String[] strings) throws SQLException {
        return false;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return 0;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    @Override
    public void setPoolable(boolean b) throws SQLException {

    }

    @Override
    public boolean isPoolable() throws SQLException {
        return false;
    }

    @Override
    public void closeOnCompletion() throws SQLException {

    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> aClass) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
        return false;
    }

    @Override
    public void setArray(int i, Array array) throws SQLException {
        queryParams.put(i, array);
        queryTypes.put(i, Types.ARRAY);
    }



    @Override
    public int executeUpdate() throws SQLException {
        try (PreparedStatement stmt = prepareStatement(); ) {
            return stmt.executeUpdate();
        }catch(SQLException e) {
            throw e;
        }
    }
}
