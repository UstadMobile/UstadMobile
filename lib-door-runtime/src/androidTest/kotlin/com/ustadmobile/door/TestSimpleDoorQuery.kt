package com.ustadmobile.door

import androidx.sqlite.db.SupportSQLiteProgram
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class TestSimpleDoorQuery {

    private lateinit var mockSqliteProgram: SupportSQLiteProgram

    @Before
    fun setup(){
        mockSqliteProgram = mock { }
    }


    @Test
    fun givenASqlWith2Args_whenListWithOnlyOneArg_thenArgCountShouldNotChange() {
        val sql = "SELECT * FROM Blha WHERE list IN (?) AND array In (?)"


        val expected = "SELECT * FROM Blha WHERE list IN (?) AND array In (?)"

        val xList = listOf(100L)
        val params = mutableListOf<Any>()
        params.addAll(listOf(xList))
        params.addAll(listOf(xList))


        val query = SimpleDoorQueryImpl(sql, params.toTypedArray())
        query.bindTo(mockSqliteProgram)

        Assert.assertEquals("Arg count matches", 2, query.argCount)
        Assert.assertEquals("sql matches", expected, query.sql)
        verify(mockSqliteProgram).bindLong(1, 100L)
        verify(mockSqliteProgram).bindLong(2, 100L)
    }

    @Test
    fun givenASqlWith2Args_whenListWith2Arg_thenArgCountBe4() {

        val sql = "SELECT * FROM Blha WHERE list IN (?) AND array In (?)"

        val expected = "SELECT * FROM Blha WHERE list IN (?,?) AND array In (?,?)"


        val xList = listOf(100L, 101L)

        val params = mutableListOf<Any>()
        params.addAll(listOf(xList))
        params.addAll(listOf(xList))

        val query = SimpleDoorQueryImpl(sql, params.toTypedArray())
        query.bindTo(mockSqliteProgram)
        Assert.assertEquals("Arg count matches", 4, query.argCount)
        Assert.assertEquals("sql matches", expected, query.sql)
        verify(mockSqliteProgram).bindLong(1, 100L)
        verify(mockSqliteProgram).bindLong(2, 101L)
        verify(mockSqliteProgram).bindLong(3, 100L)
        verify(mockSqliteProgram).bindLong(4, 101L)

    }


    @Test
    fun givenASqlWith2ArgsAndALong_whenListWith2Arg_thenArgCountBe5() {

        val sql = "SELECT * FROM Blha WHERE  list IN (?) AND x = ? AND array In (?)"

        val expected = "SELECT * FROM Blha WHERE  list IN (?,?) AND x = ? AND array In (?,?)"


        val xList = listOf(100L, 101L)

        val params = mutableListOf<Any>()
        params.addAll(listOf(xList))
        params.add(1L)
        params.addAll(listOf(xList))

        val query = SimpleDoorQueryImpl(sql, params.toTypedArray())
        query.bindTo(mockSqliteProgram)
        Assert.assertEquals("Arg count matches", 5, query.argCount)
        Assert.assertEquals("sql matches", expected, query.sql)
        verify(mockSqliteProgram).bindLong(1, 100L)
        verify(mockSqliteProgram).bindLong(2, 101L)
        verify(mockSqliteProgram).bindLong(3, 1L)
        verify(mockSqliteProgram).bindLong(4, 100L)
        verify(mockSqliteProgram).bindLong(5, 101L)

    }

}