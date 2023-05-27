package com.example.simplemusic

import org.junit.Assert
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        Assert.assertEquals(4, (2 + 2).toLong())
    }

    @Test
    fun sublist() {
        val list = mutableListOf<Int>(1, 2, 3, 4, 5)
        val dropped = list.drop(2)
        Assert.assertEquals(dropped, mutableListOf(3, 4, 5))
        // list won't change after drop
        Assert.assertEquals(
            list, mutableListOf(1, 2, 3, 4, 5)
        )
        // a view of the original list
        val sub = list.subList(0, 3)
        Assert.assertEquals(sub, mutableListOf(1, 2, 3))
        // list won't change after subList
        Assert.assertEquals(
            list, mutableListOf(1, 2, 3, 4, 5)
        )
        sub.clear()
        // list also has been changed after sublist clear
        Assert.assertEquals(
            list, mutableListOf(4, 5)
        )
    }
}