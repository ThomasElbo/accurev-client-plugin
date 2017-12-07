package org.jenkinsci.plugins.accurevclient.utils

import ch.tutteli.atrium.api.cc.en_UK.toBe
import ch.tutteli.atrium.verbs.assert.assert
import org.junit.Test

class TimestampXmlAdapterTest {
    @Test
    fun marshalling() {
        val input: Long = 1512479158
        with(TimestampAdapter()) {
            val date = unmarshal(input)
            val timestamp = marshal(date)
            val secondDate = unmarshal(timestamp)
            assert(input).toBe(timestamp)
            assert(date).toBe(secondDate)
        }
    }
}
