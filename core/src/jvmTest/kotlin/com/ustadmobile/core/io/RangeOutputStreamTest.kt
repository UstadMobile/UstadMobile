package com.ustadmobile.core.io

import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class RangeOutputStreamTest {

    private fun copyThroughRangeOutputStream(dataIn: ByteArray, from: Long, to: Long) : ByteArray {
        val bout = ByteArrayOutputStream()
        val rangeOutputStream = RangeOutputStream(bout, from.toLong(), to.toLong())
        val bin = ByteArrayInputStream(dataIn)
        bin.copyTo(rangeOutputStream)
        rangeOutputStream.flush()
        rangeOutputStream.close()

        return bout.toByteArray()
    }

    private fun makeInputData(size: Int) : ByteArray {
        return (1 .. size).map { (it and Byte.MAX_VALUE.toInt()).toByte() }.toByteArray()
    }


    @Test
    fun givenStreamData_whenWrittenWithOutputRangeInMiddleOfFirstWrite_thenOutputMatches() {
        val inputData = makeInputData(20000)
        val from = 5000
        val to = 5999


        val outputData = copyThroughRangeOutputStream(inputData, from.toLong(), to.toLong())

        //slice array to is exclusive. Ours is inclusive as per http range requests
        val expectedOutput = inputData.sliceArray(IntRange(from, to))

        Assert.assertArrayEquals("Output data matches expected", expectedOutput, outputData)
    }

    @Test
    fun givenStreamData_whenWrittenWithOutputRangeInMiddle_thenOutputMatches() {
        val inputData = makeInputData(20000)
        val from = 10000
        val to = 13000

        val outputData  = copyThroughRangeOutputStream(inputData, from.toLong(), to.toLong())
        val expectedOutput = inputData.sliceArray(IntRange(from, to))
        Assert.assertArrayEquals("Output data matches expected", expectedOutput, outputData)
    }

    @Test
    fun givenStreamData_whenWrittenWithoutEnd_thenOutputMatches() {
        val inputData = makeInputData(20000)
        val from = 10000
        val to = -1

        val outputData  = copyThroughRangeOutputStream(inputData, from.toLong(), to.toLong())
        val expectedOutput = inputData.sliceArray(IntRange(from, 19999))
        Assert.assertArrayEquals("Output data matches expected", expectedOutput, outputData)
    }

    @Test
    fun givenTwoStreams_whenConcatenated_thenMatchesOriginal() {
        val inputData = makeInputData(20000)
        val part1 = copyThroughRangeOutputStream(inputData, 0, 9999)
        val part2 = copyThroughRangeOutputStream(inputData, 10000, -1)
        Assert.assertArrayEquals("Data combined is equal to input data",
                inputData, part1 + part2)
    }

    @Test(expected = IllegalArgumentException::class)
    fun givenStartAfterEnd_whenConstructed_thenShouldThrowException() {
        RangeOutputStream(ByteArrayOutputStream(), 1000, 500)
    }



}