package jenkins.plugins.accurevclient

import ch.tutteli.atrium.api.cc.en_UK.hasSize
import ch.tutteli.atrium.api.cc.en_UK.isFalse
import ch.tutteli.atrium.api.cc.en_UK.isNotNull
import ch.tutteli.atrium.api.cc.en_UK.isNull
import ch.tutteli.atrium.api.cc.en_UK.isTrue
import ch.tutteli.atrium.api.cc.en_UK.it
import ch.tutteli.atrium.api.cc.en_UK.property
import ch.tutteli.atrium.api.cc.en_UK.toBe
import ch.tutteli.atrium.verbs.expect.expect
import jenkins.plugins.accurevclient.model.Depots
import jenkins.plugins.accurevclient.model.Stream
import jenkins.plugins.accurevclient.model.StreamType
import jenkins.plugins.accurevclient.model.Streams
import jenkins.plugins.accurevclient.model.TransactionType
import jenkins.plugins.accurevclient.model.Transactions
import jenkins.plugins.accurevclient.model.Update
import jenkins.plugins.accurevclient.model.Version
import jenkins.plugins.accurevclient.utils.TimestampAdapter
import jenkins.plugins.accurevclient.utils.unmarshal
import org.junit.Test
import java.util.Date

class AccurevModelTest {
    private val timestampAdapter = TimestampAdapter()

    @Test fun depotModel() {
        val xml = this.javaClass.getResourceAsStream("depots.xml")

        xml.use { input ->
            val depots = input.unmarshal() as Depots
            println(depots)
            expect(depots) {
                property(it::elements).hasSize(2)
            }
            expect(depots.elements[0]) {
                property(it::number).toBe("1")
                property(it::name).toBe("accurev")
            }
        }
    }

    @Test fun streamModel() {
        val xml = this.javaClass.getResourceAsStream("streams.xml")

        xml.use { input ->
            val output = input.unmarshal() as Streams
            println(output)
            expect(output) {
                property(it::streams).hasSize(4)
            }
            expect(output.streams[0]) {
                property(it::name).toBe("accurev")
                property(it::depotName).toBe("accurev")
                property(it::streamNumber).toBe(1)
                property(it::basisStreamNumber).isNull()
                property(it::dynamic).isTrue()
                property(it::startTime).toBe(timestampAdapter.unmarshal(1512169249))
                property(it::type).toBe(StreamType.Normal)
            }
            expect(output.streams[1]) {
                property(it::name).toBe("accurev_josp")
                property(it::depotName).toBe("accurev")
                property(it::streamNumber).toBe(2)
                property(it::basisStreamNumber).isNotNull { toBe(1) }
                property(it::dynamic).isFalse()
                property(it::startTime).toBe(timestampAdapter.unmarshal(1512169250))
                property(it::type).toBe(StreamType.Workspace)
            }
        }
    }

    @Test fun transactionModel() {
        val xml = this.javaClass.getResourceAsStream("hist.xml")

        xml.use { input ->
            val output = input.unmarshal() as Transactions
            println(output)
            expect(output.transactions[0]) {
                property(it::comment).toBe("c")
                property(it::id).toBe(13)
                property(it::type).toBe(TransactionType.Promote)
                property(it::user).toBe("josp")
                property(it::time).toBe(timestampAdapter.unmarshal(1512907647))
                property(it::version).isNotNull { toBe(Version("bud", 3)) }
                property(it::stream).isNull()
            }
            expect(output.transactions[3]) {
                property(it::comment).toBe("")
                property(it::id).toBe(6)
                property(it::type).toBe(TransactionType.MakeStream)
                property(it::user).toBe("josp")
                property(it::time).toBe(timestampAdapter.unmarshal(1512907076))
                property(it::version).isNull()
                property(it::stream).isNotNull { toBe(Stream("other_stream", "accurev", 3, 1, false, StreamType.Normal, Date(0))) }
            }
        }
    }

    @Test fun updateModel() {
        val xml = this.javaClass.getResourceAsStream("update.xml")

        xml.use { input ->
            val output = input.unmarshal() as Update
            println(output)
            expect(output.elements[0]) {
                property(it::path).toBe("doubleDAMN")
            }
        }
    }
}
