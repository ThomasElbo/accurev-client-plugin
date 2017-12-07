package org.jenkinsci.plugins.accurevclient.utils

import ch.tutteli.atrium.api.cc.en_UK.isNotNull
import ch.tutteli.atrium.api.cc.en_UK.toBe
import ch.tutteli.atrium.verbs.expect.expect
import org.junit.Test
import java.util.Date

class TimestampXmlAdapterTest {
    @Test
    fun marshalling() {
        val input: Long = 1512479158
        with(TimestampAdapter()) {
            val date = unmarshal(input)
            val timestamp = marshal(date)
            val secondDate = unmarshal(timestamp)
            expect(input).toBe(timestamp)
            expect(date).toBe(secondDate)
        }
    }

    @Test
    fun nullMarshalling() {
        with(TimestampAdapter()) {
            val date: Date? = unmarshal(null)
            expect(date).isNotNull { }
            val timestamp: Long? = marshal(null)
            expect(timestamp).isNotNull { }
        }
    }
}
