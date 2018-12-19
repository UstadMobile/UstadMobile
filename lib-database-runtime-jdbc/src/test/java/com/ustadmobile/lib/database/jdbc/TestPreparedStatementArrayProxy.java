package com.ustadmobile.lib.database.jdbc;

import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestPreparedStatementArrayProxy {

    @Test
    public void givenQueryWithArrays_whenExecuted_thenShouldSubstituteArrayParams() throws SQLException {
        Object[] array = new Object[]{1, 2, 3, 4};

        Array sqlArray = PreparedStatementArrayProxy.createArrayOf("INTEGER", array);
        Connection mockConnection = Mockito.mock(Connection.class);
        PreparedStatement mockPreparedStatement = Mockito.mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(any())).thenReturn(mockPreparedStatement);
        PreparedStatementArrayProxy proxy = new PreparedStatementArrayProxy(
                "SELECT * FROM TABLE WHERE ID = ? AND CATEGORY IN (?)", mockConnection);
        proxy.setInt(1, 42);
        proxy.setArray(2, sqlArray);
        proxy.prepareStatement();

        verify(mockPreparedStatement).setInt(1, 42);
        verify(mockPreparedStatement).setInt(2, 1);
        verify(mockPreparedStatement).setInt(3, 2);
        verify(mockPreparedStatement).setInt(4, 3);
        verify(mockPreparedStatement).setInt(5, 4);
    }

    @Test
    public void givenQueryWithEmptyArray_whenExecuted_shouldSubstituteParams() throws SQLException {
        Object[] array = new Object[]{};

        Array sqlArray = PreparedStatementArrayProxy.createArrayOf("INTEGER", array);
        Connection mockConnection = Mockito.mock(Connection.class);
        PreparedStatement mockPreparedStatement = Mockito.mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(any())).thenReturn(mockPreparedStatement);
        PreparedStatementArrayProxy proxy = new PreparedStatementArrayProxy(
                "SELECT * FROM TABLE WHERE ID = ? AND CATEGORY IN (?) AND SIZE > ?", mockConnection);
        proxy.setInt(1, 42);
        proxy.setArray(2, sqlArray);
        proxy.setInt(3, 43);
        proxy.prepareStatement();

        verify(mockPreparedStatement).setInt(1, 42);
        verify(mockPreparedStatement).setInt(2, 43);
    }

    @Test
    public void givenQueryWithParamsAfterArray_whenExecuted_shouldShiftParams() throws SQLException{
        Object[] array = new Object[]{1, 2};

        Array sqlArray = PreparedStatementArrayProxy.createArrayOf("INTEGER", array);
        Connection mockConnection = Mockito.mock(Connection.class);
        PreparedStatement mockPreparedStatement = Mockito.mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(any())).thenReturn(mockPreparedStatement);
        PreparedStatementArrayProxy proxy = new PreparedStatementArrayProxy(
                "SELECT * FROM TABLE WHERE ID = ? AND CATEGORY IN (?) AND SIZE > ?", mockConnection);
        proxy.setInt(1, 42);
        proxy.setArray(2, sqlArray);
        proxy.setInt(3, 43);
        proxy.prepareStatement();

        verify(mockPreparedStatement).setInt(1, 42);
        verify(mockPreparedStatement).setInt(2, 1);
        verify(mockPreparedStatement).setInt(3, 2);
        verify(mockPreparedStatement).setInt(4, 43);

    }

}
