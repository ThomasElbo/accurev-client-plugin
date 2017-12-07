package org.jenkinsci.plugins.accurevclient

import ch.tutteli.atrium.api.cc.en_UK.hasSize
import ch.tutteli.atrium.api.cc.en_UK.isFalse
import ch.tutteli.atrium.api.cc.en_UK.isNotNull
import ch.tutteli.atrium.api.cc.en_UK.isNull
import ch.tutteli.atrium.api.cc.en_UK.isTrue
import ch.tutteli.atrium.api.cc.en_UK.it
import ch.tutteli.atrium.api.cc.en_UK.property
import ch.tutteli.atrium.api.cc.en_UK.toBe
import ch.tutteli.atrium.verbs.assert.assert
import org.jenkinsci.plugins.accurevclient.model.Depots
import org.jenkinsci.plugins.accurevclient.model.StreamType
import org.jenkinsci.plugins.accurevclient.model.Streams
import org.jenkinsci.plugins.accurevclient.utils.TimestampAdapter
import org.jenkinsci.plugins.accurevclient.utils.unmarshal
import org.junit.Test

class AccurevModelTest {
    @Test fun DepotModel() {
        val xml = this.javaClass.getResourceAsStream("depots.xml")

        xml.use { input ->
            val depots = input.unmarshal() as Depots
            println(depots)
            assert(depots) {
                property(it::elements).hasSize(2)
            }
            assert(depots.elements[0]) {
                property(it::number).toBe("1")
                property(it::name).toBe("accurev")
            }
        }
    }

    @Test fun StreamModel() {
        val xml = this.javaClass.getResourceAsStream("streams.xml")
        val timestampAdapter = TimestampAdapter()

        xml.use { input ->
            val output = input.unmarshal() as Streams
            println(output)
            assert(output) {
                property(it::streams).hasSize(4)
            }
            assert(output.streams[0]) {
                property(it::name).toBe("accurev")
                property(it::depotName).toBe("accurev")
                property(it::streamNumber).toBe(1)
                property(it::basisStreamNumber).isNull()
                property(it::dynamic).isTrue()
                property(it::startTime).toBe(timestampAdapter.unmarshal(1512169249))
                property(it::type).toBe(StreamType.NORMAL)
            }
            assert(output.streams[1]) {
                property(it::name).toBe("accurev_josp")
                property(it::depotName).toBe("accurev")
                property(it::streamNumber).toBe(2)
                property(it::basisStreamNumber).isNotNull { toBe(1) }
                property(it::dynamic).isFalse()
                property(it::startTime).toBe(timestampAdapter.unmarshal(1512169250))
                property(it::type).toBe(StreamType.WORKSPACE)
            }
        }
    }
}
